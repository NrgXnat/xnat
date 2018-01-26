package org.nrg.xnat.task;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.task.XnatTaskI;
import org.nrg.framework.task.services.XnatTaskService;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.services.logging.impl.DefaultLoggingService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;

/**
 * The Class AbstractXnatTask.
 */
@Slf4j
public abstract class AbstractXnatTask extends AbstractXnatRunnable implements XnatTaskI {
    /**
     * Instantiates a new abstract XNAT task. This calls the {@link #AbstractXnatTask(XnatTaskService, boolean, XnatAppInfo, JdbcTemplate)}
     * method, passing false for the waitForInitialization flag and null for the appInfo and template parameters.
     *
     * @param taskService The task service
     */
    @SuppressWarnings("unused")
    public AbstractXnatTask(final XnatTaskService taskService) {
        this(taskService, false, null, null);
    }

    /**
     * Instantiates a new abstract XNAT task. The waitForInitialization flag indicates whether the task should wait for XNAT
     * to fully initialize before executing. This requires the appInfo and template parameters be provided if set to true.
     * You can pass null for these parameters if waitForInitialization is set to false.
     *
     * @param taskService           The task service
     * @param waitForInitialization Indicates whether the task should wait for XNAT to fully initialize before executing
     * @param appInfo               The XNAT application info object
     * @param template              JDBC template for database access
     */
    public AbstractXnatTask(final XnatTaskService taskService, final boolean waitForInitialization, final XnatAppInfo appInfo, final JdbcTemplate template) {
        if (waitForInitialization && (appInfo == null || template == null)) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You must provide the XnatAppInfo and JdbcTemplate beans when setting waitForInitialization flag to true.");
        }
        _taskService = taskService;
        _waitForInitialization = waitForInitialization;
        _appInfo = appInfo;
        _helper = new DatabaseHelper(template);
    }

    /**
     * Executes the main function of the task.
     */
    protected abstract void runTask();

    /**
     * Checks whether the task should be run. If so, this calls the {@link #runTask()} method,
     * which implements the primary functionality for the task.
     */
    @Override
    public void run() {
        DefaultLoggingService.start(this);

        try {
            if (shouldRunTask()) {
                runTask();
            }
        } finally {
            DefaultLoggingService.finish(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldRunTask() {
        log.debug("Checking whether task {} should run on this node, starting with wait for initialization check", getClass().getName());
        if (_waitForInitialization) {
            try {
                if (!_appInfo.isInitialized() || !_helper.tableExists("xdat_search", "prearchive") || !XFTManager.isInitialized()) {
                    if (!_markedUninitialized) {
                        log.info("XNAT is not yet initialized, {} task delayed until initialization completed.", getClass().getName());
                        _markedUninitialized = true;
                    }
                    return false;
                }
            } catch (final SQLException e) {
                log.error("An error occurred trying to access the database while checking for system initialization for the task " + getClass().getName(), e);
                return false;
            }
        }

        log.debug("Passed initialization check for task {}, checking for task service", getClass().getName());

        if (_taskService == null) {
            log.warn("XnatTaskService not initialized.  Could not run task {}", getClass().getName());
            return false;
        }

        log.debug("Found task service for task {}, checking for should run task", getClass().getName());

        if (_taskService.shouldRunTask(getClass())) {
            log.debug("Found should run for task {}, recording run and returning true", getClass().getName());
            recordTaskRun();
            return true;
        }

        log.debug("Task {} not set to run on this node.", getClass().getName());
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordTaskRun() {
        if (_taskService == null) {
            log.warn("XnatTaskService not initialized.  Could not record task run.");
        } else {
            _taskService.recordTaskRun(getClass());
        }
    }

    /**
     * The task service.
     */
    private final XnatTaskService _taskService;
    private final boolean         _waitForInitialization;
    private final XnatAppInfo     _appInfo;
    private final DatabaseHelper  _helper;

    private boolean _markedUninitialized = false;
}
