/*
 * web: org.nrg.xnat.ajax.Prearchive
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.ajax;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.ArcProjectI;
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.archive.PrearcImporterFactory;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.xml.sax.SAXException;


public final class Prearchive {
	private static final String XML_SUFFIX = ".xml";

	private static final FileFilter isDirectoryFilter = new FileFilter() {
		public boolean accept(final File f) { return f.isDirectory(); }
	};

	private static final FileFilter isXMLFileFilter = new FileFilter() {
		public boolean accept(final File f) { return f.isFile() && f.getName().matches(".*\\.[xX][mM][lL]\\z"); }
	};

	private static final Comparator<File> lastModifiedComparator = new Comparator<File>() {
		public int compare(final File f1, final File f2) {
			return (int)(f1.lastModified() - f2.lastModified());
		}
	};

	private static final Comparator<File> lastModifiedContentComparator = new Comparator<File>() {
		private long contentLastModified(final File f) {
			final File[] content = f.listFiles();
			if (0 == content.length) {
				return f.lastModified();
			} else {
				Arrays.sort(content, lastModifiedComparator);
				return content[0].lastModified();
			}
		}

		public int compare(final File f1, final File f2) {
			return (int)(contentLastModified(f1) - contentLastModified(f2));
		}
	};

	private static final Pattern timestampPattern = Pattern.compile("[0-9]{8}_[0-9]{6}");

	// Any IDs matching those listed here are not included when we look for
	// alternate IDs.
	private static Set<String> ignoreIDs = new HashSet<String>();
	static {
		ignoreIDs.add("");
		ignoreIDs.add("null");
	}

	public enum PrearcStatus {
		RECEIVING, BUILDING, READY, ARCHIVING, ERROR
	};


	public static final String COMMON = "Unassigned";

	private static final String ROLE_SITE_ADMIN = "Administrator";

	private final Logger log = Logger.getLogger(Prearchive.class);
	private final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
	private final SimpleDateFormat XS_DATETIME_FORMAT = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");

	private final Map<String,Map<String,Boolean>> createAllowedTypes = new HashMap<String,Map<String,Boolean>>();

	public Prearchive() {
		createAllowedTypes.put(COMMON, null);
	}


	private boolean canCreate(final UserI user, final String project, final String type) {
		try {
			if (Roles.isSiteAdmin(user)) {
				return true;
			}
		} catch (Exception e) {
			log.error("unable to check user for site admin role", e);
		}
		if (!createAllowedTypes.containsKey(project)) {
			createAllowedTypes.put(project, new HashMap<String,Boolean>());
		}

		final Map<String,Boolean> projectTypes=createAllowedTypes.get(project);
		if (!projectTypes.containsKey(type)) {
			try {
				if(Permissions.canCreate(user,type + "/project", project)){
					projectTypes.put(type, Boolean.TRUE);
				}else{
					projectTypes.put(type, Boolean.FALSE);
				}
			} catch (Exception e) {
				log.error("Failed to verify security permissions for " + project + "-" + type, e);
				projectTypes.put(type, Boolean.FALSE);
			}
		}

		return projectTypes.get(type).booleanValue();
	}

	/**
	 * Handles a "prearchives" request by returning a list of prearchives, with session counts.
	 */
	@SuppressWarnings("unchecked")
	public void prearchives(final HttpServletRequest req, final HttpServletResponse response, final ServletConfig config) {
		final HttpSession session = req.getSession();
		final UserI user = XDAT.getUserDetails();
		final String login = user.getLogin();

		log.debug("received prearchive list request for user " + login);

		if (null == login) {
			log.error("request received with no associated user");
			try {
				response.sendError(HttpServletResponse.SC_CONFLICT, "no user in session: who are you?");
			} catch (IOException ignore) {}
			return;
		}

		final Document document = org.dom4j.DocumentHelper.createDocument();
		final Element root = document.addElement("Prearchives");

		// Determine which prearchives user should see
		final Map<String,File> prearcs = new LinkedHashMap<String,File>();
		final List<List> projects = UserHelper.getUserHelperService(user).getQueryResults("xnat:projectData/ID", "xnat:projectData");
		for (final List<String> row : projects) {
			final String id = row.get(0);
			if (prearcs.containsKey(id))
				continue;
			try {
				if (Permissions.can(user,"xnat:mrSessionData/project", id, SecurityManager.CREATE)) {
					prearcs.put(id, new File(ArcSpecManager.GetInstance().getPrearchivePathForProject(id)));
				}
			} catch (Exception e) {
				log.error("Exception caught testing prearchive access", e);
			}
		}

		// Only site admin sees the common prearchive
		try {
			if (Roles.isSiteAdmin(user)) {
				final String commonPath = ArcSpecManager.GetInstance().getGlobalPrearchivePath();
				if (null == commonPath) {
					log.error("null global prearchive path");
				}
				prearcs.put(COMMON, new File(commonPath));
			}
		} catch (Exception e) {
			log.error("unable to check user for site administrator role", e);
		}

		for (final Map.Entry<String,File> e : prearcs.entrySet()) {
			final String prearc = e.getKey();

			final Element elem = root.addElement("prearc");
			elem.setText(prearc);

			final File dir = e.getValue();
			dir.mkdirs();
			if (!dir.isDirectory()) {
				log.error("prearchive " + prearc + " path " + dir + " is not a directory");
				try {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, prearc + " prearchive is improperly configured");
				} catch (IOException ignore) {}
				return;
			}

			int sessionCount = 0;
			final File[] tsdirs = dir.listFiles(isDirectoryFilter);
			Arrays.sort(tsdirs, lastModifiedContentComparator);
			for (final File tsdir : tsdirs) {
				sessionCount += tsdir.listFiles(isXMLFileFilter).length;
			}
			elem.addAttribute("size", String.valueOf(sessionCount));

			if (!COMMON.equals(prearc)) {
				elem.addAttribute("allowMatch", "true");
			}
		}

		response.setContentType("text/xml");
		response.setHeader("Cache-Control", "no-cache");
		try {
			document.write(response.getWriter());
		} catch (IOException e) {
			log.warn("response failed", e);
		}
	}


	/**
	 * Checks for obvious problems with a session XML: existence, lock, permissions.
	 * @param sessionDir Directory holding the session
	 * @return
	 */
	private PrearcStatus checkSessionStatus(final File sessionDir) {
		final File sessionXML = new File(sessionDir.getPath() + XML_SUFFIX);
		if (!sessionXML.exists()) {
			return PrearcStatus.RECEIVING;
		}
		if (!sessionXML.isFile()) {
			log.error(sessionDir.toString() + " exists, but is not a file");
			return PrearcStatus.ERROR;
		}
		final FileOutputStream fos;
		try {
			fos = new FileOutputStream(sessionXML, true);
		} catch (FileNotFoundException e) {
			log.error("File not found (but passed .exists()!)", e);
			return PrearcStatus.ERROR;
		}
		try {
			final FileLock lock = fos.getChannel().tryLock();
			if (null == lock)
				return PrearcStatus.BUILDING;
		} catch (IOException e) {
			log.error("Unable to check lock on session " + sessionDir, e);
			return PrearcStatus.ERROR;
		} finally {
			try { fos.close(); } catch (IOException ignore) {}
		}

		if (!sessionXML.canRead()) {
			log.error("able to obtain lock, but cannot read " + sessionDir);
			return PrearcStatus.ERROR;
		}

		return null;
	}

	/**
	 * Exception-free method for getting a canonical file representation.
	 * If f.getCanonicalFile() fails, returns f.getAbsoluteFile() instead.
	 * @param f file for which we want canonical representation
	 * @return Canonical representation of the given file
	 */
	private static final File getCanonical(final File f) {
		try {
			return f.getCanonicalFile();
		} catch (IOException e) {
			Logger.getLogger(Prearchive.class).warn("unable to get canonical path for " + f, e);
			return f.getAbsoluteFile();
		}
	}

	/**
	 * Gets a canonical file representation for the given pathname.
	 * @param path pathname
	 * @return Canonical representation of the named file
	 */
	private static final File getCanonical(final String path) {
		return getCanonical(new File(path));
	}


	/**
	 * Handles a "sessions" request by returning a list of sessions in the specified prearchive.
	 */
	public void sessions(final HttpServletRequest req, final HttpServletResponse response, final ServletConfig config) {
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

		final String name = (String) req.getParameter("prearc");
		log.debug("received session list request for user " + login + " prearchive " + name);

		if (!canCreate(user, name, null)) {
			log.info("Unauthorized user " + login + " tried to access prearchive " + name);
			try {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			} catch (IOException ignore) {}
			return;
		}

		final ArcArchivespecification arcspec = ArcSpecManager.GetInstance();
		final File prearc = getPrearcRoot(name);

		if (null == prearc || !prearc.isDirectory()) {
			log.warn("requested list for invalid prearchive " + name);
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (IOException ignore) {}
			return;   
		}

		if (!prearc.canRead()) {
			log.error("no access to prearchive directory " + prearc);
			try {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "prearchive " + name + " cannot be read");
			} catch (IOException ignore) {}
			return;
		}

		final Document document = org.dom4j.DocumentHelper.createDocument();
		final Element root = document.addElement("Sessions");
		root.addAttribute("name", name);

		// Extract the sessions.  This code depends on the following prearchive structure:
		//   prearc/
		//   +--- TimeStamp1/
		//   |    +--- SessionDir1/
		//   |    +--- SessionDir2/
		//   |     ...
		//   +--- TimeStamp2/
		//     ...
		// The only exception is an explicit special case: this prearchive directory may contain
		// other prearchive directories, which we ignore here.  This special case should only arise
		// when the current prearchive is the "global"/COMMON prearchive.

		final Collection<File> projectPrearcs = new HashSet<File>();
		for (final Object apo : arcspec.getProjects_project()) {
			final String apname = ((ArcProjectI)apo).getId();
			projectPrearcs.add(getCanonical(arcspec.getPrearchivePathForProject(apname)));
		}

		for (final File tsdir : prearc.listFiles(isDirectoryFilter)) {
			final File tsdc = getCanonical(tsdir);

			// If this directory is a project prearchive, ignore it and move on
			if (projectPrearcs.contains(tsdc))
				continue;

			final String ts = tsdir.getName();
			if (!timestampPattern.matcher(ts).matches()) {
				// non-project-prearchive directory: something's broken
				final Element se = root.addElement("session");
				se.addAttribute("path", ts);
				se.addAttribute("status", PrearcStatus.ERROR.toString());
				se.setText(ts);
				continue;
			}

			for (final File sessdir : tsdir.listFiles(isDirectoryFilter)) {
				final String sessDirName = sessdir.getName();
				final String relpath = ts + File.separator + sessDirName;
				final Element se = root.addElement("session");
				se.addAttribute("path", relpath);

				PrearcStatus status = checkSessionStatus(sessdir);
				String sessionDateTime = null;

				final String sessLabel;

				if (null != status) {
					sessLabel = null;
				} else {	// status hasn't been set yet, must be okay so far
					final XFTItem item = loadScanItem(sessdir);
					if (null == item) status = PrearcStatus.ERROR;

					final XnatImagesessiondata imgSession;
					if (null == item) {
						log.error("Unable to load image session data for " + sessdir);
						imgSession = null;
						status = PrearcStatus.ERROR;
						sessionDateTime = null;
						sessLabel = null;
					} else {
						final String type = item.getXSIType().toLowerCase();
						if (!canCreate(user, name, type)) {
							// Oops.  User doesn't have access to this session.  Move along.
							root.remove(se);
							continue;
						}
						imgSession = new XnatImagesessiondata(item);

						final String sessionID = imgSession.getId();
						if (null == sessionID || "".equals(sessionID) || "NULL".equals(sessionID)) {
							status = PrearcStatus.READY;
						} else {
							status = PrearcStatus.ARCHIVING;
						}
						sessionDateTime = (String)imgSession.getDate() + "T" + (String)imgSession.getTime();

						final String subjID = imgSession.getSubjectId();
						if (null != subjID && !"".equals(subjID)) {
							se.addAttribute("subject", subjID);
						}

						sessLabel = imgSession.getLabel();

						// Add any other identifying information we want.
						final Set<String> altIDs = new LinkedHashSet<String>();

						for (final String prop : new String[]{"ID"}) try {
							final String value = imgSession.getStringProperty(prop);
							if (null != value && !ignoreIDs.contains(value)) {
								altIDs.add(value);
							}
						} catch (ElementNotFoundException ignore) {
						} catch (FieldNotFoundException ignore) {
						}

						if (null != sessLabel) {	// We don't want the session label in the alternate IDs
							altIDs.remove(sessLabel);
						}
						altIDs.removeAll(ignoreIDs);

						if (!altIDs.isEmpty()) {
							final StringBuilder sb = new StringBuilder();
							for (final String id : altIDs) {
								// Don't include IDs already embedded in the session name.
								if (sessDirName.endsWith(id)) continue;
								if (sb.length() > 0) sb.append(",");
								sb.append(id);
							}
							if (sb.length() > 0) {
								se.addAttribute("addID", sb.toString());
							}
						}
					}
				}
				se.addAttribute("status", status.toString());

				try {
					se.addAttribute("uploadDateTime", XS_DATETIME_FORMAT.format(TIMESTAMP_FORMAT.parse(ts)));
				} catch (ParseException e) {
					log.error("unable to convert timestamp folder name " + ts + " to xs:dateTime", e);
				}

				if (null != sessionDateTime) {
					se.addAttribute("sessionDateTime", sessionDateTime);
				}

				final StringBuilder archiveURL = new StringBuilder(TurbineUtils.GetContext());
				archiveURL.append("/app/action/LoadImageData");
				archiveURL.append("/folder/");
				archiveURL.append(sessDirName);
				if (null != name && !COMMON.equals(name)) {
					archiveURL.append("/project/");
					archiveURL.append(name);
				}
				archiveURL.append("/root/");
				archiveURL.append(ts);
				archiveURL.append("/popup/false/purpose/insert");
				if (PrearcStatus.READY == status) {
					se.addAttribute("archiveURL", archiveURL.toString());
				}

				final String label;
				if (null != sessLabel) {
					label = sessLabel;
				} else if (null != sessDirName) {
					label = sessDirName;
				} else {
					label = "(unknown)";
				}
				se.setText(label);
			}
		}

		response.setContentType("text/xml");
		response.setHeader("Cache-Control", "no-cache");
		try {
			document.write(response.getWriter());
		} catch (IOException e) {
			log.warn("response failed", e);
		}
	}


	private static class PrearcOpException extends Exception {
		private static final long serialVersionUID = 1L;
		private final int code;
		private final String message;
		private final Throwable cause;

		PrearcOpException(final int code, final String message, final Throwable cause) {
			this.code = code;
			this.message = message;
			this.cause = cause;
		}

		PrearcOpException(final int code, final String message) {
			this(code, message, null);
		}

		void sendError(final Logger l, final HttpServletResponse r) {
			try {
				r.sendError(code, message);
			} catch (IOException ignore) {}
			l.error(message, cause);
		}

		void sendErrorLogInfo(final Logger l, final HttpServletResponse r) {
			try {
				r.sendError(code, message);
			} catch (IOException ignore) {}
			l.info(message, cause);
		}
	}


	private XFTItem loadScanItem(final File sessDir) {
		final File sessionXML = new File(sessDir.getPath() + XML_SUFFIX);
		final SAXReader reader = new SAXReader(null);
		try {
			return reader.parse(sessionXML, "scan");
		} catch (IOException e) {
			return null;
		} catch (SAXException e) {
			return null;
		}
	}


	private File getPrearcRoot(final String project) {
		if (null == project || COMMON.equals(project)) {
			return new File(ArcSpecManager.GetInstance().getGlobalPrearchivePath());
		} else {
			final String path = ArcSpecManager.GetInstance().getPrearchivePathForProject(project);
			return null == path ? null : new File(path);
		}
	}


	private void removeSession(final UserI user, final HttpServletResponse response,
			final String project, final File prearc, final String path) throws PrearcOpException {
		if (null == prearc || !prearc.isDirectory()) {
			throw new PrearcOpException(HttpServletResponse.SC_BAD_REQUEST,
					"prearchive " + prearc + " not available");
		}

		if (null == path) {
			throw new PrearcOpException(HttpServletResponse.SC_BAD_REQUEST,
			"no session specified for removal");
		}

		final File sessdir = new File(prearc, path);
		if (!sessdir.isDirectory()) {
			throw new PrearcOpException(HttpServletResponse.SC_BAD_REQUEST,
					path + " is not a valid session in " + prearc);
		}

		final XFTItem scanItem = loadScanItem(sessdir);
		final String type = null == scanItem ? null : scanItem.getXSIType();

		if (!canCreate(user, project, type)) {
			throw new PrearcOpException(HttpServletResponse.SC_FORBIDDEN,
					"user " + user + " not allowed to delete " +
					path + " from prearchive " + project);
		}

		new File(sessdir.getPath() + XML_SUFFIX).delete();	// remove the session XML
		final File tsdir = sessdir.getParentFile();
		FileUtils.DeleteFile(sessdir);
		tsdir.delete();	// delete timestamp parent only if empty.
	}

	/**
	 * Handles a "remove" request by deleting a session from a prearchive.
	 */
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

		final String name = (String) req.getParameter("prearc");

		final File prearc = getPrearcRoot(name);

		try {
			removeSession(user, response, name, prearc, (String)req.getParameter("path"));
		} catch (PrearcOpException e) {
			e.sendErrorLogInfo(log, response);
			return;
		}
	}

	/**
	 * Handles a "move" request by moving a session from one prearchive to another.
	 */
	public void move(final HttpServletRequest req, final HttpServletResponse response, final ServletConfig config) {
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


		final String fromProject = (String) req.getParameter("from");
		final File fromPrearc = getPrearcRoot(fromProject);
		if (null == fromPrearc || !fromPrearc.isDirectory()) {
			log.info("move request from invalid prearchive " + fromProject);
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "source prearchive " + fromProject + " does not exist");
			} catch (IOException ignore) {}
			return;
		}

		final String toProject = (String) req.getParameter("to");
		final File toPrearc = getPrearcRoot(toProject);
		if (null == toPrearc || !toPrearc.isDirectory()) {
			log.info("move request to invalid prearchive " + fromProject);
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "destination prearchive " + toProject + " does not exist");
			} catch (IOException ignore) {}
			return;
		}

		if (fromPrearc.equals(toPrearc)) {
			log.warn("attempted to move session from prearchive " + fromPrearc + " to same prearchive.");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "cannot move session from and to the same prearchive");
			} catch (IOException ignore) {}
			return;
		}

		final String path = (String) req.getParameter("path");
		if (path == null || "".equals(path)) {
			log.info("received session move request from " + login + " with empty path");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no session specified");
			} catch (IOException ignore) {}
			return;
		}

		final File fromSession = new File(fromPrearc, path);
		final XFTItem scanItem = loadScanItem(fromSession);
		final String type = null == scanItem ? null : scanItem.getXSIType();

		if (!canCreate(user, fromProject, type)) {
			log.info("rejected move request for " + login + ": no permission on source project " + fromProject);
			try {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "not allowed to remove session from project " + fromProject);
			} catch (IOException ignore) {}
			return;
		}
		if (!canCreate(user, toProject, type)) {
			log.info("rejected move request for " + login + ": no permission on destination project " + toProject);
			try {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "not allowed to add session to project " + toProject);
			} catch (IOException ignore) {}
			return;
		}

		// Remove the old session documents (including session build logs and image catalog files),
		// move the session contents, and build the new session XML.
		final File tsdir = fromSession.getParentFile();
		new File(tsdir, fromSession.getName() + XML_SUFFIX).delete();
		for (final File file : fromSession.listFiles(PrearcUtils.isSessionGeneratedFileFilter)) {
			file.delete();
		}
		final File newTsdir = new File(toPrearc, tsdir.getName());
		PrearcImporterFactory.getFactory().getPrearcImporter(toProject, newTsdir, fromSession).run();

		// If everything was moved, we can remove the session and timestamp directories.
		fromSession.delete();
		if (fromSession.isDirectory()) {
			log.warn("moved session " + fromSession + " to " + toPrearc + ", but unable to delete original directory.");
		}
		tsdir.delete();	// timestamp directory might contain another session, so no warning if deletion fails.
	}
}
