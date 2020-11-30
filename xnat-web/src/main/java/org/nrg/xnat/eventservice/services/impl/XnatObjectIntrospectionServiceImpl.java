package org.nrg.xnat.eventservice.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.model.XnatAbstractprojectassetI;
import org.nrg.xdat.model.XnatExperimentdataI;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xnat.eventservice.services.XnatObjectIntrospectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class XnatObjectIntrospectionServiceImpl implements XnatObjectIntrospectionService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public XnatObjectIntrospectionServiceImpl(final NamedParameterJdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }


    @Override
    public Boolean isModified(XnatExperimentdataI experiment) {
        List<Map<String, Object>> result = simpleQuery(QUERY_IS_EXPERIMENT_MODIFIED,"experimentId", experiment.getId());
        if (!result.isEmpty() && result.get(0).containsKey("modified") && result.get(0).get("modified").equals(1)) return true;
        else return false;
    }

    public Boolean hasResource(XnatExperimentdataI experiment) {
        List<Map<String, Object>> result = simpleQuery(QUERY_EXPERIMENTDATA_RESOURCE, "experimentId", experiment.getId());
        if (result == null || result.isEmpty()) return false;
        else return true;
    }

    public Boolean hasHistory(XnatSubjectdataI subject) {
        List<Map<String, Object>> result = simpleQuery(QUERY_SUBJECTDATA_HISTORY,"subjectId", subject.getId());
        if (result == null || result.isEmpty()) return false;
        else return true;
    }

    @Override
    public Boolean hasResource(XnatSubjectdataI subject) {
        List<Map<String, Object>> result = simpleQuery(QUERY_SUBJECTDATA_RESOURCE,"subjectId", subject.getId());
        if (result == null || result.isEmpty()) return false;
        else return true;
    }

    @Override
    public Boolean storedInDatabase(XnatSubjectdataI subject) {
        List<Map<String, Object>> result = simpleQuery(QUERY_SUBJECTDATA, "subjectId", subject.getId());
        if(result != null && !result.isEmpty()){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<String> getStoredImageSessionIds(XnatSubjectdataI subject) {
        List<String> sessionIds = new ArrayList<>();
        final List<Map<String, Object>> sessions = simpleQuery(QUERY_IMAGESESSIONS_BY_SUBJECT, "subjectId", subject.getId());
        if(sessions != null){
            sessions.forEach(session->sessionIds.add((String) session.get("id")));
        }
        return sessionIds;
    }

    @Override
    public List<String> getStoredSubjectAssessorIds(XnatSubjectdataI subject) {
        List<String> assessorIds = new ArrayList<>();
        final List<Map<String, Object>> assessors = simpleQuery(QUERY_SUBJECTASSESSORS_BY_SUBJECT, "subjectId", subject.getId());
        if(assessors != null){
            assessors.forEach(session->assessorIds.add((String) session.get("id")));
        }
        return assessorIds;
    }

    @Override
    public List<String> getStoredNonImageSubjectAssessorIds(XnatSubjectdataI subject) {
        List<String> assessorIds = new ArrayList<>();
        final List<Map<String, Object>> assessors = simpleQuery(QUERY_NONIMAGEASSEORS_BY_SUBJECT, "subjectId", subject.getId());
        if(assessors != null){
            assessors.forEach(session->assessorIds.add((String) session.get("id")));
        }
        return assessorIds;
    }


    @Override
    public Boolean storedInDatabase(XnatExperimentdata experiment) {
        List<Map<String, Object>> result = simpleQuery(QUERY_EXPERIMENTDATA, "experimentId", experiment.getId());
        if(result != null && !result.isEmpty()){
            return true;
        } else {
            return false;
        }    }

    @Override
    public List<String> getStoredScanIds(XnatExperimentdata experiment) {
        List<String> scanIds = new ArrayList<>();
        final List<Map<String, Object>> scans = simpleQuery(QUERY_SCANDATAID, "experimentId", experiment.getId());
        if(scans != null){
            scans.forEach(scan->scanIds.add((String) scan.get("id")));
        }
        return scanIds;
    }

    @Override
    public List<String> getStoredScanResourceLabels(XnatImagescandataI scan) {
        List<String> labels = new ArrayList<>();
        final List<Map<String, Object>> resources = simpleQuery(QUERY_SCANRESOURCELABEL, "scanId", scan.getXnatImagescandataId());
        if(resources != null){
            resources.forEach(rc->labels.add((String) rc.get("label")));
        }
        return labels;
    }

    @Override
    public boolean storedInDatabase(XnatImageassessordataI assessor) {
        List<Map<String, Object>> result = simpleQuery(QUERY_IMAGEASSESSORDATA, "imageAssessorId", assessor.getId());
        if(result != null && !result.isEmpty()){
            return true;
        } else {
            return false;
        }    }

    @Override
    public boolean storedInDatabase(XnatSubjectassessordataI subjectAssessor) {
        Integer result = jdbcTemplate.queryForObject(QUERY_COUNT_SUBJECTASESSORS_BY_ID,
                new MapSqlParameterSource("subjectAssessorId", subjectAssessor.getId()), Integer.class);
        return result > 0;
    }

    @Override
    public boolean storedInDatabase(XnatAbstractprojectassetI projectAsset) {
        Integer result = jdbcTemplate.queryForObject(QUERY_COUNT_PROJECTASSET_BY_ID,
                new MapSqlParameterSource("projectAssetId", projectAsset.getId()), Integer.class);
        return result > 0;
    }


    @Override
    public Integer getResourceCount(XnatProjectdataI project) {
        Integer result = jdbcTemplate.queryForObject(COUNT_PROJECTDATA_RESOURCES,
                new MapSqlParameterSource("projectId", project.getId()), Integer.class);
        return result;
    }


    private List<Map<String, Object>> simpleQuery(String queryString, String parameterName, String parameterValue){
        try {
            return jdbcTemplate.queryForList(queryString, new MapSqlParameterSource(parameterName, parameterValue));
        } catch (Throwable e){
            log.error("XnatObjectIntrospectionService DB query failed. " + e.getMessage());
        }
        return null;
    }

    private List<Map<String, Object>> simpleQuery(String queryString, String parameterName, Integer parameterValue){
        try {
            return jdbcTemplate.queryForList(queryString, new MapSqlParameterSource(parameterName, parameterValue));
        } catch (Throwable e){
            log.error("XnatObjectIntrospectionService DB query failed. " + e.getMessage());
        }
        return null;
    }

    private static final String QUERY_IS_EXPERIMENT_MODIFIED =  "SELECT xnat_experimentdata_meta_data.modified AS modified FROM xnat_experimentdata_meta_data WHERE meta_data_id IN " +
                                                                    "(SELECT experimentdata_info FROM xnat_experimentData WHERE id = :experimentId)";

    private static final String QUERY_EXPERIMENTDATA_RESOURCE = "SELECT * FROM xnat_experimentdata_resource WHERE xnat_experimentdata_id = :experimentId";

    private static final String QUERY_SUBJECTDATA_HISTORY = "SELECT * FROM xnat_subjectdata_history WHERE subjectdata_info IN (SELECT subjectdata_info FROM xnat_subjectdata WHERE id = :subjectId)";

    private static final String QUERY_SUBJECTDATA_RESOURCE = "SELECT * FROM xnat_subjectdata_resource WHERE xnat_subjectdata_id = :subjectId";

    private static final String QUERY_SUBJECTDATA = "SELECT * FROM xnat_subjectdata WHERE id = :subjectId";

    private static final String QUERY_IMAGESESSIONS_BY_SUBJECT = "SELECT id FROM xnat_imagesessiondata WHERE id IN (SELECT id from xnat_subjectassessordata WHERE subject_id = :subjectId)";

    private static final String QUERY_SUBJECTASSESSORS_BY_SUBJECT = "SELECT id FROM xnat_subjectassessordata WHERE subject_id = :subjectId";

    private static final String QUERY_NONIMAGEASSEORS_BY_SUBJECT = "SELECT id FROM xnat_subjectassessordata WHERE subject_id = :subjectId AND id NOT IN (SELECT id from xnat_imagesessiondata)";

    private static final String QUERY_EXPERIMENTDATA = "SELECT * FROM xnat_experimentdata WHERE id = :experimentId";

    private static final String QUERY_IMAGEASSESSORDATA = "SELECT * FROM xnat_imageassessordata WHERE id = :imageAssessorId";

    private static final String QUERY_COUNT_SUBJECTASESSORS_BY_ID = "SELECT COUNT(id) from xnat_subjectassessordata WHERE id = :subjectAssessorId";

    private static final String QUERY_COUNT_PROJECTASSET_BY_ID = "SELECT COUNT(id) from xnat_abstractprojectasset WHERE id = :projectAssetId";

    private static final String QUERY_SCANDATAID = "SELECT id FROM xnat_imagescandata WHERE image_session_id = :experimentId";

    private static final String QUERY_SCANRESOURCELABEL = "SELECT label FROM xnat_abstractresource WHERE xnat_imagescandata_xnat_imagescandata_id = :scanId";

    private static final String COUNT_PROJECTDATA_RESOURCES = "SELECT COUNT(*) FROM xnat_projectdata_meta_data WHERE meta_data_id IN (SELECT projectdata_info FROM xnat_projectdata WHERE id = :projectId)";
}
