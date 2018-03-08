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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class DicomSCPInstance {
    public static String formatDicomSCPInstanceKey(final String aeTitle, final int port) {
        return aeTitle + ":" + port;
    }

    @JsonCreator
    public DicomSCPInstance() {
        // Default constructor
    }

    public DicomSCPInstance(final int id, final String aeTitle, final int port, final String identifier, final String fileNamer, final boolean enabled) {
        setId(id);
        setPort(port);
        setAeTitle(aeTitle);
        setIdentifier(identifier);
        setFileNamer(fileNamer);
        setEnabled(enabled);
    }

    public int getId() {
        return _id;
    }

    public void setId(final int id) {
        _id = id;
    }

    public int getPort() {
        return _port;
    }

    public void setPort(final int port) {
        _port = port;
    }

    public String getAeTitle() {
        return _aeTitle;
    }

    public void setAeTitle(final String aeTitle) {
        _aeTitle = aeTitle;
    }

    public String getIdentifier() {
        return _identifier;
    }

    public void setIdentifier(final String identifier) {
        _identifier = identifier;
    }

    public String getFileNamer() {
        return _fileNamer;
    }

    public void setFileNamer(final String fileNamer) {
        _fileNamer = fileNamer;
    }

    public boolean isEnabled() {
        return _enabled;
    }

    public void setEnabled(final boolean enabled) {
        _enabled = enabled;
    }

    @Override
    public String toString() {
        return formatDicomSCPInstanceKey(_aeTitle, _port);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof DicomSCPInstance)) {
            return false;
        }

        final DicomSCPInstance instance = (DicomSCPInstance) object;

        return new EqualsBuilder()
                .append(getId(), instance.getId())
                .append(getPort(), instance.getPort())
                .append(isEnabled(), instance.isEnabled())
                .append(getAeTitle(), instance.getAeTitle())
                .append(getIdentifier(), instance.getIdentifier())
                .append(getFileNamer(), instance.getFileNamer())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getId())
                .append(getPort())
                .append(getAeTitle())
                .append(getIdentifier())
                .append(getFileNamer())
                .append(isEnabled())
                .toHashCode();
    }

    public Map<String, Object> toMap() {
        return new HashMap<String, Object>() {{
            put("id", _id == 0 ? null : _id);
            put("aeTitle", _aeTitle);
            put("port", _port);
            put("identifier", _identifier);
            put("fileNamer", _fileNamer);
            put("enabled", _enabled);
        }};
    }

    private int    _id;
    private int    _port;
    private String _aeTitle;
    private String _identifier;
    private String _fileNamer;
    private boolean _enabled = true;
}
