/*
 * framework: org.nrg.framework.services.YamlObjectMapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class YamlObjectMapper extends ObjectMapper {
    public YamlObjectMapper() {
        super(new YAMLFactory());
        final DefaultIndenter      indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        final DefaultPrettyPrinter printer  = new DefaultPrettyPrinter();
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);
        setDefaultPrettyPrinter(printer);
        configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        registerModule(new Hibernate4Module());
    }
}
