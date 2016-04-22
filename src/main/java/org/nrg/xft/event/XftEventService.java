package org.nrg.xft.event;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xft.event.persist.PersistentEventImplementerI;
import org.springframework.stereotype.Service;

import reactor.bus.Bus;
import reactor.bus.Event;
import reactor.bus.EventBus;

/**
 * The Class XftEventService.
 */
@Service
public class XftEventService {
	
	/** The _instance. */
	private static XftEventService _instance;
	
	/**
	 * Instantiates a new xft event service.
	 */
	public XftEventService() {
		_instance = this;
	}
	
	/**
	 * Gets the service.
	 *
	 * @return the service
	 */
	public static XftEventService getService() {
	    if (_instance == null) {
	    	_instance = XDAT.getContextService().getBean(XftEventService.class);
	    }
	    return _instance;
	}
	
	/** The logger. */
	static Logger logger = Logger.getLogger(XftEventService.class);
	
	/** The event bus. */
	@Inject private EventBus eventBus;
	
	/**
	 * Trigger event.
	 *
	 * @param eventDesc the event desc
	 * @param event the event
	 */
	public void triggerEvent(String eventDesc,EventI event) {
		eventBus.notify(eventDesc,Event.wrap(event));
		// For persistent events, we'll also notify by class name so the persistent event listener responds
		if (event instanceof PersistentEventImplementerI || event instanceof AutomationEventImplementerI) {
			eventBus.notify(event.getClass(),Event.wrap(event));
		}
	}
	
	/**
	 * Trigger event.
	 *
	 * @param event the event
	 */
	public void triggerEvent(EventI event) {
		eventBus.notify(event.getClass(),Event.wrap(event));
	}
	
	/**
	 * Trigger event.
	 *
	 * @param event the event
	 * @param replyTo the reply to
	 */
	public void triggerEvent(EventI event, Object replyTo) {
		if (replyTo == null) {
			throw new IllegalArgumentException("Event replyTo object cannot be null");
		}
		eventBus.notify(event.getClass(),Event.wrap(event, replyTo));
	}
	
	/**
	 * Trigger event.
	 *
	 * @param eventDesc the event desc
	 * @param event the event
	 */
	@SuppressWarnings("rawtypes")
	public void triggerEvent(String eventDesc,Event event) {
		eventBus.notify(eventDesc,event);
		// For persistent events, we'll also notify by class name so the persistent event listener responds
		if (event instanceof PersistentEventImplementerI || event instanceof AutomationEventImplementerI) {
			eventBus.notify(event.getClass(),Event.wrap(event));
		}
	}
	
	/**
	 * Trigger event.
	 *
	 * @param event the event
	 */
	@SuppressWarnings("rawtypes")
	public void triggerEvent(Event event) {
		eventBus.notify(event.getClass(),event);
	}
	
	/**
	 * Send event.
	 *
	 * @param event the event
	 */
	@SuppressWarnings("rawtypes")
	public void sendEvent(Event event) {
		if (event.getReplyTo() == null) {
			throw new IllegalArgumentException("Event replyTo object cannot be null");
		}
		eventBus.send(event.getClass(),event);
	}
	
	/**
	 * Send event.
	 *
	 * @param eventDesc the event desc
	 * @param event the event
	 * @param replyTo the reply to
	 */
	@SuppressWarnings("rawtypes")
	public void sendEvent(String eventDesc, EventI event, Bus replyTo) {
		eventBus.send(eventDesc, Event.wrap(event), replyTo);
		// For persistent events, we'll also notify by class name so the persistent event listener responds
		if (event instanceof PersistentEventImplementerI || event instanceof AutomationEventImplementerI) {
			eventBus.send(event.getClass(), Event.wrap(event), replyTo);
		}
	}
	
	/**
	 * Send event.
	 *
	 * @param event the event
	 * @param replyTo the reply to
	 */
	@SuppressWarnings("rawtypes")
	public void sendEvent(EventI event, Bus replyTo) {
		eventBus.send(event.getClass(),Event.wrap(event), replyTo);
	}

}
