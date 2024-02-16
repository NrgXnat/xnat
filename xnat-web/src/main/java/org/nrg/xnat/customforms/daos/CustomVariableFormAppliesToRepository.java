package org.nrg.xnat.customforms.daos;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.customforms.pojo.UserOptionsPojo;
import org.nrg.xnat.customforms.pojo.formio.RowIdentifier;
import org.nrg.xnat.customforms.utils.CustomFormHibernateUtils;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.entities.CustomVariableAppliesTo;
import org.nrg.xnat.entities.CustomVariableForm;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Repository
public class CustomVariableFormAppliesToRepository extends AbstractHibernateDAO<CustomVariableFormAppliesTo> {

    //This is required as the id column is not the PK anymore
    @Override
    public void saveOrUpdate(CustomVariableFormAppliesTo customVariableFormAppliesTo) {
        final long cvatId = customVariableFormAppliesTo.getCustomVariableAppliesTo().getId();
        long cvfId = customVariableFormAppliesTo.getCustomVariableForm().getId();
        try {
            final Criteria criteria = getCriteriaWithAlias();
            criteria.add(Restrictions.eq("cvat.id", cvatId));
            criteria.add(Restrictions.eq("cvf.id", cvfId));

            if (!criteria.list().isEmpty()) {
                update(customVariableFormAppliesTo);
            } else {
                create(customVariableFormAppliesTo);
            }
        } catch (NonUniqueObjectException e) {
            getSession().merge(customVariableFormAppliesTo);
        }
    }

    /**
     * Finds a row from the join table, by FormID and AppliesToID
     * @param rowId - the Row Identifier @see org.nrg.xnat.customforms.pojo.formio.RowIdentifier;
     * @return Matched row CustomVariableFormAppliesTo - null if not found
     */
    @Nullable
    public CustomVariableFormAppliesTo findByRowIdentifier(RowIdentifier rowId) {
        Criteria criteria = getCriteriaWithAlias();
        criteria.add(Restrictions.eq("cvat.id", rowId.getAppliesToId()));
        criteria.add(Restrictions.eq("cvf.id", rowId.getFormId()));
        CustomVariableFormAppliesTo unique = (CustomVariableFormAppliesTo) criteria.uniqueResult();
        initializeChild(unique);
        return unique;
    }

    /**
     * Finds a row from the join table for a Project and FormID
     * @param projectId - the project id
     * @param formId - the form id
     * @return matched row or null - CustomVariableFormAppliesTo
     */

    @Nullable
    public CustomVariableFormAppliesTo findForProject(final String projectId, final long formId) {
        Criteria criteria = getCriteriaWithAlias();
        criteria.add(Restrictions.eq("cvat.scope", Scope.Project));
        criteria.add(Restrictions.eq("cvat.entityId", projectId));
        criteria.add(Restrictions.eq("cvf.id", formId));
        CustomVariableFormAppliesTo unique = (CustomVariableFormAppliesTo) criteria.uniqueResult();
        initializeChild(unique);
        return unique;
    }


    /**
     * Find rows of the join table  after filtering out form identified by excludedFormId
     * @param userOptionsPojo - the details of the appliesTo
     * @param excludedFormId - the form to exclude
     * @return - matched rows - List of CustomVariableFormAppliesTo
     */

    public List<CustomVariableFormAppliesTo> findAllFormsByExclusion(final UserOptionsPojo userOptionsPojo,  final long excludedFormId) {
        Criteria criteria = getCriteria(userOptionsPojo, null, true);
        criteria.add(Restrictions.ne("cvf.id", excludedFormId));
        List<CustomVariableFormAppliesTo> results = criteria.list();
        initializeList(results);
        return results == null ? Collections.emptyList() : results;
    }

    /**
     * Find all project specific forms for a list of projects and form identified by formid
     * @param userOptionsPojo - the details of the appliesTo object
     * @param entityIds - the list of project ids
     * @param formId - the form id
     * @return - matched rows - List of CustomVariableFormAppliesTo
     */

    public List<CustomVariableFormAppliesTo> findAllSpecificProjectForm(final UserOptionsPojo userOptionsPojo, final List<String> entityIds, final long formId) {
        Criteria criteria = getCriteria(userOptionsPojo, Scope.Project, true);
        if (entityIds != null) {
            criteria.add(Restrictions.in("cvat.entityId", entityIds));
        }
        criteria.add(Restrictions.eq("cvf.id", formId));
        List<CustomVariableFormAppliesTo> results = criteria.list();
        initializeList(results);
        return results == null ? Collections.emptyList() : results;
    }

    public List<CustomVariableForm> findAllDistinctFormsByDatatype(final String dataType, final String status) {
        Criteria criteria = getCriteriaWithAlias();
        if (status != null) {
            criteria.add(Restrictions.eq("cfa.status",status));
        }
        criteria.add(Restrictions.eq("cvat.dataType", dataType));
        List<CustomVariableFormAppliesTo> results = criteria.list();
        initializeList(results);
        List<CustomVariableForm> forms = new ArrayList<CustomVariableForm>();
        if (null != results) {
            for (CustomVariableFormAppliesTo ca: results) {
                forms.add(ca.getCustomVariableForm());
            }
        }
       forms.stream()
                .filter(DistinctByKey(f -> f.getFormUuid()))
                .collect(Collectors.toList());
        return forms;
    }

