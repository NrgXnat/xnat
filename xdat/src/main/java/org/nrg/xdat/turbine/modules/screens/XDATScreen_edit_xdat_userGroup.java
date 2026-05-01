/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_edit_xdat_userGroup
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.group.exceptions.GroupFieldMappingException;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

@SuppressWarnings("unused")
@Slf4j
public class XDATScreen_edit_xdat_userGroup extends EditScreenA {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getElementName() {
        return XdatUsergroup.SCHEMA_ELEMENT_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finalProcessing(final RunData data, final Context context) {
        final UserI user = XDAT.getUserDetails();
        if (user == null) {
            log.error("No user object found, not even guest. Unauthenticated users can not edit groups.");
            return;
        }
        if (user.isGuest()) {
            log.error("Guest user found. The guest user can not edit groups.");
            return;
        }

        final String username = user.getUsername();

        try {
            final String     groupId = (String) getEditItem().getProperty("ID");
            final UserGroupI group   = StringUtils.isNotBlank(groupId) ? Groups.getGroup(groupId) : getGroupFromData(data);

            final ItemI objectModel = (ItemI) context.get("om");
            if (objectModel != null && StringUtils.equalsIgnoreCase(getElementName(), objectModel.getXSIType()) && TurbineUtils.HasPassedParameter("tag", data) && StringUtils.isBlank(objectModel.getStringProperty("tag"))) {
                objectModel.setProperty(getElementName() + "/tag", TurbineUtils.GetPassedParameter("tag", data));
            }

            context.put("allElements", group.getPermissionItems(username));
            context.put("ug", group);
        } catch (XFTInitException e) {
            log.error("An error occurred trying to access XFT when trying to get the ID property from this ItemI object:\n{}", getEditItem(), e);
        } catch (ElementNotFoundException e) {
            log.error("Couldn't find the element of type {}: {}", e.ELEMENT, e);
        } catch (FieldNotFoundException e) {
            log.error("Couldn't find the field named {} for type {}: {}", e.FIELD, getEditItem().getXSIType(), e.MESSAGE);
        } catch (UserFieldMappingException e) {
            log.error("An error occurred trying to map a user field when creating a {} object for user {}", getElementName(), username, e);
        } catch (GroupFieldMappingException e) {
            log.error("An error occurred trying to map a group field when creating a {} object for user {}", getElementName(), username, e);
        } catch (UserInitException e) {
            log.error("An error occurred initializing the user {}", username, e);
        } catch (Exception e) {
            log.error("An unexpected error occurred trying to create or edit a group for the user {}", username, e);
        }
    }

    private UserGroupI getGroupFromData(final RunData data) throws UserInitException, GroupFieldMappingException, UserFieldMappingException {
        final UserGroupI group = Groups.createGroup(TurbineUtils.GetDataParameterHash(data));
        if (TurbineUtils.HasPassedParameter("tag", data) && StringUtils.isBlank(group.getTag())) {
            group.setTag((String) TurbineUtils.GetPassedParameter("tag", data));
        }
        return group;
    }
}
