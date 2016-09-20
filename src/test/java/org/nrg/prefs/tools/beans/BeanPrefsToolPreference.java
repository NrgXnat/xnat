/*
 * org.nrg.prefs.tools.beans.BeanPrefsToolPreference
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tools.beans;

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

    private String _scpId;
    private int _port;
    private String _aeTitle;
    private String _identifier;
    private String _fileNamer;
    private boolean _enabled;
}
