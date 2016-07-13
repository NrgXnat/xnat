package org.nrg.prefs.services;

import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.exceptions.InvalidPreferenceName;

import java.util.List;
import java.util.Properties;

// TODO: Remove the versions of calls that take the Tool object. It would be best to get these directly via the tool ID if possible.
public interface PreferenceService extends BaseHibernateService<Preference> {
    /**
     * Checks whether the preference exists for the indicated tool. This checks at the {@link Scope#Site site scope}. If
     * you need to specify the preference for a particular entity, use the {@link #getPreference(String, String, Scope,
     * String)} form of this method instead.
     *
     * @param toolId     The unique tool ID.
     * @param preference The preference name.
     *
     * @return Returns true if the preference exists for the tool, false otherwise.
     */
    boolean hasPreference(final String toolId, final String preference);

    /**
     * Checks whether the preference exists for the indicated tool and entity.
     *
     * @param toolId     The unique tool ID.
     * @param preference The preference name.
     * @param scope      The scope of the object identified by the entityId parameter.
     * @param entityId   The ID of the particular object associated with the preference.
     *
     * @return Returns true if the preference exists for the tool, false otherwise.
     */
    boolean hasPreference(final String toolId, final String preference, final Scope scope, final String entityId);

    /**
     * Gets the preference object for the specified tool and name. This retrieves the preference for the {@link
     * Scope#Site site scope}. If you need to retrieve the preference for a particular entity, use the {@link
     * #getPreference(String, String, Scope, String)} form of this method.
     *
     * @param toolId         The tool ID for the preference object.
     * @param preferenceName The name of the preference object.
     *
     * @return The resulting preference object if it exists in the default scope.
     */
    Preference getPreference(String toolId, String preferenceName);

    /**
     * Gets the preference object for the specified tool, name, scope, and entity ID.
     *
     * @param toolId         The tool ID for the preference object.
     * @param preferenceName The name of the preference object.
     * @param scope          The specified scope for the preference.
     * @param entityId       The specified scope for the preference.
     *
     * @return The resulting preference object if it exists in the default scope.
     */
    Preference getPreference(String toolId, String preferenceName, final Scope scope, final String entityId);

    /**
     * Sets the value of the preference object for the specified tool and name. This sets the preference for the {@link
     * Scope#Site site scope}. If you need to set the preference for a particular entity, use the {@link
     * #setPreference(String, String, Scope, String, String)} form of this method.
     *
     * @param toolId         The tool ID for the preference object.
     * @param preferenceName The name of the preference object.
     * @param value          The value to set for the preference.
     *
     * @throws InvalidPreferenceName When the system can't find a preference with the indicated name associated with the tool with the indicated ID.
     */
    void setPreference(final String toolId, final String preferenceName, final String value) throws InvalidPreferenceName;

    /**
     * Sets the value of the preference object for the specified tool, name, scope, and entity ID.
     *
     * @param toolId         The tool ID for the preference object.
     * @param preferenceName The name of the preference object.
     * @param scope          The specified scope for the preference.
     * @param entityId       The specified scope for the preference.
     * @param value          The value to set for the preference.
     *
     * @throws InvalidPreferenceName When the system can't find a preference with the indicated name associated with the tool with the indicated ID.
     */
    void setPreference(final String toolId, final String preferenceName, final Scope scope, final String entityId, final String value) throws InvalidPreferenceName;

    /**
     * Deletes the preference object for the specified tool and name. This deletes the preference for the {@link
     * Scope#Site site scope}. If you need to delete the preference for a particular entity, use the {@link
     * #delete(String, String, Scope, String)} form of this method.
     *
     * @param toolId         The tool ID for the preference object.
     * @param preferenceName The name of the preference object.
     *
     * @throws InvalidPreferenceName When the system can't find a preference with the indicated name associated with the tool with the indicated ID.
     */
    void delete(final String toolId, final String preferenceName) throws InvalidPreferenceName;

    /**
     * Deletes the preference object for the specified tool, name, scope, and entity ID.
     *
     * @param toolId         The tool ID for the preference object.
     * @param preferenceName The name of the preference object.
     * @param scope          The specified scope for the preference.
     * @param entityId       The specified scope for the preference.
     *
     * @throws InvalidPreferenceName When the system can't find a preference with the indicated name associated with the tool with the indicated ID.
     */
    void delete(final String toolId, final String preferenceName, final Scope scope, final String entityId) throws InvalidPreferenceName;

    /**
     * Returns all of the properties for the selected tool at the indicated scope.
     *
     * @param toolId   The ID of the tool.
     * @param scope    The scope for which properties should be retrieved.
     * @param entityId The entity for which properties should be retrieved.
     *
     * @return All of the properties for the selected tool at the indicated scope, returned as a Java properties object.
     */
    Properties getToolProperties(final String toolId, final Scope scope, final String entityId);

    /**
     * Returns the properties with name specified in the <b>preferenceNames</b> list for the selected tool at the indicated
     * scope.
     *
     * @param toolId          The ID of the tool.
     * @param scope           The scope for which properties should be retrieved.
     * @param entityId        The entity for which properties should be retrieved.
     * @param preferenceNames The names of the preferences to retrieve.
     *
     * @return All of the properties for the selected tool at the indicated scope, returned as a Java properties object.
     */
    Properties getToolProperties(final String toolId, final Scope scope, final String entityId, final List<String> preferenceNames);
}
