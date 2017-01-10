/*
 * core: org.nrg.xdat.configuration.TestAliasTokenServiceConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.configuration;

import org.nrg.xdat.daos.AliasTokenDAO;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.impl.hibernate.HibernateAliasTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TestXdatUserAuthServiceConfig.class, MockUserConfig.class})
public class TestAliasTokenServiceConfig {
    @Bean
    public AliasTokenDAO aliasTokenDAO() {
        return new AliasTokenDAO();
    }

    @Bean
    public AliasTokenService aliasTokenService(final AliasTokenDAO aliasTokenDAO, final SiteConfigPreferences preferences, final UserManagementServiceI userService) {
        return new HibernateAliasTokenService(aliasTokenDAO, preferences, userService);
    }
}
