/*
 * framework: org.nrg.framework.orm.pacs.Pacs
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.pacs;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Note that this entity class uses the Hibernate Envers @Audited annotation simply as a sanity test to ensure that
 * Envers functionality bootstraps properly.
 */
@Entity
@Audited
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"aeTitle"}))
@Cacheable
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("unused")
public class Pacs extends AbstractHibernateEntity {
    private static final long serialVersionUID = -7601994736884673849L;

    private String  aeTitle;
    private String  host;
    private Integer storagePort;
    private Integer queryRetrievePort;
    private String  ormStrategySpringBeanId;

    @NotEmpty
    @Size(max = 100)
    public String getAeTitle() {
        return aeTitle;
    }

    public void setAeTitle(final String aeTitle) {
        this.aeTitle = aeTitle;
    }

    @NotEmpty
    @Size(max = 100)
    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    @NotNull
    public Integer getStoragePort() {
        return storagePort;
    }

    public void setStoragePort(final Integer storagePort) {
        this.storagePort = storagePort;
    }

    @NotNull
    public Integer getQueryRetrievePort() {
        return queryRetrievePort;
    }

    public void setQueryRetrievePort(final Integer queryRetrievePort) {
        this.queryRetrievePort = queryRetrievePort;
    }

    /**
     * Ugly, this doesn't really belong on a domain object, not sure where to put it though...
     */
    @NotEmpty
    @Size(max = 100)
    public String getOrmStrategySpringBeanId() {
        return ormStrategySpringBeanId;
    }

    public void setOrmStrategySpringBeanId(final String ormStrategySpringBeanId) {
        this.ormStrategySpringBeanId = ormStrategySpringBeanId;
    }
}
