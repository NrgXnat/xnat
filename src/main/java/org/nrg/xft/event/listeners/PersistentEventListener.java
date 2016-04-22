package org.nrg.xft.event.listeners;

import static reactor.bus.selector.Selectors.type;

import javax.inject.Inject;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.services.impl.hibernate.HibernatePersistentEventService;
import org.nrg.xft.event.AutomationEventImplementerI;
import org.nrg.xft.event.StructuredEventI;
import org.nrg.xft.event.persist.PersistentEventImplementerI;
import org.springframework.stereotype.Service;

import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

/**
 * The listener interface for receiving persistentEvent events.
 * The class that is interested in processing a persistentEvent
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addPersistentEventListener<code> method. When
 * the persistentEvent event occurs, that object's appropriate
 * method is invoked.
 *
 * @see PersistentEventEvent
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
			final HibernatePersistentEventService service = XDAT.getContextService().getBean(HibernatePersistentEventService.class);
			service.create(event.getData());
		}
	}

}
