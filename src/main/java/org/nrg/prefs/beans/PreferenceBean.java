package org.nrg.prefs.beans;

import org.nrg.framework.constants.Scope;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.PreferenceInfo;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public interface PreferenceBean {
    String NAMESPACE_DELIMITER = ":";

    String getToolId();

    Set<String> getPreferenceKeys();

    Map<String, Object> getPreferenceMap();

    Map<String, Object> getPreferenceMap(final String... preferenceNames);

    Map<String, Object> getPreferenceMap(final Set<String> preferenceNames);

    Class<? extends PreferenceEntityResolver> getResolver();

    Preference get(final String key, final String... subkeys) throws UnknownToolId;

    Preference get(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId;

    String getValue(final String key, final String... subkeys) throws UnknownToolId;

    String getValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId;

    Object getProperty(final String preference) throws UnknownToolId;

    Object getProperty(String preference, Object defaultValue) throws UnknownToolId;

    Object getProperty(final Scope scope, final String entityId, final String preference) throws UnknownToolId;

    Object getProperty(Scope scope, String entityId, String preference, Object defaultValue) throws UnknownToolId;

    Boolean getBooleanValue(final String key, final String... subkeys) throws UnknownToolId;

    Boolean getBooleanValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId;

    Integer getIntegerValue(final String key, final String... subkeys) throws UnknownToolId;

    Integer getIntegerValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId;

    Long getLongValue(final String key, final String... subkeys) throws UnknownToolId;

    Long getLongValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId;

    Float getFloatValue(final String key, final String... subkeys) throws UnknownToolId;

    Float getFloatValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId;

    Double getDoubleValue(final String key, final String... subkeys) throws UnknownToolId;

    Double getDoubleValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId;

    Date getDateValue(final String key, final String... subkeys) throws UnknownToolId;

    Date getDateValue(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId;

    <T> Map<String, T> getMapValue(final String preferenceName) throws UnknownToolId;

    <T> Map<String, T> getMapValue(final Scope scope, final String entityId, final String preferenceName) throws UnknownToolId;

    <T> List<T> getListValue(final String preferenceName) throws UnknownToolId;

    <T> List<T> getListValue(final Scope scope, final String entityId, final String preferenceName) throws UnknownToolId;

    <T> T[] getArrayValue(final String preferenceName) throws UnknownToolId;

    <T> T[] getArrayValue(final Scope scope, final String entityId, final String preferenceName) throws UnknownToolId;

    void create(String value, String key, String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    void create(Scope scope, String entityId, String value, String key, String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    String set(final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    String set(final Scope scope, final String entityId, final String value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    void setBooleanValue(final Boolean value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    void setBooleanValue(final Scope scope, final String entityId, final Boolean value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    void setIntegerValue(final Integer value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    void setIntegerValue(final Scope scope, final String entityId, final Integer value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    void setLongValue(final Long value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    void setLongValue(final Scope scope, final String entityId, final Long value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    void setFloatValue(final Float value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    void setFloatValue(final Scope scope, final String entityId, final Float value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    void setDoubleValue(final Double value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    void setDoubleValue(final Scope scope, final String entityId, final Double value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    void setDateValue(final Date value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    void setDateValue(final Scope scope, final String entityId, final Date value, final String key, final String... subkeys) throws UnknownToolId, InvalidPreferenceName;

    <T> void setMapValue(final String preferenceName, Map<String, T> map) throws UnknownToolId, InvalidPreferenceName;

    <T> void setMapValue(final Scope scope, final String entityId, final String preferenceName, Map<String, T> map) throws UnknownToolId, InvalidPreferenceName;

    <T> void setListValue(final String preferenceName, List<T> list) throws UnknownToolId, InvalidPreferenceName;

    <T> void setListValue(final Scope scope, final String entityId, final String preferenceName, List<T> list) throws UnknownToolId, InvalidPreferenceName;

    <T> void setArrayValue(final String preferenceName, T[] array) throws UnknownToolId, InvalidPreferenceName;

    <T> void setArrayValue(final Scope scope, final String entityId, final String preferenceName, T[] array) throws UnknownToolId, InvalidPreferenceName;

    void delete(final String key, final String... subkeys) throws InvalidPreferenceName;

    void delete(final Scope scope, final String entityId, final String key, final String... subkeys) throws InvalidPreferenceName;

    Map<String, PreferenceInfo> getDefaultPreferences();
}
