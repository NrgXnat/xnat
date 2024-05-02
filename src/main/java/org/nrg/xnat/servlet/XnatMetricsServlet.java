package org.nrg.xnat.servlet;

import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import org.nrg.xdat.XDAT;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import io.micrometer.core.instrument.MeterRegistry;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.context.support.WebApplicationContextUtils;


@SuppressWarnings("serial")
@Slf4j
public class XnatMetricsServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
      resp.setStatus(HttpServletResponse.SC_OK);
      //TODO: This is hardcoded, should be fixed
      resp.setContentType("text/plain; version=0.0.4; charset=utf-8");
      if (meterRegistry instanceof CompositeMeterRegistry) {
        if (((CompositeMeterRegistry)meterRegistry).getRegistries().size() == 0) {
          resp.getWriter().append("Metrics gathering has not been enabled on this instance.");
        }
      } else if (meterRegistry instanceof PrometheusMeterRegistry) {
        resp.getWriter().append(((PrometheusMeterRegistry)meterRegistry).scrape());
      }
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
      meterRegistry = WebApplicationContextUtils
              .getRequiredWebApplicationContext(servletConfig.getServletContext())
              .getBean(MeterRegistry.class);

    }

    private MeterRegistry meterRegistry;

}