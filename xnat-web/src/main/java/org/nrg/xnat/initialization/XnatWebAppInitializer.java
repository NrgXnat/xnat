/*
 * web: org.nrg.xnat.initialization.XnatWebAppInitializer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

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
import org.nrg.xnat.configuration.ApplicationConfig;
import org.nrg.xnat.restlet.servlet.XNATRestletServlet;
import org.nrg.xnat.restlet.util.UpdateExpirationCookie;
import org.nrg.xnat.security.XnatSessionEventPublisher;
import org.nrg.xnat.servlet.ArchiveServlet;
import org.nrg.xnat.servlet.Log4JServlet;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
@Slf4j
public class XnatWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    public static final Class<?>[] EMPTY_ARRAY = new Class<?>[0];

    @Override
    public void onStartup(final ServletContext context) throws ServletException {
        context.setInitParameter("org.restlet.component", "org.nrg.xnat.restlet.XNATComponent");

        // If the context path is not empty (meaning this isn't the root application), then we'll get true: Restlet will
        // autowire its calls. If the context path is empty (meaning that this is the root application), autowire will
        // be false.
        context.setInitParameter("org.restlet.autoWire", Boolean.toString(StringUtils.isNotEmpty(context.getContextPath())));

        // Initialize the Spring stuff.
        super.onStartup(context);

        // Now initialize everything else.
        // context.addFilter("springSecurityFilterChain", DelegatingFilterProxy.class).addMappingForUrlPatterns(null, false, "/*");
        context.addFilter("updateExpirationCookie", UpdateExpirationCookie.class).addMappingForUrlPatterns(null, false, "/*");

        context.addListener(XnatSessionEventPublisher.class);
        context.addListener(AxisHTTPSessionListener.class);

        Turbine.setTurbineServletConfig(new XnatTurbineConfig(context));

        _context = context;

        addServlet(XDATServlet.class, 1, "/xdat/*");
        addServlet(Turbine.class, 2, "/app/*");
        addServlet(XNATRestletServlet.class, 2, "/REST/*", "/data/*");
        addServlet(Log4JServlet.class, 3, "/updatelog4j/*");
        addServlet(XDATAjaxServlet.class, 4, "/ajax/*", "/servlet/XDATAjaxServlet", "/servlet/AjaxServlet");
        addServlet(AxisServlet.class, 5, "/servlet/AxisServlet", "*.jws", "/services/*");
        addServlet(AdminServlet.class, 6, "/servlet/AdminServlet");
        addServlet(ArchiveServlet.class, 7, "/archive/*");
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/admin/*", "/xapi/*"};
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        final List<Class<?>> configClasses = new ArrayList<>();
        configClasses.add(RootConfig.class);
        configClasses.add(PropertiesConfig.class);
        configClasses.add(DatabaseConfig.class);
        configClasses.add(SecurityConfig.class);
        configClasses.add(ApplicationConfig.class);
        configClasses.add(NodeConfig.class);
        configClasses.addAll(getPluginConfigs());
        configClasses.add(ControllerConfig.class);
        return configClasses.toArray(new Class[configClasses.size()]);
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return EMPTY_ARRAY;
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setMultipartConfig(getMultipartConfigElement());
    }

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
        final String prefix = "xnat_" + Long.toString(System.nanoTime());
        try {
            final Path path = Paths.get(root, subfolder);
            //noinspection ResultOfMethodCallIgnored
            path.toFile().mkdirs();
            final Path tmpDir = Files.createTempDirectory(path, prefix);
            tmpDir.toFile().deleteOnExit();
            return new MultipartConfigElement(tmpDir.toAbsolutePath().toString(), MAX_FILE_SIZE, MAX_REQUEST_SIZE, FILE_SIZE_THRESHOLD);
        } catch (IOException e) {
            throw new NrgServiceRuntimeException("An error occurred trying to create the temp folder " + prefix + " in the containing folder " + root);
        }
    }

    private static final long MAX_FILE_SIZE = 1048576 * 20; // 20 MB max file size.

    private static final long MAX_REQUEST_SIZE = 20971520;  // 20MB max request size.

    private static final int FILE_SIZE_THRESHOLD = 0; // Threshold turned off.

    private List<Class<?>> getPluginConfigs() {
        final List<Class<?>> configs = new ArrayList<>();
        try {
            for (final XnatPluginBean plugin : XnatPluginBeanManager.scanForXnatPluginBeans().values()) {
                if (log.isInfoEnabled()) {
                    log.info("Found plugin {} {}: {}", plugin.getId(), plugin.getName(), plugin.getDescription());
                }
                configs.add(Class.forName(plugin.getPluginClass()));
            }
        } catch (ClassNotFoundException e) {
            log.error("Did not find a class specified in a plugin definition.", e);
        }

        log.info("Found a total of {} plugins", configs.size());
        return configs;
    }

    private void addServlet(final Class<? extends Servlet> clazz, final int loadOnStartup, final String... mappings) {
        final String name = StringUtils.uncapitalize(clazz.getSimpleName());
        final ServletRegistration.Dynamic registration = _context.addServlet(name, clazz);
        registration.setLoadOnStartup(loadOnStartup);
        registration.addMapping(mappings);
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

        private ServletContext _context;
    }

    private ServletContext _context;
}
