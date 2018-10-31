/*
 * core: org.nrg.xft.db.PoolDBUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.db;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.utils.XftStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PoolDBUtils {
	/**
	 * Processes the specified query on the specified db with a pooled connection.
	 * The new Primary Key is returned using a SELECT currval() query with
	 * the 'table'_'pk'_seq.
	 * @param query
	 * @param db
	 * @param table
	 * @param pk
	 * @return Returns the currval of the sequence after running the query
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
				_statement.execute(query);
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
						    newQuery = "SELECT pg_get_serial_sequence('"+ table + "', '"+ StringUtils.lowerCase(pk) + "') AS col_name";
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

	private void sendBatchExec(List<String> statements, String db, String userName, int resultSetType, int resultSetConcurrency) throws SQLException, Exception {
		final Date start = Calendar.getInstance().getTime();
		try (final Connection connection = getConnection()) {
			try {
				connection.setAutoCommit(false);

				_statement = connection.createStatement(resultSetType, resultSetConcurrency);
				_statement.clearBatch();
				for (String stmt : statements) {
					_statement.addBatch(stmt);
				}

				_statement.executeBatch();

				logger.debug(getTimeDiff(start, Calendar.getInstance().getTime()) + " ms" + " (" + userName + "): " + StringUtils.replace("BATCH", "\n", " "));

				_statement.clearBatch();

				connection.commit();
			} catch (SQLException e) {
				connection.rollback();
				logger.error(statements.toString());
				logger.error(e.getMessage());
				throw e.getNextException();
			} finally {
				connection.setAutoCommit(true);
			}
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
	 * @return Returns whether the database type exists
	 * @throws Exception
	 */
	public static boolean checkIfTypeExists(String _class) throws Exception{
		Long count=(Long)PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) AS count FROM pg_catalog.pg_type WHERE  typname=LOWER('" + _class + "')", "count", null, null);
		return (count>0);
	}

	/**
	 * Check if the database class exists
	 * @param _class
	 * @return Returns whether the database class exists
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
	public void insertItem(String query, String db, String userName, DBItemCache cache) throws SQLException, Exception
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
            if (!StringUtils.containsAny(e.getMessage(), "relation \"xdat_user\" does not exist", "relation \"xdat_element_security\" does not exist")){
                logger.error(query);
            }
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
		this._connection =null;
	}

	/**
	 * Executes the selected query and transfers the data from a ResultSet to
	 * a XFTTable.
	 * @param query
	 * @param db
	 * @return Returns the XFTTable containing the results of the query
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
            if (!StringUtils.containsAny(e.getMessage(), "relation \"xdat_user\" does not exist", "relation \"xdat_element_security\" does not exist")) {
                logger.error(query);
            }
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
		if (_statement != null)
		{
			try {
				_statement.close();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		if (_connection != null)
		{
			try {
				_connection.close();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets a database connection from the configured data source.
	 *
	 * @return A connection to the database.
	 *
	 * @throws SQLException When an error occurs trying to get the database connection.
	 */
	private Connection getConnection() throws SQLException {
		if (_connection == null) {
			final DataSource dataSource = XDAT.getDataSource();
			if (dataSource != null) {
				_connection = dataSource.getConnection();
			} else {
				logger.warn("Couldn't load the data source to create a connection.");
			}
		}
		return _connection;
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
	 * @return Returns the number of Rows in a given ResultSet
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
    private static final Object ITEM_CACHE_MUTEX = new Object();

	/**
	 * Creates the XNAT item cache table.
	 *
	 * @param dbName The name of the database in which the cache should be created.
	 * @param login  The login name of the user creating the cache.
	 */
	public static void CreateCache(String dbName, String login) {
		try {
			synchronized (ITEM_CACHE_MUTEX) {
				if (!ITEM_CACHE_EXISTS) {
					final String exists = (String) PoolDBUtils.ReturnStatisticQuery(QUERY_ITEM_CACHE_EXISTS, "relname", dbName, login);

					if (exists != null) {
						ITEM_CACHE_EXISTS = true;
					} else {
						PoolDBUtils.ExecuteNonSelectQuery(QUERY_CREATE_ITEM_CACHE, dbName, login);
						ITEM_CACHE_EXISTS = true;
					}
				}
			}
		} catch (SQLException e) {
			logger.error("An database or SQL error occurred trying to validate or create the XNAT item cache.", e);
		} catch (Exception e) {
			logger.error("An unknown error occurred trying to create the XNAT item cache.", e);
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
		return "";
	}

    /**
     * @param rootElement
     * @param ids
     * @param functionQuery
     * @param functionName
     * @param login
     * @return Returns the item expressed as a string
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
                    	// itemString.replaceAll("\\\\", "\\\\\\\\") is to escape backslashes in Windows paths,
						// "'", "''" is to replace single quotes with escaped single quotes, e.g. "O'Connor" becomes
						// "O''Connor".
                        final String query = "INSERT INTO xs_item_cache (elementName,ids,contents) VALUES ('" + rootElement + "','" + ids + "','" + itemString.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "''") + "');";
                        PoolDBUtils.ExecuteNonSelectQuery(query, e.getDbName(), login);
                    }
                }
            } catch (Exception e) {
                logger.error("",e);
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
            con = XDAT.getDataSource().getConnection();
            int           count   =0;
            StringBuilder ids     = new StringBuilder();
            ArrayList     keys    = item.getGenericSchemaElement().getAllPrimaryKeys();
            Iterator      keyIter = keys.iterator();
            while (keyIter.hasNext())
            {
                GenericWrapperField sf = (GenericWrapperField)keyIter.next();
                Object id = item.getProperty(sf);

                try {
	                if(id==null){
	                	id=item.getProperty(sf.getXMLPathString(item.getGenericSchemaElement().getXSIType()));
	                }

	                if (count++>0) {
	                	ids.append(",");
					}
					ids.append(DBAction.ValueParser(id, sf, true));
				} catch (Exception e) {
					logger.error("",e);
				}
            }
            Date start = Calendar.getInstance().getTime();

            query = "SELECT update_ls_" + item.getGenericSchemaElement().getFormattedName() + "(" + ids +",NULL)";
            st = con.prepareCall(query);

            st.execute();

            logger.debug(getTimeDiff(start,Calendar.getInstance().getTime()) + " ms" + " (" + login + "): " + StringUtils.replace(query, "\n", " "));

        } catch (ElementNotFoundException e) {
            logger.error(query);
        } catch (SQLException e) {
            logger.error(query);
            throw e;
        } finally{
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
					String columnQuery ="SELECT TRUE AS exists FROM pg_attribute WHERE attrelid = 'xs_custom_searches'::regclass AND attname = 'random_id_string' AND NOT attisdropped;";
					Boolean columnExists =(Boolean)PoolDBUtils.ReturnStatisticQuery(columnQuery, "exists", dbName, login);

					if (columnExists==null || !columnExists){
						String addColumnQuery ="ALTER TABLE xs_custom_searches ADD COLUMN random_id_string VARCHAR(255);";
						PoolDBUtils.ExecuteNonSelectQuery(addColumnQuery, dbName, login);
					}
                    CUSTOM_SEARCH_LOG_EXISTS=true;
                }else{
                    query = "CREATE TABLE xs_custom_searches"+
                    "\n("+
                    "\n  id serial,"+
                    "\n  create_date timestamp DEFAULT now(),"+
                    "\n  username VARCHAR(255),"+
                    "\n  search_xml text,"+
					"\n  random_id_string VARCHAR(255)"+
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
     * @return Returns the ID of the newly logged custom search
     */
    public static Object LogCustomSearch(String login,String search_xml, String dbname)throws SQLException,DBPoolException,Exception{
        CreateCustomSearchLog(dbname, login);
        Object o = PoolDBUtils.ReturnStatisticQuery("SELECT nextval('xs_custom_searches_id_seq');", "nextval", dbname, login);
		Object uuid = UUID.randomUUID().toString();

		String columnQuery ="SELECT TRUE AS exists FROM pg_attribute WHERE attrelid = 'xs_custom_searches'::regclass AND attname = 'random_id_string' AND NOT attisdropped;";
		Boolean columnExists =(Boolean)PoolDBUtils.ReturnStatisticQuery(columnQuery, "exists", dbname, login);

		if (columnExists==null || !columnExists){
			String addColumnQuery ="ALTER TABLE xs_custom_searches ADD COLUMN random_id_string VARCHAR(255);";
			PoolDBUtils.ExecuteNonSelectQuery(addColumnQuery, dbname, login);
		}

		String query = "INSERT INTO xs_custom_searches (id,username,search_xml, random_id_string) VALUES (" + XftStringUtils.CleanForSQLValue(o.toString()) + ",'" + login + "','" + XftStringUtils.CleanForSQLValue(search_xml) + "','"+uuid+"');";
        PoolDBUtils.ExecuteNonSelectQuery(query, dbname, login);

        return uuid;
    }

    /**
     * @param login
     * @param dbname
     * @param search_id
     * @return Returns the logged custom search xml
     */
    public static String RetrieveLoggedCustomSearch(String login,String dbname,Object search_id)throws SQLException,DBPoolException,Exception{
        CreateCustomSearchLog(dbname, login);
        return (String)PoolDBUtils.ReturnStatisticQuery("SELECT search_xml FROM xs_custom_searches WHERE random_id_string='" + XftStringUtils.CleanForSQLValue(search_id.toString()) + "';", "search_xml", dbname, login);
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

    public static boolean HackCheck(final Iterable<String> values) {
		for (final String value : values) {
			if(HackCheck(value)) {
				return true;
			}
		}
    		return false;
    	}

    public static boolean HackCheck(String value) {
		if (value.matches("^[a-zA-z0-9 _\\.]*[^\\\\]$")) {
    	return false;
    }

		final String normalized = value.toUpperCase();

		return  normalized.matches("^.*[\\\\]$") ||
				normalized.matches("<*SCRIPT") ||
				StringContains(value, "SELECT") ||
				StringContains(value, "INSERT") ||
				StringContains(value, "UPDATE") ||
				StringContains(value, "DELETE") ||
				StringContains(value, "DROP") ||
				StringContains(value, "ALTER") ||
				StringContains(value, "CREATE");
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

    private Statement getStatement() throws DBPoolException, SQLException{
    	return getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    }

    public PreparedStatement getPreparedStatement(String db, String sql) throws SQLException, DBPoolException {
    	return getConnection().prepareStatement(sql);
    }

	public ResultSet executeQuery(String db, String query, String userName) throws SQLException, DBPoolException {
		_statement = getStatement();
		final Date start = Calendar.getInstance().getTime();

		try {
			return _statement.executeQuery(query);
		} catch (SQLException e) {
			final String message = e.getMessage();
			if (message.contains("Connection reset")) {
				closeConnection();
				resetConnections();
				_statement = getStatement();
				return _statement.executeQuery(query);
			} else if (message.matches(EXPR_COLUMN_NOT_FOUND)) {
				final Matcher matcher = PATTERN_COLUMN_NOT_FOUND.matcher(message);
				logger.error("Got an exception indicating that the column \"" + matcher.group(1) + "\" does not exist. The attempted query is:\n\n" + query);
				return null;
			} else if (StringUtils.containsAny(message, "relation \"xdat_user\" does not exist", "relation \"xdat_element_security\" does not exist")){
				// Just rethrow and let someone else handle it. This is probably because the system is initializing
				// and, if it's not, plenty else will go wrong to indicate the problem.
				throw e;
			} else {
				logger.error("An error occurred trying to execute the user " + userName + " query: " + query, e);
				throw e;
			}
		} finally {
			logger.debug(getTimeDiff(start, Calendar.getInstance().getTime()) + " ms" + " (" + userName + "): " + StringUtils.replace(query, "\n", " "));
		}
	}

	private void execute(String db, String query, String userName) throws SQLException, DBPoolException {
		_statement = getStatement();
		final Date start = Calendar.getInstance().getTime();

		try {
			_statement.execute(query);
		} catch (SQLException e) {
			final String message = e.getMessage();
			if (message.contains("relation \"xdat_meta_element_meta_data\" does not exist")) {
				// Honestly, we don't care much about this. It goes away once the metadata table is initialized,
				// has no real effect beforehand, and is a pretty scary message with an enormous stacktrace.
				logger.info("Metadata error occurred: " + message);
			} else if (message.contains("Connection reset")) {
				closeConnection();
				resetConnections();
				_statement = getStatement();
				_statement.execute(query);
			} else if (message.matches(EXPR_COLUMN_NOT_FOUND)) {
				final Matcher matcher = PATTERN_COLUMN_NOT_FOUND.matcher(message);
				logger.error("Got an exception indicating that the column \"" + matcher.group(1) + "\" does not exist. The attempted query is:\n\n" + query);
			} else if (message.matches(EXPR_TABLE_NOT_FOUND)) {
				final Matcher matcher = PATTERN_DROP_TABLE_QUERY.matcher(query);
				if (matcher.find()) {
					logger.debug("Got an exception that the table \"{}\" does not exist, but that's probably OK: it's trying to drop that table. This occurs during a race condition trying to drop a materialized view twice.", matcher.group("table"));
				} else {
					logger.error("Got an exception indicating that the table \"{}\" does not exist. The attempted query is:\n\n{}", matcher.group("table"), query);
				}
			} else {
				logger.error("An error occurred trying to execute the user " + userName + " query: " + query, e);
				throw e;
			}
		} finally {
			if (getTimeDiff(start, Calendar.getInstance().getTime()) > 1000) {
				logger.error(getTimeDiff(start, Calendar.getInstance().getTime()) + " ms" + " (" + userName + "): " + StringUtils.replace(query, "\n", " "));
			}

			logger.debug(getTimeDiff(start, Calendar.getInstance().getTime()) + " ms" + " (" + userName + "): " + StringUtils.replace(query, "\n", " "));
		}
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
			con=pooledConnection.getConnection();
	    	con.setAutoCommit(false);

	    	st=pooledConnection.getStatement();
		}

		public void execute(Collection<String> statements) throws SQLException {
			for (final String statement : statements) {
				st.execute(statement);
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
			} catch (SQLException ignored) {}

	    	pooledConnection.closeConnection(null);//use the pool manager to close the connection
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(PoolDBUtils.class);

	private static final String  EXPR_COLUMN_NOT_FOUND    = "column \"(?<column>[A-z0-9_-]+)\" does not exist";
	private static final Pattern PATTERN_COLUMN_NOT_FOUND = Pattern.compile(EXPR_COLUMN_NOT_FOUND);
	private static final String  EXPR_TABLE_NAME          = "(?<table>_[A-z0-9_]+_[\\d]+)";
	private static final String  EXPR_DROP_TABLE_QUERY    = "DROP TABLE xdat_search\\." + EXPR_TABLE_NAME;
	private static final Pattern PATTERN_DROP_TABLE_QUERY = Pattern.compile(EXPR_DROP_TABLE_QUERY);
	private static final String  EXPR_TABLE_NOT_FOUND     = "^.*table \"" + EXPR_TABLE_NAME + "\" does not exist.*$";
	private static final Pattern PATTERN_TABLE_NOT_FOUND  = Pattern.compile(EXPR_TABLE_NOT_FOUND);

	private static final String  EXPR_RELATION_NOT_FOUND    = "relation \"(?<relation>[a-z_]+)\" does not exist";
	private static final Pattern PATTERN_RELATION_NOT_FOUND = Pattern.compile(EXPR_RELATION_NOT_FOUND);
	private static final String  QUERY_ITEM_CACHE_EXISTS    = "SELECT relname FROM pg_catalog.pg_class WHERE  relname=LOWER('xs_item_cache');";
	private static final String  QUERY_CREATE_ITEM_CACHE    = "CREATE TABLE xs_item_cache" +
															  "\n(" +
															  "\n  elementName varchar(255) NOT NULL," +
															  "\n  ids varchar(255) NOT NULL," +
															  "\n  create_date timestamp DEFAULT now()," +
															  "\n  contents text" +
															  "\n) " +
															  "\nWITH OIDS;";

	private Connection _connection = null;
	private Statement  _statement  = null;
}


