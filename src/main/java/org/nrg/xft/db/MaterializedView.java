// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.db;

import java.sql.SQLException;
import java.util.*;

import org.apache.log4j.Logger;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.utils.StringUtils;

import com.google.common.collect.Lists;

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
		PoolDBUtils.CheckSpecialSQLChars(search_id);
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
		PoolDBUtils.CheckSpecialSQLChars(table_name);
		
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
		return getSize(null);
	}
	
	public Long getSize(Map<String,Object> filters) throws Exception{
		String query="SELECT COUNT(*) AS RECORD_COUNT FROM " + PoolDBUtils.search_schema_name + "." + table_name;
		if(filters!=null && filters.size()>0){
			validateColumns(filters.keySet(),this);
			
			query+=" WHERE ";
			int count=0;
			for(Map.Entry<String,Object> entry:filters.entrySet()){
				if(count++>0)query+=" AND ";
				
				query+=buildComparison(entry.getKey(),entry.getValue());
			}
		}
		return (Long) PoolDBUtils.ReturnStatisticQuery( query + ";","RECORD_COUNT",user.getDBName(),user.getLogin());
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
	
	public XFTTable getData(String sortBy,Integer offset, Integer limit) throws Exception{
		return getData(sortBy,offset,limit,null);
	}
	
	public XFTTable getData(String sortBy,Integer offset, Integer limit,Map<String,Object> filters) throws Exception{
		String query="SELECT * FROM " + PoolDBUtils.search_schema_name + "." + this.table_name;
		
		if(filters!=null && filters.size()>0){
			validateColumns(filters.keySet(),this);
			
			query+=" WHERE ";
			int count=0;
			for(Map.Entry<String,Object> entry:filters.entrySet()){
				if(count++>0)query+=" AND ";
				
				query+=buildComparison(entry.getKey(),entry.getValue());
			}
		}
		
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
	
	public static String buildComparison(String key, Object v){
		List<String> values=StringUtils.CommaDelimitedStringToArrayList(v.toString());
		List<String> validValues=Lists.newArrayList();
		String clause="";
		
		int count=0;
		for(String value:values){
			if(!PoolDBUtils.HackCheck(value)){
				if(value.equals("''")){
					value="NULL";
				}else if(value.equals("'NULL'")){
					value="NULL";
				}else if(value.equals("'NOT NULL'")){
					value="NOT NULL";
				}
				
				if(value.startsWith("'")&& value.endsWith("'")){
					validValues.add(value.substring(1,value.length()-1));
				}else if(value.equalsIgnoreCase("NULL")){
					if(count++>0)clause+=" OR ";
					clause+=" ("+ key +" IS NULL) ";
				}else if(value.equalsIgnoreCase("NOT NULL")){
					if(count++>0)clause+=" OR ";
					clause+=" ("+ key +" IS NOT NULL) ";
				}else if(value.contains("-")){
					if(count++>0)clause+=" OR ";
					
					String value1=value.substring(0,value.indexOf("-"));
					String value2=value.substring(value.indexOf("-")+1);
					clause+=" ("+ key +" BETWEEN '" + value1 +"' AND '" + value2 +"') ";
				}else if(value.startsWith("<=")){
					if(count++>0)clause+=" OR ";
					clause+=" ("+ key +" <= '"+ value.substring(2) +"') ";
				}else if(value.startsWith("<")){
					if(count++>0)clause+=" OR ";
					clause+=" ("+ key +" < '"+ value.substring(1) +"') ";
				}else if(value.startsWith(">=")){
					if(count++>0)clause+=" OR ";
					clause+=" ("+ key +" >= '"+ value.substring(2) +"') ";
				}else if(value.startsWith(">")){
					if(count++>0)clause+=" OR ";
					clause+=" ("+ key +" > '"+ value.substring(1) +"') ";
				}else{
					validValues.add(value);
				}
			}
		}
		
		if(validValues.size()>0){
			if(clause.length()>0)clause+=" OR ";
			
			clause+=" (" + key + " IN (";
			int inner=0;
			for(String value:validValues){
				if(inner++>0)clause+=",";
				clause+="'"+ value +"'";
			}
			clause+=")) ";
		}
		
		return "("+clause+")";
	}
	
	public static void validateColumns(Collection<String> columns, MaterializedView mv) throws Exception{
		List<String> all_columns=mv.getColumnNames();
		List<String> badColumns = new ArrayList<String>();
		for(String column:columns){
			if(!all_columns.contains(column)){
                badColumns.add(column);
			}
		}
        if (badColumns.size() > 0) {
            throw new Exception("Invalid column in request: " + org.apache.commons.lang.StringUtils.join(badColumns, ", "));
        }
	}
	
	public static void validateColumns(String columnName, MaterializedView mv) throws Exception{
		List<String> columns=StringUtils.CommaDelimitedStringToArrayList(columnName);
		validateColumns(columns,mv);
	}
	
	private List<String> cachedColumnNames=null;
	public List<String> getColumnNames() throws Exception{
		if(cachedColumnNames==null){
			String query="select LOWER(attname) as col_name from pg_attribute, pg_class,pg_type where attrelid = pg_class.oid AND atttypid=pg_type.oid AND attnum>0 and LOWER(relname) = '" + this.table_name.toLowerCase() + "';";
			XFTTable t=XFTTable.Execute(query, user.getDBName(), user.getLogin());
			cachedColumnNames=t.convertColumnToArrayList("col_name");
		}
		
		return cachedColumnNames;
	}
	
	public XFTTable getColumnValues(String column) throws SQLException,Exception{
		String query="SELECT " + column +" AS VALUES,COUNT(*) FROM " + PoolDBUtils.search_schema_name + "." + this.table_name + " GROUP BY " + column + " ORDER BY " + column;
		
		XFTTable t=XFTTable.Execute(query + ";", user.getDBName(), user.getLogin());
				
		return t;
	}
	
	public XFTTable getColumnsValues(String column) throws Exception{
		return getColumnsValues(column, null);
	}
	
	public XFTTable getColumnsValues(String column,Map<String,Object> filters) throws Exception{
		String query="SELECT " + column +",COUNT(*) FROM " + PoolDBUtils.search_schema_name + "." + this.table_name;
		
		if(filters!=null && filters.size()>0){
			validateColumns(filters.keySet(),this);
			
			query+=" WHERE ";
			int count=0;
			for(Map.Entry<String,Object> entry:filters.entrySet()){
				if(count++>0)query+=" AND ";
				
				query+=buildComparison(entry.getKey(),entry.getValue());
			}
		}
		
		query+= " GROUP BY " + column + " ORDER BY " + column;
		
		XFTTable t=XFTTable.Execute(query + ";", user.getDBName(), user.getLogin());
				
		return t;
	}
	
	
	public void delete() throws Exception{
		String drop = "DROP TABLE " + PoolDBUtils.search_schema_name + "." +table_name + ";";
		String delete = "DELETE FROM " +PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS + " WHERE table_name='" + table_name+"';";
		
		PoolDBUtils.ExecuteNonSelectQuery(drop, user.getDBName(), user.getLogin());
		PoolDBUtils.ExecuteNonSelectQuery(delete, user.getDBName(), user.getLogin());
	}
	
	public void save() throws Exception{
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
			if(e.getMessage().contains("pg_type_typname_nsp_index")){
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
	
	public static void DeleteBySearchID(String search_id, XDATUser user)throws Exception{
		MaterializedView.VerifyManagerExistence(user);
		XFTTable t = XFTTable.Execute("SELECT * FROM " +PoolDBUtils.search_schema_name + "." + MATERIALIZED_VIEWS + " WHERE search_id='" + search_id+"';", user.getDBName(), user.getLogin());
		if(t.size()>0){
			MaterializedView mv= new MaterializedView(t.rowHashs().get(0),user);
			mv.delete();
		}
	}
	
	public static void DeleteByUser(XDATUser user)throws Exception{
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
	
	public DisplaySearch getDisplaySearch(XDATUser user)throws Exception{
		XFTItem item = XFTItem.PopulateItemFromFlatString(this.getSearch_xml(),user,true);
		XdatStoredSearch search = new XdatStoredSearch(item);
		
		return search.getDisplaySearch(user);
	}
}
