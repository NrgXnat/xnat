package org.nrg.xnat.processors;

import org.dcm4che2.data.DicomObject;
import org.nrg.action.ServerException;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.dicom.mizer.service.MizerService;

import java.util.Map;

public interface ArchiveProcessor {
    /**
     * True if the Dicom instance was processed, false if the instance was rejected.
     * @param dicomData
     * @param sessionData
     * @param mizer
     * @param instance
     * @param aeParameters
     * @return
     * @throws ServerException
     */
    boolean process(final DicomObject dicomData, final SessionData sessionData, final MizerService mizer, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) throws ServerException;

    /**
     * true if this archive processor can process Dicom instance.
     *
     * @param dicomData
     * @param sessionData
     * @param mizer
     * @param instance
     * @param aeParameters
     * @return
     * @throws ServerException
     */
    boolean accept(final DicomObject dicomData, final SessionData sessionData, final MizerService mizer, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) throws ServerException;

}
