package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.PETTracerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class PetTracerHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updatePetTracer();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if(PREFERENCES.contains(preference)){
            updatePetTracer();
        }
    }

	private void updatePetTracer(){
        try {
            PETTracerUtils.getService().setSiteWideTracerList(getAdminUser().getLogin(), PETTracerUtils.buildScriptPath(PETTracerUtils.ResourceScope.SITE_WIDE, ""), XDAT.getSiteConfigPreferences().getSitewidePetTracers());
        }
        catch(Exception e){
            _log.error("Failed to set sitewide anon script.",e);
        }
    }

    private static final Logger       _log        = LoggerFactory.getLogger(PetTracerHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("sitewidePetTracers"));

    private UserI getAdminUser() throws Exception {
        for (String login : Users.getAllLogins()) {
            final UserI user = Users.getUser(login);
            if (_roleHolder.isSiteAdmin(user)) {
                return user;
            }
        }
        return null;
    }
    @Autowired
    private RoleHolder _roleHolder;
}
