package org.nrg.prefs.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.scope.EntityResolver;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferencesBean;
import org.nrg.prefs.beans.PreferencesBean;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.reflections.ReflectionUtils.withAnnotation;

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

    public Tool(final PreferencesBean bean) {
        this(bean.getClass());
    }

    public Tool(final Class<? extends PreferencesBean> beanClass) {
        final NrgPreferencesBean annotation = beanClass.getAnnotation(NrgPreferencesBean.class);
        if (annotation == null) {
            // TODO: We might be able to use bean properties to extrapolate some of the info in the annotation and allow configuration that way as well.
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The preferences bean class " + beanClass.getName() + " must be annotated with the NrgPreferencesBean annotation.");
        }
        setToolId(annotation.toolId());
        setToolName(annotation.toolName());
        setToolDescription(annotation.description());
        setToolPreferences(getToolPreferencesFromBean(beanClass));

        // TODO: This is an array because you can't set null for annotation default values, but you should never set multiple resolvers.
        final Class<? extends PreferenceEntityResolver>[] resolvers = annotation.resolver();
        if (resolvers.length > 1) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You should only set zero or one resolver for the NrgPreferencesBean annotation on the " + beanClass.getName() + ".");
        } else if (resolvers.length == 1) {
            setResolver(resolvers[0]);
        }
    }

    /**
     * Creates a tool instance with the specified ID, name, description, and default preference names and values.
     *
     * @param toolId           The ID of the tool instance.
     * @param toolName         The name of the tool instance.
     * @param toolDescription  The description of the tool instance.
     * @param toolPreferences  The default preference names and values for this tool.
     * @param strict           Whether the available preferences for this tool are limited to the specified list.
     * @param resolver         The class of the entity resolver to use for this tool.
     */
    public Tool(final String toolId, final String toolName, final String toolDescription, final Map<String, PreferenceInfo> toolPreferences, final boolean strict, final Class<? extends PreferenceEntityResolver> resolver) {
        if (_log.isDebugEnabled()) {
            _log.debug("Creating tool instance for ID [{}] {}: {}", toolId, toolName, toolDescription);
        }
        setToolId(toolId);
        setToolName(toolName);
        setToolDescription(toolDescription);
        setToolPreferences(toolPreferences);
        setStrict(strict);
        setResolver(resolver);
    }

    /**
     * Returns the ID of the tool instance.
     *
     * @return The ID of the tool instance.
     */
    @Column(nullable = false, unique = true)
    public String getToolId() {
        return _toolId;
    }

    /**
     * Sets the ID of the tool instance.
     *
     * @param toolId The ID to set for the tool instance.
     */
    public void setToolId(final String toolId) {
        if (StringUtils.isBlank(toolId)) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You can't set a blank tool ID.");
        }
        _toolId = toolId;
    }

    /**
     * Returns the name of the tool instance. The name is meant to be a readable label for the tool instance. Future
     * revisions of this API may use property keys instead to allow for localization.
     *
     * @return The name of the tool instance.
     */
    @Column(nullable = false, unique = true)
    public String getToolName() {
        return _toolName;
    }

    /**
     * Sets the name of the tool instance. The name is meant to be a readable label for the tool instance. Future
     * revisions of this API may use property keys instead to allow for localization.
     *
     * @param toolName The name to set for the tool instance.
     */
    public void setToolName(final String toolName) {
        if (StringUtils.isBlank(toolName)) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You can't set a blank tool name.");
        }
        _toolName = toolName;
    }

    /**
     * Gets the description of the tool instance. The description is meant to be a readable summary of the purpose or
     * use of the tool instance. Future revisions of this API may use property keys instead to allow for localization.
     *
     * @return The description of the tool instance.
     */
    public String getToolDescription() {
        return _toolDescription;
    }

    /**
     * Sets the description of the tool instance. The description is meant to be a readable summary of the purpose or
     * use of the tool instance. Future revisions of this API may use property keys instead to allow for localization.
     *
     * @param toolDescription The description of the tool instance.
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
     * @param strict Whether the preferences are limited to those defined by the {@link #getToolPreferences()} list.
     */
    public void setStrict(final boolean strict) {
        _strict = strict;
    }

    /**
     * Returns the class of the preferred entity resolver for this tool. If this returns null, the default entity
     * resolver for the system should be used.
     *
     * @return The class of the preferred entity resolver for this tool.
     */
    public Class<? extends PreferenceEntityResolver> getResolver() {
        return _resolver;
    }

    /**
     * Sets the class of the preferred entity resolver for this tool. If this is set to null, the default entity
     * resolver for the system should be used.
     *
     * @param resolver    The class of the preferred entity resolver for this tool.
     */
    public void setResolver(final Class<? extends PreferenceEntityResolver> resolver) {
        _resolver = resolver;
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
     * @param toolPreferences The serialized map of tool preferences.
     */
    @JsonIgnore
    public void setSerializedToolPreferences(final String toolPreferences) {
        _serializedToolPreferences = toolPreferences;
        _toolPreferences.clear();
        if (StringUtils.isNotBlank(toolPreferences)) {
            try {
                // Oddly, you HAVE to create an object instance here. You'll get an error if you do this:
                // _toolPreferences.putAll(_mapper.readValue(toolPreferences, MAP_TYPE_REFERENCE));
                final Map<String, PreferenceInfo> deserialized = _mapper.readValue(toolPreferences, MAP_TYPE_REFERENCE);
                _toolPreferences.putAll(deserialized);
            } catch (IOException e) {
                _log.error("An error occurred trying to deserialize the preferences for the tool: " + getToolId(), e);
            }
        }
    }

    @JsonProperty("toolPreferences")
    @Transient
    public Map<String, PreferenceInfo> getToolPreferences() {
        return new HashMap<>(_toolPreferences);
    }

    @Transient
    public void setToolPreferences(final Map<String, PreferenceInfo> defaults) {
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

    private Map<String, PreferenceInfo> getToolPreferencesFromBean(final Class<? extends PreferencesBean> beanClass) {
        final Map<String, PreferenceInfo> preferences = new HashMap<>();
        @SuppressWarnings("unchecked") final Set<Method> properties = ReflectionUtils.getAllMethods(beanClass, withAnnotation(NrgPreference.class));
        for (final Method method : properties) {
            if (isGetter(method)) {
                final String   name         = propertize(method.getName(), "get");
                final String   defaultValue = method.getAnnotation(NrgPreference.class).defaultValue();
                final Class<?> type         = method.getReturnType();
                preferences.put(name, new PreferenceInfo(name, defaultValue, type));
            } else if (isSetter(method)) {
                final String   name         = propertize(method.getName(), "set");
                final String   defaultValue = method.getAnnotation(NrgPreference.class).defaultValue();
                final Class<?> type         = method.getParameterTypes()[0];
                preferences.put(name, new PreferenceInfo(name, defaultValue, type));
            }
        }
        return preferences;
    }

    private String propertize(final String name, final String type) {
        return StringUtils.uncapitalize(name.replace(type, ""));
    }

    private boolean isGetter(final Method method) {
        return Modifier.isPublic(method.getModifiers()) && PATTERN_GETTER.matcher(method.getName()).matches() && method.getParameterTypes().length == 0;
    }

    private boolean isSetter(final Method method) {
        return Modifier.isPublic(method.getModifiers()) && PATTERN_SETTER.matcher(method.getName()).matches() && method.getParameterTypes().length == 1;
    }

    private static final Logger       _log           = LoggerFactory.getLogger(Tool.class);
    private static final ObjectMapper _mapper        = new ObjectMapper();
    private static final Pattern      PATTERN_GETTER = Pattern.compile("^get[A-Z][A-z]+");
    private static final Pattern      PATTERN_SETTER = Pattern.compile("^set[A-Z][A-z]+");

    private static final TypeReference<HashMap<String, PreferenceInfo>> MAP_TYPE_REFERENCE = new TypeReference<HashMap<String, PreferenceInfo>>() {
    };
    private String _toolId;
    private String _toolName;
    private String _toolDescription;
    private final Map<String, PreferenceInfo>         _toolPreferences = new HashMap<>();
    private boolean                                   _strict;
    private Class<? extends PreferenceEntityResolver> _resolver;
    private String                                    _serializedToolPreferences;
}
