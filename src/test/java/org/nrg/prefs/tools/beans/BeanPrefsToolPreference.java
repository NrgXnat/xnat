/*
 * prefs: org.nrg.prefs.tools.beans.BeanPrefsToolPreference
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tools.beans;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * For this test, this class represents any preference that might be set as a full object, i.e. as a bean. This
 * particular bean is based on the requirements for the DICOM SCP manager service, which manages multiple instances of
 * DICOM SCP receivers. The service manages instances of a worker that is configured via the property
 * values set in each bean.
 */
public class BeanPrefsToolPreference {
    public String getScpId() {
        return _scpId;
    }

    public void setScpId(final String scpId) {
        _scpId = scpId;
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
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final BeanPrefsToolPreference that = (BeanPrefsToolPreference) other;

        return new EqualsBuilder()
                .append(getPort(), that.getPort())
                .append(isEnabled(), that.isEnabled())
                .append(getScpId(), that.getScpId())
                .append(getAeTitle(), that.getAeTitle())
                .append(getIdentifier(), that.getIdentifier())
                .append(getFileNamer(), that.getFileNamer())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getScpId())
                .append(getPort())
                .append(getAeTitle())
                .append(getIdentifier())
                .append(getFileNamer())
                .append(isEnabled())
                .toHashCode();
    }

    private String  _scpId;
    private int     _port;
    private String  _aeTitle;
    private String  _identifier;
    private String  _fileNamer;
    private boolean _enabled;
}
