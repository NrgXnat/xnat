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
