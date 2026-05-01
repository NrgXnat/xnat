/*
 * spawner: org.nrg.xnat.spawner.repositories.SpawnerElementRepository
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.repositories;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.spawner.entities.SpawnerElement;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SpawnerElementRepository extends AbstractHibernateDAO<SpawnerElement> {
    public List<String> getNamespaces() {
        return getSession().createQuery(QUERY_DISTINCT_NAMESPACES, String.class).getResultList();
    }

    public List<Object[]>  getNamespacesAndResrictions() {
        return getSession().createQuery(QUERY_DISTINCT_NAMESPACES_AND_RESTRICTIONS).getResultList();
    }


    public List<String> getNamespaceElementIds(final String namespace) {
        return getSession().createQuery(QUERY_DISTINCT_ELEMENT_IDS_FOR_NAMESPACE, String.class).setParameter(PARAM_NAMESPACE, namespace).getResultList();
    }

    public List<SpawnerElement> getNamespaceElements(final String namespace) {
        return getSession().createQuery(QUERY_ELEMENT_IDS_FOR_NAMESPACE, SpawnerElement.class).setParameter(PARAM_NAMESPACE, namespace).getResultList();
    }

    public SpawnerElement getElement(final String namespace, final String elementId) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put(PARAM_NAMESPACE, namespace);
        properties.put(PARAM_ELEMENT_ID, elementId);
        return findByUniqueProperties(properties);
    }

    private static final String QUERY_DISTINCT_NAMESPACES                = "select distinct namespace from SpawnerElement order by namespace";
    private static final String QUERY_DISTINCT_NAMESPACES_AND_RESTRICTIONS = "select distinct namespace, restricted from SpawnerElement order by namespace";
    private static final String QUERY_DISTINCT_ELEMENT_IDS_FOR_NAMESPACE = "select distinct elementId from SpawnerElement where namespace = :namespace order by elementId";
    private static final String QUERY_ELEMENT_IDS_FOR_NAMESPACE          = "from SpawnerElement where namespace = :namespace order by elementId";
    private static final String PARAM_NAMESPACE                          = "namespace";
    private static final String PARAM_ELEMENT_ID                         = "elementId";
}
