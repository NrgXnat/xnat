/*
 * web: org.nrg.xnat.services.logging.impl.DefaultLoggingService
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.logging.impl;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.beans.Beans;
import org.nrg.framework.beans.XnatPluginBean;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xnat.services.logging.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Getter
@Accessors(prefix = "_")
@Slf4j
@SuppressWarnings("ClassWithMultipleLoggers")
public class DefaultLoggingService implements LoggingService {
    private static final Properties EMPTY_PROPERTIES = new Properties();

    @Autowired
    public DefaultLoggingService(final Path xnatHome, final DocumentBuilder builder, final Transformer transformer, final XnatPluginBeanManager beans) throws IOException, SAXException {
        INSTANCE = this;

        _xnatHome    = xnatHome;
        _xnatLogs    = _xnatHome.resolve("logs");
        _builder     = builder;
        _transformer = transformer;

        _runnableTasks = new HashMap<>();

        _context     = (LoggerContext) LoggerFactory.getILoggerFactory();
        _initializer = new ContextInitializer(_context);
        if (log.isDebugEnabled()) {
            StatusPrinter.printInCaseOfErrorsOrWarnings(_context);
        }

        _configurationResources  = new HashMap<>();
        _primaryLogConfiguration = getPrimaryLogConfiguration();
        _primaryElements         = new HashMap<>();

        if (_primaryLogConfiguration != null) {
            final Set<String> loggers = findAllElementNames(_primaryLogConfiguration, LOGGER);
            loggers.add(ROOT); // ROOT is not a <logger> element, but it is a logger, so add it directly.
            final Set<String> appenders = findAllElementNames(_primaryLogConfiguration, APPENDER);

            _pluginLogConfigurations = getPluginLogConfigurations(beans.getPluginBeans());

            _configurationResources.put(PRIMARY, getResourceReference(_primaryLogConfiguration));
            for (final String pluginId : _pluginLogConfigurations.keySet()) {
                final Resource resource = _pluginLogConfigurations.get(pluginId);
                _configurationResources.put(pluginId, getResourceReference(resource));
                loggers.addAll(findAllElementNames(resource, LOGGER));
                appenders.addAll(findAllElementNames(resource, APPENDER));
            }

            _primaryElements.put(LOGGERS, loggers);
            _primaryElements.put(APPENDERS, appenders);

            if (!_pluginLogConfigurations.isEmpty()) {
                attachPluginLogConfigurations();
            }
        } else {
            _pluginLogConfigurations = Collections.emptyMap();
        }
    }

    public static LoggingService getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> reset() {
        try {
            _context.reset();
            _initializer.configureByResource(_primaryLogConfiguration.getURL());
            attachPluginLogConfigurations();
            return new ArrayList<>(getConfigurationResources().values());
        } catch (JoranException | IOException e) {
            log.error("An error occurred trying to reset the logging configurations. I'm not sure what this means for logging on this server.", e);
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Runnable> void start(final T runnable) {
        final String executionId = getExecutionId(runnable);
        if (_runnableTasks.containsKey(executionId)) {
            RUNNABLE_LOGGER.warn("Received a start timing request from {} of type {}, but I already have a time started for that. I'll replace the existing time, but there might be a problem with this task.", executionId, runnable.getClass().getName());
        }
        RUNNABLE_LOGGER.info("Started method {}.run() for object {}", runnable.getClass().getSimpleName(), executionId);
        _runnableTasks.put(executionId, StopWatch.createStarted());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Runnable> void update(final T runnable, final String message, final Object... parameters) {
        final String executionId = getExecutionId(runnable);
        if (!_runnableTasks.containsKey(executionId)) {
            RUNNABLE_LOGGER.warn("Received an update timing request from {} of type {}, but I don't have a time started for that.", executionId, runnable.getClass().getName());
            return;
        }
        RUNNABLE_LOGGER.info("{} in method {}.run() for object {} in {} ns with parameters: {}", message, runnable.getClass().getSimpleName(), executionId, _runnableTasks.get(executionId).getNanoTime(), Arrays.stream(parameters).map(Objects::toString).collect(Collectors.joining(", ")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Runnable> void finish(final T runnable) {
        final String executionId = getExecutionId(runnable);
        if (!_runnableTasks.containsKey(executionId)) {
            RUNNABLE_LOGGER.warn("Received a stop timing request from {} of type {}, but I don't have a time started for that.", executionId, runnable.getClass().getName());
            return;
        }
        final StopWatch stopWatch = _runnableTasks.remove(executionId);
        stopWatch.stop();
        RUNNABLE_LOGGER.info("Finished method {}.run() for object {} in {} ns", runnable.getClass().getSimpleName(), executionId, stopWatch.getNanoTime());
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public Path getLogsFolder() {
        return _xnatLogs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationResource(final String resourceId) throws IOException, NotFoundException {
        if (StringUtils.isBlank(resourceId) || !_configurationResources.containsKey(resourceId)) {
            throw new NotFoundException("The requested resource does not exist: " + resourceId);
        }
        try (final InputStream input = StringUtils.equalsIgnoreCase(PRIMARY, resourceId) ? _primaryLogConfiguration.getInputStream() : _pluginLogConfigurations.get(resourceId).getInputStream()) {
            return IOUtils.toString(input, StandardCharsets.UTF_8);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Level> getLoggerLevels() {
        return getPrimaryElements().get(LOGGERS)
                                   .stream()
                                   .collect(Collectors.toMap(Function.identity(),
                                                             loggerName -> {
                                                                 try {
                                                                     return getLoggerLevel(loggerName);
                                                                 } catch (NotFoundException e) {
                                                                     log.error("Couldn't find logger {}, which shouldn't happen because I got this name from the logging configuration.", loggerName, e);
                                                                     return Level.ERROR;
                                                                 }
                                                             },
                                                             (level1, level2) -> level1,
                                                             () -> new TreeMap<>(Collator.getInstance(Locale.getDefault()))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AppenderHierarchy> getAppenders() {
        return getAppenderMap().values().stream().sorted().collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppenderHierarchy getAppender(final String appenderName) throws NotFoundException {
        return Optional.ofNullable(getAppenderMap().get(appenderName)).orElseThrow(() -> new NotFoundException("The requested appender does not exist: " + appenderName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Level getLoggerLevel(final String loggerName) throws NotFoundException {
        return getLoggerLevel(getLogger(loggerName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Level setLoggerLevel(final String loggerName, final Level level) throws NotFoundException {
        return setLoggerLevel(getLogger(loggerName), level);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Path> getLoggerPaths() {
        return getPrimaryElements().get(LOGGERS)
                                   .stream()
                                   .map(loggerName -> {
                                       try {
                                           final Path loggerPath = getLoggerPath(loggerName);
                                           if (loggerPath == null) {
                                               return null;
                                           }
                                           return ImmutablePair.of(loggerName, loggerPath);
                                       } catch (NotFoundException e) {
                                           log.error("Couldn't find logger {}, which shouldn't happen because I got this name from the logging configuration.", loggerName, e);
                                           return null;
                                       }
                                   })
                                   .filter(Objects::nonNull)
                                   .collect(Collectors.toMap(Pair::getKey,
                                                             Pair::getValue,
                                                             (path1, path2) -> path1,
                                                             () -> new TreeMap<>(Collator.getInstance(Locale.getDefault()))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getLoggerPath(final String loggerName) throws NotFoundException {
        final ch.qos.logback.core.FileAppender<?> logger = getLoggerFileAppender(loggerName);
        return logger != null ? Paths.get(logger.getFile()) : null;
    }

    private Map<String, AppenderHierarchy> getAppenderMap() {
        final Map<String, AppenderHierarchy> appenders = new TreeMap<>(Collator.getInstance(Locale.getDefault()));
        getPrimaryElements().get(LOGGERS).forEach(loggerName -> {
            final ch.qos.logback.classic.Logger logger   = _context.getLogger(loggerName);
            final Level                         level    = getLoggerLevel(logger);
            final FileAppender<?>               appender = getLoggerFileAppender(logger);
            if (appender != null) {
                appenders.computeIfAbsent(appender.getName(), appenderName -> AppenderHierarchy.from(appenderName, getLogFilePath(appender.getFile())))
                         .getLoggers().put(loggerName, level);
            } else {
                appenders.computeIfAbsent(AppenderHierarchy.CONSOLE, appenderName -> AppenderHierarchy.from())
                         .getLoggers().put(loggerName, level);
            }
        });
        return appenders;
    }

    /**
     * This takes the specified path and returns either the full path or, if the path is contained within the logs folder in the XNAT home directory,
     * just the relative path to the log file from there.
     *
     * @param file The file path for a log file
     *
     * @return The full path if not contained within the logs folder, or the relative path from the logs folder if it is.
     */
    private Path getLogFilePath(final String file) {
        final Path filePath = Paths.get(file).normalize();
        return filePath.startsWith(_xnatLogs)
               ? _xnatLogs.relativize(filePath).normalize()
               : filePath;
    }

    private Level getLoggerLevel(final ch.qos.logback.classic.Logger logger) {
        return getSlf4jLevel(ObjectUtils.getIfNull(logger.getLevel(), logger::getEffectiveLevel));
    }

    private Level setLoggerLevel(final ch.qos.logback.classic.Logger logger, final Level level) {
        final Level previousLevel = getLoggerLevel(logger);
        logger.setLevel(getLogbackLevel(level));
        return previousLevel;
    }

    private void attachPluginLogConfigurations() {
        if (!_pluginLogConfigurations.isEmpty()) {
            for (final String pluginId : _pluginLogConfigurations.keySet()) {
                final Resource resource = _pluginLogConfigurations.get(pluginId);
                try {
                    _initializer.configureByResource(resource.getURL());
                } catch (JoranException e) {
                    log.error("An error occurred parsing the configured resource {} for plugin {}. Skipping this configuration.", resource, pluginId, e);
                } catch (IOException e) {
                    log.error("An error occurred parsing the resource URL {} for plugin {}. Skipping this configuration.", resource, pluginId, e);
                }
            }
        }
    }

    private String getResourceReference(final Resource resource) {
        try {
            return resource.getURL().toString();
        } catch (IOException e) {
            final File file;
            try {
                file = resource.getFile();
                return file.getAbsolutePath();
            } catch (IOException ex) {
                try {
                    return resource.getURI().toString();
                } catch (IOException exc) {
                    log.warn("I was unable to get the URL, file, and URI of a resource. Not sure what this thing is, but I can't tell you about it. Trying description, which is probably empty.");
                    return resource.getDescription();
                }
            }
        }
    }

    @Nullable
    private Resource getPrimaryLogConfiguration() {
        final LoggerContext      context     = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ContextInitializer initializer = new ContextInitializer(context);
        final URL                url         = initializer.findURLOfDefaultConfigurationFile(true);
        if (url == null) {
            log.warn("No primary logback configuration found.");
            return null;
        }
        log.debug("Primary logback configuration found at {}", url);
        return new UrlResource(url);
    }

    @NotNull
    private Map<String, Resource> getPluginLogConfigurations(final Map<String, XnatPluginBean> beanMap) throws IOException {
        final Path convertedLogConfigFolder = Files.createTempDirectory(_xnatHome.resolve("work"), "logback-");

        final Map<String, XnatPluginBean> loggingBeans   = beanMap.entrySet().stream().filter(HAS_LOGGING_CONFIG_PREDICATE).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        final Map<String, Resource>       configurations = new HashMap<>();
        final List<String>                converted      = new ArrayList<>();
        for (final String pluginId : loggingBeans.keySet()) {
            final XnatPluginBean bean              = loggingBeans.get(pluginId);
            final String         configurationFile = bean.getLogConfigurationFile();
            if (StringUtils.isNotBlank(configurationFile)) {
                final Resource resource = BasicXnatResourceLocator.getResource("classpath:" + bean.getLogConfigurationFile());
                if (StringUtils.endsWith(resource.getFilename(), ".xml")) {
                    configurations.put(pluginId, resource);
                } else if (StringUtils.endsWith(resource.getFilename(), ".properties")) {
                    log.debug("Found a properties-based logging configuration for the plugin \"{}\", translating to logback format.", pluginId);
                    final Resource convertedLogResource = convertPropertiesLogConfig(pluginId, resource, convertedLogConfigFolder);
                    if (convertedLogResource == null) {
                        log.warn("Something went wrong trying to convert the log4j configuration for plugin with ID {}. Check the logs for errors that may indicate the cause.", pluginId);
                    } else {
                        configurations.put(pluginId, convertedLogResource);
                        converted.add(convertedLogResource.getFilename());
                    }
                } else {
                    log.warn("I don't recognize the format of the logging configuration for the plugin \"{}\", ignoring.", pluginId);
                }
            } else {
                log.debug("The plugin \"{}\" doesn't have a log configuration file specified, moving on.", pluginId);
            }
        }
        if (configurations.isEmpty()) {
            log.debug("No plugin log configurations found, deleting unused temporary folder.");
            Files.deleteIfExists(convertedLogConfigFolder);
        } else {
            if (converted.isEmpty()) {
                log.debug("Loaded {} logging configurations from plugins", configurations.size());
            } else {
                log.info("Found {} plugin logging configurations total, with {} using log4j properties format. These were converted to logback XML format and placed in the folder {}: {}", configurations.size(), converted.size(), convertedLogConfigFolder, StringUtils.join(converted, ", "));
            }
        }
        return configurations;
    }

    @Nullable
    private Resource convertPropertiesLogConfig(final String pluginId, final Resource resource, final Path convertedLogConfigFolder) {
        final Properties properties = new Properties();
        try {
            properties.load(resource.getInputStream());
        } catch (IOException e) {
            log.error("An error occurred trying to load the specified log configuration for the plugin \"{}\". Skipping for now.", pluginId, e);
            return null;
        }

        final Map<String, Properties> log4j   = Beans.getNamespacedPropertiesMap(properties, "log4j");
        final Properties              loggers = new Properties();
        if (log4j.containsKey(CATEGORY)) {
            loggers.putAll(log4j.get(CATEGORY));
        }
        if (log4j.containsKey(LOGGER)) {
            loggers.putAll(log4j.get(LOGGER));
        }
        final Properties additivity = new Properties();
        if (log4j.containsKey(ADDITIVITY)) {
            additivity.putAll(log4j.get(ADDITIVITY));
        }

        final Document document    = _builder.newDocument();
        final Element  rootElement = document.createElement(CONFIGURATION);
        document.appendChild(rootElement);
        Stream.concat(Beans.getNamespacedPropertiesMap(log4j.getOrDefault(APPENDER, EMPTY_PROPERTIES)).entrySet().stream()
                           .map(entry -> createAppenderElement(document, entry.getKey(), entry.getValue())),
                      loggers.stringPropertyNames().stream()
                             .map(logger -> createLoggerElement(document, logger, loggers.getProperty(logger), additivity.getProperty(logger, "false")))
                             .filter(Objects::nonNull))
              .forEach(rootElement::appendChild);

        final File outputFile = convertedLogConfigFolder.resolve(pluginId + "-logback.xml").toFile();
        log.info("Converting log configuration for plugin \"{}\" to logback configuration. You can find the translated results in the file \"{}\".", pluginId, outputFile);
        try (final OutputStream outputStream = new FileOutputStream(outputFile)) {
            _transformer.transform(new DOMSource(document), new StreamResult(outputStream));
        } catch (FileNotFoundException e) {
            log.error("Got a file not found exception trying to write out the file \"{}\" for the plugin \"{}\". This really shouldn't happen.", outputFile.getAbsolutePath(), pluginId, e);
        } catch (TransformerException | IOException e) {
            log.error("An error occurred trying to write out the file \"{}\" for the plugin \"{}\". I'm not sure what your results will be from this conversion operation.", outputFile.getAbsolutePath(), pluginId, e);
        }

        return new FileSystemResource(outputFile);
    }

    private Element createLoggerElement(final Document document, final String logger, final String property, final String additivity) {
        final List<String> atoms = Arrays.asList(property.split("\\s*,\\s*"));
        if (atoms.size() < 2) {
            log.warn("The logger '{}' doesn't seem to be properly formed. Should include a logging level and at least one appender, but was set to \"{}\". Ignoring.", logger, property);
            return null;
        }

        final Element loggerElement = document.createElement(LOGGER);
        loggerElement.setAttribute(NAME, logger);
        loggerElement.setAttribute(ADDITIVITY, additivity);
        loggerElement.setAttribute(LEVEL, atoms.get(0));
        for (final String appender : atoms.subList(1, atoms.size())) {
            final Element appenderElement = document.createElement(APPENDER_REF);
            appenderElement.setAttribute(REF, appender);
            loggerElement.appendChild(appenderElement);
        }
        return loggerElement;
    }

    private Element createAppenderElement(final Document document, final String appender, final Properties properties) {
        normalizePropertyNames(properties);

        final Element appenderElement = document.createElement(APPENDER);
        appenderElement.setAttribute(NAME, appender);
        final String logbackAppenderClass = getLogbackAppenderClass(properties.getProperty("default"));
        appenderElement.setAttribute("class", logbackAppenderClass);
        final Element append = document.createElement("append");
        append.appendChild(document.createTextNode(properties.getProperty("append", "false")));
        appenderElement.appendChild(append);
        final Element file     = document.createElement(FILE);
        final String  fileName = properties.getProperty(FILE);
        file.appendChild(document.createTextNode(fileName));
        appenderElement.appendChild(file);
        final Element encoder = document.createElement("encoder");
        final Element pattern = document.createElement("pattern");
        pattern.appendChild(document.createTextNode(properties.getProperty("layout.conversionPattern", "%d [%t] %-5p %c - %m%n")));
        encoder.appendChild(pattern);
        appenderElement.appendChild(encoder);
        if (properties.containsKey("threshold") || properties.containsKey("Threshold")) {
            final Element threshold = document.createElement("filter");
            threshold.setAttribute("class", "ch.qos.logback.classic.filter.ThresholdFilter");
            final Element level = document.createElement("level");
            level.appendChild(document.createTextNode(properties.getProperty("threshold", properties.getProperty("Threshold"))));
        }
        if (StringUtils.equals(logbackAppenderClass, "ch.qos.logback.core.rolling.RollingFileAppender")) {
            final Element rollingPolicy = document.createElement("rollingPolicy");
            rollingPolicy.setAttribute("class", "ch.qos.logback.core.rolling.TimeBasedRollingPolicy");
            final Element fileNamePattern = document.createElement("fileNamePattern");
            fileNamePattern.appendChild(document.createTextNode(fileName + ".%d{yyyy-MM-dd}"));
            rollingPolicy.appendChild(fileNamePattern);
            appenderElement.appendChild(rollingPolicy);
        }
        return appenderElement;
    }

    private String getLogbackAppenderClass(final String log4jAppenderClass) {
        if (APPENDER_MAP.containsKey(log4jAppenderClass)) {
            return APPENDER_MAP.get(log4jAppenderClass);
        }
        return "org.apache.log4j.ConsoleAppender";
    }

    private Set<String> findAllElementNames(final Resource resource, final String elementName) throws IOException, SAXException {
        final Set<String> names    = new TreeSet<>();
        final Document    document = _builder.parse(new InputSource(resource.getInputStream()));
        final NodeList    elements = document.getElementsByTagName(elementName);
        for (int index = 0; index < elements.getLength(); index++) {
            final Node         element    = elements.item(index);
            final NamedNodeMap attributes = element.getAttributes();
            final Node         name       = attributes.getNamedItem("name");
            if (name != null) {
                names.add(name.getNodeValue());
            }
        }
        return names;
    }

    private ch.qos.logback.classic.Logger getLogger(final String loggerName) throws NotFoundException {
        final ch.qos.logback.classic.Logger logger = _context.getLogger(loggerName);
        if (logger == null) {
            throw new NotFoundException("The requested logger does not exist: " + loggerName);
        }
        return logger;
    }

    private ch.qos.logback.core.FileAppender<?> getLoggerFileAppender(final String loggerName) throws NotFoundException {
        return getLoggerFileAppender(getLogger(loggerName));
    }

    private ch.qos.logback.core.FileAppender<?> getLoggerFileAppender(final ch.qos.logback.classic.Logger logger) {
        final Iterator<Appender<ILoggingEvent>> iterator = logger.iteratorForAppenders();
        while (iterator.hasNext()) {
            final Appender<ILoggingEvent> appender = iterator.next();
            if (FileAppender.class.isAssignableFrom(appender.getClass())) {
                return (FileAppender<ILoggingEvent>) appender;
            }
        }
        return null;
    }

    /**
     * Creates a unique ID for the runnable execution based on object ID and thread name and ID.
     *
     * @param runnable The runnable task
     * @param <T>      A type that extends Runnable
     *
     * @return The ID generated from object ID and thread name and ID
     */
    @NotNull
    private static <T extends Runnable> String getExecutionId(final T runnable) {
        return ObjectUtils.identityToString(runnable) + ":" + Thread.currentThread().getName();
    }

    private static Level getSlf4jLevel(final ch.qos.logback.classic.Level level) {
        switch (level.toInt()) {
            case ch.qos.logback.classic.Level.WARN_INT:
                return Level.WARN;
            case ch.qos.logback.classic.Level.INFO_INT:
                return Level.INFO;
            case ch.qos.logback.classic.Level.DEBUG_INT:
                return Level.DEBUG;
            case ch.qos.logback.classic.Level.TRACE_INT:
                return Level.TRACE;
        }
        return Level.ERROR;
    }

    private static ch.qos.logback.classic.Level getLogbackLevel(final Level level) {
        switch (level) {
            case WARN:
                return ch.qos.logback.classic.Level.WARN;
            case INFO:
                return ch.qos.logback.classic.Level.INFO;
            case DEBUG:
                return ch.qos.logback.classic.Level.DEBUG;
            case TRACE:
                return ch.qos.logback.classic.Level.TRACE;
        }
        return ch.qos.logback.classic.Level.ERROR;
    }

    private static void normalizePropertyNames(final Properties properties) {
        // Convert any properties with uppercase letters to all lowercase. This is just to simplify "file" vs "File" etc.
        final Pattern     pattern       = Pattern.compile("[A-Z]");
        final Set<String> propertyNames = properties.stringPropertyNames();
        propertyNames.stream().filter(property -> pattern.matcher(property).matches()).forEach(property -> {
            final String value = properties.getProperty(property);
            properties.remove(property);
            properties.setProperty(StringUtils.lowerCase(property), value);
        });
    }

    private static final Predicate<Map.Entry<String, XnatPluginBean>> HAS_LOGGING_CONFIG_PREDICATE = entry -> entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().getLogConfigurationFile());
    private static final Logger                                       RUNNABLE_LOGGER              = LoggerFactory.getLogger("RUNNABLE");
    private static final Map<String, String>                          APPENDER_MAP                 = ImmutableMap.of("org.apache.log4j.ConsoleAppender", "ch.qos.logback.core.ConsoleAppender",
                                                                                                                     "org.apache.log4j.DailyRollingFileAppender", "ch.qos.logback.core.rolling.RollingFileAppender",
                                                                                                                     "org.apache.log4j.FileAppender", "ch.qos.logback.core.FileAppender",
                                                                                                                     "org.apache.log4j.RollingFileAppender", "ch.qos.logback.core.rolling.RollingFileAppender");

    private static LoggingService INSTANCE;

    private final Path                     _xnatHome;
    private final Path                     _xnatLogs;
    private final DocumentBuilder          _builder;
    private final Transformer              _transformer;
    private final Resource                 _primaryLogConfiguration;
    private final Map<String, Resource>    _pluginLogConfigurations;
    private final Map<String, String>      _configurationResources;
    private final Map<String, Set<String>> _primaryElements;
    private final Map<String, StopWatch>   _runnableTasks;
    private final LoggerContext            _context;
    private final ContextInitializer       _initializer;
}
