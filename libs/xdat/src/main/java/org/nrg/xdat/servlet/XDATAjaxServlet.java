/*
 * core: org.nrg.xdat.servlet.XDATAjaxServlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.servlet;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.security.UserI;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author timo
 */
@Slf4j
public final class XDATAjaxServlet extends HttpServlet {

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {
        doOp(request, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) {
        doOp(request, response);
    }

    @Override
    protected void doDelete(final HttpServletRequest request, final HttpServletResponse response) {
        doOp(request, response);
    }

    @Override
    protected void doPut(final HttpServletRequest request, final HttpServletResponse response) {
        doOp(request, response);
    }

    private void doOp(final HttpServletRequest request, final HttpServletResponse response) {
        final String   className = request.getParameter("remote-class");
        final Class<?> clazz;
        final Object   object;
        try {
            clazz = Class.forName(className);
            object = clazz.newInstance();
        } catch (ClassNotFoundException e) {
            log.error("Couldn't find the {} class", className, e);
            AccessLogger.LogAjaxServiceAccess(getUsername(), request, BAD_REQUEST);
            return;
        } catch (InstantiationException e) {
            log.error("Couldn't create an instance of the {} class (maybe no default constructor?)", className, e);
            AccessLogger.LogAjaxServiceAccess(getUsername(), request, BAD_REQUEST);
            return;
        } catch (IllegalAccessException e) {
            log.error("Can't access the default constructor for the {} class", className, e);
            AccessLogger.LogAjaxServiceAccess(getUsername(), request, BAD_REQUEST);
            return;
        }

        final String methodName = request.getParameter("remote-method");
        try {
            callClassMethod(clazz, object, methodName, request, response, getServletConfig());
        } catch (NoSuchMethodException e) {
            try {
                callClassMethod(clazz, object, methodName, request, response);
            } catch (NoSuchMethodException e2) {
                log.error("Couldn't find the {}.{}(HttpServletRequest, HttpServletResponse, ServletConfig) method", className, methodName, e);
                AccessLogger.LogAjaxServiceAccess(getUsername(), request, BAD_REQUEST);
            }
        }
    }

    private void callClassMethod(final Class<?> clazz, final Object instance, final String methodName, final Object... objects) throws NoSuchMethodException {
        final HttpServletRequest request = (HttpServletRequest) objects[0];
        final Method             method  = clazz.getMethod(methodName, objects.length == 3 ? CLASSES_WITH_CONFIG : CLASSES_WITHOUT_CONFIG);
        try {
            method.invoke(instance, objects);
            AccessLogger.LogAjaxServiceAccess(getUsername(), request);
            return;
        } catch (IllegalArgumentException e) {
            log.error("Illegal arguments specified for the {}.{}({}) method", clazz.getName(), methodName, objects.length == 3 ? CLASS_NAMES_WITH_CONFIG : CLASS_NAMES_WITHOUT_CONFIG, e);
        } catch (InvocationTargetException e) {
            log.error("An error occurred trying to call the {}.{}({}) method", clazz.getName(), methodName, objects.length == 3 ? CLASS_NAMES_WITH_CONFIG : CLASS_NAMES_WITHOUT_CONFIG, e);
        } catch (IllegalAccessException e) {
            log.error("Can't access the {}.{}({}) method", clazz.getName(), methodName, objects.length == 3 ? CLASS_NAMES_WITH_CONFIG : CLASS_NAMES_WITHOUT_CONFIG, e);
        } catch (SecurityException e) {
            log.error("A security exception occurred calling the {}.{}({}) method", clazz.getName(), methodName, objects.length == 3 ? CLASS_NAMES_WITH_CONFIG : CLASS_NAMES_WITHOUT_CONFIG, e);
        }
        AccessLogger.LogAjaxServiceAccess(getUsername(), request, BAD_REQUEST);
    }

    private String getUsername() {
        final UserI user = XDAT.getUserDetails();
        return user != null ? user.getUsername() : "unknown";
    }

    private static final Class<?>[] CLASSES_WITH_CONFIG        = new Class[]{HttpServletRequest.class, HttpServletResponse.class, ServletConfig.class};
    private static final Class<?>[] CLASSES_WITHOUT_CONFIG     = new Class[]{HttpServletRequest.class, HttpServletResponse.class};
    private static final String     CLASS_NAMES_WITH_CONFIG    = "HttpServletRequest, HttpServletResponse, ServletConfig";
    private static final String     CLASS_NAMES_WITHOUT_CONFIG = "HttpServletRequest, HttpServletResponse";
    private static final String     BAD_REQUEST                = HttpStatus.BAD_REQUEST.toString();
}
