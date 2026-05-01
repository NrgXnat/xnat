/*
 * prefs: org.nrg.prefs.tools.beans.BeanPrefsTool
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tools.beans;

import org.nrg.prefs.beans.PreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class BeanPrefsTool {
    private BeanPrefsToolPreferenceBean _bean;

    @Autowired
    public void setBeanPrefsToolPreferenceBean(final BeanPrefsToolPreferenceBean bean) {
        _bean = bean;
    }

    public BeanPrefsToolPreference getPrefA() {
        return _bean.getPrefA();
    }

    public void setPrefA(final BeanPrefsToolPreference prefA) throws InvalidPreferenceName {
        _bean.setPrefA(prefA);
    }

    public List<String> getPrefBs() {
        return _bean.getPrefBs();
    }

    public void setPrefBs(final List<String> preferences) throws InvalidPreferenceName {
        _bean.setPrefBs(preferences);
    }

    public List<BeanPrefsToolPreference> getPrefCs() {
        return _bean.getPrefCs();
    }

    @SuppressWarnings("unused")
    public void setPrefCs(final List<BeanPrefsToolPreference> preferences) throws InvalidPreferenceName {
        _bean.setPrefCs(preferences);
    }

    @SuppressWarnings("unused")
    public BeanPrefsToolPreference getPrefC(final String scpId) {
        return _bean.getPrefC(scpId);
    }

    public void setPrefC(final BeanPrefsToolPreference bean) throws InvalidPreferenceName {
        _bean.setPrefC(bean);
    }

    public void deletePrefC(final String prefC) throws InvalidPreferenceName {
        _bean.deletePrefC(prefC);
    }

    public Map<String, String> getPrefDs() {
        return _bean.getPrefDs();
    }

    @SuppressWarnings("unused")
    public void setPrefDs(final Map<String, String> preferences) throws InvalidPreferenceName {
        _bean.setPrefDs(preferences);
    }

    public String getPrefD(final String scpId) {
        return _bean.getPrefD(scpId);
    }

    public void setPrefD(final String scpId, final String bean) throws InvalidPreferenceName {
        _bean.setPrefD(scpId, bean);
    }

    public void deletePrefD(final String prefD) throws InvalidPreferenceName {
        _bean.deletePrefD(prefD);
    }

    public Map<String, BeanPrefsToolPreference> getPrefEs() {
        return _bean.getPrefEs();
    }

    @SuppressWarnings("unused")
    public void setPrefEs(final Map<String, BeanPrefsToolPreference> preferences) throws InvalidPreferenceName {
        _bean.setPrefEs(preferences);
    }

    @SuppressWarnings("unused")
    public BeanPrefsToolPreference getPrefE(final String scpId) {
        return _bean.getPrefE(scpId);
    }

    @SuppressWarnings("unused")
    public void setPrefE(final String scpId, final BeanPrefsToolPreference bean) throws InvalidPreferenceName {
        _bean.setPrefE(scpId, bean);
    }

    @SuppressWarnings("unused")
    public void deletePrefE(final String prefE) throws InvalidPreferenceName {
        _bean.deletePrefE(prefE);
    }

    public List<String> getPrefFs() {
        return _bean.getPrefFs();
    }

    @SuppressWarnings("unused")
    public void setPrefFs(final List<String> preferences) throws InvalidPreferenceName {
        _bean.setPrefFs(preferences);
    }

    public Map<String, Object> getPreferences() {
        return _bean;
    }

    public PreferenceBean getPreferenceBean() {
        return _bean;
    }
}
