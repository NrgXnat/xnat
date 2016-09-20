/*
 * org.nrg.prefs.entities.Tool
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.entities;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.scope.EntityResolver;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.PreferenceBean;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;

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

    public Tool(final PreferenceBean bean) {
        final Class<? extends PreferenceBean> beanClass = bean.getClass();
        final NrgPreferenceBean annotation = beanClass.getAnnotation(NrgPreferenceBean.class);
        if (annotation == null) {
            // TODO: We might be able to use bean properties to extrapolate some of the info in the annotation and allow configuration that way as well.
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The preferences bean class " + beanClass.getName() + " must be annotated with the NrgPreferenceBean annotation.");
        }

        setToolId(annotation.toolId());
        setToolName(annotation.toolName());
        setToolDescription(annotation.description());
        setStrict(annotation.strict());

        // TODO: This is an array because you can't set null for annotation default values, but you should never set multiple resolvers.
        final Class<? extends PreferenceEntityResolver>[] resolvers = annotation.resolver();
        if (resolvers.length > 1) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You should only set zero or one resolver for the NrgPreferenceBean annotation on the " + beanClass.getName() + ".");
        } else if (resolvers.length == 1) {
            setResolver(resolvers[0]);
        }
    }

    /**
     * Creates a tool instance with the specified ID, name, description, and default preference names and values.
     *
     * @param toolId          The ID of the tool instance.
     * @param toolName        The name of the tool instance.
     * @param toolDescription The description of the tool instance.
     * @param strict          Whether the available preferences for this tool are limited to the specified list.
     * @param resolver        The class of the entity resolver to use for this tool.
     */
    public Tool(final String toolId, final String toolName, final String toolDescription, final boolean strict, final Class<? extends PreferenceEntityResolver> resolver) {
        if (_log.isDebugEnabled()) {
            _log.debug("Creating tool instance for ID [{}] {}: {}", toolId, toolName, toolDescription);
        }
        setToolId(toolId);
        setToolName(toolName);
        setToolDescription(toolDescription);
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
     * Indicates whether the set of preferences for the tool is strictly defined by the preferences defined by the
     * associated {@link PreferenceBean} class or whether free-form or ad hoc preferences can be added. The default is
     * false.
     *
     * @return Whether the preferences are limited to those defined by the preferences list.
     */
    public boolean isStrict() {
        return _strict;
    }

    /**
     * Sets whether the set of preferences for the tool is strictly defined by the preferences defined by the associated
     * {@link PreferenceBean} class or whether free-form or ad hoc preferences can be added. The default is false.
     *
     * @param strict Whether the preferences are limited to those defined by the preferences list.
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

    /**
     * Sets the class of the preferred entity resolver for this tool. If this is set to null, the default entity
     * resolver for the system should be used.
     *
     * @param resolver The class of the preferred entity resolver for this tool.
     */
    private void setResolver(final Class<? extends PreferenceEntityResolver> resolver) {
        _resolver = resolver;
    }

    private static final Logger _log = LoggerFactory.getLogger(Tool.class);

    private String                                    _toolId;
    private String                                    _toolName;
    private String                                    _toolDescription;
    private boolean                                   _strict;
    private Class<? extends PreferenceEntityResolver> _resolver;
}
