/*
 * framework: org.nrg.framework.orm.hibernate.AbstractHibernateEntity
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.annotations.UpdateTimestamp;
import org.nrg.framework.orm.NrgEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Provides basic properties for Hibernate entities, including:
 *
 * <ul>
 *     <li>{@link #getId() id}</li>
 *     <li>{@link #isEnabled() enabled}</li>
 *     <li>{@link #getCreated() created}</li>
 *     <li>{@link #getTimestamp() timestamp}</li>
 *     <li>{@link #getDisabled() disabled}</li>
 * </ul>
 *
 * This also supplies type definitions for <b>json</b> and <b>jsonb</b> properties. These types allow you to store
 * entity properties as JSON columns in the database. The properties may be typed as pure JsonNode or similar types or
 * as classes that can be serialized to and deserialized from JSON.
 *
 * Configure your JSON property in an entity class with the following annotations:
 *
 * <pre>
 *     {@literal @}Type(type = "json")
 *     {@literal @}Column(columnDefinition = "json")
 *     private SomeClass item1;

 *     {@literal @}Type(type = "jsonb")
 *     {@literal @}Column(columnDefinition = "jsonb")
 *     private AnotherClass item2;
 * </pre>
 *
 * The <a href="https://www.postgresql.org/docs/9.4/datatype-json.html">PostgreSQL documentation</a> describes the
 * differences between the <b>JSON</b> and <b>JSONB</b> data types.
 *
 * Note that JSON attributes are not currently compatible with the H2 database used for basic unit tests. Possible
 * workarounds are described <a href="https://vladmihalcea.com/how-to-map-json-objects-using-generic-hibernate-types">in
 * this issue</a>.
 */
@TypeDefs({@TypeDef(name = "json", typeClass = JsonStringType.class), @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)})
@MappedSuperclass
@SuppressWarnings("serial")
abstract public class AbstractHibernateEntity implements BaseHibernateEntity, Serializable {
    /**
     * Adds the submitted properties to the properties of the base {@link NrgEntity} class to exclude those properties
     * from search-by-example entity instances.
     *
     * @param properties The properties above and beyond the base entity properties to be excluded from a search.
     *
     * @return The full array of properties to be excluded from a search-by-example.
     */
    @SuppressWarnings("unused")
    public static String[] getExcludedProperties(final String... properties) {
        return ArrayUtils.addAll(EXCLUDE_BASE_PROPS, properties);
    }

    /**
     * Returns the ID of the data entity. This usually maps to the entity's primary
     * key in the appropriate database table.
     *
     * @return The ID of the data entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Override
    public long getId() {
        return _id;
    }

    /**
     * Sets the ID of the data entity. This usually maps to the entity's primary
     * key in the appropriate database table and, as such, should rarely be used
     * directly.
     *
     * @param id The ID to set for the data entity.
     */
    @Override
    public void setId(long id) {
        _id = id;
    }

    /**
     * Indicates whether this entity is currently enabled. For accountability and
     * auditing purposes, many entities can't actually be deleted from the database
     * but must be disabled instead. If this value is false, the entity should be
     * considered to be effectively deleted from the system for purposes of on-going
     * use. Note that new entities should be enabled by default.
     *
     * @return <b>true</b> if the entity is currently enabled, <b>false</b> otherwise.
     *
     * @see BaseHibernateEntity#isEnabled()
     */
    @Column(columnDefinition = "boolean default true")
    @Override
    public boolean isEnabled() {
        return _enabled;
    }

    /**
     * Sets the enabled flag for the entity. See {@link #isEnabled()} for more information
     * on the enabled state of data entities.
     *
     * @param enabled The enabled state to set on the entity.
     *
     * @see BaseHibernateEntity#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    /**
     * Returns the timestamp of the data entity's creation.
     *
     * @return The timestamp of the data entity's creation.
     */
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    @Override
    public Date getCreated() {
        return _created;
    }

    /**
     * Sets the timestamp of the data entity's creation.
     *
     * @param created The timestamp of the data entity's creation.
     */
    public void setCreated(Date created) {
        _created = created;
    }

    /**
     * Returns the timestamp of the last update to the data entity.
     *
     * @return The timestamp of the last update to the data entity.
     */
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    @Override
    public Date getTimestamp() {
        return _timestamp;
    }

    /**
     * Sets the timestamp of the last update to the data entity.
     *
     * @param timestamp The timestamp to set for the last update to the data entity.
     */
    @Override
    public void setTimestamp(Date timestamp) {
        _timestamp = timestamp;
    }

    /**
     * Returns the timestamp of the data entity's disabling. If this value is the same
     * as {@link HibernateUtils#DEFAULT_DATE}, the entity hasn't been disabled.
     *
     * @return The timestamp of the data entity's disabling.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    @Override
    public Date getDisabled() {
        return _disabled;
    }

    /**
     * Sets the timestamp of the data entity's disabling.
     *
     * @param disabled The timestamp to set for the data entity's disabling.
     */
    @Override
    public void setDisabled(Date disabled) {
        _disabled = disabled;
    }

    /**
     * Used to exclude the properties of the base {@link NrgEntity} class.
     */
    private static final String[] EXCLUDE_BASE_PROPS = new String[]{"id", "enabled", "created", "timestamp", "disabled"};

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AbstractHibernateEntity that = (AbstractHibernateEntity) o;
        return _id == that._id &&
               _enabled == that._enabled &&
               compareDates(_created, that._created) &&
               compareDates(_timestamp, that._timestamp) &&
               compareDates(_disabled, that._disabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, _enabled, _created, _timestamp, _disabled);
    }

    @Override
    public String toString() {
        return addParentPropertiesToString(MoreObjects.toStringHelper(this))
                .toString();
    }

    protected ToStringHelper addParentPropertiesToString(final ToStringHelper toStringHelper) {
        return toStringHelper
                .add("id", _id)
                .add("enabled", _enabled)
                .add("created", _created)
                .add("disabled", _disabled)
                .add("timestamp", _timestamp);
    }

    private static boolean compareDates(final Date first, final Date second) {
        // If they're both not null, we can just compare the times.
        if (ObjectUtils.allNotNull(first, second)) {
            return first.getTime() == second.getTime();
        }
        // If they're not both null, then they're not equal.
        return !ObjectUtils.anyNotNull(first, second);
    }

    private long    _id;
    private boolean _enabled  = true;
    private Date    _created;
    private Date    _timestamp;
    private Date    _disabled = HibernateUtils.DEFAULT_DATE;
}
