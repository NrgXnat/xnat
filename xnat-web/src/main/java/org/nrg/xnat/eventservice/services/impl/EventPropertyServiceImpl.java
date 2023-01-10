package org.nrg.xnat.eventservice.services.impl;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.EventPropertyNode;
import org.nrg.xnat.eventservice.model.JsonPathFilterNode;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.model.xnat.Assessor;
import org.nrg.xnat.eventservice.model.xnat.Project;
import org.nrg.xnat.eventservice.model.xnat.Resource;
import org.nrg.xnat.eventservice.model.xnat.Scan;
import org.nrg.xnat.eventservice.model.xnat.Session;
import org.nrg.xnat.eventservice.model.xnat.Subject;
import org.nrg.xnat.eventservice.model.xnat.SubjectAssessor;
import org.nrg.xnat.eventservice.model.xnat.XnatModelObject;
import org.nrg.xnat.eventservice.services.EventPropertyService;
import org.nrg.xnat.eventservice.services.EventServiceComponentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.node.JsonNodeType.BOOLEAN;
import static com.fasterxml.jackson.databind.node.JsonNodeType.NUMBER;
import static com.fasterxml.jackson.databind.node.JsonNodeType.STRING;

@Slf4j
@Service
public class EventPropertyServiceImpl implements EventPropertyService {

    private EventServiceComponentManager componentManager;
    private ObjectMapper mapper;

    @Autowired
    public EventPropertyServiceImpl(EventServiceComponentManager componentManager,
                                    ObjectMapper mapper) {
        this.componentManager = componentManager;
        this.mapper = mapper;
        this.mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    }

    @Override
    public Boolean matchFilter(Object eventPayloadObject, String jsonPathFilter) {
        return null;
    }

//    @Override
//    public String serializePayloadObject(Object eventPayloadObject, UserI user) {
//        String jsonObject = null;
//        try {
//            XnatModelObject modelObject = componentManager.getModelObject(eventPayloadObject, user);
//            if (modelObject != null && mapper.canSerialize(modelObject.getClass())) {
//                // Serialize data object
//                log.debug("Serializing event object as known Model Object.");
//                jsonObject = mapper.writeValueAsString(modelObject);
//            } else if (eventPayloadObject != null && mapper.canDeserialize(mapper.getTypeFactory().constructType(eventPayloadObject.getClass()))) {
//                log.debug("Serializing event object as unknown object type.");
//                jsonObject = mapper.writeValueAsString(eventPayloadObject);
//            } else {
//                log.debug("Could not serialize event object in: " + eventPayloadObject.toString());
//            }
//        } catch (JsonProcessingException e) {
//            log.error("Exception attempting to serialize: {}", eventPayloadObject != null ? eventPayloadObject.getClass().getCanonicalName() : "null", e);
//        }
//        return jsonObject;
//    }

    @Override
    public List<EventPropertyNode> generateEventPropertyKeys(EventServiceEvent event){
        List eventProperties = new ArrayList<EventPropertyNode>();
        eventProperties.add(EventPropertyNode.withName("event-type", "string").withValue( event.getType()));
        eventProperties.add(EventPropertyNode.withName("event-display-name", "string").withValue(event.getDisplayName()));
        eventProperties.add(EventPropertyNode.withName("event-description", "string").withValue(event.getDescription()));

        List payloadProperties = generateEventPropertyKeys(event.getObjectClass());
        if(payloadProperties != null && !payloadProperties.isEmpty()){
            eventProperties.addAll(payloadProperties);
        }
        return eventProperties;
    }

