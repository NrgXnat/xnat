/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xft.schema.db.dao;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xft.schema.db.entities.DBBackedSchema;
import org.springframework.stereotype.Repository;

@Repository
public class DBBackedSchemaDAO extends AbstractHibernateDAO<DBBackedSchema> {
    public DBBackedSchema findByPath(String path, boolean includeDisabled) {
        return instance(includeDisabled
                        ? findByProperties(parameters("path", path))
                        : findByProperties(parameters("path", path, ENABLED_PROPERTY, true)));
    }

    public DBBackedSchema findByName(String name, boolean includeDisabled) {
        return instance(includeDisabled
                        ? findByProperties(parameters("name", name))
                        : findByProperties(parameters("name", name, ENABLED_PROPERTY, true)));
    }
}
