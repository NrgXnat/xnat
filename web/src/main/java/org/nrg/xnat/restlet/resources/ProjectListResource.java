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
import org.nrg.xdat.security.helpers.*;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.restlet.representations.ItemXMLRepresentation;
import org.nrg.xnat.services.cache.DefaultGroupsAndPermissionsCache;
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
                        final XnatProjectdata saved = BaseXnatProjectdata.createProject(project, user, allowDataDeletion, false, newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN), getQueryVariable("accessibility"));
                        returnSuccessfulCreateFromList(saved.getId());
                    } else {
                        getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to edit this project.");
                    }
                } else {
                    getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Project already exists.");
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
                    || resource.containsQueryVariable(AccessLevel.Owner.code())
                    || resource.containsQueryVariable(AccessLevel.Member.code())
                    || resource.containsQueryVariable(AccessLevel.Collaborator.code())
                    || resource.containsQueryVariable("activeSince")
                    || resource.containsQueryVariable("recent")
                    || resource.containsQueryVariable("favorite")
                    || resource.containsQueryVariable(AccessLevel.Admin.code())
                    || (resource.requested_format != null && resource.requested_format.equals("search_xml")));
        }

        @Override
        public Representation handle(SecureResource resource, Variant variant) {

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

                final String dataAccess = resource.getQueryVariable(DATA_ACCESSIBILITY);
                if (StringUtils.isNotBlank(dataAccess) && !StringUtils.equalsAnyIgnoreCase(dataAccess, DATA_WRITABLE, DATA_READABLE)) {
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "The value specified for the " + DATA_ACCESSIBILITY + " parameter is invalid: " + dataAccess + ". Must be one of " + DATA_READABLE + " or " + DATA_WRITABLE + ".");
                }

                final boolean hasAllDataAccess = Groups.hasAllDataAccess(user);
                if (!hasAllDataAccess && StringUtils.isNotBlank(dataAccess)) {
                    CriteriaCollection cc = new CriteriaCollection("OR");
                    DisplayCriteria dc = new DisplayCriteria();
                    switch (dataAccess) {
                        case DATA_WRITABLE:
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
                            break;
                        case DATA_READABLE:
                            dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_USERS");
                            dc.setComparisonType(" LIKE ");
                            dc.setValue("% " + user.getLogin() + " %", false);
                            cc.add(dc);

                            dc = new DisplayCriteria();
                            dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_ACCESS");
                            dc.setValue("public", false);
                            cc.add(dc);

                            allCC.addCriteria(cc);
                            break;
                    }
                }

                final String access = resource.getQueryVariable(ACCESSIBLE);
                if (StringUtils.isNotBlank(access)) {
                    if (!hasAllDataAccess) {
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
                    if (access.equalsIgnoreCase("false")) {
                        CriteriaCollection cc2 = new CriteriaCollection("OR");
                        DisplayCriteria dc = new DisplayCriteria();
                        dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_USERS");
                        dc.setComparisonType(" NOT LIKE ");
                        dc.setValue("% " + user.getLogin() + " %", false);
                        cc2.add(dc);

                        dc = new DisplayCriteria();
                        dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_USERS");
                        dc.setComparisonType(" IS ");
                        dc.setValue(" NULL ", false);
                        dc.setOverrideDataFormatting(true);
                        cc2.add(dc);

                        allCC.addCriteria(cc2);
                    }
                }

                final String users = resource.getQueryVariable("users");
                if (StringUtils.equalsIgnoreCase(users, "true")) {
                    CriteriaCollection cc = new CriteriaCollection("OR");
                    DisplayCriteria dc = new DisplayCriteria();
                    dc.setSearchFieldByDisplayField("xnat:projectData", "PROJECT_USERS");
                    dc.setComparisonType(" LIKE ");
                    dc.setValue("% " + user.getLogin() + " %", false);
                    cc.add(dc);
                    orCC.addCriteria(cc);
                }

                final String owner = resource.getQueryVariable(AccessLevel.Owner.code());
                if (StringUtils.isNotBlank(owner)) {
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
                        if (hasAllDataAccess) {
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
            final String permissions = resource.getQueryVariable("permissions");
            final String dataType    = resource.getQueryVariable("dataType");
            final UserI  user        = resource.getUser();

            final GroupsAndPermissionsCache cache = XDAT.getContextService().getBeanSafely(GroupsAndPermissionsCache.class);
            final XFTTable table= cache.getProjectsForDatatypeAction(user,dataType, permissions);
            if(table==null){
                throw new Exception("Failed to identify projects");
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
        public Representation handle(SecureResource resource, Variant variant) {
            ProjectListResource projResource = (ProjectListResource) resource;
            XFTTable table;
            UserI user = resource.getUser();
            final Boolean allDataOverride = this.allDataOverride(resource);
            try {
                final String re = projResource.getRootElementName();

                final QueryOrganizer qo = QueryOrganizer.buildXFTQueryOrganizerWithClause(re, !allDataOverride ? user : null);

                projResource.populateQuery(qo);
                
                if (!Groups.isMember(user, Groups.ALL_DATA_ADMIN_GROUP) && !allDataOverride) {
                    String restriction = SecurityManager.READ;

                    if (resource.containsQueryVariable("restrict")){
                        String queryRestriction = resource.getQueryVariable("restrict");
                        if(queryRestriction.equals(SecurityManager.EDIT) || queryRestriction.equals(SecurityManager.DELETE)) {
                            restriction = queryRestriction;
                        }
                    }

                    final List<Object> ps = Permissions.getAllowedValues(user, "xnat:projectData", "xnat:projectData/ID", restriction);
                    if(ps!=null && ps.size()>0) {
                        final CriteriaCollection cc = new CriteriaCollection("OR");
                        for (Object p : ps) {
                            cc.addClause("xnat:projectData/ID", p);
                        }
                        qo.setWhere(cc);
                    }
                    else{
                        final CriteriaCollection cc = new CriteriaCollection("AND");
                        //If user does not have permissions on any projects, add a where clause saying that projectid
                        // must equal both 'this' and 'that'. This causes the table that returns to be empty.
                        cc.addClause("xnat:projectData/ID", "this");
                        cc.addClause("xnat:projectData/ID", "that");
                        qo.setWhere(cc);
                    }
                }


                final String query = qo.buildFullQuery();

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
        private final Boolean allDataOverride(SecureResource resource) {
            return Roles.isSiteAdmin(resource.getUser()) && resource.containsQueryVariable("allDataOverride") && (Boolean.parseBoolean(resource.getQueryVariable("allDataOverride")) == true); 
        }
    }
}
