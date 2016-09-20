/*
 * core: org.nrg.xdat.turbine.modules.actions.RefreshAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.actions;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.services.StudyRoutingService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
/**
 * @author Tim
 *
 */
public class RefreshAction extends AdminAction {
	static Logger logger = Logger.getLogger(RefreshAction.class);
	public void doPerform(RunData data, Context context) throws Exception
	{
		if (TurbineUtils.GetPassedParameter("refresh",data) !=null)
		{
			String refresh = ((String)TurbineUtils.GetPassedParameter("refresh",data));
			if (refresh.equalsIgnoreCase("DisplayManager"))
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
            }else if (refresh.equalsIgnoreCase("ClearStudyRoutings"))
            {
                XDAT.getContextService().getBean(StudyRoutingService.class).closeAll();
                System.out.println("Cleared all study routings.");
            }else if (refresh.equalsIgnoreCase("MissingMetadatas"))
            {
            	DBAction.InsertMetaDatas();
                System.out.println("Inserted Meta Data");
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

            if (!Roles.isSiteAdmin(TurbineUtils.getUser(data)))

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

