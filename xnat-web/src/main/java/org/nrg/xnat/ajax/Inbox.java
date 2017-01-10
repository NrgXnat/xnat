/*
 * web: org.nrg.xnat.ajax.Inbox
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.ajax;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sun.msv.verifier.jarv.TheFactoryImpl;
import org.apache.turbine.Turbine;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXWriter;
import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.VerifierHandler;
import org.nrg.PrearcImporter;
import org.nrg.status.StatusMessage;
import org.nrg.status.StatusMessage.Status;
import org.nrg.status.StatusQueue;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.PrearcImporterFactory;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class Inbox {
    private static final String LOCKNAME = ".importing";

    private static final Map<String,StatusQueue> active = Collections.synchronizedMap(new HashMap<String,StatusQueue>());
    private static final Map<String,StatusQueue> complete = Collections.synchronizedMap(new HashMap<String,StatusQueue>());

    // This is configurable from inside the webapp, so it might change from one instantiation to the next.
    private final File inboxRoot = new File(ArcSpecManager.GetInstance().getGlobalpaths().getFtppath());

    private final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(Inbox.class);

    public void startImport(final HttpServletRequest req, final HttpServletResponse response, final ServletConfig config) {
	final UserI user = XDAT.getUserDetails();
	final String login = user.getLogin();

	log.debug("received import request for user " + login);

	if (null == login) {
	    log.error("request received with no associated user");
	    try {
		response.sendError(HttpServletResponse.SC_CONFLICT, "no user in session: who are you?");
	    } catch (IOException ignore) {}
	    return;
	}

	final String project = req.getParameter("project");
	if (null == project || Prearchive.COMMON.equals(project)) {
	    sendEmptyListing(response, project);
	    return;
	}
	final File prearc = new File(ArcSpecManager.GetInstance().getPrearchivePathForProject(project));
	if (!prearc.isDirectory()) {
	    try {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid project " + project);
	    } catch (IOException ignore) {}
	    return;
	}
	
	final File userRoot = new File(inboxRoot, login);
	final File projectInbox = new File(userRoot, project);

	if (!projectInbox.exists()) {
	    sendEmptyListing(response, projectInbox.getPath());
	    return;
	}

	if (!projectInbox.isDirectory()) {
	    try {
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "user inbox not a directory");
	    } catch (IOException ignore) {}
	    return;
	}

	final FileOutputStream fos;
	final File lockFile = new File(projectInbox, LOCKNAME);
	try {
	    fos = new FileOutputStream(lockFile);
	} catch (FileNotFoundException e) {
	    try {
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "unable to create lock file " + lockFile);
	    } catch (IOException ignore) {}
	    return;
	}
	final FileChannel lockch = fos.getChannel();
	final FileLock lock;
	try {
	    lock = lockch.lock();
	} catch (IOException e) {
	    try {
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "unable to acquire inbox lock: " + e.getMessage());
		lockch.close();
		fos.close();
	    } catch (IOException ignore) {}
	    return;
	}

	final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
	final File tsdir = new File(prearc, formatter.format(Calendar.getInstance().getTime()));
	final String inboxPath;
	{
	    String path;
	    try {
		path = projectInbox.getCanonicalPath();
	    } catch (IOException e) {
		path = projectInbox.getAbsolutePath();
	    }
	    inboxPath = path;
	}

	final PrearcImporter importer = PrearcImporterFactory.getFactory().getPrearcImporter(project, tsdir, projectInbox, getPaths(req, projectInbox));
	final StatusQueue listener = new StatusQueue(); 
	importer.addStatusListener(listener);
	final StatusQueue prevListener = active.put(inboxPath, listener);
	if (null != prevListener) {
	    active.put(inboxPath, prevListener);	// keep the previous listener.
	    log.error("importer already active for " + projectInbox);
	    try {
		response.sendError(HttpServletResponse.SC_CONFLICT, "importer already active for " + projectInbox);
		lock.release();
		fos.close();
	    } catch (IOException ignore) {}
	    return;
	}

	importer.run();

	try {
	    lock.release();
	    fos.close();
	} catch (IOException ignore) {}

	active.remove(inboxPath);
	complete.put(inboxPath, listener);

	response.setContentType("text/xml");
	response.setHeader("Cache-Control", "no-cache");
	try {
	    writeStatus(response.getWriter(), listener);
	} catch (IOException e) {
	    log.error("response failed", e);
	}
    }

    public void monitorImport(final HttpServletRequest req, final HttpServletResponse response) {
	final UserI user = XDAT.getUserDetails();
	final String login = user.getLogin();

	log.debug("received monitor request for user " + login);

	if (null == login) {
	    log.error("request received with no associated user");
	    try {
		response.sendError(HttpServletResponse.SC_CONFLICT, "no user in session: who are you?");
	    } catch (IOException ignore) {}
	    return;
	}

	response.setContentType("text/xml");
	response.setHeader("Cache-Control", "no-cache");

	final String project = req.getParameter("project");
	final File prearc = new File(ArcSpecManager.GetInstance().getPrearchivePathForProject(project));
	if (!prearc.isDirectory()) {
	    try {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid project " + project);
	    } catch (IOException ignore) {}
	    return;
	}
	
	final File userRoot = new File(inboxRoot, login);
	final File projectInbox = new File(userRoot, project);
	final String inboxPath;
	{
	    String path;
	    try {
		path = projectInbox.getCanonicalPath();
	    } catch (IOException e) {
		path = projectInbox.getAbsolutePath();
	    }
	    inboxPath = path;
	}

	final StatusQueue listener = active.get(inboxPath);
	if (null == listener) {
	    final StatusQueue finished = complete.remove(inboxPath);
	    try {
		if (null == finished) { // no matching operation found, status unknown
		    response.getWriter().write("<status></status>");
		    log.error("no status queue found for " + projectInbox);
		} else {
		    writeStatus(response.getWriter(), finished);
		    assert null == finished.peek();
		}		    
	    } catch (IOException e) {
		log.debug("response failed", e);
	    }
	} else try {
	    writeStatus(response.getWriter(), listener);
	} catch (IOException e) {
	    log.debug("response failed", e);
	}
    }

    public void remove(final HttpServletRequest req, final HttpServletResponse response, final ServletConfig config) {
	final HttpSession session = req.getSession();
	final UserI user = XDAT.getUserDetails();
	final String login = user.getLogin();

	if (null == login) {
	    log.error("request received with no associated user");
	    try {
		response.sendError(HttpServletResponse.SC_CONFLICT, "no user in session: who are you?");
	    } catch (IOException ignore) {}
	    return;
	}

	final String project = req.getParameter("project");
	final File prearc = new File(ArcSpecManager.GetInstance().getPrearchivePathForProject(project));
	if (!prearc.isDirectory()) {
	    try {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid project " + project);
	    } catch (IOException ignore) {}
	    return;
	}
	
	final File userRoot = new File(inboxRoot, login);
	final File projectInbox = new File(userRoot, project);

	for (final File f : getPaths(req, projectInbox)) {
	    deleteTree(f);
	}

	// Response is a revised listing.
	list(req, response);
    }

    private void sendEmptyListing(final HttpServletResponse response, final String object) {
	response.setContentType("text/xml");
	response.setHeader("Cache-Control", "no-cache");
	try {
	    final PrintWriter writer = response.getWriter();
	    writer.write("<status>");
	    writer.write("<processing object=\"");
	    writer.write(object);
	    writer.write("\">(empty)</processing>");
	    writer.write("<completed object=\"");
	    writer.write(object);
	    writer.write("\"></completed>");
	    writer.write("</status>");
	} catch (IOException e) {
	    log.error("response failed", e);
	}
    }

    private static boolean isImporting(final File dir) throws IOException {
	final FileOutputStream fos = new FileOutputStream(new File(dir, LOCKNAME));
	final FileChannel fc = fos.getChannel();
	FileLock lock;
	try {
	    lock = fc.tryLock();
	} catch (OverlappingFileLockException e) {
	    return true;
	}
	try { fc.close(); } catch (IOException ignore) {}
	try { fos.close(); } catch (IOException ignore) {}
	return (null == lock);
    }

    /**
     * Deletes an entire file tree.
     * @param f Root of the file tree to be deleted.
     * @return true if the operation was successful.
     */
    private static boolean deleteTree(final File f) {
	if (f.delete()) {
	    return true;
	}
	if (f.isDirectory()) {
	    for (final File sf : f.listFiles()) try {
		if (!sf.delete() && sf.getAbsolutePath().equals(sf.getCanonicalPath())) {
		    deleteTree(sf);
		}
	    } catch (IOException ignore) {}
	    return f.delete();
	} else {
	    return false;
	}
    }

    private final class FileSummary {
	final long size;	// length in kb
	FileSummary(final File f) {
	    if (f.isDirectory()) {
		long size = 0;		
		final List<File> subdirs = new LinkedList<File>();
		subdirs.add(f);
		while (!subdirs.isEmpty()) {
		    final File dir = subdirs.remove(0);
		    for (final File file : dir.listFiles()) {
			if (file.isDirectory()) {
			    subdirs.add(file);
			} else {
			    size += file.length()/1024;
			}
		    }
		}
		this.size = size;
	    } else {
		this.size = f.length()/1024;
	    }
	}
    }


    public void list(final HttpServletRequest req, final HttpServletResponse response) {
	final HttpSession session = req.getSession();
	final UserI user = XDAT.getUserDetails();
	final String login = user.getLogin();

	if (null == login) {
	    log.error("request received with no associated user");
	    try {
		response.sendError(HttpServletResponse.SC_CONFLICT, "no user in session: who are you?");
	    } catch (IOException ignore) {}
	    return;
	}

	final String project = req.getParameter("project");
	final File prearc = new File(ArcSpecManager.GetInstance().getPrearchivePathForProject(project));
	if (!prearc.isDirectory()) {
	    try {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid project " + project);
	    } catch (IOException ignore) {}
	    return;
	}
	
	log.debug("received FTP inbox list request for user " + login + " project " + project);

	final File userRoot = new File(inboxRoot, login);
	final File projectInbox = new File(userRoot, project);

	final String path = req.getParameter("folder");
	final File folder = (path != null && path.length() > 0) ? new File(projectInbox, path) : projectInbox;

	final Document document = org.dom4j.DocumentHelper.createDocument();
	final Element root = document.addElement("Directory");

	try {
	    if (isImporting(projectInbox)) {
		root.addAttribute("locked", "true");
	    }
	} catch (IOException ignore) {}	// Can't test lock?  Plow on ahead and return the listing anyway.


	if (null != path) {	// verify that nothing funny happened resolving the path
	    final int lastf = path.lastIndexOf('/');
	    final int lastb = path.lastIndexOf('\\');
	    final int lastsep = lastf > lastb ? lastf : lastb;
	    if (lastsep > -1 && ! folder.getName().equals(path.substring(lastsep)))
		log.warn("requested inbox path " + path + " does not match retrieved " + folder);
	    root.addAttribute("name", path);
	} else {
	    root.addAttribute("name", "");
	}

	if (folder.exists()) {	// nonexistent project folder is fine -- just act like it's empty
	    if (!folder.isDirectory()) {
		log.error("User inbox folder " + folder + " is not a directory");
		try {
		    response.sendError(HttpServletResponse.SC_CONFLICT, "no such directory: " + path);
		} catch (IOException ignore) {}
		return;
	    }

	    for (final File file : folder.listFiles()) {
		if (LOCKNAME.equals(file.getName()))
		    continue;

		final FileSummary summary = new FileSummary(file);
		final Element fe = root.addElement("file");
		fe.addAttribute("size", Long.toString(summary.size));
		fe.addAttribute("isDirectory", Boolean.toString(file.isDirectory()));
		fe.addText(file.getName());
	    }
	}

	if (log.isDebugEnabled()) {
	    log.debug("Directory message validation " + (validate("Directory", document) ? "successful" : "failed"));
	}
	
	response.setContentType("text/xml");
	response.setHeader("Cache-Control", "no-cache");
	try {
	    document.write(response.getWriter());
	} catch (IOException e) {
	    log.warn("response failed: " + e.getMessage());
	}
    }

    /**
     * Builds File objects for the requested files.
     */
    private static File[] getPaths(final HttpServletRequest req, File inbox) {
	final String[] paths = req.getParameterValues("path");
	if (null == paths || 0 == paths.length) {
	    return null;
	} else {
	    final Set<File> fs = new HashSet<File>();
	    for (final String path : paths) {
		fs.add(new File(inbox, path));
	    }
	    return fs.toArray(new File[0]);
	}
    }


    private static final Map<Status,String> statusTags = new HashMap<Status,String>();
    static {
	statusTags.put(Status.PROCESSING, "processing");
	statusTags.put(Status.WARNING, "warning");
	statusTags.put(Status.FAILED, "failed");
	statusTags.put(Status.COMPLETED, "completed");
    }

    private String toXML(final StatusMessage m) {
	final StringBuilder sb = new StringBuilder("<");
	final String tag = statusTags.get(m.getStatus());
	sb.append(tag);
	sb.append(" object=\"");
	sb.append(m.getSource());
	sb.append("\">");
	sb.append(m.getMessage());
	sb.append("</");
	sb.append(tag);
	sb.append(">");
	return sb.toString();
    }


    private void writeStatus(final Writer writer, final StatusQueue queue) throws IOException{
	writer.write("<status>");
	synchronized(queue) {
	    for (StatusMessage m = queue.poll(); null != m; m = queue.poll()) {
		writer.write(toXML(m));
	    }
	}
	writer.write("</status>");
    }


    final String XSD_SUFFIX = ".xsd";

    private boolean validate(final String schemaName, final Document document) {
	final ServletContext context = Turbine.getTurbineServletContext();
	final File schemaFile = new File(context.getRealPath("schemas/ws/" + schemaName + XSD_SUFFIX));
	final VerifierFactory factory = new TheFactoryImpl();
	try {
	    final Schema schema = factory.compileSchema(schemaFile.getPath());
	    final Verifier verifier = schema.newVerifier();
	    verifier.setErrorHandler(new ErrorHandler() {
		public void error(SAXParseException e) { log.error(e); }
		public void fatalError(SAXParseException e) { log.fatal(e); }
		public void warning(SAXParseException e) { log.warn(e); }
	    });

	    VerifierHandler handler = verifier.getVerifierHandler();
	    SAXWriter writer = new SAXWriter(handler);
	    writer.write(document);
	    return handler.isValid();
	} catch (VerifierConfigurationException e) {
	    log.error(e);
	    return false;
	} catch (SAXException e) {
	    log.error(e);
	    return false;
	} catch (IOException e) {
	    log.error(e);
	    return false;
	}
    }

}
