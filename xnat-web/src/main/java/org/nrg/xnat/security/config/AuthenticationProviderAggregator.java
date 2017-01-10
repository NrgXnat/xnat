/*
 * web: org.nrg.xnat.security.config.AuthenticationProviderAggregator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.authentication.AuthenticationProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class AuthenticationProviderAggregator extends ArrayList<AuthenticationProvider> {

    public AuthenticationProviderAggregator(List<AuthenticationProvider> standaloneProviders, Map<String, AuthenticationProviderConfigurator> configurators, final ConfigPaths configFolderPaths) {
        ArrayList<HashMap<String, String>> providerList = new ArrayList<>();

        // Populate map of properties for each provider
        try {
            String filenameEnd = "-provider.properties";

            List<Resource> resources = null;

            ArrayList<String> authFilePaths = new ArrayList<>();
            try {
                //First see if there are any properties files in config/auth
                for (Path currPath : configFolderPaths){
                    Path authPath = Paths.get(currPath.toString(), "auth");

                    _log.debug("AuthPath is " + authPath.toString());
                    Collection<File> files = FileUtils.listFiles(
                            authPath.toFile(),
                            new RegexFileFilter("^.*" + filenameEnd),
                            DirectoryFileFilter.DIRECTORY
                    );
                    if (files == null) {
                        _log.debug("No files were found in " + authPath);
                    } else {
                        for (File file : files) {
                            if (!authFilePaths.contains(file.toString())) {
                                authFilePaths.add(file.toString());
                            }
                        }
                    }
                }
            }
            catch(Exception e){
                _log.debug("Could not find auth properties files in config/auth.");
            }
            if(authFilePaths!=null || !authFilePaths.isEmpty()){
                //If there were provider properties files in config/auth, use them to populate provider list
                for (final String authFilePath : authFilePaths) {
                    _log.debug("Accessing properties from "+authFilePath);
                    Properties props = new Properties();
                    InputStream inputStream = new FileInputStream(authFilePath);
                    if(inputStream!=null){
                        props.load(inputStream);
                    }
                    else{
                        _log.debug("Input stream was null for "+authFilePath);
                    }
                    _log.debug("Found "+props.size()+ " properties.");
                    HashMap<String, String> newProv = new HashMap<String, String>();
                    for (Map.Entry<Object, Object> providerProperty : props.entrySet()) {
                        _log.debug("Trying to add property "+providerProperty.getKey().toString()+" with value "+providerProperty.getValue().toString());
                        newProv.put(providerProperty.getKey().toString(), providerProperty.getValue().toString());
                    }
                    providerList.add(newProv);
                    _log.debug("Added provider (name:"+newProv.get("name")+", id:"+newProv.get("id")+", type:"+newProv.get("type")+").");
                }
            }

            //If the provider list is still empty, try to get providers from plugins
            if(providerList==null || providerList.isEmpty()){
                //If no properties files were found in the config directories, look for properties files that might be from plugins
                resources   = BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/auth/**/*" + filenameEnd);
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
            }


        } catch (Exception e) {
            _log.error("Error getting authentication provider properties", e);
        }

        // Create providers from provider list
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
