/*
 * web: org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging.prearchive;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.Operation;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.SessionData;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Accessors(prefix = "_")
@Slf4j
public class PrearchiveOperationRequest implements Serializable {
    /**
     * Used to store the project ID of the destination project for move operations.
     */
    public static final String PARAM_DESTINATION         = "destination";
    public static final String PARAM_OVERRIDE_EXCEPTIONS = "overrideExceptions";
    public static final String PARAM_ALLOW_SESSION_MERGE = "allowSessionMerge";

    public PrearchiveOperationRequest(final UserI user, final Operation operation, final PrearcSession session) throws Exception {
        this(user.getUsername(), operation, session.getSessionData(), session.getSessionDir(), session.getAdditionalValues());
    }

    public PrearchiveOperationRequest(final UserI user, final Operation operation, final SessionData sessionData, final File sessionDir) {
        this(user.getUsername(), operation, sessionData, sessionDir, null);
    }

    public PrearchiveOperationRequest(final UserI user, final Operation operation, final SessionData sessionData, final File sessionDir, final Map<String, Object> parameters) {
        this(user.getUsername(), operation, sessionData, sessionDir, parameters);
    }

    public PrearchiveOperationRequest(final String username, final Operation operation, final PrearcSession session) throws Exception {
        this(username, operation, session.getSessionData(), session.getSessionDir(), session.getAdditionalValues());
    }

    public PrearchiveOperationRequest(final String username, final Operation operation, final SessionData sessionData, final File sessionDir) {
        this(username, operation, sessionData, sessionDir, null);
    }

    public PrearchiveOperationRequest(final String username, final Operation operation, final SessionData sessionData, final File sessionDir, final Map<String, Object> parameters) {
        _username = username;
        _operation = operation;
        _sessionData = sessionData;
        _sessionDir = sessionDir;
        _parameters = parameters == null ? new HashMap<String, Object>() : new HashMap<>(parameters);
    }

    @NonNull
    private final String              _username;
    @NonNull
    private final Operation           _operation;
    @NonNull
    private final SessionData         _sessionData;
    @NonNull
    private final File                _sessionDir;
    @NonNull
    private final Map<String, Object> _parameters;
}
