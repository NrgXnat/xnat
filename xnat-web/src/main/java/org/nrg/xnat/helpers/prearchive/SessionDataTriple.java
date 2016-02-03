/*
 * org.nrg.xnat.helpers.prearchive.SessionDataTriple
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.prearchive;

import org.nrg.xnat.restlet.XNATApplication;
import org.nrg.xnat.restlet.actions.PrearcImporterA.PrearcSession;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class SessionDataTriple implements Serializable {
    private static final long serialVersionUID = 7764386535994779313L;

	private String folderName;
	private String timestamp;
	private String project;

	public SessionDataTriple() {
	}
	public String getFolderName() {
		return this.folderName;
	}
	public SessionDataTriple setFolderName(String name) {
		this.folderName = name;
		return this;
	}
	public SessionDataTriple setFolderName(Object o) {
		if (o != null) {
			this.setFolderName((String)o);
		}
		return this;
	}
	
	public String getTimestamp() {
		return this.timestamp;
	}
	public SessionDataTriple setTimestamp(String timestamp) {
		this.timestamp = timestamp;
		return this;
	}
	public SessionDataTriple setTimestamp(Object o) {
		if (o != null) {
			this.setTimestamp((String)o);
		}
		return this;
	}
	
	public String getProject() {
		return this.project;
	}
	public SessionDataTriple setProject(String project) {
		if (project != null) {
			this.project = project;
		}
		else {
			this.project = PrearcUtils.COMMON;
		}
		return this;
	}
	public SessionDataTriple setProject(Object o) {
		this.setProject((String)o);
		return this;
	}
	
	public static SessionDataTriple makeTriple (String sess, String timestamp, String proj) {
		return new SessionDataTriple().setFolderName(sess).setProject(proj).setTimestamp(timestamp);
	}
	
	public Map<String,String> toMap () {
		Map<String,String> ret = new HashMap<String,String>();
		ret.put("PROJECT_ID", this.getProject());
		ret.put("SESSION_TIMESTAMP", this.getTimestamp());
		ret.put("SESSION_LABEL", this.getFolderName());
		return ret;
	}
	
	public static SessionDataTriple fromMap (Map<String,String> m) {
		return new SessionDataTriple().setFolderName(m.get("SESSION_LABEL"))
		                              .setProject(m.get("PROJECT_ID"))
		                              .setTimestamp(m.get("SESSION_TIMESTAMP"));
	}
	public static SessionDataTriple fromFile (final String project, final File f) {
		return new SessionDataTriple().setFolderName(f.getName())
		                              .setProject(project)
		                              .setTimestamp(f.getParentFile().getName());
	}
	public static SessionDataTriple fromURI (final String uri) throws MalformedURLException {
		final PrearcUriParserUtils.SessionParser parser = new PrearcUriParserUtils.SessionParser(new PrearcUriParserUtils.UriParser(XNATApplication.PREARC_SESSION_URI));
		return SessionDataTriple.fromMap(parser.readUri(uri));
	}
	public static SessionDataTriple fromPrearcSession (final PrearcSession session) {
		return new SessionDataTriple().setFolderName(session.getFolderName())
		                              .setProject(session.getProject())
		                              .setTimestamp(session.getTimestamp());
	}
}