package org.nrg.prefs.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.scope.EntityResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a tool or feature for the purposes of grouping {@link Preference preferences} into functional areas. The
 * tool itself does little to manage the preferences, but provides the ability to associate {@link Preference
 * preferences} into groups and, through the {@link EntityResolver associated entity resolver implementation}, figure
 * out how to resolve ambiguous object scopes.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class Tool extends AbstractHibernateEntity {
    /**
     * Default constructor creates an empty tool instance.
     */
    public Tool() {
        _log.debug("Creating default tool instance, no parameters passed to constructor.");
    }

    /**
     * Creates a tool instance with the specified ID, name, description, and default preference names and values.
     *
     * @param toolId           The ID of the tool instance.
     * @param toolName         The name of the tool instance.
     * @param toolDescription  The description of the tool instance.
     * @param strict           Whether the available preferences for this tool are limited to the specified list.
     * @param preferencesClass The preferences class for this tool.
     */
    public Tool(final String toolId, final String toolName, final String toolDescription, final Map<String, String> toolPreferences, final boolean strict, final String preferencesClass, final String resolverId) {
        if (_log.isDebugEnabled()) {
            _log.debug("Creating tool instance for ID [{}] {}: {}", toolId, toolName, toolDescription);
        }
        setToolId(toolId);
        setToolName(toolName);
        setToolDescription(toolDescription);
        setToolPreferences(toolPreferences);
        setStrict(strict);
        setPreferencesClass(preferencesClass);
        setResolverId(resolverId);
    }

    /**
     * Returns the ID of the tool instance.
     * @return The ID of the tool instance.
     */
    @Column(nullable = false, unique = true)
    public String getToolId() {
        return _toolId;
    }

    /**
     * Sets the ID of the tool instance.
     * @param toolId    The ID to set for the tool instance.
     */
    public void setToolId(final String toolId) {
        _toolId = toolId;
    }

    /**
     * Returns the name of the tool instance. The name is meant to be a readable label for the tool instance. Future
     * revisions of this API may use property keys instead to allow for localization.
     * @return The name of the tool instance.
     */
    @Column(nullable = false, unique = true)
    public String getToolName() {
        return _toolName;
    }

    /**
     * Sets the name of the tool instance. The name is meant to be a readable label for the tool instance. Future
     * revisions of this API may use property keys instead to allow for localization.
     * @param toolName    The name to set for the tool instance.
     */
    public void setToolName(final String toolName) {
        _toolName = toolName;
    }

    /**
     * Gets the description of the tool instance. The description is meant to be a readable summary of the purpose or
     * use of the tool instance. Future revisions of this API may use property keys instead to allow for localization.
     * @return The description of the tool instance.
     */
    public String getToolDescription() {
        return _toolDescription;
    }

    /**
     * Sets the description of the tool instance. The description is meant to be a readable summary of the purpose or
     * use of the tool instance. Future revisions of this API may use property keys instead to allow for localization.
     * @param toolDescription    The description of the tool instance.
     */
    public void setToolDescription(final String toolDescription) {
        _toolDescription = toolDescription;
    }

    /**
     * Indicates whether the set of preferences for the tool is strictly defined by the {@link #getToolPreferences()}
     * list or if ad-hoc preferences can be added. The default is false.
     *
     * @return Whether the preferences are limited to those defined by the {@link #getToolPreferences()} list.
     */
    public boolean isStrict() {
        return _strict;
    }

    /**
     * Sets whether the set of preferences for the tool is strictly defined by the {@link #getToolPreferences()} list or
     * if ad-hoc preferences can be added. The default is false.
     *
     * @param strict    Whether the preferences are limited to those defined by the {@link #getToolPreferences()} list.
     */
    public void setStrict(final boolean strict) {
        _strict = strict;
    }

    /**
     * Returns the preferences class for this tool.
     *
     * @return The preferences class for this tool.
     */
    public String getPreferencesClass() {
        return _preferencesClass;
    }

    /**
     * Sets the preferences class for this tool.
     * @param preferencesClass    The preferences class for this tool.
     */
    public void setPreferencesClass(final String preferencesClass) {
        _preferencesClass = preferencesClass;
    }

    /**
     * Returns the ID of the preferred entity resolver for this tool.
     *
     * @return The ID of the preferred entity resolver for this tool.
     */
    public String getResolverId() {
        return _resolverId;
    }

    /**
     * Sets the ID of the preferred entity resolver for this tool.
     *
     * @param resolverId    The ID of the preferred entity resolver for this tool.
     */
    public void setResolverId(final String resolverId) {
        _resolverId = resolverId;
    }

    /**
     * Gets the available preferences for the tool, along with the default values. This returns a JSON-serialized form
     * of a standard map of key-value pairs. The JSON is stored internally as a map that can be accessed via the
     * {@link #getToolPreferences()} and {@link #setToolPreferences(Map)} methods. This property is strictly for storing
     * the preferences and default values with the tool definition in the database and shouldn't be used for most normal
     * purposes.
     *
     * @return The list of preferences for the tool.
     */
    @JsonIgnore
    @Column(name = "tool_preferences", columnDefinition = "TEXT")
    public String getSerializedToolPreferences() {
        return _serializedToolPreferences;
    }

    /**
     * Sets the available preferences for the tool, along with the default values. This accepts a JSON-serialized form
     * of a standard map of key-value pairs. The JSON is converted internally into a map that can be accessed via the
     * {@link #getToolPreferences()} and {@link #setToolPreferences(Map)} methods. This property is strictly for storing
     * the preferences and default values with the tool definition in the database and shouldn't be used for most normal
     * purposes.
     *
     * @param toolPreferences    The serialized map of tool preferences.
     */
    @JsonIgnore
    public void setSerializedToolPreferences(final String toolPreferences) {
        _serializedToolPreferences = toolPreferences;
        _toolPreferences.clear();
        if (StringUtils.isNotBlank(toolPreferences)) {
            try {
                // Oddly, you HAVE to create an object instance here. You'll get an error if you do this:
                // _toolPreferences.putAll(_mapper.readValue(toolPreferences, MAP_TYPE_REFERENCE));
                final Map<String, String> deserialized = _mapper.readValue(toolPreferences, MAP_TYPE_REFERENCE);
                _toolPreferences.putAll(deserialized);
            } catch (IOException e) {
                _log.error("An error occurred trying to deserialize the preferences for the tool: " + getToolId(), e);
            }
        }
    }

    @JsonProperty("toolPreferences")
    @Transient
    public Map<String, String> getToolPreferences() {
        return new HashMap<>(_toolPreferences);
    }

    @Transient
    public void setToolPreferences(final Map<String, String> defaults) {
        _toolPreferences.clear();
        if (defaults == null || defaults.size() == 0) {
            _serializedToolPreferences = null;
        } else {
            _toolPreferences.putAll(defaults);
            try {
                _serializedToolPreferences = _mapper.writeValueAsString(defaults);
            } catch (JsonProcessingException e) {
                _log.error("An error occurred trying to serialize the preferences for the tool: " + getToolId(), e);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Tool {id='%s', name='%s'}", _toolId, _toolName);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tool)) {
            return false;
        }

        final Tool tool = (Tool) o;

        return StringUtils.equals(_toolId, tool._toolId) &&
                StringUtils.equals(_toolName, tool._toolName) &&
                StringUtils.equals(_toolDescription, tool._toolDescription);
    }

    @Override
    public int hashCode() {
        int result = _toolId.hashCode();
        result = 31 * result + _toolName.hashCode();
        result = 31 * result + _toolDescription.hashCode();
        return result;
    }
    private static final Logger _log = LoggerFactory.getLogger(Tool.class);
    private static final ObjectMapper _mapper = new ObjectMapper();

    private static final TypeReference<HashMap<String, String>> MAP_TYPE_REFERENCE = new TypeReference<HashMap<String, String>>() {};
    private String _toolId;
    private String _toolName;
    private String _toolDescription;
    private final Map<String, String> _toolPreferences = new HashMap<>();
    private boolean _strict;
    private String _preferencesClass;
    private String _resolverId;
    private String _serializedToolPreferences;
}
