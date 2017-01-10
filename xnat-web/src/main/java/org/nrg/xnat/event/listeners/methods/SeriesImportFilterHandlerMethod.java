/*
 * web: org.nrg.xnat.event.listeners.methods.SeriesImportFilterHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.dicomtools.filters.*;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class SeriesImportFilterHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public SeriesImportFilterHandlerMethod(final SiteConfigPreferences preferences, final DicomFilterService dicomFilterService, final XnatUserProvider primaryAdminUserProvider) {
        super(primaryAdminUserProvider);
        _preferences = preferences;
        _dicomFilterService = dicomFilterService;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateSeriesImportFilter();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateSeriesImportFilter();
        }
    }

    private void updateSeriesImportFilter() {
        try {
            final boolean enabled = _preferences.getEnableSitewideSeriesImportFilter();
            final SeriesImportFilterMode mode = SeriesImportFilterMode.mode(_preferences.getSitewideSeriesImportFilterMode());
            final String filterContents = _preferences.getSitewideSeriesImportFilter();
            final SeriesImportFilter seriesImportFilter;
            if (mode == SeriesImportFilterMode.ModalityMap) {
                seriesImportFilter = new ModalityMapSeriesImportFilter(filterContents, enabled);
            } else {
                seriesImportFilter = new RegExBasedSeriesImportFilter(filterContents, mode, enabled);
            }
            final SeriesImportFilter sitewide = _dicomFilterService.getSeriesImportFilter();
            if (!seriesImportFilter.equals(sitewide)) {
                _dicomFilterService.commit(seriesImportFilter, getAdminUsername(), "Updated site-wide series import filter from administrator UI.");
            }
        } catch (Exception e) {
            _log.error("Failed to update Series Import Filter.", e);
        }
    }

    private static final Logger       _log        = LoggerFactory.getLogger(SeriesImportFilterHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("enableSitewideSeriesImportFilter", "sitewideSeriesImportFilterMode", "sitewideSeriesImportFilter"));

    private final SiteConfigPreferences _preferences;
    private final DicomFilterService    _dicomFilterService;
}
