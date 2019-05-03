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
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;
import org.nrg.framework.beans.Beans;
import org.nrg.framework.beans.XnatPluginBean;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xnat.services.logging.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Service
@Getter
@Accessors(prefix = "_")
@Slf4j
public class DefaultLoggingService implements LoggingService {
    @Autowired
    public DefaultLoggingService(final Path xnatHome, final DocumentBuilder builder, final Transformer transformer, final XnatPluginBeanManager beans) throws IOException, SAXException {
        INSTANCE = this;

        _xnatHome = xnatHome;
        _builder = builder;
        _transformer = transformer;

        _runnableTasks = new HashMap<>();

        _context = (LoggerContext) LoggerFactory.getILoggerFactory();
        _initializer = new ContextInitializer(_context);
        if (log.isDebugEnabled()) {
            StatusPrinter.printInCaseOfErrorsOrWarnings(_context);
        }

        _configurationResources = new HashMap<>();
        _primaryLogConfiguration = getPrimaryLogConfiguration();
        _primaryElements = new HashMap<>();

        if (_primaryLogConfiguration != null) {
            _primaryElements.put("loggers", findAllElementNames(_primaryLogConfiguration, "logger"));
            _primaryElements.put("appenders", findAllElementNames(_primaryLogConfiguration, "appender"));

            _pluginLogConfigurations = getPluginLogConfigurations(beans.getPluginBeans());

            _configurationResources.put("primary", getResourceReference(_primaryLogConfiguration));
            for (final String pluginId : _pluginLogConfigurations.keySet()) {
                final Resource resource = _pluginLogConfigurations.get(pluginId);
                _configurationResources.put(pluginId, getResourceReference(resource));
            }

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
    public <T extends Runnable> void start(final T runnable) {
        final String objectId = ObjectUtils.identityToString(runnable);
        if (_runnableTasks.containsKey(objectId)) {
            RUNNABLE_LOGGER.warn("Received a start timing request from {} of type {}, but I already have a time started for that. I'll replace the existing time, but there might be a problem with this task.", objectId, runnable.getClass().getName());
        }
        RUNNABLE_LOGGER.info("Started method {}.run() for object {}", runnable.getClass().getSimpleName(), objectId);
        _runnableTasks.put(objectId, StopWatch.createStarted());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Runnable> void update(final T runnable, final String message, final Object... parameters) {
        final String objectId = ObjectUtils.identityToString(runnable);
        if (!_runnableTasks.containsKey(objectId)) {
            RUNNABLE_LOGGER.warn("Received an update timing request from {} of type {}, but I don't have a time started for that.", objectId, runnable.getClass().getName());
            return;
        }
        RUNNABLE_LOGGER.info(message + " in method {}.run() for object {} in {} ns", ArrayUtils.addAll(parameters, runnable.getClass().getSimpleName(), objectId, _runnableTasks.get(objectId).getNanoTime()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Runnable> void finish(final T runnable) {
        final String objectId = ObjectUtils.identityToString(runnable);
        if (!_runnableTasks.containsKey(objectId)) {
            RUNNABLE_LOGGER.warn("Received a stop timing request from {} of type {}, but I don't have a time started for that.", objectId, runnable.getClass().getName());
            return;
        }
        final StopWatch stopWatch = _runnableTasks.remove(objectId);
        stopWatch.stop();
        RUNNABLE_LOGGER.info("Finished method {}.run() for object {} in {} ns", runnable.getClass().getSimpleName(), objectId, stopWatch.getNanoTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationResource(final String resourceId) throws IOException, NotFoundException {
        if (StringUtils.isBlank(resourceId) || !_configurationResources.containsKey(resourceId)) {
            throw new NotFoundException("The requested resource does not exist: " + resourceId);
        }
        try (final InputStream input = StringUtils.equalsIgnoreCase("primary", resourceId) ? _primaryLogConfiguration.getInputStream() : _pluginLogConfigurations.get(resourceId).getInputStream()) {
            return IOUtils.toString(input, StandardCharsets.UTF_8);
        }
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

        final Map<String, XnatPluginBean> loggingBeans   = new HashMap<>(Maps.filterValues(beanMap, HAS_LOGGING_CONFIG_PREDICATE));
        final Map<String, Resource>       configurations = new HashMap<>();
        final AtomicInteger               convertedCount = new AtomicInteger();
        for (final String pluginId : loggingBeans.keySet()) {
            final XnatPluginBean bean              = loggingBeans.get(pluginId);
            final String         configurationFile = bean.getLogConfigurationFile();
            if (StringUtils.isNotBlank(configurationFile)) {
                final Resource resource = BasicXnatResourceLocator.getResource("classpath:" + bean.getLogConfigurationFile());
                if (StringUtils.endsWith(resource.getFilename(), ".xml")) {
                    configurations.put(pluginId, resource);
                } else if (StringUtils.endsWith(resource.getFilename(), ".properties")) {
                    log.debug("Found a properties-based logging configuration for the plugin \"{}\", translating to logback format.", pluginId);
                    configurations.put(pluginId, convertPropertiesLogConfig(pluginId, resource, convertedLogConfigFolder));
                    convertedCount.incrementAndGet();
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
            log.info("Found {} plugin logging configurations total. Found {} using log4j properties format, which were converted to logback XML format and placed in the folder: {}", configurations.size(), convertedCount.get(), convertedLogConfigFolder);
        }
        return configurations;
    }

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
        if (log4j.containsKey("category")) {
            loggers.putAll(log4j.get("category"));
        }
        if (log4j.containsKey("logger")) {
            loggers.putAll(log4j.get("logger"));
        }
        final Properties additivity = new Properties();
        if (log4j.containsKey("additivity")) {
            additivity.putAll(log4j.get("additivity"));
        }

        final Document document    = _builder.newDocument();
        final Element  rootElement = document.createElement("configuration");
        document.appendChild(rootElement);
        if (log4j.containsKey("appender")) {
            final Map<String, Properties> appenders = Beans.getNamespacedPropertiesMap(log4j.get("appender"));
            for (final String appender : appenders.keySet()) {
                final Node appenderElement = createAppenderElement(document, appender, appenders.get(appender));
                if (appenderElement != null) {
                    rootElement.appendChild(appenderElement);
                }
            }
        }
        for (final String logger : loggers.stringPropertyNames()) {
            final Element loggerElement = createLoggerElement(document, logger, loggers.getProperty(logger), additivity.getProperty(logger, "false"));
            if (loggerElement != null) {
                rootElement.appendChild(loggerElement);
            }
        }

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
        final Element loggerElement = document.createElement("logger");
        loggerElement.setAttribute("name", logger);
        loggerElement.setAttribute("additivity", additivity);
        loggerElement.setAttribute("level", atoms.get(0));
        for (final String appender : atoms.subList(1, atoms.size())) {
            final Element appenderElement = document.createElement("appender-ref");
            appenderElement.setAttribute("ref", appender);
            loggerElement.appendChild(appenderElement);
        }
        return loggerElement;
    }

    private Element createAppenderElement(final Document document, final String appender, final Properties properties) {
        normalizePropertyNames(properties);

        final Element appenderElement = document.createElement("appender");
        appenderElement.setAttribute("name", appender);
        final String logbackAppenderClass = getLogbackAppenderClass(properties.getProperty("default"));
        appenderElement.setAttribute("class", logbackAppenderClass);
        final Element append = document.createElement("append");
        append.appendChild(document.createTextNode(properties.getProperty("append", "false")));
        appenderElement.appendChild(append);
        final Element file     = document.createElement("file");
        final String  fileName = properties.getProperty("file");
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

    private List<String> findAllElementNames(final Resource resource, final String elementName) throws IOException, SAXException {
        final List<String> names    = new ArrayList<>();
        final Document     document = _builder.parse(new InputSource(resource.getInputStream()));
        final NodeList     elements = document.getElementsByTagName(elementName);
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

    private static void normalizePropertyNames(final Properties properties) {
        // Convert any properties with uppercase letters to all lowercase. This is just to simplify "file" vs "File" etc.
        for (final String property : Iterables.filter(properties.stringPropertyNames(), Predicates.contains(Pattern.compile("[A-Z]")))) {
            final String value = properties.getProperty(property);
            properties.remove(property);
            properties.setProperty(StringUtils.lowerCase(property), value);
        }
    }

    private static final Predicate<XnatPluginBean> HAS_LOGGING_CONFIG_PREDICATE  = new Predicate<XnatPluginBean>() {
        @Override
        public boolean apply(@Nullable final XnatPluginBean bean) {
            return bean != null && StringUtils.isNotBlank(bean.getLogConfigurationFile());
        }
    };
    private static final Predicate<Resource>       RESOURCE_NOT_IN_JAR_PREDICATE = new Predicate<Resource>() {
        @Override
        public boolean apply(final Resource resource) {
            try {
                return !StringUtils.contains(resource.getURI().toString(), "jar!");
            } catch (IOException e) {
                return false;
            }
        }
    };

    private static final Logger              RUNNABLE_LOGGER = LoggerFactory.getLogger("RUNNABLE");
    private static final Map<String, String> APPENDER_MAP    = ImmutableMap.of("org.apache.log4j.ConsoleAppender", "ch.qos.logback.core.ConsoleAppender",
                                                                               "org.apache.log4j.DailyRollingFileAppender", "ch.qos.logback.core.rolling.RollingFileAppender",
                                                                               "org.apache.log4j.FileAppender", "ch.qos.logback.core.FileAppender",
                                                                               "org.apache.log4j.RollingFileAppender", "ch.qos.logback.core.rolling.RollingFileAppender");

    private static LoggingService INSTANCE;

    private final Path                      _xnatHome;
    private final DocumentBuilder           _builder;
    private final Transformer               _transformer;
    private final Resource                  _primaryLogConfiguration;
    private final Map<String, Resource>     _pluginLogConfigurations;
    private final Map<String, String>       _configurationResources;
    private final Map<String, List<String>> _primaryElements;
    private final Map<String, StopWatch>    _runnableTasks;
    private       LoggerContext             _context;
    private       ContextInitializer        _initializer;
}
