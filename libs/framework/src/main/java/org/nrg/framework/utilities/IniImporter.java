/*
 * framework: org.nrg.framework.utilities.IniImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.nrg.framework.configuration.ConfigPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;

/**
 * Provides utility functions for processing ini files.
 */
public class IniImporter {
    private IniImporter() {
        // Default constructor prevents creating instances.
    }

    @Nonnull
    public static Properties getIniProperties(final String iniFileSpec) {
        return getIniProperties(null, Collections.singletonList(iniFileSpec), new String[0]);
    }

    @Nonnull
    public static Properties getIniProperties(final List<String> iniFileSpecs) {
        return getIniProperties(null, iniFileSpecs, new String[0]);
    }

    @Nonnull
    public static Properties getIniProperties(final String iniFileSpec, final String... requested) {
        return getIniProperties(null, Collections.singletonList(iniFileSpec), requested);
    }

    @Nonnull
    public static Properties getIniProperties(final List<String> iniFileSpecs, final String... requested) {
        return getIniProperties(null, iniFileSpecs, requested);
    }

    @Nonnull
    public static Properties getIniProperties(final ConfigPaths configFolderPaths, final String iniFileSpec) {
        return getIniProperties(configFolderPaths, Collections.singletonList(iniFileSpec), new String[0]);
    }

    @Nonnull
    public static Properties getIniProperties(final ConfigPaths configFolderPaths, final List<String> iniFileSpecs) {
        return getIniProperties(configFolderPaths, iniFileSpecs, new String[0]);
    }

    @Nonnull
    public static Properties getIniProperties(final ConfigPaths configFolderPaths, final String iniFileSpec, final String... requested) {
        return getIniProperties(configFolderPaths, Collections.singletonList(iniFileSpec), requested);
    }

    @Nonnull
    public static Properties getIniProperties(final ConfigPaths configFolderPaths, final List<String> iniFileSpecs, final String... requested) {
        final PropertiesIniStore store = new PropertiesIniStore();
        processInisToStore(store, configFolderPaths, iniFileSpecs, requested);
        return store.getIniProperties();
    }

    @Nonnull
    public static Map<String, Map<String, String>> getIniMap(final String iniFileSpec) {
        return getIniMap(Collections.singletonList(iniFileSpec), new String[0]);
    }

    @Nonnull
    public static Map<String, Map<String, String>> getIniMap(final List<String> iniFileSpecs) {
        return getIniMap(null, iniFileSpecs, new String[0]);
    }

    @Nonnull
    public static Map<String, Map<String, String>> getIniMap(final String iniFileSpec, final String... requested) {
        return getIniMap(null, Collections.singletonList(iniFileSpec), requested);
    }

    @Nonnull
    public static Map<String, Map<String, String>> getIniMap(final List<String> iniFileSpecs, final String... requested) {
        return getIniMap(null, iniFileSpecs, requested);
    }

    @Nonnull
    public static Map<String, Map<String, String>> getIniMap(final ConfigPaths configFolderPaths, final String iniFileSpec) {
        return getIniMap(configFolderPaths, Collections.singletonList(iniFileSpec), new String[0]);
    }

    @Nonnull
    public static Map<String, Map<String, String>> getIniMap(final ConfigPaths configFolderPaths, final List<String> iniFileSpecs) {
        return getIniMap(configFolderPaths, iniFileSpecs, new String[0]);
    }

    @Nonnull
    public static Map<String, Map<String, String>> getIniMap(final ConfigPaths configFolderPaths, final String iniFileSpec, final String... requested) {
        return getIniMap(configFolderPaths, Collections.singletonList(iniFileSpec), requested);
    }

    @Nonnull
    public static Map<String, Map<String, String>> getIniMap(final ConfigPaths configFolderPaths, final List<String> iniFileSpecs, final String... requested) {
        final MapIniStore store = new MapIniStore();
        processInisToStore(store, configFolderPaths, iniFileSpecs, requested);
        return store.getIniMap();
    }

