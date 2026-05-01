/*
 * ecat4xnat: org.nrg.ecat.FileSet
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class FileSet implements Iterable<MatrixDataFile> {
    private static final String ECAT_MAGIC        = "MATRIX7";
    private static final int    ECAT_MAGIC_OFFSET = 0;
    private static final int    ECAT_MAGIC_SIZE   = 14;

    private static final Logger logger = LoggerFactory.getLogger(FileSet.class);

    private final SortedSet<MatrixDataFile> files = new TreeSet<>();

    private final File   root;
    private final String desc;


    public FileSet(final File f, final Collection<Variable> vars) {
        this(new File[]{f}, vars);
    }


    public FileSet(final File[] fs, final Collection<Variable> vars) {
        root = fs[0];
        desc = buildDesc(fs);

        for (final File f : findECATFiles(fs)) {
            try {
                files.add(new MatrixDataFile(f, vars));
            } catch (IOException e) {
                logger.warn("unable to read ECAT file " + f, e);
            }
        }
    }

    @Override
    public String toString() {
        return desc;
    }

    public File getRoot() {
        return root;
    }

    public Iterator<MatrixDataFile> iterator() {
        return getFiles().iterator();
    }

    /**
     * Returns the ECATFile records in chronological order.
     *
     * @return A list of the ECATFile records.
     */
    public List<MatrixDataFile> getFiles() {
        return new ArrayList<>(files);
    }

    private String buildDesc(final File... fs) {
        final StringBuilder sb = new StringBuilder("ECAT session");
        if (fs.length > 1) {
            sb.append(" [");
        }

        sb.append(fs[0].getPath());
        for (int i = 1; i < fs.length; i++) {
            sb.append(", ").append(fs[i].getPath());
        }

        if (fs.length > 1) {
            sb.append(" ]");
        }

        return sb.toString();
    }

    private Collection<File> findECATFiles(final File... fs) {
        final Queue<File> dirs  = new LinkedList<>();
        final List<File>  files = new LinkedList<>();

        for (final File f : fs) {
            (f.isDirectory() ? dirs : files).add(f);
        }

        for (File dir = dirs.poll(); dir != null; dir = dirs.poll()) {
            for (final File f : dir.listFiles()) {
                (f.isDirectory() ? dirs : files).add(f);
            }
        }

        for (final Iterator<File> fi = files.iterator(); fi.hasNext(); ) {
            if (!isECATFile(fi.next())) {
                fi.remove();
            }
        }
        return files;
    }


    private boolean isECATFile(final File f) {
        final byte[]    bytes = new byte[ECAT_MAGIC_SIZE];
        FileInputStream fin;
        try {
            fin = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            return false;
        }
        try {
            fin.read(bytes, ECAT_MAGIC_OFFSET, ECAT_MAGIC_SIZE);
        } catch (IOException e) {
            return false;
        } finally {
            try {
                fin.close();
            } catch (IOException ignore) {
            }
        }
        final String magic = new String(bytes);     // TODO: specify coding
        return magic.startsWith(ECAT_MAGIC);
    }


    public static void main(final String[] args) {
        final List<Variable> vars = new LinkedList<>();

        // These are nice for display
        vars.add(Variable.ORIGINAL_FILE_NAME);
        vars.add(Variable.FRAME_START_TIME);

        for (final String arg : args) {
            final FileSet es = new FileSet(new File(arg), vars);

            System.out.println("Root " + arg + "; found ECAT files: " + es.getFiles());

            System.out.println("Scans:");
            for (final MatrixDataFile ef : es) {
                for (final Map<Variable, ?> values : ef.getScanValues()) {
                    System.out.println(values);
                }
            }
        }
    }
}
