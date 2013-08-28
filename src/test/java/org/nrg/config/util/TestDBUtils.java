/*
 * org.nrg.config.util.TestDBUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:30 PM
 */
package org.nrg.config.util;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.stereotype.Component;

@Component
public final class TestDBUtils {

	//used for tests that would rather have an empty DB.
	public void cleanDb(){
		try {
			Connection connection = _dataSource.getConnection();
	        Statement statement = connection.createStatement();
	        statement.execute("DELETE FROM XHBM_CONFIGURATION;");  //FYI: this will fail if the table name changes...
	        statement.execute("DELETE FROM XHBM_CONFIGURATION_DATA;");
	        statement.close();
		} catch (SQLException e){
			fail();
		}
	}
	
	public Connection getConnection() throws SQLException {
        return _dataSource.getConnection();
	}

    public int countConfigurationRows() {
    	return countRows("SELECT * FROM XHBM_CONFIGURATION");
    }
    
    public int countConfigurationDataRows() {
    	return countRows("SELECT * FROM XHBM_CONFIGURATION_DATA");
    }
    
    private int countRows(String sql) {
    	int index = 0;
    	try {
	        Connection connection = getConnection();
	        Statement statement = connection.createStatement();
	        statement.execute(sql);  
	        ResultSet results = statement.getResultSet();  
	    	
	        while(results.next()) {
	            index++;
	        }
	        results.close();
	        statement.close();
    	} catch (SQLException e){
    		fail();
    	}
    	return index;
    }
    
    @Inject
    @Named("dataSource")
    private DataSource _dataSource;
}
