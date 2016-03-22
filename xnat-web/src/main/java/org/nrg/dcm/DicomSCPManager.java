/*
 * org.nrg.dcm.DicomSCPManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.dcm;

import org.nrg.config.services.SiteConfigurationService;
import org.nrg.dcm.preferences.DicomSCPInstance;
import org.nrg.dcm.preferences.DicomSCPPreference;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DicomSCPManager {

    @PreDestroy
    public void shutdown() {
        _log.debug("Handling pre-destroy actions, shutting down DICOM SCP receivers.");
        stopDicomSCPs();
    }

    public DicomSCP create(final DicomSCPInstance instance) throws NrgServiceException {
        final String scpId = instance.getScpId();
        if (_preferences.hasDicomSCPInstance(scpId)) {
            throw new NrgServiceException(NrgServiceError.ConfigurationError, "There is already a DICOM SCP instance with the ID " + scpId);
        }
        try {
            _preferences.setDicomSCPInstance(instance);
            return _preferences.getDicomSCP(scpId);
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Unable to create DICOM SCP: " + instance.getAeTitle() + ":" + instance.getPort(), e);
        }
    }

    public void delete(final String scpId) throws NrgServiceException {
        if (!_preferences.hasDicomSCPInstance(scpId)) {
            throw new NrgServiceException(NrgServiceError.UnknownEntity, "There is no DICOM SCP instance with the ID " + scpId);
        }
        _preferences.deleteDicomSCPInstance(scpId);
    }

    public List<DicomSCPInstance> getDicomSCPInstances() {
        return new ArrayList<>(_preferences.getDicomSCPInstances().values());
    }

    public void setDicomSCPInstance(final DicomSCPInstance instance) {
        try {
            _preferences.setDicomSCPInstance(instance);
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Unable to update DICOM SCP: " + instance.getAeTitle() + ":" + instance.getPort(), e);
        }
    }

    public List<String> startOrStopDicomSCPAsDictatedByConfiguration() {
        final boolean enableDicomReceiver = _siteConfigurationService.getBoolSiteConfigurationProperty("enableDicomReceiver", true);
        return enableDicomReceiver ? startDicomSCPs() : stopDicomSCPs();
    }

    public List<String> startDicomSCPs() {
        final List<String> started = new ArrayList<>();
        for (final DicomSCPInstance instance : _preferences.getDicomSCPInstances().values()) {
            if (instance.isEnabled()) {
                startDicomSCP(instance);
                started.add(instance.getScpId());
            }
        }
        return started;
    }

    public void startDicomSCP(final String scpId) {
        startDicomSCP(_preferences.getDicomSCPInstance(scpId));
    }

    public List<String> stopDicomSCPs() {
        final List<String> stopped = new ArrayList<>();
        for (final DicomSCP dicomSCP : _preferences.getDicomSCPs()) {
            if (dicomSCP.isStarted()) {
                dicomSCP.stop();
                stopped.add(dicomSCP.getScpId());
            }
        }
        return stopped;
    }

    public void stopDicomSCP(final String scpId) {
        final DicomSCPInstance instance = _preferences.getDicomSCPInstance(scpId);
        if (instance == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.UnknownEntity, "Couldn't find the DICOM SCP instance identified by " + scpId);
        }
        try {
            final DicomSCP dicomSCP = _preferences.getDicomSCP(scpId);
            if (dicomSCP != null) {
                if (dicomSCP.isStarted()) {
                    dicomSCP.stop();
                }
            }
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Unable to stop DICOM SCP: " + instance.getAeTitle() + ":" + instance.getPort(), e);
        }
    }

    public void enableDicomSCP(final String scpId) {
        final DicomSCPInstance instance = _preferences.getDicomSCPInstance(scpId);
        try {
            if (!instance.isEnabled()) {
                instance.setEnabled(true);
                _preferences.setDicomSCPInstance(instance);
            }
            final DicomSCP dicomSCP = _preferences.getDicomSCP(scpId);
            if (!dicomSCP.isStarted()) {
                dicomSCP.start();
            }
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Unable to enable DICOM SCP: " + instance.getAeTitle() + ":" + instance.getPort(), e);
        }
    }

    public void disableDicomSCP(final String scpId) {
        final DicomSCPInstance instance = _preferences.getDicomSCPInstance(scpId);
        try {
            if (instance.isEnabled()) {
                instance.setEnabled(false);
                _preferences.setDicomSCPInstance(instance);
            }
            final DicomSCP dicomSCP = _preferences.getDicomSCP(scpId);
            if (dicomSCP.isStarted()) {
                dicomSCP.stop();
            }
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Unable to disable DICOM SCP: " + instance.getAeTitle() + ":" + instance.getPort(), e);
        }
    }

    public Map<String, Boolean> areDicomSCPsStarted() {
        final Map<String, Boolean> statuses = new HashMap<>();
        for (final DicomSCP dicomSCP : _preferences.getDicomSCPs()) {
            statuses.put(dicomSCP.getScpId(), dicomSCP.isStarted());
        }
        return statuses;
    }

    public boolean hasDicomSCP(final String scpId) {
        return _preferences.hasDicomSCPInstance(scpId);
    }

    public DicomSCPInstance getDicomSCPInstance(final String scpId) {
        return _preferences.getDicomSCPInstance(scpId);
    }

    private void startDicomSCP(final DicomSCPInstance instance) {
        try {
            final DicomSCP dicomSCP = _preferences.getDicomSCP(instance.getScpId());
            dicomSCP.start();
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Unable to start DICOM SCP: " + instance.getAeTitle() + ":" + instance.getPort(), e);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(DicomSCPManager.class);

    @Inject
    private SiteConfigurationService _siteConfigurationService;

    @Inject
    private DicomSCPPreference _preferences;
}
