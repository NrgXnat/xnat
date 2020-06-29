package org.nrg.xdat.search;
/**
 * Developer: James Ransford <ransfordj@radiologics.com>
 * <p>
 * The purpose of this class is to prevent postgresql from truncating alias names (specifically for custom variables).
 * If we have an alias that is over 63 characters long, we generate a new alias (df_alias_2) and then cache it in a
 * HashMap so that it can be retrieved later when we represent the XFTTable.
 * <p>
 * See: https://issues.xnat.org/browse/XNAT-6374
 * The issue is: I have a custom variable on the Manual QC Assessor called 'myvariablename'. If I do an advanced search
 * on MR Sessions joined with Manual QC Assessors, the alias for column name in the sql generated will end up being
 * something like: XNAT_QCMANUALASSESSORDATA_FIELD_MAP_XNAT_QCMANUALASSESSORDATA_FIELD_MAP_myvariablename
 * Since the alias is over 63 characters, postgresql will truncate the alias. We now have no idea what the new alias is,
 * so it makes it impossible to retrieve the value from the XFTTable at a later time. As a result, we end up with
 * blank values for this variable in the search results table.
 * <p>
 * In addition, if we have two custom variables myvariablename and myvariablename2, they would both get truncated in the
 * scenario above. Since postgres only keeps the first 63 characters, we end up with two aliases that are identical:
 * XNAT_QCMANUALASSESSORDATA_FIELD_MAP_XNAT_QCMANUALASSESSORDATA_F and XNAT_QCMANUALASSESSORDATA_FIELD_MAP_XNAT_QCMANUALASSESSORDATA_F
 * <p>
 * Theoretically, the aliases Hashtable shouldn't get too large in memory because this is really only used for
 * SubQueryFields and we will only be storing hashes for aliases that are greater than 63 characters.
 * If memory does become an issue, we could make a clean up job that removes aliases that have been cached for longer
 * than 1 day.
 * <p>
 * This class is mainly a temporary solution to a much larger issue in XNAT's search engine / query generating code and
 * should not be thought of as the ultimate fix for this issue.
 */

import java.util.HashMap;
import java.util.Map;

public class DisplayFieldAliasCache {
    static private final Map<String, String> aliases = new HashMap<>();

    public static String getAlias(String key) {
        // Only cache the alias if it's greater than the postgresql column name character limit.
        if (key.length() <= 63) {
            return key;
        }

        synchronized (DisplayFieldAliasCache.class) {
            if (!aliases.containsKey(key)) {
                String alias = "df_alias_" + aliases.size();
                aliases.put(key, alias);
                return alias;
            } else {
                return aliases.get(key);
            }
        }
    }
}
