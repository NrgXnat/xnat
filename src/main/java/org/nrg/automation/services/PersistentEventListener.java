package org.nrg.automation.services;

import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.framework.event.StructuredEventI;
import org.nrg.framework.event.persist.PersistentEventImplementerI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import javax.inject.Inject;

import static reactor.bus.selector.Selectors.type;

/**
 * The listener interface for receiving persistentEvent events.
 * The class that is interested in processing a persistentEvent
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addPersistentEventListener<code> method. When
 * the persistentEvent event occurs, that object's appropriate
 * method is invoked.
 */
@Service
public class PersistentEventListener implements Consumer<Event<PersistentEventImplementerI>> {

	//private static final Logger logger = LoggerFactory.getLogger(PersistentEventListener.class);
	
	/**
	 * Instantiates a new persistent event listener.
	 *
	 * @param eventBus the event bus
	 */
	@Inject public PersistentEventListener( EventBus eventBus ){
		eventBus.on(type(PersistentEventImplementerI.class), this);
	}
	
	/* (non-Javadoc)
	 * @see reactor.fn.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(Event<PersistentEventImplementerI> event) {
		// Let the automation event do the persisting if this is an AutomationEventImplementer
		//if (AutomationEventImplementerI.class.isAssignableFrom(event.getData().getClass())) {
		if (event.getData() instanceof AutomationEventImplementerI) {
			return;
		}
		StructuredEventI persistentEvent = event.getData();
		if (persistentEvent != null) {
			_persistentEventService.create(event.getData());
		}
	}

	@Autowired
	private PersistentEventService _persistentEventService;
}
