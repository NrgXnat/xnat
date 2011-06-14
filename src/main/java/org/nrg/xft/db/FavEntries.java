// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.db;

import java.sql.SQLException;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;

public class FavEntries {
	static org.apache.log4j.Logger logger = Logger.getLogger(FavEntries.class);
	public final static String TABLE_NAME="xs_fav_entries";
	private static boolean EXISTS=false;
	
	private XDATUser user=null;
	private String dataType=null;
	private String id = null;
	private Integer xdat_user_id=null;
		
	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		PoolDBUtils.CheckSpecialSQLChars(dataType);
		this.dataType = dataType;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public XDATUser getUser() {
		return user;
	}

	public void setUser(XDATUser user) {
		this.user = user;
	}

	public Integer getXdat_user_id() {
		return xdat_user_id;
	}

	public void setXdat_user_id(Integer xdat_user_id) {
		this.xdat_user_id = xdat_user_id;
	}

	public void save() throws SQLException,Exception{
		VerifyTableExistence(user);
		
		if(dataType==null || id==null || user==null){
			throw new NullPointerException();
		}
		
		if(xdat_user_id==null){
			xdat_user_id=user.getXdatUserId();
		}
		
		if(FavEntries.GetFavoriteEntries(dataType, id, user)==null){
			String query = "INSERT INTO " + PoolDBUtils.search_schema_name + "." + TABLE_NAME + " (dataType,id,xdat_user_id) VALUES (";
						query +="'" +dataType + "'";
						query +=",'" + id + "'";
						query +="," + xdat_user_id;
						query +=")";
			
			PoolDBUtils.ExecuteNonSelectQuery(query, user.getDBName(), user.getLogin());
		}
	}
	
	public void delete() throws SQLException,Exception{
		VerifyTableExistence(user);
		if(dataType==null || id==null || (user==null)){
			throw new NullPointerException();
		}
		
		if(xdat_user_id==null){
			xdat_user_id=user.getXdatUserId();
		}
		
		String query = "DELETE FROM " + PoolDBUtils.search_schema_name + "." + TABLE_NAME + "" +
				" WHERE dataType='" +dataType + "' AND id='" + id + "' AND xdat_user_id=" + 
				xdat_user_id + ";";
		
		PoolDBUtils.ExecuteNonSelectQuery(query, user.getDBName(), user.getLogin());
	}
	
	public static XFTTable GetFavoriteEntries(String dataType, XDATUser user) throws DBPoolException,SQLException{
		VerifyTableExistence(user);
		String query = "SELECT datatype,id FROM " + PoolDBUtils.search_schema_name + "." + TABLE_NAME + " WHERE" +
				" dataType='" + dataType + "' AND xdat_user_id=" + user.getXdatUserId();
		return XFTTable.Execute(query, user.getDBName(), user.getLogin());
	}
	
	public static FavEntries GetFavoriteEntries(String dataType, String id, XDATUser user) throws DBPoolException,SQLException{
		VerifyTableExistence(user);
		String query = "SELECT datatype,id FROM " + PoolDBUtils.search_schema_name + "." + TABLE_NAME + " WHERE" +
				" dataType='" + dataType + "' AND id='" + id + "' AND xdat_user_id=" + user.getXdatUserId();
		XFTTable t= XFTTable.Execute(query, user.getDBName(), user.getLogin());
		if(t.size()>0){
			Hashtable h=t.rowHashs().get(0);
			FavEntries f = new FavEntries();
			f.setId(id);
			f.setDataType(dataType);
			f.setUser(user);
			
			return f;
		}else{
			return null;
		}
	}

	public static void VerifyTableExistence(XDATUser user){
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
                    "\n  dataType VARCHAR(255),"+
                    "\n  id VARCHAR(255),"+
                    "\n  xdat_user_id integer"+
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
}
