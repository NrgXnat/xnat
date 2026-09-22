package org.nrg.xapi.configuration;

import org.junit.Test;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Guards the springfox 2.10 wiring (XXX-303). springfox-swagger2 declares springfox-spring-webmvc as a
 * provided dependency, so the classes imported by {@link EnableSwagger2WebMvc} are only present when the
 * module is added to the runtime classpath explicitly. Without it the application context fails at startup.
 */
public class RestApiConfigTest {
    @Test
    public void restApiConfigEnablesSwagger2WebMvc() {
        assertNotNull("RestApiConfig must be annotated with @EnableSwagger2WebMvc",
                      AnnotationUtils.findAnnotation(RestApiConfig.class, EnableSwagger2WebMvc.class));
    }

    @Test
    public void swagger2WebMvcImportsAreOnTheClasspath() {
        final Import imports = AnnotationUtils.findAnnotation(EnableSwagger2WebMvc.class, Import.class);
        assertNotNull("@EnableSwagger2WebMvc should import springfox configuration classes", imports);
        assertTrue("@EnableSwagger2WebMvc should import at least one configuration class", imports.value().length > 0);
        for (final Class<?> imported : imports.value()) {
            assertNotNull("Imported springfox configuration must be loadable: " + imported.getName(),
                          imported.getDeclaredConstructors());
        }
    }

    @Test
    public void springfoxWebMvcModuleIsPresent() throws ClassNotFoundException {
        Class.forName("springfox.documentation.spring.web.SpringfoxWebMvcConfiguration");
    }
}
