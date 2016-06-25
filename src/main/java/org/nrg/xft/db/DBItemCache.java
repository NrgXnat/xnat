/*
 * org.nrg.xft.db.DBItemCache
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */

package org.nrg.xft.db;

import org.nrg.framework.utilities.Reflection;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tim
 */
public class DBItemCache {
    private static final Logger         logger      = LoggerFactory.getLogger(DBItemCache.class);
    private              ItemCollection saved       = new ItemCollection();
    private              ItemCollection removed     = new ItemCollection();
    private              ItemCollection preexisting = new ItemCollection();
    private              ItemCollection dbTrigger   = new ItemCollection();
    private              ItemCollection modified    = new ItemCollection();

    private ArrayList<String> sql = new ArrayList<>();

    private Date   mod_time;
    private Object change_id;
    private String comment;
    private Object event_id;

    private static String dbName = null, table = null, pk = null, sequence = null;

    private final UserI user;

    /**
     *
     */
    public DBItemCache(UserI user, EventMetaI event) throws Exception {
        this.mod_time = EventUtils.getEventDate(event, false);

        this.comment = (event == null) ? null : event.getMessage();

        this.event_id = (event == null) ? null : event.getEventId();

        this.user = user;
    }

    public Date getModTime() {
        return mod_time;
    }

    public Object getChangeId() throws Exception {
        if (change_id == null) {
            change_id = getNextChangeID();
        }
        return change_id;
    }

    private synchronized static Object getNextChangeID() throws Exception {
        if (dbName == null) {
            try {
                GenericWrapperElement element = GenericWrapperElement.GetElement("xdat:change_info");
                dbName = element.getDbName();
                table = element.getSQLName();
                pk = "xdat_change_info_id";
                sequence = element.getSequenceName();
            } catch (XFTInitException | ElementNotFoundException e) {
                logger.error("", e);
            }
        }
        return PoolDBUtils.GetNextID(dbName, table, pk, sequence);
    }

    public void addStatement(String query) {
        if (!query.endsWith(";")) {
            query += ";";
        }

        sql.add("\n" + query);
    }

    public ArrayList<String> getStatements() {
        return sql;
    }

    public void finalize() throws Exception {
        try {
            super.finalize();
        } catch (Throwable throwable) {
            throw new Exception(throwable);
        }
        XFTItem cache_info = XFTItem.NewItem("xdat:change_info", user);
        cache_info.setDirectProperty("change_date", getModTime());
        if (user != null) {
            cache_info.setDirectProperty("change_user", user.getID());
        }
        cache_info.setDirectProperty("xdat_change_info_id", this.getChangeId());

        if (this.comment != null) {
            cache_info.setDirectProperty("comment", this.comment);
        }

        if (this.event_id != null) {
            cache_info.setDirectProperty("event_id", this.event_id);
        }

        DBAction.InsertItem(cache_info, null, this, false);
    }

    public String getSQL() {
        final StringBuilder sb = new StringBuilder();
        for (final String aSql : sql) {
            sb.append(aSql);
        }
        return sb.toString();
    }

    public void reset() throws Exception {
        this.sql = new ArrayList<>();

        saved.clear();
        removed.clear();
        preexisting.clear();
    }

    public String toString() {
        return this.sql.toString();
    }

    /**
     * @return Returns the preexisting.
     */
    public ItemCollection getPreexisting() {
        return preexisting;
    }

    /**
     * @return Returns the removed.
     */
    public ItemCollection getRemoved() {
        return removed;
    }

    /**
     * @param removed The removed to set.
     */
    public void setRemoved(ItemCollection removed) {
        this.removed = removed;
    }

    /**
     * @return Returns the saved.
     */
    public ItemCollection getSaved() {
        return saved;
    }

    /**
     * @return the modified
     */
    public ItemCollection getDBTriggers() {
        return dbTrigger;
    }

    public ItemCollection getModified() {
        return modified;
    }

    public void setModified(ItemCollection modified) {
        this.modified = modified;
    }

    public void handlePostModificationAction(XFTItem item, String action) {
        try {
            final String element = item.getGenericSchemaElement().getJAVAName();
            final String packageName = "org.nrg.xnat.extensions.db." + action + "." + element;
            if (Reflection.getClassesForPackage(packageName).size() > 0) {
                Map<String, Object> params = new HashMap<>();
                params.put("transaction", this);
                params.put("item", item);

                Reflection.injectDynamicImplementations(packageName, params);

            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }
}
