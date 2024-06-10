/*
 * web: org.nrg.xnat.event.listeners.methods.SeriesImportFilterHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.dicomtools.filters.*;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.nrg.dicomtools.filters.SeriesImportFilter.KEY_LIST;
import static org.nrg.dicomtools.filters.SeriesImportFilter.KEY_MODALITIES;
import static org.nrg.dicomtools.filters.SeriesImportFilterMode.*;

@Component
@Getter(PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public class SeriesImportFilterHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    public static final String      SITEWIDE_FILTER           = "sitewideSeriesImportFilter";
    public static final String      SITEWIDE_FILTER_MODE      = "sitewideSeriesImportFilterMode";
    public static final String      ENABLE_SITEWIDE_FILTER    = "enableSitewideSeriesImportFilter";
    public static final Set<String> SERIES_IMPORT_PREFERENCES = ImmutableSet.of(ENABLE_SITEWIDE_FILTER, SITEWIDE_FILTER_MODE, SITEWIDE_FILTER);

    @Autowired
    public SeriesImportFilterHandlerMethod(final DicomFilterService dicomFilterService, final XnatUserProvider primaryAdminUserProvider) {
        super(primaryAdminUserProvider, ENABLE_SITEWIDE_FILTER, SITEWIDE_FILTER_MODE, SITEWIDE_FILTER);
        _dicomFilterService = dicomFilterService;
    }

    @Override
    protected void handlePreferencesImpl(final Map<String, String> values) {
        final Set<String> preferences = values.keySet();
        preferences.retainAll(getHandledPreferences());

        if (preferences.size() == 1) {
            final String preference = preferences.iterator().next();
            handlePreferenceImpl(preference, values.get(preference));
        } else {
            updateSeriesImportFilter(values);
        }
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        switch (preference) {
            case ENABLE_SITEWIDE_FILTER:
                setFilterEnabledFlag(value);
                break;

            case SITEWIDE_FILTER:
                setFilter(value);
                break;

            case SITEWIDE_FILTER_MODE:
                final SeriesImportFilter sitewide = getSeriesImportFilter();
                final SeriesImportFilterMode current = sitewide.getMode();
                final SeriesImportFilterMode proposed = SeriesImportFilterMode.mode(value);

                if (proposed == current) {
                    log.info("Tried to \"change\" import filter mode from {} to {}, which isn't a change.", value, value);
                    return;
                }

                // It only makes sense to change mode to blacklist/whitelist for regex series import filter. You
                // can't really change the mode of a modality map.
                if ((current == Blacklist || current == Whitelist) && proposed == ModalityMap) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You can't change just the mode when going from a regex import filter to modality map.");
                }
                if ((proposed == Blacklist || proposed == Whitelist) && current == ModalityMap) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You can't change just the mode when going from a modality map to a regex import filter.");
                }

                sitewide.setMode(proposed);
                commit(sitewide, "Updated site-wide series import filter mode to \"" + value + "\" from administrator UI.");
                break;
        }
    }

    private void updateSeriesImportFilter(final Map<String, String> values) {
        try {
            final SeriesImportFilter sitewide = getSeriesImportFilter();

            final SeriesImportFilterMode mode;
            final boolean                enabled;
            final String                 contents;
            final boolean                changedContents;
            if (sitewide == null) {
                final Set<String> specified = values.keySet();
                if (!specified.containsAll(SERIES_IMPORT_PREFERENCES)) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "There's currently no site-wide series import filter set. You must specify values for all of the required preference settings to initialize a new filter: " + StringUtils.join(SERIES_IMPORT_PREFERENCES, ", "));
                }
                mode = SeriesImportFilterMode.mode(values.get(SITEWIDE_FILTER_MODE));
                enabled = Boolean.parseBoolean(values.get(ENABLE_SITEWIDE_FILTER));
                contents = values.get(SITEWIDE_FILTER);
                changedContents = true;
            } else {
                mode = values.containsKey(SITEWIDE_FILTER_MODE) ? SeriesImportFilterMode.mode(values.get(SITEWIDE_FILTER_MODE)) : sitewide.getMode();
                enabled = BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBooleanObject(values.get(ENABLE_SITEWIDE_FILTER)), sitewide.isEnabled());

                final boolean isModalityMap = sitewide.getMode() == ModalityMap;
                if (!values.containsKey(SITEWIDE_FILTER)) {
                    if (isModalityMap && mode != ModalityMap || !isModalityMap && mode == ModalityMap) {
                        throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You must specify the site-wide filter contents when switching between modality map and regex-based series import filters.");
                    }
                    if (sitewide.getMode() == mode && sitewide.isEnabled() == enabled) {
                        log.info("Tried to \"change\" site-wide import filter but there are no changes. Carry on.");
                        return;
                    }
                    contents = sitewide.toMap().get(isModalityMap ? KEY_MODALITIES : KEY_LIST);
                    changedContents = false;
                } else {
                    contents = values.get(SITEWIDE_FILTER);
                    changedContents = true;
                }
            }

            final SeriesImportFilter updated = mode == ModalityMap ? new ModalityMapSeriesImportFilter(contents, enabled) : new RegExBasedSeriesImportFilter(contents, mode, enabled);
            if (sitewide == null) {
                commit(updated, "Initialized site-wide series import filter from administrator UI: " + (enabled ? "enabled " : "disabled ") + mode.toString() + " filter.");
            } else if (!updated.equals(sitewide)) {
                final boolean changedEnabled = updated.isEnabled() != sitewide.isEnabled();
                final boolean changedMode    = updated.getMode() != sitewide.getMode();
                final StringBuilder message = new StringBuilder("Updated site-wide series import filter from administrator UI: changed ");
                if (changedEnabled) {
                    message.append("enabled to ").append(enabled);
                }
                if (changedMode && changedEnabled) {
                    message.append(" and ");
                }
                if (changedMode) {
                    message.append("mode to ").append(mode.toString());
                }
                if ((changedMode || changedEnabled) && changedContents) {
                    message.append(" and ");
                }
                if (changedContents) {
                    message.append("contents (not shown)");
                }
                commit(updated, message.toString());
            }
        } catch (Exception e) {
            log.error("Failed to update Series Import Filter.", e);
        }
    }

    private void setFilter(final String value) {
        final SeriesImportFilter            sitewide   = getSeriesImportFilter();
        final LinkedHashMap<String, String> properties = sitewide.toMap();
        if (sitewide.getMode() != ModalityMap) {
            properties.put(KEY_LIST, value);
        } else {
            properties.put(KEY_MODALITIES, value);
        }
        commit(DicomFilterService.buildSeriesImportFilter(properties), "Updated site-wide series import filter from administrator UI.");
    }

    private void setFilterEnabledFlag(final String value) {
        final SeriesImportFilter sitewide = getSeriesImportFilter();
        final boolean            enabled  = Boolean.parseBoolean(value);
        sitewide.setEnabled(enabled);
        commit(sitewide, (enabled ? "Enabled" : "Disabled") + " site-wide series import sitewide from administrator UI.");
    }

    private SeriesImportFilter getSeriesImportFilter() {
        return getDicomFilterService().getSeriesImportFilter();
    }

    private void commit(final SeriesImportFilter seriesImportFilter, final String s) {
        getDicomFilterService().commit(seriesImportFilter, getAdminUsername(), s);
    }

    private final DicomFilterService _dicomFilterService;
}
