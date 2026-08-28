package org.nrg.dcm;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomStreamException;
import org.dcm4che3.media.DicomDirReader;
import org.dcm4che3.media.DicomDirWriter;
import org.dcm4che3.media.RecordFactory;
import org.dcm4che3.media.RecordType;
import org.dcm4che3.util.UIDUtils;

import java.io.File;
import java.io.IOException;

/**
 * Creates a DICOMDIR, given a DICOM file on the filesystem.
 */
@Slf4j
public class DicomDir {
    private final File file;
    private DicomDirReader dicomDirReader;
    private Attributes fsInfo;
    private final RecordFactory recordFactory = new RecordFactory();
    private boolean checkDuplicate = false;

    public DicomDir(File file) throws IOException {
        this.file = file.getCanonicalFile();
    }

    private DicomDirWriter writer() {
        return (DicomDirWriter) dicomDirReader;
    }

    public final void fsinfo(Attributes fsinfo) {
        this.fsInfo = new Attributes(fsinfo);
    }

    public Attributes fsinfo() {
        if (fsInfo == null) {
            fsInfo = new Attributes();
            fsInfo.setString(Tag.FileSetID, VR.CS, "DICOMDIR");
        }
        return fsInfo;
    }

    public void create() throws IOException {
        if (!file.exists()) {
            createEmptyDirectory();
        }
        try {
            dicomDirReader = DicomDirWriter.open(file);
        } catch (DicomStreamException e) {
            createEmptyDirectory();
            dicomDirReader = DicomDirWriter.open(file);
        }
    }

    private void createEmptyDirectory() throws IOException {
        String fsUID = UIDUtils.createUID();
        String fileSetID = fsinfo().getString(Tag.FileSetID, "DICOMDIR");
        File descFile = null;
        String charset = null;

        DicomDirWriter.createEmptyDirectory(file, fsUID, fileSetID, descFile, charset);
    }

    public int purge() throws IOException {
        return writer().purge();
    }

    // Adds a DICOM file (or directory tree) to the DICOMDIR.
    public int addFile(File f) throws IOException {
        f = f.getCanonicalFile();
        if (f.equals(file)) return 0; // skip the DICOMDIR file itself
        int n = 0;

        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            if (fs != null) {
                for (File child : fs) {
                    n += addFile(child);
                }
            }
            return n;
        }

        return addFile(f, n);
    }

    private int addFile(File f, int n) throws IOException {
        try (DicomInputStream in = new DicomInputStream(f)) {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            Attributes fmi = in.getFileMetaInformation(); // 可能为 null
            final Attributes ds  = in.readDatasetUntilPixelData();

            if (fmi == null) {
                fmi = new Attributes();
            }
            if (fmi.getString(Tag.MediaStorageSOPInstanceUID) == null) {
                String iuid = ds.getString(Tag.SOPInstanceUID);
                if (iuid != null) fmi.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, iuid);
            }
            if (fmi.getString(Tag.MediaStorageSOPClassUID) == null) {
                String cuid = ds.getString(Tag.SOPClassUID);
                if (cuid != null) fmi.setString(Tag.MediaStorageSOPClassUID, VR.UI, cuid);
            }

            Attributes patrec = recordFactory.createRecord(RecordType.PATIENT, null, ds, fmi, null);
            Attributes styrec = recordFactory.createRecord(RecordType.STUDY,   null, ds, fmi, null);
            Attributes serrec = recordFactory.createRecord(RecordType.SERIES,  null, ds, fmi, null);
            Attributes instrec = recordFactory.createRecord(RecordType.IMAGE,  null, ds, fmi, writer().toFileIDs(f));

            Attributes patAdded = writer().findOrAddPatientRecord(patrec);
            if (patAdded == patrec) ++n;

            Attributes studyAdded = writer().findOrAddStudyRecord(patAdded, styrec);
            if (studyAdded == styrec) ++n;

            Attributes seriesAdded = writer().findOrAddSeriesRecord(studyAdded, serrec);
            if (seriesAdded == serrec) ++n;

            if (n == 0 && checkDuplicate) {
                String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID, ds.getString(Tag.SOPInstanceUID));
                if (iuid != null && writer().findLowerInstanceRecord(seriesAdded, false, iuid) != null) {
                    return 0;
                }
            }

            writer().addLowerDirectoryRecord(seriesAdded, instrec);

            return n + 1;
        } catch (Exception e) {
            log.error("Unable to add file to DICOMDIR: {}", f, e);
            if (e instanceof IOException ioe) throw ioe;
            throw new IOException(e);
        }
    }

    public void close() throws IOException {
        dicomDirReader.close();
    }
}
