/*
 * framework: org.nrg.framework.orm.auditable.AuditableEntity
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.auditable;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

/**
 * AuditableEntity class.
 *
 * @author Rick Herrick
 */
@Auditable
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"field1", "field2", "field3", "disabled"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class AuditableEntity extends AbstractHibernateEntity {

    public AuditableEntity() {

    }

    public AuditableEntity(final String field1, final int field2, final Date field3) {
        setField1(field1);
        setField2(field2);
        setField3(field3);
    }

    public String getField1() {
        return _field1;
    }

    public void setField1(String field1) {
        _field1 = field1;
    }

    public int getField2() {
        return _field2;
    }

    public void setField2(int field2) {
        _field2 = field2;
    }

    public Date getField3() {
        return _field3;
    }

    public void setField3(Date field3) {
        _field3 = field3;
    }

    private static final long serialVersionUID = -5822125156502997491L;

    private String _field1;
    private int _field2;
    private Date _field3;
}
