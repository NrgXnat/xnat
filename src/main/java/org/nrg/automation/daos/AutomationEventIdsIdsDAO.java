package org.nrg.automation.daos;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.nrg.automation.event.entities.AutomationEventIdsIds;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

/**
 * The Class AutomationEventIdsDAO.
 */
@Repository
public class AutomationEventIdsIdsDAO extends AbstractHibernateDAO<AutomationEventIdsIds> {

	/**
	 * Gets the event ids.
	 *
	 * @param projectId the project id
	 * @param srcEventClass the src event class
	 * @param eventId the event id
	 * @param exactMatchExternalId the exact match external id
	 * @return the event ids
	 */
	@SuppressWarnings("unchecked")
	public List<AutomationEventIdsIds> getEventIds(String projectId, String srcEventClass, String eventId, boolean exactMatchExternalId) {
		final Criteria c = getCriteriaForType();
		c.createAlias("parentAutomationEventIds", "parentAutomationEventIds");
		if (projectId != null) {
			final Criterion extId1 = Restrictions.eq("parentAutomationEventIds.externalId",projectId);
					final Criterion extId2 = Restrictions.like("parentAutomationEventIds.externalId",projectId + "_", MatchMode.START);
					if (exactMatchExternalId) {
						c.add(extId1);
					} else {
						c.add(Restrictions.or(extId1,extId2));
					}
		}
		c.add(Restrictions.eq("parentAutomationEventIds.srcEventClass",srcEventClass));
		c.add(Restrictions.eq("eventId",eventId));
		return c.list();
	}

	/**
	 * Gets the event ids.
	 *
	 * @param projectId the project id
	 * @param srcEventClass the src event class
	 * @param exactMatchExternalId the exact match external id
	 * @return the event ids
	 */
	@SuppressWarnings("unchecked")
	public List<AutomationEventIdsIds> getEventIds(String projectId, String srcEventClass, boolean exactMatchExternalId) {
		final Criteria c = getCriteriaForType();
		c.createAlias("parentAutomationEventIds", "parentAutomationEventIds");
		if (projectId != null) {
			final Criterion extId1 = Restrictions.eq("parentAutomationEventIds.externalId",projectId);
					final Criterion extId2 = Restrictions.like("parentAutomationEventIds.externalId",projectId + "_", MatchMode.START);
					if (exactMatchExternalId) {
						c.add(extId1);
					} else {
						c.add(Restrictions.or(extId1,extId2));
					}
		}
		c.add(Restrictions.eq("parentAutomationEventIds.srcEventClass",srcEventClass));
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
	public List<AutomationEventIdsIds> getEventIds(String projectId, boolean exactMatchExternalId) {
		final Criteria c = getCriteriaForType();
		c.createAlias("parentAutomationEventIds", "parentAutomationEventIds");
		if (projectId != null) {
			final Criterion extId1 = Restrictions.eq("parentAutomationEventIds.externalId",projectId);
			final Criterion extId2 = Restrictions.like("parentAutomationEventIds.externalId",projectId + "_", MatchMode.START);
			if (exactMatchExternalId) {
				c.add(extId1);
			} else {
				c.add(Restrictions.or(extId1,extId2));
			}
		}
		return c.list();
	}
	
	/**
	 * Gets the event ids.
	 *
	 * @param projectId the project id
	 * @param exactMatchExternalId the exact match external id
	 * @param max_per_type the max_per_type
	 * @return the event ids
	 */
	public List<AutomationEventIdsIds> getEventIds(String projectId, boolean exactMatchExternalId, int max_per_type) {
		final List<AutomationEventIdsIds> idsList = getEventIds(projectId, exactMatchExternalId); 
		return trimList(idsList, max_per_type);
	}
	
	/**
	 * Trim list.
	 *
	 * @param idsList the ids list
	 * @param max_per_type the max_per_type
	 * @return the list
	 */
	private List<AutomationEventIdsIds> trimList(List<AutomationEventIdsIds> idsList, int max_per_type) {
		Collections.sort(idsList);
		final Iterator<AutomationEventIdsIds> i = idsList.iterator();
		AutomationEventIdsIds prevIds = null;
		int counter = 1;
		while (i.hasNext()) {
			final AutomationEventIdsIds ids = i.next();
			counter = (prevIds==null || !prevIds.getParentAutomationEventIds().equals(ids.getParentAutomationEventIds())) ? 1 : counter+1;
			if (counter > max_per_type) {
				i.remove();
			}
			prevIds = ids;
		}
		return idsList;
	}

	/**
	 * Save or update.
	 *
	 * @param eventIds the event ids
	 */
	public void saveOrUpdate(AutomationEventIdsIds eventIds) {
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
			// Update:  I believe this issue has mostly been resolved by making the method that primarily reads from and
			// updates this object type a synchronized method
			this.getSession().merge(eventIds);
		}
	}
	
}
