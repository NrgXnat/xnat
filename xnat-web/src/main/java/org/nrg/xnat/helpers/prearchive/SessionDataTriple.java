/*
 * web: org.nrg.xnat.helpers.prearchive.SessionDataTriple
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xnat.restlet.XNATApplication;
import org.nrg.xnat.restlet.actions.PrearcImporterA.PrearcSession;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Map;

public class SessionDataTriple implements Serializable {
    public SessionDataTriple() {
        // Default constructor
    }

    public SessionDataTriple(final String folderName, final String timestamp, final String project) {
        setFolderName(folderName);
        setTimestamp(timestamp);
        setProject(project);
    }

    public static SessionDataTriple fromMap(Map<String, String> m) {
        return new SessionDataTriple().setFolderName(m.get("SESSION_LABEL"))
                                      .setProject(m.get("PROJECT_ID"))
                                      .setTimestamp(m.get("SESSION_TIMESTAMP"));
    }

    public static SessionDataTriple fromURI(final String uri) throws MalformedURLException {
        final PrearcUriParserUtils.SessionParser parser = new PrearcUriParserUtils.SessionParser(new PrearcUriParserUtils.UriParser(XNATApplication.PREARC_SESSION_URI));
        return SessionDataTriple.fromMap(parser.readUri(uri));
    }

    public static SessionDataTriple fromPrearcSession(final PrearcSession session) {
        return new SessionDataTriple().setFolderName(session.getFolderName())
                                      .setProject(session.getProject())
                                      .setTimestamp(session.getTimestamp());
    }

    public String getFolderName() {
        return _folderName;
    }

    public SessionDataTriple setFolderName(final String name) {
        _folderName = name;
        return this;
    }

    public SessionDataTriple setFolderName(final Object object) {
        if (object != null) {
            setFolderName(object.toString());
        }
        return this;
    }

    public String getTimestamp() {
        return _timestamp;
    }

    public SessionDataTriple setTimestamp(final String timestamp) {
        _timestamp = timestamp;
        return this;
    }

    public SessionDataTriple setTimestamp(final Object object) {
        if (object != null) {
            setTimestamp(object.toString());
        }
        return this;
    }

    public String getProject() {
        return _project;
    }

    public SessionDataTriple setProject(final String project) {
        if (StringUtils.isNotBlank(project)) {
            _project = project;
        } else {
            _project = PrearcUtils.COMMON;
        }
        return this;
    }

    public SessionDataTriple setProject(final Object object) {
        setProject(object.toString());
        return this;
    }

    @Override
    public String toString() {
        return _folderName + ':' + _timestamp + ':' + _project;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final SessionDataTriple that = (SessionDataTriple) other;

        return _folderName != null
               ? _folderName.equals(that._folderName)
               : that._folderName == null
                 && (_timestamp != null
                     ? _timestamp.equals(that._timestamp)
                     : that._timestamp == null
                       && (_project != null
                           ? _project.equals(that._project)
                           : that._project == null));
    }

    @Override
    public int hashCode() {
        int result = _folderName != null ? _folderName.hashCode() : 0;
        result = 31 * result + (_timestamp != null ? _timestamp.hashCode() : 0);
        result = 31 * result + (_project != null ? _project.hashCode() : 0);
        return result;
    }

    private static final long serialVersionUID = 7764386535994779313L;

    private String _folderName;
    private String _timestamp;
    private String _project;
}
