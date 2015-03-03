// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.db;

import java.sql.SQLException;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.StringUtils;

public class ItemAccessHistory {
	static org.apache.log4j.Logger logger = Logger.getLogger(ItemAccessHistory.class);
	public final static String TABLE_NAME="xs_item_access";
	private static boolean EXISTS=false;
	
	private String search_value;
	private String search_element;
	private String search_field;
	private String method;
	private Integer xdat_user_id;
	private UserI user;
	
	

	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		PoolDBUtils.CheckSpecialSQLChars(method);
		this.method = method;
	}

	public String getSearch_element() {
		return search_element;
	}

	public void setSearch_element(String search_element) {
		PoolDBUtils.CheckSpecialSQLChars(search_element);
		this.search_element = search_element;
	}

	public String getSearch_field() {
		return search_field;
	}

	public void setSearch_field(String search_field) {
		this.search_field = search_field;
	}

	public String getSearch_value() {
		return search_value;
	}

	public void setSearch_value(String search_value) {
		this.search_value = search_value;
	}

	public UserI getUser() {
		return user;
	}

	public void setUser(UserI user) {
		this.user = user;
	}

	public Integer getXdat_user_id() {
		return xdat_user_id;
	}

	public void setXdat_user_id(Integer xdat_user_id) {
		this.xdat_user_id = xdat_user_id;
	}
	
	public void save() throws SQLException,Exception{
		VerifyManagerExistence(user);
		
		String query = "INSERT INTO " + PoolDBUtils.search_schema_name + "." + TABLE_NAME + " (search_value,search_element,search_field,xdat_user_id,method) VALUES (";
		query +="'" +search_value + "'";
		
		query +=",'" + search_element + "'";
		
		if(search_field==null){
			query +=",NULL";
		}else{
			query +=",'" + search_field + "'";
		}
		
		query +="," + user.getID();
		
		if(method==null){
			query +=",NULL";
		}else{
			query +=",'" + method + "'";
		}
		
		query +=")";
		
		PoolDBUtils.ExecuteNonSelectQuery(query, user.getDBName(), user.getLogin());
	}

	public static void VerifyManagerExistence(UserI user){
		try {
            if (!EXISTS){
        		PoolDBUtils.CreateTempSchema(user.getDBName(), user.getLogin());

                String query ="SELECT relname FROM pg_catalog.pg_class WHERE  relname=LOWER('"+TABLE_NAME+"');";
                String exists =(String)PoolDBUtils.ReturnStatisticQuery(query, "relname", user.getDBName(), user.getLogin());

                if (exists!=null){
                	EXISTS=true;
                }else{
                    query = "CREATE TABLE " + PoolDBUtils.search_schema_name + "." + TABLE_NAME+
                    "\n("+
                    "\n  search_value VARCHAR(255),"+
                    "\n  search_element VARCHAR(255),"+
                    "\n  search_field VARCHAR(255),"+
                    "\n  accessed timestamp DEFAULT now(),"+
                    "\n  xdat_user_id VARCHAR(255),"+
                    "\n  method VARCHAR(255)"+
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
	
	public static void LogAccess(UserI user,String value, String element, String field, String method) throws SQLException,Exception{
		if(user==null || value==null || element==null){
			throw new NullPointerException();
		}
		
		
		ItemAccessHistory his = new ItemAccessHistory();
		his.setUser(user);
		his.setMethod(method);
		his.setSearch_element(element);
		his.setSearch_field(field);
		his.setSearch_value(value);
		his.save();
	}
	
	public static void LogAccess(UserI user,ItemI item,String method) throws SQLException,Exception{
		if(user==null || item==null){
			throw new NullPointerException();
		}
		long startTime = Calendar.getInstance().getTimeInMillis();
		
		ItemAccessHistory his = new ItemAccessHistory();
		his.setUser(user);
		his.setMethod(method);
		his.setSearch_element(item.getXSIType());
		SchemaElementI se = SchemaElement.GetElement(item.getXSIType());
		SchemaField sf = (SchemaField)se.getAllPrimaryKeys().get(0);
		his.setSearch_field(StringUtils.ReplaceStr(StringUtils.ReplaceStr(sf.getXMLPathString(se.getFullXMLName()),"/","."),"@","."));
		Object o = item.getProperty(sf.getId());
		his.setSearch_value(o.toString());

		his.save();
		
		if(XFT.VERBOSE)System.out.println("DB Access Log: " + (Calendar.getInstance().getTimeInMillis()-startTime) + "ms");
	}

}
