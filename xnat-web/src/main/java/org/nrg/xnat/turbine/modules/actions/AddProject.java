/*
 * web: org.nrg.xnat.turbine.modules.actions.AddProject
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.modules.ScreenLoader;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.modules.actions.ModifyItem;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.modules.screens.EditScreenA;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ValidationException;
import org.nrg.xft.exception.XftItemLifecycleException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xnat.utils.WorkflowUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.nrg.xft.event.XftItemLifecyclePhase.Save;

@SuppressWarnings("unused")
@Slf4j
public class AddProject extends AbstractProjectSecureAction {
    @Override
    public void doPerform(final RunData data, final Context context) {
        if (TurbineUtils.HasPassedParameter("tag", data)) {
            context.put("tag", TurbineUtils.GetPassedParameter("tag", data));
        }

        final UserI user = XDAT.getUserDetails();
        if (!XDAT.getSiteConfigPreferences().getUiAllowNonAdminProjectCreation() && !Roles.isSiteAdmin(user)) {
            displayProjectEditError("Invalid permissions for this operation", data, null);
            return;
        }

        final PopulateItem populator = getSubmittedXFTItem(data);
        if (populator == null) {
            displayProjectEditError("Unable to retrieve the submitted project data. Please check the application logs or with your system administrator for more information.", data, null);
            return;
        }

        final XFTItem submitted = populator.getItem();
        if (displayPopulatorErrors(populator, data, submitted)) {
            return;
        }

        try {
            // Make sure there are no trailing or leading whitespace
            // in any of the project fields
            final XnatProjectdata project = new XnatProjectdata(submitted);
            project.trimProjectFields();

            final PersistentWorkflowI workflow = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, project.getId(), project.getId(), newEventInstance(data, EventUtils.CATEGORY.PROJECT_ADMIN, EventUtils.getAddModifyAction("xnat:projectData", true)));
            final EventMetaI event = workflow.buildEvent();

            final String projectId = project.getId();
            if (StringUtils.isEmpty(projectId)) {
                displayProjectEditError("Project Id cannot be blank.", data, submitted);
                return;
            }

            // Make sure the project doesn't already exist
            final XFTItem existing = project.getItem().getCurrentDBVersion(false);
            if (existing != null) {
                displayProjectEditError("Project '" + projectId + "' already exists.", data, submitted);
                return;
            } else {
                // XNAT-2780: Case insensitive check to see if the current Id has already been used. (checks current project table and project history table)
                final Long count = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM xnat_projectdata p FULL JOIN xnat_projectdata_history ph ON p.id = ph.id WHERE LOWER(p.id) = '" + projectId.toLowerCase() + "' OR LOWER(ph.id) = '" + projectId.toLowerCase() + "';", "COUNT", null, null);
                if (count > 0) {
                    displayProjectEditError("Invalid Id: '" + projectId + "' was previously used as a project ID and cannot be reused.", data, submitted);
                    return;
                }
            }

            // Validate project fields.  If there are conflicts, build a error message and display it to the user.
            final Collection<String> conflicts = project.validateProjectFields();
            if (!conflicts.isEmpty()) {
                displayProjectConflicts(conflicts, data, submitted);
                return;
            }

            try {
                dynamicPreSave(XDAT.getUserDetails(),project,TurbineUtils.GetDataParameterHash(data), workflow);
            } catch (ModifyItem.CriticalException e) {
                throw e;
            } catch (RuntimeException e) {
                log.error("",e);
                throw e;
            }

            final XnatProjectdata postSave = saveProject(project, submitted, user, event, data, context);
            if (postSave == null) {
                // Just return: error display and handling is already done in method.
                return;
            }

            final ItemI working = postSave.getItem();

            try {
                WorkflowUtils.complete(workflow, event);
                UserHelper.setUserHelper(data.getRequest(), user);
            } catch (Exception e) {
                WorkflowUtils.fail(workflow, event);
                throw e;
            }

            final RunData completed = TurbineUtils.SetSearchProperties(TurbineUtils.setDataItem(data, working), working);
            final String destination = TurbineUtils.HasPassedParameter("destination", completed)
                                       ? (String) TurbineUtils.GetPassedParameter("destination", completed, "AddStep2.vm")
                                       : "XDATScreen_report_xnat_projectData.vm";
            redirectToReportScreen(destination, postSave, completed);
        } catch (Exception e) {
            handleException(data, submitted, e, TurbineUtils.EDIT_ITEM);
        }
    }



    private PopulateItem getSubmittedXFTItem(final RunData data) {
        try {
            final EditScreenA screen = (EditScreenA) ScreenLoader.getInstance().getInstance("XDATScreen_add_xnat_projectData");
            final XFTItem newItem = (XFTItem) screen.getEmptyItem(data);
            return PopulateItem.Populate(data, "xnat:projectData", true, newItem);
        } catch (Exception e) {
            log.error("An error occurred trying to build the submitted XFT item", e);
            return null;
        }
    }

    private XnatProjectdata saveProject(final XnatProjectdata project, final XFTItem submitted, final UserI user, final EventMetaI event, final RunData data, final Context context) {
        try {
            return BaseXnatProjectdata.createProject(project, user, false, false, event, StringUtils.defaultIfBlank((String) TurbineUtils.GetPassedParameter("accessibility", data), "protected"));
        } catch (ValidationException e) {
            final ValidationResults validation = e.getValidation();
            if (!validation.isValid()) {
                context.put("vr", validation);
                displayProjectEditError(data, submitted);
            }
        } catch (XftItemLifecycleException e) {
            if (e.getPhase() == Save) {
                displayProjectEditError("Error Saving item.", data, submitted);
            } else {
                    displayProjectEditError(e.getMessage(), data, submitted);
            }
        } catch (Exception e) {
            displayProjectEditError(e.getMessage(), data, submitted);
        }
        return null;
    }
}
