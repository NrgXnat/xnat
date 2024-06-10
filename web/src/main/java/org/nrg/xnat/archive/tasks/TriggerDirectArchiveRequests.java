package org.nrg.xnat.archive.tasks;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.task.XnatTask;
import org.nrg.framework.task.services.XnatTaskService;
import org.nrg.xnat.archive.services.DirectArchiveSessionService;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.task.AbstractXnatTask;
import org.springframework.jdbc.core.JdbcTemplate;

@XnatTask(taskId = "TriggerDirectArchiveRequests", description = "Sends JMS requests for direct archiving", defaultExecutionResolver = "SingleNodeExecutionResolver")
@Slf4j
public class TriggerDirectArchiveRequests extends AbstractXnatTask {

    public TriggerDirectArchiveRequests(final DirectArchiveSessionService directArchiveSessionService,
                                        final XnatTaskService taskService,
                                        final XnatAppInfo appInfo,
                                        final JdbcTemplate jdbcTemplate) {
        super(taskService, true, appInfo, jdbcTemplate);
        this.directArchiveSessionService = directArchiveSessionService;
    }

    @Override
    protected void runTask() {
        directArchiveSessionService.triggerArchive();
    }

    private final DirectArchiveSessionService directArchiveSessionService;
}
