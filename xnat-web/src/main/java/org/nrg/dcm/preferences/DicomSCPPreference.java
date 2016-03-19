package org.nrg.dcm.preferences;

import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

@NrgPreferenceBean(toolId = "dicomScpManager", toolName = "DICOM SCP Manager", description = "Manages configuration of the various DICOM SCP endpoints on the XNAT system.")
public class DicomSCPPreference extends AbstractPreferenceBean {
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
        try {
            set(serialize(instance), PREF_ID, instance.getScpId());
        } catch (InvalidPreferenceName invalidPreferenceName) {
            _log.info("Got an invalidate preference name error for " + instance.getScpId());
        }
    }

    public void deleteDicomSCPInstance(final String scpId) {
        final String instanceId = getNamespacedPropertyId(PREF_ID, scpId);
        try {
            delete(instanceId);
        } catch (InvalidPreferenceName invalidPreferenceName) {
            _log.info("Got an invalidate preference name error trying to delete DICOM SCP instance " + scpId);
        }
    }
    private static final Logger _log = LoggerFactory.getLogger(DicomSCPInstance.class);
    private static final String PREF_ID = "dicomSCPInstances";
}
