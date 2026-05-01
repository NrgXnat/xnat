package org.nrg.dcm.xnat.services;

import org.nrg.dcm.xnat.entities.DicomMappingEntity;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface DicomMappingEntityService extends BaseHibernateService<DicomMappingEntity> {

    /**
     * Find entities for schemaElement matching project or for site
     * @param project the project or null
     * @param schemaElement the schema element
     * @return a list of entities
     * @throws NotFoundException if no entities configured for schemaElement
     */
    @Nonnull
    List<DicomMappingEntity> find(@Nullable String project, String schemaElement) throws NotFoundException;
}
