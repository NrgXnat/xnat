/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatProjectdata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.om.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.entities.ScriptTriggerTemplate;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.automation.services.ScriptTriggerTemplateService;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.SecurityValues;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.cache.UserItemCache;
import org.nrg.xdat.shared.OmUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.EventRequirementAbsent;
import org.nrg.xft.exception.*;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.data.Status;

import javax.annotation.Nullable;
import java.io.File;
import java.io.Serial;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.nrg.xft.event.XftItemEventI.CREATE;
import static org.nrg.xft.event.XftItemEventI.DELETE;
import static org.nrg.xft.event.XftItemEventI.UPDATE;
import static org.nrg.xft.event.XftItemLifecyclePhase.*;

/**
 * @author XDAT
 */
@SuppressWarnings({"unchecked", "rawtypes", "WeakerAccess", "unused", "RedundantThrows", "deprecation", "DuplicateThrows"})
@Slf4j
public class BaseXnatProjectdata extends AutoXnatProjectdata implements ArchivableItem {
    @Serial
    private static final long serialVersionUID = 1;
    public static final String OWNER_GROUP        = Groups.OWNER_GROUP;
    public static final String MEMBER_GROUP       = Groups.MEMBER_GROUP;
    public static final String COLLABORATOR_GROUP = Groups.COLLABORATOR_GROUP;

    public static final Function<XdatStoredSearch, String> STORED_SEARCH_TO_STRING_FUNCTION = search -> search.getId() + ": " + search.getTag();

    public BaseXnatProjectdata(ItemI item) {
        super(item);
    }

    public BaseXnatProjectdata(UserI user) {
        super(user);
    }

    public BaseXnatProjectdata() {
    }

    public BaseXnatProjectdata(Hashtable properties, UserI user) {
        super(properties, user);
    }

    public int getSubjectCount() {
        try {
            final XFTTable table = XFTTable.Execute("SELECT COUNT(*) FROM (SELECT DISTINCT subject_id,project FROM (SELECT pp.subject_id,pp.project FROM xnat_projectparticipant pp LEFT JOIN xnat_subjectData sub ON pp.subject_id=sub.id WHERE sub.id IS NOT NULL UNION SELECT ID,project FROM xnat_subjectdata )SEARCH )SEARCH WHERE project='" + getId() + "';", getDBName(), null);
            final Long     count = (Long) table.getFirstObject();
            if (count != null) {
                return count.intValue();
            }
        } catch (SQLException | DBPoolException e) {
            logger.error("An error occurred accessing the database or connection while trying to get a subject count for the project " + getId(), e);
        }
        return -1;
    }

    public Hashtable<String, Long> getExperimentCountByName() {
        final Hashtable<String, Long> hash = new Hashtable<>();
        try {
            final XFTTable table = XFTTable.Execute("SELECT COUNT(*) AS expt_count,element_name FROM (SELECT DISTINCT project,sharing_share_xnat_experimentda_id,extension FROM (SELECT exs.project, sharing_share_xnat_experimentda_id,extension FROM xnat_experimentdata_share exs LEFT JOIN xnat_experimentData ex ON exs.sharing_share_xnat_experimentda_id=ex.id WHERE ex.id IS NOT NULL UNION SELECT project,ID,extension FROM xnat_experimentdata) SEARCH )SEARCH LEFT JOIN xdat_meta_element ON  SEARCH.extension=xdat_meta_element.xdat_meta_element_id WHERE project='" + getId() + "' GROUP BY element_name;", getDBName(), null);
            table.resetRowCursor();
            while (table.hasMoreRows()) {
                final Object[] row     = table.nextRow();
                final Long     count   = (Long) row[0];
                final String   initial = (String) row[1];
                try {
                    final SchemaElement schemaElement = SchemaElement.GetElement(initial);
                    hash.put(schemaElement.getProperName(), count);
                } catch (XFTInitException e) {
                    logger.error("An error occurred accessing XFT while trying to create a new element security entry for an item of type {}" + SCHEMA_ELEMENT_NAME, e);
                    hash.put(initial, count);
                } catch (ElementNotFoundException e) {
                    logger.error("Couldn't find the element " + e.ELEMENT + " while trying to create a new element security entry for an item of type " + SCHEMA_ELEMENT_NAME, e);
                    hash.put(initial, count);
                }
            }
        } catch (SQLException | DBPoolException e) {
            logger.error("An error occurred accessing the database or connection while trying to get a subject count for the project " + getId(), e);
        }
        return hash;
    }

    public Hashtable<String, Long> getExperimentCountByXSIType() {
        final Hashtable<String, Long> exptCountsByType = new Hashtable<>();
        try {
            final XFTTable table = XFTTable.Execute("SELECT COUNT(*) AS expt_count,element_name FROM (SELECT DISTINCT project,sharing_share_xnat_experimentda_id,extension FROM (SELECT exs.project, sharing_share_xnat_experimentda_id,extension FROM xnat_experimentdata_share exs LEFT JOIN xnat_experimentData ex ON exs.sharing_share_xnat_experimentda_id=ex.id WHERE ex.id IS NOT NULL UNION SELECT project,ID,extension FROM xnat_experimentdata) SEARCH )SEARCH LEFT JOIN xdat_meta_element ON  SEARCH.extension=xdat_meta_element.xdat_meta_element_id WHERE project='" + getId() + "' GROUP BY element_name;", getDBName(), null);
            table.resetRowCursor();
            while (table.hasMoreRows()) {
                final Object[] row      = table.nextRow();
                final Long     count    = (Long) row[0];
                final String   elementN = (String) row[1];
                if (elementN == null) {
                    logger.error("Found " + count + " experiment(s) that are not associated with a valid data type. Please check for relevant messages in other log files.");
                } else {
                    exptCountsByType.put(elementN, count);
                }
            }
        } catch (SQLException | DBPoolException e) {
            logger.error("An error occurred accessing the database or connection while trying to get an experiment count by data type for the project " + getId(), e);
        }

        return exptCountsByType;
    }

    public Hashtable<String, Long> getScanCountByXSIType() {
        final Hashtable<String, Long> scanCountsByType = new Hashtable<>();
        try {
            final XFTTable table = XFTTable.Execute("SELECT COUNT(*) AS expt_count,element_name FROM (SELECT DISTINCT project,sharing_share_xnat_imagescandat_xnat_imagescandata_id,extension FROM (SELECT exs.project, sharing_share_xnat_imagescandat_xnat_imagescandata_id,extension FROM xnat_imageScanData_share exs LEFT JOIN xnat_imageScanData ex ON exs.sharing_share_xnat_imagescandat_xnat_imagescandata_id=ex.xnat_imagescandata_id WHERE ex.xnat_imagescandata_id IS NOT NULL UNION SELECT project,xnat_imagescandata_id,extension FROM xnat_imageScanData) SEARCH )SEARCH LEFT JOIN xdat_meta_element ON  SEARCH.extension=xdat_meta_element.xdat_meta_element_id WHERE project='" + getId() + "' GROUP BY element_name;", getDBName(), null);
            table.resetRowCursor();
            while (table.hasMoreRows()) {
                final Object[] row      = table.nextRow();
                final Long     count    = (Long) row[0];
                final String   elementN = (String) row[1];
                scanCountsByType.put(elementN, count);
            }
        } catch (SQLException | DBPoolException e) {
            logger.error("An error occurred accessing the database or connection while trying to get an experiment count by data type for the project " + getId(), e);
        }

        return scanCountsByType;
    }

    public ArrayList<XnatPublicationresourceI> getPublicationsByType(final String type) {
        return getPublications_publication().stream().filter(Objects::nonNull).filter(resource -> StringUtils.equals(type, resource.getType())).collect(Collectors.toCollection(ArrayList::new));
    }

    public String getShortenedDescription() {
        if (getDescription() == null) {
            return "";
        }
        if (getDescription().length() > 500) {
            return getDescription().substring(0, 499) + "...";
        } else {
            return getDescription();
        }
    }

