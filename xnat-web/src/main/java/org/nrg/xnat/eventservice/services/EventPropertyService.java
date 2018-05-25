package org.nrg.xnat.eventservice.services;

import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.EventPropertyNode;
import org.nrg.xnat.eventservice.model.JsonPathFilterNode;
import org.nrg.xnat.eventservice.model.Subscription;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public interface EventPropertyService {

    Boolean matchFilter(Object eventPayloadObject, String jsonPathFilter);

    String serializePayloadObject(Object eventPayloadObject, UserI user);

    Map<String, JsonPathFilterNode> generateEventFilterNodes(EventServiceEvent event);

    List<EventPropertyNode> generateEventPropertyKeys(EventServiceEvent event);

    Subscription resolveEventPropertyVariables(Subscription subscription, EventServiceEvent esEvent, UserI user, Long deliveryId);

    Map<String, JsonPathFilterNode> generateEventFilterNodes(@Nonnull Class eventPayloadClass);

    String generateJsonPathFilter(Map<String, JsonPathFilterNode> filterNodeMap);

}
