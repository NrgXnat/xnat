/*
 * org.nrg.xnat.helpers.prearchive.SessionDataModifierI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.prearchive;

import org.nrg.xnat.helpers.prearchive.PrearcDatabase.SyncFailedException;

import java.util.List;

/**
 * Modify the session data in some permanent store
 * @author aditya
 *
 */
public interface SessionDataModifierI extends PrearcSessionDataModifierI {
	void move(SessionData s , final String newProj) throws SyncFailedException;
	void moveScans(SessionData source, final String newSessionLabel, final String newSessionFolder, final List<String> scans) throws SyncFailedException;
	void delete(SessionData sd) throws SyncFailedException;
}
