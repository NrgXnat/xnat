package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.task.XnatTask;
import org.nrg.framework.task.services.XnatTaskService;
import org.nrg.xnat.spawner.services.SpawnerService;
import org.nrg.xnat.task.AbstractXnatTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@XnatTask(taskId = "SpawnerInitializer", description = "Spawner Initialization Task", defaultExecutionResolver = "SingleNodeExecutionResolver", executionResolverConfigurable = true)
@Slf4j
public class SpawnerInitializer extends AbstractXnatTask {
    @Autowired
    public SpawnerInitializer(final XnatTaskService taskService, final SpawnerService service) {
        super(taskService);
        _service = service;
    }

    @Override
    protected void runTask() {
        log.debug("Spawner preferences indicates spawner namespaces should be purged and reloaded.");
        _service.initialize();
    }

    private final SpawnerService _service;
}
