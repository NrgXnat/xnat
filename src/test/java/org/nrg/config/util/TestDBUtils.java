package org.nrg.config.util;

import static org.junit.Assert.fail;

import java.sql.Connection;
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

    @Inject
    @Named("dataSource")
    private DataSource _dataSource;
}
