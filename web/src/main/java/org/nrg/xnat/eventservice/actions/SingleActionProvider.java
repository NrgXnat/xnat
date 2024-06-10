package org.nrg.xnat.eventservice.actions;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.model.Action;
import org.nrg.xnat.eventservice.model.ActionAttributeConfiguration;
import org.nrg.xnat.eventservice.services.EventServiceActionProvider;

import java.util.*;

@Slf4j
public abstract class SingleActionProvider implements  EventServiceActionProvider {

    public abstract Map<String, ActionAttributeConfiguration> getAttributes(String projectId, UserI user);

    @Override
    public String getName() { return this.getClass().getName(); }

    @Override
    public List<Action> getAllActions() {
        return new ArrayList<>(Arrays.asList(
                Action.builder().id(getName())
                      .actionKey(getActionKey() )
                      .displayName(getDisplayName())
                      .description(getDescription())
                      .provider(this)
                      .build()
        ));
    }

    @Override
    public List<Action> getActions(String projectId, List<String> xnatTypes, UserI user) {
        return new ArrayList<>(Collections.singletonList(Action.builder().id(getName())
                                                               .actionKey(getActionKey())
                                                               .displayName(getDisplayName())
                                                               .description(getDescription())
                                                               .attributes(getAttributes(projectId, user))
                                                               .provider(this)
                                                               .build()));
    }

    @Override
    public Boolean isActionAvailable(String actionKey, String projectId, UserI user) {
        final List<Action> actions = getActions(projectId, null, user);
        return actions != null && !actions.isEmpty() && actions.get(0).actionKey().contentEquals(actionKey);
    }

    public String getActionKey() {
        return actionIdToActionKey(this.getName());
    }

    @Override
    public String actionKeyToActionId(String actionKey) {
        return Splitter.on(':').splitToList(actionKey).get(0);
    }

    @Override
    public String actionIdToActionKey(String actionId) { return Joiner.on(':').join(this.getName(), actionId); }
}
