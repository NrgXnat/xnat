/*
 * framework: org.nrg.framework.orm.hibernate.audit.NrgRevisionEntity
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate.audit;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * The Hibernate Envers revision entity for the XNAT session factory, adding the username responsible for each revision
 * to the standard revision number and timestamp.
 *
 * <p>This entity deliberately maps onto the same table and "rev"/"revtstmp" columns that Envers uses when no custom
 * revision entity is configured, which is what XNAT deployments have used until now for entities that were already
 * audited (e.g. {@code FileStoreInfo} and {@code ArchiveProcessorInstance} in xnat-web, {@code Script} in automation).
 * The mapped name "revinfo" becomes the physical table "xhbm_revinfo" under
 * {@link org.nrg.framework.orm.hibernate.PrefixedPhysicalNamingStrategy}, which is exactly what Envers' own default
 * revision entity produces. Revisions recorded before this entity existed therefore remain valid: upgrading only adds
 * the nullable "username" column to the existing table. Do not rename the table or the inherited columns.
 *
 * <p>Note that Envers permits exactly ONE {@link RevisionEntity} per session factory, so this entity serves every
 * audited entity in the XNAT stack, all of which gain username capture through {@link NrgRevisionListener}. It lives
 * in framework, alongside {@link org.nrg.framework.orm.hibernate.AbstractHibernateEntity} and the Envers read path in
 * {@link org.nrg.framework.orm.hibernate.AbstractHibernateDAO}, so that every module picks up the same revision
 * entity.
 */
@Entity
@Table(name = "revinfo")
@RevisionEntity(NrgRevisionListener.class)
@AttributeOverrides({@AttributeOverride(name = "id", column = @Column(name = "rev")),
                     @AttributeOverride(name = "timestamp", column = @Column(name = "revtstmp"))})
public class NrgRevisionEntity extends DefaultRevisionEntity {
    private static final long serialVersionUID = -4085207304228190166L;

    /**
     * {@link DefaultRevisionEntity} declares its identifier on the field, so this hierarchy uses field access and the
     * column must be named here rather than on the getter.
     */
    @Column(name = "username")
    private String _username;

    /**
     * Gets the username of the user responsible for this revision, or null when the revision was made with no security
     * context on the writing thread, such as by an initialization task or a scheduler. Note that null does not mean
     * "an unauthenticated caller": a change made on a request thread by a caller who never logged in is attributed to
     * the anonymous principal, which in xnat-web is the guest username. See {@link NrgRevisionListener}.
     *
     * @return The username responsible for this revision.
     */
    public String getUsername() {
        return _username;
    }

    /**
     * Sets the username of the user responsible for this revision.
     *
     * @param username The username responsible for this revision.
     */
    public void setUsername(final String username) {
        _username = username;
    }
}
