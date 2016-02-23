package org.nrg.xft.event;

import java.io.Serializable;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.springframework.stereotype.Service;

import reactor.bus.Event;
import reactor.bus.EventBus;

/**
 * The Class XftEventService.
 */
@Service
public class XftEventService {
	
	private static XftEventService _instance;
	
	public XftEventService() {
		_instance = this;
	}
	
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
	public void triggerEvent(String eventDesc,Serializable event) {
		
		eventBus.notify(eventDesc,Event.wrap(event));
		
	}
	
	/**
	 * Trigger event.
	 *
	 * @param event the event
	 */
	public void triggerEvent(Serializable event) {
		
		triggerEvent(event.getClass().getName(), event);
		
	}

}
