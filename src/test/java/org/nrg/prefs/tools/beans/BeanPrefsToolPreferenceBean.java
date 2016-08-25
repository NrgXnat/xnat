package org.nrg.prefs.tools.beans;

import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
@NrgPreferenceBean(toolId = "beanPreferenceBean",
                   toolName = "Bean preference bean",
                   description = "Manages preferences that are stored in a bean rather than a simple Java type.")
public class BeanPrefsToolPreferenceBean extends AbstractPreferenceBean {
    @Autowired
    public BeanPrefsToolPreferenceBean(final NrgPreferenceService preferenceService) {
        super(preferenceService);
    }

    @NrgPreference(defaultValue = "{'scpId':'CCIR','aeTitle':'CCIR','port':8104,'enabled':true}")
    public BeanPrefsToolPreference getPrefA() throws IOException {
        return deserialize(getValue("prefA"), BeanPrefsToolPreference.class);
    }

    public void setPrefA(final BeanPrefsToolPreference preference) throws IOException, InvalidPreferenceName {
        set(serialize(preference), "prefA");
    }

    @NrgPreference(defaultValue = "['XNAT','CCIR']")
    public List<String> getPrefBs() {
        return getListValue("prefBs");
    }

    public void setPrefBs(final List<String> preferences) throws IOException, InvalidPreferenceName {
        set(serialize(preferences), "prefBs");
    }

    @NrgPreference(defaultValue = "[{'scpId':'XNAT','aeTitle':'XNAT','port':8104,'enabled':true},{'scpId':'CCIR','aeTitle':'CCIR','port':8104,'enabled':true}]", key = "scpId")
    public List<BeanPrefsToolPreference> getPrefCs() {
        return getListValue("prefCs");
    }

    public void setPrefCs(final List<BeanPrefsToolPreference> preferences) throws IOException, InvalidPreferenceName {
        for (final BeanPrefsToolPreference preference : preferences) {
            setPrefC(preference);
        }
    }

    public BeanPrefsToolPreference getPrefC(final String scpId) throws IOException {
        final String value = getValue("prefCs", scpId);
        return deserialize(value, BeanPrefsToolPreference.class);
    }

    public void setPrefC(final BeanPrefsToolPreference instance) throws IOException, InvalidPreferenceName {
        final String prefCId = getPrefId("prefCs", instance);
        set(serialize(instance), prefCId);
    }

    public void deletePrefC(final String scpId) throws InvalidPreferenceName {
        final String prefCId = getNamespacedPropertyId("prefCs", scpId);
        delete(prefCId);
    }

    @NrgPreference(defaultValue = "{'XNAT':'192.168.10.1','CCIR':'192.168.10.100'}")
    public Map<String, String> getPrefDs() {
        return getMapValue("prefDs");
    }

    public void setPrefDs(final Map<String, String> preferences) throws IOException, InvalidPreferenceName {
        setMapValue("prefDs", preferences);
    }

    public String getPrefD(final String scpId) {
        return getPrefDs().get(scpId);
    }

    public void setPrefD(final String scpId, final String preference) throws InvalidPreferenceName, IOException {
        set(serialize(preference), "prefDs", scpId);
    }

    public void deletePrefD(final String scpId) throws InvalidPreferenceName {
        final String prefDId = getNamespacedPropertyId("prefDs", scpId);
        delete(prefDId);
    }

    @NrgPreference(defaultValue = "{'XNAT':{'port':8104,'scpId':'XNAT','identifier':'XNAT','aeTitle':'XNAT','enabled':true},'CCIR':{'port':8104,'scpId':'CCIR','identifier':'CCIR','aeTitle':'CCIR','enabled':true}}", key = "scpId")
    public Map<String, BeanPrefsToolPreference> getPrefEs() {
        return getMapValue("prefEs");
    }

    public void setPrefEs(final Map<String, BeanPrefsToolPreference> preferences) throws IOException, InvalidPreferenceName {
        setMapValue("prefEs", preferences);
    }

    public BeanPrefsToolPreference getPrefE(final String scpId) {
        return getPrefEs().get(scpId);
    }

    public void setPrefE(final String scpId, final BeanPrefsToolPreference preference) throws InvalidPreferenceName, IOException {
        set(serialize(preference), "prefEs", scpId);
    }

    public void deletePrefE(final String scpId) throws InvalidPreferenceName {
        final String prefEId = getNamespacedPropertyId("prefEs", scpId);
        delete(prefEId);
    }

    private String getPrefId(final String prefId, final BeanPrefsToolPreference preference) {
        final String scpId = preference.getScpId();
        return getNamespacedPropertyId(prefId, scpId);
    }
}
