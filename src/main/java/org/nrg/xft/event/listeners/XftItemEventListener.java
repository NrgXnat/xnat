/*
 * core: org.nrg.xft.event.listeners.XftItemEventListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.event.listeners;

import static reactor.bus.selector.Selectors.type;

import javax.inject.Inject;

import org.nrg.xft.event.XftItemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.nrg.xft.cache.CacheManager;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

/**
 * The listener interface for receiving xftItemEvent events.
 * The class that is interested in processing a xftItemEvent
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addXftItemEventListener</code> method. When
 * the xftItemEvent event occurs, that object's appropriate
 * method is invoked.
 */
@Service
public class XftItemEventListener implements Consumer<Event<XftItemEvent>> {

	/** The logger. */
	private static final Logger logger = LoggerFactory.getLogger(XftItemEventListener.class);
	
	/**
	 * Instantiates a new xft item event listener.
	 *
	 * @param eventBus the event bus
	 */
	@Inject public XftItemEventListener( EventBus eventBus ){
		eventBus.on(type(XftItemEvent.class), this);
	}
	
	/**
	 * {@inheritDoc}
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
