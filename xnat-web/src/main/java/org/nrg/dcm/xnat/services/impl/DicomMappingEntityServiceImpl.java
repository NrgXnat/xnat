package org.nrg.dcm.xnat.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.dcm.xnat.daos.DicomMappingEntityDao;
import org.nrg.dcm.xnat.entities.DicomMappingEntity;
import org.nrg.dcm.xnat.services.DicomMappingEntityService;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


@Slf4j
@Service
public class DicomMappingEntityServiceImpl
        extends AbstractHibernateEntityService<DicomMappingEntity, DicomMappingEntityDao>
        implements DicomMappingEntityService {

    @Override
    @Transactional
    @Nonnull
    public List<DicomMappingEntity> find(@Nullable String project, String schemaElement)
            throws NotFoundException {
        List<DicomMappingEntity> entities = getDao().findInScopeByProperty(project,
                "schemaElement", schemaElement);
        if (entities == null) {
            throw new NotFoundException("No matching DICOMAddParamMappingEntities");
        }
        return entities;
    }
}
