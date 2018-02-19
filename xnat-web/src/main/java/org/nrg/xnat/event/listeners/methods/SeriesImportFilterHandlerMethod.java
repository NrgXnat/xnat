/*
 * web: org.nrg.xnat.event.listeners.methods.SeriesImportFilterHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.extern.slf4j.Slf4j;
import org.nrg.dicomtools.filters.DicomFilterService;
import org.nrg.dicomtools.filters.ModalityMapSeriesImportFilter;
import org.nrg.dicomtools.filters.SeriesImportFilter;
import org.nrg.dicomtools.filters.SeriesImportFilterMode;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SeriesImportFilterHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public SeriesImportFilterHandlerMethod(final DicomFilterService dicomFilterService, final XnatUserProvider primaryAdminUserProvider) {
        super(primaryAdminUserProvider, ENABLE_SITEWIDE_FILTER, SITEWIDE_FILTER_MODE, SITEWIDE_FILTER);
        _dicomFilterService = dicomFilterService;
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
                final SeriesImportFilterMode mode = SeriesImportFilterMode.mode(value);
                final SeriesImportFilter seriesImportFilter = _dicomFilterService.getSeriesImportFilter();
                // It only makes sense to change mode for regex series import filter.
                if (mode != SeriesImportFilterMode.ModalityMap && !(seriesImportFilter instanceof ModalityMapSeriesImportFilter)) {
                    seriesImportFilter.setMode(mode);
                }
                _dicomFilterService.commit(seriesImportFilter, getAdminUsername(), "Updated site-wide series import filter mode from administrator UI.");
                break;
        }
    }

    private void setFilter(final String value) {
        final SeriesImportFilter filter = DicomFilterService.buildSeriesImportFilter(value);
        _dicomFilterService.commit(filter, getAdminUsername(), "Updated site-wide series import filter from administrator UI.");
    }

    private void setFilterEnabledFlag(final String value) {
        final SeriesImportFilter filter  = _dicomFilterService.getSeriesImportFilter();
        final boolean            enabled = Boolean.parseBoolean(value);
        filter.setEnabled(enabled);
        _dicomFilterService.commit(filter, getAdminUsername(), (enabled ? "Enabled" : "Disabled") + " site-wide series import filter from administrator UI.");
    }

    private static final String SITEWIDE_FILTER        = "sitewideSeriesImportFilter";
    private static final String SITEWIDE_FILTER_MODE   = "sitewideSeriesImportFilterMode";
    private static final String ENABLE_SITEWIDE_FILTER = "enableSitewideSeriesImportFilter";

    private final DicomFilterService _dicomFilterService;
}
