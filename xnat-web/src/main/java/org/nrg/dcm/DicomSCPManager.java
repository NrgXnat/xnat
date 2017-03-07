/*
 * web: org.nrg.dcm.DicomSCPManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.nrg.dcm.exceptions.EnabledDICOMReceiverWithDuplicatePortException;
import org.nrg.dcm.id.CompositeDicomObjectIdentifier;
import org.nrg.dcm.preferences.DicomSCPInstance;
import org.nrg.dcm.preferences.DicomSCPPreference;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.DicomObjectIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

public class DicomSCPManager {
    @Autowired
    public DicomSCPManager(final DicomSCPPreference dicomScpPreferences, final SiteConfigPreferences siteConfigPreferences, final DicomObjectIdentifier<XnatProjectdata> primaryDicomObjectIdentifier, final Map<String, DicomObjectIdentifier<XnatProjectdata>> dicomObjectIdentifiers) {
        _dicomScpPreferences = dicomScpPreferences;
        _siteConfigPreferences = siteConfigPreferences;
        _dicomObjectIdentifiers = Maps.newHashMap();

        String primaryBeanId = null;
        final List<String> sortedDicomObjectIdentifierBeanIds = Lists.newArrayList();
        for (final String beanId : dicomObjectIdentifiers.keySet()) {
            final DicomObjectIdentifier<XnatProjectdata> identifier = dicomObjectIdentifiers.get(beanId);
            _dicomObjectIdentifiers.put(beanId, identifier);
            if (identifier == primaryDicomObjectIdentifier) {
                primaryBeanId = beanId;
            } else {
                sortedDicomObjectIdentifierBeanIds.add(beanId);
                _dicomObjectIdentifiers.put(beanId, identifier);
            }
        }

        Collections.sort(sortedDicomObjectIdentifierBeanIds);
        if (StringUtils.isNotBlank(primaryBeanId)) {
            _primaryDicomObjectIdentifierBeanId = primaryBeanId;
            sortedDicomObjectIdentifierBeanIds.add(0, _primaryDicomObjectIdentifierBeanId);
        } else {
            _primaryDicomObjectIdentifierBeanId = sortedDicomObjectIdentifierBeanIds.get(0);
        }

        _dicomObjectIdentifierBeanIds = ImmutableSet.copyOf(sortedDicomObjectIdentifierBeanIds);
        _identifiersToMapFunction = new IdentifiersToMapFunction(_dicomObjectIdentifiers);
    }

    @PreDestroy
    public void shutdown() {
        _log.debug("Handling pre-destroy actions, shutting down DICOM SCP receivers.");
        stopDicomSCPs();
    }

    public DicomSCP create(final DicomSCPInstance instance) throws IOException, EnabledDICOMReceiverWithDuplicatePortException {
        instance.setId(getNextKey());
        _dicomScpPreferences.setDicomSCPInstance(instance);
        if (_log.isDebugEnabled()) {
            _log.debug("Created new DICOM SCP: " + instance.toString());
        }
        final DicomSCP dicomSCP = _dicomScpPreferences.getDicomSCP(instance.getId());
        if (instance.isEnabled()) {
            dicomSCP.start();
        }
        return dicomSCP;
    }

    public void delete(final int id) throws NrgServiceException {
        if (!_dicomScpPreferences.hasDicomSCPInstance(id)) {
            throw new NrgServiceException(NrgServiceError.UnknownEntity, "There is no DICOM SCP instance with the ID " + id);
        }
        if (_log.isDebugEnabled()) {
            _log.debug("Deleting DICOM SCP: " + id);
        }
        _dicomScpPreferences.deleteDicomSCPInstance(id);
    }

    public boolean hasDicomSCP(final int id) {
        return _dicomScpPreferences.hasDicomSCPInstance(id);
    }

    public Map<DicomSCP, Boolean> areDicomSCPsStarted() {
        final Map<DicomSCP, Boolean> statuses = new HashMap<>();
        for (final DicomSCP dicomSCP : _dicomScpPreferences.getDicomSCPs()) {
            statuses.put(dicomSCP, dicomSCP.isStarted());
        }
        return statuses;
    }

    public List<DicomSCPInstance> getDicomSCPInstances() {
        return new ArrayList<>(_dicomScpPreferences.getDicomSCPInstances().values());
    }

    public DicomSCPInstance getDicomSCPInstance(final int id) {
        return _dicomScpPreferences.getDicomSCPInstance(id);
    }

    public void setDicomSCPInstance(final DicomSCPInstance instance) throws EnabledDICOMReceiverWithDuplicatePortException {
        try {
            _dicomScpPreferences.setDicomSCPInstance(instance);
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Unable to update DICOM SCP: " + instance.getAeTitle() + ":" + instance.getPort(), e);
        }
    }

    public List<DicomSCP> startOrStopDicomSCPAsDictatedByConfiguration() {
        final boolean enableDicomReceiver = _siteConfigPreferences.isEnableDicomReceiver();
        return enableDicomReceiver ? startDicomSCPs() : stopDicomSCPs();
    }

    public List<DicomSCP> startDicomSCPs() {
        final List<DicomSCP> started = new ArrayList<>();
        for (final DicomSCPInstance instance : _dicomScpPreferences.getDicomSCPInstances().values()) {
            if (instance.isEnabled()) {
                final DicomSCP dicomSCP = startDicomSCP(instance);
                if (dicomSCP != null) {
                    started.add(dicomSCP);
                }
            }
        }
        return started;
    }

    public DicomSCP startDicomSCP(final int id) {
        final DicomSCPInstance instance = _dicomScpPreferences.getDicomSCPInstance(id);
        if (instance == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.UnknownEntity, "Couldn't find the DICOM SCP instance identified by " + id);
        }
        return startDicomSCP(instance);
    }

    public DicomSCP startDicomSCP(final DicomSCPInstance instance) {
        try {
            final DicomSCP dicomSCP = _dicomScpPreferences.getDicomSCP(instance.getId());
            if (!dicomSCP.isStarted()) {
                dicomSCP.start();
                return dicomSCP;
            }
            return null;
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Unable to start DICOM SCP: " + instance.getAeTitle() + ":" + instance.getPort(), e);
        }
    }

    public List<DicomSCP> stopDicomSCPs() {
        final List<DicomSCP> stopped = new ArrayList<>();
        for (final DicomSCPInstance instance : _dicomScpPreferences.getDicomSCPInstances().values()) {
            final DicomSCP dicomSCP = stopDicomSCP(instance);
            if (dicomSCP != null) {
                stopped.add(dicomSCP);
            }
        }
        return stopped;
    }

    public void stopDicomSCP(final int id) {
        final DicomSCPInstance instance = _dicomScpPreferences.getDicomSCPInstance(id);
        if (instance == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.UnknownEntity, "Couldn't find the DICOM SCP instance identified by " + id);
        }
        stopDicomSCP(instance);
    }

    public DicomSCP stopDicomSCP(final DicomSCPInstance instance) {
        try {
            final DicomSCP dicomSCP = _dicomScpPreferences.getDicomSCP(instance.getId());
            if (dicomSCP != null) {
                if (dicomSCP.isStarted()) {
                    dicomSCP.stop();
                    return dicomSCP;
                }
            }
            return null;
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Unable to stop DICOM SCP: " + instance.getAeTitle() + ":" + instance.getPort(), e);
        }
    }

    public void enableDicomSCP(final int id) throws EnabledDICOMReceiverWithDuplicatePortException {
        final DicomSCPInstance instance = _dicomScpPreferences.getDicomSCPInstance(id);
        try {
            if (!instance.isEnabled()) {
                instance.setEnabled(true);
                _dicomScpPreferences.setDicomSCPInstance(instance);
            }
            final DicomSCP dicomSCP = _dicomScpPreferences.getDicomSCP(id);
            if (!dicomSCP.isStarted()) {
                dicomSCP.start();
            }
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Unable to enable DICOM SCP: " + instance.getAeTitle() + ":" + instance.getPort(), e);
        }
    }

    public void disableDicomSCP(final int id) {
        final DicomSCPInstance instance = _dicomScpPreferences.getDicomSCPInstance(id);
        try {
            if (instance.isEnabled()) {
                instance.setEnabled(false);
                _dicomScpPreferences.setDicomSCPInstance(instance);
            }
            final DicomSCP dicomSCP = _dicomScpPreferences.getDicomSCP(id);
            if (dicomSCP.isStarted()) {
                dicomSCP.stop();
            }
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Unable to disable DICOM SCP: " + instance.getAeTitle() + ":" + instance.getPort(), e);
        } catch (EnabledDICOMReceiverWithDuplicatePortException ignored) {
            // We can ignore this: the exception comes when an enabled instance is inserted with the same port as
            // another enabled instance. Since we're explicitly disabling this instance, we won't actually get this
            // error.
        }
    }

    public Map<String, String> getDicomObjectIdentifierBeans() {
        return Maps.asMap(_dicomObjectIdentifierBeanIds, _identifiersToMapFunction);
    }

    public Map<String, DicomObjectIdentifier<XnatProjectdata>> getDicomObjectIdentifiers() {
        return _dicomObjectIdentifiers;
    }

    public DicomObjectIdentifier<XnatProjectdata> getDicomObjectIdentifier(final String beanId) {
        if (!_dicomObjectIdentifierBeanIds.contains(beanId)) {
            return null;
        }
        return getDicomObjectIdentifiers().get(beanId);
    }

    public DicomObjectIdentifier<XnatProjectdata> getDefaultDicomObjectIdentifier() {
        return getDicomObjectIdentifiers().get(_primaryDicomObjectIdentifierBeanId);
    }

    private int getNextKey() {
        final Set<String> keys = _dicomScpPreferences.getDicomSCPInstances().keySet();
        final Set<Integer> values = new HashSet<>(keys.size());
        for (final String key : keys) {
            values.add(Integer.parseInt(key));
        }
        return Collections.max(values) + 1;
    }

    private static class IdentifiersToMapFunction implements Function<String, String> {
        IdentifiersToMapFunction(final Map<String, DicomObjectIdentifier<XnatProjectdata>> identifiers) {
            _identifiers = identifiers;
        }

        @Nullable
        @Override
        public String apply(@Nullable final String beanId) {
            if (StringUtils.isBlank(beanId)) {
                return null;
            }
            final DicomObjectIdentifier<XnatProjectdata> identifier = _identifiers.get(beanId);
            if (identifier instanceof CompositeDicomObjectIdentifier) {
                return ((CompositeDicomObjectIdentifier) identifier).getName();
            }
            return beanId;
        }

        private final Map<String, DicomObjectIdentifier<XnatProjectdata>> _identifiers;
    }

    private static final Logger _log = LoggerFactory.getLogger(DicomSCPManager.class);

    private final DicomSCPPreference                                  _dicomScpPreferences;
    private final SiteConfigPreferences                               _siteConfigPreferences;
    private final String                                              _primaryDicomObjectIdentifierBeanId;
    private final Set<String>                                         _dicomObjectIdentifierBeanIds;
    private final Map<String, DicomObjectIdentifier<XnatProjectdata>> _dicomObjectIdentifiers;
    private final IdentifiersToMapFunction                            _identifiersToMapFunction;
}
