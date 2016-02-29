package org.nrg.xnat.initialization;

import org.nrg.framework.datacache.SerializerRegistry;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.HistoricPasswordValidator;
import org.nrg.xdat.security.PasswordValidatorChain;
import org.nrg.xdat.security.RegExpValidator;
import org.nrg.xnat.utils.XnatUserProvider;
import org.nrg.xnat.restlet.XnatRestletExtensions;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerPackages;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Configuration
@ComponentScan({"org.nrg.framework.datacache.impl.hibernate",
                "org.nrg.framework.services.impl",
                "org.nrg.xdat.daos",
                "org.nrg.xdat.services",
                "org.nrg.xft.daos",
                "org.nrg.xft.services",
                "org.nrg.xapi.configuration",
                "org.nrg.xnat.helpers.merge",
                "org.nrg.xnat.configuration",
                "org.nrg.xnat.services",
                "org.nrg.prefs.repositories",
                "org.nrg.prefs.services.impl.hibernate",
                "org.nrg.dicomtools.filters"})
@ImportResource({"WEB-INF/conf/xnat-security.xml", "WEB-INF/conf/orm-config.xml", "WEB-INF/conf/mq-context.xml"})
public class RootConfig {

    public static final List<String> DEFAULT_ENTITY_PACKAGES = Arrays.asList("org.nrg.framework.datacache", "org.nrg.xft.entities", "org.nrg.xdat.entities", "org.nrg.prefs.entities", "org.nrg.config.entities");

    @Bean
    public String siteId() {
        return _siteId;
    }

    @Bean
    public RegExpValidator regexValidator(@Value("${security.password_complexity:^.*$}") final String complexityExpression,
                                          @Value("${security.password_complexity_message:Password is not sufficiently complex.}") final String complexityMessage) {
        return new RegExpValidator(complexityExpression, complexityMessage);
    }

    @Bean
    public HistoricPasswordValidator historicPasswordValidator(@Value("${security.password_history:365}") final int durationInDays) {
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
    public XnatUserProvider receivedFileUserProvider(@Value("${services.dicom.scp.receivedfileuser:admin}") final String receivedFileUser) {
        return new XnatUserProvider(receivedFileUser);
    }

    @Bean
    public XnatRestletExtensions xnatRestletExtensions() {
		return new XnatRestletExtensions(new HashSet<String>(Arrays.asList(new String[] {"org.nrg.xnat.restlet.extensions"})));
    }

    @Bean
    public ImporterHandlerPackages importerHandlerPackages() {
		return new ImporterHandlerPackages(new HashSet<String>(Arrays.asList(new String[] {"org.nrg.xnat.restlet.actions","org.nrg.xnat.archive"})));
    }

    @Bean
    public SerializerRegistry serializerRegistry() {
        return new SerializerRegistry();
    }

    @Value("${site.title:XNAT}")
    private String _siteId;
}
