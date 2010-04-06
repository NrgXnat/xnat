// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.event;

import org.nrg.xft.XFTItem;

public interface EventListener {
	public void handleEvent(Event e) throws Exception;
}
