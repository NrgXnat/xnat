package org.nrg.prefs.services.impl.hibernate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.scope.EntityId;
import org.nrg.prefs.beans.PreferenceBean;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.prefs.services.ToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
@Service
public class DefaultNrgPreferenceService implements NrgPreferenceService, ApplicationContextAware, InitializingBean {
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
        return getPreference(toolId, preferenceName, Scope.Site, null);
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
        final Tool tool = getTool(toolId);
        final Preference preference = getPreference(toolId, preferenceName, scope, entityId);
        if (preference != null) {
            preference.setValue(value);
            _preferenceService.update(preference);
        } else {
            _preferenceService.setPreference(tool.getToolId(), preferenceName, scope, entityId, value);
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
     * {@inheritDoc}
     */
    @Override
    public Set<String> getToolPropertyNames(final String toolId) {
        return _preferenceService.getToolProperties(toolId, DEFAULT_SCOPE, DEFAULT_ENTITY_ID).stringPropertyNames();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Properties getToolProperties(final String toolId) {
        return _preferenceService.getToolProperties(toolId, DEFAULT_SCOPE, DEFAULT_ENTITY_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        _context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (_resolvers == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You must have at least one preferences entity resolver instance available.");
        }
        final PreferenceEntityResolver defaultResolver = _context.getBean("defaultResolver", PreferenceEntityResolver.class);
        if (defaultResolver != null) {
            _defaultResolver = defaultResolver;
        } else if (_resolvers.size() == 1) {
            _defaultResolver = _resolvers.get(0);
        } else {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You must have at least one preferences entity resolver instance marked as the default resolver for the application.");
        }
        // Make sure all of our preferences are initialized.
        for (final PreferenceEntityResolver resolver : _resolvers) {
            _resolversByClass.put(resolver.getClass(), resolver);
        }
        final Map<String, PreferenceBean> preferenceBeans = _context.getBeansOfType(PreferenceBean.class);
        if (preferenceBeans != null) {
            for (final PreferenceBean bean : preferenceBeans.values()) {
                final String toolId = bean.getToolId();
                if (!getToolIds().contains(toolId)) {
                    createTool(bean);
                }
                _beansByToolId.put(toolId, bean.initialize(this));
                getResolverByClass(toolId, bean.getResolver());
            }
        }
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
        final Tool tool = _toolService.getByToolId(toolId);
        if (tool == null) {
            // What?!
            throw new UnknownToolId(toolId);
        }

        // Get the resolver ID from the tool.
        final Class<? extends PreferenceEntityResolver> resolverClass = tool.getResolver();
        return getResolverByClass(toolId, resolverClass);
    }

    private PreferenceEntityResolver getResolverByClass(final String toolId, final Class<? extends PreferenceEntityResolver> resolverClass) {
        if (resolverClass == null) {
            _resolversByToolId.put(toolId, _defaultResolver);
        } else if (_resolversByClass.containsKey(resolverClass)) {
            _resolversByToolId.put(toolId, _resolversByClass.get(resolverClass));
        } else {
            try {
                final PreferenceEntityResolver resolver = _context.getBean(resolverClass);
                _resolversByToolId.put(toolId, resolver);
                _resolversByClass.put(resolverClass, resolver);
            } catch (NoSuchBeanDefinitionException e) {
                try {
                    final PreferenceEntityResolver resolver = resolverClass.newInstance();
                    _resolversByToolId.put(toolId, resolver);
                    _resolversByClass.put(resolverClass, resolver);
                } catch (InstantiationException | IllegalAccessException e1) {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "There was an error constructing the resolver class " + resolverClass, e1);
                }
            }
        }
        return _resolversByToolId.get(toolId);
    }

    /**
     * Gets the {@link Tool tool instance} indicated by the submitted tool ID. Note that this method does not create the
     * tool if it does not yet exist in the preferences service. Instead it throws an {@link UnknownToolId} exception.
     *
     * @param toolId The unique tool ID.
     *
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

    private static final Scope  DEFAULT_SCOPE     = EntityId.Default.getScope();
    private static final String DEFAULT_ENTITY_ID = EntityId.Default.getEntityId();

    private static final Logger       _log    = LoggerFactory.getLogger(DefaultNrgPreferenceService.class);
    private static final ObjectMapper _mapper = new ObjectMapper();

    static {
        _mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    @Inject
    private ToolService _toolService;

    @Inject
    private PreferenceService _preferenceService;

    @Inject
    private List<PreferenceEntityResolver> _resolvers;

    private final Map<String, PreferenceEntityResolver>                                    _resolversByToolId = new HashMap<>();
    private final Map<Class<? extends PreferenceEntityResolver>, PreferenceEntityResolver> _resolversByClass  = new HashMap<>();

    private final Map<String, PreferenceBean> _beansByToolId = new HashMap<>();

    private ApplicationContext       _context;
    private PreferenceEntityResolver _defaultResolver;
}
