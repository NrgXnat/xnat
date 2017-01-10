/*
 * web: org.nrg.xnat.restlet.resources.search.SearchResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.search;

import com.noelios.restlet.ext.servlet.ServletCall;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
import org.nrg.xdat.display.DisplayFieldReferenceI;
import org.nrg.xdat.display.HTMLLink;
import org.nrg.xdat.display.HTMLLinkProperty;
import org.nrg.xdat.display.SQLQueryField;
import org.nrg.xdat.om.XdatCriteriaSet;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.MaterializedViewI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.restlet.presentation.RESTHTMLPresenter;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.Reader;
import java.util.*;

public class SearchResource extends SecureResource {
    private static final Logger   logger    = LoggerFactory.getLogger(SearchResource.class);
    private              XFTTable table     = null;
    private              Long     rows      = null;
    private              String   tableName = null;

    private String rootElementName = null;

    private final Hashtable<String, Object>        tableParams = new Hashtable<>();
    private final Map<String, Map<String, String>> cp          = new LinkedHashMap<>();

    public SearchResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Override
    public boolean allowGet() {
        return false;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void handlePost() {
        try {
            String cacheRequest = getQueryVariable("cache");
            boolean cache = false;
            if (cacheRequest != null && cacheRequest.equalsIgnoreCase("true")) {
                cache = true;
            }

            XFTItem item = null;
            Representation entity = getRequest().getEntity();
            final UserI user = getUser();
            if (entity != null && entity.getMediaType() != null && entity.getMediaType().getName().equals(MediaType.MULTIPART_FORM_DATA.getName())) {
                try {
                    @SuppressWarnings("deprecation") org.apache.commons.fileupload.DefaultFileItemFactory factory = new org.apache.commons.fileupload.DefaultFileItemFactory();
                    org.restlet.ext.fileupload.RestletFileUpload upload = new org.restlet.ext.fileupload.RestletFileUpload(factory);

                    List<FileItem> items = upload.parseRequest(getRequest());

                    for (final FileItem fi : items) {
                        if (fi.getName().endsWith(".xml")) {
                            SAXReader reader = new SAXReader(user);
                            try {
                                item = reader.parse(fi.getInputStream());

                                if (!reader.assertValid()) {
                                    throw reader.getErrors().get(0);
                                }
                                if (XFT.VERBOSE) {
                                    System.out.println("Loaded XML Item:" + item.getProperName());
                                }

                                if (item != null) {
                                    completeDocument = true;
                                }
                            } catch (SAXParseException e) {
                                logger.error("", e);
                                getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e.getMessage());
                                throw e;
                            } catch (Exception e) {
                                logger.error("", e);
                                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                            }
                        }
                    }
                } catch (FileUploadException e) {
                    logger.error("", e);
                    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                }
            } else {
                if (entity != null) {
                    Reader sax = entity.getReader();
                    try {

                        SAXReader reader = new SAXReader(user);
                        item = reader.parse(sax);

                        if (!reader.assertValid()) {
                            throw reader.getErrors().get(0);
                        }
                        if (XFT.VERBOSE) {
                            System.out.println("Loaded XML Item:" + item.getProperName());
                        }

                        if (item != null) {
                            completeDocument = true;
                        }

                    } catch (SAXParseException e) {
                        logger.error("", e);
                        getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e.getMessage());
                        throw e;
                    } catch (Exception e) {
                        logger.error("", e);
                        getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                    }
                }
            }

            if (item == null || !item.instanceOf("xdat:stored_search")) {
                getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
                return;
            }

            XdatStoredSearch search = new XdatStoredSearch(item);

            // If a user has been manually added to a secret search, it is allowed (the criteria cannot be modified,
            // which is checked in the canQueryByAllowedUser() method)
            boolean allowed = canQueryByAllowedUser(search);

            // If the user is not explicitly allowed to perform a search...
            if (!allowed) {
                // See if the user can *implicitly* perform the search.
                if (!Permissions.canQuery(user, search.getRootElementName())) {
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                    return;
                }
            }

            rootElementName = search.getRootElementName();

            DisplaySearch ds = search.getDisplaySearch(user);

            String sortBy = getQueryVariable("sortBy");
            String sortOrder = getQueryVariable("sortOrder");
            if (sortBy != null) {
                ds.setSortBy(sortBy);
                if (sortOrder != null) {
                    ds.setSortOrder(sortOrder);
                }
            }

            MaterializedViewI mv = null;

            if (search.getId() != null && !search.getId().equals("")) {
                mv = MaterializedView.getViewBySearchID(search.getId(), user, getQueryVariable(MaterializedView.CACHING_HANDLER, MaterializedView.DEFAULT_MATERIALIZED_VIEW_SERVICE_CODE));
            }

            if (mv != null && (search.getId().startsWith("@") || isQueryVariableTrue("refresh"))) {
                mv.delete();
                mv = null;
            }

            cp.clear();
            cp.putAll(setColumnProperties(ds, user, this));

            if (!cache) {
                if (mv != null) {
                    table = mv.getData(null, null, null);
                } else {
                    ds.setPagingOn(false);
                    MediaType mt = getRequestedMediaType();
                    if (mt != null && mt.equals(SecureResource.APPLICATION_XLIST)) {
                        table = (XFTTable) ds.execute(new RESTHTMLPresenter(TurbineUtils.GetRelativePath(ServletCall.getRequest(getRequest())), null, user, sortBy), user.getLogin());
                    } else {
                        table = (XFTTable) ds.execute(null, user.getLogin());
                    }
                    //table=(XFTTable)ds.execute(null,user.getLogin());

                }
            } else {
                if (mv != null) {
                    if (search.getId() != null && !search.getId().equals("") && mv.getLast_access() != null) {
                        tableParams.put("last_access", mv.getLast_access());
                    }
                    table = mv.getData(null, null, 0);
                    tableName = mv.getTable_name();
                    rows = mv.getSize();
                } else {
                    ds.setPagingOn(false);
                    ds.addKeyColumn(true);

                    String query = ds.getSQLQuery(null);
                    query = StringUtils.replace(query, "'", "*'*");
                    query = StringUtils.replace(query, "*'*", "''");

                    String codeToUse = getQueryVariable(MaterializedView.CACHING_HANDLER, MaterializedView.DEFAULT_MATERIALIZED_VIEW_SERVICE_CODE);
                    mv = MaterializedView.createView(user, codeToUse);
                    if (search.getId() != null && !search.getId().equals("")) {
                        mv.setSearch_id(search.getId());
                    }
                    mv.setSearch_sql(query);
                    mv.setSearch_xml(item.writeToFlatString(0));

                    MaterializedView.save(mv, codeToUse);

                    if (search.getId() != null && !search.getId().equals("") && mv.getLast_access() != null) {
                        tableParams.put("last_access", mv.getLast_access());
                    }

                    tableName = mv.getTable_name();

                    int limit = 0;
                    if (getQueryVariable("limit") != null) {
                        limit = Integer.valueOf(getQueryVariable("limit"));
                    }
                    table = mv.getData(null, null, limit);
                    rows = mv.getSize();
                }
            }

            returnDefaultRepresentation();
        } catch (SAXException e) {
            logger.error("Failed POST", e);
            getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
        } catch (Exception e) {
            logger.error("Failed POST", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    @Override
    public Representation represent(Variant variant) {
        if (tableName != null) {
            tableParams.put("ID", tableName);
        }

        if (rows != null) {
            tableParams.put("totalRecords", rows);
        } else {
            tableParams.put("totalRecords", table.getNumRows());
        }

        if (rootElementName != null) {
            tableParams.put("rootElementName", rootElementName);
        }

        MediaType mt = overrideVariant(variant);

        return representTable(table, mt, tableParams, cp);
    }

    @SuppressWarnings("ConstantConditions")
    public static LinkedHashMap<String, Map<String, String>> setColumnProperties(DisplaySearch search, UserI user, SecureResource sr) {
        LinkedHashMap<String, Map<String, String>> cp = new LinkedHashMap<>();
        try {
            List<DisplayFieldReferenceI> fields = search.getAllFields("");

            //int fieldCount = visibleFields.size() + search.getInClauses().size();

            if (search.getInClauses().size() > 0) {
                for (int i = 0; i < search.getInClauses().size(); i++) {
                    cp.put("search_field" + i, new Hashtable<String, String>());
                    cp.get("search_field" + i).put("header", "");
                }
            }

            //POPULATE HEADERS

            for (DisplayFieldReferenceI dfr : fields) {
                try {
                    String id;
                    if (dfr.getValue() != null && !dfr.getValue().equals("")) {
                        if (dfr.getValue().equals("{XDAT_USER_ID}")) {
                            dfr.setValue(user.getID());
                        }
                    }
                    if (dfr.getElementName().equalsIgnoreCase(search.getRootElement().getFullXMLName())) {
                        id = dfr.getRowID().toLowerCase();
                    } else {
                        id = dfr.getElementSQLName().toLowerCase() + "_" + dfr.getRowID().toLowerCase();
                    }
                    cp.put(id, new Hashtable<String, String>());
                    cp.get(id).put("element_name", dfr.getElementName());
                    try {
                        String temp_id = dfr.getDisplayField().getId();
                        if (dfr.getValue() != null) {
                            temp_id += "=" + dfr.getValue();
                        }
                        cp.get(id).put("id", temp_id);
                    } catch (DisplayFieldNotFoundException e2) {
                        logger.error("", e2);
                    }
                    cp.get(id).put("xPATH", dfr.getElementName() + "." + dfr.getSortBy());

                    if (dfr.getHeader().equalsIgnoreCase("")) {
                        cp.get(id).put("header", " ");
                    } else {
                        cp.get(id).put("header", dfr.getHeader());
                    }

                    String t = dfr.getType();
                    if (t == null) {
                        try {
                            if (dfr.getDisplayField() != null) {
                                t = dfr.getDisplayField().getDataType();
                            }
                        } catch (DisplayFieldNotFoundException e) {
                            logger.error("", e);
                        }
                    }
                    if (t != null) {
                        cp.get(id).put("type", t);
                    }

                    try {
                        if (!dfr.isVisible()) {
                            cp.get(id).put("visible", "false");
                        }
                    } catch (DisplayFieldNotFoundException e1) {
                        logger.error("", e1);
                    }

                    if (dfr.getHTMLLink() != null && sr.getQueryVariable("format") != null && sr.getQueryVariable("format").equalsIgnoreCase("json")) {
                        cp.get(id).put("clickable", "true");
                        HTMLLink link = dfr.getHTMLLink();

                        StringBuilder linkProps = new StringBuilder("[");
                        int propCounter = 0;
                        for (HTMLLinkProperty prop : link.getProperties()) {
                            if (propCounter++ > 0) {
                                linkProps.append(",");
                            }
                            linkProps.append("{");
                            linkProps.append("\"name\":\"");
                            linkProps.append(prop.getName()).append("\"");
                            linkProps.append(",\"value\":\"");
                            String v = prop.getValue();
                            v = StringUtils.replace(v, "@WEBAPP", TurbineUtils.GetRelativePath(ServletCall.getRequest(sr.getRequest())) + "/");

                            linkProps.append(v).append("\"");

                            if (prop.getInsertedValues().size() > 0) {
                                linkProps.append(",\"inserts\":[");
                                int valueCounter = 0;
                                for (Map.Entry<String, String> entry : prop.getInsertedValues().entrySet()) {
                                    if (valueCounter++ > 0) {
                                        linkProps.append(",");
                                    }
                                    linkProps.append("{\"name\":\"");
                                    linkProps.append(entry.getKey()).append("\"");
                                    linkProps.append(",\"value\":\"");

                                    String insert_value = entry.getValue();
                                    if (insert_value.startsWith("@WHERE")) {
                                        try {
                                            if (dfr.getDisplayField() instanceof SQLQueryField) {
                                                Object insertValue = dfr.getValue();

                                                if (insertValue == null) {
                                                    insertValue = "NULL";
                                                } else {
                                                    if (insertValue.toString().contains(",")) {
                                                        insert_value = insert_value.substring(6);
                                                        //noinspection Duplicates
                                                        try {
                                                            Integer i = Integer.parseInt(insert_value);
                                                            ArrayList<String> al = XftStringUtils.CommaDelimitedStringToArrayList(insertValue.toString());
                                                            insertValue = al.get(i);
                                                        } catch (Throwable e) {
                                                            logger.error("", e);
                                                        }
                                                    }
                                                }

                                                linkProps.append("@").append(insertValue);
                                            }
                                        } catch (DisplayFieldNotFoundException e) {
                                            logger.error("", e);
                                        }
                                    } else {
                                        if (!dfr.getElementName().equalsIgnoreCase(search.getRootElement().getFullXMLName())) {
                                            insert_value = dfr.getElementSQLName().toLowerCase() + "_" + insert_value.toLowerCase();
                                        } else {
                                            insert_value = insert_value.toLowerCase();
                                        }
                                        if (cp.get(insert_value) == null) {
                                            cp.put(insert_value, new Hashtable<String, String>());

                                            if (!dfr.getElementName().equalsIgnoreCase(search.getRootElement().getFullXMLName())) {
                                                cp.get(insert_value).put("xPATH", dfr.getElementName() + "." + insert_value);
                                            } else {
                                                cp.get(insert_value).put("xPATH", insert_value);
                                            }
                                        }

                                        linkProps.append(insert_value);
                                    }
                                    linkProps.append("\"}");
                                }
                                linkProps.append("]");
                            }
                            linkProps.append("}");
                        }
                        linkProps.append("]");

                        cp.get(id).put("linkProps", linkProps.toString());
                    }

                    if (dfr.isImage()) {
                        cp.get(id).put("imgRoot", TurbineUtils.GetRelativePath(ServletCall.getRequest(sr.getRequest())) + "/");
                    }
                } catch (XFTInitException | ElementNotFoundException e) {
                    logger.error("", e);
                }

            }

            cp.put("quarantine_status", new Hashtable<String, String>());
        } catch (ElementNotFoundException | XFTInitException e) {
            logger.error("", e);
        }

        return cp;
    }

    private boolean canQueryByAllowedUser(final XdatStoredSearch search) {
        boolean allowed = false;
        if (StringUtils.isNotBlank(search.getId())) {
            //need to check against unmodified stored search
            final UserI                            user   = getUser();
            final org.nrg.xdat.om.XdatStoredSearch stored = XdatStoredSearch.getXdatStoredSearchsById(search.getId(), user, true);

            //if the user was added to the search
            if (stored != null && stored.hasAllowedUser(user.getUsername())) {
                //confirm it has a WHERE clause and hasn't been modified
                if (XdatCriteriaSet.compareCriteriaSets(stored.getSearchWhere(), search.getSearchWhere())) {
                    allowed = true;
                }
            }
        }
        return allowed;
    }
}
