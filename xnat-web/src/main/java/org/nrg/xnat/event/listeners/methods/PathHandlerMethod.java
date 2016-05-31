package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
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
public class PathHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateArchivePath();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateArchivePath();
        }
    }

    private void updateArchivePath() {
        try {
            ArcSpecManager.initialize(getAdminUser());
        } catch (Exception e) {
            _log.error("",e);
        }
    }

    private UserI getAdminUser() {
        for (String login : Users.getAllLogins()) {
            try {
                final UserI user = Users.getUser(login);
                if (Roles.isSiteAdmin(user)) {
                    return user;
                }
            }
            catch(Exception e){

            }
        }
        return null;
    }

    private static final Logger       _log        = LoggerFactory.getLogger(PathHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("archivePath","prearchivePath","cachePath","ftpPath","buildPath","pipelinePath"));

    @Autowired
    @Lazy
    private JdbcTemplate _template;
}
