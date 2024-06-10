package org.nrg.xnat.helpers.uri;

public interface ManageableXnatURIContainer{
	
	public String getTemplate();
	public String getBaseTemplate();
	public int getMode();
	public URIManager.TEMPLATE_TYPE getTemplateType();
	public Class<? extends URIManager.DataURIA> getUri();
	
}