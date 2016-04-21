package org.nrg.xnat.initialization;

import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.framework.configuration.FrameworkConfig;
import org.nrg.framework.datacache.SerializerRegistry;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.HistoricPasswordValidator;
import org.nrg.xdat.security.PasswordValidatorChain;
import org.nrg.xdat.security.RegExpValidator;
import org.nrg.xnat.event.conf.EventPackages;
import org.nrg.xnat.restlet.XnatRestletExtensions;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerPackages;
import org.nrg.xnat.utils.XnatUserProvider;
import org.springframework.context.annotation.*;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Configuration
@ComponentScan({"org.nrg.xdat.daos", "org.nrg.xnat.daos", "org.nrg.xnat.services.impl.hibernate", "org.nrg.xdat.services", "org.nrg.xft.daos", "org.nrg.xft.services", "org.nrg.xnat.helpers.merge", "org.nrg.xnat.configuration", "org.nrg.xnat.services", "org.nrg.dicomtools.filters"})
@Import(FrameworkConfig.class)
@ImportResource({"WEB-INF/conf/xnat-security.xml", "WEB-INF/conf/mq-context.xml"})
public class RootConfig {

    public static final List<String> DEFAULT_ENTITY_PACKAGES = Arrays.asList("org.nrg.xft.entities", "org.nrg.xft.event.entities", "org.nrg.xdat.entities", "org.nrg.xnat.entities", "org.nrg.xnat.event.entities", "org.nrg.config.entities");

    @Bean
    public InitializerSiteConfiguration initializerSiteConfiguration() {
        return new InitializerSiteConfiguration();
    }

    @Bean
    public RegExpValidator regexValidator() throws SiteConfigurationException {
        final String complexityExpression = _preferences.getPasswordComplexity();
        final String complexityMessage = _preferences.getPasswordComplexityMessage();
        return new RegExpValidator(complexityExpression, complexityMessage);
    }

    @Bean
    public HistoricPasswordValidator historicPasswordValidator() throws SiteConfigurationException {
        final int durationInDays = _preferences.getPasswordHistoryDuration();
        return new HistoricPasswordValidator(durationInDays);
    }

    @Bean
    public PasswordValidatorChain validator(final RegExpValidator regExpValidator, final HistoricPasswordValidator historicPasswordValidator) {
        return new PasswordValidatorChain(Arrays.asList(regExpValidator, historicPasswordValidator));
    }

    // MIGRATION: I'm not even sure this is used, but we need to do away with it in favor of prefs.
    @Bean
    public List<String> propertiesRepositories() {
        return Collections.singletonList("WEB-INF/conf/properties");
    }

    @Bean
    public ContextService contextService() {
        return ContextService.getInstance();
    }

    @Bean
    public HibernateEntityPackageList coreXnatEntityPackages() {
        return new HibernateEntityPackageList(DEFAULT_ENTITY_PACKAGES);
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

    @Bean
    public EventPackages eventPackages() {
        // NOTE:  These should be treated as parent packages.  All sub-packages should be searched
        return new EventPackages(new HashSet<>(Arrays.asList(new String[]{"org.nrg.xnat.event", "org.nrg.xft.event", "org.nrg.xdat.event"})));
    }

    @Bean
    public SerializerRegistry serializerRegistry() {
        return new SerializerRegistry();
    }

    @Inject
    private InitializerSiteConfiguration _preferences;
}
