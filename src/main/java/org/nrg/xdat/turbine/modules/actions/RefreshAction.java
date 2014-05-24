//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 18, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import java.io.File;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTool;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.utils.FileUtils;
/**
 * @author Tim
 *
 */
public class RefreshAction extends AdminAction {
	static Logger logger = Logger.getLogger(RefreshAction.class);
	public void doPerform(RunData data, Context context) throws Exception
	{
		if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("refresh",data)) !=null)
		{
			String refresh = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("refresh",data));
			if (refresh.equalsIgnoreCase("security"))
			{
				String location = XFTTool.GetSettingsLocation();
				location =FileUtils.AppendSlash(location);
				File f = new File(location + "security.xml");
				if (f.exists())
				{
					XFTTool.StoreXMLToDB(f,TurbineUtils.getUser(data),null,false);
				}
			}else if (refresh.equalsIgnoreCase("DisplayManager"))
			{
				XDAT.RefreshDisplay();
                System.out.println("DisplayManager Refreshed.");
			}else if (refresh.equalsIgnoreCase("ElementSecurity"))
			{
				ElementSecurity.refresh();
                System.out.println("Element Security Refreshed.");
			}else if (refresh.equalsIgnoreCase("ClearDBCache"))
            {
                PoolDBUtils.ClearCache(TurbineUtils.getUser(data).getDBName(), TurbineUtils.getUser(data).getLogin());
                System.out.println("DB Cache Refreshed.");
    
                org.nrg.xft.cache.CacheManager.GetInstance().clearAll();
            }else if (refresh.equalsIgnoreCase("MissingMetadatas"))
            {
            	DBAction.InsertMetaDatas();
                System.out.println("Inserted Meta Datas");
            }else if (refresh.equalsIgnoreCase("PGVacuum"))
            {
                VacuumThread vt = new VacuumThread();
                vt.db=TurbineUtils.getUser(data).getDBName();
                vt.user=TurbineUtils.getUser(data).getLogin();
                vt.start();
            }
		}
	}


    @Override

    protected boolean isAuthorized(RunData data) throws Exception {

        boolean authorized= super.isAuthorized(data);

        if (authorized)

        {

            if (!TurbineUtils.getUser(data).checkRole("Administrator"))

            {

                authorized=false;

                data.setMessage("Unauthorized access.  Please login to gain access to this page.");

                logger.error("Unauthorized Access by " + TurbineUtils.getUser(data).getLogin() +" to Refresh Actions (prevented).");

                AdminUtils.sendAdminEmail(TurbineUtils.getUser(data),"Unauthorized Admin Access Attempt", "Unauthorized Access by " + TurbineUtils.getUser(data).getLogin() +" to Refresh Actions (prevented).");
            }

        }
        return authorized;

    }
    
    public class VacuumThread extends Thread{
        public String db = null;
        public String user=null;
        public void run(){
            System.out.println("Calling PG Vacuum...");
            try {
                PoolDBUtils.ExecuteNonSelectQuery("VACUUM ANALYZE;", db, user);
                System.out.println("PG Vacuum completed.");
            } catch (SQLException e) {
                System.out.println("PG Vacuum failed.");
                logger.error("",e);
            } catch (Exception e) {
                System.out.println("PG Vacuum failed.");
                logger.error("",e);
            }
        }
    }
}

