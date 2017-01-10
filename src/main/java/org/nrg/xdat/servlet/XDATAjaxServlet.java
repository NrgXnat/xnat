/*
 * core: org.nrg.xdat.servlet.XDATAjaxServlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author timo
 */
public final class XDATAjaxServlet extends HttpServlet {
    private final static long serialVersionUID = 1L;
    private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATAjaxServlet.class);

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        doOp(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        doOp(arg0, arg1);
    }
    
    @Override
    protected void doDelete(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException,IOException {
	doOp(arg0, arg1);
    }
    
    @Override
    protected void doPut(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException,IOException {
	doOp(arg0, arg1);
    }
    
    private void doOp(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        final String classname = req.getParameter("remote-class");
        final String methodname = req.getParameter("remote-method");
       
        final ServletConfig sc = this.getServletConfig();
        
        try {
            final Class c = Class.forName(classname);
            final Object o = c.newInstance();
            
            Class[] pClasses = new Class[]{HttpServletRequest.class,HttpServletResponse.class,ServletConfig.class};
            try {
                Method m= c.getMethod(methodname, pClasses);
                Object [] objects = new Object[]{req,res,sc};
                try {
                    m.invoke(o, objects);
                } catch (IllegalArgumentException e) {
                    logger.error("",e);
                } catch (InvocationTargetException e) {
                    logger.error("",e);
                }
            } catch (SecurityException e) {
                logger.error(classname + ":" + methodname,e);
            } catch (NoSuchMethodException e) {
            	pClasses = new Class[]{HttpServletRequest.class,HttpServletResponse.class};
                try {
                    final Method m = c.getMethod(methodname, pClasses);
                    final Object[] objects = new Object[]{req,res};
                    try {
                        m.invoke(o, objects);
                    } catch (IllegalArgumentException e2) {
                        logger.error("",e);
                    } catch (InvocationTargetException e2) {
                        logger.error("",e);
                    }
                } catch (SecurityException e2) {
                    logger.error(classname + ":" + methodname,e);
                } catch (NoSuchMethodException e2) {
                    logger.error(classname + ":" + methodname,e);
                }
            }
        } catch (ClassNotFoundException e) {
            logger.error(classname + ":" + methodname,e);
        } catch (InstantiationException e) {
            logger.error(classname + ":" + methodname,e);
        } catch (IllegalAccessException e) {
            logger.error(classname + ":" + methodname,e);
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig arg0) throws ServletException {
        super.init(arg0);
    }
}
