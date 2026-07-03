package org.nrg.xnat.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.UpdateTimestamp;
import org.nrg.framework.constants.Scope;

import org.nrg.framework.orm.hibernate.BaseHibernateEntity;
import org.nrg.framework.orm.hibernate.HibernateUtils;
import org.nrg.xnat.customforms.pojo.formio.RowIdentifier;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;


// CACHING: Had to modify this class because @IdClass and @Id annotations in this class conflicted with @Id in AbstractHibernateEntity
//  and broke everything.
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CustomVariableFormAppliesToId.class)
public class CustomVariableFormAppliesTo implements BaseHibernateEntity, Serializable {
    @Serial
    private static final long serialVersionUID = -1264374836830855705L;

    private long                    _id;
    private boolean                 _enabled  = true;
    private Date                    _created;
    private Date                    _timestamp;
    private Date                    _disabled = HibernateUtils.DEFAULT_DATE;
    private CustomVariableAppliesTo _customVariableAppliesTo;
    private CustomVariableForm      _customVariableForm;
    private String                  _xnatUser;
    private String                  _status;

    /**
     * Returns the ID of the data entity. This usually maps to the entity's primary
     * key in the appropriate database table.
     *
     * @return The ID of the data entity.
     */
    // The database primary key is the composite (customVariableAppliesTo, customVariableForm) — matching the
    // @IdClass — while this id is a plain sequence-defaulted column. Hibernate 6 validates that @Id properties
    // match the @IdClass exactly, so unlike Hibernate 5 this may no longer carry a third @Id.
    @Generated
    @Column(insertable = false, updatable = false, columnDefinition = "bigserial")
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

    private static boolean compareDates(final Date first, final Date second) {
        // If they're both not null, we can just compare the times.
        if (ObjectUtils.allNotNull(first, second)) {
            return first.getTime() == second.getTime();
        }
        // If they're not both null, then they're not equal.
        return !ObjectUtils.anyNotNull(first, second);
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    public CustomVariableAppliesTo getCustomVariableAppliesTo() {
        return _customVariableAppliesTo;
    }

    public void setCustomVariableAppliesTo(CustomVariableAppliesTo customVariableAppliesTo) {
        _customVariableAppliesTo = customVariableAppliesTo;
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    public CustomVariableForm getCustomVariableForm() {
        return _customVariableForm;
    }

    public void setCustomVariableForm(CustomVariableForm customVariableForm) {
        _customVariableForm = customVariableForm;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || Hibernate.getClass(this) != Hibernate.getClass(object)) {
            return false;
        }
        final CustomVariableFormAppliesTo that = (CustomVariableFormAppliesTo) object;
        return _id == that.getId() &&
               _enabled == that.isEnabled() &&
               compareDates(_created, that.getCreated()) &&
               compareDates(_timestamp, that.getTimestamp()) &&
               compareDates(_disabled, that.getDisabled()) &&
               StringUtils.equals(getXnatUser(), that.getXnatUser()) &&
               StringUtils.equals(getStatus(), that.getStatus()) &&
               getCustomVariableAppliesTo().equals(that.getCustomVariableAppliesTo()) &&
               getCustomVariableForm().equals(that.getCustomVariableForm());
    }

    public int hashCode() {
        return Objects.hash(getId(), getXnatUser(), getStatus(), getCustomVariableAppliesTo().hashCode(), getCustomVariableForm().hashCode());
    }

    @Override
    public String toString() {
        //Consistent with String.toString() on null
        String str = "null";
        if (getCustomVariableAppliesTo() != null && getCustomVariableForm() != null) {
            return getCustomVariableAppliesTo().toString() + " " + getCustomVariableForm().toString();
        }
        return str;
    }

    public String getXnatUser() {
        return _xnatUser;
    }

    public void setXnatUser(String xnatUser) {
        this._xnatUser = xnatUser;
    }

    public String getStatus() {
        return _status;
    }

    public void setStatus(String status) {
        this._status = status;
    }

    @Transient
    public @NotNull RowIdentifier getRowIdentifier() {
        RowIdentifier id = new RowIdentifier();
        id.setAppliesToId((_customVariableAppliesTo == null) ? -1 : _customVariableAppliesTo.getId());
        id.setFormId((_customVariableForm == null) ? -1 : _customVariableForm.getId());
        return id;
    }

    @Transient
    public boolean doProjectsShareForm() {
        return getCustomVariableForm() != null
               && getCustomVariableAppliesTo() != null
               && getCustomVariableAppliesTo().getScope().equals(Scope.Project)
               && getCustomVariableForm().getCustomVariableFormAppliesTos().size() > 1;
    }
}