    private List<EventPropertyNode> generateEventPropertyKeys(Class<?> payloadClass){
        List payloadProperties = new ArrayList<EventPropertyNode>();
        try {
            // if this is a class that we can convert to an XnatModelObject, generate property keys from that object instead
            Class modelObjectClass = componentManager.getModelObjectClass(payloadClass);
            if(modelObjectClass != null){
                payloadClass = modelObjectClass;
            }

            // If the class can be serialized by the ObjectMapper, use introspection to get a list of (String|Number|Boolean) properties
            if(payloadClass != null && mapper.canSerialize(payloadClass)){
                // Construct a Jackson JavaType for your class
                JavaType javaType = mapper.getTypeFactory().constructType(payloadClass);

                // Introspect the given type
                BeanDescription beanDescription = mapper.getSerializationConfig().introspect(javaType);

                // Find properties
                List<BeanPropertyDefinition> properties = beanDescription.findProperties();

                for(BeanPropertyDefinition beanProperty : properties){
                    Class<?> returnType = beanProperty.hasGetter() ? beanProperty.getGetter().getMember().getReturnType() : null;
                    if(returnType != null && String.class.isAssignableFrom(returnType)) {
                        payloadProperties.add(EventPropertyNode.withName(beanProperty.getName(), "string"));
                    }else if(returnType != null && Boolean.class.isAssignableFrom(returnType)){
                        payloadProperties.add(EventPropertyNode.withName(beanProperty.getName(), "boolean"));
                    }else if(returnType != null && Number.class.isAssignableFrom(returnType)){
                        payloadProperties.add(EventPropertyNode.withName(beanProperty.getName(), "number"));
                    }
                    else{
                        log.debug("Skipping property: " + beanProperty.getName() + " in  " + payloadClass.getName() + ", because it is not a string|boolean|number type.");
                    }
                }
            }else{
                log.debug("Cannot extract event payload property keys from :" + (payloadClass != null ? payloadClass.getName() : "NULL"));
            }
        } catch (Throwable e){
            log.error("Exception while attempting to extract property labels from payloadClass: " + payloadClass.getName(), e.getMessage());
        }


        return payloadProperties;
    }

    @Override
    public Subscription resolveEventPropertyVariables(final Subscription subscription, final EventServiceEvent esEvent,
                                                      final UserI user, final Long deliveryId) {
        Subscription resolvedSubscription = subscription;
        Map<String, String> resolvedAttributes = subscription.attributes();
        if(resolvedAttributes != null && !resolvedAttributes.isEmpty()) {
            // Check which, if any, of the attributes contain replacement matcher keys
            Pattern REGEX = Pattern.compile(".*(#\\S+#).*");
            List<Map.Entry<String, String>> resolvableAttributes = resolvedAttributes.entrySet().stream()
                                                                             .filter(entry -> REGEX.matcher(entry.getValue()).matches())
                                                                             .collect(Collectors.toList());
            if(resolvableAttributes != null && !resolvableAttributes.isEmpty()){
                // Get all of the event property nodes for this event
                List<EventPropertyNode> eventPropertyNodes = generateEventPropertyValues(esEvent, user);
                // Collect property nodes that are needed to replace found replacement keys
                for (Map.Entry<String, String> resolvableAttribute : resolvableAttributes) {
                    List<EventPropertyNode> matchingPropertyNodes = eventPropertyNodes.stream()
                            .filter(epn -> (epn.replacementKey() != null &&
                                    (!Strings.isNullOrEmpty(resolvableAttribute.getValue()) && StringUtils.containsIgnoreCase(resolvableAttribute.getValue(), epn.replacementKey()))))
                            .collect(Collectors.toList());
                    String attributeValue = resolvableAttribute.getValue();
                    log.debug("Resolving event property in {}", attributeValue);
                    for (EventPropertyNode node : matchingPropertyNodes) {
                        attributeValue = attributeValue.replaceAll("(?i)" + node.replacementKey(), node.value());
                    }
                    // TODO: There are properties in the key generator that are not in the value nodes ???
                    resolvedAttributes.put(resolvableAttribute.getKey(), attributeValue);
                }
            }
        }
        return resolvedSubscription;
    }

    public List<EventPropertyNode> generateEventPropertyValues(EventServiceEvent event, UserI user) {
        List<EventPropertyNode> eventProperties = new ArrayList<EventPropertyNode>();
        eventProperties.add(EventPropertyNode.withName("event-type", "string").withValue(event.getType()));
        eventProperties.add(EventPropertyNode.withName("event-display-name", "string").withValue(event.getDisplayName()));
        eventProperties.add(EventPropertyNode.withName("event-description", "string").withValue(event.getDescription()));

        List<EventPropertyNode> payloadProperties = generateEventPropertyValues(event.getObject(user), user);
        if (payloadProperties != null && !payloadProperties.isEmpty()) {
            eventProperties.addAll(payloadProperties);
        }
        return eventProperties;
    }

