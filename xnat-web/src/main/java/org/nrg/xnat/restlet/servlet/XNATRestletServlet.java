/*
 * web: org.nrg.xnat.restlet.servlet.XNATRestletServlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.servlet;

import com.noelios.restlet.ext.servlet.ServerServlet;
import org.nrg.dcm.DicomSCPManager;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class XNATRestletServlet extends ServerServlet {
    private static final long serialVersionUID = -4149339105144231596L;

    public static ServletConfig REST_CONFIG = null;

    private final Logger logger = LoggerFactory.getLogger(XNATRestletServlet.class);

    @Override
    public void init() throws ServletException {
        super.init();

        XNATRestletServlet.REST_CONFIG = getServletConfig();

        try {
            PrearcDatabase.initDatabase(XDAT.getBoolSiteConfigurationProperty("reloadPrearcDatabaseOnStartup", false));
        } catch (Throwable e) {
            logger.error("Unable to initialize prearchive database", e);
        }

        XDAT.getContextService().getBean(DicomSCPManager.class).startOrStopDicomSCPAsDictatedByConfiguration();
    }

    @Override
    public void destroy() {
        XDAT.getContextService().getBean(DicomSCPManager.class).stopDicomSCPs();
    }
}
