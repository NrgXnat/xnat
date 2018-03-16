package org.nrg.xnat.helpers.uri;

public abstract class ManageableXnatURIContainer{
	
	public abstract String getTemplate();
	public abstract String getBaseTemplate();
	public abstract int getMode();
	public abstract URIManager.TEMPLATE_TYPE getTemplateType();
	public abstract Class<? extends URIManager.DataURIA> getUri();
	
	}