/*
 * prefs: org.nrg.prefs.tools.beans.BeanPrefsToolPreferenceBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tools.beans;

import com.google.common.collect.Lists;
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
        return getObjectValue("prefA");
    }

    public void setPrefA(final BeanPrefsToolPreference preference) throws IOException, InvalidPreferenceName {
        setObjectValue(preference, "prefA");
    }

    @NrgPreference(defaultValue = "['XNAT','CCIR']")
    public List<String> getPrefBs() {
        return getListValue("prefBs");
    }

    public void setPrefBs(final List<String> preferences) throws IOException, InvalidPreferenceName {
        setListValue("prefBs", preferences);
    }

    @NrgPreference(defaultValue = "[{'scpId':'XNAT','aeTitle':'XNAT','port':8104,'enabled':true},{'scpId':'CCIR','aeTitle':'CCIR','port':8104,'enabled':true}]", key = "scpId")
    public List<BeanPrefsToolPreference> getPrefCs() {
        return getListValue("prefCs");
    }

    public void setPrefCs(final List<BeanPrefsToolPreference> preferences) throws IOException, InvalidPreferenceName {
        setListValue("prefCs", preferences);
    }

    public BeanPrefsToolPreference getPrefC(final String scpId) throws IOException {
        final List<BeanPrefsToolPreference> prefCs = getPrefCs();
        for (final BeanPrefsToolPreference prefC : prefCs) {
            if (prefC.getScpId().equals(scpId)) {
                return prefC;
            }
        }
        return null;
    }

    public void setPrefC(final BeanPrefsToolPreference preference) throws IOException, InvalidPreferenceName {
        final String scpId = preference.getScpId();
        final List<BeanPrefsToolPreference> prefCs = getPrefCs();
        final List<BeanPrefsToolPreference> newPrefCs = Lists.newArrayList();
        for (final BeanPrefsToolPreference prefC : prefCs) {
            if (prefC.getScpId().equals(scpId)) {
                newPrefCs.add(preference);
                break;
            } else {
                newPrefCs.add(prefC);
            }
        }
        if (!newPrefCs.contains(preference)) {
            newPrefCs.add(preference);
        }
        setPrefCs(newPrefCs);
    }

    public void deletePrefC(final String scpId) throws InvalidPreferenceName, IOException {
        final List<BeanPrefsToolPreference> prefCs = getPrefCs();
        BeanPrefsToolPreference target = null;
        for (final BeanPrefsToolPreference prefC : prefCs) {
            if (prefC.getScpId().equals(scpId)) {
                target = prefC;
                break;
            }
        }
        if (target != null) {
            prefCs.remove(target);
            setPrefCs(prefCs);
        }
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
        final Map<String, String> prefDs = getPrefDs();
        prefDs.put(scpId, preference);
        setPrefDs(prefDs);
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
        final Map<String, BeanPrefsToolPreference> prefEs = getPrefEs();
        prefEs.put(scpId, preference);
        setPrefEs(prefEs);
    }

    public void deletePrefE(final String scpId) throws InvalidPreferenceName, IOException {
        final Map<String, BeanPrefsToolPreference> prefEs = getPrefEs();
        if (prefEs.containsKey(scpId)) {
            prefEs.remove(scpId);
            setPrefEs(prefEs);
        }
    }

    @NrgPreference(defaultValue = "[]")
    public List<String> getPrefFs() {
        return getListValue("prefFs");
    }

    public void setPrefFs(final List<String> preferences) throws IOException, InvalidPreferenceName {
        setListValue("prefFs", preferences);
    }
}
