package org.nrg.xft.db.views.service;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.MaterializedViewI;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;

import com.google.common.collect.Lists;

public class MaterializedViewManager {
	static org.apache.log4j.Logger logger = Logger.getLogger(MaterializedViewManager.class);

	public final static String MATERIALIZED_VIEWS="xs_materialized_views";
	private static MaterializedViewManager manager=null;
	
	public static MaterializedViewManager getMaterializedViewManager(){
		try {
            if (manager==null){
        		PoolDBUtils.CreateTempSchema(PoolDBUtils.getDefaultDBName(),null);

                String query ="SELECT relname FROM pg_catalog.pg_class WHERE  relname=LOWER('"+MATERIALIZED_VIEWS+"');";
                String exists =(String)PoolDBUtils.ReturnStatisticQuery(query, "relname", PoolDBUtils.getDefaultDBName(), null);

                if (exists!=null){
                	manager=new MaterializedViewManager();
                }else{
                    query = "CREATE TABLE " + PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS+
                    "\n("+
                    "\n  table_name VARCHAR(255),"+
                    "\n  created timestamp DEFAULT now(),"+
                    "\n  last_access timestamp DEFAULT now(),"+
                    "\n  username VARCHAR(255),"+
                    "\n  search_id text,"+
                    "\n  tag VARCHAR(255),"+
                    "\n  search_sql text,"+
                    "\n  search_xml text"+
                    "\n);";

                    PoolDBUtils.ExecuteNonSelectQuery(query, PoolDBUtils.getDefaultDBName(), null);

                	manager=new MaterializedViewManager();
                }
            }
        } catch (SQLException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }
		
		return manager;
	}
	

		
		public static class DBMaterializedViewManager extends Thread{
			String dbname=null;
			String currentView=null;
				
			public DBMaterializedViewManager(String currentView,String dbname) {
				super();
				this.currentView=currentView;
				this.dbname=dbname;
				
			}
				
			public DBMaterializedViewManager(String currentView) {
				super();
			}

			@Override
			public void run() {
				try {
					String query;
					if(currentView!=null){
						query = "UPDATE " +PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS + " SET last_access=NOW() WHERE table_name='" + currentView + "';";
						PoolDBUtils.ExecuteNonSelectQuery(query, dbname, "system");
					}
					
					query="SELECT * FROM " +PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS + " WHERE last_access + interval '1 hour'< NOW();";
					XFTTable table = XFTTable.Execute(query, dbname, "system");
					
					for(Hashtable row : table.rowHashs()){
						try {
							query = "DROP TABLE " + PoolDBUtils.search_schema_name + "." + row.get("table_name") + ";";
							PoolDBUtils.ExecuteNonSelectQuery(query, dbname, "system");
						} catch (Throwable e) {
				            continue;
						}
						try{
							query = "DELETE FROM " + PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS + " WHERE table_name ='" + row.get("table_name") + "' ;";
							PoolDBUtils.ExecuteNonSelectQuery(query, dbname, "system");
						} catch (Throwable e) {
				            logger.error("",e);
						}
					}
				} catch (Throwable e) {
		            logger.error("",e);
				}
			}
		}



		public List<MaterializedViewI> getViewsByUser(UserI user,MaterializedViewServiceI service) throws Exception {
			List<MaterializedViewI> views=Lists.newArrayList();
			XFTTable t = XFTTable.Execute("SELECT * FROM " +PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS + " WHERE username='" + user.getUsername() + "';", PoolDBUtils.getDefaultDBName(), user.getUsername());
			if(t.size()>0){
				while(t.hasMoreRows()){
					views.add(service.populateView(t.nextRowHash(),user));
				}
			}
			return views;
		}
		
		public MaterializedViewI getViewBySearchID(String search_id, UserI user,MaterializedViewServiceI service) throws Exception {
			XFTTable t = XFTTable.Execute("SELECT * FROM " +PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS + " WHERE search_id='" + search_id+"';", PoolDBUtils.getDefaultDBName(), user.getUsername());
			if(t.size()>0){
				return service.populateView(t.nextRowHash(),user);
			}
			return null;
		}

		public MaterializedViewI getViewByTablename(String tablename, UserI user,MaterializedViewServiceI service) throws Exception {
			XFTTable t = XFTTable.Execute("SELECT * FROM " +PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS + " WHERE table_name='" + tablename+"';", PoolDBUtils.getDefaultDBName(), user.getUsername());
			if(t.size()>0){
				return service.populateView(t.nextRowHash(),user);
			}
			return null;
		}
		
		public void delete(MaterializedViewI view) throws SQLException, Exception{
			String delete = "DELETE FROM " +PoolDBUtils.search_schema_name + "." + MaterializedViewManager.MATERIALIZED_VIEWS + " WHERE table_name='" + view.getTable_name() +"';";

			PoolDBUtils.ExecuteNonSelectQuery(delete, PoolDBUtils.getDefaultDBName(), view.getUser_name());
		}
		
		public void register(MaterializedViewI view) throws SQLException, Exception{
			String insert = "INSERT INTO " +PoolDBUtils.search_schema_name + "." + MaterializedViewManager.MATERIALIZED_VIEWS + " " +
					"(table_name,created,last_access,username,search_id,tag,search_sql,search_xml) VALUES " +
					"('" + view.getTable_name()+"',NOW(),NOW(),'" + view.getUser_name() + "'";
			if(view.getSearch_id()==null){
				insert+=",NULL";
			}else{
				insert+=",'" + view.getSearch_id() + "'";
			}
			if(view.getTag()==null){
				insert+=",NULL";
			}else{
				insert+=",'" + view.getTag() + "'";
			}
			if(view.getSearch_sql()==null){
				insert+=",NULL";
			}else{
				insert+=",'" + view.getSearch_sql() + "'";
			}
			if(view.getSearch_xml()==null){
				insert+=",NULL";
			}else{
				insert+=",'" + view.getSearch_xml().replaceAll("'", "''") + "'";
			}
			insert+=");";

			PoolDBUtils.ExecuteNonSelectQuery(insert, PoolDBUtils.getDefaultDBName(), view.getUser_name());
		}
		
		public static void Register(MaterializedViewI view) throws SQLException, Exception{
			getMaterializedViewManager().register(view);
		}
		
		public static void Delete(MaterializedViewI view) throws SQLException, Exception{
			getMaterializedViewManager().delete(view);
		}
}
