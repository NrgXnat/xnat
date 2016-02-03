/*
 * org.nrg.xnat.helpers.prearchive.SessionDataDelegate
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
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public abstract class SessionDataDelegate implements SessionDataProducerI, SessionDataModifierI {
	private SessionDataProducerI sp;
	private SessionDataModifierI sm;

	public SessionDataDelegate(SessionDataProducerI sp, SessionDataModifierI sm) {
		this.sp = sp;
		this.sm = sm;
	}
	
	public void setSp (SessionDataProducerI sp) {this.sp = sp;};
	public void setSm (SessionDataModifierI sm) {this.sm = sm;}
	public SessionDataProducerI getSp() {return sp;}
	public SessionDataModifierI getSm() {return sm;};
	public Collection<SessionData> get() throws IOException {
		return this.sp.get();
	}
	public void move(SessionData s, String newProj) throws SyncFailedException {
		this.sm.move(s, newProj);		
	}
	public void moveScans(SessionData source, final String newSessionLabel, final String newSessionFolder, final List<String> scans) throws SyncFailedException {
		this.sm.moveScans(source, newSessionLabel, newSessionFolder, scans);
	}
	public void delete(SessionData sd) throws SyncFailedException {
		this.sm.delete(sd);
	}
	public void setStatus(SessionData sd, PrearcStatus status) {
		this.sm.setStatus(sd, status);
	}
}
