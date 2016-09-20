/*
 * core: org.nrg.xft.db.DBAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.db;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.*;
import org.nrg.xft.references.XFTManyToManyReference;
import org.nrg.xft.references.XFTMappingColumn;
import org.nrg.xft.references.XFTReferenceI;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.references.XFTRelationSpecification;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.SecurityManagerI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.XftStringUtils;

@SuppressWarnings({"unchecked", "rawtypes"})
public class DBAction {
    private static final Logger logger = Logger.getLogger(DBAction.class);
    private static Hashtable sequences = new Hashtable();

    private static final String QUERY_FIND_SEQLESS_TABLES = "SELECT table_name FROM information_schema.columns WHERE table_name LIKE 'xhbm_%' AND column_name = 'id' AND (column_default NOT LIKE 'nextval%' OR column_default IS NULL)";
    private static final String QUERY_CREATE_SEQUENCE = "CREATE SEQUENCE %s_id_seq";
    private static final String QUERY_SET_ID_DEFAULT = "ALTER TABLE %s ALTER COLUMN id SET DEFAULT nextval('%s_id_seq')";
    private static final String QUERY_SET_ID_NOT_NULL = "ALTER TABLE %s ALTER COLUMN id SET NOT NULL";
    private static final String QUERY_SET_SEQUENCE_OWNER = "ALTER SEQUENCE %s_id_seq OWNED BY %s.id";
    private static final String QUERY_GET_SEQUENCE_START = "SELECT (MAX(id) + 1) AS value FROM %s";
    private static final String QUERY_SET_SEQUENCE_VALUE = "SELECT setval('%s_id_seq', %d) AS value";

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
     * @return updated XFTItem
     */
    public static boolean StoreItem(XFTItem item, UserI user, boolean checkForDuplicates, boolean quarantine, boolean overrideQuarantine, boolean allowItemOverwrite, SecurityManagerI securityManager, EventMetaI c) throws Exception {
        long totalStartTime = Calendar.getInstance().getTimeInMillis();
        long localStartTime = Calendar.getInstance().getTimeInMillis();
        DBItemCache cache = new DBItemCache(user, c);
        item = StoreItem(item, user, checkForDuplicates, new ArrayList(), quarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, false);

        logger.debug("prepare-sql: " + (Calendar.getInstance().getTimeInMillis() - localStartTime) + " ms");
        localStartTime = Calendar.getInstance().getTimeInMillis();

        if (!cache.getSQL().equals("") && !cache.getSQL().equals("[]")) {
            Quarantine(item, user, quarantine, overrideQuarantine, cache);

            logger.debug("quarantine-sql: " + (Calendar.getInstance().getTimeInMillis() - localStartTime) + " ms");
            localStartTime = Calendar.getInstance().getTimeInMillis();

            PoolDBUtils con;
            String username = null;
            Integer xdat_user_id = null;
            if (user != null) {
                username = user.getUsername();
                xdat_user_id = user.getID();
            }
            if (!cache.getDBTriggers().contains(item, false)) {
                cache.getDBTriggers().add(item);
            }
            if (cache.getRemoved().size() > 0) {
                if (XFT.VERBOSE)
                    System.out.println("***** " + cache.getRemoved().size() + " REMOVED ITEMS *******");
                PerformUpdateTriggers(cache, username, xdat_user_id, false);
            }
            logger.debug("pre-triggers: " + (Calendar.getInstance().getTimeInMillis() - localStartTime) + " ms");
            localStartTime = Calendar.getInstance().getTimeInMillis();
            con = new PoolDBUtils();
            con.sendBatch(cache, item.getDBName(), username);
            if (XFT.VERBOSE)
                System.out.println("Item modifications stored. " + cache.getDBTriggers().size() + " modified elements. " + cache.getStatements().size() + " SQL statements.");
            logger.debug("store: " + (Calendar.getInstance().getTimeInMillis() - localStartTime) + " ms");
            localStartTime = Calendar.getInstance().getTimeInMillis();
            PerformUpdateTriggers(cache, username, xdat_user_id, false);
            logger.debug("post-triggers: " + (Calendar.getInstance().getTimeInMillis() - localStartTime) + " ms");
            logger.debug("Total: " + (Calendar.getInstance().getTimeInMillis() - totalStartTime) + " ms");
            return true;
        } else {
            logger.info("Pre-existing item found without modifications");
            if (XFT.VERBOSE) System.out.println("Pre-existing item found without modifications");
            return false;
        }

    }

    public static void executeCache(final DBItemCache cache, final UserI user, final String db) throws Exception {
        PoolDBUtils con;
        String username = null;
        Integer xdat_user_id = null;
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
                        logger.warn("Error accessing database", e);
                    } catch (Exception e) {
                        logger.warn("Unknown exception occurred", e);
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
                    XFTItem sub = (XFTItem) al.get(0);
                    String output = "Duplicate " + item.getGenericSchemaElement().getFullXMLName() + " Found. (" + sub.getPK() + ")";
                    if (sub.getXSIType().startsWith("xdat:")) {
                        logger.debug(output);
                    } else {
                        logger.info(output);
                    }

                    if (hasOneColumnTable) {
                        DBAction.ImportNoIdentifierFKs(item, sub);
                    }

                    if (HasNewFields(sub, item, allowItemOverwrite)) {
                        logger.debug("OLD\n" + sub.toString());
                        logger.debug("NEW\n" + item.toString());
                        item = UpdateItem(sub, item, user, localQuarantine, overrideQuarantine, cache, allowItemOverwrite);
                    } else {
                        item.importNonItemFields(sub, allowItemOverwrite);
                    }

                    cache.getPreexisting().add(item);

                    if (hasOneColumnTable) {
                        sub.importNonItemFields(item, false);
                        StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));

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
                            String output = "Duplicate " + item.getGenericSchemaElement().getFullXMLName() + " Found. (" + duplicate.getPK() + ")";
                            if (duplicate.getXSIType().startsWith("xdat:")) {
                                logger.debug(output);
                            } else {
                                logger.info(output);
                            }

                            if (hasOneColumnTable) {
                                DBAction.ImportNoIdentifierFKs(item, duplicate);
                            }

                            if (HasNewFields(duplicate, item, allowItemOverwrite)) {
                                logger.debug("OLD\n" + duplicate.toString());
                                logger.debug("NEW\n" + item.toString());
                                item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, allowItemOverwrite);
                            } else {
                                item.importNonItemFields(duplicate, allowItemOverwrite);

                            }

                            cache.getPreexisting().add(item);

                            if (hasOneColumnTable) {
                                duplicate.importNonItemFields(item, false);
                                StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));

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
                                    StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));
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
                                    StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));
                                }
                            } else {
                                //            				      ITEM IS NEW
                                if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                                    GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                                    XFTItem meta = (XFTItem) item.getProperty(f);
                                    if (meta == null) {
                                        meta = XFTItem.NewMetaDataElement(user, item.getXSIType(), localQuarantine, cache.getModTime(), cache.getChangeId());
                                        StoreItem(meta, user, true, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));

                                        GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");

                                        item.setChild(ref, meta, true);

                                        for (final Object o : ref.getLocalRefNames()) {
                                            ArrayList refName = (ArrayList) o;
                                            String localKey = (String) refName.get(0);
                                            GenericWrapperField foreignKey = (GenericWrapperField) refName.get(1);
                                            Object value = meta.getProperty(foreignKey.getId());
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
                                    StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));
                                }

                                item = InsertItem(item, login, cache, false);
                            }

                        }

                    } else {
                        if (!cache.getSaved().containsByPK(item, false)) {
                            //ITEM IS NEW
                            if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                                GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                                XFTItem meta = (XFTItem) item.getProperty(f);
                                if (meta == null) {
                                    meta = XFTItem.NewMetaDataElement(user, item.getXSIType(), localQuarantine, cache.getModTime(), cache.getChangeId());
                                    StoreItem(meta, user, true, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));

                                    GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");

                                    item.setChild(ref, meta, true);

                                    for (final Object o : ref.getLocalRefNames()) {
                                        ArrayList refName = (ArrayList) o;
                                        String localKey = (String) refName.get(0);
                                        GenericWrapperField foreignKey = (GenericWrapperField) refName.get(1);
                                        Object value = meta.getProperty(foreignKey.getId());
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
                                StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));
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
                                StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));
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
                        String output = "Duplicate " + item.getGenericSchemaElement().getFullXMLName() + " Found. (" + duplicate.getPK() + ")";
                        if (duplicate.getXSIType().startsWith("xdat:")) {
                            logger.debug(output);
                        } else {
                            logger.info(output);
                        }

                        if (hasOneColumnTable) {
                            DBAction.ImportNoIdentifierFKs(item, duplicate);
                        }

                        if (HasNewFields(duplicate, item, allowItemOverwrite)) {
                            logger.debug("OLD\n" + duplicate.toString());
                            logger.debug("NEW\n" + item.toString());
                            item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, allowItemOverwrite);

                        } else {
                            item.importNonItemFields(duplicate, allowItemOverwrite);

                        }

                        cache.getPreexisting().add(item);

                        if (hasOneColumnTable) {
                            duplicate.importNonItemFields(item, false);
                            StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));

                            if (HasNewFields(duplicate, item, allowItemOverwrite)) {
                                item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, false);

                            }
                        }
                    } else {
                        if (!cache.getSaved().containsByUnique(item, false)) {
                            //ITEM IS NEW
                            if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                                GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                                XFTItem meta = (XFTItem) item.getProperty(f);
                                if (meta == null) {
                                    meta = XFTItem.NewMetaDataElement(user, item.getXSIType(), localQuarantine, cache.getModTime(), cache.getChangeId());
                                    StoreItem(meta, user, true, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));

                                    GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");

                                    item.setChild(ref, meta, true);

                                    for (final Object o : ref.getLocalRefNames()) {
                                        ArrayList refName = (ArrayList) o;
                                        String localKey = (String) refName.get(0);
                                        GenericWrapperField foreignKey = (GenericWrapperField) refName.get(1);
                                        Object value = meta.getProperty(foreignKey.getId());
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
                                StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));
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
                                StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));
                            }
                        }

                    }
                } else if (item.getGenericSchemaElement().matchByValues() && allowFieldMatching) {
                    ItemCollection temp = item.getExtFieldsMatches(true);
                    if (temp.size() > 0) {
                        isNew = false;
                        XFTItem duplicate = (XFTItem) temp.get(0);
                        String output = "Duplicate " + item.getGenericSchemaElement().getFullXMLName() + " Found. (" + duplicate.getPK() + ")";
                        if (duplicate.getXSIType().startsWith("xdat:")) {
                            logger.debug(output);
                        } else {
                            logger.info(output);
                        }

                        if (hasOneColumnTable) {
                            DBAction.ImportNoIdentifierFKs(item, duplicate);
                        }

                        if (HasNewFields(duplicate, item, allowItemOverwrite)) {
                            logger.debug("OLD\n" + duplicate.toString());
                            logger.debug("NEW\n" + item.toString());
                            item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, allowItemOverwrite);

                        } else {
                            item.importNonItemFields(duplicate, allowItemOverwrite);

                        }

                        cache.getPreexisting().add(item);

                        if (hasOneColumnTable) {
                            duplicate.importNonItemFields(item, false);
                            StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));

                            if (HasNewFields(duplicate, item, allowItemOverwrite)) {
                                item = UpdateItem(duplicate, item, user, localQuarantine, overrideQuarantine, cache, false);
                            }
                        }
                    } else {
//                		  ITEM IS NEW
                        if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                            GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                            XFTItem meta = (XFTItem) item.getProperty(f);
                            if (meta == null) {
                                meta = XFTItem.NewMetaDataElement(user, item.getXSIType(), localQuarantine, cache.getModTime(), cache.getChangeId());
                                StoreItem(meta, user, true, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));

                                GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");

                                item.setChild(ref, meta, true);

                                for (final Object o : ref.getLocalRefNames()) {
                                    ArrayList refName = (ArrayList) o;
                                    String localKey = (String) refName.get(0);
                                    GenericWrapperField foreignKey = (GenericWrapperField) refName.get(1);
                                    Object value = meta.getProperty(foreignKey.getId());
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
                            StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));
                        }

                        item = InsertItem(item, login, cache, false);
                    }
                } else {
                    //ITEM IS NEW
                    if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase("")) {
                        GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                        XFTItem meta = (XFTItem) item.getProperty(f);
                        if (meta == null) {
                            meta = XFTItem.NewMetaDataElement(user, item.getXSIType(), localQuarantine, cache.getModTime(), cache.getChangeId());
                            StoreItem(meta, user, true, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));

                            GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");

                            item.setChild(ref, meta, true);

                            for (final Object o : ref.getLocalRefNames()) {
                                ArrayList refName = (ArrayList) o;
                                String localKey = (String) refName.get(0);
                                GenericWrapperField foreignKey = (GenericWrapperField) refName.get(1);
                                Object value = meta.getProperty(foreignKey.getId());
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
                        StoreSingleRefs(item, true, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager, (!isNew));
                    }

                    item = InsertItem(item, login, cache, false);
                }
            }

            if (!itemAlreadyStoredInCache) {
                StoreMultipleRefs(item, user, localQuarantine, overrideQuarantine, allowItemOverwrite, cache, securityManager);
            }

            //StoreDuplicateRelationships(item,user,storedRelationships,localQuarantine,overrideQuarantine);
        } catch (XFTInitException e) {
            logger.error("Error initializing XFT");
            throw e;
        } catch (ElementNotFoundException e) {
            logger.error("Element not found " + e.ELEMENT);
            throw e;
        } catch (FieldNotFoundException e) {
            logger.error("Field not found " + e.FIELD + ": " + e.MESSAGE);
            throw e;
        } catch (DBPoolException e) {
            logger.error("Database connection or pooling exception occurred: " + item.getIDValue() + " " + item.getXSIType());
            throw e;
        } catch (SQLException e) {
            logger.error("An SQL error occurred [" + e.getErrorCode() + "] " + e.getSQLState());
            throw e;
        } catch (Exception e) {
            logger.error("An unknown error occurred.");
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
     * @throws Exception
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
     * @return True if the new item has any new fields, false otherwise.
     * @throws XFTInitException
     * @throws ElementNotFoundException
     * @throws InvalidValueException
     */
    private static boolean HasNewFields(XFTItem oldI, XFTItem newI, boolean allowItemOverwrite) throws XFTInitException, ElementNotFoundException, InvalidValueException {

        Hashtable newHash = newI.getProps();
        Hashtable oldHashClone = (Hashtable) oldI.getProps().clone();
        Enumeration enumer = newHash.keys();
        while (enumer.hasMoreElements()) {
            String field = (String) enumer.nextElement();
            GenericWrapperField gwf = oldI.getGenericSchemaElement().getField(field);
            Object newObject = newHash.get(field);
            if (!(newObject instanceof XFTItem)) {
                try {
                    if (oldI.getProperty(field) != null) {
                        oldHashClone.remove(field);
                        String oldValue = DBAction.ValueParser(oldI.getProperty(field), gwf, true);
                        String newValue = DBAction.ValueParser(newHash.get(field), gwf, false);
                        String type = null;
                        if (gwf != null) {
                            type = gwf.getXMLType().getLocalType();
                        }

                        if (IsNewValue(type, oldValue, newValue)) {
                            return true;
                        }
                    } else {
                        if (!(newObject.toString().equals("NULL") || newObject.toString().equals(""))) {
                            logger.info("OLD:NULL NEW:" + newObject);
                            return true;
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.error("", e);
                    logger.info("OLD:NULL NEW:" + newObject);
                    return true;
                } catch (XFTInitException e) {
                    logger.error("", e);
                    logger.info("OLD:NULL NEW:" + newObject);
                    return true;
                } catch (ElementNotFoundException e) {
                    logger.error("", e);
                    logger.info("OLD:NULL NEW:" + newObject);
                    return true;
                } catch (FieldNotFoundException e) {
                    logger.error("", e);
                    logger.info("OLD:NULL NEW:" + newObject);
                    return true;
                }
            }

        }

        if (allowItemOverwrite) {
            enumer = oldHashClone.keys();
            while (enumer.hasMoreElements()) {
                String field = (String) enumer.nextElement();
                GenericWrapperField gwf = oldI.getGenericSchemaElement().getField(field);
                if (gwf == null) {
                    if (!oldI.getGenericSchemaElement().isHiddenFK(field)) {
                        Object newObject = oldHashClone.get(field);
                        if (!(newObject instanceof XFTItem)) {
                            final String newString = newObject.toString();
                            if (StringUtils.isBlank(newString)) {
                                if (!oldI.getPkNames().contains(field)) {
                                    logger.info("NEW:NULL OLD:" + newString);
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
                                if (StringUtils.isBlank(newString)) {
                                    if (!oldI.getPkNames().contains(field)) {
                                        logger.info("NEW:NULL OLD:" + newObject);
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
                        String type = gwf.getXMLType().getLocalType();
                        if (type != null) {
                            newObject = XMLWriter.ValueParser(newObject, type);
                        }
                        if (!(newObject instanceof XFTItem)) {
                            final String newString = newObject.toString();
                            if (StringUtils.isBlank(newString)) {
                                if (!oldI.getPkNames().contains(field)) {
                                    logger.info("NEW:NULL OLD:" + newObject);
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
                logger.info("OLD:" + oldValue + " NEW:" + newValue);
                return true;
            }
        } else {
            if (type.equalsIgnoreCase("")) {
                if (!oldValue.equals(newValue)) {
                    logger.info("OLD:" + oldValue + " NEW:" + newValue);
                    return true;
                }
            } else {
                if (type.equalsIgnoreCase("integer")) {
                    Integer o1 = Integer.valueOf(oldValue);
                    Integer o2 = Integer.valueOf(newValue);

                    if (!o1.equals(o2)) {
                        logger.info("OLD:" + oldValue + " NEW:" + newValue);
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
                        logger.info("OLD:" + oldValue + " NEW:" + newValue);
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
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    } else if (oldValue.equals("'INF'") || newValue.equals("'INF'")) {
                        if (!oldValue.equals(newValue)) {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    } else {
                        Float o1 = Float.valueOf(oldValue);
                        Float o2 = Float.valueOf(newValue);

                        if (!o1.equals(o2)) {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
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
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    } else if (oldValue.contains("Infinity") || newValue.contains("Infinity")) {
                        if (!oldValue.equals(newValue)) {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    } else {
                        Double o1 = Double.valueOf(oldValue);
                        Double o2 = Double.valueOf(newValue);

                        if (!o1.equals(o2)) {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
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
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    } else if (oldValue.equals("'INF'") || newValue.equals("'INF'")) {
                        if (!oldValue.equals(newValue)) {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    } else {
                        Float o1 = Float.valueOf(oldValue);
                        Float o2 = Float.valueOf(newValue);

                        if (!o1.equals(o2)) {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    }
                } else if (type.equalsIgnoreCase("date")) {
                    try {
                        Date o1 = DateUtils.parseDate(oldValue);
                        Date o2 = DateUtils.parseDate(newValue);

                        if (!o1.equals(o2)) {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    } catch (ParseException e) {
                        logger.error("", e);
                        logger.info("OLD:" + oldValue + " NEW:" + newValue);
                        return true;
                    }
                } else if (type.equalsIgnoreCase("dateTime")) {
                    try {
                        Date o1 = DateUtils.parseDateTime(oldValue);
                        Date o2 = DateUtils.parseDateTime(newValue);

                        if (!o1.equals(o2)) {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    } catch (ParseException e) {
                        logger.error("", e);
                        logger.info("OLD:" + oldValue + " NEW:" + newValue);
                        return true;
                    }
                } else if (type.equalsIgnoreCase("time")) {
                    try {
                        Date o1 = DateUtils.parseTime(oldValue);
                        Date o2 = DateUtils.parseTime(newValue);

                        if (!o1.equals(o2)) {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    } catch (ParseException e) {
                        logger.error("", e);
                        logger.info("OLD:" + oldValue + " NEW:" + newValue);
                        return true;
                    }
                } else {
                    if (!oldValue.equals(newValue)) {
                        logger.info("OLD:" + oldValue + " NEW:" + newValue);
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

                        if (o != null && o instanceof XFTItem) {
                            XFTItem temp = (XFTItem) o;

                            //check for one column table (if found, see if root item is already stored.  If, the item already was stored and has
                            // a fk value for this table that should be used. Else, this ref item will be stored and the root item will be updated with the fk value
                            String keyName;
                            if (temp != null) {
                                if (temp.getPossibleFieldNames().size() == 1) {
                                    keyName = (String) temp.getPossibleFieldNames().get(0)[0];
                                    try {
                                        if (temp.getProperty(keyName) == null) {
                                            isNoIdentifierTable = true;
                                        }
                                    } catch (FieldNotFoundException e1) {
                                        logger.error("", e1);
                                    }
                                }

                                if (!temp.getGenericSchemaElement().hasUniqueIdentifiers()) {
                                    isNoIdentifierTable = true;
                                }
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
                                                logger.error("Element not found " + e.ELEMENT, e);
                                            } catch (XFTInitException e) {
                                                logger.error("Error initializing XFT ", e);
                                            }
                                        }
                                    } catch (XFTInitException e) {
                                        logger.error("Error initializing XFT ", e);
                                    } catch (ElementNotFoundException e) {
                                        logger.error("Element not found " + e.ELEMENT, e);
                                    } catch (FieldNotFoundException e) {
                                        logger.error("Field not found " + e.FIELD + ": " + e.MESSAGE, e);
                                    }
                                }
                            } catch (XFTInitException e) {
                                logger.error("Error initializing XFT ", e);
                            } catch (ElementNotFoundException e) {
                                logger.error("Element not found " + e.ELEMENT, e);
                            }
                        }
                    } catch (FieldNotFoundException e) {
                        logger.error("Field not found " + e.FIELD + ": " + e.MESSAGE, e);
                    }
                } catch (XFTInitException e) {
                    logger.error("Error initializing XFT ", e);
                } catch (ElementNotFoundException e) {
                    logger.error("Element not found " + e.ELEMENT, e);
                }
            }
        } catch (ElementNotFoundException e) {
            logger.error("", e);
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

                if (o != null && o instanceof XFTItem) {
                    XFTItem temp = (XFTItem) o;

                    //check for one column table (if found, see if root item is already stored.  If, the item already was stored and has
                    // a fk value for this table that should be used. Else, this ref item will be stored and the root item will be updated with the fk value
                    boolean isNoIdentifierTable = false;
                    String keyName = "";
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
                            Object value = temp.getProperty(spec.getForeignCol());
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
                        ArrayList refName = (ArrayList) ref.getLocalRefNames().get(0);
                        String localKey = (String) refName.get(0);
                        GenericWrapperField foreignKey = (GenericWrapperField) refName.get(1);
                        Object rootFKValue = item.getProperty(localKey);

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

            if (o != null && o instanceof XFTItem) {
                XFTItem temp = (XFTItem) o;

                //check for one column table (if found, see if root item is already stored.  If, the item already was stored and has
                // a fk value for this table that should be used. Else, this ref item will be stored and the root item will be updated with the fk value
                boolean isNoIdentifierTable = false;
                String keyName = "";
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
                        String fieldName = spec.getLocalCol();
                        final Object value = temp.getProperty(spec.getForeignCol());
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
                    ArrayList refName = (ArrayList) ext.getLocalRefNames().get(0);
                    String localKey = (String) refName.get(0);
                    GenericWrapperField foreignKey = (GenericWrapperField) refName.get(1);
                    Object rootFKValue = item.getProperty(localKey);

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
            GenericWrapperField ref = (GenericWrapperField) mRefs.next();
            XFTReferenceI xftRef = ref.getXFTReference();
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

                int counter = 0;
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
                        Object value = item.getProperty(spec.getForeignCol());
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

        XFTTable table = TableSearch.GetMappingTable(mapping, criteria, login);

        if (table.getNumRows() > 0) {
            logger.info("Duplicate mapping table row found in '" + mapping.getMappingTable() + "'");
            return false;
        } else {
            String query = "INSERT INTO ";

            query += mapping.getMappingTable() + " (";

            String fields = "";
            String values = "";

            Iterator props = criteria.iterator();
            int counter = 0;
            while (props.hasNext()) {
                SearchCriteria c = (SearchCriteria) props.next();
                if (c.getValue() != null) {
                    if (counter++ == 0) {
                        fields = c.getField_name();
                        values = c.valueToDB();
                    } else {
                        fields += "," + c.getField_name();
                        values += "," + c.valueToDB();
                    }
                }
            }
            query += fields + ") VALUES (" + values + ");";

            try {
                PoolDBUtils con = new PoolDBUtils();
                con.insertItem(query, mapping.getElement1().getDbName(), login, cache);
            } catch (ClassNotFoundException e) {
                logger.warn("Class missing", e);
            } catch (SQLException e) {
                logger.warn("Error accessing database", e);
            } catch (Exception e) {
                logger.warn("Unknown exception occurred", e);
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
     * @return The item after insertion.
     */
    @SuppressWarnings("ConstantConditions")
    public static XFTItem InsertItem(XFTItem item, String login, DBItemCache cache, boolean allowInvalidValues) throws Exception {
        item.modified = true;
        item.assignDefaultValues();
        String query = "INSERT INTO ";
        GenericWrapperElement element = item.getGenericSchemaElement();

        PoolDBUtils con = new PoolDBUtils();
        if (element.isAutoIncrement()) {
            if (!item.hasPK()) {
                Object key = con.getNextID(element.getDbName(), element.getSQLName(), item.getPkNames().get(0), element.getSequenceName());
                if (key != null) {
                    item.setFieldValue(item.getPkNames().get(0), key);
                }
            }
        }

        query += element.getSQLName() + " (";

        String fields = "";
        String values = "";

        Hashtable props = item.getProps();
        Enumeration enumer = props.keys();
        int counter = 0;
        while (enumer.hasMoreElements()) {
            String key = (String) enumer.nextElement();
            Object value = props.get(key);
            if (!(value instanceof XFTItem)) {
                GenericWrapperField field = element.getNonMultipleDataField(key);
                if (field == null) {
                    if (element.validateID(key)) {
                        if (counter++ == 0) {
                            fields = key;
                            values = ValueParser(value, field, allowInvalidValues);
                        } else {
                            fields += "," + key;
                            values += "," + ValueParser(value, field, allowInvalidValues);
                        }
                    }
                } else {
                    if (counter++ == 0) {
                        fields = key;
                        values = ValueParser(value, field, allowInvalidValues);
                    } else {
                        fields += "," + key;
                        values += "," + ValueParser(value, field, allowInvalidValues);
                    }
                }
            }
        }
        query += fields + ") VALUES (" + values + ");";

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
            logger.info(element.getFullXMLName() + " stored.");
        }

        return item;
    }


    public static String getSequenceName(GenericWrapperElement e) {
        if (sequences.get(e.getSQLName().toLowerCase()) == null) {
            String col_name;

            GenericWrapperField key = e.getAllPrimaryKeys().get(0);

            String newQuery = "SELECT pg_get_serial_sequence('" + e.getSQLName() + "','" + key.getSQLName() + "') AS col_name";
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
                sequences.put(e.getSQLName().toLowerCase(), col_name);
            }
        }

        return (String) sequences.get(e.getSQLName().toLowerCase());
    }

    public static String getSequenceName(String table, String key, String dbName) {
        String col_name;
        String newQuery = "SELECT pg_get_serial_sequence('" + table + "','" + key + "') AS col_name";
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
                if (overrideQuarantine)
                    q = quarantine;
                else
                    q = oldI.getGenericSchemaElement().isQuarantine(quarantine);

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
                    if (quarantine)
                        meta.setFieldValue("status", ViewManager.QUARANTINE);
                    else
                        meta.setFieldValue("status", ViewManager.ACTIVE);
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
     */
    private static XFTItem UpdateItem(XFTItem oldI, XFTItem newI, UserI user, boolean quarantine, boolean overrideQuarantine, DBItemCache cache, boolean storeNULLS) throws Exception {

        Boolean q;
        if (overrideQuarantine)
            q = quarantine;
        else
            q = oldI.getGenericSchemaElement().isQuarantine(quarantine);

        // MARK MODIFIED AS TRUE
        StoreHistoryAndMeta(oldI, newI, user, q, cache);

        //COPY PK VALUES INTO NEW ITEM
        newI.getProps().putAll(oldI.getPkValues());

        //UPDATE ITEM
        newI = UpdateItem(newI, user, cache, storeNULLS);

        cache.getModified().add(newI);

        oldI.modified = true;
        newI.modified = true;

        return newI;
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

            Hashtable pkHash = (Hashtable) oldI.getPkValues();
            Enumeration pks = pkHash.keys();
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
     * @return The item after updating.
     */
    private static XFTItem UpdateItem(XFTItem item, UserI user, DBItemCache cache, boolean storeNULLS) throws ElementNotFoundException, XFTInitException, FieldNotFoundException, InvalidValueException {
        String login = null;
        if (user != null) {
            login = user.getUsername();
        }
        String query = "UPDATE ";
        GenericWrapperElement element = item.getGenericSchemaElement();

        query += element.getSQLName() + " SET ";

        Hashtable props = item.getProps();
        int counter = 0;
        Iterator iter = item.getPossibleFieldNames().iterator();
        while (iter.hasNext()) {
            Object[] possibleField = (Object[]) iter.next();
            String key = (String) possibleField[0];
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
                                            query += key + "= NULL";
                                        } else {
                                            query += ", " + key + "= NULL";
                                        }
                                    }
                                } else {
                                    if (counter++ == 0) {
                                        query += key + "= NULL";
                                    } else {
                                        query += ", " + key + "= NULL";
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (!(value instanceof XFTItem)) {
                        GenericWrapperField field = element.getNonMultipleField(key);
                        if (counter++ == 0) {
                            query += key + "=" + ValueParser(value, field, false);
                        } else {
                            query += ", " + key + "=" + ValueParser(value, field, false);
                        }
                    }
                }
            }
        }
        query += " WHERE ";

        counter = 0;
        iter = item.getPkNames().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            GenericWrapperField field = element.getNonMultipleField(key);
            if (counter++ == 0) {
                query += key + "=" + ValueParser(item.getProperty(key), field, false) + " ";
            } else {
                query += " AND " + key + "=" + ValueParser(item.getProperty(key), field, false) + " ";
            }
        }
        query += ";";

        //logger.debug(query);
        try {
            PoolDBUtils con = new PoolDBUtils();
            con.updateItem(query, element.getDbName(), login, cache);
            cache.handlePostModificationAction(item, "update");

            if (!element.getFullXMLName().toLowerCase().startsWith("xdat")) {
                logger.info(element.getFullXMLName() + " updated.");
            }
        } catch (ClassNotFoundException e) {
            logger.warn("Class missing", e);
        } catch (SQLException e) {
            logger.warn("Error accessing database", e);
        } catch (Exception e) {
            logger.warn("Unknown exception occurred", e);
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
     * @return The string parsed from the object.
     * @throws XFTInitException
     * @throws InvalidValueException
     */
    public static String ValueParser(Object object, GenericWrapperField field, boolean allowInvalidValues) throws org.nrg.xft.exception.XFTInitException, InvalidValueException {
        if (field != null) {
            if (field.isReference()) {
                try {
                    GenericWrapperElement foreign = (GenericWrapperElement) field.getReferenceElement();
                    GenericWrapperField foreignKey = foreign.getAllPrimaryKeys().get(0);
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
     * @return The string parsed from the object.
     * @throws InvalidValueException
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
                byte[] b = (byte[]) object;
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                try {
                    baos.write(b);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (baos.toString().equalsIgnoreCase("NULL")) {
                    return "NULL";
                } else {
                    String s = baos.toString();
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
                String s = object.toString();
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
                    if (!allowInvalidValues)
                        FileUtils.ValidateUriAgainstRoot(uri, XDAT.getSiteConfigPreferences().getArchivePath(), "URI references data outside of the archive:" + uri);

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
            String username = null;
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
            logger.info("Pre-existing item found without modifications");
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

            boolean foundField = false;
            GenericWrapperField field = null;
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
                    if (cache.getPreexisting().contains(toRemove, false)) {
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
                                XFTManyToManyReference ref = (XFTManyToManyReference) field.getXFTReference();
                                ArrayList values = new ArrayList();
                                Iterator refCols = ref.getMappingColumnsForElement(foreign).iterator();
                                while (refCols.hasNext()) {
                                    XFTMappingColumn spec = (XFTMappingColumn) refCols.next();
                                    Object o = toRemove.getProperty(spec.getForeignKey().getXMLPathString(toRemove.getXSIType()));
                                    ArrayList al = new ArrayList();
                                    al.add(spec.getLocalSqlName());
                                    al.add(DBAction.ValueParser(o, spec.getXmlType().getLocalType(), true));
                                    values.add(al);
                                }

                                refCols = ref.getMappingColumnsForElement(root).iterator();
                                while (refCols.hasNext()) {
                                    XFTMappingColumn spec = (XFTMappingColumn) refCols.next();
                                    Object o = item.getProperty(spec.getForeignKey().getXMLPathString(item.getXSIType()));
                                    ArrayList al = new ArrayList();
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

                                GenericWrapperElement gwe = null;
                                XFTSuperiorReference ref = (XFTSuperiorReference) field.getXFTReference();
                                Iterator refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext()) {
                                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
                                    GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(toRemove.getXSIType() + "." + spec.getLocalCol());
                                    gwe = f.getParentElement().getGenericXFTElement();
                                }

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
                                XFTManyToManyReference ref = (XFTManyToManyReference) field.getXFTReference();
                                ArrayList values = new ArrayList();
                                ArrayList<XFTMappingColumn> forCols = ref.getMappingColumnsForElement(foreign);
                                for (XFTMappingColumn spec : forCols) {
                                    Object o = toRemove.getProperty(spec.getForeignKey().getXMLPathString(toRemove.getXSIType()));
                                    ArrayList al = new ArrayList();
                                    al.add(spec.getLocalSqlName());
                                    al.add(DBAction.ValueParser(o, spec.getXmlType().getLocalType(), true));
                                    values.add(al);
                                }

                                ArrayList<XFTMappingColumn> localCols = ref.getMappingColumnsForElement(root);
                                for (XFTMappingColumn spec : localCols) {
                                    Object o = item.getProperty(spec.getForeignKey().getXMLPathString(item.getXSIType()));
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
                                XFTSuperiorReference ref = (XFTSuperiorReference) field.getXFTReference();

                                //FIND EXTENSION LEVEL FOR UPDATE
                                Iterator refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext()) {
                                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
                                    GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(toRemove.getXSIType() + "." + spec.getLocalCol());
                                    gwe = f.getParentElement().getGenericXFTElement();
                                }

                                //FIND CORRECT EXTENSION LEVEL ITEM
                                XFTItem updateItem = item;
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
                                XFTSuperiorReference ref = (XFTSuperiorReference) field.getXFTReference();

                                //FIND EXTENSION LEVEL FOR UPDATE
                                Iterator refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext()) {
                                    XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
                                    GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(toRemove.getXSIType() + "." + spec.getLocalCol());
                                    gwe = f.getParentElement().getGenericXFTElement();
                                }

                                //FIND CORRECT EXTENSION LEVEL ITEM
                                XFTItem updateItem = item;
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
                            XFTManyToManyReference ref = (XFTManyToManyReference) field.getXFTReference();
                            ArrayList values = new ArrayList();
                            Iterator refCols = ref.getMappingColumnsForElement(foreign).iterator();
                            while (refCols.hasNext()) {
                                XFTMappingColumn spec = (XFTMappingColumn) refCols.next();
                                Object o = toRemove.getProperty(spec.getForeignKey().getXMLPathString(toRemove.getXSIType()));
                                ArrayList al = new ArrayList();
                                al.add(spec.getLocalSqlName());
                                al.add(DBAction.ValueParser(o, spec.getXmlType().getLocalType(), true));
                                values.add(al);
                            }

                            refCols = ref.getMappingColumnsForElement(root).iterator();
                            while (refCols.hasNext()) {
                                XFTMappingColumn spec = (XFTMappingColumn) refCols.next();
                                Object o = item.getProperty(spec.getForeignKey().getXMLPathString(item.getXSIType()));
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
                    } else {

                        if (!parentDeleted) {
                            if (field.isRequired()) {
                                throw new Exception("Unable to delete REQUIRED " + toRemove.getXSIType() + ". The entire parent " + item.getXSIType() + " must be deleted.");
                            }

                            GenericWrapperElement gwe = null;
                            XFTSuperiorReference ref = (XFTSuperiorReference) field.getXFTReference();

                            //FIND EXTENSION LEVEL FOR UPDATE
                            Iterator refsCols = ref.getKeyRelations().iterator();
                            while (refsCols.hasNext()) {
                                XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();
                                GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(toRemove.getXSIType() + "." + spec.getLocalCol());
                                gwe = f.getParentElement().getGenericXFTElement();
                            }

                            //FIND CORRECT EXTENSION LEVEL ITEM
                            XFTItem updateItem = item;
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
            logger.error("The specified element was not found: " + e.ELEMENT, e);
        } catch (XFTInitException e) {
            logger.error("There was an error initializing XFT.", e);
        } catch (FieldNotFoundException e) {
            logger.error("The specified field was not found: " + e.FIELD, e);
        } catch (Exception e) {
            logger.error("An unknown error occurred.", e);
            throw e;
        }
    }


    /**
     * @throws Exception
     */
    private static void DeleteMappings(XFTManyToManyReference mapping, String dbName, ArrayList values, String login, DBItemCache cache, boolean noHistory) throws Exception {
        if (values.size() > 0) {
            PoolDBUtils con;
            try {
                con = new PoolDBUtils();

                String query;
                int counter;
                Iterator keys;
                if (!noHistory) {
                    query = "INSERT INTO " + mapping.getHistoryTableName();
                    query += " (";

                    counter = 0;
                    keys = values.iterator();
                    while (keys.hasNext()) {
                        ArrayList key = (ArrayList) keys.next();

                        if (counter++ != 0) {
                            query += ", ";
                        }
                        query += key.get(0);
                    }
                    query += ") VALUES (";

                    counter = 0;
                    keys = values.iterator();
                    while (keys.hasNext()) {
                        ArrayList key = (ArrayList) keys.next();

                        if (counter++ != 0) {
                            query += ", ";
                        }
                        query += key.get(1);
                    }
                    query += ");";

                    logger.debug(query);
                    con.updateItem(query, dbName, login, cache);
                }

                query = "DELETE FROM " + mapping.getMappingTable() + " WHERE ";
                counter = 0;
                keys = values.iterator();
                while (keys.hasNext()) {
                    ArrayList key = (ArrayList) keys.next();

                    if (counter++ != 0) {
                        query += " AND ";
                    }
                    query += key.get(0) + "=" + key.get(1);
                }

                logger.debug(query);
                con.updateItem(query, dbName, login, cache);
                logger.info(mapping.getMappingTable() + " removed.");
            } catch (ClassNotFoundException e) {
                logger.error("The requested class definition was not found.", e);
            } catch (SQLException e) {
                logger.warn("There was an error executing the database query.", e);
            } catch (Exception e) {
                logger.error("An unknown error occurred.", e);
            }
        }
    }

    /**
     */
    public static void DeleteItem(XFTItem item, UserI user, EventMetaI c, boolean skipTriggers) throws Exception {
        DBItemCache cache = new DBItemCache(user, c);
        DeleteItem(item, user, cache, false, false);

        String username = null;
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
     */
    @SuppressWarnings("unused")
    public static void CleanDeleteItem(XFTItem item, UserI user, EventMetaI c) throws Exception {
        DBItemCache cache = new DBItemCache(user, c);
        DeleteItem(item, user, cache, true, false);

        PoolDBUtils con;
        String username = null;
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
                    Iterator iter = items.getItemIterator();
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
                logger.error("", e);
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
    public static void DeleteItem(XFTItem item, UserI user, EventMetaI c) throws SQLException,Exception{
    	DeleteItem(item, user, c, false);
    }
    
    private static void DeleteItem(XFTItem item, String login, DBItemCache cache) throws Exception {
        String query = "DELETE FROM ";
        GenericWrapperElement element = item.getGenericSchemaElement();

        query += element.getSQLName() + " WHERE ";

        Hashtable props = (Hashtable) item.getPkValues();
        Enumeration enumer = props.keys();
        int counter = 0;
        while (enumer.hasMoreElements()) {
            String key = (String) enumer.nextElement();
            Object value = props.get(key);
            if (!(value instanceof XFTItem)) {
                GenericWrapperField field = element.getNonMultipleDataField(key);
                if (counter++ == 0) {
                    query += key + "=" + ValueParser(value, field, true);
                } else {
                    query += ", " + key + "=" + ValueParser(value, field, true);
                }
            }
        }
        query += ";";

        logger.debug(query);
        try {
            PoolDBUtils con = new PoolDBUtils();
            con.updateItem(query, element.getDbName(), login, cache);
            cache.handlePostModificationAction(item, "delete");
            logger.info(element.getFullXMLName() + " Removed.");
        } catch (ClassNotFoundException e) {
            logger.warn("Class missing", e);
        } catch (SQLException e) {
            logger.warn("Error accessing database", e);
        } catch (Exception e) {
            logger.warn("Unknown exception occurred", e);
        }
    }

    public static void InsertMetaDatas() {
        try {
            // Migration: Parameterize all array lists, hashtables, etc.
            for (final Object object : XFTManager.GetInstance().getAllElements()) {
                GenericWrapperElement e = (GenericWrapperElement) object;
                if (e.getAddin().equals(""))
                    InsertMetaDatas(e.getXSIType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void InsertMetaDatas(String elementName) {
        try {
            GenericWrapperElement element = GenericWrapperElement.GetElement(elementName);

            DBItemCache cache = new DBItemCache(null, null);
            logger.info("Update Meta-Data: " + element.getFullXMLName());
            ArrayList<GenericWrapperField> keys = element.getAllPrimaryKeys();
            String keyString = "";
            for (GenericWrapperField key : keys) {
                if (!keyString.equals("")) {
                    keyString += ",";
                }
                keyString += key.getSQLName();
            }
            String query = "SELECT " + keyString + " FROM " + element.getSQLName() + " WHERE " + element.getMetaDataFieldName() + " IS NULL;";
            XFTTable t = XFTTable.Execute(query, element.getDbName(), null);
            if (t.size() > 0) {
                System.out.println(element.getFullXMLName() + " missing " + t.size() + " meta rows.");
                t.resetRowCursor();
                while (t.hasMoreRows()) {
                    Object[] row = t.nextRow();
                    XFTItem meta = XFTItem.NewMetaDataElement(null, element.getXSIType(), false, Calendar.getInstance().getTime(), null);
                    StoreItem(meta, null, true, false, false, false, cache, null, true);
                    keyString = "";
                    int count = 0;
                    for (GenericWrapperField key : keys) {
                        if (!keyString.equals("")) {
                            keyString += " AND ";
                        }
                        keyString += key.getSQLName() + "=" + DBAction.ValueParser(row[count++], key, true);
                    }
                    if (meta != null) {
                        String st = "UPDATE " + element.getSQLName() + " SET " + element.getMetaDataFieldName() + "=" + meta.getProperty("meta_data_id") + " WHERE " + keyString;
                        cache.addStatement(st);
                    }
                }
            } else {
                System.out.println(element.getFullXMLName() + " has all meta rows.");
            }
            if (!cache.getSQL().equals("") && !cache.getSQL().equals("[]")) {
                try {
                    PoolDBUtils con = new PoolDBUtils();
                    con.sendBatch(cache, element.getDbName(), null);

                } catch (SQLException e) {
                    logger.warn("Error accessing database", e);
                } catch (Exception e) {
                    logger.warn("Unknown exception occurred", e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed inserting metadata", e);
        }
    }


    public static void AdjustSequences() {
        long startTime = Calendar.getInstance().getTimeInMillis();
        try {
            XFTTable tables = XFTTable.Execute(QUERY_FIND_SEQLESS_TABLES, PoolDBUtils.getDefaultDBName(), null);
            if (tables.size() > 0) {
                for (Object table : tables.convertColumnToArrayList("table_name")) {
                    try {
                        logger.error("Preparing to convert table " + table + " to use sequence for default value.");
                        ArrayList<String> queries = new ArrayList<>();
                        queries.add(String.format(QUERY_CREATE_SEQUENCE, table));
                        queries.add(String.format(QUERY_SET_ID_DEFAULT, table, table));
                        queries.add(String.format(QUERY_SET_ID_NOT_NULL, table));
                        queries.add(String.format(QUERY_SET_SEQUENCE_OWNER, table, table));
                        logger.error("Queries prepared for conversion:");
                        for (String query : queries) {
                            logger.error(" *** " + query);
                        }
                        PoolDBUtils.ExecuteBatch(queries, PoolDBUtils.getDefaultDBName(), null);
                        Long start = (Long) PoolDBUtils.ReturnStatisticQuery(String.format(QUERY_GET_SEQUENCE_START, table), "value", PoolDBUtils.getDefaultDBName(), null);
                        if (start == null) {
                            start = 1L;
                        }
                        logger.error("Ran the query " + String.format(QUERY_GET_SEQUENCE_START, table) + " and got the value " + start);
                        logger.error("Now preparing to run the query: " + String.format(QUERY_SET_SEQUENCE_VALUE, table, start));
                        PoolDBUtils.ReturnStatisticQuery(String.format(QUERY_SET_SEQUENCE_VALUE, table, start), "value", PoolDBUtils.getDefaultDBName(), null);
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
            }

            ArrayList dbs = new ArrayList();
            for (final Object o1 : XFTManager.GetInstance().getAllElements()) {
                try {
                    GenericWrapperElement input = (GenericWrapperElement) o1;
                    if (input.isAutoIncrement() && !input.getSQLName().equalsIgnoreCase("xdat_history") && !input.getSQLName().equalsIgnoreCase("xdat_meta_data")) {
                        String dbName = input.getDbName();
                        if (!dbs.contains(dbName)) {
                            dbs.add(dbName);
                        }
                        GenericWrapperField pk = input.getAllPrimaryKeys().get(0);
                        String sequenceName = input.getSequenceName();
                        adjustSequence(sequenceName, pk.getSQLName(), input.getSQLName(), input.getDbName());
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }

            }

            for (Object object : XFTReferenceManager.GetInstance().getUniqueMappings()) {
                try {
                    XFTManyToManyReference map = (XFTManyToManyReference) object;
                    String sequenceName = DBAction.getSequenceName(map.getMappingTable(), map.getMappingTable() + "_id", map.getElement1().getDbName());
                    adjustSequence(sequenceName, map.getMappingTable() + "_id", map.getMappingTable(), map.getElement1().getDbName());

                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        if (XFT.VERBOSE) {
            System.out.println("Finished db sequence check " + (Calendar.getInstance().getTimeInMillis() - startTime) + "ms");
        }
    }

    //be very careful about modifying the contents of this method.  Its easy to introduce bugs and not realize it here.
    //It reviews the number of rows in the table and makes sure the sequence is set higher then that.
    //the logic ends up changing the sequence to aggressively when the row count is 1.  I tried to fix it, but it introduced lots of bugs.
    //so, this small issue is tolerable.  If you mess with it, test installing a new server, and creating some stuff.
    private static void adjustSequence(String sequenceName, String column, String table, String dbName) throws Exception {
        Object numRows = PoolDBUtils.ReturnStatisticQuery("SELECT MAX(" + column + ") AS MAX_COUNT from " + table, "MAX_COUNT", dbName, null);
        Object nextValue = PoolDBUtils.ReturnStatisticQuery("SELECT CASE WHEN (start_value >= last_value) THEN start_value ELSE (last_value + 1) END AS LAST_COUNT from " + sequenceName, "LAST_COUNT", dbName, null);
        if (numRows == null) {
            numRows = 0;
        }

        if (nextValue == null) {
            logger.info("Adjusting missing sequence (" + table + ");");
            PoolDBUtils.ExecuteNonSelectQuery("SELECT setval('" + sequenceName + "'," + (((Number) numRows).intValue() + 1) + ")", dbName, null);
        } else {
            Number i1 = (Number) numRows;
            Number i2 = (Number) nextValue;
            if (i1.intValue() >= i2.intValue()) {
                logger.info(String.format("Adjusting invalid sequence (%s) from %s to %s (%s max_row)", table, i2, (i1.intValue() + 1), i1));
                PoolDBUtils.ExecuteNonSelectQuery("SELECT setval('" + sequenceName + "'," + (i1.intValue() + 1) + ")", dbName, null);
            }
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
        String s = (String) PoolDBUtils.ReturnStatisticQuery(query, functionName, element.getDbName(), login);
        XFTItem item = XFTItem.PopulateItemFromFlatString(s, user);
        if (allowMultiples) {
            item.setPreLoaded(true);
        }
        return item;
    }

    public static XFTItem SelectItemByIDs(GenericWrapperElement element, Object[] ids, UserI user, boolean allowMultiples, boolean preventLoop) throws Exception {
        String functionName = element.getTextFunctionName();
        String functionCall = "SELECT " + functionName + "(";
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) functionCall += ",";
            functionCall += ids[i];
        }
        functionCall += ",0," + allowMultiples + ",FALSE," + preventLoop + ");";

        return SelectItemByID(functionCall, functionName, element, user, allowMultiples);
    }

    public static void PerformUpdateTriggers(DBItemCache cache, String userName, Integer xdat_user_id, boolean asynchronous) {
        long localStartTime = Calendar.getInstance().getTimeInMillis();
        ArrayList<String> cmds = new ArrayList<>();
        try {
            //process modification triggers
            if (cache.getDBTriggers().size() > 0) {

                ArrayList<XFTItem> items = cache.getDBTriggers().items();

                if (asynchronous) cmds.add("SET LOCAL synchronous_commit TO OFF;");

                String dbname = null;

                for (XFTItem mod : items) {
                    int count = 0;
                    String ids = "";
                    ArrayList keys = mod.getGenericSchemaElement().getAllPrimaryKeys();
                    for (final Object key : keys) {
                        GenericWrapperField sf = (GenericWrapperField) key;
                        Object id = mod.getProperty(sf);
                        if (count++ > 0) ids += ",";
                        ids += DBAction.ValueParser(id, sf, true);
                    }

                    dbname = mod.getDBName();

                    PoolDBUtils.CreateCache(dbname, userName);
                    cmds.add(String.format("SELECT update_ls_%s(%s,%s)", mod.getGenericSchemaElement().getFormattedName(), ids, (xdat_user_id == null) ? "NULL" : xdat_user_id));
                }

                if (asynchronous) cmds.add("SET LOCAL synchronous_commit TO ON;");

                //PoolDBUtils.ExecuteBatch(cmds, dbname, userName);

                for (String s : cmds) {
                    try {
                        PoolDBUtils.ExecuteNonSelectQuery(s, dbname, userName);
                    } catch (RuntimeException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        if (XFT.VERBOSE)
            System.out.println("triggers (" + cmds.size() + "): " + (Calendar.getInstance().getTimeInMillis() - localStartTime) + " ms");
    }
}

