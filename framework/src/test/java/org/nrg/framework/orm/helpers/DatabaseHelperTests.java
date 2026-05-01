package org.nrg.framework.orm.helpers;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nrg.framework.orm.DatabaseHelper.getFunctionParameterSource;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OrmTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
    @Disabled
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

        final List<Map<String, Object>> activeCounts = _helper.callFunction("get_data_active_count", new LinkedHashMap<>());
        assertThat(activeCounts).isNotNull();
    }

    @Test
    @Disabled("Requires PostgreSQL and a function named foo_bar")
    public void testFunctionExistsMethod() throws SQLException {
        assertThat(_helper).isNotNull();
        final boolean fooBarExists = _helper.functionExists("foo_bar");
        assertThat(fooBarExists).isTrue();
        final boolean barFooExists = _helper.functionExists("bar_foo");
        assertThat(barFooExists).isFalse();
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
