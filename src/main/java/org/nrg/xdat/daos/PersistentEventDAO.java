package org.nrg.xdat.daos;

import java.util.List;

import org.hibernate.Query;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xft.event.AutomationEventImplementerI;
import org.nrg.xft.event.entities.PersistentEvent;
import org.springframework.stereotype.Repository;

/**
 * The Class PersistentEventDAO.
 */
@Repository
public class PersistentEventDAO extends AbstractHibernateDAO<PersistentEvent> {
	
	/**
	 * Gets the distinct values.
	 *
	 * @param clazz the clazz
	 * @param column the column
	 * @param projectId the project id
	 * @return the distinct values
	 */
	public List<Object[]> getDistinctValues(Class<AutomationEventImplementerI> clazz, String column, String projectId) {
		Query query = getSession().createQuery(
				"select distinct c." + column + " from " + clazz.getName() + " c where c.externalId='" + projectId + "'"
				);
		List<Object[]> rows = query.list();
		return rows;
	}
	
}
