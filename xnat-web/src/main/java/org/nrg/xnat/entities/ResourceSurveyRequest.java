package org.nrg.xnat.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.Type;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.services.archive.ResourceMitigationReport;
import org.nrg.xnat.services.archive.ResourceSurveyReport;
import org.springframework.jdbc.core.RowMapper;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"projectId", "subjectId", "experimentId", "scanId", "resourceLabel", "closingDate"})})
@Access(AccessType.FIELD)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
@NamedQueries({@NamedQuery(name = "findRequestIdAndProject", query = "SELECT r.id, r.projectId FROM ResourceSurveyRequest r WHERE r.id IN (:requestIds)"),
               @NamedQuery(name = "findRequestAndResourceId", query = "SELECT r.id, r.resourceId FROM ResourceSurveyRequest r WHERE r.resourceId IN (:resourceIds) AND r.closingDate IS NULL"),
               @NamedQuery(name = "findRequestsForProject", query = "SELECT r.id FROM ResourceSurveyRequest r WHERE r.projectId = :projectId AND r.closingDate IS NULL")})
public class ResourceSurveyRequest extends AbstractHibernateEntity {
    private static final long   serialVersionUID       = -2595929129619207093L;
    private static final String TEMPLATE_MITIGATION_ID = "mitigation-%s-%d-%s";

    public static final Comparator<ResourceSurveyRequest> REQUESTS_BY_DATE = Comparator.comparing(AbstractHibernateEntity::getTimestamp);
    public static final String                            BEAN_NAME        = "resourceSurveyRequest";
    public static final RowMapper<ResourceSurveyRequest>  ROW_MAPPER       = (resultSet, index) -> ResourceSurveyRequest.builder()
                                                                                                                        .subjectLabel(resultSet.getString("subject_label"))
                                                                                                                        .experimentLabel(resultSet.getString("experiment_label"))
                                                                                                                        .scanLabel(resultSet.getString("scan_label"))
                                                                                                                        .scanDescription(resultSet.getString("scan_description"))
                                                                                                                        .projectId(resultSet.getString("project_id"))
                                                                                                                        .subjectId(resultSet.getString("subject_id"))
                                                                                                                        .experimentId(resultSet.getString("experiment_id"))
                                                                                                                        .xsiType(resultSet.getString("xsi_type"))
                                                                                                                        .scanId(resultSet.getInt("scan_id"))
                                                                                                                        .resourceId(resultSet.getInt("resource_id"))
                                                                                                                        .resourceLabel(resultSet.getString("resource_label"))
                                                                                                                        .resourceUri(resultSet.getString("resource_uri"))
                                                                                                                        .requester(resultSet.getString("requester"))
                                                                                                                        .mitigationRequester(resultSet.getString("mitigation_requester"))
                                                                                                                        .requestTime(resultSet.getString("request_time"))
                                                                                                                        .build();

    public enum Status {
        CREATED,
        QUEUED_FOR_SURVEY,
        SURVEYING,
        DIVERGENT,
        CONFORMING,
        QUEUED_FOR_MITIGATION,
        MITIGATING,
        RESOURCE_DELETED,
        CANCELED,
        ERROR;

        public static final String       ALL_VALUES     = Arrays.stream(ResourceSurveyRequest.Status.values()).map(Objects::toString).collect(Collectors.joining(", "));
        public static final List<Status> SURVEY_VALUES  = Arrays.asList(CREATED, QUEUED_FOR_SURVEY, SURVEYING, DIVERGENT);
        public static final List<Status> CLOSING_VALUES = Arrays.asList(CONFORMING, RESOURCE_DELETED, CANCELED, ERROR);

        public static Optional<Status> parse(final String status) {
            return Arrays.stream(values())
                         .filter(value -> StringUtils.equalsIgnoreCase(status, value.toString()))
                         .findFirst();
        }

        public static boolean isClosingStatus(final Status status) {
            return CLOSING_VALUES.contains(status);
        }
    }

    /**
     * Gets the operation-specific ID for the current resource survey request. This consists of a few properties,
     * including:
     *
     * <ul>
     *     <li>{@link #getExperimentId() Experiment ID}</li>
     *     <li>{@link #getScanId() Scan ID}</li>
     *     <li>{@link #getRequestTime() Request time}</li>
     * </ul>
     * <p>
     * This provides a unique identifier to distinguish <i>this</i> survey request from other survey requests that may
     * have been or be performed on the same scan data.
     *
     * @return The unique resource survey request mitigation ID.
     */
    @JsonIgnore
    @Transient
    public String getMitigationId() {
        return String.format(ResourceSurveyRequest.TEMPLATE_MITIGATION_ID, getExperimentId(), getScanId(), getRequestTime());
    }

    @JsonIgnore
    @Transient
    public boolean isOpen() {
        return closingDate == null;
    }

    public void setRsnStatus(final Status status) {
        if (Status.isClosingStatus(status)) {
            closingDate = new Date();
        }
        rsnStatus = status;
    }

    @Positive
    private int resourceId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status rsnStatus = Status.CREATED;

    @NonNull
    @NotNull
    private String projectId;

    @NonNull
    @NotNull
    private String subjectId;

    @NonNull
    @NotNull
    private String experimentId;

    @NonNull
    @NotNull
    private String xsiType;

    @NotNull
    private int scanId;

    @NonNull
    @NotNull
    private String resourceLabel;

    @NonNull
    @NotNull
    private String resourceUri;

    private String subjectLabel;
    private String experimentLabel;
    private String scanLabel;
    private String scanDescription;

    private int workflowId;

    @NonNull
    @NotNull
    private String requester;

    private String mitigationRequester;

    @NonNull
    @NotNull
    private String requestTime;

    private Date closingDate;

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(columnDefinition = "json")
    private ResourceSurveyReport surveyReport;

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(columnDefinition = "json")
    private ResourceMitigationReport mitigationReport;
}
