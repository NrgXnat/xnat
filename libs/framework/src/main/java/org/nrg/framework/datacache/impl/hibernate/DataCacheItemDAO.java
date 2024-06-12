/*
 * framework: org.nrg.framework.datacache.impl.hibernate.DataCacheItemDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.datacache.impl.hibernate;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.datacache.DataCacheItem;
import org.springframework.stereotype.Repository;

@Repository
public class DataCacheItemDAO extends AbstractHibernateDAO<DataCacheItem> {
    public DataCacheItem getByKey(final String key) {
        return findByUniqueProperty("key", key);
    }
}
