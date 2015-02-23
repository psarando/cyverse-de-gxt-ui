package org.iplantc.de.apps.client.views;

import org.iplantc.de.apps.client.SubmitAppForPublicUseView;
import org.iplantc.de.client.models.DEProperties;
import org.iplantc.de.client.models.apps.App;
import org.iplantc.de.client.models.apps.AppAutoBeanFactory;
import org.iplantc.de.client.models.apps.AppCategory;
import org.iplantc.de.client.models.apps.AppRefLink;
import org.iplantc.de.client.models.diskResources.Folder;
import org.iplantc.de.commons.client.validators.UrlValidator;
import org.iplantc.de.commons.client.widgets.ContextualHelpPopup;
import org.iplantc.de.diskResource.client.gin.factory.DiskResourceSelectorFieldFactory;
import org.iplantc.de.diskResource.client.views.widgets.FolderSelectorField;
import org.iplantc.de.resources.client.IplantResources;
import org.iplantc.de.resources.client.messages.I18N;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.container.HtmlLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent.CompleteEditHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.CellSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.selection.CellSelection;
import com.sencha.gxt.widget.core.client.selection.CellSelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.CellSelectionChangedEvent.CellSelectionChangedHandler;
import com.sencha.gxt.widget.core.client.tree.Tree;
import com.sencha.gxt.widget.core.client.tree.Tree.CheckCascade;
import com.sencha.gxt.widget.core.client.tree.Tree.CheckNodes;
import com.sencha.gxt.widget.core.client.tree.Tree.TreeAppearance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * A form that enables the user to make an app public
 * 
 * @author sriram
 * 
 */
public class SubmitAppForPublicUseViewImpl implements SubmitAppForPublicUseView {

    private static SubmitAppForPublicUseViewUiBinder uiBinder = GWT.create(SubmitAppForPublicUseViewUiBinder.class);

    final private Widget widget;

    @UiField
    TextField appName;

    @UiField
    TextArea appDesc;

    @UiField
    VerticalLayoutContainer container;

    @UiField(provided = true)
    TreeStore<AppCategory> treeStore;

    @UiField(provided = true)
    Tree<AppCategory, String> tree;

    @UiField
    Grid<AppRefLink> grid;

    @UiField
    ListStore<AppRefLink> listStore;

    @UiField
    TextButton addBtn;

    @UiField
    TextButton delBtn;

    @UiField
    ContentPanel catPanel;

    @UiField
    ContentPanel refPanel;

    @UiField
    FieldLabel appfield;

    @UiField
    FieldLabel descfield;

    @UiField
    HtmlLayoutContainer intro;

    @UiField
    FieldLabel descInputfield;

    @UiField
    FieldLabel descParamfield;

    @UiField
    FieldLabel descOutputfield;

    @UiField
    TextArea inputDesc;

    @UiField
    TextArea paramDesc;

    @UiField
    TextArea outputDesc;

    @UiField
    HTML testDataLbl;

    private GridEditing<AppRefLink> editing;

    private App selectedApp;

    private final DEProperties deprops;

    @UiField(provided = true)
    final FolderSelectorField dataFolderSelector;

    Logger LOG = Logger.getLogger("Submit Application for Public Use");

    @UiTemplate("SubmitAppForPublicUseView.ui.xml")
    interface SubmitAppForPublicUseViewUiBinder extends UiBinder<Widget, SubmitAppForPublicUseViewImpl> {
    }

    @Inject
    public SubmitAppForPublicUseViewImpl(final DiskResourceSelectorFieldFactory folderSelectorFieldFactory,
                                         final DEProperties props) {
        this.dataFolderSelector = folderSelectorFieldFactory.defaultFolderSelector();
        this.deprops = props;
        initCategoryTree();
        widget = uiBinder.createAndBindUi(this);
        initGrid();
        container.setScrollMode(ScrollMode.AUTOY);
        initFieldLabels();
        dataFolderSelector.addValueChangeHandler(new ValueChangeHandler<Folder>() {

            @Override
            public void onValueChange(ValueChangeEvent<Folder> event) {
                validate();

            }
        });
        addhelp();
    }

