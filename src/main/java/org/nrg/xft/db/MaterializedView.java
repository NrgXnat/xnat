// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.db;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.utils.StringUtils;

public class MaterializedView {
	static org.apache.log4j.Logger logger = Logger.getLogger(MaterializedView.class);
	public final static String MATERIALIZED_VIEWS="xs_materialized_views";
	private static boolean EXISTS=false;
	
	private String table_name;
	private String user_name;
	private String search_id;
	private String tag;
	private String search_sql;
	private String search_xml;
	private Date created;
	private Date last_access;
	private XDATUser user;
	
	public XDATUser getUser() {
		return user;
	}

	public void setUser(XDATUser user) {
		this.user = user;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getLast_access() {
		return last_access;
	}

	public void setLast_access(Date last_access) {
		this.last_access = last_access;
	}

	public String getSearch_id() {
		return search_id;
	}

	public void setSearch_id(String search_id) {
		this.search_id = search_id;
	}

	public String getSearch_sql() {
		return search_sql;
	}

	public void setSearch_sql(String search_sql) {
		this.search_sql = search_sql;
	}

	public String getSearch_xml() {
		return search_xml;
	}

	public void setSearch_xml(String search_xml) {
		this.search_xml = search_xml;
	}

	public String getTable_name() {
		return table_name;
	}

	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	
	public MaterializedView(Hashtable t,XDATUser u){
		if(u==null){
			throw new NullPointerException();
		}
		this.setCreated((Date)t.get("created"));
		this.setLast_access((Date)t.get("last_access"));
		this.setSearch_id((String)t.get("search_id"));
		this.setSearch_sql((String)t.get("search_sql"));
		this.setSearch_xml((String)t.get("search_xml"));
		this.setTable_name((String)t.get("table_name"));
		this.setTag((String)t.get("tag"));
		this.setUser_name((String)t.get("username"));
		this.setUser(u);
	}
	
	public MaterializedView(XDATUser u){
		if(u==null){
			throw new NullPointerException();
		}
		this.setUser(u);
		this.setUser_name(u.getLogin());
	}
	
	public Long getSize() throws Exception{
		return (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) AS RECORD_COUNT FROM " + PoolDBUtils.search_schema_name + "." + table_name  + ";","RECORD_COUNT",user.getDBName(),user.getLogin());
	}

	public synchronized static void VerifyManagerExistence(XDATUser user){
		PoolDBUtils.CreateTempSchema(user.getDBName(), user.getLogin());
		try {
            if (!EXISTS){

                String query ="SELECT relname FROM pg_catalog.pg_class WHERE  relname=LOWER('"+MATERIALIZED_VIEWS+"');";
                String exists =(String)PoolDBUtils.ReturnStatisticQuery(query, "relname", user.getDBName(), user.getLogin());

                if (exists!=null){
                	EXISTS=true;
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

                    PoolDBUtils.ExecuteNonSelectQuery(query, user.getDBName(), user.getLogin());

                    EXISTS=true;
                }
            }
        } catch (SQLException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }
	}
	
	public XFTTable getData(String sortBy,Integer offset, Integer limit) throws SQLException,Exception{
		String query="SELECT * FROM " + PoolDBUtils.search_schema_name + "." + this.table_name;
		
		if(sortBy!=null){
			query+=" ORDER BY " + sortBy;
		}
		
		if(offset!=null){
			query+=" OFFSET " + offset;
		}
		if(limit!=null){
			query+=" LIMIT " + limit;
		}
		
		XFTTable t=XFTTable.Execute(query + ";", user.getDBName(), user.getLogin());
		
		Thread thread = new MaterializedViewManager(this.table_name,user.getDBName());
		thread.start();
		
		return t;
	}
	
	public XFTTable getColumnValues(String column) throws SQLException,Exception{
		String query="SELECT " + column +" AS VALUES,COUNT(*) FROM " + PoolDBUtils.search_schema_name + "." + this.table_name + " GROUP BY " + column + " ORDER BY " + column;
		
		XFTTable t=XFTTable.Execute(query + ";", user.getDBName(), user.getLogin());
				
		return t;
	}
	
	
	public void delete() throws SQLException,Exception{
		String drop = "DROP TABLE " + PoolDBUtils.search_schema_name + "." +table_name + ";";
		String delete = "DELETE FROM " +PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS + " WHERE table_name='" + table_name+"';";
		
		PoolDBUtils.ExecuteNonSelectQuery(drop, user.getDBName(), user.getLogin());
		PoolDBUtils.ExecuteNonSelectQuery(delete, user.getDBName(), user.getLogin());
	}
	
	public void save() throws SQLException,Exception{		
		if(search_sql==null){
			throw new NullPointerException();
		}
		if(user==null){
			throw new NullPointerException();
		}
		
		
		MaterializedView.VerifyManagerExistence(user);
		
		if(table_name==null){
			if(search_id!=null)
				table_name= "_" + DisplaySearch.cleanColumnName(search_id)+"_"+DisplaySearch.cleanColumnName(user.getLogin()) + "_" + Calendar.getInstance().getTimeInMillis();
			else
				table_name= "_" + DisplaySearch.cleanColumnName(user.getLogin()) + "_" + Calendar.getInstance().getTimeInMillis();
		}
		String select="SELECT relname FROM pg_catalog.pg_class WHERE  relname=LOWER('"+table_name+"');";
		Object o=PoolDBUtils.ReturnStatisticQuery(select, "relname", user.getDBName(), user.getLogin());
		if(o==null){
		search_sql=StringUtils.ReplaceStr(search_sql, ";", "");
		String create = "CREATE TABLE " +PoolDBUtils.search_schema_name + "." + table_name + " AS " + StringUtils.ReplaceStr(search_sql,"''","'") + ";";
		String insert = "INSERT INTO " +PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS + " " +
				"(table_name,created,last_access,username,search_id,tag,search_sql,search_xml) VALUES " +
				"('" + table_name+"',NOW(),NOW(),'" + user.getLogin() + "'";
		if(search_id==null){
			insert+=",NULL";
		}else{
			insert+=",'" + search_id + "'";
		}
		if(tag==null){
			insert+=",NULL";
		}else{
			insert+=",'" + tag + "'";
		}
		if(search_sql==null){
			insert+=",NULL";
		}else{
			insert+=",'" + search_sql + "'";
		}
		if(search_xml==null){
			insert+=",NULL";
		}else{
			insert+=",'" + search_xml.replaceAll("'", "''") + "'";
		}
		insert+=");";
		
		
		try {
			if(XFT.VERBOSE)System.out.println("Creating Materialized View: " + table_name);
		PoolDBUtils.ExecuteNonSelectQuery(create, user.getDBName(), user.getLogin());
		PoolDBUtils.ExecuteNonSelectQuery(insert, user.getDBName(), user.getLogin());
		} catch (Exception e) {
			if(e.getMessage().indexOf("pg_type_typname_nsp_index")>-1){
				//retry
				logger.info("Duplicate materialized view.");
				if(XFT.VERBOSE)System.out.println("Duplicate materialized view.");
//				PoolDBUtils.ExecuteNonSelectQuery(create, user.getDBName(), user.getLogin());
//				PoolDBUtils.ExecuteNonSelectQuery(insert, user.getDBName(), user.getLogin());
			}else{
				throw e;
			}
		}
	}
	}

	public static MaterializedView GetMaterializedView(String table_name, XDATUser user)throws DBPoolException, SQLException{
		MaterializedView.VerifyManagerExistence(user);
		XFTTable t = XFTTable.Execute("SELECT * FROM " +PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS + " WHERE table_name='" + table_name+"';", user.getDBName(), user.getLogin());
		if(t.size()>0){
			return new MaterializedView(t.rowHashs().get(0),user);
		}else{
			return null;
		}
	}

	public static MaterializedView GetMaterializedViewBySearchID(String search_id, XDATUser user)throws DBPoolException, SQLException{
		MaterializedView.VerifyManagerExistence(user);
		XFTTable t = XFTTable.Execute("SELECT * FROM " +PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS + " WHERE search_id='" + search_id+"' AND username='" + user.getLogin() + "';", user.getDBName(), user.getLogin());
		if(t.size()>0){
			return new MaterializedView(t.rowHashs().get(0),user);
		}else{
			return null;
		}
	}
	
	public static void DeleteBySearchID(String search_id, XDATUser user)throws DBPoolException, SQLException,Exception{
		MaterializedView.VerifyManagerExistence(user);
		XFTTable t = XFTTable.Execute("SELECT * FROM " +PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS + " WHERE search_id='" + search_id+"';", user.getDBName(), user.getLogin());
		if(t.size()>0){
			MaterializedView mv= new MaterializedView(t.rowHashs().get(0),user);
			mv.delete();
		}
	}
	
	public static void DeleteByUser(XDATUser user)throws DBPoolException, SQLException,Exception{
		MaterializedView.VerifyManagerExistence(user);
		XFTTable t = XFTTable.Execute("SELECT * FROM " +PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS + " WHERE username='" + user.getLogin() + "';", user.getDBName(), user.getLogin());
		if(t.size()>0){
			while(t.hasMoreRows()){
				try{
				MaterializedView mv= new MaterializedView(t.nextRowHash(),user);
				mv.delete();
				}catch(Exception e){
					//ignore
				}
			}
		}
	}
	
	public class MaterializedViewManager extends Thread{
		String dbname=null;
		String currentView=null;
			
		public MaterializedViewManager(String currentView,String dbname) {
			super();
			this.currentView=currentView;
			this.dbname=dbname;
			
		}
			
		public MaterializedViewManager(String currentView) {
			super();
		}

		@Override
		public void run() {
			try {
				String query=null;
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
	
	public DisplaySearch getDisplaySearch(XDATUser user)throws Exception{
		XFTItem item = item = XFTItem.PopulateItemFromFlatString(this.getSearch_xml(),user,true); 
		XdatStoredSearch search = new XdatStoredSearch(item);
		
		return search.getDisplaySearch(user);
	}
}
