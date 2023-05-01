package org.nrg.xnat.customforms.service;

import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.entities.CustomVariableAppliesTo;

import java.util.List;


public interface CustomVariableAppliesToService extends BaseHibernateService<CustomVariableAppliesTo> {

    CustomVariableAppliesTo findById(final long id);

    List<CustomVariableAppliesTo> findAllByScopeEntityIdDataType(Scope scope,
                                                                        String entityId,
                                                                        String dataType);

    void saveOrUpdate(CustomVariableAppliesTo customVariableAppliesTo);

    List<CustomVariableAppliesTo> filterByStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(Scope scope,
                                                                                                String entityId,
                                                                                                String dataType,
                                                                                                String protocol,
                                                                                                String visit,
                                                                                                String subtype,
                                                                                                String status);

    List<CustomVariableAppliesTo> filterByStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(Scope scope,
                                                                                                String entityId,
                                                                                                String dataType,
                                                                                                String protocol,
                                                                                                String visit,
                                                                                                String subtype,
                                                                                                String status,
                                                                                                boolean imposeIsNull);

    List<CustomVariableAppliesTo> filterByPossibleStatusFindByScopeEntityIdDataTypeProtocolVisitSubtype(Scope scope,
                                                                                                        String entityId,
                                                                                                        String dataType,
                                                                                                        String protocol,
                                                                                                        String visit,
                                                                                                        String subtype,
                                                                                                        List<String> status);

    List<CustomVariableAppliesTo> filterByPossibleStatusFindWithoutVisitAndProtocol(Scope scope,
                                                                                                               String entityId,
                                                                                                               String dataType,
                                                                                                               List<String> statuses,
                                                                                                               boolean imposeIsNull);

    List<CustomVariableAppliesTo> findAllByScopeEntityIdDataType(String dataType);

    void evict(CustomVariableAppliesTo appliesTo);

}
