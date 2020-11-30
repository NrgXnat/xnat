/*
 * web: org.nrg.xnat.services.messaging.processing.ProcessingOperationRequest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging.processing;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;

@ApiModel(description = "Provides a container for the properties required to launch a Clara training session.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(prefix = "_")
@Slf4j
public abstract class ProcessingOperationRequestData implements Serializable {
    @ApiModelProperty("Indicates the unique ID of this processing request.")
    @NonNull
    private String _processingId;

    @ApiModelProperty("Indicates the username of the user launching the training session instance.")
    @NonNull
    private String _username;

    @ApiModelProperty("Provides the parameters for the training session. The keys in the map are JSON paths indicating particular items in the training configuration template, while the values in the map indicate the value to be set for the corresponding item.")
    @NonNull
    private Map<String, String> _parameters;

    @ApiModelProperty("Indicates the workflow ID associated with this processing request.")
    @Nullable
    private String _workflowId;
}
