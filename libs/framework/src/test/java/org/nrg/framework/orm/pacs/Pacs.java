/*
 * framework: org.nrg.framework.orm.pacs.Pacs
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.pacs;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"aeTitle"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class Pacs extends AbstractHibernateEntity {

    private static final long serialVersionUID = -7601994736884673849L;
    private String aeTitle;

    private String host;

    private Integer storagePort;

    private Integer queryRetrievePort;

    private String ormStrategySpringBeanId;

    /**
     * for Hibernate
     */
    public Pacs() {
    }

    public Pacs(final String aeTitle, final String host, final Integer storagePort, final Integer queryRetrievePort, final String ormStrategySpringBeanId) {
        this.aeTitle = aeTitle;
        this.host = host;
        this.storagePort = storagePort;
        this.queryRetrievePort = queryRetrievePort;
        this.ormStrategySpringBeanId = ormStrategySpringBeanId;
    }

    public Pacs(final String aeTitle, final String host, final Integer port, final String ormStrategyId) {
        this(aeTitle, host, port, port, ormStrategyId);
    }

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
