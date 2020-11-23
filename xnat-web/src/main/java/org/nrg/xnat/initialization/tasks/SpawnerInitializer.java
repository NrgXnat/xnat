package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.spawner.services.SpawnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpawnerInitializer extends AbstractInitializingTask {
    @Autowired
    public SpawnerInitializer(final XnatAppInfo appInfo, final SpawnerService service) {
        _appInfo = appInfo;
        _service = service;
    }

    @Override
    public String getTaskName() {
        return "Spawner initializer";
    }

    @Override
    protected void callImpl() {
        if (_appInfo.isPrimaryNode()) {
            log.debug("This is the primary node, so I'm going to initialize the Spawner service");
            _service.initialize();
        } else {
            log.debug("This is not the primary node, so I'm not going to initialize the Spawner service");
        }
    }

    private final XnatAppInfo    _appInfo;
    private final SpawnerService _service;
}
