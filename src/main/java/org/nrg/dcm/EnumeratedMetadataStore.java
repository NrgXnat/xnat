/*
 * DicomDB: org.nrg.dcm.EnumeratedMetadataStore
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.media.DicomDirReader;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.util.StringUtils;
import org.hsqldb.jdbc.JDBCDataSource;
import org.nrg.attr.ConversionFailureException;
import org.nrg.progress.NullProgressUpdater;
import org.nrg.progress.ProgressMonitorI;
import org.nrg.progress.ProgressUpdater;
import org.nrg.progress.ProgressUpdaterFactory;
import org.nrg.util.Opener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.*;

import static org.nrg.dcm.Attributes.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class EnumeratedMetadataStore implements DicomMetadataStore, Closeable {
    /**
     * A hook into adding a DICOM file addition to the database.
     *
     * @author aditya
     */
    private class FileOp {
        final Function<DicomObject, DicomObject> dicomOp;

        FileOp() {
            this.dicomOp = null;
        }

        FileOp(final Function<DicomObject, DicomObject> dicomOp) {
            this.dicomOp = dicomOp;
        }

        /**
         * Operate on a number of resources
         *
         * @param statement The database action to perform
         * @param resources The resources
         * @param progress  The progress indicator
         * @throws SQLException                   When an error occurs interacting with the database.
         * @throws IOException                    When an error occurs reading or writing data.
         * @throws UserCanceledOperationException When the user cancels the operation before completion.
         */
        void call(final Statement statement, final Iterable<URI> resources, final ProgressUpdater progress, final Map<String, String> addCols) throws SQLException, IOException, UserCanceledOperationException {
            if (this.dicomOp != null) {

                for (final URI resource : resources) {
                    if (progress.isCanceled()) {
                        throw new UserCanceledOperationException();
                    }
                    try {
                        add(statement, resource,
                                DataSetAttrs.create(resource, columns.keySet(), uriOpener),
                                addCols);
                    } catch (IOException e) {
                        logger.info("resource " + resource + " not cached", e);
                    }
                    progress.incrementProgress();
                }
            } else {
                addAll(statement, resources, addCols, progress);
            }
        }

        /**
         * Operate on a single file.
         *
         * @param statement The database action to perform.
         * @param resource  The resource to work with.
         * @param addCols   The columns to select.
         * @throws SQLException When an error occurs performing the SQL statement.
         * @throws IOException  When an error occurs accessing the resource URI.
         */
        void call(final Statement statement, final URI resource, final Map<String, String> addCols)
                throws SQLException, IOException {
            if (this.dicomOp == null) {
                add(statement, resource, addCols);
            } else {
                IOException       ioexception = null;
                final InputStream in          = uriOpener.open(resource);
                try {
                    final DicomObject o = this.dicomOp.apply(DicomUtils.read(in));
                    add(resource, o, addCols);
                } catch (IOException e) {
                    throw ioexception = e;
                } catch (RuntimeException e) {
                    final Throwable cause = e.getCause();
                    if (null != cause && cause instanceof SQLException) {
                        throw (SQLException) cause;
                    } else {
                        throw e;
                    }
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        if (null == ioexception) {
                            throw e;
                        } else {
                            logger.error("unable to close DICOM resource", e);
                            throw ioexception;
                        }
                    }
                }
            }
        }
    }

    public static final class UserCanceledOperationException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    private static final String tableName = "Attributes";
    private static final String DICOMDIR  = "DICOMDIR";
    private static final String DBPREFIX  = "org.nrg.dcm";

    private static final String DBSUFFIX       = "-db";
    private static final String PROP_RETAIN_DB = "org.nrg.DicomDB.retain-db";

    private static final String COMMIT   = "COMMIT";
    private static final String ROLLBACK = "ROLLBACK";

    private static final Map<DicomAttributeIndex, String> UNCONSTRAINED          = Collections.emptyMap();
    private static final ProgressUpdaterFactory           progressUpdaterFactory = ProgressUpdaterFactory.getFactory();

    private final static Function<File, URI> toURI = new Function<File, URI>() {
        public URI apply(final File f) {
            try {
                return DicomUtils.getQualifiedUri(f.getPath());
            } catch (URISyntaxException e) {
                return f.toURI();
            }
        }
    };

    private static DataSource buildHSQLDataSource(final Map<String, String> options,
                                                  final Collection<Closeable> closeHandlers)
            throws IOException {
        //  don't need this file, but we use the name
        final File dbf = File.createTempFile(DBPREFIX, DBSUFFIX);
        dbf.deleteOnExit(); // add delete on exit in case this thread dies

        final StringBuilder sb = new StringBuilder("jdbc:hsqldb:file:");
        sb.append(dbf.getPath());
        for (final String opt : options.keySet()) {
            sb.append(";");
            sb.append(opt);
            sb.append("=");
            sb.append(options.get(opt));
        }

        final String user     = "sa";
        final String password = "";

        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        final JDBCDataSource hsqlDataSource = new JDBCDataSource();
        hsqlDataSource.setDatabase(sb.toString());
        hsqlDataSource.setUser(user);
        hsqlDataSource.setPassword(password);

        final Collection<File> dbfiles = Lists.newArrayList();
        for (final String suffix : Arrays.asList("", ".properties", ".script", ".data", ".backup", ".log", ".lck")) {
            final File f = new File(dbf.getPath() + suffix);
            dbfiles.add(f);
        }

        if (!"true".equals(System.getProperty(PROP_RETAIN_DB))) {
            for (final File f : dbfiles) {
                f.deleteOnExit();
            }
            closeHandlers.add(new Closeable() {
                public void close() {
                    for (final File f : dbfiles) {
                        f.delete();
                    }
                }
            });
        }
        closeHandlers.add(new Closeable() {
            public void close() {
                try (final Connection c = hsqlDataSource.getConnection(); final Statement s = c.createStatement()) {
                    s.executeUpdate("SHUTDOWN IMMEDIATELY");
                } catch (SQLException e) {
                    logger.error("Unable to shut down database", e);
                }
            }
        });

        return hsqlDataSource;
    }

    /**
     * Creates an HSQLDB-backed EnumeratedMetadataStore. The backing database files are placed in
     * ${java.io.tmpdir} and are removed when the store's close() method is called, or at exit,
     * if close() is not called.
     *
     * @param indices   DICOM attributes to be stored
     * @param addCols   additional table columns to be defined
     * @param uriOpener The opener for the URI.
     * @return new, HSQLDB-backed EnumeratedMetadataStore object
     * @throws SQLException When an error occurs interacting with the database.
     * @throws IOException  When an error occurs reading or writing data.
     */
    public static EnumeratedMetadataStore createHSQLDBBacked(final Iterable<DicomAttributeIndex> indices,
                                                             final Iterable<String> addCols,
                                                             Opener<URI> uriOpener) throws IOException, SQLException {
        final Collection<Closeable>   closeHandlers = Lists.newArrayListWithExpectedSize(2);
        final DataSource              ds            = buildHSQLDataSource(new LinkedHashMap<String, String>(), closeHandlers);
        final EnumeratedMetadataStore s             = new EnumeratedMetadataStore(ds, indices, addCols, uriOpener);
        s.closeHandlers.addAll(closeHandlers);
        return s;
    }

    /**
     * Creates an HSQLDB-backed EnumeratedMetadataStore. The backing database files are placed in
     * ${java.io.tmpdir} and are removed when the store's close() method is called, or at exit,
     * if close() is not called.
     *
     * @param indices   The indices of the DICOM attributes to be stored.
     * @param uriOpener The opener for the URI.
     * @return A new, HSQLDB-backed EnumeratedMetadataStore object
     * @throws SQLException When an error occurs interacting with the database.
     * @throws IOException  When an error occurs reading or writing data.
     */
    public static EnumeratedMetadataStore createHSQLDBBacked(final Iterable<DicomAttributeIndex> indices,
                                                             Opener<URI> uriOpener)
            throws IOException, SQLException {
        return createHSQLDBBacked(indices, null, uriOpener);
    }

    private static TransferCapability[] extractTransferCapabilities(final String role, final ResultSet rs)
            throws SQLException {
        final Map<String, Set<String>> tcmap = Maps.newLinkedHashMap();
        while (rs.next()) {
            final String scuid = rs.getString(1);
            if (!tcmap.containsKey(scuid)) {
                tcmap.put(scuid, new LinkedHashSet<String>());
            }
            tcmap.get(scuid).add(rs.getString(2));
        }
        final List<TransferCapability> tcs = Lists.newArrayList();
        for (Map.Entry<String, Set<String>> e : tcmap.entrySet()) {
            tcs.add(new TransferCapability(e.getKey(), e.getValue().toArray(new String[0]), role));
        }
        return tcs.toArray(new TransferCapability[0]);
    }

    private static Iterable<URI> toURIs(final Iterable<File> files) {
        return Iterables.transform(files, toURI);
    }

    private static final Logger logger = LoggerFactory.getLogger(EnumeratedMetadataStore.class);

    private final Collection<Closeable> closeHandlers = Lists.newArrayList();

    private final DataSource dataSource;

    private final SortedMap<DicomAttributeIndex, DicomAttributeIndex> columns;

    private final Opener<URI> uriOpener;

    public EnumeratedMetadataStore(final DataSource dataSource, final Iterable<DicomAttributeIndex> indices,
                                   final Iterable<String> addCols,
                                   final Opener<URI> uriOpener)
            throws IOException, SQLException {
        this.uriOpener = uriOpener;
        columns = new TreeMap<>(
                new Comparator<DicomAttributeIndex>() {
                    public int compare(DicomAttributeIndex i0, DicomAttributeIndex i1) {
                        return i0.getColumnName().compareTo(i1.getColumnName());
                    }
                });

        columns.put(SOPClassUID, SOPClassUID);
        columns.put(TransferSyntaxUID, TransferSyntaxUID);
        columns.put(StudyInstanceUID, StudyInstanceUID);
        columns.put(SeriesInstanceUID, SeriesInstanceUID);

        for (final DicomAttributeIndex i : indices) {
            columns.put(i, i);
        }

        final StringBuilder createTable = new StringBuilder("CREATE TABLE ");
        createTable.append(tableName);
        createTable.append(" ( uri VARCHAR(4096) PRIMARY KEY");
        if (null != addCols) {
            for (final String columnName : addCols) {
                createTable.append(", ").append(columnName).append(" VARCHAR(1024)");
            }
        }
        for (final DicomAttributeIndex i : columns.keySet()) {
            createTable.append(", ").append(i.getColumnName()).append(" VARCHAR(1024)");
        }
        createTable.append(" );");
        logger.trace("SQL command: {}", createTable);

        this.dataSource = dataSource;

        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);

            statement.executeUpdate(createTable.toString());
            statement.executeUpdate("CREATE INDEX study_idx ON " + tableName + " (StudyInstanceUID);");
            statement.executeUpdate("CREATE INDEX series_idx ON " + tableName + " (SeriesInstanceUID);");
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#add(java.lang.Iterable)
     */
    public void add(final Iterable<URI> resources) throws IOException, SQLException {
        findResources(resources.iterator(), new FileOp(), new NullProgressUpdater());
    }

    public void add(Iterable<URI> resources, Function<DicomObject, DicomObject> fn)
            throws IOException, SQLException {
        findResources(resources.iterator(), new FileOp(fn), new NullProgressUpdater());
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#add(java.lang.Iterable, java.util.Map)
     */
    public void add(final Iterable<URI> resources, final Map<String, String> addCols) throws IOException, SQLException {
        findResources(resources, addCols, new NullProgressUpdater());
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#add(java.lang.Iterable, java.util.Map, org.nrg.progress.ProgressMonitorI)
     */
    public void add(final Iterable<URI> resources, final Map<String, String> addCols, final ProgressMonitorI pm)
            throws IOException, SQLException {
        findResources(resources, addCols, progressUpdaterFactory.build(resources, pm));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#add(java.lang.Iterable, org.nrg.progress.ProgressMonitorI)
     */
    public void add(final Iterable<URI> resources, ProgressMonitorI pm) throws IOException, SQLException {
        findResources(resources, null, progressUpdaterFactory.build(resources, pm));
    }

    /**
     * Uses the given Statement to add the given attributes from the given file
     * to the store. Does NOT commit the change.
     *
     * @param statement SQL Statement
     * @param resource  DICOM object resource
     * @param attrs     attributes from the file
     * @param addCols   additional column values
     * @throws SQLException When an error occurs interacting with the database.
     * @throws IOException  When an error occurs reading or writing data.
     */
    private void add(final Statement statement, final URI resource, final DataSetAttrs attrs, final Map<String, String> addCols)
            throws IOException, SQLException {
        final String insert = buildInsertStatement(resource, attrs, addCols);
        logger.trace("add: {}", insert);
        try {
            final int updated = statement.executeUpdate(insert);
            assert 1 == updated;
        } catch (SQLException ignore) {
            // most likely a multiple addition
            // TODO: can we verify this?
        }
    }

    private void add(final Statement statement, final URI resource, final Map<String, String> addCols) throws IOException, SQLException {
        add(statement, resource, DataSetAttrs.create(resource, columns.keySet(), uriOpener), addCols);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#add(java.io.File, org.dcm4che2.data.DicomObject)
     */
    public void add(final URI resource, final DicomObject o) throws IOException, SQLException {
        add(resource, o, null);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#add(java.io.File, org.dcm4che2.data.DicomObject, java.util.Map)
     */
    public void add(final URI resource, final DicomObject o, final Map<String, String> addCols) throws IOException, SQLException {
        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement()) {
            add(statement, resource, new DataSetAttrs(o, columns.keySet()), addCols);
        }
    }

    private void addAll(final Statement s, final Iterable<URI> resources, final Map<String, String> addCols, final ProgressUpdater progress) throws UserCanceledOperationException, SQLException {
        for (final URI resource : resources) {
            if (progress.isCanceled()) {
                throw new UserCanceledOperationException();
            }
            try {
                add(s, resource, DataSetAttrs.create(resource, columns.keySet(), uriOpener), addCols);
            } catch (IOException e) {
                logger.info("resource " + resource + " not cached", e);
            }
            progress.incrementProgress();
        }
    }

    private void addTableColumn(final DicomAttributeIndex index) throws SQLException {
        if (!columns.containsKey(index)) {
            final StringBuilder sb = new StringBuilder("ALTER TABLE ");
            sb.append(tableName).append(" ADD COLUMN ");
            sb.append(index.getColumnName()).append(" VARCHAR(2048)");

            try (final Connection connection = dataSource.getConnection();
                 final Statement statement = connection.createStatement()) {
                statement.executeUpdate(sb.toString());
                statement.executeUpdate(COMMIT);
                columns.put(index, index);
            }
        }
    }

    private void addTableColumns(final Iterable<DicomAttributeIndex> indices) throws SQLException {
        for (final DicomAttributeIndex index : indices) {
            addTableColumn(index);
        }
    }

    /**
     * Appends a WHERE clause for the given value constraints to the given StringBuilder
     *
     * @param sb          destination for the WHERE clause
     * @param constraints columnName->value constraints for the WHERE clause
     * @return sb
     */
    private StringBuilder appendConstraints(final StringBuilder sb,
                                            final Map<String, String> constraints) {
        if (!constraints.isEmpty()) {
            sb.append(" WHERE ");
            final Joiner joiner = Joiner.on(" AND ");
            joiner.appendTo(sb, Iterables.transform(constraints.entrySet(),
                    new Function<Map.Entry<String, String>, String>() {
                        public String apply(final Map.Entry<String, String> me) {
                            return String.format("%s='%s'", me.getKey(), me.getValue());
                        }
                    }));
        }
        return sb;
    }

    /**
     * Builds an SQL INSERT statement representing adding one resource to the database.
     *
     * @param resource URI for the resource
     * @param dsa      DataSetAttrs describing attributes to be extracted
     * @param addCols  Additional column values to set
     * @return SQL statement specifying row insertion
     */
    private String buildInsertStatement(final URI resource, final DataSetAttrs dsa,
                                        final Map<String, String> addCols) {
        final Map<String, String> assignments = Maps.newLinkedHashMap();
        for (final DicomAttributeIndex index : columns.values()) {
            try {
                final String v = dsa.get(index);
                if (null != v) {
                    assignments.put(index.getColumnName(), v);
                }
            } catch (ConversionFailureException ignored) {
            }
        }
        if (null != addCols) {
            assignments.putAll(addCols);
        }

        final StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(tableName);
        sb.append("(uri");
        for (final String col : assignments.keySet()) {
            sb.append(", ").append(col);
        }

        sb.append(") VALUES(");
        sb.append("'").append(resource.toString()).append("'");
        for (final Map.Entry<String, String> me : assignments.entrySet()) {
            sb.append(", '").append(me.getValue().replaceAll("'", "''")).append("'");
        }
        sb.append(");");
        return sb.toString();
    }

    private String buildUpdateStatement(final URI resource, final DataSetAttrs dsa) {
        // TODO: rework this to not do multiple calls to dsa.get(i)
        final Iterable<String> assignments = Iterables.transform(Iterables.filter(dsa,
                new Predicate<DicomAttributeIndex>() {
                    public boolean apply(final DicomAttributeIndex i) {
                        try {
                            return null != dsa.get(i);
                        } catch (ConversionFailureException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }),
                new Function<DicomAttributeIndex, String>() {
                    public String apply(final DicomAttributeIndex i) {
                        try {
                            return String.format("%s='%s'", i.getColumnName(), dsa.get(i));
                        } catch (ConversionFailureException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        if (Iterables.isEmpty(assignments)) {
            final StringBuilder sb = new StringBuilder("UPDATE ");
            sb.append(tableName).append(" SET ");
            Joiner.on(", ").appendTo(sb, assignments);
            sb.append(" WHERE path='").append(resource.toString().replaceAll("'", "''")).append("';");
            return sb.toString();
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    public void close() {
        for (final Closeable closeable : closeHandlers) {
            try {
                closeable.close();
            } catch (IOException e) {
                logger.error("close handler failed", e);
            }
        }
    }

    public void dumpTable(final PrintStream out) throws SQLException {
        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet results = statement.executeQuery("SELECT * FROM " + tableName)) {
            final ResultSetMetaData metaData = results.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                out.print(metaData.getColumnLabel(i));
                out.print("\t");
            }
            out.println();
            while (results.next()) {
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    out.print(results.getString(i));
                    out.print("\t");
                }
                out.println();
            }
        }
    }

    private void findResources(final Iterable<URI> resources,
                               final Map<String, String> addCols,
                               final ProgressUpdater progress) throws SQLException {
        logger.trace("findResources({}, progress)", resources);
        findResources(resources.iterator(), new FileOp(), progress, addCols);
    }

    private void findResources(final Iterator<URI> resources,
                               final FileOp fileOp,
                               final ProgressUpdater progress)
            throws SQLException {
        findResources(resources, fileOp, progress, new HashMap<String, String>());
    }

    /**
     * Walks the given resources to find DICOM objects.
     * If the resource if a file
     * If a directory contains a DICOMDIR, we stop descending that way
     *
     * @param resources The file tree
     * @param hook      The operation to perform on the file(s).
     * @param progress  The progress indicator
     * @throws SQLException When an error occurs interacting with the database.
     */
    private synchronized void findResources(final Iterator<URI> resources,
                                            final FileOp hook,
                                            final ProgressUpdater progress,
                                            final Map<String, String> addCols) throws SQLException {
        logger.trace("findDataFiles({}, ..., {}", resources, addCols);
        final Set<File> directories = Sets.newLinkedHashSet();

        progress.initialize("Scanning resources...");   // TODO: localize


        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement()) {
            try {
                while (resources.hasNext()) {
                    if (progress.isCanceled()) {
                        throw new UserCanceledOperationException();
                    }
                    final URI  uri  = resources.next();
                    final File file = getLocalFileResource(uri);
                    if (null != file && file.isDirectory()) {
                        directories.add(file);
                        progress.incrementMax();
                    } else if (null != file && DICOMDIR.equals(file.getName())) {
                        // If this is a DICOMDIR, it indexes lots of files.
                        try {
                            final DicomDirReader reader = new DicomDirReader(file);
                            try {
                                final File             dir = file.getParentFile();
                                final Collection<File> fs  = readFileSetRecords(dir, reader, reader.findFirstRootRecord());
                                progress.incrementMax(fs.size());
                                try {
                                    hook.call(statement, toURIs(fs), progress, addCols);
                                } catch (Throwable t) {
                                    logger.error("Unable to cache DICOM object: hook failed", t);
                                }
                            } finally {
                                reader.close();
                            }
                        } catch (IOException e) {
                            logger.info("DICOMDIR processing failed for " + uri, e);
                        }
                    } else {
                        try {
                            hook.call(statement, uri, addCols);
                        } catch (IOException e) {
                            logger.info("resource " + uri + " not cached", e);
                        } catch (SQLException e) {
                            throw e;
                        } catch (Throwable t) {
                            logger.error("Unable to cache DICOM object: hook failed", t);
                        }
                        progress.incrementMax();
                        progress.incrementProgress();
                    }
                }

                // Walk through the given directories and all their subdirectories.
                // Because directories is a LinkedHashSet, the iterator is FIFO (like a Queue),
                // but add()ing a directory that's already present doesn't add a second instance.
                for (Iterator<File> iterator = directories.iterator(); iterator.hasNext(); ) {
                    final File dir = iterator.next();
                    iterator.remove();

                    progress.incrementProgress();

                    try {
                        // This iterator should be only real directories, but filesystem contents change.
                        if (!dir.isDirectory() || !dir.getPath().equals(dir.getCanonicalPath())) {
                            continue;
                        }
                    } catch (IOException e) {
                        continue;
                    }

                    if (progress.isCanceled()) {
                        statement.executeUpdate(ROLLBACK);
                        return;
                    }

                    progress.setNote(dir.getPath());

                    boolean dirsAdded = false;

                    final File dcmdir = new File(dir, DICOMDIR);
                    if (dcmdir.exists()) {
                        // If there's a DICOMDIR in this directory, assume that it indexes all files
                        // in this directory and its subdirectories.
                        try {
                            final DicomDirReader reader = new DicomDirReader(dcmdir);
                            try {
                                final Collection<File> fs = readFileSetRecords(dir, reader, reader.findFirstRootRecord());
                                progress.incrementMax(fs.size());
                                hook.call(statement, toURIs(fs), progress, addCols);
                            } finally {
                                reader.close();
                            }
                        } catch (IOException e) {
                            logger.info("DICOMDIR processing failed for " + dcmdir, e);
                        }
                    } else {
                        final File[] contents = dir.listFiles();
                        if (null == contents) {
                            logger.error("Unable to list contents of {}", dir);
                        } else {
                            for (final File file : contents) {
                                try {
                                    final File cfile = file.getCanonicalFile();
                                    if (file.isDirectory()) {
                                        // Try not to follow symbolic links (this is an imperfect kludge)
                                        if (cfile.getPath().equals(file.getAbsolutePath())) {
                                            directories.add(cfile);
                                            progress.incrementMax();
                                            dirsAdded = true;
                                        }
                                    } else {
                                        hook.call(statement, toURI.apply(cfile), addCols);
                                    }
                                } catch (IOException ignore) {
                                } finally {
                                    progress.incrementProgress();
                                }
                            }

                            // If we've added any directories, need to rebuild the iterator.
                            if (dirsAdded) {
                                iterator = directories.iterator();
                            }
                        }
                    }
                }

                // Finished without user cancel or exception; commit the db changes
                statement.execute(COMMIT);
            } catch (UserCanceledOperationException e) {
                statement.execute(ROLLBACK);
            } catch (SQLException e) {
                statement.execute(ROLLBACK);
                throw e;
            } catch (RuntimeException e) {
                statement.execute(ROLLBACK);
                throw e;
            } catch (Error e) {
                statement.execute(ROLLBACK);
                throw e;
            }
        }
    }

    private File getLocalFileResource(final URI uri) {
        final File f = new File(uri.getPath());
        try {
            return f.getCanonicalFile();
        } catch (IOException e) {
            logger.warn("can't get canonical path for resource " + uri, e);
            return f.getAbsoluteFile();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#getResources()
     */
    public Set<URI> getResources() throws SQLException {
        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet rs = statement.executeQuery("SELECT uri FROM " + tableName)) {
            try {
                final Set<URI> resources = Sets.newLinkedHashSet();
                while (rs.next()) {
                    resources.add(new URI(rs.getString(1)));
                }
                return resources;
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#getResourcesForValues(java.util.Map, java.util.Map)
     */
    public Set<URI> getResourcesForValues(Map<?, String> values,
                                          Map<DicomAttributeIndex, ConversionFailureException> failed) throws IOException, SQLException {
        if (values.isEmpty()) {       // special case: no constraints means all files
            return getResources();
        }

        final Set<DicomAttributeIndex> tags        = Sets.newHashSetWithExpectedSize(values.size());
        final Map<String, String>      constraints = translateConstraints(values, tags);
        updateCache(tags, null);

        final StringBuilder sb = new StringBuilder("SELECT uri FROM ");
        sb.append(tableName);
        appendConstraints(sb, constraints);

        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet results = statement.executeQuery(sb.toString())) {
            final Set<URI> resources = Sets.newLinkedHashSet();
            while (results.next()) {
                final String uri = results.getString(1);
                if (results.wasNull()) {
                    throw new IOException("NULL path value in cache");
                }
                try {
                    resources.add(new URI(uri));
                } catch (URISyntaxException e) {
                    logger.error("uri entry not a valid URI", e);
                }
            }
            return resources;
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#getSize()
     */
    public int getSize() throws SQLException {
        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet results = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            results.next();
            return results.getInt(1);
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#getTransferCapabilities(java.lang.String, java.lang.Iterable)
     */
    public TransferCapability[] getTransferCapabilities(String role,
                                                        Iterable<URI> files) throws SQLException {
        final StringBuilder sb = new StringBuilder("SELECT DISTINCT SOPClassUID, TransferSyntaxUID FROM ");
        sb.append(tableName).append(" WHERE uri IN ('");
        Joiner.on("','").appendTo(sb, Iterables.transform(files, new Function<URI, String>() {
            public String apply(final URI uri) {
                return uri.toString();
            }
        }));
        sb.append("')");

        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet results = statement.executeQuery(sb.toString())) {
            return extractTransferCapabilities(role, results);
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#getTransferCapabilities(java.lang.String, java.util.Map)
     */
    public TransferCapability[] getTransferCapabilities(final String role, final Map<?, String> constraints)
            throws SQLException {
        final StringBuilder sb = new StringBuilder("SELECT DISTINCT SOPClassUID, TransferSyntaxUID FROM ");
        sb.append(tableName);
        appendConstraints(sb, translateConstraints(constraints));
        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet results = statement.executeQuery(sb.toString())) {
            return extractTransferCapabilities(role, results);
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#getUniqueCombinations(java.util.Collection, java.util.Map)
     */
    public Set<Map<DicomAttributeIndex, String>> getUniqueCombinations(
            final Collection<DicomAttributeIndex> tags, final Map<DicomAttributeIndex, ConversionFailureException> failed)
            throws IOException, SQLException {
        return getUniqueCombinationsGivenValues(UNCONSTRAINED, tags, failed);
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#getUniqueCombinationsGivenValues(java.util.Map, java.util.Collection, java.util.Map)
     */
    public Set<Map<DicomAttributeIndex, String>> getUniqueCombinationsGivenValues(
            final Map<?, ? extends String> given,
            final Collection<? extends DicomAttributeIndex> requested,
            final Map<? extends DicomAttributeIndex, ConversionFailureException> failed)
            throws IOException, SQLException {
        if (requested.isEmpty()) {
            return Collections.emptySet();
        }

        final Set<DicomAttributeIndex> tags = Sets.newLinkedHashSet();
        tags.addAll(requested);
        final Map<String, String> constraints = translateConstraints(given, tags);
        updateCache(tags, null);

        final List<DicomAttributeIndex> req    = Lists.newArrayList(requested);
        StringBuilder                   sb     = new StringBuilder("SELECT DISTINCT ");
        final Joiner                    joiner = Joiner.on(", ");
        joiner.appendTo(sb, Iterables.transform(req,
                new Function<DicomAttributeIndex, String>() {
                    public String apply(final DicomAttributeIndex i) {
                        return columns.get(i).getColumnName();
                    }
                }));

        sb.append(" FROM ");
        sb.append(tableName);
        appendConstraints(sb, constraints);

        final Set<Map<DicomAttributeIndex, String>> combs = Sets.newHashSet();
        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet results = statement.executeQuery(sb.toString())) {
            while (results.next()) {
                final Map<DicomAttributeIndex, String> vals = Maps.newHashMap();
                int                                    i    = 1;
                for (final DicomAttributeIndex index : req) {
                    final String val = results.getString(i++);
                    if (val != null) {
                        vals.put(columns.get(index), val);
                    }
                }
                combs.add(vals);
            }
            return combs;
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#getUniqueValues(java.util.Collection, java.util.Map)
     */
    public SetMultimap<DicomAttributeIndex, String> getUniqueValues(final Collection<DicomAttributeIndex> tags,
                                                                    final Map<DicomAttributeIndex, ConversionFailureException> failed)
            throws IOException, SQLException {
        final Map<DicomAttributeIndex, String> constraints = Collections.emptyMap();
        return getUniqueValuesGiven(constraints, tags, failed);
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#getUniqueValues(int)
     */
    public Set<String> getUniqueValues(final DicomAttributeIndex index)
            throws ConversionFailureException, IOException, SQLException {
        final Map<DicomAttributeIndex, ConversionFailureException> failed = Maps.newHashMap();
        final SetMultimap<DicomAttributeIndex, String>             values = getUniqueValues(Collections.singleton(index), failed);
        if (failed.isEmpty()) {
            return values.get(index);
        } else {
            throw failed.get(index);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#getUniqueValuesGiven(java.util.Map, java.util.Collection, java.util.Map)
     */
    public SetMultimap<DicomAttributeIndex, String> getUniqueValuesGiven(Map<?, String> given,
                                                                         Collection<DicomAttributeIndex> requested, Map<DicomAttributeIndex, ConversionFailureException> failed)
            throws IOException, SQLException {
        if (requested.isEmpty()) {
            return ImmutableSetMultimap.of();
        }
        updateCache(Sets.newLinkedHashSet(requested), null);
        final StringBuilder sb     = new StringBuilder("SELECT DISTINCT ");
        final Joiner        joiner = Joiner.on(",");
        joiner.appendTo(sb, Iterables.transform(requested,
                new Function<DicomAttributeIndex, String>() {
                    public String apply(final DicomAttributeIndex i) {
                        return i.getColumnName();
                    }
                }));
        sb.append(" FROM ").append(tableName);
        appendConstraints(sb, translateConstraints(given));

        final SetMultimap<DicomAttributeIndex, String> values = HashMultimap.create();
        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet results = statement.executeQuery(sb.toString())) {
            while (results.next()) {
                int i = 0;
                for (final DicomAttributeIndex tag : requested) {
                    values.put(tag, results.getString(++i));
                }
            }
            return values;
        }
    }

    public Map<URI, Map<DicomAttributeIndex, String>>
    getValuesForResourcesMatching(final Collection<DicomAttributeIndex> tags,
                                  final Map<?, String> constraints)
            throws SQLException {
        // Make a local copy to protect against asynchronous changes
        final Set<DicomAttributeIndex> ts = ImmutableSet.copyOf(tags);

        updateCache(ts, null);

        final StringBuilder sb = new StringBuilder("SELECT uri");
        for (final DicomAttributeIndex tag : ts) {
            sb.append(", ").append(tag.getColumnName());
        }
        sb.append(" FROM ").append(tableName);
        appendConstraints(sb, translateConstraints(constraints));

        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet results = statement.executeQuery(sb.toString())) {
            final Map<URI, Map<DicomAttributeIndex, String>> values = Maps.newLinkedHashMap();
            while (results.next()) {
                final Map<DicomAttributeIndex, String> fvs = Maps.newLinkedHashMap();
                try {
                    values.put(DicomUtils.getQualifiedUri(results.getString(1)), fvs);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                int i = 2;
                for (final DicomAttributeIndex tag : ts) {
                    fvs.put(tag, results.getString(i++));
                }
            }
            return values;
        }
    }

    /**
     * Walks the entries of a DICOMDIR file set to extract the image file information.
     */
    private synchronized Collection<File> readFileSetRecords(final File dir, final DicomDirReader dcd,
                                                             final DicomObject fsRecord)
            throws IOException {
        final List<File> files = Lists.newArrayList();

        for (DicomObject r = fsRecord; r != null; r = dcd.findNextSiblingRecord(r)) {
            try {
                if (r.contains(Tag.ReferencedFileID)) {
                    // dcm4che breaks fields on the same character used as pathname separator ('\')
                    // this isn't such a bad thing, because we want to use the os-appropriate separator anyway
                    final File file = new File(dir, StringUtils.join(r.getStrings(Tag.ReferencedFileID), File.separatorChar)).getCanonicalFile();
                    assert file.getCanonicalPath().equals(file.getPath());
                    if (file.exists()) {
                        final DirectoryRecord.Type type = DirectoryRecord.Type.getInstance(r.getString(Tag.DirectoryRecordType));
                        if (DirectoryRecord.Type.INSTANCE.equals(type)) {
                            files.add(file);
                        }
                    }
                }

                final DicomObject child = dcd.findFirstChildRecord(r);
                if (child != null) {
                    files.addAll(readFileSetRecords(dir, dcd, child));
                }
            } catch (IOException e) {   // IOException is bad news for our ability to continue
                throw e;
            } catch (Exception e) { // Other exceptions are troubling but not necessarily disasters
                logger.error("Error reading DICOMDIR " + dir.getPath() + " directory record: " + e.getMessage());
                logger.error("Record object: " + r);
            }
        }

        return files;
    }

    /**
     * Recache the indicated tags for all files.  This is expensive.
     *
     * @param tags DICOM tags to be cached.
     * @param pm   Progress monitor.
     * @throws SQLException When an error occurs interacting with the database.
     */
    private void recache(final Collection<DicomAttributeIndex> tags, final ProgressMonitorI pm)
            throws SQLException {
        if (tags.isEmpty()) {
            return;
        }
        int progress = 0;
        if (null != pm) {
            pm.setMinimum(0);
            pm.setProgress(progress);
            pm.setMaximum(getSize());
        }
        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement()) {
            for (final URI resource : getResources()) {
                // Try reading the resource as a DICOM object.
                final DataSetAttrs attrs;
                try {
                    attrs = DataSetAttrs.create(resource, tags, uriOpener);
                } catch (IOException e) {
                    if (null != pm) {
                        pm.setProgress(++progress);
                    }
                    continue;   // not a DICOM object; move on to next resource
                }

                // Add this file and all current tags to the db
                final String insert = buildUpdateStatement(resource, attrs);
                if (null != insert) {
                    final int updated = statement.executeUpdate(insert);
                    statement.executeUpdate(COMMIT);
                    assert 1 == updated;
                }
                if (null != pm) {
                    pm.setProgress(++progress);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#remove(java.lang.Iterable)
     */
    public void remove(final Iterable<URI> resources) throws SQLException {
        if (Iterables.isEmpty(resources)) {
            return;
        }
        final StringBuilder sb = new StringBuilder("DELETE FROM ");
        sb.append(tableName);
        sb.append(" WHERE uri IN (");
        Joiner.on(",").appendTo(sb, Iterables.transform(resources, new Function<URI, String>() {
            public String apply(final URI resource) {
                return "'" + resource.toString() + "'";
            }
        }));
        sb.append(")");
        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement()) {
            final int count = statement.executeUpdate(sb.toString());
            logger.debug("{} resources removed from db", count);
            statement.executeUpdate(COMMIT);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.DicomMetadataStore#remove(java.util.Map)
     */
    public void remove(final Map<?, String> constraints) throws SQLException {
        final StringBuilder sb = new StringBuilder("DELETE FROM ");
        sb.append(tableName);
        appendConstraints(sb, translateConstraints(constraints));
        try (final Connection connection = dataSource.getConnection(); final Statement s = connection.createStatement()) {
            final int count = s.executeUpdate(sb.toString());
            s.executeUpdate(COMMIT);
            logger.trace("Removed " + count + " objects");
        }
    }

    private Map<String, String> translateConstraints(final Map<?, ? extends String> constraints,
                                                     final Collection<DicomAttributeIndex> dicomAttributes) {
        final Map<String, String> translated = Maps.newLinkedHashMap();
        for (final Map.Entry<?, ? extends String> me : constraints.entrySet()) {
            final Object key = me.getKey();
            if (key instanceof String) {
                translated.put((String) key, me.getValue());
            } else if (key instanceof DicomAttributeIndex) {
                final DicomAttributeIndex i = (DicomAttributeIndex) key;
                translated.put(i.getColumnName(), me.getValue());
                if (null != dicomAttributes) {
                    dicomAttributes.add(i);
                }
            }
        }
        return translated;
    }

    private Map<String, String> translateConstraints(final Map<?, String> constraints) {
        return translateConstraints(constraints, null);
    }

    private synchronized void updateCache(final Set<DicomAttributeIndex> tags,
                                          final ProgressMonitorI pm)
            throws SQLException {
        final Set<DicomAttributeIndex> newTags = Sets.difference(tags, Sets.newHashSet(columns.values()));
        if (!newTags.isEmpty()) {
            addTableColumns(tags);
            recache(tags, pm);
        }
    }
}
