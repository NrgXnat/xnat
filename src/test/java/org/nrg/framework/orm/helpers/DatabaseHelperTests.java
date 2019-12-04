package org.nrg.framework.orm.helpers;

import lombok.Getter;
import lombok.Setter;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nrg.framework.orm.DatabaseHelper.getFunctionParameterSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = OrmTestConfiguration.class)
@Transactional(transactionManager = "transactionManager")
@Rollback
public class DatabaseHelperTests {
    @Autowired
    public void setDatabaseHelper(final DatabaseHelper helper) {
        _helper = helper;
    }

    @Test
    public void testContextLoads() {
        assertThat(_helper).isNotNull();
    }

    @Test
    @Ignore
    // This test works in IntelliJ but fails when run from Maven for some reason.
    public void testCallDatabaseFunctions() throws IOException {
        assertThat(_helper).isNotNull();
        _helper.executeScript(BasicXnatResourceLocator.getResource("classpath:org/nrg/framework/orm/helpers/create-test-tables.sql"));
        final List<Map<String, Object>> singles = _helper.callFunction("GET_DATA", getFunctionParameterSource("groupId", "single"));
        final List<Map<String, Object>> doubles = _helper.callFunction("GET_DATA", getFunctionParameterSource("groupId", "double"));
        assertThat(singles).isNotNull().isNotEmpty().hasSize(10);
        assertThat(doubles).isNotNull().isNotEmpty().hasSize(10);
        final List<TestData> singleObjects = _helper.callFunction("GET_DATA", getFunctionParameterSource("groupId", "single"), TestData.class);
        final List<TestData> doubleObjects = _helper.callFunction("GET_DATA", getFunctionParameterSource("groupId", "double"), TestData.class);
        assertThat(singleObjects).isNotNull().isNotEmpty().hasSize(10);
        assertThat(doubleObjects).isNotNull().isNotEmpty().hasSize(10);
    }

    @Getter
    @Setter
    private static class TestData {
        private int     id;
        private String  itemId;
        private boolean isActive;
    }

    private DatabaseHelper _helper;
}