    private static void processInisToStore(final IniStore store, final ConfigPaths configFolderPaths, final List<String> iniFileSpecs, final String... requested) {
        for (final String iniFileSpec : iniFileSpecs) {
            try {
                if (configFolderPaths != null) {
                    for (final File iniFile : configFolderPaths.findFiles(iniFileSpec)) {
                        if (iniFile != null && iniFile.exists()) {
                            final String name = iniFile.getName();
                            // If we find the requested ini file, load that.
                            try (final Reader reader = new FileReader(iniFile)) {
                                loadIni(store, reader, name, requested);
                            }
                        }
                    }
                }
                for (final Resource resource : BasicXnatResourceLocator.getResources(iniFileSpec)) {
                    try {
                        try (final Reader reader = new InputStreamReader(resource.getInputStream())) {
                            final String name = resource.getFilename();
                            loadIni(store, reader, name, requested);
                        }
                    } catch (FileNotFoundException e) {
                        _log.info("Resource {} not found. This is probably OK, sometimes Spring makes stuff up, so we'll just skip it.", resource.getFilename());
                    }
                }
            } catch (IOException | ConfigurationException e) {
                _log.error("An error occurred trying to read the ini file " + iniFileSpec, e);
            }
        }
    }

    private static void loadIni(final IniStore store, final Reader reader, final String name, final String[] requested) throws ConfigurationException, IOException {
        if (name.endsWith(".ini")) {
            loadIni(store, reader, requested);
        } else if (name.endsWith(".properties")) {
            loadProperties(store, reader, requested);
        } else {
            _log.info("Found specified init file {}, but it doesn't end in '.ini' or '.properties', so I'm not sure what to do with it.", name);
        }
    }

    private static void loadIni(final IniStore store, final Reader reader, final String... requested) throws ConfigurationException, IOException {
        final INIConfiguration ini = new INIConfiguration();
        ini.read(reader);

        final Set<String> sections = requested.length == 0 ? ini.getSections() : Sets.newHashSet(requested);
        for (final String section : sections) {
            // So check whether it contains the tool ID...
            if (ini.getSections().contains(section)) {
                // If so, we'll use this...
                final SubnodeConfiguration configuration = ini.getSection(section);
                final Iterator<String>     keys          = configuration.getKeys();
                while (keys.hasNext()) {
                    final String key = keys.next();
                    store.storeIniValue(section, key, configuration.getString(key, ""));
                }
            }
        }
    }

    private static void loadProperties(final IniStore store, final Reader reader, final String... requested) throws ConfigurationException, IOException {
        final Properties properties = new Properties();
        properties.load(reader);

        final Set<String> sections = Sets.newHashSet(requested);
        for (final String propertyName : properties.stringPropertyNames()) {
            final String[] atoms = propertyName.split("\\.", 2);
            final String   section, key;
            if (atoms.length == 1) {
                section = "";
                key = atoms[0];
            } else {
                section = atoms[0];
                key = atoms[1];
            }
            if (requested.length == 0 || sections.contains(section)) {
                store.storeIniValue(section, key, properties.getProperty(propertyName, ""));
            }
        }
    }

    private interface IniStore {
        void storeIniValue(final String section, final String key, final String value);
    }

    private static class MapIniStore implements IniStore {
        @Override
        public void storeIniValue(final String section, final String key, final String value) {
            if (!_iniMap.containsKey(section)) {
                final Map<String, String> sectionMap = Maps.newHashMap();
                _iniMap.put(section, sectionMap);
            }
            _iniMap.get(section).put(key, value);
        }

        public Map<String, Map<String, String>> getIniMap() {
            return _iniMap;
        }

        private final Map<String, Map<String, String>> _iniMap = Maps.newHashMap();
    }

    private static class PropertiesIniStore implements IniStore {
        @Override
        public void storeIniValue(final String section, final String key, final String value) {
            _properties.put(section + "." + key, value);
        }

        public Properties getIniProperties() {
            return _properties;
        }

        private final Properties _properties = new Properties();
    }

    private static final Logger _log = LoggerFactory.getLogger(IniImporter.class);
}
