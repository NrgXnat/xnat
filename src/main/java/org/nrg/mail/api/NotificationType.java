/**
 * NotificationType
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 23, 2011
 */
package org.nrg.mail.api;

import java.util.HashMap;
import java.util.Map;

// TODO: This should eventually be dynamically populated, e.g. from a database.
public enum NotificationType {
	Error ("Errors"), 
	Issue ("Issues"), 
	NewUser ("New Users"), 
	Update ("Updates");
	
	private NotificationType(String label) {
		_label = label;
	}
	
	public String id() {
		return _id;
	}

	public String label() {
		return _label;
	}
	
	public static Map<String, String> getIdLabelMap() {
		return _idLabelMap;
	}

	private final String _id = this.toString().toLowerCase();
	private final String _label;

	private static final Map<String, String> _idLabelMap;

	static {
		_idLabelMap = new HashMap<String, String>();

		for (NotificationType type : values()) {
			_idLabelMap.put(type.id(), type.label());
		}
	}
}
