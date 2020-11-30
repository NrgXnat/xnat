/*
 * framework: org.nrg.framework.ajax.HibernateSimpleEntityService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.ajax;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HibernateSimpleEntityService extends AbstractHibernateEntityService<SimpleEntity, SimpleEntityDAO> implements SimpleEntityService {
    @Transactional
    @Override
    public SimpleEntity findByName(final String name) {
        final List<SimpleEntity> pacs = getDao().findByProperty("name", name);
        if (pacs == null) {
            return null;
        }
        if (pacs.size() > 1) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Found multiple entities with name " + name + ", but name is a unique attribute.");
        }
        return pacs.get(0);
    }
}
