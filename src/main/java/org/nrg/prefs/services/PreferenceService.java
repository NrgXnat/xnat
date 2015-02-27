package org.nrg.prefs.services;

import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;

import java.util.Properties;

// TODO: Remove the versions of calls that take the Tool object. It would be best to get these directly via the tool ID if possible.
public interface PreferenceService extends BaseHibernateService<Preference> {
    /**
     * Gets the preference object for the specified tool, name, scope, and entity ID.
     * @param toolId            The tool ID for the preference object.
     * @param preferenceName    The name of the preference object.
     * @param scope             The specified scope for the preference.
     * @param entityId          The specified scope for the preference.
     * @return The resulting preference object if it exists in the default scope.
     */
    public abstract Preference getPreference(String toolId, String preferenceName, final Scope scope, final String entityId);

    /**
     * Gets the preference object for the specified tool, name, scope, and entity ID.
     * @param tool              The tool for the preference object.
     * @param preferenceName    The name of the preference object.
     * @param scope             The specified scope for the preference.
     * @param entityId          The specified scope for the preference.
     * @return The resulting preference object if it exists in the default scope.
     */
    public abstract Preference getPreference(final Tool tool, String preferenceName, final Scope scope, final String entityId);

    /**
     * Sets the value of the preference object for the specified tool, name, scope, and entity ID.
     * @param toolId            The tool ID for the preference object.
     * @param preferenceName    The name of the preference object.
     * @param scope             The specified scope for the preference.
     * @param entityId          The specified scope for the preference.
     * @param value             The value to set for the preference.
     */
    public abstract void setPreference(final String toolId, final String preferenceName, final Scope scope, final String entityId, String value);

    /**
     * Sets the value of the preference object for the specified tool, name, scope, and entity ID.
     * @param tool              The tool for the preference object.
     * @param preferenceName    The name of the preference object.
     * @param scope             The specified scope for the preference.
     * @param entityId          The specified scope for the preference.
     * @param value             The value to set for the preference.
     */
    public abstract void setPreference(final Tool tool, final String preferenceName, final Scope scope, final String entityId, String value);

    /**
     * Returns all of the properties for the selected tool at the indicated scope.
     *
     * @param toolId    The ID of the tool.
     * @param scope     The scope for which properties should be retrieved.
     * @param entityId  The entity for which properties should be retrieved.
     * @return All of the properties for the selected tool at the indicated scope, returned as a Java properties object.
     */
    public abstract Properties getToolProperties(final String toolId, final Scope scope, final String entityId);
}
