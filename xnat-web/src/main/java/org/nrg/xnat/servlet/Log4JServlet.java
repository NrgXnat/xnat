/*
 * web: org.nrg.xnat.servlet.ArchiveServlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.servlet;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.xnat.services.logging.impl.DefaultLoggingService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("serial")
@Slf4j
public class Log4JServlet extends HttpServlet {
    public Log4JServlet() {
        _instance = this;
    }

    public static Log4JServlet getInstance() {
        return _instance;
    }

    /**
     * Initializes the logging system based on the configured application settings.
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        loadProperties(config.getServletContext().getRealPath(""));
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        response.sendRedirect("/xapi/logs/reset");
    }

    private void loadProperties(final String rootPath) {
        final List<String> paths   = DefaultLoggingService.getPluginLog4jResourcePaths(XnatPluginBeanManager.scanForXnatPluginBeans());
        final Properties   results = DefaultLoggingService.reset(DefaultLoggingService.getLog4jProperties(rootPath, paths));
        log.info("Completed initial configuration for log4j from {} properties", results.size());
        if (log.isTraceEnabled()) {
            log.trace("Configuration properties:");
            try (final StringWriter writer = new StringWriter()) {
                results.store(writer, "Generated properties for XNAT log4j configuration");
                log.trace(writer.getBuffer().toString());
            } catch (IOException e) {
                log.warn("An error occurred trying to write the log4j properties", e);
            }
        }
    }

    private static Log4JServlet _instance;
}