    @UiFactory
    HtmlLayoutContainer buildIntroContainer() {
        return new HtmlLayoutContainer(I18N.DISPLAY.submitForPublicUseIntro());
    }

    @Override
    public void loadReferences(List<AppRefLink> refs) {
        listStore.clear();
        listStore.addAll(refs);
    }

    private void initGrid() {
        CellSelectionModel<AppRefLink> csm = buildRefCellSelecitonModel();
        grid.setSelectionModel(csm);
        editing = createGridEditing();
        ColumnConfig<AppRefLink, String> cc = grid.getColumnModel().getColumn(0);
        final TextField editor = buildRefLinkEditor();
        editing.addEditor(cc, editor);
        editing.addCompleteEditHandler(new CompleteEditHandler<AppRefLink>() {

            @Override
            public void onCompleteEdit(CompleteEditEvent<AppRefLink> event) {
                String refLink = editor.getCurrentValue();
                AppRefLink ref = listStore.get(0);
                ref.setRefLink(refLink);
                listStore.update(ref);
            }
        });
        grid.getView().setAutoExpandColumn(cc);
    }

    private String buildRequiredFieldLabel(String label) {
        if (label == null) {
            return null;
        }

        return "<span style='color:red; top:-5px;' >*</span> " + label; //$NON-NLS-1$
    }

