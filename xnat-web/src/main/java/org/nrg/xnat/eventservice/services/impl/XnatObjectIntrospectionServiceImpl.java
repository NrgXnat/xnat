package org.nrg.xnat.eventservice.services.impl;

import org.nrg.xdat.model.XnatExperimentdataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xnat.eventservice.services.XnatObjectIntrospectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class XnatObjectIntrospectionServiceImpl implements XnatObjectIntrospectionService {

    private static final Logger log = LoggerFactory.getLogger(XnatObjectIntrospectionService.class);
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
    public Boolean storedInDatabase(XnatExperimentdata experiment) {
        List<Map<String, Object>> result = simpleQuery(QUERY_EXPERIMENTDATA, "experimentId", experiment.getId());
        if(result != null && !result.isEmpty()){
            return true;
        } else {
            return false;
        }    }

    @Override
    public List<String> getScanIds(XnatExperimentdata experiment) {
        final List<Map<String, Object>> scans = simpleQuery(QUERY_SCANDATAID, "experimentId", experiment.getId());
        if(scans != null){
            List<String> scanIds = new ArrayList<>();
            scans.forEach(scan->scanIds.add((String) scan.get("id")));
            return scanIds;
        }
        return null;
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


    private static final String QUERY_IS_EXPERIMENT_MODIFIED =  "SELECT xnat_experimentdata_meta_data.modified AS modified FROM xnat_experimentdata_meta_data WHERE meta_data_id IN " +
                                                                    "(SELECT experimentdata_info FROM xnat_experimentData WHERE id = :experimentId)";

    private static final String QUERY_EXPERIMENTDATA_RESOURCE = "SELECT * FROM xnat_experimentdata_resource WHERE xnat_experimentdata_id = :experimentId";

    private static final String QUERY_SUBJECTDATA_HISTORY = "SELECT * FROM xnat_subjectdata_history WHERE subjectdata_info IN (SELECT subjectdata_info FROM xnat_subjectdata WHERE id = :subjectId)";

    private static final String QUERY_SUBJECTDATA_RESOURCE = "SELECT * FROM xnat_subjectdata_resource WHERE xnat_subjectdata_id = :subjectId";

    private static final String QUERY_SUBJECTDATA = "SELECT * FROM xnat_subjectdata WHERE id = :subjectId";

    private static final String QUERY_EXPERIMENTDATA = "SELECT * FROM xnat_experimentdata WHERE id = :experimentId";

    private static final String QUERY_SCANDATAID = "SELECT id FROM xnat_imagescandata WHERE image_session_id = :experimentId";

    private static final String COUNT_PROJECTDATA_RESOURCES = "SELECT COUNT(*) FROM xnat_projectdata_meta_data WHERE meta_data_id IN (SELECT projectdata_info FROM xnat_projectdata WHERE id = :projectId)";
}
