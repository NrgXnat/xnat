package org.nrg.xft.event;

import java.io.Serializable;

import org.nrg.xdat.XDAT;

import reactor.bus.Event;
import reactor.bus.EventBus;

/**
 * The Class ReactorEventUtils.
 */
public class ReactorEventUtils {
	
	/** The event bus. */
	private static EventBus eventBus = XDAT.getContextService().getBean(EventBus.class);
	
	/**
	 * Trigger event.
	 *
	 * @param eventDesc the event desc
	 * @param event the event
	 */
	public static void triggerEvent(String eventDesc,Serializable event) {
		
		eventBus.notify(eventDesc,Event.wrap(event));
		
	}
	
	/**
	 * Trigger event.
	 *
	 * @param event the event
	 */
	public static void triggerEvent(Serializable event) {
		
		triggerEvent(event.getClass().getName(), event);
		
	}
	
}
