package org.nrg.xft.event.methods;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.cache.CacheManager;
import org.nrg.xft.event.XftItemEventI;
import org.springframework.stereotype.Component;

import static org.nrg.xft.XFTItem.XDAT_META_ELEMENT;
import static org.nrg.xft.event.XftItemEventI.CREATE;
import static org.nrg.xft.event.XftItemEventI.DELETE;
import static org.nrg.xft.event.XftItemEventI.UPDATE;

/**
 * Clears the XFT cache when an item is updated.
 */
@Component
@Slf4j
public class CacheManagerXftItemEventHandlerMethod extends AbstractXftItemEventHandlerMethod {
    private final CacheManager cache;

    public CacheManagerXftItemEventHandlerMethod(final CacheManager cache) {
        super(XftItemEventCriteria.getXsiTypeCriteria(XDAT_META_ELEMENT));
        this.cache = cache;
    }

    @Override
    protected boolean handleEventImpl(final XftItemEventI event) {
        final String xsiType = event.getXsiType();
        if (StringUtils.isBlank(xsiType)) {
            return false;
        }

        switch (event.getAction()) {
            case CREATE:
                if (event.getId() != null && event.getItem() != null) {
                    cache.put(xsiType, event.getId(), event.getItem());
                }
                break;
            case UPDATE:
                if (event.getId() != null && event.getItem() != null) {
                    cache.put(xsiType, event.getId(), event.getItem());
                } else if (event.getId() != null) {
                    cache.remove(xsiType, event.getId());
                } else {
                    cache.clearXsiType(xsiType);
                }
                break;
            case DELETE:
                if (event.getId() != null) {
                    cache.remove(xsiType, event.getId());
                } else {
                    cache.clearXsiType(xsiType);
                }
                break;
        }
        return true;
    }
}
