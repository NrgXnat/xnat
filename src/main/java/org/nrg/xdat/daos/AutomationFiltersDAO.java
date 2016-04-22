package org.nrg.xdat.daos;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xft.event.entities.AutomationFilters;
import org.springframework.stereotype.Repository;

/**
 * The Class AutomationFiltersDAO.
 */
@Repository
public class AutomationFiltersDAO extends AbstractHibernateDAO<AutomationFilters> {
	
	/**
	 * Gets the automation filters.
	 *
	 * @param externalId the external id
	 * @param srcEventClass the src event class
	 * @param column the column
	 * @param exactMatchExternalId the exact match external id
	 * @return the automation filters
	 */
	@SuppressWarnings("rawtypes")
	public AutomationFilters getAutomationFilters(String externalId, String srcEventClass, String column, boolean exactMatchExternalId) {
		final Criteria c = getCriteriaForType();
		if (externalId != null) {
			final Criterion extId1 = Restrictions.eq("externalId",externalId);
			final Criterion extId2 = Restrictions.like("externalId",externalId + "_", MatchMode.START);
			if (exactMatchExternalId) {
				c.add(extId1);
			} else {
				c.add(Restrictions.or(extId1,extId2));
			}
		}
		c.add(Restrictions.eq("srcEventClass",srcEventClass));
		c.add(Restrictions.eq("field",column));
		List oList = c.list();
		if (oList.size()>0 && oList.get(0) instanceof AutomationFilters) {
			return (AutomationFilters)oList.get(0);
		}
		return null;
	}
	
	/**
	 * Gets the automation filters.
	 *
	 * @param externalId the external id
	 * @param srcEventClass the src event class
	 * @param exactMatchExternalId the exact match external id
	 * @return the automation filters
	 */
	@SuppressWarnings("unchecked")
	public List<AutomationFilters> getAutomationFilters(String externalId, String srcEventClass, boolean exactMatchExternalId) {
		final Criteria c = getCriteriaForType();
		if (externalId != null) {
			final Criterion extId1 = Restrictions.eq("externalId",externalId);
			final Criterion extId2 = Restrictions.like("externalId",externalId + "_", MatchMode.START);
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
	 * Gets the automation filters.
	 *
	 * @param externalId the external id
	 * @param exactMatchExternalId the exact match external id
	 * @return the automation filters
	 */
	@SuppressWarnings("unchecked")
	public List<AutomationFilters> getAutomationFilters(String externalId, boolean exactMatchExternalId) {
		final Criteria c = getCriteriaForType();
		if (externalId != null) {
			final Criterion extId1 = Restrictions.eq("externalId",externalId);
			final Criterion extId2 = Restrictions.like("externalId",externalId + "_", MatchMode.START);
			if (exactMatchExternalId) {
				c.add(extId1);
			} else {
				c.add(Restrictions.or(extId1,extId2));
			}
			c.add(Restrictions.or(extId1,extId2));
		}
		return c.list();
	}

	/**
	 * Save or update.
	 *
	 * @param filters the filters
	 */
	public void saveOrUpdate(AutomationFilters filters) {
		try {
			final Criteria c = getCriteriaForType();
			c.add(Restrictions.eq("id",filters.getId()));
			if (c.list().size()>0) {
				this.update(filters);
			} else {
				this.create(filters);
			}
		} catch (NonUniqueObjectException e) {
			// TODO:  There's got to be a good way to check for the need to do a merge or to prevent this exception
			// for object being updated.
			this.getSession().merge(filters);
		}
	}

}
