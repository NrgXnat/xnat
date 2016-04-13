package org.nrg.xnat.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jetbrains.annotations.NotNull;
import org.nrg.framework.services.SerializerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerializerConfig {

    @Bean
    @NotNull
    public PrettyPrinter prettyPrinter() {
        final DefaultIndenter      indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        final DefaultPrettyPrinter printer  = new DefaultPrettyPrinter();
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);
        return printer;
    }

    @Bean
    public ObjectMapper jsonObjectMapper() {
        final PrettyPrinter printer = prettyPrinter();
        final ObjectMapper  mapper  = new ObjectMapper().setDefaultPrettyPrinter(printer);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        return mapper;
    }

    @Bean
    public ObjectMapper yamlObjectMapper() {
        final PrettyPrinter printer = prettyPrinter();
        final ObjectMapper  mapper  = new ObjectMapper(new YAMLFactory()).setDefaultPrettyPrinter(printer);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        return mapper;
    }

    @Bean
    public SerializerService serializerService() {
        return new SerializerService();
    }
}
