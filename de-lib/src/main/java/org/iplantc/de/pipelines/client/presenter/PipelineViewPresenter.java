package org.iplantc.de.pipelines.client.presenter;

import org.iplantc.de.apps.client.AppsView;
import org.iplantc.de.apps.client.events.AppCategoryCountUpdateEvent;
import org.iplantc.de.apps.client.events.AppSavedEvent;
import org.iplantc.de.client.events.EventBus;
import org.iplantc.de.client.gin.ServicesInjector;
import org.iplantc.de.client.models.apps.App;
import org.iplantc.de.client.models.pipelines.Pipeline;
import org.iplantc.de.client.models.pipelines.PipelineAppMapping;
import org.iplantc.de.client.models.pipelines.PipelineTask;
import org.iplantc.de.commons.client.ErrorHandler;
import org.iplantc.de.commons.client.info.ErrorAnnouncementConfig;
import org.iplantc.de.commons.client.info.IplantAnnouncer;
import org.iplantc.de.commons.client.presenter.Presenter;
import org.iplantc.de.pipelines.client.dnd.AppsGridDragHandler;
import org.iplantc.de.pipelines.client.dnd.PipelineBuilderDNDHandler;
import org.iplantc.de.pipelines.client.dnd.PipelineBuilderDropHandler;
import org.iplantc.de.pipelines.client.util.PipelineAutoBeanUtil;
import org.iplantc.de.pipelines.client.views.AppSelectionDialog;
import org.iplantc.de.pipelines.client.views.PipelineAppMappingView;
import org.iplantc.de.pipelines.client.views.PipelineAppOrderView;
import org.iplantc.de.pipelines.client.views.PipelineView;
import org.iplantc.de.pipelines.client.views.widgets.PipelineViewToolbar;
import org.iplantc.de.pipelines.client.views.widgets.PipelineViewToolbarImpl;
import org.iplantc.de.resources.client.messages.I18N;
import org.iplantc.de.shared.AppsCallback;

import com.google.common.base.Strings;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.autobean.shared.Splittable;

import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.dnd.core.client.DND.Operation;
import com.sencha.gxt.dnd.core.client.DragSource;
import com.sencha.gxt.dnd.core.client.DropTarget;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.container.Container;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.error.DefaultEditorError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Presenter for the Pipeline View.
 * 
 * @author psarando
 * 
 */
