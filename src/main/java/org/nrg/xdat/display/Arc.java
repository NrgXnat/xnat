/*
 * org.nrg.xdat.display.Arc
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.display;

import java.util.Hashtable;
/**
 * @author Tim
 *
 */
public class Arc {
	private String name = null;
	private Hashtable commonFields = new Hashtable();
		
	/**
	 * @return
	 */
	public Hashtable getCommonFields() {
		return commonFields;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param hashtable
	 */
	public void setCommonFields(Hashtable hashtable) {
		commonFields = hashtable;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}
	
	public void addCommonField(String id,String localField)
	{
		commonFields.put(id,localField);
	}
    /**
     * @return Returns the arcDefinition.
     */
    public ArcDefinition getArcDefinition() {
        return DisplayManager.GetInstance().getArcDefinition(getName());
    }

}

