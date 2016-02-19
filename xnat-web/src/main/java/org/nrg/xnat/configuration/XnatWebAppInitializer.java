package org.nrg.xnat.configuration;

import org.apache.axis.transport.http.AdminServlet;
import org.apache.axis.transport.http.AxisHTTPSessionListener;
import org.apache.axis.transport.http.AxisServlet;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.Turbine;
import org.apache.turbine.util.TurbineConfig;
import org.nrg.xdat.servlet.XDATAjaxServlet;
import org.nrg.xdat.servlet.XDATServlet;
import org.nrg.xnat.restlet.servlet.XNATRestletServlet;
import org.nrg.xnat.restlet.util.UpdateExpirationCookie;
import org.nrg.xnat.security.XnatSessionEventPublisher;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

public class XnatWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    public void onStartup(final ServletContext context) throws ServletException {
        context.setInitParameter("contextAttribute", "org.springframework.web.servlet.FrameworkServlet.CONTEXT.spring-mvc");
        context.setInitParameter("org.restlet.component", "org.nrg.xnat.restlet.XNATComponent");

        // If the context path is not empty (meaning this isn't the root application), then we'll get true: Restlet will
        // autowire its calls. If the context path is empty (meaning that this is the root application), autowire will
        // be false.
        context.setInitParameter("org.restlet.autoWire", Boolean.toString(StringUtils.isNotEmpty(context.getContextPath())));

        // Initialize the Spring stuff.
        super.onStartup(context);

        // Now initialize everything else.
        context.addFilter("updateExpirationCookie", UpdateExpirationCookie.class);

        context.addListener(XnatSessionEventPublisher.class);
        context.addListener(AxisHTTPSessionListener.class);

        Turbine.setTurbineServletConfig(new TurbineConfig("turbine", "WEB-INF/conf/TurbineResources.properties"));

        _context = context;

        addServlet(XDATServlet.class, 1, "/xdat/*");
        addServlet(Turbine.class, 2, "/app/*");
        addServlet(XNATRestletServlet.class, 2, "/REST/*", "/data/*");
        addServlet(XDATAjaxServlet.class, 3, "/ajax/*", "/servlet/XDATAjaxServlet", "/servlet/AjaxServlet");
        addServlet(AxisServlet.class, 4, "/servlet/AxisServlet", "*.jws", "/services/*");
        addServlet(AdminServlet.class, 5, "/servlet/AdminServlet");

        // TODO: Don't know how to do these things through the servlet context.
        /*
          <welcome-file-list>
            <welcome-file>index.jsp</welcome-file>
            <welcome-file>app</welcome-file>
          </welcome-file-list>
          <!-- ======================================================================== -->
          <!--                                                                          -->
          <!-- Mapping HTTP error codes and exceptions to custom error pages to make    -->
          <!-- the display a bit more pleasant and preserve system confidentiality.     -->
          <!--                                                                          -->
          <!-- ======================================================================== -->
          <error-page>
            <exception-type>java.lang.Throwable</exception-type>
            <location>/app/template/Error.vm</location>
          </error-page>
          <!-- ======================================================================== -->
          <!--                                                                          -->
          <!-- Make sure that templates, resources and logs are not available through   -->
          <!-- the servlet container. Remove security constraints or add an authen-     -->
          <!-- tication role if you need access to these paths.                         -->
          <!--                                                                          -->
          <!-- ======================================================================== -->
          // Might need to do these through Spring Security configuration:
          // http://stackoverflow.com/questions/19297796/how-to-programmatically-setup-a-security-constraint-in-servlets-3-x
          // Or move them into WEB-INF a la Spring views. Note that logs is already removed.
          <security-constraint>
            <web-resource-collection>
              <web-resource-name>templates</web-resource-name>
              <url-pattern>/templates/*</url-pattern>
            </web-resource-collection>
            <web-resource-collection>
              <web-resource-name>resources</web-resource-name>
              <url-pattern>/resources/*</url-pattern>
            </web-resource-collection>
            <auth-constraint />
          </security-constraint>
        */
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/admin/*", "/xapi/*" };
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] { RootConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[0];
    }

    private void addServlet(final Class<? extends Servlet> clazz, final int loadOnStartup, final String... mappings) {
        final String                      name = StringUtils.uncapitalize(clazz.getSimpleName());
        final ServletRegistration.Dynamic registration  = _context.addServlet(name, clazz);
        registration.setLoadOnStartup(loadOnStartup);
        registration.addMapping(mappings);
    }

    private ServletContext _context;
}
