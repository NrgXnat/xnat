package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xnat.customforms.service.FormDisplayFieldService;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * The purpose of this task is to go over the list of all Site wide and Project Specific enabled forms
 * and create display fields for each of the form elements
 */

@Component
@Slf4j
public class DynamicFormDisplayInitializationTask extends AbstractInitializingTask {

    @Autowired
    public DynamicFormDisplayInitializationTask(final XnatAppInfo appInfo,
                                                final JdbcTemplate jdbcTemplate,
                                                final FormDisplayFieldService formDisplayFieldService) {
        this.appInfo               = appInfo;
        this.databaseHelper        = new DatabaseHelper(jdbcTemplate);
        this.formDisplayFieldService = formDisplayFieldService;
    }

    @Override
    public String getTaskName() {
        return "Initialize all display fields for all custom forms";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        try {
               if (!appInfo.isInitialized() || !databaseHelper.tableExists("xdat_element_security")) {
                       throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
               }

        } catch (SQLException e) {
           throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to access the database to check for the table and column 'xdat_element_security'.", e);
       }
        formDisplayFieldService.refreshDisplayFields();
    }

    private final XnatAppInfo    appInfo;
    private final DatabaseHelper databaseHelper;
    private final FormDisplayFieldService formDisplayFieldService;

}