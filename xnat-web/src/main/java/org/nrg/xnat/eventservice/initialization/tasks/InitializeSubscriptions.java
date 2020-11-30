package org.nrg.xnat.eventservice.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitializeSubscriptions extends AbstractInitializingTask {

    private final EventService eventService;

    @Autowired
    public InitializeSubscriptions(final EventService eventService){
        this.eventService = eventService;
    }

    @Override
    public String getTaskName() { return "Register active subscriptions with Reactor."; }

    @Override
    protected void callImpl() throws InitializingTaskException {
        if (eventService != null && eventService.getPrefs() != null && !eventService.getPrefs().getEnabled()){
            if(log.isDebugEnabled()){ log.debug("Preference: enabled == false. Skipping Event Service Subscription Initialization");  }
            return;
        }
        try {
            log.debug("Registering all active event subscriptions from SubscriptionEntity table to Reactor.EventBus.");
            eventService.reactivateAllSubscriptions();
        } catch (Exception e){
            log.error("Failed to initialized subscriptions.\n" + e.getMessage());
            throw new InitializingTaskException(InitializingTaskException.Level.Error, e.getMessage());
        }
    }
}
