/*
 * dicomtools: org.nrg.dicomtools.filters.DicomFilterService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.filters;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.LinkedHashMap;

@Service
public class DicomFilterService {
    public static final String SERIES_IMPORT_TOOL = "seriesImportFilter";
    public static final String SERIES_IMPORT_PATH = "config";

    public SeriesImportFilter getSeriesImportFilter() {
        return getSeriesImportFilter(null);
    }

    public SeriesImportFilter getSeriesImportFilter(final String entityId) {
        final Configuration configuration = StringUtils.isBlank(entityId)
                ? _configService.getConfig(SERIES_IMPORT_TOOL, SERIES_IMPORT_PATH, Scope.Site, null)
                : _configService.getConfig(SERIES_IMPORT_TOOL, SERIES_IMPORT_PATH, Scope.Project, entityId);
        if (configuration == null) {
            return null;
        }
        final SeriesImportFilter filter = buildSeriesImportFilter(configuration.getContents());
        if (StringUtils.isNotBlank(entityId)) {
            filter.setProjectId(entityId);
        }
        filter.setEnabled(configuration.getStatus().equals(Configuration.ENABLED_STRING));
        return filter;
    }

    public static SeriesImportFilter buildSeriesImportFilter(final String json) {
        final LinkedHashMap<String, String> map = getSeriesFilterAsMap(json);
        return buildSeriesImportFilter(map);
    }

    public static SeriesImportFilter buildSeriesImportFilter(final LinkedHashMap<String, String> map) {
        if (map.containsKey("contents")) {
            return buildSeriesImportFilter(map.get("contents"));
        }
        if (map.containsKey("content")) {
            return buildSeriesImportFilter(map.get("content"));
        }
        if (map.get(SeriesImportFilter.KEY_MODE).equals(SeriesImportFilterMode.ModalityMap.getValue())) {
            return new ModalityMapSeriesImportFilter(map);
        } else {
            return new RegExBasedSeriesImportFilter(map);
        }
    }

    public static LinkedHashMap<String, String> getSeriesFilterAsMap(Configuration configuration) {
        if (configuration == null) {
            return getDefaultSeriesFilterMap();
        }
        return getSeriesFilterAsMap(configuration.getContents());
    }

    public static LinkedHashMap<String, String> getSeriesFilterAsMap(String contents) {
        if (StringUtils.isBlank(contents)) {
            return getDefaultSeriesFilterMap();
        }
        try {
            return getContentsAsMap(contents);
        } catch (IOException exception) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Something went wrong unmarshalling the series import filter configuration.", exception);
        }
    }

    @Transactional
    public void commit(final SeriesImportFilter filter, final String username) {
        commit(filter, username, "No reason given");
    }

    @Transactional
    public void commit(final SeriesImportFilter filter, String username, String reason) {
        // Remove enabled and project ID, since those are not actually stored as the config contents.
        final LinkedHashMap<String, String> map = filter.toMap();
        final String persisted;
        try {
            persisted = MAPPER.writeValueAsString(map);
        } catch (IOException exception) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Error occurred trying to marshall filter values", exception);
        }

        final String projectId = filter.getProjectId();
        final boolean enabled = filter.isEnabled();

        // Get the config if it exists.
        final Configuration existing = StringUtils.isBlank(projectId)
                ? _configService.getConfig(SERIES_IMPORT_TOOL, SERIES_IMPORT_PATH, Scope.Site, null)
                : _configService.getConfig(SERIES_IMPORT_TOOL, SERIES_IMPORT_PATH, Scope.Project, projectId);

        // If the config is null, we can't very well enable or disable it.
        if (existing != null) {
            final LinkedHashMap<String, String> existingContents = getSeriesFilterAsMap(existing);
            final SeriesImportFilterMode oldMode = SeriesImportFilterMode.mode(existingContents.get(SeriesImportFilter.KEY_MODE));
            final SeriesImportFilterMode newMode = SeriesImportFilterMode.mode(map.get(SeriesImportFilter.KEY_MODE));
            final boolean isModeChanged = !oldMode.equals(newMode);
            final boolean isFilterChanged = !map.equals(existingContents);
            if (isModeChanged || isFilterChanged) {
                StringBuilder message = new StringBuilder("Updated ");
                if (StringUtils.isBlank(projectId)) {
                    message.append("site-wide series import filter ");
                } else {
                    message.append("series import filter for project ").append(projectId).append(" ");
                }
                if (isModeChanged) {
                    message.append("mode to ").append(newMode);
                }
                if (isModeChanged && isFilterChanged) {
                    message.append(" and ");
                }
                if (isFilterChanged) {
                    if (newMode == SeriesImportFilterMode.ModalityMap) {
                        message.append("modes set to ").append(Joiner.on(", ").join(map.keySet()));
                    } else {
                        final String contents = map.get(SeriesImportFilter.KEY_LIST).trim().replaceAll("\n", ", ");
                        message.append("list to ").append(contents.length() < 120 ? contents : contents.substring(0, 120) + "...");
                    }
                }
                try {
                    if (StringUtils.isBlank(projectId)) {
                        _configService.replaceConfig(username, message.toString(), SERIES_IMPORT_TOOL, SERIES_IMPORT_PATH, persisted, Scope.Site, null);
                    } else {
                        _configService.replaceConfig(username, message.toString(), SERIES_IMPORT_TOOL, SERIES_IMPORT_PATH, persisted, Scope.Project, projectId);
                    }
                } catch (ConfigServiceException exception) {
                    throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Error updating configuration for the series import filter", exception);
                }
            }
            if (enabled && !existing.getStatus().equals(Configuration.ENABLED_STRING)) {
                try {
                    if (StringUtils.isBlank(projectId)) {
                        _configService.enable(username, reason, SERIES_IMPORT_TOOL, SERIES_IMPORT_PATH, Scope.Site, null);
                    } else {
                        _configService.enable(username, reason, SERIES_IMPORT_TOOL, SERIES_IMPORT_PATH, Scope.Project, projectId);
                    }
                } catch (ConfigServiceException exception) {
                    final String message = StringUtils.isBlank(projectId) ? "Error enabling the site-wide series import filter" : "Error enabling the series import filter for project " + projectId;
                    throw new NrgServiceRuntimeException(NrgServiceError.Unknown, message, exception);
                }
            } else if (!enabled && existing.getStatus().equals(Configuration.ENABLED_STRING)) {
                try {
                    if (StringUtils.isBlank(projectId)) {
                        _configService.disable(username, reason, SERIES_IMPORT_TOOL, SERIES_IMPORT_PATH, Scope.Site, null);
                    } else {
                        _configService.disable(username, reason, SERIES_IMPORT_TOOL, SERIES_IMPORT_PATH, Scope.Project, projectId);
                    }
                } catch (ConfigServiceException exception) {
                    throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Error disabling the site-wide series import filter", exception);
                }
            }
        } else {
            try {
                if (StringUtils.isBlank(projectId)) {
                    _configService.replaceConfig(username, reason, SERIES_IMPORT_TOOL, SERIES_IMPORT_PATH, true, persisted, Scope.Site, null);
                } else {
                    _configService.replaceConfig(username, reason, SERIES_IMPORT_TOOL, SERIES_IMPORT_PATH, true, persisted, Scope.Project, projectId);
                }
                // In reality, this shouldn't ever really happen. You can't disable the filter and send filter values, but just in case...
                if (!enabled) {
                    if (StringUtils.isBlank(projectId)) {
                        _configService.disable(username, "Disabled on creation", SERIES_IMPORT_TOOL, SERIES_IMPORT_PATH, Scope.Site, null);
                    } else {
                        _configService.disable(username, "Disabled on creation", SERIES_IMPORT_TOOL, SERIES_IMPORT_PATH, Scope.Project, projectId);
                    }
                }
            } catch (ConfigServiceException exception) {
                throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Error creating new configuration for the series import filter", exception);
            }
        }
    }

    static LinkedHashMap<String, String> getContentsAsMap(final String contents) throws IOException {
        if (StringUtils.isBlank(contents)) {
            return new LinkedHashMap<>();
        }
        return MAPPER.readValue(contents, SeriesImportFilter.MAP_TYPE_REFERENCE);
    }

    private static LinkedHashMap<String, String> getDefaultSeriesFilterMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(SeriesImportFilter.KEY_MODE, "blacklist");
        map.put(SeriesImportFilter.KEY_LIST, "");
        return map;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper(new JsonFactory());

    @Autowired
    private ConfigService _configService;
}
