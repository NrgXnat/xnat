/*
 * core: org.nrg.xdat.search.DisplaySearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.search;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
import org.nrg.xdat.collections.DisplayFieldWrapperCollection;
import org.nrg.xdat.display.*;
import org.nrg.xdat.exceptions.InvalidSearchException;
import org.nrg.xdat.om.XdatCriteria;
import org.nrg.xdat.om.XdatCriteriaSet;
import org.nrg.xdat.om.XdatSearchField;
import org.nrg.xdat.presentation.PresentationA;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.SchemaFieldI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.*;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Tim
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DisplaySearch implements TableSearchI {
    private static final Logger logger = LoggerFactory.getLogger(DisplaySearch.class);
    public static final String QUERY_MODE_CRITERIA = "criteria";
    public static final String QUERY_MODE_BYID = "byId";
    private static final int QUERY_MODE_VAL_CRITERIA = 0;
    private static final int QUERY_MODE_VAL_BYID = 1;
    private static final int QUERY_MODE_VAL_NONE = 2;
    private SchemaElement rootElement = null;
    private String display = "default";
    private final List<String[]> additionalViews = new ArrayList<>();
    private String sortBy = "";
    private String sortOrder = "";
    private String customSortBy = "";
    private String description = "";
    private boolean useVersions = false;

    public String resultsTableName = null;

    private String title = "";

    private DisplayFieldWrapperCollection fields = new DisplayFieldWrapperCollection();

    private Hashtable<String, String> inClauses = new Hashtable<>();

    private org.nrg.xft.search.CriteriaCollection criteria = new org.nrg.xft.search.CriteriaCollection("OR");

    private boolean pagingOn = false;
    private int currentPageNum = 0;
    private int rowsPerPage = 30;
    private int numRows = 0;
    private int pages = 1;

    private boolean newQuery = true;

    public boolean allowDiffs = true;

    private XFTTableI table = null;
    private XFTTableI presentedTable = null;
    private PresentationA lastPresenter = null;

    private UserI user = null;

    private String level = ViewManager.ACCESSIBLE;

    private Hashtable isMultipleRelationship = new Hashtable();

    private Hashtable webFormValues = new Hashtable();

    String query = "";

    private boolean isStoredSearch = false;
    private org.nrg.xdat.om.XdatStoredSearch storedSearch = null;

    public DisplaySearch() {
    }


    /**
     * @param presenter The presenter.
     * @param login     The login to use.
     * @return The table resulting from the query.
     * @throws Exception When an error occurs.
     */
    public XFTTableI execute(PresentationA presenter, String login) throws Exception {
        lastPresenter = presenter;

        query = this.getSQLQuery(presenter);
        resetResultsTableName();

        String db = rootElement.getGenericXFTElement().getDbName();
        if (pagingOn) {
            query = StringUtils.replace(query, "'", "*'*");
            query = StringUtils.replace(query, "*'*", "''");

            Long count = MaterializedView.createView(this.getResultsTableName(), query, user, MaterializedView.DEFAULT_MATERIALIZED_VIEW_SERVICE_CODE);
            try {

                if (count.intValue() == 0) {
                    table = new XFTTable();
                    presentedTable = new XFTTable();
                } else {
                    currentPageNum = 0;
                    int offset = currentPageNum * rowsPerPage;
                    if (offset < count.intValue()) {
                        table = MaterializedView.retrieveView(this.getResultsTableName(), user, offset, rowsPerPage);
                        this.numRows = count.intValue();
                        calculatePages();
                        newQuery = false;
                    } else {
                        table = new XFTTable();
                        presentedTable = new XFTTable();
                        throw new Exception("Index out of bounds: Index:" + offset + " Rows:" + count);
                    }
                }
            } catch (RuntimeException e) {
                table = new XFTTable();
                presentedTable = new XFTTable();
                throw e;
            }
        } else {
            table = TableSearch.Execute(query, db, login);
        }

        //logger.debug("BEGIN FORMAT FOR PRESENTATION");
        if (presenter != null) {
            presenter.setRootElement(rootElement);
            presenter.setDisplay(display);
            presenter.setAdditionalViews(additionalViews);
            presentedTable = presenter.formatTable(table, this, this.allowDiffs);
        } else {
            presentedTable = table;
        }
        //logger.debug("END FORMAT FOR PRESENTATION");
        return presentedTable;
    }

    @SuppressWarnings("unused")
    public Long createSearchCache(PresentationA presenter, String login) throws Exception {
        lastPresenter = presenter;

        query = this.getSQLQuery(presenter);
        resetResultsTableName();

        query = StringUtils.replace(query, "'", "*'*");
        query = StringUtils.replace(query, "*'*", "''");

        return MaterializedView.createView(this.getResultsTableName(), query, user, MaterializedView.DEFAULT_MATERIALIZED_VIEW_SERVICE_CODE);

    }

    @SuppressWarnings("unused")
    public void clearTables() {
        presentedTable = null;

        table = null;
    }

    private boolean hasSchemaOnlyCriteria() {
        return this.criteria != null && this.criteria.numClauses() > 0 && this.criteria.numClauses() == this.criteria.numSchemaClauses();
    }

    public String getSQLQuery(PresentationA presenter) throws Exception {
        ArrayList displayFields = new ArrayList();

        ElementDisplay ed = DisplayManager.GetElementDisplay(rootElement.getFullXMLName());
        DisplayVersion dv = null;
        if (this.getFields().size() == 0) {
            useVersions = true;
            if (presenter != null && !presenter.getVersionExtension().equalsIgnoreCase("")) {
                dv = ed.getVersion(display + "_" + presenter.getVersionExtension(), display);
            } else {
                dv = ed.getVersion(display, "default");
            }

            displayFields.addAll(dv.getSortedDisplayFieldRefs());

            if (!additionalViews.isEmpty()) {
                for (Object additionalView : additionalViews) {
                    String[] key = (String[]) additionalView;
                    String elementName = key[0];
                    String version = key[1];
                    SchemaElementI foreign = SchemaElement.GetElement(elementName);

                    ElementDisplay foreignEd = DisplayManager.GetElementDisplay(foreign.getFullXMLName());
                    final DisplayVersion foreignDV;
                    if (presenter != null && !presenter.getVersionExtension().equalsIgnoreCase("")) {
                        foreignDV = foreignEd.getVersion(version + "_" + presenter.getVersionExtension(), version);
                    } else {
                        foreignDV = foreignEd.getVersion(version, "default");
                    }
                    displayFields.addAll(foreignDV.getSortedDisplayFieldRefs());
                }
            }

            this.setAllowDiffs(dv.isAllowDiffs());

            if (this.inClauses.size() > 0) {
                for (Map.Entry<String, String> entry : inClauses.entrySet()) {
                    for (String key : XftStringUtils.CommaDelimitedStringToArrayList(entry.getKey())) {
                        try {
                            DisplayField df = DisplayField.getDisplayFieldForUnknownPath(key);

                            if (df != null) {
                                DisplayFieldReferenceI ref = new DisplayFieldWrapper(df);

                                boolean found = false;

                                for (Object displayField : displayFields) {
                                    DisplayFieldReferenceI temp = (DisplayFieldReferenceI) displayField;
                                    if (temp.getId().equalsIgnoreCase(df.getId()) && temp.getElementName().equalsIgnoreCase(ref.getElementName())) {
                                        found = true;
                                        break;
                                    }
                                }

                                if (!found) {
                                    displayFields.add(ref);
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        } else {
            useVersions = false;
            displayFields.addAll(this.getFields().getSortedFields());

            if (this.inClauses.size() > 0) {
                for (Map.Entry<String, String> entry : inClauses.entrySet()) {
                    for (String key : XftStringUtils.CommaDelimitedStringToArrayList(entry.getKey())) {
                        try {
                            DisplayField df = DisplayField.getDisplayFieldForUnknownPath(key);

                            if (df != null) {
                                DisplayFieldReferenceI ref = new DisplayFieldWrapper(df);

                                boolean found = false;

                                for (Object displayField : displayFields) {
                                    DisplayFieldReferenceI temp = (DisplayFieldReferenceI) displayField;
                                    if (temp.getId().equalsIgnoreCase(df.getId()) && temp.getElementName().equalsIgnoreCase(ref.getElementName())) {
                                        found = true;
                                        break;
                                    }
                                }

                                if (!found) {
                                    displayFields.add(ref);
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }

        Iterator iter = displayFields.iterator();
        while (iter.hasNext()) {
            DisplayFieldReferenceI dfw = (DisplayFieldReferenceI) iter.next();
            try {
                DisplayField df = dfw.getDisplayField();
                if (df instanceof SQLQueryField) {
                    if (dfw.getValue() != null && dfw.getValue().equals("{XDAT_USER_ID}")) {
                        dfw.setValue(user.getID());
                    }
                }
            } catch (Exception ignored) {

            }
        }

        StringBuilder sb = new StringBuilder();
        StringBuilder where = new StringBuilder();
        StringBuilder join = new StringBuilder();
        StringBuilder select = new StringBuilder();
        StringBuilder orderBy = new StringBuilder();

        QueryOrganizer qo = new QueryOrganizer(this.getRootElement(), user, level);

        if (hasSchemaOnlyCriteria()) {
            qo.setWhere(criteria);
        }

        try {
            qo.addField(getRootElement().getFullXMLName() + "/meta/status");
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        }

//		build ORDER BY clause
        if ((sortBy == null || sortBy.equalsIgnoreCase("")) && (customSortBy.equalsIgnoreCase(""))) {
            if(XDAT.getBoolSiteConfigurationProperty("defaultToSortedListings",Boolean.TRUE)) {
            if (dv == null) {
                DisplayFieldWrapper dfw = ((DisplayFieldWrapper) this.getFields().getSortedFields().get(0));
                sortBy = dfw.getId();
            } else {
                sortBy = dv.getDefaultOrderBy();
                sortOrder = dv.getDefaultSortOrder();
            }
        }
        }

        if (sortBy != null && !sortBy.equalsIgnoreCase("")) {
            if (!this.customSortBy.equalsIgnoreCase("")) {
                customSortBy = "";
            }

            if (sortOrder == null || sortOrder.equalsIgnoreCase("")) {
                if (sortBy.contains(".")) {
                    SchemaElement e = SchemaElement.GetElement(sortBy.substring(0, sortBy.indexOf(".")));
                    String fieldID = sortBy.substring(sortBy.indexOf(".") + 1);
                    if (fieldID.contains(".")) {
                        fieldID = fieldID.substring(0, fieldID.indexOf("."));
                    }
                    DisplayField df = e.getDisplayField(fieldID);
                    sortOrder = df.getSortOrder();
                } else {
                    DisplayField df = rootElement.getDisplayField(sortBy);
                    sortOrder = df.getSortOrder();
                }
            }
        }

        if (this.getCustomSortBy().equalsIgnoreCase("")) {
            if (sortBy.contains(".")) {
                SchemaElement e = SchemaElement.GetElement(sortBy.substring(0, sortBy.indexOf(".")));
                String fieldID = sortBy.substring(sortBy.indexOf(".") + 1);
                if (fieldID.contains(".")) {
                    fieldID = fieldID.substring(0, fieldID.indexOf("."));
                }
                DisplayField df = e.getDisplayField(fieldID);

                for (final Object[] o : df.getSchemaFields()) {
                    String s = (String) o[0];
                    qo.addField(s);
                }
            } else if(StringUtils.isNotEmpty(sortBy)) {
                DisplayField df = rootElement.getDisplayField(sortBy);
                for (final Object[] o : df.getSchemaFields()) {
                    String s = (String) o[0];
                    qo.addField(s);
                }
            }
        }

        //build JOIN clause
        iter = displayFields.iterator();
        while (iter.hasNext()) {
            DisplayFieldReferenceI dfw = (DisplayFieldReferenceI) iter.next();
            try {
                DisplayField df = dfw.getDisplayField();

                Iterator sfs = df.getSchemaFields().iterator();
                while (sfs.hasNext()) {
                    Object[] o = (Object[]) sfs.next();
                    String s = (String) o[0];
                    qo.addField(s);
                }

                sfs = dfw.getSecondaryFields().iterator();
                while (sfs.hasNext()) {
                    String s = (String) sfs.next();
                    DisplayField df2 = DisplayField.getDisplayFieldForUnknownPath(s);

                    if (df2 != null) {
                        for (final Object[] o : df2.getSchemaFields()) {
                            String f = (String) o[0];
                            qo.addField(f);
                        }
                    }
                }

                if (df instanceof SQLQueryField) {
//System.out.println("SUBQUERY_" + df.getParentDisplay().getElementName() + ".SUBQUERYFIELD_" + df.getId() +"." + StringUtils.replace(StringUtils.replace((dfw).getValue().toString(), ",", "_com_"),":", "_col_"));
                    String df_value = (dfw.getValue() == null) ? "NULL" : dfw.getValue().toString();
                    qo.addField("SUBQUERY_" + df.getParentDisplay().getElementName() + ".SUBQUERYFIELD_" + df.getId() + "." + df_value);
                }
            } catch (DisplayFieldNotFoundException e) {
                if (dfw.getType() != null) {
                    if (dfw.getType().equalsIgnoreCase("COUNT")) {
//System.out.println("VIEW_" + rootElement.getFullXMLName() + ".COUNT_" + dfw.getElementName() +".count");
                        qo.addField("VIEW_" + rootElement.getFullXMLName() + ".COUNT_" + dfw.getElementName() + ".count");
                    }
                } else {
                    logger.error("", e);
                }
            }
        }

        if (this.inClauses.size() > 0) {
            for (Map.Entry<String, String> entry : inClauses.entrySet()) {
                for (String key : XftStringUtils.CommaDelimitedStringToArrayList(entry.getKey())) {
                    try {
                        DisplayField df = DisplayField.getDisplayFieldForUnknownPath(key);

                        if (df != null) {
                            for (final Object[] o : df.getSchemaFields()) {
                                String s = (String) o[0];
                                if (XFT.VERBOSE) System.out.println(s);
                                qo.addField(s);
                            }
                        }
                    } catch (DisplayFieldNotFoundException e) {
                        logger.error("", e);
                    }
                }
            }
        }

        StringBuilder query = new StringBuilder(qo.buildQuery());
        if (!query.toString().startsWith("SELECT DISTINCT")) {
            query = new StringBuilder("SELECT DISTINCT " + query.substring(6));
        }
        join.append(" FROM (").append(query).append(") SEARCH");

        //build SELECT clause
        List<String> added = new ArrayList();
        iter = displayFields.iterator();
        while (iter.hasNext()) {
            DisplayFieldReferenceI dfr = (DisplayFieldReferenceI) iter.next();
            try {
                DisplayField df = dfr.getDisplayField();
                if (df instanceof SQLQueryField) {
                    if (dfr.getValue() != null) {
                        if (dfr.getValue().equals("{XDAT_USER_ID}")) {
                            dfr.setValue(user.getID());
                        }
                    }
                }

                String dfrRowID = dfr.getRowID();
                if (!added.contains(dfr.getElementName() + dfrRowID)) {
                    String content = this.getSQLContent(df, qo);
                    if (df instanceof SQLQueryField) {
                        String df_value = (dfr.getValue() == null) ? "NULL" : dfr.getValue().toString();
                        content = qo.getFieldAlias(df.getParentDisplay().getElementName() + ".SUBQUERYFIELD_" +
                                                          df.getId() + "." + df_value);
                    }

                    select.append((added.size() == 0) ? "SELECT " : ", ");
                    select.append(content);
                    select.append(" AS ");

                    SchemaElementI se = df.getParentDisplay().getSchemaElement();
                    if (se.getFullXMLName().equalsIgnoreCase(this.getRootElement().getFullXMLName())) {
                        select.append(DisplayFieldAliasCache.getAlias(dfrRowID));
                    }else{
                        select.append(DisplayFieldAliasCache.getAlias(dfr.getElementSQLName() + "_" + dfrRowID));
                    }
                    added.add(dfr.getElementName() + dfrRowID);
                }

                for (final String s : dfr.getSecondaryFields()) {
                    DisplayField df2 = DisplayField.getDisplayFieldForUnknownPath(s);
                    if (df2 != null && !added.contains(dfr.getElementName() + df2.getId())) {
                        String content = this.getSQLContent(df2, qo);

                        select.append((added.size() == 0) ? "SELECT " : ", ");
                        select.append(content);

                        SchemaElementI se = SchemaElement.GetElement(XftStringUtils.GetRootElementName(s));
                        if (se.getFullXMLName().equalsIgnoreCase(this.getRootElement().getFullXMLName())) {
                            select.append(" AS ").append(df2.getId());
                        } else {
                            select.append(" AS ").append(se.getSQLName()).append("_").append(df2.getId());
                        }
                        added.add(dfr.getElementName() + df2.getId());
                    }
                }
            } catch (DisplayFieldNotFoundException e) {
                if (dfr.getType() != null) {
                    if (dfr.getType().equalsIgnoreCase("COUNT")) {
                        select.append(", ");
                        SchemaElementI se = SchemaElement.GetElement(dfr.getElementName());
                        select.append(se.getSQLName()).append("_COUNT").append(" AS ").append(se.getSQLName()).append("_").append(dfr.getId());
                    }
                } else {
                    logger.error("", e);
                }
            }
        }

        String statusCol = qo.getFieldAlias(getRootElement().getFullXMLName() + "/meta/status", "SEARCH");
        select.append(", ");
        select.append(statusCol).append(" AS QUARANTINE_STATUS ");

        final List<String> addons = getAddOns(displayFields);
        if (addons.size() > 0) {
            String rootField = rootElement.getGenericXFTElement().getFilter();
            if (rootField != null) {
                for (final Object addon : addons) {
                    String fName = (String) addon;
                    SchemaElementI foreign = SchemaElement.GetElement(fName);
                    if (isMultipleRelationship(foreign)) {
                        final String foreignFilter = foreign.getGenericXFTElement().getFilterField();
                        final String localType = GenericWrapperElement.GetFieldForXMLPath(rootField).getXMLType().getLocalType();
                        final String foreignType = GenericWrapperElement.GetFieldForXMLPath(foreignFilter).getXMLType().getLocalType();
                        if (localType.equalsIgnoreCase(foreignType)) {
                            select.append(", ").append(XftStringUtils.SQLMaxCharsAbbr(rootElement.getSQLName() + "_" + foreign.getSQLName() + "_DIFF"));
                        }
                    }
                }
            }
        }

        if (this.addKeyColumn()) {
            String keyCol = qo.getFieldAlias(qo.getKeys().get(0), "SEARCH");
            select.append(", ");
            select.append(keyCol).append(" AS KEY ");
        }

        if(StringUtils.isNotEmpty(sortBy)) {
        orderBy.append(" ORDER BY ");
        if (this.getCustomSortBy().equalsIgnoreCase("")) {
            if (sortBy.contains(".")) {
                SchemaElement e = SchemaElement.GetElement(sortBy.substring(0, sortBy.indexOf(".")));
                String fieldID = sortBy.substring(sortBy.indexOf(".") + 1);
                if (fieldID.contains(".")) {
                    fieldID = fieldID.substring(0, fieldID.indexOf("."));
                }
                DisplayField df = e.getDisplayField(fieldID);
                String content = this.getSQLContent(df, qo);
                if (df instanceof SQLQueryField) {
                    iter = displayFields.iterator();
                    boolean matched = false;
                    DisplayFieldReferenceI dfr = null;
                    while (iter.hasNext()) {
                        dfr = (DisplayFieldReferenceI) iter.next();
                        if (dfr.getDisplayField().getId().equals(df.getId())) {
                            matched = true;
                            break;
                        }
                    }

                    if (matched) {
                        String df_value = (dfr.getValue() == null) ? "NULL" : dfr.getValue().toString();

                        content = qo.getFieldAlias(df.getParentDisplay().getElementName() + ".SUBQUERYFIELD_" + df.getId() + "." + df_value);
                    } else {
                        content = "1";
                    }

                }

                orderBy.append("(").append(content).append(") ");
                sortOrder = sortOrder.trim();
                if (sortOrder.equalsIgnoreCase("desc") || sortOrder.equalsIgnoreCase("asc")) {
                    orderBy.append(sortOrder);
                }

            } else {
                DisplayField df = rootElement.getDisplayField(sortBy);
                String content = this.getSQLContent(df, qo);
                orderBy.append("(").append(content).append(") ");
                sortOrder = sortOrder.trim();
                if (sortOrder.equalsIgnoreCase("desc") || sortOrder.equalsIgnoreCase("asc")) {
                    orderBy.append(sortOrder);
                }
            }
        } else {
            orderBy.append(this.getCustomSortBy());
            }
        }

        if (!hasSchemaOnlyCriteria()) {

            QueryOrganizer whereqo = new QueryOrganizer(this.getRootElement(), user, level);

            //build WHERE clause
            Iterator criteriaIter = criteria.getSchemaFields().iterator();
            while (criteriaIter.hasNext()) {
                Object[] o = (Object[]) criteriaIter.next();
                String s = (String) o[0];
                whereqo.addField(s);
            }

            for (DisplayCriteria dc : criteria.getSubQueries()) {

                whereqo.addField("SUBQUERY_" + dc.getElementName() + ".SUBQUERYFIELD_" + dc.getField() + "." + dc.getWhere_value());
            }

            Iterator keys = rootElement.getAllPrimaryKeys().iterator();
            ArrayList keyXMLFields = new ArrayList();
            while (keys.hasNext()) {
                SchemaFieldI sf = (SchemaFieldI) keys.next();
                String key = sf.getXMLPathString(rootElement.getFullXMLName());
                keyXMLFields.add(key);
                whereqo.addField(key);
            }

            String subQuery = whereqo.buildQuery(false);

            int whereCounter = 0;
            criteriaIter = criteria.iterator();
            while (criteriaIter.hasNext()) {
                SQLClause c = (SQLClause) criteriaIter.next();
                if (whereCounter++ == 0) {
                    where.append(" \nWHERE ");
                } else {
                    where.append(" AND ");
                }

                where.append(c.getSQLClause(whereqo));

            }

            StringBuilder whereQuery = new StringBuilder("SELECT DISTINCT ");

            for (int i = 0; i < keyXMLFields.size(); i++) {
                if (i > 0) {
                    whereQuery.append(", ");
                }
                whereQuery.append(whereqo.getFieldAlias((String) keyXMLFields.get(i)));
            }

            whereQuery.append(" FROM (").append(subQuery).append(") WHERE_CLAUSE ").append(where);

            query = new StringBuilder(select.toString() + join.toString() + " RIGHT JOIN (" + whereQuery + ") WHERE_CLAUSE ON ");
            //query = "SELECT SEARCH.* FROM ("+ whereQuery +") WHERE_CLAUSE LEFT JOIN (" + select + join + orderBy + ") SEARCH ON ";

            keys = rootElement.getAllPrimaryKeys().iterator();
            int keyCounter = 0;
            while (keys.hasNext()) {
                SchemaFieldI sf = (SchemaFieldI) keys.next();
                String key = sf.getXMLPathString(rootElement.getFullXMLName());
                if (keyCounter++ > 0) {
                    query.append(" AND ");
                }
                query.append("WHERE_CLAUSE.").append(whereqo.getFieldAlias(key)).append("=SEARCH.").append(qo.getFieldAlias(key));

            }

            query.append(orderBy.toString());
        } else {
            query = new StringBuilder(select.toString() + join.toString() + orderBy.toString());
        }

        if (this.inClauses.size() > 0) {
            sb.append(query);

            StringBuilder sb2 = new StringBuilder();
            sb2.append("SELECT DISTINCT DISPLAY_SEARCH.*");

            int inCounter = 0;
            while (inCounter < inClauses.size()) {
                sb2.append(", search").append(inCounter).append(".strings AS SEARCH_FIELD").append(inCounter);

                sb2.append(", search").append(inCounter).append(".sort_order AS SORT_ORDER").append(inCounter);
                inCounter++;
            }

            sb2.append(" FROM (").append(sb.toString()).append(") AS DISPLAY_SEARCH");

            inCounter = 0;

            StringBuilder orderByClause = new StringBuilder();
            for (Map.Entry<String, String> entry : inClauses.entrySet()) {
                String values = entry.getValue();
                sb2.append(" RIGHT JOIN (SELECT * FROM stringstosortedtable(").append(values);
                sb2.append(")) AS search").append(inCounter);

                int subCounter = 0;
                for (String key : XftStringUtils.CommaDelimitedStringToArrayList(entry.getKey())) {
                    DisplayField inDF = DisplayField.getDisplayFieldForUnknownPath(key);
                    if (inDF != null) {
                        String keyField = inDF.getId();

                        if (!inDF.getParentDisplay().getElementName().equalsIgnoreCase(this.getRootElement().getFullXMLName())) {
                            keyField = inDF.getParentDisplay().getSchemaElement().getSQLName() + "_" + keyField;
                        }

                        if (subCounter++ == 0) {
                            sb2.append(" ON DISPLAY_SEARCH");
                            sb2.append(".").append(keyField).append("=search").append(inCounter).append(".strings");
                        } else {
                            sb2.append(" OR DISPLAY_SEARCH");
                            sb2.append(".").append(keyField).append("=search").append(inCounter).append(".strings");
                        }
                    }
                }

                if (inCounter == 0) {
                    orderByClause.append(" ORDER BY search0.sort_order");
                } else {
                    orderByClause.append(", search").append(inCounter).append(".sort_order");
                }

                inCounter++;
            }
            sb2.append(orderByClause);
            sb = sb2;
        } else {
            sb.append(query);
        }
        return sb.toString();
    }

    private boolean addKey = false;

    public boolean addKeyColumn() {
        return addKey;
    }

    public void addKeyColumn(boolean b) {
        addKey = b;
    }

    /**
     * @param foreign The foreign relationship.
     * @return Indicates whether the foreign key schema element is a multiple relationship.
     */
    public boolean isMultipleRelationship(SchemaElementI foreign) {
        if (foreign.getFullXMLName().equals(rootElement.getFullXMLName())) {
            return false;
        } else {
            Boolean b = (Boolean) this.isMultipleRelationship.get(foreign.getSQLName());
            if (b == null) {
                b = IsMultipleReference(rootElement, foreign);
                this.isMultipleRelationship.put(foreign.getSQLName(), b);
            }
            return b;
        }
    }

    private static boolean IsMultipleReference(SchemaElementI rootElement, SchemaElementI foreign) {
        boolean isMultiple = true;
        //XFT.LogCurrentTime("isMultipleRelationship :1");
        String connectionType = QueryOrganizer.GetConnectionType(rootElement.getFullXMLName(), foreign.getFullXMLName());
        switch (connectionType) {
            case "schemaelement":
                isMultiple = false;
                break;
            case "arc":
                ArcDefinition arcDefine = DisplayManager.GetInstance().getArcDefinition(rootElement, foreign);
                if (arcDefine != null) {
                    if (arcDefine.getBridgeElement().equals(rootElement.getFullXMLName())) {
                        return false;
                    } else if (arcDefine.getBridgeElement().equals(foreign.getFullXMLName())) {
                        return false;
                    } else {
                        String s = arcDefine.getClosestField();
                        return s != null;
                    }
                }
                break;
            case "connection":
                String[] connection = rootElement.getGenericXFTElement().findSchemaConnection(foreign.getGenericXFTElement());

                //XFT.LogCurrentTime("isMultipleRelationship :2");
                if (connection != null) {
                    if (!connection[2].equalsIgnoreCase("reference")) {
                        try {
                            isMultiple = GenericWrapperElement.IsMultipleReference(connection[0]);
                        } catch (RuntimeException ignored) {
                        }
                    }
                }
                break;
            case "multi-leveled":
                ArrayList checked = new ArrayList();
                String s = foreign.getFullXMLName();
                String mappingElement = null;

                for (final ArcDefinition arc : DisplayManager.GetInstance().getArcDefinitions(rootElement)) {
                    if (!arc.getBridgeElement().equals(rootElement.getFullXMLName())) {
                        if (!checked.contains(arc.getBridgeElement())) {
                            checked.add(arc.getBridgeElement());
                            if (QueryOrganizer.CanConnect(arc.getBridgeElement(), s)) {
                                mappingElement = arc.getBridgeElement();
                                break;
                            }
                        }
                    }

                    Iterator arcMembers = arc.getMembers();
                    while (arcMembers.hasNext()) {
                        String member = (String) arcMembers.next();
                        if (!checked.contains(member)) {
                            checked.add(member);
                            if (QueryOrganizer.CanConnect(member, s)) {
                                mappingElement = member;
                                break;
                            }
                        }
                    }
                }

                if (mappingElement == null) {
                    //UNKNOWN CONNECTION
                    return true;
                } else {
                    try {
                        SchemaElementI mappingE = SchemaElement.GetElement(mappingElement);
                        return IsMultipleReference(rootElement, mappingE) || IsMultipleReference(mappingE, foreign);
                    } catch (Exception e) {
                        logger.error("", e);
                        return true;
                    }
                }

        }

        return isMultiple;
    }

    public List<String> getAddOns(final List<DisplayFieldReferenceI> displayFields) {
        final List<String> al = new ArrayList<>();
        final List<String> done = new ArrayList<>();
        for (final DisplayFieldReferenceI dfr : displayFields) {
            if (!done.contains(dfr.getElementName())) {
                if (dfr.getType() == null || dfr.getType().equals("")) {
                    done.add(dfr.getElementName());
                    if (!rootElement.getFullXMLName().equalsIgnoreCase(dfr.getElementName())) {

                        al.add(dfr.getElementName());
                    }
                }
            }
        }
        return al;
    }

    public String getSQLContent(DisplayField df2, QueryOrganizer qo) throws FieldNotFoundException {
        String content = df2.getSqlContent();

        for (final DisplayFieldElement dfe : df2.getElements()) {
            String dfeAlias;
            if (dfe.getSchemaElementName().equalsIgnoreCase("")) {
                String viewName = df2.getParentDisplay().getElementName() + ".";
                viewName += dfe.getViewName() + "." + dfe.getViewColumn();
                if (qo.getFieldAlias(viewName) != null) {
                    dfeAlias = qo.getFieldAlias(viewName);
                } else {
                    dfeAlias = dfe.getViewName() + "_" + dfe.getViewColumn();
                }
            } else {
                if (dfe.getXdatType() == null || dfe.getXdatType().equalsIgnoreCase("")) {
                    dfeAlias = qo.getFieldAlias(dfe.getSchemaElementName(), "SEARCH");
                } else {
                    if (df2.getParentDisplay().getElementName().equals(this.getRootElement().getFullXMLName())) {
                        try {
                            dfeAlias = SchemaElement.GetElement(dfe.getSchemaElementName()).getSQLName() + "_COUNT";
                        } catch (XFTInitException e) {
                            logger.error("Error initializing XFT", e);
                            dfeAlias = "'ERROR'";
                        } catch (ElementNotFoundException e) {
                            logger.error("XFT element not found: " + e.ELEMENT, e);
                            dfeAlias = "'ERROR'";
                        }
                    } else {
                        String viewName = df2.getParentDisplay().getElementName() + ".";
                        viewName += dfe.getXdatType() + "_" + dfe.getSchemaElementName() + "." + dfe.getXdatType();
                        dfeAlias = qo.getFieldAlias(viewName);
                    }
                }
            }

            if (content == null) {
                content = dfeAlias;
            } else {
                content = StringUtils.replace(content, "@" + dfe.getName(), dfeAlias);
            }
        }

        return content;
    }

    public XFTTableI execute(String login) throws Exception {
        return this.execute(null, login);
    }

    public org.nrg.xft.search.CriteriaCollection getEmptyCollection(String andOr) {
        return new org.nrg.xft.search.CriteriaCollection(andOr);
    }

    public XFTTableI getPage(int pageNumber, PresentationA p, String login) throws Exception {
        if (newQuery || !pagingOn) {
            currentPageNum = pageNumber;
            lastPresenter = p;
            return execute(lastPresenter, login);
        } else {

            currentPageNum = pageNumber;
            int offset = currentPageNum * rowsPerPage;

            MaterializedView.createView(this.getResultsTableName(), query, user, MaterializedView.DEFAULT_MATERIALIZED_VIEW_SERVICE_CODE);
            table = MaterializedView.retrieveView(this.getResultsTableName(), user, offset, rowsPerPage);
            lastPresenter = p;
            if (lastPresenter != null) {
                lastPresenter.setRootElement(rootElement);
                lastPresenter.setDisplay(display);
                lastPresenter.setAdditionalViews(additionalViews);
                presentedTable = lastPresenter.formatTable(table, this, this.allowDiffs);
            } else {
                presentedTable = table;
            }
            return presentedTable;
        }
    }

    private void calculatePages() {
        if (this.numRows > this.rowsPerPage) {
            pages = (numRows / rowsPerPage) + 1;
        } else if (numRows == 0) {
            pages = 0;
        } else {
            pages = 1;
        }
    }

    /**
     * Gets any additional views for the display search.
     * @return Any additional views for the display search.
     */
    public List<String[]> getAdditionalViews() {
        return new ArrayList<>(additionalViews);
    }

    /**
     * Gets any additional criteria for the display search.
     * @return Any additional criteria for the display search.
     */
    public ArrayList getCriteria() {
        return criteria.toArrayList();
    }

    /**
     * Gets the display for the display search.
     * @return The display for the display search.
     */
    public String getDisplay() {
        return display;
    }

    /**
     * @return The presented table.
     */
    public XFTTableI getPresentedTable() {
        return presentedTable;
    }


    /**
     * @return The {@link SchemaElement schema element} for the display search.
     */
    public SchemaElement getRootElement() {
        return rootElement;
    }

    /**
     * @return The sort-by column for the display search.
     */
    public String getSortBy() {
        return sortBy;
    }

    /**
     * @return The sort order for the display search.
     */
    public String getSortOrder() {
        return sortOrder;
    }

    /**
     * Sets additional views to set for the display search.
     * @param additionalViews    The additional views to set for the display search.
     */
    public void setAdditionalViews(final ArrayList additionalViews) {
        this.additionalViews.clear();
        this.additionalViews.addAll(additionalViews);
        newQuery = true;
    }

    public void addAdditionalView(String element, String display) {
        additionalViews.add(new String[]{element, display});
        newQuery = true;
    }

    /**
     * @param list A list of Criteria and/or CriteriaCollections
     */
    public void setCriteria(ArrayList list) {
        criteria.addCriteria(list);
        newQuery = true;
    }

    public void addCriteria(SQLClause c) {
        criteria.add(c);
        newQuery = true;
    }

    public void addCriteria(String element, String displayField, String comparisonType, Object value) throws Exception {
        addCriteria(element, displayField, comparisonType, value, false);
    }

    public void addCriteria(String element, String displayField, String comparisonType, Object value, boolean overrideDataTypeFormatting) throws Exception {
        DisplayCriteria dc = new DisplayCriteria();
        dc.setSearchFieldByDisplayField(element, displayField);
        dc.setComparisonType(comparisonType);
        dc.setValue(value, true);
        dc.setOverrideDataFormatting(overrideDataTypeFormatting);
        addCriteria(dc);
    }

    public void addCriteria(String xmlPath, String comparisonType, Object value) throws Exception {
        ElementCriteria ec = new ElementCriteria();
        ec.setFieldWXMLPath(xmlPath);
        SchemaElement se = SchemaElement.GetElement(ec.getElementName());
        DisplayField df = se.getDisplayFieldForXMLPath(xmlPath);
        if (df == null) {
            ec.setComparison_type(comparisonType);
            ec.setValue(value);
            addCriteria(ec);
        } else {
            addCriteria(se.getFullXMLName(), df.getId(), comparisonType, value);
        }
    }

    /**
     * Sets the display for the search.
     * @param display    The display to set for the search.
     */
    public void setDisplay(final String display) {
        this.display = display;
        newQuery = true;
    }

    /**
     * Sets the root element for the search.
     * @param element    The root element to set for the search.
     */
    public void setRootElement(SchemaElement element) {
        rootElement = element;
        newQuery = true;
    }

    public GenericWrapperElement getElement() {
        return this.rootElement.getGenericXFTElement();
    }

    public void setElement(GenericWrapperElement element) {
        SchemaElement se = new SchemaElement(element);
        setRootElement(se);
    }

    public void setRootElement(String elementName) throws XFTInitException, ElementNotFoundException {
        rootElement = SchemaElement.GetElement(elementName);
        newQuery = true;
    }

    /**
     * Sets the sort-by column for the search
     * @param sortBy    The sort-by column to set for the search
     */
    public void setSortBy(final String sortBy) {
        this.sortBy = sortBy;
        newQuery = true;
    }

    /**
     * Sets the sort order for the search.
     * @param sortOrder    The sort order to set for the search.
     */
    public void setSortOrder(final String sortOrder) {
        this.sortOrder = sortOrder;
        newQuery = true;
    }

    public boolean isSuperSearch() {
        return this.additionalViews.size() > 0;
    }

    /**
     * Returns the current page number.
     * @return The current page number.
     */
    public int getCurrentPageNum() {
        return currentPageNum;
    }

    /**
     * Indicates whether paging is on for the search.
     * @return True if paging is on, false otherwise.
     */
    @SuppressWarnings("unused")
    public boolean isPagingOn() {
        return pagingOn;
    }

    /**
     * Indicates the number of rows included per page of search results.
     * @return The number of rows per page.
     */
    public int getRowsPerPage() {
        return rowsPerPage;
    }

    /**
     * Indicates whether paging should be on.
     * @param pagingOn    Whether paging should be turned on.
     */
    public void setPagingOn(boolean pagingOn) {
        if (this.pagingOn != pagingOn) {
            newQuery = true;
            this.pagingOn = pagingOn;
        }
    }

    /**
     * Indicates the number of rows per page to set for the search.
     * @param rowsPerPage    The rows per page to set for the search.
     */
    public void setRowsPerPage(final int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
        currentPageNum = 0;
        calculatePages();
    }

    /**
     * @return The number of pages for the current search
     */
    public int getPages() {
        return pages;
    }

    public static ArrayList SearchForItems(SchemaElementI e, org.nrg.xft.search.CriteriaCollection criteria) throws Exception {
        ItemSearch search = new ItemSearch(null, e.getGenericXFTElement(), criteria);
        return search.exec(true).getItems();
    }

    /**
     * Returns the user owning the search.
     * @return The user owning the search.
     */
    public UserI getUser() {
        return user;
    }

    /**
     * Sets the user to be search owner.
     * @param user    The user to set as search owner.
     */
    public void setUser(UserI user) {
        this.user = user;
    }

    /**
     * @return The number of rows in the search.
     */
    public int getNumRows() {
        return numRows;
    }

    /**
     * @return Returns the criteriaCollection.
     */
    public org.nrg.xft.search.CriteriaCollection getCriteriaCollection() {
        return criteria;
    }

    /**
     * @param criteriaCollection The criteriaCollection to set.
     */
    public void setCriteriaCollection(org.nrg.xft.search.CriteriaCollection criteriaCollection) {
        this.criteria = criteriaCollection;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DisplaySearch (").append(this.getElement().getFullXMLName());
        try {
            sb.append(") Criteria:").append(this.criteria.getSQLClause(null));
        } catch (Exception e) {
            logger.error("", e);
        }
        return sb.toString();
    }

    /**
     * @return Returns the customSortBy.
     */
    public String getCustomSortBy() {
        return customSortBy;
    }

    /**
     * @param customSortBy The customSortBy to set.
     */
    @SuppressWarnings("unused")
    public void setCustomSortBy(String customSortBy) {
        this.customSortBy = customSortBy;
    }

    /**
     * @return Returns the inClauses.
     */
    public Hashtable<String, String> getInClauses() {
        return inClauses;
    }

    public void addInClause(String fields, String commaDelimitedValues) {
        commaDelimitedValues = StringUtils.replace(commaDelimitedValues, "\r\n,", ",");
        commaDelimitedValues = StringUtils.replace(commaDelimitedValues, ",\r\n", ",");
        commaDelimitedValues = StringUtils.replace(commaDelimitedValues, "\"", "");
        commaDelimitedValues = StringUtils.replace(commaDelimitedValues, "'", "");
        commaDelimitedValues = StringUtils.replace(commaDelimitedValues, "\r\n", ",");

        StringBuilder sb = new StringBuilder();
        int counter = 0;
        for (final String s : XftStringUtils.CommaDelimitedStringToArrayList(commaDelimitedValues)) {
            if (!s.trim().contains(" ")) {
                if (counter++ != 0) {
                    sb.append(",\"").append(s.trim()).append("\"");
                } else {
                    sb.append("\"").append(s.trim()).append("\"");
                }
            } else {
                for (final String s2 : XftStringUtils.DelimitedStringToArrayList(s.trim(), " ")) {
                    if (counter++ != 0) {
                        sb.append(",\"").append(s2.trim()).append("\"");
                    } else {
                        sb.append("\"").append(s2.trim()).append("\"");
                    }
                }
            }
        }

        commaDelimitedValues = "'{" + sb.toString() + "}'";
        inClauses.put(fields, commaDelimitedValues);
    }


    /**
     * @return Returns the fields.
     */
    public DisplayFieldWrapperCollection getFields() {
        return fields;
    }

    /**
     * @param fields The fields to set.
     */
    public void setFields(DisplayFieldWrapperCollection fields) {
        this.fields = fields;
    }

    public void addDisplayField(DisplayField df) {
        this.getFields().addDisplayField(df);
    }

    @SuppressWarnings("unused")
    public void addDisplayFields(Collection coll) {
        this.getFields().addDisplayFields(coll);
    }

    public void addDisplayField(String elementName, String fieldID) {
        try {
            SchemaElement se = SchemaElement.GetElement(elementName);
            try {
                DisplayField df = se.getDisplayField(fieldID);
                this.getFields().addDisplayField(df);
            } catch (DisplayFieldNotFoundException e) {
                try {
                    DisplayField df = se.getDisplayFieldForXMLPath(fieldID);
                    if (df == null) {
                        logger.error("", e);
                    } else {
                        this.getFields().addDisplayField(df);
                    }
                } catch (Exception e1) {
                    logger.error("", e);
                }
            }
        } catch (XFTInitException e) {
            logger.error("XFT initialization failed", e);
        } catch (ElementNotFoundException e) {
            logger.error("XFT element not found: " + e.ELEMENT, e);
        }
    }

    @SuppressWarnings("unused")
    public void addDisplayField(String elementName, String fieldID, String header) {
        try {
            SchemaElement se = SchemaElement.GetElement(elementName);
            try {
                DisplayField df = se.getDisplayField(fieldID);
                this.getFields().addDisplayField(df, header, null);
            } catch (DisplayFieldNotFoundException e) {
                try {
                    DisplayField df = se.getDisplayFieldForXMLPath(fieldID);
                    if (df == null) {
                        logger.error("", e);
                    } else {
                        this.getFields().addDisplayField(df, header, null);
                    }
                } catch (Exception e1) {
                    logger.error("", e);
                }
            }
        } catch (XFTInitException e) {
            logger.error("XFT initialization failed", e);
        } catch (ElementNotFoundException e) {
            logger.error("XFT element not found: " + e.ELEMENT, e);
        }
    }

    public void addDisplayField(String elementName, String fieldID, Object value) {
        try {
            SchemaElement se = SchemaElement.GetElement(elementName);
            try {
                DisplayField df = se.getDisplayField(fieldID);
                this.getFields().addDisplayField(df, value);
            } catch (DisplayFieldNotFoundException e) {
                try {
                    DisplayField df = se.getDisplayFieldForXMLPath(fieldID);
                    if (df == null) {
                        logger.error("", e);
                    } else {
                        this.getFields().addDisplayField(df, value);
                    }
                } catch (Exception e1) {
                    logger.error("", e);
                }
            }
        } catch (XFTInitException e) {
            logger.error("XFT initialization failed", e);
        } catch (ElementNotFoundException e) {
            logger.error("XFT element not found: " + e.ELEMENT, e);
        }
    }

    public void addDisplayField(String elementName, String fieldID, String header, Object value) {
        try {
            SchemaElement se = SchemaElement.GetElement(elementName);
            try {
                DisplayField df = se.getDisplayField(fieldID);
                this.getFields().addDisplayField(df, header, value);
            } catch (DisplayFieldNotFoundException e) {
                try {
                    DisplayField df = se.getDisplayFieldForXMLPath(fieldID);
                    if (df == null) {
                        logger.error("", e);
                    } else {
                        this.getFields().addDisplayField(df, header, value);
                    }
                } catch (Exception e1) {
                    logger.error("", e);
                }
            }
        } catch (XFTInitException e) {
            logger.error("XFT initialization failed", e);
        } catch (ElementNotFoundException e) {
            logger.error("XFT element not found: " + e.ELEMENT, e);
        }
    }

    public void addDisplayField(String elementName, String fieldID, String header, Object value, Boolean visible) {
        try {
            SchemaElement se = SchemaElement.GetElement(elementName);
            try {
                DisplayField df = se.getDisplayField(fieldID);
                this.getFields().addDisplayField(df, header, value, visible);
            } catch (DisplayFieldNotFoundException e) {
                try {
                    DisplayField df;
                    if (!fieldID.contains(".") && !fieldID.contains("/")) {
                        df = se.getDisplayFieldForXMLPath(this.getRootElement().getFullXMLName() + "/" + fieldID);
                    } else {
                        df = se.getDisplayFieldForXMLPath(fieldID);
                    }
                    if (df == null) {
                        logger.error("", e);
                    } else {
                        this.getFields().addDisplayField(df, header, value, visible);
                    }
                } catch (Exception e1) {
                    logger.error(e.getMessage());
                }
            }
        } catch (XFTInitException e) {
            logger.error("XFT initialization failed", e);
        } catch (ElementNotFoundException e) {
            logger.error("XFT element not found: " + e.ELEMENT, e);
        }
    }

    /**
     * @return Returns the useVersions.
     */
    public boolean useVersions() {
        return useVersions;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the level.
     */
    public String getLevel() {
        return level;
    }

    /**
     * @param level The level to set.
     */
    public void setLevel(String level) {
        this.level = level;
    }


    /**
     * @return Returns the title.
     */
    public String getTitle() {
        if (title != null && !title.equals("")) {
            return title;
        } else {
            if (this.isSuperSearch()) {
                return "Super Search";
            } else {
                return getRootElement().getDisplay().getDescription();
            }
        }
    }

    /**
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @SuppressWarnings("unused")
    public Object getWebFormValue(String formFieldName) {
        if (this.webFormValues.get(formFieldName.toLowerCase()) == null) {
            return "";
        } else {
            return webFormValues.get(formFieldName.toLowerCase());
        }
    }

    public void setWebFormValue(String formFieldName, Object value) {
        webFormValues.put(formFieldName.toLowerCase(), value);
    }

    public void resetWebFormValues() {
        webFormValues = new Hashtable();
    }

    public Hashtable getWebFormValues() {
        return webFormValues;
    }

    public ArrayList getAdditialViewArrayLists() {
        ArrayList al = new ArrayList();
        for (int i = 0; i < this.getAdditionalViews().size(); i++) {
            String[] view = this.getAdditionalViews().get(i);
            ArrayList sub = new ArrayList();
            sub.add(view[0]);
            sub.add(view[1]);
            al.add(sub);
        }
        al.trimToSize();
        return al;
    }


    /**
     * Resets the results table name.
     */
    public void resetResultsTableName() {
        resultsTableName = null;
    }

    /**
     * @return Returns the resultsTableName.
     */
    private String getResultsTableName() {
        if (resultsTableName == null) {
            if (this.getUser() == null) {
                resultsTableName = "xs_" + Calendar.getInstance().getTimeInMillis();
            } else {
                resultsTableName = "xs_" + getUser().getUsername() + "_" + Calendar.getInstance().getTimeInMillis();
            }
        }
        return resultsTableName.toLowerCase();
    }

    /**
     * @return Returns the customSearch.
     */
    public boolean isStoredSearch() {
        return isStoredSearch;
    }

    /**
     * @param isStoredSearch Indicates whether this is a stored search.
     */
    public void setStoredSearch(boolean isStoredSearch) {
        this.isStoredSearch = isStoredSearch;
    }

    /**
     * @return Returns the storedSearch.
     */
    public org.nrg.xdat.om.XdatStoredSearch getStoredSearch() {
        return storedSearch;
    }

    /**
     * @param storedSearch The storedSearch to set.
     */
    public void setStoredSearch(
            org.nrg.xdat.om.XdatStoredSearch storedSearch) {
        this.storedSearch = storedSearch;
        this.setStoredSearch(true);
    }

    /**
     * @return Returns the allowDiffs.
     */
    @SuppressWarnings("unused")
    public boolean isAllowDiffs() {
        return allowDiffs;
    }

    /**
     * @param allowDiffs The allowDiffs to set.
     */
    public void setAllowDiffs(boolean allowDiffs) {
        this.allowDiffs = allowDiffs;
    }

    /**
     * Converts the display search object to an {@link XdatStoredSearch} object. This method calls the
     * {@link #convertToStoredSearch(String, String)} version of this method, passing in a default value for the
     * query-mode parameter.
     *
     * @param identifier The identifier for the stored search.
     * @return The resulting stored search object.
     */
    public XdatStoredSearch convertToStoredSearch(final String identifier) {
        return convertToStoredSearch(identifier, null);
    }

    /**
     * Converts the display search object to an {@link XdatStoredSearch} object.
     *
     * @param identifier The identifier for the stored search.
     * @param queryMode  The query mode. This can be null, "criteria", or "byId".
     * @return The resulting stored search object.
     */
    @SuppressWarnings("unused")
    public XdatStoredSearch convertToStoredSearch(final String identifier, final String queryMode) {
        XdatStoredSearch xss = null;
        try {
            xss = new XdatStoredSearch(getUser());

            xss.setRootElementName(this.getRootElement().getFullXMLName());
            String sortBy = this.getSortBy();

            if (sortBy.contains(".")) {
                String elementName = sortBy.substring(0, sortBy.indexOf("."));
                String fieldId = sortBy.substring(sortBy.indexOf(".") + 1);
                xss.setSortBy_elementName(elementName);
                xss.setSortBy_fieldId(fieldId);
            } else if (!sortBy.equals("")) {
                xss.setSortBy_elementName(xss.getRootElementName());
                xss.setSortBy_fieldId(sortBy);
            }

            ElementDisplay ed = DisplayManager.GetElementDisplay(xss.getRootElementName());
            if (this.getFields().size() > 0) {
                int sequence = 0;

                for (DisplayFieldReferenceI ref : this.getFields().getSortedVisibleFields()) {
                    XdatSearchField xsf = new XdatSearchField();
                    if (ref.getElementName() != null) {
                        xsf.setElementName(ref.getElementName());
                    } else {
                        xsf.setElementName(this.getRootElement().getFullXMLName());
                    }
                    if (!ref.getId().contains(".")) {
                        String f = ref.getId();
                        if (!f.contains("=")) {
                            xsf.setFieldId(f);
                        } else {
                            xsf.setFieldId(f.substring(0, f.indexOf("=")));
                            xsf.setValue(f.substring(f.indexOf("=") + 1));
                        }
                    } else {
                        String f = ref.getId().substring(ref.getId().indexOf(".") + 1);
                        if (!f.contains("=")) {
                            xsf.setFieldId(f);
                        } else {
                            xsf.setFieldId(f.substring(0, f.indexOf("=")));
                            xsf.setValue(f.substring(f.indexOf("=") + 1));
                        }
                    }
                    if (ref.getHeader() == null || ref.getHeader().equals(""))
                        xsf.setHeader("  ");
                    else
                        xsf.setHeader(ref.getHeader());
                    xsf.setType(ref.getDisplayField().getDataType());
                    xsf.setSequence(sequence++);
                    if (ref.getValue() != null && !ref.getValue().equals(""))
                        xsf.setValue(ref.getValue().toString());
                    if (!ref.isVisible()) {
                        xsf.setVisible(false);
                    }
                    xss.setSearchField(xsf);
                }
            } else {
                DisplayVersion rootdv = ed.getVersion(this.getDisplay(), "listing");

                ArrayList<DisplayVersion> displayVersions = new ArrayList<>();
                displayVersions.add(rootdv);

                if (this.getAdditionalViews() != null && this.getAdditionalViews().size() > 0) {
                    for (Object o : this.getAdditionalViews()) {
                        String[] key = (String[]) o;
                        String elementName = key[0];
                        String version = key[1];
                        SchemaElementI foreign = SchemaElement.GetElement(elementName);

                        ElementDisplay foreignEd = DisplayManager.GetElementDisplay(foreign.getFullXMLName());
                        DisplayVersion foreignDV = foreignEd.getVersion(version, "default");
                        displayVersions.add(foreignDV);
                    }
                }

                int sequence = 0;

                for (DisplayVersion dv : displayVersions) {
                    Iterator iter = dv.getDisplayFieldRefIterator();
                    while (iter.hasNext()) {
                        DisplayFieldRef ref = (DisplayFieldRef) iter.next();
                        try {
                            if (ref.getDisplayField() != null) {
                                XdatSearchField xsf = new XdatSearchField();
                                if (ref.getElementName() != null) {
                                    xsf.setElementName(ref.getElementName());
                                } else {
                                    xsf.setElementName(dv.getParentElementDisplay().getElementName());
                                }
                                xsf.setFieldId(ref.getId());
                                if (ref.getHeader() == null || ref.getHeader().equals(""))
                                    xsf.setHeader("  ");
                                else
                                    xsf.setHeader(ref.getHeader());
                                xsf.setType(ref.getDisplayField().getDataType());
                                xsf.setSequence(sequence++);
                                if (ref.getValue() != null && !ref.getValue().equals(""))
                                    xsf.setValue(ref.getValue().toString());
                                if (!ref.isVisible()) {
                                    xsf.setVisible(false);
                                }
                                xss.setSearchField(xsf);
                            }
                        } catch (DisplayFieldNotFoundException e) {
                            logger.error("", e);
                        }
                    }
                }
            }

            final boolean hasCriteria = getCriteria().size() > 0;
            final boolean hasInclauses = getInClauses().size() > 0;
            final int resolvedQueryMode = resolveQueryMode(queryMode);
            if (hasCriteria && hasInclauses && resolvedQueryMode == QUERY_MODE_VAL_NONE) {
                throw new InvalidSearchException("The specified query contains both query criteria and specific IDs without specifying a query mode. Queries by criteria or IDs are exclusive of each other. You should either use criteria or IDs exclusively or indicate a query mode to specify which should be used for the query.");
            }
            final boolean useCriteria = resolvedQueryMode == QUERY_MODE_VAL_CRITERIA || hasCriteria;
            final boolean useInclauses = resolvedQueryMode == QUERY_MODE_VAL_BYID || hasInclauses;
            if (useCriteria) {
                XdatCriteriaSet set = new XdatCriteriaSet();
                for (int i = 0; i < this.getCriteria().size(); i++) {
                    set.setMethod("AND");
                    for (Object o : this.getCriteria()) {
                        SQLClause c = (SQLClause) o;
                        if (c instanceof org.nrg.xft.search.CriteriaCollection) {
                            XdatCriteriaSet subset = new XdatCriteriaSet();
                            subset.populateCriteria((CriteriaCollection) c);

                            if (subset.size() > 0) {
                                set.setChildSet(subset);
                            }
                        } else {
                            XdatCriteria criteria = new XdatCriteria();
                            criteria.populateCriteria(c);

                            set.setCriteria(criteria);
                        }
                    }
                }
                if (set.size() > 0) {
                    xss.setSearchWhere(set);
                }
            } else if (useInclauses) {
                XdatCriteriaSet set = new XdatCriteriaSet();
                set.setMethod("OR");
                for (Map.Entry<String, String> entry : this.getInClauses().entrySet()) {
                    XdatCriteria crit = new XdatCriteria();
                    crit.setSchemaField(entry.getKey());
                    crit.setValue(entry.getValue());
                    crit.setComparisonType("IN");
                    set.setCriteria(crit);
                }
                xss.setSearchWhere(set);
            }
        } catch (Exception e) {
            if (e instanceof InvalidSearchException) {
                throw (InvalidSearchException) e;
            }
            logger.error("", e);
        }

        return xss;
    }

    /**
     * Returns the query mode based on the value of the submitted parameter. Returns the following values if
     * <b>queryMode</b> equals:
     * <p/>
     * <ul>
     * <li>{@link #QUERY_MODE_CRITERIA} returns {@link #QUERY_MODE_VAL_CRITERIA}</li>
     * <li>{@link #QUERY_MODE_BYID} returns {@link #QUERY_MODE_VAL_BYID}</li>
     * <li>Any other value, including blank or null, returns {@link #QUERY_MODE_VAL_NONE}</li>
     * </ul>
     *
     * @param queryMode The submitted query mode value.
     * @return {@link #QUERY_MODE_VAL_CRITERIA}, {@link #QUERY_MODE_VAL_BYID}, or {@link #QUERY_MODE_VAL_NONE} based on
     * the query mode parameter.
     */
    private int resolveQueryMode(final String queryMode) {
        if (StringUtils.equalsIgnoreCase(queryMode, QUERY_MODE_CRITERIA)) {
            return QUERY_MODE_VAL_CRITERIA;
        }
        if (StringUtils.equalsIgnoreCase(queryMode, QUERY_MODE_BYID)) {
            return QUERY_MODE_VAL_BYID;
        }
        return QUERY_MODE_VAL_NONE;
    }


    public List<DisplayFieldReferenceI> getAllFields(String versionExtension) throws ElementNotFoundException, XFTInitException {
        final ElementDisplay ed = DisplayManager.GetElementDisplay(getRootElement().getFullXMLName());
        final DisplayVersion dv;
        List<DisplayFieldReferenceI> allfields;
        if (this.useVersions()) {
            if (!versionExtension.equalsIgnoreCase("")) {
                dv = ed.getVersion(getDisplay() + "_" + versionExtension, getDisplay());
            } else {
                dv = ed.getVersion(getDisplay(), "default");
            }
            allfields = dv.getAllFields();

            if (getAdditionalViews() != null && getAdditionalViews().size() > 0) {
                for (final String[] key : getAdditionalViews()) {
                    String elementName = key[0];
                    String version = key[1];
                    GenericWrapperElement foreign = GenericWrapperElement.GetElement(elementName);

                    ElementDisplay foreignEd = DisplayManager.GetElementDisplay(foreign.getType().getFullForeignType());
                    DisplayVersion foreignDV;
                    if (!versionExtension.equalsIgnoreCase("")) {
                        foreignDV = foreignEd.getVersion(version + "_" + versionExtension, version);
                    } else {
                        foreignDV = foreignEd.getVersion(version, "default");
                    }

                    allfields.addAll(foreignDV.getAllFields());
                }
            }
        } else {
            allfields = this.getFields().getSortedFields();
        }

        return allfields;
    }

    public ArrayList<DisplayFieldReferenceI> getVisibleFields(String versionExtension) throws ElementNotFoundException, XFTInitException {
        ElementDisplay ed = DisplayManager.GetElementDisplay(getRootElement().getFullXMLName());
        DisplayVersion dv;
        ArrayList<DisplayFieldReferenceI> visibleFields;
        if (this.useVersions()) {
            if (!versionExtension.equalsIgnoreCase("")) {
                dv = ed.getVersion(getDisplay() + "_" + versionExtension, getDisplay());
            } else {
                dv = ed.getVersion(getDisplay(), "default");
            }
            visibleFields = dv.getVisibleFields();

            if (getAdditionalViews() != null && getAdditionalViews().size() > 0) {
                for (Object o : getAdditionalViews()) {
                    String[] key = (String[]) o;
                    String elementName = key[0];
                    String version = key[1];
                    GenericWrapperElement foreign = GenericWrapperElement.GetElement(elementName);

                    ElementDisplay foreignEd = DisplayManager.GetElementDisplay(foreign.getType().getFullForeignType());
                    DisplayVersion foreignDV;
                    if (!versionExtension.equalsIgnoreCase("")) {
                        foreignDV = foreignEd.getVersion(version + "_" + versionExtension, version);
                    } else {
                        foreignDV = foreignEd.getVersion(version, "default");
                    }

                    visibleFields.addAll(foreignDV.getVisibleFields());
                }
            }
        } else {
            visibleFields = this.getFields().getSortedVisibleFields();
        }

        return visibleFields;
    }
}

