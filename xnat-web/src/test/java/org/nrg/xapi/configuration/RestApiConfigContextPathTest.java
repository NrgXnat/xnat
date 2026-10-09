package org.nrg.xapi.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Swagger UI builds each request URL as basePath + path from the generated spec. When XNAT is deployed to a
 * non-ROOT context, the context path must appear exactly once in that URL.
 */
public class RestApiConfigContextPathTest {
    private static final String CONTEXT_PATH = "/xnat";
    private static final String SERVLET_PATH = "/xapi";

    @Test
    public void operationUrlContainsContextPathOnceInNonRootDeployment() throws Exception {
        assertThat(operationUrls(CONTEXT_PATH)).contains("/xnat/xapi/siteConfig")
                                               .doesNotContain("/xnat/xapi/xnat/siteConfig");
    }

    @Test
    public void operationUrlIsUnchangedInRootDeployment() throws Exception {
        assertThat(operationUrls("")).contains("/xapi/siteConfig");
    }

    private List<String> operationUrls(final String contextPath) throws Exception {
        final MockServletContext servletContext = new MockServletContext();
        servletContext.setContextPath(contextPath);
        final AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(servletContext);
        context.register(SwaggerTestConfig.class);
        context.refresh();
        try {
            final MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
            final String json = mockMvc.perform(get(contextPath + SERVLET_PATH + "/v2/api-docs").contextPath(contextPath).servletPath(SERVLET_PATH).accept(MediaType.APPLICATION_JSON))
                                       .andReturn().getResponse().getContentAsString();
            final JsonNode spec = new ObjectMapper().readTree(json);
            final String basePath = spec.get("basePath").asText();
            final List<String> urls = new ArrayList<>();
            spec.get("paths").fieldNames().forEachRemaining(path -> urls.add((basePath + path).replaceAll("/+", "/")));
            return urls;
        } finally {
            context.close();
        }
    }

    @Configuration
    @EnableWebMvc
    @EnableSwagger2WebMvc
    public static class SwaggerTestConfig {
        @Bean
        public Docket api(final ServletContext servletContext) {
            final XnatAppInfo info = mock(XnatAppInfo.class);
            when(info.getVersion()).thenReturn("test");
            final StaticMessageSource messageSource = new StaticMessageSource();
            messageSource.setUseCodeAsDefaultMessage(true);
            return new RestApiConfig().api(info, messageSource, servletContext);
        }

        @Bean
        public SiteConfigStub siteConfigStub() {
            return new SiteConfigStub();
        }
    }

    @XapiRestController
    @RequestMapping("/siteConfig")
    public static class SiteConfigStub {
        @GetMapping
        public String get() {
            return "ok";
        }
    }
}
