package org.nrg.prefs.services.impl.hibernate;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.scope.EntityId;
import org.nrg.prefs.annotations.NrgPrefValue;
import org.nrg.prefs.annotations.NrgPrefsTool;
import org.nrg.prefs.beans.NrgPreferences;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownResolverId;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.services.NrgPrefsService;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.prefs.services.ToolService;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.reflections.ReflectionUtils.withName;
import static org.reflections.ReflectionUtils.withParametersAssignableTo;

@Service
public class DefaultNrgPrefsService implements NrgPrefsService, ApplicationContextAware, InitializingBean {

    /**
     * Creates a {@link Tool tool} with the indicated default properties and optional values.
     *
     * @param toolId           The unique tool ID.
     * @param toolName         The readable tool name.
     * @param description      The readable description of the tool.
     * @param defaults         The default properties and values for the tool.
     * @param preferencesClass The preferences class to set for the tool.
     * @param resolverId       The ID of the entity resolver instance to set for the tool.
     * @return The object representing the persisted tool definition.
     */
    // TODO: Here the defaults are in a String, String map. The valueType attribute can indicate another type, but currently we only handle strings. This needs to be handled later with ValueDuple.
    @Override
    public Tool createTool(final String toolId, final String toolName, final String description, final Map<String, String> defaults, final boolean strict, final String preferencesClass, final String resolverId) {
        if (_log.isDebugEnabled()) {
            _log.debug("Request to create a new tool received: {}", toolId);
        }
        final Tool tool = new Tool(toolId, toolName, description, defaults, strict, preferencesClass, resolverId);
        _toolService.create(tool);
        if (_log.isDebugEnabled()) {
            _log.debug("New tool {} created with primary key ID: {} at {}", toolId, tool.getId(), tool.getCreated());
        }
        if (defaults != null) {
            if (_log.isDebugEnabled()) {
                _log.debug("Found {} default values to add to tool {}", defaults.size(), toolId);
            }
            for (final String preference : defaults.keySet()) {
                final String value = defaults.get(preference);
                if (StringUtils.isNotBlank(value)) {
                    try {
                        _preferenceService.setPreference(tool.getToolId(), preference, EntityId.Default.getScope(), EntityId.Default.getEntityId(), value);
                    } catch (InvalidPreferenceName ignored) {
                        // This shouldn't happen: we're creating new preferences from the defaults that define the list of acceptable preferences.
                    }
                    if (_log.isDebugEnabled()) {
                        _log.debug(" * {}: {}", preference, value);
                    }
                } else if (_log.isDebugEnabled()) {
                    _log.debug(" * {}: No default value specified", preference);
                }
            }
        }
        return tool;
    }

    /**
     * Gets the preference for the indicated tool. This retrieves for the {@link Scope#Site site scope}. If you need to
     * specify the preference for a particular entity, use the {@link #getPreference(String, String, Scope, String)}
     * form of this method instead.
     *
     * @param toolId         The tool name.
     * @param preferenceName The preference name.
     * @return The {@link Preference preference} for the specified tool and preference.
     */
    @Override
    public Preference getPreference(final String toolId, final String preferenceName) throws UnknownToolId {
        return getPreference(toolId, preferenceName, Scope.Site, null);
    }

    @Override
    public Preference getPreference(final String toolId, final String preferenceName, final Scope scope, final String entityId) throws UnknownToolId {
        final PreferenceEntityResolver resolver = getResolver(toolId);
        return resolver.resolve(new EntityId(scope, entityId), toolId, preferenceName);
    }

    @Override
    public String getPreferenceValue(final String toolId, final String preferenceName) throws UnknownToolId {
        return getPreferenceValue(toolId, preferenceName, EntityId.Default.getScope(), EntityId.Default.getEntityId());
    }

    @Override
    public String getPreferenceValue(final String toolId, final String preferenceName, final Scope scope, final String entityId) throws UnknownToolId {
        final Preference preference = getPreference(toolId, preferenceName, scope, entityId);
        return preference != null ? preference.getValue() : null;
    }

    @Override
    public void setPreferenceValue(final String toolId, final String preferenceName, final String value) throws UnknownToolId, InvalidPreferenceName {
        setPreferenceValue(toolId, preferenceName, DEFAULT_SCOPE, DEFAULT_ENTITY_ID, value);
    }

    @Override
    public void setPreferenceValue(final String toolId, final String preferenceName, final Scope scope, final String entityId, final String value) throws UnknownToolId, InvalidPreferenceName {
        final Tool tool = getTool(toolId);
        final Preference preference = getPreference(toolId, preferenceName, scope, entityId);
        if (preference != null) {
            preference.setValue(value);
            _preferenceService.update(preference);
        } else {
            _preferenceService.setPreference(tool.getToolId(), preferenceName, scope, entityId, value);
        }
    }

