package org.nrg.xnat.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.action.ServerException;
import org.nrg.config.entities.Configuration;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.restlet.data.Status;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class StudyRemappingArchiveProcessor extends AbstractArchiveProcessor {

    @Override
    public boolean process(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, final MizerService mizer, Map<String, String> parameters) throws ServerException{
        try {
            final String studyInstanceUID = imageData.getString(Tag.StudyInstanceUID);

            String script = DefaultAnonUtils.getService().getStudyScript(studyInstanceUID);

            if(StringUtils.isNotBlank(script)){
                mizer.anonymize(metadata, sessionData.getProject(), sessionData.getSubject(), sessionData.getFolderName(), script);
            }
        } catch (Throwable e) {
            log.debug("Dicom anonymization failed: " + metadata, e);
            throw new ServerException(Status.SERVER_ERROR_INTERNAL,e);
        }
        return true;
    }

    @Override
    public boolean accept(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, final MizerService mizer, Map<String, String> parameters) throws ServerException{
        return true;
    }
}
