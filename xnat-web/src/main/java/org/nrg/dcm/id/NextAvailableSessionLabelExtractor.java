package org.nrg.dcm.id;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.dcm.Extractor;
import org.nrg.framework.utilities.SortedSets;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.DicomObjectIdentifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.SortedSet;

/**
 * Returns the next available session label for the given project, subject, and DICOM modality.
 */
public class NextAvailableSessionLabelExtractor implements IdentifierReferencingExtractor, SubjectReferencingExtractor {
    public NextAvailableSessionLabelExtractor(final JdbcTemplate template) {
        _template = template;
    }

    @Override
    public void setIdentifier(final DicomObjectIdentifier<XnatProjectdata> identifier) {
        _identifier = identifier;
    }

    @Override
    public void setSubjectExtractor(final Extractor subjectExtractor) {
        _subjectExtractor = subjectExtractor;
    }

    @Override
    public String extract(final DicomObject object) {
        final XnatProjectdata project  = _identifier.getProject(object);
        final String          subject  = _subjectExtractor.extract(object);
        final String          modality = object.getString(Tag.Modality);
        final String          stem     = subject + "_" + modality;

        final List<String> labels = _template.query(String.format(EXPT_QUERY, project.getId(), stem), new RowMapper<String>() {
            @Override
            public String mapRow(final ResultSet results, final int rowNum) throws SQLException {
                return results.getString("label");
            }
        });

        int index = 1;
        while (true) {
            final String sessionLabel = stem + index++;
            if (!labels.contains(sessionLabel)) {
                return sessionLabel;
            }
        }
    }

    @Override
    public SortedSet<Integer> getTags() {
        return SortedSets.empty();
    }

    private static final String EXPT_QUERY = "SELECT label FROM xnat_experimentdata WHERE project = '%s' AND label LIKE '%s%%'";

    private final JdbcTemplate _template;

    private DicomObjectIdentifier<XnatProjectdata> _identifier;
    private Extractor                              _subjectExtractor;
}
