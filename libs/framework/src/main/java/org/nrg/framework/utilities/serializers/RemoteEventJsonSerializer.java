/*
 * framework: org.nrg.framework.utilities.serializers.RemoteEventJsonSerializer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.nrg.framework.logging.RemoteEvent;

import java.io.IOException;

public class RemoteEventJsonSerializer extends JsonSerializer<RemoteEvent> {
    @Override
    public void serialize(final RemoteEvent value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {

    }
}
