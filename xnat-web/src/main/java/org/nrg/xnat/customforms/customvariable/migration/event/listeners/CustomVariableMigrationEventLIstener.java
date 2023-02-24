package org.nrg.xnat.customforms.customvariable.migration.event.listeners;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.customforms.customvariable.migration.event.CustomVariableMigrationEvent;
import org.nrg.xnat.event.EventListener;
import org.nrg.xnat.tracking.TrackEvent;
import reactor.bus.Event;
import reactor.fn.Consumer;

@EventListener
@Slf4j
public class CustomVariableMigrationEventLIstener implements Consumer<Event<CustomVariableMigrationEvent>> {

    @Override
    @TrackEvent
    public void accept(Event<CustomVariableMigrationEvent> busEvent) {
        log.debug(busEvent.getData().getMessage());
        log.debug(busEvent.getData().toString());
    }
}
