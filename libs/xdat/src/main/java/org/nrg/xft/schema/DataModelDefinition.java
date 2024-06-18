/*
 * core: org.nrg.xft.schema.DataModelDefinition
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.schema;

/**
 * Data Model Definition is used to define a schema that should be loaded from the classpath (jar).
 * 
 **/
public interface DataModelDefinition {
	
	/**
	 * Defines the schema path
	 * @return Returns the schema path String
	 */
	public String getSchemaPath();
	
	/**
	 * @return Returns an array of display doc Strings
	 */
	public String[] getDisplayDocs();
	
	/**
	 * @return Returns an array of secured elements as Strings
	 */
	public String[] getSecuredElements();
	
    /**
     * @return Returns whether it is required
     */
    public boolean required();
}
