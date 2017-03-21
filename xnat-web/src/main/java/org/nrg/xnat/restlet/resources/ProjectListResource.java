/*
 * web: org.nrg.xnat.restlet.resources.ProjectListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XdatStoredSearch;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.restlet.representations.ItemXMLRepresentation;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.util.*;

public class ProjectListResource extends QueryOrganizerResource {
    private static final String ACCESSIBLE = "accessible";
    private static final String DATA_ACCESSIBILITY = "data";
    private static final String DATA_READABLE = "readable";
    private static final String DATA_WRITABLE = "writable";
    private static final List<String> PERMISSIONS = Arrays.asList(SecurityManager.ACTIVATE, SecurityManager.CREATE, SecurityManager.DELETE, SecurityManager.EDIT, SecurityManager.READ);

    public ProjectListResource(Context context, Request request, Response response) {
        super(context, request, response);

        this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        this.getVariants().add(new Variant(MediaType.TEXT_HTML));
        this.getVariants().add(new Variant(MediaType.TEXT_XML));

        this.fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XMLPathShortcuts.PROJECT_DATA, true));

    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void handlePost() {
        XFTItem item;
        try {
            item = this.loadItem("xnat:projectData", true);

            final UserI user = getUser();
            if (item == null) {
                String xsiType = this.getQueryVariable("xsiType");
                if (xsiType != null) {
                    item = XFTItem.NewItem(xsiType, user);
                }
            }

            if (item == null) {
                this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Need POST Contents");
                return;
            }

            boolean allowDataDeletion = false;
            if (this.getQueryVariable("allowDataDeletion") != null && this.getQueryVariable("allowDataDeletion").equalsIgnoreCase("true")) {
                allowDataDeletion = true;
            }

            if (item.instanceOf("xnat:projectData")) {
                XnatProjectdata project = new XnatProjectdata(item);

                if (StringUtils.isBlank(project.getId())) {
                    this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Requires XNAT ProjectData ID");
                    return;
                }

                if (!XftStringUtils.isValidId(project.getId())) {
                    this.getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Invalid character in project ID.");
                    return;
                }

                if (item.getCurrentDBVersion() == null) {
                    if (XDAT.getSiteConfigPreferences().getUiAllowNonAdminProjectCreation() || Roles.isSiteAdmin(user)) {
                        this.returnSuccessfulCreateFromList(BaseXnatProjectdata.createProject(project, user, allowDataDeletion, false, newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN), getQueryVariable("accessibility")));
                    } else {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to edit this project.");
                    }
                } else {
                    this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Project already exists.");
                }
            }
        } catch (ActionException e) {
            this.getResponse().setStatus(e.getStatus(), e.getMessage());
        } catch (Exception e) {
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            logger.error(e);
        }
    }

    @Override
    public ArrayList<String> getDefaultFields(GenericWrapperElement e) {
        ArrayList<String> al = new ArrayList<>();
        al.add("ID");
        al.add("secondary_ID");
        al.add("name");
        al.add("description");
        al.add("pi_firstname");
        al.add("pi_lastname");

        return al;
    }

    public String getDefaultElementName() {
        return "xnat:projectData";
    }

    @Override
    public boolean allowGet() {
        return true;
    }

    private final static List<FilteredResourceHandlerI> _defaultHandlers = Lists.newArrayList();

    static {
        _defaultHandlers.add(new DefaultProjectHandler());
        _defaultHandlers.add(new FilteredProjects());
        _defaultHandlers.add(new PermissionsProjectHandler());
    }

    @Override
    public Representation represent(Variant variant) {
        Representation defaultRepresentation = super.represent(variant);
        if (defaultRepresentation != null)
            return defaultRepresentation;

        FilteredResourceHandlerI handler = null;
        try {
            final List<FilteredResourceHandlerI> handlers = getHandlers("org.nrg.xnat.restlet.projectsList.extensions", _defaultHandlers);
            for (FilteredResourceHandlerI filter : handlers) {
                if (filter.canHandle(this)) {
                    handler = filter;
                }
            }
        } catch (InstantiationException | IllegalAccessException e1) {
            logger.error("", e1);
        }

        try {
            if (handler != null) {
                return handler.handle(this, variant);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("", e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
    }

    public static class FilteredProjects implements FilteredResourceHandlerI {

        @Override
        public boolean canHandle(SecureResource resource) {
            return (resource.containsQueryVariable(ACCESSIBLE)
                    || resource.containsQueryVariable(DATA_ACCESSIBILITY)
                    || resource.containsQueryVariable("prearc_code")
                    || resource.containsQueryVariable("owner")
                    || resource.containsQueryVariable("member")
                    || resource.containsQueryVariable("collaborator")
                    || resource.containsQueryVariable("activeSince")
                    || resource.containsQueryVariable("recent")
                    || resource.containsQueryVariable("favorite")
                    || resource.containsQueryVariable("admin")
                    || (resource.requested_format != null && resource.requested_format.equals("search_xml")));
        }

        @Override
        public Representation handle(SecureResource resource, Variant variant) throws Exception {

            DisplaySearch ds = new DisplaySearch();
            UserI user = resource.getUser();
            XFTTable table = null;
            try {
                ds.setUser(user);
                ds.setRootElement("xnat:projectData");
                ds.addDisplayField("xnat:projectData", "ID");
                ds.addDisplayField("xnat:projectData", "NAME");
                ds.addDisplayField("xnat:projectData", "DESCRIPTION");
                ds.addDisplayField("xnat:projectData", "SECONDARY_ID");
                ds.addDisplayField("xnat:projectData", "PI");
                ds.addDisplayField("xnat:projectData", "PROJECT_INVS");
                ds.addDisplayField("xnat:projectData", "PROJECT_ACCESS");
                ds.addDisplayField("xnat:projectData", "PROJECT_ACCESS_IMG");
                ds.addDisplayField("xnat:projectData", "INSERT_DATE");
                ds.addDisplayField("xnat:projectData", "INSERT_USER");
                ds.addDisplayField("xnat:projectData", "USER_ROLE", "Role", user.getID());
                ds.addDisplayField("xnat:projectData", "LAST_ACCESSED", "Last Accessed", user.getID());

                if (resource.isQueryVariableTrue("prearc_code")) {
                    ds.addDisplayField("xnat:projectData", "PROJ_QUARANTINE");
                    ds.addDisplayField("xnat:projectData", "PROJ_PREARCHIVE_CODE");
                }

                CriteriaCollection allCC = new CriteriaCollection("AND");
                CriteriaCollection orCC = new CriteriaCollection("OR");

                String dataAccess = resource.getQueryVariable(DATA_ACCESSIBILITY);
                if (dataAccess != null) {
                    if (dataAccess.equalsIgnoreCase(DATA_WRITABLE)) {
                        if (!Groups.hasAllDataAccess(user)) {
                            CriteriaCollection cc = new CriteriaCollection("OR");
                            DisplayCriteria dc = new DisplayCriteria();
                            dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_MEMBERS");
                            dc.setComparisonType(" LIKE ");
                            dc.setValue("% " + user.getLogin() + " %", false);
                            cc.add(dc);

                            dc = new DisplayCriteria();
                            dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_OWNERS");
                            dc.setComparisonType(" LIKE ");
                            dc.setValue("% " + user.getLogin() + " %", false);
                            cc.add(dc);

                            allCC.addCriteria(cc);
                        }
                    } else if (dataAccess.equalsIgnoreCase(DATA_READABLE)) {
                        if (!Groups.hasAllDataAccess(user)) {
                            CriteriaCollection cc = new CriteriaCollection("OR");
                            DisplayCriteria dc = new DisplayCriteria();
                            dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_USERS");
                            dc.setComparisonType(" LIKE ");
                            dc.setValue("% " + user.getLogin() + " %", false);
                            cc.add(dc);

                            dc = new DisplayCriteria();
                            dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_ACCESS");
                            dc.setValue("public", false);
                            cc.add(dc);

                            allCC.addCriteria(cc);
                        }
                    } else {
                        throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "The value specified for the " + DATA_ACCESSIBILITY + " parameter is invalid: " + dataAccess + ". Must be one of " + DATA_READABLE + " or " + DATA_WRITABLE + ".");
                    }
                }

                String access = resource.getQueryVariable(ACCESSIBLE);
                if (access != null) {
                    if (access.equalsIgnoreCase("true")) {
                        if (!Groups.isMember(user, "ALL_DATA_ACCESS") && !Groups.isMember(user, "ALL_DATA_ADMIN")) {
                            CriteriaCollection cc = new CriteriaCollection("OR");
                            DisplayCriteria dc = new DisplayCriteria();
                            dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_USERS");
                            dc.setComparisonType(" LIKE ");
                            dc.setValue("% " + user.getLogin() + " %", false);
                            cc.add(dc);

                            dc = new DisplayCriteria();
                            dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_ACCESS");
                            dc.setValue("public", false);
                            cc.add(dc);

                            dc = new DisplayCriteria();
                            dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_ACCESS");
                            dc.setValue("protected", false);
                            cc.add(dc);

                            allCC.addCriteria(cc);
                        }
                    } else {
                        CriteriaCollection cc = new CriteriaCollection("OR");
                        DisplayCriteria dc = new DisplayCriteria();
                        dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_USERS");
                        dc.setComparisonType(" NOT LIKE ");
                        dc.setValue("% " + user.getLogin() + " %", false);
                        cc.add(dc);

                        dc = new DisplayCriteria();
                        dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_USERS");
                        dc.setComparisonType(" IS ");
                        dc.setValue(" NULL ", false);
                        dc.setOverrideDataFormatting(true);
                        cc.add(dc);

                        allCC.addCriteria(cc);
                    }
                }
                String users = resource.getQueryVariable("users");
                if (users != null) {
                    if (users.equalsIgnoreCase("true")) {
                        CriteriaCollection cc = new CriteriaCollection("OR");
                        DisplayCriteria dc = new DisplayCriteria();
                        dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_USERS");
                        dc.setComparisonType(" LIKE ");
                        dc.setValue("% " + user.getLogin() + " %", false);
                        cc.add(dc);

                        orCC.addCriteria(cc);
                    }
                }

                String owner = resource.getQueryVariable("owner");
                if (owner != null) {
                    if (owner.equalsIgnoreCase("true")) {
                        CriteriaCollection cc = new CriteriaCollection("OR");
                        DisplayCriteria dc = new DisplayCriteria();
                        dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_OWNERS");
                        dc.setComparisonType(" LIKE ");
                        dc.setValue("% " + user.getLogin() + " %", false);
                        cc.add(dc);

                        orCC.addCriteria(cc);
                    } else {
                        CriteriaCollection cc = new CriteriaCollection("OR");
                        DisplayCriteria dc = new DisplayCriteria();
                        dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_USERS");
                        dc.setComparisonType(" NOT LIKE ");
                        dc.setValue("% " + user.getLogin() + " %", false);
                        cc.add(dc);

                        orCC.addCriteria(cc);
                    }
                }
                if (resource.getQueryVariable("admin") != null) {
                    if (resource.isQueryVariableTrue("admin")) {
                        if (Roles.isSiteAdmin(user)) {
                            CriteriaCollection cc = new CriteriaCollection("OR");
                            DisplayCriteria dc = new DisplayCriteria();
                            dc.setSearchFieldByDisplayField("xnat:projectData", "ID");
                            dc.setComparisonType(" IS NOT ");
                            dc.setValue(" NULL ", false);
                            dc.setOverrideDataFormatting(true);
                            cc.add(dc);
                            orCC.addCriteria(cc);
                        }
                    }
                }

                String member = resource.getQueryVariable("member");
                if (member != null) {
                    if (member.equalsIgnoreCase("true")) {
                        CriteriaCollection cc = new CriteriaCollection("OR");
                        DisplayCriteria dc = new DisplayCriteria();
                        dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_MEMBERS");
                        dc.setComparisonType(" LIKE ");
                        dc.setValue("% " + user.getLogin() + " %", false);
                        cc.add(dc);

                        orCC.addCriteria(cc);
                    } else {
                        CriteriaCollection cc = new CriteriaCollection("OR");
                        DisplayCriteria dc = new DisplayCriteria();
                        dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_MEMBERS");
                        dc.setComparisonType(" NOT LIKE ");
                        dc.setValue("% " + user.getLogin() + " %", false);
                        cc.add(dc);

                        orCC.addCriteria(cc);
                    }
                }

                String collaborator = resource.getQueryVariable("collaborator");
                if (collaborator != null) {
                    if (collaborator.equalsIgnoreCase("true")) {
                        CriteriaCollection cc = new CriteriaCollection("OR");
                        DisplayCriteria dc = new DisplayCriteria();
                        dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_COLLABS");
                        dc.setComparisonType(" LIKE ");
                        dc.setValue("% " + user.getLogin() + " %", false);
                        cc.add(dc);

                        orCC.addCriteria(cc);
                    } else {
                        CriteriaCollection cc = new CriteriaCollection("OR");
                        DisplayCriteria dc = new DisplayCriteria();
                        dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_COLLABS");
                        dc.setComparisonType(" NOT LIKE ");
                        dc.setValue("% " + user.getLogin() + " %", false);
                        cc.add(dc);

                        orCC.addCriteria(cc);
                    }
                }

                String activeSince = resource.getQueryVariable("activeSince");
                if (activeSince != null) {
                    try {
                        Date d = DateUtils.parseDateTime(activeSince);

                        DisplayCriteria dc = new DisplayCriteria();
                        dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_LAST_WORKFLOW");
                        dc.setComparisonType(">");
                        dc.setValue(d, false);
                        orCC.add(dc);
                    } catch (RuntimeException e) {
                        resource.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
                    }
                }

                String recent = resource.getQueryVariable("recent");
                if (recent != null) {
                    try {
                        DisplayCriteria dc = new DisplayCriteria();
                        dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_LAST_ACCESS");
                        dc.setComparisonType(" LIKE ");
                        dc.setValue("% " + user.getLogin() + " %", false);
                        orCC.addCriteria(dc);
                    } catch (RuntimeException e) {
                        resource.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
                    }
                }

                String favorite = resource.getQueryVariable("favorite");
                if (favorite != null) {
                    try {
                        DisplayCriteria dc = new DisplayCriteria();
                        dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_FAV");
                        dc.setComparisonType(" LIKE ");
                        dc.setValue("% " + user.getLogin() + " %", false);
                        orCC.addCriteria(dc);
                    } catch (RuntimeException e) {
                        resource.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
                    }
                }

                if (orCC.size() > 0)
                    allCC.addCriteria(orCC);

                if (allCC.size() > 0)
                    ds.addCriteria(allCC);

                ds.setSortBy("SECONDARY_ID");

                if (resource.requested_format == null || !resource.requested_format.equals("search_xml")) {
                    table = (XFTTable) ds.execute(user.getLogin());
                }
            } catch (IllegalAccessException e) {
                logger.error("", e);
                resource.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                return null;
            } catch (Exception e) {
                logger.error("", e);
                resource.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                return null;
            }

            Hashtable<String, Object> params = new Hashtable<>();
            params.put("title", "Projects");
            params.put("xdat_user_id", user.getID());

            MediaType mt = resource.overrideVariant(variant);

            if (resource.requested_format != null && resource.requested_format.equals("search_xml")) {

                XdatStoredSearch xss = ds.convertToStoredSearch("");

                if (xss != null) {
                    ItemXMLRepresentation rep = new ItemXMLRepresentation(xss.getItem(), MediaType.TEXT_XML);
                    rep.setAllowDBAccess(false);
                    return rep;
                } else {
                    resource.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                    return new StringRepresentation("", MediaType.TEXT_XML);
                }
            } else {
                if (table != null)
                    params.put("totalRecords", table.size());
                return resource.representTable(table, mt, params);
            }
        }

    }

    public static class PermissionsProjectHandler implements FilteredResourceHandlerI {

        @Override
        public boolean canHandle(SecureResource resource) {
            return resource.containsQueryVariable("permissions");
        }

        @Override
        public Representation handle(SecureResource resource, Variant variant) throws Exception {
            final ArrayList<String> columns = new ArrayList<>();
            columns.add("id");
            columns.add("secondary_id");

            final XFTTable table = new XFTTable();
            table.initTable(columns);

            final String permissions = resource.getQueryVariable("permissions");
            if (StringUtils.isBlank(permissions)) {
                throw new Exception("You must specify a value for the permissions parameter.");
            } else if (!PERMISSIONS.contains(permissions)) {
                throw new Exception("You must specify one of the following values for the permissions parameter: " + Joiner.on(", ").join(PERMISSIONS));
            }

            final String             dataType          = resource.getQueryVariable("dataType");
            final UserI              user              = resource.getUser();
            final UserHelperServiceI userHelperService = UserHelper.getUserHelperService(user);
            if (userHelperService != null) {
                final Map<Object, Object> projects = userHelperService.getCachedItemValuesHash("xnat:projectData", null, false, "xnat:projectData/ID", "xnat:projectData/secondary_ID");
                for (final Object key : projects.keySet()) {
                    final String projectId = (String) key;
                    // If no data type is specified, we check both MR and PET session data permissions. This is basically
                    // tailored for checking for projects to which the user can upload imaging data.
                    final boolean canEdit = StringUtils.isBlank(dataType) ? userHelperService.hasEditAccessToSessionDataByTag(projectId) : Permissions.can(user, dataType + "/project", projectId, permissions);
                    if (canEdit) {
                        table.insertRowItems(projectId, projects.get(projectId));
                    }
                }
                table.sort("secondary_id", "ASC");
            }
            return resource.representTable(table, resource.overrideVariant(variant), null);
        }
    }

    public static class DefaultProjectHandler implements FilteredResourceHandlerI {

        @Override
        public boolean canHandle(SecureResource resource) {
            return true;
        }

        @Override
        public Representation handle(SecureResource resource, Variant variant) throws Exception {
            ProjectListResource projResource = (ProjectListResource) resource;
            XFTTable table;
            UserI user = resource.getUser();
            try {
                final String re = projResource.getRootElementName();

                final QueryOrganizer qo = new QueryOrganizer(re, user, ViewManager.ALL);

                projResource.populateQuery(qo);

                if (resource.containsQueryVariable("restrict") && !Groups.isMember(user, "ALL_DATA_ADMIN")) {
                    final String restriction = resource.getQueryVariable("restrict");
                    if (restriction.equals(SecurityManager.EDIT) || restriction.equals(SecurityManager.DELETE)) {
                        final List<Object> ps = Permissions.getAllowedValues(user, "xnat:projectData", "xnat:projectData/ID", restriction);
                        final CriteriaCollection cc = new CriteriaCollection("OR");
                        for (Object p : ps) {
                            cc.addClause("xnat:projectData/ID", p);
                        }
                        qo.setWhere(cc);
                    }
                }

                final String query = qo.buildQuery();

                table = XFTTable.Execute(query, user.getDBName(), resource.userName);

                table = projResource.formatHeaders(table, qo, re + "/ID", "/data/projects/");
            } catch (IllegalAccessException e) {
                logger.error("", e);
                resource.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                return null;
            } catch (Exception e) {
                logger.error("", e);
                resource.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                return null;
            }

            final MediaType mt = resource.overrideVariant(variant);
            final Hashtable<String, Object> params = new Hashtable<>();
            if (table != null) {
                params.put("totalRecords", table.size());
            }
            return resource.representTable(table, mt, params);
        }
    }
}
