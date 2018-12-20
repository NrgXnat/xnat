package org.nrg.xnat.processors;

import org.dcm4che2.data.DicomObject;
import org.nrg.action.ServerException;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.xnat.processor.importer.ProcessorGradualDicomImporter;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractArchiveProcessor implements ArchiveProcessor {
    //Should return a boolean representing whether processing should continue. If it returns true, other processors will
    // be executed and then the data will be written (unless other issues are encountered). If it returns false, the
    // data being processed will not be written. If a ServerException is thrown, the data being processed will not be
    // written and the exception also may be passed to the calling class.
    @Override
    public abstract boolean process(final DicomObject dicomData, final SessionData sessionData, final MizerService mizer, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) throws ServerException;

    @Override
    public boolean accept(final DicomObject dicomData, final SessionData sessionData, final MizerService mizer, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) throws ServerException{
        return processorConfiguredForDataComingInToThisScpReceiverAndProject(sessionData, instance, aeParameters);
    }

    protected boolean processorConfiguredForDataComingInToThisScpReceiverAndProject(final SessionData sessionData, ArchiveProcessorInstance instance, Map<String, Object> aeParameters){
        Object aeTitle = aeParameters.get(ProcessorGradualDicomImporter.RECEIVER_AE_TITLE_PARAM);
        Object port = aeParameters.get(ProcessorGradualDicomImporter.RECEIVER_PORT_PARAM);
        String aeAndPort = null;
        if(aeTitle!=null && port!=null){
            aeAndPort = aeTitle.toString()+':'+port.toString();
        }
        Set<String> scpWhitelist = instance.getScpWhitelist();
        Set<String> scpBlacklist = instance.getScpBlacklist();
        if(scpWhitelist.isEmpty()||scpWhitelist.contains(aeAndPort)){
            if(scpBlacklist.isEmpty()||!scpBlacklist.contains(aeAndPort)){
                //This SCP receiver is set up to use this processor.

                List<String> projectsToProcess = instance.getProjectIdsList();
                if(projectsToProcess!=null && projectsToProcess.size()>0){
                    if(sessionData==null){
                        //Project has not yet been set. Processors will not take effect if you try to configure them to
                        // take place only on certain projects, but before the project is set.
                        return false;
                    }
                    else if(projectsToProcess.contains(sessionData.getProject())){
                        return true;
                    }
                    else{
                        return false;
                    }
                }

                return true;
            }
        }
        return false;
    }
}
