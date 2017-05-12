package org.iplantc.de.collaborators.client.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.iplantc.de.client.models.collaborators.Collaborator;
import org.iplantc.de.client.models.groups.Group;
import org.iplantc.de.client.models.groups.GroupAutoBeanFactory;
import org.iplantc.de.client.models.groups.UpdateMemberResult;
import org.iplantc.de.client.services.GroupServiceFacade;
import org.iplantc.de.collaborators.client.GroupDetailsView;
import org.iplantc.de.collaborators.client.GroupView;
import org.iplantc.de.collaborators.client.events.AddGroupMemberSelected;
import org.iplantc.de.collaborators.client.events.GroupSaved;
import org.iplantc.de.commons.client.info.ErrorAnnouncementConfig;
import org.iplantc.de.commons.client.info.IplantAnnouncer;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.autobean.shared.AutoBean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author aramsey
 */
@RunWith(GwtMockitoTestRunner.class)
public class GroupDetailsPresenterImplTest {

    @Mock GroupDetailsView viewMock;
    @Mock GroupServiceFacade serviceFacadeMock;
    @Mock GroupAutoBeanFactory factoryMock;
    @Mock GroupView.GroupViewAppearance appearanceMock;
    @Mock Group groupMock;
    @Mock Group newGroupMock;
    @Mock AutoBean<Group> groupAutoBeanMock;
    @Mock List<Collaborator> collaboratorListMock;
    @Mock HandlerManager handlerManagerMock;
    @Mock UpdateMemberResult updateResultMock;
    @Mock List<UpdateMemberResult> updateMemberResultsMock;
    @Mock List<UpdateMemberResult> failedUpdateResultsMock;
    @Mock Stream<UpdateMemberResult> updateMemberResultStreamMock;
    @Mock IplantAnnouncer announcerMock;
    @Mock Collaborator subjectMock;
    @Mock Iterator<Collaborator> collaboratorIteratorMock;
    @Mock Iterator<UpdateMemberResult> resultIteratorMock;

    @Captor ArgumentCaptor<AsyncCallback<List<Collaborator>>> collabListCallbackCaptor;
    @Captor ArgumentCaptor<AsyncCallback<Group>> groupCallbackCaptor;
    @Captor ArgumentCaptor<AsyncCallback<List<UpdateMemberResult>>> updateMembersCallbackCaptor;
    @Captor ArgumentCaptor<AsyncCallback<Void>> voidCallbackCaptor;

    private GroupDetailsPresenterImpl uut;

    @Before
    public void setUp() {

        when(factoryMock.getGroup()).thenReturn(groupAutoBeanMock);
        when(groupAutoBeanMock.as()).thenReturn(newGroupMock);
        when(collaboratorListMock.size()).thenReturn(2);
        when(collaboratorListMock.iterator()).thenReturn(collaboratorIteratorMock);
        when(collaboratorIteratorMock.hasNext()).thenReturn(true, true, false);
        when(collaboratorIteratorMock.next()).thenReturn(subjectMock, subjectMock);
        when(failedUpdateResultsMock.size()).thenReturn(2);
        when(failedUpdateResultsMock.iterator()).thenReturn(resultIteratorMock);
        when(resultIteratorMock.hasNext()).thenReturn(true, true, false);
        when(resultIteratorMock.next()).thenReturn(updateResultMock, updateResultMock);

        uut = new GroupDetailsPresenterImpl(viewMock,
                                            serviceFacadeMock,
                                            factoryMock,
                                            appearanceMock) {
            @Override
            HandlerManager ensureHandlers() {
                return handlerManagerMock;
            }
        };
        uut.announcer = announcerMock;
    }

    @Test
    public void go_editGroup() {
        HasOneWidget containerMock = mock(HasOneWidget.class);

        /** CALL METHOD UNDER TEST **/
        uut.go(containerMock, groupMock, GroupDetailsView.MODE.EDIT);

        verify(containerMock).setWidget(eq(viewMock));
        verify(serviceFacadeMock).getMembers(eq(groupMock), collabListCallbackCaptor.capture());

        collabListCallbackCaptor.getValue().onSuccess(collaboratorListMock);
        verify(viewMock).addMembers(eq(collaboratorListMock));
        verify(viewMock).edit(eq(groupMock), eq(GroupDetailsView.MODE.EDIT));
    }

    @Test
    public void go_newGroup() {
        HasOneWidget containerMock = mock(HasOneWidget.class);

        /** CALL METHOD UNDER TEST **/
        uut.go(containerMock, null, GroupDetailsView.MODE.ADD);

        verify(containerMock).setWidget(eq(viewMock));
        verify(viewMock).edit(eq(newGroupMock), eq(GroupDetailsView.MODE.ADD));
    }

    @Test
    public void saveGroupSelected_newGroup() {
        when(viewMock.getGroup()).thenReturn(groupMock);
        uut.mode = GroupDetailsView.MODE.ADD;
        GroupDetailsPresenterImpl spy = Mockito.spy(uut);

        /** CALL METHOD UNDER TEST **/
        spy.saveGroupSelected();

        verify(spy).addGroup(eq(groupMock));
    }

    @Test
    public void addGroup() {

        /** CALL METHOD UNDER TEST **/
        uut.addGroup(groupMock);
        verify(serviceFacadeMock).addGroup(eq(groupMock), groupCallbackCaptor.capture());
        groupCallbackCaptor.getValue().onSuccess(groupMock);

        verify(handlerManagerMock).fireEvent(isA(GroupSaved.class));

    }

    @Test
    public void updateGroupMembers_withFailures() {
        when(collaboratorListMock.isEmpty()).thenReturn(false);
        when(updateMemberResultsMock.stream()).thenReturn(updateMemberResultStreamMock);
        when(updateMemberResultStreamMock.filter(any())).thenReturn(updateMemberResultStreamMock);
        when(updateMemberResultStreamMock.collect(any())).thenReturn(failedUpdateResultsMock);
        when(appearanceMock.unableToAddMembers(any())).thenReturn("announcement");
        /** CALL METHOD UNDER TEST **/
        uut.updateGroupMembers(groupMock, collaboratorListMock);

        verify(serviceFacadeMock).updateMembers(eq(groupMock),
                                                eq(collaboratorListMock),
                                                updateMembersCallbackCaptor.capture());

        updateMembersCallbackCaptor.getValue().onSuccess(updateMemberResultsMock);
        verify(appearanceMock).unableToAddMembers(eq(failedUpdateResultsMock));
        verify(announcerMock).schedule(isA(ErrorAnnouncementConfig.class));
        verify(viewMock).unmask();
    }

    @Test
    public void onAddGroupMemberSelected() {
        AddGroupMemberSelected eventMock = mock(AddGroupMemberSelected.class);
        when(eventMock.getGroup()).thenReturn(groupMock);
        when(eventMock.getSubject()).thenReturn(subjectMock);
        when(groupMock.getName()).thenReturn("name");

        /** CALL METHOD UNDER TEST **/
        uut.onAddGroupMemberSelected(eventMock);

        verify(serviceFacadeMock).addMember(eq(groupMock),
                                            eq(subjectMock),
                                            voidCallbackCaptor.capture());

        voidCallbackCaptor.getValue().onSuccess(null);
        verify(viewMock).addMembers(any());
    }
}