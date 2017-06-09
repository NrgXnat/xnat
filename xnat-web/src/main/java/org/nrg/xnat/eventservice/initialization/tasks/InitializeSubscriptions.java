package org.nrg.xnat.eventservice.initialization.tasks;

import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InitializeSubscriptions extends AbstractInitializingTask {
    private static final Logger log = LoggerFactory.getLogger(InitializeSubscriptions.class);

    private final EventService eventService;

    @Autowired
    public InitializeSubscriptions(final EventService eventService){
        this.eventService = eventService;
    }

    @Override
    public String getTaskName() { return "Register active subscriptions with Reactor."; }

    @Override
    protected void callImpl() throws InitializingTaskException {
        try {
            log.debug("Registering all active event subscriptions from SubscriptionEntity table to Reactor.EventBus.");
            eventService.reactivateAllSubscriptions();
        } catch (Exception e){
            log.error("Failed to initialized subscriptions.\n" + e.getMessage());
            throw new InitializingTaskException(InitializingTaskException.Level.Error, e.getMessage());
        }
    }
}
