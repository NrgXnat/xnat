package org.nrg.xapi.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Swagger UI builds each request URL as server URL + path from the generated spec, and resolves a server URL
 * that starts with "/" against the host. When XNAT is deployed to a non-ROOT context, the context path must
 * appear exactly once in that URL. The springdoc counterpart of develop's springfox test for XXX-303.
 */
public class OpenApiConfigContextPathTest {
    private static final String SERVLET_PATH = "/xapi";

    @Test
    public void operationUrlContainsContextPathOnceInNonRootDeployment() throws Exception {
        assertThat(operationUrls("/xnat")).contains("/xnat/xapi/siteConfig")
                                          .doesNotContain("/xnat/xapi/xnat/siteConfig", "/xapi/siteConfig");
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
        context.register(OpenApiTestConfig.class);
        context.refresh();
        try {
            final MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
            final String json = mockMvc.perform(get(contextPath + SERVLET_PATH + "/v3/api-docs").contextPath(contextPath).servletPath(SERVLET_PATH).accept(MediaType.APPLICATION_JSON))
                                       .andReturn().getResponse().getContentAsString();
            final JsonNode spec = new ObjectMapper().readTree(json);
            final JsonNode servers = spec.get("servers");
            assertThat(servers).as("servers in %s", json).isNotNull().hasSize(1);
            final String serverUrl = servers.get(0).get("url").asText();
            final List<String> urls = new ArrayList<>();
            spec.get("paths").fieldNames().forEachRemaining(path -> urls.add((serverUrl + path).replaceAll("/+", "/")));
            return urls;
        } finally {
            context.close();
        }
    }

    @Configuration
    @EnableWebMvc
    @Import(OpenApiConfig.class)
    public static class OpenApiTestConfig {
        @Bean
        public XnatAppInfo xnatAppInfo() {
            final XnatAppInfo info = mock(XnatAppInfo.class);
            when(info.getVersion()).thenReturn("test");
            return info;
        }

        @Bean
        public MessageSource messageSource() {
            final StaticMessageSource messageSource = new StaticMessageSource();
            messageSource.setUseCodeAsDefaultMessage(true);
            return messageSource;
        }

        @Bean
        public SiteConfigStub siteConfigStub() {
            return new SiteConfigStub();
        }
    }

    @RestController
    @RequestMapping("/siteConfig")
    public static class SiteConfigStub {
        @GetMapping
        public String get() {
            return "ok";
        }
    }
}
