/*
 * web: org.nrg.xnat.helpers.prearchive.handlers.AbstractPrearchiveOperationHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive.handlers;

import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractPrearchiveOperationHandler {

    public AbstractPrearchiveOperationHandler(final PrearchiveOperationRequest request) throws Exception {
        _user = Users.getUser(request.getUser().getUsername());
        _sessionData = request.getSessionData();
        _sessionDir = request.getSessionDir();
        _parameters.clear();
        _parameters.putAll(request.getParameters());
    }

    public static AbstractPrearchiveOperationHandler getHandler(final PrearchiveOperationRequest request) {
        if (_handlers.size() == 0) {
            synchronized (_handlers) {
                final Reflections reflections = new Reflections(AbstractPrearchiveOperationHandler.class.getPackage().getName());
                final Set<Class<? extends AbstractPrearchiveOperationHandler>> handlers = reflections.getSubTypesOf(AbstractPrearchiveOperationHandler.class);
                for (final Class<? extends AbstractPrearchiveOperationHandler> handler : handlers) {
                    try {
                        if (_log.isDebugEnabled()) {
                            _log.debug("Found handler for {} operation: {}", handler.getAnnotation(Handles.class).value(), handler.getName());
                        }
                        _handlers.put(handler.getAnnotation(Handles.class).value(), handler.getConstructor(PrearchiveOperationRequest.class));
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("No proper constructor found for " + handler.getName() + " class. It must have a constructor that accepts a " + AbstractPrearchiveOperationHandler.class.getName() + " object.");
                    }
                }
            }
        }
        final String operation = request.getOperation();
        final Constructor<? extends AbstractPrearchiveOperationHandler> constructor = _handlers.get(operation);
        if (constructor == null) {
            throw new RuntimeException("No handler found for operation " + operation + ". Please check your classpath.");
        }
        try {
            if (_log.isDebugEnabled()) {
                _log.debug("Found handler for operation {}, creating with request type {}", operation, request.getClass().getName());
            }
            return constructor.newInstance(request);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("An error occurred trying to instantiate a prearchive operation handler.", e);
        }
    }

    public abstract void execute() throws Exception;

    protected UserI getUser() {
        return _user;
    }

    protected SessionData getSessionData() {
        return _sessionData;
    }

    protected File getSessionDir() {
        return _sessionDir;
    }

    protected Map<String, String> getParameters() {
        return _parameters;
    }

    private final static Logger _log = LoggerFactory.getLogger(AbstractPrearchiveOperationHandler.class);
    private final static Map<String, Constructor<? extends AbstractPrearchiveOperationHandler>> _handlers = new HashMap<>();

    private final UserI _user;
    private final SessionData _sessionData;
    private final File _sessionDir;
    private final Map<String, String> _parameters = new HashMap<>();
}
