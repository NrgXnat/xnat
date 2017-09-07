/*
 * web: org.nrg.dcm.id.ScriptedSessionAssignmentExtractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.DicomObject;
import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.dcm.ChainExtractor;
import org.nrg.dcm.Extractor;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.DicomObjectIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ScriptedSessionAssignmentExtractor extends ChainExtractor implements IdentifierReferencingExtractor {

    @SuppressWarnings("unused")
    public ScriptedSessionAssignmentExtractor(final String event, final Extractor... extractors) {
        this(event, false, extractors);
    }

    @SuppressWarnings("unused")
    public ScriptedSessionAssignmentExtractor(final String event, final List<Extractor> extractors) {
        this(event, false, extractors);
    }

    public ScriptedSessionAssignmentExtractor(final String event, final boolean continueOnScriptFailure, final Extractor... extractors) {
        this(event, continueOnScriptFailure, Arrays.asList(extractors));
    }

    public ScriptedSessionAssignmentExtractor(final String event, final boolean continueOnScriptFailure, final List<Extractor> extractors) {
        super(extractors);
        _event = event;
        _continueOnScriptFailure = continueOnScriptFailure;
        if (_log.isDebugEnabled()) {
            _log.debug("Initializing scripted extractor with event {} and continue on failure set to {}", _event, _continueOnScriptFailure);
        }
    }

    @Override
    public void setIdentifier(DicomObjectIdentifier<XnatProjectdata> identifier) {
        _identifier = identifier;
    }

    @Override
    public String extract(final DicomObject object) {
        // Get the project ID if available.
        final XnatProjectdata project = _identifier.getProject(object);
        final String projectId = (String) project.getItem().getProps().get("id");

        if (_log.isDebugEnabled()) {
            if (StringUtils.isBlank(projectId)) {
                _log.debug("Starting session assignment extraction for unassigned imaging data.");
            } else {
                _log.debug("Starting session assignment extraction for imaging data in project {}.", projectId);
            }
        }

        // Get the default value. We'll return this if there's no script associated with the current event and scope.
        final String defaultValue = super.extract(object);

        if (_log.isDebugEnabled()) {
            _log.debug("Got the default value of " + defaultValue);
        }

        // So... is there a script associated with the current event and scope?
        final Script script = getScript(projectId);

        // If not...
        if (script == null) {
            if (_log.isDebugEnabled()) {
                if (StringUtils.isBlank(projectId)) {
                    _log.debug("Didn't find a script for the site and event {}, returning default value.", _event);
                } else {
                    _log.debug("Didn't find a script for the project {} and event {}, returning default value.", projectId, _event);
                }
            }
            return defaultValue;
        }

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("defaultValue", defaultValue);
        parameters.put("dicom", object);
        parameters.put("logger", _log);
        parameters.put("projectId", projectId == null ? "" : projectId);

        try {
            final Object results = _service.runScript(script, parameters);
            if (_log.isDebugEnabled()) {
                if (results == null) {
                    if (StringUtils.isBlank(projectId)) {
                        _log.debug("Ran the script {} for the site and event {}, got null results, returning default value.", script.getScriptId(), _event);
                    } else {
                        _log.debug("Ran the script {} for the project {} and event {}, got null results, returning default value.", script.getScriptId(), projectId, _event);
                    }
                } else {
                    if (StringUtils.isBlank(projectId)) {
                        _log.debug("Ran the script {} for the site and event {}, got results of type {}, returning value {}.", script.getScriptId(), _event, results.getClass().getName(), results.toString());
                    } else {
                        _log.debug("Ran the script {} for the project {} and event {}, got results of type {}, returning value {}.", script.getScriptId(), projectId, _event, results.getClass().getName(), results.toString());
                    }
                }
            }
            return results == null ? defaultValue : results.toString();
        } catch (RuntimeException | Error e) {
            _log.error("Got an exception running the " + script.getScriptId() + "script for event " + _event + ". " + (_continueOnScriptFailure ? "Continue on failure is true, returning default value " + defaultValue : "Continue on failure is false, re-throwing exception"), e);
            if (_continueOnScriptFailure) {
                return defaultValue;
            } else {
                throw e;
            }
        } catch (Throwable e) {
            _log.error("Got an exception running the " + script.getScriptId() + "script for event " + _event + ". " + (_continueOnScriptFailure ? "Continue on failure is true, returning default value " + defaultValue : "Continue on failure is false, returning error-annotated default version."), e);
            if (_continueOnScriptFailure) {
                return defaultValue;
            } else {
                return "error_" + defaultValue;
            }
        }
    }

    private Script getScript(final String projectId) {
        final boolean hasEntity = StringUtils.isNotBlank(projectId);
        final Script script = _service.getScript(hasEntity ? Scope.Project : Scope.Site, projectId, ScriptTrigger.DEFAULT_CLASS, _event, ScriptTrigger.DEFAULT_FILTER);

        // If we didn't find a script for the indicated scope but we have an entity ID, then fail up to the site level.
        if (script == null && hasEntity) {
            return getScript(null);
        }
        // Otherwise, return what we got, if not null then return that, but if null but no entity ID then return that.
        return script;
    }

    private static final Logger _log = LoggerFactory.getLogger(ScriptedSessionAssignmentExtractor.class);

    @Inject
    private ScriptRunnerService _service;

    private DicomObjectIdentifier<XnatProjectdata> _identifier;
    private final String _event;
    private final boolean _continueOnScriptFailure;
}
