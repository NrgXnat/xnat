package org.nrg.prefs.beans;

import org.nrg.framework.constants.Scope;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;

public interface NrgPreferences {
    Preference get(String preference, String entityId) throws UnknownToolId;

    Preference get(String preference, Scope scope, String entityId) throws UnknownToolId;

    String getValue(String preferenceName) throws UnknownToolId;

    String getValue(String preferenceName, String entityId) throws UnknownToolId;

    String getValue(String preferenceName, Scope scope, String entityId) throws UnknownToolId;

    void set(String preference, String value) throws UnknownToolId, InvalidPreferenceName;

    void set(String preference, String value, String entityId) throws UnknownToolId, InvalidPreferenceName;

    void set(String preference, String value, Scope scope, String entityId) throws UnknownToolId, InvalidPreferenceName;

    void delete(String preference) throws InvalidPreferenceName;

    void delete(String preference, String entityId) throws InvalidPreferenceName;

    void delete(String preference, Scope scope, String entityId) throws InvalidPreferenceName;
}
