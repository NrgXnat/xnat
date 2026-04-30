/*
 * ecat4xnat: org.nrg.ecat.MatrixDataFile
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Queue;


public class MatrixDataFile implements Comparable<MatrixDataFile> {
    private static final long RECORD_SIZE = 512L;
    private static final int MAIN_HDR_RECORD = 1;
    private static final int FIRST_DIR_RECORD = 2;
    private static final int FILE_TYPE_OFFSET = 50;     // main header variable FILE_TYPE

    private static final SubheaderFactory subheaderFactory = StaticSubheaderFactory.getInstance();

    private final File file;
    private final MainHeader mainHeader;
    private final List<Header> subheaders;

    public MatrixDataFile(final File file, final Collection<Variable> reqvars) throws IOException {
        this.file = file;

        mainHeader = new MainHeader();
        final Set<Variable> vars = new LinkedHashSet<Variable>(reqvars);
        vars.add(Variable.SCAN_START_TIME);
        vars.add(Variable.ACQUISITION_TYPE);

        subheaders = new ArrayList<Header>();

        final FileInputStream fin = new FileInputStream(file);
        try {
            final FileChannel fc = fin.getChannel();

            // Read the main header.
            final MappedByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY,
                    (MAIN_HDR_RECORD-1)*RECORD_SIZE, RECORD_SIZE);

            mainHeader.read(bbuf, vars);

            final int fileType = bbuf.getShort(FILE_TYPE_OFFSET);

            // Walk the directory(ies) to build a list of subheaders
            final Queue<Integer> directories = new LinkedList<Integer>();
            directories.add(FIRST_DIR_RECORD);
            final List<Integer> subhis = new ArrayList<Integer>();
            for (Integer dirrec = directories.poll(); dirrec != null; dirrec = directories.poll()) {
                final MappedByteBuffer dbbuf = fc.map(FileChannel.MapMode.READ_ONLY,
                        (dirrec-1)*RECORD_SIZE, RECORD_SIZE);

                // First examine the directory info to see if this record points
                // to another directory.
                dbbuf.getInt();  // # of entries free
                final int nextDirRecord = dbbuf.getInt();
                dbbuf.getInt(); // previous directory record
                final int nEntriesUsed = dbbuf.getInt();

                if (nextDirRecord != FIRST_DIR_RECORD) {
                    directories.add(nextDirRecord);
                }

                // Now add all referred subheaders to the list.
                for (int i = 0; i < nEntriesUsed; i++) {
                    dbbuf.getInt();          // ID
                    final int subhi = dbbuf.getInt();
                    dbbuf.getInt(); // last data record
                    final int status = dbbuf.getInt();
                    if (status == 1) {
                        subhis.add(subhi);
                    }
                }
            }

            // Now read each subheader to extract variables.
            for (final int subhi : subhis) {
                final MappedByteBuffer shbbuf = fc.map(FileChannel.MapMode.READ_ONLY,
                        (subhi-1)*RECORD_SIZE, RECORD_SIZE);
                final Header subheader = subheaderFactory.create(fileType);
                subheader.read(shbbuf, vars);
                subheaders.add(subheader);
            }
            fc.close();

        } finally {
            fin.close();
        }
    }


    public Date getScanStartTime() {
        return (Date)mainHeader.get(Variable.SCAN_START_TIME);
    }

    public Date getDoseStartTime() {
        return (Date)mainHeader.get(Variable.DOSE_START_TIME);
    }

    public Variable.AcquisitionType getAcquisitionType() {
        return Variable.AcquisitionType.getInstance((Short)mainHeader.get(Variable.ACQUISITION_TYPE));
    }

    public File getFile() { return file; }

    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final MatrixDataFile other) {
        final int startOrder = getScanStartTime().compareTo(other.getScanStartTime());
        return 0 == startOrder ? file.compareTo(other.file) : startOrder;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
        if (o instanceof MatrixDataFile other) {
            return getScanStartTime().equals(other.getScanStartTime()) && file.equals(other.file);
        } else {
            return false;
        }
    }


    /**
     * @return List of all variable value combinations in the file
     */
    public List<SortedMap<Variable,?>> getScanValues() {
        final List<SortedMap<Variable,?>> values = new ArrayList<SortedMap<Variable,?>>();
        for (final Header subh : subheaders) {
            final SortedMap<Variable,Object> v = new TreeMap<Variable,Object>(mainHeader.getValues());
            v.putAll(subh.getValues());
            values.add(v);
        }
        return values;
    }

    /**
     * @return List of all subheaders in the file
     */
    public List<Header> getSubheaders() {
        return new ArrayList<Header>(subheaders);
    }

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";


    public static void main(final String[] args) throws IOException {
        System.out.println(XML_HEADER);
        for (final String arg : args) {
            final MatrixDataFile ef = new MatrixDataFile(new File(arg), Variable.getVars());

            System.out.println("<ecatfile xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" name=\"%1$s\">".formatted(arg));
            ef.mainHeader.emitXML(System.out);
            for (final Header header : ef.getSubheaders()) {
                header.emitXML(System.out);
            }

            System.out.println("</ecatfile>");
        }
    }
}
