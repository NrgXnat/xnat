package org.nrg.xnat.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.action.ServerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.AnonymizationResultNoOp;
import org.nrg.dicom.mizer.objects.AnonymizationResultReject;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.restlet.data.Status;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class StudyRemappingArchiveProcessor extends AbstractArchiveProcessor {

    @Override
    public boolean process(final DicomObject dicomData, final SessionData sessionData, final MizerService mizer, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) throws ServerException{
        try {
            final String studyInstanceUID = dicomData.getString(Tag.StudyInstanceUID);

            String script = DefaultAnonUtils.getService().getStudyScript(studyInstanceUID);

            if(StringUtils.isNotBlank(script)){
                String proj = "";
                String subj = "";
                String folder = "";
                if(sessionData!=null){
                    proj = sessionData.getProject();
                    subj = sessionData.getSubject();
                    folder = sessionData.getFolderName();
                }
                AnonymizationResult result = mizer.anonymize(dicomData, proj, subj, folder, script, true);
                if (result instanceof AnonymizationResultError) {
                    String msg = result.getMessage();
                    log.debug("Dicom anonymization failed: {}: {}", dicomData, msg);
                    throw new ServerException(Status.SERVER_ERROR_INTERNAL,msg);
                }
                if ( result instanceof AnonymizationResultNoOp) {
                    return false;
                }
            }
        } catch (Throwable e) {
            log.debug("Dicom anonymization failed: " + dicomData, e);
            throw new ServerException(Status.SERVER_ERROR_INTERNAL,e);
        }
        return true;
    }
}
