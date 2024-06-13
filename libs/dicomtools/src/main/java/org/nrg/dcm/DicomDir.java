/*
 * dicomtools: org.nrg.dcm.DicomDir
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import java.io.File;
import java.io.IOException;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.media.ApplicationProfile;
import org.dcm4che2.media.DicomDirReader;
import org.dcm4che2.media.DicomDirWriter;
import org.dcm4che2.media.StdGenJPEGApplicationProfile;
import org.dcm4che2.media.FileSetInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a DICOMDIR, given a DICOM file on the filesystem.
 * Essentially a pared down version of dcm4che's DcmDir tool
 * found at ../dcm4che-tool/dcm4che-tool-dcmdir of the dcm4che 2.0.22
 * distribution
 */

public class DicomDir {
    private final File file;
    private DicomDirReader dicomdir;
    private FileSetInformation fsinfo;
    private ApplicationProfile ap = new StdGenJPEGApplicationProfile();
    private boolean checkDuplicate = false;
    private final Logger logger = LoggerFactory.getLogger(DicomDir.class);

    public DicomDir(File file) throws IOException {
        this.file = file.getCanonicalFile();
    }

    private DicomDirWriter writer() {
	return ((DicomDirWriter) dicomdir);
    }
    
    public final void fsinfo(FileSetInformation fsinfo) {
	BasicDicomObject dest = new BasicDicomObject();
	fsinfo.getDicomObject().copyTo(dest);
	this.fsinfo = new FileSetInformation(dest);
    }
    
    public FileSetInformation fsinfo() {
        if (fsinfo == null) {
            fsinfo = new FileSetInformation();
            fsinfo.init();
        }
        return fsinfo;
    }

    public void create() throws IOException {
        dicomdir = new DicomDirWriter(file, fsinfo());        
    }

    public int purge() throws IOException {
	return writer().purge();
    }

    // Adds a DICOM file to the DICOMDIR. Given a directory it searches
    // the tree for DICOM files and adds them to the DICOMDIR.
    // Returns the number of files added to the DICOMDIR
    public int addFile(File f) throws IOException {
        f = f.getCanonicalFile();
        // skip adding DICOMDIR
        if (f.equals(file)) return 0;
        int n = 0;

	// If the file is a directory recurse through it's contents
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            for (int i = 0; i < fs.length; i++) {
                n += addFile(fs[i]);
            }
            return n;
        }

	// read just the header information from the DICOM file
        final DicomInputStream in = new DicomInputStream(f);
        boolean successful = false;
        try {
            in.setHandler(new StopTagInputHandler(Tag.PixelData));
            final DicomObject dcmobj = in.readDicomObject();

            // If the Media Storage SOP Instance UID is null set it to the SOP Instance UID.
            if(dcmobj.getString(Tag.MediaStorageSOPInstanceUID) == null){
               dcmobj.putString(Tag.MediaStorageSOPInstanceUID, org.dcm4che2.data.VR.UI, dcmobj.getString(Tag.SOPInstanceUID));
            }
            
            // If the Media Storage SOP Class UID is null, set it to the SOP Class UID.
            if(dcmobj.getString(Tag.MediaStorageSOPClassUID) == null){
               dcmobj.putString(Tag.MediaStorageSOPClassUID, org.dcm4che2.data.VR.UI, dcmobj.getString(Tag.SOPClassUID));
            }

            // create the record hierarchy - each patient folder has a study folder, each
            // study has a series and each series has a set of instances (in this case images).
            final DicomObject patrec = ap.makePatientDirectoryRecord(dcmobj);
            final DicomObject styrec = ap.makeStudyDirectoryRecord(dcmobj);
            final DicomObject serrec = ap.makeSeriesDirectoryRecord(dcmobj);
            final DicomObject instrec = ap.makeInstanceDirectoryRecord(dcmobj, dicomdir.toFileID(f));

            DicomObject rec = writer().addPatientRecord(patrec);
            if (rec == patrec) {
                ++n;
            }
            rec = writer().addStudyRecord(rec, styrec);
            if (rec == styrec) {
                ++n;
            }
            rec = writer().addSeriesRecord(rec, serrec);
            if (rec == serrec) {
                ++n;
            }
            if (n == 0 && checkDuplicate) {
                String iuid = dcmobj.getString(Tag.MediaStorageSOPInstanceUID);
                if (dicomdir.findInstanceRecord(rec, iuid) != null) {
                    return 0;
                }
            }
            writer().addChildRecord(rec, instrec);
            successful = true;
            // add the instance record
            return n + 1;
        }
        finally {
            try {in.close();} catch (IOException e) {
                if (successful) throw e;
                else logger.error("Unable to add file to DICOMDIR", e);
            }
        }
    }
    
    public void close() throws IOException {
        dicomdir.close();
    }
}
