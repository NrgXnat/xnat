package org.nrg.xnat.services.messaging.archive;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.methods.AbstractXftItemEventHandlerMethod;
import org.nrg.xft.event.methods.XftItemEventCriteria;
import org.nrg.xnat.services.archive.impl.hibernate.ResourceMitigationHelper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@Getter(AccessLevel.PROTECTED)
@Accessors(prefix = "_")
@Slf4j
abstract public class AbstractResourceMitigationEventHandlerMethod extends AbstractXftItemEventHandlerMethod {
    public static final String PARAM_RESOURCE_ID                     = "resourceId";
    public static final String QUERY_FIND_IMAGE_SESSION_FOR_RESOURCE = "SELECT x.id "
                                                                       + "FROM xnat_imagesessiondata x "
                                                                       + "         LEFT JOIN xnat_imagescandata s ON x.id = s.image_session_id "
                                                                       + "         LEFT JOIN xnat_abstractresource ar ON s.xnat_imagescandata_id = ar.xnat_imagescandata_xnat_imagescandata_id "
                                                                       + "WHERE ar.xnat_abstractresource_id = :" + PARAM_RESOURCE_ID;

    protected static final String DELETED   = "deleted";
    protected static final String UPDATED   = "updated";
    protected static final String REQUESTER = "requester";

    private final SerializerService          _serializer;
    private final NamedParameterJdbcTemplate _template;

    protected AbstractResourceMitigationEventHandlerMethod(final SerializerService serializer, final NamedParameterJdbcTemplate template) {
        this(XftItemEventCriteria.builder()
                                 .xsiType(XnatResourcecatalog.SCHEMA_ELEMENT_NAME)
                                 .action(XftItemEvent.UPDATE)
                                 .predicate(event -> StringUtils.equals(ResourceMitigationHelper.FILE_MITIGATION, (String) event.getProperties().get(XftItemEventI.OPERATION)))
                                 .build(), serializer, template);
    }

    protected AbstractResourceMitigationEventHandlerMethod(final XftItemEventCriteria criteria, final SerializerService serializer, final NamedParameterJdbcTemplate template) {
        super(criteria);
        _serializer = serializer;
        _template   = template;
    }

    /**
     * Provides placeholder for method to handle the file mitigation event.
     *
     * @param properties The properties extracted from the {@link XftItemEventI event object}
     * @param event      The full event object
     *
     * @return Returns <pre>true</pre> if the event was handled properly, <pre>false</pre> otherwise.
     */
    abstract protected boolean handleMitigationEvent(final ResourceMitigationEventProperties properties, final XftItemEventI event);

    @Override
    protected boolean handleEventImpl(final XftItemEventI event) {
        final ResourceMitigationEventProperties properties = new ResourceMitigationEventProperties(event);
        log.debug("Handling resource mitigation event for resource {}/ID = {} requested by user {} with a total of {} moved or deleted files",
                  properties.getXsiType(), properties.getId(), properties.getUsername(), properties.getTotalFileCount());
        return handleMitigationEvent(properties, event);
    }

    protected JsonNode deserializeJson(final String json) throws IOException {
        return _serializer.deserializeJson(json);
    }

    protected <T> String toJson(final T instance) throws IOException {
        return _serializer.toJson(instance);
    }

    /**
     * This method flips the map of lists, with distinct objects in the lists as the key and a list of keys from the
     * original map that contained the key in its list. For example, given this structure:
     * <p>
     * <code>
     * {
     * "one": [1, 2, 3],
     * "two": [2, 3, 4],
     * "three": [3, 4, 5]
     * }
     * </code>
     * <p>
     * Inverting this would yield:
     * <p>
     * <code>
     * {
     * 1: ["one"],
     * 2: ["one", "two"],
     * 3: ["one", "two", "three"],
     * 4: ["two", "three"],
     * 5: ["three"]
     * }
     * </code>
     *
     * @param mapOfLists A map of objects with a list of objects from the original map keys.
     * @param <T>        The type of keys in the original map and lists in the inverted map
     * @param <V>        The type of lists in the original map and keys in the inverted map
     *
     * @return A map with distinct objects from all lists in the map as keys and lists of referencing keys from the original map.
     */
    protected static <T, V> Map<T, List<V>> invertMapOfLists(final Map<V, List<T>> mapOfLists) {
        return mapOfLists.entrySet().stream()
                         .flatMap(entry -> entry.getValue().stream()
                                                .map(value -> new AbstractMap.SimpleEntry<>(value, entry.getKey())))
                         .collect(Collectors.groupingBy(Map.Entry::getKey,
                                                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }
}
