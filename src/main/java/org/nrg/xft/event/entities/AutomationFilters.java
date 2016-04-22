package org.nrg.xft.event.entities;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xft.event.AutomationEventImplementerI;
import org.nrg.xft.event.Filterable;

import com.google.common.collect.Lists;

/**
 * The Class AutomationFilters.
 */
@Entity
@SuppressWarnings("serial")
@Table(uniqueConstraints=@UniqueConstraint(columnNames = { "externalId", "srcEventClass", "field"}))
public class AutomationFilters extends AbstractHibernateEntity implements Serializable {
    
	/** The external id. */
	private String externalId;
	
	/** The src event class. */
	private String srcEventClass;
	
	/** The field. */
	private String field;
	
	/** The values. */
	private List<String> values;
	//private Set<AutomationFiltersFields> automationFiltersFields = Sets.newHashSet();
	
	/**
	 * Instantiates a new automation filters.
	 */
	public AutomationFilters() {
		super();
	}
    
	/**
	 * Instantiates a new automation filters.
	 *
	 * @param eventData the event data
	 * @param field the field
	 */
	public AutomationFilters(AutomationEventImplementerI eventData, String field) {
		this();
		this.externalId = eventData.getExternalId();
		this.srcEventClass = eventData.getSrcEventClass();
		final Class<? extends AutomationEventImplementerI> clazz = eventData.getClass();
		for (final Method method : Arrays.asList(clazz.getMethods())) {
			if (method.isAnnotationPresent(Filterable.class) && method.getName().substring(0,3).equalsIgnoreCase("get")) {
				final char c[] = method.getName().substring(3).toCharArray();
				c[0] = Character.toLowerCase(c[0]);
				final String column = new String(c);
				if (!column.equalsIgnoreCase(field)) {
					continue;
				}
				try {
					final String value = method.invoke(eventData).toString();
					if (value != null && value.length()>0) {
						final List<String> values = Lists.newArrayList();
						values.add(value);
						this.setField(column);
						this.setValues(values);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					// Do nothing for now
				}
			}
		}
	}

	/**
	 * Sets the external id.
	 *
	 * @param externalId the new external id
	 */
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	/**
	 * Gets the external id.
	 *
	 * @return the external id
	 */
	public String getExternalId() {
		return this.externalId;
	}

	/**
	 * Sets the src event class.
	 *
	 * @param srcEventClass the new src event class
	 */
	public void setSrcEventClass(String srcEventClass) {
		this.srcEventClass = srcEventClass;
	}

	/**
	 * Gets the src event class.
	 *
	 * @return the src event class
	 */
	public String getSrcEventClass() {
		return this.srcEventClass;
	}
	
	/**
	 * Gets the field.
	 *
	 * @return the field
	 */
	public String getField() {
		return field;
	}

	/**
	 * Sets the field.
	 *
	 * @param field the new field
	 */
	public void setField(String field) {
		this.field = field;
	}

    /**
     * Gets the values.
     *
     * @return the values
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable
	public List<String> getValues() {
		return values;
	}

	/**
	 * Sets the values.
	 *
	 * @param values the new values
	 */
	public void setValues(List<String> values) {
		this.values = values;
	}

}
