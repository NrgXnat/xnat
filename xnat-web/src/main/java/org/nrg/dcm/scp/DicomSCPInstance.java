/*
 * web: org.nrg.dcm.scp.DicomSCPInstance
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

// XNAT-6115: The @Builder, @JsonDeserialize, and @JsonPOJOBuilder annotations force Jackson to use the Lombok-generated
// builder instance when deserializing JSON to a DicomSCPInstance object. This works around the issue where boolean properties
// that aren't set in the JSON are set to false. Using the builder, which has the default value for enabled set to true,
// means that, if enabled is not specified in the JSON, the instance is automatically enabled.
@Data
@Builder(builderClassName = "InstanceBuilder", toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Accessors(prefix = "_")
@Slf4j
@JsonDeserialize(builder = DicomSCPInstance.InstanceBuilder.class)
public class DicomSCPInstance {
    public static String formatDicomSCPInstanceKey(final String aeTitle, final int port) {
        return aeTitle + ":" + port;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class InstanceBuilder {
        // This empty class is a placeholder for Lombok.
    }

    @Override
    public String toString() {
        return formatDicomSCPInstanceKey(_aeTitle, _port);
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("id", _id == 0 ? null : _id);
        map.put("aeTitle", _aeTitle);
        map.put("port", _port);
        map.put("identifier", _identifier);
        map.put("fileNamer", _fileNamer);
        map.put("enabled", _enabled);
        map.put("customProcessing", _customProcessing);
        return map;
    }

    private int     _id;
    private String  _aeTitle;
    private int     _port;
    private String  _identifier;
    private String  _fileNamer;
    @Builder.Default
    private boolean _enabled          = true;
    @Builder.Default
    private boolean _customProcessing = false;
}
