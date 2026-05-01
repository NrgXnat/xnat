/*
 * dicomtools: org.nrg.dicomtools.filters.SeriesImportFilterTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.filters;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.dicomtools.configuration.SeriesImportFilterTestsConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.dcm4che2.data.Tag.*;
import static org.dcm4che2.data.VR.CS;
import static org.dcm4che2.data.VR.LO;
import static org.junit.Assert.*;
import static org.nrg.dicomtools.filters.SeriesImportFilter.KEY_LIST;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SeriesImportFilterTestsConfiguration.class)
public class SeriesImportFilterTests {
    @Test
    public void testStandaloneFilterCreation() throws IOException {
        assertTrue(StringUtils.isNotBlank(_whitelistRegexFilter));
        assertTrue(StringUtils.isNotBlank(_whitelistWithTagNamesRegexFilter));
        assertTrue(StringUtils.isNotBlank(_blacklistRegexFilter));
        assertTrue(StringUtils.isNotBlank(_blacklistWithTagNamesRegexFilter));
        assertTrue(StringUtils.isNotBlank(_modalityMapFilter));

        SeriesImportFilter whitelistRegexFilter             = new RegExBasedSeriesImportFilter(_whitelistRegexFilter);
        SeriesImportFilter whitelistWithTagNamesRegexFilter = new RegExBasedSeriesImportFilter(_whitelistWithTagNamesRegexFilter);
        SeriesImportFilter blacklistRegexFilter             = new RegExBasedSeriesImportFilter(_blacklistRegexFilter);
        SeriesImportFilter blacklistWithTagNamesRegexFilter = new RegExBasedSeriesImportFilter(_blacklistWithTagNamesRegexFilter);
        SeriesImportFilter modalityMapFilter                = new ModalityMapSeriesImportFilter(_modalityMapFilter);

        assertNotNull(whitelistRegexFilter);
        assertNotNull(whitelistWithTagNamesRegexFilter);
        assertNotNull(blacklistRegexFilter);
        assertNotNull(blacklistWithTagNamesRegexFilter);
        assertNotNull(modalityMapFilter);

        final List<Integer> whitelistRegexFilterTags             = whitelistRegexFilter.getFilterTags();
        final List<Integer> whitelistWithTagNamesRegexFilterTags = whitelistWithTagNamesRegexFilter.getFilterTags();
        final List<Integer> blacklistRegexFilterTags             = blacklistRegexFilter.getFilterTags();
        final List<Integer> blacklistWithTagNamesRegexFilterTags = blacklistWithTagNamesRegexFilter.getFilterTags();
        final List<Integer> modalityMapFilterTags                = modalityMapFilter.getFilterTags();

        assertThat(whitelistRegexFilterTags).isNotNull().hasSize(1).containsExactly(Tag.SeriesDescription);
        assertThat(whitelistWithTagNamesRegexFilterTags).isNotNull().hasSize(1).containsExactly(Tag.BurnedInAnnotation);
        assertThat(blacklistRegexFilterTags).isNotNull().hasSize(1).containsExactly(Tag.SeriesDescription);
        assertThat(blacklistWithTagNamesRegexFilterTags).isNotNull().hasSize(2).containsExactly(Tag.ImageType, Tag.BurnedInAnnotation);
        assertThat(modalityMapFilterTags).isNotNull().hasSize(3).containsExactly(Tag.Modality, Tag.SeriesDescription, Tag.BurnedInAnnotation);

        assertTrue(whitelistRegexFilter.shouldIncludeDicomObject(_t1SpinEcho));
        assertTrue(whitelistRegexFilter.shouldIncludeDicomObject(_localizer1));
        assertTrue(whitelistRegexFilter.shouldIncludeDicomObject(_localizer2));
        assertFalse(whitelistRegexFilter.shouldIncludeDicomObject(_petData));
        assertFalse(whitelistRegexFilter.shouldIncludeDicomObject(_massivePhi));
        assertTrue(whitelistWithTagNamesRegexFilter.shouldIncludeDicomObject(_mrScan));
        assertFalse(whitelistWithTagNamesRegexFilter.shouldIncludeDicomObject(_burnedInAnnotation));
        assertTrue(blacklistRegexFilter.shouldIncludeDicomObject(_t1SpinEcho));
        assertTrue(blacklistRegexFilter.shouldIncludeDicomObject(_localizer1));
        assertFalse(blacklistRegexFilter.shouldIncludeDicomObject(_petData));
        assertFalse(blacklistRegexFilter.shouldIncludeDicomObject(_massivePhi));
        assertTrue(blacklistWithTagNamesRegexFilter.shouldIncludeDicomObject(_mrScan));
        assertTrue(blacklistWithTagNamesRegexFilter.shouldIncludeDicomObject(_mrWithImageTypeDerivedScan));
        assertFalse(blacklistWithTagNamesRegexFilter.shouldIncludeDicomObject(_mrWithImageTypePatientDataScan));
        assertFalse(blacklistWithTagNamesRegexFilter.shouldIncludeDicomObject(_burnedInAnnotation));

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
    public void testDicomFilterService() throws IOException {
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
        final ModalityMapSeriesImportFilter handRolledFilter = new ModalityMapSeriesImportFilter();
        handRolledFilter.setModalityFilter("exclude", "/^yes$/i.test('#BurnedInAnnotation#')");
        handRolledFilter.setModalityFilter("PT", "'#Modality#' == 'PT' || ('#Modality#' == 'MR' && '#SeriesDescription#' == 'PET Data')");
        handRolledFilter.setModalityFilter("MR", "'#Modality#' != 'PT' && !('#Modality#' == 'MR' && '#SeriesDescription#' == 'PET Data')");
        handRolledFilter.setProjectId("3");
        _service.commit(handRolledFilter, "admin");
        final SeriesImportFilter whitelistWithTagNamesRegexFilter = DicomFilterService.buildSeriesImportFilter(_whitelistWithTagNamesRegexFilter);
        assertTrue(whitelistWithTagNamesRegexFilter.isEnabled());
        whitelistWithTagNamesRegexFilter.setProjectId("4");
        _service.commit(whitelistWithTagNamesRegexFilter, "admin");
        final SeriesImportFilter blacklistWithTagNamesRegexFilter = DicomFilterService.buildSeriesImportFilter(_blacklistWithTagNamesRegexFilter);
        assertTrue(blacklistWithTagNamesRegexFilter.isEnabled());
        blacklistWithTagNamesRegexFilter.setProjectId("5");
        _service.commit(blacklistWithTagNamesRegexFilter, "admin");

        final SeriesImportFilter retrievedWhitelistRegexFilter             = _service.getSeriesImportFilter("1");
        final SeriesImportFilter retrievedBlacklistRegexFilter             = _service.getSeriesImportFilter("2");
        final SeriesImportFilter retrievedModalityMapFilter                = _service.getSeriesImportFilter();
        final SeriesImportFilter retrievedHandRolledFilter                 = _service.getSeriesImportFilter("3");
        final SeriesImportFilter retrievedWhitelistWithTagNamesRegexFilter = _service.getSeriesImportFilter("4");
        final SeriesImportFilter retrievedBlacklistWithTagNamesRegexFilter = _service.getSeriesImportFilter("5");

        assertEquals(whitelistRegexFilter, retrievedWhitelistRegexFilter);
        assertEquals(blacklistRegexFilter, retrievedBlacklistRegexFilter);
        assertEquals(modalityMapFilter, retrievedModalityMapFilter);
        assertEquals(handRolledFilter, retrievedHandRolledFilter);
        assertNotNull(handRolledFilter.getModalityFilter("PT"));
        assertEquals(whitelistWithTagNamesRegexFilter, retrievedWhitelistWithTagNamesRegexFilter);
        assertEquals(blacklistWithTagNamesRegexFilter, retrievedBlacklistWithTagNamesRegexFilter);
        assertEquals(whitelistWithTagNamesRegexFilter.toMap().get(KEY_LIST), retrievedWhitelistWithTagNamesRegexFilter.toMap().get(KEY_LIST));
        assertEquals(blacklistWithTagNamesRegexFilter.toMap().get(KEY_LIST), retrievedBlacklistWithTagNamesRegexFilter.toMap().get(KEY_LIST));

        final List<Integer> retrievedWhitelistRegexFilterTags             = retrievedWhitelistRegexFilter.getFilterTags();
        final List<Integer> retrievedBlacklistRegexFilterTags             = retrievedBlacklistRegexFilter.getFilterTags();
        final List<Integer> retrievedModalityMapFilterTags                = retrievedModalityMapFilter.getFilterTags();
        final List<Integer> retrievedHandRolledFilterTags                 = retrievedHandRolledFilter.getFilterTags();
        final List<Integer> retrievedWhitelistWithTagNamesRegexFilterTags = retrievedWhitelistWithTagNamesRegexFilter.getFilterTags();
        final List<Integer> retrievedBlacklistWithTagNamesRegexFilterTags = retrievedBlacklistWithTagNamesRegexFilter.getFilterTags();

        assertThat(retrievedWhitelistRegexFilterTags).isNotNull().hasSize(1).containsExactly(Tag.SeriesDescription);
        assertThat(retrievedBlacklistRegexFilterTags).isNotNull().hasSize(1).containsExactly(Tag.SeriesDescription);
        assertThat(retrievedModalityMapFilterTags).isNotNull().hasSize(3).containsExactly(Tag.Modality, Tag.SeriesDescription, Tag.BurnedInAnnotation);
        assertThat(retrievedHandRolledFilterTags).isNotNull().hasSize(3).containsExactly(Tag.Modality, Tag.SeriesDescription, Tag.BurnedInAnnotation);
        assertThat(retrievedWhitelistWithTagNamesRegexFilterTags).isNotNull().hasSize(1).containsExactly(Tag.BurnedInAnnotation);
        assertThat(retrievedBlacklistWithTagNamesRegexFilterTags).isNotNull().hasSize(2).containsExactly(Tag.ImageType, Tag.BurnedInAnnotation);

        final List<DicomObject> shouldIncludes = buildDicomObjects(SHOULD_INCLUDES);
        for (final DicomObject shouldInclude : shouldIncludes) {
            assertTrue(retrievedModalityMapFilter.shouldIncludeDicomObject(shouldInclude));
        }
        final List<DicomObject> shouldNotIncludes = buildDicomObjects(SHOULD_NOT_INCLUDES);
        for (final DicomObject shouldNotInclude : shouldNotIncludes) {
            assertFalse(retrievedModalityMapFilter.shouldIncludeDicomObject(shouldNotInclude));
        }
    }

    private List<DicomObject> buildDicomObjects(final ImmutableMap<String, List<ImmutableMap<Integer, ImmutablePair<VR, String>>>> dataSet) {
        final List<DicomObject> dicomObjects = new ArrayList<>();
        for (final String sessionModality : dataSet.keySet()) {
            for (final ImmutableMap<Integer, ImmutablePair<VR, String>> sessionMap : dataSet.get(sessionModality)) {
                final DicomObject dicomObject = new BasicDicomObject();
                for (final int tag : sessionMap.keySet()) {
                    final Pair<VR, String> value = sessionMap.get(tag);
                    dicomObject.putString(tag, value.getLeft(), value.getRight());
                }
            }
        }
        return dicomObjects;
    }

    private static final ImmutableMap<String, List<ImmutableMap<Integer, ImmutablePair<VR, String>>>> SHOULD_INCLUDES     = ImmutableMap.of("MR", Arrays.asList(ImmutableMap.of(Modality, ImmutablePair.of(CS, "MR"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "MR data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "no")),
                                                                                                                                                                ImmutableMap.of(Modality, ImmutablePair.of(CS, "PT"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "Some other PT data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "no")),
                                                                                                                                                                ImmutableMap.of(Modality, ImmutablePair.of(CS, "CT"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "CT data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "no"))),
                                                                                                                                            "PT", Arrays.asList(ImmutableMap.of(Modality, ImmutablePair.of(CS, "MR"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "MR data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "no")),
                                                                                                                                                                ImmutableMap.of(Modality, ImmutablePair.of(CS, "PT"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "Some other PT data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "no")),
                                                                                                                                                                ImmutableMap.of(Modality, ImmutablePair.of(CS, "CT"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "CT data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "no"))),
                                                                                                                                            "CT", Arrays.asList(ImmutableMap.of(Modality, ImmutablePair.of(CS, "MR"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "MR data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "no")),
                                                                                                                                                                ImmutableMap.of(Modality, ImmutablePair.of(CS, "PT"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "Some other PT data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "no")),
                                                                                                                                                                ImmutableMap.of(Modality, ImmutablePair.of(CS, "CT"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "CT data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "no"))));
    private static final ImmutableMap<String, List<ImmutableMap<Integer, ImmutablePair<VR, String>>>> SHOULD_NOT_INCLUDES = ImmutableMap.of("MR", Arrays.asList(ImmutableMap.of(Modality, ImmutablePair.of(CS, "MR"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "PET data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "no")),
                                                                                                                                                                ImmutableMap.of(Modality, ImmutablePair.of(CS, "PT"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "Some other PT data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "yes")),
                                                                                                                                                                ImmutableMap.of(Modality, ImmutablePair.of(CS, "CT"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "CT data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "yes"))),
                                                                                                                                            "PT", Arrays.asList(ImmutableMap.of(Modality, ImmutablePair.of(CS, "MR"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "MR data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "no")),
                                                                                                                                                                ImmutableMap.of(Modality, ImmutablePair.of(CS, "PT"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "Some other PT data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "yes")),
                                                                                                                                                                ImmutableMap.of(Modality, ImmutablePair.of(CS, "CT"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "CT data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "yes"))),
                                                                                                                                            "CT", Arrays.asList(ImmutableMap.of(Modality, ImmutablePair.of(CS, "MR"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "MR data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "yes")),
                                                                                                                                                                ImmutableMap.of(Modality, ImmutablePair.of(CS, "PT"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "Some other PT data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "yes")),
                                                                                                                                                                ImmutableMap.of(Modality, ImmutablePair.of(CS, "CT"),
                                                                                                                                                                                SeriesDescription, ImmutablePair.of(LO, "CT data"),
                                                                                                                                                                                BurnedInAnnotation, ImmutablePair.of(CS, "yes"))));

    @Inject
    private DicomFilterService _service;

    @Value("${whitelistRegexFilter}")
    private String _whitelistRegexFilter;
    @Value("${whitelistWithTagNamesRegexFilter}")
    private String _whitelistWithTagNamesRegexFilter;
    @Value("${blacklistRegexFilter}")
    private String _blacklistRegexFilter;
    @Value("${blacklistWithTagNamesRegexFilter}")
    private String _blacklistWithTagNamesRegexFilter;
    @Value("${modalityMapFilter}")
    private String _modalityMapFilter;

    private final Map<String, String> _t1SpinEcho                     = new HashMap<>(ImmutableMap.of("SeriesDescription", "T1 Spin Echo"));
    private final Map<String, String> _localizer1                     = new HashMap<>(ImmutableMap.of("SeriesDescription", "LOCALIZER"));
    private final Map<String, String> _localizer2                     = new HashMap<>(ImmutableMap.of("SeriesDescription", "SAG LOCALIZER And Then Some"));
    private final Map<String, String> _petData                        = new HashMap<>(ImmutableMap.of("SeriesDescription", "PET Data"));
    private final Map<String, String> _massivePhi                     = new HashMap<>(ImmutableMap.of("SeriesDescription", "This is PHI as all get out"));
    private final Map<String, String> _burnedInAnnotation             = new HashMap<>(ImmutableMap.of("SeriesDescription", "T1 Spin Echo",
                                                                                                      "Modality", "MR",
                                                                                                      "BurnedInAnnotation", "YES"));
    private final Map<String, String> _petScan                        = new HashMap<>(ImmutableMap.of("SeriesDescription", "PET WB",
                                                                                                      "Modality", "PT",
                                                                                                      "BurnedInAnnotation", "NO"));
    private final Map<String, String> _mrScan                         = new HashMap<>(ImmutableMap.of("SeriesDescription", "T1 Spin Echo",
                                                                                                      "Modality", "MR"));
    private final Map<String, String> _mrWithImageTypePatientDataScan = new HashMap<>(ImmutableMap.of("SeriesDescription", "T1 Spin Echo",
                                                                                                      "Modality", "MR",
                                                                                                      "ImageType", "Captured Patient Data"));
    private final Map<String, String> _mrWithImageTypeDerivedScan     = new HashMap<>(ImmutableMap.of("SeriesDescription", "T1 Spin Echo",
                                                                                                      "Modality", "MR",
                                                                                                      "ImageType", "DERIVED"));
    private final Map<String, String> _mrWithPetDataScan              = new HashMap<>(ImmutableMap.of("SeriesDescription", "PET Data",
                                                                                                      "Modality", "MR"));
}
