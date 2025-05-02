package org.nrg.xnat.services.logging.impl;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

@Value
@Builder
public class AppenderHierarchy implements Comparable<AppenderHierarchy> {
    public static final String CONSOLE = "console";

    /**
     * The name of the appender.
     */
    String name;

    /**
     * The path to the appender's log file. May be null if the appender is not a file appender.
     */
    String path;

    /**
     * Loggers associated with this appender. The key is the logger name, and the value is the logging level.
     */
    @Singular
    Map<String, Level> loggers = new TreeMap<>();

    /**
     * Returns an initialized hierarchy with a default console appender and null path.
     *
     * @return Initialized hierarchy with default console appender.
     */
    public static AppenderHierarchy from() {
        return AppenderHierarchy.builder().name("console").build();
    }

    /**
     * Returns an initialized hierarchy with the appender name and path set to the submitted values.
     *
     * @param appender The name of the appender.
     * @param path     The path to the appender's log file.
     *
     * @return Initialized hierarchy with appender and log-file path set.
     */
    public static AppenderHierarchy from(final String appender, final Path path) {
        return AppenderHierarchy.builder().name(appender).path(path.toString()).build();
    }

    @Override
    public int compareTo(final AppenderHierarchy other) {
        return StringUtils.compare(name, other.name);
    }
}
