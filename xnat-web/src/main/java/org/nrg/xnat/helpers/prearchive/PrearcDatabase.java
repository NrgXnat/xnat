/*
 * web: org.nrg.xnat.helpers.prearchive.PrearcDatabase
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.automation.entities.Script;
import org.nrg.automation.services.ScriptService;
import org.nrg.dicomtools.filters.DicomFilterService;
import org.nrg.dicomtools.filters.SeriesImportFilter;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.SerializerService;
import org.nrg.status.ListenerUtils;
import org.nrg.status.StatusListenerI;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.XnatMrsessiondataBean;
import org.nrg.xdat.bean.XnatPetmrsessiondataBean;
import org.nrg.xdat.bean.XnatPetsessiondataBean;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatPetscandataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.PrearcSessionArchiver;
import org.nrg.xnat.archive.XNATSessionBuilder;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.restlet.XNATApplication;
import org.nrg.xnat.restlet.actions.PrearcImporterA.PrearcSession;
import org.nrg.xnat.restlet.services.Archiver;
import org.nrg.xnat.utils.XnatUserProvider;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public final class PrearcDatabase {
    private static final Logger logger = LoggerFactory.getLogger(PrearcTableBuilder.class);
    public static Connection conn;
    final static String table = "prearchive";
    final static String tableWithSchema = PoolDBUtils.search_schema_name + "." + PrearcDatabase.table;
    private final static String tableSql = PrearcDatabase.createTableSql();
    public static boolean ready = false;

    // an object that synchronizes the cache with some permanent store
    private static SessionDataDelegate sessionDelegate;

    private static String prearcPath;

    private static SerializerService _serializer;

    public static final String SPLIT_PETMR_SESSION_ID = "SplitPetMrSessions";

    public static final String DEFAULT_SPLIT_PETMR_SESSION_FILTER = "{\n" +
            "    \"mode\": \"modalityMap\",\n" +
            "    \"exclude\": \"/^yes$/i.test('#BurnedInAnnotation#')\",\n" +
            "    \"PT\": \"'#Modality#' == 'PT' || ('#Modality#' == 'MR' && /^.*MRAC.*$/.test('#SeriesDescription#'))\",\n" +
            "    \"MR\": \"'#Modality#' != 'PT' && !('#Modality#' == 'MR' && /^.*MRAC.*$/.test('#SeriesDescription#'))\",\n" +
            "    \"default\": \"MR\"\n" +
            "}\n";

    public static final Script DEFAULT_SPLIT_PETMR_SESSION_SCRIPT = new Script(SPLIT_PETMR_SESSION_ID,
            "Split PET/MR script",
            "Default implementation of the split PET/MR session script.",
            "groovy", "", DEFAULT_SPLIT_PETMR_SESSION_FILTER);

    /**
     * The default initializer uses the file system as this cache's permanent store.
     *
     * @param recreateDBMSTablesFromScratch Indicates whether the prearchive database tables should be rebuilt from scratch.
     *
     * @throws IllegalStateException
     * @throws SessionException
     * @throws IOException
     */
    public static void initDatabase(final boolean recreateDBMSTablesFromScratch) throws Exception {
        initDatabase(null, null, recreateDBMSTablesFromScratch);
    }

    /**
     * Initialize the cache with a path to the prearchive that syncs up the cache with the permanent store.
     *
     * @param prearcPath                    Indicates the prearchive path.
     * @param recreateDBMSTablesFromScratch Indicates whether the prearchive database tables should be rebuilt from scratch.
     *
     * @throws IllegalStateException
     * @throws SessionException
     * @throws IOException
     */
    public static void initDatabase(final String prearcPath, final boolean recreateDBMSTablesFromScratch) throws Exception {
        initDatabase(prearcPath, null, recreateDBMSTablesFromScratch);
    }

    /**
     * Initialize the cache with a session data delegate that syncs up the cache with the permanent store.
     *
     * @param delegate                      Carries the session data delegate.
     * @param recreateDBMSTablesFromScratch Indicates whether the prearchive database tables should be rebuilt from scratch.
     *
     * @throws IllegalStateException
     * @throws SessionException
     * @throws IOException
     */
    public static void initDatabase(SessionDataDelegate delegate, boolean recreateDBMSTablesFromScratch) throws Exception {
        initDatabase(null, delegate, recreateDBMSTablesFromScratch);
    }

    /**
     * Initialize the cache with the prearchive path and a session data delegate that syncs up the cache with the permanent store.
     *
     * @param prearcPath                    Indicates the prearchive path.
     * @param delegate                      Carries the session data delegate.
     * @param recreateDBMSTablesFromScratch Indicates whether the prearchive database tables should be rebuilt from scratch.
     *
     * @throws IllegalStateException
     * @throws SessionException
     * @throws IOException
     */
    public static void initDatabase(String prearcPath, SessionDataDelegate delegate, boolean recreateDBMSTablesFromScratch) throws Exception {
        if (!PrearcDatabase.ready) {
            PrearcDatabase.prearcPath = StringUtils.isBlank(prearcPath) ? PrearcDatabase.getPrearcPath() : prearcPath;
            if (PrearcDatabase.prearcPath != null) {
                PrearcDatabase.sessionDelegate = delegate != null ? delegate : new FileSystemSessionData(PrearcDatabase.prearcPath);

                if (!tableExists()) { // create the table if it does not currently exist
                    PrearcDatabase.createTable();
                } else { // check to see if the table has the correct set of columns (older versions may not)
                    PrearcDatabase.correctTable(); // if not, correct the table by adding the required columns
                }

                if (recreateDBMSTablesFromScratch) {
                    PrearcDatabase.deleteRows();
                }

                PrearcDatabase.populateTable(); // add rows to the table from the prearchive directory if not already present
                PrearcDatabase.pruneDatabase(); // remove rows from the table if they are not present in the prearchive directory

                PrearcDatabase.ready = true;
            }
        }
    }

    protected static void setSessionDataModifier(SessionDataModifierI sm) {
        PrearcDatabase.sessionDelegate.setSm(sm);
    }

    /**
     * @return Path to the prearchive on the user filesystem
     */

    protected static String getPrearcPath() {
        return XDAT.getSiteConfigPreferences().getPrearchivePath();
    }

    private static boolean tableExists() throws Exception {
        try {
            return new SessionOp<Boolean>() {
                public Boolean op() throws Exception {
                    String query = "SELECT * FROM pg_catalog.pg_tables WHERE schemaname = 'xdat_search' AND tablename = 'prearchive';";

                    ResultSet r = this.pdb.executeQuery(null, query, null);
                    return PoolDBUtils.GetResultSetSize(r) == 1;
                }
            }.run();
        }
        // can't happen
        catch (SessionException e) {
            logger.error("", e);
        }
        return true;
    }

    /**
     * Create the table if it doesn't exist. Should only be called once. Delete table argument on class load.
     *
     * @throws Exception
     */
    private static void createTable() throws Exception {
        try {
            new SessionOp<Void>() {
                public Void op() throws Exception {
                    String query = "SELECT * FROM information_schema.tables WHERE table_schema = LOWER('xdat_search') and table_name = LOWER('" + PrearcDatabase.table + "');";
                    String exists = (String) PoolDBUtils.ReturnStatisticQuery(query, "relname", null, null);
                    if (exists == null) {
                        PoolDBUtils.ExecuteNonSelectQuery(tableSql, null, null);
                    }
                    return null;
                }
            }.run();
        } catch (SessionException e) {
            logger.error("", e);
        }
    }

    /**
     * Corrects the prearchive table, checking for column existence and including new columns when required.
     *
     * @throws Exception
     */
    private static void correctTable() throws Exception {
        try {
            new SessionOp<Void>() {
                public Void op() throws Exception {
                    // First find out what's in the existing table. We're assuming it exists, because we should have
                    // checked for existing prior to trying to correct the table.
                    final List<String> existing = new ArrayList<>();
                    final String query = "SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE table_schema = 'xdat_search' AND table_name = 'prearchive';";
                    final ResultSet results = this.pdb.executeQuery(null, query, null);
                    while (results.next()) {
                        existing.add(results.getString("column_name").toLowerCase());
                    }

                    // Now find out what's SUPPOSED to be in the table.
                    final List<String> required = new ArrayList<>(DatabaseSession.values().length);
                    for (final DatabaseSession d : DatabaseSession.values()) {
                        required.add(d.getColumnName().toLowerCase());
                    }

                    // Now check the ordinals. This is where undeclared column queries go to die, e.g. insert into table
                    // values (1, 2, 3) when the columns have actually moved. Start by checking table size. If that's
                    // off, we don't even need to check the ordering of the columns, since the column mismatch will
                    // cause ordering errors anyways.
                    boolean ordered;
                    if (required.size() == existing.size()) {
                        ordered = true;
                        for (int index = 0; index < required.size(); index++) {
                            if (!required.get(index).equals(existing.get(index))) {
                                ordered = false;
                                break;
                            }
                        }
                    } else {
                        ordered = false;
                    }

                    // Now find out what the existing and required columns have in common. If the in-common columns list
                    // is the same size as the required, that means we have all of the required columns.
                    Collection inCommon = CollectionUtils.intersection(required, existing);
                    boolean allRequiredExist = inCommon.size() == required.size();

                    // If we have all required columns and the ordering is good, we're done, the table matches.
                    if (allRequiredExist && ordered) {
                        return null;
                    }

                    // Build the ALTER query required to sync to the required definition. First rename prearc table to
                    // a holding table.
                    final StringBuilder buffer = new StringBuilder();
                    buffer.append("ALTER TABLE ").append(PrearcDatabase.tableWithSchema).append(" RENAME TO ");
                    buffer.append(PrearcDatabase.table).append("_deprecated");
                    PoolDBUtils.ExecuteNonSelectQuery(buffer.toString(), null, null);

                    // Now create the standard prearchive table.
                    createTable();

                    // Create a list of column names with the in-common columns. These are specified on both the insert
                    // and select to match mis-ordered columns in the query results. This is what does the migration
                    // mapping for us so that the ordinality of the table structure matches the expectations of the
                    // later INSERT queries. So we remove required columns that aren't in-common because we can't
                    // migrate those.
                    if (!allRequiredExist) {
                        List<String> removals = new ArrayList<>();
                        for (String column : required) {
                            if (!inCommon.contains(column)) {
                                removals.add(column);
                            }
                        }
                        if (removals.size() > 0) {
                            required.removeAll(removals);
                        }
                    }

                    String columns = StringUtils.join(required.toArray(), ", ");

                    // Clear the query and create an insert that will select all of the in-common columns from the
                    // holding table and put them into the new prearchive table.
                    buffer.setLength(0);
                    buffer.append("INSERT INTO ").append(PrearcDatabase.tableWithSchema).append(" (").append(columns).append(")");
                    buffer.append("SELECT ").append(columns).append(" FROM ").append(PrearcDatabase.tableWithSchema).append("_deprecated");
                    PoolDBUtils.ExecuteNonSelectQuery(buffer.toString(), null, null);

                    // OK, data's migrated! Great! Nuke the old table.
                    buffer.setLength(0);
                    buffer.append("DROP TABLE ").append(PrearcDatabase.tableWithSchema).append("_deprecated");
                    PoolDBUtils.ExecuteNonSelectQuery(buffer.toString(), null, null);

                    // Leave.
                    return null;
                }
            }.run();
        } catch (SessionException e) {
            logger.error("", e);
        }
    }

    /**
     * Populate the table with sessions in the prearchive directory. Should only be called once on class load.
     *
     * @throws SessionException
     * @throws SAXException
     * @throws SQLException
     * @throws IOException
     * @throws IllegalStateException
     */
    private static void populateTable() throws Exception {
        PrearcDatabase.addSessions(PrearcDatabase.sessionDelegate.get());
    }

    private static void addSessions(final Collection<SessionData> ss) throws Exception {
        new SessionOp<Void>() {
            public java.lang.Void op() throws Exception {
                PreparedStatement statement = this.pdb.getPreparedStatement(null, PrearcDatabase.insertSql());
                for (final SessionData s : ss) {
                    SessionDataTriple sdt = s.getSessionDataTriple(); // only insert if the session is not already present
                    SessionData session = getSessionIfExists(sdt.getFolderName(), sdt.getTimestamp(), sdt.getProject());

                    if (session == null) {
                        for (int i = 0; i < DatabaseSession.values().length; i++) {
                            DatabaseSession.values()[i].setInsertStatement(statement, s);
                        }
                        statement.executeUpdate();
                    } else if (!s.getStatus().equals(session.getStatus())) { // newly generated status may need to override existing status
                        setStatus(sdt.getFolderName(), sdt.getTimestamp(), sdt.getProject(), s.getStatus());
                    }
                }
                return null;
            }
        }.run();
    }

    /**
     * Add the given session to the table. Only used when initially populating the database, or when it is refreshed.
     *
     * @param s The session
     *
     * @throws SQLException
     */
    public static void addSession(final SessionData s) throws Exception {
        PrearcDatabase.checkArgs(s);
        new SessionOp<Void>() {
            public java.lang.Void op() throws Exception {
                int rowCount = PrearcDatabase.countOf(s.getFolderName(), s.getTimestamp(), s.getProject());
                if (rowCount >= 1) {
                    throw new SessionException("Trying to add an existing session");
                } else {
                    PreparedStatement statement = this.pdb.getPreparedStatement(null, PrearcDatabase.insertSql());
                    for (int i = 0; i < DatabaseSession.values().length; i++) {
                        DatabaseSession.values()[i].setInsertStatement(statement, s);
                    }
                    statement.executeUpdate();
                }
                return null;
            }
        }.run();
    }


    /**
     * Parse the given uri and return a list of sessions in the database.
     *
     * @param uri The URI from which projects should be retrieved.
     *
     * @return A list of session data objects containing the projects present at the indicated URI.
     *
     * @throws Exception Thrown if there is an issue with the database connection.
     */
    public static List<SessionData> getProjects(String uri) throws Exception {
        final PrearcUriParserUtils.ProjectsParser parser = new PrearcUriParserUtils.ProjectsParser(new PrearcUriParserUtils.UriParser(XNATApplication.PREARC_PROJECT_URI));
        final List<String> projects = parser.readUri(uri);
        return new SessionOp<List<SessionData>>() {
            public List<SessionData> op() throws Exception {
                List<SessionData> ls = new ArrayList<>();
                String sql = DatabaseSession.PROJECT.allMatchesSql(projects.toArray(new String[projects.size()]));
                ResultSet rs;
                try {
                    rs = this.pdb.executeQuery(null, sql, null);
                } catch (DBPoolException e) {
                    throw new Exception(e.getMessage());
                }
                while (rs.next()) {
                    ls.add(DatabaseSession.fillSession(rs));
                }
                return ls;
            }
        }.run();
    }

    /**
     * Parse uri and return a specific session in the database
     *
     * @param uri The URI from which to retrieve a session.
     *
     * @return The retrieved session
     *
     * @throws Exception
     */
    public static SessionData getSession(String uri) throws Exception {
        final PrearcUriParserUtils.SessionParser parser = new PrearcUriParserUtils.SessionParser(new PrearcUriParserUtils.UriParser(XNATApplication.PREARC_SESSION_URI));
        final Map<String, String> sess = parser.readUri(uri);
        return new SessionOp<SessionData>() {
            public SessionData op() throws Exception {
                return PrearcDatabase.getSession(sess.get("SESSION_LABEL"), sess.get("SESSION_TIMESTAMP"), sess.get("PROJECT_ID"));
            }
        }.run();
    }


    /**
     * Path to the project in the users prearchive directory
     *
     * @param project The project for which you want to retrieve the path.
     *
     * @return The path to the project.
     */
    public static String projectPath(String project) {
        if(project==null){
            return PrearcDatabase.prearcPath;
        }
        else {
            return Paths.get(PrearcDatabase.prearcPath, project).toString();
        }
    }

    /**
     * Generate prepared SQL statement to insert a session.
     *
     * @return The insert SQL for the current schema and table.
     */
    private static String insertSql() {
        List<String> ss = new ArrayList<>();
        for (int i = 0; i < DatabaseSession.values().length; i++) {
            ss.add("?");
        }
        return "INSERT INTO " + PrearcDatabase.tableWithSchema + " VALUES(" + StringUtils.join(ss.toArray(), ',') + ")";
    }

    /**
     * Recreate the database from scratch. This is an expensive operation.
     *
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void refresh() throws Exception {
        refresh(XDAT.getBoolSiteConfigurationProperty("reloadPrearcDatabaseOnStartup", false));
    }

    /**
     * Recreate the database from scratch. This is an expensive operation. The {@link #refresh()} version of this method
     * will only recreate the database from scratch if the {@link SiteConfigPreferences#isReloadPrearcDatabaseOnStartup()}
     * property is set to <b>true</b>. This is useful for cleaning up the table on application start-up without
     * incurring the additional overhead of a full rebuild of the prearchive database. This version lets you specify
     * <b>true</b> for the force parameter to force the delete and full rebuild of the table.
     *
     * @param force Indicates whether the table should be dropped.
     *
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void refresh(boolean force) throws Exception {
        if (force) {
            PrearcDatabase.deleteRows();
        }

        PrearcDatabase.populateTable(); // add rows to the table from the prearchive directory if not already present
        PrearcDatabase.pruneDatabase(); // remove rows from the table if they are not present in the prearchive directory
    }

    /**
     * Move a session from one project to another. 'oldProj' is allowed to be null or empty to allow moving from an unassigned project to a real one. The other arguments must be non-empty, non-null values.
     *
     * @param session       The session name
     * @param timestamp     The session timestamp
     * @param origin        The origin project of the session.
     * @param destination   Name of the new Project
     *
     * @return Return true if successful, false otherwise
     *
     * @throws SessionException
     * @throws SQLException
     * @throws Exception
     */
    private static boolean _moveToProject(final String session, final String timestamp, final String origin, final String destination) throws Exception {
        if (null == destination || destination.isEmpty()) {
            throw new SessionException("New project argument is null or empty");
        }

        final SessionData sessionData = PrearcDatabase.getSession(session, timestamp, origin);

        LockAndSync<java.lang.Void> l = new LockAndSync<java.lang.Void>(sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject(), sessionData.getStatus()) {
            java.lang.Void extSync() throws PrearcDatabase.SyncFailedException {
                PrearcDatabase.sessionDelegate.move(sessionData, destination);
                return null;
            }

            void cacheSync() throws Exception {
                PrearcDatabase.modifySession(sess, timestamp, proj, new SessionOp<Void>() {
                    public Void op() throws Exception {
                        try {
                            PrearcDatabase._unsafeDeleteSession(sess, timestamp, proj);
                            sessionData.setProject(destination);
                            sessionData.setStatus(PrearcUtils.PrearcStatus.READY);

                            final File projectF = new File(PrearcDatabase.getPrearcPath(), destination);
                            final File timestampDir = new File(projectF, timestamp);
                            final File session = new File(timestampDir, sess);
                            sessionData.setUrl(session.getAbsolutePath());

                            PrearcDatabase.addSession(sessionData);

                            PrearcUtils.log(sessionData, new Exception(String.format("Moved from %1$s to %2$s", proj, destination)));
                        } catch (SyncFailedException e) {
                            logger.error("Session sync operation failed", e);
                            throw new IllegalStateException(e.getMessage());
                        }
                        return null;
                    }
                });
            }

            @Override
            boolean checkStatus() {
                return sessionData.getStatus().equals(PrearcStatus.MOVING);
            }
        };
        boolean ran;
        Exception e = null;
        try {
            ran = l.run();
        } catch (Exception _e) {
            logger.error("", _e);
            e = _e;
            ran = false;
        }

        if (!ran) {
            wrapException(e);
        }
        return true;
    }

    /**
     * Separate a PET/MR session into separate MR and PET sessions.
     *
     * @param session       The session name
     * @param timestamp     The session timestamp
     * @param project       The origin project of the session.
     * @param petmrSession  The PET/MR session bean.
     *
     * @return Return true if successful, false otherwise
     *
     * @throws SessionException
     * @throws SQLException
     * @throws Exception
     */
    private static Map<String, SessionData> _separatePetMrSession(final String session, final String timestamp, final String project, final XnatPetmrsessiondataBean petmrSession) throws Exception {
        final SessionData sessionData = PrearcDatabase.getSession(session, timestamp, project);

        final XnatUserProvider provider = XDAT.getContextService().getBean("receivedFileUserProvider", XnatUserProvider.class);
        final UserI importer = provider.get();

        final LockAndSync<Map<String, SessionData>> l = new LockAndSync<Map<String, SessionData>>(sessionData.getName(), sessionData.getTimestamp(), sessionData.getProject(), sessionData.getStatus()) {
            @Override
            Map<String, SessionData> extSync() throws PrearcDatabase.SyncFailedException {
                final String label = petmrSession.getLabel();
                _mrSession = getUniqueSessionLabel(label, "PETMR", "MR", sessionData.getProject(), importer);
                _petSession = getUniqueSessionLabel(label, "PETMR", "PET", sessionData.getProject(), importer);
                _mrSessionTimestamp = PrearcUtils.makeTimestamp();
                do {
                    _petSessionTimestamp = PrearcUtils.makeTimestamp();
                } while (_mrSessionTimestamp.equals(_petSessionTimestamp));
                try {
                    _mrSessionFolder = PrearcUtils.getPrearcSessionDir(importer, sessionData.getProject(), _mrSessionTimestamp, _mrSession, true).getAbsolutePath();
                    _petSessionFolder = PrearcUtils.getPrearcSessionDir(importer, sessionData.getProject(), _petSessionTimestamp, _petSession, true).getAbsolutePath();
                } catch (Exception e) {
                    throw new SyncFailedException("Sync failed trying to create new session folders", e);
                }
                final Map<String, List<String>> separatedScans;
                try {
                    separatedScans = separateScans(petmrSession);
                } catch (IOException e) {
                    throw new SyncFailedException("An error occurred trying to separate the scans", e);
                }
                _mrScanIds = separatedScans.get("MR");
                _petScanIds = separatedScans.get("PT");
                PrearcDatabase.sessionDelegate.moveScans(sessionData, _mrSession, _mrSessionFolder, _mrScanIds);
                PrearcDatabase.sessionDelegate.moveScans(sessionData, _petSession, _petSessionFolder, _petScanIds);

                s = new HashMap<>();
                s.put("MR", getSessionData(_mrSessionFolder));
                s.put("PT", getSessionData(_petSessionFolder));
                return s;
            }

            @Override
            void cacheSync() throws Exception {
                PrearcDatabase.modifySession(sess, timestamp, proj, new SessionOp<Void>() {
                    public Void op() throws Exception {
                        SessionData mrSessionData = s.get("MR");
                        SessionData petSessionData = s.get("PT");
                        try {
                            PrearcDatabase.addSession(mrSessionData);
                            final File mrSessionDir = new File(_mrSessionFolder);
                            PrearcDatabase.setStatus(mrSessionDir.getName(), _mrSessionTimestamp, project, PrearcUtils.PrearcStatus.BUILDING);
                            PrearcDatabase.buildSession(mrSessionDir, mrSessionDir.getName(), _mrSessionTimestamp, project, sessionData.getVisit(), sessionData.getProtocol(), sessionData.getTimeZone(), sessionData.getSource());
                            PrearcUtils.resetStatus(importer, project, _mrSessionTimestamp, mrSessionDir.getName(), true);
                            PrearcUtils.log(mrSessionData, String.format("Moved %d scans from %s to %s", _mrScanIds.size(), sessionData.getName(), _mrSession));

                            PrearcDatabase.addSession(petSessionData);
                            final File petSessionDir = new File(_petSessionFolder);
                            PrearcDatabase.setStatus(petSessionDir.getName(), _petSessionTimestamp, project, PrearcUtils.PrearcStatus.BUILDING);
                            PrearcDatabase.buildSession(petSessionDir, petSessionDir.getName(), _petSessionTimestamp, project, sessionData.getVisit(), sessionData.getProtocol(), sessionData.getTimeZone(), sessionData.getSource());
                            PrearcUtils.resetStatus(importer, project, _petSessionTimestamp, petSessionDir.getName(), true);
                            PrearcUtils.log(petSessionData, String.format("Moved %d scans from %s to %s", _petScanIds.size(), sessionData.getName(), _petSession));
                        } catch (SyncFailedException e) {
                            logger.error("Session sync failed", e);
                            throw new IllegalStateException(e.getMessage());
                        } finally {
                            if (mrSessionData != null && petSessionData != null) {
                                PrearcDatabase._unsafeDeleteSession(sess, timestamp, proj);
                            }
                        }
                        return null;
                    }
                });
            }

            @Override
            boolean checkStatus() {
                return sessionData.getStatus().equals(PrearcStatus.SEPARATING);
            }

            private SessionData getSessionData(final String folder) {
                final File path = Paths.get(folder).toFile();
                final SessionData newSessionData = new SessionData();
                newSessionData.setName(path.getName());
                newSessionData.setFolderName(path.getName());
                newSessionData.setSubject(sessionData.getSubject());
                newSessionData.setProject(path.getParentFile().getParentFile().getName());
                newSessionData.setUrl(path.getAbsolutePath());
                newSessionData.setUploadDate(sessionData.getUploadDate());
                newSessionData.setTimestamp(path.getParentFile().getName());
                newSessionData.setScan_date(sessionData.getScan_date());
                newSessionData.setScan_time(sessionData.getScan_time());
                newSessionData.setTag(sessionData.getTag());
                newSessionData.setProtocol(sessionData.getProtocol());
                newSessionData.setSource(sessionData.getSource());
                newSessionData.setVisit(sessionData.getVisit());
                newSessionData.setTimeZone(sessionData.getTimeZone());
                newSessionData.setAutoArchive(sessionData.getAutoArchive());
                newSessionData.setPreventAnon(sessionData.getPreventAnon());
                newSessionData.setPreventAutoCommit(sessionData.getPreventAutoCommit());
                newSessionData.setStatus(PrearcStatus.READY);
                return newSessionData;
            }

            String _mrSession;
            String _petSession;
            String _mrSessionTimestamp;
            String _petSessionTimestamp;
            String _mrSessionFolder;
            String _petSessionFolder;
            List<String> _mrScanIds;
            List<String> _petScanIds;
        };

        boolean ran;
        Exception e = null;
        try {
            ran = l.run();
        } catch (Exception _e) {
            logger.error("", _e);
            e = _e;
            ran = false;
        }

        if (!ran) {
            wrapException(e);
            return null;
        }

        return l.s;
    }

    public static SeriesImportFilter getSplitPetMrSessionsFilter() throws IOException {
        final ScriptService service = XDAT.getContextService().getBean(ScriptService.class);
        final Script script = service.getByScriptId(SPLIT_PETMR_SESSION_ID);
        final String content;
        if (script == null) {
            content = DEFAULT_SPLIT_PETMR_SESSION_FILTER;
        } else {
            content = script.getContent();
        }
        final LinkedHashMap<String, String> keys = getSerializer().deserializeJson(content, SeriesImportFilter.MAP_TYPE_REFERENCE);
        return DicomFilterService.buildSeriesImportFilter(keys);
    }

    private static SerializerService getSerializer() {
        if (_serializer == null) {
            _serializer = XDAT.getContextService().getBean(SerializerService.class);
        }
        return _serializer;
    }

    static String getUniqueSessionLabel(final String stem, final String target, final String replacement, final String projectId, final UserI user) {
        String label;
        if (stem.contains(target.toUpperCase())) {
            label = stem.replace(target.toUpperCase(), replacement.toUpperCase());
        } else if (stem.contains(target.toLowerCase())) {
            label = stem.replace(target.toLowerCase(), replacement.toLowerCase());
        } else {
            label = stem + "_" + replacement;
        }

        int index = 0;
        while (XnatExperimentdata.GetExptByProjectIdentifier(projectId, getExperimentId(label, index), user, false) != null) {
            index++;
        }

        return getExperimentId(label, index);
    }

    private static String getExperimentId(String label, int index) {
        return label + (index == 0 ? "" : Integer.toString(index));
    }

    private static Map<String, List<String>> separateScans(final XnatPetmrsessiondataBean petmrSession) throws IOException {
        final Map<String, List<String>> scansByModality = new HashMap<>();
        scansByModality.put("MR", new ArrayList<String>());
        scansByModality.put("PT", new ArrayList<String>());

        final SeriesImportFilter splitPetMrSessionFilter = getSplitPetMrSessionsFilter();

        final List<XnatImagescandataI> scans = petmrSession.getScans_scan();
        if (logger.isDebugEnabled()) {
            logger.debug("Processing {} scans from the PET/MR session {} from the scanner {} in the project {}", scans.size(), petmrSession.getId(), petmrSession.getScanner(), petmrSession.getProject());
        }
        for (final XnatImagescandataI scan : scans) {
            final String index = scan.getId();
            final String modality = getScanModality(scan);
            final String description = scan.getSeriesDescription();
            if (logger.isDebugEnabled()) {
                logger.debug("Processing scan {} with modality {} and description {}", index, modality, description);
            }
            final Map<String, String> headers = new HashMap<>();
            headers.put("SeriesNumber", index);
            headers.put("Modality", modality);
            headers.put("SeriesDescription", description);
            final String foundModality = splitPetMrSessionFilter.findModality(headers);
            if (StringUtils.isNotEmpty(foundModality)) {
                final List<String> foundScans = scansByModality.get(foundModality);
                if (foundScans != null) {
                    foundScans.add(index);
                } else {
                    logger.warn("Session " + petmrSession.getLabel() + " scan " + scan.getId() + "\"" + scan.getSeriesDescription() + "\" has a modality that didn't map to MR or PET according to the split PET/MR session series import filter: " + foundModality);
                }
            }
        }
        return scansByModality;
    }

    // TODO: This should use modality-mapped series import filters to define the appropriate modality.
    private static String getScanModality(final XnatImagescandataI scan) {
        if (!StringUtils.isBlank(scan.getModality())) {
            return scan.getModality();
        }
        if (scan instanceof XnatPetscandataI) {
            return "PT";
        }
        return "MR";
    }

    @SuppressWarnings("unused")
    private static void copySessionMetaData(final XnatPetmrsessiondataBean petmrSession, final XnatPetsessiondataBean petSession, final XnatMrsessiondataBean mrSession) {
        final Method[] methods = XnatPetmrsessiondataBean.class.getMethods();
        for (final Method method : methods) {
            if (method.isAccessible() && method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                try {
                    final Object value = method.invoke(petmrSession);
                    invokeSetter(method, petSession, value);
                    invokeSetter(method, mrSession, value);
                } catch (IllegalAccessException e) {
                    // This shouldn't happen since we're checking for accessibility, but still...
                    logger.warn("Illegal access of method " + method.getName(), e);
                } catch (InvocationTargetException e) {
                    logger.warn("An error occurred invoking method " + method.getName(), e);
                }
            }
        }
        petSession.setId(petmrSession.getId().replace("PETMR", "PET"));
        petSession.setLabel(petmrSession.getId().replace("PETMR", "PET"));
        mrSession.setId(petmrSession.getId().replace("PETMR", "MR"));
        mrSession.setLabel(petmrSession.getLabel().replace("PETMR", "MR"));

        // TODO: Fill this in.
    }

    private static void invokeSetter(final Method method, final Object target, final Object value) {
        try {
            final Method setter = target.getClass().getMethod(method.getName().replaceFirst("get", "set"), method.getReturnType());
            setter.invoke(value);
        } catch (NoSuchMethodException ignored) {
            // This is totally OK: it just means that, e.g., the MR session bean doesn't have one of the PET properties or vice versa.
        } catch (IllegalAccessException e) {
            // This shouldn't happen since we're checking for accessibility, but still...
            logger.warn("Illegal access of method " + method.getName(), e);
        } catch (InvocationTargetException e) {
            logger.warn("An error occurred invoking method " + method.getName(), e);
        }
    }

    private static void pruneDatabase() throws Exception {
        // construct list of timestamps with extant folders
        Set<String> timestamps = PrearcDatabase.getPrearchiveFolderTimestamps();
        // delete all prearchive entries that are not in that timestamp set
        PrearcDatabase.deleteUnusedPrearchiveEntries(timestamps);
    }

    private static Set<String> getPrearchiveFolderTimestamps() {
        final Set<String> timestamps = new HashSet<>();
        timestamps.add("0"); // there must be at least one element in the list
        final File baseDir = new File(prearcPath);
        if (!baseDir.exists()) {
            final boolean success = baseDir.mkdirs();
            if (!success) {
                throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Couldn't create the base prearchive folder in " + baseDir.getPath());
            }
            // One thing we know: if we had to create this folder, there ain't anything in it.
            return timestamps;
        }
        final File[] dirs = baseDir.listFiles(FileSystemSessionTrawler.hiddenAndDatabaseFileFilter);
        if (dirs != null) {
            for (final File dir : dirs) {
                timestamps.add(dir.getName());
                final String[] prearchives = dir.list();
                if (prearchives != null) {
                    timestamps.addAll(Arrays.asList(prearchives));
                }
            }
        }
        return timestamps;
    }

    private static void deleteUnusedPrearchiveEntries(Set<String> timestamps) throws Exception {
        final StringBuilder sb = new StringBuilder();
        for (final String timestamp : timestamps) {
            sb.append("'").append(timestamp.replaceAll("'", "''")).append("'").append(',');
        }
        final String usedSessionTimestamps = sb.deleteCharAt(sb.length() - 1).toString();
        new SessionOp<Void>() {
            public Void op() throws Exception {
                PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.deleteUnusedSessionsSql(usedSessionTimestamps), null, null);
                return null;
            }
        }.run();
    }

    public static boolean moveToProject(final String sess, final String timestamp, final String proj, final String destination) throws Exception {
        final SessionData sessionData = PrearcDatabase.getSession(sess, timestamp, proj);
        final String project = sessionData.getProject();
        if (!sessionData.getStatus().equals(PrearcStatus._MOVING) && markSession(sessionData.getSessionDataTriple(), PrearcStatus.MOVING)) {
            if (!project.equals(destination)) {
                PrearcDatabase._moveToProject(sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject(), destination);
                return true;
            } else {
                // cannot move a session back on itself.
                markSession(sessionData.getSessionDataTriple(), PrearcStatus.READY);
                return false;
            }
        } else {
            return false;
        }
    }

    public static Map<String, SessionData> separatePetMrSession(final String session, final String timestamp, final String project, final XnatPetmrsessiondataBean petmrSession) throws Exception {
        final SessionData sessionData = getSession(session, timestamp, project);
        if (!sessionData.getStatus().equals(PrearcStatus._SEPARATING) && markSession(sessionData.getSessionDataTriple(), PrearcStatus.SEPARATING)) {
            return PrearcDatabase._separatePetMrSession(session, timestamp, project, petmrSession);
        } else {
            // Something weird happened...
            logger.error("Couldn't separate the session {}, not sure what happened.", sessionData.getUrl());
            markSession(sessionData.getSessionDataTriple(), PrearcStatus.READY);
            return null;
        }
    }

    /**
     * Move a session from the prearchive to the archive.
     *
     * @param sessions              The sessions to archive
     * @param overrideExceptions    Whether archiving should continue if there's an exception.
     * @param allowSessionMerge     Whether sessions should be merged.
     * @param overwriteFiles        Whether existing session files should be overwritten.
     * @param user                  The requesting user.
     * @param listeners             Any listeners to be notified.
     *
     * @return Return true if successful, false otherwise
     *
     * @throws Exception
     * @throws SQLException
     * @throws SessionException
     * @throws SyncFailedException
     * @throws IllegalStateException
     */
    public static Map<SessionDataTriple, Boolean> archive(final List<PrearcSession> sessions, final Boolean overrideExceptions, final Boolean allowSessionMerge, final Boolean overwriteFiles, final UserI user, final Set<StatusListenerI> listeners) throws Exception {
        List<SessionDataTriple> ss = new ArrayList<>();

        for (PrearcSession map : sessions) {
            ss.add(SessionDataTriple.fromPrearcSession(map));
        }

        final Map<SessionDataTriple, Boolean> ret = PrearcDatabase.markSessions(ss, PrearcUtils.PrearcStatus.ARCHIVING);
        new Thread() {
            public void run() {
                for (final PrearcSession session : sessions) {
                    try {
                        PrearcDatabase._archive(session, overrideExceptions, allowSessionMerge, overwriteFiles, user, listeners, true);
                    } catch (SyncFailedException e) {
                        logger.error("", e);
                    }
                }
            }
        }.start();
        return ret;
    }

    public static String archive(PrearcSession session, final Boolean overrideExceptions, final Boolean allowSessionMerge, final Boolean overwriteFiles, UserI user, Set<StatusListenerI> listeners) throws SyncFailedException {
        return PrearcDatabase._archive(session, overrideExceptions, allowSessionMerge, overwriteFiles, user, listeners, false);
    }

    private static String _archive(PrearcSession session, final Boolean overrideExceptions, final Boolean allowSessionMerge, final Boolean overwriteFiles, UserI user, Set<StatusListenerI> listeners, boolean waitFor) throws SyncFailedException {
        final String prearcDIR = session.getFolderName();
        final String timestamp = session.getTimestamp();
        final String project = session.getProject();

        final PrearcSessionArchiver archiver;
        try {
            archiver = Archiver.buildArchiver(session, overrideExceptions, allowSessionMerge, overwriteFiles, user, waitFor);
        } catch (Exception e1) {
            PrearcUtils.log(project, timestamp, prearcDIR, e1);
            throw new IllegalStateException(e1);
        }

        ListenerUtils.addListeners(listeners, archiver);

        final SessionData sd;
        try {
            sd = session.getSessionData();
        } catch (Exception e) {
            PrearcUtils.log(project, timestamp, prearcDIR, e);
            throw new IllegalStateException(e);
        }

        LockAndSync<String> l = new LockAndSync<String>(prearcDIR, timestamp, project, sd.getStatus()) {
            String extSync() throws SyncFailedException {
                try {
                    return archiver.call();
                } catch (Exception e) {
                    throw new SyncFailedException(e.getMessage(), e);
                }
            }

            void cacheSync() throws Exception {
                PrearcDatabase.modifySession(sess, timestamp, proj, new SessionOp<Void>() {
                    public Void op() throws Exception {
                        PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.deleteSessionSql(sess, timestamp, proj), null, null);
                        return null;
                    }
                });
            }

            @Override
            boolean checkStatus() {
                return sd.getStatus().equals(PrearcStatus.ARCHIVING);
            }
        };
        boolean ran;
        Exception e = null;
        try {
            ran = l.run();

        } catch (Exception _e) {
            logger.error("", _e);
            PrearcUtils.log(sd, _e);
            e = _e;
            ran = false;
        }

        if (!ran) {
            wrapException(e);
        }
        return l.s;
    }

    public static void wrapException(Exception e) throws SyncFailedException {
        if ((e instanceof SyncFailedException && e.getCause() != null)) {
            throw new SyncFailedException("Operation Failed: " + e.getCause().getMessage(), e.getCause());
        } else {
            throw new SyncFailedException("Operation Failed: " + e.getMessage(), e);
        }
    }

    public static void buildSession(final File sessionDir, final String session, final String timestamp, final String project, final String visit, final String protocol, final String timezone, final String source) throws Exception {
        final SessionData sd = PrearcDatabase.getSession(session, timestamp, project);

        try {
            new LockAndSync<Void>(session, timestamp, project, sd.getStatus()) {
                Void extSync() throws SyncFailedException {
                    final Map<String, String> params = Maps.newLinkedHashMap();
                    if (!Strings.isNullOrEmpty(project) && !PrearcUtils.COMMON.equals(project)) {
                        params.put("project", project);
                        params.put("separatePetMr", PrearcUtils.getSeparatePetMr(project));
                    } else {
                        params.put("separatePetMr", PrearcUtils.getSeparatePetMr());
                    }
                    params.put("label", session);
                    final String subject = sd.getSubject();
                    if (!Strings.isNullOrEmpty(subject)) {
                        params.put("subject_ID", sd.getSubject());
                    }
                    if (!Strings.isNullOrEmpty(visit)) {
                        params.put("visit", visit);
                    }
                    if (!Strings.isNullOrEmpty(protocol)) {
                        params.put("protocol", protocol);
                    }
                    if (!Strings.isNullOrEmpty(timezone)) {
                        params.put("TIMEZONE", timezone);
                    }
                    if (!Strings.isNullOrEmpty(source)) {
                        params.put("SOURCE", source);
                    }

                    PrearcUtils.cleanLockDirs(sd.getSessionDataTriple());

                    try {
                        final Boolean r = new XNATSessionBuilder(sessionDir, new File(sessionDir.getPath() + ".xml"), true, params).call();
                        if (!r) {
                            throw new SyncFailedException("Error building session");
                        }
                    } catch (SyncFailedException e) {
                        throw e;
                    } catch (Throwable t) {
                        final SyncFailedException e = new SyncFailedException("Error building session");
                        e.initCause(t);
                        throw e;
                    }
                    return null;
                }

                void cacheSync() {
                }

                @Override
                boolean checkStatus() {
                    return sd.getStatus().equals(PrearcStatus.BUILDING);
                }
            }.run();
        }
        // cacheSync is empty so it can't throw an exception
        catch (SQLException | SessionException ignored) {
        }
    }

    protected static boolean markSession(SessionDataTriple ss, PrearcUtils.PrearcStatus s) throws Exception {
        return PrearcDatabase.setStatus(ss.getFolderName(), ss.getTimestamp(), ss.getProject(), s);
    }

    protected static Map<SessionDataTriple, Boolean> markSessions(List<SessionDataTriple> ss, PrearcUtils.PrearcStatus s) throws Exception {
        java.util.Iterator<SessionDataTriple> i = ss.iterator();
        Map<SessionDataTriple, Boolean> ret = new HashMap<>();
        while (i.hasNext()) {
            SessionDataTriple t = i.next();
            ret.put(t, PrearcDatabase.markSession(t, s));
        }
        return ret;
    }

    /**
     * Queue a list of sessions to move to a new project.
     *
     * @param ss         The list of triples identifying the sessions to be moved.
     * @param newProj    The destination project.
     *
     * @return A map of the given sessions and flag that indicates whether the session was successfully queued.
     *
     * @throws SQLException
     * @throws SessionException
     * @throws SyncFailedException
     */
    public static Map<SessionDataTriple, Boolean> moveToProject(final List<SessionDataTriple> ss, final String newProj) throws Exception, SyncFailedException, IllegalStateException {
        final Map<SessionDataTriple, Boolean> ret = PrearcDatabase.markSessions(ss, PrearcUtils.PrearcStatus.MOVING);
        new Thread() {
            public void run() {
                for (final SessionDataTriple session : ss) {
                    try {
                        if (!session.getProject().equals(newProj)) {
                            PrearcDatabase._moveToProject(session.getFolderName(), session.getTimestamp(), session.getProject(), newProj);
                        } else {
                            PrearcDatabase.markSession(session, PrearcUtils.PrearcStatus.READY);
                        }
                    } catch (SyncFailedException e) {
                        logger.error("Session sync failed", e);
                    } catch (Exception e) {
                        logger.error("An error occurred", e);
                    }
                }
            }
        }.start();
        return ret;
    }

    /**
     * Queue a list of sessions for deletion.
     *
     * @param ss    The sessions to be deleted.
     *
     * @return A map of the given sessions and flag that indicates whether the session was successfully queued.
     *
     * @throws SQLException
     * @throws SessionException
     * @throws SyncFailedException
     * @throws IllegalStateException Thrown if any session fails to sync with the cache, irrecoverable because it indicates that the prearchive directory is in a bad state requiring manual intervention.
     */
    public static Map<SessionDataTriple, Boolean> deleteSession(final List<SessionDataTriple> ss) throws Exception, SyncFailedException, IllegalStateException {
        Map<SessionDataTriple, Boolean> ret = PrearcDatabase.markSessions(ss, PrearcUtils.PrearcStatus.DELETING);
        new Thread() {
            public void run() {
                for (final SessionDataTriple _s : ss) {
                    try {
                        PrearcDatabase._deleteSession(_s.getFolderName(), _s.getTimestamp(), _s.getProject());
                    } catch (SyncFailedException e) {
                        logger.error("Session sync failed", e);
                    } catch (Exception e) {
                        logger.error("An error occurred trying to delete the session", e);
                    }
                }
            }
        }.start();
        return ret;
    }

    /**
     * Move to project by uri.
     *
     * @param uri    The project URI.
     *
     * @return Whether the move was successful.
     *
     * @throws SessionException
     * @throws SyncFailedException
     * @throws SQLException
     */
    public static boolean moveToProject(String uri) throws Exception, SQLException {
        final PrearcUriParserUtils.SessionParser parser = new PrearcUriParserUtils.SessionParser(new PrearcUriParserUtils.UriParser(XNATApplication.PREARC_SESSION_URI));
        Map<String, String> sess = parser.readUri(uri);
        return PrearcDatabase.moveToProject(sess.get("SESSION_LABEL"), sess.get("SESSION_TIMESTAMP"), sess.get("PROJECT_ID"), parser.i.f.getValues("dest"));
    }


    /**
     * Set the status of an existing session. All arguments must be non-null and non-empty. Allows the user to set an inprocess status (i.e a status that begins with '_')
     *
     * @param sess          Session label.
     * @param timestamp     The session timestamp.
     * @param proj          Project name.
     * @param status        Status to be set.
     *
     * @throws SQLException
     * @throws SessionException
     */
    public static boolean setStatus(final String sess, final String timestamp, final String proj, final PrearcUtils.PrearcStatus status) throws Exception {
        return setStatus(sess, timestamp, proj, status, false);
    }

    /**
     * Set the status of an existing session. All arguments must be non-null and non-empty. Allows the user to set an inprocess status (i.e a status that begins with '_')
     *
     * @param sess          Session label.
     * @param timestamp     The session timestamp.
     * @param proj          Project name.
     * @param status        Status to be set.
     *
     * @return True if the status was set properly, false otherwise.
     *
     * @throws SQLException
     * @throws SessionException
     */
    public static boolean setStatus(final String sess, final String timestamp, final String proj, final PrearcUtils.PrearcStatus status, boolean overrideLock) throws Exception {
        if (!overrideLock && PrearcDatabase.isLocked(sess, timestamp, proj)) {
            return false;
        }
        PrearcDatabase.unsafeSetStatus(sess, timestamp, proj, status);
        return true;
    }

    /**
     * Set the status of a session, accept the status as a string and before setting it first check that the given status isn't one that can lock a session (i.e begins with '_').
     * <p/>
     * However a status of "_RECEIVING" is allowed because it allows the sys admin to lock a session directory if they need to mess with it manually.
     *
     * @return True if the status was set properly, false otherwise.
     */
    public static boolean setStatus(final String sess, final String timestamp, final String proj, final String status) throws Exception {
        PrearcUtils.PrearcStatus p = PrearcUtils.PrearcStatus.valueOf(status);
        if (p != null) {
            if (PrearcUtils.inProcessStatusMap.containsValue(p)) {
                throw new SessionException("Cannot set session status to " + status);
            } else {
                return PrearcDatabase.setStatus(sess, timestamp, proj, p);
            }
        } else {
            throw new SessionException("Status " + status + " is not a legitimate status");
        }
    }

    /**
     * Set the status of an existing session. No check is performed to see if the database is locked. Allows the user to set an inprocess status (i.e a status that begins with '_')
     *
     * @param sess          Session label.
     * @param timestamp     The session timestamp.
     * @param proj          Project name.
     * @param status        Status to be set.
     *
     * @throws Exception
     * @throws SQLException
     * @throws SessionException
     */
    public static void unsafeSetStatus(final String sess, final String timestamp, final String proj, final PrearcUtils.PrearcStatus status) throws Exception {
        if (null == status) {
            throw new SessionException("Status argument is null or empty");
        }
        PrearcDatabase.modifySession(sess, timestamp, proj, new SessionOp<Void>() {
            public Void op() throws Exception {
                PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.STATUS.updateSessionSql(sess, timestamp, proj, status), null, null);
                return null;
            }
        });
    }

    /**
     * Set the status given a uri specifying the project, timestamp and session and the new status. Allows the user to set an inprocess status (i.e a status that begins with '_')
     *
     * @param uri       The project URI.
     * @param status    Status to be set.
     *
     * @return True if the status was set properly, false otherwise.
     *
     * @throws Exception
     * @throws SQLException
     * @throws SessionException
     */
    public static boolean setStatus(final String uri, final PrearcUtils.PrearcStatus status) throws Exception {
        return setStatus(uri, status, false);
    }

    /**
     * Set the status given the uri specifying the project,timestamp and session, and an override lock that will that will set status even if the session is locked. Allows the user to set an inprocess status (i.e a status that begins with '_')
     *
     * @param uri             The project URI.
     * @param status          Status to be set.
     * @param overrideLock    Whether an existing lock should be overridden.
     *
     * @return True if the status was set properly, false otherwise.
     *
     * @throws Exception
     * @throws SQLException
     * @throws SessionException
     */
    public static boolean setStatus(final String uri, final PrearcUtils.PrearcStatus status, boolean overrideLock) throws Exception {
        final PrearcUriParserUtils.SessionParser parser = new PrearcUriParserUtils.SessionParser(new PrearcUriParserUtils.UriParser(XNATApplication.PREARC_SESSION_URI));
        final Map<String, String> sess = parser.readUri(uri);
        return PrearcDatabase.setStatus(sess.get("SESSION_LABEL"), sess.get("SESSION_TIMESTAMP"), sess.get("PROJECT_ID"), status, overrideLock);
    }

    /**
     * Delete a session from the prearchive database. if the session is locked.
     *
     * @param sess         Session label.
     * @param timestamp    The session timestamp.
     * @param proj         Project name.
     *
     * @return Return true if successful, false
     *
     * @throws Exception
     * @throws SQLException
     * @throws SessionException
     * @throws SyncFailedException
     */
    public static boolean deleteCacheRow(final String sess, final String timestamp, final String proj) throws Exception, SyncFailedException {
        final SessionData sd = PrearcDatabase.getSession(sess, timestamp, proj);
        new LockAndSync<java.lang.Void>(sess, timestamp, proj, sd.getStatus()) {
            protected boolean checkStatus() {
                return PrearcStatus._DELETING.equals(this.status);
            }

            java.lang.Void extSync() throws SyncFailedException {
                return null;
            }

            void cacheSync() throws Exception {
                PrearcDatabase.modifySession(sess, timestamp, proj, new SessionOp<Void>() {
                    public java.lang.Void op() throws Exception {
                        PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.deleteSessionSql(sess, timestamp, proj), null, null);
                        return null;
                    }
                });
            }
        }.run();
        return true;
    }


    /**
     * Delete a session from the prearchive database. if the session is locked.
     *
     * @param sess         Session label.
     * @param timestamp    The session timestamp.
     * @param proj         Project name.
     *
     * @return Return true if successful, false
     *
     * @throws Exception
     * @throws SQLException
     * @throws SessionException
     * @throws SyncFailedException
     */
    private static boolean _deleteSession(final String sess, final String timestamp, final String proj) throws Exception, SyncFailedException {
        final SessionData sd = PrearcDatabase.getSession(sess, timestamp, proj);
        LockAndSync<java.lang.Void> l = new LockAndSync<java.lang.Void>(sess, timestamp, proj, sd.getStatus()) {
            protected boolean checkStatus() {
                return PrearcStatus.DELETING.equals(this.status);
            }

            java.lang.Void extSync() throws SyncFailedException {
                sessionDelegate.delete(sd);
                return null;
            }

            void cacheSync() throws Exception {
                PrearcDatabase.withSession(sess, timestamp, proj, new SessionOp<Void>() {
                    public java.lang.Void op() throws Exception {
                        PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.deleteSessionSql(sess, timestamp, proj), null, null);
                        return null;
                    }
                });
            }
        };

        boolean ran;
        Exception e = null;
        try {
            ran = l.run();

        } catch (Exception _e) {
            logger.error("", _e);
            e = _e;
            ran = false;
        }

        if (!ran) {
            wrapException(e);
        }
        return true;
    }

    private static boolean _unsafeDeleteSession(final String sess, final String timestamp, final String proj) throws Exception, SyncFailedException {
        final SessionData sd = PrearcDatabase.getSession(sess, timestamp, proj);
        new LockAndSync<java.lang.Void>(sess, timestamp, proj, sd.getStatus()) {
            protected boolean checkStatus() {
                return true;
            }

            java.lang.Void extSync() throws SyncFailedException {
                sessionDelegate.delete(sd);
                return null;
            }

            void cacheSync() throws Exception {
                PrearcDatabase.modifySession(sess, timestamp, proj, new SessionOp<Void>() {
                    public java.lang.Void op() throws Exception {
                        PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.deleteSessionSql(sess, timestamp, proj), null, null);
                        return null;
                    }
                });
            }
        }.run();
        return true;
    }

    /**
     * Delete the prearchive row with the given session, timestamp,project triple
     *
     * @param sess         Session label.
     * @param timestamp    The session timestamp.
     * @param proj         Project name.
     *
     * @return True if the session was deleted properly, false otherwise.
     *
     * @throws Exception
     * @throws SQLException
     * @throws SessionException
     * @throws SyncFailedException
     */
    public static boolean deleteSession(final String sess, final String timestamp, final String proj) throws Exception, SessionException, SyncFailedException {
        Either<Void, Void> result = new PredicatedOp<Void, Void>() {
            SessionData sd;

            @Override
            boolean predicate() throws Exception {
                sd = PrearcDatabase.getSessionIfExists(sess, timestamp, proj);
                return (sd != null &&
                        !sd.getStatus().equals(PrearcStatus.DELETING) &&
                        markSession(sd.getSessionDataTriple(), PrearcStatus.DELETING));
            }

            @Override
            Either<Void, Void> trueOp() throws Exception {
                PrearcDatabase._deleteSession(sess, timestamp, proj);
                return new Either<Void, Void>() {
                }.setRight(null);
            }

            @Override
            Either<Void, Void> falseOp() throws Exception {
                return new Either<Void, Void>() {
                }.setLeft(null);
            }

        }.run();

        return !result.isLeft();
    }

    /**
     * Abstract holding results of a binary choice. Inspired by Haskell's Either datatype the Left branch is typically used to store the results of an error and the Right branch stores the results of a successful operation.
     * <p/>
     * The user of this class needs to make sure that only one of the branches is set.
     *
     * @param <Left>  Return type if the left branch of tree is taken.
     * @param <Right> Return type if the right branch of the tree is taken.
     *
     * @author aditya
     */
    public static abstract class Either<Left, Right> {
        enum Eithers {LEFT, RIGHT}

        // typically the result of an error
        Left l;
        // typically the result of a successful operation
        Right r;
        // true if Right is not null, false if Left is not null.
        Eithers set;

        Either<Left, Right> setLeft(Left l) {
            this.set = Eithers.LEFT;
            this.l = l;
            return this;
        }

        Either<Left, Right> setRight(Right r) {
            this.set = Eithers.RIGHT;
            this.r = r;
            return this;
        }

        public Left getLeft() {
            return this.l;
        }

        public Right getRight() {
            return this.r;
        }

        public boolean isLeft() {
            return this.set == Eithers.LEFT;
        }

        public boolean isRight() {
            return this.set == Eithers.RIGHT;
        }
    }

    /**
     * Abstract running a operation depending on the value of a predicate. Testing the predicate and running the operation are done atomically and are thus thread-safe.
     *
     * @param <X> Return type if the predicate fails
     * @param <Y> Return type if the predicate holds
     *
     * @author aditya
     */
    static abstract class PredicatedOp<X, Y> {
        // the predicate
        abstract boolean predicate() throws Exception;

        // run if predicate holds
        abstract Either<X, Y> trueOp() throws Exception;

        // run if predicate fails
        abstract Either<X, Y> falseOp() throws Exception;

        // thread-safe driver
        synchronized Either<X, Y> run() throws Exception {
            if (predicate()) {
                return trueOp();
            } else {
                return falseOp();
            }
        }
    }

    /**
     * Retrieve a session if it exists or null.
     *
     * @param session      The session name.
     * @param timestamp    The session timestamp.
     * @param project      The project of the session.
     *
     * @return The session data if it exists, null otherwise.
     *
     * @throws SQLException
     * @throws SessionException
     * @throws Exception
     */
    public static SessionData getSessionIfExists(final String session, final String timestamp, final String project) throws SQLException, SessionException, Exception {
        Either<java.lang.Void, SessionData> result = new PredicatedOp<java.lang.Void, SessionData>() {
            /**
             * Retrieve the session for prearchive table
             */
            Either<java.lang.Void, SessionData> trueOp() throws SQLException, SessionException, Exception {
                return new Either<java.lang.Void, SessionData>() {
                }.setRight(PrearcDatabase.getSession(session, timestamp, project));
            }

            /**
             * Set the result to null
             */
            Either<java.lang.Void, SessionData> falseOp() throws SQLException, SessionException, Exception {
                return new Either<java.lang.Void, SessionData>() {
                }.setLeft(null);
            }

            /**
             * Test whether the session exists
             */
            boolean predicate() throws SQLException, SessionException, Exception {
                return PrearcDatabase.exists(session, timestamp, project);
            }
        }.run();

        if (result.isLeft()) {
            return null;
        } else {
            return result.getRight();
        }
    }

    /**
     * A class that abstracts synching of the prearchive table and the filesystem. It ensures that a session is locked before any operation and any error that occurs on the filesystem side leaves the session with a status of ERROR.
     *
     * @param <T>
     *
     * @author aditya
     */
    static abstract class LockAndSync<T> {
        final String sess, timestamp, proj;
        final PrearcStatus status;
        T s;

        /**
         * The session, timestamp, proj triple on which to run this operation
         *
         * @param sess
         * @param timestamp
         * @param proj
         * @param status
         */
        LockAndSync(String sess, String timestamp, String proj, PrearcStatus status) {
            this.sess = sess;
            this.timestamp = timestamp;
            this.proj = proj;
            this.status = status;
        }

        abstract boolean checkStatus();

        abstract T extSync() throws SyncFailedException;

        abstract void cacheSync() throws SQLException, SessionException, Exception;

        boolean run() throws SQLException, SessionException, SyncFailedException, Exception {
            try {
                if (!checkStatus()) {
                    return false;
                }
                lockSession(this.sess, this.timestamp, this.proj);
                s = extSync();
                cacheSync();
                return true;
            } catch (SQLException e) {
                logger.error("", e);
                PrearcDatabase.unLockSession(this.sess, this.timestamp, this.proj);
                PrearcUtils.log(proj, timestamp, sess, e);
                throw e;
            } catch (SessionException e) {
                logger.error("", e);
                PrearcDatabase.unLockSession(this.sess, this.timestamp, this.proj);
                PrearcUtils.log(proj, timestamp, sess, e);
                throw e;
            } catch (SyncFailedException e) {
                logger.error("", e);

                PrearcDatabase.unLockSession(this.sess, this.timestamp, this.proj);
                if ((e.cause != null && (e.cause instanceof ClientException) && Status.CLIENT_ERROR_CONFLICT.equals(((ClientException) e.cause).getStatus()))) {
                    //if this failed due to a conflict
                    PrearcDatabase.setStatus(sess, timestamp, proj, PrearcUtils.PrearcStatus.CONFLICT);
                    PrearcUtils.log(proj, timestamp, sess, e.cause);
                } else {
                    PrearcDatabase.setStatus(sess, timestamp, proj, PrearcUtils.PrearcStatus.ERROR);
                    PrearcUtils.log(proj, timestamp, sess, (e.cause != null) ? e.cause : e);
                }
                throw e;
            } catch (Exception e) {
                logger.error("", e);
                PrearcDatabase.unLockSession(this.sess, this.timestamp, this.proj);
                PrearcUtils.log(proj, timestamp, sess, e);
                throw e;
            }
        }
    }

    /**
     * A URI decoding wrapper around {@link PrearcDatabase#isLocked(String, String, String)}
     *
     * @param uri
     *
     * @return
     *
     * @throws SQLException
     * @throws SessionException
     */
    protected static boolean isLocked(String uri) throws Exception, SQLException, SessionException {
        final PrearcUriParserUtils.SessionParser parser = new PrearcUriParserUtils.SessionParser(new PrearcUriParserUtils.UriParser(XNATApplication.PREARC_SESSION_URI));
        final Map<String, String> sess = parser.readUri(uri);
        return PrearcDatabase.isLocked(sess.get("SESSION_LABEL"), sess.get("SESSION_TIMESTAMP"), sess.get("PROJECT_ID"));
    }


    /**
     * Check to see if the sessions locked against edits.
     *
     * @param sess
     * @param timestamp
     * @param proj
     *
     * @return
     *
     * @throws SQLException
     * @throws SessionException
     */
    public static boolean isLocked(final String sess, final String timestamp, final String proj) throws Exception, SQLException, SessionException {
        SessionData sd = PrearcDatabase.getSession(sess, timestamp, proj);
        return PrearcUtils.inProcessStatusMap.containsValue(sd.getStatus());
    }

    /**
     * Reset the session status to READY
     *
     * @param uri
     *
     * @throws Exception
     * @throws SQLException
     * @throws SessionException
     */
    public static void unLockSession(String uri) throws Exception, SQLException, SessionException {
        final PrearcUriParserUtils.SessionParser parser = new PrearcUriParserUtils.SessionParser(new PrearcUriParserUtils.UriParser(XNATApplication.PREARC_SESSION_URI));
        final Map<String, String> sess = parser.readUri(uri);
        PrearcDatabase.unLockSession(sess.get("SESSION_LABEL"), sess.get("SESSION_TIMESTAMP"), sess.get("PROJECT_ID"));
    }

    protected static void unLockSession(final String sess, final String timestamp, final String proj) throws Exception, SQLException, SessionException {
        try {
            PrearcDatabase.getSession(sess, timestamp, proj);
            PrearcDatabase.unsafeSetStatus(sess, timestamp, proj, PrearcUtils.PrearcStatus.READY);
        } catch (SessionException e) {

        }
    }

    /**
     * A URI decoding wrapper around {@link PrearcDatabase#lockSession(String, String, String)}
     *
     * @param uri
     *
     * @return
     *
     * @throws SQLException
     * @throws SessionException
     */
    protected static boolean lockSession(String uri) throws Exception, SQLException, SessionException {
        final PrearcUriParserUtils.SessionParser parser = new PrearcUriParserUtils.SessionParser(new PrearcUriParserUtils.UriParser(XNATApplication.PREARC_SESSION_URI));
        final Map<String, String> sess = parser.readUri(uri);
        return PrearcDatabase.lockSession(sess.get("SESSION_LABEL"), sess.get("SESSION_TIMESTAMP"), sess.get("PROJECT_ID"));
    }

    /**
     * A database row is locked by setting its status to the "locked" version its current status. {@link PrearcUtils#inProcessStatusMap} shows the mapping.
     *
     * @param sess
     * @param timestamp
     * @param proj
     *
     * @return
     *
     * @throws SQLException
     * @throws SessionException
     */
    protected static boolean lockSession(final String sess, final String timestamp, final String proj) throws Exception, SQLException, SessionException {
        SessionData sd = PrearcDatabase.getSession(sess, timestamp, proj);
        if (PrearcUtils.inProcessStatusMap.containsKey(sd.getStatus())) {
            final PrearcUtils.PrearcStatus inp = PrearcUtils.inProcessStatusMap.get(sd.getStatus());
            PrearcDatabase.modifySession(sess, timestamp, proj, new SessionOp<Void>() {
                public java.lang.Void op() throws SQLException, SessionException, Exception {
                    PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.STATUS.updateSessionSql(sess, timestamp, proj, inp), null, null);
                    return null;
                }
            });
            return true;
        } else {
            return false;
        }
    }

    /**
     * A URI decoding wrapper for {@link PrearcDatabase#deleteSession(String, String, String)}
     *
     * @param uri
     *
     * @throws SQLException
     * @throws SessionException
     * @throws SyncFailedException
     */
    public static boolean deleteSession(String uri) throws Exception, SQLException, SessionException, SyncFailedException {
        final PrearcUriParserUtils.SessionParser parser = new PrearcUriParserUtils.SessionParser(new PrearcUriParserUtils.UriParser(XNATApplication.PREARC_SESSION_URI));
        final Map<String, String> sess = parser.readUri(uri);
        return PrearcDatabase.deleteSession(sess.get("SESSION_LABEL"), sess.get("SESSION_TIMESTAMP"), sess.get("PROJECT_ID"));
    }


    /**
     * Search for a session given its name and project.
     *
     * @param sess
     * @param proj
     *
     * @return
     *
     * @throws SQLException
     * @throws SessionException Throws if the given arguments match more than one session
     */
    public static SessionData getSession(final String sess, final String timestamp, final String proj) throws Exception, SQLException, SessionException {
        return PrearcDatabase.withSession(sess, timestamp, proj, new SessionOp<SessionData>() {
            public SessionData op() throws SQLException, Exception {
                ResultSet rs = this.pdb.executeQuery(null, DatabaseSession.findSessionSql(sess, timestamp, proj), null);
                rs.next();
                return DatabaseSession.fillSession(rs);
            }
        });
    }

    /**
     * Gets the session from a given triple.
     *
     * @param triple The triple containing the session name, timestamp, and project.
     *
     * @return The corresponding session data.
     *
     * @throws Exception When something goes wrong.
     */
    public static SessionData getSession(final SessionDataTriple triple) throws Exception {
        return getSession(triple.getFolderName(), triple.getTimestamp(), triple.getProject());
    }

    /**
     * Set the prearchive row that corresponds to the given session, timestamp, project triple to the given autoArchive setting.
     *
     * @param sess
     * @param timestamp
     * @param proj
     * @param autoArchive
     *
     * @throws Exception
     * @throws SQLException
     * @throws SessionException
     */
    public static void setAutoArchive(final String sess, final String timestamp, final String proj, final PrearchiveCode autoArchive) throws Exception, SQLException, SessionException {
        PrearcDatabase.modifySession(sess, timestamp, proj, new SessionOp<Void>() {
            public Void op() throws SQLException, SessionException, Exception {
                PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.AUTOARCHIVE.updateSessionSql(sess, timestamp, proj, autoArchive), null, null);
                return null;
            }
        });
    }

    public static void setPreventAnon(final String sess, final String timestamp, final String proj, final boolean preventAnon) throws Exception, SQLException, SessionException {
        PrearcDatabase.modifySession(sess, timestamp, proj, new SessionOp<Void>() {
            public Void op() throws SQLException, SessionException, Exception {
                PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.PREVENT_ANON.updateSessionSql(sess, timestamp, proj, preventAnon), null, null);
                return null;
            }
        });
    }

    public static void setSource(final String sess, final String timestamp, final String proj, final String source) throws Exception, SQLException, SessionException {
        PrearcDatabase.modifySession(sess, timestamp, proj, new SessionOp<Void>() {
            public Void op() throws SQLException, SessionException, Exception {
                PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.SOURCE.updateSessionSql(sess, timestamp, proj, source), null, null);
                return null;
            }
        });
    }

    public static void setPreventAutoCommit(final String sess, final String timestamp, final String proj, final boolean preventAutoCommit) throws Exception, SQLException, SessionException {
        PrearcDatabase.modifySession(sess, timestamp, proj, new SessionOp<Void>() {
            public Void op() throws SQLException, SessionException, Exception {
                PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.PREVENT_AUTO_COMMIT.updateSessionSql(sess, timestamp, proj, preventAutoCommit), null, null);
                return null;
            }
        });
    }

    /**
     * Return all sessions with the given session, timestamp and project. There should only be one row returned, but if not this function will return all the duplicate rows.
     *
     * @param sess
     * @param timestamp
     * @param proj
     *
     * @return
     *
     * @throws Exception
     * @throws SQLException
     * @throws SessionException
     */
    private static Collection<SessionData> unsafeGetSession(final String sess, final String timestamp, final String proj) throws Exception, SQLException, SessionException {
        return new SessionOp<Collection<SessionData>>() {
            public Collection<SessionData> op() throws SQLException, Exception {
                ResultSet rs = this.pdb.executeQuery(null, DatabaseSession.findSessionSql(sess, timestamp, proj), null);
                Collection<SessionData> ss = new ArrayList<SessionData>();
                while (rs.next()) {
                    ss.add(DatabaseSession.fillSession(rs));
                }
                return ss;
            }
        }.run();
    }

    /**
     * Return all sessions in the prearchive table
     *
     * @return
     *
     * @throws Exception
     * @throws SessionException
     * @throws SQLException
     */
    public static List<SessionData> getAllSessions() throws Exception, SessionException, SQLException {
        final List<SessionData> sds = new ArrayList<SessionData>();
        new SessionOp<Void>() {
            public Void op() throws SQLException, Exception {
                ResultSet rs = pdb.executeQuery(null, DatabaseSession.getAllRows(), null);
                while (rs.next()) {
                    sds.add(DatabaseSession.fillSession(rs));
                }
                return null;
            }
        }.run();
        return sds;
    }


    /**
     * Search for a session given its UID.
     *
     * @param uid
     *
     * @return
     *
     * @throws SQLException
     * @throws SessionException Thrown if the given arguments match more than one session
     */
    public static Collection<SessionData> getSessionByUID(final String uid) throws Exception, SQLException, SessionException {
        return new SessionOp<Collection<SessionData>>() {
            public Collection<SessionData> op() throws SQLException, Exception {
                List<SessionData> matches = new ArrayList<SessionData>();
                ResultSet rs = this.pdb.executeQuery(null, DatabaseSession.TAG.findSql(uid), null);
                while (rs.next()) {
                    matches.add(DatabaseSession.fillSession(rs));
                }
                return matches;
            }
        }.run();
    }

    /**
     * Count the number of session in the database with the given name associated with the given project.
     *
     * @param sess
     * @param proj
     * @param timestamp
     *
     * @return
     *
     * @throws SQLException
     * @throws SessionException
     */
    public static int countOf(final String sess, final String timestamp, final String proj) throws Exception, SQLException, SessionException {
        return new SessionOp<Integer>() {
            public Integer op() throws SQLException, SessionException, Exception {
                ResultSet rs = this.pdb.executeQuery(null, DatabaseSession.countSessionSql(sess, timestamp, proj), null);
                rs.next();
                return rs.getInt(1);
            }
        }.run();
    }

    /**
     * Either retrieve and existing session or create a new one. If a session is created an Either object with the "Right" branch set is returned. If we just retrieve one that is already in the prearchive table an Either object with the "Left" branch set is returned.
     * <p/>
     * This is useful in case the caller needs to know which operation was performed.
     *
     * @param tsFile
     * @param autoArchive
     *
     * @return
     *
     * @throws SQLException
     * @throws SessionException
     * @throws Exception
     */
    public static synchronized Either<SessionData, SessionData> eitherGetOrCreateSession(final SessionData sessionData, final File tsFile, final PrearchiveCode autoArchive) throws SQLException, SessionException, Exception {
        return new PredicatedOp<SessionData, SessionData>() {
            SessionData _sessionData;

            /**
             * Return the found session
             * (non-Javadoc)
             * @see org.nrg.xnat.helpers.prearchive.PrearcDatabase.PredicatedOp#trueOp()
             */
            Either<SessionData, SessionData> trueOp() throws SQLException, SessionException, Exception {
                return new Either<SessionData, SessionData>() {
                }.setRight(_sessionData);
            }

            /**
             * Create and return a new session
             */
            Either<SessionData, SessionData> falseOp() throws SQLException, SessionException, Exception {
                Either<SessionData, SessionData> result = new Either<SessionData, SessionData>() {
                };

                SessionData resultSession = new SessionOp<SessionData>() {
                    public SessionData op() throws SQLException, SessionException, Exception {
                        int dups = PrearcDatabase.countOf(sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject());
                        int suffix = 1;
                        String suffixString = "";
                        while (dups == 1) {
                            suffixString = "_" + suffix;
                            dups = PrearcDatabase.countOf(sessionData.getFolderName() + suffixString, sessionData.getTimestamp(), sessionData.getProject());
                            if (dups > 1) {
                                throw new SessionException("Database is in a bad state, " + dups + "sessions (name : " + sessionData.getFolderName() + " timestamp: " + sessionData.getTimestamp() + " project : " + sessionData.getProject());
                            }
                            suffix++;
                        }

                        sessionData.setFolderName(sessionData.getFolderName() + suffixString);
                        sessionData.setName(sessionData.getName() + suffixString);
                        sessionData.setUrl((new File(tsFile, sessionData.getFolderName()).getAbsolutePath()));
                        sessionData.setAutoArchive((Object) autoArchive);

                        PreparedStatement statement = this.pdb.getPreparedStatement(null, PrearcDatabase.insertSql());
                        for (int i = 0; i < DatabaseSession.values().length; i++) {
                            DatabaseSession.values()[i].setInsertStatement(statement, sessionData);
                        }
                        statement.executeUpdate();
                        return PrearcDatabase.getSession(sessionData.getFolderName(), sessionData.getTimestamp(), sessionData.getProject());
                    }
                }.run();
                result.setLeft(resultSession);
                return result;
            }

            /**
             * Test whether session exists. If it find the session the instance variable "SessionData _sessionData"
             * is initialized here.
             *
             * Originally this function initialized a "ResultSet r" instance variable and the "trueOp()" above
             * read that into a SessionData, but I kept running into "ResultSet Is Closed" errors when "trueOp()"
             * was called so I'm doing it here.
             *
             */
            boolean predicate() throws Exception {
                return new SessionOp<Boolean>() {
                    public Boolean op() throws Exception {
                        final List<String> constraints = new ArrayList<>();
                        constraints.add(DatabaseSession.PROJECT.searchSql(sessionData.getProject()));
                        constraints.add(DatabaseSession.TAG.searchSql(sessionData.getTag()));
                        constraints.add(DatabaseSession.NAME.searchSql(sessionData.getName()));

                        final ResultSet rs = pdb.executeQuery(null, DatabaseSession.findSessionSql(constraints.toArray(new String[constraints.size()])), null);
                        if (!rs.next()) {
                            if(logger.isDebugEnabled()) {
                                logger.debug("Found no existing session for " + sessionData.getSessionDataTriple().toString() + ". A new session data object will be created for data reception.");
                            }
                            return false;
                        }

                        final SessionData sessionData = DatabaseSession.fillSession(rs);

                        final PrearcStatus status = sessionData.getStatus();
                        if (PrearcStatus.RECEIVING.equals(status)|| PrearcStatus.RECEIVING_INTERRUPT.equals(status)) {
                            // Obviously if we're receiving we're fine.
                            if(logger.isDebugEnabled()) {
                                logger.debug("Receiving incoming data for session " + sessionData.getSessionDataTriple().toString() + ", which is currently in " + status + " state, which is totally fine.");
                            }
                            _sessionData = sessionData;
                            return true;
                        }
                        if (status == PrearcStatus.BUILDING) {
                            // If the session is currently building, then set this session to RECEIVING_INTERRUPT,
                            // which will allow it to continue receiving but prevent autoarchiving or session
                            // splitting afterwards.
                            if(logger.isWarnEnabled()) {
                                logger.warn("Receiving incoming data for session " + sessionData.getSessionDataTriple().toString() + " in BUILDING state, setting status to RECEIVING_INTERRUPT to block autoarchive and other operations and allow continuation of data reception.");
                            }
                            PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.updateSessionStatusSQL(sessionData.getName(), sessionData.getTimestamp(), sessionData.getProject(), PrearcStatus.RECEIVING_INTERRUPT), null, null);
                            _sessionData = sessionData;
                            return true;
                        }
                        if (status.isInterruptable()) {
                            // If the session is interruptable, which means it's not receiving but it's OK to set it to
                            // receiving (ready, in error, or in conflict), that's OK. Set to RECEIVING and return the
                            // session. Any other issues will be worked out (or re-occur) later.
                            if (logger.isInfoEnabled()) {
                                logger.info("Receiving incoming data for session " + sessionData.getSessionDataTriple().toString() + ", which is currently in the interruptable " + status + " state. Setting status to RECEIVING to allow continuation of data reception.");
                            }
                            PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.updateSessionStatusSQL(sessionData.getName(), sessionData.getTimestamp(), sessionData.getProject(), PrearcStatus.RECEIVING), null, null);
                            _sessionData = sessionData;
                            return true;
                        }
                        // If the status isn't interruptable, e.g. we're archiving or moving or deleting or whatever,
                        // then return false: we'll create a new session to receive the incoming data. This may require
                        // a merge later, but should prevent data loss.
                        if (logger.isWarnEnabled()) {
                            logger.warn("Receiving incoming data for session " + sessionData.getSessionDataTriple().toString() + ", which is currently in the non-interruptable " + status + " state. Creating a new RECEIVING session to allow continuation of data reception.");
                        }
                        return false;
                    }
                }.run();
            }
        }.run();
    }

    /**
     * Delete all the rows in the prearchive table.
     *
     * @throws SQLException
     */
    private static void deleteRows() throws Exception {
        try {
            new SessionOp<Void>() {
                public Void op() throws Exception {
                    PoolDBUtils.ExecuteNonSelectQuery("DELETE FROM " + PrearcDatabase.tableWithSchema, null, null);
                    return null;
                }
            }.run();
        } catch (SessionException e) {
            // should never happen
        }
    }

    /**
     * Debug method : Print the rows of the given ResultSet.
     *
     * @return A string containing the rows from the result set.
     *
     * @throws SQLException
     */
    private static String showRows(ResultSet rs) throws SQLException {
        StringBuilder sb = new StringBuilder();
        while (rs.next()) {
            sb.append("[");
            for (DatabaseSession d : DatabaseSession.values()) {
                sb.append(d.getColumnName());
                sb.append(":");
                String tmp = d.resultToString(rs);
                sb.append(tmp == null ? "NULL" : tmp);
                sb.append("\n");
            }
            sb.append("]");
        }
        return sb.toString();
    }

    // prevent instantiation
    private PrearcDatabase() {
    }

    /**
     * Generate SQL statement to create the table.
     *
     * @return The string containing the SQL for creating the prearchive table.
     */
    private static String createTableSql() {
        StringBuilder s = new StringBuilder();
        s.append("CREATE TABLE " + PrearcDatabase.tableWithSchema + "(");
        List<String> values = new ArrayList<>();
        for (DatabaseSession d : DatabaseSession.values()) {
            values.add(d.getColumnName() + " " + d.getColumnDefinition());
        }
        s.append(StringUtils.join(values.toArray(), ','));
        s.append(")");
        return s.toString();
    }

    /**
     * Build a list of sessions in the given projects.
     *
     * @param projects    The projects for which sessions should be retrieved.
     *
     * @return A list of lists of objects.
     *
     * @throws SQLException
     * @throws SessionException
     */
    public static ArrayList<ArrayList<Object>> buildRows(final String[] projects) throws Exception, SessionException {
        return new SessionOp<ArrayList<ArrayList<Object>>>() {
            public ArrayList<ArrayList<Object>> op() throws SQLException, SessionException, Exception {
                ArrayList<ArrayList<Object>> ao = new ArrayList<ArrayList<Object>>();
                if (projects.length > 0) {
                    ResultSet rs = this.pdb.executeQuery(null, DatabaseSession.PROJECT.allMatchesSql(projects), null);
                    ao = convertRStoList(rs);
                }
                return ao;
            }
        }.run();
    }

    /**
     * Retrieve all sessions in the prearchive table that are part of the given project
     *
     * @param project
     *
     * @return
     *
     * @throws Exception
     * @throws SQLException
     * @throws SessionException
     */
    public static ArrayList<SessionData> getSessionsInProject(final String project) throws Exception, SQLException, SessionException {
        return new SessionOp<ArrayList<SessionData>>() {
            public ArrayList<SessionData> op() throws SQLException, SessionException, Exception {
                ArrayList<SessionData> ao = new ArrayList<SessionData>();
                String[] sdr = {project};
                ResultSet rs = this.pdb.executeQuery(null, DatabaseSession.PROJECT.allMatchesSql(sdr), null);
                while (rs.next()) {
                    ao.add(DatabaseSession.fillSession(rs));
                }
                return ao;
            }
        }.run();
    }

    /**
     * Build a list of all sessions in the prearchive.
     *
     * @return
     *
     * @throws Exception
     * @throws SQLException
     * @throws SessionException
     */
    public static ArrayList<ArrayList<Object>> buildRows() throws Exception, SQLException, SessionException {
        return new SessionOp<ArrayList<ArrayList<Object>>>() {
            public ArrayList<ArrayList<Object>> op() throws SQLException, SessionException, Exception {
                ArrayList<ArrayList<Object>> ao = new ArrayList<ArrayList<Object>>();
                ResultSet rs = this.pdb.executeQuery(null, DatabaseSession.allMatchesSql(), null);
                ao = convertRStoList(rs);
                return ao;
            }
        }.run();
    }

    /**
     * Build a list of sessions in the given projects.
     *
     * @param ss
     *
     * @return
     *
     * @throws SQLException
     * @throws SessionException
     */
    public static ArrayList<ArrayList<Object>> buildRows(final Collection<SessionDataTriple> ss) throws Exception, SQLException, SessionException {
        return new SessionOp<ArrayList<ArrayList<Object>>>() {
            public ArrayList<ArrayList<Object>> op() throws SQLException, SessionException, Exception {
                ArrayList<ArrayList<Object>> ao = new ArrayList<ArrayList<Object>>();
                for (final SessionDataTriple s : ss) {
                    ResultSet rs = this.pdb.executeQuery(null, DatabaseSession.findSessionSql(s.getFolderName(), s.getTimestamp(), s.getProject()), null);
                    ao.addAll(convertRStoList(rs));
                }
                return ao;
            }
        }.run();
    }

    public static ArrayList<ArrayList<Object>> buildRows(final UserI user, final String requestedProject) throws Exception {
        final ArrayList<String> projects = PrearcUtils.getProjects(user, requestedProject);

        final String[] _proj = new String[projects.size()];

        return PrearcDatabase.buildRows(projects.toArray(_proj));
    }

    public static ArrayList<ArrayList<Object>> findMyStudy(final String patientName, final String patientID, final Date studyDate) throws Exception, SQLException, SessionException {
        return new SessionOp<ArrayList<ArrayList<Object>>>() {
            public ArrayList<ArrayList<Object>> op() throws Exception {
                PreparedStatement statement = this.pdb.getPreparedStatement(null, DatabaseSession.findMyStudySql());//patientID, patientName, studyDate
                statement.setString(1,patientID);
                statement.setString(2,patientName);
                if(studyDate!=null) {
                    statement.setDate(3, new java.sql.Date(studyDate.getTime()));
                }
                else{
                    statement.setDate(3, new java.sql.Date(0L));
                }
                ResultSet rs = statement.executeQuery();
                return convertRStoList(rs);
            }
        }.run();
    }

    private static ArrayList<ArrayList<Object>> convertRStoList(ResultSet rs) throws SQLException {
        ArrayList<ArrayList<Object>> ao = new ArrayList<ArrayList<Object>>();
        while (rs.next()) {
            ArrayList<Object> al = new ArrayList<Object>();
            for (DatabaseSession d : DatabaseSession.values()) {
                if (d.equals(DatabaseSession.URL)) {
                    final String project = DatabaseSession.PROJECT.getFromResult(rs);
                    final String timestamp = DatabaseSession.TIMESTAMP.getFromResult(rs);
                    final String session = DatabaseSession.FOLDER_NAME.getFromResult(rs);
                    al.add(String.format("/prearchive/projects/%s/%s/%s", project, timestamp, session));
                } else {
                    al.add(d.getFromResult(rs));
                }
            }
            ao.add(al);
        }
        return ao;
    }

    /**
     * Get the columns in the database table.
     *
     * @return
     *
     * @throws SQLException
     */
    public static ArrayList<String> getCols() {
        ArrayList<String> s = new ArrayList<String>();
        for (DatabaseSession d : DatabaseSession.values()) {
            s.add(d.getColumnName());
        }
        return s;
    }

    /**
     * Debug method that outputs the columns in the prearchive table
     *
     * @return
     *
     * @throws SQLException
     */
    public static String printCols() throws SQLException {
        ResultSet rs = PrearcDatabase.conn.createStatement().executeQuery("SHOW COLUMNS FROM " + PrearcDatabase.tableWithSchema);
        ArrayList<String> as = new ArrayList<String>();
        while (rs.next()) {
            as.add(rs.getString(1));
        }
        return StringUtils.join(as.toArray(new String[as.size()]), ",");
    }

    /**
     * Update the last modified time of the session to the current time.
     *
     * @param triple    The triple containing the session name, timestamp, and project.
     *
     * @throws SQLException
     * @throws SessionException
     * @throws Exception
     */
    public static void updateTimestamp(final SessionDataTriple triple) throws SQLException, SessionException, Exception {
        updateTimestamp(triple.getFolderName(), triple.getTimestamp(), triple.getProject());
    }

    /**
     * Update the last modified time of the session to the current time.
     *
     * @param sess      Session label
     * @param timestamp Timestamp directory
     * @param proj      Project name
     *
     * @throws SQLException
     * @throws SessionException
     * @throws Exception
     */
    public static void updateTimestamp(String sess, String timestamp, String proj) throws SQLException, SessionException, Exception {
        modifySession(sess, timestamp, proj, new SessionOp<java.lang.Void>() {
            public Void op() throws SQLException, Exception {
                return null;
            }
        });
    }

    /**
     * A generic class that stores a database operation on a session. It assumes that PrearcTable.conn is a valid connection, and the operations that change the database run a conn.commit() after they are done.
     *
     * @param <T> The type of data returned by the operation, use java.lang.Void of the operation returns nothing
     *
     * @author aditya
     */

    static abstract class SessionOp<T> {
        // Connection conn;
        PoolDBUtils pdb;

        public void createConnection() throws SQLException {
            // this.conn = DriverManager.getConnection("jdbc:h2:" + PrearcDatabase.prearcPath + dbName, "sa", "");
            this.pdb = new PoolDBUtils();
        }

        public void closeConnection() throws SQLException {
            this.pdb.closeConnection();
        }

        public void rollbackConnection() throws SQLException {
            // this.conn.rollback();
        }

        public abstract T op() throws SQLException, SessionException, Exception;

        public T run() throws SQLException, SessionException, Exception {
            this.createConnection();
            Object o = null;
            try {
                o = this.op();
            } catch (Exception e) {
                logger.error("", e);
                throw e;
            } finally {
                closeConnection();
            }
            return (T) o; // unchecked cast
        }
    }

    /**
     * Check that session arguments are valid and there is unique session that matches the arguments. If 'proj' is null it is assumed that the session is "Unassigned"
     *
     * @param sess
     * @param timestamp
     * @param proj
     *
     * @throws SQLException
     * @throws SessionException
     */
    private static void checkSession(String sess, String timestamp, String proj) throws Exception, SQLException, SessionException {
        PrearcDatabase.checkArgs(sess, timestamp, proj);
        PrearcDatabase.checkUniqueRow(sess, timestamp, proj);
    }

    private static void checkArgs(String sess, String timestamp, String proj) throws SQLException, SessionException {
        if (null == sess || sess.isEmpty()) {
            throw new SessionException("Session argument is null or empty");
        }
        if (null == timestamp || timestamp.isEmpty()) {
            throw new SessionException("Timestamp argument is null or empty");
        }
    }

    private static void checkArgs(SessionData s) throws SQLException, SessionException {
        PrearcDatabase.checkArgs(s.getFolderName(), s.getTimestamp(), s.getProject());
    }

    private static void checkUniqueRow(String sess, String timestamp, String proj) throws Exception, SQLException, SessionException {
        int rowCount = PrearcDatabase.countOf(sess, timestamp, proj);
        if (rowCount == 0) {
            throw new SessionException("A record with session " + sess + ", timestamp " + timestamp + " and project " + proj + " could not be found.");
        }
        if (rowCount > 1) {
            throw new SessionException("Multiple records with session " + sess + ", timestamp " + timestamp + " and project " + proj + " were found.");
        }
    }

    /**
     * Check that a session exists in the prearchive table.
     *
     * @param sess
     * @param timestamp
     * @param proj
     *
     * @return
     *
     * @throws Exception
     * @throws SQLException
     * @throws SessionException
     */
    public static boolean exists(final String sess, final String timestamp, final String proj) throws Exception, SQLException, SessionException {
        int rowCount = PrearcDatabase.countOf(sess, timestamp, proj);
        boolean b = false;
        if (rowCount == 0) {
            b = false;
        }
        if (rowCount == 1) {
            b = true;
        }
        return b;
    }


    /**
     * Check session parameters and run the operation
     *
     * @param <T>
     * @param sess
     * @param timestamp
     * @param proj
     * @param op
     *
     * @return
     *
     * @throws SQLException
     * @throws SessionException
     */

    private static <T extends Object> T withSession(String sess, String timestamp, String proj, SessionOp<T> op) throws Exception, SQLException, SessionException {
        PrearcDatabase.checkSession(sess, timestamp, proj);
        return op.run();
    }

    private static <T extends Object> T modifySession(final String sess, final String timestamp, final String proj, SessionOp<T> op) throws Exception, SQLException, SessionException {
        withSession(sess, timestamp, proj, new SessionOp<java.lang.Void>() {
            public Void op() throws SQLException, Exception {
                PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.LASTMOD.updateSessionSql(sess, timestamp, proj, Calendar.getInstance().getTime()), null, null);
                return null;
            }
        });
        return op.run();
    }

    @SuppressWarnings("serial")
    public static class SyncFailedException extends IOException {
        public Throwable cause = null;

        public SyncFailedException() {
            super();
        }

        public SyncFailedException(String message, Throwable cause) {
            super(message, cause);
            this.cause = cause;
        }

        public SyncFailedException(String message) {
            super(message);
        }

        public SyncFailedException(Throwable cause) {
            super(cause);
            this.cause = cause;
        }

    }
}
