package org.nrg.dcm.preferences;

import com.fasterxml.jackson.annotation.JsonInclude;

@SuppressWarnings("WeakerAccess")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DicomSCPInstance {
    @SuppressWarnings("unused")
    public DicomSCPInstance() {
        // default constructor
    }

    @SuppressWarnings("unused")
    public DicomSCPInstance(final String scpId, final int port, final String aeTitle) {
        setScpId(scpId);
        setPort(port);
        setAeTitle(aeTitle);
    }

    @SuppressWarnings("unused")
    public DicomSCPInstance(final String scpId, final int port, final String aeTitle, final String identifier, final String fileNamer) {
        setScpId(scpId);
        setPort(port);
        setAeTitle(aeTitle);
        setIdentifier(identifier);
        setFileNamer(fileNamer);
    }

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
    public String toString() {
        return "DicomSCPInstance{" +
                "_scpId='" + _scpId + '\'' +
                ", _port=" + _port +
                ", _aeTitle='" + _aeTitle + '\'' +
                ", _identifier='" + _identifier + '\'' +
                ", _fileNamer='" + _fileNamer + '\'' +
                ", _enabled='" + _enabled + '\'' +
                '}';
    }

    private String _scpId;
    private int _port;
    private String _aeTitle;
    private String _identifier;
    private String _fileNamer;
    private boolean _enabled = true;
}
