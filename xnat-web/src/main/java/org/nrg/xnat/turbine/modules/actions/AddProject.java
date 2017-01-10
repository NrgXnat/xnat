/*
 * web: org.nrg.xnat.turbine.modules.actions.AddProject
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.modules.ScreenLoader;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.services.NrgEventService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.modules.screens.EditScreenA;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.nrg.xnat.utils.WorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@SuppressWarnings("unused")
public class AddProject extends SecureAction {
    private static final Logger logger = LoggerFactory.getLogger(AddProject.class);

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        UserI user = TurbineUtils.getUser(data);
        XFTItem found = null;

        if (TurbineUtils.HasPassedParameter("tag", data)) {
            context.put("tag", TurbineUtils.GetPassedParameter("tag", data));
        }

        if (!XDAT.getSiteConfigPreferences().getUiAllowNonAdminProjectCreation() && !Roles.isSiteAdmin(user)) {
            displayProjectEditError("Invalid permissions for this operation", data, null);
            return;
        }

        try {
            EditScreenA screen = (EditScreenA) ScreenLoader.getInstance().getInstance("XDATScreen_add_xnat_projectData");

            XFTItem newItem = (XFTItem) screen.getEmptyItem(data);

            PopulateItem populator = PopulateItem.Populate(data, "xnat:projectData", true, newItem);
            found = populator.getItem();

            XnatProjectdata project = new XnatProjectdata(found);

            // Make sure there are no trailing or leading whitespace 
            // in any of the project fields
            project.trimProjectFields();

            final PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, project.getId(), project.getId(), newEventInstance(data, EventUtils.CATEGORY.PROJECT_ADMIN, EventUtils.getAddModifyAction("xnat:projectData", true)));
            EventMetaI c = wrk.buildEvent();

            String id = project.getId();
            if (StringUtils.isEmpty(id)) {
                displayProjectEditError("Project Id cannot be blank.", data, found);
                return;
            }

            // Make sure the project doesn't already exist
            XFTItem existing = project.getItem().getCurrentDBVersion(false);
            if (existing != null) {
                displayProjectEditError("Project '" + id + "' already exists.", data, found);
                return;
            } else {
                // XNAT-2780: Case insensitive check to see if the current Id has already been used. (checks current project table and project history table)
                Long count = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM xnat_projectdata p FULL JOIN xnat_projectdata_history ph ON p.id = ph.id WHERE LOWER(p.id) = '" + id.toLowerCase() + "' OR LOWER(ph.id) = '" + id.toLowerCase() + "';", "COUNT", null, null);
                if (count > 0) {
                    displayProjectEditError("Invalid Id: '" + id + "' was previously used as a project ID and cannot be reused.", data, found);
                    return;
                }
            }

            // Validate project fields.  If there are conflicts, build a error message and display it to the user.
            Collection<String> conflicts = project.validateProjectFields();
            if (!conflicts.isEmpty()) {
                StringBuilder conflictStr = new StringBuilder();
                for (String conflict : conflicts) {
                    conflictStr.append(conflict).append("<br/>");
                }
                displayProjectEditError(conflictStr.toString(), data, found);
                return;
            }

            try {
                project.initNewProject(user, false, false, c);
            } catch (Exception e2) {
                displayProjectEditError(e2.getMessage(), data, found);
                return;
            }

            ValidationResultsI vr = null;

            ValidationResultsI temp = project.getItem().validate();
            if (!project.getItem().isValid()) {
                vr = temp;
            }

            if (vr != null) {
                context.put("vr", vr);
                displayProjectEditError(data, project.getItem());
            } else {
                try {
                    SaveItemHelper.authorizedSave(project, TurbineUtils.getUser(data), false, false, c);
                    ItemI temp1 = project.getItem().getCurrentDBVersion(false);
                    if (temp1 != null) {
                        found = (XFTItem) temp1;
                    }
                } catch (Exception e) {
                    logger.error("Error Storing " + found.getXSIType(), e);
                    displayProjectEditError("Error Saving item.", data, found);
                    return;
                }

                XnatProjectdata postSave;
                try {
                    postSave = new XnatProjectdata(found);
                    postSave.getItem().setUser(user);

                    postSave.initGroups();

                    //postSave.initBundles(user);

                    String accessibility = ((String) org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("accessibility", data));
                    if (accessibility == null) {
                        accessibility = "protected";
                    }

                    if (!accessibility.equals("private"))
                        Permissions.setDefaultAccessibility(postSave.getId(), accessibility, true, user, c);

                    Groups.reloadGroupForUser(user, postSave.getId() + "_" + BaseXnatProjectdata.OWNER_GROUP);
                    populator = PopulateItem.Populate(data, "arc:project", true);

                    XFTItem item = populator.getItem();
                    ArcProject arcP = new ArcProject(item);
                    postSave.initArcProject(arcP, user, c);

                    WorkflowUtils.complete(wrk, c);

                    UserHelper.setUserHelper(data.getRequest(),user);
            		final NrgEventService eventService = XDAT.getContextService().getBean(NrgEventService.class);
                    eventService.triggerEvent(new XftItemEvent(XnatProjectdata.SCHEMA_ELEMENT_NAME, postSave.getId(), XftItemEvent.UPDATE));
                } catch (Exception e) {
                    WorkflowUtils.fail(wrk, c);
                    throw e;
                }

                data = TurbineUtils.setDataItem(data, found);
                data = TurbineUtils.SetSearchProperties(data, found);


                if (TurbineUtils.HasPassedParameter("destination", data)) {
                    this.redirectToReportScreen((String) TurbineUtils.GetPassedParameter("destination", data, "AddStep2.vm"), postSave, data);
                } else {
                    this.redirectToReportScreen("XDATScreen_report_xnat_projectData.vm", postSave, data);
                }

            }
        } catch (Exception e) {
            handleException(data, found, e, TurbineUtils.EDIT_ITEM);
        }
    }
}
