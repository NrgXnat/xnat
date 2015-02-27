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
import org.nrg.framework.scope.EntityId;
import org.nrg.framework.scope.EntityResolver;
import org.nrg.framework.services.NrgService;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;

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
    public abstract Tool createTool(final String toolId, final String toolName, final String description);
    /**
     * Creates a {@link Tool tool} with the indicated default properties and optional values.
     * @param toolId         The unique tool ID.
     * @param toolName       The readable tool name.
     * @param description    The readable description of the tool.
     * @param defaults       The default properties and values for the tool.
     * @return The object representing the persisted tool definition.
     */
    public abstract Tool createTool(final String toolId, final String toolName, final String description, final Map<String, String> defaults);
    /**
     * Creates a {@link Tool tool} with the indicated default properties and optional values.
     * @param toolId         The unique tool ID.
     * @param toolName       The readable tool name.
     * @param description    The readable description of the tool.
     * @param defaults       The default properties and values for the tool.
     * @return The object representing the persisted tool definition.
     */
    public abstract Tool createTool(final String toolId, final String toolName, final String description, final Properties defaults);

    public abstract Preference getPreference(final String toolId, final String preferenceName, final Scope scope, final String entityId);

    public abstract String getPreferenceValue(final String toolId, final String preferenceName);
    public abstract String getPreferenceValue(final String toolId, final String preferenceName, final Scope scope, final String entityId);
    public abstract void setPreferenceValue(final String toolId, final String preferenceName, final String value);
    public abstract void setPreferenceValue(final String toolId, final String preferenceName, final Scope scope, final String entityId, final String value);

    /**
     * Gets a set of all of the tool names with preferences stored in the service.
     * @return A set of all available tool names.
     */
    public abstract Set<String> getToolIds();

    /**
     * Gets a set of all of the tools with preferences stored in the service.
     * @return A set of all available tools.
     */
    public abstract Set<Tool> getTools();

    /**
     * Gets a list of all of the property names associated with the indicated {@link Tool tool}.
     * @param toolId    The ID of the tool.
     * @return A list of all of the property names for the indicated tool.
     */
    public abstract Set<String> getToolPropertyNames(final String toolId);

    /**
     * Gets all of the properties associated with the indicated {@link Tool tool} in the form of a standard Java
     * properties object.
     * @param toolId    The ID of the tool.
     * @return All of the properties for the indicated tool.
     */
    public abstract Properties getToolProperties(final String toolId);

    /**
     * Gets the value for a particular property on the specified tool.
     * @param toolId      The ID of the tool.
     * @param property    The property to retrieve.
     * @return The value of the property for the tool.
     */
    public abstract String getPropertyValue(final String toolId, final String property);

    /**
     * Gets the value for a particular property on the specified tool as associated with a particular entity. How the
     * entity is resolved is dependent on the entity resolver specified for the tool.
     * @param toolId      The ID of the tool.
     * @param property    The property to retrieve.
     * @param entityId    The ID of the entity for which the property value should be retrieved.
     * @return The value of the property for the tool.
     */
    public abstract String getPropertyValue(final String toolId, final String property, final EntityId entityId);

    /**
     * Gets the value for a particular property on the specified tool as associated with a particular entity. How the
     * entity is resolved is dependent on the entity resolver specified for the tool.
     * @param toolId      The ID of the tool.
     * @param property    The property to retrieve.
     * @param scope       The scope of the specified entity ID.
     * @param entityId    The ID of the entity for which the property value should be retrieved.
     * @return The value of the property for the tool.
     */
    public abstract String getPropertyValue(final String toolId, final String property, final Scope scope, final String entityId);

    /**
     * Sets the entity resolver implementation for the specified tool ID.
     * @param toolId    The ID of the tool.
     * @param resolver  The entity resolver that should be used to resolve entity IDs for a particular tool.
     */
    public abstract void setEntityResolver(final String toolId, final EntityResolver resolver);
}
