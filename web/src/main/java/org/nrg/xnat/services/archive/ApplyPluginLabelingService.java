package org.nrg.xnat.services.archive;

import org.nrg.xdat.om.XnatSubjectassessordata;
import org.nrg.xft.security.UserI;

import javax.annotation.Nullable;
import java.util.Map;

public interface ApplyPluginLabelingService {
    /**
     * Determine a label for the session based on SubjectAssessorLabelingService beans.
     *
     * @param sad the subject assessor
     * @param parameters the parameters
     * @param user the user
     * @return the label or null if not applicable to your plugin
     */
    @Nullable
    String label(XnatSubjectassessordata sad, Map<String, Object> parameters, UserI user);
}