public class PipelineViewPresenter implements Presenter, PipelineView.Presenter, PipelineViewToolbar.Presenter, PipelineBuilderDNDHandler.Presenter, PipelineAppOrderView.Presenter,
        PipelineAppMappingView.Presenter, AppSelectionDialog.Presenter {

    private final class PipelineSaveCallback extends AppsCallback<String> {
        private final Pipeline pipeline;

        private PipelineSaveCallback(Pipeline pipeline) {
            this.pipeline = pipeline;
        }

        @Override
        public void onSuccess(String result) {

            String newId = utils.parseServiceSaveResponseId(result);

            if (!Strings.isNullOrEmpty(newId) && !newId.equals(pipeline.getId())) {
                pipeline.setId(newId);
                loadPipeline(pipeline);

                AppCategoryCountUpdateEvent event = new AppCategoryCountUpdateEvent(true, null);
                EventBus.getInstance().fireEvent(event);
                AppSavedEvent aevent = new AppSavedEvent(null);
                EventBus.getInstance().fireEvent(aevent);
            }

            toolbar.setPublishButtonEnabled(true);

            if (onPublishCallback != null) {
                onPublishCallback.execute();
            }
        }

        @Override
        public void onFailure(Integer statusCode, Throwable caught) {
            ErrorHandler.post(I18N.ERROR.workflowPublishError(), caught);
            toolbar.setPublishButtonEnabled(true);
        }
    }

    private final PipelineView view;
    private final PipelineViewToolbar toolbar;
    private AppsView.Presenter appsPresenter;
    private AppSelectionDialog appSelectView;
    private final Command onPublishCallback;
    private final PipelineAutoBeanUtil utils = new PipelineAutoBeanUtil();

    public PipelineViewPresenter(PipelineView view, Command onPublishCallback,
                                 AppsView.Presenter appsPresenter) {
        this.view = view;
        this.onPublishCallback = onPublishCallback;
        this.appsPresenter = appsPresenter;

        toolbar = new PipelineViewToolbarImpl();

        view.setPresenter(this);
        view.getAppOrderPanel().setPresenter(this);
        view.getMappingPanel().setPresenter(this);
        toolbar.setPresenter(this);

        view.setNorthWidget(toolbar);

        initAppsView();

        // Set an initial Pipeline to edit.
        Pipeline pipeline = utils.getPipelineFactory().pipeline().as();
        setPipeline(pipeline);
    }

    private void initAppsView() {
        appSelectView = new AppSelectionDialog();
        appSelectView.setPresenter(this);

        initAppsDragHandlers(appsPresenter.getAppsDragSources());
        initPipelineBuilderDropHandler(view.getBuilderDropContainer());

        // TODO Possibly inject with annotation to replace with a different toolbar impl
        appsPresenter.hideAppMenu().hideWorkflowMenu().go(appSelectView, null, null);
    }

    private void initAppsDragHandlers(List<DragSource> sources) {
        AppsGridDragHandler handler = new AppsGridDragHandler();
        handler.setPresenter(this);

        for (DragSource source : sources) {
            source.addDragStartHandler(handler);
            source.addDragCancelHandler(handler);
        }
    }

    private void initPipelineBuilderDropHandler(Container builderPanel) {
        PipelineBuilderDropHandler handler = new PipelineBuilderDropHandler();
        handler.setPresenter(this);

        DropTarget target = new DropTarget(builderPanel);
        target.setOperation(Operation.COPY);
        target.addDragEnterHandler(handler);
        target.addDragLeaveHandler(handler);
        target.addDragCancelHandler(handler);
        target.addDropHandler(handler);
    }

    @Override
    public void setPipeline(Pipeline pipeline) {
        if (pipeline == null) {
            pipeline = utils.getPipelineFactory().pipeline().as();
        }
        view.setPipeline(pipeline);
    }

    @Override
    public void setPipeline(Splittable serviceWorkflowJson) {
        Pipeline pipeline = null;
        if (serviceWorkflowJson != null) {
            pipeline = utils.serviceJsonToPipeline(serviceWorkflowJson);
        }
        setPipeline(pipeline);
    }

    @Override
    public void go(HasOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void onPublishClicked() {
        if (view.getActiveView() == view.getBuilderPanel()) {
            if (isValidJson(getPipeline())) {
                publishPipeline();
            }

        } else if (view.isValid()) {
            publishPipeline();
        } else {
            markErrors(true);
        }
    }

    @Override
    public void saveOnClose() {
        onPublishClicked();
    }

    private boolean isValidJson(Pipeline pipeline) {
        List<EditorError> errorList = new ArrayList<>();
        if (Strings.isNullOrEmpty(pipeline.getName()) || pipeline.getName().equalsIgnoreCase("Click to edit name")) {
            errorList.add(new DefaultEditorError(null, "Name is required.", null));
        }

        if (Strings.isNullOrEmpty(pipeline.getDescription()) || pipeline.getDescription().equalsIgnoreCase("Click to edit description")) {
            errorList.add(new DefaultEditorError(null, "Description is required.", null));
        }

        if (pipeline.getApps() == null || pipeline.getApps().size() < 2) {
            errorList.add(new DefaultEditorError(null, I18N.DISPLAY.selectOrderPnlTip(), null));
        } else {
            List<PipelineTask> apps = pipeline.getApps();
            for (PipelineTask app : apps) {
                if (!isMappingValid(app)) {
                    errorList.add(new DefaultEditorError(null, I18N.DISPLAY.inputsOutputsPnlTip(), null));
                    break;
                }
            }
        }

        showErrors(errorList);
        return errorList.isEmpty();
    }

    private void showErrors(List<EditorError> errorList) {
        if (errorList == null || errorList.size() == 0) {
            return;
        }

        Dialog d = new Dialog();
        d.setModal(true);
        d.setHeading(I18N.DISPLAY.error());
        VerticalLayoutContainer vlc = new VerticalLayoutContainer();

        d.setWidget(vlc);
        HTML html = new HTML(formatErrors(errorList));
        vlc.add(html);
        d.setSize("300", "175");
        d.setHideOnButtonClick(true);
        d.show();
    }

    private SafeHtml formatErrors(List<EditorError> errorList) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendEscapedLines("Please fix the following errors:");
        for (EditorError err : errorList) {
            // SS - this is ugly fix to display field names in the error message
            if (err.getMessage().equalsIgnoreCase("This field is required")) {
                builder.appendHtmlConstant("<p>*&nbsp;<span style='color:red;'>" + "Name / Description field is required." + "</span> </p>");
            } else {
                builder.appendHtmlConstant("<p>*&nbsp;<span style='color:red;'>" + err.getMessage() + "</span> </p>");
            }
        }
        return builder.toSafeHtml();
    }

    @Override
    public String getPublishJson(Pipeline pipeline) {
        String publishJson = utils.getPublishJson(pipeline);
        return publishJson;
    }

    private void publishPipeline() {
        toolbar.setPublishButtonEnabled(false);
        view.markInfoBtnValid();
        view.markAppOrderBtnValid();
        view.markMappingBtnValid();

        final Pipeline pipeline = getPipeline();
        String publishJson = utils.getPublishJson(pipeline);
        if (publishJson == null) {
            ErrorHandler.post(I18N.ERROR.workflowPublishError());
            toolbar.setPublishButtonEnabled(true);
            return;
        }

        // create pipeline
        if (Strings.isNullOrEmpty(pipeline.getId())) {
            ServicesInjector.INSTANCE.getAppUserServiceFacade()
                                     .createWorkflows(publishJson, new PipelineSaveCallback(pipeline));
        } else {
            // update existing pipeline
        ServicesInjector.INSTANCE.getAppUserServiceFacade().publishWorkflow(pipeline.getId(),
                                                                            publishJson,
                                                                            new PipelineSaveCallback(pipeline));
        }
    }

    private void markErrors(boolean showErrDialog) {
        view.markInfoBtnValid();
        view.markAppOrderBtnValid();
        view.markMappingBtnValid();

        List<EditorError> errors = view.getErrors();
        if (errors != null) {
            for (EditorError err : errors) {
                String path = err.getAbsolutePath();
                if ("name".equals(path) || "description".equals(path)) { //$NON-NLS-1$ //$NON-NLS-2$
                    view.markInfoBtnInvalid(err.getMessage());
                } else if (err.getUserData() == view.getAppOrderPanel()) {
                    view.markAppOrderBtnInvalid(err.getMessage());
                } else if (err.getUserData() == view.getMappingPanel()) {
                    view.markMappingBtnInvalid(err.getMessage());
                }
            }
            if (showErrDialog) {
                showErrors(errors);
            }
        }
    }

    @Override
    public void onSwapViewClicked() {
        view.clearInvalid();

        IsWidget activeView = view.getActiveView();
        Pipeline pipeline = getPipeline();

        if (activeView == view.getStepEditorPanel()) {
            activeView = view.getBuilderPanel();
            appsPresenter.go(view.getAppsContainer(), null, null);
        } else {
            activeView = view.getStepEditorPanel();
            appsPresenter.go(appSelectView, null, null);
        }

        view.setActiveView(activeView);

        loadPipeline(pipeline);
    }

    private void reconfigurePipelineAppMappingView(int startingStep, List<PipelineTask> apps) {
        if (apps != null) {
            for (PipelineTask app : apps) {
                if (app.getStep() >= startingStep) {
                    utils.resetAppMappings(app);
                }
            }
        }

        view.getMappingPanel().setValue(apps);
    }

    @Override
    public Pipeline getPipeline() {
        if (view.getActiveView() == view.getBuilderPanel()) {
            return view.getPipelineCreator().getPipeline();
        }

        return view.getPipeline();
    }

    private void loadPipeline(Pipeline pipeline) {
        if (pipeline != null) {
            if (view.getActiveView() == view.getBuilderPanel()) {
                view.getPipelineCreator().loadPipeline(pipeline);
            } else {
                view.setPipeline(pipeline);
            }
        }

    }

    @Override
    public void onInfoClick() {
        view.getStepPanel().setActiveWidget(view.getInfoPanel());
        view.getHelpContainer().setHTML(I18N.DISPLAY.infoPnlTip());
        updateErrors();
    }

    @Override
    public void onAppOrderClick() {
        view.getStepPanel().setActiveWidget(view.getAppOrderPanel());
        view.getHelpContainer().setHTML(I18N.DISPLAY.selectOrderPnlTip());
        updateErrors();
    }

    @Override
    public void onMappingClick() {
        view.getStepPanel().setActiveWidget(view.getMappingPanel());
        view.getHelpContainer().setHTML(I18N.DISPLAY.inputsOutputsPnlTip());
        updateErrors();
    }

    private void updateErrors() {
        view.isValid();
        markErrors(false);
    }

    @Override
    public void onAddAppsClicked() {
        appSelectView.show();
    }

    @Override
    public void onMoveUpClicked() {
        PipelineTask selectedApp = view.getOrderGridSelectedApp();
        if (selectedApp == null) {
            return;
        }

        ListStore<PipelineTask> store = view.getPipelineAppStore();

        int selectedStep = selectedApp.getStep();
        if (selectedStep > 0) {
            int stepUp = selectedStep - 1;
            PipelineTask prevApp = store.get(stepUp);
            prevApp.setStep(selectedStep);
            selectedApp.setStep(stepUp);

            store.update(selectedApp);
            store.update(prevApp);

            store.applySort(false);

            reconfigurePipelineAppMappingView(stepUp, store.getAll());
        }
    }

    @Override
    public void onMoveDownClicked() {
        PipelineTask selectedApp = view.getOrderGridSelectedApp();
        if (selectedApp == null) {
            return;
        }

        ListStore<PipelineTask> store = view.getPipelineAppStore();

        int selectedStep = selectedApp.getStep();
        if (selectedStep < store.size() - 1) {
            int stepDown = selectedStep + 1;
            PipelineTask nextApp = store.get(stepDown);
            nextApp.setStep(selectedStep);
            selectedApp.setStep(stepDown);

            store.update(selectedApp);
            store.update(nextApp);

            store.applySort(false);

            reconfigurePipelineAppMappingView(stepDown, store.getAll());
        }
    }

    @Override
    public void onRemoveAppClicked() {
        PipelineTask selectedApp = view.getOrderGridSelectedApp();

        if (selectedApp != null) {
            ListStore<PipelineTask> store = view.getPipelineAppStore();

            store.remove(selectedApp);

            for (int step = 1; step <= store.size(); step++) {
                PipelineTask app = store.get(step - 1);
                app.setStep(step);
                store.update(app);
            }

            reconfigurePipelineAppMappingView(selectedApp.getStep(), store.getAll());
        }
    }

    @Override
    public void onAddAppClick() {
        App selectedApp = appsPresenter.getSelectedApp();
        if (!selectedApp.isDisabled()) {
            utils.appToPipelineApp(selectedApp, new AsyncCallback<PipelineTask>() {

                @Override
                public void onSuccess(PipelineTask result) {
                    if (result != null) {
                        ListStore<PipelineTask> store = view.getPipelineAppStore();

                        result.setStep(store.size());
                        store.add(result);

                        appSelectView.updateStatusBar(store.size(), result.getName());

                        view.getMappingPanel().setValue(store.getAll());
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    SafeHtmlBuilder builder = new SafeHtmlBuilder();
                    builder.appendEscaped("Error adding app to workflow:" + caught.getMessage());
                    ErrorAnnouncementConfig config = new ErrorAnnouncementConfig(builder.toSafeHtml(), true);
                    IplantAnnouncer.getInstance().schedule(config);
                }
            });
        } else {
            IplantAnnouncer.getInstance().schedule(new ErrorAnnouncementConfig("Cannot add disabled App to workflow!"));
        }

    }

    @Override
    public void addAppToPipeline(final App app) {
        utils.appToPipelineApp(app, new AsyncCallback<PipelineTask>() {

            @Override
            public void onSuccess(PipelineTask result) {
                if (result != null) {
                    view.getPipelineCreator().appendApp(result);
                    unmaskPipelineBuilder();
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                unmaskPipelineBuilder();
            }
        });
    }

    @Override
    public void maskPipelineBuilder(String message) {
        view.getBuilderDropContainer().mask(message);
    }

    @Override
    public void unmaskPipelineBuilder() {
        view.getBuilderDropContainer().unmask();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStepName(PipelineTask app) {
        return app.getStep() + "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInputOutputMapping(PipelineTask targetStep, String targetInputId, PipelineTask sourceStep, String sourceOutputId) {
        utils.setInputOutputMapping(targetStep, targetInputId, sourceStep, sourceOutputId);
    }

    @Override
    public boolean isMappingValid(PipelineTask targetStep) {
        if (targetStep == null) {
            return false;
        }

        // Each app after the first one should have at least one output-to-input mapping.
        if (targetStep.getStep() > 0) {
            List<PipelineAppMapping> mappings = targetStep.getMappings();
            if (mappings == null || mappings.size() < 1) {
                return false;
            }

            for (PipelineAppMapping mapping : mappings) {
                Map<String, String> map = mapping.getMap();

                if (map == null || map.keySet().isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }
}
