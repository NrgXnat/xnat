/*
 * web: org.nrg.dcm.preferences.DicomSCPInstance
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.preferences;

@SuppressWarnings("WeakerAccess")
public class DicomSCPInstance {
    @SuppressWarnings("unused")
    public DicomSCPInstance() {
        // default constructor
    }

    @SuppressWarnings("unused")
    public DicomSCPInstance(final int id, final int port, final String aeTitle) {
        setId(id);
        setPort(port);
        setAeTitle(aeTitle);
    }

    @SuppressWarnings("unused")
    public DicomSCPInstance(final int id, final int port, final String aeTitle, final boolean enabled) {
        setId(id);
        setPort(port);
        setAeTitle(aeTitle);
        setEnabled(enabled);
    }

    @SuppressWarnings("unused")
    public DicomSCPInstance(final int id, final int port, final String aeTitle, final String identifier, final String fileNamer) {
        setId(id);
        setPort(port);
        setAeTitle(aeTitle);
        setIdentifier(identifier);
        setFileNamer(fileNamer);
    }

    @SuppressWarnings("unused")
    public DicomSCPInstance(final int id, final int port, final String aeTitle, final String identifier, final String fileNamer, final boolean enabled) {
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
        return _aeTitle + ":" + _port;
    }

    private int    _id;
    private int    _port;
    private String _aeTitle;
    private String _identifier;
    private String _fileNamer;
    private boolean _enabled = true;
}
