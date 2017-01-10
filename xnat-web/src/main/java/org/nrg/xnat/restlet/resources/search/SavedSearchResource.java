/*
 * web: org.nrg.xnat.restlet.resources.search.SavedSearchResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.search;

import com.noelios.restlet.ext.servlet.ServletCall;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.om.*;
import org.nrg.xdat.presentation.CSVPresenter;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.MaterializedViewI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.restlet.presentation.RESTHTMLPresenter;
import org.nrg.xnat.restlet.representations.ItemXMLRepresentation;
import org.nrg.xnat.restlet.resources.ItemResource;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.Reader;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

public class SavedSearchResource extends ItemResource {
    private XdatStoredSearch xss            = null;
    private String           sID            = null;
    private boolean          loadedFromFile = false;

    public SavedSearchResource(Context context, Request request,
                               Response response) {
        super(context, request, response);

        sID = (String) getParameter(request, "SEARCH_ID");
        if (sID != null) {
            this.getVariants().add(new Variant(MediaType.TEXT_XML));
        } else {
            response.setStatus(Status.CLIENT_ERROR_GONE);
        }
    }

    /**
     * Returns a file containing search xmls which was stored on the file system.  This provides a way to standardize search xmls outside of the database, for easy sharing across installations.
     *
     * @return The search XMLs stored on the file system.
     */
    private synchronized static File getFileSystemSearch(String name) {
        if (!name.contains("..")) {
            final File file = new File(new File(XFT.GetConfDir()).getParentFile().getParentFile(), "resources/searches/" + name);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    @Override
    public Representation represent(Variant variant) {
        MediaType   mt   = overrideVariant(variant);
        final UserI user = getUser();

        if (xss == null && sID != null) {
            if (sID.startsWith("@")) {
                try {
                    String dv = this.getQueryVariable("dv");
                    if (dv == null) {
                        dv = "listing";
                    }
                    DisplaySearch ds = new DisplaySearch();
                    ds.setUser(user);
                    ds.setDisplay(dv);
                    ds.setRootElement(sID.substring(1));
                    xss = ds.convertToStoredSearch(sID);
                    xss.setId(sID);
                } catch (XFTInitException | ElementNotFoundException e) {
                    logger.error("", e);
                }
            } else {
                xss = XdatStoredSearch.getXdatStoredSearchsById(sID, user, true);
            }
        }

        if (xss != null) {
            if (!xss.hasAllowedUser(user.getLogin()) && !Permissions.canQuery(user, xss.getRootElementName())) {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                return null;
            }
        } else {
            //allow loading of saved searches from xml stored on hte file system
            final File searchXml = getFileSystemSearch(sID);

            if (searchXml != null) {
                if (mt.equals(MediaType.TEXT_XML) && (filepath == null || !filepath.startsWith("results")) && !this.hasQueryVariable("project")) {
                    return new FileRepresentation(searchXml, mt);
                } else {
                    try {
                        SAXReader reader = new SAXReader(user);
                        XFTItem item = reader.parse(searchXml);
                        xss = new XdatStoredSearch(item);

                        loadedFromFile = true;

                        if (this.getQueryVariable("project") != null) {
                            final XdatCriteriaSet cs = new XdatCriteriaSet(user);
                            cs.setMethod("OR");

                            for (final String p : StringUtils.commaDelimitedListToSet(this.getQueryVariable("project"))) {
                                XdatCriteria c = new XdatCriteria(user);
                                c.setSchemaField(xss.getRootElementName() + "/project");
                                c.setComparisonType("=");
                                c.setValue(p);
                                cs.setCriteria(c);

                                c = new XdatCriteria(user);
                                c.setSchemaField(xss.getRootElementName() + "/sharing/share/project");
                                c.setComparisonType("=");
                                c.setValue(p);
                                cs.setCriteria(c);
                            }

                            xss.setSearchWhere(cs);
                        }
                    } catch (Exception e) {
                        logger.error("", e);
                        getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        return null;
                    }
                }
            }
        }

        if (xss != null) {
            if (filepath != null && filepath.startsWith("results")) {
                if ((mt.equals(SecureResource.TEXT_CSV) || mt.equals(MediaType.APPLICATION_EXCEL)) &&
                    !filepath.endsWith(".csv")) {
                    this.setContentDisposition(filepath + ".csv");
                }
                try {
                    DisplaySearch ds = xss.getDisplaySearch(user);
                    String sortBy = this.getQueryVariable("sortBy");
                    String sortOrder = this.getQueryVariable("sortOrder");
                    if (sortBy != null) {
                        ds.setSortBy(sortBy);
                        if (sortOrder != null) {
                            ds.setSortOrder(sortOrder);
                        }
                    }

                    MaterializedViewI mv = null;

                    if (xss.getId() != null && !xss.getId().equals("")) {
                        mv = MaterializedView.getViewBySearchID(xss.getId(), user, getQueryVariable(MaterializedView.CACHING_HANDLER, MaterializedView.DEFAULT_MATERIALIZED_VIEW_SERVICE_CODE));
                    }

                    if (mv != null && (xss.getId().startsWith("@") || this.isQueryVariableTrue("refresh"))) {
                        mv.delete();
                        mv = null;
                    }

                    LinkedHashMap<String, Map<String, String>> cp = SearchResource.setColumnProperties(ds, user, this);

                    final XFTTable table;
                    if (mv != null) {
                        if (mt.equals(SecureResource.APPLICATION_XLIST)) {
                            table = (XFTTable) ds.execute(new RESTHTMLPresenter(TurbineUtils.GetRelativePath(ServletCall.getRequest(this.getRequest())), this.getCurrentURI(), user, sortBy), user.getLogin());
                        } else if (this.isQueryVariableTrue("guiStyle")) {
                            table = (XFTTable) ds.execute(new CSVPresenter(), user.getLogin());
                        } else {
                            table = mv.getData(null, null, null);
                        }
                    } else {
                        ds.setPagingOn(false);
                        if (mt.equals(SecureResource.APPLICATION_XLIST)) {
                            table = (XFTTable) ds.execute(new RESTHTMLPresenter(TurbineUtils.GetRelativePath(ServletCall.getRequest(this.getRequest())), this.getCurrentURI(), user, sortBy), user.getLogin());
                        } else if (this.isQueryVariableTrue("guiStyle")) {
                            table = (XFTTable) ds.execute(new CSVPresenter(), user.getLogin());
                        } else {
                            table = (XFTTable) ds.execute(null, user.getLogin());
                        }

                    }

                    Hashtable<String, Object> tableParams = new Hashtable<>();
                    tableParams.put("totalRecords", table.getNumRows());

                    return this.representTable(table, mt, tableParams, cp);
                } catch (Exception e) {
                    logger.error("", e);
                    this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                }
            } else {
                if (mt.equals(MediaType.TEXT_XML)) {
                    ItemXMLRepresentation rep = new ItemXMLRepresentation(xss.getItem(), MediaType.TEXT_XML);
                    if (sID.startsWith("@") || loadedFromFile) {
                        rep.setAllowDBAccess(false);
                    }

                    return rep;
                }
            }
        }

        return null;

    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void handlePut() {
        try {
            final UserI user = getUser();
            Reader sax = this.getRequest().getEntity().getReader();

            SAXReader reader = new SAXReader(user);
            XFTItem item = reader.parse(sax);

            if (!item.instanceOf("xdat:stored_search")) {
                this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
                return;
            }
            XdatStoredSearch search = new XdatStoredSearch(item);

            boolean isNew = false;

            if (search.getId() == null || !search.getId().equals(sID)) {
                search.setId(sID);
                isNew = true;
            } else {
                XFTItem xss = search.getCurrentDBVersion(false);
                if (xss == null) {
                    isNew = true;
                } else if (this.isQueryVariableTrue("saveAs")) {
                    while (xss != null) {
                        search.setId(search.getId() + "_1");
                        xss = search.getCurrentDBVersion(false);
                    }
                    isNew = true;
                }
            }
            if (isNew && search.getTag() != null) {
                CriteriaCollection cc = new CriteriaCollection("AND");
                cc.addClause("xdat:stored_search/tag", search.getTag());
                cc.addClause("xdat:stored_search/brief-description", search.getBriefDescription());
                ItemCollection result = ItemSearch.GetItems(cc, user, false);
                if (result.size() > 0) {
                    isNew = false;
                    search.setId(result.getFirst().getStringProperty("ID"));
                }
            }

            if (!Permissions.canQuery(user, search.getRootElementName())) {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                return;
            }

            final boolean isPrimary = (search.getTag() != null && (search.getId().equals(search.getTag() + "_" + search.getRootElementName()))) ||
                                      (org.apache.commons.lang3.StringUtils.isNotBlank(search.getBriefDescription()) && search.getBriefDescription().equals(DisplayManager.GetInstance().getPluralDisplayNameForElement(search.getRootElementName())));

            if (isNew && isPrimary) {
                if (!Permissions.can(user, "xnat:projectData/ID", search.getTag(), SecurityManager.DELETE)) {
                    isNew = false;
                }
            }

            if (this.isQueryVariableTrue("saveAs")) {
                while (search.getAllowedGroups_groupid().size() > 0) {
                    search.removeAllowedGroups_groupid(0);
                }

                while (search.getAllowedUser().size() > 0) {
                    search.removeAllowedUser(0);
                }
            }

            boolean found = false;
            for (XdatStoredSearchAllowedUser au : search.getAllowedUser()) {
                if (au.getLogin().equals(user.getLogin())) {
                    found = true;
                }
            }

            for (XdatStoredSearchGroupid ag : search.getAllowedGroups_groupid()) {
                if (Groups.isMember(user, ag.getGroupid())) {
                    found = true;
                }
            }

            if (!found && !isNew) {
                if (search.getTag() != null && !search.getTag().equals("")) {
                    if (!Permissions.canEdit(user, "xnat:projectData/ID", search.getTag())) {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                        return;
                    } else {
                        XdatStoredSearchAllowedUser au = new XdatStoredSearchAllowedUser(user);
                        au.setLogin(user.getLogin());
                        search.setAllowedUser(au);
                    }
                } else {
                    this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                    return;
                }
            }

            if (isNew && !found) {
                XdatStoredSearchAllowedUser au = new XdatStoredSearchAllowedUser(user);
                au.setLogin(user.getLogin());
                search.setAllowedUser(au);
            }

            try {
                SaveItemHelper.unauthorizedSave(search, user, false, true, this.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, (isNew) ? "Creating new stored search" : "Modified existing stored search"));
            } catch (Exception e) {
                logger.error("", e);
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            }

        } catch (SAXException e) {
            logger.error("", e);
            this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        } catch (Exception e) {
            logger.error("", e);
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    @Override
    public void handleDelete() {
        if (sID != null) {
            final UserI user = getUser();
            try {
                XdatStoredSearch search = XdatStoredSearch.getXdatStoredSearchsById(sID, user, false);

                if (search != null) {
                    XdatStoredSearchAllowedUser mine = null;
                    XdatStoredSearchGroupid group = null;

                    for (XdatStoredSearchAllowedUser au : search.getAllowedUser()) {
                        if (au.getLogin().equals(user.getLogin())) {
                            mine = au;
                            break;
                        }
                    }

                    for (XdatStoredSearchGroupid ag : search.getAllowedGroups_groupid()) {
                        if (Groups.isMember(user, ag.getGroupid())) {
                            group = ag;
                            break;
                        }
                    }

                    if (mine != null) {
                        if (search.getAllowedUser().size() > 1 || search.getAllowedGroups_groupid().size() > 0) {
                            SaveItemHelper.authorizedDelete(mine.getItem(), user, this.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, "Removed user from stored search"));
                        } else {
                            SaveItemHelper.authorizedDelete(search.getItem(), user, this.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, "Removed stored search"));
                        }
                    } else if (group != null) {
                        if (search.getAllowedUser().size() > 0 || search.getAllowedGroups_groupid().size() > 1) {
                            SaveItemHelper.authorizedDelete(group.getItem(), user, this.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, "Removed group from stored search"));
                        } else {
                            SaveItemHelper.authorizedDelete(search.getItem(), user, this.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, "Removed stored search"));
                        }
                    } else if (Roles.isSiteAdmin(user)) {
                        SaveItemHelper.authorizedDelete(search.getItem(), user, this.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, "Removed stored search"));
                    } else {
                        this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                    }
                }
            } catch (SAXException e) {
                logger.error("", e);
                this.getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
            } catch (Exception e) {
                logger.error("", e);
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            }
        }
    }
}
