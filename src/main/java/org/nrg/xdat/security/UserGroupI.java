package org.nrg.xdat.security;

import org.nrg.xft.ItemI;

public interface UserGroupI {

	public abstract String getId();
	public abstract String getTag();
	public abstract String getDisplayname();
	public abstract Integer getPK();
	public abstract void setId(String id);
	public abstract void setTag(String tag);
	public abstract void setDisplayname(String displayName);
	public abstract void setPK(Integer pk);
}