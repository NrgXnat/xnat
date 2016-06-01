package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.editscript.DicomEdit;
import org.nrg.xnat.helpers.merge.AnonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class AnonymizationHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateAnon();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if(PREFERENCES.contains(preference)){
            updateAnon();
        }
    }

	private void updateAnon(){
        try {
            if (XDAT.getSiteConfigPreferences().getEnableSitewideAnonymizationScript()) {
                AnonUtils.getService().enableSiteWide(getAdminUser().getLogin(), DicomEdit.buildScriptPath(DicomEdit.ResourceScope.SITE_WIDE, null));
            } else {
                AnonUtils.getService().disableSiteWide(getAdminUser().getLogin(), DicomEdit.buildScriptPath(DicomEdit.ResourceScope.SITE_WIDE, null));
            }
        }
        catch(Exception e){
            _log.error("Failed to enable/disable sitewide anon script.",e);
        }
        try {
            AnonUtils.getService().setSiteWideScript(getAdminUser().getLogin(), DicomEdit.buildScriptPath(DicomEdit.ResourceScope.SITE_WIDE, null), XDAT.getSiteConfigPreferences().getSitewideAnonymizationScript());
        }
        catch(Exception e){
            _log.error("Failed to set sitewide anon script.",e);
        }
    }

    private static final Logger       _log        = LoggerFactory.getLogger(AnonymizationHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("enableSitewideAnonymizationScript", "sitewideAnonymizationScript"));

    private UserI getAdminUser() throws Exception {
        for (String login : Users.getAllLogins()) {
            final UserI user = Users.getUser(login);
            if (Roles.isSiteAdmin(user)) {
                return user;
            }
        }
        return null;
    }
    @Autowired
    @Lazy
    private JdbcTemplate _template;
}
