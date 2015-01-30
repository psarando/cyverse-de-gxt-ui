package org.iplantc.de.diskResource.client.events.selection;

import org.iplantc.de.client.models.diskResources.DiskResource;

import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

import java.util.List;

/**
 * @author jstroot
 */
public class ManageSharingSelectedEvent extends GwtEvent<ManageSharingSelectedEvent.ManageSharingSelectedEventHandler> {

    public interface ManageSharingSelectedEventHandler extends EventHandler {
        void onRequestManageSharingSelected(ManageSharingSelectedEvent event);
    }

    public static interface HasManageSharingSelectedEventHandlers {
        HandlerRegistration addManageSharingSelectedEventHandler(ManageSharingSelectedEventHandler handler);
    }

    private final List<DiskResource> diskResourcesToShare;

    public ManageSharingSelectedEvent(List<DiskResource> diskResourcesToShare) {
        this.diskResourcesToShare = diskResourcesToShare;
    }

    public ManageSharingSelectedEvent(DiskResource diskResourceToShare) {
        this(Lists.newArrayList(diskResourceToShare));
    }

    public static final Type<ManageSharingSelectedEventHandler> TYPE = new Type<>();

    @Override
    public Type<ManageSharingSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    public List<DiskResource> getDiskResourceToShare() {
        return diskResourcesToShare;
    }

    @Override
    protected void dispatch(ManageSharingSelectedEventHandler handler) {
        handler.onRequestManageSharingSelected(this);
    }
}
