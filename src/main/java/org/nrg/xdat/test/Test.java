/*
 * org.nrg.xdat.test.Test
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.test;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xft.XFTTableI;


/**
 * @author Tim
 *
 */
public class Test {

	public static void main(String[] args) {
		try {
			//String appDir = "C:\\xdat\\projects\\sample";
			String appDir = "C:\\xdat\\deployments\\cnda_xnat";
            String sep =System.getProperty("line.separator");
            String id = "050603_vc18118";
            
             XDAT.init(appDir,false,true);
//            
//            
//           // XFTTool.GenerateSQL("cdisc.sql");
            org.nrg.xdat.security.XDATUser user = new org.nrg.xdat.security.XDATUser("tolsen","mysql");
            org.nrg.xdat.security.ElementSecurity.GetElementSecurities();

            DisplaySearch ds = user.getSearch("xnat:mrSessionData", "listing");
            ds.setUser(null);
            XFTTableI t= ds.execute("tolsen");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
			
	}
}