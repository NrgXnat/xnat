package org.nrg.xnat.event;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.services.NrgEventService;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.services.DataTypeAwareEventService;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.XftItemEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.EventBus;

import java.util.Arrays;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Service
@Getter(PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public class XnatEventService extends NrgEventService implements DataTypeAwareEventService {
    @Autowired
    public XnatEventService(final EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void triggerXftItemEvent(final String xsiType, final String id, final String action) {
        triggerEvent(XftItemEvent.builder().xsiType(xsiType).id(id).action(action).build());
    }

    @Override
    public void triggerXftItemEvent(final XFTItem item, final String action) {
        triggerEvent(XftItemEvent.builder().item(item).action(action).build());
    }

    @Override
    public void triggerXftItemEvent(final BaseElement baseElement, final String action) {
        triggerEvent(XftItemEvent.builder().element(baseElement).action(action).build());
    }

    @Override
    public void triggerXftItemEvent(final BaseElement[] baseElements, final String action) {
        triggerEvent(XftItemEvent.builder().elements(Arrays.asList(baseElements)).action(action).build());
    }

    @Override
    public void triggerXftItemEvent(final String xsiType, final String id, final String action, final Map<String, ?> properties) {
        triggerEvent(XftItemEvent.builder().xsiType(xsiType).id(id).action(action).properties(properties).build());
    }

    @Override
    public void triggerUserIEvent(final String username, final String action, final Map<String, ?> properties) {
        triggerEvent(XftItemEvent.builder().xsiType(XdatUser.SCHEMA_ELEMENT_NAME).id(username).action(action).properties(properties).build());
    }

    @Override
    public void triggerXftItemEvent(final XFTItem item, final String action, final Map<String, ?> properties) {
        triggerEvent(XftItemEvent.builder().item(item).action(action).properties(properties).build());
    }

    @Override
    public void triggerXftItemEvent(final BaseElement baseElement, final String action, final Map<String, ?> properties) {
        triggerEvent(XftItemEvent.builder().element(baseElement).action(action).properties(properties).build());
    }
}
