/*
 * framework: org.nrg.framework.messaging.DlqListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.messaging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DlqListener {
    @Autowired
    public DlqListener(final List<? extends JmsRequestListener<?>> listeners) {
        for (final JmsRequestListener<?> listener : listeners) {
            final Class<?> listenerClass = listener.getClass();
            final List<Method> methods = Arrays.stream(listenerClass.getDeclaredMethods())
                                               .filter(method -> method.getAnnotation(JmsListener.class) != null)
                                               .collect(Collectors.toList());
            final String listenerClassName = listenerClass.getName();
            if (methods.isEmpty()) {
                log.warn("Found the class {} annotated with @JmsRequestListener but it doesn't have a method annotated with @JmsListener. Ignoring.", listenerClassName);
            } else {
                methods.forEach(this::addMessageListenerMapping);
            }
        }
    }

    @SuppressWarnings("unused")
    public void setMessageListenerMapping(final Map<String, String> mapping) {
        for (final String requestTypeName : mapping.keySet()) {
            try {
                final Class<?> requestType = Class.forName(requestTypeName);
                if (_mapping.containsKey(requestType)) {
                    log.info("Found duplicate entry for JMS request class {}, ignoring explicit setting.", requestType.getName());
                } else {
                    _mapping.put(requestType, new ListenerMethod(mapping.get(requestTypeName), requestType));
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Poorly formed DLQ mapping " + requestTypeName + " -> " + mapping.get(requestTypeName) + ": could not find the specified request class " + requestTypeName + ".");
            }
        }
    }

    @SuppressWarnings("unused")
    public void onReceiveDeadLetter(final Object object) {
        if (object == null) {
            throw new RuntimeException("Received null dead letter. That's not OK.");
        }

        final Class<?> objectClass = object.getClass();
        if (_mapping.isEmpty()) {
            log.error("Received dead letter of type: {}, however no listeners are currently configured.", objectClass);
            return;
        }

        final Map<Class<?>, ListenerMethod> matches = _mapping.keySet().stream().filter(key -> key.isAssignableFrom(objectClass)).collect(Collectors.toMap(Function.identity(), _mapping::get));
        if (matches.isEmpty()) {
            log.error("Received dead letter of unknown type, ignoring: {}", objectClass);
            return;
        }
        matches.values().forEach(value -> value.callListenerMethod(object));
    }

    public void addMessageListenerMapping(final Method method) {
        if (method.getAnnotation(JmsListener.class) == null) {
            log.warn("The method {}.{}() is not annotated with @JmsListener. A listener method must have the @JmsListener annotation to be activated.", method.getDeclaringClass().getName(), method.getName());
            return;
        }
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            log.warn("The method {}.{}() is annotated with @JmsListener but has no parameters. A listener method must have one and only parameter, which should be the request object.", method.getDeclaringClass().getName(), method.getName());
            return;
        }
        if (parameterTypes.length > 1) {
            log.warn("The method {}.{}() is annotated with @JmsListener but has {} parameters. A listener method must have one and only parameter, which should be the request object.", method.getDeclaringClass().getName(), method.getName(), parameterTypes.length);
            return;
        }

        _mapping.put(parameterTypes[0], new ListenerMethod(method));
    }

    private static class ListenerMethod {
        public ListenerMethod(final String classAndMethod, final Class<?> requestType) {
            if (!classAndMethod.contains(".")) {
                throw new RuntimeException("Poorly formed listener specifier " + classAndMethod + ": must be of the form package.of.ListenerClass.ListenerMethod.");
            }
            final String className = StringUtils.substringBeforeLast(classAndMethod, ".");
            try {
                _listenerClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Poorly formed listener specifier " + classAndMethod + ": could not find the specified class " + className + ".");
            }
            final String methodName = StringUtils.substringAfterLast(classAndMethod, ".");
            _listenerMethod = Arrays.stream(_listenerClass.getDeclaredMethods())
                                    .filter(method -> StringUtils.equals(methodName, method.getName()) && method.getParameterCount() == 1 && method.getAnnotation(JmsListener.class) != null)
                                    .findFirst().orElseThrow(() -> new RuntimeException("Poorly formed listener specifier " + classAndMethod + ": could not find a method named " + methodName + "() with one parameter and the @JmsListener annotation on the specified class " + className + "."));
            if (!_listenerMethod.getParameters()[0].getClass().equals(requestType)) {
                throw new RuntimeException("Poorly formed listener specifier " + classAndMethod + ": the parameter for the method " + methodName + "() on the specified class " + className + " is not of the specified request type " + requestType.getName() + ".");
            }
        }

        public ListenerMethod(final Method listenerMethod) {
            _listenerClass  = listenerMethod.getDeclaringClass();
            _listenerMethod = listenerMethod;
        }

        public void callListenerMethod(final Object... objects) {
            try {
                _listenerMethod.invoke(_listenerClass.newInstance(), objects);
            } catch (InstantiationException exception) {
                throw new RuntimeException("Error creating verified class: " + _listenerClass.getName(), exception);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException("Illegal access to verified method: " + _listenerClass.getName() + "." + _listenerMethod.getName(), exception);
            } catch (InvocationTargetException exception) {
                throw new RuntimeException("Error invoking verified method: " + _listenerClass.getName() + "." + _listenerMethod.getName(), exception);
            }
        }

        private final Class<?> _listenerClass;
        private final Method   _listenerMethod;
    }

    private final Map<Class<?>, ListenerMethod> _mapping = new HashMap<>();
}
