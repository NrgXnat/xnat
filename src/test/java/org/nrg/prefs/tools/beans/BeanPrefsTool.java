package org.nrg.prefs.tools.beans;

import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@Component
public class BeanPrefsTool {

    public BeanPrefsToolPreference getPrefA() throws IOException {
        return _bean.getPrefA();
    }

    public void setPrefA(final BeanPrefsToolPreference prefA) throws IOException, InvalidPreferenceName {
        _bean.setPrefA(prefA);
    }

    public List<BeanPrefsToolPreference> getPrefBs() {
        return _bean.getPrefBs();
    }

    public void setPrefBs(final List<BeanPrefsToolPreference> preferences) throws IOException, InvalidPreferenceName {
        _bean.setPrefBs(preferences);
    }

    public BeanPrefsToolPreference getPrefB(final String scpId) throws IOException {
        return _bean.getPrefB(scpId);
    }

    public void setPrefB(final BeanPrefsToolPreference bean) {
        _bean.setPrefB(bean);
    }

    public void deletePrefB(final String prefB) {
        _bean.deletePrefB(prefB);
    }

    @Inject
    private BeanPrefsToolPreferenceBean _bean;
}
