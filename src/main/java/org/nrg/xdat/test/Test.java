//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 3, 2005
 *
 */
package org.nrg.xdat.test;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.XFTTableI;

import java.io.File;


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
            
             XDAT.init(new File(appDir).toURI(), false, true);
//            
//            
//           // XFTTool.GenerateSQL("cdisc.sql");
            XDATUser user = new org.nrg.xdat.security.XDATUser("tolsen","mysql");
            ElementSecurity.GetElementSecurities();

            DisplaySearch ds = user.getSearch("xnat:mrSessionData", "listing");
            ds.setUser(null);
            XFTTableI t= ds.execute("tolsen");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
			
	}
}