    public static <T> Predicate<T> DistinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private Criteria getCriteria(final UserOptionsPojo userOptionsPojo, final Scope scope, boolean restrictOnlyDataType) {
        Criteria criteria = getCriteriaWithAlias();
        criteria.add(Restrictions.ne("cfa.status", CustomFormsConstants.OPTED_OUT_STATUS_STRING));
        if (scope != null) {
            criteria.add(Restrictions.eq("cvat.scope", scope));
        }
        criteria.add(Restrictions.eq("cvat.dataType", userOptionsPojo.getDataType()));
        if (!restrictOnlyDataType && userOptionsPojo.getProtocol() != null) {
            criteria.add(Restrictions.eq("cvat.protocol", userOptionsPojo.getProtocol()));
        }
        if (!restrictOnlyDataType && userOptionsPojo.getVisit() != null) {
            criteria.add(Restrictions.eq("cvat.visit", userOptionsPojo.getVisit()));
        }
        if (!restrictOnlyDataType && userOptionsPojo.getSubType() != null) {
            criteria.add(Restrictions.eq("cvat.subType", userOptionsPojo.getSubType()));
        }
        if (!restrictOnlyDataType && userOptionsPojo.getScanType() != null) {
            criteria.add(Restrictions.eq("cvat.scanType", userOptionsPojo.getScanType()));
        }
        return criteria;
    }

    public List<CustomVariableFormAppliesTo> findByFormId(final long formId) {
        Criteria criteria = getCriteriaWithAlias();
        criteria.add(Restrictions.eq("cvf.id", formId));
        List<CustomVariableFormAppliesTo> results = super.emptyToNull(GenericUtils.convertToTypedList(criteria.list(), getParameterizedType()));
        initializeList(results);
        return results == null ? Collections.emptyList() : results;
    }

    /**
     * Find rows of the join table matched by FormId and Status
     * @param formId - the formId
     * @param status - the status
     * @return - matched rows - List of CustomVariableFormAppliesTo
     */

    public List<CustomVariableFormAppliesTo> findByFormIdAndStatus(final long formId, final String status) {
        Criteria criteria = getCriteriaWithAlias();
        criteria.add(Restrictions.eq("cfa.status", status));
        criteria.add(Restrictions.eq("cvf.id", formId));
        List<CustomVariableFormAppliesTo> results = super.emptyToNull(GenericUtils.convertToTypedList(criteria.list(), getParameterizedType()));
        initializeList(results);
        return results == null ? Collections.emptyList(): results;
    }

    /**
     * Find rows of the join table with a given form id except the row identified by RowIdentifier
     * @param formId - the formId
     * @param rowIdentifier - the Primary Key of the row to exclude
     * @return - matched rows - List of CustomVariableFormAppliesTo
     */

    public List<CustomVariableFormAppliesTo> findByFormIdByExclusion(final long formId, final RowIdentifier rowIdentifier) {
        List<CustomVariableFormAppliesTo> all = findByFormId(formId);
        List<CustomVariableFormAppliesTo> results = new ArrayList<CustomVariableFormAppliesTo>();
        for (CustomVariableFormAppliesTo c : all) {
            if (c.getCustomVariableForm().getId() != rowIdentifier.getFormId() && c.getCustomVariableAppliesTo().getId() != rowIdentifier.getAppliesToId()) {
                results.add(c);
            }
        }
        initializeList(results);
        return results == null ? Collections.emptyList() : results;
    }

    /**
     * Find rows of the join table which are applicable to a given appliesToId
     * @param appliesToId - the appliesTo id
     * @return - matched rows - List of CustomVariableFormAppliesTo
     */

    public List<CustomVariableFormAppliesTo> findByAppliesToId(final long appliesToId) {
        final Criteria criteria = getCriteriaWithAlias();
        criteria.add(Restrictions.eq("cvat.id", appliesToId));
        List<CustomVariableFormAppliesTo> results = super.emptyToNull(GenericUtils.convertToTypedList(criteria.list(), getParameterizedType()));
        initializeList(results);
        return results == null ? Collections.emptyList() : results;
    }

    /**
     * Convenience method to initialize a list of CustomVariableFormAppliesTo
     * @param results  - initiates value from proxy
     */

    private void initializeList(List<CustomVariableFormAppliesTo> results) {
        if (results != null) {
            for (CustomVariableFormAppliesTo a : results) {
                initializeChild(a);
            }
        }
    }

    private Criteria getCriteriaWithAlias() {
        Criteria criteria = getSession().createCriteria(CustomVariableFormAppliesTo.class, "cfa");
        criteria.setFetchMode("cfa.customVariableAppliesTo", FetchMode.JOIN);
        criteria.setFetchMode("cfa.customVariableForm", FetchMode.JOIN);
        criteria.createAlias("cfa.customVariableAppliesTo", "cvat");
        criteria.createAlias("cfa.customVariableForm", "cvf");
        return criteria;
    }

    /**
     * Unproxy and initialize child objects
     * @param obj - the parent whose children are to be initialized
     */

    //This ugly unproxy step seems to be required as Hibernate is not serializing object back
    private void initializeChild(CustomVariableFormAppliesTo obj) {
        if (obj == null) {
            return;
        }
        CustomVariableForm form = (CustomVariableForm)CustomFormHibernateUtils.initializeAndUnproxy(obj.getCustomVariableForm());
        CustomVariableAppliesTo appliesTo = (CustomVariableAppliesTo)CustomFormHibernateUtils.initializeAndUnproxy(obj.getCustomVariableAppliesTo());
        obj.setCustomVariableForm(form);
        obj.setCustomVariableAppliesTo(appliesTo);
    }

    /**
     * Evict object from the Session
     * @param obj - the object to evict
     */

    public void evict(CustomVariableFormAppliesTo obj) {
        getSession().evict(obj);
    }

}
