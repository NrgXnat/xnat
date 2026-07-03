/*
 * web: org.nrg.xnat.services.messaging.processing.ProcessingOperationRequest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging.processing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Schema(description = "Provides a container for the properties required to launch a Clara training session.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(prefix = "_")
@Slf4j
public abstract class ProcessingOperationRequestData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1;
    @Schema(description = "Indicates the unique ID of this processing request.")
    @NonNull
    private String _processingId;

    @Schema(description = "Indicates the username of the user launching the training session instance.")
    @NonNull
    private String _username;

    @Schema(description = "Provides the parameters for the training session. The keys in the map are JSON paths indicating particular items in the training configuration template, while the values in the map indicate the value to be set for the corresponding item.")
    @NonNull
    private Map<String, String> _parameters;

    @Schema(description = "Indicates the workflow ID associated with this processing request.")
    @Nullable
    private String _workflowId;
}
