package org.nrg.xnat.compute.config;

import org.mockito.Mockito;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.compute.repositories.ComputeEnvironmentConfigDao;
import org.nrg.xnat.compute.repositories.HardwareConfigDao;
import org.nrg.xnat.compute.services.*;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class MockConfig {

    @Bean
    public NamedParameterJdbcTemplate mockNamedParameterJdbcTemplate() {
        return Mockito.mock(NamedParameterJdbcTemplate.class);
    }

    @Bean
    public UserManagementServiceI mockUserManagementServiceI() {
        return Mockito.mock(UserManagementServiceI.class);
    }

    @Bean
    @Qualifier("mockRoleService")
    public RoleServiceI mockRoleService() {
        return Mockito.mock(RoleServiceI.class);
    }

    @Bean
    public RoleHolder mockRoleHolder(@Qualifier("mockRoleService") final RoleServiceI mockRoleService,
                                     final NamedParameterJdbcTemplate mockNamedParameterJdbcTemplate) {
        return new RoleHolder(mockRoleService, mockNamedParameterJdbcTemplate);
    }

    @Bean
    public SerializerService mockSerializerService() {
        return Mockito.mock(SerializerService.class);
    }

    @Bean
    public XnatAppInfo mockXnatAppInfo() {
        return Mockito.mock(XnatAppInfo.class);
    }

    @Bean
    public ConstraintConfigService mockPlacementConstraintConfigService() {
        return Mockito.mock(ConstraintConfigService.class);
    }

    @Bean
    public ComputeEnvironmentConfigService mockComputeEnvironmentConfigService() {
        return Mockito.mock(ComputeEnvironmentConfigService.class);
    }

    @Bean
    @Qualifier("mockComputeEnvironmentConfigEntityService")
    public ComputeEnvironmentConfigEntityService mockComputeEnvironmentConfigEntityService() {
        return Mockito.mock(ComputeEnvironmentConfigEntityService.class);
    }

    @Bean
    public HardwareConfigService mockHardwareConfigService() {
        return Mockito.mock(HardwareConfigService.class);
    }

    @Bean
    @Qualifier("mockHardwareConfigEntityService")
    public HardwareConfigEntityService mockHardwareConfigEntityService() {
        return Mockito.mock(HardwareConfigEntityService.class);
    }

    @Bean
    @Qualifier("mockHardwareConfigDao")
    public HardwareConfigDao mockHardwareConfigDao() {
        return Mockito.mock(HardwareConfigDao.class);
    }

    @Bean
    @Qualifier("mockComputeEnvironmentConfigDao")
    public ComputeEnvironmentConfigDao mockComputeEnvironmentConfigDao() {
        return Mockito.mock(ComputeEnvironmentConfigDao.class);
    }

    @Bean
    public JobTemplateService mockJobTemplateService() {
        return Mockito.mock(JobTemplateService.class);
    }

    @Bean
    public ConstraintConfigEntityService mockConstraintConfigEntityService() {
        return Mockito.mock(ConstraintConfigEntityService.class);
    }
}
