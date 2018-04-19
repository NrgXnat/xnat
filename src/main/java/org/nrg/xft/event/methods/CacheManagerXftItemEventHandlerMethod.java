package org.nrg.xft.event.methods;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.cache.CacheManager;
import org.nrg.xft.event.XftItemEventI;
import org.springframework.stereotype.Component;

/**
 * Clears the XFT cache when an item is updated.
 */
@Component
@Slf4j
public class CacheManagerXftItemEventHandlerMethod extends AbstractXftItemEventHandlerMethod {
    public CacheManagerXftItemEventHandlerMethod() {
        super(XftItemEventCriteria.getXsiTypeCriteria("xdat:meta_element"));
    }

    @Override
    protected boolean handleEventImpl(final XftItemEventI event) {
        try {
            CacheManager.GetInstance().handleXftItemEvent(event);
            return true;
        } catch (Exception e) {
            log.error("Could not update cache after event", e);
            return false;
        }
    }
}
