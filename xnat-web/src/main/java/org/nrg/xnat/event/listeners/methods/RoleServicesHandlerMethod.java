package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.RoleRepositoryHolder;
import org.nrg.xdat.security.services.RoleRepositoryServiceI;
import org.nrg.xdat.security.services.RoleServiceI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class RoleServicesHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateFeatureServices();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if(PREFERENCES.contains(preference)){
            updateFeatureServices();
        }
    }

	private void updateFeatureServices(){
        try {
            _roleHolder.setRoleService(Class.forName(XDAT.getSiteConfigPreferences().getRoleService()).asSubclass(RoleServiceI.class).newInstance());
        }
        catch(Exception e){
            _log.error("",e);
        }
        try {
            _roleRepositoryHolder.setRoleRepositoryService(Class.forName(XDAT.getSiteConfigPreferences().getRoleRepositoryService()).asSubclass(RoleRepositoryServiceI.class).newInstance());
        }
        catch(Exception e){
            _log.error("",e);
        }
    }

    private static final Logger       _log        = LoggerFactory.getLogger(RoleServicesHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("security.services.role.default", "security.services.roleRepository.default"));


    @Autowired
    private RoleHolder _roleHolder;

    @Autowired
    private RoleRepositoryHolder _roleRepositoryHolder;

}
