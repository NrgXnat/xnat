/*
 * core: org.nrg.xft.db.MaterializedViewI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.db;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface MaterializedViewI {

    String getCode();

    UserI getUser();

	void setUser(UserI user);

	Date getCreated();

	void setCreated(Date created);

	Date getLast_access();

	void setLast_access(Date last_access);

	String getSearch_id();

	void setSearch_id(String search_id);

	String getSearch_sql();

	void setSearch_sql(String search_sql);

	String getSearch_xml();

	void setSearch_xml(String search_xml);

	String getTable_name();

	void setTable_name(String table_name);

	String getTag();

	void setTag(String tag);

	String getUser_name();

	void setUser_name(String user_name);

	Long getSize() throws Exception;

	Long getSize(Map<String, Object> filters) throws Exception;

	XFTTable getData(String sortBy, Integer offset,
			Integer limit) throws Exception;

	XFTTable getData(String sortBy, Integer offset,
			Integer limit, Map<String, Object> filters) throws Exception;

	List<String> getColumnNames() throws Exception;

	XFTTable getColumnValues(String column) throws Exception;

	XFTTable getColumnsValues(String column) throws Exception;

	XFTTable getColumnsValues(String column,
			Map<String, Object> filters) throws Exception;

	void delete() throws Exception;

	void save() throws Exception;

	DisplaySearch getDisplaySearch(UserI user) throws Exception;

	default String generateMaterializedViewName() {
		return XftStringUtils.formatPostgreSQLIdentifier((StringUtils.isNotBlank(getSearch_id()) ? "_" + XftStringUtils.cleanColumnName(getSearch_id()) : "") + "_" + XftStringUtils.cleanColumnName(getUser().getUsername()) + "_" + Calendar.getInstance().getTimeInMillis());
	}
}
