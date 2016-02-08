package org.nrg.xft.event.listeners;

import static reactor.bus.selector.Selectors.$;

import javax.inject.Inject;

import org.nrg.xft.event.XftItemEvent;
import org.springframework.stereotype.Service;
import org.apache.log4j.Logger;
import org.nrg.xft.cache.CacheManager;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

/**
 * The listener interface for receiving xftItemEvent events.
 * The class that is interested in processing a xftItemEvent
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addXftItemEventListener<code> method. When
 * the xftItemEvent event occurs, that object's appropriate
 * method is invoked.
 *
 * @see XftItemEventEvent
 */
@Service
public class XftItemEventListener implements Consumer<Event<XftItemEvent>> {

	/** The logger. */
	static Logger logger = Logger.getLogger(XftItemEventListener.class);
	
	/**
	 * Instantiates a new xft item event listener.
	 *
	 * @param eventBus the event bus
	 */
	@Inject public XftItemEventListener( EventBus eventBus ){
		eventBus.on($(XftItemEvent.class.getName()), this);
	}
	
	/* (non-Javadoc)
	 * @see reactor.fn.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(Event<XftItemEvent> event) {
		
		try {
			CacheManager.GetInstance().handleXftItemEvent(event.getData());
		} catch (Exception e) {
			logger.error("ERROR:  Could not update cache after event", e);
		}
		
	}

}