    private List<EventPropertyNode> generateEventPropertyValues(Object eventPayloadObject, UserI user) {
        if(eventPayloadObject == null){
            return null;
        }
        List properties = new ArrayList<EventPropertyNode>();
        JsonNode jsonNode = null;
        try {
            // If this is a known object type - convert it to model object, then serialize
            XnatModelObject modelObject = componentManager.getModelObject(eventPayloadObject, user);
            if (modelObject != null && mapper.canSerialize(modelObject.getClass())) {
                // Serialize data object
                log.debug("Mapping event object as known Model Object.");
                jsonNode = mapper.valueToTree(modelObject);
            // Otherwise, attempt to convert the original object
            } else if (eventPayloadObject != null && mapper.canSerialize(eventPayloadObject.getClass())) {
                log.debug("Mapping event object as unknown object type.");
                jsonNode = mapper.valueToTree(eventPayloadObject);
            } else {
                log.debug("Could not map event object in: " + eventPayloadObject.toString());
            }
        } catch (Throwable e) {
            log.error("Exception attempting to node-map: {}", eventPayloadObject != null ? eventPayloadObject.getClass().getCanonicalName() : "null", e);
        }

        if(jsonNode != null){
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> next = fields.next();
                JsonNode value = next.getValue();
                if (value.isValueNode()) {
                    String key = next.getKey();
                    if (!Strings.isNullOrEmpty(key) && (value.getNodeType() == STRING || value.getNodeType() == NUMBER || value.getNodeType() == BOOLEAN)) {
                        properties.add(EventPropertyNode.withName(key, value.getNodeType().toString().toLowerCase()).withValue(value.asText()));
                    }
                }
            }
        }
        return properties;
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
            } else if (XnatSubjectassessordataI.class.isAssignableFrom(eventPayloadClass)) {
                sampleObject = SubjectAssessor.populateSample();
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
            log.error("Exception caught while attempting to generateEventFilterNodes. ", e.getMessage(), e);
        }
        return filterNodes;
    }


    @Override
    public String generateJsonPathFilter(Map<String, JsonPathFilterNode> filterNodeMap) {
        String jsonPathFilter = "";
        for (Iterator<String> it = filterNodeMap.keySet().iterator(); it.hasNext();){
            String key = it.next();
            String jsonPath = generateJsonPath(key, filterNodeMap.get(key));
            if(!Strings.isNullOrEmpty(jsonPath)) {
                if(!jsonPathFilter.isEmpty()){
                    jsonPathFilter += " && ";
                }
                jsonPathFilter += jsonPath;
            }
        }
        return "$[?(" + jsonPathFilter + ")]";
    }

    // ** generate JSONPath filter string based on filterNode value and settings
    private String generateJsonPath(String nodeKey, JsonPathFilterNode filterNode){
        Boolean matchNullValue = filterNode.matchNullValue() != null ? filterNode.matchNullValue() : false;
        String regExMatcher = generateRegExMatcher(filterNode);
        if(!Strings.isNullOrEmpty(regExMatcher)) {
            return " @." + nodeKey + " =~ " + regExMatcher + " ";
        }
        return null;
    }

    private String generateRegExMatcher(JsonPathFilterNode filterNode) {
        String jsonPathRegEx = null;
        if(!Strings.isNullOrEmpty(filterNode.value())){
            final String IGNORE_CASE = "i";
            final String MATCH_CASE = "";
            final String MATCH_ANY = ".*";
            final String MATCH_LINE_START = "^";
            final String MATCH_LINE_END = "$";

            final String value = escapeForRegEx(filterNode.value());

            final Boolean matchCase = filterNode.matchCase() != null ? filterNode.matchCase() : false;
            final Boolean matchWord = filterNode.matchWord() != null ? filterNode.matchWord() : false;

            final String caseFlag = matchCase ? MATCH_CASE : IGNORE_CASE;
            final String wordStartFlag = matchWord ? MATCH_LINE_START : (MATCH_LINE_START + MATCH_ANY);
            final String wordEndFlag = matchWord ? MATCH_LINE_END : (MATCH_ANY + MATCH_LINE_END);

            jsonPathRegEx = "/" + wordStartFlag + value + wordEndFlag + "/" + caseFlag;
        }
        return jsonPathRegEx;
    }

    private String escapeForRegEx(String value){
        char[] regExChars = "<([{\\^-=$!|]})?*+.>/".toCharArray();
        if(StringUtils.containsAny(value, regExChars)) {
            for(char rec : regExChars){
                value = value.replace(String.valueOf(rec), "\\"+String.valueOf(rec));
            }
        }
        return value;
    }


}
