//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 6, 2005
 *
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

