package org.nrg.xnat.helpers.prearchive.handlers;

import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;

public interface PrearchiveOperationHandlerResolver {
    PrearchiveOperationHandler getHandler(final PrearchiveOperationRequest request);
}
