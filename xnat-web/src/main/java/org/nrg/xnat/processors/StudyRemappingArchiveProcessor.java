package org.nrg.xnat.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.action.ServerException;
import org.nrg.config.entities.Configuration;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.processor.importer.ProcessorGradualDicomImporter;
import org.restlet.data.Status;
import org.springframework.stereotype.Component;

import java.util.List;
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
                mizer.anonymize(dicomData, proj, subj, folder, script);
            }
        } catch (Throwable e) {
            log.debug("Dicom anonymization failed: " + dicomData, e);
            throw new ServerException(Status.SERVER_ERROR_INTERNAL,e);
        }
        return true;
    }
}
