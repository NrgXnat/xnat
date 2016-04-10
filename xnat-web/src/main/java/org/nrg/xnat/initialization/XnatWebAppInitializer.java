package org.nrg.xnat.initialization;

import org.apache.axis.transport.http.AdminServlet;
import org.apache.axis.transport.http.AxisHTTPSessionListener;
import org.apache.axis.transport.http.AxisServlet;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.Turbine;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.processors.XnatPluginBean;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.servlet.XDATAjaxServlet;
import org.nrg.xdat.servlet.XDATServlet;
import org.nrg.xnat.restlet.servlet.XNATRestletServlet;
import org.nrg.xnat.restlet.util.UpdateExpirationCookie;
import org.nrg.xnat.security.XnatSessionEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class XnatWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

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
        context.addFilter("springSecurityFilterChain", DelegatingFilterProxy.class).addMappingForUrlPatterns(null, false, "/*");
        context.addFilter("updateExpirationCookie", UpdateExpirationCookie.class).addMappingForUrlPatterns(null, false, "/*");

        context.addListener(XnatSessionEventPublisher.class);
        context.addListener(AxisHTTPSessionListener.class);

        Turbine.setTurbineServletConfig(new XnatTurbineConfig(context));

        _context = context;

        addServlet(XDATServlet.class, 1, "/xdat/*");
        addServlet(Turbine.class, 2, "/app/*");
        addServlet(XNATRestletServlet.class, 2, "/REST/*", "/data/*");
        addServlet(XDATAjaxServlet.class, 3, "/ajax/*", "/servlet/XDATAjaxServlet", "/servlet/AjaxServlet");
        addServlet(AxisServlet.class, 4, "/servlet/AxisServlet", "*.jws", "/services/*");
        addServlet(AdminServlet.class, 5, "/servlet/AdminServlet");
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] {"/admin/*", "/xapi/*"};
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        final List<Class<?>> configClasses = new ArrayList<>();
        configClasses.add(RootConfig.class);
        configClasses.addAll(getPluginConfigs());
        return configClasses.toArray(new Class[configClasses.size()]);
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[0];
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
            for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/**/*-plugin.properties")) {
                final Properties     properties = PropertiesLoaderUtils.loadProperties(resource);
                final XnatPluginBean plugin     = new XnatPluginBean(properties);
                final Class<?>       config     = plugin.getConfigClass();
                configs.add(config);
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred trying to locate XNAT plugin definitions.");
        } catch (ClassNotFoundException e) {
            _log.error("Did not find a class specified in a plugin definition.", e);
        }

        return configs;
    }

    private void addServlet(final Class<? extends Servlet> clazz, final int loadOnStartup, final String... mappings) {
        final String                      name         = StringUtils.uncapitalize(clazz.getSimpleName());
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

    private static final Logger _log = LoggerFactory.getLogger(XnatWebAppInitializer.class);
    private ServletContext _context;
}
