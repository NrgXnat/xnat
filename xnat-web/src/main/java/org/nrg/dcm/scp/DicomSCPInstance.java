/*
 * web: org.nrg.dcm.scp.DicomSCPInstance
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@Accessors(prefix = "_")
public class DicomSCPInstance {
    public static String formatDicomSCPInstanceKey(final String aeTitle, final int port) {
        return aeTitle + ":" + port;
    }

    @JsonCreator
    public DicomSCPInstance() {
        // Default constructor
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
