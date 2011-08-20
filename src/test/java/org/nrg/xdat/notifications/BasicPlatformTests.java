/**
 * BasicPlatformTests
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 18, 2011
 */
package org.nrg.xdat.notifications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xdat.notifications.api.Category;
import org.nrg.xdat.notifications.api.CategoryScope;
import org.nrg.xdat.notifications.daos.CategoryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests various platform attributes to ensure that they're working, e.g. connection
 * to the data source.
 *
 * @author rherrick
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class BasicPlatformTests {
    /**
     * This runs some basic sanity checks on the h2 data source to make
     * certain that simple database transactions are working properly
     * before moving onto more complex operations.
     * 
     * @throws SQLException
     */
    @Test
    public void testDataSource() throws SQLException {
        assertNotNull(_dataSource);
        Connection connection = _dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("DROP TABLE IF EXISTS TEST");
        statement.execute("CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))");
        statement.execute("INSERT INTO TEST VALUES(1, 'Hello')");
        statement.execute("INSERT INTO TEST VALUES(2, 'World')");
        statement.execute("SELECT * FROM TEST ORDER BY ID");
        ResultSet results = statement.getResultSet();
        int index = 1;
        while(results.next()) {
            int id = results.getInt("ID");
            String name = results.getString("NAME");
            assertEquals(index, id);
            assertEquals(index == 1 ? "Hello" : "World", name);
            index++;
        }
        statement.execute("DROP TABLE TEST");
    }

    @Test
    @Transactional
    public void testCategoryDAO() throws SQLException {
        
        Category category = new Category();
        category.setScope(CategoryScope.Site);
        category.setEvent("TestEvent");
        
        _categoryDAO.create(category);
        
        Category retrieved = _categoryDAO.retrieve(category.getId());
        
        assertEquals(category.getId(), retrieved.getId());
        assertEquals(category.getScope(), retrieved.getScope());
        assertEquals(category.getEvent(), retrieved.getEvent());
        
        category.setEvent("TestEventUpdated");
        _categoryDAO.update(category);
        
        retrieved = _categoryDAO.retrieve(category.getId());
        
        assertEquals(category.getId(), retrieved.getId());
        assertEquals(category.getScope(), retrieved.getScope());
        assertEquals(category.getEvent(), retrieved.getEvent());
        assertEquals("TestEventUpdated", retrieved.getEvent());
        
        _categoryDAO.delete(category);

        retrieved = _categoryDAO.retrieve(category.getId());
        assertNull(retrieved);
    }

    @Autowired
    private DataSource _dataSource;
    
    @Autowired
    private CategoryDAO _categoryDAO;
}
