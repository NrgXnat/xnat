/*
 * web: org.nrg.dcm.scp.DicomSCPInstance
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.*;
import java.util.*;

@Entity
public class DicomSCPInstance extends AbstractHibernateEntity {
    private String  _aeTitle;
    private int     _port;
    private String  _identifier;
    private String  _fileNamer;
    private boolean _enabled          = true;
    private boolean _customProcessing = false;
    private boolean _directArchive    = false;
    private boolean _anonymizationEnabled  = true;
    private boolean _whitelistEnabled = false;
    private List<String> _whitelist = new ArrayList<>();
    private boolean _routingExpressionsEnabled = false;
    private String _projectRoutingExpression;
    private String _subjectRoutingExpression;
    private String _sessionRoutingExpression;

    public String getAeTitle() { return _aeTitle; }
    public void setAeTitle(String aeTitle) { this._aeTitle = aeTitle; }

    public int getPort() { return _port; }
    public void setPort(int port) { this._port = port; }

    public String getIdentifier() { return _identifier; }
    public void setIdentifier(String identifier) { this._identifier = identifier; }

    public String getFileNamer() { return _fileNamer; }
    public void setFileNamer(String fileNamer) { this._fileNamer = fileNamer; }

    public boolean isCustomProcessing() { return _customProcessing;}
    public void setCustomProcessing(boolean customProcessing) { this._customProcessing = customProcessing;}

    public boolean isDirectArchive() { return _directArchive;}
    public void setDirectArchive(boolean directArchive) { this._directArchive = directArchive;}

    public boolean isAnonymizationEnabled() { return _anonymizationEnabled;}
    public void setAnonymizationEnabled(boolean anonymizationEnabled) { this._anonymizationEnabled = anonymizationEnabled;}

    public boolean isWhitelistEnabled() {return _whitelistEnabled;}
    public void setWhitelistEnabled(boolean whitelistEnabled) { this._whitelistEnabled = whitelistEnabled;}

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="dicomSCPInstance_whitelist", joinColumns=@JoinColumn(name="scp_id"))
    @Column(name="whitelist")
    public List<String> getWhitelist() {
        return _whitelist;
    }
    public void setWhitelist( List<String> whitelist) {
        _whitelist = (whitelist == null)? new ArrayList<>(): whitelist;
    }

    public static String formatDicomSCPInstanceKey(final String aeTitle, final int port) {
        return aeTitle + ":" + port;
    }

    public boolean isRoutingExpressionsEnabled() {return _routingExpressionsEnabled;}
    public void setRoutingExpressionsEnabled(boolean routingExpressionsEnabled) { this._routingExpressionsEnabled = routingExpressionsEnabled;}

    public String getProjectRoutingExpression() {
        return _projectRoutingExpression;
    }
    public void setProjectRoutingExpression( String projectRoutingExpression) {
        _projectRoutingExpression = projectRoutingExpression;
    }

    public String getSubjectRoutingExpression() {
        return _subjectRoutingExpression;
    }
    public void setSubjectRoutingExpression( String subjectRoutingExpression) {
        _subjectRoutingExpression = subjectRoutingExpression;
    }

    public String getSessionRoutingExpression() {
        return _sessionRoutingExpression;
    }
    public void setSessionRoutingExpression( String sessionRoutingExpression) {
        _sessionRoutingExpression = sessionRoutingExpression;
    }

    @Transient
    public String getLabel() {
        return formatDicomSCPInstanceKey(_aeTitle, _port);
    }

    public static DicomSCPInstance createDefault() {
        return create( "XNAT", 8104, "dicomObjectIdentifier");
    }

    public static DicomSCPInstance create( String aeTitle, int port, String identifierType) {
        DicomSCPInstance defaultInstance = new DicomSCPInstance();
        defaultInstance.setAeTitle( aeTitle);
        defaultInstance.setPort( port);
        defaultInstance.setIdentifier( identifierType);
        defaultInstance.setCreated( new Date());
        defaultInstance.setTimestamp( new Date());
        return defaultInstance;
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("id", getId() == 0 ? null : getId());
        map.put("aeTitle", _aeTitle);
        map.put("port", _port);
        map.put("identifier", _identifier);
        map.put("fileNamer", _fileNamer);
        map.put("enabled", _enabled);
        map.put("customProcessing", _customProcessing);
        map.put("directArchive", _directArchive);
        map.put("anonymizationEnabled", _anonymizationEnabled);
        map.put("whitelistEnabled", _whitelistEnabled);
        map.put("whitelist", _whitelist);
        map.put("routingExpressionsEnabled", _routingExpressionsEnabled);
        map.put("projectRoutingExpression", _projectRoutingExpression);
        map.put("subjectRoutingExpression", _subjectRoutingExpression);
        map.put("sessionRoutingExpression", _sessionRoutingExpression);
        map.put("created", getCreated());
        map.put("timestamp", getTimestamp());
        return map;
    }

    // make a new instance for caching.
    public DicomSCPInstance clone() {
        DicomSCPInstance instance = new DicomSCPInstance();
        instance.setAeTitle( getAeTitle());
        instance.setPort( getPort());
        instance.setIdentifier( getIdentifier());
        instance.setFileNamer( getFileNamer());
        instance.setEnabled( _enabled);
        instance.setCustomProcessing( isCustomProcessing());
        instance.setDirectArchive( isDirectArchive());
        instance.setAnonymizationEnabled( isAnonymizationEnabled());
        instance.setWhitelistEnabled( isWhitelistEnabled());
        instance.setWhitelist( getWhitelist());
        instance.setRoutingExpressionsEnabled( isRoutingExpressionsEnabled());
        instance.setProjectRoutingExpression( getProjectRoutingExpression());
        instance.setSubjectRoutingExpression( getSubjectRoutingExpression());
        instance.setSessionRoutingExpression( getSessionRoutingExpression());
        instance.setCreated( getCreated());
        instance.setTimestamp( getTimestamp());
        return instance;
    }

    @Override
    public String toString() {
        return "DicomSCPInstance{id='" + getId() + "', "
                + "aeTitle='" + _aeTitle + "', "
                + "port=" + _port + "', "
                + "identifier='" + _identifier + "', "
                + "fileNamer='" + _fileNamer + "', "
                + "enabled=" + _enabled + ", "
                + "customProcessing=" + _customProcessing + ", "
                + "directArchive=" + _directArchive + ", "
                + "anonymizationEnabled=" + _anonymizationEnabled + ", "
                + "whitelistEnabled=" + _whitelistEnabled + ", "
                + "whitelist=" + _whitelist + ", "
                + "routingExpressionsEnabled=" + _routingExpressionsEnabled + ", "
                + "projectRoutingExpression='" + _projectRoutingExpression + "', "
                + "subjectRoutingExpression='" + _subjectRoutingExpression + "', "
                + "sessionRoutingExpression='" + _sessionRoutingExpression + "'"
                + "}";
    }
}
