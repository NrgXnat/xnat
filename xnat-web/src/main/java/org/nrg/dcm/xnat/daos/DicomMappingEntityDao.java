package org.nrg.dcm.xnat.daos;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.criterion.Subqueries;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.dcm.xnat.entities.DicomMappingEntity;
import org.nrg.framework.orm.hibernate.QueryBuilder;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DicomMappingEntityDao extends AbstractHibernateDAO<DicomMappingEntity> {
    /**
     * Find entities with project scope and matching projectid OR in site scope (and not overridden by project)
     *
     * @param project  the project or null to only search site scope
     * @param property the property
     * @param value    the value
     *
     * @return list of matching entities or null if none configured
     */
    @Nullable
    public List<DicomMappingEntity> findInScopeByProperty(@Nullable String project, String property, String value) {
        Criteria criteria = this.getCriteriaForType();
        SimpleExpression propertyCriteria = Restrictions.eq(property, value);
        SimpleExpression siteScopeCriteria = Restrictions.eq("scope", Scope.Site);
        criteria.add(propertyCriteria); // Add property criteria

        if (StringUtils.isBlank(project)) {
            criteria.add(siteScopeCriteria);
        } else {
            String dicomTagProperty = "dicomTag";

            // project scope and id matches
            LogicalExpression projectScopeCriteria = Restrictions.and(Restrictions.eq("scope", Scope.Project),
                    Restrictions.eq("scopeObjectId", project));

            // only retrieve site scope matches if they're for different DICOM tags, so first get dicom tags covered
            // by project scope mappings
            DetachedCriteria subquery = DetachedCriteria.forClass(DicomMappingEntity.class);
            subquery.add(propertyCriteria)
                    .add(projectScopeCriteria)
                    .setProjection(Projections.property(dicomTagProperty));

            // and then exclude them from the site scope results
            LogicalExpression restrictedSiteScope = Restrictions.and(siteScopeCriteria,
                                                                     Subqueries.propertiesNotIn(new String[]{dicomTagProperty}, subquery));

            criteria.add(Restrictions.or(projectScopeCriteria, restrictedSiteScope));
        }

        List<DicomMappingEntity> list = criteria.list();
        return list != null && !list.isEmpty() ? list : null;
    }
}
