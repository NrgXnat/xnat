/*
 * web: org.nrg.xnat.restlet.services.prearchive.BatchPrearchiveActionsA
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.services.prearchive;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.predicates.ProjectAccessPredicate;
import org.nrg.xnat.helpers.PrearcImporterHelper;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionDataTriple;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.utils.functions.UriToSessionDataTriple;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.util.*;

import static lombok.AccessLevel.PROTECTED;

@Getter(PROTECTED)
@Setter(PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public abstract class BatchPrearchiveActionsA extends SecureResource {
    public static final String SRC   = "src";
    public static final String ASYNC = "async";

    public BatchPrearchiveActionsA(final Context context, final Request request, final Response response) {
        super(context, request, response);
    }

    @Override
    public void handleParam(final String key, final Object value) {
        switch (key) {
            case SRC:
                _sources.add((String) value);
                break;

            case ASYNC:
                setAsync(!isFalse(value));
        }
    }

    @Override
    public boolean allowGet() {
        return false;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void handlePost() {
        //build fileWriters
        try {
            loadBodyVariables();
            loadQueryVariables();

            initialize();

            final List<PrearcSession> sessions = new ArrayList<>();

            _projectId = PrearcImporterHelper.identifyProject(_additionalValues);

            if ((StringUtils.isAnyBlank(_projectId, _timestamp) || _sessionFolder == null) && _sources == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unknown prearchive session.");
                return;
            }

            if (_sources != null) {
                for (final String source : _sources) {
                    final URIManager.DataURIA data;
                    try {
                        data = UriParserUtils.parseURI(source);
                        assert data != null;
                    } catch (MalformedURLException e) {
                        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
                        return;
                    }
                    if (data instanceof URIManager.ArchiveURI) {
                        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid src URI (" + source + ")");
                        return;
                    }

                    try {
                        sessions.add(new PrearcSession((URIManager.PrearchiveURI) data, _additionalValues, getUser()));
                    } catch (InvalidPermissionException e) {
                        throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, data.getUri());
                    } catch (Exception e) {
                        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, data.getUri() + " invalid.");
                    }
                }
            } else if (_destination != null) {
                final URIManager.DataURIA data;
                try {
                    data = UriParserUtils.parseURI(_destination);
                    assert data != null;
                } catch (MalformedURLException e) {
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
                    return;
                }
                if (data instanceof URIManager.PrearchiveURI) {
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid _destination URI (" + _destination + ")");
                    return;
                }
                _additionalValues.putAll(data.getProps());
            } else {
                for (final String session : _sessionFolder) {
                    try {
                        sessions.add(new PrearcSession(_projectId, _timestamp, session, _additionalValues, getUser()));
                    } catch (InvalidPermissionException e) {
                        throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, String.format("/prearchive/projects/%s/%s/%s not found.", _projectId, _timestamp, session));
                    } catch (Exception e) {
                        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, String.format("/prearchive/projects/%s/%s/%s invalid.", _projectId, _timestamp, session));
                    }
                }
            }

            //validate specified folders
            for (final PrearcSession session : sessions) {
                if (session.getProject() == null) {
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Cannot archive sessions from the Unassigned folder.");
                }

                if (!session.getSessionDir().exists()) {
                    throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, session.getUrl() + " not found.");
                }
            }

            if (sessions.size() == 1) {
                final PrearcSession session = sessions.get(0);

                if (!PrearcUtils.canModify(getUser(), session.getProject())) {
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Invalid permissions for new project.");
                    return;
                }

                if (!PrearcDatabase.setStatus(session.getFolderName(), session.getTimestamp(), session.getProject(), PrearcUtils.PrearcStatus.ARCHIVING)) {
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Operation already in progress on this prearchive entry.");
                    return;
                }

                finishSingleSessionArchive(session);
            } else {
                finishNonSingleSessionUpload(sessions);
            }
        } catch (ActionException e) {
            log.error("", e);
            getResponse().setStatus(e.getStatus(), e.getMessage());
        } catch (PrearcDatabase.SyncFailedException e) {
            if (e.cause instanceof ActionException) {
                log.error("", e.cause);
                getResponse().setStatus(((ActionException) e.cause).getStatus(), e.cause.getMessage());
            } else {
                log.error("", e);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
            }
        } catch (ResourceException e) {
            log.error("", e);
            getResponse().setStatus(e.getStatus(), e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
        } catch (Exception e) {
            log.error("", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
        }
    }

    /**
     * Can be used to initialize variables and context. Occurs after {@link #loadBodyVariables()} and
     * {@link #loadQueryVariables()} have been called, so all request parameters should be loaded and
     * available.
     */
    protected void initialize() {
        log.trace("Now in initialize() stub");
    }
    /**
     * Provides a method to implement custom processing for completing the archive operation for a single
     * prearchive session.
     *
     * @param session The prearchive session to be archived.
     *
     * @throws Exception When an error occurs during archiving. Specific exceptions vary by implementation.
     */
    protected void finishSingleSessionArchive(final PrearcSession session) throws Exception {
        log.trace("Now in finishSingleSessionArchive() stub");
    }

    /**
     * Provides a method to implement custom processing for completing the archive operation for multiple
     * prearchive sessions.
     *
     * @param sessions The prearchive sessions to be archived.
     *
     * @throws Exception When an error occurs during archiving. Specific exceptions vary by implementation.
     */
    protected void finishNonSingleSessionUpload(final List<PrearcSession> sessions) throws Exception {
        log.trace("Now in finishNonSingleSessionUpload() stub");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean loadVariables() {
        try {
            loadBodyVariables();
            loadQueryVariables();
            return true;
        } catch (ClientException e) {
            getResponse().setStatus(e.getStatus(), e);
            return false;
        }
    }

    /**
     * Transforms the {@link #getSources() list of source URIs} into a {@link SessionDataTriple list of triples}. If any
     * of the source URIs are malformed or the user has insufficient privileges on one or more of the prearchive projects,
     * the response status and message are set appropriately and <b>null</b> is returned. Otherwise a list of one or more
     * triples is returned.
     *
     * @return A list of triples if successful, otherwise null.
     */
    @Nullable
    protected List<SessionDataTriple> getSessionDataTriples() {
        final UriToSessionDataTriple  transformer = new UriToSessionDataTriple();
        final List<SessionDataTriple> triples     = Lists.transform(getSources(), transformer);
        if (transformer.hasMalformedUrls()) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "One or more source values contained a malformed URL: " + StringUtils.join(transformer.getMalformedUrls(), ", "));
            return null;
        }

        final UserI user = getUser();
        final List<String> denied = getDeniedProjectsFromPrearcSources(user, triples);
        if (!denied.isEmpty()) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Invalid permissions for user " + user.getUsername() + " to delete prearchive sessions in one or more projects: " + StringUtils.join(denied, ", "));
            return null;
        }

        return triples;
    }

    protected void setTriplesRepresentation(final List<SessionDataTriple> triples) {
        try {
            getResponse().setEntity(updatedStatusRepresentation(triples, overrideVariant(getPreferredVariant())));
        } catch (Exception e) {
            log.error("", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
        }
    }

    protected Representation updatedStatusRepresentation(final Collection<SessionDataTriple> sessions, final MediaType mediaType) throws Exception {
        final XFTTable table = PrearcUtils.convertArrayLtoTable(PrearcDatabase.buildRows(sessions));
        return representTable(table, mediaType, new Hashtable<String, Object>());
    }

    protected static List<String> getDeniedProjectsFromPrearcSources(final UserI user, final Collection<SessionDataTriple> triples) {
        return Lists.newArrayList(Iterables.filter(Iterables.transform(triples, FUNCTION_SESSION_DATA_TRIPLE_TO_PROJECT_ID), new ProjectAccessPredicate(XDAT.getContextService().getBean(PermissionsServiceI.class), XDAT.getNamedParameterJdbcTemplate(), user, AccessLevel.Edit)));
    }

    private static final Function<SessionDataTriple, String> FUNCTION_SESSION_DATA_TRIPLE_TO_PROJECT_ID = new Function<SessionDataTriple, String>() {
        @Nullable
        @Override
        public String apply(final SessionDataTriple triple) {
            return triple.getProject();
        }
    };

    private String  _projectId   = null;
    private String  _timestamp   = null;
    private String  _destination = null;
    private boolean _async       = true;

    private final Map<String, Object> _additionalValues = new HashMap<>();
    private final List<String>        _sessionFolder    = new ArrayList<>();
    private final List<String>        _sources          = new ArrayList<>();
}
