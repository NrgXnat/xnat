/*
 * web: org.nrg.xnat.turbine.modules.actions.ModifyProject
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.services.NrgEventService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatInvestigatordataI;
import org.nrg.xdat.model.XnatProjectdataAliasI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.ValidationUtils.XFTValidator;
import org.nrg.xnat.utils.WorkflowUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
@Slf4j
public class ModifyProject extends SecureAction {
    /**
     * {@inheritDoc}
     */
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        final PopulateItem populator = PopulateItem.Populate(data, "xnat:projectData", true);
        final XFTItem item = populator.getItem();

        if (displayPopulatorErrors(populator, data, item)) {
            return;
        }

        final ValidationResults validation = XFTValidator.Validate(item);
        if (!validation.isValid()) {
            context.put("vr", validation);
            displayProjectEditError(data, item);
            return;
        }

        // Remove trailing and leading whitespace from all project fields.
        final XnatProjectdata project = new XnatProjectdata(item);
        project.trimProjectFields();

        final UserI user = XDAT.getUserDetails();
        if (user == null || !Permissions.canEdit(user, project)) {
            error(new InvalidPermissionException("User cannot modify project " + project.getId()), data);
            return;
        }

        final PersistentWorkflowI workflow = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, project.getId(), project.getId(), newEventInstance(data, EventUtils.CATEGORY.PROJECT_ADMIN));
        final EventMetaI event = workflow.buildEvent();

        try {
            final Collection<String> conflicts = project.validateProjectFields();
            if (!conflicts.isEmpty()) {
                displayProjectConflicts(conflicts, data, item);
                return;
            }

            try {
                project.initNewProject(user, false, true, event);
            } catch (Exception e) {
                TurbineUtils.SetEditItem(item, data);
                displayProjectEditError(e.getMessage(), data, item);
                return;
            }

            removeExcessInvestigators(project, user);
            removeExcessAliases(project, user);
            SaveItemHelper.authorizedSave(item, user, false, false, event);

            final XnatProjectdata postSave = new XnatProjectdata(item);
            postSave.getItem().setUser(user);
            postSave.initGroups();

            Groups.reloadGroupForUser(user, postSave.getId() + "_" + BaseXnatProjectdata.OWNER_GROUP);
            Users.clearCache(user);

            final String accessibility = StringUtils.defaultIfBlank((String) TurbineUtils.GetPassedParameter("accessibility", data), "protected");
            Permissions.setDefaultAccessibility(project.getId(), accessibility, true, user, event);

            final String destination = TurbineUtils.HasPassedParameter("destination", data)
                                       ? (String) TurbineUtils.GetPassedParameter("destination", data, "AddStep2.vm")
                                       : "XDATScreen_report_xnat_projectData.vm";
            redirectToReportScreen(destination, postSave, data);

            WorkflowUtils.complete(workflow, event);
            Users.clearCache(user);

            final NrgEventService eventService = XDAT.getContextService().getBean(NrgEventService.class);
            eventService.triggerEvent(new XftItemEvent(XnatProjectdata.SCHEMA_ELEMENT_NAME, postSave.getId(), XftItemEvent.UPDATE));
        } catch (SecurityException e) {
            log.error("Security exception triggered by user '{}': {}", user.getUsername(), e.getMessage(), e);
            handleException(data, project.getItem(), e, TurbineUtils.EDIT_ITEM);
        } catch (Exception e) {
            log.error("An unexpected error occurred", e);
            WorkflowUtils.fail(workflow, event);
        }
    }

    /**
     * Inelegant solution to the need to be able to remove investigators from a project.
     *
     * @param project The project from which the PI should be removed.
     * @param user    The user removing the PI.
     *
     * @throws Exception When an unexpected error occurs.
     */
    private void removeExcessInvestigators(XnatProjectdata project, UserI user) throws Exception {
        // get a List of investigators on the project to be saved
        final List<Integer> investigatorIds = Lists.transform(Lists.newArrayList(Iterables.filter(project.getInvestigators_investigator(), INVESTIGATOR_ID_FILTER)), INVESTIGATOR_ID_FUNCTION);

         // if there are investigators, we don't want to delete them, so create a statement to exclude them from the delete
        final String supplementaryClause = !investigatorIds.isEmpty() ? " AND xnat_investigatordata_xnat_investigatordata_id NOT IN (" + StringUtils.join(investigatorIds, ", ") + ")" : "";
        final String query = "DELETE FROM xnat_projectdata_investigator WHERE xnat_projectdata_id = '" + project.getId() + "'" + supplementaryClause;
        PoolDBUtils.ExecuteNonSelectQuery(query, user.getDBName(), user.getUsername());
    }

    /**
     * Inelegant solution to the need to be able to remove aliases from a project.
     *
     * @param project The project from which the aliases should be removed.
     * @param user    The user removing the aliases.
     *
     * @throws Exception When an unexpected error occurs.
     */
    private void removeExcessAliases(XnatProjectdata project, UserI user) throws Exception {
        // get a List of aliases on the project to be saved
        final List<String> aliases = Lists.transform(Lists.newArrayList(Iterables.filter(project.getAliases_alias(), ALIAS_FILTER)), ALIAS_FUNCTION);

        // if there are aliases, we don't want to delete them, so create a statement to exclude them from the delete
        final String supplementaryClause;
        if (!aliases.isEmpty()) {
            supplementaryClause = " AND alias NOT IN ('" + StringUtils.join(aliases, "', '") + "')";
        } else {
            supplementaryClause = "";
        }
        final String query = "DELETE FROM xnat_projectdata_alias WHERE aliases_alias_xnat_projectdata_id = '" + project.getId() + "'" + supplementaryClause;
        PoolDBUtils.ExecuteNonSelectQuery(query, user.getDBName(), user.getUsername());
    }

    private static final Function<XnatProjectdataAliasI, String> ALIAS_FUNCTION = new Function<XnatProjectdataAliasI, String> () {
        @Override
        public String apply(final XnatProjectdataAliasI alias) {
            return alias.getAlias();
        }
    };

    private static final Predicate<XnatProjectdataAliasI> ALIAS_FILTER = new Predicate<XnatProjectdataAliasI>() {
        @Override
        public boolean apply(final XnatProjectdataAliasI alias) {
            return alias != null && StringUtils.isNotBlank(alias.getAlias());
        }
    };

    private static final Function<XnatInvestigatordataI, Integer> INVESTIGATOR_ID_FUNCTION = new Function<XnatInvestigatordataI, Integer>() {
        @Override
        public Integer apply(final XnatInvestigatordataI investigator) {
            return investigator.getXnatInvestigatordataId();
        }
    };

    private static final Predicate<XnatInvestigatordataI> INVESTIGATOR_ID_FILTER = new Predicate<XnatInvestigatordataI>() {
        @Override
        public boolean apply(@Nullable final XnatInvestigatordataI investigator) {
            return investigator != null && investigator.getXnatInvestigatordataId() != null;
        }
    };
}
