/*
 * org.nrg.automation.entities.EventFilters
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.util.List;

/**
 * The Class EventFilters.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class EventFilters implements Serializable {

	/**
	 * Instantiates a new event filters.
	 */
	public EventFilters() {
    }
    
    /**
     * Instantiates a new event filters.
     *
     * @param filterVar the filter var
     * @param filterVals the filter vals
     */
    public EventFilters(final String filterVar, final List<String> filterVals) {
        _filterVar = filterVar;
        _filterVals= filterVals;
    }
    
    /**
     * Gets the id.
     *
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return _id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(Long id) {
        _id = id;
    }

    /**
     * Gets the filter var.
     *
     * @return the filter var
     */
    public String getFilterVar() {
        return _filterVar;
    }

    /**
     * Sets the filter var.
     *
     * @param filterVar the new filter var
     */
    public void setFilterVar(final String filterVar) {
        _filterVar = filterVar;
    }

    /**
     * Gets the filter vals.
     *
     * @return the filter vals
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable
    public List<String> getFilterVals() {
        return _filterVals;
    }

    /**
     * Sets the filter vals.
     *
     * @param filterVals the new filter vals
     */
    public void setFilterVals(final List<String> filterVals) {
        _filterVals = filterVals;
    }

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9095111702641022354L;
    
    /** The _filter var. */
    private String _filterVar;
    
    /** The _filter vals. */
    private List<String> _filterVals;
    
    /** The _id. */
    private Long  _id;

}
