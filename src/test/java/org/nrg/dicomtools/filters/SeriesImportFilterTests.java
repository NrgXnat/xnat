package org.nrg.dicomtools.filters;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SeriesImportFilterTests {

    @Test
    public void testStandaloneFilterCreation() throws IOException {
        assertTrue(StringUtils.isNotBlank(_whitelistRegexFilter));
        assertTrue(StringUtils.isNotBlank(_blacklistRegexFilter));
        assertTrue(StringUtils.isNotBlank(_modalityMapFilter));

        SeriesImportFilter whitelistRegexFilter = new RegExBasedSeriesImportFilter(_whitelistRegexFilter);
        SeriesImportFilter blacklistRegexFilter = new RegExBasedSeriesImportFilter(_blacklistRegexFilter);
        SeriesImportFilter modalityMapFilter = new ModalityMapSeriesImportFilter(_modalityMapFilter);

        assertNotNull(whitelistRegexFilter);
        assertNotNull(blacklistRegexFilter);
        assertNotNull(modalityMapFilter);

        assertTrue(whitelistRegexFilter.shouldIncludeDicomObject(_t1SpinEcho));
        assertTrue(whitelistRegexFilter.shouldIncludeDicomObject(_localizer1));
        assertTrue(whitelistRegexFilter.shouldIncludeDicomObject(_localizer2));
        assertFalse(whitelistRegexFilter.shouldIncludeDicomObject(_petData));
        assertFalse(whitelistRegexFilter.shouldIncludeDicomObject(_massivePhi));
        assertTrue(blacklistRegexFilter.shouldIncludeDicomObject(_t1SpinEcho));
        assertTrue(blacklistRegexFilter.shouldIncludeDicomObject(_localizer1));
        assertFalse(blacklistRegexFilter.shouldIncludeDicomObject(_petData));
        assertFalse(blacklistRegexFilter.shouldIncludeDicomObject(_massivePhi));

        modalityMapFilter.setModality("MR");
        assertFalse(modalityMapFilter.shouldIncludeDicomObject(_burnedInAnnotation));
        assertFalse(modalityMapFilter.shouldIncludeDicomObject(_petScan));
        assertTrue(modalityMapFilter.shouldIncludeDicomObject(_mrScan));
        assertFalse(modalityMapFilter.shouldIncludeDicomObject(_mrWithPetDataScan));

        modalityMapFilter.setModality("PT");
        assertFalse(modalityMapFilter.shouldIncludeDicomObject(_burnedInAnnotation));
        assertTrue(modalityMapFilter.shouldIncludeDicomObject(_petScan));
        assertFalse(modalityMapFilter.shouldIncludeDicomObject(_mrScan));
        assertTrue(modalityMapFilter.shouldIncludeDicomObject(_mrWithPetDataScan));

        assertTrue(StringUtils.isBlank(modalityMapFilter.findModality(_burnedInAnnotation)));
        assertEquals("PT", modalityMapFilter.findModality(_petScan));
        assertEquals("MR", modalityMapFilter.findModality(_mrScan));
        assertEquals("PT", modalityMapFilter.findModality(_mrWithPetDataScan));
    }

    @Test
    public void testDicomFilterService() {
        final SeriesImportFilter whitelistRegexFilter = DicomFilterService.buildSeriesImportFilter(_whitelistRegexFilter);
        assertTrue(whitelistRegexFilter.isEnabled());
        whitelistRegexFilter.setProjectId("1");
        _service.commit(whitelistRegexFilter, "admin");
        final SeriesImportFilter blacklistRegexFilter = DicomFilterService.buildSeriesImportFilter(_blacklistRegexFilter);
        assertTrue(blacklistRegexFilter.isEnabled());
        blacklistRegexFilter.setProjectId("2");
        _service.commit(blacklistRegexFilter, "admin");
        final SeriesImportFilter modalityMapFilter = DicomFilterService.buildSeriesImportFilter(_modalityMapFilter);
        assertTrue(modalityMapFilter.isEnabled());
        _service.commit(modalityMapFilter, "admin");

        final SeriesImportFilter retrievedWhitelistRegexFilter = _service.getSeriesImportFilter("1");
        final SeriesImportFilter retrievedBlacklistRegexFilter = _service.getSeriesImportFilter("2");
        final SeriesImportFilter retrievedModalityMapFilter = _service.getSeriesImportFilter();

        assertEquals(whitelistRegexFilter, retrievedWhitelistRegexFilter);
        assertEquals(blacklistRegexFilter, retrievedBlacklistRegexFilter);
        assertEquals(modalityMapFilter, retrievedModalityMapFilter);
    }

    @Inject
    private DicomFilterService _service;

    @Value("${whitelistRegexFilter}")
    private String _whitelistRegexFilter;
    @Value("${blacklistRegexFilter}")
    private String _blacklistRegexFilter;
    @Value("${modalityMapFilter}")
    private String _modalityMapFilter;

    private final Map<String, String> _t1SpinEcho = new HashMap<String, String>() {{ put("SeriesDescription", "T1 Spin Echo"); }};
    private final Map<String, String> _localizer1 = new HashMap<String, String>() {{ put("SeriesDescription", "LOCALIZER"); }};
    private final Map<String, String> _localizer2 = new HashMap<String, String>() {{ put("SeriesDescription", "SAG LOCALIZER And Then Some"); }};
    private final Map<String, String> _petData = new HashMap<String, String>() {{ put("SeriesDescription", "PET Data"); }};
    private final Map<String, String> _massivePhi = new HashMap<String, String>() {{ put("SeriesDescription", "This is PHI as all get out"); }};
    private final Map<String, String> _burnedInAnnotation = new HashMap<String, String>() {{
        put("SeriesDescription", "T1 Spin Echo");
        put("Modality", "MR");
        put("BurnedInAnnotation", "YES");
    }};
    private final Map<String, String> _petScan = new HashMap<String, String>() {{
        put("SeriesDescription", "PET WB");
        put("Modality", "PT");
        put("BurnedInAnnotation", "NO");
    }};
    private final Map<String, String> _mrScan = new HashMap<String, String>() {{
        put("SeriesDescription", "T1 Spin Echo");
        put("Modality", "MR");
    }};
    private final Map<String, String> _mrWithPetDataScan = new HashMap<String, String>() {{
        put("SeriesDescription", "PET Data");
        put("Modality", "MR");
    }};
}
