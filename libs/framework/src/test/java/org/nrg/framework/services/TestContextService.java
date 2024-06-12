package org.nrg.framework.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.exceptions.NotFoundException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestContextServiceConfiguration.class)
@WebAppConfiguration("src/test/resources")
public class TestContextService {
    private static final Pattern             STAND_IN_SERVICE_BEAN_NAME = Pattern.compile("^standInService[1-3]$");
    private static final Pattern             STAND_IN_SERVICE_NAME      = Pattern.compile("^Service [1-3]$");
    private static final Map<String, String> EXPECTED_PROPERTIES        = ImmutableMap.<String, String>builder().put("log4j.rootCategory", "DEBUG, frameworkTestAppender")
                                                                                      .put("log4j.appender.frameworkTestAppender", "org.apache.log4j.FileAppender")
                                                                                      .put("log4j.appender.frameworkTestAppender.File", "nrg-framework-test.log")
                                                                                      .put("log4j.appender.frameworkTestAppender.layout", "org.apache.log4j.PatternLayout")
                                                                                      .put("log4j.appender.frameworkTestAppender.layout.ConversionPattern", "%d{ABSOLUTE} %5p %40.40c:%4L - %m%n").build();

    private ContextService _contextService;

    @Autowired
    public void setContextService(final ContextService contextService) {
        _contextService = contextService;
    }

    @Test
    public void testGetBeanByType() {
        final SingletonService singleton = _contextService.getBean(SingletonService.class);
        assertThat(singleton).isNotNull().hasFieldOrPropertyWithValue("name", TestContextServiceConfiguration.SINGLETON);
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testGetBeanByTypeFails() {
        _contextService.getBean(NoSuchService.class);
        fail("This should have thrown NoSuchBeanDefinitionException");
    }

    @Test
    public void testGetBeanByName() {
        final SingletonService singleton = (SingletonService) _contextService.getBean("singletonService");
        assertThat(singleton).isNotNull().hasFieldOrPropertyWithValue("name", TestContextServiceConfiguration.SINGLETON);
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testGetBeanByNameFails() {
        _contextService.getBean("noSuchService");
        fail("This should have thrown NoSuchBeanDefinitionException");
    }

    @Test
    public void testGetBeansOfType() {
        final Map<String, StandInService> services = _contextService.getBeansOfType(StandInService.class);
        assertThat(services).isNotNull().hasSize(3).allSatisfy((beanName, service) -> {
            assertThat(beanName).matches(STAND_IN_SERVICE_BEAN_NAME);
            assertThat(service.getName()).matches(STAND_IN_SERVICE_NAME);
        });
    }

    @Test
    public void testGetBeanSafely() {
        final NoSuchService noSuchService = _contextService.getBeanSafely(NoSuchService.class);
        assertThat(noSuchService).isNull();
        final SingletonService singleton = _contextService.getBeanSafely(SingletonService.class);
        assertThat(singleton).isNotNull().hasFieldOrPropertyWithValue("name", TestContextServiceConfiguration.SINGLETON);
        final ManyImplService manyImplService = _contextService.getBeanSafely(ManyImplService.class, () -> _contextService.getBeanSafely(ManyImplServiceImpl.class));
        assertThat(manyImplService).isNotNull().hasFieldOrPropertyWithValue("name", TestContextServiceConfiguration.MANY_IMPL);
        final StandInService standInX = _contextService.getBeanSafely("standInServiceX", StandInService.class);
        assertThat(standInX).isNull();
        final StandInService standIn = _contextService.getBeanSafely("standInServiceX", StandInService.class, () -> _contextService.getBeanSafely("standInService1", StandInService.class));
        assertThat(standIn).isNotNull().hasFieldOrPropertyWithValue("name", TestContextServiceConfiguration.SERVICE_1);
    }

    @Test
    public void testGetConfigurationLocation() throws NotFoundException {
        final URI location = _contextService.getConfigurationLocation("log4j.properties");
        assertThat(location).isNotNull().asString().isNotBlank();
    }

    @Test
    public void testGetConfigurationStream() throws IOException {
        final Properties properties = new Properties();
        try (final InputStream input = _contextService.getConfigurationStream("log4j.properties")) {
            properties.load(input);
        }
        assertThat(properties).isNotEmpty().hasSize(5).containsExactlyInAnyOrderEntriesOf(EXPECTED_PROPERTIES);
    }
}
