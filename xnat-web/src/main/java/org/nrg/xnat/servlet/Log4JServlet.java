/*
 * web: org.nrg.xnat.servlet.ArchiveServlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.servlet;

import org.apache.log4j.PropertyConfigurator;
import org.apache.turbine.Turbine;
import org.nrg.framework.beans.XnatPluginBean;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@SuppressWarnings("serial")
public class Log4JServlet extends HttpServlet {

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        if(org.nrg.xdat.security.helpers.Roles.isSiteAdmin(org.nrg.xdat.XDAT.getUserDetails())){
            loadProperties();
        }
        else{
            throw new ServletException("Only admins can access Log4jServlet.");
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig arg0) throws ServletException {
        loadProperties();
    }

    private void loadProperties() throws ServletException {
        Properties properties = new Properties();
        try {
            File file = new File(Turbine.getRealPath("/WEB-INF/classes/log4j.properties"));
            FileInputStream fileInput = new FileInputStream(file);
            properties.load(fileInput);
            fileInput.close();
        } catch (Throwable t) {
            throw new ServletException("Log4jServlet loading of properties failed.", t);
        }
        try {
            for (final XnatPluginBean plugin : XnatPluginBeanManager.scanForXnatPluginBeans().values()) {
                try{
                    String log4jPropertiesFile = plugin.getLog4jPropertiesFile();
                    if(!org.apache.commons.lang.StringUtils.isEmpty(log4jPropertiesFile)) {
                        for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:"+log4jPropertiesFile)) {
                            Properties pluginProperties = PropertiesLoaderUtils.loadProperties(resource);
                            for(Object propertyKey : pluginProperties.keySet()){
                                properties.setProperty(propertyKey.toString(), pluginProperties.getProperty(propertyKey.toString()));
                            }
                        }
                    }
                } catch (Throwable t) {
                    throw new ServletException("Log4jServlet loading of properties failed for plugin "+plugin.getName()+".", t);
                }
            }
        } catch (Throwable t) {
            throw new ServletException("Log4jServlet loading of plugin properties failed.", t);
        }
        try{
            PropertyConfigurator.configure(properties);
        } catch (Throwable t) {
            throw new ServletException("Log4jServlet configuring of properties failed.", t);
        }
    }
}