    @Override
    public void deletePreference(final String toolId, final String preference) throws InvalidPreferenceName {
        deletePreference(toolId, preference, DEFAULT_SCOPE, DEFAULT_ENTITY_ID);
    }

    @Override
    public void deletePreference(final String toolId, final String preference, final Scope scope, final String entityId) throws InvalidPreferenceName {
        _preferenceService.delete(toolId, preference);
    }

    @Override
    public Set<String> getToolIds() {
        return _toolService.getToolIds();
    }

    @Override
    public Set<Tool> getTools() {
        return new HashSet<>(_toolService.getAll());
    }

    @Override
    public Set<String> getToolPropertyNames(final String toolId) {
        return _preferenceService.getToolProperties(toolId, DEFAULT_SCOPE, DEFAULT_ENTITY_ID).stringPropertyNames();
    }

    @Override
    public Properties getToolProperties(final String toolId) {
        return _preferenceService.getToolProperties(toolId, DEFAULT_SCOPE, DEFAULT_ENTITY_ID);
    }

    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        _context = context;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Initialize the cache of resolver beans by ID.
        _resolversById.putAll(_context.getBeansOfType(PreferenceEntityResolver.class));

        // Now get all of the prefs tools.
        final Map<String, Object> tools = _context.getBeansWithAnnotation(NrgPrefsTool.class);

