package org.nrg.xnat.config;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.mockito.Mockito;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xnat.preferences.FileStorePreferences;
import org.nrg.xnat.services.archive.FileStore;
import org.nrg.xnat.services.archive.impl.hibernate.FileStoreInfoDAO;
import org.nrg.xnat.services.archive.impl.hibernate.HibernateFileStoreService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(OrmTestConfiguration.class)
public class TestFileStoreConfig {
    @Bean
    public HibernateEntityPackageList fileStoreEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.xnat.entities");
    }

    @Bean
    public NrgPreferenceService nrgPreferenceService() {
        return Mockito.mock(NrgPreferenceService.class);
    }

    @Bean
    public FileStoreInfoDAO fileStoreInfoDAO() {
        return new FileStoreInfoDAO();
    }

    @Bean
    public FileStorePreferences fileStorePreferences() throws IOException {
        final Path temp = Files.createTempDirectory("fileStore-");
        temp.toFile().deleteOnExit();
        final FileStorePreferences preferences = Mockito.mock(FileStorePreferences.class);
        when(preferences.getFileStorePath()).thenReturn(temp.toString());
        return preferences;
    }

    @Bean
    public FileStore fileStore() throws IOException {
        return new HibernateFileStoreService(fileStorePreferences());
    }
}
