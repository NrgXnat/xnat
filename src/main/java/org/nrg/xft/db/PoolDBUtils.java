//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Oct 22, 2004
 */
package org.nrg.xft.db;
import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.utils.StringUtils;

import com.google.common.collect.Lists;
/**
 * This class is in charge of performing all sql queries against a database.
 *
 * <BR><BR>  This class allows for sql processing without interation with any of
 * the java.sql classes.  It can process inserts, updates, and selects.  All connection
 * closures are handled within the methods.
 *
 * @author Tim
 */
public class PoolDBUtils {
	static org.apache.log4j.Logger logger = Logger.getLogger(PoolDBUtils.class);
	//private ResultSet rs = null;
	private Connection con = null;
	private Statement st = null;
	int queryLogSize = 90;

	/**
	 * Processes the specified query on the specified db with a pooled connection.
	 * The new Primary Key is returned using a SELECT currval() query with
	 * the 'table'_'pk'_seq.
	 * @param query
	 * @param db
	 * @param table
	 * @param pk
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public Object insertNativeItem(String query,String db,String table, String pk, String sequence) throws SQLException, Exception
	{
		Object o = null;
		ResultSet rs = null;
		try {
			if (sequence != null && !sequence.equalsIgnoreCase(""))
			{
				logger.debug("QUERY:" + query);
				executeQuery(db, query, "");
				st.execute(query);
				String newQuery = "SELECT currval('"+ sequence + "') AS " + pk;
				try {
					rs = executeQuery(db, newQuery, "");
					if (rs.first())
					{
						o = rs.getObject(pk);
					}
				} catch (SQLException e1) {
					newQuery = "SELECT currval('"+ table + "_" + pk + "_seq') AS " + pk;
					try {
						rs = executeQuery(db, newQuery, "");
						if (rs.first())
						{
							o = rs.getObject(pk);
						}
					} catch (SQLException e2) {
						newQuery = "SELECT currval('"+ table + "_" + table + "_seq') AS " + pk;
						try {
							rs = executeQuery(db, newQuery, "");
							if (rs.first())
							{
								o = rs.getObject(pk);
							}
						} catch (SQLException e3) {
						    newQuery = "SELECT pg_get_serial_sequence('"+ table + "','"+ pk + "') AS col_name";
						    try {
								rs = executeQuery(db, newQuery, "");
								if (rs.first())
								{
									String colName = rs.getObject("col_name").toString();
									newQuery = "SELECT currval('"+ colName + "') AS " + pk;
									rs = executeQuery(db, newQuery, "");
									if (rs.first())
									{
										o = rs.getObject(pk);
									}
								}
							} catch (Exception e4) {
								logger.error("POSTGRES - SEQUENCE BUG",e1);
								logger.error("POSTGRES - SEQUENCE BUG",e2);
								logger.error("POSTGRES - SEQUENCE BUG",e3);
								throw e1;
							}
						}
					}
				}
			}else{
				rs= executeQuery(db, query, "");
				String newQuery = "SELECT currval('"+ table + "_" + pk + "_seq') AS " + pk;
				try {
					rs= executeQuery(db, newQuery, "");
					if (rs.first())
					{
						o = rs.getObject(pk);
					}
				} catch (SQLException e1) {
					newQuery = "SELECT currval('"+ table + "_" + table + "_seq') AS " + pk;
					try {
						rs= executeQuery(db, newQuery, "");
						if (rs.first())
						{
							o = rs.getObject(pk);
						}
					} catch (SQLException e2) {
						logger.error("POSTGRES - SEQUENCE BUG",e1);
						logger.error("POSTGRES - SEQUENCE BUG",e2);
						throw e1;
					}
				}
			}
		} catch (SQLException e) {
			logger.error(query);
			throw e;
		} catch (DBPoolException e) {
			logger.error(query);
			throw e;
		}finally{
			closeConnection(rs);
		}

		return o;
	}
	
	public static synchronized Object GetNextID(String db,String table, String pk, String sequence) throws SQLException, Exception{
		return (new PoolDBUtils()).getNextID(db, table, pk, sequence);
	}

	public Object getNextID(String db,String table, String pk, String sequence) throws SQLException, Exception
	{
	    if(db==null)db=PoolDBUtils.getDefaultDBName();
		Object o = null;
		ResultSet rs = null;
		try {
			if (sequence != null && !sequence.equalsIgnoreCase(""))
			{
				String newQuery = "SELECT nextval('"+ sequence + "') AS " + pk;
				try {
					rs= executeQuery(db, newQuery, "");
					if (rs.first())
					{
						o = rs.getObject(pk);
					}
				} catch (SQLException e1) {
					newQuery = "SELECT nextval('"+ table + "_" + pk + "_seq') AS " + pk;
					try {
						rs= executeQuery(db, newQuery, "");
						if (rs.first())
						{
							o = rs.getObject(pk);
						}
					} catch (SQLException e2) {
						newQuery = "SELECT nextval('"+ table + "_" + table + "_seq') AS " + pk;
						try {
							rs= executeQuery(db, newQuery, "");
							if (rs.first())
							{
								o = rs.getObject(pk);
							}
						} catch (SQLException e3) {
						    newQuery = "SELECT nextval('"+ table + "','"+ pk + "') AS col_name";
						    try {
								rs= executeQuery(db, newQuery, "");
								if (rs.first())
								{
									String colName = rs.getObject("col_name").toString();
									newQuery = "SELECT nextval('"+ colName + "') AS " + pk;
									rs= executeQuery(db, newQuery, "");
									if (rs.first())
									{
										o = rs.getObject(pk);
									}
								}
							} catch (Exception e4) {
								logger.error("POSTGRES - SEQUENCE BUG",e1);
								logger.error("POSTGRES - SEQUENCE BUG",e2);
								logger.error("POSTGRES - SEQUENCE BUG",e3);
								throw e1;
							}
						}
					}
				}
			}else{
				String newQuery = "SELECT nextval('"+ table + "_" + pk + "_seq') AS " + pk;
				try {
					rs= executeQuery(db, newQuery, "");
					if (rs.first())
					{
						o = rs.getObject(pk);
					}
				} catch (SQLException e1) {
					newQuery = "SELECT nextval('"+ table + "_" + table + "_seq') AS " + pk;
					try {
						rs= executeQuery(db, newQuery, "");
						if (rs.first())
						{
							o = rs.getObject(pk);
						}
					} catch (SQLException e2) {
						logger.error("POSTGRES - SEQUENCE BUG",e1);
						logger.error("POSTGRES - SEQUENCE BUG",e2);
						throw e1;
					}
				}
			}
		} catch (SQLException e) {
			throw e;
		} catch (DBPoolException e) {
			throw e;
		}finally{
			closeConnection(rs);
		}

		return o;
	}

	private void sendBatchExec(List<String> statements,String db,String userName,int resultSetType,int resultSetConcurrency) throws SQLException, Exception{
		if(db==null)db=PoolDBUtils.getDefaultDBName();
	    Date start = Calendar.getInstance().getTime();
	    try {
            con = getConnection(db);
            try {
            	con.setAutoCommit(false);

            	st = con.createStatement(resultSetType, resultSetConcurrency);
            	st.clearBatch();
            	int c=0;
            	for (String stmt:statements)
            	{
            	    st.addBatch(stmt);
            	}

            	st.executeBatch();

            	logger.debug(getTimeDiff(start,Calendar.getInstance().getTime()) + " ms" + " (" + userName + "): " + StringUtils.ReplaceStr("BATCH","\n"," "));

            	st.clearBatch();

            	con.commit();
            }catch (SQLException e) {
                con.rollback();
                logger.error(statements.toString());
                logger.error(e.getMessage());
               throw e.getNextException();
			}finally{
			    con.setAutoCommit(true);
			}
        } catch (DBPoolException e) {
            logger.error("",e);
            throw e;
        }finally{
		    closeConnection(null);
		}
	}


	public void sendBatch(DBItemCache cache,String db,String userName) throws SQLException, Exception
	{
		cache.finalize();
		this.sendBatch(cache.getStatements(), db, userName, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    	cache.reset();
	}



	
	/**
	 * Check if the database type exists
	 * @param _class
	 * @return
	 * @throws Exception
	 */
	public static boolean checkIfTypeExists(String _class) throws Exception{
		Long count=(Long)PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) AS count FROM pg_catalog.pg_type WHERE  typname=LOWER('" + _class + "')", "count", null, null);
		return (count>0);
	}
	
	/**
	 * Check if the database class exists
	 * @param _class
	 * @return
	 * @throws Exception
	 */
	public static boolean checkIfClassExists(String _class) throws Exception{
		Long count=(Long)PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) AS count FROM pg_catalog.pg_class WHERE  relname=LOWER('" + _class + "') GROUP BY relname", "count", null, null);
		return (count>0);
	}

	/**
	 * Executes the given query with a pooled connection and closes the connection.
	 * @param query
	 * @param db
	 * @throws SQLException
	 * @throws Exception
	 */
	public void insertItem(String query,String db, String userName,DBItemCache cache) throws SQLException, Exception
	{
		cache.addStatement(query);
	}

	/**
	 * Executes the given query with a pooled connection and closes the connection.
	 * @param query
	 * @param db
	 * @throws SQLException
	 * @throws Exception
	 */
	public void updateItem(String query,String db, String userName,DBItemCache cache) throws SQLException, Exception
	{
		cache.addStatement(query);
	}

	/**
	 * Executes the given query with a pooled connection and closes the connection.
	 * @param query
	 * @param db
	 * @throws SQLException
	 * @throws Exception
	 */
	public void executeNonSelectQuery(String query,String db, String userName) throws SQLException,Exception
	{
		try {
			execute(db, query, userName);
		}catch (SQLException e) {
		    logger.error(query);
		   throw e;
	   } catch (DBPoolException e) {
		    logger.error(query);
		   throw e;
	   }finally{
		   closeConnection(null);
	   }
	}

	public static long getTimeDiff(Date start, Date end)
    {
        long time1 = start.getTime();
        long time2 = end.getTime();

        if (time1 > time2)
            return -1;

        return ((time2 - time1));
    }

	public static void ExecuteNonSelectQuery(String query,String db, String userName) throws SQLException,Exception
	{
		PoolDBUtils con = new PoolDBUtils();

		con.executeNonSelectQuery(query,db,userName);
	}

	public static void ExecuteBatch(List<String> queries,String db, String userName) throws SQLException,Exception
	{
		PoolDBUtils con = new PoolDBUtils();

		con.sendBatch(queries,db,userName, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	public static void ExecuteBatch(File sql,String db, String userName) throws SQLException,Exception
	{
		PoolDBUtils con = new PoolDBUtils();
		
		List<String> queries=Lists.newArrayList();
		Scanner scanner=new Scanner(sql).useDelimiter(";");
		while(scanner.hasNext()){
			queries.add(scanner.next());
		}

		con.sendBatch(queries,db,userName, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}
	public Object returnStatisticQuery(String query,String column,String db, String userName) throws SQLException,Exception
	{
		Object o = null;
		ResultSet rs = null;
		try {
			rs=executeQuery(db, query, userName);

			if (rs.first())
			{
				o = rs.getObject(column);
			}

		} catch (SQLException e) {
			logger.error(query);
			throw e;
		} catch (DBPoolException e) {
			logger.error(query);
			throw e;
		}finally{
			closeConnection(rs);
		}

		return o;
	}

	public static Object ReturnStatisticQuery(String query,String column,String db, String userName) throws SQLException,Exception
	{
		PoolDBUtils con = new PoolDBUtils();
		return con.returnStatisticQuery(query,column,db,userName);
	}

	private void resetConnections(){
		System.out.println("WARNING: DB CONNECTION FAILURE: Resetting all DB connections!!!!!!");
		this.con=null;
		DBPool.GetPool().resetConnections();
	}

	/**
	 * Executes the selected query and transfers the data from a ResultSet to
	 * a XFTTable.
	 * @param query
	 * @param db
	 * @return
	 * @throws SQLException
	 * @throws DBPoolException
	 */
	public XFTTable executeSelectQuery(String query,String db, String userName) throws SQLException,DBPoolException
	{
		ResultSet rs = null;
		XFTTable results = new XFTTable();

		try {
		    rs=executeQuery(db, query, userName);

			final String[] columns = new String[rs.getMetaData().getColumnCount()];
			for (int i=1;i<=columns.length;i++)
			{
				columns[i-1]= rs.getMetaData().getColumnName(i);
			}

			results.initTable(columns);

			while (rs.next())
			{
				Object [] row = new Object[columns.length];
				for (int i=1;i<=columns.length;i++)
				{
					try {
                        Object o = rs.getObject(i);
                        row[i-1]= o;
                    } catch (Exception e1) {
                        logger.error("",e1);
                    }
				}
				results.insertRow(row);
			}

			//logger.debug("AFTER XFTTable");
		}catch (SQLException e) {
			logger.error(query);
		   throw e;
	   } catch (DBPoolException e) {
			logger.error(query);
		   throw e;
	   }finally{
		   closeConnection(rs);
	   }

		return results;
	}


	private void closeConnection(ResultSet rs)
	{
		if (rs != null)
		{
			try {
				rs.close();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		closeConnection();
	}

	public void closeConnection()
	{
		if (st != null)
		{
			try {
				st.close();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		if (con != null)
		{
			try {
				con.close();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return
	 */
	private Connection getConnection(String db) throws SQLException, DBPoolException {
	    if (con == null)
		{
		    if(db==null)db=PoolDBUtils.getDefaultDBName();
			con = DBPool.GetConnection(db);
		}
		return con;
	}

//	/**
//	 * @return
//	 */
//	public ResultSet getResultSet() {
//		return rs;
//	}

	/**
	 * Returns the number of Rows in a given ResultSet
	 * @param rs
	 * @return
	 */
	public static int GetResultSetSize(ResultSet rs)
	{
		int rowCount =0;

		try {
			if (rs.last())
			{
				rowCount = rs.getRow();
				rs.beforeFirst();
			}
		} catch (SQLException e) {
			logger.error("DBUtils::GetResultSetSize",e);
			e.printStackTrace();
		}
		return rowCount;
	}

    private static boolean ITEM_CACHE_EXISTS = false;

    /**
     * @param dbName
     * @param login
     */
    public static void CreateCache(String dbName,String login){
        try {
            if (!ITEM_CACHE_EXISTS){

                String query ="SELECT relname FROM pg_catalog.pg_class WHERE  relname=LOWER('xs_item_cache');";
                String exists =(String)PoolDBUtils.ReturnStatisticQuery(query, "relname", dbName, login);

                if (exists!=null){
                    ITEM_CACHE_EXISTS=true;
                }else{
                    query = "CREATE TABLE xs_item_cache"+
                    "\n("+
                    "\n  elementName varchar(255) NOT NULL,"+
                    "\n  ids varchar(255) NOT NULL,"+
                    "\n  create_date timestamp DEFAULT now(),"+
                    "\n  contents text"+
                    "\n) "+
                    "\nWITH OIDS;";

                    PoolDBUtils.ExecuteNonSelectQuery(query, dbName, login);

                    ITEM_CACHE_EXISTS=true;
                }
            }
        } catch (SQLException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }
    }

    /**
     * @param dbName
     * @param login
     */
    public static void ClearCache(String dbName,String login){
        try {
            if (ITEM_CACHE_EXISTS){

                String query ="DELETE FROM xs_item_cache;";
                PoolDBUtils.ExecuteNonSelectQuery(query, dbName, login);
            }
        } catch (SQLException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }
    }

    /**
     * @param dbName
     * @param login
     */
    public static void ClearCache(String dbName,String login, String xsiType){
        try {
            if (ITEM_CACHE_EXISTS){

                String query ="DELETE FROM xs_item_cache WHERE elementname='" + xsiType + "';";
                PoolDBUtils.ExecuteNonSelectQuery(query, dbName, login);
            }
        } catch (SQLException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }
    }

    public static String getDefaultDBName(){
    	DBConfig config=DBPool.GetDBConfig((String)DBPool.GetPool().getDS().keySet().toArray()[0]);
    	return config.getDbIdentifier();
    }

    /**
     * @param rootElement
     * @param ids
     * @param functionQuery
     * @param functionName
     * @param login
     * @return
     */
    public static String RetrieveItemString(String rootElement, String ids,String functionQuery, String functionName, String login){
        String itemString=null;

            try {
                GenericWrapperElement e = GenericWrapperElement.GetElement(rootElement);

                CreateCache(e.getDbName(),login);

                itemString =(String)PoolDBUtils.ReturnStatisticQuery("SELECT contents FROM xs_item_cache WHERE elementName='" + rootElement + "' AND ids='" + ids + "';","contents",e.getDbName(),login);
                if (itemString==null){
                    itemString =(String)PoolDBUtils.ReturnStatisticQuery(functionQuery,functionName,e.getDbName(),login);
                    if(itemString!=null){
                    	// itemString.replaceAll("\\\\", "\\\\\\\\") is to escape backslashes in Windows paths.
                        String query = "INSERT INTO xs_item_cache (elementName,ids,contents) VALUES ('" + rootElement + "','" + ids + "','" + itemString.replaceAll("\\\\", "\\\\\\\\") + "');";
                        PoolDBUtils.ExecuteNonSelectQuery(query, e.getDbName(), login);
                    }
                }
            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            } catch (SQLException e) {
                logger.error("",e);
            } catch (DBPoolException e) {
                logger.error("",e);
            } catch (Exception e) {
                logger.error("",e);
            }finally{
            }


        return itemString;
    }

    /**
     * @param item
     * @param login
     * @throws SQLException
     * @throws DBPoolException
     */
    @SuppressWarnings("rawtypes")
	public static void PerformUpdateTrigger(XFTItem item, String login)throws SQLException,DBPoolException{
        CreateCache(item.getDBName(),login);

        Connection con = null;
        CallableStatement st = null;
        String query = null;
        try {
            con = DBPool.GetConnection(item.getDBName());
            int count =0;
            String ids = "";
            ArrayList keys = item.getGenericSchemaElement().getAllPrimaryKeys();
            Iterator keyIter = keys.iterator();
            while (keyIter.hasNext())
            {
                GenericWrapperField sf = (GenericWrapperField)keyIter.next();
                Object id = item.getProperty(sf);

                try {
	                if(id==null){
	                	id=item.getProperty(sf.getXMLPathString(item.getGenericSchemaElement().getXSIType()));
	                }
	                
	                if (count++>0)ids+=",";
					ids+=DBAction.ValueParser(id, sf,true);
				} catch (Exception e) {
					logger.error("",e);
				}
            }
            Date start = Calendar.getInstance().getTime();

            query = "SELECT update_ls_" + item.getGenericSchemaElement().getFormattedName() + "(" + ids +",NULL)";
            st = con.prepareCall(query);

            st.execute();

            logger.debug(getTimeDiff(start,Calendar.getInstance().getTime()) + " ms" + " (" + login + "): " + StringUtils.ReplaceStr(query,"\n"," "));

        } catch (ElementNotFoundException e) {
            logger.error(query);
        } catch (SQLException e) {
            logger.error(query);
            throw e;
        } catch (DBPoolException e) {
            logger.error(query);
            throw e;
        }finally{
            if (st != null)
            {
                try {
                    st.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (con != null)
            {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static boolean CUSTOM_SEARCH_LOG_EXISTS=false;
    /**
     * @param dbName
     * @param login
     */
    public static void CreateCustomSearchLog(String dbName,String login){
        try {
            if (!CUSTOM_SEARCH_LOG_EXISTS){

                String query ="SELECT relname FROM pg_catalog.pg_class WHERE  relname=LOWER('xs_custom_searches');";
                String exists =(String)PoolDBUtils.ReturnStatisticQuery(query, "relname", dbName, login);

                if (exists!=null){
                    CUSTOM_SEARCH_LOG_EXISTS=true;
                }else{
                    query = "CREATE TABLE xs_custom_searches"+
                    "\n("+
                    "\n  id serial,"+
                    "\n  create_date timestamp DEFAULT now(),"+
                    "\n  username VARCHAR(255),"+
                    "\n  search_xml text"+
                    "\n) "+
                    "\nWITH OIDS;";

                    PoolDBUtils.ExecuteNonSelectQuery(query, dbName, login);

                    CUSTOM_SEARCH_LOG_EXISTS=true;
                }
            }
        } catch (SQLException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }
    }

    /**
     * @param login
     * @param search_xml
     * @param dbname
     * @return
     */
    public static Object LogCustomSearch(String login,String search_xml, String dbname)throws SQLException,DBPoolException,Exception{
        CreateCustomSearchLog(dbname, login);
        Object o = PoolDBUtils.ReturnStatisticQuery("SELECT nextval('xs_custom_searches_id_seq');", "nextval", dbname, login);
        String query = "INSERT INTO xs_custom_searches (id,username,search_xml) VALUES (" + StringUtils.CleanForSQLValue(o.toString()) + ",'" + login + "','" + StringUtils.CleanForSQLValue(search_xml) + "');";
        PoolDBUtils.ExecuteNonSelectQuery(query, dbname, login);

        return o;
    }

    /**
     * @param login
     * @param search_xml
     * @param dbname
     * @return
     */
    public static String RetrieveLoggedCustomSearch(String login,String dbname,Object search_id)throws SQLException,DBPoolException,Exception{
        CreateCustomSearchLog(dbname, login);
        return (String)PoolDBUtils.ReturnStatisticQuery("SELECT search_xml FROM xs_custom_searches WHERE id=" + StringUtils.CleanForSQLValue(search_id.toString()) + ";","search_xml",dbname,login);
    }

    public static final String search_schema_name="xdat_search";

    private static boolean TEMP_SCHEMA_EXISTS=false;
    /**
     * @param dbName
     * @param login
     */
    public static void CreateTempSchema(String dbName,String login){
        try {
            if (!TEMP_SCHEMA_EXISTS){

                String query ="SELECT nspname FROM pg_namespace WHERE has_schema_privilege(nspname, 'USAGE') AND nspname='" + search_schema_name +"';";
                String exists =(String)PoolDBUtils.ReturnStatisticQuery(query, "nspname", dbName, login);

                if (exists!=null){
                    TEMP_SCHEMA_EXISTS=true;
                }else{
                    query = "CREATE SCHEMA " + search_schema_name +";";

                    PoolDBUtils.ExecuteNonSelectQuery(query, dbName, login);

                    TEMP_SCHEMA_EXISTS=true;
                }
            }
        } catch (SQLException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }
    }

    public static Long CreateManagedTempTable(String tablename, String query,XDATUser user) throws Exception{
        MaterializedView mv = new MaterializedView(user);
        mv.setTable_name(tablename);
        mv.setSearch_sql(query);
        mv.save();

        return mv.getSize();
    }

    public static XFTTable RetrieveManagedTempTable(String tablename,XDATUser user,int offset, int rowsPerPage) throws Exception{
        MaterializedView mv = MaterializedView.GetMaterializedView(tablename, user);
        if(mv==null){
        	return null;
        }else{
        	return mv.getData(null, offset, rowsPerPage);
        }
    }

    public static boolean HackCheck(String value)
    {
    	if(value.matches("[a-zA-z0-9 _\\.]*")){
    		return false;
    	}
    	
    	value=value.toUpperCase();
    	
    	if(value.matches("<*SCRIPT"))return true;
    	if(StringContains(value,"SELECT")) return true;
    	if(StringContains(value,"INSERT")) return true;
    	if(StringContains(value,"UPDATE")) return true;
    	if(StringContains(value,"DELETE")) return true;
    	if(StringContains(value,"DROP")) return true;
    	if(StringContains(value,"ALTER")) return true;
    	if(StringContains(value,"CREATE")) return true;

    	return false;
    }

    public static boolean StringContains(String value, String s){
    	if(value.contains(s+' ')){
    		if(value.startsWith(s +' ')) return true;
    		if(value.contains(' ' + s +' ')) return true;
    		if(value.contains('(' + s +' ')) return true;
    		if(value.contains('[' + s +' ')) return true;
    		if(value.contains('\'' + s +' ')) return true;
    		if(value.contains('\n' + s +' ')) return true;
    		if(value.contains('\t' + s +' ')) return true;
    	}
    		return false;
    }
    
    public static void CheckSpecialSQLChars(final String s){
    	if(s==null)return;
    	
		if(s.contains("'")){
			throw new IllegalArgumentException(s);
		}
    }

    private Statement getStatement(String db) throws DBPoolException,SQLException{
    	return getConnection(db).createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    }
    
    public PreparedStatement getPreparedStatement(String db, String sql) throws SQLException, DBPoolException {
    	if(db==null)db=PoolDBUtils.getDefaultDBName();
    	return getConnection(db).prepareStatement(sql);
    }

    public ResultSet executeQuery(String db, String query, String userName) throws SQLException, DBPoolException{
    	ResultSet rs;

    	if(db==null)db=PoolDBUtils.getDefaultDBName();

		st = getStatement(db);
		final Date start = Calendar.getInstance().getTime();

		try {
			rs = st.executeQuery(query);
		} catch (SQLException e) {
			if(e.getMessage().contains("Connection reset")){
				closeConnection();
				resetConnections();
				st = getStatement(db);
				rs = st.executeQuery(query);
			}else{
				throw e;
			}
		}

		logger.debug(getTimeDiff(start,Calendar.getInstance().getTime()) + " ms" + " (" + userName + "): " + StringUtils.ReplaceStr(query,"\n"," "));

		return rs;
    }

    private void execute(String db, String query, String userName) throws SQLException, DBPoolException{
    	if(db==null)db=PoolDBUtils.getDefaultDBName();

		st = getStatement(db);
		final Date start = Calendar.getInstance().getTime();

		try {
			st.execute(query);
		} catch (SQLException e) {
			if(e.getMessage().contains("Connection reset")){
				closeConnection();
				resetConnections();
				st = getStatement(db);
				st.execute(query);
			}else{
				throw e;
			}
		}
		
		if(getTimeDiff(start,Calendar.getInstance().getTime())>1000){
			logger.error(getTimeDiff(start,Calendar.getInstance().getTime()) + " ms" + " (" + userName + "): " + StringUtils.ReplaceStr(query,"\n"," "));
		}

		logger.debug(getTimeDiff(start,Calendar.getInstance().getTime()) + " ms" + " (" + userName + "): " + StringUtils.ReplaceStr(query,"\n"," "));
    }

	public void sendBatch(List<String> statements,String db,String userName,int resultSetType,int resultSetConcurrency) throws SQLException, Exception{
		try{
			sendBatchExec(statements,db,userName,resultSetType,resultSetConcurrency);
		}catch(SQLException e){
			if(e.getMessage().contains("Connection reset")){
				sendBatchExec(statements,db,userName,resultSetType,resultSetConcurrency);
			}else{
				logger.error("",e);
				throw e;
			}
		}
	}

	public static Transaction getTransaction(){
		return new Transaction();
	}
	
	
	/**
	 * @author tim@deck5consulting.com
	 *
	 * The transaction class is used to process db transactions which cannot be passed as a batch (include SELECTs).
	 * It maintains a single open connection (locked) until the close method is called.
	 */
	public static class Transaction {	
		PoolDBUtils pooledConnection=new PoolDBUtils();//pooled connection manager
		Connection con;
		Statement st;
		
		public void start() throws SQLException, DBPoolException{
			con=pooledConnection.getConnection(PoolDBUtils.getDefaultDBName());
	    	con.setAutoCommit(false);
	    	
	    	st=pooledConnection.getStatement(PoolDBUtils.getDefaultDBName());
		}
		
		public void execute(String query) throws SQLException{
			st.execute(query);
		}
		
		public void execute(Collection<String> stmts) throws SQLException{
			for(String s:stmts){
				st.execute(s);
			}
		}
		
		public void commit() throws SQLException{
	    	con.commit();
		}
		
		public void rollback() throws SQLException{
			con.rollback();
		}
		
		public void close() {
			try {
				con.setAutoCommit(true);//reset pooled connection to auto-commit for next consumer
			} catch (SQLException e) {}
			
	    	pooledConnection.closeConnection(null);//use the pool manager to close the connection
		}
	}
}

