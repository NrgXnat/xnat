package org.nrg.xnat.processors;

import org.dcm4che2.data.DicomObject;
import org.nrg.action.ServerException;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.dicom.mizer.service.MizerService;

import java.util.Map;

public interface ArchiveProcessor {
    boolean process(final DicomObject metadata, final SessionData sessionData, Map<String, String> parameters) throws ServerException;
    boolean process(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, Map<String, String> parameters) throws ServerException;
    boolean process(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, final MizerService mizer, Map<String, String> parameters) throws ServerException;
    boolean accept(final DicomObject metadata, final SessionData sessionData, Map<String, String> parameters) throws ServerException;
    boolean accept(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, Map<String, String> parameters) throws ServerException;
    boolean accept(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, final MizerService mizer, Map<String, String> parameters) throws ServerException;

}
