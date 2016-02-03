/*
 * org.nrg.xdat.om.base.BaseArcPathinfo
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoArcPathinfo;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseArcPathinfo extends AutoArcPathinfo {

	public BaseArcPathinfo(ItemI item)
	{
		super(item);
	}

	public BaseArcPathinfo(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseArcPathinfo(UserI user)
	 **/
	public BaseArcPathinfo()
	{}

	public BaseArcPathinfo(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    public String getArchivepath(){
        try{
            String path = super.getArchivepath();

            path = path.replace('\\', '/');
            if (!path.endsWith("/")){
                path = path +"/";
            }
            return path;
        } catch (Throwable e1) {logger.error(e1);return null;}
    }
    /**
     * @return Returns the prearchivePath.
     */
    public String getPrearchivepath(){
        try{
            String path = super.getPrearchivepath();

            path = path.replace('\\', '/');
            if (!path.endsWith("/")){
                path = path +"/";
            }
            return path;
        } catch (Throwable e1) {logger.error(e1);return null;}
    }

    /**
     * @return Returns the cachePath.
     */
    public String getCachepath(){
        try{
            String path = super.getCachepath();

            path = path.replace('\\', '/');
            if (!path.endsWith("/")){
                path = path +"/";
            }
            return path;
        } catch (Throwable e1) {logger.error(e1);return null;}
    }

    /**
     * @return Returns the buildPath.
     */
    public String getBuildpath(){
        try{
            String path = super.getBuildpath();

            path = path.replace('\\', '/');
            if (!path.endsWith("/")){
                path = path +"/";
            }
            return path;
        } catch (Throwable e1) {logger.error(e1);return null;}
    }
}
