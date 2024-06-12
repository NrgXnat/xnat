/*
 * framework: org.nrg.framework.datacache.DataCacheItem
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.datacache;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Represents an entry in the NRG cache service. The entry is defined by a {@link #getKey() unique key}. The value is
 * always a {@link #getValue() simple string value} representing a serialized object, but {@link DataCacheService}
 * provides helper methods for de-serializing the
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class DataCacheItem extends AbstractHibernateEntity {
    @SuppressWarnings("unused")
    public DataCacheItem() {

    }

    @SuppressWarnings("unused")
    public DataCacheItem(final String key, final String value, final String type) {
        setKey(key);
        setValue(value);
        setType(type);
    }

    @Column(unique = true)
    public String getKey() {
        return _key;
    }

    public void setKey(final String key) {
        _key = key;
    }

    @Column(columnDefinition = "TEXT")
    public String getValue() {
        return _value;
    }

    public void setValue(final String value) {
        _value = value;
    }

    public String getType() {
        return _type;
    }

    public void setType(final String type) {
        _type = type;
    }

    private String _key;
    private String _value;
    private String _type;
}
