package org.nrg.xnat.helpers.resolvers;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.ItemI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Resolver to determine the ID of a project, subject, or experiment based on one or more of the following:
 *
 * <ul>
 *     <li> Project ID</li>
 *     <li> Subject ID or label</li>
 *     <li> Experiment ID or label</li>
 * </ul>
 */
@Service
@Getter(AccessLevel.PRIVATE)
@Slf4j
public class XftDataObjectIdResolver implements ObjectIdResolver<XftDataObjectIdResolver.PSEParameters> {
    @Autowired
    public XftDataObjectIdResolver(final NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Value
    @Builder(builderClassName = "Builder")
    public static class PSEParameters implements ObjectIdResolver.Parameters {
        String project;
        String subject;
        String experiment;

        public boolean hasProject() {
            return StringUtils.isNotBlank(project);
        }

        public boolean hasSubject() {
            return StringUtils.isNotBlank(subject);
        }

        public boolean hasExperiment() {
            return StringUtils.isNotBlank(experiment);
        }

        public SqlParameterSource getParameterSource() {
            final MapSqlParameterSource parameters = new MapSqlParameterSource();
            if (hasProject()) {
                parameters.addValue(PROJECT, project);
            }
            if (hasSubject()) {
                parameters.addValue(SUBJECT, subject);
            }
            if (hasExperiment()) {
                parameters.addValue(EXPERIMENT, experiment);
            }
            return parameters;
        }
    }

    @Override
    public List<Class<? extends ItemI>> identifies() {
        return IDENTIFIES;
    }

    @Override
    public List<List<String>> accepts() {
        return ACCEPTS;
    }

    @Override
    public String resolve(final PSEParameters parameters) throws DataFormatException, NotFoundException {
        final boolean hasProject    = parameters.hasProject();
        final boolean hasSubject    = parameters.hasSubject();
        final boolean hasExperiment = parameters.hasExperiment();

        final String query;
        if (hasExperiment) {
            if (hasProject && hasSubject) {
                query = QUERY_PROJECT_SUBJECT_EXPERIMENT;
            } else if (hasProject) {
                query = QUERY_PROJECT_EXPERIMENT;
            } else if (hasSubject) {
                query = QUERY_SUBJECT_EXPERIMENT;
            } else {
                query = QUERY_EXPERIMENT;
            }
        } else if (hasSubject) {
            query = hasProject ? QUERY_PROJECT_SUBJECT : QUERY_SUBJECT;
        } else if (hasProject) {
            query = QUERY_PROJECT;
        } else {
            throw new DataFormatException("You must specify at least one of project, subject, or experiment");
        }

        try {
            return getTemplate().queryForObject(query, parameters.getParameterSource(), String.class);
        } catch (EmptyResultDataAccessException e) {
            final List<String> submitted = new ArrayList<>();
            if (hasProject) {
                submitted.add("project: " + parameters.getProject());
            }
            if (hasSubject) {
                submitted.add("subject: " + parameters.getSubject());
            }
            if (hasExperiment) {
                submitted.add("experiment: " + parameters.getExperiment());
            }
            log.error("No existing object found for parameters:\n\n * {}\n\nQuery:\n\n{}", String.join("\n * ", submitted), query);
            throw new NotFoundException("No existing object found for " + String.join(", ", submitted));
        }
    }

    private static final String                       PROJECT_ID_PATH                  = XnatProjectdata.SCHEMA_ELEMENT_NAME + "/ID";
    private static final String                       SUBJECT_ID_PATH                  = XnatSubjectdata.SCHEMA_ELEMENT_NAME + "/ID";
    private static final String                       SUBJECT_LABEL_PATH               = XnatSubjectdata.SCHEMA_ELEMENT_NAME + "/label";
    private static final String                       EXPERIMENT_ID_PATH               = XnatExperimentdata.SCHEMA_ELEMENT_NAME + "/ID";
    private static final String                       EXPERIMENT_LABEL_PATH            = XnatExperimentdata.SCHEMA_ELEMENT_NAME + "/label";
    private static final List<Class<? extends ItemI>> IDENTIFIES                       = Arrays.asList(XnatProjectdata.class, XnatSubjectdata.class, XnatExperimentdata.class);
    private static final List<List<String>>           ACCEPTS                          = Arrays.asList(Collections.singletonList(PROJECT_ID_PATH),
                                                                                                       Collections.singletonList(SUBJECT_ID_PATH),
                                                                                                       Collections.singletonList(EXPERIMENT_ID_PATH),
                                                                                                       Arrays.asList(PROJECT_ID_PATH, SUBJECT_ID_PATH),
                                                                                                       Arrays.asList(PROJECT_ID_PATH, SUBJECT_LABEL_PATH),
                                                                                                       Arrays.asList(PROJECT_ID_PATH, SUBJECT_ID_PATH, EXPERIMENT_ID_PATH),
                                                                                                       Arrays.asList(PROJECT_ID_PATH, SUBJECT_ID_PATH, EXPERIMENT_LABEL_PATH),
                                                                                                       Arrays.asList(PROJECT_ID_PATH, SUBJECT_LABEL_PATH, EXPERIMENT_ID_PATH),
                                                                                                       Arrays.asList(PROJECT_ID_PATH, SUBJECT_LABEL_PATH, EXPERIMENT_LABEL_PATH),
                                                                                                       Arrays.asList(SUBJECT_ID_PATH, EXPERIMENT_ID_PATH),
                                                                                                       Arrays.asList(SUBJECT_ID_PATH, EXPERIMENT_LABEL_PATH));
    private static final String                       QUERY_PROJECT                    = "SELECT " +
                                                                                         "    id AS resolved " +
                                                                                         "FROM " +
                                                                                         "    xnat_projectdata " +
                                                                                         "WHERE id = :project";
    private static final String                       QUERY_SUBJECT                    = "SELECT " +
                                                                                         "    id AS resolved " +
                                                                                         "FROM xnat_subjectdata " +
                                                                                         "WHERE " +
                                                                                         "    id = :subjectect";
    private static final String                       QUERY_EXPERIMENT                 = "SELECT " +
                                                                                         "    e.id AS resolved " +
                                                                                         "FROM " +
                                                                                         "    xnat_experimentdata e " +
                                                                                         "    LEFT JOIN xdat_meta_element m ON e.extension = m.xdat_meta_element_id " +
                                                                                         "WHERE " +
                                                                                         "    e.id = :experiment";
    private static final String                       QUERY_PROJECT_SUBJECT            = "SELECT " +
                                                                                         "    s.id AS resolved " +
                                                                                         "FROM " +
                                                                                         "    xnat_subjectdata s " +
                                                                                         "    LEFT JOIN xnat_projectdata p ON s.project = p.id " +
                                                                                         "WHERE " +
                                                                                         "    (s.id = :subject OR s.label = :subject) AND p.id = :project " +
                                                                                         "UNION " +
                                                                                         "SELECT " +
                                                                                         "    pp.subject_id AS resolved " +
                                                                                         "FROM " +
                                                                                         "    xnat_projectparticipant pp " +
                                                                                         "    LEFT JOIN xnat_projectdata p ON pp.project = p.id " +
                                                                                         "WHERE " +
                                                                                         "    (pp.subject_id = :subject OR pp.label = :subject) AND p.id = :project";
    private static final String                       QUERY_PROJECT_EXPERIMENT         = "SELECT " +
                                                                                         "    x.id AS resolved " +
                                                                                         "FROM " +
                                                                                         "    xnat_experimentdata x " +
                                                                                         "    LEFT JOIN xnat_subjectassessordata a ON a.id = x.id " +
                                                                                         "    LEFT JOIN xnat_subjectdata s ON a.subject_id = s.id " +
                                                                                         "    LEFT JOIN xnat_experimentdata_share sh ON x.id = sh.sharing_share_xnat_experimentda_id AND sh.project = :project " +
                                                                                         "    LEFT JOIN xnat_projectparticipant p ON p.subject_id = a.subject_id AND p.project = :project " +
                                                                                         "    LEFT JOIN xdat_meta_element e ON x.extension = e.xdat_meta_element_id " +
                                                                                         "WHERE " +
                                                                                         "    (((x.id = :experiment OR x.label = :experiment) AND x.project = :project) OR " +
                                                                                         "     ((sh.sharing_share_xnat_experimentda_id = :experiment OR sh.label = :experiment) AND sh.project = :project))";
    private static final String                       QUERY_SUBJECT_EXPERIMENT         = "WITH " +
                                                                                         "    subjects AS " +
                                                                                         "        (SELECT " +
                                                                                         "             s.id, " +
                                                                                         "             s.label, " +
                                                                                         "             s.project " +
                                                                                         "         FROM " +
                                                                                         "             xnat_subjectdata s " +
                                                                                         "         WHERE :subject IN (s.id, s.label) " +
                                                                                         "         UNION " +
                                                                                         "         SELECT " +
                                                                                         "             p.subject_id AS id, " +
                                                                                         "             p.label, " +
                                                                                         "             p.project " +
                                                                                         "         FROM " +
                                                                                         "             xnat_projectparticipant p " +
                                                                                         "         WHERE :subject IN (p.subject_id, label)), " +
                                                                                         "    experiments AS " +
                                                                                         "        (SELECT " +
                                                                                         "             e.id, " +
                                                                                         "             e.label, " +
                                                                                         "             e.project, " +
                                                                                         "             a.subject_id, " +
                                                                                         "             m.element_name, " +
                                                                                         "             (m.element_name || '/project')::VARCHAR(255) AS secured_property " +
                                                                                         "         FROM " +
                                                                                         "             xnat_experimentdata e " +
                                                                                         "             LEFT JOIN xnat_imageassessordata i ON e.id = i.id " +
                                                                                         "             LEFT JOIN xnat_subjectassessordata a ON a.id IN (e.id, i.imagesession_id) " +
                                                                                         "             LEFT JOIN xdat_meta_element m ON e.extension = m.xdat_meta_element_id " +
                                                                                         "         WHERE :experiment IN (e.id, e.label) " +
                                                                                         "         UNION " +
                                                                                         "         SELECT " +
                                                                                         "             s.sharing_share_xnat_experimentda_id AS id, " +
                                                                                         "             s.label, " +
                                                                                         "             s.project, " +
                                                                                         "             a.subject_id, " +
                                                                                         "             m.element_name, " +
                                                                                         "             (m.element_name || '/sharing/share/project')::VARCHAR(255) AS secured_property " +
                                                                                         "         FROM " +
                                                                                         "             xnat_experimentdata_share s " +
                                                                                         "             LEFT JOIN xnat_imageassessordata i ON s.sharing_share_xnat_experimentda_id = i.id " +
                                                                                         "             LEFT JOIN xnat_subjectassessordata a ON a.id IN (s.sharing_share_xnat_experimentda_id, i.imagesession_id) " +
                                                                                         "             LEFT JOIN xnat_experimentdata e ON a.id = e.id " +
                                                                                         "             LEFT JOIN xdat_meta_element m ON e.extension = m.xdat_meta_element_id " +
                                                                                         "         WHERE :experiment IN (s.sharing_share_xnat_experimentda_id, s.label)) " +
                                                                                         "SELECT " +
                                                                                         "    e.id AS resolved " +
                                                                                         "FROM " +
                                                                                         "    experiments e " +
                                                                                         "    LEFT JOIN subjects s ON e.project = s.project AND e.subject_id = s.id " +
                                                                                         "WHERE s.id IS NOT NULL";
    private static final String                       QUERY_PROJECT_SUBJECT_EXPERIMENT = QUERY_PROJECT_EXPERIMENT + " AND " +
                                                                                         "    (((s.id = :subject OR s.label = :subject) AND s.project = :project) OR " +
                                                                                         "     ((p.subject_id = :subject OR p.label = :subject) AND p.project = :project))";

    private static final String SUBJECT    = "subject";
    private static final String PROJECT    = "project";
    private static final String EXPERIMENT = "experiment";

    private final NamedParameterJdbcTemplate template;
}
