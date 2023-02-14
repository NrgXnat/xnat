/*
 * core: org.nrg.xft.db.views.LegacyMaterializedViewImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.db.views;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.search.DisplayFieldAliasCache;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.MaterializedViewI;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.views.service.MaterializedViewManager;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;

import java.sql.SQLException;
import java.util.*;

public class LegacyMaterializedViewImpl implements MaterializedViewI {
	static org.apache.log4j.Logger logger = Logger.getLogger(LegacyMaterializedViewImpl.class);
	
	private String table_name;
	private String user_name;
	private String search_id;
	private String tag;
	private String search_sql;
	private String search_xml;
	private Date created;
	private Date last_access;
	private UserI user;
	
	public LegacyMaterializedViewImpl(Hashtable t,UserI u){
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
	
	public LegacyMaterializedViewImpl(UserI u){
		if(u==null){
			throw new NullPointerException();
		}
		this.setUser(u);
		this.setUser_name(u.getUsername());
	}

    @Override
    public String getCode() {
        return MaterializedView.DEFAULT_MATERIALIZED_VIEW_SERVICE_CODE;
    }

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getUser()
	 */
	@Override
	public UserI getUser() {
		return user;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#setUser(org.nrg.xdat.security.UserI)
	 */
	@Override
	public void setUser(UserI user) {
		this.user = user;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getCreated()
	 */
	@Override
	public Date getCreated() {
		return created;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#setCreated(java.util.Date)
	 */
	@Override
	public void setCreated(Date created) {
		this.created = created;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getLast_access()
	 */
	@Override
	public Date getLast_access() {
		return last_access;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#setLast_access(java.util.Date)
	 */
	@Override
	public void setLast_access(Date last_access) {
		this.last_access = last_access;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getSearch_id()
	 */
	@Override
	public String getSearch_id() {
		return search_id;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#setSearch_id(java.lang.String)
	 */
	@Override
	public void setSearch_id(String search_id) {
		PoolDBUtils.CheckSpecialSQLChars(search_id);
		this.search_id = search_id;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getSearch_sql()
	 */
	@Override
	public String getSearch_sql() {
		return search_sql;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#setSearch_sql(java.lang.String)
	 */
	@Override
	public void setSearch_sql(String search_sql) {
		this.search_sql = search_sql;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getSearch_xml()
	 */
	@Override
	public String getSearch_xml() {
		return search_xml;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#setSearch_xml(java.lang.String)
	 */
	@Override
	public void setSearch_xml(String search_xml) {
		this.search_xml = search_xml;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getTable_name()
	 */
	@Override
	public String getTable_name() {
		return table_name;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#setTable_name(java.lang.String)
	 */
	@Override
	public void setTable_name(String table_name) {
		PoolDBUtils.CheckSpecialSQLChars(table_name);
		
		this.table_name = table_name;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getTag()
	 */
	@Override
	public String getTag() {
		return tag;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#setTag(java.lang.String)
	 */
	@Override
	public void setTag(String tag) {
		this.tag = tag;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getUser_name()
	 */
	@Override
	public String getUser_name() {
		return user_name;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#setUser_name(java.lang.String)
	 */
	@Override
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getSize()
	 */
	@Override
	public Long getSize() throws Exception{
		return getSize(null);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getData(java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public XFTTable getData(String sortBy,Integer offset, Integer limit) throws Exception{
		return getData(sortBy,offset,limit,null);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getData(java.lang.String, java.lang.Integer, java.lang.Integer, java.util.Map)
	 */
	@Override
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
			String[] sortParts = sortBy.split(" ");
			String columnName = sortParts[0];
			String sortOrder = "";
			if (sortParts.length == 2) {
				sortOrder = sortParts[1];
			}
			columnName = DisplayFieldAliasCache.getAlias(columnName);
			query+=" ORDER BY " + columnName + " " + sortOrder + ", key";
		}


		if(offset!=null){
			query+=" OFFSET " + offset;
		}
		if(limit!=null){
			query+=" LIMIT " + limit;
		}
		
		XFTTable t=XFTTable.Execute(query + ";", PoolDBUtils.getDefaultDBName(), user.getUsername());
		
		Thread thread = new MaterializedViewManager.DBMaterializedViewManager(XDAT.getNamedParameterJdbcTemplate(), table_name);
		thread.start();

		return t;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getSize(java.util.Map)
	 */
	@Override
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
		return (Long) PoolDBUtils.ReturnStatisticQuery( query + ";","RECORD_COUNT",PoolDBUtils.getDefaultDBName(),user.getUsername());
	}
	
	private List<String> cachedColumnNames=null;
	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getColumnNames()
	 */
	@Override
	public List<String> getColumnNames() throws Exception{
		if(cachedColumnNames==null){
			String query="select LOWER(attname) as col_name from pg_attribute, pg_class,pg_type where attrelid = pg_class.oid AND atttypid=pg_type.oid AND attnum>0 and LOWER(relname) = '" + this.table_name.toLowerCase() + "';";
			XFTTable t=XFTTable.Execute(query, PoolDBUtils.getDefaultDBName(), user.getUsername());
			cachedColumnNames=t.convertColumnToArrayList("col_name");
		}
		
		return cachedColumnNames;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getColumnValues(java.lang.String)
	 */
	@Override
	public XFTTable getColumnValues(String column) throws SQLException,Exception{
		String columnAlias = DisplayFieldAliasCache.getAlias(column);
		String query="SELECT " + columnAlias +" AS VALUES,COUNT(*) FROM " + PoolDBUtils.search_schema_name + "." + this.table_name + " GROUP BY " + columnAlias + " ORDER BY " + columnAlias;
		
		XFTTable t=XFTTable.Execute(query + ";", PoolDBUtils.getDefaultDBName(), user.getUsername());
				
		return t;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getColumnsValues(java.lang.String)
	 */
	@Override
	public XFTTable getColumnsValues(String column) throws Exception{
		return getColumnsValues(column, null);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getColumnsValues(java.lang.String, java.util.Map)
	 */
	@Override
	public XFTTable getColumnsValues(String column,Map<String,Object> filters) throws Exception{
		String columnAlias = DisplayFieldAliasCache.getAlias(column);
		String query="SELECT " + columnAlias +",COUNT(*) FROM " + PoolDBUtils.search_schema_name + "." + this.table_name;
		
		if(filters!=null && filters.size()>0){
			validateColumns(filters.keySet(),this);
			
			query+=" WHERE ";
			int count=0;
			for(Map.Entry<String,Object> entry:filters.entrySet()){
				if(count++>0)query+=" AND ";
				
				query+=buildComparison(entry.getKey(),entry.getValue());
			}
		}
		
		query+= " GROUP BY " + columnAlias + " ORDER BY " + columnAlias;
		
		XFTTable t=XFTTable.Execute(query + ";", PoolDBUtils.getDefaultDBName(), user.getUsername());
				
		return t;
	}
	
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#delete()
	 */
	@Override
	public void delete() throws Exception{
		String drop = "DROP TABLE " + PoolDBUtils.search_schema_name + "." +table_name + ";";
		PoolDBUtils.ExecuteNonSelectQuery(drop, PoolDBUtils.getDefaultDBName(), user.getUsername());
		
		MaterializedViewManager.Delete(this);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#save()
	 */
	@Override
	public void save() throws Exception{
		if(search_sql==null){
			throw new NullPointerException();
		}
		if(user==null){
			throw new NullPointerException();
		}

		if (StringUtils.isBlank(table_name)) {
			table_name = generateMaterializedViewName();
		}
		String select="SELECT relname FROM pg_catalog.pg_class WHERE  relname=LOWER('"+table_name+"');";
		Object o=PoolDBUtils.ReturnStatisticQuery(select, "relname", PoolDBUtils.getDefaultDBName(), user.getUsername());
		if(o==null){
			search_sql= StringUtils.replace(search_sql, ";", "");
			String create = "CREATE TABLE " + PoolDBUtils.search_schema_name + "." + table_name + " AS " + StringUtils.replace(search_sql, "''", "'") + ";";
			
		
			try {
				if(XFT.VERBOSE)System.out.println("Creating Materialized View: " + table_name);
				PoolDBUtils.ExecuteNonSelectQuery(create, PoolDBUtils.getDefaultDBName(), user.getUsername());
				
				MaterializedViewManager.Register(this);
			} catch (Exception e) {
				if(e.getMessage().contains("pg_type_typname_nsp_index")){
					//retry
					logger.info("Duplicate materialized view.");
					if(XFT.VERBOSE)System.out.println("Duplicate materialized view.");
				}else{
					throw e;
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.db.MaterializedViewI#getDisplaySearch(org.nrg.xdat.security.UserI)
	 */
	@Override
	public DisplaySearch getDisplaySearch(UserI user)throws Exception{
		XFTItem item = XFTItem.PopulateItemFromFlatString(this.getSearch_xml(),user,true);
		XdatStoredSearch search = new XdatStoredSearch(item);
		
		return search.getDisplaySearch(user);
	}
	
	public static String buildComparison(String requestedKey, Object v){
		List<String> values= XftStringUtils.CommaDelimitedStringToArrayList(v.toString());
		List<String> validValues=Lists.newArrayList();
		String clause="";
		String key = DisplayFieldAliasCache.getAlias(requestedKey);

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
	
	private static void validateColumns(Collection<String> columns, MaterializedViewI mv) throws Exception{
		List<String> all_columns=mv.getColumnNames();
		List<String> badColumns = new ArrayList<String>();
		for(String column:columns){
			if(!all_columns.contains(column)){
                badColumns.add(column);
			}
		}
        if (badColumns.size() > 0) {
            throw new Exception("Invalid column in request: " + StringUtils.join(badColumns, ", "));
        }
	}

}
