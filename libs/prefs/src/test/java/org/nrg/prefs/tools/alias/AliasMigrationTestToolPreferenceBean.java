/*
 * prefs: org.nrg.prefs.tools.alias.AliasMigrationTestToolPreferenceBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tools.alias;

import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("WeakerAccess")
@NrgPreferenceBean(toolId = AliasMigrationTestToolPreferenceBean.TOOL_ID, toolName = AliasMigrationTestToolPreferenceBean.TOOL_NAME, description = "This is a test of migrating aliases to primary preference names.")
public class AliasMigrationTestToolPreferenceBean extends AbstractPreferenceBean {

    public static final String TOOL_ID   = "aliasMigration";
    public static final String TOOL_NAME = "Alias Migration Test";

    public static final String PREF_A              = "prefA";
    public static final String PREF_A_ALIAS        = "prefAAlias";
    public static final String PREF_A_VALUE        = "valueA";
    public static final String PREF_A_IMPORT_VALUE = "importValueA";
    public static final String PREF_B              = "prefB";
    public static final String PREF_B_VALUE        = "valueB";
    public static final String PREF_B_IMPORT_VALUE = "importValueB";
    public static final String PREF_C              = "prefC";
    public static final String PREF_C_ALIAS        = "prefCAlias";
    public static final String PREF_C_VALUE        = "valueC";
    public static final String PREF_C_IMPORT_VALUE = "importValueC";

    @Autowired
    public AliasMigrationTestToolPreferenceBean(final NrgPreferenceService preferenceService) {
        super(preferenceService);
    }

    @NrgPreference(defaultValue = PREF_A_VALUE, aliases = PREF_A_ALIAS)
    public String getPrefA() {
        return getValue(PREF_A);
    }

    public void setPrefA(final String prefA) throws InvalidPreferenceName {
        set(prefA, PREF_A);
    }

    @NrgPreference(defaultValue = PREF_B_VALUE)
    public String getPrefB() {
        return getValue(PREF_B_VALUE);
    }

    public void setPrefB(final String prefB) throws InvalidPreferenceName {
        set(prefB, PREF_B);
    }

    @NrgPreference(defaultValue = PREF_C_VALUE, aliases = PREF_C_ALIAS)
    public String getPrefC() {
        return getValue(PREF_C);
    }

    public void setPrefC(final String prefC) throws InvalidPreferenceName {
        set(prefC, PREF_C);
    }
}
