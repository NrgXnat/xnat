package org.nrg.xnat.processors;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che2.data.DicomObject;
import org.nrg.action.ServerException;
import org.nrg.config.entities.Configuration;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class DummyArchiveProcessor extends AbstractArchiveProcessor {

    @Override
    public boolean process(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, final MizerService mizer, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) throws ServerException{
        return true;
    }
}
