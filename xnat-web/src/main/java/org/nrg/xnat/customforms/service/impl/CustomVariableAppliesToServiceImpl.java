package org.nrg.xnat.customforms.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xapi.model.users.User;
import org.nrg.xnat.customforms.daos.CustomVariableAppliesToRepository;
import org.nrg.xnat.customforms.pojo.UserOptionsPojo;
import org.nrg.xnat.customforms.service.CustomVariableAppliesToService;
import org.nrg.xnat.entities.CustomVariableAppliesTo;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class CustomVariableAppliesToServiceImpl
        extends AbstractHibernateEntityService<CustomVariableAppliesTo, CustomVariableAppliesToRepository>
        implements CustomVariableAppliesToService {


    public List<CustomVariableAppliesTo> findAllByScopeEntityIdDataType(Scope scope,
                                                                        String entityId,
                                                                        String dataType) {

        final Map<String, Object> properties = new HashMap<>();
        properties.put("scope", scope);
        if (entityId != null)
            properties.put("entityId", entityId);
        if (dataType != null) {
            properties.put("dataType", dataType);
        }
        return getDao().findByProperties(properties);
    }

    public List<CustomVariableAppliesTo> filterByStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(Scope scope,
                                                                                                       String entityId,
                                                                                                       String dataType,
                                                                                                       String protocol,
                                                                                                       String visit,
                                                                                                       String subtype,
                                                                                                       String status) {
        List<CustomVariableAppliesTo> results = new ArrayList<CustomVariableAppliesTo>();
        List<CustomVariableAppliesTo> rows = getRows(scope, entityId, dataType, protocol, visit, subtype);
        if (rows != null) {
            if (status != null) {
                for (CustomVariableAppliesTo c : rows) {
                    for (CustomVariableFormAppliesTo cfs : c.getCustomVariableFormAppliesTos()) {
                        String rowStatus = cfs.getStatus();
                        if (null != rowStatus && rowStatus.equals(status)) {
                            if (!results.contains(c)) {
                                results.add(c);
                            }
                        }
                    }
                }
            } else {
                results = rows;
            }
        }
        return results;
    }

    public List<CustomVariableAppliesTo> filterByPossibleStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(Scope scope,
                                                                                                               String entityId,
                                                                                                               String dataType,
                                                                                                               String protocol,
                                                                                                               String visit,
                                                                                                               String subtype,
                                                                                                               List<String> statuses
    ) {

        List<CustomVariableAppliesTo> results = new ArrayList<CustomVariableAppliesTo>();
        List<CustomVariableAppliesTo> rows = getRows(scope, entityId, dataType, protocol, visit, subtype);

        if (rows != null) {
            if (statuses != null && statuses.size() > 0) {
                for (CustomVariableAppliesTo c : rows) {
                    for (CustomVariableFormAppliesTo cfs : c.getCustomVariableFormAppliesTos()) {
                        String rowStatus = cfs.getStatus();
                        if (null != rowStatus && statuses.contains(rowStatus) && !results.contains(c)) {
                            results.add(c);
                        }
                    }
                }
            } else {
                results = rows;
            }
        }
        return results;
    }

    public List<CustomVariableAppliesTo> filterByStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(Scope scope,
                                                                                                       String entityId,
                                                                                                       String dataType,
                                                                                                       String protocol,
                                                                                                       String visit,
                                                                                                       String subtype,
                                                                                                       String status,
                                                                                                       boolean imposeIsNull) {
        List<CustomVariableAppliesTo> results = new ArrayList<CustomVariableAppliesTo>();
        UserOptionsPojo userOptionsPojo = toUserOptionsPojo(dataType, protocol,visit,subtype);
        List<CustomVariableAppliesTo> rows = getDao().findByOptions(userOptionsPojo, entityId, imposeIsNull);
        if (rows != null) {
            if (status != null) {
                for (CustomVariableAppliesTo c : rows) {
                    for (CustomVariableFormAppliesTo cfs : c.getCustomVariableFormAppliesTos()) {
                        String rowStatus = cfs.getStatus();
                        if (null != rowStatus && rowStatus.equals(status)) {
                            if (!results.contains(c)) {
                                results.add(c);
                            }
                        }
                    }
                }
            } else {
                results = rows;
            }
        }
        return results;
    }


    public List<CustomVariableAppliesTo> filterByPossibleStatusFindWithoutVisitAndProtocol(Scope scope,
                                                                                                               String entityId,
                                                                                                               String dataType,
                                                                                                               List<String> statuses,
                                                                                                               boolean imposeIsNull
    ) {
        List<CustomVariableAppliesTo> results = new ArrayList<CustomVariableAppliesTo>();
        UserOptionsPojo userOptionsPojo = toUserOptionsPojo(dataType, null,null,null);
        List<CustomVariableAppliesTo> rows = getDao().findByOptions(userOptionsPojo, entityId, imposeIsNull);
        if (rows != null) {
            if (statuses != null) {
                for (CustomVariableAppliesTo c : rows) {
                    for (CustomVariableFormAppliesTo cfs : c.getCustomVariableFormAppliesTos()) {
                        String rowStatus = cfs.getStatus();
                        if (null != rowStatus && statuses.contains(rowStatus) && !results.contains(c)) {
                            results.add(c);
                        }
                    }
                }
            } else {
                results = rows;
            }
        }
        return results;
    }


    private List<CustomVariableAppliesTo> getRows(Scope scope,
                                                  String entityId,
                                                  String dataType,
                                                  String protocol,
                                                  String visit,
                                                  String subtype) {

        final Map<String, Object> properties = new HashMap<>();
        properties.put("scope", scope);
        if (entityId != null) {
            properties.put("entityId", entityId);
        }
        properties.put("dataType", dataType);
        if (null != protocol) {
            properties.put("protocol", protocol);
        }
        if (null != visit) {
            properties.put("visit", visit);
        }
        if (null != subtype) {
            properties.put("subType", subtype);
        }
        List<CustomVariableAppliesTo> rows = getDao().findByProperties(properties);
        return rows;
    }


    public List<CustomVariableAppliesTo> findAllByScopeEntityIdDataType(String dataType) {
        List<CustomVariableAppliesTo> forms = getDao().findByProperty("dataType", dataType);
        return forms;
    }


    public CustomVariableAppliesTo findById(final long id) {
        return getDao().findById(id);
    }



    public void saveOrUpdate(CustomVariableAppliesTo customVariableAppliesTo) {
        getDao().saveOrUpdate(customVariableAppliesTo);
    }



    public void evict(CustomVariableAppliesTo appliesTo) {
        getDao().evict(appliesTo);
    }

    private UserOptionsPojo toUserOptionsPojo(String dataType,
                                              String protocol,
                                              String visit,
                                              String subtype) {
        return new UserOptionsPojo(dataType, protocol, visit, subtype);

    }

}
