package org.nrg.xnat.services.logging.impl;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.PropertyConfigurator;
import org.apache.turbine.Turbine;
import org.jetbrains.annotations.NotNull;
import org.nrg.framework.beans.XnatPluginBean;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xnat.initialization.XnatWebAppInitializer;
import org.nrg.xnat.services.logging.LoggingService;
import org.nrg.xnat.servlet.Log4JServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Slf4j
public class DefaultLoggingService implements LoggingService {
    @Autowired
    public DefaultLoggingService(final XnatPluginBeanManager beans) {
        _pluginLog4jProperties = getPluginLog4jResourcePaths(beans.getPluginBeans());
    }

    public static <T extends Runnable> void start(final T runnable) {
        final String objectId = ObjectUtils.identityToString(runnable);
        if (RUNNABLE_TASKS.containsKey(objectId)) {
            RUNNABLE_LOGGER.warn("Received a start timing request from {} of type {}, but I already have a time started for that. I'll replace the existing time, but there might be a problem with this task.", objectId, runnable.getClass().getName());
        }
        RUNNABLE_LOGGER.info("Started method {}.run() for object {}", runnable.getClass().getSimpleName(), objectId);
        RUNNABLE_TASKS.put(objectId, StopWatch.createStarted());
    }

    public static <T extends Runnable> void update(final T runnable, final String message, final Object... parameters) {
        final String objectId = ObjectUtils.identityToString(runnable);
        if (!RUNNABLE_TASKS.containsKey(objectId)) {
            RUNNABLE_LOGGER.warn("Received an update timing request from {} of type {}, but I don't have a time started for that.", objectId, runnable.getClass().getName());
            return;
        }
        RUNNABLE_LOGGER.info(message + " in method {}.run() for object {} in {} ns", ArrayUtils.addAll(parameters, runnable.getClass().getSimpleName(), objectId, RUNNABLE_TASKS.get(objectId).getNanoTime()));
    }

    public static <T extends Runnable> void finish(final T runnable) {
        final String objectId = ObjectUtils.identityToString(runnable);
        if (!RUNNABLE_TASKS.containsKey(objectId)) {
            RUNNABLE_LOGGER.warn("Received a stop timing request from {} of type {}, but I don't have a time started for that.", objectId, runnable.getClass().getName());
            return;
        }
        final StopWatch stopWatch = RUNNABLE_TASKS.remove(objectId);
        stopWatch.stop();
        RUNNABLE_LOGGER.info("Finished method {}.run() for object {} in {} ns", runnable.getClass().getSimpleName(), objectId, stopWatch.getNanoTime());
    }

    /**
     * Resets the logging configuration from the submitted property sets. Before resetting the configuration, the submitted properties
     * are compared against the properties used to configure the logging system previously. If the properties haven't changed, the logging
     * is not reset. This calls {@link #reset(Pair, boolean)} with the <b>force</b> parameter set to false. If you want the logging
     * configuration set regardless of whether the properties have changed, call that method with the <b>force</b> parameter set to true.
     *
     * @param log4jProperties The properties to be used when configuring the logging system
     *
     * @return The properties that were used when configuring the logging system.
     */
    public static Properties reset(final Pair<Properties, ? extends Map<String, Properties>> log4jProperties) {
        return reset(log4jProperties, false);
    }

    public static Properties reset(final Pair<Properties, ? extends Map<String, Properties>> log4jProperties, final boolean force) {
        final Properties properties = log4jProperties.getLeft();
        log.info("Loaded {} properties from the master log4j configuration", properties.size());

        final Map<String, Properties> extendedProperties = log4jProperties.getRight();
        for (final String extendedKey : extendedProperties.keySet()) {
            final Properties extended = extendedProperties.get(extendedKey);
            properties.putAll(extended);
            log.info("Loaded {} properties from the extended log4j configuration {}", extended.size(), extendedKey);
        }

        synchronized (PREVIOUS_LOG4J_CONFIG) {
            if (force || hasConfigChanged(properties)) {
                try {
                    PREVIOUS_LOG4J_CONFIG.clear();
                    PREVIOUS_LOG4J_CONFIG.putAll(properties);
                    PropertyConfigurator.configure(properties);
                    log.info("Configuring logging with a total of {} properties", properties.size());
                    return properties;
                } catch (Throwable t) {
                    throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Logging configuration failed.", t);
                }
            }
            return PREVIOUS_LOG4J_CONFIG;
        }
    }

    @NotNull
    public static List<String> getPluginLog4jResourcePaths(final Map<String, XnatPluginBean> beanMap) {
        final List<String> paths = new ArrayList<>(Lists.transform(new ArrayList<>(beanMap.keySet()), new Function<String, String>() {
            @Override
            public String apply(final String pluginId) {
                final String log4jPropertiesFile = beanMap.get(pluginId).getLog4jPropertiesFile();
                return StringUtils.isNotBlank(log4jPropertiesFile) ? "classpath*:" + log4jPropertiesFile : null;
            }
        }));
        paths.removeAll(Collections.singletonList(null));
        return ImmutableList.copyOf(paths);
    }

