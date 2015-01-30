package org.iplantc.de.diskResource.client.views.toolbar;

import org.iplantc.de.client.models.UserInfo;
import org.iplantc.de.client.models.diskResources.DiskResource;
import org.iplantc.de.client.models.diskResources.File;
import org.iplantc.de.client.models.diskResources.Folder;
import org.iplantc.de.client.models.viewer.MimeType;
import org.iplantc.de.client.util.DiskResourceUtil;
import org.iplantc.de.commons.client.views.window.configs.ConfigFactory;
import org.iplantc.de.commons.client.views.window.configs.TabularFileViewerWindowConfig;
import org.iplantc.de.diskResource.client.ToolbarView;
import org.iplantc.de.diskResource.client.events.DiskResourceSelectionChangedEvent;
import org.iplantc.de.diskResource.client.events.FolderSelectionEvent;
import org.iplantc.de.diskResource.client.events.selection.ManageCommentsSelectedEvent;
import org.iplantc.de.diskResource.client.events.selection.ManageMetadataSelectedEvent;
import org.iplantc.de.diskResource.client.events.selection.ManageSharingSelectedEvent;
import org.iplantc.de.diskResource.client.events.selection.SendToCogeSelected;
import org.iplantc.de.diskResource.client.events.selection.SendToEnsemblSelected;
import org.iplantc.de.diskResource.client.events.selection.SendToTreeViewerSelected;
import org.iplantc.de.diskResource.client.events.selection.ShareByDataLinkSelectedEvent;
import org.iplantc.de.diskResource.client.search.views.DiskResourceSearchField;
import org.iplantc.de.diskResource.client.views.widgets.TabFileConfigDialog;
import org.iplantc.de.diskResource.share.DiskResourceModule.Ids;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import java.util.List;

/**
 * @author jstroot
 */
