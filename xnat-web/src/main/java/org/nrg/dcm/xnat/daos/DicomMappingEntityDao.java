package org.nrg.dcm.xnat.daos;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.dcm.xnat.entities.DicomMappingEntity;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
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
        final CriteriaBuilder                    builder = getCriteriaBuilder();
        final CriteriaQuery<DicomMappingEntity>  query   = builder.createQuery(DicomMappingEntity.class);
        final Root<DicomMappingEntity>           root    = query.from(DicomMappingEntity.class);

        final Predicate propertyPredicate  = builder.equal(root.get(property), value);
        final Predicate siteScopePredicate = builder.equal(root.get("scope"), Scope.Site);

        if (StringUtils.isBlank(project)) {
            query.where(propertyPredicate, siteScopePredicate);
        } else {
            final String dicomTagProperty = "dicomTag";

            // project scope and id matches
            final Predicate projectScopePredicate = builder.and(builder.equal(root.get("scope"), Scope.Project),
                                                                builder.equal(root.get("scopeObjectId"), project));

            // only retrieve site scope matches if they're for different DICOM tags, so first get dicom tags covered
            // by project scope mappings
            final Subquery<String>         subquery     = query.subquery(String.class);
            final Root<DicomMappingEntity> subqueryRoot = subquery.from(DicomMappingEntity.class);
            subquery.select(subqueryRoot.get(dicomTagProperty))
                    .where(builder.equal(subqueryRoot.get(property), value),
                           builder.and(builder.equal(subqueryRoot.get("scope"), Scope.Project),
                                       builder.equal(subqueryRoot.get("scopeObjectId"), project)));

            // and then exclude them from the site scope results
            final Predicate restrictedSiteScope = builder.and(siteScopePredicate,
                                                              builder.not(root.get(dicomTagProperty).in(subquery)));

            query.where(propertyPredicate, builder.or(projectScopePredicate, restrictedSiteScope));
        }

        final List<DicomMappingEntity> list = createQuery(query).getResultList();
        return list != null && !list.isEmpty() ? list : null;
    }
}
