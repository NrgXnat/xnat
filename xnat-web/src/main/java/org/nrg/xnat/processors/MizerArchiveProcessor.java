package org.nrg.xnat.processors;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che2.data.DicomObject;
import org.nrg.action.ServerException;
import org.nrg.config.entities.Configuration;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.restlet.data.Status;
import org.springframework.stereotype.Component;
import org.nrg.dicom.mizer.service.MizerService;

import java.util.Map;

@Component
@Slf4j
public class MizerArchiveProcessor extends AbstractArchiveProcessor {

    @Override
    public boolean process(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, final MizerService mizer, Map<String, String> parameters) throws ServerException{
        try {
            Configuration c = DefaultAnonUtils.getCachedSitewideAnon();
            if (c != null && c.getStatus().equals(Configuration.ENABLED_STRING)) {
                //noinspection deprecation
                Long scriptId = c.getId();

                mizer.anonymize(metadata, sessionData.getProject(), sessionData.getSubject(), sessionData.getFolderName(), c.getContents());
            } else {
                log.debug("Anonymization is not enabled, allowing session {} {} {} to proceed without anonymization.", sessionData.getProject(), sessionData.getSubject(), sessionData.getName());
            }
        } catch (Throwable e) {
            log.debug("Dicom anonymization failed: " + metadata, e);
            throw new ServerException(Status.SERVER_ERROR_INTERNAL,e);
        }
        return true;
    }

    @Override
    public boolean accept(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, final MizerService mizer, Map<String, String> parameters) throws ServerException{
        try {
            // check to see of this session came in through an application that may have performed anonymization
            // prior to transfer, e.g. the XNAT Upload Assistant.
            if (sessionData.getPreventAnon()){
                log.debug("The session {} {} {} has already been anonymized by the uploader, proceeding without further anonymization.", sessionData.getProject(), sessionData.getSubject(), sessionData.getName());
                return false;
            }
            else if (DefaultAnonUtils.getService().isSiteWideScriptEnabled()){
                return true;
            }
            else {
                return false;
            }
        } catch (Throwable e) {
            log.debug("Failed check of whether dicom anonymization could be performed: " + metadata, e);
            //Throw exception so we don't just proceed with importing the data without anonymization.
            //I'm not certain whether this is what we want, but this is how it currently works and I don't want to mess anything up.
            throw new ServerException(Status.SERVER_ERROR_INTERNAL, e);
        }
    }
}