public class DiskResourceViewToolbarImpl extends Composite implements ToolbarView,
                                                                      DiskResourceSelectionChangedEvent.DiskResourceSelectionChangedEventHandler,
                                                                      FolderSelectionEvent.FolderSelectionEventHandler {

    @UiTemplate("DiskResourceViewToolbar.ui.xml")
    interface DiskResourceViewToolbarUiBinder extends UiBinder<Widget, DiskResourceViewToolbarImpl> { }
    @UiField TextButton downloadMenu;
    @UiField TextButton editMenu;
    @UiField TextButton fileMenu;
    @UiField MenuItem newFileMi;

    @UiField MenuItem newWindowMi, newWindowAtLocMi, newFolderMi,
        duplicateMi, newPlainTextFileMi,
        newTabularDataFileMi, moveToTrashMi, newRFileMi, newPerlFileMi, newPythonFileMi,
        newShellScriptFileMi, newMdFileMi;
    @UiField MenuItem openTrashMi, restoreMi, emptyTrashMi;
    @UiField TextButton refreshButton;
    @UiField MenuItem renameMi, moveMi, deleteMi,
        editFileMi, editCommentsMi, editInfoTypeMi, metadataMi;
    @UiField(provided = true) DiskResourceSearchField searchField;
    @UiField TextButton shareMenu;
    @UiField MenuItem shareWithCollaboratorsMi, createPublicLinkMi, sendToCogeMi,
        sendToEnsemblMi, sendToTreeViewerMi;
    @UiField MenuItem simpleDownloadMi, bulkDownloadMi;
    @UiField MenuItem simpleUploadMi, bulkUploadMi, importFromUrlMi;
    @UiField TextButton trashMenu;
    @UiField TextButton uploadMenu;
    @UiField MenuItem shareFolderLocationMi;
    @UiField MenuItem newPathListMi;
    @UiField(provided = true) ToolbarView.Appearance appearance;

    private static DiskResourceViewToolbarUiBinder BINDER = GWT.create(DiskResourceViewToolbarUiBinder.class);
    private final UserInfo userInfo;
    private ToolbarView.Presenter presenter;
    private List<DiskResource> selectedDiskResources;
    private Folder selectedFolder;
    @Inject DiskResourceUtil diskResourceUtil;

    @Inject
    DiskResourceViewToolbarImpl(final DiskResourceSearchField searchField,
                                final UserInfo userInfo,
                                final ToolbarView.Appearance appearance,
                                @Assisted final ToolbarView.Presenter presenter) {
        this.searchField = searchField;
        this.userInfo = userInfo;
        this.appearance = appearance;
        this.presenter = presenter;
        initWidget(BINDER.createAndBindUi(this));
    }

    @Override
    public HandlerRegistration addManageCommentsSelectedEventHandler(ManageCommentsSelectedEvent.ManageCommentsSelectedEventHandler handler) {
        return addHandler(handler, ManageCommentsSelectedEvent.TYPE);
    }

    @Override
    public HandlerRegistration addManageMetadataSelectedEventHandler(ManageMetadataSelectedEvent.ManageMetadataSelectedEventHandler handler) {
        return addHandler(handler, ManageMetadataSelectedEvent.TYPE);
    }

    @Override
    public HandlerRegistration addManageSharingSelectedEventHandler(ManageSharingSelectedEvent.ManageSharingSelectedEventHandler handler) {
        return addHandler(handler, ManageSharingSelectedEvent.TYPE);
    }

    @Override
    public HandlerRegistration addSendToCogeSelectedHandler(SendToCogeSelected.SendToCogeSelectedHandler handler) {
        return addHandler(handler, SendToCogeSelected.TYPE);
    }

    @Override
    public HandlerRegistration addSendToEnsemblSelectedHandler(SendToEnsemblSelected.SendToEnsemblSelectedHandler handler) {
        return addHandler(handler, SendToEnsemblSelected.TYPE);
    }

    @Override
    public HandlerRegistration addSendToTreeViewerSelectedHandler(SendToTreeViewerSelected.SendToTreeViewerSelectedHandler handler) {
        return addHandler(handler, SendToTreeViewerSelected.TYPE);
    }

    @Override
    public HandlerRegistration addShareByDataLinkSelectedEventHandler(ShareByDataLinkSelectedEvent.ShareByDataLinkSelectedEventHandler handler) {
        return addHandler(handler, ShareByDataLinkSelectedEvent.TYPE);
    }

    @Override
    public DiskResourceSearchField getSearchField() {
        return searchField;
    }

//    @Override
//    public void init(DiskResourceView.Presenter presenter, DiskResourceView view) {
//        this.presenter = presenter;
//        this.presenter.addDiskResourceSelectionChangedEventHandler(this);
//        this.presenter.addFolderSelectedEventHandler(this);
//    }

    @Override
    public void onDiskResourceSelectionChanged(DiskResourceSelectionChangedEvent event) {

        boolean duplicateMiEnabled, addToSideBarMiEnabled, moveToTrashMiEnabled;

        boolean renameMiEnabled, moveMiEnabled, deleteMiEnabled, editFileMiEnabled, editCommentsMiEnabled, editInfoTypeMiEnabled, metadataMiEnabled;

        boolean simpleDownloadMiEnabled, bulkDownloadMiEnabled;
        boolean sendToCogeMiEnabled, sendToEnsemblMiEnabled, sendToTreeViewerMiEnabled;

        boolean shareWithCollaboratorsMiEnabled, createPublicLinkMiEnabled, shareFolderLocationMiEnabled;

        boolean restoreMiEnabled;

        selectedDiskResources = event.getSelection();
        final boolean isSelectionEmpty = selectedDiskResources.isEmpty();
        final boolean isSingleSelection = selectedDiskResources.size() == 1;
        final boolean isOwner = isOwner(selectedDiskResources);
        final boolean isSelectionInTrash = isSelectionInTrash(selectedDiskResources);

        duplicateMiEnabled = !isSelectionEmpty && isOwner && !isSelectionInTrash;
        moveToTrashMiEnabled = !isSelectionEmpty && isOwner && !isSelectionInTrash;

        renameMiEnabled = !isSelectionEmpty && isSingleSelection && isOwner && !isSelectionInTrash;
        moveMiEnabled = !isSelectionEmpty && isOwner && !isSelectionInTrash;
        deleteMiEnabled = !isSelectionEmpty && isOwner;
        editFileMiEnabled = !isSelectionEmpty && isSingleSelection && containsFile(selectedDiskResources) && isOwner && !isSelectionInTrash;
        editCommentsMiEnabled = !isSelectionEmpty && isSingleSelection && !isSelectionInTrash && isReadable(selectedDiskResources.get(0));
        editInfoTypeMiEnabled = !isSelectionEmpty && isSingleSelection && !isSelectionInTrash && containsFile(selectedDiskResources) && isOwner;
        metadataMiEnabled = !isSelectionEmpty && isSingleSelection && !isSelectionInTrash && isReadable(selectedDiskResources.get(0));

        simpleDownloadMiEnabled = !isSelectionEmpty && containsFile(selectedDiskResources);
        bulkDownloadMiEnabled = !isSelectionEmpty;
        sendToCogeMiEnabled = !isSelectionEmpty && isSingleSelection && containsFile(selectedDiskResources) && !isSelectionInTrash;
        sendToEnsemblMiEnabled = !isSelectionEmpty && isSingleSelection && containsFile(selectedDiskResources) && !isSelectionInTrash;
        sendToTreeViewerMiEnabled = !isSelectionEmpty && isSingleSelection && containsFile(selectedDiskResources) && !isSelectionInTrash;

        shareWithCollaboratorsMiEnabled = !isSelectionEmpty && isOwner && !isSelectionInTrash;
        createPublicLinkMiEnabled = !isSelectionEmpty && isOwner && !isSelectionInTrash && containsFile(selectedDiskResources);
        shareFolderLocationMiEnabled = !isSelectionEmpty && isSingleSelection && !isSelectionInTrash && containsOnlyFolders(selectedDiskResources);


        restoreMiEnabled = !isSelectionEmpty && isSelectionInTrash && isOwner;

        duplicateMi.setEnabled(duplicateMiEnabled);
        moveToTrashMi.setEnabled(moveToTrashMiEnabled);

        renameMi.setEnabled(renameMiEnabled);
        moveMi.setEnabled(moveMiEnabled);
        deleteMi.setEnabled(deleteMiEnabled);
        editFileMi.setEnabled(editFileMiEnabled);
        editCommentsMi.setEnabled(editCommentsMiEnabled);
        editInfoTypeMi.setEnabled(editInfoTypeMiEnabled);
        metadataMi.setEnabled(metadataMiEnabled);

        simpleDownloadMi.setEnabled(simpleDownloadMiEnabled);
        bulkDownloadMi.setEnabled(bulkDownloadMiEnabled);

        shareWithCollaboratorsMi.setEnabled(shareWithCollaboratorsMiEnabled);
        createPublicLinkMi.setEnabled(createPublicLinkMiEnabled);
        shareFolderLocationMi.setEnabled(shareFolderLocationMiEnabled);
        sendToCogeMi.setEnabled(sendToCogeMiEnabled);
        sendToEnsemblMi.setEnabled(sendToEnsemblMiEnabled);
        sendToTreeViewerMi.setEnabled(sendToTreeViewerMiEnabled);

        restoreMi.setEnabled(restoreMiEnabled);
    }

    @Override
    public void onFolderSelected(FolderSelectionEvent event) {
        boolean simpleUploadMiEnabled, bulkUploadMiEnabled, importFromUrlMiEnabled;

        boolean newFolderMiEnabled, newPlainTextFileMiEnabled, newTabularDataFileMiEnabled;
        boolean refreshButtonEnabled;

        selectedFolder = event.getSelectedFolder();
        final boolean isFolderInTrash = isSelectionInTrash(Lists.<DiskResource>newArrayList(selectedFolder));
        final boolean isNull = selectedFolder == null;
        final boolean canUploadTo = canUploadTo(selectedFolder);

        simpleUploadMiEnabled = !isFolderInTrash && (isNull || canUploadTo);
        bulkUploadMiEnabled = !isFolderInTrash && (isNull || canUploadTo);
        importFromUrlMiEnabled = !isFolderInTrash && (isNull || canUploadTo);

        newFolderMiEnabled = isNull || canUploadTo;
        newPlainTextFileMiEnabled = isNull || canUploadTo;
        newTabularDataFileMiEnabled = isNull || canUploadTo;

        refreshButtonEnabled = !isNull;

        simpleUploadMi.setEnabled(simpleUploadMiEnabled);
        bulkUploadMi.setEnabled(bulkUploadMiEnabled);
        importFromUrlMi.setEnabled(importFromUrlMiEnabled);

        newFolderMi.setEnabled(newFolderMiEnabled);
        newFileMi.setEnabled(newPlainTextFileMiEnabled);
        // newPlainTextFileMi.setEnabled(newPlainTextFileMiEnabled);
        // newTabularDataFileMi.setEnabled(newTabularDataFileMiEnabled);

        refreshButton.setEnabled(refreshButtonEnabled);
    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        super.onEnsureDebugId(baseID);
        uploadMenu.ensureDebugId(baseID + Ids.UPLOAD_MENU);
        fileMenu.ensureDebugId(baseID + Ids.FILE_MENU);
        editMenu.ensureDebugId(baseID + Ids.EDIT_MENU);
        downloadMenu.ensureDebugId(baseID + Ids.DOWNLOAD_MENU);
        refreshButton.ensureDebugId(baseID + Ids.REFRESH_BUTTON);
        shareMenu.ensureDebugId(baseID + Ids.SHARE_MENU);
        trashMenu.ensureDebugId(baseID + Ids.TRASH_MENU);
        searchField.ensureDebugId(baseID + Ids.SEARCH_FIELD);

        // Upload menu
        simpleUploadMi.ensureDebugId(baseID + Ids.UPLOAD_MENU + Ids.MENU_ITEM_SIMPLE_UPLOAD);
        bulkUploadMi.ensureDebugId(baseID + Ids.UPLOAD_MENU + Ids.MENU_ITEM_BULK_UPLOAD);
        importFromUrlMi.ensureDebugId(baseID + Ids.UPLOAD_MENU + Ids.MENU_ITEM_IMPORT_FROM_URL);

        // File menu
        newWindowMi.ensureDebugId(baseID + Ids.FILE_MENU + Ids.MENU_ITEM_NEW_WINDOW);
        newWindowAtLocMi.ensureDebugId(baseID + Ids.FILE_MENU + Ids.MENU_ITEM_NEW_WINDOW_AT_LOC);
        newFolderMi.ensureDebugId(baseID + Ids.FILE_MENU + Ids.MENU_ITEM_NEW_FOLDER);
        duplicateMi.ensureDebugId(baseID + Ids.FILE_MENU + Ids.MENU_ITEM_DUPLICATE);
        newPlainTextFileMi.ensureDebugId(baseID + Ids.FILE_MENU + Ids.MENU_ITEM_NEW_PLAIN_TEXT);
        newTabularDataFileMi.ensureDebugId(baseID + Ids.FILE_MENU + Ids.MENU_ITEM_NEW_TABULAR_DATA);

        newPerlFileMi.ensureDebugId(baseID + Ids.FILE_MENU + Ids.MENU_ITEM_NEW_PERL_DATA);
        newPythonFileMi.ensureDebugId(baseID + Ids.FILE_MENU + Ids.MENU_ITEM_NEW_PYTHON_DATA);
        newRFileMi.ensureDebugId(baseID + Ids.FILE_MENU + Ids.MENU_ITEM_NEW_R_DATA);
        newShellScriptFileMi.ensureDebugId(baseID + Ids.FILE_MENU + Ids.MENU_ITEM_NEW_SHELL_DATA);
        newMdFileMi.ensureDebugId(baseID + Ids.FILE_MENU + Ids.MENU_ITEM_NEW_MD_DATA);
        newPathListMi.ensureDebugId(baseID + Ids.FILE_MENU + Ids.MENU_ITEM_NEW_PATH_LIST);

        moveToTrashMi.ensureDebugId(baseID + Ids.FILE_MENU + Ids.MENU_ITEM_MOVE_TO_TRASH);

        // Edit menu
        renameMi.ensureDebugId(baseID + Ids.EDIT_MENU + Ids.MENU_ITEM_RENAME);
        moveMi.ensureDebugId(baseID + Ids.EDIT_MENU + Ids.MENU_ITEM_MOVE);
        editFileMi.ensureDebugId(baseID + Ids.EDIT_MENU + Ids.MENU_ITEM_EDIT_FILE);
        editInfoTypeMi.ensureDebugId(baseID + Ids.EDIT_MENU + Ids.MENU_ITEM_EDIT_INFO_TYPE);
        metadataMi.ensureDebugId(baseID + Ids.EDIT_MENU + Ids.MENU_ITEM_METADATA);

        // Download menu
        simpleDownloadMi.ensureDebugId(baseID + Ids.DOWNLOAD_MENU + Ids.MENU_ITEM_SIMPLE_DOWNLOAD);
        bulkDownloadMi.ensureDebugId(baseID + Ids.DOWNLOAD_MENU + Ids.MENU_ITEM_BULK_DOWNLOAD);

        // Share menu
        shareWithCollaboratorsMi.ensureDebugId(baseID + Ids.SHARE_MENU + Ids.MENU_ITEM_SHARE_WITH_COLLABORATORS);
        createPublicLinkMi.ensureDebugId(baseID + Ids.SHARE_MENU + Ids.MENU_ITEM_CREATE_PUBLIC_LINK);
        shareFolderLocationMi.ensureDebugId(baseID + Ids.SHARE_MENU + Ids.MENU_ITEM_SHARE_FOLDER_LOCATION);
        sendToCogeMi.ensureDebugId(baseID + Ids.SHARE_MENU + Ids.MENU_ITEM_SEND_TO_COGE);
        sendToEnsemblMi.ensureDebugId(baseID + Ids.SHARE_MENU + Ids.MENU_ITEM_SEND_TO_ENSEMBL);
        sendToTreeViewerMi.ensureDebugId(baseID + Ids.SHARE_MENU + Ids.MENU_ITEM_SEND_TO_TREE_VIEWER);

        // Trash menu
        openTrashMi.ensureDebugId(baseID + Ids.TRASH_MENU + Ids.MENU_ITEM_OPEN_TRASH);
        restoreMi.ensureDebugId(baseID + Ids.TRASH_MENU + Ids.MENU_ITEM_RESTORE);
        emptyTrashMi.ensureDebugId(baseID + Ids.TRASH_MENU + Ids.MENU_ITEM_EMPTY_TRASH);
        deleteMi.ensureDebugId(baseID + Ids.TRASH_MENU + Ids.MENU_ITEM_DELETE);

    }

    boolean canUploadTo(DiskResource folder){
        return diskResourceUtil.canUploadTo(folder);
    }

    boolean containsFile(final List<DiskResource> selection) {
        return diskResourceUtil.containsFile(selection);
    }

    boolean containsOnlyFolders(List<DiskResource> selection) {
        for(DiskResource dr : selection ) {
            if(dr instanceof File)
                return false;
        }
       return true;
    }

    boolean isOwner(final List<DiskResource> selection){
        return diskResourceUtil.isOwner(selection);
    }

    boolean isReadable(final DiskResource item){
        return diskResourceUtil.isReadable(item);
    }

    boolean isSelectionInTrash(final List<DiskResource> selection){
        if (selection.isEmpty()) {
            return false;
        }

        String trashPath = userInfo.getTrashPath();
        for (DiskResource dr : selection) {
            if (dr.getPath().equals(trashPath)) {
                return false;
            }

            if (!dr.getPath().startsWith(trashPath)) {
                return false;
            }
        }

        return true;
    }

    @UiHandler("bulkDownloadMi")
    void onBulkDownloadClicked(SelectionEvent<Item> event) {
        // FIXME Fire event up. This needs to be handled in DR presenter
        presenter.onBulkDownloadSelected();
    }

    @UiHandler("bulkUploadMi")
    void onBulkUploadClicked(SelectionEvent<Item> event) {
        presenter.onBulkUploadSelected();
    }

    @UiHandler("createPublicLinkMi")
    void onCreatePublicLinkClicked(SelectionEvent<Item> event){
        presenter.onCreatePublicLinkSelected();
    }

    @UiHandler("deleteMi")
    void onDeleteClicked(SelectionEvent<Item> event){
        presenter.onDeleteResourcesSelected();
    }

    @UiHandler("duplicateMi")
    void onDuplicateClicked(SelectionEvent<Item> event){
    }

    @UiHandler("editCommentsMi")
    void onEditCommentClicked(SelectionEvent<Item> event){
        Preconditions.checkState(selectedDiskResources != null
                                     && selectedDiskResources.size() == 1);
        fireEvent(new ManageCommentsSelectedEvent(selectedDiskResources.iterator().next()));
    }

    @UiHandler("editFileMi")
    void onEditFileClicked(SelectionEvent<Item> event){
        // TODO Is there already an event for this?
        presenter.onEditFileSelected();
    }

    @UiHandler("editInfoTypeMi")
    void onEditInfoTypeClicked(SelectionEvent<Item> event){
        presenter.onEditInfoTypeSelected();
    }

    @UiHandler("metadataMi")
    void onEditMetadataClicked(SelectionEvent<Item> event){
        Preconditions.checkState(selectedDiskResources != null
                                     && selectedDiskResources.size() == 1);
        fireEvent(new ManageMetadataSelectedEvent(selectedDiskResources.iterator().next()));
    }

    @UiHandler("emptyTrashMi")
    void onEmptyTrashClicked(SelectionEvent<Item> event) {
        presenter.onEmptyTrashSelected();
    }

    @UiHandler("importFromUrlMi")
    void onImportFromUrlClicked(SelectionEvent<Item> event) {
        presenter.onImportFromUrlSelected();
    }

    @UiHandler("moveMi")
    void onMoveClicked(SelectionEvent<Item> event){
        presenter.onMoveDiskResourcesSelected();
    }

    @UiHandler("moveToTrashMi")
    void onMoveToTrashClicked(SelectionEvent<Item> event){
        presenter.onMoveToTrashSelected();
    }

    @UiHandler("newFolderMi")
    void onNewFolderClicked(SelectionEvent<Item> event) {
        presenter.onCreateNewFolderSelected(selectedFolder);
    }

    @UiHandler("newPlainTextFileMi")
    void onNewPlainTextFileClicked(SelectionEvent<Item> event){
        // FIXME Move this config generation to presenter
//        FileViewerWindowConfig config = ConfigFactory.fileViewerWindowConfig(null);
//        config.setEditing(true);
        presenter.onCreateNewFileSelected(selectedFolder, MimeType.PLAIN);
    }

    @UiHandler("newRFileMi")
    void onNewRFileClicked(SelectionEvent<Item> event) {
        // FIXME Move this config generation to presenter
//        FileViewerWindowConfig config = ConfigFactory.fileViewerWindowConfig(null);
//        config.setEditing(true);
        presenter.onCreateNewFileSelected(selectedFolder, MimeType.X_RSRC);
    }

    @UiHandler("newPerlFileMi")
    void onNewPerlFileClicked(SelectionEvent<Item> event) {
        // FIXME Move this config generation to presenter
//        FileViewerWindowConfig config = ConfigFactory.fileViewerWindowConfig(null);
//        config.setEditing(true);
        presenter.onCreateNewFileSelected(selectedFolder, MimeType.X_PERL);
    }

    @UiHandler("newPythonFileMi")
    void onNewPythonFileClicked(SelectionEvent<Item> event) {
        // FIXME Move this config generation to presenter
//        FileViewerWindowConfig config = ConfigFactory.fileViewerWindowConfig(null);
//        config.setEditing(true);
        presenter.onCreateNewFileSelected(selectedFolder, MimeType.X_PYTHON);
    }

    @UiHandler("newShellScriptFileMi")
    void onNewShellScript(SelectionEvent<Item> event) {
        // FIXME Move this config generation to presenter
//        FileViewerWindowConfig config = ConfigFactory.fileViewerWindowConfig(null);
//        config.setEditing(true);
        presenter.onCreateNewFileSelected(selectedFolder, MimeType.X_SH);
    }

    @UiHandler("newMdFileMi")
    void onNewMdFile(SelectionEvent<Item> event) {
        // FIXME Move this config generation to presenter
//        FileViewerWindowConfig config = ConfigFactory.fileViewerWindowConfig(null);
//        config.setEditing(true);
        presenter.onCreateNewFileSelected(selectedFolder, MimeType.X_WEB_MARKDOWN);
    }

    @UiHandler("newTabularDataFileMi")
    void onNewTabularDataFileClicked(SelectionEvent<Item> event){
        // FIXME Move this dialog logic to presenter
        presenter.onCreateNewDelimitedFileSelected();
        final TabFileConfigDialog d = new TabFileConfigDialog();
        d.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
        d.setModal(true);
        d.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                TabularFileViewerWindowConfig config = ConfigFactory.newTabularFileViewerWindowConfig();
                config.setEditing(true);
                config.setVizTabFirst(true);
                config.setSeparator(d.getSeparator());
                config.setColumns(d.getNumberOfColumns());
                config.setContentType(MimeType.PLAIN);
//                presenter.onCreateNewTabFileSelected(config);
            }
        });
        d.setHideOnButtonClick(true);
        d.setSize("300px", "150px");
        d.show();
    }

    @UiHandler("newPathListMi")
    void onNewPathListFileClicked(SelectionEvent<Item> event){
        // FIXME Move the config generation to presenter
//        PathListWindowConfig config = ConfigFactory.newPathListWindowConfig();
//        config.setEditing(true);
        presenter.onCreateNewPathListSelected();
    }

    @UiHandler("newWindowAtLocMi")
    void onNewWindowAtLocClicked(SelectionEvent<Item> event) {
        presenter.onOpenNewWindowAtLocationSelected();
    }

    //---------- File ----------
    @UiHandler("newWindowMi")
    void onNewWindowClicked(SelectionEvent<Item> event) {
        presenter.onOpenNewWindowSelected();
    }

    //------------- Trash ---------------
    @UiHandler("openTrashMi")
    void onOpenTrashClicked(SelectionEvent<Item> event) {
        presenter.onOpenTrashFolderSelected();
    }

    //------------ Refresh --------------
    @UiHandler("refreshButton")
    void onRefreshClicked(SelectEvent event) {
        presenter.onRefreshFolderSelected();
    }

    //----------- Edit -----------
    @UiHandler("renameMi")
    void onRenameClicked(SelectionEvent<Item> event){
        presenter.onRenameResourceSelected();
    }

    @UiHandler("restoreMi")
    void onRestoreClicked(SelectionEvent<Item> event){
        presenter.onRestoreResourcesSelected();
    }

    @UiHandler("sendToCogeMi")
    void onSendToCogeClicked(SelectionEvent<Item> event) {
//        presenter.sendSelectedResourcesToCoge();
        fireEvent(new SendToCogeSelected(selectedDiskResources));
    }

    @UiHandler("sendToEnsemblMi")
    void onSendToEnsemblClicked(SelectionEvent<Item> event){
//        presenter.onSendSelectedResourceToEnsemblSelected();
        fireEvent(new SendToEnsemblSelected(selectedDiskResources));
    }

    @UiHandler("sendToTreeViewerMi")
    void onSendToTreeViewerClicked(SelectionEvent<Item> event){
//        presenter.onSendSelectedResourcesToTreeViewerSelected();
        fireEvent(new SendToTreeViewerSelected(selectedDiskResources));
    }

    //--------- Sharing -------------
    @UiHandler("shareWithCollaboratorsMi")
    void onShareWithCollaboratorsClicked(SelectionEvent<Item> event) {
        Preconditions.checkState(selectedDiskResources != null
                                     && !selectedDiskResources.isEmpty());
        fireEvent(new ManageSharingSelectedEvent(selectedDiskResources));
    }

    @UiHandler("shareFolderLocationMi")
    void onShareFolderLocationClicked(SelectionEvent<Item> event){
        Preconditions.checkState(selectedFolder != null);
        fireEvent(new ShareByDataLinkSelectedEvent(selectedFolder));
    }

    //---------- Download --------------
    @UiHandler("simpleDownloadMi")
    void onSimpleDownloadClicked(SelectionEvent<Item> event) {
        presenter.onSimpleDownloadSelected(selectedFolder, selectedDiskResources);
    }

    //-------- Upload ---------------
    @UiHandler("simpleUploadMi")
    void onSimpleUploadClicked(SelectionEvent<Item> event) {
        presenter.onSimpleUploadSelected();
    }

    @Override
    public void maskSendToCoGe() {
        sendToCogeMi.mask();

    }

    @Override
    public void unmaskSendToCoGe() {
        sendToCogeMi.unmask();

    }

    @Override
    public void maskSendToEnsembl() {
        sendToEnsemblMi.mask();

    }

    @Override
    public void unmaskSendToEnsembl() {
        sendToEnsemblMi.unmask();

    }

    @Override
    public void maskSendToTreeViewer() {
        sendToTreeViewerMi.mask();

    }

    @Override
    public void unmaskSendToTreeViewer() {
        sendToTreeViewerMi.unmask();

    }
}
