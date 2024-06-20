package org.nrg.xnat.initialization.tasks;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.DatabaseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.db.PostgreSQLDatabaseMetrics;
import org.apache.commons.dbcp2.BasicDataSource;
import javax.sql.DataSource;


@Component
@Slf4j
public class SetupDatabaseMetrics extends AbstractInitializingTask {

    @Autowired
    public SetupDatabaseMetrics(final JdbcTemplate jdbcTemplate, final MeterRegistry meterRegistry) {
        this.jdbcTemplate = jdbcTemplate;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public String getTaskName() {
        return "Instrument database metrics";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            log.error("No data source found for instrument database metrics");
            return;
        }
        final String uri = getUri(dataSource);
        String dbName = getDatabaseName(uri);
        if (dbName == null) {
            log.error("No database name found for instrument database metrics");
            return;
        }

        log.info("Using {} for PostgesSQLDatabaseMetrics" , dbName);
        new PostgreSQLDatabaseMetrics(dataSource,dbName).bindTo(meterRegistry);
    }

    private String getUri(DataSource dataSource) {
        if (dataSource instanceof HikariDataSource) {
            return ((HikariDataSource)dataSource).getJdbcUrl();
        } else if (dataSource instanceof BasicDataSource) {
            return ((BasicDataSource)dataSource).getUrl();
        } else {
            return null;
        }
    }

    private String getDatabaseName(String uri) {
        if (uri == null) {
            return null;
        }
        String dbName = null;
        int lastIndexOfSlash = uri.lastIndexOf("/");
        if (lastIndexOfSlash == -1) {
            dbName = uri;
        } else {
            dbName = uri.substring(lastIndexOfSlash);
            if (dbName.startsWith("/")) {
                dbName = dbName.substring(1);
            }
        }
        return dbName;
    }

    private final JdbcTemplate jdbcTemplate;
    private final MeterRegistry meterRegistry;
}