    public String createID(String base, int digits) throws Exception {
        return StringUtils.isNotBlank(base) ? incrementID(StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(base, " ", ""), "-", "_"), "\"", ""), "'", ""), digits) : "";
    }

    public String createID(String base) throws Exception {
        return createID(base, -1);
    }

    private String incrementID(final String start, final int digits) throws Exception {
        if (StringUtils.isBlank(start)) {
            throw new NullPointerException();
        }

        final XFTTable  table      = TableSearch.Execute("SELECT id FROM xnat_projectdata WHERE id LIKE '" + start + "%';", getSchemaElement().getDbName(), null);
        final ArrayList projectIds = table.convertColumnToArrayList("id");

        final NumberFormat formatter = NumberFormat.getIntegerInstance();
        formatter.setMinimumIntegerDigits(digits);

        if (projectIds.isEmpty()) {
            return start + formatter.format(1);
        }

        int    count       = projectIds.size() + 1;
        String temporaryId = start + StringUtils.replace(formatter.format(count), ",", "");
        while (projectIds.contains(temporaryId)) {
            count++;
            temporaryId = start + StringUtils.replace(formatter.format(count), ",", "");
        }
        return temporaryId;
    }

    public String setId(final XnatInvestigatordataI investigator) throws Exception {
        if (investigator == null) {
            throw new NullPointerException();
        }

        final String tempId = createID(investigator.getLastname());
        setId(tempId);
        return tempId;
    }

    public ArrayList<XnatSubjectdata> getParticipants_participant() {
        if (_participants.isEmpty()) {
            final UserI              user = getUser();
            final CriteriaCollection cc   = new CriteriaCollection("OR");
            cc.addClause("xnat:subjectData/project", getId());
            cc.addClause("xnat:subjectData/sharing/share/project", getId());
            _participants.addAll(XnatSubjectdata.getXnatSubjectdatasByField(cc, user, false));
        }
        return _participants;
    }

    public boolean getProjectHasSharedSubjects() throws DBPoolException, SQLException {
        final XFTTable table = XFTTable.Execute("SELECT COUNT(pp.project) FROM xnat_projectparticipant AS pp WHERE pp.project = '" + getId() + "';", getDBName(), null);
        final Long count = (Long) table.getFirstObject();
        return count > 0;
    }

    public XFTTable getSubjectsByProject() throws DBPoolException, SQLException {
        final XFTTable subjectTable = XFTTable.Execute("SELECT label, origProject, originalLabel FROM (SELECT DISTINCT * FROM (SELECT pp.label,pp.project,sub.project AS origProject,sub.label AS originalLabel FROM xnat_projectparticipant pp LEFT JOIN xnat_subjectData sub ON pp.subject_id=sub.id WHERE sub.id IS NOT NULL UNION SELECT label, project, project AS origProject, label AS originalLabel FROM xnat_subjectdata)SEARCH )SEARCH WHERE project='" + getId() + "';", getDBName(), null);
        return subjectTable;
    }


    public ArrayList<XnatExperimentdata> getExperiments() {
        if (_experiments.isEmpty()) {
            final UserI              user = getUser();
            final CriteriaCollection cc   = new CriteriaCollection("OR");
            cc.addClause("xnat:experimentData/project", getId());
            cc.addClause("xnat:experimentData/sharing/share/project", getId());
            _experiments.addAll(XnatExperimentdata.getXnatExperimentdatasByField(cc, user, false));
        }
        return _experiments;
    }

    public boolean getProjectHasSharedExperiments() throws DBPoolException, SQLException {
        final XFTTable table = XFTTable.Execute("SELECT COUNT(shr.project) FROM xnat_experimentdata_share AS shr WHERE shr.project = '" + getId() + "';", getDBName(), null);
        final Long count = (Long) table.getFirstObject();
        return count > 0;
    }
    public XFTTable getExperimentsByProject() throws DBPoolException, SQLException {
        final XFTTable experimentTable = XFTTable.Execute(
                "SELECT shr.label, exp.project AS origProject, exp.label AS originalLabel, " +
                "exp.id AS id, meta.element_name, ia.imagesession_id, " +
                "parent_exp.project AS parent_session_project, parent_exp.label AS parent_session_label " +
                "FROM xnat_experimentdata_share shr " +
                "LEFT JOIN xnat_experimentdata AS exp ON shr.sharing_share_xnat_experimentda_id = exp.id " +
                "LEFT JOIN xdat_meta_element meta ON exp.extension = meta.xdat_meta_element_id " +
                "LEFT JOIN xnat_imageassessordata ia ON exp.id = ia.id " +
                "LEFT JOIN xnat_experimentdata parent_exp ON ia.imagesession_id = parent_exp.id " +
                "WHERE shr.project='" + getId() + "' " +
                "UNION " +
                "SELECT exp.label, exp.project AS origProject, exp.label AS originalLabel, " +
                "exp.id AS id, meta.element_name, ia.imagesession_id, " +
                "parent_exp.project AS parent_session_project, parent_exp.label AS parent_session_label " +
                "FROM xnat_experimentdata exp " +
                "LEFT JOIN xdat_meta_element meta ON exp.extension = meta.xdat_meta_element_id " +
                "LEFT JOIN xnat_imageassessordata ia ON exp.id = ia.id " +
                "LEFT JOIN xnat_experimentdata parent_exp ON ia.imagesession_id = parent_exp.id " +
                "WHERE exp.project='" + getId() + "';",
                getDBName(), null);
        return experimentTable;
    }


    public ArrayList getExperimentsByXSIType(final String xsiType) {
        return getExperiments().stream().filter(experiment -> experiment != null && StringUtils.equalsIgnoreCase(xsiType, experiment.getXSIType())).collect(Collectors.toCollection(ArrayList::new));
    }

    public Hashtable getFieldsByName() {
        if (_fieldsByName.isEmpty()) {
            _fieldsByName.putAll(getFields_field().stream().collect(Collectors.toMap(XnatProjectdataFieldI::getName, XnatProjectdataField.class::cast)));
        }
        return _fieldsByName;
    }

    public Object getFieldByName(String s) {
        final XnatProjectdataFieldI field = (XnatProjectdataFieldI) getFieldsByName().get(s);
        return field != null ? field.getField() : null;
    }

    public ArrayList<String> getOwnerEmails() throws Exception {
        return getOwners();
    }

    public ArrayList<String> getOwners() throws Exception {
        final XFTTable table2 = XFTTable.Execute("SELECT DISTINCT email FROM xdat_element_access ea LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id=fms.permissions_allow_set_xdat_elem_xdat_element_access_id LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id=fm.xdat_field_mapping_set_xdat_field_mapping_set_id LEFT JOIN xdat_userGroup ug ON ea.xdat_usergroup_xdat_usergroup_id=ug.xdat_usergroup_id LEFT JOIN xdat_user_groupid map ON ug.id=map.groupid LEFT JOIN xdat_user u ON map.groups_groupid_xdat_user_xdat_user_id=u.xdat_user_id  WHERE read_element=1 AND delete_element=1 AND login !='guest' AND element_name='xnat:subjectData' AND field_value='" + getId() + "' ORDER BY email;", getDBName(), null);
        return table2.convertColumnToArrayList("email");
    }

    public String getSubjectSummary() {
        return getSubjectCount() + " Subjects ";
    }

    public String getRootArchivePath() {
        String path = null;

        final ArcProject arcProj = getArcSpecification();
        if (arcProj != null) {
            ArcPathinfo pathInfo = arcProj.getPaths();
            if (pathInfo != null) {
                path = pathInfo.getArchivepath();
            }
        }

        if (path == null) {
            path = Path.of(ArcSpecManager.GetInstance().getGlobalArchivePath(), getId()).toString();
        }

        path = path.replace('\\', '/');
        if (!path.endsWith("/")) {
            path = path + "/";
        }

        return path;
    }

    public String getCachePath() {
        String path = null;

        final ArcProject arcProj = getArcSpecification();
        if (arcProj != null) {
            ArcPathinfoI pathInfo = arcProj.getPaths();
            if (pathInfo != null) {
                path = pathInfo.getCachepath();
            }
        }

        if (path == null) {
            path = ArcSpecManager.GetInstance().getGlobalCachePath() + "/" + getId();
        }

        path = path.replace('\\', '/');
        if (!path.endsWith("/")) {
            path = path + "/";
        }

        return path;
    }

    public String getPrearchivePath() {
        String path = null;

        final ArcProject arcProj = getArcSpecification();
        if (arcProj != null) {
            ArcPathinfoI pathInfo = arcProj.getPaths();
            if (pathInfo != null) {
                path = pathInfo.getPrearchivepath();
            }
        }

        if (path == null) {
            path = ArcSpecManager.GetInstance().getGlobalPrearchivePath() + "/" + getId();
        }

        path = path.replace('\\', '/');
        if (!path.endsWith("/")) {
            path = path + "/";
        }

        return path;
    }

    public String getBuildPath() {
        String path = null;

        final ArcProject arcProj = getArcSpecification();
        if (arcProj != null) {
            ArcPathinfoI pathInfo = arcProj.getPaths();
            if (pathInfo != null) {
                path = pathInfo.getBuildpath();
            }
        }

        if (path == null) {
            path = ArcSpecManager.GetInstance().getGlobalBuildPath() + "/" + getId();
        }

        path = path.replace('\\', '/');
        if (!path.endsWith("/")) {
            path = path + "/";
        }

        return path;
    }

    public String getCurrentArc() {
        final ArcProject arcProj = getArcSpecification();
        return arcProj == null ? "arc001" : StringUtils.defaultIfBlank(arcProj.getCurrentArc(), "arc001");
    }

    public ArrayList<String> getGroupMembers(String level) {
        try {
            final XFTTable table = XFTTable.Execute("SELECT DISTINCT email FROM xdat_user RIGHT JOIN xdat_user_groupid xug ON xdat_user.xdat_user_id=xug.groups_groupid_xdat_user_xdat_user_id WHERE groupid='" + getId() + "_" + level + "';", getDBName(), null);
            return table.convertColumnToArrayList("email");
        } catch (SQLException | DBPoolException e) {
            logger.error("An error occurred accessing the database or connection while trying to get a subject count for the project " + getId(), e);
        }

        return null;
    }

    public ArrayList<String> getGroupMembersByGroupID(String groupid) {
        try {
            final XFTTable table = XFTTable.Execute("SELECT DISTINCT email FROM xdat_user RIGHT JOIN xdat_user_groupid xug ON xdat_user.xdat_user_id=xug.groups_groupid_xdat_user_xdat_user_id WHERE groupid='" + groupid + "';", getDBName(), null);
            return table.convertColumnToArrayList("email");
        } catch (SQLException | DBPoolException e) {
            logger.error("An error occurred accessing the database or connection while trying to get a subject count for the project " + getId(), e);
        }

        return null;
    }

    public List<UserGroupI> getGroups() throws Exception {
        return Groups.getGroupsByTag(getId());
    }

    public ArrayList<List> getGroupIDs() {
        try {
            final XFTTable groups = XFTTable.Execute("SELECT id,displayname FROM xdat_usergroup WHERE tag='" + getId() + "' ORDER BY displayname DESC", getDBName(), null);
            return groups.toArrayListOfLists();
        } catch (Exception e) {
            logger.error("An unknown error occurred retrieving group IDs associated with the project " + getId(), e);
            return new ArrayList();
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public UserGroupI addGroupMember(String groupId, UserI newUser, UserI currentUser, EventMetaI ci) throws Exception {
        if (!Permissions.canDelete(currentUser, this) && !currentUser.getLogin().equals(newUser.getLogin())) {//equal user skips security here.
            throw new InvalidPermissionException("User cannot modify project " + getId());
        }
        return Groups.addUserToGroup(groupId, newUser, currentUser, ci);
    }

    public void removeGroupMember(String group_id, UserI newUser, UserI currentUser, EventDetails ci) throws Exception {
        if (!Permissions.canDelete(currentUser, this)) {
            throw new InvalidPermissionException("User cannot modify project " + getId());
        }

        if (Groups.isMember(newUser, group_id)) {
            PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(currentUser, Users.getUserDataType(), newUser.getID().toString(), getId(), ci);
            try {
                Groups.removeUserFromGroup(newUser, currentUser, group_id, wrk.buildEvent());

                PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
            } catch (Exception e) {
                PersistentWorkflowUtils.fail(wrk, wrk.buildEvent());
                throw e;
            }
        }
    }


    private List<ElementSecurity> getSecuredElements() {
        try {
            return ElementSecurity.GetElementSecurities().values().stream().filter(es -> {
                try {
                    if (es != null && es.isSecure() && (es.getSchemaElement().getGenericXFTElement().instanceOf(XnatSubjectdata.SCHEMA_ELEMENT_NAME) || es.getSchemaElement().getGenericXFTElement().instanceOf(XnatExperimentdata.SCHEMA_ELEMENT_NAME))) {
                        es.initPSF(es.getElementName() + "/project", EventUtils.DEFAULT_EVENT(getUser(), null));
                        es.initPSF(es.getElementName() + "/sharing/share/project", EventUtils.DEFAULT_EVENT(getUser(), null));
                        return true;
                    }
                } catch (Exception e) {
                    logger.error("An error occurred processing an element security object for " + es.getSchemaElementName(), e);
                }
                return false;
            }).collect(Collectors.toCollection(ArrayList::new));
        } catch (Exception e) {
            logger.error("An error occurred processing element security objects for the project " + getId(), e);
            return Collections.emptyList();
        }
    }

    public List<UserGroupI> initGroups() throws Exception {
        final long startTime = Calendar.getInstance().getTimeInMillis();

        try {
            logger.info("Group init() BEGIN: " + startTime + "ms");
            final List<ElementSecurity> securedElements = getSecuredElements();
            return Groups.createOrUpdateProjectGroups(getId(), getUser());
        } finally {
            final long endTime = Calendar.getInstance().getTimeInMillis();
            logger.info("Group init() COMPLETE: " + endTime + "ms, " + (endTime - startTime) + " ms elapsed.");
        }
    }

    public static void quickSave(final XnatProjectdata project, final UserI user, final boolean allowDataDeletion, final boolean overrideSecurity, final EventMetaI eventMeta) throws Exception {
        project.initNewProject(user, allowDataDeletion, true, eventMeta);

        SaveItemHelper.authorizedSave(project, user, overrideSecurity, false, eventMeta);
        XFTItem item = project.getItem().getCurrentDBVersion(false);

        XnatProjectdata postSave = new XnatProjectdata(item);
        postSave.getItem().setUser(user);

        postSave.initGroups();

        Groups.reloadGroupForUser(user, postSave.getId() + "_" + OWNER_GROUP);

        // Use impl method, which skips permissions check: the user is creating the project, we KNOW the user can edit/delete the project.
        postSave.initArcProjectImpl(user, eventMeta);

        Users.clearCache(user);
        ElementSecurity.refresh();
    }

    public XnatAbstractprotocol getProtocolByDataType(final String elementName) {
        return (XnatAbstractprotocol) getStudyprotocol().stream().filter(Objects::nonNull).filter(protocol -> StringUtils.equals(protocol.getDataType(), elementName)).findFirst().orElse(null);
    }

    public ArrayList<XdatStoredSearch> getBundles() {
        final ArrayList<XdatStoredSearch> searches = XdatStoredSearch.GetSearches("xdat:stored_search/tag", getId(), true);
        final Map<String, Long>           counts   = getExperimentCountByXSIType();
        counts.putAll(getScanCountByXSIType());

        if (needsStoredSearchForDataType(XnatSubjectdata.SCHEMA_ELEMENT_NAME, searches)) {
            searches.add(getStoredSearchForDataType(XnatSubjectdata.SCHEMA_ELEMENT_NAME));
        }

        if (log.isDebugEnabled()) {
            log.debug("Found counts for data types: {}", String.join(", ", counts.keySet()));
            log.debug("Found searches for data types: {}", searches.stream().map(XdatStoredSearch::getId).collect(Collectors.joining(", ")));
        }

        try {
            final UserI user = getUser();
            for (final String key : counts.keySet()) {
                if (Permissions.can(user, key+"/project",this.getId(), SecurityManager.READ)) {// this check only really impacts Custom User Groups.  In standard groups, a user with acces to the project, has access to all the data in it (not just some of it).
                    log.debug("Checking if stored search needed for {}", key);
                    if (needsStoredSearchForDataType(key, searches)) {
                        log.info("Stored search for {}", key);
                        final XdatStoredSearch search = getStoredSearchForDataType(key);
                        if (search != null) {
                            log.debug("I got a stored search for {}: {}", key, search.getId());
                            searches.add(search);
                        } else {
                            log.error("Erroneous data (rows={}) of type '{}' in project '{}'.", counts.get(key), key, getId());
                        }
                    }
                } else {
                    log.info("User {} doesn't have read access to the data type {}, so not checking for stored search", user.getUsername(), key);
                }
            }
            dynamicSearchAdd(user, searches, this);
        } catch (Exception e) {
            log.error("An error occurred trying to retrieve the stored search bundles for project {}", getId(), e);
        }
        return searches;
    }

    public interface AddBundlesI {
        void execute(UserI user, List<XdatStoredSearch> storedSearches, BaseXnatProjectdata proj) throws Exception;
    }

    public XdatStoredSearch getDefaultSearch(final String elementName) {
        XdatStoredSearch storedSearch = null;
        try {
            final ElementSecurity elementSecurity = ElementSecurity.GetElementSecurity(elementName);

            storedSearch = getDefaultSearch(elementName, getId() + "_" + elementName);
            storedSearch.setId(getId() + "_" + elementName);
            storedSearch.setBriefDescription(elementSecurity != null ? elementSecurity.getPluralDescription() : elementName);
            storedSearch.setSecure(false);
            storedSearch.setAllowDiffColumns(false);
            storedSearch.setTag(getId());

            final UserI user = getUser();

            final XnatAbstractprotocol protocol = getProtocolByDataType(elementName);
            if (protocol != null) {
                if (protocol instanceof XnatDatatypeprotocol datatypeprotocol) {
                    for (final XnatFielddefinitiongroupI group : datatypeprotocol.getDefinitions_definition()) {
                        for (final XnatFielddefinitiongroupFieldI field : group.getFields_field()) {
                            final XdatSearchField searchField = new XdatSearchField(getUser());
                            searchField.setElementName(protocol.getDataType());
                            String fieldId = null;
                            if (field.getType().equals("custom")) {
                                fieldId = protocol.getDatatypeSchemaElement().getSQLName().toUpperCase() + "_FIELD_MAP=" + field.getName().toLowerCase();
                            } else {
                                try {
                                    final SchemaElement schemaElement = SchemaElement.GetElement(protocol.getDataType());
                                    try {
                                        final DisplayField displayField = schemaElement.getDisplayFieldForXMLPath(field.getXmlpath());
                                        if (displayField != null) {
                                            fieldId = displayField.getId();
                                        }
                                    } catch (Exception e) {
                                        logger.error("An error occurred trying to get display fields for the schema element " + schemaElement.getProperName(), e);
                                    }
                                } catch (XFTInitException e) {
                                    logger.error("An error occurred accessing XFT while trying to create a new element security entry for an item of type {}" + SCHEMA_ELEMENT_NAME, e);
                                } catch (ElementNotFoundException e) {
                                    logger.error("Couldn't find the element " + e.ELEMENT + " while trying to create a new element security entry for an item of type " + SCHEMA_ELEMENT_NAME, e);
                                }
                            }

                            if (StringUtils.isNotBlank(fieldId)) {
                                searchField.setFieldId(fieldId);
                                searchField.setHeader(field.getName());
                                searchField.setType(field.getDatatype());
                                searchField.setSequence(storedSearch.getSearchField().size());
                                if (field.getType().equals("custom")) {
                                    searchField.setValue(field.getName().toLowerCase());
                                }
                                try {
                                    storedSearch.setSearchField(searchField);
                                    logger.info("Loaded the field definition group field " + field.getXmlpath());
                                } catch (Exception e) {
                                    logger.error("An error occurred trying to set a search field for field ID " + fieldId, e);
                                }
                            } else {
                                logger.warn("Failed to load the field definition group field " + field.getXmlpath());
                            }
                        }
                    }
                }
            }

            if (XDAT.getBoolSiteConfigurationProperty("removeScanAggregateFields", Boolean.FALSE)) {
                int index = 0;
                for(XdatSearchField field: storedSearch.getSearchField()){
                    if(StringUtils.endsWith(field.getFieldId(),"SCAN_COUNT_AGG")){
                        storedSearch.removeSearchField(index);
                        break;
                    }
                    index++;
                }
            }

            if (XDAT.getBoolSiteConfigurationProperty("addCountFieldsToProjectListings", Boolean.TRUE)) {
                final SchemaElement root = SchemaElement.GetElement(elementName);
                if (elementName.equals(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
                    for (final String xsiType : getExperimentCountByXSIType().keySet()) {
                        try {
                            final GenericWrapperElement element = GenericWrapperElement.GetElement(xsiType);
                            if (element.instanceOf(XnatSubjectassessordata.SCHEMA_ELEMENT_NAME)) {
                                //generate a project specific count column
                                final SchemaElement schemaElement = SchemaElement.GetElement(xsiType);
                                final DisplayField displayField = root.getSQLQueryField("CNT_" + schemaElement.getSQLName().toUpperCase(), ElementSecurity.GetPluralDescription(xsiType), true, false, "integer", "sub_project_count", "SELECT COUNT(*) as sub_project_count, subject_id FROM xnat_subjectAssessorData sad LEFT JOIN xnat_experimentData ex ON sad.ID=ex.ID LEFT JOIN xnat_experimentData_meta_data inf ON ex.experimentData_info=inf.meta_data_id JOIN xdat_meta_element xme ON ex.extension=xme.xdat_meta_element_id LEFT JOIN xnat_experimentdata_share sp ON ex.id=sp.sharing_share_xnat_experimentda_id AND sp.project='@WHERE' WHERE xme.element_name='" + xsiType + "' AND (ex.project='@WHERE' OR sp.project='@WHERE') AND (inf.status = 'active' OR inf.status = 'locked' OR inf.status = 'quarantine') GROUP BY subject_id ORDER BY subject_id", "xnat:subjectData.ID", "subject_id");

                                final XdatSearchField searchField = new XdatSearchField(user);
                                searchField.setElementName(XnatSubjectdata.SCHEMA_ELEMENT_NAME);
                                searchField.setFieldId(displayField.getId() + "=" + getId());
                                searchField.setHeader(ElementSecurity.GetPluralDescription(xsiType));
                                searchField.setValue(getId());
                                searchField.setType("integer");
                                searchField.setSequence(storedSearch.getSearchField().size());
                                storedSearch.setSearchField(searchField);
                            }
                        } catch (XFTInitException e) {
                            logger.error("An error occurred accessing XFT while trying to create a new subject search field for type " + xsiType + " for the project " + getId(), e);
                        } catch (ElementNotFoundException e) {
                            logger.error("Couldn't find the element " + e.ELEMENT + " while trying to create a new subject search field for type " + xsiType + " for the project " + getId(), e);
                        } catch (Exception e) {
                            logger.error("An unknown error occurred trying to create a new subject search field for type " + xsiType + " for the project " + getId(), e);
                        }
                    }
                }

                if (root.getGenericXFTElement().instanceOf(XnatImagesessiondata.SCHEMA_ELEMENT_NAME)) {
                    for (String xsiType : getExperimentCountByXSIType().keySet()) {
                        try {
                            final GenericWrapperElement element = GenericWrapperElement.GetElement(xsiType);
                            if (element.instanceOf(XnatImageassessordata.SCHEMA_ELEMENT_NAME)) {
                                //generate a project specific count column
                                final SchemaElement schemaElement = SchemaElement.GetElement(xsiType);
                                final DisplayField displayField = root.getSQLQueryField("CNT_" + schemaElement.getSQLName().toUpperCase(), ElementSecurity.GetPluralDescription(xsiType), true, false, "integer", "mr_project_count", "SELECT COUNT(*) as mr_project_count, imagesession_id FROM xnat_imageAssessorData iad LEFT JOIN xnat_experimentData ex ON iad.ID=ex.ID LEFT JOIN xnat_experimentData_meta_data inf ON ex.experimentData_info=inf.meta_data_id JOIN xdat_meta_element xme ON ex.extension=xme.xdat_meta_element_id LEFT JOIN xnat_experimentdata_share sp ON ex.id=sp.sharing_share_xnat_experimentda_id AND sp.project='@WHERE' WHERE xme.element_name='" + xsiType + "' AND (ex.project='@WHERE' OR sp.project='@WHERE') AND (inf.status = 'active' OR inf.status = 'locked' OR inf.status = 'quarantine') GROUP BY imagesession_id", elementName + ".ID", "imagesession_id");

                                final XdatSearchField searchField = new XdatSearchField(user);
                                searchField.setElementName(elementName);
                                searchField.setFieldId(displayField.getId() + "=" + getId());
                                searchField.setHeader(ElementSecurity.GetPluralDescription(xsiType));
                                searchField.setValue(getId());
                                searchField.setType("integer");
                                searchField.setSequence(storedSearch.getSearchField().size());
                                storedSearch.setSearchField(searchField);
                            }
                        } catch (XFTInitException e) {
                            logger.error("An error occurred accessing XFT while trying to create a new image session search field for type " + xsiType + " for the project " + getId(), e);
                        } catch (ElementNotFoundException e) {
                            logger.error("Couldn't find the element " + e.ELEMENT + " while trying to create a new image session search field for type " + xsiType + " for the project " + getId(), e);
                        } catch (Exception e) {
                            logger.error("An unknown error occurred trying to create a new image session search field for type " + xsiType + " for the project " + getId(), e);
                        }
                    }
                }
            }
        } catch(Exception e){
            logger.error("An unknown error occurred trying to create the default search for element type " + elementName + " for the project " + getId(), e);
        }
        return storedSearch;
    }

    public String getPublicAccessibility() throws Exception {
        final UserI  guest     = Users.getGuest();
        final String projectId = getId();
        if (Permissions.canRead(guest, "xnat:subjectData/project", projectId)) {
            return "public";
        }
        if (Permissions.canRead(guest, "xnat:projectData/ID", projectId)) {
            return "protected";
        }
        return "private";
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemWrapper#save(org.nrg.xft.security.UserI, boolean, boolean)
     */
    @Override
    public boolean save(final UserI user, final boolean overrideSecurity, final boolean allowItemRemoval, final EventMetaI event) throws Exception {
        final String     groupId    = getId() + "_" + OWNER_GROUP;
        final UserGroupI ownerGroup = Groups.getGroup(groupId);
        if (ownerGroup == null) {
            // If there is no owner group, then this must be a new project. Projects are secured.  Thus, you can't just create a project. In order to create
            // a project, you have to be an owner of the project. So, before we actually save the project, we create the owner group and add the user to it.
            final UserGroupI group = Groups.createOrUpdateGroup(groupId, "Owners", true, true, true, true, true, true, getSecuredElements(), getId(), getUser(), Collections.singletonList(user));
            if (group == null) {
                logger.warn("Tried to create or update the group " + groupId + " but didn't get a return value from the method. Please check the log for errors.");
            }
        }

        boolean successful = super.save(user, overrideSecurity, allowItemRemoval, event);
        if (successful) {
            XDAT.triggerXftItemEvent(this, UPDATE);
        }
        return successful;
    }

    public String getDisplayName() {
        return StringUtils.defaultIfBlank(getSecondaryId(), getId());
    }

    public String getDisplayID() {
        return StringUtils.defaultIfBlank(getSecondaryId(), getId());
    }

    public ArcProject getArcSpecification() {
        return ArcSpecManager.GetInstance().getProjectArc(getId());
    }

    public static Comparator GetComparator() {
        return (new BaseXnatProjectdata()).getComparator();
    }

    public Comparator getComparator() {
        return new ProjectIDComparator();
    }

    public static class ProjectIDComparator implements Comparator {
        public ProjectIDComparator() {
        }

        public int compare(Object o1, Object o2) {
            BaseXnatProjectdata value1 = (BaseXnatProjectdata) (o1);
            BaseXnatProjectdata value2 = (BaseXnatProjectdata) (o2);

            if (value1 == null) {
                if (value2 == null) {
                    return 0;
                } else {
                    return -1;
                }
            }
            if (value2 == null) {
                return 1;
            }

            return value1.getId().compareTo(value2.getId());

        }
    }

    public void initNewProject(UserI user, boolean allowDataDeletion, boolean allowMatchingID, EventMetaI c) throws Exception {
        XFTItem db = null;
        if (StringUtils.isBlank(getId())) {
            final String secondaryID = getSecondaryId();
            if (StringUtils.isBlank(secondaryID)) {
                throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, new Exception("Please define a project abbreviation."));
            } else {
                setId(secondaryID);
                db = getCurrentDBVersion();
                if (db != null) {
                    setId("");
                    throw new ClientException(Status.CLIENT_ERROR_CONFLICT, new Exception("Project '" + getId() + "' already exists."));
                }
            }
        } else {
            db = getCurrentDBVersion();
            if (!allowMatchingID) {
                if (db != null) {
                    setId("");
                    throw new ClientException(Status.CLIENT_ERROR_CONFLICT, new Exception("Project '" + getId() + "' already exists."));
                }
            } else if (db != null) {
                if (!Permissions.canEdit(user, db)) {
                    setId("");
                    throw new ClientException(Status.CLIENT_ERROR_CONFLICT, new Exception("Project '" + getId() + "' already exists."));
                }
            }
        }

        if (StringUtils.isBlank(getSecondaryId())) {
            setSecondaryId(getId());
        }

        if (StringUtils.isBlank(getName())) {
            setName(getId());
        }

        if (!XDAT.getSiteConfigPreferences().getUiAllowNonAdminProjectCreation() && !Roles.isSiteAdmin(user)) {
            //check if this is an edit or create (edit is allowed by non-admin owners)
            if (db == null) {
                throw new InvalidPermissionException(user.getUsername(), SCHEMA_ELEMENT_NAME);
            }
        }

        if (!getStudyprotocol().isEmpty()) {
            final Map<String, ElementSecurity> ess = ElementSecurity.GetElementSecurities();

            for (final XnatAbstractprotocolI protocolT : getStudyprotocol()) {
                final XnatAbstractprotocol protocol = (XnatAbstractprotocol) protocolT;
                if (protocol.getProperty("data-type") == null) {
                    if (allowDataDeletion) {
                        //NOT REQUESTED
                        if (protocol.getProperty("xnat_abstractProtocol_id") != null) {
                            try {
                                getItem().getCurrentDBVersion().removeChildFromDB("xnat:projectData/studyProtocol", protocol.getCurrentDBVersion(), user, c);
                                //This may need to use a authorized call instead of the unauthorized call that's inside removeChildFromDB
                            } catch (SQLException e) {
                                logger.error("An error occurred accessing the database or connection while trying to initialize the new project " + getId(), e);
                            } catch (Exception e) {
                                logger.error("An unexpected error occurred while trying to initialize the new project " + getId(), e);
                            }
                        }

                        getItem().removeChild("xnat:projectData/studyProtocol", getItem().getChildItems("xnat:projectData/studyProtocol").indexOf(protocol.getItem()));
                    }
                } else {
                    //REQUESTED
                    final GenericWrapperElement element = GenericWrapperElement.GetElement((String) protocol.getProperty("data-type"));
                    if (protocol.getProperty("ID") == null) {
                        final String value = getItem().getProperty("ID") + "_" + element.getSQLName();
                        try {
                            protocol.setProperty("ID", value);
                        } catch (InvalidValueException e) {
                            logger.error("Got an invalid value exception trying to set the " + element.getName() + " ID property to " + value, e);
                        }
                    }
                    if (protocol.getProperty("name") == null) {
                        protocol.setProperty("name", ess.get(element.getFullXMLName()).getPluralDescription());
                    }

                    if (protocol.getXSIType().equals(XnatDatatypeprotocol.SCHEMA_ELEMENT_NAME)) {
                        protocol.setProperty("xnat:datatypeProtocol/definitions/definition[ID=default]/data-type", protocol.getProperty("data-type"));
                        protocol.setProperty("xnat:datatypeProtocol/definitions/definition[ID=default]/project-specific", "false");
                    }
                }
            }
        }

        for (final XnatInvestigatordataI investigator : getInvestigators_investigator()) {
            if (StringUtils.isBlank(investigator.getFirstname())) {
                final XFTItem temp = ((XnatInvestigatordata) investigator).getCurrentDBVersion();
                investigator.setFirstname(temp.getStringProperty("firstname"));
                investigator.setLastname(temp.getStringProperty("lastname"));
            }
        }
    }

    public void initArcProject(final UserI user, final EventMetaI c) throws Exception {
        initArcProject(null, user, c);
    }

    public void initArcProject(final @Nullable ArcProject arcProject, final UserI user, final EventMetaI c) throws Exception {
        if (!Permissions.canDelete(user, this)) {
            throw new InvalidPermissionException("User cannot modify project " + getId());
        }
        initArcProjectImpl(arcProject, user, c);
    }

    public XdatStoredSearch getDefaultSearch(final String dataType, final String id) {
        try {
            final DisplaySearch search = new DisplaySearch();
            search.setDisplay("project_bundle");
            search.setRootElement(dataType);

            final CriteriaCollection cc = new CriteriaCollection("OR");
            cc.addClause(dataType + "/sharing/share/project", "=", getId());
            cc.addClause(dataType + "/project", "=", getId());

            search.addCriteria(cc);

            final XdatStoredSearch storedSearch = search.convertToStoredSearch(id);
            for (final Object object : storedSearch.getSearchFields()) {
                final XdatSearchField searchField = (XdatSearchField) object;
                if (searchField.getFieldId().endsWith("_PROJECT_IDENTIFIER")) {
                    searchField.setValue(getId());
                    searchField.setFieldId(searchField.getFieldId() + "=" + getId());
                }
            }
            return storedSearch;
        } catch (XFTInitException e) {
            logger.error("An error occurred accessing XFT while trying to get the default search for data type " + dataType + " from stored search " + id + " in project " + getId(), e);
        } catch (ElementNotFoundException e) {
            logger.error("Couldn't find the element " + e.ELEMENT + " while trying to get the default search for data type " + dataType + " from stored search " + id + " in project " + getId(), e);
        } catch (FieldNotFoundException e) {
            logger.error("Couldn't find the field " + e.FIELD + " while trying to get the default search for data type " + dataType + " from stored search " + id + " in project " + getId() + ": " + e.MESSAGE, e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred =while trying to get the default search for data type " + dataType + " from stored search " + id + " in project " + getId(), e);
        }
        return null;
    }

    public static String CleanID(final String id) {
        return StringUtils.replaceAll(id, INVALID_CHAR_REGEX, "_");
    }

    public String checkDelete(final UserI user) {
        try {
            if (!Permissions.canDelete(user, getItem())) {
                return "Invalid delete permissions for this project.";
            }
        } catch (Exception e) {
            logger.error("An unexpected error occurred while trying to check delete permissions for user " + user.getUsername() + " on project " + getId(), e);
            return "An error occurred. Check the system logs.";
        }

        final XnatProjectdata thisOne = (XnatProjectdata) this;
        // return getParticipants_participant().stream().map(subject -> subject.canDelete(thisOne, user)).filter(StringUtils::isNotBlank).findFirst().orElse(null);
        for (final XnatSubjectdata subject : getParticipants_participant()) {
            final String message = subject.canDelete(this, user);
            if (message != null) {
                return message;
            }
        }

        return null;
    }

    public void deleteFiles(final UserI user, final EventMetaI ci) throws Exception {
        OmUtils.deleteResourceFiles(user, getRootArchivePath(), getId(), getResources_resource(), ci);
    }

    public void delete(final boolean removeFiles, final UserI user, final EventMetaI ci) throws SQLException, Exception {
        if (!Permissions.canDelete(user, this)) {
            throw new InvalidPermissionException("User cannot delete project:" + getId());
        }

        if (XDAT.getBoolSiteConfigurationProperty("security.prevent-data-deletion", false)) {
            throw new InvalidPermissionException("User cannot delete project:" + getId());
        }

        boolean preventProjectDelete    = false;
        boolean preventProjectDeleteByP = false;

        for (final XnatSubjectdata subject : getParticipants_participant()) {
            if (subject != null) {
                boolean                              preventSubjectDelete    = false;
                boolean                              preventSubjectDeleteByP = false;
                final List<XnatSubjectassessordataI> experiments             = subject.getExperiments_experiment();

                if (experiments.size() != subject.getSubjectAssessorCount()) {
                    preventSubjectDelete = true;
                }

                for (XnatSubjectassessordataI exptI : experiments) {
                    final XnatSubjectassessordata expt = (XnatSubjectassessordata) exptI;

                    if (expt.getProject().equals(getId())) {
                        if (Permissions.canDelete(user, expt)) {
                            if (removeFiles) {
                                final List<XFTItem> hash = expt.getItem().getChildrenOfType(XnatAbstractresource.SCHEMA_ELEMENT_NAME, true);

                                for (XFTItem resource : hash) {
                                    ItemI om = BaseElement.GetGeneratedItem(resource);
                                    if (om instanceof XnatAbstractresource resourceA) {
                                        resourceA.deleteWithBackup(getRootArchivePath(), this.getId(), user, ci);
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
                            if (pp.getProject().equals(getId())) {
                                SaveItemHelper.authorizedDelete(((XnatExperimentdataShare) pp).getItem(), user, ci);
                            }
                        }
                    }
                }

                if (!subject.getProject().equals(getId())) {
                    for (XnatProjectparticipantI pp : subject.getSharing_share()) {
                        if (pp.getProject().equals(getId())) {
                            SaveItemHelper.authorizedDelete(((XnatProjectparticipant) pp).getItem(), user, ci);
                        }
                    }
                } else {
                    if (preventSubjectDelete) {
                        preventProjectDelete = true;
                    } else if (preventSubjectDeleteByP) {
                        preventProjectDeleteByP = true;
                    } else {
                        if (Permissions.canDelete(user, subject)) {
                            if (removeFiles) {
                                final List<XFTItem> hash = subject.getItem().getChildrenOfType(XnatAbstractresource.SCHEMA_ELEMENT_NAME, true);

                                for (XFTItem resource : hash) {
                                    ItemI om = BaseElement.GetGeneratedItem(resource);
                                    if (om instanceof XnatAbstractresource resourceA) {
                                        resourceA.deleteFromFileSystem(getRootArchivePath(), this.getId());
                                    }
                                }
                            }
                            SaveItemHelper.authorizedDelete(subject.getItem().getCurrentDBVersion(), user, ci);
                        } else {
                            preventProjectDeleteByP = true;
                        }
                    }
                }
            }
        }

        if (XDAT.getBoolSiteConfigurationProperty("security.allowProjectIdReuse", false)) {
            final String query = "UPDATE xnat_projectdata_history SET id = '" + getId() + Calendar.getInstance().getTimeInMillis() + "' where id = '" + getId() + "';";
            PoolDBUtils.ExecuteNonSelectQuery(query, getItem().getDBName(), user.getLogin());
        }

        Users.clearCache(user);

        if (!preventProjectDelete && !preventProjectDeleteByP) {
            final File arc = new File(getRootArchivePath());

            PrearcUtils.deleteProject(getId());
            SaveItemHelper.authorizedDelete(getItem().getCurrentDBVersion(), user, ci);

            Groups.deleteGroupsByTag(getId(), user, ci);

            //DELETE any other field mappings (i.e. guest settings)
            ItemSearch is = ItemSearch.GetItemSearch(XdatFieldMapping.SCHEMA_ELEMENT_NAME, user);
            is.addCriteria("xdat:field_mapping.field_value", getId());
            Iterator items = is.exec(false).iterator();
            while (items.hasNext()) {
                XFTItem item = (XFTItem) items.next();
                SaveItemHelper.authorizedDelete(item, user, ci);
            }

            //DELETE storedSearches
            for (final ItemI bundle : getBundles()) {
                try {
                    SaveItemHelper.authorizedDelete(bundle.getItem(), user, ci);
                } catch (Throwable e) {
                    logger.error("An unknown error occurred trying to delete the project " + getId(), e);
                }
            }

            final ArcProject arcProject = getArcSpecification();
            try {
                if (arcProject != null) {
                    SaveItemHelper.authorizedDelete(arcProject.getItem(), user, ci);
                }
            } catch (Throwable e) {
                logger.error("An unknown error occurred trying to delete the arc project " + arcProject.getId() + " for the project " + getId(), e);
            }

            try {
                if (arc.exists() && removeFiles) {
                    FileUtils.MoveToCache(arc);
                }
            } catch (Exception e) {
                logger.error("An unknown error occurred trying to move the data archive at " + arc.getAbsolutePath() + " for the project " + getId() + " to the cache folder", e);
            }

            final ScriptTriggerTemplateService templateService = XDAT.getContextService().getBean(ScriptTriggerTemplateService.class);
            final List<ScriptTriggerTemplate>  templates       = templateService.getTemplatesForEntity(getId());
            if (templates != null) {
                for (final ScriptTriggerTemplate template : templates) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Deleting script trigger template association between template " + template.getTemplateId() + " and project " + getId());
                    }
                    template.getAssociatedEntities().remove(getId());
                    templateService.update(template);
                }
            }
            final ScriptTriggerService triggerService = XDAT.getContextService().getBean(ScriptTriggerService.class);
            final List<ScriptTrigger>  triggers       = triggerService.getByScope(Scope.Project, getId());
            if (triggers != null) {
                for (ScriptTrigger trigger : triggers) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Deleting script trigger " + trigger.getTriggerId() + " with script ID " + trigger.getScriptId() + " and event " + trigger.getEvent() + " while deleting project " + getId());
                    }
                    triggerService.delete(trigger);
                }
            }

            XDAT.triggerXftItemEvent(SCHEMA_ELEMENT_NAME, getId(), DELETE);
        }
    }

    @Override
    public void preSave() throws Exception {
        super.preSave();

        final String projectId = getId();

        if (StringUtils.isBlank(projectId)) {
            throw new IllegalArgumentException("You must specify the project ID to save it");
        }

        if (!XftStringUtils.isValidId(projectId)) {
            throw new IllegalArgumentException("Identifiers cannot use special characters: " + projectId);
        }

        // Validate project fields.  If there are conflicts, throw a new exception
        trimProjectFields();
        Collection<String> conflicts = validateProjectFields();
        if (!conflicts.isEmpty()) {
            StringBuilder conflictStr = new StringBuilder();
            for (String conflict : conflicts) {
                conflictStr.append(conflict).append("\n");
            }
            throw new IllegalArgumentException(conflictStr.toString());
        }

        final String expectedPath = getExpectedCurrentDirectory().getAbsolutePath().replace('\\', '/');
        OmUtils.validateXnatAbstractResources(expectedPath, getResources_resource());

        final XFTItem existing = getCurrentDBVersion();
        if (existing == null) {
            if (((Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(ID) FROM xnat_projectdata_history WHERE ID='" + projectId + "';", "COUNT", null, null)) > 0) {
                throw new Exception("Project '" + projectId + "' was used in a previously deleted project and cannot be reused.");
            }
        }

        UserGroupI ownerG = Groups.getGroup(projectId + "_" + OWNER_GROUP);
        if (ownerG == null) {
            PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, getUser(), getXSIType(), projectId, PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, EventUtils.TYPE.WEB_SERVICE, "Initialized permissions"));

            EventMetaI ci = wrk.buildEvent();
            try {
                UserI u = getUser();

                UserGroupI group = Groups.createOrUpdateGroup(projectId + "_" + OWNER_GROUP, "Owners", Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, getSecuredElements(), projectId, getUser());

                wrk.setDataType(Groups.getGroupDatatype());
                wrk.setId(group.getPK().toString());
                wrk.setExternalid(projectId);

                if (!Groups.isMember(u, group.getId())) {
                    addGroupMember(projectId + "_" + OWNER_GROUP, u, u, ci);

                    Groups.updateUserForGroup(u, group.getId(), group);

                    //add a workflow entry for the user audit trail
                    PersistentWorkflowI wrk2 = PersistentWorkflowUtils.getOrCreateWorkflowData(null, u, XdatUser.SCHEMA_ELEMENT_NAME, u.getID().toString(), PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, EventUtils.TYPE.WEB_SERVICE, "Initialized permissions"));
                    PersistentWorkflowUtils.complete(wrk2, wrk2.buildEvent());
                }

                PersistentWorkflowUtils.complete(wrk, ci);
            } catch (Exception e) {
                PersistentWorkflowUtils.fail(wrk, ci);
                throw e;
            }
        }
    }

    public String getArchiveDirectoryName() {
        return getId();
    }

    public File getExpectedCurrentDirectory() throws InvalidArchiveStructure {
        return new File(getRootArchivePath(), "resources");
    }

    @SuppressWarnings("unused")
    public boolean isAutoArchive() {
        Integer i = ArcSpecManager.GetInstance().getAutoQuarantineCodeForProject(getId());
        return !(i == null || i < 4);
    }

    public static ArrayList<XnatProjectdata> getAllXnatProjectdatas(final UserI user, final boolean preLoad) {
        final UserItemCache<XnatProjectdata> cache = getUserProjectCache();
        if (cache != null) {
            final List<XnatProjectdata> all = cache.getAll(user);
            if (all != null) {
                return new ArrayList<>(all);
            }
        }

        return AutoXnatProjectdata.getAllXnatProjectdatas(user, preLoad);
    }

    @Nullable
    public static ArrayList<XnatProjectdata> getXnatProjectdatasByField(final String xmlPath, final Object value, final UserI user, final boolean preLoad) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return null;
        }

        final UserItemCache<XnatProjectdata> cache = getUserProjectCache();
        if (cache != null) {
            final List<XnatProjectdata> byField = cache.getByField(user, xmlPath, value.toString());
            if (byField != null) {
                return new ArrayList<>(byField);
            }
        }

        return AutoXnatProjectdata.getXnatProjectdatasByField(xmlPath, value, user, preLoad);
    }

    public static XnatProjectdata getXnatProjectdatasById(final Object value, final UserI user, final boolean preLoad) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return null;
        }

        final UserItemCache<XnatProjectdata> cache = getUserProjectCache();
        if (cache != null) {
            final XnatProjectdata project = cache.get(user, value.toString());
            if (project != null) {
                return project;
            }
        }

        return AutoXnatProjectdata.getXnatProjectdatasById(value, user, preLoad);
    }

    public static XnatProjectdata getProjectByIDorAlias(final String pID, final UserI user, final boolean preLoad) {
        if (StringUtils.isBlank(pID)) {
            return null;
        }

        final UserItemCache<XnatProjectdata> cache = getUserProjectCache();
        if (cache != null) {
            return cache.get(user, pID);
        }

        final XnatProjectdata project = AutoXnatProjectdata.getXnatProjectdatasById(pID, user, preLoad);
        if (project == null) {
            final List<XnatProjectdata> matches = XnatProjectdata.getXnatProjectdatasByField("xnat:projectData/aliases/alias/alias", pID, user, preLoad);
            if (matches != null && !matches.isEmpty()) {
                return matches.getFirst();
            }
        }
        return null;
    }

    @Override
    public String getProject() {
        return getId();
    }

    @Override
    public String getArchiveRootPath() {
        return getRootArchivePath();
    }

    public static XnatProjectdata createProject(final XnatProjectdata project, final UserI user, final boolean allowDataDeletion, final boolean allowMatchingID, final EventDetails event, final String accessibility) throws XftItemException {
        final PersistentWorkflowI workflow;
        try {
            workflow = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, XnatProjectdata.SCHEMA_ELEMENT_NAME, project.getId(), project.getId(), event);
        } catch (EventRequirementAbsent e) {
            throw new XftItemLifecycleException(Initialization, "Some requirement for creating an event workflow for project " + project.getId() + " was not found.", e);
        }

        final EventMetaI eventMeta = workflow.buildEvent();

        try {
            final XnatProjectdata saved = createProject(project, user, allowDataDeletion, allowMatchingID, eventMeta, accessibility);
            Users.clearCache(user);
            ElementSecurity.refresh();
            try {
                WorkflowUtils.complete(workflow, eventMeta);
            } catch (Exception e) {
                throw new MetaDataException("An error occurred while trying to mark the workflow " + workflow.getWorkflowId() + " as completed. All other tasks for this workflow were completed successfully!", e);
            }
            return saved;
        } catch (XftItemException e) {
            if (!(e instanceof MetaDataException && StringUtils.contains(e.getMessage(), "An error occurred while trying to mark the workflow") && StringUtils.contains(e.getMessage(), "All other tasks for this workflow were completed successfully"))) {
                try {
                    WorkflowUtils.fail(workflow, eventMeta);
                } catch (Exception embedded) {
                    logger.error("An error occurred trying to mark the workflow " + workflow.getWorkflowId() + " as failed", embedded);
                }
            }
            throw e;
        }
    }

    public static XnatProjectdata createProject(final XnatProjectdata project, final UserI user, final boolean allowDataDeletion, final boolean allowMatchingId, final EventMetaI event, final String accessibility) throws XftItemException {
        final ValidationResults validation;
        try {
            project.initNewProject(user, allowDataDeletion, allowMatchingId, event);
            validation = project.validate();
        } catch (XftItemException e) {
            // If it's already an XftItemException, just throw it up the call stack.
            throw e;
        } catch (Exception e) {
            final String message = "An error occurred trying to initialize a new project with ID " + project.getId();
            logger.error(message, e);
            throw new XftItemLifecycleException(Initialization, message, e);
        }

        if (validation != null && !validation.isValid()) {
            throw new ValidationException(validation);
        }

        try {
            project.save(user, true, false, event);
        } catch (Exception e) {
            final String message = "An error occurred trying to save the project " + project.getId();
            logger.error(message, e);
            throw new XftItemLifecycleException(Save, message, e);
        }

        final XnatProjectdata postSave     = new XnatProjectdata(project.getItem().getCurrentDBVersion(false));
        final XFTItem         postSaveItem = postSave.getItem();
        postSaveItem.setUser(user);
        final List<UserGroupI> groups;
        try {
            groups = postSave.initGroups();
        } catch (Exception e) {
            final String message = "An error occurred trying to initialize groups for the project " + project.getId();
            logger.error(message, e);
            throw new XftItemLifecycleException(InitializeGroups, message, e);
        }

        Groups.reloadGroupsForUser(user);

        try {
            final String resolved = StringUtils.defaultIfBlank(accessibility, "protected");
            if (!resolved.equals("private") && XDAT.getBoolSiteConfigurationProperty("securityAllowNonPrivateProjects", true)) {
                if (!Permissions.initializeDefaultAccessibility(project.getId(), resolved, true, user, event)) {
                    logger.warn("Something went wrong initializing accessibility for project " + project.getId() + ". Check the logs to find more information.");
                }
            }
        } catch (Exception e) {
            final String message = "An error occurred trying to initialize the default accessibility for the project " + project.getId();
            logger.error(message, e);
            throw new XftItemLifecycleException(InitializeAccessibility, message, e);
        }

        try {
            // Use impl method, which skips permissions check: the user is creating the project, we KNOW the user can edit/delete the project.
            postSave.initArcProjectImpl(user, event);
        } catch (Exception e) {
            final String message = "An error occurred trying to initialize the arc project for the project " + project.getId();
            logger.error(message, e);
            throw new XftItemLifecycleException(InitializeArcProject, message, e);
        }

        XDAT.triggerXftItemEvent(postSaveItem, CREATE);
        for (final UserGroupI group : groups) {
            XDAT.triggerXftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), CREATE);
        }
        return postSave;
    }

    public Integer getMetaId() {
        return getItem().getMetaDataId();
    }

    /**
     * Return the project info ID (meta data id) for this project ID.
     *
     * @param project The project ID.
     *
     * @return The project metadata ID.
     */
    public static Long getProjectInfoIdFromStringId(String project) {
        if (project != null) {
            XnatProjectdata p = XnatProjectdata.getXnatProjectdatasById(project, null, false);
            if (p != null) {
                return Long.parseLong(p.getItem().getProps().get("projectdata_info").toString());
            }
        }
        return null;
    }

    /**
     * Function removes excess whitespace from the project id, secondary id, name and alias fields.
     */
    public void trimProjectFields() throws Exception {
        //Trim excess white space from the project id
        final String id = getId();
        if (!StringUtils.isBlank(id)) {
            final String trimmed = id.trim();
            if (!trimmed.equals(id)) {
                setId(trimmed);
            }
        }

        // Trim excess white space from the secondary id
        final String secondaryId = getSecondaryId();
        if (!StringUtils.isBlank(secondaryId)) {
            final String trimmed = secondaryId.trim();
            if (!trimmed.equals(secondaryId)) {
                setSecondaryId(trimmed);
            }
        }

        // Trim excess white space from the project name
        final String name = getName();
        if (!StringUtils.isBlank(name)) {
            final String trimmed = name.trim();
            if (!trimmed.equals(name)) {
                setName(trimmed);
            }
        }

        // Trim excess white space from each alias
        for (final XnatProjectdataAliasI alias : getAliases_alias()) {
            final String value   = alias.getAlias();
            final String trimmed = value.trim();
            if (!trimmed.equals(value)) {
                alias.setAlias(trimmed);
            }
        }
    }

    /**
     * Function validates a project's id, secondary id, name and aliases
     * to make sure they will not conflict with any existing projects within the database.
     * See: XNAT-2801,  XNAT-2934, XNAT-2813, XNAT-2551, XNAT-2628, XNAT-2780
     *
     * @return - A list of conflict errors. If the list is empty, all elements passed validation.
     */
    public Collection<String> validateProjectFields() throws Exception {

        // Make sure the Id isn't null or empty
        final String id = getId();
        if (StringUtils.isBlank(id)) {
            return Collections.singletonList("Missing required field: Project Id.");
        }

        // Add all the elements that require validation to a Map.
        final Map<String, String> elements = new HashMap<>();
        elements.put("Project Id", TurbineUtils.escapeParam(id.toLowerCase())); // Add the Project Id.

        final String name = getName();
        if (!StringUtils.isBlank(name)) { // Add the Project Title.
            elements.put("Project Title", TurbineUtils.escapeParam(name.toLowerCase()));
        }

        final String secondaryId = getSecondaryId();
        if (!StringUtils.isBlank(secondaryId)) { // Add the Running Title.
            elements.put("Running Title", TurbineUtils.escapeParam(secondaryId.toLowerCase()));
        }

        return validateElements(elements, getNewProjectAliasStrings());
    }

    /**
     * Validates a collection of elements and aliases that we wish to insert into the database.
     * Elements are validated against all
     *
     * @param elements - The Collection elements we wish to validate.
     * @param aliases  - The Collection of aliases we wish to validate.
     *
     * @return - A list of conflict errors. If the list is empty, all elements passed validation.
     *
     * @throws Exception When an error occurs.
     */
    private Collection<String> validateElements(final Map<String, String> elements, final Collection<String> aliases) throws Exception {
        if (elements == null) {
            return Collections.emptyList();
        }

        if (!elements.isEmpty() && PoolDBUtils.HackCheck(elements.values())) {
            final String values = String.join(", ", elements.values());
            logger.error("Found possible SQL injection attempt in values: " + values);
            throw new SecurityException("Found invalid value in submitted strings: " + values);
        }

        if (aliases != null && !aliases.isEmpty() && PoolDBUtils.HackCheck(aliases)) {
            final String values = String.join(", ", aliases);
            logger.error("Found possible SQL injection attempt in values: " + values);
            throw new SecurityException("Found invalid value in submitted alias strings: " + values);
        }

        // Get a list of the aliases the user wishes to add and validate them.
        final Collection<String> conflicts = validateAliases(aliases);

        // Create a query and find all possible matches in the database
        final String             inClause = "'" + StringUtils.join(new HashSet<>(elements.values()), "','") + "'";
        final Collection<String> matches  = getMatchingElements("SELECT LOWER(a.id) as id, LOWER(a.secondary_id) as secondary_id, LOWER(a.name) as name, LOWER(b.alias) as alias FROM (SELECT id,secondary_id,name FROM xnat_projectdata WHERE LOWER(id) != '" + getId().toLowerCase() + "') a FULL OUTER JOIN ( SELECT aliases_alias_xnat_projectdata_id as id, alias FROM xnat_projectdata_alias) b ON a.id = b.id WHERE (LOWER(a.secondary_id) IN (" + inClause + ") OR LOWER(a.name) IN (" + inClause + ") OR LOWER(b.alias) IN (" + inClause + ") OR LOWER(a.id) IN (" + inClause + "));");

        // For each element, check to see if it is contained within the collection of possible matches. If so, add a conflict.
        for (final Map.Entry<String, String> entry : elements.entrySet()) {
            if (matches.contains(entry.getValue())) {
                conflicts.add("Invalid " + entry.getKey() + ": '" + entry.getValue() + "' is already being used.");
            }
            if (aliases != null && aliases.contains(entry.getValue())) { // element cannot be an alias as well as a project id, secondary id or title.
                conflicts.add("Invalid " + entry.getKey() + ": '" + entry.getValue() + "' cannot be used as the " + entry.getKey() + " and an alias.");
            }
        }
        return conflicts;
    }

    /**
     * Validates any Aliases the user is trying to add to this project.
     *
     * @return - A list of conflict errors. If the list is empty, all elements passed validation.
     *
     * @throws Exception When an error occurs.
     */
    private Collection<String> validateAliases(final Collection<String> aliases) throws Exception {
        if (null == aliases || aliases.isEmpty()) {
            return new ArrayList<>();
        }

        // Create a query and find all possible matches in the database
        final String             inClause  = collectionToCommaDelimitedString(aliases);
        final Collection<String> matches   = getMatchingElements("SELECT LOWER(a.id) as id, LOWER(a.secondary_id) as secondary_id, LOWER(a.name) as name, LOWER(b.alias) as alias FROM (SELECT id,secondary_id,name FROM xnat_projectdata) a FULL OUTER JOIN ( SELECT aliases_alias_xnat_projectdata_id as id, alias FROM xnat_projectdata_alias WHERE LOWER(aliases_alias_xnat_projectdata_id) != '" + getId().toLowerCase() + "') b ON a.id = b.id WHERE (LOWER(a.secondary_id) IN (" + inClause + ") OR LOWER(a.name) IN (" + inClause + ") OR LOWER(b.alias) IN (" + inClause + ") OR LOWER(a.id) IN (" + inClause + "));");
        final Collection<String> conflicts = new ArrayList<>();

        // For each alias, check to see if it is contained within the collection of possible matches. If so, add a conflict.
        for (final String entry : aliases) {
            if (matches.contains(entry)) {
                conflicts.add("Invalid Alias: '" + entry + "' is already being used.");
            }
        }
        return conflicts;
    }

    /**
     * Function executes a query and returns all results in one Set of strings.
     *
     * @param query - the query to execute
     *
     * @return A set of any strings that are returned from the database
     *
     * @throws Exception When something goes wrong.
     */
    private Collection<String> getMatchingElements(final String query) throws Exception {
        final XFTTable           table    = new PoolDBUtils().executeSelectQuery(query, null, getUser().getUsername());
        final List<List<String>> elements = table.convertColumnsToArrayList(new ArrayList(Arrays.asList(table.getColumns())));

        // Convert the ArrayList<ArrayList<String>> into a one Set<String> so it's easy to manage.
        // We don't care about duplicate values or the column names anymore.
        return elements.stream().flatMap(List::stream).collect(Collectors.toSet());
    }

    /**
     * Function gets the string form of each alias the user is trying to add to this project.
     *
     * @return - List of Alias Strings
     */
    public Collection<String> getNewProjectAliasStrings() {
        return getAliases_alias().stream().map(alias -> TurbineUtils.escapeParam(alias.getAlias().toLowerCase(Locale.ROOT))).collect(Collectors.toList());
    }

    /**
     * Converts a collection of strings into a single comma delimited string.
     *
     * @param elements - A collection of strings
     *
     * @return a comma delimited string. e.g. "'element_1', 'element_2', 'element_3' ... 'element_n'"
     */
    private String collectionToCommaDelimitedString(final Collection<String> elements) {
        return "'" + StringUtils.join(elements, "','") + "'";
    }

    @Override
    public SecurityValues getSecurityTags() {
        final SecurityValues projects = new SecurityValues();
        projects.getHash().put("xnat:projectData/ID", getId());
        return projects;
    }

    public boolean getUseScanTypeMapping() {
        if (!XDAT.getSiteConfigPreferences().getScanTypeMapping()) {
            return false;
        }

        final ConfigService configService = XDAT.getConfigService();

        // check project config
        final Configuration config = configService.getConfig("project", "scanTypeMapping", Scope.Project, getId());
        if (config != null && config.getStatus().equals("enabled")) {
            return Boolean.parseBoolean(config.getContents());
        }

        // if nothing there, check site config
        return XDAT.getBoolSiteConfigurationProperty("scanTypeMapping", true);
    }

    public void setUseScanTypeMapping(boolean newValue) {
        ConfigService configService = XDAT.getConfigService();
        try {
            configService.replaceConfig(getUser().getUsername(), "", "project", "scanTypeMapping", String.valueOf(newValue), Scope.Project, getId());
        } catch (ConfigServiceException exception) {
            logger.error("Configuration service error replacing config for user " + getUser().getUsername() + " and project " + getId());
        }
    }

    private void dynamicSearchAdd(UserI user, List<XdatStoredSearch> storedSearches, BaseXnatProjectdata proj) throws Exception {
        List<Class<?>> classes = Reflection.getClassesForPackage("org.nrg.xnat.project.getBundles.extensions");

        if (classes != null && classes.size() > 0) {
            for (Class<?> clazz : classes) {
                if (AddBundlesI.class.isAssignableFrom(clazz)) {
                    AddBundlesI action = (AddBundlesI) clazz.newInstance();
                    action.execute(user, storedSearches, proj);
                }
            }
        }
    }

    private boolean needsStoredSearchForDataType(final String xsiType, final Collection<XdatStoredSearch> searches) {
        try {
            if (!ElementSecurity.IsBrowseableElement(xsiType)) {
                log.info("The data type {} isn't browseable, so no stored search needed", xsiType);
                return false;
            }
        } catch (Exception e) {
            log.warn("Got an error trying to test whether the data type '{}' in project '{}' is browseable, presuming this error means no", xsiType, getId(), e);
            return false;
        }
        final String displayName = DisplayManager.GetInstance().getPluralDisplayNameForElement(xsiType);
        return searches.stream().filter(Objects::nonNull).noneMatch(search -> StringUtils.equalsIgnoreCase(search.getRootElementName(), xsiType) &&
                                                                              StringUtils.equalsIgnoreCase(search.getBriefDescription(), displayName));
    }

    @Nullable
    private XdatStoredSearch getStoredSearchForDataType(final String xsiType) {
        try {
            if (!ElementSecurity.IsBrowseableElement(xsiType)) {
                log.info("The data type {} isn't browseable, so no stored search exists", xsiType);
                return null;
            }
        } catch (Exception e) {
            log.error("Error while trying to test whether the data type '{}' in project '{}' is browseable, presuming this error means no.", xsiType, getId(), e);
            return null;
        }
        final XnatAbstractprotocol protocol = getProtocolByDataType(xsiType);
        final XdatStoredSearch     xss      = protocol != null ? protocol.getDefaultSearch((XnatProjectdata) this) : getDefaultSearch(xsiType);
        xss.setId("@" + xsiType);
        return xss;
    }

    protected void initArcProjectImpl(final UserI user, final EventMetaI eventMeta) throws Exception {
        initArcProjectImpl(null, user, eventMeta);
    }

    protected void initArcProjectImpl(final @Nullable ArcProject arcProject, final UserI user, final EventMetaI eventMeta) throws Exception {
        final ArcProject working = getDefaultArcProject(user, arcProject);
        working.setProperty("projects_project_arc_archivespe_arc_archivespecification_id", ArcSpecManager.GetInstance().getArcArchivespecificationId());
        working.setId(getId());
        working.setProperty("arc:project/paths/archivePath", ArcSpecManager.GetInstance().getGlobalArchivePath() + getId() + "/");
        working.setProperty("arc:project/paths/prearchivePath", ArcSpecManager.GetInstance().getGlobalPrearchivePath() + getId() + "/");
        working.setProperty("arc:project/paths/cachePath", ArcSpecManager.GetInstance().getGlobalCachePath() + getId() + "/");
        working.setProperty("arc:project/paths/buildPath", ArcSpecManager.GetInstance().getGlobalBuildPath() + getId() + "/");
        working.setPrearchiveCode(XDAT.getIntSiteConfigurationProperty("defaultPrearchiveCode", 4));

        SaveItemHelper.authorizedSave(working, user, true, false, eventMeta);
        ArcSpecManager.Reset();
    }

    private ArcProject getDefaultArcProject(final UserI user, final @Nullable ArcProject existing) throws XFTInitException, ElementNotFoundException {
        if (existing != null) {
            return existing;
        }
        final ArcProject arcProject = new ArcProject(XFTItem.NewItem(ArcProject.SCHEMA_ELEMENT_NAME, user));
        arcProject.setCurrentArc("arc001");
        return arcProject;
    }

    private boolean canReadDataType(final UserI user, final String dataType) {
        final SecurityValues values = new SecurityValues();
        try {
            values.put(dataType + "/project", getId());
            values.put(dataType + "/sharing/share/project", getId());
            final SchemaElement schemaElement = SchemaElement.GetElement(dataType);
            return Permissions.canRead(getUser(), schemaElement, values);
        } catch (Exception e) {
            log.error("An error occurred trying to check whether the user {} can access the data type {} in the project {}", user.getUsername(), dataType, getId());
            return false;
        }
    }

    private static UserItemCache<XnatProjectdata> getUserProjectCache() {
        synchronized (_cacheMutex) {
            if (_cache == null) {
                _cache = XDAT.getContextService().getBeanSafely("userProjectCache", UserItemCache.class);
                if (_cache == null) {
                    _cache = XDAT.getContextService().getBeanSafely(UserItemCache.class);
                }
            }
        }
        return _cache;
    }

    private static final String INVALID_CHAR_REGEX = "[" + Pattern.quote("`~!@#$%^&*()+=|\\{[}]:;\"'<>?,./") + "]";
    private static final Object _cacheMutex        = new Object();

    private static UserItemCache<XnatProjectdata> _cache = null;

    private final Hashtable<String, XnatProjectdataField> _fieldsByName = new Hashtable<>();
    private final ArrayList<XnatSubjectdata>              _participants = new ArrayList<>();
    private final ArrayList<XnatExperimentdata>           _experiments  = new ArrayList<>();
}
