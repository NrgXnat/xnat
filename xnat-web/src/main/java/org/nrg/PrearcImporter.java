/*
 * PrearcImporter: org.nrg.PrearcImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.BUnzip2;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.GUnzip;
import org.apache.tools.ant.taskdefs.Move;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.nrg.attr.Utils;
import org.nrg.dcm.xnat.DICOMSessionBuilder;
import org.nrg.dcm.xnat.XnatAttrDef;
import org.nrg.dicomtools.filters.DicomFilterService;
import org.nrg.dicomtools.filters.SeriesImportFilter;
import org.nrg.ecat.Variable;
import org.nrg.ecat.xnat.PETSessionBuilder;
import org.nrg.framework.status.*;
import org.nrg.resources.SupplementalResourceBuilderUtils;
import org.nrg.session.SessionBuilder.NoUniqueSessionException;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.LabelType;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Trawls through a directory tree to reorganize into a nice prearchive format
 * and build session XML for all contained data
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@Slf4j
public final class PrearcImporter implements StatusProducerI, Runnable {
    private static final String     IMA_FILE_REGEX       = ".*\\.[iI][mM][aA]\\z";    // IMA files end in .ima
    private static final String     XML_SUFFIX           = ".xml";
    private static final String     PET_SCAN_INDEX_PARAM = "xnat:petSessionData/scans/scan/ID";
    private static final FileFilter isFileFilter         = File::isFile;
    private static final FileFilter isDirectoryFilter    = File::isDirectory;
    private static final FileFilter isIMAFileFilter      = f -> f.isFile() && f.getName().matches(IMA_FILE_REGEX);

    private static DicomFilterService filterService = null;

    private final BasicStatusPublisher publisher = new BasicStatusPublisher();
    private final StatusListenerI      listener  = publisher::publish;

    private final Set<File> sessions = new HashSet<>();

    private final String              project;
    private final File                root;
    private final Set<File>           importFiles;
    private final File                prearc;
    private final String[]            imaBuildXMLCommand;
    private final String[]            imaBuildXMLEnv;
    private final Map<String, Object> additionalValues = new LinkedHashMap<>();

    // Some fields have complicated rule sets for determining their values from DICOM data.
    public final XnatAttrDef projectAttrDef;
    public final XnatAttrDef subjectLabelAttrDef;
    public final XnatAttrDef sessionLabelAttrDef;

    /**
     * Builds an importer
     *
     * @param project            Name of the project to which these data should be assigned
     * @param toDir              Destination directory for the reorganized data
     * @param fromDir            Directory containing the data to be reorganized
     * @param files              Files to be imported.  If null, import all files from fromDir;
     *                           if non-null but zero size, import no files.
     * @param buildXmlFromIMA    External command to build session XML from directory containing IMA data
     * @param buildXmlFromIMAEnv Environment variable settings (whitespace-separated) for build command
     */
    public PrearcImporter(final String project, final File toDir,
                          final File fromDir, final File[] files,
                          final String[] buildXmlFromIMA, final String[] buildXmlFromIMAEnv) {
        if (!fromDir.isDirectory()) {
            throw new IllegalArgumentException("root (" + fromDir + ") must be a directory");
        }
        this.project = project;
        this.prearc = toDir;
        this.root = fromDir;
        this.importFiles = new HashSet<>();
        if (null == files) {
            this.importFiles.add(root);
        } else {
            this.importFiles.addAll(Arrays.asList(files));
        }
        this.imaBuildXMLCommand = buildXmlFromIMA;
        this.imaBuildXMLEnv = buildXmlFromIMAEnv;
        final Project antProject = new Project();
        antProject.setBaseDir(fromDir);

        this.projectAttrDef = this.project != null ?
                              new XnatAttrDef.Constant("project", this.project) :
                              LabelType.PROJECT.getAttrDef();
        this.subjectLabelAttrDef = LabelType.SUBJECT.getAttrDef();
        this.sessionLabelAttrDef = LabelType.SESSION.getAttrDef();
    }

    /**
     * Builds an importer for all files in fromDir
     *
     * @param project            Name of the project to which these data should be assigned
     * @param toDir              Destination directory for the reorganized data
     * @param fromDir            Directory containing the data to be reorganized
     * @param buildXmlFromIMA    Strings for XML building from IMA files.
     * @param buildXmlFromIMAEnv Strings for XML building from IMA environment.
     */
    public PrearcImporter(final String project, final File toDir, final File fromDir,
                          final String[] buildXmlFromIMA, final String[] buildXmlFromIMAEnv) {
        this(project, toDir, fromDir, null, buildXmlFromIMA, buildXmlFromIMAEnv);
    }

    /**
     * Builds an importer for all files in fromDir, without specifying environment variables for the external IMA XML
     * builder.
     *
     * @param project         Name of the project to which these data should be assigned
     * @param toDir           The directory where files should be imported to.
     * @param fromDir         The directory where files should be imported from.
     * @param buildXmlFromIMA Strings for the builder.
     */
    public PrearcImporter(final String project, final File toDir, final File fromDir, final String[] buildXmlFromIMA) {
        this(project, toDir, fromDir, null, buildXmlFromIMA, null);
    }

    /**
     * Provide additional parameters to the PrearcImporter before execution.
     * Parameters used:
     * xnat:petSessionData/scans/scan/ID  (value = index of first scan, default 1)
     * all other map entries are quietly ignored.
     *
     * @param values parameter map
     *
     * @return A reference to the prearchive importer.
     */
    @SuppressWarnings("unused")
    public PrearcImporter setAdditionalValues(final Map<String, ?> values) {
        additionalValues.putAll(values);
        return this;
    }

    static class UnpackDispatcher {
        private final Map<String, Unpacker> unpackers = new LinkedHashMap<>();

        public void add(final String suffix, final Unpacker unpacker) {
            unpackers.put(suffix, unpacker);
        }

        public boolean unpack(final File f, final File dest) {
            final String name = f.getName();
            for (Map.Entry<String, Unpacker> me : unpackers.entrySet()) {
                if (name.endsWith(me.getKey())) {
                    me.getValue().unpack(f, dest);
                    return true;
                }
            }
            return false;
        }

        @SuppressWarnings("unused")
        public boolean unpack(final File f) {
            return unpack(f, null);
        }
    }

    private File createTmpDir() {
        try {
            final File tmpDir = File.createTempFile("prearc-unpack", "");
            if (!tmpDir.delete()) {
                throw new IOException("unable to delete temp file");
            }
            if (!tmpDir.mkdir()) {
                throw new IOException("mkdir failed");
            }
            return tmpDir;
        } catch (IOException e) {
            log.error("unable to create temporary directory for unpacking", e);
            publishFailure(root, "unable to open temporary directory for unpacking: " + e.getMessage());
            return null;
        }
    }

    /**
     * Reorganizes the indicated file tree
     */
    public void run() {
        publishStatus(root, "processing");

        final Project antProject = new Project();
        antProject.setBaseDir(root);

        final UnpackDispatcher ud = new UnpackDispatcher();
        ud.add(".tar", new Untarrer("none", antProject));
        ud.add(".tar.gz", new Untarrer("gzip", antProject));
        ud.add(".tgz", new Untarrer("gzip", antProject));
        ud.add(".tar.bz2", new Untarrer("bzip2", antProject));
        ud.add(".zip", new Unzipper());

        // compression suffixes without archiving must come after archive suffixes
        ud.add(".gz", new Uncompressor(new GUnzip(), antProject));
        ud.add(".bz2", new Uncompressor(new BUnzip2(), antProject));

        assert root.isDirectory();    // already checked by setBaseDir() above

        final Queue<File> dirs = new LinkedList<>(importFiles);

        // First look (recursively) for archives; unarchive all and uncompress compressed files before moving on.
        final Collection<File> tmpDirs = new LinkedList<>();
        final File             tmpDir  = createTmpDir();
        if (tmpDir == null) {
            return;
        }
        tmpDirs.add(tmpDir);

        try {
            for (File dir = dirs.poll(); dir != null; dir = dirs.poll()) {
                // For all regular files, uncompress if compressed.
                // If the file is archived, extract into a temporary directory
                // and add it to the imports list.
                final File[] files = dir.isDirectory() ? dir.listFiles(isFileFilter) : new File[]{dir};
                if (files != null) {
                    for (final File file : files) {
                        log.debug("checking inbox file " + file);
                        if (ud.unpack(file, tmpDir)) {
                            log.debug("extracted and removed " + file);
                            file.delete();
                            dirs.add(tmpDir);
                            importFiles.add(tmpDir);
                            final File subTmpDir = createTmpDir();
                            if (subTmpDir == null) {
                                return;
                            }
                            tmpDirs.add(subTmpDir);
                        }
                    }
                }

                // Now add all subdirectories to the list and continue.
                if (dir.isDirectory()) {
                    final File[] contents = dir.listFiles(isDirectoryFilter);
                    if (contents != null) {
                        dirs.addAll(Arrays.asList(contents));
                    }
                }
            }
            assert dirs.isEmpty();

            final File[] sources = importFiles.toArray(new File[0]);

            final List<SeriesImportFilter> filters    = new ArrayList<>();
            final SeriesImportFilter       siteFilter = getDicomFilterService().getSeriesImportFilter();
            if (siteFilter != null && siteFilter.isEnabled()) {
                filters.add(siteFilter);
            }
            if (StringUtils.isNotBlank(project)) {
                final SeriesImportFilter projectFilter = getDicomFilterService().getSeriesImportFilter(project);
                if (projectFilter != null && projectFilter.isEnabled()) {
                    filters.add(projectFilter);
                }
            }
            // Now turn the DICOM and ECAT restructurers loose
            org.nrg.dcm.Restructurer dicom;
            try {
                dicom = new org.nrg.dcm.Restructurer(sources, prearc, filters);
                dicom.addStatusListener(listener);
                dicom.run();
                sessions.addAll(dicom.getSessions());
            } catch (IOException e) {
                dicom = null;
                log.warn("unable to run DICOM restructurer", e);
            }

            final org.nrg.ecat.Restructurer ecat = new org.nrg.ecat.Restructurer(sources, prearc, new Variable[]{Variable.PATIENT_ID}, "%1$s");
            ecat.addStatusListener(listener);
            if (additionalValues.containsKey(PET_SCAN_INDEX_PARAM)) {
                try {
                    ecat.setScanNumber(Integer.parseInt((String) additionalValues.get(PET_SCAN_INDEX_PARAM)));
                } catch (Throwable t) {
                    log.error("unable to set initial scan index", t);
                }
            }
            ecat.run();
            sessions.addAll(ecat.getSessions());

            // Generate session XML for all DICOM/ECAT sessions we just moved to the prearchive.
            if (null != dicom) {
                for (final File sessionDir : dicom) {
                    publishStatus(sessionDir, "creating XML for DICOM session " + sessionDir.getName());
                    try {
                        try (DICOMSessionBuilder builder = new DICOMSessionBuilder(sessionDir,
                                                                                   projectAttrDef, subjectLabelAttrDef, sessionLabelAttrDef)) {
                            SessionXMLBuilder.run(builder);
                        }
                        publishSuccess(sessionDir, "Done creating XML for DICOM session " + sessionDir.getName());
                    } catch (IOException e) {
                        log.warn("unable to write session", e);
                        publishFailure(sessionDir, "Unable to write session: " + e.getMessage());
                    } catch (SQLException e) {
                        log.warn("error extracting session metadata", e);
                        publishFailure(sessionDir, "Error extracting session metadata: " + e.getMessage());
                    } catch (NoUniqueSessionException e) {
                        log.warn(sessionDir + " does not contain a single session", e);
                        publishFailure(sessionDir, e.getMessage());
                    }
                }
            }

            for (final File sessionDir : ecat) {    // all ECAT sessions are PET
                publishStatus(root, "creating XML for ECAT session " + sessionDir.getName());
                final Writer writer;
                try {
                    final File             sessionXML = new File(sessionDir.getPath() + XML_SUFFIX);
                    final FileOutputStream fos        = new FileOutputStream(sessionXML);
                    try {
                        fos.getChannel().lock();
                    } catch (IOException e) {
                        log.error("Unable to obtain lock for session writer", e);
                    }
                    writer = new BufferedWriter(new OutputStreamWriter(fos));
                } catch (IOException e) {
                    publishFailure(sessionDir, "Unable to open session writer: " + e.getMessage());
                    continue;
                }
                final PETSessionBuilder builder = new PETSessionBuilder(sessionDir, writer, project);
                try {
                    SessionXMLBuilder.run(builder);
                } catch (IOException e) {
                    log.warn("unable to write session", e);
                    publishFailure(sessionDir, "Unable to write session: " + e.getMessage());
                } catch (SQLException | NoUniqueSessionException e) {
                    throw new RuntimeException(e);    // can't happen
                }
            }


            // Finally, look for any straggling IMA files that can be assembled into sessions.
            assert dirs.isEmpty();
            dirs.addAll(importFiles);
            for (File dir = dirs.poll(); dir != null; dir = dirs.poll()) {
                if (!dir.isDirectory()) {
                    continue;
                }

                log.debug("looking for IMA files in " + dir);

                final File[] imaFiles = dir.listFiles(isIMAFileFilter);
                if (imaFiles != null && imaFiles.length > 0) {
                    // This is an IMA data RAW directory; try to build a session XML.
                    log.debug("trying to build IMA session for " + dir);
                    final File xmlFile = buildIMASessionXML(dir);
                    if (xmlFile != null) {
                        publishStatus(xmlFile, "found IMA study");

                        // Load the XML file into a document
                        final SAXReader reader = new SAXReader();
                        Document        sessionXML;
                        try {
                            sessionXML = reader.read(xmlFile);
                        } catch (DocumentException e) {
                            publishFailure(xmlFile, e.getMessage());
                            continue;
                        }

                        final Element session = sessionXML.getRootElement();
                        final String  id      = session.attributeValue("ID");
                        publishStatus(xmlFile, "IMA study ID: " + id);

                        final File sessionDir = dir.getParentFile();
                        final File parent     = sessionDir.getParentFile();
                        final File output     = Utils.getUnique(prearc, id);

                        final Move move = new Move();
                        move.setProject(antProject);
                        move.setFile(sessionDir);
                        move.setTofile(output);
                        move.execute();
                        if (output.isDirectory()) {
                            // Rebuild the session XML in the prearchive location
                            if (buildIMASessionXML(new File(output, dir.getName())) != null) {
                                sessions.add(output);
                            } else {
                                publishFailure(sessionDir, "unable to rebuild session XML for prearchive");
                            }
                        } else {
                            publishFailure(sessionDir, "Unable to rename " + dir + " to " + output);
                        }

                        // If moving this directory left parent directories empty, delete them.
                        for (File p = parent; p != null; ) {
                            final String[] list = p.list();
                            if (list == null || 0 == list.length) {
                                final File empty = p;
                                p = p.getParentFile();
                                empty.delete();
                            } else {
                                p = p.getParentFile();
                            }
                        }
                        publishSuccess(xmlFile, "done processing IMA study " + id);
                    }
                } else {
                    // This isn't a data directory, so descend.
                    final File[] directories = dir.listFiles(isDirectoryFilter);
                    if (directories != null) {
                        dirs.addAll(Arrays.asList(directories));
                    }
                }
            }

            // look for plugin builders and pass them session xml and remaining temp dirs
            publishStatus(root, "Adding supplemental resources");
            SupplementalResourceBuilderUtils.addSupplementalResourcesToXml(sessions.stream().map(file -> new File(file.getAbsolutePath() + ".xml")).filter(File::exists).collect(Collectors.toList()), importFiles);

            publishSuccess(this.root, "done");
        } finally {
            // Get rid of anything left in the temp directories.
            for (final File td : tmpDirs) {
                final Delete delete = new Delete();
                delete.setProject(antProject);
                delete.setDir(td);
                try {
                    delete.execute();
                } catch (BuildException e) {
                    log.warn("Error cleaning up from import", e);
                }
            }
        }
    }

    /**
     * Call an external program to generate an XML session document from IMA files.
     * Note that the "dir" argument is the RAW directory; archiveIma must be called
     * with the parent of that directory.
     *
     * @param rawDir directory containing raw IMA files
     *
     * @return the XML session document, or null if the build failed
     */
    private File buildIMASessionXML(final File rawDir) {
        if (null == imaBuildXMLCommand) {
            publishFailure(rawDir, "No IMA session build command defined");
            return null;
        }

        final String sessionDirPath;
        try {
            sessionDirPath = rawDir.getCanonicalFile().getParent();
        } catch (IOException e) {
            publishFailure(rawDir, "Unable to get canonical path for " + rawDir + ": " + e.getMessage());
            return null;
        }

        final String[] command = new String[imaBuildXMLCommand.length + 1];
        System.arraycopy(imaBuildXMLCommand, 0, command, 0, imaBuildXMLCommand.length);
        command[imaBuildXMLCommand.length] = sessionDirPath;
        publishStatus(rawDir, "building IMA session");

        try {
            final Process process = Runtime.getRuntime().exec(command, imaBuildXMLEnv);
            new Thread(new StreamConsumer(process.getInputStream(), rawDir)).start();
            new Thread(new StreamConsumer(process.getErrorStream(), rawDir)).start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            publishFailure(rawDir, e.getMessage());
            return null;
        }

        final File xmlFile = new File(sessionDirPath + XML_SUFFIX);
        if (xmlFile.isFile()) {
            publishSuccess(rawDir, "built IMA session XML");
            return xmlFile;
        } else {
            publishWarning(rawDir, "unable to build IMA session XML");
            return null;
        }
    }


    /**
     * @return Final (prearchive) location of each session directory found
     */
    public Collection<File> getSessions() {
        return sessions;
    }


    /* (non-Javadoc)
     * @see org.nrg.StatusPublisher#addStatusListener(org.nrg.StatusListener)
     */
    public void addStatusListener(final StatusListenerI l) {
        publisher.addStatusListener(l);
    }

    /* (non-Javadoc)
     * @see org.nrg.StatusPublisher#removeStatusListener(org.nrg.StatusListener)
     */
    public void removeStatusListener(final StatusListenerI l) {
        publisher.removeStatusListener(l);
    }

    private void publishStatus(final Object o, final String message) {
        publisher.publish(new StatusMessage(o, StatusMessage.Status.PROCESSING, message));
    }

    @SuppressWarnings("SameParameterValue")
    private void publishWarning(final Object o, final String message) {
        publisher.publish(new StatusMessage(o, StatusMessage.Status.WARNING, message));
    }

    private void publishFailure(final Object o, final String message) {
        publisher.publish(new StatusMessage(o, StatusMessage.Status.FAILED, message));
    }

    private void publishSuccess(final Object o, final String message) {
        publisher.publish(new StatusMessage(o, StatusMessage.Status.COMPLETED, message));
    }

    private DicomFilterService getDicomFilterService() {
        if (filterService == null) {
            synchronized (this) {
                filterService = XDAT.getContextService().getBean(DicomFilterService.class);
            }
        }
        return filterService;
    }

    /**
     * Run the reorganizer using the given destination (first argument)
     * and source directories (all following arguments).
     *
     * @param args Should specify the paths to the destination and source directories
     */
    public static void main(String[] args) {
        final String   buildString        = System.getProperty("ima2xml.cmd");
        final String[] buildXmlFromIMA    = null == buildString ? new String[]{"echo", "No IMA build command defined for "} : buildString.split("\\s");
        final String   envString          = System.getProperty("ima2xml.env");
        final String[] buildXmlFromIMAEnv = null == envString ? null : envString.split("\\s");

        if (args.length < 2) {
            System.err.println("Usage: PrearcImporter <destination-dir> <source-dir-1> <project-name-1> [<source-dir-2> ...]");
            System.exit(-1);
        }
        final File newArchive = new File(args[0]);
        for (int i = 1; i < args.length; i += 2) {
            final PrearcImporter importer = new PrearcImporter(args[i + 1], newArchive, new File(args[i]), buildXmlFromIMA, buildXmlFromIMAEnv);
            importer.addStatusListener(new LoggerStatusReporter(PrearcImporter.class));
            importer.run();
            System.out.println("Found sessions: " + importer.getSessions());
        }
    }
}
