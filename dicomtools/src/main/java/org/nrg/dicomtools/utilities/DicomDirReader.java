package org.nrg.dicomtools.utilities;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class DicomDirReader implements Closeable {
    private final Attributes dicomdir;
    private final List<Attributes> rootRecords;

    public DicomDirReader(File dicomdirFile) throws IOException {
        try (DicomInputStream dis = new DicomInputStream(dicomdirFile)) {
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            dicomdir = dis.readDataset(Tag.DirectoryRecordSequence+1);
        }

        Sequence seq = dicomdir.getSequence(Tag.DirectoryRecordSequence);
        if (seq == null) {
            throw new IllegalArgumentException("Not a valid DICOMDIR: missing DirectoryRecordSequence");
        }
        this.rootRecords = seq;
    }

    public Attributes findFirstRootRecord() {
        return rootRecords.isEmpty() ? null : rootRecords.get(0);
    }

    public Attributes findNextSiblingRecord(Attributes record) {
        Sequence parentSeq = getParentSequence(record);
        if (parentSeq == null) return null;

        Iterator<Attributes> it = parentSeq.iterator();
        while (it.hasNext()) {
            Attributes next = it.next();
            if (next == record && it.hasNext()) {
                return it.next();
            }
        }
        return null;
    }

    public Attributes findFirstChildRecord(Attributes record) {
        Sequence children = record.getSequence(Tag.DirectoryRecordSequence);
        return (children != null && !children.isEmpty()) ? children.get(0) : null;
    }

    public String getRecordType(Attributes record) {
        return record.getString(Tag.DirectoryRecordType);
    }

    public String getReferencedFileID(Attributes record) {
        String[] ids = record.getStrings(Tag.ReferencedFileID);
        return (ids != null) ? String.join(File.separator, ids) : null;
    }

    private Sequence getParentSequence(Attributes record) {
        return findParentSequence(dicomdir.getSequence(Tag.DirectoryRecordSequence), record);
    }

    private Sequence findParentSequence(Sequence seq, Attributes target) {
        if (seq == null) return null;
        for (Attributes item : seq) {
            if (item == target) {
                return seq;
            }
            Sequence childSeq = item.getSequence(Tag.DirectoryRecordSequence);
            Sequence found = findParentSequence(childSeq, target);
            if (found != null) return found;
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        // nothing to close (keep API symmetry)
    }
}
