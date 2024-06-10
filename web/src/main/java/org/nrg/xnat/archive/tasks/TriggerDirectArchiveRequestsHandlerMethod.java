package org.nrg.xnat.archive.tasks;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.nrg.framework.task.services.XnatTaskService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.archive.services.DirectArchiveSessionService;
import org.nrg.xnat.event.listeners.methods.SessionXmlRebuilderHandlerMethod;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TriggerDirectArchiveRequestsHandlerMethod extends SessionXmlRebuilderHandlerMethod {

    @Autowired
    public TriggerDirectArchiveRequestsHandlerMethod(final DirectArchiveSessionService directArchiveSessionService,
                                                     final SiteConfigPreferences preferences,
                                                     final ThreadPoolTaskScheduler scheduler,
                                                     final XnatTaskService taskService,
                                                     final JmsTemplate jmsTemplate,
                                                     final XnatUserProvider primaryAdminUserProvider,
                                                     final XnatAppInfo appInfo,
                                                     final JdbcTemplate jdbcTemplate) {
        super(preferences, scheduler, taskService, jmsTemplate, primaryAdminUserProvider, appInfo, jdbcTemplate);
        this.directArchiveSessionService = directArchiveSessionService;
    }

    @Override
    @NotNull
    protected TriggerDirectArchiveRequests getTask() {
        return new TriggerDirectArchiveRequests(directArchiveSessionService,
                getTaskService(), getAppInfo(), getJdbcTemplate());
    }

    private final DirectArchiveSessionService directArchiveSessionService;
}
