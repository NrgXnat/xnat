/*
 * framework: org.nrg.framework.logging.RemoteEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class RemoteEvent extends HashMap<String, String> {
    /**
     * This is used to retrieve the remote logging from configuration.
     */
    @SuppressWarnings("unused")
    public static final String REMOTE_LOG = "org.nrg.xnat.remote";

    protected RemoteEvent() {
        log.debug("Creating default remote event instance");
    }

    @SuppressWarnings("unused")
    public RemoteEvent(Properties properties) {
        log.debug("Creating remote event instance from properties");
        addProperties(properties);
    }

    public Level getLevel() {
        if (containsKey("level")) {
            return Level.valueOf(get("level"));
        }
        if (containsKey("LEVEL")) {
            return Level.valueOf(get("LEVEL"));
        }
        return Level.TRACE;
    }

    public void setLevel(Level level) {
        put("level", level.toString());
    }

    public Map<String, String> getProperties() {
        return _properties;
    }

    public void setProperties(Map<String, String> properties) {
        _properties.clear();
        _properties.putAll(properties);
    }

    @Override
    public String toString() {
        try {
            // TODO: The replaceAll() call converts single quotes to work properly in escaped SQL queries. This really only needs to be done at the JDBC insert level.
            // There's a modified JDBCAppender which handles this at http://sourceforge.net/projects/jdbcappender, but it hasn't been updated since 2005. That may be OK.
            return _mapper.writeValueAsString(this).replaceAll("'", "\\\\'");
        } catch (IOException exception) {
            return "Error occurred while converting to string: " + exception.getMessage();
        }
    }

    private void addProperties(final Properties properties) {
        for (String property : properties.stringPropertyNames()) {
            final Object value = properties.get(property);
            if (value instanceof String string) {
                put(property, string);
            } else {
                try {
                    put(property, _mapper.writeValueAsString(value));
                } catch (IOException e) {
                    put(property, "Invalid value found for property: " + property);
                }
            }
        }
    }

    // TODO: This might be the wrong thing to do, since it'll result in a plethora of object mappers.
    private static final ObjectMapper _mapper = new ObjectMapper();

    private final Map<String, String> _properties = new HashMap<>();
}
