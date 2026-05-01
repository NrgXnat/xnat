/*
 * PrearcImporter: org.nrg.ecat.Restructurer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Move;
import org.nrg.AbstractRestructurer;
import org.nrg.attr.Utils;
import org.nrg.framework.status.LoggerStatusReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Converts an arbitrary arrangement of ECAT files into a
 * directory structure we like.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class Restructurer extends AbstractRestructurer {
    private final static String MULTIPLE_FILES = "(selected files)";

    private static final Logger log = LoggerFactory.getLogger(Restructurer.class);

    private final Collection<File> sessionDirs = new LinkedHashSet<>();

    private final File[]     infiles;
    private final File       outdir;
    private final Variable[] sessionNameVars;
    private final String     sessionNameFormat;
    private final Project    project;
    private int scanNumber = 1;

    /**
     * Converts an arbitrary arrangement of ECAT files into a directory structure we like.
     * Each session will be placed into its own directory, named after the session name.
     * The session name is constructed from the given ECAT variables, according to the
     * given name format.  The ith item in the format string is the ith ECAT variable
     * in the array.
     *
     * @param infiles           ECAT files and directories containing ECAT files to be reorganized
     * @param outdir            Directory into which reorganized files will be placed
     * @param sessionNameVars   ECAT variables used to identify session
     * @param sessionNameFormat Format string for making session IDs from ECAT variables
     */
    public Restructurer(final File[] infiles, final File outdir, final Variable[] sessionNameVars, final String sessionNameFormat) {
        super(log);

        if (sessionNameVars == null || sessionNameVars.length < 1) {
            throw new IllegalArgumentException("must specify at least one session-defining ECAT variable");
        }
        if (outdir.exists() && !outdir.isDirectory()) {
            throw new IllegalArgumentException(outdir + " is not a directory");
        }

        this.infiles = infiles;
        this.outdir = outdir;
        this.sessionNameVars = sessionNameVars;
        this.sessionNameFormat = sessionNameFormat;
        project = new Project();
        project.setBaseDir(outdir);
    }

    public Restructurer(final File infile, final File outdir,
                        final Variable[] sessionNameVars, final String sessionNameFormat) {
        this(new File[]{infile}, outdir, sessionNameVars, sessionNameFormat);
    }

    /**
     * Set the scan index of the next scan.
     *
     * @param n scan index
     * @return this
     */
    @SuppressWarnings("UnusedReturnValue")
    public Restructurer setScanNumber(final int n) {
        scanNumber = n;
        return this;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void run() {
        if (0 == infiles.length) {
            return;
        }

        final Object masterObj = (1 == infiles.length) ? infiles[0] : MULTIPLE_FILES;
        publishStatus(masterObj, "looking for ECAT files");

        if (!outdir.exists()) {
            outdir.mkdirs();
        }

        // Find all ECAT files in the indir and organize them by session
        final FileSet                 eset     = new FileSet(infiles, Arrays.asList(sessionNameVars));
        final List<MatrixDataFile>    efiles   = eset.getFiles();
        final Map<String, List<File>> sessions = new HashMap<>();

        publishStatus(masterObj, "found " + efiles.size() + " ECAT files");

        ECATFILE:
        for (final MatrixDataFile ef : efiles) {
            // Verify that the session-defining variables have exactly one value in this file
            final Map<Variable, Set<Object>> sessionValues = new HashMap<>();
            for (final SortedMap<Variable, ?> scanValues : ef.getScanValues()) {
                for (final Map.Entry<Variable, ?> me : scanValues.entrySet()) {
                    if (!sessionValues.containsKey(me.getKey())) {
                        sessionValues.put(me.getKey(), new HashSet<>());
                    }
                    sessionValues.get(me.getKey()).add(me.getValue());
                }
            }
            for (final Map.Entry<Variable, Set<Object>> me : sessionValues.entrySet()) {
                if (me.getValue().size() > 1) {
                    publishStatus(masterObj, "Skipping reorganization for " + ef.getFile());
                    publishFailure(ef.getFile(), ef.getFile().getName()
                                                 + " has multiple values for session variable "
                                                 + me.getKey() + ": " + me.getValue());
                    continue ECATFILE;
                }
            }

            // Determine the session name for this file.
            final Object[] sessionValueArray = new Object[sessionNameVars.length];
            for (int j = 0; j < sessionNameVars.length; j++) {
                assert sessionValues.containsKey(sessionNameVars[j]);
                assert sessionValues.get(sessionNameVars[j]).size() == 1;
                sessionValueArray[j] = sessionValues.get(sessionNameVars[j]).toArray()[0];
            }
            final String session = String.format(sessionNameFormat, sessionValueArray).replaceAll("[\\W]", "_");

            // Add this file to the appropriate session.
            if (!sessions.containsKey(session)) {
                sessions.put(session, new ArrayList<File>());
                publishStatus(masterObj, "found ECAT session " + session);
            }
            sessions.get(session).add(ef.getFile());
        }

        // Now create a directory for each session and move the files.
        for (final Map.Entry<String, List<File>> me : sessions.entrySet()) {
            // Construct a unique directory pathname for this study
            final File sessionDir = Utils.getUnique(outdir, me.getKey());
            final File scansDir   = new File(sessionDir, SCANS_DIR);
            scansDir.mkdirs();
            if (!scansDir.isDirectory()) {
                publishFailure(sessionDir, "Unable to create images directory " + scansDir);
                continue;
            }

            assert !sessionDirs.contains(sessionDir);
            sessionDirs.add(sessionDir);

            for (final File infile : me.getValue()) {
                // Create a scan directory.
                final File scanDir = new File(scansDir, Integer.toString(scanNumber++));
                final File ecatDir = new File(scanDir, "ECAT");
                ecatDir.mkdirs();
                // Construct a unique name for the file in the study directory.
                final File outfile  = Utils.getUnique(ecatDir, infile.getName(), ".v");
                final Move moveTask = new Move();
                moveTask.setProject(project);
                moveTask.setFile(infile);
                moveTask.setTofile(outfile);
                moveTask.execute();
                if (!outfile.exists()) {
                    publishWarning(infiles, "Unable to rename " + infile + " to " + outfile);
                }

                // If moving this file left parent directories empty, delete them.
                File parent = infile.getParentFile();
                if (parent != null) {
                    final String[] contents = parent.list();
                    while (null != parent && contents != null && contents.length > 0) {
                        final File empty = parent;
                        parent = empty.getParentFile();
                        empty.delete();
                    }
                }
            }
        }

        // Remove any remaining empty directories.
        pruneDirectoryTree(infiles);

        publishSuccess(masterObj, "done finding ECAT");
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.Restructurer#getSessions()
     */
    public Collection<File> getSessions() {
        return sessionDirs;
    }

    /**
     * Restructure using PATIENT_ID as the session identifier.
     *
     * @param args First argument is the pathname of the destination directory. Successive arguments are path names to
     *             be restructured.
     */
    public static void main(final String[] args) {
        final Variable[] sessionVars   = {Variable.PATIENT_ID};
        final String     sessionFormat = "%1$s";

        final File destination = new File(args[0]);
        for (int i = 1; i < args.length; i++) {
            final Restructurer r = new Restructurer(new File(args[i]), destination, sessionVars, sessionFormat);
            r.addStatusListener(new LoggerStatusReporter(Restructurer.class));
            r.run();
            System.out.println("Sessions found in " + args[i] + ": " + r.getSessions());
        }
    }
}