    public static Pair<Properties, ? extends Map<String, Properties>> getLog4jProperties(final String rootPath, final List<String> resourcePatterns, final String... exclusions) {
        final Pair<Properties, ? extends Map<String, Properties>> log4jProperties = new ImmutablePair<>(new Properties(), new HashMap<String, Properties>());

        try {
            log4jProperties.getLeft().putAll(getPrimaryLog4jProperties(rootPath));
        } catch (IOException e) {
            throw new NrgServiceRuntimeException("Failed during load of primary logging properties", e);
        }

        final String[]                fullExclusions  = ArrayUtils.addAll(exclusions, "**/WEB-INF/lib/**/*", "**/WEB-INF/classes/*");
        final Map<String, Properties> otherProperties = log4jProperties.getRight();
        otherProperties.putAll(getResourceProperties("classpath*:log4j.properties", fullExclusions));
        for (final String resourcePattern : resourcePatterns) {
            otherProperties.putAll(getResourceProperties(resourcePattern, fullExclusions));
        }

        return log4jProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Properties reset() {
        final Pair<Properties, ? extends Map<String, Properties>> log4jProperties = getLog4jProperties(Turbine.getRealPath("/"), _pluginLog4jProperties, "**/WEB-INF/lib/**/*");
        return reset(log4jProperties);
    }

    // TODO: This would be good to have in a common utils class or something.
    private static Map<String, Properties> getResourceProperties(final String pattern, final String... exclusions) {
        final Map<String, Properties> resourceProperties = new HashMap<>();
        try {
            for (final Resource resource : BasicXnatResourceLocator.getResources(pattern)) {
                try {
                    final URI uri = resource.getURI();
                    if (!isExcluded(uri, exclusions)) {
                        try {
                            resourceProperties.put(uri.toString(), PropertiesLoaderUtils.loadProperties(resource));
                        } catch (IOException e) {
                            log.error("An error occurred trying to load the properties from URI " + uri, e);
                        }
                    }
                } catch (IOException e) {
                    log.error("An error occurred trying to get the URI for resource " + resource.toString(), e);
                }
            }
        } catch (IOException e) {
            log.error("An error occurred trying to load resources based on the pattern " + pattern, e);
        }
        return resourceProperties;
    }

    private static boolean isExcluded(final URI uri, final String... exclusions) {
        final String uriPath = uri.toString();
        for (final String exclusion : exclusions) {
            if (MATCHER.match(exclusion, uriPath)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasConfigChanged(final Properties properties) {
        if (PREVIOUS_LOG4J_CONFIG.isEmpty()) {
            final ServletContext context = getServletContext();
            if (context != null) {
                try {
                    final Properties primary = getPrimaryLog4jProperties(context.getRealPath(context.getContextPath()));
                    PREVIOUS_LOG4J_CONFIG.putAll(primary);
                } catch (IOException e) {
                    log.error("Failed during load of primary logging properties", e);
                    return true;
                }
            }
        }
        return !PREVIOUS_LOG4J_CONFIG.equals(properties);
    }

    private static ServletContext getServletContext() {
        final Log4JServlet instance = Log4JServlet.getInstance();
        if (instance != null) {
            final ServletContext context = instance.getServletContext();
            if (context != null) {
                return context;
            }
        }
        return XnatWebAppInitializer.getServletContext();
    }

    private static Properties getPrimaryLog4jProperties(final String rootPath) throws IOException {
        // Try by path first...
        final Path primaryPath;
        if (StringUtils.isNotBlank(rootPath)) {
            primaryPath = Paths.get(rootPath, "WEB-INF/classes/log4j.properties");
            if (primaryPath.toFile().exists()) {
                return PropertiesLoaderUtils.loadProperties(new PathResource(primaryPath));
            }
        } else {
            primaryPath = null;
        }

        // If that didn't work (sometimes the getRealPath() method fails, so we need to fall back.
        for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:log4j.properties")) {
            final URI uri = resource.getURI();
            if (uri != null && uri.toString().contains("WEB-INF/classes")) {
                return PropertiesLoaderUtils.loadProperties(resource);
            }
        }
        throw new IOException("Can't find the log4j properties, checked " + (primaryPath != null ? primaryPath.toString() : "<blank>") + " and by URI");
    }

    private static final Logger                 RUNNABLE_LOGGER       = LoggerFactory.getLogger("RUNNABLE");
    private static final AntPathMatcher         MATCHER               = new AntPathMatcher();
    private static final Properties             PREVIOUS_LOG4J_CONFIG = new Properties();
    private static final Map<String, StopWatch> RUNNABLE_TASKS        = new HashMap<>();

    private final List<String> _pluginLog4jProperties;
}
