/*
 * web: org.nrg.xnat.restlet.extensions.UserRolesRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.extensions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import static org.nrg.xdat.security.helpers.Roles.*;
import static org.nrg.xft.event.XftItemEventI.OPERATION;

/**
 * @author tim@deck5consulting.com
 *         <p>
 *         Implementation of the User Roles functionality.  The post method adds or removes role for the specified user.
 */
@XnatRestlet("/user/{USER_ID}/roles")
@Slf4j
public class UserRolesRestlet extends SecureResource {
    /**
     * @param context  standard
     * @param request  standard
     * @param response standard
     */
    public UserRolesRestlet(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);

        if (!Roles.isSiteAdmin(getUser())) {
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "User does not have privileges to access this project.");
        }

        _userId = (String) getRequest().getAttributes().get("USER_ID");
        if (StringUtils.isBlank(_userId)) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "As of this release, you must specify a user on which to perform.");
        }

        try {
            _user = Users.getUser(_userId);
        } catch (Exception e) {
            getResponse().setEntity(new StringRepresentation("An error occurred trying to retrieve the user " + _userId));
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "An error occurred trying to retrieve the user " + _userId);
            log.error("An error occurred trying to retrieve the user {}", _userId, e);
            return;
        }

        if (_user == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "The user " + _userId + " could not be found.");
        }

        getVariants().add(new Variant(MediaType.ALL));
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void handlePost() {
        final Set<String> roles = new HashSet<>();

        if (hasQueryVariable("roles")) {
            Collections.addAll(roles, getQueryVariable("roles").split(","));
        }

        try {
            final UserI authUser = getUser();

            final Set<String> existing = new HashSet<>(Roles.getRoles(_user));

            // If existing and submitted are the same, there's nothing to do.
            if (roles.equals(existing)) {
                getResponse().setEntity(new StringRepresentation(""));
                getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            }

            // Everything that's in roles but not existing is being added. Everything that's in existing
            // but not roles is being deleted.
            final Sets.SetView<String> added   = Sets.difference(roles, existing);
            final Sets.SetView<String> deleted = Sets.difference(existing, roles);

            //remove roles and save one at a time so that there is a separate workflow entry for each one
            for (final String role : deleted) {
                if (Roles.deleteRole(authUser, _user, role)) {
                    log.debug("Deleted role {} from user {}", role, _userId);
                } else {
                    log.warn("Tried to delete role {} from user {}, but that didn't happen for some reason", role, _userId);
                }
                _user = Users.getUser(_userId); //get fresh db copy
            }

            //add roles and save one at a time so that there is a separate workflow entry for each one
            for (final String role : added) {
                //add role if isn't there
                if (Roles.addRole(authUser, _user, role)) {
                    log.debug("Added role {} to user {}", role, _userId);
                } else {
                    log.warn("Tried to add role {} to user {}, but that didn't happen for some reason", role, _userId);
                }
                _user = Users.getUser(_userId);//get fresh db copy
            }

            getResponse().setEntity(new StringRepresentation(""));
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            if (!added.isEmpty() && !deleted.isEmpty()) {
                builder.put(OPERATION, OPERATION_MODIFIED_ROLES);
                builder.put(ADDED_ROLES, added);
                builder.put(DELETED_ROLES, deleted);
            } else if (!added.isEmpty()) {
                addRolesToMap(added, builder, OPERATION_ADD_ROLE, OPERATION_ADD_ROLES);
            } else {
                addRolesToMap(deleted, builder, OPERATION_DELETE_ROLE, OPERATION_DELETE_ROLES);
            }
            XDAT.triggerUserIEvent(_userId, XftItemEvent.UPDATE, builder.build());
        } catch (Throwable e) {
            log.error("An error occurred trying to add roles ", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final MediaType                 mediaType = overrideVariant(variant);
        final Hashtable<String, Object> params    = new Hashtable<>();

        final XFTTable table = new XFTTable();
        table.initTable(new String[]{"role"});
        for (final String role : Roles.getRoles(_user)) {
            table.rows().add(new Object[]{role});
        }

        params.put("totalRecords", table.size());
        return representTable(table, mediaType, params);
    }

    protected void addRolesToMap(final Sets.SetView<String> deleted, final ImmutableMap.Builder<String, Object> builder, final String operationDeleteRole, final String operationDeleteRoles) {
        if (deleted.size() == 1) {
            builder.put(OPERATION, operationDeleteRole);
            builder.put(ROLE, deleted.iterator().next());
        } else {
            builder.put(OPERATION, operationDeleteRoles);
            builder.put(ROLES, new HashSet<>(deleted));
        }
    }

    private final String _userId;

    private UserI _user;
}
