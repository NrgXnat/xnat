package org.nrg.xnat.services.archive.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.StopTagInputHandler;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dcm.id.TemplatizedDicomFileNamer;
import org.nrg.framework.configuration.SerializerConfig;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@Import(SerializerConfig.class)
@Slf4j
public class TestResourceSurveyAndMitigationHelperConfig {
    @Bean
    public NrgPreferenceService nrgPreferenceService() {
        return mock(NrgPreferenceService.class);
    }

    @Bean
    public SiteConfigPreferences siteConfigPreferences() {
        final SiteConfigPreferences mock = mock(SiteConfigPreferences.class);
        when(mock.getDicomFileNameTemplate()).thenReturn("${StudyInstanceUID}-${SeriesNumber}-${InstanceNumber}-${HashSOPClassUIDWithSOPInstanceUID}");
        return mock;
    }

    @Bean
    public DicomFileNamer dicomFileNamer() {
        return new TemplatizedDicomFileNamer(siteConfigPreferences());
    }

    @Bean
    public StopTagInputHandler stopTagInputHandler() {
        return new StopTagInputHandler(Tag.PixelData);
    }
}
