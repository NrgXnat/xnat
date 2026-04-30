/*
 * spawner: org.nrg.xnat.spawner.entities.SpawnerElement
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.spawner.exceptions.InvalidElementIdException;

import javax.persistence.*;

import java.io.Serial;
import java.util.regex.Pattern;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"namespace", "elementId"}))
@Cacheable
public class SpawnerElement extends AbstractHibernateEntity implements Comparable<SpawnerElement> {
    @Serial
    private static final long serialVersionUID = -8959822111407129444L;

    public static final String DEFAULT_NAMESPACE = "xnat";
    public static final String NAMESPACE_COLUMN = "character varying(255) default '" + DEFAULT_NAMESPACE + "'";

    public SpawnerElement() {
        //
    }

    public SpawnerElement(final String elementId, final String yaml) throws InvalidElementIdException {
        setNamespace(DEFAULT_NAMESPACE);
        setElementId(elementId);
        setYaml(yaml);
    }

    @SuppressWarnings({"unused", "RedundantSuppression"})
    public SpawnerElement(final String namespace, final String elementId, final String yaml) throws InvalidElementIdException {
        setNamespace(namespace);
        setElementId(elementId);
        setYaml(yaml);
    }

    /**
     * Indicates the namespace for this element. The namespace provides a way to organize groups of elements within the
     * system and otherwise has no effect on the element's rendering.
     *
     * @return The element's namespace.
     */
    @Column(nullable = false, columnDefinition = NAMESPACE_COLUMN)
    public String getNamespace() {
        return _namespace;
    }

    public void setNamespace(final String namespace) {
        _namespace = namespace;
    }

    /**
     * The element's individual ID. The element ID must be unique within its {@link #getNamespace() namespace}.
     *
     * @return The element's ID.
     */
    @Column(nullable = false)
    public String getElementId() {
        return _elementId;
    }

    public void setElementId(final String elementId) throws InvalidElementIdException {
        if (!ELEMENT_ID_REGEX.matcher(elementId).matches()) {
            throw new InvalidElementIdException(getNamespace(), elementId);
        }
        _elementId = elementId;
    }

    /**
     * A readable label for the element. This should indicate the function and use for the element.
     *
     * @return The element's label.
     */
    public String getLabel() {
        return _label;
    }

    public void setLabel(final String label) {
        _label = label;
    }

    /**
     * A description for the element.
     *
     * @return The element's description.
     */
    @Column(columnDefinition = "varchar(1024)")
    public String getDescription() {
        return _description;
    }

    public void setDescription(final String description) {
        _description = description;
    }

    /**
     * This is the element's payload that is delivered to the rendering client.
     *
     * @return The element payload.
     */
    @Column(columnDefinition = "TEXT")
    public String getYaml() {
        return _yaml;
    }

    public void setYaml(final String yaml) {
        _yaml = yaml;
    }

    /**
     * This is the element's meta.restricted if it exists
     *
     * @return The element's meta.restricted value
     */
    public String getRestricted() {
        return _restricted;
    }

    public void setRestricted(final String restricted) {
        _restricted = restricted;
    }

    @Transient
    @JsonIgnore
    public void updateFrom(final SpawnerElement other) {
        if (StringUtils.isNotBlank(other.getElementId()) && !StringUtils.equals(other.getElementId(), getElementId())) {
            throw new NrgServiceRuntimeException("You can't update the element ID from one spawner element to the other.");
        }
        if (other.getLabel() != null && !StringUtils.equals(other.getLabel(), getLabel())) {
            setLabel(other.getLabel());
        }
        if (other.getDescription() != null && !StringUtils.equals(other.getDescription(), getDescription())) {
            setDescription(other.getDescription());
        }
        if (other.getYaml() != null && !StringUtils.equals(other.getYaml(), getYaml())) {
            setYaml(other.getYaml());
        }
        if (other.getRestricted() != null && !StringUtils.equals(other.getRestricted(), getRestricted())) {
            setRestricted(other.getRestricted());
        }
    }
    
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof SpawnerElement)) {
            return false;
        }

        final SpawnerElement that = (SpawnerElement) other;

        return new EqualsBuilder().append(_namespace, that._namespace).append(_elementId, that._elementId).append(_yaml, that._yaml).isEquals();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(final SpawnerElement other) {
        if (other == null) {
            return 1;
        }
        final int namespaces = _namespace.compareTo(other._namespace);
        return namespaces == 0 ? 0 : _elementId.compareTo(other._elementId);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(_elementId).append(_yaml).toHashCode();
    }

    private static final Pattern ELEMENT_ID_REGEX = Pattern.compile("^[0-9a-zA-Z_-]+$");

    private String _namespace;
    private String _elementId;
    private String _label;
    private String _description;
    private String _yaml;
    private String _restricted;
}
