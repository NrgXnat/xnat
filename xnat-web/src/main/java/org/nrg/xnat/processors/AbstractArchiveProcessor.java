package org.nrg.xnat.processors;

import org.dcm4che2.data.DicomObject;
import org.nrg.action.ServerException;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.xnat.processor.importer.ProcessorGradualDicomImporter;

import java.util.List;
import java.util.Map;

public abstract class AbstractArchiveProcessor implements ArchiveProcessor {
    @Override
    public boolean process(final DicomObject metadata, final SessionData sessionData, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) throws ServerException{
        return process(metadata, metadata, sessionData, instance, aeParameters);
    }

    @Override
    public boolean process(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) throws ServerException{
        return process(metadata, metadata, sessionData, null, instance, aeParameters);
    }

    //Should return a boolean representing whether processing should continue. If it returns true, other processors will
    // be executed and then the data will be written (unless other issues are encountered). If it returns false, the
    // data being processed will not be written. If a ServerException is thrown, the data being processed will not be
    // written and the exception also may be passed to the calling class.
    @Override
    public abstract boolean process(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, final MizerService mizer, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) throws ServerException;

    @Override
    public boolean accept(final DicomObject metadata, final SessionData sessionData, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) throws ServerException{
        return accept(metadata, metadata, sessionData, instance, aeParameters);
    }

    @Override
    public boolean accept(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) throws ServerException{
        return accept(metadata, metadata, sessionData, null, instance, aeParameters);
    }

    @Override
    public boolean accept(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, final MizerService mizer, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) throws ServerException{
        return true;
    }

    protected boolean processorConfiguredForDataComingInToThisScpReceiver(ArchiveProcessorInstance instance, Map<String, Object> aeParameters){
        Object aeTitle = aeParameters.get(ProcessorGradualDicomImporter.RECEIVER_AE_TITLE_PARAM);
        Object port = aeParameters.get(ProcessorGradualDicomImporter.RECEIVER_PORT_PARAM);
        String aeAndPort = null;
        if(aeTitle!=null && port!=null){
            aeAndPort = aeTitle.toString()+':'+port.toString();
        }
        List<String> scpWhitelist = instance.getScpWhitelist();
        List<String> scpBlacklist = instance.getScpBlacklist();
        if(scpWhitelist.isEmpty()||scpWhitelist.contains(aeAndPort)){
            if(scpBlacklist.isEmpty()||!scpBlacklist.contains(aeAndPort)){
                //This SCP receiver is set up to use this processor.
                return true;
            }
        }
        return false;
    }
}
