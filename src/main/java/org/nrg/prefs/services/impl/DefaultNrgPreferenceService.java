/*
 * prefs: org.nrg.prefs.services.impl.DefaultNrgPreferenceService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.services.impl;

import com.google.common.collect.Maps;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.scope.EntityId;
import org.nrg.prefs.beans.PreferenceBean;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.PreferenceInfo;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.prefs.services.ToolService;
import org.nrg.prefs.transformers.PreferenceTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DefaultNrgPreferenceService implements NrgPreferenceService {
    @Autowired
    public DefaultNrgPreferenceService(final ToolService toolService, final PreferenceService preferenceService, final List<PreferenceTransformer> transformers, final Map<String, PreferenceEntityResolver> resolvers) {
        _toolService = toolService;
        _preferenceService = preferenceService;

        for (final PreferenceTransformer transformer : transformers) {
            _transformersByValueType.put(transformer.getValueType(), transformer);
        }

        if (resolvers == null || resolvers.size() == 0) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You must have at least one preferences entity resolver instance available.");
        }

        final PreferenceEntityResolver defaultResolver = resolvers.get("defaultResolver");
        if (defaultResolver != null) {
            _defaultResolver = defaultResolver;
        } else if (resolvers.size() == 1) {
            _defaultResolver = new ArrayList<>(resolvers.values()).get(0);
        } else {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You must have at least one preferences entity resolver instance marked as the default resolver for the application.");
        }

        for (final PreferenceEntityResolver resolver : resolvers.values()) {
            _resolversByClass.put(resolver.getClass(), resolver);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tool createTool(final PreferenceBean bean) {
        if (_log.isDebugEnabled()) {
            _log.debug("Request to create a new tool received, ID: {}", bean.getClass().getName());
        }
        final Tool tool = new Tool(bean);
        return createTool(tool);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tool createTool(final Tool tool) {
        _toolService.create(tool);
        if (_log.isInfoEnabled()) {
            _log.info("New tool {} created with primary key ID: {} at {}", tool.getToolId(), tool.getId(), tool.getCreated());
        }
        return tool;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(final String toolId, final String namespacedPropertyId, final String value) {
        create(toolId, namespacedPropertyId, EntityId.Default.getScope(), EntityId.Default.getEntityId(), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(final String toolId, final String namespacedPropertyId, final Scope scope, final String entityId, final String value) {
        final Preference preference = new Preference();
        preference.setTool(_toolService.getByToolId(toolId));
        preference.setName(namespacedPropertyId);
        preference.setScope(scope);
        preference.setEntityId(entityId);
        preference.setValue(value);
        _preferenceService.create(preference);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPreference(final String toolId, final String preference) {
        return _preferenceService.hasPreference(toolId, preference);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPreference(final String toolId, final String preference, final Scope scope, final String entityId) {
        return _preferenceService.hasPreference(toolId, preference, scope, entityId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Preference getPreference(final String toolId, final String preferenceName) throws UnknownToolId {
        return getPreference(toolId, preferenceName, EntityId.Default.getScope(), EntityId.Default.getEntityId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Preference getPreference(final String toolId, final String preferenceName, final Scope scope, final String entityId) throws UnknownToolId {
        final PreferenceEntityResolver resolver = getResolver(toolId);
        return resolver.resolve(new EntityId(scope, entityId), toolId, preferenceName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Preference migrate(final String toolId, final String alias, final String preference) throws UnknownToolId {
        return migrate(toolId, alias, preference, EntityId.Default.getScope(), EntityId.Default.getEntityId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Preference migrate(final String toolId, final String alias, final String preferenceName, final Scope scope, final String entityId) throws UnknownToolId {
        final Preference preference = getPreference(toolId, alias, scope, entityId);
        if (preference == null) {
            return null;
        }
        preference.setName(preferenceName);
        _preferenceService.update(preference);
        _log.info("Migrated preference entry in tool {} from alias {} to preference name {}.", toolId, alias, preferenceName);
        return preference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPreferenceValue(final String toolId, final String preferenceName) throws UnknownToolId {
        return getPreferenceValue(toolId, preferenceName, EntityId.Default.getScope(), EntityId.Default.getEntityId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPreferenceValue(final String toolId, final String preferenceName, final Scope scope, final String entityId) throws UnknownToolId {
        final Preference preference = getPreference(toolId, preferenceName, scope, entityId);
        return preference != null ? preference.getValue() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPreferenceValue(final String toolId, final String preferenceName, final String value) throws UnknownToolId, InvalidPreferenceName {
        setPreferenceValue(toolId, preferenceName, DEFAULT_SCOPE, DEFAULT_ENTITY_ID, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPreferenceValue(final String toolId, final String preferenceName, final Scope scope, final String entityId, final String value) throws UnknownToolId, InvalidPreferenceName {
        final Preference preference = _preferenceService.getPreference(toolId, preferenceName, scope, entityId);
        if (preference != null) {
            preference.setValue(value);
            _preferenceService.update(preference);
        } else {
            _preferenceService.setPreference(toolId, preferenceName, scope, entityId, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePreference(final String toolId, final String preference) throws InvalidPreferenceName {
        deletePreference(toolId, preference, DEFAULT_SCOPE, DEFAULT_ENTITY_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePreference(final String toolId, final String preference, final Scope scope, final String entityId) throws InvalidPreferenceName {
        _preferenceService.delete(toolId, preference, scope, entityId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getToolIds() {
        return _toolService.getToolIds();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Tool> getTools() {
        return new HashSet<>(_toolService.getAll());
    }

    /**
     * Gets a set of all of the tools with preferences stored in the service.
     *
     * @param toolId The ID of the tool to retrieve.
     *
     * @return The requested tool if available, null otherwise.
     */
    @Override
    public Tool getTool(final String toolId) {
        return _toolService.getByToolId(toolId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getToolPropertyNames(final String toolId, final Scope scope, final String entityId) {
        return _preferenceService.getToolProperties(toolId, scope, entityId).stringPropertyNames();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getToolPropertyNames(final String toolId) {
        return getToolPropertyNames(toolId, DEFAULT_SCOPE, DEFAULT_ENTITY_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Properties getToolProperties(final String toolId, final Scope scope, final String entityId) {
        return _preferenceService.getToolProperties(toolId, scope, entityId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Properties getToolProperties(final String toolId) {
        return getToolProperties(toolId, DEFAULT_SCOPE, DEFAULT_ENTITY_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Properties getToolProperties(final String toolId, final Scope scope, final String entityId, final List<String> preferenceNames) {
        return _preferenceService.getToolProperties(toolId, scope, entityId, preferenceNames);
    }

    /**
     * Gets the properties with the names specified in the <b>preferences</b> list and associated with the indicated
     * {@link Tool tool} in the form of a standard Java properties object.
     *
     * @param toolId          The unique tool ID.
     * @param preferenceNames The names of the preferences to retrieve.
     *
     * @return All of the properties for the indicated tool.
     */
    @Override
    public Properties getToolProperties(final String toolId, final List<String> preferenceNames) {
        return getToolProperties(toolId, DEFAULT_SCOPE, DEFAULT_ENTITY_ID, preferenceNames);
    }

    @Override
    public void registerResolver(final String toolId, final PreferenceEntityResolver resolver) {
        _resolversByToolId.put(toolId, resolver);
        _resolversByClass.put(resolver.getClass(), resolver);
    }

    @Override
    public PreferenceTransformer getTransformer(final PreferenceInfo info) {
        if (_transformersByPreference.containsKey(info)) {
            return _transformersByPreference.get(info);
        }

        final Class<?> valueType = info.getValueType();
        final Class<?> itemType = info.getItemType();

        final PreferenceTransformer<?> transformer;
        if (_transformersByValueType.containsKey(valueType)) {
            transformer = _transformersByValueType.get(valueType);
        } else if (isContainerType(valueType) && _transformersByValueType.containsKey(itemType)) {
            transformer = _transformersByValueType.get(itemType);
        } else {
            transformer = getTransformerByInfo(info);
        }

        if (transformer != null) {
            _transformersByPreference.put(info, transformer);
        }

        return transformer;
    }

    private PreferenceTransformer getTransformerByInfo(final PreferenceInfo info) {
        for (final PreferenceTransformer transformer : _transformersByValueType.values()) {
            //noinspection unchecked
            if (transformer.handles(info)) {
                return transformer;
            }
        }
        return null;
    }

    private static boolean isContainerType(final Class<?> valueType) {
        return List.class.isAssignableFrom(valueType) || Set.class.isAssignableFrom(valueType) || Map.class.isAssignableFrom(valueType);
    }

    /**
     * Returns the resolver specified for the given tool ID. If the entity resolver is not already cached or found in
     * the current application context, this method returns null.
     *
     * @param toolId The tool ID for retrieving a registered entity resolver.
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
        final Class<? extends PreferenceEntityResolver> resolverClass;
        final Tool tool = _toolService.getByToolId(toolId);
        if (tool != null) {
            // Get the resolver ID from the tool.
            resolverClass = tool.getResolver();
        } else {
            // What?!
            throw new UnknownToolId(toolId);
        }

        return getResolverByClass(toolId, resolverClass);
    }

    private PreferenceEntityResolver getResolverByClass(final String toolId, final Class<? extends PreferenceEntityResolver> resolverClass) {
        if (resolverClass == null) {
            // If there's no class specified, give them the default resolver.
            _resolversByToolId.put(toolId, _defaultResolver);
        } else if (_resolversByClass.containsKey(resolverClass)) {
            // If there is a class specified and we already have it, set that instance for the tool ID.
            _resolversByToolId.put(toolId, _resolversByClass.get(resolverClass));
        } else {
            // Couldn't find it at all? That's not great, but we can try to build it.
            try {
                final PreferenceEntityResolver resolver = resolverClass.newInstance();
                _resolversByToolId.put(toolId, resolver);
                _resolversByClass.put(resolverClass, resolver);
            } catch (InstantiationException | IllegalAccessException e1) {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "There was an error constructing the resolver class " + resolverClass, e1);
            }
        }

        // Give them what we got.
        return _resolversByToolId.get(toolId);
    }

    private static final Scope  DEFAULT_SCOPE     = EntityId.Default.getScope();
    private static final String DEFAULT_ENTITY_ID = EntityId.Default.getEntityId();

    private static final Logger _log = LoggerFactory.getLogger(DefaultNrgPreferenceService.class);

    private final ToolService                 _toolService;
    private final PreferenceService           _preferenceService;

    private final Map<Class<?>, PreferenceTransformer<?>>                                  _transformersByValueType  = Maps.newHashMap();
    private final Map<PreferenceInfo, PreferenceTransformer<?>>                            _transformersByPreference = Maps.newHashMap();
    private final Map<String, PreferenceEntityResolver>                                    _resolversByToolId        = new HashMap<>();
    private final Map<Class<? extends PreferenceEntityResolver>, PreferenceEntityResolver> _resolversByClass         = new HashMap<>();

    private final PreferenceEntityResolver _defaultResolver;
}
