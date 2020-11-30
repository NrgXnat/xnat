package org.nrg.xnat.eventservice.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.eventservice.entities.SubscriptionDeliveryEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class XnatObjectIntrospectionDao extends AbstractHibernateDAO<SubscriptionDeliveryEntity> {

    public List<XnatProjectdata> getProjects(String projectId) throws Throwable{
        return getSession()
                .createQuery("SELECT pro FROM xnat_projectdata WHERE id = :projectId")
                .setString("projectId", projectId)
                .list();
    }

    public Boolean isExperimentModified(String experimentId){
        List<Integer> modified =  getSession()
                .createQuery("SELECT A.modified FROM xnat_experimentData_meta_data A WHERE A.meta_data_id in (SELECT B.experimentdata_info FROM xnat_experimentData B WHERE B.id = :experimentId)")
                .setString("experimentId", experimentId)
                .list();
        return (modified == null || modified.isEmpty()) ? false : modified.get(0) == 1;
    }

}
