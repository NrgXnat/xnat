package org.nrg.xnat.compute.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.compute.entities.ComputeEnvironmentConfigEntity;
import org.nrg.xnat.compute.models.ComputeEnvironmentConfig;

import java.util.List;

public interface ComputeEnvironmentConfigEntityService extends BaseHibernateService<ComputeEnvironmentConfigEntity> {

    void addHardwareConfigEntity(Long computeEnvironmentConfigId, Long hardwareConfigId);
    void removeHardwareConfigEntity(Long computeEnvironmentConfigId, Long hardwareConfigId);
    List<ComputeEnvironmentConfigEntity> findByType(ComputeEnvironmentConfig.ConfigType type);

}
