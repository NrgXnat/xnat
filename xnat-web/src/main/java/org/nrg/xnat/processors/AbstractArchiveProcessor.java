package org.nrg.xnat.processors;

import org.dcm4che2.data.DicomObject;
import org.nrg.action.ServerException;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.dicom.mizer.service.MizerService;

import java.util.Map;

public abstract class AbstractArchiveProcessor implements ArchiveProcessor {
    @Override
    public boolean process(final DicomObject metadata, final SessionData sessionData, Map<String, String> parameters) throws ServerException{
        return process(metadata, metadata, sessionData, parameters);
    }

    @Override
    public boolean process(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, Map<String, String> parameters) throws ServerException{
        return process(metadata, metadata, sessionData, null, parameters);
    }

    //Should return a boolean representing whether processing should continue. If it returns true, other processors will
    // be executed and then the data will be written (unless other issues are encountered). If it returns false, the
    // data being processed will not be written. If a ServerException is thrown, the data being processed will not be
    // written and the exception also may be passed to the calling class.
    @Override
    public abstract boolean process(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, final MizerService mizer, Map<String, String> parameters) throws ServerException;

    @Override
    public boolean accept(final DicomObject metadata, final SessionData sessionData, Map<String, String> parameters) throws ServerException{
        return accept(metadata, metadata, sessionData, parameters);
    }

    @Override
    public boolean accept(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, Map<String, String> parameters) throws ServerException{
        return accept(metadata, metadata, sessionData, null, parameters);
    }

    @Override
    public boolean accept(final DicomObject metadata, final DicomObject imageData, final SessionData sessionData, final MizerService mizer, Map<String, String> parameters) throws ServerException{
        return true;
    }
}
