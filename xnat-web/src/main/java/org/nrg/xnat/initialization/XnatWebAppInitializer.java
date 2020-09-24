/*
 * web: org.nrg.xnat.initialization.XnatWebAppInitializer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

import ch.qos.logback.classic.servlet.LogbackServletContextListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.axis.transport.http.AdminServlet;
import org.apache.axis.transport.http.AxisHTTPSessionListener;
import org.apache.axis.transport.http.AxisServlet;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.Turbine;
import org.nrg.framework.beans.XnatPluginBean;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.servlet.XDATAjaxServlet;
import org.nrg.xdat.servlet.XDATServlet;
import org.nrg.xnat.restlet.servlet.XNATRestletServlet;
import org.nrg.xnat.security.XnatSessionEventPublisher;
import org.nrg.xnat.servlet.ArchiveServlet;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_CLASS_ARRAY;

@SuppressWarnings("unused")
@Slf4j
public class XnatWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    public static ServletContext getServletContext() {
        return SERVLET_CONTEXT;
    }

    @Override
    public void onStartup(final ServletContext context) throws ServletException {
        context.addListener(new LogbackServletContextListener());

        context.setInitParameter("org.restlet.component", "org.nrg.xnat.restlet.XNATComponent");

        // If the context path is not empty (meaning this isn't the root application), then we'll get true: Restlet will
        // autowire its calls. If the context path is empty (meaning that this is the root application), autowire will
        // be false.
        context.setInitParameter("org.restlet.autoWire", Boolean.toString(StringUtils.isNotEmpty(context.getContextPath())));

        // Initialize the Spring stuff.
        super.onStartup(context);

        context.addListener(XnatSessionEventPublisher.class);
        context.addListener(AxisHTTPSessionListener.class);

        Turbine.setTurbineServletConfig(new XnatTurbineConfig(context));

        SERVLET_CONTEXT = context;

        addServlet(XDATServlet.class, 1, "/xdat/*");
        addServlet(Turbine.class, 2, "/app/*");
        addServlet(XNATRestletServlet.class, 2, "/REST/*", "/data/*");
        addServlet(XDATAjaxServlet.class, 4, "/ajax/*", "/servlet/XDATAjaxServlet", "/servlet/AjaxServlet");
        addServlet(AxisServlet.class, 5, "/servlet/AxisServlet", "*.jws", "/services/*");
        addServlet(AdminServlet.class, 6, "/servlet/AdminServlet");
        addServlet(ArchiveServlet.class, 7, "/archive/*");
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/admin/*", "/xapi/*", "/pages/*", "/schemas/*"};
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        final List<Class<?>> configClasses = new ArrayList<>();
        configClasses.add(RootConfig.class);
        configClasses.addAll(getPluginConfigs());
        configClasses.add(ControllerConfig.class);
        return configClasses.toArray(new Class[0]);
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return EMPTY_CLASS_ARRAY;
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setMultipartConfig(getMultipartConfigElement());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private MultipartConfigElement getMultipartConfigElement() {
        final String root;
        final String subfolder;
        if (StringUtils.isNotBlank(System.getProperty("xnat.home"))) {
            root = System.getProperty("xnat.home");
            subfolder = "work";
        } else {
            root = System.getProperty("java.io.tmpdir");
            subfolder = "xnat";
        }
        final String prefix = "xnat_" + System.nanoTime();
        try {
            log.debug("Found root folder {} with subfolder {}, will try to create a temporary folder with prefix {}", root, subfolder, prefix);
            final Path workPath = Paths.get(root, subfolder);
            workPath.toFile().mkdirs();
            final Path tmpDir = Files.createTempDirectory(workPath, prefix);
            tmpDir.toFile().deleteOnExit();
            final File config = Paths.get(root, "config/xnat-conf.properties").toFile();
            final long maxFileSize, maxRequestSize;
            final int  fileSizeThreshold;
            if (config.exists() && config.isFile() && config.canRead()) {
                final Properties properties = PropertiesLoaderUtils.loadProperties(new FileSystemResource(config));
                maxFileSize = getMultipartSetting(PROPERTY_MAX_FILE_SIZE, "max file size", properties.getProperty(PROPERTY_MAX_FILE_SIZE), DEFAULT_MAX_FILE_SIZE);
                maxRequestSize = getMultipartSetting(PROPERTY_MAX_REQUEST_SIZE, "max request size", properties.getProperty(PROPERTY_MAX_REQUEST_SIZE), DEFAULT_MAX_REQUEST_SIZE);
                fileSizeThreshold = (int) getMultipartSetting(PROPERTY_FILE_SIZE_THRESHOLD, "file size threshold", properties.getProperty(PROPERTY_FILE_SIZE_THRESHOLD), DEFAULT_FILE_SIZE_THRESHOLD);
            } else {
                log.debug("No configuration properties found, setting max file size, max request size, and file size threshold to their default values: {}, {}, {}", DEFAULT_MAX_FILE_SIZE, DEFAULT_MAX_REQUEST_SIZE, DEFAULT_FILE_SIZE_THRESHOLD);
                maxFileSize = DEFAULT_MAX_FILE_SIZE;
                maxRequestSize = DEFAULT_MAX_REQUEST_SIZE;
                fileSizeThreshold = DEFAULT_FILE_SIZE_THRESHOLD;
            }
            return new MultipartConfigElement(tmpDir.toAbsolutePath().toString(), maxFileSize, maxRequestSize, fileSizeThreshold);
        } catch (IOException e) {
            throw new NrgServiceRuntimeException("An error occurred trying to create the temp folder " + prefix + " in the containing folder " + root, e);
        }
    }

    private List<Class<?>> getPluginConfigs() {
        final List<Class<?>> configs = new ArrayList<>();
        try {
            for (final XnatPluginBean plugin : XnatPluginBeanManager.scanForXnatPluginBeans().values()) {
                log.info("Found plugin {} {}: {}", plugin.getId(), plugin.getName(), plugin.getDescription());
                configs.add(Class.forName(plugin.getPluginClass()));
            }
        } catch (ClassNotFoundException e) {
            log.error("Did not find a class specified in a plugin definition.", e);
        }

        log.info("Found a total of {} plugins", configs.size());
        return configs;
    }

    private void addServlet(final Class<? extends Servlet> clazz, final int loadOnStartup, final String... mappings) {
        final String                      name         = StringUtils.uncapitalize(clazz.getSimpleName());
        final ServletRegistration.Dynamic registration = SERVLET_CONTEXT.addServlet(name, clazz);
        registration.setLoadOnStartup(loadOnStartup);
        registration.addMapping(mappings);
    }

    private static long getMultipartSetting(final String property, final String name, final String propertyValue, final long defaultValue) {
        if (StringUtils.isNotBlank(propertyValue)) {
            log.debug("Found {} property, setting {} to: {}", property, name, propertyValue);
            return Long.parseLong(propertyValue);
        }
        log.debug("No {} property found, setting {} to default: {}", property, name, defaultValue);
        return defaultValue;
    }

    private static class XnatTurbineConfig implements ServletConfig {
        XnatTurbineConfig(final ServletContext context) {
            _context = context;
        }

        @Override
        public String getServletName() {
            return "Turbine";
        }

        @Override
        public ServletContext getServletContext() {
            return _context;
        }

        @Override
        public String getInitParameter(final String s) {
            if (s.equals("properties")) {
                return "WEB-INF/conf/TurbineResources.properties";
            }
            return null;
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            final List<String> parameters = new ArrayList<>();
            parameters.add("properties");
            return Collections.enumeration(parameters);
        }

        private final ServletContext _context;
    }

    private static final int            ONE_MB                       = 1048576;
    private static final long           DEFAULT_MAX_FILE_SIZE        = 20 * ONE_MB; // Default 20 MB max file size.
    private static final long           DEFAULT_MAX_REQUEST_SIZE     = 20 * ONE_MB; // Default 20 MB max request size.
    private static final int            DEFAULT_FILE_SIZE_THRESHOLD  = ONE_MB * 10; // Default file size threshold set to 10 MB.
    private static final String         PROPERTY_MAX_FILE_SIZE       = "spring.http.multipart.max-file-size";
    private static final String         PROPERTY_MAX_REQUEST_SIZE    = "spring.http.multipart.max-request-size";
    private static final String         PROPERTY_FILE_SIZE_THRESHOLD = "spring.http.multipart.file-size-threshold";
    private static       ServletContext SERVLET_CONTEXT              = null;
}
