/*
 * web: org.nrg.xnat.utils.UserUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;


import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class UserUtils {
    public static void CreatedArchivePathCache(final String dbName, final String login) throws Exception {
        if (!ARCHIVE_PATH_CHECKED.get()) {
            if (!(Boolean) PoolDBUtils.ReturnStatisticQuery(QUERY_CACHE_TABLE_EXISTS, "exists", dbName, login)) {
                PoolDBUtils.ExecuteNonSelectQuery(QUERY_CACHE_TABLE_CREATE, dbName, login);
            }
            ARCHIVE_PATH_CHECKED.set(true);
        }
    }

    public static Object cacheFileLink(final String url, final String absolutePath, final String dbName, final String username) throws Exception {
        UserUtils.CreatedArchivePathCache(dbName, username);

        final String cacheId = getUniqueCachePathToken(dbName, username);
        PoolDBUtils.ExecuteNonSelectQuery(String.format(QUERY_INSERT_INTO_PATH_CACHE, username, DBAction.ValueParser(url, "string", true), cacheId, DBAction.ValueParser(absolutePath, "string", true)), dbName, username);
        return cacheId;
    }

    public static String retrieveCacheFileLink(final String cacheId, final String dbName, final String login) throws Exception {
        return (String) PoolDBUtils.ReturnStatisticQuery(String.format(QUERY_GET_CACHE_PATH, StringUtils.remove(cacheId, '\'')), "absolute_path", dbName, login);
    }

    private static String getUniqueCachePathToken(final String dbName, final String username) throws Exception {
        String cacheId = RandomStringUtils.randomAlphanumeric(64);
        while (!((Boolean) PoolDBUtils.ReturnStatisticQuery(String.format(QUERY_CHECK_TOKEN_EXISTS, cacheId), "exists", dbName, username))) {
            cacheId = RandomStringUtils.randomAlphanumeric(64);
        }
        return cacheId;
    }

    private static final AtomicBoolean ARCHIVE_PATH_CHECKED         = new AtomicBoolean();
    private static final String        QUERY_CACHE_TABLE_EXISTS     = "SELECT EXISTS(SELECT relname FROM pg_catalog.pg_class WHERE relname = LOWER('xs_archive_path_cache')) AS exists";
    private static final String        QUERY_CACHE_TABLE_CREATE     = "CREATE TABLE xs_archive_path_cache (id serial, create_date timestamp DEFAULT now(), username VARCHAR(255), url text, _token VARCHAR(255), absolute_path text)";
    private static final String        QUERY_CHECK_TOKEN_EXISTS     = "SELECT EXISTS(SELECT id FROM xs_archive_path_cache WHERE _token='%s') AS exists";
    private static final String        QUERY_INSERT_INTO_PATH_CACHE = "INSERT INTO xs_archive_path_cache (username, url, _token, absolute_path) VALUES ('%s', %s, '%s', %s)";
    private static final String        QUERY_GET_CACHE_PATH         = "SELECT absolute_path FROM xs_archive_path_cache WHERE _token='%s'";
}
