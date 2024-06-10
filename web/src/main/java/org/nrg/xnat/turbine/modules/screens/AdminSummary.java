/*
 * web: org.nrg.xnat.turbine.modules.screens.AdminSummary
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.sql.SQLException;
import java.util.Hashtable;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.AdminScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.security.UserI;

public class AdminSummary extends AdminScreen {
	UserI u;
	Hashtable tableProps ;
	@Override
	protected void doBuildTemplate(RunData data, Context context)
			throws Exception {
		
		if(TurbineUtils.HasPassedParameter("duration1", data)){
			context.put("duration1", TurbineUtils.GetPassedParameter("duration1", data));
		}else{
			context.put("duration1","1 week");
		}
		if(TurbineUtils.HasPassedParameter("duration2", data)){
			context.put("duration2", TurbineUtils.GetPassedParameter("duration2", data));
		}else{
			context.put("duration2","1 second");
		}
		
		tableProps = new Hashtable();
		tableProps.put("bgColor","white"); 
		tableProps.put("border","0"); 
		tableProps.put("cellPadding","0"); 
		tableProps.put("cellSpacing","0"); 
		tableProps.put("width","95%"); 
		
		u = TurbineUtils.getUser(data);
		
		context.put("qm", new QueryManager(u.getLogin(),u.getDBName(),tableProps));
	}
	
	public class QueryManager{
		String user=null;
		String db=null;
		public Hashtable tp=null;
		public QueryManager(String user,String db,Hashtable hash){
			this.user=user;
			this.db=db;
			this.tp=hash;
		}
		
		public String execute(String sql){
			try {
				return XFTTable.Execute(sql.replace("\\\"", "\""),db,user).toHTML(true,"FFFFFF","DEDEDE",tp,0);
			} catch (SQLException e) {
				logger.error("", e);
				return e.getMessage();
			} catch (DBPoolException e) {
				logger.error("", e);
				return null;
			}
			
		}
	}
	
	public class QObject{
		public String alias=null;
		public String description=null;
		public String sql=null;
		public Hashtable tp=null;
		public UserI u=null;
		
		public QObject(String alias,String sql,Hashtable tp,UserI u) {
			super();
			this.alias = alias;
			this.sql = sql;
			this.tp=tp;
			this.u=u;
		}
		
		

		public QObject(String alias, String description, String sql, Hashtable tp,UserI u) {
			super();
			this.alias = alias;
			this.description = description;
			this.sql = sql;
			this.tp = tp;
			this.u=u;
		}



		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}

		public String getData() {
			try {
				return XFTTable.Execute(sql,u.getDBName(),u.getLogin()).toHTML(false,"FFFFFF","DEDEDE",tp,0);
			} catch (SQLException e) {
				logger.error("", e);
			} catch (DBPoolException e) {
				logger.error("", e);
			}
			return null;
		}		
		
	}
}
