package org.nrg.xnat.initialization.tasks;

import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xnat.services.XnatAppInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class CreateOrUpdateDatabaseViews extends AbstractInitializingTask {
    @Autowired
    public CreateOrUpdateDatabaseViews(final XnatAppInfo appInfo, final JdbcTemplate template) {
        _appInfo = appInfo;
        _helper = new DatabaseHelper(template);
    }

    @Override
    public String getTaskName() {
        return "Create or update database views";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        if (_appInfo.isPrimaryNode()) {
            _log.info("This service is the primary XNAT node, checking whether database updates are required.");
            try {
                if (!_helper.tableExists("xdat_search", "xs_item_access")) {
                    throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "The table 'xdat_search.xs_item_access' does not yet exist. Deferring execution.");
                }
            } catch (SQLException e) {
                throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to access the database to check for the table 'xdat_search.xs_item_access'.", e);
            }

            final PoolDBUtils.Transaction transaction = PoolDBUtils.getTransaction();
            try {
                try {
                    transaction.start();
                } catch (SQLException | DBPoolException e) {
                    throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to start the transaction.", e);
                }

                //create the views defined in the display documents
                _log.info("Initializing database views...");
                try {
                    transaction.execute(DisplayManager.GetCreateViewsSQL().get(0));
                } catch (Exception e) {
                    transaction.execute(DisplayManager.GetCreateViewsSQL().get(1));//drop all
                    transaction.execute(DisplayManager.GetCreateViewsSQL().get(0));//then try to create all
                }
                try {
                    transaction.commit();
                } catch (SQLException e) {
                    transaction.rollback();
                    throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to commit the transaction.", e);
                }
            } catch (SQLException e) {
                throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to roll back the transaction.", e);
            } finally {
                transaction.close();
            }
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(CreateOrUpdateDatabaseViews.class);

    private final XnatAppInfo    _appInfo;
    private final DatabaseHelper _helper;
}
