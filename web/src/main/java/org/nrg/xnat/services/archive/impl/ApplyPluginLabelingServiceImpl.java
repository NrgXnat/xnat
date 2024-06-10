package org.nrg.xnat.services.archive.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.om.XnatSubjectassessordata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.archive.ApplyPluginLabelingService;
import org.nrg.xnat.services.archive.SubjectAssessorLabelingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ApplyPluginLabelingServiceImpl implements ApplyPluginLabelingService {
    private List<SubjectAssessorLabelingService> services = null;

    @Autowired(required = false)
    public void setServices(List<SubjectAssessorLabelingService> services) {
        this.services = services;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public String label(XnatSubjectassessordata sad, Map<String, Object> parameters, UserI user) {
        if (services == null || services.isEmpty()) {
            log.trace("No SubjectAssessorLabelingService beans");
            return null;
        }

        if (services.size() != 1) {
            log.warn("Multiple plugins with SubjectAssessorLabelingService beans: {}. " +
                    "First wins, no order enforced", services);
        }

        for (SubjectAssessorLabelingService s : services) {
            String label = s.determineLabel(sad, parameters, user);
            if (label != null) {
                return label;
            }
        }
        return null;
    }
}
