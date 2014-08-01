package org.nrg.xdat.security.helpers;

public interface FeatureDefinitionI {
	
	/**
	 * What is the key which identifies this feature
	 * @return
	 */
	public String getKey();
	
	/**
	 * What is the human readable name of this feature
	 * @return
	 */
	public String getName();
	
	/**
	 * What is the description of this feature for human readability?
	 * @return
	 */
	public String getDescription();
	
	public boolean isBanned();


	public boolean isOnByDefault();
}
