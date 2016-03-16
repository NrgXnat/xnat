package org.nrg.prefs.tools.beans;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@NrgPreferenceBean(toolId = "beanPreferenceBean",
                   toolName = "Bean preference bean",
                   description = "Manages preferences that are stored in a bean rather than a simple Java type.")
public class BeanPrefsToolPreferenceBean extends AbstractPreferenceBean {

    @NrgPreference(defaultValue = "{'scpId':'CCIR','aeTitle':'CCIR','port':8104,'enabled':true}")
    public BeanPrefsToolPreference getPrefA() throws IOException {
        return deserialize(getValue("prefA"), BeanPrefsToolPreference.class);
    }

    public void setPrefA(final BeanPrefsToolPreference preference) throws IOException, InvalidPreferenceName {
        set(serialize(preference));
    }

    @NrgPreference(defaultValue = "[{'scpId':'XNAT','aeTitle':'XNAT','port':8104,'enabled':true},{'scpId':'CCIR','aeTitle':'CCIR','port':8104,'enabled':true}]", key = "scpId")
    public List<BeanPrefsToolPreference> getPrefBs() {
        return getListValue("prefB");
    }

    public void setPrefBs(final List<BeanPrefsToolPreference> preferences) throws IOException, InvalidPreferenceName {
        for (final BeanPrefsToolPreference preference : preferences) {
            setPrefB(preference);
        }
    }

    public BeanPrefsToolPreference getPrefB(final String scpId) throws IOException {
        final String value = getValue("prefB", scpId);
        return deserialize(value, BeanPrefsToolPreference.class);
    }

    public void setPrefB(final BeanPrefsToolPreference instance) {
        final String prefBId = getPrefBId(instance);
        try {
            set(serialize(instance));
        } catch (IOException | NrgServiceException e) {
            _log.error("An error occurred writing the DICOM SCP instance " + prefBId + " out to the preferences service.", e);
        }
    }

    public void deletePrefB(final String scpId) {
        final String prefBId = getNamespacedPropertyId("prefB", scpId);
        try {
            delete(prefBId);
        } catch (InvalidPreferenceName exception) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "An error occurred trying to delete the DICOM SCP instance: couldn't find the bean identified by the ID " + scpId, exception);
        }
    }

    @NrgPreference(defaultValue = "{'XNAT':{'port':8104,'scpId':'XNAT','identifier':'XNAT','aeTitle':'XNAT','enabled':true},'CCIR':{'port':8104,'scpId':'CCIR','identifier':'CCIR','aeTitle':'CCIR','enabled':true}}", key = "scpId")
    public Map<String, BeanPrefsToolPreference> getPrefCs() {
        return getMapValue("prefC");
    }

    public void setPrefCs(final Map<String, BeanPrefsToolPreference> preferences) throws IOException, InvalidPreferenceName {
        setMapValue("prefC", preferences);
    }

    private String getPrefBId(final BeanPrefsToolPreference preference) {
        final String scpId = preference.getScpId();
        return getNamespacedPropertyId("prefB", scpId);
    }

    private static final Logger       _log    = LoggerFactory.getLogger(BeanPrefsToolPreferenceBean.class);
}
