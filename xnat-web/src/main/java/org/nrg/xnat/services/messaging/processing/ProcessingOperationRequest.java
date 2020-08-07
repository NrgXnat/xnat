package org.nrg.xnat.services.messaging.processing;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(prefix = "_")
public class ProcessingOperationRequest<T extends ProcessingOperationRequestData> implements Serializable {
    public static <T extends ProcessingOperationRequestData> ProcessingOperationRequest<T> wrap(final T data) {
        return new ProcessingOperationRequest<>(data);
    }

    @NonNull
    private final T _requestData;
}