        // Go through each of them.
        for (final Object object : tools.values()) {
            // First get the preferences annotation.
            Class<?> objectClass = object.getClass();
            final NrgPrefsTool annotation = objectClass.getAnnotation(NrgPrefsTool.class);
            if (_log.isDebugEnabled()) {
                _log.debug("Found NrgPrefsTools annotation for tool ID {} on an instance of the class {}", annotation.toolId(), objectClass.getName());
            }

            // Verify the tool for the annotation. This will create the tool if it doesn't already exist.
            final Tool tool = getTool(annotation);
            if (_log.isDebugEnabled()) {
                _log.debug("Successfully loaded the tool with ID {} on an instance of the class {}", tool.getToolId(), objectClass.getName());
            }

            // Now set the preferences for the object using the property specified on the annotation.
            setPreferences(object, annotation);

            // Oh happy day! We've done all of this stuff so let's stash our entity resolver!
            final PreferenceEntityResolver resolver = getResolver(annotation.toolId());
            _resolversByToolId.put(annotation.toolId(), resolver);
            _resolversByClass.put(resolver.getClass().getName(), resolver);
        }
    }

    /**
     * Returns the resolver specified for the given tool ID. If the entity resolver is not already cached or found in
     * the current application context, this method returns null.
     *
     * @param toolId    The tool ID for retrieving a registered entity resolver.
     *
     * @return The entity resolver with the submitted ID if found, null otherwise.
     */
    private PreferenceEntityResolver getResolver(final String toolId) throws UnknownToolId {
        // If it's already cached by tool ID, then return that one. Easy!
        if (_resolversByToolId.containsKey(toolId)) {
            return _resolversByToolId.get(toolId);
        }

        // If it's not cached by tool ID, then let's get the tool and find the preferred resolver ID from that.
        // TODO: If the context ID could be set to the tool ID implicitly, we might be able to get all of this from the application context.
        final Tool tool = _toolService.getByToolId(toolId);
        if (tool == null) {
            // What?!
            throw new UnknownToolId(toolId);
        }

        // Get the resolver ID from the tool.
        final String resolverId = tool.getResolverId();

        if (StringUtils.isBlank(resolverId)) {
            if (_resolversById.containsKey("defaultResolver")) {
                return _resolversById.get("defaultResolver");
            }
            if (_resolversById.size() == 1) {
                return _resolversById.values().iterator().next();
            }
            throw new UnknownResolverId();
        }

        // See if it exists in our cache by resolver ID. This is initialized from the context, so we don't need to check there.
        if (_resolversById.containsKey(resolverId)) {
            final PreferenceEntityResolver resolver = _resolversById.get(resolverId);
            if (!_resolversByClass.containsKey(resolver.getClass().getName())) {
                _resolversByClass.put(resolver.getClass().getName(), resolver);
            }
            _resolversByToolId.put(toolId, resolver);
            return resolver;
        }

        throw new UnknownResolverId(resolverId);
    }

    /**
     * Creates a preferences object based on the annotation, then sets it using the method specified by the annotation's
     * {@link NrgPrefsTool#property()} attribute.
     *
     * @param object     The object on which to set the preferences.
     * @param annotation The annotation with the attributes set for creating and setting the preferences.
     */
    private void setPreferences(final Object object, final NrgPrefsTool annotation) {
        // Get the preferences object specified by the annotation.
        final NrgPreferences preferences = getPreferences(annotation);
        if (_log.isDebugEnabled()) {
            _log.debug("Loaded a preferences instance of type {} from the annotation for tool ID {} on the object {}", preferences.getClass().getName(), annotation.toolId(), object.getClass().getName());
        }

        final String name = "set" + StringUtils.capitalize(annotation.property());
        //noinspection unchecked
        final Set<Method> methods = ReflectionUtils.getMethods(object.getClass(), withName(name), withParametersAssignableTo(preferences.getClass()));
        if (methods.size() != 1) {
            throw new NrgServiceRuntimeException(NrgServiceError.Instantiation, "Couldn't find one and only one method named " + name + "() that takes a parameter assignable to " + preferences.getClass().getName());
        }
        final Method method = methods.iterator().next();
        try {
            method.invoke(object, preferences);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Instantiation, "An error occurred trying to call the method " + object.getClass().getName() + "." + name + "() on the tool class " + annotation.toolId(), e);
        }

        if (_log.isDebugEnabled()) {
            _log.debug("Set a preferences instance of type {} using the method {} on an object of type {}", preferences.getClass().getName(), method.getName(), object.getClass().getName());
        }
    }

    /**
     * Gets the {@link Tool tool instance} indicated by the submitted {@link NrgPrefsTool annotation instance}. Note
     * that, unlike the {@link #getTool(String)} version of this method, this method creates the tool if it does not yet
     * exist in the preferences service.
     *
     * @param annotation The {@link NrgPrefsTool} annotation.
     * @return The initialized tool for the indicated object.
     */
    private Tool getTool(final NrgPrefsTool annotation) {
        final String toolId = annotation.toolId();
        if (!getToolIds().contains(toolId)) {
            return createTool(toolId, annotation.toolName(), annotation.description(), getToolDefaults(annotation.preferences()), annotation.strict(), annotation.preferencesClass().getName(), annotation.resolverId());
        }
        try {
            return getTool(toolId);
        } catch (UnknownToolId e) {
            throw new NrgServiceRuntimeException("The tool with the ID " + toolId + " could not be retrieved, although the tool ID seems to exist in the system. Maybe there was an error?", e);
        }
    }

    /**
     * Gets the {@link Tool tool instance} indicated by the submitted {@link NrgPrefsTool annotation instance}. Note
     * that, unlike the {@link #getTool(NrgPrefsTool)} version of this method, this method does not create the tool if
     * it does not yet exist in the preferences service. Instead it throws an {@link NrgServiceException}.
     *
     * @param toolId The unique tool ID.
     * @return The initialized tool for the indicated object.
     */
    private Tool getTool(final String toolId) throws UnknownToolId {
        final Tool tool = _toolService.getByToolId(toolId);
        if (tool == null) {
            throw new UnknownToolId(toolId);
        }
        if (_log.isDebugEnabled()) {
            _log.debug("Found the tool identified by tool ID: {}", toolId);
        }
        return tool;
    }

    /**
     * Converts an array of {@link NrgPrefValue} annotations into a map of string and value duples. This returns the set
     * of default preferences and values to be initialized.
     *
     * @param defaults The {@link NrgPrefValue annotations} to be processed.
     * @return A map of value duples organized by preference name.
     */
    // TODO: Here the defaults are in a String, String map. The valueType attribute can indicate another type, but currently we only handle strings. This needs to be handled later with ValueDuple.
    private Map<String, String> getToolDefaults(final NrgPrefValue[] defaults) {
        if (defaults == null || defaults.length == 0) {
            return null;
        }
        final Map<String, String> converted = new HashMap<>();
        for (final NrgPrefValue value : defaults) {
            converted.put(value.name(), value.defaultValue());
        }
        return converted;
    }

    /**
     * Gets the appropriate preferences bean for the submitted {@link NrgPrefsTool annotation instance}.
     *
     * @param annotation The {@link}
     * @return The initialized preferences bean for the indicated object.
     */
    private NrgPreferences getPreferences(final NrgPrefsTool annotation) {
        final String toolId = annotation.toolId();
        try {
            return annotation.preferencesClass().getConstructor(NrgPrefsService.class, String.class).newInstance(this, toolId);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "An error occurred trying to create the preferences bean for the annotation for tool: " + toolId, e);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(DefaultNrgPrefsService.class);
    private static final Scope DEFAULT_SCOPE = EntityId.Default.getScope();
    private static final String DEFAULT_ENTITY_ID = EntityId.Default.getEntityId();

    @Inject
    private ToolService _toolService;
    @Inject
    private PreferenceService _preferenceService;
    // @Autowired(required = false)
    // private List<PreferenceEntityResolver> _resolvers;

    private final Map<String, PreferenceEntityResolver> _resolversById = new HashMap<>();
    private final Map<String, PreferenceEntityResolver> _resolversByToolId = new HashMap<>();
    private final Map<String, PreferenceEntityResolver> _resolversByClass = new HashMap<>();

    private ApplicationContext _context;
}
