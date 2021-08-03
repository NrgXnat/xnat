/*
 * config: org.nrg.config.entities.ConfigurationData
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.entities;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.Set;

@SuppressWarnings("deprecation")
@Auditable
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class ConfigurationData extends AbstractHibernateEntity {
    public static final int MAX_FILE_LENGTH = 1073741824; // 1 GB

    @Column(columnDefinition = "TEXT", length = MAX_FILE_LENGTH)
    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    @OneToMany(fetch = FetchType.EAGER)
    public Set<Configuration> getConfigurations() {
        return configurations;
    }

    @SuppressWarnings("unused")
    public void setConfigurations(Set<Configuration> configurations) {
        this.configurations = configurations;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final ConfigurationData that = (ConfigurationData) object;
        return new EqualsBuilder().appendSuper(super.equals(object)).append(contents, that.contents).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(contents).toHashCode();
    }

    private Set<Configuration> configurations;
    private String             contents;
}
