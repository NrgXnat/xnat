package org.nrg.xnat.helpers.prearchive.handlers;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.archive.Operation;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultPrearchiveOperationHandlerResolver implements PrearchiveOperationHandlerResolver {
    @Autowired
    public DefaultPrearchiveOperationHandlerResolver(final NrgEventServiceI eventService, final XnatUserProvider receivedFileUserProvider, final DicomInboxImportRequestService importRequestService) {
        _eventService         = eventService;
        _userProvider         = receivedFileUserProvider;
        _importRequestService = importRequestService;

        final Reflections reflections = new Reflections(AbstractPrearchiveOperationHandler.class.getPackage().getName());
        _handlers.putAll(reflections.getSubTypesOf(PrearchiveOperationHandler.class).stream()
                                    .filter(ReflectionUtils.withAnnotation(Handles.class))
                                    .collect(Collectors.toMap(HANDLER_OP, HANDLER_CTOR)));
    }

    @Override
    public PrearchiveOperationHandler getHandler(final PrearchiveOperationRequest request) {
        final Operation operation = request.getOperation();
        log.debug("Searching for handler for prearchive operation {}", operation);

        final Constructor<? extends PrearchiveOperationHandler> constructor = _handlers.get(operation);
        if (constructor == null) {
            throw new RuntimeException("No handler found for operation " + operation + ". Please check your classpath.");
        }

        log.debug("Found handler for operation {}, creating with request type {}", operation, request.getClass().getName());
        try {
            return constructor.newInstance(request, _eventService, _userProvider, _importRequestService);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("An error occurred trying to instantiate a prearchive operation handler.", e);
        }
    }

    private static final Function<Class<? extends PrearchiveOperationHandler>, Operation>                                         HANDLER_OP   = handler -> {
        final Operation operation = handler.getAnnotation(Handles.class).value();
        log.debug("Found handler for {} operation: {}", operation, handler.getName());
        return operation;
    };
    private static final Function<Class<? extends PrearchiveOperationHandler>, Constructor<? extends PrearchiveOperationHandler>> HANDLER_CTOR = handler -> {
        try {
            return handler.getConstructor(PrearchiveOperationRequest.class, NrgEventServiceI.class, XnatUserProvider.class, DicomInboxImportRequestService.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No proper constructor found for " + handler.getName() + " class. It must have a constructor that accepts a " + AbstractPrearchiveOperationHandler.class.getName() + " object.");
        }
    };
    private final        Map<Operation, Constructor<? extends PrearchiveOperationHandler>>                                        _handlers    = new HashMap<>();

    private final NrgEventServiceI               _eventService;
    private final XnatUserProvider               _userProvider;
    private final DicomInboxImportRequestService _importRequestService;
}
