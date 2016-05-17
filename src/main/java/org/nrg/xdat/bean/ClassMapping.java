package org.nrg.xdat.bean;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.nrg.framework.utilities.Reflection;

import com.google.common.collect.Maps;

public class ClassMapping implements ClassMappingI {
    private static final Logger  logger                = Logger.getLogger(ClassMapping.class);
    private static final String  DEFINITION_PACKAGE    = "org.nrg.xdat.bean";
    private static final Pattern DEFINITION_PROPERTIES = Pattern.compile(".*-bean-definition\\.properties");

    private final Map<String, String> elements;

    public ClassMapping() {
        elements = Maps.newHashMap();
        final Set<String> propFiles = Reflection.findResources(DEFINITION_PACKAGE, DEFINITION_PROPERTIES);
        if (propFiles.size() > 0) {
            for (final String props : propFiles) {
                try {
                    final Configuration config = new PropertiesConfiguration(props);
                    @SuppressWarnings("unchecked")
                    final Iterator<Object> keys = config.getKeys();
                    while (keys.hasNext()) {
                        final String key = (String) keys.next();
                        final String value = config.getString(key);
                        // Sometimes the keys are loaded with the backslash, sometimes they're not...
                        elements.put(key.replace("\\", ""), value);
                    }
                } catch (ConfigurationException e) {
                    logger.error("", e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getElements() {
        return elements;
    }
}