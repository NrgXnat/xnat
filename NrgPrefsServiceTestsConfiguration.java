package org.nrg.prefs.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.resolvers.SimplePrefsEntityResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.IOException;

@Configuration
@EnableTransactionManagement
@Import({DataSourceConfiguration.class, NrgPrefsServiceConfiguration.class})
@ComponentScan("org.nrg.prefs.tools")
public class NrgPrefsServiceTestsConfiguration {
    @Bean
    public PreferenceEntityResolver defaultResolver() throws IOException {
        return new SimplePrefsEntityResolver();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper() {{
            configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        }};
    }
}
