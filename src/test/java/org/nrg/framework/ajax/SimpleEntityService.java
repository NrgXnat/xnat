/*
 * framework: org.nrg.framework.ajax.SimpleEntityService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.ajax;


import org.nrg.framework.orm.hibernate.BaseHibernateService;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public interface SimpleEntityService extends BaseHibernateService<SimpleEntity> {
    SimpleEntity findByName(final String name);

    List<SimpleEntity> findAllOrderedByTimestamp();

    List<SimpleEntity> findAllOrderedByTimestamp(final SimpleEntityPaginatedRequest request);

    long getAllForUserCount(final String username);

    List<SimpleEntity> findAllByUsername(final String username);

    List<SimpleEntity> findAllByUsername(final String username, final @Nonnull SimpleEntityPaginatedRequest request);

    Optional<SimpleEntity> findByIdAndUsername(final long id, final String username);
}
