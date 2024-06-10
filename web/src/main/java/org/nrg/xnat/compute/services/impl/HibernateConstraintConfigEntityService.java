package org.nrg.xnat.compute.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.compute.entities.ConstraintConfigEntity;
import org.nrg.xnat.compute.repositories.ConstraintConfigDao;
import org.nrg.xnat.compute.services.ConstraintConfigEntityService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HibernateConstraintConfigEntityService extends AbstractHibernateEntityService<ConstraintConfigEntity, ConstraintConfigDao> implements ConstraintConfigEntityService {

}
