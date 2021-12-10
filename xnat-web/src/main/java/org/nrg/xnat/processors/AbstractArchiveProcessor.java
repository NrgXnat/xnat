package org.nrg.xnat.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.dcm4che2.data.DicomObject;
import org.nrg.action.ServerException;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.xnat.archive.GradualDicomImporter;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.helpers.prearchive.SessionData;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public abstract class AbstractArchiveProcessor implements ArchiveProcessor {
    protected AbstractArchiveProcessor() {
        log.info("Now in the abstract base constructor for implementation {}", getClass().getName());
    }

    //Should return a boolean representing whether processing should continue. If it returns true, other processors will
    // be executed and then the data will be written (unless other issues are encountered). If it returns false, the
    // data being processed will not be written. If a ServerException is thrown, the data being processed will not be
    // written and the exception also may be passed to the calling class.
    @Override
    public abstract boolean process(final DicomObject dicomData, final SessionData sessionData, final MizerService mizer, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) throws ServerException;

    @Override
    public boolean accept(final DicomObject dicomData, final SessionData sessionData, final MizerService mizer, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) throws ServerException {
        return processorConfiguredForDataComingInToThisScpReceiverAndProject(sessionData, instance, aeParameters);
    }

    protected boolean processorConfiguredForDataComingInToThisScpReceiverAndProject(final SessionData sessionData, ArchiveProcessorInstance instance, Map<String, Object> aeParameters) {
        final Object aeTitle   = aeParameters.get(GradualDicomImporter.RECEIVER_AE_TITLE_PARAM);
        final Object port      = aeParameters.get(GradualDicomImporter.RECEIVER_PORT_PARAM);
        final String aeAndPort = aeTitle != null && port != null ? aeTitle.toString() + ':' + port : null;

        final Set<String> scpWhitelist = instance.getScpWhitelist();
        final Set<String> scpBlacklist = instance.getScpBlacklist();
        if (!scpWhitelist.isEmpty() && !scpWhitelist.contains(aeAndPort) || !scpBlacklist.isEmpty() && scpBlacklist.contains(aeAndPort)) {
            return false;
        }
        //This SCP receiver is set up to use this processor.
        final List<String> projectsToProcess = instance.getProjectIdsList();
        if (CollectionUtils.isEmpty(projectsToProcess)) {
            return true;
        }
        if (sessionData == null) {
            //Project has not yet been set. Processors will not take effect if you try to configure them to
            // take place only on certain projects, but before the project is set.
            return false;
        }
        return projectsToProcess.contains(sessionData.getProject());
    }
}
