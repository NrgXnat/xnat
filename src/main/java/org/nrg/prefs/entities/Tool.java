package org.nrg.prefs.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.scope.EntityResolver;
import org.nrg.prefs.services.NrgPrefsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a tool or feature for the purposes of grouping {@link Preference preferences} into functional areas. The
 * tool itself does little to manage the preferences, but provides the ability to associate {@link Preference
 * preferences} into groups and, through the {@link NrgPrefsService#setEntityResolver(String, EntityResolver)}
 * associated entity resolver implementation}, figure out how to resolve ambiguous object scopes.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"toolId", "toolName"}))
public class Tool extends AbstractHibernateEntity {
    /**
     * Default constructor creates an empty tool instance.
     */
    public Tool() {
        _log.debug("Creating default tool instance, no parameters passed to constructor.");
    }

    /**
     * Creates a tool instance with the specified ID, name, and description. There are no {@link Preference preferences}
     * set for the resulting tool instance.
     * @param toolId             The ID of the tool instance.
     * @param toolName           The name of the tool instance.
     * @param toolDescription    The description of the tool instance.
     */
    public Tool(final String toolId, final String toolName, final String toolDescription) {
        if (_log.isDebugEnabled()) {
            _log.debug("Creating tool instance for ID [{}] {}: {}", toolId, toolName, toolDescription);
        }
        _id = toolId;
        _name = toolName;
        _description = toolDescription;
    }

    /**
     * Creates a tool instance with the specified ID, name, description, and default preference names and values.
     * @param toolId             The ID of the tool instance.
     * @param toolName           The name of the tool instance.
     * @param toolDescription    The description of the tool instance.
     * @param preferences        The set of preference names and default values.
     */
    public Tool(final String toolId, final String toolName, final String toolDescription, final Map<String, String> preferences) {
        if (_log.isDebugEnabled()) {
            _log.debug("Creating tool instance for ID [{}] {}: {}", toolId, toolName, toolDescription);
        }
        _id = toolId;
        _name = toolName;
        _description = toolDescription;
        _preferences = preferences;
    }

    /**
     * Returns the ID of the tool instance.
     * @return The ID of the tool instance.
     */
    @Column(nullable = false)
    public String getToolId() {
        return _id;
    }

    /**
     * Sets the ID of the tool instance.
     * @param toolId    The ID to set for the tool instance.
     */
    public void setToolId(final String toolId) {
        _id = toolId;
    }

    /**
     * Returns the name of the tool instance. The name is meant to be a readable label for the tool instance. Future
     * revisions of this API may use property keys instead to allow for localization.
     * @return The name of the tool instance.
     */
    @Column(nullable = false)
    public String getToolName() {
        return _name;
    }

    /**
     * Sets the name of the tool instance. The name is meant to be a readable label for the tool instance. Future
     * revisions of this API may use property keys instead to allow for localization.
     * @param toolName    The name to set for the tool instance.
     */
    public void setToolName(final String toolName) {
        _name = toolName;
    }

    /**
     * Gets the description of the tool instance. The description is meant to be a readable summary of the purpose or
     * use of the tool instance. Future revisions of this API may use property keys instead to allow for localization.
     * @return The description of the tool instance.
     */
    public String getToolDescription() {
        return _description;
    }

    /**
     * Sets the description of the tool instance. The description is meant to be a readable summary of the purpose or
     * use of the tool instance. Future revisions of this API may use property keys instead to allow for localization.
     * @param toolDescription    The description of the tool instance.
     */
    public void setToolDescription(final String toolDescription) {
        _description = toolDescription;
    }

    /**
     * Gets the available preferences for the tool, along with the default values.
     * @return The list of preferences for the tool.
     */
    @ElementCollection
    @Column(length = 65535)
    public Map<String, String> getToolPreferences() {
        return _preferences != null ? _preferences : new HashMap<String, String>();
    }

    /**
     * Sets the available preferences for the tool, along with the default values.
     * @param preferences    The list of preferences for the tool.
     */
    public void setToolPreferences(final Map<String, String> preferences) {
        _preferences = preferences;
    }

    @Override
    public String toString() {
        return "Tool {" +
                "name='" + _name + '\'' +
                ", id='" + _id + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Tool)) return false;

        final Tool tool = (Tool) o;

        return _id.equals(tool._id) && _name.equals(tool._name);

    }

    @Override
    public int hashCode() {
        int result = _id.hashCode();
        result = 31 * result + _name.hashCode();
        return result;
    }

    private static final Logger _log = LoggerFactory.getLogger(Tool.class);

    private String _id;
    private String _name;
    private String _description;
    private Map<String, String> _preferences = new HashMap<>();
}
