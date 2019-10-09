/*
 * core: org.nrg.xft.db.DBAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.db;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.*;
import org.nrg.xft.references.*;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.SQLClause;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.SecurityManagerI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.XftStringUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes", "UnusedReturnValue"})
@Slf4j
public class DBAction {
    /**
     * This method is used to insert/update an item into the database.
     *
     * First, if the item has an extended field, then the extended field is populated with
     * the extension name.  Next, it stores the single-reference items.  The pk values of those items
     * are then copied into this item as foreign-keys.  If this item is a single column item then its pk
     * is set manually using the nextVal().  If the item has its primary key, then a select is performed
     * to verify if the record already exists based on its pks.  If so, then it is an UPDATE statement.
     * If the record does not exist, then an INSERT is performed.  If the item did not have a pk value
     * set, then a select is performed to see if there are any rows in the table that have all of the
     * item's field values.  If one is found, then it is assumed that this item is a duplicate of that
     * row.  Otherwise, a new row is INSERTed.
     *
     * @param item The item to be stored.
     *             git
     * @return updated XFTItem
     */
    public static boolean StoreItem(XFTItem item, UserI user, boolean checkForDuplicates, boolean quarantine, boolean overrideQuarantine, boolean allowItemOverwrite, SecurityManagerI securityManager, EventMetaI c) throws Exception {
        long        localStartTime = Calendar.getInstance().getTimeInMillis();
        DBItemCache cache          = new DBItemCache(user, c);
        item = StoreItem(item, user, checkForDuplicates, new ArrayList(), quarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);

        log.debug("prepare-sql: {} ms", Calendar.getInstance().getTimeInMillis() - localStartTime);
        localStartTime = Calendar.getInstance().getTimeInMillis();

        if (!cache.getSQL().equals("") && !cache.getSQL().equals("[]")) {
            Quarantine(item, user, quarantine, overrideQuarantine, cache);

            log.debug("quarantine-sql: {} ms", Calendar.getInstance().getTimeInMillis() - localStartTime);
            localStartTime = Calendar.getInstance().getTimeInMillis();

            PoolDBUtils con;
            String      username     = null;
            Integer     xdat_user_id = null;
            if (user != null) {
                username = user.getUsername();
                xdat_user_id = user.getID();
            }
            if (!cache.getDBTriggers().contains(item, false)) {
                cache.getDBTriggers().add(item);
            }
            if (cache.getRemoved().size() > 0) {
                log.debug("***** {} REMOVED ITEMS *******", cache.getRemoved().size());
                PerformUpdateTriggers(cache, username, xdat_user_id, false);
            }
            log.debug("pre-triggers: {} ms", Calendar.getInstance().getTimeInMillis() - localStartTime);
            localStartTime = Calendar.getInstance().getTimeInMillis();
            con = new PoolDBUtils();
            con.sendBatch(cache, item.getDBName(), username);
            log.debug("Item modifications stored. {} modified elements. {} SQL statements.", cache.getDBTriggers().size(), cache.getStatements().size());
            log.debug("store: {} ms", Calendar.getInstance().getTimeInMillis() - localStartTime);
            localStartTime = Calendar.getInstance().getTimeInMillis();
            PerformUpdateTriggers(cache, username, xdat_user_id, false);
            log.debug("post-triggers: {} ms", Calendar.getInstance().getTimeInMillis() - localStartTime);
            log.debug("Total: {} ms", Calendar.getInstance().getTimeInMillis() - localStartTime);
            return true;
        } else {
            log.info("Pre-existing item found without modifications");
            return false;
        }

    }

    public static void executeCache(final DBItemCache cache, final UserI user, final String db) throws Exception {
        PoolDBUtils con;
        String      username     = null;
        Integer     xdat_user_id = null;
        if (user != null) {
            username = user.getUsername();
            xdat_user_id = user.getID();
        }
        if (cache.getRemoved().size() > 0) {
            PerformUpdateTriggers(cache, username, xdat_user_id, false);
        }
        con = new PoolDBUtils();
        con.sendBatch(cache, db, username);
        PerformUpdateTriggers(cache, username, xdat_user_id, false);
    }

    /**
     * This method is used to insert/update an item into the database.
     *
     * First, if the item has an extended field, then the extended field is populated with
     * the extension name.  Next, it stores the single-reference items.  The pk values of those items
     * are then copied into this item as foreign-keys.  If this item is a single column item then its pk
     * is set manually using the nextVal().  If the item has its primary key, then a select is performed
     * to verify if the record already exists based on its pks.  If so, then it is an UPDATE statement.
     * If the record does not exist, then an INSERT is performed.  If the item did not have a pk value
     * set, then a select is performed to see if there are any rows in the table that have all of the
     * item's field values.  If one is found, then it is assumed that this item is a duplicate of that
     * row.  Otherwise, a new row is INSERTed.
     *
     * @param item               The item to store.
     * @param user               The user performing the operation.
     * @param checkForDuplicates Whether to check for duplicates prior to storing the item.
     * @param quarantine         Whether the item should be quarantined after storing.
     * @param overrideQuarantine Whether an existing quarantine should be overridden.
     * @param allowItemOverwrite Whether an existing item should be overwritten.
     * @param securityManager    The security manager for evaluating the operation.
     * @param cache              The database item cache.
     * @return The updated database item cache with the stored item.
     */
    public static DBItemCache StoreItem(XFTItem item, UserI user, boolean checkForDuplicates, boolean quarantine, boolean overrideQuarantine, boolean allowItemOverwrite, SecurityManagerI securityManager, DBItemCache cache) throws Exception {
        StoreItem(item, user, checkForDuplicates, new ArrayList(), quarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);

        return cache;
    }

    /**
     * This method is used to insert/update an item into the database.
     *
     * First, if the item has an extended field, then the extended field is populated with
     * the extension name.  Next, it stores the single-reference items.  The pk values of those items
     * are then copied into this item as foreign-keys.  If this item is a single column item then its pk
     * is set manually using the nextVal().  If the item has its primary key, then a select is performed
     * to verify if the record already exists based on its pks.  If so, then it is an UPDATE statement.
     * If the record does not exist, then an INSERT is performed.  If the item did not have a pk value
     * set, then a select is performed to see if there are any rows in the table that have all of the
     * item's field values.  If one is found, then it is assumed that this item is a duplicate of that
     * row.  Otherwise, a new row is INSERTed.
     *
     * @param item               The item to store.
     * @param user               The user performing the operation.
     * @param checkForDuplicates Whether to check for duplicates prior to storing the item.
     * @param quarantine         Whether the item should be quarantined after storing.
     * @param overrideQuarantine Whether an existing quarantine should be overridden.
     * @param allowItemRemoval   Whether an existing item can be removed.
     * @param securityManager    The security manager for evaluating the operation.
     * @param cache              The database item cache.
     * @param allowFieldMatching Whether field matching should be performed.
     * @return The updated database item cache with the stored item.
     */
    private static XFTItem StoreItem(XFTItem item, UserI user, boolean checkForDuplicates, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval, DBItemCache cache, SecurityManagerI securityManager, boolean allowFieldMatching) throws Exception {
        return StoreItem(item, user, checkForDuplicates, new ArrayList(), quarantine, overrideQuarantine, allowItemRemoval, cache, securityManager, allowFieldMatching);
    }

    /**
     * This method is used to insert/update an item into the database.
     *
     * First, if the item has an extended field, then the extended field is populated with
     * the extension name.  Next, it stores the single-reference items.  The pk values of those items
     * are then copied into this item as foreign-keys.  If this item is a single column item then its pk
     * is set manually using the nextVal().  If the item has its primary key, then a select is performed
     * to verify if the record already exists based on its pks.  If so, then it is an UPDATE statement.
     * If the record does not exist, then an INSERT is performed.  If the item did not have a pk value
     * set, then a select is performed to see if there are any rows in the table that have all of the
     * item's field values.  If one is found, then it is assumed that this item is a duplicate of that
     * row.  Otherwise, a new row is INSERTed.
     *
     * @param item               The item to store.
     * @param user               The user performing the operation.
     * @param checkForDuplicates Whether to check for duplicates prior to storing the item.
     * @param quarantine         Whether the item should be quarantined after storing.
     * @param overrideQuarantine Whether an existing quarantine should be overridden.
     * @param allowItemOverwrite Whether an existing item should be overwritten.
     * @param securityManager    The security manager for evaluating the operation.
     * @param cache              The database item cache.
     *
     * @return The updated database item cache with the stored item.
     */
    @SuppressWarnings("UnusedParameters")
    private static XFTItem StoreItem(XFTItem item, UserI user, boolean checkForDuplicates, ArrayList storedRelationships, boolean quarantine, boolean overrideQuarantine, boolean allowItemOverwrite, DBItemCache cache, SecurityManagerI securityManager, boolean allowFieldMatching) throws Exception {
        boolean isNew = true;
        try {
            String login = null;
            if (user != null) {
                login = user.getUsername();
            }

            if (item.hasExtendedField()) {
                item.setExtenderName();
            }

            if (item.getGenericSchemaElement().isExtension()) {
                item.setExtenderName();
            }
            boolean localQuarantine;
            if (overrideQuarantine) {
                localQuarantine = quarantine;
            } else {
                localQuarantine = item.getGenericSchemaElement().isQuarantine(quarantine);
            }
            boolean hasOneColumnTable = StoreSingleRefs(item, false, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, true);

            if (item.getPossibleFieldNames().size() == 1) {
                String keyName = (String) item.getPossibleFieldNames().get(0)[0];
                if (item.getProperty(keyName) == null) {
                    PoolDBUtils con;
                    try {
                        con = new PoolDBUtils();
                        XFTTable t = con.executeSelectQuery("SELECT nextval('" + item.getGenericSchemaElement().getSQLName() + "_" + keyName + "_seq" + "')", item.getGenericSchemaElement().getDbName(), login);
                        while (t.hasMoreRows()) {
                            t.nextRow();
                            item.setFieldValue(keyName, t.getCellValue("nextval"));
                        }
                    } catch (SQLException e) {
                        log.warn("Error accessing database", e);
                    } catch (Exception e) {
                        log.warn("Unknown exception occurred", e);
                    }
                }
            }

            //Check if the primary key for this item is set.
            boolean hasPK = item.hasPK();

            boolean itemAlreadyStoredInCache = false;

            if (hasPK) {
                //HAS ASSIGNED PK
                ItemCollection al = item.getPkMatches(false);
                if (al.size() > 0) {
                    isNew = false;
                    //ITEM EXISTS
                    XFTItem sub    = (XFTItem) al.get(0);
                    String  output = "Duplicate " + item.getGenericSchemaElement().getFullXMLName() + " Found. (" + sub.getPK() + ")";
                    if (sub.getXSIType().startsWith("xdat:")) {
                        log.debug(output);
                    } else {
                        log.info(output);
                    }

                    if (hasOneColumnTable) {
                        DBAction.ImportNoIdentifierFKs(item, sub);
                    }

                    if (HasNewFields(sub, item, allowItemOverwrite)) {
                        log.debug("OLD: {}\nNEW: {}" + sub.toString(), item.toString());
                        item = UpdateItem(sub, item, user, localQuarantine, overrideQuarantine, cache, allowItemOverwrite);
                    } else {
                        item.importNonItemFields(sub, allowItemOverwrite);
                    }

                    cache.getPreexisting().add(item);

                    if (hasOneColumnTable) {
                        sub.importNonItemFields(item, false);
                        StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, true);

                        if (HasNewFields(sub, item, allowItemOverwrite)) {
                            item = UpdateItem(sub, item, user, localQuarantine, overrideQuarantine, cache, false);

                        }
                    }
                } else {
                    if (item.hasUniques()) {
                        ItemCollection temp = item.getUniqueMatches(false);
                        if (temp.size() > 0) {
                            isNew = false;
                            XFTItem duplicate = (XFTItem) temp.get(0);
                            String  output    = "Duplicate " + item.getGenericSchemaElement().getFullXMLName() + " Found. (" + duplicate.getPK() + ")";
                            if (duplicate.getXSIType().startsWith("xdat:")) {
                                log.debug(output);
                            } else {
                                log.info(output);
                            }

                            if (hasOneColumnTable) {
                                DBAction.ImportNoIdentifierFKs(item, duplicate);
                            }

                            if (HasNewFields(duplicate, item, allowItemOverwrite)) {
                                log.debug("OLD: {}\nNEW: {}", duplicate.toString(), item.toString());
                                item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, allowItemOverwrite);
                            } else {
                                item.importNonItemFields(duplicate, allowItemOverwrite);

                            }

                            cache.getPreexisting().add(item);

                            if (hasOneColumnTable) {
                                duplicate.importNonItemFields(item, false);
                                StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, true);

                                if (HasNewFields(duplicate, item, allowItemOverwrite)) {
                                    item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, false);

                                }
                            }
                        } else {
                            if (cache.getSaved().containsByPK(item, false)) {
                                itemAlreadyStoredInCache = true;
                                XFTItem duplicate = (XFTItem) cache.getSaved().findByPK(item, false);
                                item.importNonItemFields(duplicate, false);

                                if (HasNewFields(duplicate, item, false)) {
                                    item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, false);
                                }

                                if (hasOneColumnTable) {
                                    DBAction.ImportNoIdentifierFKs(item, duplicate);
                                    StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);
                                }
                            } else if (cache.getSaved().containsByUnique(item, false)) {
                                itemAlreadyStoredInCache = true;
                                XFTItem duplicate = (XFTItem) cache.getSaved().findByUnique(item, false);
                                item.importNonItemFields(duplicate, false);

                                if (HasNewFields(duplicate, item, false)) {
                                    item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, false);
                                }

                                if (hasOneColumnTable) {
                                    DBAction.ImportNoIdentifierFKs(item, duplicate);
                                    StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);
                                }
                            } else {
                                //            				      ITEM IS NEW
                                if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                                    GenericWrapperField f    = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                                    XFTItem             meta = (XFTItem) item.getProperty(f);
                                    if (meta == null) {
                                        meta = XFTItem.NewMetaDataElement(user, item.getXSIType(), localQuarantine, cache.getModTime(), cache.getChangeId());
                                        assert meta != null;

                                        StoreItem(meta, user, true, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);
                                        GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");
                                        item.setChild(ref, meta, true);

                                        for (final Object o : ref.getLocalRefNames()) {
                                            ArrayList           refName    = (ArrayList) o;
                                            String              localKey   = (String) refName.get(0);
                                            GenericWrapperField foreignKey = (GenericWrapperField) refName.get(1);
                                            Object              value      = meta.getProperty(foreignKey.getId());
                                            if (value != null) {
                                                if (!item.setFieldValue(localKey, value)) {
                                                    throw new FieldNotFoundException(item.getXSIType() + "/" + localKey.toLowerCase());
                                                }
                                            }
                                        }
                                    } else {
                                        meta.setDirectProperty("row_last_modified", cache.getModTime());
                                        meta.setDirectProperty("last_modified", cache.getModTime());
                                    }
                                }

                                if (hasOneColumnTable) {
                                    StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);
                                }

                                item = InsertItem(item, login, cache, false);
                            }
                        }
                    } else {
                        if (!cache.getSaved().containsByPK(item, false)) {
                            //ITEM IS NEW
                            if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                                GenericWrapperField f    = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                                XFTItem             meta = (XFTItem) item.getProperty(f);
                                if (meta == null) {
                                    meta = XFTItem.NewMetaDataElement(user, item.getXSIType(), localQuarantine, cache.getModTime(), cache.getChangeId());
                                    assert meta != null;

                                    StoreItem(meta, user, true, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);
                                    GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");
                                    item.setChild(ref, meta, true);

                                    for (final Object o : ref.getLocalRefNames()) {
                                        ArrayList           refName    = (ArrayList) o;
                                        String              localKey   = (String) refName.get(0);
                                        GenericWrapperField foreignKey = (GenericWrapperField) refName.get(1);
                                        Object              value      = meta.getProperty(foreignKey.getId());
                                        if (value != null) {
                                            if (!item.setFieldValue(localKey, value)) {
                                                throw new FieldNotFoundException(item.getXSIType() + "/" + localKey.toLowerCase());
                                            }
                                        }
                                    }
                                } else {
                                    meta.setDirectProperty("row_last_modified", cache.getModTime());
                                    meta.setDirectProperty("last_modified", cache.getModTime());
                                }
                            }

                            if (hasOneColumnTable) {
                                StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);
                            }

                            item = InsertItem(item, login, cache, false);
                        } else {
                            itemAlreadyStoredInCache = true;
                            XFTItem duplicate = (XFTItem) cache.getSaved().findByPK(item, false);
                            item.importNonItemFields(duplicate, false);

                            if (HasNewFields(duplicate, item, false)) {
                                item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, false);
                            }

                            if (hasOneColumnTable) {
                                DBAction.ImportNoIdentifierFKs(item, duplicate);
                                StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);
                            }
                        }
                    }
                }

            } else {
                //HAS NO PK
                if (item.hasUniques()) {
                    ItemCollection temp = item.getUniqueMatches(false);
                    if (temp.size() > 0) {
                        isNew = false;
                        XFTItem duplicate = (XFTItem) temp.get(0);
                        String  output    = "Duplicate " + item.getGenericSchemaElement().getFullXMLName() + " Found. (" + duplicate.getPK() + ")";
                        if (duplicate.getXSIType().startsWith("xdat:")) {
                            log.debug(output);
                        } else {
                            log.info(output);
                        }

                        if (hasOneColumnTable) {
                            DBAction.ImportNoIdentifierFKs(item, duplicate);
                        }

                        if (HasNewFields(duplicate, item, allowItemOverwrite)) {
                            log.debug("OLD: {}\nNEW: {}", duplicate, item);
                            item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, allowItemOverwrite);
                        } else {
                            item.importNonItemFields(duplicate, allowItemOverwrite);

                        }

                        cache.getPreexisting().add(item);

                        if (hasOneColumnTable) {
                            duplicate.importNonItemFields(item, false);
                            StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, true);

                            if (HasNewFields(duplicate, item, allowItemOverwrite)) {
                                item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, false);

                            }
                        }
                    } else {
                        if (!cache.getSaved().containsByUnique(item, false)) {
                            //ITEM IS NEW
                            if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                                GenericWrapperField f    = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                                XFTItem             meta = (XFTItem) item.getProperty(f);
                                if (meta == null) {
                                    meta = XFTItem.NewMetaDataElement(user, item.getXSIType(), localQuarantine, cache.getModTime(), cache.getChangeId());
                                    assert meta != null;

                                    StoreItem(meta, user, true, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);
                                    GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");
                                    item.setChild(ref, meta, true);

                                    for (final Object o : ref.getLocalRefNames()) {
                                        ArrayList           refName    = (ArrayList) o;
                                        String              localKey   = (String) refName.get(0);
                                        GenericWrapperField foreignKey = (GenericWrapperField) refName.get(1);
                                        Object              value      = meta.getProperty(foreignKey.getId());
                                        if (value != null) {
                                            if (!item.setFieldValue(localKey, value)) {
                                                throw new FieldNotFoundException(item.getXSIType() + "/" + localKey.toLowerCase());
                                            }
                                        }
                                    }
                                } else {
                                    meta.setDirectProperty("row_last_modified", cache.getModTime());
                                    meta.setDirectProperty("last_modified", cache.getModTime());
                                }
                            }

                            if (hasOneColumnTable) {
                                StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);
                            }

                            item = InsertItem(item, login, cache, false);
                        } else {
                            itemAlreadyStoredInCache = true;
                            XFTItem duplicate = (XFTItem) cache.getSaved().findByUnique(item, false);
                            item.importNonItemFields(duplicate, false);

                            if (HasNewFields(duplicate, item, false)) {
                                item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, false);
                            }


                            if (hasOneColumnTable) {
                                DBAction.ImportNoIdentifierFKs(item, duplicate);
                                StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);
                            }
                        }

                    }
                } else if (item.getGenericSchemaElement().matchByValues() && allowFieldMatching) {
                    ItemCollection temp = item.getExtFieldsMatches(true);
                    if (temp.size() > 0) {
                        isNew = false;
                        XFTItem duplicate = (XFTItem) temp.get(0);
                        String  output    = "Duplicate " + item.getGenericSchemaElement().getFullXMLName() + " Found. (" + duplicate.getPK() + ")";
                        if (duplicate.getXSIType().startsWith("xdat:")) {
                            log.debug(output);
                        } else {
                            log.info(output);
                        }

                        if (hasOneColumnTable) {
                            DBAction.ImportNoIdentifierFKs(item, duplicate);
                        }

                        if (HasNewFields(duplicate, item, allowItemOverwrite)) {
                            log.debug("OLD: {}\nNEW: {}", duplicate.toString(), item.toString());
                            item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, allowItemOverwrite);

                        } else {
                            item.importNonItemFields(duplicate, allowItemOverwrite);

                        }

                        cache.getPreexisting().add(item);

                        if (hasOneColumnTable) {
                            duplicate.importNonItemFields(item, false);
                            StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, true);

                            if (HasNewFields(duplicate, item, allowItemOverwrite)) {
                                item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, false);
                            }
                        }
                    } else {
//                		  ITEM IS NEW
                        if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                            GenericWrapperField f    = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                            XFTItem             meta = (XFTItem) item.getProperty(f);
                            if (meta == null) {
                                meta = XFTItem.NewMetaDataElement(user, item.getXSIType(), localQuarantine, cache.getModTime(), cache.getChangeId());
                                assert meta != null;

                                StoreItem(meta, user, true, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);
                                GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");
                                item.setChild(ref, meta, true);

                                for (final Object o : ref.getLocalRefNames()) {
                                    ArrayList           refName    = (ArrayList) o;
                                    String              localKey   = (String) refName.get(0);
                                    GenericWrapperField foreignKey = (GenericWrapperField) refName.get(1);
                                    Object              value      = meta.getProperty(foreignKey.getId());
                                    if (value != null) {
                                        if (!item.setFieldValue(localKey, value)) {
                                            throw new FieldNotFoundException(item.getXSIType() + "/" + localKey.toLowerCase());
                                        }
                                    }
                                }
                            } else {
                                meta.setDirectProperty("row_last_modified", cache.getModTime());
                                meta.setDirectProperty("last_modified", cache.getModTime());
                            }
                        }

                        if (hasOneColumnTable) {
                            StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);
                        }

                        item = InsertItem(item, login, cache, false);
                    }
                } else {
                    //ITEM IS NEW
                    if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                        GenericWrapperField f    = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                        XFTItem             meta = (XFTItem) item.getProperty(f);
                        if (meta == null) {
                            meta = XFTItem.NewMetaDataElement(user, item.getXSIType(), localQuarantine, cache.getModTime(), cache.getChangeId());
                            assert meta != null;

                            StoreItem(meta, user, true, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);
                            GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");
                            item.setChild(ref, meta, true);

                            for (final Object o : ref.getLocalRefNames()) {
                                ArrayList           refName    = (ArrayList) o;
                                String              localKey   = (String) refName.get(0);
                                GenericWrapperField foreignKey = (GenericWrapperField) refName.get(1);
                                Object              value      = meta.getProperty(foreignKey.getId());
                                if (value != null) {
                                    if (!item.setFieldValue(localKey, value)) {
                                        throw new FieldNotFoundException(item.getXSIType() + "/" + localKey.toLowerCase());
                                    }
                                }
                            }
                        } else {
                            meta.setDirectProperty("row_last_modified", cache.getModTime());
                            meta.setDirectProperty("last_modified", cache.getModTime());
                        }
                    }

                    if (hasOneColumnTable) {
                        StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);
                    }

                    item = InsertItem(item, login, cache, false);
                }
            }

            if (!itemAlreadyStoredInCache) {
                StoreMultipleRefs(item, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager);
            }

            //StoreDuplicateRelationships(item,user,storedRelationships,localQuarantine,overrideQuarantine);
        } catch (XFTInitException e) {
            log.error("Error initializing XFT");
            throw e;
        } catch (ElementNotFoundException e) {
            log.error("Element not found: {}", e.ELEMENT);
            throw e;
        } catch (FieldNotFoundException e) {
            log.error("Field not found {}: {}", e.FIELD, e.MESSAGE);
            throw e;
        } catch (DBPoolException e) {
            log.error("Database connection or pooling exception occurred: {} {}", item.getIDValue(), item.getXSIType());
            throw e;
        } catch (SQLException e) {
            log.error("An SQL error occurred [{}] {}", e.getErrorCode(), e.getSQLState());
            throw e;
        } catch (Exception e) {
            log.error("An unknown error occurred.");
            throw e;
        }

        if (item.modified && !isNew) {
            if (item.getGenericSchemaElement().isExtension()) {
                //add extensions to the history (if they weren't already)
                confirmExtensionHistory(item, cache, user);

                //confirm that item has history has been modified
                if (!cache.getModified().contains(item, false)) {
                    StoreHistoryAndMeta(item, user, null, cache);
                    cache.getModified().add(item);
                }
            }
        }

        if ((item.modified || item.child_modified) && item.getGenericSchemaElement().canBeRoot()) {
            if (!cache.getDBTriggers().contains(item, false)) {
                cache.getDBTriggers().add(item);
            }
        }

        return item;
    }

    /**
     * if this item is an extension of another item, and this item was modified, then add history rows for the extended item.
     *
     * @param i     The item to inspect.
     * @param cache The database item cache.
     * @param user  The user performing the operation.
     *
     * @throws Exception When an unexpected error occurs.
     */
    private static void confirmExtensionHistory(XFTItem i, DBItemCache cache, UserI user) throws Exception {
        if (i.getGenericSchemaElement().isExtension()) {
            final XFTItem extension = i.getExtensionItem();

            //add history rows for this if extended row modified
            if (!cache.getModified().contains(extension, false)) {
                //extended item was not modified... but this one was.
                //add history rows for extended items.
                StoreHistoryAndMeta(extension, user, null, cache);
                cache.getModified().add(extension);
            }

            confirmExtensionHistory(extension, cache, user);
        }
    }

    /**
     * Compares two items to see if there are any new fields.
     *
     * @param oldI               The old item for comparison.
     * @param newI               The new item for comparison.
     * @param allowItemOverwrite Indicates whether overwriting should be allowed.
     *
     * @return True if the new item has any new fields, false otherwise.
     *
     * @throws XFTInitException         When an error occurs accessing XFT
     * @throws ElementNotFoundException When the element or a referenced element can't be found.
     * @throws InvalidValueException    When an invalid value is specified for item properties.
     */
    private static boolean HasNewFields(final XFTItem oldI, final XFTItem newI, final boolean allowItemOverwrite) throws XFTInitException, ElementNotFoundException, InvalidValueException {
        Hashtable   newHash      = newI.getProps();
        Hashtable   oldHashClone = (Hashtable) oldI.getProps().clone();
        Enumeration enumer       = newHash.keys();
        while (enumer.hasMoreElements()) {
            String              field     = (String) enumer.nextElement();
            GenericWrapperField gwf       = oldI.getGenericSchemaElement().getField(field);
            Object              newObject = newHash.get(field);
            if (!(newObject instanceof XFTItem)) {
                try {
                    if (oldI.getProperty(field) != null) {
                        oldHashClone.remove(field);
                        String oldValue = DBAction.ValueParser(oldI.getProperty(field), gwf, true);
                        String newValue = DBAction.ValueParser(newHash.get(field), gwf, false);
                        String type     = null;
                        if (gwf != null) {
                            type = gwf.getXMLType().getLocalType();
                        }

                        if (IsNewValue(type, oldValue, newValue)) {
                            return true;
                        }
                    } else {
                        if (!(newObject.toString().equals("NULL") || newObject.toString().equals(""))) {
                            log.info("OLD:NULL NEW: {}", newObject);
                            return true;
                        }
                    }
                } catch (NumberFormatException e) {
                    log.error("An invalid value was specified for a number", e);
                    log.info("OLD:NULL NEW: {}", newObject);
                    return true;
                } catch (XFTInitException e) {
                    log.error("An error occurred accessing XFT", e);
                    log.info("OLD:NULL NEW: {}", newObject);
                    return true;
                } catch (ElementNotFoundException e) {
                    log.error("Element not found: {}", e.ELEMENT, e);
                    log.info("OLD:NULL NEW: {}", newObject);
                    return true;
                } catch (FieldNotFoundException e) {
                    log.error("Field not found {}: {}", e.FIELD, e.MESSAGE, e);
                    log.info("OLD:NULL NEW: {}", newObject);
                    return true;
                }
            }
        }

        if (allowItemOverwrite) {
            enumer = oldHashClone.keys();
            while (enumer.hasMoreElements()) {
                String              field = (String) enumer.nextElement();
                GenericWrapperField gwf   = oldI.getGenericSchemaElement().getField(field);
                if (gwf == null) {
                    if (!oldI.getGenericSchemaElement().isHiddenFK(field)) {
                        Object newObject = oldHashClone.get(field);
                        if (!(newObject instanceof XFTItem)) {
                            final String newString = newObject.toString();
                            if (StringUtils.isNotBlank(newString)) {
                                if (!oldI.getPkNames().contains(field)) {
                                    log.info("NEW:NULL OLD: {}", newString);
                                    return true;
                                } else {
                                    newI.getProps().put(field, newObject);
                                }
                            }
                        }
                    }
                } else if (gwf.isReference()) {
                    if (!oldI.getGenericSchemaElement().isHiddenFK(field)) {

                        GenericWrapperElement e = (GenericWrapperElement) gwf.getReferenceElement();

                        Object newObject = oldHashClone.get(field);
                        if (e.getAddin().equals("")) {
                            if (!(newObject instanceof XFTItem)) {
                                final String newString = newObject.toString();
                                if (StringUtils.isNotBlank(newString)) {
                                    if (!oldI.getPkNames().contains(field)) {
                                        log.info("NEW:NULL OLD: {}", newObject);
                                        return true;
                                    } else {
                                        newI.getProps().put(field, newObject);
                                    }
                                }
                            }
                        } else {
                            newI.getProps().put(field, newObject);
                        }
                    }
                } else {
                    if (!oldI.getGenericSchemaElement().isHiddenFK(field)) {
                        Object newObject = oldHashClone.get(field);
                        String type      = gwf.getXMLType().getLocalType();
                        if (type != null) {
                            newObject = XMLWriter.ValueParser(newObject, type);
                        }
                        if (!(newObject instanceof XFTItem)) {
                            final String newString = newObject.toString();
                            if (StringUtils.isNotBlank(newString)) {
                                if (!oldI.getPkNames().contains(field)) {
                                    log.info("NEW:NULL OLD: {}", newObject);
                                    return true;
                                } else {
                                    newI.getProps().put(field, newObject);
                                }
                            }
                        }
                    }

                }
            }
        }
        return false;
    }

    public static boolean IsNewValue(String type, String oldValue, String newValue) {
        if (type == null) {
            //REMOVE STRING FORMATTING
            if (oldValue.startsWith("'") && oldValue.endsWith("'")) {
                oldValue = oldValue.substring(1, oldValue.lastIndexOf("'"));
            }
            if (newValue.startsWith("'") && newValue.endsWith("'")) {
                newValue = newValue.substring(1, newValue.lastIndexOf("'"));
            }

            if (!oldValue.equals(newValue)) {
                log.info("OLD: {} NEW: {}", oldValue, newValue);
                return true;
            }
        } else {
            if (type.equalsIgnoreCase("")) {
                if (!oldValue.equals(newValue)) {
                    log.info("OLD: {} NEW: {}", oldValue, newValue);
                    return true;
                }
            } else {
                if (type.equalsIgnoreCase("integer")) {
                    Integer o1 = Integer.valueOf(oldValue);
                    Integer o2 = Integer.valueOf(newValue);

                    if (!o1.equals(o2)) {
                        log.info("OLD: {} NEW: {}", oldValue, newValue);
                        return true;
                    }
                } else if (type.equalsIgnoreCase("boolean")) {
                    Boolean o1;
                    Boolean o2;

                    if (oldValue.equalsIgnoreCase("true") || oldValue.equalsIgnoreCase("1")) {
                        o1 = Boolean.TRUE;
                    } else {
                        o1 = Boolean.FALSE;
                    }

                    if (newValue.equalsIgnoreCase("true") || newValue.equalsIgnoreCase("1")) {
                        o2 = Boolean.TRUE;
                    } else {
                        o2 = Boolean.FALSE;
                    }

                    if (!o1.equals(o2)) {
                        log.info("OLD: {} NEW: {}", oldValue, newValue);
                        return true;
                    }
                } else if (type.equalsIgnoreCase("float")) {
                    if (oldValue.equalsIgnoreCase("NaN")) {
                        oldValue = "'NaN'";
                    }
                    if (newValue.equalsIgnoreCase("NaN")) {
                        newValue = "'NaN'";
                    }
                    if (oldValue.equalsIgnoreCase("INF")) {
                        oldValue = "'Infinity'";
                    }
                    if (oldValue.equalsIgnoreCase("-INF")) {
                        oldValue = "'-Infinity'";
                    }
                    if (newValue.equalsIgnoreCase("-INF")) {
                        newValue = "'-Infinity'";
                    }
                    if (oldValue.equals("'NaN'") || newValue.equals("'NaN'")) {
                        if (!oldValue.equals(newValue)) {
                            log.info("OLD: {} NEW: {}", oldValue, newValue);
                            return true;
                        }
                    } else if (oldValue.equals("'INF'") || newValue.equals("'INF'")) {
                        if (!oldValue.equals(newValue)) {
                            log.info("OLD: {} NEW: {}", oldValue, newValue);
                            return true;
                        }
                    } else {
                        Float o1 = Float.valueOf(oldValue);
                        Float o2 = Float.valueOf(newValue);

                        if (!o1.equals(o2)) {
                            log.info("OLD: {} NEW: {}", oldValue, newValue);
                            return true;
                        }
                    }
                } else if (type.equalsIgnoreCase("double")) {
                    if (oldValue.equalsIgnoreCase("NaN")) {
                        oldValue = "'NaN'";
                    }
                    if (newValue.equalsIgnoreCase("NaN")) {
                        newValue = "'NaN'";
                    }
                    if (oldValue.equalsIgnoreCase("INF")) {
                        oldValue = "'Infinity'";
                    }
                    if (newValue.equalsIgnoreCase("INF")) {
                        newValue = "'Infinity'";
                    }
                    if (oldValue.equalsIgnoreCase("-INF")) {
                        oldValue = "'-Infinity'";
                    }
                    if (newValue.equalsIgnoreCase("-INF")) {
                        newValue = "'-Infinity'";
                    }
                    if (oldValue.equals("'NaN'") || newValue.equals("'NaN'")) {
                        if (!oldValue.equals(newValue)) {
                            log.info("OLD: {} NEW: {}", oldValue, newValue);
                            return true;
                        }
                    } else if (oldValue.contains("Infinity") || newValue.contains("Infinity")) {
                        if (!oldValue.equals(newValue)) {
                            log.info("OLD: {} NEW: {}", oldValue, newValue);
                            return true;
                        }
                    } else {
                        Double o1 = Double.valueOf(oldValue);
                        Double o2 = Double.valueOf(newValue);

                        if (!o1.equals(o2)) {
                            log.info("OLD: {} NEW: {}", oldValue, newValue);
                            return true;
                        }
                    }
                } else if (type.equalsIgnoreCase("decimal")) {
                    if (oldValue.equalsIgnoreCase("NaN")) {
                        oldValue = "'NaN'";
                    }
                    if (newValue.equalsIgnoreCase("NaN")) {
                        newValue = "'NaN'";
                    }
                    if (oldValue.equalsIgnoreCase("INF")) {
                        oldValue = "'Infinity'";
                    }
                    if (oldValue.equalsIgnoreCase("-INF")) {
                        oldValue = "'-Infinity'";
                    }
                    if (newValue.equalsIgnoreCase("-INF")) {
                        newValue = "'-Infinity'";
                    }
                    if (oldValue.equals("'NaN'") || newValue.equals("'NaN'")) {
                        if (!oldValue.equals(newValue)) {
                            log.info("OLD: {} NEW: {}", oldValue, newValue);
                            return true;
                        }
                    } else if (oldValue.equals("'INF'") || newValue.equals("'INF'")) {
                        if (!oldValue.equals(newValue)) {
                            log.info("OLD: {} NEW: {}", oldValue, newValue);
                            return true;
                        }
                    } else {
                        Float o1 = Float.valueOf(oldValue);
                        Float o2 = Float.valueOf(newValue);

                        if (!o1.equals(o2)) {
                            log.info("OLD: {} NEW: {}", oldValue, newValue);
                            return true;
                        }
                    }
                } else if (type.equalsIgnoreCase("date")) {
                    try {
                        Date o1 = DateUtils.parseDate(oldValue);
                        Date o2 = DateUtils.parseDate(newValue);

                        if (!o1.equals(o2)) {
                            log.info("OLD: {} NEW: {}", oldValue, newValue);
                            return true;
                        }
                    } catch (ParseException e) {
                        log.error("", e);
                        log.info("OLD: {} NEW: {}", oldValue, newValue);
                        return true;
                    }
                } else if (type.equalsIgnoreCase("dateTime")) {
                    try {
                        Date o1 = DateUtils.parseDateTime(oldValue);
                        Date o2 = DateUtils.parseDateTime(newValue);

                        if (!o1.equals(o2)) {
                            log.info("OLD: {} NEW: {}", oldValue, newValue);
                            return true;
                        }
                    } catch (ParseException e) {
                        log.error("", e);
                        log.info("OLD: {} NEW: {}", oldValue, newValue);
                        return true;
                    }
                } else if (type.equalsIgnoreCase("time")) {
                    try {
                        Date o1 = DateUtils.parseTime(oldValue);
                        Date o2 = DateUtils.parseTime(newValue);

                        if (!o1.equals(o2)) {
                            log.info("OLD: {} NEW: {}", oldValue, newValue);
                            return true;
                        }
                    } catch (ParseException e) {
                        log.error("", e);
                        log.info("OLD: {} NEW: {}", oldValue, newValue);
                        return true;
                    }
                } else {
                    if (!oldValue.equals(newValue)) {
                        log.info("OLD: {} NEW: {}", oldValue, newValue);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static XFTItem ImportNoIdentifierFKs(XFTItem item, XFTItem dbVersion) {
        try {
            for (final GenericWrapperField ref : item.getGenericSchemaElement().getReferenceFields(false)) {
                try {
                    boolean isNoIdentifierTable = false;
                    try {
                        Object o = item.getProperty(ref.getId());

                        if (o instanceof XFTItem) {
                            XFTItem temp = (XFTItem) o;

                            //check for one column table (if found, see if root item is already stored.  If, the item already was stored and has
                            // a fk value for this table that should be used. Else, this ref item will be stored and the root item will be updated with the fk value
                            String keyName;
                            if (temp.getPossibleFieldNames().size() == 1) {
                                keyName = (String) temp.getPossibleFieldNames().get(0)[0];
                                try {
                                    if (temp.getProperty(keyName) == null) {
                                        isNoIdentifierTable = true;
                                    }
                                } catch (FieldNotFoundException e1) {
                                    log.error("", e1);
                                }
                            }

                            if (!temp.getGenericSchemaElement().hasUniqueIdentifiers()) {
                                isNoIdentifierTable = true;
                            }
                        }

                        if (isNoIdentifierTable) {
                            try {
                                XFTSuperiorReference supRef = (XFTSuperiorReference) ref.getXFTReference();
                                //Set foreign keys based on saved values.
                                for (final XFTRelationSpecification spec : supRef.getKeyRelations()) {
                                    String fieldName = spec.getLocalCol();
                                    try {
                                        Object value = dbVersion.getProperty(fieldName);
                                        if (value != null) {
                                            try {
                                                if (!item.setFieldValue(fieldName.toLowerCase(), value)) {
                                                    throw new FieldNotFoundException(item.getXSIType() + "/" + fieldName.toLowerCase());
                                                }
                                            } catch (ElementNotFoundException e) {
                                                log.error("Element not found {}", e.ELEMENT, e);
                                            } catch (XFTInitException e) {
                                                log.error("Error initializing XFT ", e);
                                            }
                                        }
                                    } catch (XFTInitException e) {
                                        log.error("Error initializing XFT ", e);
                                    } catch (ElementNotFoundException e) {
                                        log.error("Element not found {}", e.ELEMENT, e);
                                    } catch (FieldNotFoundException e) {
                                        log.error("Field not found {}: {}", e.FIELD, e.MESSAGE, e);
                                    }
                                }
                            } catch (XFTInitException e) {
                                log.error("Error initializing XFT ", e);
                            } catch (ElementNotFoundException e) {
                                log.error("Element not found {}", e.ELEMENT, e);
                            }
                        }
                    } catch (FieldNotFoundException e) {
                        log.error("Field not found {}: {}", e.FIELD, e.MESSAGE, e);
                    }
                } catch (XFTInitException e) {
                    log.error("Error initializing XFT ", e);
                } catch (ElementNotFoundException e) {
                    log.error("Element not found {}", e.ELEMENT, e);
                }
            }
        } catch (ElementNotFoundException e) {
            log.error("", e);
        }
        return item;
    }

    private static boolean StoreSingleRefs(XFTItem item, boolean storeSubItems, UserI user, boolean quarantine, boolean overrideQuarantine, boolean allowItemOverwrite, DBItemCache cache, SecurityManagerI securityManager, boolean allowFieldMatching) throws Exception {
        boolean hasNoIdentifier = false;
        //save single refs
        GenericWrapperField ext = null;
        for (final GenericWrapperField ref : item.getGenericSchemaElement().getReferenceFields(false)) {
            if (!item.getGenericSchemaElement().getExtensionFieldName().equalsIgnoreCase(ref.getName())) {
                Object o = item.getProperty(ref.getId());

                if (o instanceof XFTItem) {
                    XFTItem temp = (XFTItem) o;

                    //check for one column table (if found, see if root item is already stored.  If, the item already was stored and has
                    // a fk value for this table that should be used. Else, this ref item will be stored and the root item will be updated with the fk value
                    boolean isNoIdentifierTable = false;
                    String  keyName             = "";
                    if (temp.getPossibleFieldNames().size() == 1) {
                        keyName = (String) temp.getPossibleFieldNames().get(0)[0];
                        if (temp.getProperty(keyName) == null) {
                            isNoIdentifierTable = true;
                            hasNoIdentifier = true;
                        }
                    }

                    if (!temp.getGenericSchemaElement().hasUniqueIdentifiers()) {
                        isNoIdentifierTable = true;
                        hasNoIdentifier = true;
                    }

                    if ((!isNoIdentifierTable) && (!storeSubItems)) {
                        //Store this item.
                        if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName())) {
                            StoreItem(temp, user, false, quarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, true);
                        } else {
                            if (securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName())) {
                                StoreItem(temp, user, false, quarantine, overrideQuarantine, false, cache, securityManager, true);
                            }
                        }

                        XFTSuperiorReference supRef = (XFTSuperiorReference) ref.getXFTReference();
                        //Set foreign keys based on saved values.
                        for (final XFTRelationSpecification spec : supRef.getKeyRelations()) {
                            String fieldName = spec.getLocalCol();
                            Object value     = temp.getProperty(spec.getForeignCol());
                            if (value != null) {
                                if (!item.setFieldValue(fieldName.toLowerCase(), value)) {
                                    throw new FieldNotFoundException(item.getXSIType() + "/" + fieldName.toLowerCase());
                                }
                            }
                        }

                        if (temp.modified || temp.child_modified) {
                            item.child_modified = true;
                        }
                    } else if ((isNoIdentifierTable) && (storeSubItems)) {
                        ArrayList           refName     = (ArrayList) ref.getLocalRefNames().get(0);
                        String              localKey    = (String) refName.get(0);
                        GenericWrapperField foreignKey  = (GenericWrapperField) refName.get(1);
                        Object              rootFKValue = item.getProperty(localKey);

                        if (rootFKValue != null) {
                            if (keyName.equals("")) {
                                keyName = foreignKey.getId();
                                temp.setFieldValue(keyName, rootFKValue);
                                if (temp.getGenericSchemaElement().isExtension()) {
                                    temp.extendPK();
                                }
                                if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName())) {
                                    StoreItem(temp, user, false, quarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, true);
                                } else {
                                    StoreItem(temp, user, false, quarantine, overrideQuarantine, false, cache, securityManager, true);
                                }
                            } else {
                                temp.setFieldValue(keyName, rootFKValue);
                                if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName())) {
                                    StoreItem(temp, user, false, quarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, true);
                                } else {
                                    StoreItem(temp, user, false, quarantine, overrideQuarantine, false, cache, securityManager, true);
                                }
                            }
                        } else {
                            //							 Store this item.
                            if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName())) {
                                StoreItem(temp, user, false, quarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, allowFieldMatching);
                            } else {
                                StoreItem(temp, user, false, quarantine, overrideQuarantine, false, cache, securityManager, true);
                            }

                            //Set foreign keys based on saved values.
                            ref.getLocalRefNames().iterator();
                            Object value = temp.getProperty(foreignKey.getId());
                            if (value != null) {
                                if (!item.setFieldValue(localKey, value)) {
                                    throw new FieldNotFoundException(item.getXSIType() + "/" + localKey.toLowerCase());
                                }
                            }

                        }
                        if (temp.modified || temp.child_modified) {
                            item.child_modified = true;
                            item.modified = true;
                        }
                    }
                }
            } else {
                ext = ref;
            }
        }

        //STORE EXTENSION AFTER OTHER REFERENCES
        if (ext != null) {
            Object o = item.getProperty(ext.getId());

            if (o instanceof XFTItem) {
                XFTItem temp = (XFTItem) o;

                //check for one column table (if found, see if root item is already stored.  If, the item already was stored and has
                // a fk value for this table that should be used. Else, this ref item will be stored and the root item will be updated with the fk value
                boolean isNoIdentifierTable = false;
                String  keyName             = "";
                if (temp.getPossibleFieldNames().size() == 1) {
                    keyName = (String) temp.getPossibleFieldNames().get(0)[0];
                    if (temp.getProperty(keyName) == null) {
                        isNoIdentifierTable = true;
                        hasNoIdentifier = true;
                    }
                }

                if (!temp.getGenericSchemaElement().hasUniqueIdentifiers() && !temp.getGenericSchemaElement().matchByValues()) {
                    isNoIdentifierTable = true;
                    hasNoIdentifier = true;
                }

                if ((!isNoIdentifierTable) && (!storeSubItems)) {
                    //Store this item.
                    if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName())) {
                        StoreItem(temp, user, false, quarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, allowFieldMatching);
                    }

                    final XFTSuperiorReference supRef = (XFTSuperiorReference) ext.getXFTReference();
                    //Set foreign keys based on saved values.
                    for (final XFTRelationSpecification spec : supRef.getKeyRelations()) {
                        String       fieldName = spec.getLocalCol();
                        final Object value     = temp.getProperty(spec.getForeignCol());
                        if (value != null) {
                            if (!item.setFieldValue(fieldName.toLowerCase(), value)) {
                                throw new FieldNotFoundException(item.getXSIType() + "/" + fieldName.toLowerCase());
                            }
                        }
                    }

                    if (temp.modified) {
                        item.modified = true;
                    }
                } else if ((isNoIdentifierTable) && (storeSubItems)) {
                    ArrayList           refName     = (ArrayList) ext.getLocalRefNames().get(0);
                    String              localKey    = (String) refName.get(0);
                    GenericWrapperField foreignKey  = (GenericWrapperField) refName.get(1);
                    Object              rootFKValue = item.getProperty(localKey);

                    if (rootFKValue != null) {
                        if (keyName.equals("")) {
                            keyName = foreignKey.getId();
                            temp.setFieldValue(keyName, rootFKValue);
                            if (temp.getGenericSchemaElement().isExtension()) {
                                temp.extendPK();
                            }
                            if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName())) {
                                StoreItem(temp, user, false, quarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, allowFieldMatching);
                            } else {
                                StoreItem(temp, user, false, quarantine, overrideQuarantine, false, cache, securityManager, allowFieldMatching);
                            }
                        } else {
                            temp.setFieldValue(keyName, rootFKValue);
                            if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName())) {
                                StoreItem(temp, user, false, quarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, allowFieldMatching);
                            } else {
                                StoreItem(temp, user, false, quarantine, overrideQuarantine, false, cache, securityManager, allowFieldMatching);
                            }
                        }
                    } else {
                        //							 Store this item.
                        if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName())) {
                            StoreItem(temp, user, false, quarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, allowFieldMatching);
                        } else {
                            StoreItem(temp, user, false, quarantine, overrideQuarantine, false, cache, securityManager, allowFieldMatching);
                        }

                        //Set foreign keys based on saved values.
                        ext.getLocalRefNames().iterator();
                        Object value = temp.getProperty(foreignKey.getId());
                        if (value != null) {
                            if (!item.setFieldValue(localKey, value)) {
                                throw new FieldNotFoundException(item.getXSIType() + "/" + localKey);
                            }
                        }

                    }

                    if (temp.modified) {
                        item.modified = true;
                    }
                }
            }
        }
        return hasNoIdentifier;
    }

    private static ItemI StoreMultipleRefs(XFTItem item, UserI user, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval, DBItemCache cache, SecurityManagerI securityManager) throws Exception {
//		save multiple refs
        Iterator mRefs = item.getGenericSchemaElement().getMultiReferenceFields().iterator();

        String login = null;
        if (user != null) {
            login = user.getUsername();
        }
        XFTItem dbVersion = null;
        while (mRefs.hasNext()) {
            GenericWrapperField ref    = (GenericWrapperField) mRefs.next();
            XFTReferenceI       xftRef = ref.getXFTReference();
            if (xftRef.isManyToMany()) {
                GenericWrapperElement foreignElement = (GenericWrapperElement) ref.getReferenceElement();

                XFTManyToManyReference many = (XFTManyToManyReference) xftRef;

                if (!foreignElement.hasUniqueIdentifiers() && (!foreignElement.matchByValues())) {
                    //if allowItemRemoval then removes pre-existing non-identified rows
                    if (allowItemRemoval) {
                        if (dbVersion == null) {
                            dbVersion = item.getCurrentDBVersion(false, false);
                        }

                        if (dbVersion != null) {
                            ItemCollection items = dbVersion.getChildItemCollection(ref);

                            Iterator itemsToRemove = items.iterator();
                            while (itemsToRemove.hasNext()) {
                                XFTItem itemToRemove = (XFTItem) itemsToRemove.next();

                                boolean found = false;
                                for (final Object thing : item.getChildItems(ref)) {
                                    XFTItem temp = (XFTItem) thing;
                                    if (temp.hasPK()) {
                                        if (XFTItem.CompareItemsByPKs(temp, itemToRemove)) {
                                            found = true;
                                            break;
                                        }
                                    } else {
                                        if (temp.hasUniques(true)) {
                                            if (XFTItem.CompareItemsByUniques(temp, itemToRemove, true)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                    }

                                }

                                if (!found) {

                                    DBAction.RemoveItemReference(dbVersion, ref.getXMLPathString(item.getXSIType()), itemToRemove, user, cache, false, false);

                                    item.child_modified = true;
                                }
                            }
                        }
                    }
                }

                int      counter  = 0;
                Iterator children = item.getChildItems(ref).iterator();
                while (children.hasNext()) {
                    XFTItem temp = (XFTItem) children.next();
                    temp = StoreItem(temp, user, false, quarantine, overrideQuarantine, allowItemRemoval, cache, securityManager, true);
                    item.setFieldValue(ref.getSQLName().toLowerCase() + (counter), temp);
                    counter = counter + 1;


                    if (temp.modified || temp.child_modified) {
                        item.child_modified = true;
                    }
                }

                children = item.getChildItems(ref).iterator();
                while (children.hasNext()) {
                    XFTItem temp = (XFTItem) children.next();

                    CriteriaCollection search = new CriteriaCollection("AND");
                    //SET MAPPING VALUES
                    for (final Object thing : many.getMappingColumns()) {
                        XFTMappingColumn col = (XFTMappingColumn) thing;
                        if (col.getForeignElement().getFormattedName().equalsIgnoreCase(item.getGenericSchemaElement().getFormattedName())) {
                            //PRIMARY ITEM
                            if (item.getProperty(col.getForeignKey().getId()) != null) {
                                SearchCriteria c = new SearchCriteria();
                                c.setField_name(col.getLocalSqlName());
                                c.setValue(item.getProperty(col.getForeignKey().getId()));
                                c.setCleanedType(col.getXmlType().getLocalType());

                                search.add(c);
                            }
                        } else {
                            //TEMP ITEM
                            if (temp.getProperty(col.getForeignKey().getId()) != null) {
                                SearchCriteria c = new SearchCriteria();
                                c.setField_name(col.getLocalSqlName());
                                c.setValue(temp.getProperty(col.getForeignKey().getId()));
                                c.setCleanedType(col.getXmlType().getLocalType());

                                search.add(c);
                            }
                        }
                    }

                    if (StoreMapping(many, search, login, cache)) {
                        item.child_modified = true;
                    }
                }

                if (foreignElement.hasUniqueIdentifiers() && (!foreignElement.matchByValues())) {
                    //if allowItemRemoval then removes pre-existing non-identified rows
                    if (allowItemRemoval) {
                        if (dbVersion == null) {
                            dbVersion = item.getCurrentDBVersion(false, false);
                        }

                        if (dbVersion != null) {
                            ItemCollection items = dbVersion.getChildItemCollection(ref);

                            Iterator itemsToRemove = items.iterator();
                            while (itemsToRemove.hasNext()) {
                                XFTItem itemToRemove = (XFTItem) itemsToRemove.next();

                                boolean found = false;
                                children = item.getChildItems(ref).iterator();
                                while (children.hasNext()) {
                                    XFTItem temp = (XFTItem) children.next();
                                    if (temp.hasPK()) {
                                        if (XFTItem.CompareItemsByPKs(temp, itemToRemove)) {
                                            found = true;
                                            break;
                                        }
                                    } else {
                                        if (temp.hasUniques(true)) {
                                            if (XFTItem.CompareItemsByUniques(temp, itemToRemove, true)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                }

                                if (!found) {
                                    DBAction.RemoveItemReference(dbVersion, ref.getXMLPathString(item.getXSIType()), itemToRemove, user, cache, false, false);
                                    item.child_modified = true;
                                }
                            }
                        }
                    }
                }

            } else {
                GenericWrapperElement foreignElement = (GenericWrapperElement) ref.getReferenceElement();

                foreignElement.getField(item.getGenericSchemaElement().getFullXMLName());
                if (!foreignElement.hasUniqueIdentifiers() && (!foreignElement.matchByValues())) {
                    if (allowItemRemoval) {
                        if (dbVersion == null) {
                            dbVersion = item.getCurrentDBVersion(false, false);
                        }
                        if (dbVersion != null) {
                            ItemCollection items = dbVersion.getChildItemCollection(ref);

                            Iterator itemsToRemove = items.iterator();
                            while (itemsToRemove.hasNext()) {
                                XFTItem itemToRemove = (XFTItem) itemsToRemove.next();

                                boolean found = false;
                                for (final Object thing : item.getChildItems(ref)) {
                                    XFTItem temp = (XFTItem) thing;
                                    if (temp.hasPK()) {
                                        if (XFTItem.CompareItemsByPKs(temp, itemToRemove)) {
                                            found = true;
                                            break;
                                        }
                                    } else {
                                        if (temp.hasUniques(true)) {
                                            if (XFTItem.CompareItemsByUniques(temp, itemToRemove, true)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                }

                                if (!found) {

                                    DBAction.RemoveItemReference(dbVersion, ref.getXMLPathString(item.getXSIType()), itemToRemove, user, cache, false, false);

                                    item.child_modified = true;
                                }
                            }
                        }
                    }
                }

                XFTSuperiorReference supRef = (XFTSuperiorReference) xftRef;

                for (final Object thing : item.getChildItems(ref)) {
                    XFTItem temp = (XFTItem) thing;

                    for (final XFTRelationSpecification spec : supRef.getKeyRelations()) {
                        String fieldName = spec.getLocalCol();
                        Object value     = item.getProperty(spec.getForeignCol());
                        if (value != null) {
                            boolean set = temp.setFieldValue(fieldName.toLowerCase(), value);
                            if (!set) {
                                throw new FieldNotFoundException(temp.getXSIType() + "/" + fieldName.toLowerCase());
                            }
                        }
                    }

                    if (temp != null) {
                        temp = StoreItem(temp, user, false, quarantine, overrideQuarantine, allowItemRemoval, cache, securityManager, true);


                        if (temp.modified || temp.child_modified) {
                            item.child_modified = true;
                        }

                    }
                }


                if (allowItemRemoval) {
                    if (dbVersion == null) {
                        dbVersion = item.getCurrentDBVersion(false, false);
                    }

                    if (dbVersion != null) {
                        ItemCollection items = dbVersion.getChildItemCollection(ref);

                        Iterator dbItems = items.iterator();
                        while (dbItems.hasNext()) {
                            XFTItem itemToRemove = (XFTItem) dbItems.next();

                            boolean found = false;
                            for (final Object thing : item.getChildItems(ref)) {
                                XFTItem temp = (XFTItem) thing;
                                if (temp.hasPK()) {
                                    if (XFTItem.CompareItemsByPKs(temp, itemToRemove)) {
                                        found = true;
                                        break;
                                    }
                                } else {
                                    if (temp.hasUniques(true)) {
                                        if (XFTItem.CompareItemsByUniques(temp, itemToRemove, true)) {
                                            found = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (!found) {
                                DBAction.RemoveItemReference(dbVersion, ref.getXMLPathString(item.getXSIType()), itemToRemove, user, cache, false, false);
                                item.child_modified = true;
                            }
                        }
                    }
                }
            }
        }

        return item;
    }

    private static boolean StoreMapping(XFTManyToManyReference mapping, CriteriaCollection criteria, String login, DBItemCache cache) throws Exception {
        final XFTTable table = TableSearch.GetMappingTable(mapping, criteria, login);

        if (table.getNumRows() > 0) {
            log.info("Duplicate mapping table row found in '{}'", mapping.getMappingTable());
            return false;
        } else {
            final List<String> values = new ArrayList<>();
            final String query = "INSERT INTO " +
                                 mapping.getMappingTable() +
                                 " (" +
                                 StringUtils.join(Iterables.filter(Iterables.transform(Iterables.filter(criteria.toList(), Predicates.instanceOf(SearchCriteria.class)), new Function<SQLClause, String>() {
                                     @Nullable
                                     @Override
                                     public String apply(final SQLClause clause) {
                                         final SearchCriteria search = (SearchCriteria) clause;
                                         if (search.getValue() == null) {
                                             return null;
                                         }
                                         values.add(search.valueToDB());
                                         return search.getField_name();
                                     }
                                 }), Predicates.<String>notNull()), ", ") +
                                 ") VALUES (" +
                                 StringUtils.join(values, ", ") + ");";
            log.debug("Preparing to store mapping with query: {}", query);
            try {
                new PoolDBUtils().insertItem(query, mapping.getElement1().getDbName(), login, cache);
            } catch (ClassNotFoundException e) {
                log.warn("Class missing", e);
            } catch (SQLException e) {
                log.warn("Error accessing database", e);
            } catch (Exception e) {
                log.warn("Unknown exception occurred", e);
            }
            return true;
        }
    }

    @SuppressWarnings("unused")
    public static XFTItem CheckMetaData(XFTItem item, UserI user, boolean quarantine) {
        try {
            GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
            if (item.getProperty(f) == null) {
                XFTItem meta = XFTItem.NewMetaDataElement(user, item.getXSIType(), quarantine, Calendar.getInstance().getTime(), null);

                item.setChild(f, meta, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

    /**
     * @param item               The item to insert.
     * @param login              The requesting user's login name.
     * @param cache              The database item cache.
     * @param allowInvalidValues Indicates whether an invalid value show trigger an exception and possible email to the site administrator.
     *
     * @return The item after insertion.
     */
    @SuppressWarnings("ConstantConditions")
    public static XFTItem InsertItem(XFTItem item, String login, DBItemCache cache, boolean allowInvalidValues) throws Exception {
        item.modified = true;
        item.assignDefaultValues();

        final GenericWrapperElement element = item.getGenericSchemaElement();
        final PoolDBUtils           con     = new PoolDBUtils();
        if (element.isAutoIncrement()) {
            if (!item.hasPK()) {
                Object key = con.getNextID(element.getDbName(), element.getSQLName(), item.getPkNames().get(0), element.getSequenceName());
                if (key != null) {
                    item.setFieldValue(item.getPkNames().get(0), key);
                }
            }
        }

        final StringBuilder buffer = new StringBuilder("INSERT INTO ").append(element.getSQLName()).append(" (");

        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();

        final Hashtable   props   = item.getProps();
        final Enumeration keys    = props.keys();
        int               counter = 0;
        while (keys.hasMoreElements()) {
            String key   = (String) keys.nextElement();
            Object value = props.get(key);
            if (!(value instanceof XFTItem)) {
                GenericWrapperField field = element.getNonMultipleDataField(key);
                if (field == null) {
                    if (element.validateID(key)) {
                        if (counter++ == 0) {
                            fields = new StringBuilder(key);
                            values = new StringBuilder(ValueParser(value, field, allowInvalidValues));
                        } else {
                            fields.append(",").append(key);
                            values.append(",").append(ValueParser(value, field, allowInvalidValues));
                        }
                    }
                } else {
                    if (counter++ == 0) {
                        fields = new StringBuilder(key);
                        values = new StringBuilder(ValueParser(value, field, allowInvalidValues));
                    } else {
                        fields.append(",").append(key);
                        values.append(",").append(ValueParser(value, field, allowInvalidValues));
                    }
                }
            }
        }
        buffer.append(fields).append(") VALUES (").append(values).append(");");

        final String query = buffer.toString();

        //XFT.LogSQLInfo(query);
        cache.getSaved().add(item);
        if (element.isAutoIncrement()) {
            if (!item.hasPK()) {
                Object key = con.insertNativeItem(query, element.getDbName(), element.getSQLName(), item.getPkNames().get(0), element.getSequenceName());
                if (key != null) {
                    item.setFieldValue(item.getPkNames().get(0), key);
                }
            } else {
                con.insertItem(query, element.getDbName(), login, cache);
            }
        } else {
            con.insertItem(query, element.getDbName(), login, cache);
        }

        cache.handlePostModificationAction(item, "insert");

        if (!element.getFullXMLName().toLowerCase().startsWith("xdat")) {
            log.info("{} stored.", element.getFullXMLName());
        }

        return item;
    }


    public static String getSequenceName(GenericWrapperElement e) {
        if (SEQUENCES.get(e.getSQLName().toLowerCase()) == null) {
            String col_name;

            GenericWrapperField key = e.getAllPrimaryKeys().get(0);

            String newQuery = "SELECT pg_get_serial_sequence('" + e.getSQLName() + "', '" + StringUtils.lowerCase(key.getSQLName()) + "') AS col_name";
            try {
                Object o = PoolDBUtils.ReturnStatisticQuery(newQuery, "col_name", e.getDbName(), null);
                col_name = o.toString();

            } catch (Exception e1) {
                col_name = e.getSQLName() + "_" + key.getSQLName() + "_seq";
                newQuery = "SELECT * FROM " + col_name;
                try {
                    PoolDBUtils.ExecuteNonSelectQuery(newQuery, e.getDbName(), null);
                } catch (Exception e2) {
                    col_name = XftStringUtils.SQLSequenceFormat1(e.getSQLName(), key.getSQLName());
                    newQuery = "SELECT * FROM " + col_name;
                    try {
                        PoolDBUtils.ExecuteNonSelectQuery(newQuery, e.getDbName(), null);
                    } catch (Exception e3) {
                        col_name = XftStringUtils.SQLSequenceFormat2(e.getSQLName(), key.getSQLName());
                        newQuery = "SELECT * FROM " + col_name;
                        try {
                            PoolDBUtils.ExecuteNonSelectQuery(newQuery, e.getDbName(), null);
                        } catch (Exception ignored) {
                        }
                    }
                }

            }

            if (col_name != null) {
                SEQUENCES.put(e.getSQLName().toLowerCase(), col_name);
            }
        }

        return SEQUENCES.get(e.getSQLName().toLowerCase());
    }

    public static String getSequenceName(String table, String key, String dbName) {
        String col_name;
        String newQuery = "SELECT pg_get_serial_sequence('" + table + "', '" + StringUtils.lowerCase(key) + "') AS col_name";
        try {
            Object o = PoolDBUtils.ReturnStatisticQuery(newQuery, "col_name", dbName, null);
            col_name = o.toString();

        } catch (Exception e1) {
            col_name = table + "_" + key + "_seq";
            newQuery = "SELECT * FROM " + col_name;
            try {
                PoolDBUtils.ExecuteNonSelectQuery(newQuery, dbName, null);
            } catch (Exception e2) {
                col_name = XftStringUtils.SQLSequenceFormat1(table, key);
                newQuery = "SELECT * FROM " + col_name;
                try {
                    PoolDBUtils.ExecuteNonSelectQuery(newQuery, dbName, null);
                } catch (Exception e3) {
                    col_name = XftStringUtils.SQLSequenceFormat2(table, key);
                    newQuery = "SELECT * FROM " + col_name;
                    try {
                        PoolDBUtils.ExecuteNonSelectQuery(newQuery, dbName, null);
                    } catch (Exception ignored) {
                    }
                }
            }

        }
        return col_name;
    }

    private static void Quarantine(XFTItem oldI, UserI user, boolean quarantine, boolean overrideQuarantine, DBItemCache cache) throws Exception {
        // MARK MODIFIED AS TRUE
        if (oldI.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
            Object metaDataId = oldI.getProperty(oldI.getGenericSchemaElement().getMetaDataFieldName().toLowerCase());
            if (metaDataId != null) {
                XFTItem oldMeta = XFTItem.NewItem(oldI.getGenericSchemaElement().getFullXMLName() + "_meta_data", null);
                oldMeta.setFieldValue("meta_data_id", metaDataId);
                oldMeta.setFieldValue("modified", "1");
                oldMeta.setFieldValue("last_modified", Calendar.getInstance().getTime());

                boolean q;
                if (overrideQuarantine) {
                    q = quarantine;
                } else {
                    q = oldI.getGenericSchemaElement().isQuarantine(quarantine);
                }

                if (q) {
                    oldMeta.setFieldValue("status", ViewManager.QUARANTINE);
                }
                UpdateItem(oldMeta, user, cache, false);
            }
            oldI.modified = true;
        }

    }

    private static void StoreHistoryAndMeta(XFTItem oldI, XFTItem newI, UserI user, Boolean quarantine, DBItemCache cache) throws Exception {
        XFTItem meta = StoreHistoryAndMeta(oldI, user, quarantine, cache);


        if (newI != null && meta != null) {
            newI.setProperty("meta.meta_data_id", meta.getProperty("meta_data_id"));
            newI.setProperty("meta.status", meta.getProperty("status"));
        }
    }

    private static final List<String> modifiable_status = Arrays.asList(ViewManager.ACTIVE, ViewManager.QUARANTINE);

    private static XFTItem StoreHistoryAndMeta(XFTItem oldI, UserI user, Boolean quarantine, DBItemCache cache) throws Exception {
        if (oldI.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
            XFTItem meta = (XFTItem) oldI.getMeta();

            //STORE HISTORY ITEM
            try {
                StoreHistoryItem(oldI, user, cache, oldI.getRowLastModified());
            } catch (ElementNotFoundException ignored) {
            }

            if (meta != null) {
                meta.setFieldValue("modified", "1");
                meta.setFieldValue("last_modified", cache.getModTime());//other processes may change this as well, like modifications to a child element
                meta.setFieldValue("row_last_modified", cache.getModTime());//added to track specific changes to this row
                meta.setFieldValue("xft_version", cache.getChangeId());//added to track specific changes to this row

                //noinspection SuspiciousMethodCalls
                if (!modifiable_status.contains(meta.getField("status"))) {
                    throw new UnmodifiableStatusException(oldI.getXSIType() + ":" + oldI.getPKValueString() + ":" + meta.getField("status"));
                }

                if (quarantine != null) {
                    if (quarantine) {
                        meta.setFieldValue("status", ViewManager.QUARANTINE);
                    } else {
                        meta.setFieldValue("status", ViewManager.ACTIVE);
                    }
                }

                UpdateItem(meta, user, cache, false);
            }
            return meta;
        }
        return null;
    }

    public static class UnmodifiableStatusException extends Exception {
        private static final long serialVersionUID = 68282673755608498L;

        public UnmodifiableStatusException(String s) {
            super(s);
        }
    }

    /**
     *
     */
    private static XFTItem UpdateItem(final XFTItem oldI, final XFTItem newI, final UserI user, final boolean quarantine, final boolean overrideQuarantine, final DBItemCache cache, final boolean storeNULLS) throws Exception {
        // MARK MODIFIED AS TRUE
        StoreHistoryAndMeta(oldI, newI, user, overrideQuarantine ? quarantine : oldI.getGenericSchemaElement().isQuarantine(quarantine), cache);

        //COPY PK VALUES INTO NEW ITEM
        newI.getProps().putAll(oldI.getPkValues());

        //UPDATE ITEM
        final XFTItem newItem = UpdateItem(newI, user, cache, storeNULLS);

        cache.getModified().add(newItem);

        oldI.modified = true;
        newItem.modified = true;

        return newItem;
    }

    public static void StoreHistoryItem(XFTItem oldI, UserI user, final DBItemCache cache, final Date previousChangeDate) throws Exception {
        String login = null;
        if (user != null) {
            login = user.getUsername();
        }
        if (oldI.getGenericSchemaElement().storeHistory()) {
            XFTItem history = XFTItem.NewItem(oldI.getXSIType() + "_history", null);
            history.importNonItemFields(oldI, false);
            if (user != null) {
                history.setDirectProperty("change_user", user.getID());
            }
            if (previousChangeDate != null) {
                history.setDirectProperty("previous_change_date", previousChangeDate);
            }
            history.setDirectProperty("change_date", cache.getModTime());
            history.setDirectProperty("xft_version", oldI.getXFTVersion());

            Hashtable   pkHash = (Hashtable) oldI.getPkValues();
            Enumeration pks    = pkHash.keys();
            while (pks.hasMoreElements()) {
                String name = (String) pks.nextElement();
                history.setFieldValue("new_row_" + name, pkHash.get(name));
            }

            //INSERT HISTORY ITEM
            InsertItem(history, login, cache, true);
        }
    }

    /**
     * @param item       The item to be updated.
     * @param user       The user performing the operation.
     * @param cache      The database item cache.
     * @param storeNULLS Whether NULL values should be stored or cleared out.
     *
     * @return The item after updating.
     */
    private static XFTItem UpdateItem(XFTItem item, UserI user, DBItemCache cache, boolean storeNULLS) throws ElementNotFoundException, XFTInitException, FieldNotFoundException, InvalidValueException {
        String login = null;
        if (user != null) {
            login = user.getUsername();
        }
        StringBuilder         query   = new StringBuilder("UPDATE ");
        GenericWrapperElement element = item.getGenericSchemaElement();

        query.append(element.getSQLName()).append(" SET ");

        Hashtable props   = item.getProps();
        int       counter = 0;
        for (final Object[] possibleField : item.getPossibleFieldNames()) {
            String key   = (String) possibleField[0];
            Object value = props.get(key);
            if (!item.getPkNames().contains(key)) {
                if (value == null) {
                    if (storeNULLS) {
                        if (!item.getGenericSchemaElement().isHiddenFK(key)) {
                            GenericWrapperField field = element.getNonMultipleDataField(key);

                            if (!element.getExtensionFieldName().equalsIgnoreCase(field.getName())) {
                                if (field.isReference()) {
                                    GenericWrapperElement e = (GenericWrapperElement) field.getReferenceElement();

                                    if (e.getAddin().equals("")) {
                                        if (counter++ == 0) {
                                            query.append(key).append("= NULL");
                                        } else {
                                            query.append(", ").append(key).append("= NULL");
                                        }
                                    }
                                } else {
                                    if (counter++ == 0) {
                                        query.append(key).append("= NULL");
                                    } else {
                                        query.append(", ").append(key).append("= NULL");
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (!(value instanceof XFTItem)) {
                        GenericWrapperField field = element.getNonMultipleField(key);
                        if (counter++ == 0) {
                            query.append(key).append("=").append(ValueParser(value, field, false));
                        } else {
                            query.append(", ").append(key).append("=").append(ValueParser(value, field, false));
                        }
                    }
                }
            }
        }
        query.append(" WHERE ");

        counter = 0;
        for (final String key : item.getPkNames()) {
            final GenericWrapperField field = element.getNonMultipleField(key);
            if (counter++ == 0) {
                query.append(key).append("=").append(ValueParser(item.getProperty(key), field, false)).append(" ");
            } else {
                query.append(" AND ").append(key).append("=").append(ValueParser(item.getProperty(key), field, false)).append(" ");
            }
        }
        query.append(";");

        try {
            final PoolDBUtils con = new PoolDBUtils();
            con.updateItem(query.toString(), element.getDbName(), login, cache);
            cache.handlePostModificationAction(item, "update");

            if (!element.getFullXMLName().toLowerCase().startsWith("xdat")) {
                log.info("{} updated.", element.getFullXMLName());
            }
        } catch (ClassNotFoundException e) {
            log.warn("Class missing", e);
        } catch (SQLException e) {
            log.warn("Error accessing database", e);
        } catch (Exception e) {
            log.warn("Unknown exception occurred", e);
        }

        item.modified = true;

        return item;
    }

    private static String buildType(GenericWrapperField field) {
        return field.getXMLType().getLocalType();
    }


    /**
     * Formats the object to a string for SQL interaction based on the XMLType of the field.
     *
     * @param object             The object containing the value to be parsed.
     * @param field              The generic wrapper field.
     * @param allowInvalidValues Indicates whether an invalid value show trigger an exception and possible email to the site administrator.
     *
     * @return The string parsed from the object.
     *
     * @throws XFTInitException      When an error occurs accessing XFT.
     * @throws InvalidValueException When an invalid value is specified for item properties.
     */
    public static String ValueParser(Object object, GenericWrapperField field, boolean allowInvalidValues) throws XFTInitException, InvalidValueException {
        if (field != null) {
            if (field.isReference()) {
                try {
                    GenericWrapperElement foreign    = (GenericWrapperElement) field.getReferenceElement();
                    GenericWrapperField   foreignKey = foreign.getAllPrimaryKeys().get(0);
                    return ValueParser(object, foreignKey, allowInvalidValues);
                } catch (Exception e) {
                    return object.toString();
                }
            }


            String type = buildType(field);
            if (type.equalsIgnoreCase("")) {
                if (object instanceof String) {
                    if (object.toString().equalsIgnoreCase("NULL")) {
                        return "NULL";
                    } else {
                        return "'" + object.toString() + "'";
                    }
                } else {
                    return object.toString();
                }
            } else if (type.equalsIgnoreCase("LONGVARCHAR")) {
                return "'" + XftStringUtils.CleanForSQLValue(object.toString()) + "'";
            } else {
                if (type.equalsIgnoreCase("string")) {
                    if (field.getWrapped().getRule().getBaseType().equals("xs:anyURI")) {
                        return ValueParser(object, "anyURI", allowInvalidValues);
                    }
                }
                return ValueParser(object, type, allowInvalidValues);
            }
        } else {
            if (object instanceof String) {
                if (object.toString().equalsIgnoreCase("NULL")) {
                    return "NULL";
                } else {
                    return "'" + XftStringUtils.CleanForSQLValue(object.toString()) + "'";
                }
            } else {
                return object.toString();
            }
        }
    }

    /**
     * Formats the object to a string for SQL interaction based on the submitted type.
     *
     * @param object             The The object containing the value to be parsed.
     * @param type               The type to be used for conversion.
     * @param allowInvalidValues Indicates whether an invalid value show trigger an exception and possible email to the site administrator.
     *
     * @return The string parsed from the object.
     *
     * @throws InvalidValueException When an invalid value is specified for item properties.
     */
    public static String ValueParser(Object object, String type, boolean allowInvalidValues) throws InvalidValueException {
        if (object != null) {
            if (object.toString().equalsIgnoreCase("NULL")) {
                return "NULL";
            }
        } else {
            return "";
        }

        if (type.equalsIgnoreCase("string")) {
            if (object.getClass().getName().equalsIgnoreCase("[B")) {
                byte[]                        b    = (byte[]) object;
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                try {
                    baos.write(b);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (baos.toString().equalsIgnoreCase("NULL")) {
                    return "NULL";
                } else {
                    String s     = baos.toString();
                    String upper = s.toUpperCase();
                    if (s.contains("<") && s.contains(">") && (upper.contains("SCRIPT") || ((upper.contains("IMG") || upper.contains("IMAGE")) && (upper.contains("JAVASCRIPT"))))) {
                        if (!allowInvalidValues) {
                            AdminUtils.sendAdminEmail("Possible Cross-site scripting attempt blocked", s);
                            throw new InvalidValueException("Use of '<' and '>' are not allowed in content.");
                        }
                    }
                    return "'" + XftStringUtils.CleanForSQLValue(s) + "'";
                }
            } else if (object.toString().equalsIgnoreCase("NULL")) {
                return "NULL";
            } else {
                String s     = object.toString();
                String upper = s.toUpperCase();
                if (s.contains("<") && s.contains(">") && (upper.contains("SCRIPT") || ((upper.contains("IMG") || upper.contains("IMAGE")) && (upper.contains("JAVASCRIPT"))))) {
                    if (!allowInvalidValues) {
                        AdminUtils.sendAdminEmail("Possible Cross-site scripting attempt blocked", s);
                        throw new InvalidValueException("Use of '<' and '>' are not allowed in content.");
                    }
                }
                return "'" + XftStringUtils.CleanForSQLValue(s) + "'";
            }
        } else if (type.equalsIgnoreCase("anyURI")) {
            if (object.toString().equalsIgnoreCase("NULL")) {
                return "NULL";
            } else {
                final String uri = object.toString().replace('\\', '/');
                if (uri.contains("..")) {
                    throw new InvalidValueException("URIs cannot contain '..'");
                }
                if (FileUtils.IsAbsolutePath(uri)) {
                    //must be within a specified directory, to prevent directory traversal to secured files.
                    if (!allowInvalidValues) {
                        FileUtils.ValidateUriAgainstRoot(uri, XDAT.getSiteConfigPreferences().getArchivePath(), "URI references data outside of the archive:" + uri);
                    }

                }
                return "'" + XftStringUtils.CleanForSQLValue(uri) + "'";
            }
        } else if (type.equalsIgnoreCase("ID")) {
            return "'" + XftStringUtils.CleanForSQLValue(object.toString()) + "'";
        } else if (type.equalsIgnoreCase("boolean")) {
            if (object.toString().equalsIgnoreCase("true") || object.toString().equalsIgnoreCase("1")) {
                return "1";
            } else {
                return "0";
            }
        } else if (type.equalsIgnoreCase("float")) {
            if (object.toString().equalsIgnoreCase("nan")) {
                return "'" + object.toString() + "'";
            } else if (object.toString().equalsIgnoreCase("inf") || object.toString().equalsIgnoreCase("Infinity")) {
                return "'Infinity'";
            } else if (object.toString().equalsIgnoreCase("-inf") || object.toString().equalsIgnoreCase("-Infinity")) {
                return "'-Infinity'";
            } else if (object.toString().equals("")) {
                return "NULL";
            } else {
                return object.toString();
            }
        } else if (type.equalsIgnoreCase("double")) {
            if (object.toString().equalsIgnoreCase("nan")) {
                return "'" + object.toString() + "'";
            } else if (object.toString().equalsIgnoreCase("inf") || object.toString().equalsIgnoreCase("Infinity")) {
                return "'Infinity'";
            } else if (object.toString().equalsIgnoreCase("-inf") || object.toString().equalsIgnoreCase("-Infinity")) {
                return "'-Infinity'";
            } else if (object.toString().equals("")) {
                return "NULL";
            } else {
                return object.toString();
            }
        } else if (type.equalsIgnoreCase("decimal")) {
            if (object.toString().equalsIgnoreCase("nan")) {
                return "'" + object.toString() + "'";
            } else if (object.toString().equalsIgnoreCase("inf") || object.toString().equalsIgnoreCase("Infinity")) {
                return "'Infinity'";
            } else if (object.toString().equalsIgnoreCase("-inf") || object.toString().equalsIgnoreCase("-Infinity")) {
                return "'-Infinity'";
            } else if (object.toString().equals("")) {
                return "NULL";
            } else {
                return object.toString();
            }
        } else if (type.equalsIgnoreCase("integer")) {
            if (object.toString().equalsIgnoreCase("nan")) {
                return "'" + object.toString() + "'";
            } else if (object.toString().equalsIgnoreCase("inf") || object.toString().equalsIgnoreCase("Infinity")) {
                return "'Infinity'";
            } else if (object.toString().equalsIgnoreCase("-inf") || object.toString().equalsIgnoreCase("-Infinity")) {
                return "'-Infinity'";
            } else if (object.toString().equals("")) {
                return "NULL";
            } else {
                return object.toString();
            }
        } else if (type.equalsIgnoreCase("nonPositiveInteger")) {
            if (object.toString().equalsIgnoreCase("nan")) {
                return "'" + object.toString() + "'";
            } else if (object.toString().equalsIgnoreCase("inf") || object.toString().equalsIgnoreCase("Infinity")) {
                return "'Infinity'";
            } else if (object.toString().equalsIgnoreCase("-inf") || object.toString().equalsIgnoreCase("-Infinity")) {
                return "'-Infinity'";
            } else if (object.toString().equals("")) {
                return "NULL";
            } else {
                return object.toString();
            }
        } else if (type.equalsIgnoreCase("negativeInteger")) {
            if (object.toString().equalsIgnoreCase("nan")) {
                return "'" + object.toString() + "'";
            } else if (object.toString().equalsIgnoreCase("inf") || object.toString().equalsIgnoreCase("Infinity")) {
                return "'Infinity'";
            } else if (object.toString().equalsIgnoreCase("-inf") || object.toString().equalsIgnoreCase("-Infinity")) {
                return "'-Infinity'";
            } else if (object.toString().equals("")) {
                return "NULL";
            } else {
                return object.toString();
            }
        } else if (type.equalsIgnoreCase("long")) {
            if (object.toString().equalsIgnoreCase("nan")) {
                return "'" + object.toString() + "'";
            } else if (object.toString().equalsIgnoreCase("inf") || object.toString().equalsIgnoreCase("Infinity")) {
                return "'Infinity'";
            } else if (object.toString().equalsIgnoreCase("-inf") || object.toString().equalsIgnoreCase("-Infinity")) {
                return "'-Infinity'";
            } else if (object.toString().equals("")) {
                return "NULL";
            } else {
                return object.toString();
            }
        } else if (type.equalsIgnoreCase("int")) {
            if (object.toString().equalsIgnoreCase("nan")) {
                return "'" + object.toString() + "'";
            } else if (object.toString().equalsIgnoreCase("inf") || object.toString().equalsIgnoreCase("Infinity")) {
                return "'Infinity'";
            } else if (object.toString().equalsIgnoreCase("-inf") || object.toString().equalsIgnoreCase("-Infinity")) {
                return "'-Infinity'";
            } else if (object.toString().equals("")) {
                return "NULL";
            } else {
                return object.toString();
            }
        } else if (type.equalsIgnoreCase("short")) {
            return object.toString();
        } else if (type.equalsIgnoreCase("byte")) {
            return object.toString();
        } else if (type.equalsIgnoreCase("nonNegativeInteger")) {
            if (object.toString().equalsIgnoreCase("nan")) {
                return "'" + object.toString() + "'";
            } else if (object.toString().equalsIgnoreCase("inf") || object.toString().equalsIgnoreCase("Infinity")) {
                return "'Infinity'";
            } else if (object.toString().equalsIgnoreCase("-inf") || object.toString().equalsIgnoreCase("-Infinity")) {
                return "'-Infinity'";
            } else if (object.toString().equals("")) {
                return "NULL";
            } else {
                return object.toString();
            }
        } else if (type.equalsIgnoreCase("unsignedLong")) {
            if (object.toString().equalsIgnoreCase("nan")) {
                return "'" + object.toString() + "'";
            } else if (object.toString().equalsIgnoreCase("inf") || object.toString().equalsIgnoreCase("Infinity")) {
                return "'Infinity'";
            } else if (object.toString().equalsIgnoreCase("-inf") || object.toString().equalsIgnoreCase("-Infinity")) {
                return "'-Infinity'";
            } else if (object.toString().equals("")) {
                return "NULL";
            } else {
                return object.toString();
            }
        } else if (type.equalsIgnoreCase("unsignedInt")) {
            if (object.toString().equalsIgnoreCase("nan")) {
                return "'" + object.toString() + "'";
            } else if (object.toString().equalsIgnoreCase("inf") || object.toString().equalsIgnoreCase("Infinity")) {
                return "'Infinity'";
            } else if (object.toString().equalsIgnoreCase("-inf") || object.toString().equalsIgnoreCase("-Infinity")) {
                return "'-Infinity'";
            } else if (object.toString().equals("")) {
                return "NULL";
            } else {
                return object.toString();
            }
        } else if (type.equalsIgnoreCase("unsignedShort")) {
            return object.toString();
        } else if (type.equalsIgnoreCase("unsignedByte")) {
            return object.toString();
        } else if (type.equalsIgnoreCase("positiveInteger")) {
            if (object.toString().equalsIgnoreCase("nan")) {
                return "'" + object.toString() + "'";
            } else if (object.toString().equalsIgnoreCase("inf") || object.toString().equalsIgnoreCase("Infinity")) {
                return "'Infinity'";
            } else if (object.toString().equalsIgnoreCase("-inf") || object.toString().equalsIgnoreCase("-Infinity")) {
                return "'-Infinity'";
            } else if (object.toString().equals("")) {
                return "NULL";
            } else {
                return object.toString();
            }
        } else if (type.equalsIgnoreCase("time")) {
            return "'" + XftStringUtils.CleanForSQLValue(object.toString()) + "'";
        } else if (type.equalsIgnoreCase("date")) {
            return "'" + XftStringUtils.CleanForSQLValue(object.toString()) + "'";
        } else if (type.equalsIgnoreCase("dateTime")) {
            Date d;
            if (object instanceof Date) {
                d = (Date) object;
            } else {
                try {
                    d = DateUtils.parseDateTime(object.toString());
                } catch (ParseException e) {
                    if (object.toString().trim().equals("NOW()")) {
                        return "NOW()";
                    } else {
                        return "'" + XftStringUtils.CleanForSQLValue(object.toString()) + "'";
                    }
                }
            }

            if (d != null) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                return "'" + df.format(d) + "'";
            } else {
                return "'" + XftStringUtils.CleanForSQLValue(object.toString()) + "'";
            }
        } else {
            return object.toString();
        }
    }

    /**
     * @param item     The item with the reference to be removed.
     * @param xmlPath  The XML path of the property reference.
     * @param toRemove The item to be removed from the reference.
     * @param user     The user removing the item.
     * @param c        The event for the remove item reference action.
     */
    public static void RemoveItemReference(XFTItem item, String xmlPath, XFTItem toRemove, UserI user, EventMetaI c) throws Exception {
        DBItemCache cache = new DBItemCache(user, c);
        RemoveItemReference(item, xmlPath, toRemove, user, cache, false, false);

        PoolDBUtils con;
        if (!cache.getSQL().equals("") && !cache.getSQL().equals("[]")) {
            String  username     = null;
            Integer xdat_user_id = null;
            if (user != null) {
                username = user.getUsername();
                xdat_user_id = user.getID();
            }

            if (!cache.getDBTriggers().contains(item, false)) {
                cache.getDBTriggers().add(item);
            }

            PerformUpdateTriggers(cache, username, xdat_user_id, false);

            con = new PoolDBUtils();
            con.sendBatch(cache, item.getDBName(), username);

            //PerformUpdateTriggers(cache, username,xdat_user_id,false); This shouldn't be necessary TO
        } else {
            log.info("Pre-existing item found without modifications");
        }

    }

    /**
     * @param item          The item with the reference to be removed.
     * @param xmlPath       The XML path of the property reference.
     * @param toRemove      The item to be removed from the reference.
     * @param user          The user removing the item.
     * @param cache         The database item cache.
     * @param parentDeleted Whether the parent element was deleted.
     * @param noHistory     Whether history should be recorded for the action.
     */
    private static void RemoveItemReference(XFTItem item, String xmlPath, XFTItem toRemove, UserI user, DBItemCache cache, boolean parentDeleted, boolean noHistory) throws Exception {
        String login = null;
        if (user != null) {
            login = user.getUsername();
        }

        if (cache != null) {
            cache.getRemoved().add(toRemove);
        }

        try {
            GenericWrapperElement root = item.getGenericSchemaElement();

            boolean             foundField = false;
            GenericWrapperField field      = null;
            if (xmlPath != null && !xmlPath.equals("")) {
                field = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
            } else {
                for (final GenericWrapperField f : root.getReferenceFields(true)) {
                    if (f.getReferenceElement().getFullXMLName().equalsIgnoreCase(toRemove.getXSIType())) {
                        for (final Object object : item.getChildItems(f)) {
                            XFTItem child = (XFTItem) object;
                            if (XFTItem.CompareItemsByPKs(child, toRemove)) {
                                foundField = true;
                                field = f;
                                break;
                            }
                        }

                        if (foundField) {
                            break;
                        }
                    }
                }
            }


            if (field != null) {
                //GenericWrapperElement foreign = (GenericWrapperElement)field.getReferenceElement();
                //MODIFIED ON 04/25/07 BY TIM To capture extensions
                GenericWrapperElement foreign = toRemove.getGenericSchemaElement();
                if (foreign.hasUniqueIdentifiers() || foreign.matchByValues()) {
                    Integer referenceCount;
                    if (cache != null && cache.getPreexisting().contains(toRemove, false)) {
                        referenceCount = 100;
                    } else {
                        referenceCount = XFTReferenceManager.NumberOfReferences(toRemove);
                    }

                    if (field.isMultiple()) {
                        //CHECK TO SEE IF OTHER ELEMENTS REFERENCE THIS ONE
                        //IF SO, BREAK THE REFERENCE BUT DO NOT DELETE
                        //ELSE, DELETE THE ITEM
                        if (referenceCount > 1) {
                            if (field.getXFTReference().isManyToMany()) {
                                XFTManyToManyReference ref     = (XFTManyToManyReference) field.getXFTReference();
                                ArrayList              values  = new ArrayList();
                                Iterator               refCols = ref.getMappingColumnsForElement(foreign).iterator();
                                while (refCols.hasNext()) {
                                    XFTMappingColumn spec = (XFTMappingColumn) refCols.next();
                                    Object           o    = toRemove.getProperty(spec.getForeignKey().getXMLPathString(toRemove.getXSIType()));
                                    ArrayList        al   = new ArrayList();
                                    al.add(spec.getLocalSqlName());
                                    al.add(DBAction.ValueParser(o, spec.getXmlType().getLocalType(), true));
                                    values.add(al);
                                }

                                refCols = ref.getMappingColumnsForElement(root).iterator();
                                while (refCols.hasNext()) {
                                    XFTMappingColumn spec = (XFTMappingColumn) refCols.next();
                                    Object           o    = item.getProperty(spec.getForeignKey().getXMLPathString(item.getXSIType()));
                                    ArrayList        al   = new ArrayList();
                                    al.add(spec.getLocalSqlName());
                                    al.add(DBAction.ValueParser(o, spec.getXmlType().getLocalType(), true));
                                    values.add(al);
                                }

                                if (values.size() > 1) {
                                    DBAction.DeleteMappings(ref, root.getDbName(), values, login, cache, noHistory);
                                } else {
                                    throw new Exception("Failed to identify both ids for the mapping table.");
                                }

                            } else {

                                GenericWrapperElement gwe      = null;
                                XFTSuperiorReference  ref      = (XFTSuperiorReference) field.getXFTReference();
                                Iterator              refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext()) {
                                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
                                    GenericWrapperField      f    = GenericWrapperElement.GetFieldForXMLPath(toRemove.getXSIType() + "." + spec.getLocalCol());
                                    assert f != null;
                                    gwe = f.getParentElement().getGenericXFTElement();
                                }
                                assert gwe != null;

                                XFTItem updateItem = toRemove;
                                if (!gwe.getFullXMLName().equalsIgnoreCase(updateItem.getXSIType())) {
                                    updateItem = updateItem.getExtensionItem(gwe.getFullXMLName());
                                    if (updateItem == null) {
                                        updateItem = toRemove;
                                    }
                                }

                                if (!noHistory) {
                                    try {
                                        StoreHistoryAndMeta(updateItem, user, null, cache);
                                    } catch (ElementNotFoundException e) {
                                        if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                                            throw e;
                                        }
                                    }
                                }

                                refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext()) {
                                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
                                    updateItem.setProperty(updateItem.getXSIType() + "." + spec.getLocalCol(), "NULL");
                                }

                                //UPDATE ITEM
                                toRemove = UpdateItem(updateItem, user, cache, true);
                                item.removeItem(toRemove);
                            }
                        } else {
                            if (field.getXFTReference().isManyToMany()) {
                                XFTManyToManyReference      ref     = (XFTManyToManyReference) field.getXFTReference();
                                ArrayList                   values  = new ArrayList();
                                ArrayList<XFTMappingColumn> forCols = ref.getMappingColumnsForElement(foreign);
                                for (XFTMappingColumn spec : forCols) {
                                    Object    o  = toRemove.getProperty(spec.getForeignKey().getXMLPathString(toRemove.getXSIType()));
                                    ArrayList al = new ArrayList();
                                    al.add(spec.getLocalSqlName());
                                    al.add(DBAction.ValueParser(o, spec.getXmlType().getLocalType(), true));
                                    values.add(al);
                                }

                                ArrayList<XFTMappingColumn> localCols = ref.getMappingColumnsForElement(root);
                                for (XFTMappingColumn spec : localCols) {
                                    Object    o  = item.getProperty(spec.getForeignKey().getXMLPathString(item.getXSIType()));
                                    ArrayList al = new ArrayList();
                                    al.add(spec.getLocalSqlName());
                                    al.add(DBAction.ValueParser(o, spec.getXmlType().getLocalType(), true));
                                    values.add(al);
                                }

                                if (values.size() > 1) {

                                    DBAction.DeleteMappings(ref, root.getDbName(), values, login, cache, noHistory);

                                    DeleteItem(toRemove, user, cache, noHistory, field.isPossibleLoop());
                                    item.removeItem(toRemove);
                                } else {
                                    throw new Exception("Failed to identify both ids for the mapping table.");
                                }
                            } else {

                                DeleteItem(toRemove, user, cache, noHistory, field.isPossibleLoop());
                                item.removeItem(toRemove);
                            }
                        }
                    } else {
                        if (referenceCount > 1) {
                            if (!parentDeleted) {
                                GenericWrapperElement gwe = null;
                                XFTSuperiorReference  ref = (XFTSuperiorReference) field.getXFTReference();

                                //FIND EXTENSION LEVEL FOR UPDATE
                                Iterator refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext()) {
                                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
                                    GenericWrapperField      f    = GenericWrapperElement.GetFieldForXMLPath(toRemove.getXSIType() + "." + spec.getLocalCol());
                                    assert f != null;
                                    gwe = f.getParentElement().getGenericXFTElement();
                                }

                                //FIND CORRECT EXTENSION LEVEL ITEM
                                XFTItem updateItem = item;
                                assert gwe != null;
                                if (!gwe.getFullXMLName().equalsIgnoreCase(updateItem.getXSIType())) {
                                    updateItem = updateItem.getExtensionItem(gwe.getFullXMLName());
                                    if (updateItem == null) {
                                        updateItem = item;
                                    }
                                }

                                if (!noHistory) {
                                    try {
                                        StoreHistoryAndMeta(updateItem, user, null, cache);
                                    } catch (ElementNotFoundException e) {
                                        if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                                            throw e;
                                        }
                                    }
                                }

                                //UPDATE REFERENCE
                                refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext()) {
                                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
                                    updateItem.setProperty(updateItem.getXSIType() + "." + spec.getLocalCol(), "NULL");
                                }


                                //UPDATE ITEM
                                UpdateItem(updateItem, user, cache, false);
                            }
                            item.removeItem(toRemove);
                        } else {
                            if (!parentDeleted) {
                                GenericWrapperElement gwe = null;
                                XFTSuperiorReference  ref = (XFTSuperiorReference) field.getXFTReference();

                                //FIND EXTENSION LEVEL FOR UPDATE
                                Iterator refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext()) {
                                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
                                    GenericWrapperField      f    = GenericWrapperElement.GetFieldForXMLPath(toRemove.getXSIType() + "." + spec.getLocalCol());
                                    assert f != null;
                                    gwe = f.getParentElement().getGenericXFTElement();
                                }

                                //FIND CORRECT EXTENSION LEVEL ITEM
                                XFTItem updateItem = item;
                                assert gwe != null;
                                if (!gwe.getFullXMLName().equalsIgnoreCase(updateItem.getXSIType())) {
                                    updateItem = updateItem.getExtensionItem(gwe.getFullXMLName());
                                    if (updateItem == null) {
                                        updateItem = item;
                                    }
                                }

                                if (!noHistory) {
                                    try {
                                        StoreHistoryAndMeta(updateItem, user, null, cache);
                                    } catch (ElementNotFoundException e) {
                                        if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                                            throw e;
                                        }
                                    }
                                }

                                //UPDATE REFERENCE
                                refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext()) {
                                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
                                    updateItem.setProperty(updateItem.getXSIType() + "." + spec.getLocalCol(), "NULL");
                                }


                                //UPDATE ITEM
                                UpdateItem(updateItem, user, cache, false);
                            }
                            item.removeItem(toRemove);

                            DeleteItem(toRemove, user, cache, noHistory, field.isPossibleLoop());
                        }
                    }
                } else {
                    if (field.isMultiple()) {
                        if (field.getXFTReference().isManyToMany()) {
                            XFTManyToManyReference ref     = (XFTManyToManyReference) field.getXFTReference();
                            ArrayList              values  = new ArrayList();
                            Iterator               refCols = ref.getMappingColumnsForElement(foreign).iterator();
                            while (refCols.hasNext()) {
                                XFTMappingColumn spec = (XFTMappingColumn) refCols.next();
                                Object           o    = toRemove.getProperty(spec.getForeignKey().getXMLPathString(toRemove.getXSIType()));
                                ArrayList        al   = new ArrayList();
                                al.add(spec.getLocalSqlName());
                                al.add(DBAction.ValueParser(o, spec.getXmlType().getLocalType(), true));
                                values.add(al);
                            }

                            refCols = ref.getMappingColumnsForElement(root).iterator();
                            while (refCols.hasNext()) {
                                XFTMappingColumn spec = (XFTMappingColumn) refCols.next();
                                Object           o    = item.getProperty(spec.getForeignKey().getXMLPathString(item.getXSIType()));
                                ArrayList        al   = new ArrayList();
                                al.add(spec.getLocalSqlName());
                                al.add(DBAction.ValueParser(o, spec.getXmlType().getLocalType(), true));
                                values.add(al);
                            }

                            if (values.size() > 1) {
                                DBAction.DeleteMappings(ref, root.getDbName(), values, login, cache, noHistory);

                                DeleteItem(toRemove, user, cache, noHistory, field.isPossibleLoop());
                                item.removeItem(toRemove);
                            } else {
                                throw new Exception("Failed to identify both ids for the mapping table.");
                            }
                        } else {

                            DeleteItem(toRemove, user, cache, noHistory, field.isPossibleLoop());
                            item.removeItem(toRemove);
                        }
                    } else {

                        if (!parentDeleted) {
                            if (field.isRequired()) {
                                throw new Exception("Unable to delete REQUIRED " + toRemove.getXSIType() + ". The entire parent " + item.getXSIType() + " must be deleted.");
                            }

                            GenericWrapperElement gwe = null;
                            XFTSuperiorReference  ref = (XFTSuperiorReference) field.getXFTReference();

                            //FIND EXTENSION LEVEL FOR UPDATE
                            Iterator refsCols = ref.getKeyRelations().iterator();
                            while (refsCols.hasNext()) {
                                XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
                                GenericWrapperField      f    = GenericWrapperElement.GetFieldForXMLPath(toRemove.getXSIType() + "." + spec.getLocalCol());
                                assert f != null;
                                gwe = f.getParentElement().getGenericXFTElement();
                            }

                            //FIND CORRECT EXTENSION LEVEL ITEM
                            XFTItem updateItem = item;
                            assert gwe != null;
                            if (!gwe.getFullXMLName().equalsIgnoreCase(updateItem.getXSIType())) {
                                updateItem = updateItem.getExtensionItem(gwe.getFullXMLName());
                                if (updateItem == null) {
                                    updateItem = item;
                                }
                            }

                            if (!noHistory) {
                                try {
                                    StoreHistoryAndMeta(updateItem, user, null, cache);
                                } catch (ElementNotFoundException e) {
                                    if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                                        throw e;
                                    }
                                }
                            }

                            //UPDATE REFERENCE
                            refsCols = ref.getKeyRelations().iterator();
                            while (refsCols.hasNext()) {
                                XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
                                updateItem.setProperty(updateItem.getXSIType() + "." + spec.getLocalCol(), "NULL");
                            }


                            //UPDATE ITEM
                            UpdateItem(updateItem, user, cache, false);
                        }

                        item.removeItem(toRemove);

                        DeleteItem(toRemove, user, cache, noHistory, field.isPossibleLoop());
                    }
                }
            }
        } catch (ElementNotFoundException e) {
            log.error("The specified element was not found: {}", e.ELEMENT, e);
        } catch (XFTInitException e) {
            log.error("There was an error initializing XFT.", e);
        } catch (FieldNotFoundException e) {
            log.error("The specified field was not found: {}", e.FIELD, e);
        } catch (Exception e) {
            log.error("An unknown error occurred.", e);
            throw e;
        }
    }

    private static void DeleteMappings(XFTManyToManyReference mapping, String dbName, ArrayList values, String login, DBItemCache cache, boolean noHistory) {
        if (values.size() > 0) {
            PoolDBUtils con;
            try {
                con = new PoolDBUtils();

                StringBuilder query;
                int           counter;
                Iterator      keys;
                if (!noHistory) {
                    query = new StringBuilder("INSERT INTO ").append(mapping.getHistoryTableName());
                    query.append(" (");

                    counter = 0;
                    keys = values.iterator();
                    while (keys.hasNext()) {
                        ArrayList key = (ArrayList) keys.next();

                        if (counter++ != 0) {
                            query.append(", ");
                        }
                        query.append(key.get(0));
                    }
                    query.append(") VALUES (");

                    counter = 0;
                    keys = values.iterator();
                    while (keys.hasNext()) {
                        ArrayList key = (ArrayList) keys.next();

                        if (counter++ != 0) {
                            query.append(", ");
                        }
                        query.append(key.get(1));
                    }
                    query.append(");");

                    log.debug(query.toString());
                    con.updateItem(query.toString(), dbName, login, cache);
                }

                query = new StringBuilder("DELETE FROM " + mapping.getMappingTable() + " WHERE ");
                counter = 0;
                keys = values.iterator();
                while (keys.hasNext()) {
                    ArrayList key = (ArrayList) keys.next();

                    if (counter++ != 0) {
                        query.append(" AND ");
                    }
                    query.append(key.get(0)).append("=").append(key.get(1));
                }

                log.debug(query.toString());
                con.updateItem(query.toString(), dbName, login, cache);
                log.info("{} removed.", mapping.getMappingTable());
            } catch (ClassNotFoundException e) {
                log.error("The requested class definition was not found.", e);
            } catch (SQLException e) {
                log.warn("There was an error executing the database query.", e);
            } catch (Exception e) {
                log.error("An unknown error occurred.", e);
            }
        }
    }

    /**
     *
     */
    public static void DeleteItem(XFTItem item, UserI user, EventMetaI c, boolean skipTriggers) throws Exception {
        DBItemCache cache = new DBItemCache(user, c);
        DeleteItem(item, user, cache, false, false);

        String  username     = null;
        Integer xdat_user_id = null;
        if (user != null) {
            username = user.getUsername();
            xdat_user_id = user.getID();
        }

        if (!cache.getDBTriggers().contains(item, false)) {
            cache.getDBTriggers().add(item);
        }

        if (!skipTriggers) {
            PerformUpdateTriggers(cache, username, xdat_user_id, false);
        }

        final PoolDBUtils con = new PoolDBUtils();
        con.sendBatch(cache, item.getDBName(), username);

        //PerformUpdateTriggers(cache, username,xdat_user_id,false); This shouldn't be necessary TO
    }

    /**
     *
     */
    @SuppressWarnings("unused")
    public static void CleanDeleteItem(XFTItem item, UserI user, EventMetaI c) throws Exception {
        DBItemCache cache = new DBItemCache(user, c);
        DeleteItem(item, user, cache, true, false);

        PoolDBUtils con;
        String      username     = null;
        Integer     xdat_user_id = null;
        if (user != null) {
            username = user.getUsername();
            xdat_user_id = user.getID();
        }
        if (!cache.getDBTriggers().contains(item, false)) {
            cache.getDBTriggers().add(item);
        }
        PerformUpdateTriggers(cache, username, xdat_user_id, false);
        con = new PoolDBUtils();
        con.sendBatch(cache, item.getDBName(), username);
    }

    private static void DeleteItem(XFTItem item, UserI user, DBItemCache cache, boolean cleanHistory, boolean possibleLoop) throws Exception {
        String login = null;
        if (user != null) {
            login = user.getUsername();
        }

        if (user != null) {
            if (!Permissions.canDelete(user, item)) {
                throw new org.nrg.xdat.exceptions.IllegalAccessException("Unable to delete " + item.getXSIType());
            }
        }

        if (!cleanHistory) {
            try {
                StoreHistoryAndMeta(item, user, null, cache);
            } catch (Exception e) {
                if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                    throw e;
                }
            }
        }

        XFTItem extensionItem = null;
        if (item.getGenericSchemaElement().isExtension()) {
            extensionItem = item.getExtensionItem();
        }

        //DELETE RELATIONS
        //allowExtension set to true to delete xnat:demographicData when deleting xnat:subjectData (otherwise only deletes xnat:abstactDemographicdata)
        item = item.getCurrentDBVersion(false, false);

        if (item != null) {
            for (final GenericWrapperField ref : item.getGenericSchemaElement().getReferenceFields(true)) {
                final String xmlPath = ref.getXMLPathString(item.getXSIType());

                if (!ref.getPreventLoop() || !possibleLoop) {
                    for (final XFTItem child : item.getChildItems(xmlPath)) {
                        if (child.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                            if (extensionItem == null || (!XFTItem.CompareItemsByPKs(child, extensionItem))) {
                                DBAction.RemoveItemReference(item, xmlPath, child.getCurrentDBVersion(true, true), user, cache, true, cleanHistory);
                            }
                        }
                    }
                }
            }

            if (cleanHistory) {
                //CLEAN HISTORY
                if (item.hasHistory()) {
                    ItemCollection items = item.getHistory();
                    Iterator       iter  = items.getItemIterator();
                    while (iter.hasNext()) {
                        ItemI history = (ItemI) iter.next();
                        DeleteHistoryItem(history.getItem(), item.getGenericSchemaElement(), login, cache);
                    }
                }
            }


            try {
                // MARK META_DATA to DELETED
                if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                    Object metaDataId = item.getProperty(item.getGenericSchemaElement().getMetaDataFieldName().toLowerCase());
                    if (metaDataId != null) {
                        XFTItem oldMeta = XFTItem.NewItem(item.getGenericSchemaElement().getFullXMLName() + "_meta_data", null);
                        oldMeta.setFieldValue("meta_data_id", metaDataId);
                        oldMeta.setFieldValue("status", ViewManager.DELETED);
                        UpdateItem(oldMeta, user, cache, false);
                    }
                }
            } catch (Exception e) {
                log.error("", e);
            }

            //DELETE
            DeleteItem(item, login, cache);

            if (extensionItem != null) {
                DeleteItem(extensionItem, user, cache, cleanHistory, false);
            }
        }
    }

    @SuppressWarnings("UnusedParameters")
    private static void DeleteHistoryItem(XFTItem history, GenericWrapperElement parentElement, String login, DBItemCache cache) throws Exception {
        //DELETE OTHER HISTORY ITEMS

        DeleteItem(history, login, cache);
    }

    //Added for backward compatibility - MR
    @SuppressWarnings("unused")
    public static void DeleteItem(XFTItem item, UserI user, EventMetaI c) throws Exception {
        DeleteItem(item, user, c, false);
    }

    private static void DeleteItem(XFTItem item, String login, DBItemCache cache) throws Exception {
        StringBuilder         query   = new StringBuilder("DELETE FROM ");
        GenericWrapperElement element = item.getGenericSchemaElement();

        query.append(element.getSQLName()).append(" WHERE ");

        Hashtable   props   = (Hashtable) item.getPkValues();
        Enumeration enumer  = props.keys();
        int         counter = 0;
        while (enumer.hasMoreElements()) {
            String key   = (String) enumer.nextElement();
            Object value = props.get(key);
            if (!(value instanceof XFTItem)) {
                GenericWrapperField field = element.getNonMultipleDataField(key);
                if (counter++ == 0) {
                    query.append(key).append("=").append(ValueParser(value, field, true));
                } else {
                    query.append(", ").append(key).append("=").append(ValueParser(value, field, true));
                }
            }
        }
        query.append(";");

        log.debug(query.toString());
        try {
            PoolDBUtils con = new PoolDBUtils();
            con.updateItem(query.toString(), element.getDbName(), login, cache);
            cache.handlePostModificationAction(item, "delete");
            log.info("{} Removed.", element.getFullXMLName());
        } catch (ClassNotFoundException e) {
            log.warn("Class missing", e);
        } catch (SQLException e) {
            log.warn("Error accessing database", e);
        } catch (Exception e) {
            log.warn("Unknown exception occurred", e);
        }
    }

    public static void InsertMetaDatas() {
        try {
            // Migration: Parameterize all array lists, hashtables, etc.
            for (final Object object : XFTManager.GetInstance().getAllElements()) {
                GenericWrapperElement e = (GenericWrapperElement) object;
                if (e.getAddin().equals("")) {
                    InsertMetaDatas(e.getXSIType());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void InsertMetaDatas(String elementName) {
        try {
            GenericWrapperElement element = GenericWrapperElement.GetElement(elementName);

            DBItemCache cache = new DBItemCache(null, null);
            log.info("Update Meta-Data: {}", element.getFullXMLName());
            ArrayList<GenericWrapperField> keys      = element.getAllPrimaryKeys();
            StringBuilder                  keyString = new StringBuilder();
            for (GenericWrapperField key : keys) {
                if (!keyString.toString().equals("")) {
                    keyString.append(",");
                }
                keyString.append(key.getSQLName());
            }
            String   query = "SELECT " + keyString + " FROM " + element.getSQLName() + " WHERE " + element.getMetaDataFieldName() + " IS NULL;";
            XFTTable t     = XFTTable.Execute(query, element.getDbName(), null);
            if (t.size() > 0) {
                log.info("{} missing {} meta rows.", element.getFullXMLName(), t.size());
                t.resetRowCursor();
                while (t.hasMoreRows()) {
                    Object[] row  = t.nextRow();
                    XFTItem  meta = XFTItem.NewMetaDataElement(null, element.getXSIType(), false, Calendar.getInstance().getTime(), null);
                    StoreItem(meta, null, true, false, false, false, cache, null, true);
                    keyString = new StringBuilder();
                    int count = 0;
                    for (GenericWrapperField key : keys) {
                        if (!keyString.toString().equals("")) {
                            keyString.append(" AND ");
                        }
                        keyString.append(key.getSQLName()).append("=").append(DBAction.ValueParser(row[count++], key, true));
                    }
                    if (meta != null) {
                        String st = "UPDATE " + element.getSQLName() + " SET " + element.getMetaDataFieldName() + "=" + meta.getProperty("meta_data_id") + " WHERE " + keyString;
                        cache.addStatement(st);
                    }
                }
            } else {
                log.info("{} has all meta rows.", element.getFullXMLName());
            }
            if (!cache.getSQL().equals("") && !cache.getSQL().equals("[]")) {
                try {
                    PoolDBUtils con = new PoolDBUtils();
                    con.sendBatch(cache, element.getDbName(), null);

                } catch (SQLException e) {
                    log.warn("Error accessing database", e);
                } catch (Exception e) {
                    log.warn("Unknown exception occurred", e);
                }
            }
        } catch (Exception e) {
            log.error("Failed inserting metadata", e);
        }
    }


    public static void AdjustSequences() {
        // TODO: Convert this to use adjust_sequences() database function in sequence-adjustment-functions.sql
        long startTime = Calendar.getInstance().getTimeInMillis();
        try {
            final String   defaultDBName = PoolDBUtils.getDefaultDBName();
            final XFTTable tables        = XFTTable.Execute(QUERY_FIND_SEQLESS_TABLES, defaultDBName, null);
            if (tables.size() > 0) {
                for (Object table : tables.convertColumnToArrayList("table_name")) {
                    log.error("Preparing to convert table '{}' to use sequence for default value.", table);
                    final List<String> queries = Arrays.asList(String.format(QUERY_CREATE_SEQUENCE, table),
                                                               String.format(QUERY_SET_ID_DEFAULT, table, table),
                                                               String.format(QUERY_SET_ID_NOT_NULL, table),
                                                               String.format(QUERY_SET_SEQUENCE_OWNER, table, table));
                    log.error("Queries prepared for conversion:");
                    for (final String query : queries) {
                        log.error(" *** {}", query);
                    }
                    PoolDBUtils.ExecuteBatch(queries, defaultDBName, null);

                    final String querySequenceStart = String.format(QUERY_GET_SEQUENCE_START, table);
                    final long   start              = ObjectUtils.defaultIfNull((Long) PoolDBUtils.ReturnStatisticQuery(querySequenceStart, "value", defaultDBName, null), 1L);
                    final String querySequenceValue = String.format(QUERY_SET_SEQUENCE_VALUE, table, start);
                    log.error("Ran the query '{}' and got the value {}. Now preparing to run the query: {}", querySequenceStart, start, querySequenceValue);
                    PoolDBUtils.ReturnStatisticQuery(querySequenceValue, "value", defaultDBName, null);
                }
            }

            for (final Object o1 : XFTManager.GetInstance().getAllElements()) {
                try {
                    GenericWrapperElement input = (GenericWrapperElement) o1;
                    if (input.isAutoIncrement() && !input.getSQLName().equalsIgnoreCase("xdat_history") && !input.getSQLName().equalsIgnoreCase("xdat_meta_data")) {
                        adjustSequence(input.getSequenceName(), input.getAllPrimaryKeys().get(0).getSQLName(), input.getSQLName(), input.getDbName());
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }

            for (final Object object : XFTReferenceManager.GetInstance().getUniqueMappings()) {
                try {
                    final XFTManyToManyReference map = (XFTManyToManyReference) object;
                    adjustSequence(DBAction.getSequenceName(map.getMappingTable(), map.getMappingTable() + "_id", map.getElement1().getDbName()), map.getMappingTable() + "_id", map.getMappingTable(), map.getElement1().getDbName());
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        log.info("Finished db sequence check {} ms", Calendar.getInstance().getTimeInMillis() - startTime);
    }

    //be very careful about modifying the contents of this method.  Its easy to introduce bugs and not realize it here.
    //It reviews the number of rows in the table and makes sure the sequence is set higher then that.
    //the logic ends up changing the sequence to aggressively when the row count is 1.  I tried to fix it, but it introduced lots of bugs.
    //so, this small issue is tolerable.  If you mess with it, test installing a new server, and creating some stuff.
    private static void adjustSequence(final String sequenceName, final String column, final String table, final String dbName) throws Exception {
        final int maxValue  = ((Number) ObjectUtils.defaultIfNull(PoolDBUtils.ReturnStatisticQuery("SELECT MAX(" + column + ") AS MAX_VALUE from " + table, "MAX_VALUE", dbName, null), 0)).intValue();
        final int nextValue = ((Number) ObjectUtils.defaultIfNull(PoolDBUtils.ReturnStatisticQuery(getSequenceQuery(sequenceName), "LAST_COUNT", dbName, null), -1)).intValue();

        if (nextValue == -1) {
            log.info("Sequence {} has not yet been used, but rows exist in the database. The largest existing value is {}, so adjusting sequence to the next value after that.", table, maxValue);
            PoolDBUtils.ExecuteNonSelectQuery("SELECT setval('" + sequenceName + "'," + (maxValue + 1) + ")", dbName, null);
        } else if (maxValue >= nextValue) {
            log.info("Sequence {} would issue {} as the next value, but there are already rows with larger values. The largest existing value is {}, so adjusting sequence to the next value after that.", table, nextValue, maxValue);
            PoolDBUtils.ExecuteNonSelectQuery("SELECT setval('" + sequenceName + "'," + (maxValue + 1) + ")", dbName, null);
        } else {
            log.debug("Checked sequence {}: the next sequence value is {} while the larger existing value is {}, so no action is required.", sequenceName, nextValue, maxValue);
        }
    }

    private static String getSequenceQuery(final String sequenceName) throws SQLException {
        if (PoolDBUtils.getDatabaseVersion() < 10.0) {
            return String.format(QUERY_PRE_PG10_SEQUENCE, sequenceName);
        }
        final String[] atoms = StringUtils.split(sequenceName, ".");
        switch (atoms.length) {
            case 0:
                throw new SQLException("Invalid sequence name, empty!");

            case 1:
                return String.format(QUERY_POST_PG10_SEQUENCE, "public", atoms[0]);

            case 2:
                return String.format(QUERY_POST_PG10_SEQUENCE, atoms[0], atoms[1]);

            default:
                throw new SQLException("Invalid sequence name: cross-database references are not supported, so the sequence name \"" + sequenceName + " is invalid");
        }
    }

    public static Long CountInstancesOfFieldValues(String tableName, String db, CriteriaCollection al) throws Exception {
        String query = " SELECT COUNT(*) AS INSTANCE_COUNT FROM " + tableName;
        query += " WHERE " + al.getSQLClause(null) + ";";

        return (Long) PoolDBUtils.ReturnStatisticQuery(query, "INSTANCE_COUNT", db, null);
    }

    public static XFTItem SelectItemByID(String query, String functionName, GenericWrapperElement element, UserI user, boolean allowMultiples) throws Exception {
        String login = null;
        if (user != null) {
            login = user.getUsername();
        }
        String  s    = (String) PoolDBUtils.ReturnStatisticQuery(query, functionName, element.getDbName(), login);
        XFTItem item = XFTItem.PopulateItemFromFlatString(s, user);
        if (allowMultiples) {
            item.setPreLoaded(true);
        }
        return item;
    }

    public static XFTItem SelectItemByIDs(final GenericWrapperElement element, final Object[] ids, final UserI user, final boolean allowMultiples, final boolean preventLoop) throws Exception {
        final String functionName = element.getTextFunctionName();
        final String formattedIds = StringUtils.join(ids, ", ");
        final String functionCall = "SELECT " + functionName + "(" + formattedIds + ", 0," + allowMultiples + ", FALSE," + preventLoop + ");";
        log.debug("Selecting {} items by IDs [{}] with database function call: {}", element.getXSIType(), formattedIds, functionCall);
        return SelectItemByID(functionCall, functionName, element, user, allowMultiples);
    }

    public static void PerformUpdateTriggers(final DBItemCache cache, final String username, final Integer userId, final boolean asynchronous) {
        if (cache.getDBTriggers().size() < 1) {
            return;
        }

        //process modification triggers
        final long          localStartTime = Calendar.getInstance().getTimeInMillis();
        final List<String>  commands       = new ArrayList<>();
        final List<XFTItem> items          = cache.getDBTriggers().items();

        if (asynchronous) {
            commands.add("SET LOCAL synchronous_commit TO OFF;");
        }

        String dbname = null;

        for (final XFTItem item : items) {
            try {
                final GenericWrapperElement     schemaElement = item.getGenericSchemaElement();
                final List<GenericWrapperField> keys          = schemaElement.getAllPrimaryKeys();

                final String ids = StringUtils.join(Iterables.filter(Lists.transform(keys, new Function<GenericWrapperField, String>() {
                    @Nullable
                    @Override
                    public String apply(final GenericWrapperField field) {
                        final Object value = item.getProperty(field);
                        try {
                            return DBAction.ValueParser(value, field, true);
                        } catch (XFTInitException e) {
                            log.error("Error initializing XFT ", e);
                        } catch (InvalidValueException e) {
                            log.error("Invalid value \"{}\" specified for field \"{}\"", value, field, e);
                        }
                        return null;
                    }
                }), Predicates.notNull()), ", ");

                PoolDBUtils.CreateCache(dbname = item.getDBName(), username);
                commands.add(String.format("SELECT update_ls_%s(%s,%s)", schemaElement.getFormattedName(), ids, ObjectUtils.defaultIfNull(userId, "NULL").toString()));
            } catch (ElementNotFoundException e) {
                log.error("Element not found while trying to process item of type {}", e.ELEMENT, e);
            }
        }

        if (asynchronous) {
            commands.add("SET LOCAL synchronous_commit TO ON;");
        }

        //process modification triggers
        for (final String command : commands) {
            try {
                PoolDBUtils.ExecuteNonSelectQuery(command, dbname, username);
            } catch (RuntimeException ignored) {
            } catch (SQLException e) {
                log.error("An SQL exception occurred trying to execute the command: \"{}\"", command, e);
            } catch (Exception e) {
                log.error("An unexpected exception occurred trying to execute the command: \"{}\"", command, e);
            }
        }
        log.debug("Processed {} triggers in {} ms", commands.size(), Calendar.getInstance().getTimeInMillis() - localStartTime);
    }

    private static final Map<String, String> SEQUENCES = new HashMap<>();

    private static final String QUERY_FIND_SEQLESS_TABLES = "SELECT table_name FROM information_schema.columns WHERE table_name LIKE 'xhbm_%' AND column_name = 'id' AND (column_default NOT LIKE 'nextval%' OR column_default IS NULL)";
    private static final String QUERY_CREATE_SEQUENCE     = "CREATE SEQUENCE %s_id_seq";
    private static final String QUERY_SET_ID_DEFAULT      = "ALTER TABLE %s ALTER COLUMN id SET DEFAULT nextval('%s_id_seq')";
    private static final String QUERY_SET_ID_NOT_NULL     = "ALTER TABLE %s ALTER COLUMN id SET NOT NULL";
    private static final String QUERY_SET_SEQUENCE_OWNER  = "ALTER SEQUENCE %s_id_seq OWNED BY %s.id";
    private static final String QUERY_GET_SEQUENCE_START  = "SELECT (MAX(id) + 1) AS value FROM %s";
    private static final String QUERY_SET_SEQUENCE_VALUE  = "SELECT setval('%s_id_seq', %d) AS value";
    private static final String QUERY_PRE_PG10_SEQUENCE   = "SELECT CASE WHEN (start_value >= last_value) THEN start_value ELSE (last_value + 1) END AS LAST_COUNT from %s";
    private static final String QUERY_POST_PG10_SEQUENCE  = "SELECT CASE WHEN (start_value >= last_value) THEN start_value ELSE (last_value + 1) END AS LAST_COUNT FROM pg_sequences WHERE schemaname = '%s' AND sequencename = '%s'";
}
