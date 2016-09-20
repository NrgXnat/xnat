/*
 * core: org.nrg.xft.schema.DataModelDefinition
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
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
	 * Defines teh 
	 * @return
	 */
	public String getSchemaPath();
	
	/**
	 * @return
	 */
	public String[] getDisplayDocs();
	
	/**
	 * @return
	 */
	public String[] getSecuredElements();
	
    /**
     * @return
     */
    public boolean required();
}
