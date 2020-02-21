/*
 * web: org.nrg.xnat.turbine.modules.actions.DeleteProjectData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatExperimentdataShareI;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatProjectparticipantI;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.utils.WorkflowUtils;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.nrg.xft.event.XftItemEventI.DELETE;

@Slf4j
public class DeleteProjectData extends SecureAction {
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        final UserI           user      = XDAT.getUserDetails();
        final String          projectId = (String) TurbineUtils.GetPassedParameter("project", data);
        final XnatProjectdata project   = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);

        final boolean deleteProject = TurbineUtils.HasPassedParameter("delete_project", data);
        final boolean removeFiles   = TurbineUtils.HasPassedParameter("removeFiles", data);

        final XftItemEvent.Builder builder = XftItemEvent.builder().action(DELETE);

        boolean preventProjectDelete    = false;
        boolean preventProjectDeleteByP = false;

        if (Permissions.canDelete(user, project)) {
            final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, project.getId(), project.getId(), newEventInstance(data, EventUtils.CATEGORY.PROJECT_ADMIN, EventUtils.getDeleteAction(project.getXSIType())));
            final EventMetaI          ci       = workflow.buildEvent();
            PersistentWorkflowUtils.save(workflow, ci);

            try {
                for (XnatSubjectdata subject : project.getParticipants_participant()) {
                    if (subject != null) {
                        boolean                              preventSubjectDelete    = false;
                        boolean                              preventSubjectDeleteByP = false;
                        final List<XnatSubjectassessordataI> expts                   = subject.getExperiments_experiment();

                        if (!(preventSubjectDelete || preventSubjectDeleteByP) && expts.size() != subject.getSubjectAssessorCount()) {
                            preventSubjectDelete = true;
                        }

                        for (XnatSubjectassessordataI exptI : expts) {
                            if (TurbineUtils.HasPassedParameter("expt_" + exptI.getId().toLowerCase(), data)) {
                                final XnatSubjectassessordata expt = (XnatSubjectassessordata) exptI;

                                if (expt.getProject().equals(project.getId())) {
                                    if (Permissions.canDelete(user, expt)) {
                                        if (removeFiles) {
                                            final List<XFTItem> hash = expt.getItem().getChildrenOfType("xnat:abstractResource", true);

                                            String archivePath = project.getRootArchivePath();
                                            for (XFTItem resource : hash) {
                                                ItemI om = BaseElement.GetGeneratedItem(resource);
                                                if (om instanceof XnatAbstractresource) {
                                                    XnatAbstractresource resourceA = (XnatAbstractresource) om;
                                                    resourceA.deleteWithBackup(archivePath, projectId, user, ci);
                                                    if (!deleteProject) {
                                                        builder.element(resourceA);
                                                    }
                                                }
                                            }
                                        }

                                        SaveItemHelper.authorizedDelete(expt.getItem().getCurrentDBVersion(), user, ci);
                                    } else {
                                        preventSubjectDeleteByP = true;
                                    }
                                } else {
                                    preventSubjectDelete = true;
                                    for (XnatExperimentdataShareI pp : expt.getSharing_share()) {
                                        if (pp.getProject().equals(project.getId())) {
                                            SaveItemHelper.authorizedDelete(((XnatExperimentdataShare) pp).getItem(), user, ci);
                                        }
                                    }
                                }
                            } else {
                                if (exptI instanceof XnatImagesessiondata) {
                                    for (XnatImageassessordataI expt : ((XnatImagesessiondata) exptI).getAssessors_assessor()) {
                                        if (TurbineUtils.HasPassedParameter("expt_" + expt.getId().toLowerCase(), data)) {

                                            if (expt.getProject().equals(project.getId())) {
                                                if (Permissions.canDelete(user, (XnatImageassessordata) expt)) {
                                                    if (removeFiles) {
                                                        final List<XFTItem> hash = ((XnatImageassessordata) expt).getItem().getChildrenOfType("xnat:abstractResource", false);

                                                        String archivePath = project.getRootArchivePath();
                                                        for (XFTItem resource : hash) {
                                                            ItemI om = BaseElement.GetGeneratedItem(resource);
                                                            if (om instanceof XnatAbstractresource) {
                                                                XnatAbstractresource resourceA = (XnatAbstractresource) om;
                                                                resourceA.deleteWithBackup(archivePath, projectId, user, ci);
                                                                if (!deleteProject) {
                                                                    builder.element(resourceA);
                                                                }
                                                            }
                                                        }
                                                    }
                                                    SaveItemHelper.authorizedDelete(((XnatImageassessordata) expt).getItem().getCurrentDBVersion(), user, ci);
                                                    if (!deleteProject) {
                                                        builder.element(((XnatImageassessordata) expt));
                                                    }
                                                } else {
                                                    preventSubjectDeleteByP = true;
                                                }
                                            } else {
                                                preventSubjectDelete = true;
                                                for (XnatExperimentdataShareI pp : expt.getSharing_share()) {
                                                    if (pp.getProject().equals(project.getId())) {
                                                        SaveItemHelper.authorizedDelete(((XnatExperimentdataShare) pp).getItem(), user, ci);
                                                        if (!deleteProject) {
                                                            builder.element((XnatExperimentdataShare) pp);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    preventSubjectDelete = true;
                                }
                            }
                        }

                        if (TurbineUtils.HasPassedParameter("subject_" + subject.getId().toLowerCase(), data)) {
                            if (!subject.getProject().equals(project.getId())) {
                                for (XnatProjectparticipantI pp : subject.getSharing_share()) {
                                    if (pp.getProject().equals(project.getId())) {
                                        SaveItemHelper.authorizedDelete(((XnatProjectparticipant) pp).getItem(), user, ci);
                                        if (!deleteProject) {
                                            builder.element((XnatProjectparticipant) pp);
                                        }
                                    }
                                }
                            } else {
                                if (preventSubjectDelete) {
                                    preventProjectDelete = true;
                                } else if (preventSubjectDeleteByP) {
                                    preventProjectDeleteByP = true;
                                } else {
                                    if (Permissions.canDelete(user, subject)) {
                                        SaveItemHelper.authorizedDelete(subject.getItem().getCurrentDBVersion(), user, ci);
                                        if (!deleteProject) {
                                            builder.element(subject);
                                        }
                                    } else {
                                        preventProjectDeleteByP = true;
                                    }
                                }
                            }
                        }
                    }
                }

                Users.clearCache(user);
                MaterializedView.deleteByUser(user);

                if (deleteProject && !preventProjectDelete && !preventProjectDeleteByP) {
                    SaveItemHelper.authorizedDelete(project.getItem().getCurrentDBVersion(), user, ci);
                    XDAT.triggerEvent(builder.element(project).build());

                    //DELETE field mappings
                    ItemSearch is = ItemSearch.GetItemSearch("xdat:field_mapping", user);
                    is.addCriteria("xdat:field_mapping.field_value", project.getId());
                    Iterator items = is.exec(false).iterator();
                    while (items.hasNext()) {
                        XFTItem item = (XFTItem) items.next();
                        SaveItemHelper.authorizedDelete(item, user, ci);
                    }

                    Groups.deleteGroupsByTag(project.getId(), user, ci);

                    if (XDAT.getBoolSiteConfigurationProperty("security.allowProjectIdReuse", false)) {
                        Date   curr      = new Date();
                        long   timestamp = curr.getTime();
                        String query     = "UPDATE xnat_projectdata_history SET id = '" + project.getId() + timestamp + "' where id = '" + project.getId() + "';";
                        PoolDBUtils.ExecuteNonSelectQuery(query, project.getItem().getDBName(), user.getLogin());
                    }

                    //DELETE storedSearches
                    for (final XdatStoredSearch bundle : project.getBundles()) {
                        try {
                            SaveItemHelper.authorizedDelete(bundle.getItem(), user, ci);
                        } catch (Throwable e) {
                            log.error("", e);
                        }
                    }

                    ArcProject p = project.getArcSpecification();
                    try {
                        if (p != null) {
                            SaveItemHelper.authorizedDelete(p.getItem(), user, ci);
                        }
                    } catch (Throwable e) {
                        log.error("", e);
                    }

                    data.setMessage(project.getId() + " Project Deleted.");
                    data.setScreenTemplate("Index.vm");
                } else if (preventProjectDeleteByP && deleteProject) {
                    data.setMessage(project.getId() + " Failed to delete subject or experiments owned by other projects.  Please modify the ownership of those items and retry the project deletion.");
                    this.redirectToReportScreen(project, data);
                } else if (preventProjectDelete && deleteProject) {
                    data.setMessage(project.getId() + " Failed to delete subject or experiments owned by other projects.  Please modify the ownership of those items and retry the project deletion.");
                    this.redirectToReportScreen(project, data);
                } else {
                    data.setMessage(project.getId() + " Items Deleted.");
                    this.redirectToReportScreen(project, data);
                }
            } catch (Exception e) {
                log.error("", e);
            }
        } else {
            data.setMessage(project.getId() + " Invalid permissions.");
            this.redirectToReportScreen(project, data);
        }
    }
}
