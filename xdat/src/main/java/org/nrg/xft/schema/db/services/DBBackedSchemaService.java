/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xft.schema.db.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xft.schema.XFTDataModel;
import org.nrg.xft.schema.db.entities.DBBackedSchema;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DBBackedSchemaService extends BaseHibernateService<DBBackedSchema> {
    @Transactional
    DBBackedSchema findConfigByPath(String path);

    @Transactional
    DBBackedSchema findConfigByName(String name);

    @Transactional
    DBBackedSchema createConfig(String path, String name, String content) throws Exception;

    XFTDataModel initializeSchema(DBBackedSchema schema) throws Exception;

    @Transactional
    XFTDataModel registerNewElement(String prefix, String complexType, String prettyPrintName, SchemaElement extended, String singular, String plural, boolean generateDisplayDoc) throws Exception;

    @Transactional
    XFTDataModel registerNewSchema(String path, String name, String content, boolean generateDisplayDoc) throws Exception;

    List<String> getElementNames(DBBackedSchema config);

    @Transactional
    List<DBBackedSchema> findAllSchema();

    /**
     * Checks whether the elements of the specified schema are already loaded. This
     * allows you to check whether the schema is already loaded before trying to
     * load it again.
     *
     * @param id The ID of the {@link DBBackedSchema schema} to check
     *
     * @return Returns <pre>true</pre> if the schema is loaded, <pre>false</pre> otherwise.
     * @throws NotFoundException If no schema exists with the specified ID
     */
    boolean isSchemaLoaded(long id) throws NotFoundException;

    /**
     * Checks whether the elements of the specified schema are already loaded. This
     * allows you to check whether the schema is already loaded before trying to
     * load it again.
     *
     * @param schema The {@link DBBackedSchema schema} to check
     *
     * @return Returns <pre>true</pre> if the schema is loaded, <pre>false</pre> otherwise.
     */
    boolean isSchemaLoaded(DBBackedSchema schema);
}
