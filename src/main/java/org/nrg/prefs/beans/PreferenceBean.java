/*
 * prefs: org.nrg.prefs.beans.PreferenceBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.beans;

import org.nrg.framework.constants.Scope;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.PreferenceInfo;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;

import java.util.*;

@SuppressWarnings("unused")
public interface PreferenceBean extends Map<String, Object> {
    String NAMESPACE_DELIMITER = ":";

    String getToolId();

    /**
     * Gets the keys for all the preferences.
     *
     * @return The set of preference keys.
     *
     * @deprecated Use the {@link #keySet()} method instead.
     */
    @Deprecated
    Set<String> getPreferenceKeys();

    /**
     * Gets the preferences for the current implementation as a map.
     *
     * @return The preferences for the current implementation as a map.
     *
     * @deprecated Preference beans are now themselves maps. This method just returns the preference bean itself.
     */
    @Deprecated
    Map<String, Object> getPreferenceMap();

    /**
     * Gets the preferences for the current implementation as a map, including only the specified keys.
     *
     * @param preferenceNames The preferences to return in the map.
     *
     * @return The preferences for the current implementation as a map.
     *
     * @deprecated Preference beans are now themselves maps. This method just returns the preference bean itself. Use
     * streams or Guava methods to filter the resulting map.
     */
    @Deprecated
    Map<String, Object> getPreferenceMap(final String... preferenceNames);

    /**
     * Gets the preferences for the current implementation as a map, including only the specified keys.
     *
     * @param preferenceNames The preferences to return in the map.
     *
     * @return The preferences for the current implementation as a map.
     *
     * @deprecated Preference beans are now themselves maps. This method just returns the preference bean itself. Use
     * streams or Guava methods to filter the resulting map.
     */
    @Deprecated
    Map<String, Object> getPreferenceMap(final Set<String> preferenceNames);

    Map<String, Object> getPreferences(final Set<String> preferenceNames);

    Properties asProperties();

    Class<? extends PreferenceEntityResolver> getResolver();

    Preference getPreference(final String key, final String... subkeys) throws UnknownToolId;

    Preference getPreference(final Scope scope, final String entityId, final String key, final String... subkeys) throws UnknownToolId;

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
