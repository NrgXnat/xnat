/*
 * ddict.entities.Resource
 *
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * XNAT is an open-source project of the Neuroinformatics Research Group.
 * Released under the Simplified BSD.
 *
 * Last modified 5/1/14 11:06 AM
 */

package org.nrg.prefs.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.scope.EntityId;
import org.nrg.framework.scope.EntityResolver;
import org.nrg.prefs.services.NrgPrefsService;

import javax.persistence.*;

/**
 * Represents a preference within the preferences service. The preference can either be a default value without a
 * specific scope and entity applied or an entity-specific override value. The entity to which the override value
 * applies depends on the value set for the {@link #getScope() scope} and {@link #getEntityId() entity ID} properties
 * for the preference. These can be resolved to a particular object in the system through the {@link EntityResolver}
 * implementation associated with the preference's {@link Tool} instance, which can be accessed via the {@link
 * NrgPrefsService#setEntityResolver(String, EntityResolver)} method.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"tool", "name", "scope", "entityId"}))
public class Preference extends AbstractHibernateEntity {
    /**
     * Creates a default empty preferences instance.
     */
    public Preference() {

    }

    /**
     * Creates a preference with the indicated tool, name, and value associated with the default scope.
     * @param tool     The tool with which the preference is associated.
     * @param name     The name of the preference.
     * @param value    The value for the preference.
     */
    public Preference(final Tool tool, final String name, final String value) {
        setTool(tool);
        setName(name);
        setValue(value);
    }

    /**
     * Creates a preference with the indicated tool, name, and value associated with the entity identified by the
     * given scope and entity ID.
     * @param tool     The tool with which the preference is associated.
     * @param name     The name of the preference.
     * @param scope    The scope for the preference.
     * @param entityId The entity ID for the preference.
     * @param value    The value for the preference.
     */
    public Preference(final Tool tool, final String name, final Scope scope, final String entityId, final String value) {
        setTool(tool);
        setName(name);
        setValue(value);
        setScope(scope);
        setEntityId(entityId);
    }

    /**
     * Returns the {@link Tool tool} with which this preference is associated.
     * @return The tool instance with which this preference is associated.
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    public Tool getTool() {
        return _tool;
    }

    /**
     * Sets the {@link Tool tool} with which this preference is associated.
     * @param tool    The tool with which the preference is associated.
     */
    public void setTool(final Tool tool) {
        _tool = tool;
    }

    /**
     * Gets the name for this preference. The name is not intended to be human readable and can include hierarchy
     * information encoded into the name.
     * @return The name for this preference.
     */
    @Column(nullable = false)
    public String getName() {
        return _name;
    }

    /**
     * Sets the name for this preference. The name is not intended to be human readable and can include hierarchy
     * information encoded into the name.
     * @param name    The name for this preference.
     */
    public void setName(final String name) {
        _name = name;
    }

    /**
     * Gets the value for this preference. The value must be in string form but may be encoded or serialized values to
     * be interpreted by the specific tool.
     * @return The value for this preference.
     */
    @Column(nullable = false, length = 65535)
    public String getValue() {
        return _value;
    }

    /**
     * Sets the value for this preference. The value must be in string form but may be encoded or serialized values to
     * be interpreted by the specific tool.
     * @param value    The value to set for this preference.
     */
    public void setValue(final String value) {
        _value = value;
    }

    /**
     * Sets the scope for this preference.
     * @return The current scope for this preference.
     */
    public Scope getScope() {
        return _scope;
    }

    /**
     * Sets the scope for this preference.
     * @param scope    The scope to set for the preference.
     */
    public void setScope(final Scope scope) {
        _scope = scope;
    }

    /**
     * Gets the entity ID for this preference. The ID only makes sense in the context of the {@link #getScope()
     * preference's scope}.
     * @return The entity ID for this preference.
     */
    public String getEntityId() {
        return _entityId;
    }

    /**
     * Sets the entity ID for this preference.
     * @param entityId    The entity ID to set for this preference.
     */
    public void setEntityId(final String entityId) {
        _entityId = entityId;
    }

    private Tool _tool;
    private String _name;
    private String _value;
    private Scope _scope = EntityId.Default.getScope();
    private String _entityId = EntityId.Default.getEntityId();
}
