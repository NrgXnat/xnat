package org.nrg.prefs.beans;

import org.nrg.framework.constants.Scope;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.PreferenceInfo;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;

import java.util.List;
import java.util.Map;

public interface PreferenceBean {
    void initialize();

    String getToolId();

    Class<? extends PreferenceEntityResolver> getResolver();

    Preference get(final String... preference) throws UnknownToolId;

    Preference get(final Scope scope, final String entityId, final String... preference) throws UnknownToolId;

    String getValue(final String... preferenceName) throws UnknownToolId;

    String getValue(final Scope scope, final String entityId, final String... preferenceName) throws UnknownToolId;

    <T> Map<String, T> getMapValue(final String preferenceName) throws UnknownToolId;

    <T> Map<String, T> getMapValue(final Scope scope, final String entityId, final String preferenceName) throws UnknownToolId;

    <T> List<T> getListValue(final String preferenceName) throws UnknownToolId;

    <T> List<T> getListValue(final Scope scope, final String entityId, final String preferenceName) throws UnknownToolId;

    <T> T[] getArrayValue(final String preferenceName) throws UnknownToolId;

    <T> T[] getArrayValue(final Scope scope, final String entityId, final String preferenceName) throws UnknownToolId;

    void set(String value, String... preference) throws UnknownToolId, InvalidPreferenceName;

    void set(final Scope scope, final String entityId, final String value, final String... preference) throws UnknownToolId, InvalidPreferenceName;

    <T> void setMapValue(final String preferenceName, Map<String, T> map) throws UnknownToolId;

    <T> void setMapValue(final Scope scope, final String entityId, final String preferenceName, Map<String, T> map) throws UnknownToolId;

    <T> void setListValue(final String preferenceName, List<T> list) throws UnknownToolId;

    <T> void setListValue(final Scope scope, final String entityId, final String preferenceName, List<T> list) throws UnknownToolId;

    <T> void setArrayValue(final String preferenceName, T[] array) throws UnknownToolId;

    <T> void setArrayValue(final Scope scope, final String entityId, final String preferenceName, T[] array) throws UnknownToolId;

    void delete(final String... preference) throws InvalidPreferenceName;

    void delete(final Scope scope, final String entityId, final String... preference) throws InvalidPreferenceName;

    Map<String, PreferenceInfo> getDefaultPreferences();
}
