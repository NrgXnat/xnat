package org.nrg.xnat.initialization.tasks;

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
        String dbName = null;
        try {
            if (dataSource instanceof  BasicDataSource) {
                final String uri = ((BasicDataSource)dataSource).getUrl();
                int lastIndexOfSlash = uri.lastIndexOf("/");
                if (lastIndexOfSlash == -1) {
                    dbName = uri;
                } else {
                    dbName = uri.substring(lastIndexOfSlash);
                    if (!dbName.isEmpty() && dbName.startsWith("/")) {
                        dbName = dbName.substring(1);
                    }
                }
            }
        } catch(Exception e) {
            log.error("Unable to extract database name from the configuration property url", e);
        }
        log.info("Using {} for PostgesSQLDatabaseMetrics" , dbName );
        new PostgreSQLDatabaseMetrics(dataSource,dbName).bindTo(meterRegistry);

    }

    private final JdbcTemplate jdbcTemplate;
    private final MeterRegistry meterRegistry;

}
