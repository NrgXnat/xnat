package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CheckSeriesImportFilterInSite extends AbstractInitializingTask {

    private static final String TOOL_NAME = "seriesImportFilter";
    public static final String SERIES_IMPORT_FILTER_ENABLED = "enabled";
    private final SiteConfigPreferences siteConfigPreferences;
    private final ConfigService configService;

    @Autowired
    public CheckSeriesImportFilterInSite(
            final SiteConfigPreferences siteConfigPreferences,
            final ConfigService configService) {
        this.siteConfigPreferences = siteConfigPreferences;
        this.configService = configService;
    }

    @Override
    public String getTaskName() {
        return "Check Series Import Filter Enabled In Site";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        List<Configuration> configurations = configService.getConfigsByTool(TOOL_NAME);
        if (CollectionUtils.isEmpty(configurations)) {
            siteConfigPreferences.setEnableProjectsSeriesImportFilter(false);
        } else {
            siteConfigPreferences.setEnableProjectsSeriesImportFilter(getLatestVersionStatus(configurations));
        }
    }

    public static boolean getLatestVersionStatus(List<Configuration> data) {
        return data.stream()
                .filter(config -> Scope.Project.equals(config.getScope()) && StringUtils.isNotEmpty(config.getEntityId()))
                .collect(Collectors.toMap(
                        Configuration::getEntityId,
                        config -> config,
                        (existing, replacement) -> replacement.getVersion() > existing.getVersion() ? replacement : existing
                ))
                .values()
                .stream()
                .anyMatch(config -> SERIES_IMPORT_FILTER_ENABLED.equals(config.getStatus()));
    }
}
