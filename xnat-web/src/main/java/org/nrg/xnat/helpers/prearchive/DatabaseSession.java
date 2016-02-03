/*
 * org.nrg.xnat.helpers.prearchive.DatabaseSession
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 9/5/13 2:38 PM
 */
package org.nrg.xnat.helpers.prearchive;

import org.apache.commons.lang.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public enum DatabaseSession {
	// a project column is allowed to hold a null value to indicate
	// an unassigned project.
	PROJECT("project", ColType.PROJECTNAME, true){
		@Override
		public Object readSession (SessionData s) {
			return s.getProject();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setProject(o);
		}
		@Override
		public String searchSql (Object o) {
			if (null == o|| ((String)o).equals(PrearcUtils.COMMON)) {
				return this.nullSql();
			}
			else {
				return this.eqSql(this.getColumnType().typeToString(o));
			}
		}
	},
	TIMESTAMP("timestamp", ColType.VARCHAR, false){
		@Override
		public Object readSession(SessionData s) {
			return s.getTimestamp();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setTimestamp(o);
		}
	},
	
	LASTMOD("lastmod", ColType.TIMESTAMP, true){
		@Override
		public Object readSession (SessionData s) {
			return s.getLastBuiltDate();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setLastBuiltDate(o);
		}
	},
	UPLOADED("uploaded", ColType.TIMESTAMP, true){
		@Override
		public Object readSession (SessionData s) {
			return s.getUploadDate();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setUploadDate(o);
		}
	},
	SCANDATE("scan_date", ColType.TIMESTAMP, true){
		@Override
		public Object readSession (SessionData s) {
			return s.getScan_date();
		}
		@Override
		
		public void writeSession (SessionData s, Object o) {
			s.setScan_date(o);
		}
	},
	
	SCANTIME("scan_time", ColType.VARCHAR, true){
		@Override
		public Object readSession (SessionData s) {
			return s.getScan_time();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setScan_time(o);
		}
	},
	SUBJECT("subject", ColType.VARCHAR, true){
		@Override
		public Object readSession (SessionData s) {
			return s.getSubject();
		}
		@Override
		public void writeSession (SessionData s,Object o) {
			s.setSubject(o);
		}
	},
	FOLDER_NAME("folderName", ColType.VARCHAR, false){
		@Override
		public Object readSession (SessionData s){
			return s.getFolderName();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setFolderName(o);
		}
	},
	NAME("name", ColType.VARCHAR, true){
		@Override
		public Object readSession (SessionData s){
			return s.getName();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setName(o);
		}
	},
	TAG("tag", ColType.VARCHAR, true){
		@Override
		public Object readSession (SessionData s){
			return s.getTag();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setTag(o);
		}
	},
	STATUS("status", ColType.STATUS, false){
		@Override
		public Object readSession (SessionData s){
			return s.getStatus();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setStatus(o);
		}
		@Override
		public String updateSessionSql (String sess, String timestamp, String proj, Object newVal) {
			String s =  "UPDATE " + PrearcDatabase.tableWithSchema + " SET " + 
			       this.searchSql(newVal) +
			       ", " + DatabaseSession.LASTMOD.searchSql(Calendar.getInstance().getTime()) + 
			       " WHERE " + DatabaseSession.sessionSql(sess, timestamp, proj);
			return s;
		}
	}
	,
	URL("url", ColType.VARCHAR, false){
		@Override
		public Object readSession (SessionData s){
			return s.getUrl();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setUrl(o);
		}
	},
	AUTOARCHIVE ("autoarchive", ColType.VARCHAR, true) {
		@Override
		public Object readSession (SessionData s) {
			return s.getAutoArchive();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setAutoArchive(o);
		}
	},
	PREVENT_ANON ("prevent_anon", ColType.BOOL, true) {
		@Override
		public Object readSession (SessionData s) {
			return s.getPreventAnon();
	}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setPreventAnon(o);
		}
	},
	PREVENT_AUTO_COMMIT ("prevent_auto_commit", ColType.BOOL, true) {
		@Override
		public Object readSession (SessionData s) {
			return s.getPreventAutoCommit();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setPreventAutoCommit(o);
		}
	},
	SOURCE ("SOURCE", ColType.VARCHAR, true) {
		@Override
		public Object readSession (SessionData s) {
			return s.getSource();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setSource(o);
		}
	},
	VISIT ("VISIT", ColType.VARCHAR, true) {
		@Override
		public Object readSession (SessionData s) {
			return s.getVisit();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setVisit(o);
		}
	},
	PROTOCOL ("PROTOCOL", ColType.VARCHAR, true) {
		@Override
		public Object readSession (SessionData s) {
			return s.getProtocol();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setProtocol(o);
		}
	},
	TIMEZONE ("TIMEZONE", ColType.VARCHAR, true) {
		@Override
		public Object readSession (SessionData s) {
			return s.getTimeZone();
		}
		@Override
		public void writeSession (SessionData s, Object o) {
			s.setTimeZone(o);
		}
	};
	/**
	 * ColType provides a simple mapping from Java objects to java.sql.* objects   
	 * @author aditya siram
	 *
	 */
	private enum ColType {
		PROJECTNAME () {
			@Override
			public void setInsertStatement(int columnIndex, PreparedStatement s, Object o) throws SQLException {
				if (((String) o).equals(PrearcUtils.COMMON)) {
					ColType.VARCHAR.setInsertStatement(columnIndex, s, null);	
				}
				else {
					ColType.VARCHAR.setInsertStatement(columnIndex, s, o);	
				}
				
			}
			@Override
			@SuppressWarnings("unchecked")
			public <T extends Object> T getFromResult(int columnIndex, ResultSet r) throws SQLException {
				Object res = ColType.VARCHAR.getFromResult(columnIndex, r);
				if (null == res) {
					return (T) PrearcUtils.COMMON;
				}
				else {
					return (T) res;
				}
			}
			@Override
			public String resultToString (int columnIndex, ResultSet r) throws SQLException {
				return ColType.VARCHAR.resultToString(columnIndex,r);
			}
			@Override
			public String toString () {
				return ColType.VARCHAR.toString();
			}
			@Override
			public String typeToString (Object o) {
				return ColType.VARCHAR.typeToString(o);
			}
		},
		STATUS () {
			@Override
			public void setInsertStatement(int columnIndex, PreparedStatement s, Object o) throws SQLException {
				if (o != null) {
					s.setString(columnIndex, ((PrearcUtils.PrearcStatus)o).name());
				}
				else {
					s.setNull(columnIndex, java.sql.Types.VARCHAR);
				}
			}
			@Override
			@SuppressWarnings("unchecked")
			public <T extends Object> T getFromResult(int columnIndex, ResultSet r) throws SQLException {
				return (T) PrearcUtils.PrearcStatus.valueOf(r.getString(columnIndex));
			}
			@Override
			public String typeToString (Object o) {
				return null == o ? null : DatabaseSession.singleQuote(((PrearcUtils.PrearcStatus)o).name());
			}
			@Override
			public String resultToString (int columnIndex, ResultSet r) throws SQLException {
				return ColType.VARCHAR.resultToString(columnIndex, r);
			}
			@Override
			public String toString() {
				return ColType.VARCHAR.toString();
			}
			
		},
		VARCHAR () {
			@Override
			public void setInsertStatement(int columnIndex, PreparedStatement s, Object o) throws SQLException {
				if (o != null) {
					s.setString(columnIndex, o.toString());
				}
				else {
					s.setNull(columnIndex, java.sql.Types.VARCHAR);
				}
			}
			@Override
			@SuppressWarnings("unchecked")
			public <T> T getFromResult(int columnIndex, ResultSet r) throws SQLException {
				return (T) r.getString(columnIndex);
			}
			@Override
			public String typeToString(Object o) {
				return null == o ? null : DatabaseSession.singleQuote(o.toString());
			} 
			@Override
			public String resultToString (int columnIndex, ResultSet r) throws SQLException {
				return this.typeToString(r.getString(columnIndex));
			}
		},
		TIMESTAMP () {
			@Override
			public void setInsertStatement(int columnIndex, PreparedStatement s, Object o) throws SQLException {
				if (o != null) {
					if (o instanceof java.util.Date) {
						java.sql.Timestamp t = new java.sql.Timestamp(((java.util.Date)o).getTime());
						s.setTimestamp(columnIndex, t);}
					else {
						s.setTimestamp(columnIndex, this.date2Timestamp(o));
					}
				}
				else {
					s.setNull(columnIndex, java.sql.Types.TIMESTAMP);
				}
			}

			public java.sql.Timestamp date2Timestamp (Object o) {
				java.util.Date d = (java.util.Date) o;
				return new java.sql.Timestamp(d.getTime());
			}
			@Override
			@SuppressWarnings("unchecked")
			public <T> T getFromResult(int columnIndex, ResultSet r) throws SQLException {
				return (T) r.getTimestamp(columnIndex);
			}
			@Override
			public String typeToString (Object o) {
				return null == o ? null : DatabaseSession.singleQuote(this.date2Timestamp(o).toString());
			}
			@Override
			public String resultToString (int columnIndex, ResultSet r) throws SQLException {
				return this.typeToString(r.getTimestamp(columnIndex));
			}
		}, 
		BOOL {
			@Override
			public void setInsertStatement(int columnIndex,
					PreparedStatement s, Object o) throws SQLException {
				if (o != null) {
					if (o instanceof Boolean) {
						s.setBoolean(columnIndex, (Boolean)o);
					}
				}
				else {
					s.setNull(columnIndex, java.sql.Types.BOOLEAN);
				}
				
			}

			@Override
			@SuppressWarnings("unchecked")
			public <T> T getFromResult(int columnIndex, ResultSet r)
					throws SQLException {
				Boolean res = r.getBoolean(columnIndex);
				if (r.wasNull()) {
					return null;
				} 
				else {
					return (T) res;	
				}
			}

			@Override
			public String resultToString(int columnIndex, ResultSet r)
					throws SQLException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String typeToString(Object o) {
				return null == o ? null : DatabaseSession.singleQuote(o.toString());
			}
		},
		INTEGER {
			@Override
			public void setInsertStatement(int columnIndex, PreparedStatement s, Object o) throws SQLException {
				if (o != null) {
					if (o instanceof Integer) {
						s.setInt(columnIndex, (Integer) o);
					}
				}
				else {
					s.setNull(columnIndex, Types.INTEGER);
				}

			}

			@Override
			@SuppressWarnings("unchecked")
			public <T> T getFromResult(int columnIndex, ResultSet r)
					throws SQLException {
				Integer res = r.getInt(columnIndex);
				if (r.wasNull()) {
					return null;
				}
				else {
					return (T) res;
				}
			}

			@Override
			public String resultToString(int columnIndex, ResultSet r) throws SQLException {
                String result = getFromResult(columnIndex, r);
                if (result == null) {
                    return "";
                }
                return result;
			}

			@Override
			public String typeToString(Object o) {
				return null == o ? null : DatabaseSession.singleQuote(o.toString());
			}
		};
		/**
		 * Insert the given object into the prepared statement.
		 * @param columnIndex
		 * @param s
		 * @param o
		 * @throws SQLException
		 */
		public abstract void setInsertStatement(int columnIndex, PreparedStatement s, Object o) throws SQLException;
		/**
		 * Extract this columns' data from the given row. 
		 * @param <T>
		 * @param columnIndex
		 * @param r
		 * @return
		 * @throws SQLException
		 */
		public abstract <T extends Object> T getFromResult(int columnIndex, ResultSet r) throws SQLException;
		/**
		 * Extract this columns data from the given row and convert it to a string.
		 * @param columnIndex
		 * @param r
		 * @return
		 * @throws SQLException
		 */
		public abstract String resultToString (int columnIndex, ResultSet r) throws SQLException;
		/**
		 * Convert this columns data to a string. Returns null if the given object is null.
		 * @param o
		 * @return
		 */
		public abstract String typeToString (Object o);
	}
	private String columnName;
	private DatabaseSession.ColType columnType;
	private boolean canBeNull;
	
	/**
	 * Construct the representation of a database column 
	 * @param columnName The name of the column (no spaces) 
	 * @param columnType The kind of value this column can hold
	 * @param canBeNull  Whether this column can hold a null value
	 */
	private DatabaseSession (String columnName, DatabaseSession.ColType columnType, boolean canBeNull) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.canBeNull = canBeNull;
	}
	public DatabaseSession.ColType getColumnType () {
		return this.columnType;
	}
	public String getColumnName () {
		return this.columnName;
	}
	
	

	/**
	 * Generate the <column=value> SQL
	 * eg. "SELECT ... FROM ... WHERE <column=value>"
	 */
	public String eqSql (String value) {
		return this.getColumnName() + "=" + value;
	}
	/**
	 * Generate the "column IS NULL" in eg. "SELECT ... FROM ... WHERE column IS NULL"
	 * @return
	 */
	public String nullSql () {
		return this.getColumnName() + " IS NULL";
	}
	
	/**
	 * In Postgres setting a column to NULL uses "=". Hope this is somewhat portable.
	 * @return
	 */
	public String updateToNull() {
		return this.eqSql("NULL");
	}
	
	/**
	 * Generate the <column selection> SQL in eg. "SELECT ... FROM ... WHERE <column selection>.
	 * The value to select on is pulled directly from the given Session object. 
	 * @param s
	 * @return
	 */
	public String searchSql (SessionData s) {
		return searchSql(this.readSession(s));
	}
	
	/**
	 * Generate the <column selection> SQL 
	 * eg. "SELECT ... FROM ... WHERE <column selection>
	 * @param value value of column
	 * @return
	 */
	public String searchSql (Object value) {
		String obj = this.columnType.typeToString(value);
		if (null == obj || obj.isEmpty()) {
			return this.nullSql();
		}
		else {
			return this.eqSql(obj);
		}
	}
	
	public String updateSql (Object value) {
		String obj = this.columnType.typeToString(value);
		if (null == obj || obj.isEmpty()){
			return this.updateToNull();
		}
		else {
			return this.eqSql(obj);
		}
	}
	
	/**
	 * Generate <column selection> SQL for all columns 
	 * eg. "SELECT ... FROM ... WHERE <column selection> operator <column selection> operator ..." 
	 * @param s         Session object containing values
	 * @param operator  boolean SQL operator eg. "AND", "OR" etc
	 * @return
	 */
	public static String searchSql (SessionData s, String operator) {
		ArrayList<String> ss = new ArrayList<String>();
		for (DatabaseSession d : DatabaseSession.values()){
			ss.add(d.searchSql(s));
		}
		return StringUtils.join(ss.toArray(new String[ss.size()]), operator);
	}
	
	/**
	 * Generate WHERE expression SQL for single column, multiple values query
	 * eg. "SELECT ... FROM ... WHERE column=val1 OR column=val2 OR ..."
	 * @param values list of values for this column    
	 * @return
	 */
	public String searchSql (String[]values) {
		ArrayList<String> ss = new ArrayList<String>();
		for (int i = 0; i < values.length; i++) {
			ss.add(this.searchSql(values[i]));
		}
		return StringUtils.join(ss.toArray(new String[ss.size()]), " OR ");
	}
	
	/**
	 * This column's type
	 * @return
	 */
	public String getColumnDefinition () {
		if (!this.canBeNull) {
			return this.columnType.toString() + " NOT NULL";
		}
		else {
			return this.columnType.toString();
		}
	}
	
	
	public String updateSessionSql (String sess, String timestamp, String proj, Object newVal) {
		return "UPDATE " + PrearcDatabase.tableWithSchema + " SET " + 
		       this.updateSql(newVal) + " WHERE " +
		       DatabaseSession.sessionSql(sess, timestamp, proj);
	}
	
	
	
	public String findSql (String sess, String timestamp, String proj) {
		return "SELECT " + this.columnName + " FROM " + PrearcDatabase.tableWithSchema + " WHERE " + DatabaseSession.sessionSql(sess, timestamp, proj);
	}
	
	public String findSql (final Object o) {
		return "SELECT * FROM " + PrearcDatabase.tableWithSchema + " WHERE " + this.searchSql(o);
	}
	
	/**
	 * Generate SQL to find a row that matches all the given Sessions' slots.
	 * @param s
	 * @return
	 */
	public static String findSessionSql (SessionData s) {
		return "SELECT * FROM " + PrearcDatabase.tableWithSchema + " WHERE " + DatabaseSession.searchSql(s, " AND ");			
	}
	
	/**
	 * Take a list of SQL (column = 'value') constraints and return SQL 
	 * with an appended a "SELECT ... WHERE " statement.
	 * 
	 * If there are no constraints a SQL statement that gets all the rows
	 * will be generated.
	 * 
	 * @param sql An array of strings containing SQL constraints
	 * @return A complete SQL statement constructed from the submitted constraints.
	 */
	public static String findSessionSql (String[] sql) {
		String selectAll = "SELECT * FROM " + PrearcDatabase.tableWithSchema; 
		if (sql.length == 0) {
			return selectAll;
		}
		else {
			String combinedSql = StringUtils.join(sql, " AND ");
			return selectAll + " WHERE " + combinedSql;
		}
	}
	
	/**
	 * Generate SQL to find a row where that matches the given session and project
	 * @param session The session ID on which to search
     * @param timestamp The timestamp on which to search
	 * @param project The project ID on which to search
     * @return A complete SQL statement constructed from the submitted criteria.
	 */
	public static String findSessionSql (String session, String timestamp, String project) {
		return "SELECT * FROM " + PrearcDatabase.tableWithSchema + " WHERE " + DatabaseSession.sessionSql(session,timestamp,project);
	}
		
	/**
	 * Count the number of sessions that match the given arguments.
     * @param session The session ID on which to search
     * @param timestamp The timestamp on which to search
     * @param project The project ID on which to search
     * @return A complete SQL statement constructed from the submitted criteria.
	 */
	public static String countSessionSql (String session, String timestamp, String project) {
		String statement = "SELECT COUNT(*) FROM " + PrearcDatabase.tableWithSchema + " WHERE " + DatabaseSession.sessionSql(session, timestamp, project);
		return statement;
	}
	
	public static String countSessionSql (final String sess, final String timestamp, final String proj, final String suid) {
		String s = "SELECT COUNT(*) FROM " + PrearcDatabase.tableWithSchema + " WHERE "  + DatabaseSession.sessionSql(sess, timestamp,proj) 
		           + " AND " + DatabaseSession.TAG.searchSql(suid);
		return s;
	}
	
	private static String sessionSql (String sess, String timestamp, String proj) {
		return DatabaseSession.FOLDER_NAME.searchSql(sess) + " AND " + 
               DatabaseSession.TIMESTAMP.searchSql(timestamp) + " AND " +
               DatabaseSession.PROJECT.searchSql(proj);
	}
	
	/**
	 * Generate SQL that deletes a row that matches the given session and project
	 * @param sess
	 * @param proj
	 * @return
	 */
	public static String deleteSessionSql (String sess, String timestamp, String proj) {
		return "DELETE FROM " + PrearcDatabase.tableWithSchema + " WHERE " + DatabaseSession.sessionSql(sess,timestamp,proj);
	}

    /**
     * Generates SQL that deletes all rows that do not match any of the given timestamps
     * @param usedSessionTimestamps
     * @return
     */
    public static String deleteUnusedSessionsSql (String usedSessionTimestamps) {
        return "DELETE FROM " + PrearcDatabase.tableWithSchema + " WHERE timestamp NOT IN (" + usedSessionTimestamps + ")";
    }

	/**
	 * Generate SQL that queries any rows matching the given names.
	 * This query will fail if this column is not a string.
	 * @param names
	 * @return
	 */
	public String allMatchesSql (String[]names){
		return "SELECT * FROM " + PrearcDatabase.tableWithSchema + " WHERE " + this.searchSql(names);
	}
	
	/**
	 * Return all rows from the database
	 * @return
	 */
	public static String allMatchesSql () {
		return "SELECT * FROM " + PrearcDatabase.tableWithSchema;
	}
	
	/**
	 * Single-quote all strings in the array
	 * @param ss
	 * @return
	 */
	public static String[] singleQuote (String[]ss) {
		String[] _temp = new String[ss.length];			
		for (int i = 0; i < ss.length; i++) {
			_temp[i] = DatabaseSession.singleQuote(ss[i]);
		}
		return _temp;
	}
	
	/**
	 * Single quote the given object. 
	 * @param o
	 * @return
	 */
	public static String singleQuote(Object o) {
		return o != null ? "'" + o.toString() + "'" : "null";
	}

	/**
	 * Extract the value which corresponds to this column from the given session and insert it 
	 * into the given PreparedStatement.
	 * @param st
	 * @param s
	 * @throws SQLException
	 */
	public void setInsertStatement(PreparedStatement st, SessionData s) throws SQLException {
		this.columnType.setInsertStatement(this.ordinal()+1,st,this.readSession(s));
	}
	
	/**
	 * Extract this columns value from the ResultSet. 
	 * @param <T>
	 * @param r
	 * @return
	 * @throws SQLException
	 */
	public <T extends Object> T getFromResult(ResultSet r) throws SQLException {
		return (T) this.columnType.getFromResult(this.ordinal()+1,r);
	}
	
	/**
	 * Convert this columns value to a String 
	 * @param r
	 * @return
	 * @throws SQLException
	 */
	public String resultToString(ResultSet r) throws SQLException {
		return this.columnType.resultToString(this.ordinal() + 1, r);
	}
	
	/**
	 * Sql that creates the prearchive table.
	 * @return
	 */
	public static String createTableSql () {
		StringBuilder s = new StringBuilder();
		s.append("CREATE TABLE " + PrearcDatabase.tableWithSchema + "(");
		List<String> values = new ArrayList<String>();
		for (DatabaseSession d : DatabaseSession.values()) {
			values.add(d.getColumnName() + " " + d.getColumnDefinition());
		}
		s.append(StringUtils.join(values.toArray(), ','));
		s.append(")");
		return s.toString();
	}
	

	public static String getAllRows() {
		return "SELECT * FROM " + PrearcDatabase.tableWithSchema; 
	}
	
	/**
	 * Transfer the values from a ResultSet into a SessionData object 
	 * @param r
	 * @return
	 * @throws SQLException
	 */
	public static SessionData fillSession (ResultSet r) throws SQLException {
		SessionData s = new SessionData();
		for (DatabaseSession d : DatabaseSession.values()){
			d.writeSession(s, d.getFromResult(r));
		}
		return s;
	}
	
	/**
	 * Extract this columns value from the given Session.
	 * @param s
	 * @return
	 */
	public abstract Object readSession (SessionData s);
	
	/**
	 * Cast 'o' to the right type and map add to the appropriate slot in s.
	 * @param s
	 * @param o
	 */
	public abstract void writeSession (SessionData s, Object o);

	/**
	 * Generates SQL to be used to update the prearchive status without any unnecessary SELECT statements.
	 * @param sess
	 * @param timestamp
	 * @param proj
	 * @param status
	 * @return
	 */
	public static String updateSessionStatusSQL (String sess, String timestamp, String proj, PrearcUtils.PrearcStatus status) {
		return "UPDATE " + PrearcDatabase.tableWithSchema + " SET " + 
		       DatabaseSession.STATUS.updateSql(status) + ", " + 
		       DatabaseSession.LASTMOD.updateSql(Calendar.getInstance().getTime()) + " WHERE " +
		       DatabaseSession.sessionSql(sess, timestamp, proj);
	}

	/**
	 * Generates SQL to be used to update the prearchive last modified time without any unnecessary SELECT statements.
	 * @param sess
	 * @param timestamp
	 * @param proj
	 * @return
	 */
	public static String updateSessionLastModSQL (String sess, String timestamp, String proj) {
		return "UPDATE " + PrearcDatabase.tableWithSchema + " SET " + 
		       DatabaseSession.LASTMOD.updateSql(Calendar.getInstance().getTime()) + " WHERE " +
		       DatabaseSession.sessionSql(sess, timestamp, proj);
	}
}