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
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class BeanPrefsTool {

    public BeanPrefsToolPreference getPrefA() throws IOException {
        return _bean.getPrefA();
    }

    public void setPrefA(final BeanPrefsToolPreference prefA) throws IOException, InvalidPreferenceName {
        _bean.setPrefA(prefA);
    }

    public List<String> getPrefBs() {
        return _bean.getPrefBs();
    }

    public void setPrefBs(final List<String> preferences) throws IOException, InvalidPreferenceName {
        _bean.setPrefBs(preferences);
    }

    public List<BeanPrefsToolPreference> getPrefCs() {
        return _bean.getPrefCs();
    }

    public void setPrefCs(final List<BeanPrefsToolPreference> preferences) throws IOException, InvalidPreferenceName {
        _bean.setPrefCs(preferences);
    }

    public BeanPrefsToolPreference getPrefC(final String scpId) throws IOException {
        return _bean.getPrefC(scpId);
    }

    public void setPrefC(final BeanPrefsToolPreference bean) throws IOException, InvalidPreferenceName {
        _bean.setPrefC(bean);
    }

    public void deletePrefC(final String prefC) throws InvalidPreferenceName, IOException {
        _bean.deletePrefC(prefC);
    }

    public Map<String, String> getPrefDs() {
        return _bean.getPrefDs();
    }

    public void setPrefDs(final Map<String, String> preferences) throws IOException, InvalidPreferenceName {
        _bean.setPrefDs(preferences);
    }

    public String getPrefD(final String scpId) throws IOException {
        return _bean.getPrefD(scpId);
    }

    public void setPrefD(final String scpId, final String bean) throws IOException, InvalidPreferenceName {
        _bean.setPrefD(scpId, bean);
    }

    public void deletePrefD(final String prefD) throws InvalidPreferenceName {
        _bean.deletePrefD(prefD);
    }

    public Map<String, BeanPrefsToolPreference> getPrefEs() {
        return _bean.getPrefEs();
    }

    public void setPrefEs(final Map<String, BeanPrefsToolPreference> preferences) throws IOException, InvalidPreferenceName {
        _bean.setPrefEs(preferences);
    }

    public BeanPrefsToolPreference getPrefE(final String scpId) throws IOException {
        return _bean.getPrefE(scpId);
    }

    public void setPrefE(final String scpId, final BeanPrefsToolPreference bean) throws IOException, InvalidPreferenceName {
        _bean.setPrefE(scpId, bean);
    }

    public void deletePrefE(final String prefE) throws InvalidPreferenceName, IOException {
        _bean.deletePrefE(prefE);
    }

    public List<String> getPrefFs() {
        return _bean.getPrefFs();
    }

    public void setPrefFs(final List<String> preferences) throws IOException, InvalidPreferenceName {
        _bean.setPrefFs(preferences);
    }

    public Map<String, Object> getPreferences() {
        return _bean;
    }

    public PreferenceBean getPreferenceBean() {
        return _bean;
    }

    @Inject
    private BeanPrefsToolPreferenceBean _bean;
}
