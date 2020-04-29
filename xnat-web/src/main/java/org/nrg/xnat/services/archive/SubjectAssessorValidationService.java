package org.nrg.xnat.services.archive;

import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.om.XnatSubjectassessordata;
import org.nrg.xft.security.UserI;

import java.util.Map;

public interface SubjectAssessorValidationService {
    /**
     * Validate session data within a plugin
     *
     * @param sad the subejct assessor
     * @param parameters the parameters
     * @param user the user
     * @throws ClientException for warning
     * @throws ServerException for failure
     */
    void validate(XnatSubjectassessordata sad, Map<String, Object> parameters, UserI user)
            throws ClientException, ServerException;
}
