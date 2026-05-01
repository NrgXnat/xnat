/*
 * framework: org.nrg.framework.orm.NrgEntity
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm;

import java.util.Date;

public interface NrgEntity {
    /**
     * Returns the ID of the data entity. This usually maps to the entity's primary
     * key in the appropriate database table.
     * @return The ID of the data entity.
     */
    long getId();

    /**
     * Sets the ID of the data entity. This usually maps to the entity's primary
     * key in the appropriate database table and, as such, should rarely be used
     * directly.
     * @param id The ID to set for the data entity.
     */
    void setId(long id);

    /**
     * Indicates whether this entity is currently enabled. For accountability and
     * auditability purposes, many entities can't actually be deleted from the database
     * but must be disabled instead. If this value is false, the entity should be
     * considered to be effectively deleted from the system for purposes of on-going use.
     * Note that new entities should be enabled by default.
     * @return <b>true</b> if the entity is currently enabled, <b>false</b> otherwise.
     */
    boolean isEnabled();

    /**
     * Sets the enabled flag for the entity. See {@link #isEnabled()} for more information
     * on the enabled state of data entities.
     * @param enabled The enabled state to set on the entity.
     */
    void setEnabled(boolean enabled);

    /**
     * Returns the timestamp of the data entity's creation.
     * @return The timestamp of the data entity's creation.
     */
    Date getCreated();

    /**
     * Sets the timestamp of the data entity's creation.
     * @param created The timestamp of the data entity's creation.
     */
    void setCreated(Date created);

    /**
     * Returns the timestamp of the last update to the data entity.
     * @return The timestamp of the last update to the data entity.
     */
    Date getTimestamp();

    /**
     * Sets the timestamp of the last update to the data entity.
     * @param timestamp The timestamp of the last update to the data entity.
     */
    void setTimestamp(Date timestamp);

    /**
     * Returns the timestamp of the data entity's disabling.
     * @return The timestamp of the data entity's disabling.
     */
    Date getDisabled();

    /**
     * Sets the timestamp of the data entity's disabling.
     * @param disabled The timestamp of the data entity's disabling.
     */
    void setDisabled(Date disabled);
}
