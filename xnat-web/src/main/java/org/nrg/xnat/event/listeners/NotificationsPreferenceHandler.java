/*
 * web: org.nrg.xnat.event.listeners.NotificationsPreferenceHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.prefs.events.AbstractPreferenceHandler;
import org.nrg.prefs.events.PreferenceHandlerMethod;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.PreferenceEvent;
import org.springframework.stereotype.Service;
import reactor.bus.EventBus;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationsPreferenceHandler extends AbstractPreferenceHandler<PreferenceEvent> {
	private String _toolId=NotificationsPreferences.NOTIFICATIONS_TOOL_ID;
	private final List<PreferenceHandlerMethod> _methods = new ArrayList<>();

	@Inject
	public NotificationsPreferenceHandler(final EventBus eventBus){
		super(eventBus);
	}


	@Override
	public String getToolId() {
		return _toolId;
	}

	@Override
	public void setToolId(String toolId) {
		_toolId=toolId;
	}

	@Override
	public List<PreferenceHandlerMethod> getMethods(){
		return _methods;
	}

	@Override
	public void addMethod(PreferenceHandlerMethod method){
		_methods.add(method);
	}

	private static final Log _log = LogFactory.getLog(NotificationsPreferenceHandler.class);


}
