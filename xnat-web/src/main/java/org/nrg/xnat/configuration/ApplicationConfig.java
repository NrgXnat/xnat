package org.nrg.xnat.configuration;

import org.apache.commons.lang.StringUtils;
import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.InitializerSiteConfiguration;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.*;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.services.ThemeService;
import org.nrg.xdat.services.impl.ThemeServiceImpl;
import org.nrg.xnat.initialization.InitializingTasksExecutor;
import org.nrg.xnat.restlet.XnatRestletExtensions;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerPackages;
import org.nrg.xnat.services.PETTracerUtils;
import org.nrg.xnat.utils.XnatUserProvider;
import org.springframework.context.annotation.*;

import javax.inject.Inject;
import java.util.*;

@Configuration
@ComponentScan({"org.nrg.automation.repositories", "org.nrg.config.daos", "org.nrg.dcm.xnat", "org.nrg.dicomtools.filters",
                "org.nrg.framework.datacache.impl.hibernate", "org.nrg.framework.services.impl", "org.nrg.notify.daos",
                "org.nrg.prefs.repositories", "org.nrg.xdat.daos", "org.nrg.xdat.services.impl.hibernate", "org.nrg.xft.daos",
                "org.nrg.xft.event.listeners", "org.nrg.xft.services", "org.nrg.xnat.configuration", "org.nrg.xnat.daos",
                "org.nrg.xnat.event.listeners", "org.nrg.xnat.helpers.merge", "org.nrg.xnat.initialization.tasks",
                "org.nrg.xnat.services.impl.hibernate", "org.nrg.xnat.spawner.repositories", "org.nrg.automation.daos"})
@Import({FeaturesConfig.class, ReactorConfig.class})
@ImportResource("WEB-INF/conf/mq-context.xml")
public class ApplicationConfig {
    @Bean
    public ThemeService themeService() {
        return new ThemeServiceImpl();
    }

    @Bean
    @Primary
    public ContextService contextService() {
        return ContextService.getInstance();
    }

    @Bean
    public InitializingTasksExecutor initializingTasksExecutor() {
        return new InitializingTasksExecutor();
    }

    @Bean
    public SiteConfigPreferences siteConfigPreferences() {
        return new SiteConfigPreferences();
    }

    @Bean
    public PETTracerUtils petTracerUtils() throws Exception {
        return new PETTracerUtils();
    }

    @Bean
    public UserManagementServiceI userManagementService() {
        // TODO: This should be made to use a preference setting.
        return new XDATUserMgmtServiceImpl();
    }

    @Bean
    public RegExpValidator regexValidator() throws SiteConfigurationException {
        return new RegExpValidator();
    }

    @Bean
    public HistoricPasswordValidator historicPasswordValidator() throws SiteConfigurationException {
        return new HistoricPasswordValidator();
    }

    @Bean
    public PasswordValidatorChain validator(final RegExpValidator regExpValidator, final HistoricPasswordValidator historicPasswordValidator) {
        return new PasswordValidatorChain();

    }

    // MIGRATION: I'm not even sure this is used, but we need to do away with it in favor of prefs.
    @Bean
    public List<String> propertiesRepositories() {
        return Collections.singletonList("WEB-INF/conf/properties");
    }

    @Bean
    public XnatUserProvider receivedFileUserProvider() throws SiteConfigurationException {
        final String receivedFileUser = _preferences.getReceivedFileUser();
        return new XnatUserProvider(receivedFileUser);
    }

    @Bean
    public XnatRestletExtensions xnatRestletExtensions() {
        return new XnatRestletExtensions(new HashSet<>(Arrays.asList(new String[]{"org.nrg.xnat.restlet.extensions"})));
    }

    @Bean
    public ImporterHandlerPackages importerHandlerPackages() {
        return new ImporterHandlerPackages(new HashSet<>(Arrays.asList(new String[]{"org.nrg.xnat.restlet.actions", "org.nrg.xnat.archive"})));
    }

    @Inject
    private InitializerSiteConfiguration _preferences;
}
