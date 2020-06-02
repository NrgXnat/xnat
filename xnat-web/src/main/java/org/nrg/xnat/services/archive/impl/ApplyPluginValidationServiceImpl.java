package org.nrg.xnat.services.archive.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.om.XnatSubjectassessordata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.archive.ApplyPluginValidationService;
import org.nrg.xnat.services.archive.SubjectAssessorValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ApplyPluginValidationServiceImpl implements ApplyPluginValidationService {
    private List<SubjectAssessorValidationService> services = null;

    @Autowired(required = false)
    public void setServices(List<SubjectAssessorValidationService> services) {
        this.services = services;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(XnatSubjectassessordata sad, Map<String, Object> parameters, UserI user)
            throws ServerException, ClientException {
        if (services == null || services.isEmpty()) {
            log.trace("No SubjectAssessorValidationService beans");
            return;
        }

        for (SubjectAssessorValidationService s : services) {
            s.validate(sad, parameters, user);
        }
    }

}
