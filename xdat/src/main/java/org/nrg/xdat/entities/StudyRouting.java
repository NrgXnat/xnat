package org.nrg.xdat.entities;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xdat.services.StudyRoutingService;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Objects;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class StudyRouting extends AbstractHibernateEntity {
    private static final long serialVersionUID = 7984563149428174135L;

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");

    public static final String STUDY_INSTANCE_UID = "studyInstanceUid";
    public static final String PROJECT_ID         = "projectId";
    public static final String SUBJECT_ID         = "subjectId";
    public static final String LABEL              = "label";
    public static final String USER_ID            = "userId";

    private String _studyInstanceUid;
    private String _projectId;
    private String _subjectId;
    private String _label;
    private String _userId;

    @Column(unique = true, nullable = false, updatable = false)
    public String getStudyInstanceUid() {
        return _studyInstanceUid;
    }

    @SuppressWarnings("unused")
    public void setStudyInstanceUid(final String studyInstanceUid) {
        _studyInstanceUid = studyInstanceUid;
    }

    public String getProjectId() {
        return _projectId;
    }

    public void setProjectId(String projectId) {
        _projectId = projectId;
    }

    public String getSubjectId() {
        return _subjectId;
    }

    public void setSubjectId(String subjectId) {
        _subjectId = subjectId;
    }

    public String getLabel() {
        return _label;
    }

    public void setLabel(String label) {
        _label = label;
    }

    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String userId) {
        _userId = userId;
    }

    public Object[] properties() {
        return new Object[] {getStudyInstanceUid(), getProjectId(), getSubjectId(), getUserId(), getCreated(), getTimestamp(), getLabel()};
    }

    public Map<String, String> toMap() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        if (StringUtils.isNotBlank(getStudyInstanceUid())) {
            builder.put(StudyRouting.STUDY_INSTANCE_UID, getStudyInstanceUid());
        }
        if (StringUtils.isNotBlank(getProjectId())) {
            builder.put(StudyRoutingService.PROJECT, getProjectId());
        }
        if (StringUtils.isNotBlank(getSubjectId())) {
            builder.put(StudyRoutingService.SUBJECT, getSubjectId());
        }
        if (StringUtils.isNotBlank(getLabel())) {
            builder.put(StudyRoutingService.LABEL, getLabel());
        }
        if (StringUtils.isNotBlank(getUserId())) {
            builder.put(StudyRoutingService.USER, getUserId());
        }
        if (getCreated() != null) {
            builder.put(StudyRoutingService.CREATED, FORMATTER.format(getCreated()));
        }
        if (getTimestamp() != null) {
            builder.put(StudyRoutingService.ACCESSED, FORMATTER.format(getTimestamp()));
        }
        return builder.build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _studyInstanceUid, _projectId, _subjectId, _label, _userId);
    }

    protected MoreObjects.ToStringHelper addParentPropertiesToString(final MoreObjects.ToStringHelper toStringHelper) {
        return super.addParentPropertiesToString(toStringHelper)
                    .add(STUDY_INSTANCE_UID, getStudyInstanceUid())
                    .add(PROJECT_ID, getProjectId())
                    .add(SUBJECT_ID, getSubjectId())
                    .add(LABEL, getLabel())
                    .add(USER_ID, getUserId());
    }
}
