package org.nrg.automation.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.nrg.automation.event.entities.AutomationEventIds;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

/**
 * The Class AutomationEventIdsDAO.
 */
@Repository
public class AutomationEventIdsDAO extends AbstractHibernateDAO<AutomationEventIds> {
	
	/**
	 * Gets the event ids.
	 *
	 * @param projectId the project id
	 * @param srcEventClass the src event class
	 * @param exactMatchExternalId the exact match external id
	 * @return the event ids
	 */
	@SuppressWarnings("unchecked")
	public List<AutomationEventIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId) {
		final Criteria c = getCriteriaForType();
		if (projectId != null) {
			final Criterion extId1 = Restrictions.eq("externalId",projectId);
					final Criterion extId2 = Restrictions.like("externalId",projectId + "_", MatchMode.START);
					if (exactMatchExternalId) {
						c.add(extId1);
					} else {
						c.add(Restrictions.or(extId1,extId2));
					}
		}
		c.add(Restrictions.eq("srcEventClass",srcEventClass));
		return c.list();
	}
	
	/**
	 * Gets the event ids.
	 *
	 * @param projectId the project id
	 * @param exactMatchExternalId the exact match external id
	 * @return the event ids
	 */
	@SuppressWarnings("unchecked")
	public List<AutomationEventIds> getEventIds(String projectId, boolean exactMatchExternalId) {
		final Criteria c = getCriteriaForType();
		if (projectId != null) {
			final Criterion extId1 = Restrictions.eq("externalId",projectId);
			final Criterion extId2 = Restrictions.like("externalId",projectId + "_", MatchMode.START);
			if (exactMatchExternalId) {
				c.add(extId1);
			} else {
				c.add(Restrictions.or(extId1,extId2));
			}
		}
		return c.list();
	}

	/**
	 * Save or update.
	 *
	 * @param eventIds the event ids
	 */
	public void saveOrUpdate(AutomationEventIds eventIds) {
		try {
			final Criteria c = getCriteriaForType();
			c.add(Restrictions.eq("id",eventIds.getId()));
			if (c.list().size()>0) {
				this.update(eventIds);
			} else {
				this.create(eventIds);
			}
		} catch (NonUniqueObjectException e) {
			// TODO:  There's got to be a good way to check for the need to do a merge or to prevent this exception
			// for object being updated.
			this.getSession().merge(eventIds);
		}
	}
	
}
