package org.nrg.xnat.compute.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.compute.entities.HardwareConfigEntity;
import org.nrg.xnat.compute.repositories.HardwareConfigDao;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HibernateHardwareConfigEntityService extends AbstractHibernateEntityService<HardwareConfigEntity, HardwareConfigDao> implements HardwareConfigEntityService {

}
