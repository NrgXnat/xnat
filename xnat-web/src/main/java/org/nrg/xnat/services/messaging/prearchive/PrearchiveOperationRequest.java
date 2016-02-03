/*
 * org.nrg.xnat.services.messaging.prearchive.MoveSessionRequest
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/19/13 3:01 PM
 */
package org.nrg.xnat.services.messaging.prearchive;

import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.SessionData;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PrearchiveOperationRequest implements Serializable {

    /**
     * Used to store the project ID of the destination project for move operations.
     */
    public static final String PARAM_DESTINATION = "destination";

    public PrearchiveOperationRequest(final UserI user, final SessionData sessionData, final File sessionDir, final String operations) {
        this(user, sessionData, sessionDir, operations, null);
    }

    public PrearchiveOperationRequest(final UserI user, final SessionData sessionData, final File sessionDir, final String operation, final Map<String, String> parameters) {
        _user = user;
        _sessionData = sessionData;
        _sessionDir = sessionDir;
        _operation = operation;
        _parameters = parameters == null ? new HashMap<String, String>() : new HashMap<>(parameters);
    }

    public UserI getUser() {
        return _user;
    }

    public SessionData getSessionData() {
        return _sessionData;
    }

    public File getSessionDir() {
        return _sessionDir;
    }

    public String getOperation() {
        return _operation;
    }

    public Map<String, String> getParameters() {
        return new HashMap<>(_parameters);
    }

    private static final long serialVersionUID = -6953780271999788326L;

    private final UserI _user;
    private final SessionData _sessionData;
    private final File _sessionDir;
    private final String _operation;
    private final Map<String, String> _parameters;
}
