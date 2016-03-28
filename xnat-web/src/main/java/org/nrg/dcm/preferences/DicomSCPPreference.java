package org.nrg.dcm.preferences;

import org.apache.commons.lang3.StringUtils;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dcm.DicomSCP;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

@NrgPreferenceBean(toolId = "dicomScpManager", toolName = "DICOM SCP Manager", description = "Manages configuration of the various DICOM SCP endpoints on the XNAT system.")
public class DicomSCPPreference extends AbstractPreferenceBean implements ApplicationContextAware {

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        _context = context;
    }

    public boolean hasDicomSCPInstance(final String scpId) {
        return getDicomSCPInstances().containsKey(scpId);
    }

    @NrgPreference(defaultValue = "{'XNAT':{'scpId':'XNAT','aeTitle':'XNAT','port':8104,'enabled':true}}", key = "scpId")
    public Map<String, DicomSCPInstance> getDicomSCPInstances() {
        return getMapValue(PREF_ID);
    }

    public DicomSCPInstance getDicomSCPInstance(final String scpId) {
        return getDicomSCPInstances().get(scpId);
    }

    public void setDicomSCPInstance(final DicomSCPInstance instance) throws IOException {
        final String scpId = instance.getScpId();
        deleteDicomSCP(scpId);
        try {
            set(serialize(instance), PREF_ID, scpId);
        } catch (InvalidPreferenceName invalidPreferenceName) {
            _log.info("Got an invalidate preference name error for " + scpId);
        }
        _dicomSCPs.put(scpId, getDicomSCP(scpId));
    }

    public void deleteDicomSCPInstance(final String scpId) {
        deleteDicomSCP(scpId);
        try {
            delete(PREF_ID, scpId);
        } catch (InvalidPreferenceName invalidPreferenceName) {
            _log.info("Got an invalidate preference name error trying to delete DICOM SCP instance " + scpId);
        }
    }

    public List<DicomSCP> getDicomSCPs() {
        return new ArrayList<>(_dicomSCPs.values());
    }

    public DicomSCP getDicomSCP(final String scpId) throws IOException {
        if (!hasDicomSCPInstance(scpId)) {
            throw new NrgServiceRuntimeException(NrgServiceError.UnknownEntity, "There is no definition for the DICOM SCP with ID " + scpId);
        }
        if (!_dicomSCPs.containsKey(scpId)) {
            final DicomSCPInstance instance = getDicomSCPInstance(scpId);
            _dicomSCPs.put(scpId, DicomSCP.create(scpId, Executors.newCachedThreadPool(), instance.getPort(), _provider, instance.getAeTitle(), getIdentifier(instance.getIdentifier()), getDicomFileNamer(instance.getFileNamer())));
        }
        return _dicomSCPs.get(scpId);
    }

    private void deleteDicomSCP(final String scpId) {
        if (_dicomSCPs.containsKey(scpId)) {
            final DicomSCP deleted = _dicomSCPs.remove(scpId);
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

    @Inject
    private XnatUserProvider _provider;

    private ApplicationContext _context;
    private final Map<String, DicomSCP> _dicomSCPs = new HashMap<>();
}
