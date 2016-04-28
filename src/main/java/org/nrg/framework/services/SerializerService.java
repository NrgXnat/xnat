package org.nrg.framework.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class SerializerService {
    public JsonNode deserializeJson(final String json) throws IOException {
        return _jsonObjectMapper.readTree(json);
    }

    public <T> T deserializeJson(final String json, final Class<T> clazz) throws IOException {
        return _jsonObjectMapper.readValue(json, clazz);
    }

    public <T> T deserializeJson(final String json, final TypeReference<T> typeRef) throws IOException {
        return _jsonObjectMapper.readValue(json, typeRef);
    }

    public Map<String, String> deserializeJsonToMapOfStrings(final String json) throws IOException {
        return _jsonObjectMapper.readValue(json, MAP_STRING_STRING_TYPE_REFERENCE);
    }

    public <T> String toJson(final T instance) throws IOException {
        return _jsonObjectMapper.writeValueAsString(instance);
    }

    public JsonNode deserializeYaml(final String yaml) throws IOException {
        return _yamlObjectMapper.readTree(yaml);
    }

    public <T> T deserializeYaml(final String yaml, Class<T> clazz) throws IOException {
        return _yamlObjectMapper.readValue(yaml, clazz);
    }

    public <T> T deserializeYaml(final String yaml, final TypeReference<T> typeRef) throws IOException {
        return _yamlObjectMapper.readValue(yaml, typeRef);
    }

    public <T> String toYaml(final T instance) throws IOException {
        return _yamlObjectMapper.writeValueAsString(instance);
    }

    private final static TypeReference<HashMap<String, String>> MAP_STRING_STRING_TYPE_REFERENCE = new TypeReference<HashMap<String, String>>() {};

    @Inject
    @Named("jsonObjectMapper")
    private ObjectMapper _jsonObjectMapper;

    @Inject
    @Named("yamlObjectMapper")
    private ObjectMapper _yamlObjectMapper;
}
