/*
 * framework: org.nrg.framework.orm.pacs.HibernatePacsEntityService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.pacs;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HibernatePacsEntityService extends AbstractHibernateEntityService<Pacs, PacsDAO> implements PacsEntityService {
    @Transactional
    @Override
    public Pacs findByAeTitle(final String aeTitle) {
        final List<Pacs> pacs = getDao().findByProperty("aeTitle", aeTitle);
        if (pacs == null) {
            return null;
        } else if (pacs.size() > 1) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Found multiple PACS with AE title " + aeTitle + ", but AE title is a unique attribute.");
        }
        return pacs.get(0);
    }
}
