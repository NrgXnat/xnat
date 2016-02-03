package org.nrg.dcm.preferences;

import com.fasterxml.jackson.core.JsonParseException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.prefs.beans.BaseNrgPreferences;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPrefsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class DicomSCPPreferences extends BaseNrgPreferences {

    public DicomSCPPreferences(final NrgPrefsService service, final String toolId) throws IOException {
        super(service, toolId);
        final Properties properties = service.getToolProperties(toolId);
        if (properties.size() > 0) {
            for (final String property : properties.stringPropertyNames()) {
                final String value = properties.getProperty(property);
                try {
                    final DicomSCPInstance instance = DicomSCPInstance.deserialize(value);
                    if (_instances.containsKey(instance.getScpId())) {
                        throw new RuntimeException("Found multiple definitions for the DICOM SCP provider with ID " + instance.getScpId());
                    }
                    _instances.put(instance.getScpId(), instance);
                } catch (JsonParseException e) {
                    _log.error("The DICOM SCP instance is unparseable and can't be created or started: " + value);
                }
            }
        }
    }

    public boolean hasDicomSCPInstance(final String scpId) {
        return _instances.containsKey(scpId);
    }

    public List<DicomSCPInstance> getDicomSCPInstances() {
        return new ArrayList<>(_instances.values());
    }

    public DicomSCPInstance getDicomSCPInstance(final String scpId) {
        return _instances.get(scpId);
    }

    public void setDicomSCPInstance(final DicomSCPInstance instance) {
        final String scpId = instance.getScpId();
        try {
            set(scpId, DicomSCPInstance.serialize(instance));
            _instances.put(scpId, instance);
        } catch (IOException | NrgServiceException e) {
            _log.error("An error occurred writing the DICOM SCP instance " + scpId + " out to the preferences service.", e);
        }
    }

    public void deleteDicomSCPInstance(final String scpId) {
        if (_instances.containsKey(scpId)) {
            try {
                delete(scpId);
            } catch (InvalidPreferenceName exception) {
                throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to delete the DICOM SCP instance: " + scpId, exception);
            }
            _instances.remove(scpId);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(DicomSCPPreferences.class);

    private final Map<String, DicomSCPInstance> _instances = new HashMap<>();
}
