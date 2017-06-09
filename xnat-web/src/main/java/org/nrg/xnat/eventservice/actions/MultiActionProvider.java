package org.nrg.xnat.eventservice.actions;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.eventservice.services.EventServiceActionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class MultiActionProvider implements EventServiceActionProvider {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public String actionKeyToActionId(String actionKey) {

        List<String> keys = Splitter.on(':').splitToList(actionKey);
        if(keys.size()>1) {
            return keys.get(1);
        }else{
            log.error("ActionKey: " + actionKey + " does not have enough components. Cannot extract actionId.");
        }
        return null;
    }

    @Override
    public String actionIdToActionKey(String actionId) {
        return Joiner.on(':').join(this.getName(), actionId);
    }
}