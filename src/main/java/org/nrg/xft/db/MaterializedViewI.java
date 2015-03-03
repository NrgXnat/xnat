package org.nrg.xft.db;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;

public interface MaterializedViewI {

	public abstract UserI getUser();

	public abstract void setUser(UserI user);

	public abstract Date getCreated();

	public abstract void setCreated(Date created);

	public abstract Date getLast_access();

	public abstract void setLast_access(Date last_access);

	public abstract String getSearch_id();

	public abstract void setSearch_id(String search_id);

	public abstract String getSearch_sql();

	public abstract void setSearch_sql(String search_sql);

	public abstract String getSearch_xml();

	public abstract void setSearch_xml(String search_xml);

	public abstract String getTable_name();

	public abstract void setTable_name(String table_name);

	public abstract String getTag();

	public abstract void setTag(String tag);

	public abstract String getUser_name();

	public abstract void setUser_name(String user_name);

	public abstract Long getSize() throws Exception;

	public abstract Long getSize(Map<String, Object> filters) throws Exception;

	public abstract XFTTable getData(String sortBy, Integer offset,
			Integer limit) throws Exception;

	public abstract XFTTable getData(String sortBy, Integer offset,
			Integer limit, Map<String, Object> filters) throws Exception;

	public abstract List<String> getColumnNames() throws Exception;

	public abstract XFTTable getColumnValues(String column)
			throws SQLException, Exception;

	public abstract XFTTable getColumnsValues(String column) throws Exception;

	public abstract XFTTable getColumnsValues(String column,
			Map<String, Object> filters) throws Exception;

	public abstract void delete() throws Exception;

	public abstract void save() throws Exception;

	public abstract DisplaySearch getDisplaySearch(UserI user)
			throws Exception;

}