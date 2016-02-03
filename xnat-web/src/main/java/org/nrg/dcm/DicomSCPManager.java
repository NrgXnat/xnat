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

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.services.SiteConfigurationService;
import org.nrg.dcm.preferences.DicomSCPInstance;
import org.nrg.dcm.preferences.DicomSCPPreferences;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.prefs.annotations.NrgPrefValue;
import org.nrg.prefs.annotations.NrgPrefsTool;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

@NrgPrefsTool(toolId = "dicomScpManager",
        toolName = "DICOM SCP Manager",
        description = "Manages configuration of the various DICOM SCP endpoints on the XNAT system.",
        preferencesClass = DicomSCPPreferences.class,
        preferences = {@NrgPrefValue(name = "xnat", defaultValue = "{'scpId': 'xnat', 'port': 8104, 'aeTitle': 'XNAT'}", valueType = DicomSCPInstance.class)})
public class DicomSCPManager implements ApplicationContextAware {

    public DicomSCPManager(final XnatUserProvider provider) throws IOException {
        _provider = provider;
    }

    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        _context = context;
    }

    public void setPreferences(final DicomSCPPreferences preferences) {
        _preferences = preferences;
        for (final DicomSCPInstance instance : preferences.getDicomSCPInstances()) {
            try {
                createDicomScpFromInstance(instance);
            } catch (IOException e) {
                _log.error("An error occurred trying to create the DICOM SCP instance " + instance.toString());
            }
        }
    }

    public void create(final DicomSCPInstance instance) throws IOException, NrgServiceException {
        final String scpId = instance.getScpId();
        if (_preferences.hasDicomSCPInstance(scpId)) {
            throw new NrgServiceException(NrgServiceError.ConfigurationError, "There is already a DICOM SCP instance with the ID " + scpId);
        }
        _preferences.setDicomSCPInstance(instance);
        createDicomScpFromInstance(instance);
    }

    public void delete(final String scpId) throws NrgServiceException {
        if (!_preferences.hasDicomSCPInstance(scpId)) {
            throw new NrgServiceException(NrgServiceError.UnknownEntity, "There is no DICOM SCP instance with the ID " + scpId);
        }
        stopDicomSCP(scpId);
        _dicomSCPs.remove(scpId);
        _preferences.deleteDicomSCPInstance(scpId);
    }

    public void startOrStopDicomSCPAsDictatedByConfiguration() {
        final boolean enableDicomReceiver = _siteConfigurationService.getBoolSiteConfigurationProperty("enableDicomReceiver", true);
        if (enableDicomReceiver) {
            startDicomSCPs();
        } else {
            stopDicomSCPs();
        }
    }

    public void startDicomSCPs() {
        for (final String scpId : _dicomSCPs.keySet()) {
            final DicomSCPInstance instance = _preferences.getDicomSCPInstance(scpId);
            if (instance.isEnabled()) {
                startDicomSCP(scpId);
            }
        }
    }

    public void startDicomSCP(final String scpId) {
        final DicomSCP dicomSCP = getDicomSCP(scpId);
        try {
            dicomSCP.start();
            final DicomSCPInstance instance = _preferences.getDicomSCPInstance(scpId);
            if (!instance.isEnabled()) {
                enableDicomSCP(scpId);
            }
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Unable to start DICOM SCP: " + Joiner.on("/").join(dicomSCP.getAEs()) + ":" + dicomSCP.getPort(), e);
        }
    }

    public void enableDicomSCP(final String scpId) throws IOException {
        final DicomSCPInstance instance = _preferences.getDicomSCPInstance(scpId);
        if (!instance.isEnabled()) {
            instance.setEnabled(true);
            _preferences.setDicomSCPInstance(instance);
        }
        final DicomSCP dicomSCP = _dicomSCPs.get(scpId);
        if (!dicomSCP.isStarted()) {
            dicomSCP.start();
        }
    }

    public void disableDicomSCP(final String scpId) {
        final DicomSCPInstance instance = _preferences.getDicomSCPInstance(scpId);
        if (instance.isEnabled()) {
            instance.setEnabled(false);
            _preferences.setDicomSCPInstance(instance);
        }
        final DicomSCP dicomSCP = _dicomSCPs.get(scpId);
        if (dicomSCP.isStarted()) {
            dicomSCP.stop();
        }
    }

    public void stopDicomSCPs() {
        for (final String scpId : _dicomSCPs.keySet()) {
            stopDicomSCP(scpId);
        }
    }

    public void stopDicomSCP(final String scpId) {
        final DicomSCP dicomSCP = _dicomSCPs.get(scpId);
        if (dicomSCP == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.UnknownEntity, "Couldn't find the DICOM SCP instance identified by " + scpId);
        }
        if (dicomSCP.isStarted()) {
            dicomSCP.stop();
        }
    }

    public Map<String, Boolean> areDicomSCPsStarted() {
        final Map<String, Boolean> statuses = new HashMap<>();
        for (final String scpId : _dicomSCPs.keySet()) {
            statuses.put(scpId, isDicomSCPStarted(scpId));
        }
        return statuses;
    }

    public boolean isDicomSCPStarted(final String scpId) {
        final DicomSCP dicomSCP = _dicomSCPs.get(scpId);
        if (dicomSCP == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.UnknownEntity, "Couldn't find the DICOM SCP instance identified by " + scpId);
        }
        return dicomSCP.isStarted();
    }

    public boolean hasDicomSCP(final String scpId) {
        return _dicomSCPs.containsKey(scpId);
    }

    public List<DicomSCPInstance> getDicomSCPInstances() {
        return _preferences.getDicomSCPInstances();
    }

    public DicomSCPInstance getDicomSCPInstance(final String scpId) {
        return _preferences.getDicomSCPInstance(scpId);
    }

    private DicomSCP getDicomSCP(final String scpId) {
        final DicomSCP dicomSCP = _dicomSCPs.get(scpId);
        if (dicomSCP == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.UnknownEntity, "Couldn't find the DICOM SCP instance identified by " + scpId);
        }
        return dicomSCP;
    }

    private void createDicomScpFromInstance(final DicomSCPInstance instance) throws IOException {
        final String scpId = instance.getScpId();
        final DicomSCP dicomScp = DicomSCP.create(scpId, Executors.newCachedThreadPool(), instance.getPort(), _provider, instance.getAeTitle(), getIdentifier(instance.getIdentifier()), getDicomFileNamer(instance.getFileNamer()));
        _dicomSCPs.put(scpId, dicomScp);
        if (instance.isEnabled()) {
            dicomScp.start();
        }
    }

    private DicomObjectIdentifier<XnatProjectdata> getIdentifier(final String identifier) {
        final DicomObjectIdentifier bean = StringUtils.isBlank(identifier) ? _context.getBean(DicomObjectIdentifier.class) : _context.getBean(identifier, DicomObjectIdentifier.class);
        if (bean == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Could not find a DICOM object identifier with the ID " + identifier);
        }
        //noinspection unchecked
        return (DicomObjectIdentifier<XnatProjectdata>) bean;
    }

    private DicomFileNamer getDicomFileNamer(final String identifier) {
        //noinspection unchecked
        final DicomFileNamer bean = StringUtils.isBlank(identifier) ? _context.getBean(DicomFileNamer.class) : _context.getBean(identifier, DicomFileNamer.class);
        if (bean == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Could not find a DICOM object identifier with the ID " + identifier);
        }
        return bean;
    }

    private static final Logger _log = LoggerFactory.getLogger(DicomSCPManager.class);

    @Inject
    private SiteConfigurationService _siteConfigurationService;

    private final Map<String, DicomSCP> _dicomSCPs = new HashMap<>();

    private ApplicationContext _context;
    private DicomSCPPreferences _preferences;
    private final XnatUserProvider _provider;
}
