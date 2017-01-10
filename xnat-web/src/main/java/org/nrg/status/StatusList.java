/*
 * web: org.nrg.status.StatusList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.status;

import java.util.LinkedList;
import java.util.List;

public class StatusList implements StatusListenerI {
	private final static String LINE_SEPARATOR = System.getProperty("line.separator");
	private List<StatusMessage> messages=new LinkedList<StatusMessage>();
	
	@Override
	public void notify(StatusMessage message) {
		messages.add(message);
	}
	
	public List<StatusMessage> getMessages(){
		return messages;
	}

	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("StatusLog");
		if (!messages.isEmpty()) {
			synchronized(messages) {
				for (final StatusMessage m : messages) {
					sb.append(m.getSource() + " " + m.getStatus() + ": " + m.getMessage());
					sb.append(LINE_SEPARATOR);
				}
			}
		}
		return sb.toString();
	}
}
