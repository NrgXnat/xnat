/*
 * framework: org.nrg.framework.datacache.impl.hibernate.DataCacheItemDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.datacache.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.datacache.DataCacheItem;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class DataCacheItemDAO extends AbstractHibernateDAO<DataCacheItem> {
    public DataCacheItem getByKey(final String key) {
        return findByUniqueProperty("key", key);
    }
}
