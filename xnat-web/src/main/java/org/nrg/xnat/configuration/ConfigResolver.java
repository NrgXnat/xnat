package org.nrg.xnat.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Component
public class ConfigResolver {

    public InputStream getConfigurationFile(final String name) throws IOException {
        for (final Path configFolder : _configFolderPaths) {
            final Path configFile = configFolder.resolve(name);
            if (_log.isDebugEnabled()) {
                _log.info("Searching for the configuration file {} in the path {}.", name, configFile.toString());
            }
            if (configFile.toFile().exists()) {
                if (_log.isInfoEnabled()) {
                    _log.info("Found the configuration file {} in the path {}, returning.", name, configFile.toString());
                }
                return Files.newInputStream(configFile, StandardOpenOption.READ);
            }
        }
        if (_log.isDebugEnabled()) {
            _log.info("Found no configuration file named {} on any of the available configuration folders.");
        }
        return null;
    }

    private static final Logger _log = LoggerFactory.getLogger(ConfigResolver.class);

    @Resource(name="configFolderPaths")
    private List<Path> _configFolderPaths;
}
