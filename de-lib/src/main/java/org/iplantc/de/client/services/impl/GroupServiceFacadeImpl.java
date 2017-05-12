package org.iplantc.de.client.services.impl;

import static org.iplantc.de.shared.services.BaseServiceCallWrapper.Type.DELETE;
import static org.iplantc.de.shared.services.BaseServiceCallWrapper.Type.GET;
import static org.iplantc.de.shared.services.BaseServiceCallWrapper.Type.POST;
import static org.iplantc.de.shared.services.BaseServiceCallWrapper.Type.PUT;

import org.iplantc.de.client.models.collaborators.Collaborator;
import org.iplantc.de.client.models.collaborators.CollaboratorAutoBeanFactory;
import org.iplantc.de.client.models.groups.Group;
import org.iplantc.de.client.models.groups.GroupAutoBeanFactory;
import org.iplantc.de.client.models.groups.GroupList;
import org.iplantc.de.client.models.groups.UpdateMemberRequest;
import org.iplantc.de.client.models.groups.UpdateMemberResult;
import org.iplantc.de.client.models.groups.UpdateMemberResultList;
import org.iplantc.de.client.services.GroupServiceFacade;
import org.iplantc.de.client.services.converters.AsyncCallbackConverter;
import org.iplantc.de.client.services.converters.CollaboratorListCallbackConverter;
import org.iplantc.de.client.services.converters.GroupCallbackConverter;
import org.iplantc.de.client.services.converters.StringToVoidCallbackConverter;
import org.iplantc.de.shared.services.DiscEnvApiService;
import org.iplantc.de.shared.services.ServiceCallWrapper;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.shared.Splittable;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * @author aramsey
 */
public class GroupServiceFacadeImpl implements GroupServiceFacade {

    private final String LISTS = "org.iplantc.services.collaboratorLists";

    private GroupAutoBeanFactory factory;
    private CollaboratorAutoBeanFactory collabFactory;
    private DiscEnvApiService deService;

    @Inject
    public GroupServiceFacadeImpl(GroupAutoBeanFactory factory,
                                  CollaboratorAutoBeanFactory collabFactory,
                                  DiscEnvApiService deService) {

        this.factory = factory;
        this.collabFactory = collabFactory;
        this.deService = deService;
    }

    @Override
    public void getGroups(AsyncCallback<List<Group>> callback) {
        String address = LISTS;

        ServiceCallWrapper wrapper = new ServiceCallWrapper(GET, address);
        deService.getServiceData(wrapper, new AsyncCallbackConverter<String, List<Group>>(callback) {
            @Override
            protected List<Group> convertFrom(String object) {
                AutoBean<GroupList> decode = AutoBeanCodex.decode(factory, GroupList.class, object);
                return decode.as().getGroups();
            }
        });
    }

    @Override
    public void addGroup(Group group, AsyncCallback<Group> callback) {
        String address = LISTS;

        final Splittable encode = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(group));

        ServiceCallWrapper wrapper = new ServiceCallWrapper(POST, address, encode.getPayload());
        deService.getServiceData(wrapper, new GroupCallbackConverter(callback, factory));
    }

    @Override
    public void deleteGroup(String name, AsyncCallback<Group> callback) {
        String address = LISTS + "/" + URL.encodeQueryString(name);

        ServiceCallWrapper wrapper = new ServiceCallWrapper(DELETE, address);
        deService.getServiceData(wrapper, new GroupCallbackConverter(callback, factory));
    }

    @Override
    public void getMembers(Group group, AsyncCallback<List<Collaborator>> callback) {
        String groupName = group.getName();
        String address = LISTS + "/" + URL.encodeQueryString(groupName) + "/members";

        ServiceCallWrapper wrapper = new ServiceCallWrapper(GET, address);
        deService.getServiceData(wrapper, new CollaboratorListCallbackConverter(callback, collabFactory));
    }

    @Override
    public void addMember(Group group, Collaborator member, AsyncCallback<Void> callback) {
        String groupName = group.getName();
        String subjectId = member.getId();

        String address = LISTS + "/" + groupName + "/members/" + subjectId;

        ServiceCallWrapper wrapper = new ServiceCallWrapper(PUT, address);
        deService.getServiceData(wrapper, new StringToVoidCallbackConverter(callback));
    }

    @Override
    public void updateMembers(Group group,
                              List<Collaborator> collaborators,
                              AsyncCallback<List<UpdateMemberResult>> callback) {
        String groupName = group.getName();
        UpdateMemberRequest request = factory.getUpdateMemberRequest().as();
        List<String> ids = collaborators.stream()
                                            .map(collaborator -> collaborator.getId())
                                            .collect(Collectors.toList());
        request.setMembers(ids);

        String address = LISTS + "/" + groupName + "/members";

        Splittable encode = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(request));

        ServiceCallWrapper wrapper = new ServiceCallWrapper(PUT, address, encode.getPayload());
        deService.getServiceData(wrapper, new AsyncCallbackConverter<String, List<UpdateMemberResult>>(callback) {
            @Override
            protected List<UpdateMemberResult> convertFrom(String object) {
                AutoBean<UpdateMemberResultList> listAutoBean = AutoBeanCodex.decode(factory, UpdateMemberResultList.class, object);
                return listAutoBean.as().getResults();
            }
        });
    }

}