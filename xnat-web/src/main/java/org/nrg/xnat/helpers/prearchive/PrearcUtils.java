/*
 * web: org.nrg.xnat.helpers.prearchive.PrearcUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import static org.nrg.xdat.preferences.HandlePetMr.SEPARATE_PET_MR;
import static org.nrg.xft.utils.predicates.ProjectAccessPredicate.UNASSIGNED;
import static org.nrg.xnat.turbine.utils.XNATUtils.setArcProjectPaths;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.ArcProjectI;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.preferences.HandlePetMr;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xnat.archive.XNATSessionBuilder;
import org.nrg.xnat.helpers.prearchive.PrearcTableBuilder.Session;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.CatalogUtils;
import org.restlet.resource.ResourceException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ResultOfMethodCallIgnored")
@Slf4j
public class PrearcUtils {
    public static final String APPEND                = "append";
    public static final String DELETE                = "delete";
    public static final String PREARCHIVE_PATH       = HandlePetMr.PREARCHIVE_PATH;
    public static final String PREARC_TIMESTAMP      = "PREARC_TIMESTAMP";
    public static final String PREARC_SESSION_FOLDER = "PREARC_SESSION_FOLDER";
    public static final String PREFIX_QUEUED         = "QUEUED_";
    public static final String PREFIX_PENDING        = "_";
    public static final String PARAM_SOURCE          = HandlePetMr.PARAM_SOURCE;
    public static final String PARAM_TIMEZONE        = "TIMEZONE";
    public static final String PARAM_PROTOCOL        = "protocol";
    public static final String PARAM_VISIT           = "visit";
    public static final String PARAM_SUBJECT_ID      = "subject_ID";
    public static final String PARAM_LABEL           = "label";
    public static final String PARAM_PROJECT         = "project";

    public enum PrearcStatus {
        ARCHIVING,
        BUILDING(true),
        CONFLICT(true),
        DELETING,
        ERROR(true),
        MOVING,
        READY(true),
        RECEIVING(true),
        RECEIVING_INTERRUPT(true),
        SEPARATING,

        QUEUED_ARCHIVING,
        QUEUED_BUILDING(true),
        QUEUED_DELETING,
        QUEUED_MOVING,
        QUEUED_SEPARATING,

        _ARCHIVING,
        _BUILDING(true),
        _CONFLICT(true),
        _DELETING,
        _MOVING,
        _RECEIVING(true),
        _RECEIVING_INTERRUPT(true),
        _SEPARATING;

        public static boolean potentiallyReady(PrearcStatus status) {
            return (status == null || status.equals(READY));
        }

        public boolean isInterruptable() {
            return _interruptable;
        }

        PrearcStatus() {
            this(true);
        }

        PrearcStatus(final boolean interruptable) {
            _interruptable = interruptable;
        }

        private final boolean _interruptable;
    }

    public static final Map<PrearcStatus, PrearcStatus> inProcessStatusMap = createInProcessMap();

    public static Map<PrearcStatus, PrearcStatus> createInProcessMap() {
        return Arrays.stream(PrearcStatus.values())
                     .filter(status -> status != PrearcStatus.READY && status != PrearcStatus.ERROR && !StringUtils.startsWithAny(status.toString(), "_", PREFIX_QUEUED))
                     .collect(Collectors.toMap(Function.identity(), status -> PrearcStatus.valueOf(PREFIX_PENDING + status.name())));
    }

    @SuppressWarnings("unchecked")
    public static List<String> getProjects(final UserI user, String requestedProject) {
        final List<String> projects = new ArrayList<>();
        if (requestedProject != null) {
            if (requestedProject.contains(",")) {
                String[] projectIds = StringUtils.split(requestedProject, ',');
                for (final String projectId : projectIds) {
                    String cleanProject = cleanProject(projectId);
                    if (cleanProject != null || Roles.isSiteAdmin(user)) {
                        projects.add(cleanProject);
                    }
                }
            } else {
                String cleanProject = cleanProject(requestedProject);
                if (cleanProject != null || Roles.isSiteAdmin(user)) {
                    projects.add(cleanProject);
                }
            }
        } else {
            final UserHelperServiceI userHelperService = UserHelper.getUserHelperService(user);
            for (final List<String> row : userHelperService.getQueryResults("xnat:projectData/ID", "xnat:projectData")) {
                final String id = row.get(0);
                if (projects.contains(id)) {
                    continue;
                }
                try {
                    if (canModify(user, id)) {
                        projects.add(id);
                    }
                } catch (Exception e) {
                    log.error("Exception caught testing prearchive access", e);
                }
            }
            // if the user is an admin also add unassigned projects
            if (Roles.isSiteAdmin(user)) {
                projects.add(null);
            }
        }
        return projects;
    }

    private static String cleanProject(final String p) {
        if (UNASSIGNED.equals(p)) {
            return null;
        } else {
            return p;
        }
    }

    public static boolean canModify(final UserI user, final String projectId) throws Exception {
        final UserHelperServiceI userHelperService = UserHelper.getUserHelperService(user);
        return Roles.isSiteAdmin(user) || projectId != null && userHelperService.hasEditAccessToSessionDataByTag(projectId);
    }

    /**
     * Retrieves the File reference to the prearchive root directory for the
     * named project.
     * <p/>
     * 4/30/12- removed requirement that user object be not null.  null users are allowed here for administrative code that happens outside the permissions structure (like logging).
     * 4/30/12- refactored to prevent unnecessary database queries
     *
     * @param username Name of the user getting the directory.
     * @param project  Project abbreviation or alias
     *
     * @return prearchive root directory
     *
     * @throws ResourceException if the named project does not exist, or if the user does not
     *                           have create permission for it, or if the prearchive directory
     *                           does not exist.
     */
    @SuppressWarnings("unused")
    public static File getPrearcDir(final String username, final String project, final boolean allowUnassigned) throws Exception {
        return getPrearcDir(StringUtils.isNotBlank(username) ? Users.getUser(username) : null, project, allowUnassigned);
    }

    /**
     * Retrieves the File reference to the prearchive root directory for the
     * named project.
     * <p/>
     * 4/30/12- removed requirement that user object be not null.  null users are allowed here for administrative code that happens outside the permissions structure (like logging).
     * 4/30/12- refactored to prevent unnecessary database queries
     *
     * @param user    The user getting the directory.
     * @param project Project abbreviation or alias
     *
     * @return prearchive root directory
     *
     * @throws ResourceException if the named project does not exist, or if the user does not
     *                           have create permission for it, or if the prearchive directory
     *                           does not exist.
     */
    public static File getPrearcDir(final UserI user, final String project, final boolean allowUnassigned) throws Exception {
        String prearcPath;
        String prearcRootPref = XDAT.getSiteConfigPreferences().getPrearchivePath();
        if (project == null || project.equals(UNASSIGNED)) {
            if (allowUnassigned || user == null || Roles.isSiteAdmin(user) || Groups.isDataAdmin(user)) {
                prearcPath = prearcRootPref;
            } else {
                throw new InsufficientPrivilegesException(user.getUsername(), XnatProjectdata.SCHEMA_ELEMENT_NAME, UNASSIGNED);
            }
        } else {
            //Refactored to remove unnecessary database hits.  It only needs to hit the xnat_projectdata table if the query is using a project alias rather than a project id.  TO
            ArcProject               p                 = ArcSpecManager.GetInstance().getProjectArc(project);
            final UserHelperServiceI userHelperService = UserHelper.getUserHelperService(user);
            if (p != null) {
                if (!userHelperService.hasEditAccessToSessionDataByTag(project)) {
                    throw new InvalidPermissionException(user.getUsername(), "edit", XnatProjectdata.SCHEMA_ELEMENT_NAME, project);
                }
                final String arcSpecPathForProject = ArcSpecManager.GetInstance().getPrearchivePathForProject(project);
                final String newPathForProject     = RegExUtils.replaceFirst(arcSpecPathForProject, "^/data/xnat/prearchive/", "");
                if (!StringUtils.equals(arcSpecPathForProject, newPathForProject)) {
                    prearcPath = Paths.get(prearcRootPref, newPathForProject).toString();
                } else {
                    prearcPath = arcSpecPathForProject;
                }
                final Optional<ArcProjectI> optional = ArcSpecManager.GetInstance().getProjects_project().stream().filter(arcProject -> StringUtils.equals(project, arcProject.getId())).findFirst();
                if (optional.isPresent()) {
                    setArcProjectPaths(optional.get(), XDAT.getSiteConfigPreferences());
                }
            } else {
                //check to see if it used a project alias
                XnatProjectdata proj = XnatProjectdata.getProjectByIDorAlias(project, user, false);
                if (proj != null) {
                    if (!userHelperService.hasEditAccessToSessionDataByTag(project)) {
                        throw new InvalidPermissionException(user.getUsername(), "edit", XnatProjectdata.SCHEMA_ELEMENT_NAME, project);
                    }
                    String arcSpecPathForProject = proj.getPrearchivePath();
                    String newPathForProject     = arcSpecPathForProject.replaceFirst("^/data/xnat/prearchive/", "");
                    if (!StringUtils.equals(arcSpecPathForProject, newPathForProject)) {
                        prearcPath = Paths.get(prearcRootPref, newPathForProject).toString();
                    } else {
                        prearcPath = arcSpecPathForProject;
                    }
                    proj.setProperty("arc:project/paths/prearchivePath", Paths.get(ArcSpecManager.GetInstance().getGlobalPrearchivePath(), proj.getId()).toString());
                } else {
                    throw new IOException("No project named " + project);
                }
            }

            if (null == prearcPath) {
                final String message = "Unable to retrieve prearchive path for project " + project;
                log.error(message);
                throw new Exception(message);
            }
        }
        final File prearc = new File(prearcPath);
        if (prearc.exists() && !prearc.isDirectory()) {
            final String message = "Prearchive directory is invalid for project " + project;
            log.error(message);
            throw new Exception(message);
        }
        return prearc;
    }

    /**
     * Checks that the user has permissions on the project. If getPrearcDir goes through without
     * exceptions the user is valid.
     *
     * @param user    The user to test.
     * @param project If the project is null, it is the unassigned project
     *                project abbreviation or alias
     *
     * @return true if the user has permissions to access the project, false otherwise
     *
     * @throws Exception   When something goes wrong.
     * @throws IOException When an error occurs reading or writing data.
     */
    @SuppressWarnings("unused")
    public static boolean validUser(final UserI user, final String project, final boolean allowUnassigned) throws Exception {
        boolean valid = true;
        try {
            if (null == project) {
                PrearcUtils.getPrearcDir(user, UNASSIGNED, allowUnassigned);
            } else {
                PrearcUtils.getPrearcDir(user, project, allowUnassigned);
            }
        } catch (InvalidPermissionException e) {
            valid = false;
        }
        return valid;
    }

    /**
     * A list of all projects in the prearchive.
     *
     * @return a list of project names
     */
    @SuppressWarnings("unused")
    public static String[] allPrearchiveProjects() {
        File d = new File(ArcSpecManager.GetInstance(false).getGlobalPrearchivePath());
        return d.list(DirectoryFileFilter.INSTANCE);
    }

    private static final Pattern TSDIR_SECONDS_PATTERN = Pattern.compile("[0-9]{8}_[0-9]{6}");
    private static final String  TSDIR_SECONDS_FORMAT  = "yyyyMMdd_HHmmss";

    private static final Pattern TSDIR_MILLISECONDS_PATTERN = Pattern.compile("[0-9]{8}_[0-9]{9}");
    private static final String  TSDIR_MILLISECONDS_FORMAT  = "yyyyMMdd_HHmmssSSS";

    public static final FileFilter isTimestampDirectory = f -> f.isDirectory() && (TSDIR_SECONDS_PATTERN.matcher(f.getName()).matches() || TSDIR_MILLISECONDS_PATTERN.matcher(f.getName()).matches());

    public static Date parseTimestampDirectory(final String stamp) throws ParseException {
        final DateFormat format;
        if (stamp.length() == 18) {
            format = new SimpleDateFormat(TSDIR_MILLISECONDS_FORMAT);
        } else {
            format = new SimpleDateFormat(TSDIR_SECONDS_FORMAT);
        }

        return format.parse(stamp);
    }

    public static final FileFilter isDirectory = File::isDirectory;

    /**
     * Creates a formatted timestamp using the {@link #TSDIR_MILLISECONDS_FORMAT} specification
     * and the U.S. locale.
     *
     * @return The formatted timestamp
     */
    public static String makeTimestamp() {
        final SimpleDateFormat formatter = new SimpleDateFormat(TSDIR_MILLISECONDS_FORMAT, Locale.US);
        return formatter.format(new Date());
    }

    /**
     * Checks for obvious problems with a session XML: existence, permissions.
     *
     * @param sessionXML The XML defining the session
     *
     * @return The {@link PrearcStatus} for the session.
     */
    public static PrearcStatus checkSessionStatus(final File sessionXML) {
        if (!sessionXML.exists()) {
            return PrearcStatus.RECEIVING;
        }
        if (!sessionXML.isFile()) {
            log.error("{} exists, but is not a file. ", sessionXML);
            return PrearcStatus.ERROR;
        }
        if (!sessionXML.canRead()) {
            log.error("cannot read {}.", sessionXML);
            return PrearcStatus.ERROR;
        }
        if (sessionXML.length() == 0) {
            log.error("{} is empty.", sessionXML);
            return PrearcStatus.ERROR;
        }
        return null;
    }

    public static java.util.Date timestamp2Date(java.sql.Timestamp t) {
        return new java.util.Date(t.getTime());
    }

    /**
     * Create a blank session that will be used to populate a row in the prearchive table that will
     * be filled later.
     * <p/>
     * No attempt is made to create the necessary folder structure in the prearchive directory on the
     * filesystem.
     * <p/>
     * The essential fields are set:
     * - folderName
     * - project
     * - url
     * - tag (the Study Instance UID)
     *
     * @param project      The project for the session data object.
     * @param sessionLabel The label for the session data object.
     * @param tag          The tag for the session data object.
     *
     * @return A new blank session data object.
     */
    @SuppressWarnings("unused")
    public static SessionData blankSession(String project, String sessionLabel, String tag) throws IOException {
        if (sessionLabel == null || tag == null) {
            throw new IOException("Cannot create a SessionData object with a session label or study instance uid");
        }

        final File root;
        if (null == project) {
            root = new File(ArcSpecManager.GetInstance().getGlobalPrearchivePath());
        } else {
            //root = new File(project.getPrearchivePath());
            root = new File(ArcSpecManager.GetInstance().getGlobalPrearchivePath() + "/" + project);
        }
        // doesn't currently exist only used to get pathname to create the URL
        final File tsdir;
        tsdir = new File(root, PrearcUtils.makeTimestamp());

        SessionData sess = new SessionData();
        sess.setFolderName(sessionLabel);
        sess.setName(sessionLabel);
        sess.setTimestamp(tsdir.getName());
        sess.setProject(project);
        sess.setUrl((new File(tsdir, sessionLabel)).getAbsolutePath());
        sess.setTag(tag);
        return sess;
    }

    @SuppressWarnings("unused")
    public static void deleteProject(String project) throws Exception {
        final List<SessionData> sessions = PrearcDatabase.getSessionsInProject(project);
        for (final SessionData session : sessions) {
            PrearcDatabase.deleteSession(session.getFolderName(), session.getTimestamp(), session.getProject());
        }
    }

    public static File getPrearcSessionDir(final UserI user, final String project, final String timestamp, final String session, final boolean allowUnassigned) throws Exception {
        if (user == null || timestamp == null || session == null) {
            throw new IllegalArgumentException(String.format("Invalid prearchive session: user %s; timestamp %s; session %s",
                                                             user, timestamp, session));
        }
        return new File(new File(getPrearcDir(user, project, allowUnassigned), timestamp), session);
    }

    public static final FileFilter isSessionGeneratedFileFilter = new FileFilter() {
        private final Pattern conversionLogPattern = Pattern.compile("(\\w*)toxnat\\.log");
        private final Pattern scanCatalogPattern = Pattern.compile("scan_(\\d*)_catalog.xml");

        public boolean accept(final File f) {
            return scanCatalogPattern.matcher(f.getName()).matches()
                   || conversionLogPattern.matcher(f.getName()).matches();
        }
    };

    public static void resetStatus(final UserI user, final String project, final String timestamp, final String session, final boolean allowUnassigned) throws Exception {
        SessionData deleted = null;
        try {
            deleted = PrearcDatabase.getSession(session, timestamp, project);
            PrearcDatabase.unsafeSetStatus(session, timestamp, project, PrearcStatus._DELETING);
            PrearcDatabase.deleteCacheRow(session, timestamp, project);
        } catch (SessionException ignored) {
        }

        addSession(user, project, timestamp, session, allowUnassigned);
        cleanUpDeletedSession(project, timestamp, session, deleted);
    }

    public static void resetStatus(final UserI user, final String project, final String timestamp, final String session, final String uID, final boolean allowUnassigned) throws Exception {
        SessionData deleted = null;
        try {
            deleted = PrearcDatabase.getSession(session, timestamp, project);
            PrearcDatabase.unsafeSetStatus(session, timestamp, project, PrearcStatus._DELETING);
            PrearcDatabase.deleteCacheRow(session, timestamp, project);
        } catch (SessionException ignored) {
        }

        addSession(user, project, timestamp, session, uID, allowUnassigned);
        cleanUpDeletedSession(project, timestamp, session, deleted);
    }

    public static void addSession(final UserI user, final String project, final String timestamp, final String session, final boolean allowUnassigned) throws Exception {
        addSession(user, project, timestamp, session, null, allowUnassigned);
    }

    public static void addSession(final UserI user, final String project, final String timestamp, final String session, final String uID, final boolean allowUnassigned) throws Exception {
        final Session     s  = PrearcTableBuilder.buildSessionObject(PrearcUtils.getPrearcSessionDir(user, project, timestamp, session, allowUnassigned), timestamp, project);
        final SessionData sd = s.getSessionData(PrearcDatabase.projectPath(project));
        if (s.getSessionXML() != null) {
            sd.setUrl((new File(s.getSessionXML().getParentFile(), s.getFolderName()).getAbsolutePath()));
        }
        if (StringUtils.isNotEmpty(uID)) {
            sd.setTag(uID);
        }
        PrearcDatabase.addSession(sd);
    }

    public static String makeUri(final String urlBase, final String timestamp, final String folderName) {
        return StringUtils.join(urlBase, "/", timestamp, "/", folderName);
    }

    public static Map<String, Object> parseURI(final String uri) throws MalformedURLException {
        //noinspection ConstantConditions
        return UriParserUtils.parseURI(uri).getProps();
    }

    public static String buildURI(final String project, final String timestamp, final String folderName) {
        return StringUtils.join("/prearchive/projects/", (project == null) ? UNASSIGNED : project, "/", timestamp, "/", folderName);
    }

    public static XFTTable convertArrayLtoTable(ArrayList<ArrayList<Object>> rows) {
        XFTTable table = new XFTTable();
        table.initTable(PrearcDatabase.getCols());
        for (final ArrayList<Object> row : rows) {
            table.insertRow(row.toArray());
        }
        return table;
    }

    public static String identifyProject(final Map<String, Object> params) throws MalformedURLException {
        if (params.containsKey(URIManager.PROJECT_ID)) {
            return (String) params.get(URIManager.PROJECT_ID);
        } else if (params.containsKey(RequestUtil.DEST)) {
            return (String) (parseURI((String) params.get(RequestUtil.DEST))).get(URIManager.PROJECT_ID);
        }
        return null;
    }

    public static final String TEMP_UNPACK = "temp-unpack";

    public static boolean isUnassigned(final SessionData sd) {
        return StringUtils.isEmpty(sd.getProject()) || sd.getProject().equals(UNASSIGNED);
    }

    /*******************
     * The prearchive logging code begins here.
     * <p/>
     * In the future, we might want to move this to a database table.  However, the current prearchive table doesn't have a primary key column (really?).
     * So, there would be no way to reliably join from the logs table to the prearchive table.  Also, this would make more sense to do as part of a image session logging framework
     * which would capture a lot more than just prearchive logs, but requires more requirements gathering.
     * <p/>
     * As such, this is more of a stub implementation, that should probably change when the above problems are dealt with.  It will facilitate the current requirement, which
     * is just that we can show the last exception via REST.
     */

    private static File getLogDir(final String project, final String timestamp, final String session) throws Exception {
        if (timestamp == null || session == null) {
            throw new IllegalArgumentException(String.format("Invalid prearchive session: timestamp %s; session %s",
                                                             timestamp, session));
        }
        final XnatUserProvider provider         = XDAT.getContextService().getBeanSafely("receivedFileUserProvider", XnatUserProvider.class);
        final UserI            receivedFileUser = provider != null ? provider.get() : Users.getUser(XDAT.getSiteConfigurationProperty("receivedFileUser"));
        return new File(new File(new File(getPrearcDir(receivedFileUser, project, true), timestamp), session), "logs");
    }

    /**
     * Logs a message for a particular prearchive session.  The log entry will be placed in a log file named with the current timestamp in a logs subdirectory.
     *
     * @param data    The session data object associated with the message.
     * @param message The message to be logged.
     */
    public static void log(final SessionData data, final Throwable message) {
        PrearcUtils.log(data.getProject(), data.getTimestamp(), data.getName(), message);
    }

    /**
     * Logs a message for a particular prearchive session.  The log entry will be placed in a log file named with the current timestamp in a logs subdirectory.
     *
     * @param data    The session data object associated with the message.
     * @param message The message to be logged.
     */
    public static void log(final SessionData data, final String message) {
        PrearcUtils.log(data.getProject(), data.getTimestamp(), data.getName(), message);
    }

    /**
     * Logs a message for a particular prearchive session.  The log entry will be placed in a log file named with the current timestamp in a logs subdirectory.
     *
     * @param project   The prearchive session's associated project.
     * @param timestamp The prearchive session's timestamp.
     * @param session   The prearchive session's ID.
     * @param message   The message to be logged.
     */
    public static void log(final String project, final String timestamp, final String session, final Throwable message) {
        log(project, timestamp, session, message.getMessage());
    }

    /**
     * Logs a message for a particular prearchive session.  The log entry will be placed in a log file named with the current timestamp in a logs subdirectory.
     *
     * @param project   The prearchive session's associated project.
     * @param timestamp The prearchive session's timestamp.
     * @param session   The prearchive session's ID.
     * @param message   The message to be logged.
     */
    public static void log(final String project, final String timestamp, final String session, final String message) {
        try {
            File logs = getLogDir(project, timestamp, session);
            if (!logs.exists()) {
                logs.mkdirs();
            }
            //noinspection deprecation
            FileUtils.writeStringToFile(new File(logs, Calendar.getInstance().getTimeInMillis() + ".log"), message);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Logs a message for a particular prearchive session.  The log entry will be placed in a log file named with the current timestamp in a logs subdirectory.
     *
     * @param path    The path to the prearchive session.
     * @param message The message to be logged.
     */
    public static void log(final File path, final Throwable message) {
        try {
            File logs = new File(path, "logs");
            if (!logs.exists()) {
                logs.mkdirs();
            }
            //noinspection deprecation
            FileUtils.writeStringToFile(new File(logs, Calendar.getInstance().getTimeInMillis() + ".log"), message.getMessage());
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Get all of the log files for this prearchived session.  Returns an empty list when none are present.
     *
     * @param project   The project to check for log files.
     * @param timestamp The timestamp to check for log files.
     * @param session   The prearchive session's ID.
     *
     * @return A collection of file objects referencing any located log files.
     */
    public static Collection<File> getLogs(final String project, final String timestamp, final String session) {
        final Collection<File> logs = new ArrayList<>();
        try {
            final File logDir = getLogDir(project, timestamp, session);
            if (logDir.exists()) {
                final File[] files = logDir.listFiles();
                if (files != null) {
                    logs.addAll(Arrays.asList(files));
                }
            }
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
        return logs;
    }


    /**
     * Get all of the log IDs for this prearchived session.  Returns an empty list when none are present.
     *
     * @param project   The project to check for log files.
     * @param timestamp The timestamp to check for log files.
     * @param session   The prearchive session's ID.
     *
     * @return A collection of file objects referencing any located log files.
     */
    public static Collection<String> getLogIds(final String project, final String timestamp, final String session) {
        final Collection<String> logs  = new ArrayList<>();
        final Collection<File>   found = PrearcUtils.getLogs(project, timestamp, session);
        if (found != null && found.size() > 0) {
            try {
                for (File f : found) {
                    logs.add(f.getName().substring(0, f.getName().indexOf(".log")));//strip off the .log so it would be seamless to not use physical log files here.
                }
            } catch (Exception e) {
                log.error("", e);
                return null;
            }
        }
        return logs;
    }

    /**
     * Return the log entry for the specified ID (timestamp).  Returns null when it isn't found.
     *
     * @param project   The project to check for log files.
     * @param timestamp The timestamp to check for log files.
     * @param session   The prearchive session's ID.
     * @param logId     The ID of the desired log entry.
     *
     * @return The log entry if found, null otherwise.
     */
    public static String getLog(final String project, final String timestamp, final String session, final String logId) {
        try {
            final File logDir = getLogDir(project, timestamp, session);
            if (logDir.exists()) {
                final File log = new File(logDir, logId + ".log");//the .log is hidden from log users to conceal implementation details
                if (log.exists()) {
                    //noinspection deprecation
                    return DateUtils.format(new Date(log.lastModified()), "MM/dd/yyyy HH:mm:ss") + ":" + FileUtils.readFileToString(log);
                }
            }
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
        return null;
    }

    /**
     * Return the last log entry for this prearchived session.  When none are present, null is returned.
     *
     * @param project   The project to check for log files.
     * @param timestamp The timestamp to check for log files.
     * @param session   The prearchive session's ID.
     *
     * @return The last log entry for the indicated log, null if not found.
     */
    public static String getLastLog(final String project, final String timestamp, final String session) {
        try {
            final File logDir = getLogDir(project, timestamp, session);
            if (logDir.exists()) {
                final File[] files = logDir.listFiles();
                if (files != null && files.length > 0) {
                    final File lastFile = Arrays.stream(files).max(Comparator.comparingLong(File::lastModified)).orElse(null);
                    return FileUtils.readFileToString(lastFile, Charset.defaultCharset());
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    /**
     * Is the session currently receiving files?
     * <p/>
     * It reviews the file locks that are currently open for this session.
     *
     * @param session The session to test for receiving.
     *
     * @return True if the session still appears to be receiving new files, false otherwise.
     */
    public static boolean isSessionReceiving(final SessionDataTriple session) {
        final File lockFolder = org.nrg.xnat.utils.FileUtils.buildCacheSubDir("prearc_locks", session.getProject(), session.getTimestamp(), session.getFolderName());
        if (!lockFolder.exists()) {
            return false;
        }

        final File[] locks = lockFolder.listFiles();
        return locks != null && locks.length > 0;
    }

    public static void buildSession(SessionData sd) throws PrearcDatabase.SyncFailedException {
        buildSession(sd, new File(sd.getUrl()), sd.getName(), sd.getTimestamp(), sd.getProject(), sd.getVisit(),
                     sd.getProtocol(), sd.getTimeZone(), sd.getSource());
    }

    public static void buildSession(final SessionData sd, final File sessionDir, final String session, final String timestamp,
                                    final String project, final String visit, final String protocol,
                                    final String timezone, final String source) throws PrearcDatabase.SyncFailedException {
        buildSession(sd, sessionDir, session, timestamp, project, sd.getSubject(), visit, protocol, timezone, source);
    }

    public static void buildSession(final SessionData sd, final File sessionDir, final String session, @SuppressWarnings("unused") final String timestamp,
                                    final String project, final String subject, final String visit, final String protocol,
                                    final String timezone, final String source) throws PrearcDatabase.SyncFailedException {
        final Map<String, String> params = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(project) && !StringUtils.equals(UNASSIGNED, project)) {
            params.put(PARAM_PROJECT, project);
            params.put(SEPARATE_PET_MR, HandlePetMr.getSeparatePetMr(project).value());
        } else {
            params.put(SEPARATE_PET_MR, HandlePetMr.getSeparatePetMr().value());
        }
        params.put(PARAM_LABEL, StringUtils.defaultIfBlank(sd.getName(), session));
        if (StringUtils.isNotBlank(subject)) {
            params.put(PARAM_SUBJECT_ID, subject);
        }
        if (StringUtils.isNotBlank(visit)) {
            params.put(PARAM_VISIT, visit);
        }
        if (StringUtils.isNotBlank(protocol)) {
            params.put(PARAM_PROTOCOL, protocol);
        }
        if (StringUtils.isNotBlank(timezone)) {
            params.put(PARAM_TIMEZONE, timezone);
        }
        if (StringUtils.isNotBlank(source)) {
            params.put(PARAM_SOURCE, source);
        }

        PrearcUtils.cleanLockDirs(sd.getSessionDataTriple());

        try {
            final File sessionXmlFile = new File(sessionDir.getPath() + ".xml");
            log.info("Attempting to build prearchive session in folder '{}' into the session XML file '{}'",
                     sessionDir.getPath(), sessionXmlFile.getPath());

            final Boolean success = new XNATSessionBuilder(sessionDir, sessionXmlFile, true, params).call();
            if (BooleanUtils.isNotTrue(success)) {
                throw new PrearcDatabase.SyncFailedException("Error building session");
            }
        } catch (PrearcDatabase.SyncFailedException e) {
            throw e;
        } catch (Throwable t) {
            throw new PrearcDatabase.SyncFailedException("Error building session", t);
        }
    }

    public static void setupScans(final XnatImagesessiondata session, final String root) {
        final String fixedRootPath = fixRootPath(root);
        for (XnatImagescandataI scan : session.getScans_scan()) {
            for (final XnatAbstractresourceI resource : scan.getFile()) {
                updateResourceWithArchivePathAndPopulateStats((XnatAbstractresource) resource, fixedRootPath, true);
            }
        }
        for (final XnatAbstractresourceI resource : session.getResources_resource()) {
            updateResourceWithArchivePathAndPopulateStats((XnatAbstractresource) resource, fixedRootPath, false);
        }
    }

    public static void cleanupScans(final XnatImagesessiondata session, final String rootPath, final EventMetaI c) {
        final String          fixedRootPath = fixRootPath(rootPath);
        final XnatProjectdata project       = session.getProjectData();
        final boolean         checksums     = getChecksumConfiguration(project);

        Stream.concat(session.getScans_scan()
                .stream()
                .map(XnatImagescandataI::getFile).flatMap(Collection::stream), session.getResources_resource().stream())
               .filter(XnatResourcecatalog.class::isInstance)
               .map(XnatResourcecatalog.class::cast)
               .forEach(catalog -> {
                   try {
                       CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate(fixedRootPath, catalog, project.getId());
                       if (CatalogUtils.formalizeCatalog(catalogData.catBean, catalogData.catPath, catalogData.project, c.getUser(), c, checksums, false)) {
                           CatalogUtils.writeCatalogToFile(catalogData, checksums);
                       }
                   } catch (Exception e) {
                       log.error("An error occurred trying to write catalog data for {}", catalog.getUri(), e);
                   }
               });
    }

    private static void updateResourceWithArchivePathAndPopulateStats(XnatAbstractresource resource, String root, boolean setContentToRawIfMissing) {
        resource.prependPathsWith(root);
        if (setContentToRawIfMissing && StringUtils.isBlank(resource.getContent())) {
            ((XnatResource) resource).setContent("RAW");
        }
        if (resource instanceof XnatResourcecatalog) {
            ((XnatResourcecatalog) resource).clearFiles();
        }
        CatalogUtils.populateStats(resource, root);
    }

    private static void cleanUpDeletedSession(final String project, final String timestamp, final String session, final SessionData deleted) throws Exception {
        if (deleted != null) {
            PrearcDatabase.setAutoArchive(session, timestamp, project, deleted.getAutoArchive());
            PrearcDatabase.setPreventAnon(session, timestamp, project, deleted.getPreventAnon());
            PrearcDatabase.setSource(session, timestamp, project, deleted.getSource());
            PrearcDatabase.setPreventAutoCommit(session, timestamp, project, deleted.getPreventAutoCommit());
        }
    }

    private final static Object syncLock = new Object();

    /**
     * This method will attempt to create a lock for the referenced file, and return a PrearcFileLock for managing the lock.
     * <p/>
     * If the file is already locked, it will throw a SessionFileLockException.
     * <p/>
     * The file locking for prearchive file manipulation is performed on a shadow copy of the actual file.
     * The file receipt process includes a write, copy to anon, delete, and copy back.  File locks are lost
     * as soon as their associated streams are closed.  So to do file locking on the actual file, we'd have to
     * refactor the code considerably so that a single stream (or more likely RandomAccessFile) could be used and
     * kept open through all of those processes.  For the time being, we'll create a shadow (empty) file in the cache space
     * to lock instead.  This has the added convenience of making it really easy for other processes to review the contents of
     * a cache dir to see if any locks are currently open (see isSessionReceiving()).
     * <p/>
     * ATTENTION: You must call the .release() method on the returned object to unlock the file.
     *
     * @param session  The session to be locked.
     * @param filename The filename to be locked.
     *
     * @return PrearcFileLock
     *
     * @throws SessionFileLockException When an attempt is made to access a locked file.
     * @throws IOException              When an error occurs reading or writing data.
     */
    public static PrearcFileLock lockFile(final SessionDataTriple session, final String filename) throws SessionFileLockException, IOException {
        //putting these in a subdirectory of the cache space
        //this will allow other features to see if there are any locks in this session.
        final File lockFolder = org.nrg.xnat.utils.FileUtils.buildCacheSubDir("prearc_locks", session.getProject(), session.getTimestamp(), session.getFolderName());

        if (!lockFolder.exists()) {
            lockFolder.mkdirs();
        }

        final File             lockFile = new File(lockFolder, filename);
        final FileLock         lock;
        final FileOutputStream stream;
        final FileChannel      channel;

        synchronized (syncLock) {
            //the lock will be lost if this stream is closed.
            stream = new FileOutputStream(lockFile);
            channel = stream.getChannel();

            try {
                lock = channel.tryLock();
                if (lock == null) {
                    stream.close();
                    throw new SessionFileLockException(session, filename);
                }
            } catch (OverlappingFileLockException e) {
                stream.close();
                throw new SessionFileLockException(session, filename, e);
            }
        }

        return new PrearcFileLock(lockFile, lock, stream);
    }

    /**
     * Used to delete the empty directories that are generated by the prearc import processes
     *
     * @param session The session to be cleaned.
     */
    public static void cleanLockDirs(final SessionDataTriple session) {
        final File project   = org.nrg.xnat.utils.FileUtils.buildCacheSubDir("prearc_locks", session.getProject());
        final File timestamp = new File(project, session.getTimestamp());
        final File name      = new File(timestamp, session.getFolderName());

        synchronized (syncLock) {
            //synchronized to prevent overlap with .lockFile()
            if (name.exists()) {
                final String[] names = name.list();
                if (names == null || names.length == 0) {
                    try {
                        FileUtils.deleteDirectory(name);
                        if (timestamp.exists()) {
                            final String[] timestamps = timestamp.list();
                            if (timestamps == null || timestamps.length == 0) {
                                FileUtils.deleteDirectory(timestamp);
                                if (project.exists()) {
                                    final String[] projects = project.list();
                                    if (projects == null || projects.length == 0) {
                                        FileUtils.deleteDirectory(project);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        log.error("Couldn't clean temporary lock directories in the cache folder.", e);
                    }
                }
            }
        }
    }

    /**
     * File Lock maintenance object.
     *
     * @author tim@deck5consulting.com
     */
    public static class PrearcFileLock {
        private final File             f;
        private final FileLock         lock;
        private final FileOutputStream stream;

        public PrearcFileLock(final File f, final FileLock fl, final FileOutputStream stream) {
            this.f = f;
            this.stream = stream;
            this.lock = fl;
        }

        /**
         * releases the lock on the file by closing the associated stream, and deleting the shadow file.
         * amended to specifically release the file lock, in case closing the stream was inadequate.
         */
        public void release() {
            try {
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (Exception e) {
                        // ignore
                    }
                }
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                //ignore
            }

            FileUtils.deleteQuietly(f);
        }
    }

    /**
     * Thrown when a file is already locked, but an additional lock is requested.
     *
     * @author tim@deck5consulting.com
     */
    public static class SessionFileLockException extends Exception {
        private static final long serialVersionUID = 7752495772994240672L;

        public SessionFileLockException(SessionDataTriple session, String fileName) {
            this(session, fileName, new Exception());
        }

        public SessionFileLockException(SessionDataTriple session, String fileName, Exception e) {
            super(String.format("Unable to obtain lock on %4$s within %1$s/%2$s/%3$s", session.getProject(), session.getTimestamp(), session.getFolderName(), fileName), e);
        }
    }

    public static boolean parseParam(Map<String, Object> parameters, String paramName, boolean defaultValue) {
        if (!parameters.containsKey(paramName)) {
            return defaultValue;
        }
        Object value = parameters.get(paramName);
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            log.error("{} is not a valid value for {}, using default {}", value, paramName, defaultValue);
            return defaultValue;
        }
    }

    @Nonnull
    private static String fixRootPath(final String rootPath) {
        return StringUtils.appendIfMissing(StringUtils.replaceChars(rootPath, '\\', '/'), "/");
    }

    private static boolean getChecksumConfiguration(final XnatProjectdata project) {
        try {
            return CatalogUtils.getChecksumConfiguration(project);
        } catch (ConfigServiceException e) {
            return false;
        }
    }
}