    private void addhelp() {
        final ToolButton tool_help_ref = new ToolButton(ToolButton.QUESTION);
        refPanel.getHeader().addTool(tool_help_ref);
        tool_help_ref.addSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                ContextualHelpPopup popup = new ContextualHelpPopup();
                popup.add(new HTML(I18N.HELP.publicSubmissionFormAttach()));
                popup.showAt(tool_help_ref.getAbsoluteLeft(), tool_help_ref.getAbsoluteTop() + 15);

            }
        });
        final ToolButton tool_help_cat = new ToolButton(ToolButton.QUESTION);
        catPanel.getHeader().addTool(tool_help_cat);
        tool_help_cat.addSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                ContextualHelpPopup popup = new ContextualHelpPopup();
                popup.add(new HTML(I18N.HELP.publicSubmissionFormCategories()));
                popup.showAt(tool_help_cat.getAbsoluteLeft(), tool_help_cat.getAbsoluteTop() + 15);

            }
        });
    }

    private void initFieldLabels() {
        appfield.setHTML(buildRequiredFieldLabel(I18N.DISPLAY.publicName()));
        descfield.setHTML(buildRequiredFieldLabel(I18N.DISPLAY.publicDescription()));
        refPanel.setHeadingHtml(I18N.DISPLAY.publicAttach());

        catPanel.setHeadingHtml(buildRequiredFieldLabel(I18N.DISPLAY.publicCategories()));
        descInputfield.setHTML(buildRequiredFieldLabel(I18N.DISPLAY.describeInputLbl()));
        descOutputfield.setHTML(buildRequiredFieldLabel(I18N.DISPLAY.describeOutputLbl()));
        descParamfield.setHTML(buildRequiredFieldLabel(I18N.DISPLAY.describeParamLbl()));
        testDataLbl.setHTML(buildRequiredFieldLabel(testDataLbl.getHTML()));
    }

    private CellSelectionModel<AppRefLink> buildRefCellSelecitonModel() {
        CellSelectionModel<AppRefLink> csm = new CellSelectionModel<>();
        csm.addCellSelectionChangedHandler(new CellSelectionChangedHandler<AppRefLink>() {
            @Override
            public void onCellSelectionChanged(CellSelectionChangedEvent<AppRefLink> event) {
                List<CellSelection<AppRefLink>> list = event.getSelection();
                if (list.size() > 0) {
                    if (list.get(0).getModel() == null) {
                        delBtn.disable();
                    } else {
                        delBtn.enable();
                    }
                } else {
                    delBtn.disable();
                }

            }
        });
        return csm;
    }

    private void initCategoryTree() {
        treeStore = new TreeStore<>(new ModelKeyProvider<AppCategory>() {

            @Override
            public String getKey(AppCategory item) {
                if (item == null) {
                    return null;
                }
                return item.getId();
            }
        });

        initTreeStoreSorter();

        tree = new Tree<>(treeStore, new ValueProvider<AppCategory, String>() {

            @Override
            public String getValue(AppCategory object) {
                return object.getName();
            }

            @Override
            public void setValue(AppCategory object, String value) {
                // do nothing intentionally
            }

            @Override
            public String getPath() {
                return null;
            }
        });

        setTreeIcons();
        tree.setCheckable(true);
        tree.setCheckStyle(CheckCascade.CHILDREN);
        tree.setCheckNodes(CheckNodes.LEAF);
    }

    private void initTreeStoreSorter() {
        Comparator<AppCategory> comparator = new Comparator<AppCategory>() {

            @Override
            public int compare(AppCategory group1, AppCategory group2) {
                return group1.getName().compareToIgnoreCase(group2.getName());
            }
        };

        treeStore.addSortInfo(new StoreSortInfo<>(comparator, SortDir.ASC));
    }

    @UiFactory
    ListStore<AppRefLink> buildRefLinksStore() {
        return new ListStore<>(new ModelKeyProvider<AppRefLink>() {

            @Override
            public String getKey(AppRefLink item) {
                if (item == null) {
                    return null;
                }
                return item.getId();
            }
        });

    }

    @UiFactory
    ColumnModel<AppRefLink> buildRefLinkColumn() {
        List<ColumnConfig<AppRefLink, ?>> columns = new ArrayList<>();

        ColumnConfig<AppRefLink, String> link = new ColumnConfig<>(new ValueProvider<AppRefLink, String>() {

                                                                       @Override
                                                                       public String
                                                                               getValue(AppRefLink object) {
                                                                           return object.getRefLink();
                                                                       }

                                                                       @Override
                                                                       public void
                                                                               setValue(AppRefLink object,
                                                                                        String value) {
                                                                           // do nothing

                                                                       }

                                                                       @Override
                                                                       public String getPath() {

                                                                           return null;
                                                                       }
                                                                   },
                                                                   250,
                                                                   I18N.DISPLAY.links());

        link.setHideable(false);
        link.setMenuDisabled(true);
        columns.add(link);
        return new ColumnModel<>(columns);
    }

    private TextField buildRefLinkEditor() {
        TextField field = new TextField();
        field.addValidator(new UrlValidator());
        field.setAllowBlank(false);
        return field;
    }

    /**
     * FIXME JDS This needs to be implemented in an {@link TreeAppearance}
     */
    private void setTreeIcons() {
        com.sencha.gxt.widget.core.client.tree.TreeStyle style = tree.getStyle();
        style.setNodeCloseIcon(IplantResources.RESOURCES.category());
        style.setNodeOpenIcon(IplantResources.RESOURCES.category_open());
        style.setLeafIcon(IplantResources.RESOURCES.subCategory());
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @UiHandler("addBtn")
    public void addClicked(SelectEvent event) {
        AppAutoBeanFactory factory = GWT.create(AppAutoBeanFactory.class);
        AutoBean<AppRefLink> bean = AutoBeanCodex.decode(factory, AppRefLink.class, "{}");
        editing.cancelEditing();
        AppRefLink link = bean.as();
        link.setId(new Date().getTime() + "");
        listStore.add(0, link);
        editing.startEditing(new GridCell(0, 0));
    }

    @UiHandler("delBtn")
    public void delClicked(SelectEvent event) {
        List<AppRefLink> links = grid.getSelectionModel().getSelectedItems();
        for (AppRefLink link : links) {
            listStore.remove(link);
        }
    }

    @Override
    public TreeStore<AppCategory> getTreeStore() {
        return treeStore;
    }

    @Override
    public void expandAppCategories() {
        tree.expandAll();
    }

    private GridEditing<AppRefLink> createGridEditing() {
        return new GridInlineEditing<>(grid);
    }

    @Override
    public boolean validate() {
        Folder tdFolder = dataFolderSelector.getValue();

        if (tdFolder == null ||  tdFolder.getPath() == null) {
            return false;
        }
        
        if (!tdFolder.getPath().startsWith(deprops.getCommunityDataPath())) {
            dataFolderSelector.setInfoErrorText(I18N.DISPLAY.testdataWarn());
            return false;
        } else {
            dataFolderSelector.setInfoErrorText(null);
        }

        return appName.getCurrentValue() != null && appName.getCurrentValue().length() <= 255
                && appDesc.getCurrentValue() != null && checkRefLinksGrid() && checkAppCategories()
                && inputDesc.isValid() && paramDesc.isValid()
                && outputDesc.isValid();

    }

    public static native String generateMarkDown(String name,
                                                 String qs,
                                                 String td,
                                                 String ifp,
                                                 String ps,
                                                 String of) /*-{

		var context = {
			appName : name,
			quickStart : qs,
			testData : td,
			inputFiles : ifp,
			params : ps,
			outputFiles : of
		};
		var template = $wnd.Handlebars
				.compile('### {{appName}} \n> #### Description and Quick Start \n>> {{quickStart}} \n> #### Test Data \n>> {{testData}} \n> #### Input File(s) \n>> {{inputFiles}} \n> #### Parameters Used in App \n>> {{params}} \n> #### Output File(s) \n>> {{outputFiles}}');
		var html = template(context);
		return html;
    }-*/
    ;

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        json.put("id", getJsonString(selectedApp.getId())); //$NON-NLS-1$
        json.put("name", getJsonString(appName.getValue()));
        json.put("description", getJsonString(appDesc.getValue())); //$NON-NLS-1$
        json.put("categories", buildSelectedCategoriesAsJson()); //$NON-NLS-1$
        json.put("references", buildRefLinksAsJson()); //$NON-NLS-1$



        String md = generateMarkDown(appName.getValue(),
                                       appDesc.getValue(),
                                       dataFolderSelector.getValue().getPath(),
                                       inputDesc.getValue(),
                                       paramDesc.getValue(),
                                       outputDesc.getValue());
        LOG.log(Level.SEVERE, md);
        json.put("documentation", getJsonString(md));

        return json;
    }

    private boolean checkRefLinksGrid() {
        if (listStore.size() > 0) {
            for (AppRefLink link : listStore.getAll()) {
                if (link.getRefLink() == null || link.getRefLink().isEmpty()) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }

    }

    private boolean checkAppCategories() {
        if (tree.getCheckedSelection().size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private JSONValue buildRefLinksAsJson() {
        JSONArray arr = new JSONArray();
        int index = 0;
        for (AppRefLink model : listStore.getAll()) {
            arr.set(index++, new JSONString(model.getRefLink()));
        }
        return arr;
    }

    private JSONString getJsonString(String value) {
        return new JSONString(value == null ? "" : value); //$NON-NLS-1$
    }

    private JSONArray buildSelectedCategoriesAsJson() {
        JSONArray arr = new JSONArray();
        int index = 0;
        for (AppCategory model : tree.getCheckedSelection()) {
            arr.set(index++, new JSONString(model.getId()));
        }
        return arr;
    }

    @Override
    public App getSelectedApp() {
        return selectedApp;
    }

    @Override
    public void setSelectedApp(App selectedApp) {
        this.selectedApp = selectedApp;
        appName.setValue(selectedApp.getName());
        appDesc.setValue(selectedApp.getDescription());
    }

}
