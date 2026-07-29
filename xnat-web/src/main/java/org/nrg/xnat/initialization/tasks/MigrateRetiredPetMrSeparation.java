/*
 * web: org.nrg.xnat.initialization.tasks.MigrateRetiredPetMrSeparation
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.preferences.HandlePetMr;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.db.PoolDBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Cleans up data left behind by the removal of PET/MR session separation.
 *
 * <p>Separating a PET/MR session into distinct PET and MR sessions is no longer supported. Three kinds of stored data
 * refer to that retired behavior and have to be reconciled at startup:</p>
 *
 * <ol>
 *     <li>Prearchive rows left in one of the separation statuses. Those status values no longer exist in {@link
 *     org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus PrearcStatus}, so reading such a row now fails and the
 *     session is unreachable. They're reset to READY.</li>
 *     <li>The site-wide {@code sitewidePetMr} preference set to "separate".</li>
 *     <li>Project-level {@code separatePETMR} configurations set to "separate".</li>
 * </ol>
 *
 * <p>The latter two are remapped to "petmr", the closest surviving behavior. {@link HandlePetMr#get(String)} also
 * remaps this value defensively at read time, so a value this task misses degrades gracefully rather than failing.</p>
 */
@Component
@Slf4j
public class MigrateRetiredPetMrSeparation extends AbstractInitializingTask {
    private static final String REASON = "Removed retired PET/MR session separation setting";

    private static final String QUERY_RESET_SEPARATING_SESSIONS =
            "UPDATE " + PoolDBUtils.search_schema_name + ".prearchive " +
            "SET status = 'READY' " +
            "WHERE status IN ('SEPARATING', 'QUEUED_SEPARATING', '_SEPARATING')";

    private final SiteConfigPreferences      _preferences;
    private final ConfigService              _configService;
    private final NamedParameterJdbcTemplate _template;

    @Autowired
    public MigrateRetiredPetMrSeparation(final SiteConfigPreferences preferences,
                                         final ConfigService configService,
                                         final NamedParameterJdbcTemplate template) {
        super();
        _preferences = preferences;
        _configService = configService;
        _template = template;
    }

    @Override
    public String getTaskName() {
        return "Migrate retired PET/MR separation settings";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        resetSeparatingPrearchiveSessions();
        migrateSitePreference();
        migrateProjectConfigurations();
    }

    /**
     * Resets any prearchive session left in one of the retired separation statuses. Those sessions were mid-separation
     * when the feature was removed, so there's no operation left to resume: returning them to READY makes them visible
     * and actionable in the prearchive again.
     */
    private void resetSeparatingPrearchiveSessions() {
        try {
            final int reset = _template.update(QUERY_RESET_SEPARATING_SESSIONS, Collections.emptyMap());
            if (reset > 0) {
                log.info("Reset {} prearchive session(s) from a retired PET/MR separation status to READY.", reset);
            }
        } catch (Exception e) {
            // The prearchive table may not exist yet on a brand-new installation, in which case there's nothing to fix.
            log.warn("Unable to reset prearchive sessions left in a retired PET/MR separation status. If any sessions are stuck in a SEPARATING state, they may need to be reset manually.", e);
        }
    }

    private void migrateSitePreference() {
        final String current = _preferences.getSitewidePetMr();
        if (!StringUtils.equalsIgnoreCase(StringUtils.deleteWhitespace(current), HandlePetMr.SEPARATE)) {
            return;
        }
        log.info("The site-wide PET/MR setting is set to \"{}\", which is no longer supported. Setting it to \"{}\" instead.", HandlePetMr.SEPARATE, HandlePetMr.PETMR);
        _preferences.setSitewidePetMr(HandlePetMr.PETMR);
    }

    private void migrateProjectConfigurations() {
        final List<Configuration> configurations = _configService.getConfigsByTool(HandlePetMr.SEPARATE_PET_MR);
        if (CollectionUtils.isEmpty(configurations)) {
            return;
        }
        for (final Configuration configuration : configurations) {
            if (configuration.getScope() != Scope.Project || StringUtils.isBlank(configuration.getEntityId())) {
                continue;
            }
            if (!StringUtils.equalsIgnoreCase(StringUtils.deleteWhitespace(configuration.getContents()), HandlePetMr.SEPARATE)) {
                continue;
            }
            final String projectId = configuration.getEntityId();
            try {
                _configService.replaceConfig("admin", REASON, HandlePetMr.SEPARATE_PET_MR, HandlePetMr.CONFIG, HandlePetMr.PETMR, Scope.Project, projectId);
                log.info("The PET/MR setting for project {} was set to \"{}\", which is no longer supported. It has been set to \"{}\" instead.", projectId, HandlePetMr.SEPARATE, HandlePetMr.PETMR);
            } catch (ConfigServiceException e) {
                log.error("Unable to update the retired PET/MR separation setting for project {}. It will be handled as \"{}\" at run time regardless.", projectId, HandlePetMr.PETMR, e);
            }
        }
    }
}
