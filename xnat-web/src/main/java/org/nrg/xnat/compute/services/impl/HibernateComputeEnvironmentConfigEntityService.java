package org.nrg.xnat.compute.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.compute.entities.ComputeEnvironmentConfigEntity;
import org.nrg.xnat.compute.entities.HardwareConfigEntity;
import org.nrg.xnat.compute.models.ComputeEnvironmentConfig;
import org.nrg.xnat.compute.repositories.ComputeEnvironmentConfigDao;
import org.nrg.xnat.compute.repositories.HardwareConfigDao;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Slf4j
public class HibernateComputeEnvironmentConfigEntityService extends AbstractHibernateEntityService<ComputeEnvironmentConfigEntity, ComputeEnvironmentConfigDao> implements ComputeEnvironmentConfigEntityService {

    private final HardwareConfigDao hardwareConfigDao;

    // For testing
    public HibernateComputeEnvironmentConfigEntityService(final ComputeEnvironmentConfigDao computeEnvironmentConfigDao,
                                                          final HardwareConfigDao hardwareConfigDao) {
        super();
        this.hardwareConfigDao = hardwareConfigDao;
        setDao(computeEnvironmentConfigDao);
    }

    @Autowired
    public HibernateComputeEnvironmentConfigEntityService(HardwareConfigDao hardwareConfigDao) {
        this.hardwareConfigDao = hardwareConfigDao;
    }

    /**
     * Associates a hardware config with a compute environment config
     * @param computeEnvironmentConfigId The compute environment config id
     * @param hardwareConfigId The hardware config id to associate with the compute environment config
     */
    @Override
    @Transactional
    public void addHardwareConfigEntity(Long computeEnvironmentConfigId, Long hardwareConfigId) {
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity = getDao().retrieve(computeEnvironmentConfigId);
        HardwareConfigEntity hardwareConfigEntity = hardwareConfigDao.retrieve(hardwareConfigId);
        computeEnvironmentConfigEntity.getHardwareOptions().addHardwareConfig(hardwareConfigEntity);
        getDao().update(computeEnvironmentConfigEntity);
        hardwareConfigDao.update(hardwareConfigEntity);
    }

    /**
     * Removes association between a hardware config and a compute environment config
     * @param computeEnvironmentConfigId The compute environment config id
     * @param hardwareConfigId The hardware config id to remove from the compute environment config
     */
    @Override
    @Transactional
    public void removeHardwareConfigEntity(Long computeEnvironmentConfigId, Long hardwareConfigId) {
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity = getDao().retrieve(computeEnvironmentConfigId);
        HardwareConfigEntity hardwareConfigEntity = hardwareConfigDao.retrieve(hardwareConfigId);
        computeEnvironmentConfigEntity.getHardwareOptions().removeHardwareConfig(hardwareConfigEntity);
        getDao().update(computeEnvironmentConfigEntity);
        hardwareConfigDao.update(hardwareConfigEntity);
    }

    /**
     * Returns a list of compute environment configs by type
     * @param type The type of compute environment configs to return
     * @return A list of compute environment configs of the environmentified type
     */
    @Override
    @Transactional
    public List<ComputeEnvironmentConfigEntity> findByType(ComputeEnvironmentConfig.ConfigType type) {
        return getDao().findByType(type.name());
    }

}
