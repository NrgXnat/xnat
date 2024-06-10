/*
 * web: org.nrg.xnat.initialization.tasks.CreateOrUpdateDatabaseViews
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.UserGroupManager;
import org.nrg.xdat.security.UserGroupServiceI;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class FixMismatchedMappingElements extends AbstractInitializingTask {
    @Autowired
    public FixMismatchedMappingElements(final XnatAppInfo appInfo, final DatabaseHelper helper, final UserGroupServiceI manager, final SiteConfigPreferences preferences) {
        _appInfo = appInfo;
        _helper = helper;
        _manager = manager;
        _siteUrl = preferences.getSiteUrl();
    }

    @Override
    public String getTaskName() {
        return "Fixes mismatched field mapping elements.";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        if (_appInfo.isPrimaryNode()) {
            log.info("This service is the primary XNAT node, checking for mismatched field mapping elements.");

            try {
                if (!_helper.tablesExist(REFERENCED_TABLES_AND_VIEWS)) {
                    throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "Deferring execution because the following tables and views do not yet all exist: " + String.join(", ", REFERENCED_TABLES_AND_VIEWS));
                }
                if (!_helper.functionsExist(REFERENCED_FUNCTIONS)) {
                    throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "Deferring execution because the following functions do not yet all exist: " + String.join(", ", REFERENCED_FUNCTIONS));
                }

                Users.getGuest();

                log.info("Preparing to check for and fix any mismatched data-type permissions.");
                final int mismatched = _helper.callFunction("data_type_fns_fix_mismatched_permissions", Integer.class);
                if (mismatched > 0) {
                    log.warn("Found and fixed {} mismatched data-type permissions", mismatched);
                }
                final Integer missing = _helper.callFunction("data_type_fns_fix_missing_public_element_access_mappings", Integer.class);
                if (missing > 0) {
                    log.warn("Found and fixed {} missing data-type permission mappings", missing);
                }
                final int corrected = _helper.callFunction("data_type_fns_correct_group_permissions", Integer.class);
                if (corrected > 0) {
                    log.warn("Found and fixed {} misconfigured data-type permission mappings", corrected);
                }
                final List<Map<String, Object>> irregulars = _manager.findIrregularProjectGroups();
                if (!irregulars.isEmpty()) {
                    log.warn("Found project groups with irregular permission mappings:\n\n * {}\n\nI'm not going to try to fix these issues automatically. You can request that these be fixed by making a POST request to the URL:\n\n   {}/xapi/access/permissions/irregular/fix", StringUtils.join(UserGroupManager.formatIrregularProjectGroups(irregulars), "\n * "), _siteUrl);
                }
            } catch (SQLException e) {
                throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to access the database to check for the table 'xdat_search.xs_item_access'.", e);
            } catch (UserNotFoundException e) {
                throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "Didn't find the guest user. Will defer execution until that exists.", e);
            } catch (UserInitException e) {
                throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to retrieve the guest user. This isn't just that the user doesn't exist, so may indicate a more serious issue.", e);
            }
        }
    }

    private static final String[] REFERENCED_TABLES_AND_VIEWS = new String[]{"data_type_views_member_edit_permissions", "data_type_views_mismatched_mapping_elements", "data_type_views_missing_mapping_elements", "data_type_views_orphaned_field_sets", "project_groups_find_irregular_settings", "xdat_element_access", "xdat_element_security", "xdat_field_mapping", "xdat_field_mapping_set", "xdat_primary_security_field", "xdat_user", "xdat_usergroup"};
    private static final String[] REFERENCED_FUNCTIONS        = new String[]{"data_type_fns_correct_group_permissions", "data_type_fns_fix_mismatched_permissions", "data_type_fns_fix_missing_public_element_access_mappings"};

    private final XnatAppInfo       _appInfo;
    private final DatabaseHelper    _helper;
    private final UserGroupServiceI _manager;
    private final String            _siteUrl;
}
