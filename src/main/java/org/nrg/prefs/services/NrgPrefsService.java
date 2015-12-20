/*
 * ddict.services.DataDictionaryService
 *
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * XNAT is an open-source project of the Neuroinformatics Research Group.
 * Released under the Simplified BSD.
 *
 * Last modified 5/1/14 11:06 AM
 */

package org.nrg.prefs.services;

import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.scope.EntityResolver;
import org.nrg.framework.services.NrgService;
import org.nrg.prefs.annotations.NrgPrefsTool;
import org.nrg.prefs.beans.NrgPreferences;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * The preferences service interface is the primary means of working with preferences
 * within the XNAT service context.
 */
public interface NrgPrefsService extends NrgService {
    /**
     * Creates a {@link Tool tool} with no default properties or values set.
     * @param toolId         The unique tool ID.
     * @param toolName       The readable tool name.
     * @param description    The readable description of the tool.
     * @return The object representing the persisted tool definition.
     */
    Tool createTool(final String toolId, final String toolName, final String description);
    /**
     * Creates a {@link Tool tool} with the indicated default properties and optional values.
     * @param toolId         The unique tool ID.
     * @param toolName       The readable tool name.
     * @param description    The readable description of the tool.
     * @param defaults       The default properties and values for the tool.
     * @return The object representing the persisted tool definition.
     */
    Tool createTool(final String toolId, final String toolName, final String description, final Map<String, String> defaults);
    /**
     * Creates a {@link Tool tool} with the indicated default properties and optional values.
     * @param toolId         The unique tool ID.
     * @param toolName       The readable tool name.
     * @param description    The readable description of the tool.
     * @param defaults       The default properties and values for the tool.
     * @return The object representing the persisted tool definition.
     */
    Tool createTool(final String toolId, final String toolName, final String description, final Properties defaults);

    /**
     * Gets the preference for the indicated tool. This retrieves for the {@link Scope#Site site scope}. If you need to
     * specify the preference for a particular entity, use the {@link #getPreference(String, String, Scope, String)}
     * form of this method instead.
     * @param toolId            The tool name.
     * @param preferenceName    The preference name.
     * @return The {@link Preference preference} for the corres
     */
    Preference getPreference(final String toolId, final String preferenceName);

    /**
     * Gets the preference for the indicated tool and entity.
     * @param toolId            The tool ID.
     * @param preferenceName    The preference name.
     * @return The {@link Preference preference} for the corresponding tool and entity.
     */
    Preference getPreference(final String toolId, final String preferenceName, final Scope scope, final String entityId);

    String getPreferenceValue(final String toolId, final String preferenceName);
    String getPreferenceValue(final String toolId, final String preferenceName, final Scope scope, final String entityId);
    void setPreferenceValue(final String toolId, final String preferenceName, final String value);
    void setPreferenceValue(final String toolId, final String preferenceName, final Scope scope, final String entityId, final String value);

    /**
     * Gets a set of all of the tool names with preferences stored in the service.
     * @return A set of all available tool names.
     */
    Set<String> getToolIds();

    /**
     * Gets a set of all of the tools with preferences stored in the service.
     * @return A set of all available tools.
     */
    Set<Tool> getTools();

    /**
     * Gets a list of all of the property names associated with the indicated {@link Tool tool}.
     * @param toolId    The ID of the tool.
     * @return A list of all of the property names for the indicated tool.
     */
    Set<String> getToolPropertyNames(final String toolId);

    /**
     * Gets all of the properties associated with the indicated {@link Tool tool} in the form of a standard Java
     * properties object.
     * @param toolId    The ID of the tool.
     * @return All of the properties for the indicated tool.
     */
    Properties getToolProperties(final String toolId);

    /**
     * Sets the entity resolver implementation for the specified tool ID.
     * @param toolId    The ID of the tool.
     * @param resolver  The entity resolver that should be used to resolve entity IDs for a particular tool.
     */
    void setEntityResolver(final String toolId, final EntityResolver resolver);

    /**
     * Gets the appropriate preferences bean for the indicated object type. The object must be annotated using the
     * {@link NrgPrefsTool} annotation. This also performs checks to create the {@link Tool} entry for the object type.
     * @param object    The class for which you want to retrieve the preferences bean.
     * @return The initialized preferences bean for the indicated object.
     */
    NrgPreferences getPreferenceBean(final Object object) throws NrgServiceRuntimeException;
}
