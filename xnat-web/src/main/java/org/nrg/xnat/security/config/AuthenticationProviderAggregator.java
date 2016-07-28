package org.nrg.xnat.security.config;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.authentication.AuthenticationProvider;

import java.util.*;

public class AuthenticationProviderAggregator extends ArrayList<AuthenticationProvider> {

    public AuthenticationProviderAggregator(List<AuthenticationProvider> standaloneProviders, Map<String, AuthenticationProviderConfigurator> configurators) {
        ArrayList<HashMap<String, String>> providerList = new ArrayList<>();

        // Populate map of properties
        try {
            String               filenameEnd = "-provider.properties";
            final List<Resource> resources   = BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/auth/**/*" + filenameEnd);
            if(resources==null || resources.isEmpty()){
                String            dbName        = "Database";
                String            dbId          = "localdb";
                String            dbType        = "db";
                HashMap<String, String> dbProv = new HashMap<String, String>();
                dbProv.put("name", dbName);
                dbProv.put("id", dbId);
                dbProv.put("type", dbType);
                providerList.add(dbProv);
            }
            else {
                for (final Resource resource : resources) {
                    String filename = resource.getFilename();
                    String id = filename.substring(0, (filename.length() - filenameEnd.length()));
                    HashMap<String, String> newProv = new HashMap<String, String>();

                    final Properties provider = PropertiesLoaderUtils.loadProperties(resource);
                    for (Map.Entry<Object, Object> providerProperty : provider.entrySet()) {
                        newProv.put(providerProperty.getKey().toString(), providerProperty.getValue().toString());
                    }
                    providerList.add(newProv);
                }
            }
        } catch (Exception e) {
            _log.error("", e);
        }

        // Create providers
        for (HashMap<String, String> prov : providerList) {
            String name = prov.get("name");
            String id   = prov.get("id");
            String type = prov.get("type");

            assert !StringUtils.isBlank(name) : "You must provide a name for all authentication provider configurations";
            assert !StringUtils.isBlank(id) : "You must provide an ID for all authentication provider configurations";
            assert !StringUtils.isBlank(type) : "You must provide a type for all authentication provider configurations";

            if (configurators.containsKey(type)) {
                AuthenticationProviderConfigurator configurator = configurators.get(type);

                addAll(configurator.getAuthenticationProviders(id, name, prov));
            }
        }

        if (standaloneProviders != null) {
            addAll(standaloneProviders);
        }

        Collections.sort(this, new Comparator<AuthenticationProvider>(){
            public int compare(AuthenticationProvider o1, AuthenticationProvider o2){
                if(XnatAuthenticationProvider.class.isAssignableFrom(o1.getClass())){
                    if(XnatAuthenticationProvider.class.isAssignableFrom(o2.getClass())){
                        if(((XnatAuthenticationProvider)o1).getOrder() == ((XnatAuthenticationProvider)o2).getOrder())
                            return 0;
                        return ((XnatAuthenticationProvider)o1).getOrder() < ((XnatAuthenticationProvider)o2).getOrder() ? -1 : 1;
                    }
                    else{
                        return 1;
                    }
                }
                else{
                    if(XnatAuthenticationProvider.class.isAssignableFrom(o2.getClass())){
                        return -1;
                    }
                    else{
                        return 0;
                    }
                }
            }
        });
    }

    private static final Logger _log = LoggerFactory.getLogger(AuthenticationProviderAggregator.class);
}
