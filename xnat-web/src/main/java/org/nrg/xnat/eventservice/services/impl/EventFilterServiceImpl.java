package org.nrg.xnat.eventservice.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.nrg.xdat.model.*;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.JsonPathFilterNode;
import org.nrg.xnat.eventservice.model.xnat.*;
import org.nrg.xnat.eventservice.services.EventFilterService;
import org.nrg.xnat.eventservice.services.EventServiceComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public class EventFilterServiceImpl implements EventFilterService {
    private static final Logger log = LoggerFactory.getLogger(EventFilterService.class);

    private EventServiceComponentManager componentManager;
    private ObjectMapper mapper;

    @Autowired
    public EventFilterServiceImpl(EventServiceComponentManager componentManager,
                                  ObjectMapper mapper) {
        this.componentManager = componentManager;
        this.mapper = mapper;
    }

    @Override
    public Boolean matchFilter(Object eventPayloadObject, String jsonPathFilter) {
        return null;
    }

    @Override
    public String serializePayloadObject(Object eventPayloadObject, UserI user) {
        String jsonObject = null;
        try {
            XnatModelObject modelObject = componentManager.getModelObject(eventPayloadObject, user);
            if (modelObject != null && mapper.canSerialize(modelObject.getClass())) {
                // Serialize data object
                log.debug("Serializing event object as known Model Object.");
                jsonObject = mapper.writeValueAsString(modelObject);
            } else if (eventPayloadObject != null && mapper.canSerialize(eventPayloadObject.getClass())) {
                log.debug("Serializing event object as unknown object type.");
                jsonObject = mapper.writeValueAsString(eventPayloadObject);
            } else {
                log.debug("Could not serialize event object in: " + eventPayloadObject.toString());
            }
        } catch (JsonProcessingException e) {
            log.error("Exception attempting to serialize: {}", eventPayloadObject != null ? eventPayloadObject.getClass().getCanonicalName() : "null", e);
        }
        return jsonObject;
    }

    @Override
    public Map<String, JsonPathFilterNode> generateEventFilterNodes(@Nonnull EventServiceEvent eventServiceEvent) {
        Class payloadObjectClass = eventServiceEvent.getObjectClass();
        return generateEventFilterNodes(payloadObjectClass);
    }

    @Override
    public Map<String, JsonPathFilterNode> generateEventFilterNodes(@Nonnull Class eventPayloadClass) {
        Map<String, JsonPathFilterNode> filterNodes = new HashMap<>();

        try {
            // ** Build Sample XnatModelObject, if a supported type ** //
            XnatModelObject sampleObject = null;
            if (XnatImageassessordataI.class.isAssignableFrom(eventPayloadClass)) {
                sampleObject = Assessor.populateSample();
            } else if (XnatProjectdataI.class.isAssignableFrom(eventPayloadClass)) {
                sampleObject = Project.populateSample();
            } else if (XnatResourcecatalogI.class.isAssignableFrom(eventPayloadClass)) {
                sampleObject = Resource.populateSample();
            } else if (XnatImagescandataI.class.isAssignableFrom(eventPayloadClass)) {
                sampleObject = Scan.populateSample();
            } else if (XnatImagesessiondataI.class.isAssignableFrom(eventPayloadClass)) {
                sampleObject = Session.populateSample();
            } else if (XnatSubjectdataI.class.isAssignableFrom(eventPayloadClass)) {
                sampleObject = Subject.populateSample();
            } else {
                log.debug("Skipping Filter Node generation for {}. Event Filter Nodes can only be generated for supported XnatModelObject types.", eventPayloadClass.getCanonicalName());
            }
            if(sampleObject != null){
                JsonNode jsonNode = mapper.valueToTree(sampleObject);
                Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
                while(fields.hasNext()) {
                    Map.Entry<String, JsonNode> next = fields.next();
                    JsonNode value = next.getValue();
                    if (value.isValueNode()) {
                        JsonPathFilterNode filterNode = null;
                        String key = next.getKey();
                        switch (value.getNodeType()) {
                            case STRING:
                                filterNode = JsonPathFilterNode.builder()
                                                               .type("string")
                                                               .sampleValue(value.asText())
                                                               .build();
                                break;
                            case BOOLEAN:
                                filterNode = JsonPathFilterNode.builder()
                                                               .type("boolean")
                                                               .sampleValue(value.asText())
                                                               .build();
                                break;
                            case NUMBER:
                                filterNode = JsonPathFilterNode.builder()
                                                               .type("number")
                                                               .sampleValue(value.asText())
                                                               .build();
                                break;
                        }
                        if (!Strings.isNullOrEmpty(key) && filterNode != null) {
                            filterNodes.put(key, filterNode);
                        }
                    }
                }
            }
        } catch (Throwable e){
            log.error("Exception caught while attemping to generateEventFilterNodes. ", e.getMessage(), e);
        }
        return filterNodes;
    }

    @Override
    public String generateJsonPathFilter(Map<String, JsonPathFilterNode> filterNodeMap) {
        return null;
    }
}
