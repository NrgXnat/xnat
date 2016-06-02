package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.dicomtools.filters.*;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class SeriesImportFilterHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
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
        if(PREFERENCES.contains(preference)){
            updateSeriesImportFilter();
        }
    }

	private void updateSeriesImportFilter(){
        try {
            final boolean enabled = XDAT.getSiteConfigPreferences().getEnableSitewideSeriesImportFilter();
            final SeriesImportFilterMode mode = SeriesImportFilterMode.mode(XDAT.getSiteConfigPreferences().getSitewideSeriesImportFilterMode());
            final String filterContents = XDAT.getSiteConfigPreferences().getSitewideSeriesImportFilter();
            final SeriesImportFilter seriesImportFilter;
            if (mode == SeriesImportFilterMode.ModalityMap) {
                seriesImportFilter = new ModalityMapSeriesImportFilter(filterContents, enabled);
            } else {
                seriesImportFilter = new RegExBasedSeriesImportFilter(filterContents, mode, enabled);
            }
            if (!seriesImportFilter.equals(getSeriesImportFilter())) {
                getDicomFilterService().commit(seriesImportFilter, getAdminUser().getLogin(), "Updated site-wide series import filter from administrator UI.");
            }
        }
        catch(Exception e){
            _log.error("Failed to update Series Import Filter.",e);
        }
    }

    private static final Logger       _log        = LoggerFactory.getLogger(SeriesImportFilterHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("enableSitewideSeriesImportFilter", "sitewideSeriesImportFilterMode", "sitewideSeriesImportFilter"));

    private UserI getAdminUser() throws Exception {
        for (String login : Users.getAllLogins()) {
            final UserI user = Users.getUser(login);
            if (_roleHolder.isSiteAdmin(user)) {
                return user;
            }
        }
        return null;
    }

    private DicomFilterService getDicomFilterService() {
        return XDAT.getContextService().getBean(DicomFilterService.class);
    }

    private SeriesImportFilter getSeriesImportFilter() {
        DicomFilterService service = getDicomFilterService();
        if (service != null) {
            return service.getSeriesImportFilter();
        }
        else{
            return null;
        }
    }

    @Autowired
    private RoleHolder _roleHolder;
}
