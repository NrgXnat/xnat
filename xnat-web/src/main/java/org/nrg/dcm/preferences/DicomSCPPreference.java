/*
 * web: org.nrg.dcm.preferences.DicomSCPPreference
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.preferences;

import org.apache.commons.lang3.StringUtils;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dcm.DicomSCP;
import org.nrg.dcm.exceptions.EnabledDICOMReceiverWithDuplicatePortException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

@NrgPreferenceBean(toolId = "dicomScpManager", toolName = "DICOM SCP Manager", description = "Manages configuration of the various DICOM SCP endpoints on the XNAT system.")
public class DicomSCPPreference extends AbstractPreferenceBean {
    @Autowired
    public DicomSCPPreference(final NrgPreferenceService preferenceService, final XnatUserProvider primaryAdminUserProvider, final ApplicationContext context) {
        super(preferenceService);
        _provider = primaryAdminUserProvider;
        _context = context;
    }

    public boolean hasDicomSCPInstance(final int id) {
        return getDicomSCPInstances().containsKey(Integer.toString(id));
    }

    @SuppressWarnings("unused")
    public List<DicomSCPInstance> getDicomSCPAtAETitle(final String aeTitle) {
        final List<DicomSCPInstance> found = new ArrayList<>();
        for (final DicomSCPInstance instance : getDicomSCPInstances().values()) {
            if (StringUtils.equals(aeTitle, instance.getAeTitle())) {
                found.add(instance);
            }
        }
        return found;
    }

    public DicomSCPInstance getDicomSCPAtPort(final int port) {
        for (final DicomSCPInstance instance : getDicomSCPInstances().values()) {
            if (port == instance.getPort() && instance.isEnabled()) {
                return instance;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public List<DicomSCPInstance> getAllDicomSCPsAtPort(final int port) {
        final List<DicomSCPInstance> found = new ArrayList<>();
        for (final DicomSCPInstance instance : getDicomSCPInstances().values()) {
            if (port == instance.getPort() && instance.isEnabled()) {
                found.add(instance);
            }
        }
        return found;
    }

    @NrgPreference(defaultValue = "{'1': {'id': '1', 'aeTitle': 'XNAT', 'port': 8104, 'enabled': true}}", key = "id")
    public Map<String, DicomSCPInstance> getDicomSCPInstances() {
        return getMapValue(PREF_ID);
    }

    public DicomSCPInstance getDicomSCPInstance(final int id) {
        return getDicomSCPInstances().get(Integer.toString(id));
    }

    public void setDicomSCPInstance(final DicomSCPInstance instance) throws IOException, EnabledDICOMReceiverWithDuplicatePortException {
        final int id = instance.getId();

        final DicomSCPInstance atPort = getDicomSCPAtPort(instance.getPort());
        if (atPort != null && atPort.getId() != id) {
            throw new EnabledDICOMReceiverWithDuplicatePortException(atPort, instance);
        }

        if (hasDicomSCPInstance(id)) {
            deleteDicomSCP(id);
        }

        try {
            set(serialize(instance), PREF_ID, Integer.toString(id));
        } catch (InvalidPreferenceName invalidPreferenceName) {
            _log.info("Got an invalidate preference name error for " + id);
        }
        _dicomSCPs.put(id, getDicomSCP(id));
    }

    public void deleteDicomSCPInstance(final int id) {
        deleteDicomSCP(id);
        try {
            delete(PREF_ID, Integer.toString(id));
        } catch (InvalidPreferenceName invalidPreferenceName) {
            _log.info("Got an invalidate preference name error trying to delete DICOM SCP instance " + id);
        }
    }

    public List<DicomSCP> getDicomSCPs() {
        return new ArrayList<>(_dicomSCPs.values());
    }

    public DicomSCP getDicomSCP(final int id) throws IOException {
        if (!hasDicomSCPInstance(id)) {
            throw new NrgServiceRuntimeException(NrgServiceError.UnknownEntity, "There is no definition for the DICOM SCP with ID " + id);
        }
        if (!_dicomSCPs.containsKey(id)) {
            final DicomSCPInstance instance = getDicomSCPInstance(id);
            _dicomSCPs.put(id, DicomSCP.create(id, Executors.newCachedThreadPool(), instance.getPort(), _provider, instance.getAeTitle(), getIdentifier(instance.getIdentifier()), getDicomFileNamer(instance.getFileNamer())));
        }
        return _dicomSCPs.get(id);
    }

    private void deleteDicomSCP(final int id) {
        if (_dicomSCPs.containsKey(id)) {
            final DicomSCP deleted = _dicomSCPs.remove(id);
            deleted.stop();
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

    private static final Logger _log    = LoggerFactory.getLogger(DicomSCPInstance.class);
    private static final String PREF_ID = "dicomSCPInstances";

    private final XnatUserProvider   _provider;
    private final ApplicationContext _context;
    private final Map<Integer, DicomSCP> _dicomSCPs = new HashMap<>();
}
