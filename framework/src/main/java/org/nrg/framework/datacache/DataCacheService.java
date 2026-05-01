/*
 * framework: org.nrg.framework.datacache.DataCacheService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.datacache;

import org.nrg.framework.orm.hibernate.BaseHibernateService;

import java.io.Serializable;

public interface DataCacheService extends BaseHibernateService<DataCacheItem> {
    <T extends Serializable> long put(String key, T value);
    <T extends Serializable> T get(String key);
    <T extends Serializable> T remove(String key);
    void clean();
}
