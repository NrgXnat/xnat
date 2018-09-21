package org.nrg.xnat.eventservice.actions;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.eventservice.services.EventServiceActionProvider;

import java.util.List;

@Slf4j
public abstract class MultiActionProvider implements EventServiceActionProvider {

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