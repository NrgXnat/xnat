/**
 * ScriptTrigger
 * (C) 2014 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 9/19/2014 by Rick Herrick
 */
package org.nrg.automation.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jetbrains.annotations.NotNull;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.python.google.common.collect.Lists;
import org.python.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;

/**
 * ScriptTrigger class.
 *
 * @author Rick Herrick
 */
@Entity
@Table
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class ScriptTrigger extends AbstractHibernateEntity implements Comparable<ScriptTrigger> {

	/** The Constant DEFAULT_CLASS. */
	public static final String DEFAULT_CLASS = "org.nrg.xnat.event.entities.WorkflowStatusEvent";
	
	/** The Constant DEFAULT_EVENT. */
	public static final String DEFAULT_EVENT = "Manual";
	
	/** The Constant DEFAULT_FILTERS. */
	public static final Map<String,List<String>> DEFAULT_FILTERS;
	
	/** The Constant DEFAULT_FILTER. */
	public static final Map<String,String> DEFAULT_FILTER;
	
	/** The Constant STATUS_COMPLETE. */
	public static final String STATUS_COMPLETE = "Complete";
	
	static {
	 	DEFAULT_FILTERS = Maps.newHashMap();
	 	DEFAULT_FILTER = Maps.newHashMap();
		final List<String> filterValues = Lists.newArrayList(); 
		filterValues.add(STATUS_COMPLETE);
		DEFAULT_FILTERS.put("status", filterValues);
		DEFAULT_FILTER.put("status", STATUS_COMPLETE);
	}

    /**
     * Instantiates a new script trigger.
     */
    public ScriptTrigger() {
        _log.debug("Creating a default ScriptTrigger object.");
    }

    /**
     * Instantiates a new script trigger.
     *
     * @param triggerId the trigger id
     * @param description the description
     * @param scriptId the script id
     * @param association the association
     * @param srcEventClass the src event class
     * @param event the event
     */
    public ScriptTrigger(final String triggerId, final String description, final String scriptId, final String association, final String srcEventClass, final String event) {
    	this(triggerId, description, scriptId, association, srcEventClass, event, new HashSet<EventFilters>());
        if (_log.isDebugEnabled()) {
            _log.debug("Creating a ScriptTrigger object with the values: {}", toString());
        }
    }

    /**
     * Instantiates a new script trigger.
     *
     * @param triggerId the trigger id
     * @param description the description
     * @param scriptId the script id
     * @param association the association
     * @param srcEventClass the src event class
     * @param event the event
     * @param eventFilters the event filters
     */
    public ScriptTrigger(final String triggerId, final String description, final String scriptId, final String association, final String srcEventClass, final String event, final Set<EventFilters> eventFilters) {
        setTriggerId(triggerId);
        setDescription(description);
        setScriptId(scriptId);
        setAssociation(association); // datatype:xnat:mrSessionData
        setSrcEventClass(srcEventClass); 
        setEvent(event);             // archived
        if (eventFilters != null) {
        	setEventFilters(eventFilters);
        } else {
        	final Set<EventFilters> emptySet = Sets.newHashSet();
        	setEventFilters(emptySet);
        }
        if (_log.isDebugEnabled()) {
            _log.debug("Creating a ScriptTrigger object with the values: {}", toString());
        }
    }
    
    /**
     * Instantiates a new script trigger.
     *
     * @param triggerId the trigger id
     * @param description the description
     * @param scriptId the script id
     * @param association the association
     * @param srcEventClass the src event class
     * @param event the event
     * @param filterMap the filter map
     */
    public ScriptTrigger(final String triggerId, final String description, final String scriptId, final String association, final String srcEventClass, final String event, final Map<String,List<String>> filterMap) {
    	this(triggerId, description, scriptId, association, srcEventClass, event, mapToEventFilters(filterMap));
    }
    
    /**
     * Gets the trigger id.
     *
     * @return the trigger id
     */
    @Column(nullable = false, unique = true)
    public String getTriggerId() {
        return _triggerId;
    }

    /**
     * Sets the trigger id.
     *
     * @param triggerId the new trigger id
     */
    public void setTriggerId(final String triggerId) {
        _triggerId = triggerId;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(final String description) {
        _description = description;
    }

    /**
     * Gets the script id.
     *
     * @return the script id
     */
    @Column(nullable = false)
    public String getScriptId() {
        return _scriptId;
    }

    /**
     * Sets the script id.
     *
     * @param scriptId the new script id
     */
    public void setScriptId(final String scriptId) {
        _scriptId = scriptId;
    }

    /**
     * For the current iteration of this API, associations may be XNAT data types (in the form of the xsiType string),
     * a project ID (in the form "prj:ID"), or the containing site (in the form "site").
     *
     * @return The association for this trigger.
     */
    @Column(nullable = false)
    public String getAssociation() {
        return _association;
    }

    /**
     * Sets the association.
     *
     * @param association the new association
     */
    public void setAssociation(final String association) {
        _association = association;
    }

    /**
     * Gets the event.
     *
     * @return the event
     */
    public String getEvent() {
        return _event;
    }

    /**
     * Sets the event.
     *
     * @param event the new event
     */
    public void setEvent(final String event) {
        _event = event;
    }

	/**
	 * Gets the src event class.
	 *
	 * @return the src event class
	 */
	public String getSrcEventClass() {
		return _srcEventClass;
	}

	/**
	 * Sets the src event class.
	 *
	 * @param srcEventClass the new src event class
	 */
	public void setSrcEventClass(final String srcEventClass) {
		_srcEventClass = srcEventClass;
	}

	/**
	 * Gets the event filters.
	 *
	 * @return the event filters
	 */
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "id")
	public Set<EventFilters> getEventFilters() {
		return _eventFilters;
	}

	/**
	 * Sets the event filters.
	 *
	 * @param eventFilters the new event filters
	 */
	public void setEventFilters(Set<EventFilters> eventFilters) {
		_eventFilters = eventFilters;
	}

	/**
	 * Sets the event filters as map.
	 *
	 * @param filterMap the filter map
	 */
	public void setEventFiltersAsMap(Map<String,List<String>> filterMap) {
		_eventFilters = mapToEventFilters(filterMap);
	}

	/**
	 * Gets the event filters as map.
	 *
	 * @return the event filters as map
	 */
	@Transient
	public Map<String,List<String>> getEventFiltersAsMap() {
		final Map<String,List<String>> eventMap = Maps.newHashMap();
		for (final EventFilters filters : _eventFilters) {
			eventMap.put(filters.getFilterVar(), filters.getFilterVals());
		}
		return eventMap;
	}

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "ScriptTrigger{" +
                "name='" + _triggerId + '\'' +
                ", description='" + _description + '\'' +
                ", scriptId='" + _scriptId + '\'' +
                ", association='" + _association + '\'' +
                ", srcEventClass='" + _srcEventClass + '\'' +
                ", event='" + _event + '\'' +
                ", eventTriggers='" + _eventFilters.hashCode() + '\'' +
                '}';
    }

    /**
     * Equals.
     *
     * @param object the object
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ScriptTrigger)) {
            return false;
        }

        final ScriptTrigger trigger = (ScriptTrigger) object;

        return _association.equals(trigger._association) &&
                !(_description != null ? !_description.equals(trigger._description) : trigger._description != null) &&
                _event.equals(trigger._event) &&
                _triggerId.equals(trigger._triggerId) &&
                _scriptId.equals(trigger._scriptId) &&
                _srcEventClass.equals(trigger._srcEventClass) &&
                _eventFilters.equals(trigger._eventFilters);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int result = _triggerId.hashCode();
        result = 31 * result + (_description != null ? _description.hashCode() : 0);
        result = 31 * result + _scriptId.hashCode();
        result = 31 * result + _association.hashCode();
        result = 31 * result + _event.hashCode();
        result = 31 * result + _srcEventClass.hashCode();
        result = 31 * result + _eventFilters.hashCode();
        return result;
    }

    /**
     * Compare to.
     *
     * @param other the other
     * @return the int
     */
    @Override
    public int compareTo(@NotNull final ScriptTrigger other) {
        return toString().compareTo(other.toString());
    }
	
	/**
	 * Map to event filters.
	 *
	 * @param filterMap the filter map
	 * @return the sets the
	 */
	private static Set<EventFilters> mapToEventFilters(Map<String, List<String>> filterMap) {
		final Set<EventFilters> eventFilters = Sets.newHashSet();
    	for (final String filterKey : filterMap.keySet()) {
    		EventFilters ef = new EventFilters(filterKey,filterMap.get(filterKey));
    		eventFilters.add(ef);
    	}
		return eventFilters;
	}

    /** The Constant _log. */
    private static final Logger _log = LoggerFactory.getLogger(ScriptTrigger.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6922583117863143778L;
    
    /** The _trigger id. */
    private String _triggerId;
    
    /** The _description. */
    private String _description;
    
    /** The _script id. */
    private String _scriptId;
    
    /** The _association. */
    private String _association;
    
    /** The _event. */
    private String _event;
    
    /** The _src event class. */
    private String _srcEventClass;
    
    /** The _event filters. */
    private Set<EventFilters> _eventFilters;
    
}
