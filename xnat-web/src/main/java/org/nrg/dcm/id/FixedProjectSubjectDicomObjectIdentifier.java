package org.nrg.dcm.id;

import org.nrg.dcm.ChainExtractor;
import org.nrg.xnat.DicomObjectIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Used to test configurations and workflows in XNAT archive operations.
 */
@Component
public class FixedProjectSubjectDicomObjectIdentifier extends ReferencedCompositeDicomObjectIdentifier {
    /**
     * Configures a {@link DicomObjectIdentifier DICOM object identifier} that always assigns the DICOM to the submitted
     * project and subject. It will assign the next available ID based on the template:
     *
     * <ul>
     *     <li><i>subject</i>_<i>modality</i><b>n</b></li>
     * </ul>
     *
     * Where <b>n</b> is the next number in sequence that doesn't match any existing experiment labels in the project.
     *
     * @param jdbcTemplate The JDBC template used for querying for available experiment labels.
     * @param projectId    The fixed project ID.
     * @param subjectLabel The fixed subject label.
     */
    @Autowired
    public FixedProjectSubjectDicomObjectIdentifier(final JdbcTemplate jdbcTemplate, final String projectId, final String subjectLabel) {
        super("Fixed project-subject identifier for " + projectId + " and " + subjectLabel,
              new FixedDicomProjectIdentifier(projectId),
              new FixedSubjectExtractor(subjectLabel),
              new NextAvailableSessionLabelExtractor(jdbcTemplate),
              new ChainExtractor(ClassicDicomObjectIdentifier.getAAExtractors()));
    }
}
