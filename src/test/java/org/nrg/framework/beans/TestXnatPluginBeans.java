package org.nrg.framework.beans;

import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class TestXnatPluginBeans {
    public static final String       CENTRAL_DATATYPES_PLUGIN           = "central_datatypes_plugin";
    public static final String       CNDA_PLUGIN_ADRC                   = "cnda_plugin_adrc";
    public static final String       CNDA_PLUGIN_CNDAPLUGINDEPENDENCIES = "cnda_plugin_cndaplugindependencies";
    public static final String       CNDA_PLUGIN_UDS                    = "cnda_plugin_uds";
    public static final String       CUSTOM_DOWNLOADER_PLUGIN           = "custom_downloader_plugin";
    public static final String       NRG_PLUGIN_FREESURFERCOMMON        = "nrg_plugin_freesurfercommon";
    public static final String       NRG_PLUGIN_PUP                     = "nrg_plugin_pup";
    public static final List<String> ALL_PLUGINS                        = Arrays.asList(CENTRAL_DATATYPES_PLUGIN, CNDA_PLUGIN_ADRC, CNDA_PLUGIN_CNDAPLUGINDEPENDENCIES, CNDA_PLUGIN_UDS, CUSTOM_DOWNLOADER_PLUGIN, NRG_PLUGIN_FREESURFERCOMMON, NRG_PLUGIN_PUP);
    public static final String       PROPERTY_PLUGIN_BEANS              = "pluginBeans";
    public static final String       PROPERTY_ID                        = "id";
    public static final String       PROPERTY_PLUGIN_CLASS              = "pluginClass";
    public static final String       PROPERTY_NAME                      = "name";
    public static final String       PROPERTY_BEAN_NAME                 = "beanName";
    public static final String       PROPERTY_TYPE                      = "type";
    public static final String       PROPERTY_SECURED                   = "secured";
    public static final String       PROPERTY_SINGULAR                  = "singular";
    public static final String       PROPERTY_PLURAL                    = "plural";
    public static final String       PROPERTY_CODE                      = "code";

    @Test
    public void testXnatPluginBeanManager() {
        final XnatPluginBeanManager manager = new XnatPluginBeanManager();
        assertThat(manager).isNotNull().hasFieldOrProperty(PROPERTY_PLUGIN_BEANS);
        final Map<String, XnatPluginBean> pluginBeans = manager.getPluginBeans();
        assertThat(pluginBeans).isNotNull().isNotEmpty().hasSize(7).containsOnlyKeys(ALL_PLUGINS);

        final XnatPluginBean centralDataTypes = pluginBeans.get(CENTRAL_DATATYPES_PLUGIN);
        assertThat(centralDataTypes).isNotNull()
                                    .hasFieldOrPropertyWithValue(PROPERTY_ID, CENTRAL_DATATYPES_PLUGIN)
                                    .hasFieldOrPropertyWithValue(PROPERTY_PLUGIN_CLASS, "org.nrg.xnat.central.configuration.CentralDataPlugin")
                                    .hasFieldOrPropertyWithValue(PROPERTY_NAME, "XNAT Central Data Types Plugin")
                                    .hasFieldOrPropertyWithValue(PROPERTY_BEAN_NAME, "centralDataPlugin");
        assertThat(centralDataTypes.getDataModelBeans()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(centralDataTypes.getDataModelBeans().get(0)).isNotNull()
                                                               .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "genetics:geneticTestResults")
                                                               .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                               .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "Genetic Test Result")
                                                               .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "Genetic Test Results")
                                                               .hasFieldOrPropertyWithValue(PROPERTY_CODE, "GENES");

        final XnatPluginBean cndaPluginAdrc = pluginBeans.get(CNDA_PLUGIN_ADRC);
        assertThat(cndaPluginAdrc).isNotNull()
                                  .hasFieldOrPropertyWithValue(PROPERTY_ID, CNDA_PLUGIN_ADRC)
                                  .hasFieldOrPropertyWithValue(PROPERTY_PLUGIN_CLASS, "org.nrg.xnat.plugin.AdrcPlugin")
                                  .hasFieldOrPropertyWithValue(PROPERTY_NAME, "XNAT 1.7 ADRC Plugin")
                                  .hasFieldOrPropertyWithValue(PROPERTY_BEAN_NAME, "adrcPlugin");
        assertThat(cndaPluginAdrc.getDataModelBeans()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(cndaPluginAdrc.getDataModelBeans().get(0)).isNotNull()
                                                             .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "adrc:ADRCClinicalData")
                                                             .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                             .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "ADRC Clinical Data")
                                                             .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "ADRC Clinical Data")
                                                             .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);

        final XnatPluginBean cndaPluginDependencies = pluginBeans.get(CNDA_PLUGIN_CNDAPLUGINDEPENDENCIES);
        assertThat(cndaPluginDependencies).isNotNull()
                                          .hasFieldOrPropertyWithValue(PROPERTY_ID, CNDA_PLUGIN_CNDAPLUGINDEPENDENCIES)
                                          .hasFieldOrPropertyWithValue(PROPERTY_PLUGIN_CLASS, "org.nrg.xnat.plugin.CndaPluginDependenciesPlugin")
                                          .hasFieldOrPropertyWithValue(PROPERTY_NAME, "CNDA 1.7 Dependencies Plugin")
                                          .hasFieldOrPropertyWithValue(PROPERTY_BEAN_NAME, "cndaPluginDependenciesPlugin");
        assertThat(cndaPluginDependencies.getDataModelBeans()).isNullOrEmpty();

        final XnatPluginBean cndaPluginUds = pluginBeans.get(CNDA_PLUGIN_UDS);
        assertThat(cndaPluginUds).isNotNull()
                                 .hasFieldOrPropertyWithValue(PROPERTY_ID, CNDA_PLUGIN_UDS)
                                 .hasFieldOrPropertyWithValue(PROPERTY_PLUGIN_CLASS, "org.nrg.xnat.plugin.UdsPlugin")
                                 .hasFieldOrPropertyWithValue(PROPERTY_NAME, "XNAT 1.7 UDS Plugin")
                                 .hasFieldOrPropertyWithValue(PROPERTY_BEAN_NAME, "udsPlugin");
        final List<XnatDataModelBean> cndaPluginUdsDataModelBeans = cndaPluginUds.getDataModelBeans().stream().sorted(Comparator.comparing(XnatDataModelBean::getType)).collect(Collectors.toList());
        assertThat(cndaPluginUdsDataModelBeans).isNotNull().isNotEmpty().hasSize(16);
        assertThat(cndaPluginUdsDataModelBeans.get(0)).isNotNull()
                                                      .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:a1subdemoData")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS A1: Sub Demo")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS A1: Sub Demos")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(1)).isNotNull()
                                                      .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:a2infdemoData")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS A2: Informant Demos")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS A2: Informant Demos")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(2)).isNotNull()
                                                      .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:a3sbfmhstData")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS A3: Partcpt Family Hist.")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS A3: Partcpt Family Hist.")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(3)).isNotNull()
                                                      .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:a4drugsData")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS A4: Sub Meds")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS A4: Sub Meds")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(4)).isNotNull()
                                                      .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:a5subhstData")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS A5: Sub Health Hist.")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS A5: Sub Health Hist.")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(5)).isNotNull()
                                                      .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:b2hachData")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS B2: HIS and CVD")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS B2: HIS and CVD")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(6)).isNotNull()
                                                      .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:b3updrsData")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS B3: UPDRS")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS B3: UPDRS")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(7)).isNotNull()
                                                      .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:b4cdrData")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS B4: CDR")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS B4: CDRs")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(8)).isNotNull()
                                                      .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:b5behavasData")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS B5: NPI-Q")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS B5: NPI-Q")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(9)).isNotNull()
                                                      .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:b6bevgdsData")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                      .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS B6: GDS")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS B6: GDS")
                                                      .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(10)).isNotNull()
                                                       .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:b6suppData")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                       .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS B6 Supplement")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS B6 Supplements")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(11)).isNotNull()
                                                       .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:b7faqData")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                       .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS B7: FAQ")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS B7: FAQs")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(12)).isNotNull()
                                                       .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:b8evalData")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                       .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS B8: Phys. Neuro Findings")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS B8: Phys. Neuro Findings")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(13)).isNotNull()
                                                       .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:b9clinjdgData")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                       .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS B9: Clin. Judgements")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS B9: Clin. Judgements")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(14)).isNotNull()
                                                       .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:b9suppData")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                       .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS B9 Supplement")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS B9 Supplements")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(cndaPluginUdsDataModelBeans.get(15)).isNotNull()
                                                       .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "uds:d1dxData")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                       .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "UDS D1: Clinician Diagnosis")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "UDS D1: Clinician Diagnosis")
                                                       .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);

        final XnatPluginBean customDownloaderPlugin = pluginBeans.get(CUSTOM_DOWNLOADER_PLUGIN);
        assertThat(customDownloaderPlugin).isNotNull()
                                          .hasFieldOrPropertyWithValue(PROPERTY_ID, CUSTOM_DOWNLOADER_PLUGIN)
                                          .hasFieldOrPropertyWithValue(PROPERTY_PLUGIN_CLASS, "org.nrg.xnat.downloader.plugin.CustomDownloaderPlugin")
                                          .hasFieldOrPropertyWithValue(PROPERTY_NAME, "XNAT Custom Downloader Plugin")
                                          .hasFieldOrPropertyWithValue(PROPERTY_BEAN_NAME, "customDownloaderPlugin");
        assertThat(customDownloaderPlugin.getDataModelBeans()).isNullOrEmpty();

        final XnatPluginBean nrgPluginFreesurfer = pluginBeans.get(NRG_PLUGIN_FREESURFERCOMMON);
        assertThat(nrgPluginFreesurfer).isNotNull()
                                       .hasFieldOrPropertyWithValue(PROPERTY_ID, NRG_PLUGIN_FREESURFERCOMMON)
                                       .hasFieldOrPropertyWithValue(PROPERTY_PLUGIN_CLASS, "org.nrg.xnat.generated.plugin.FreesurferCommonPlugin")
                                       .hasFieldOrPropertyWithValue(PROPERTY_NAME, "XNAT 1.7 FreeSurfer Common Plugin")
                                       .hasFieldOrPropertyWithValue(PROPERTY_BEAN_NAME, "freesurferCommonPlugin");
        final List<XnatDataModelBean> nrgPluginFreesurferDataModelBeans = nrgPluginFreesurfer.getDataModelBeans().stream().sorted(Comparator.comparing(XnatDataModelBean::getType)).collect(Collectors.toList());
        assertThat(nrgPluginFreesurferDataModelBeans).isNotNull().isNotEmpty().hasSize(5);
        assertThat(nrgPluginFreesurferDataModelBeans.get(0)).isNotNull()
                                                            .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "fs:aparcRegionAnalysis")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                            .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "APARC")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "APARCs")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(nrgPluginFreesurferDataModelBeans.get(1)).isNotNull()
                                                            .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "fs:asegRegionAnalysis")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                            .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "ASEG")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "ASEGs")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(nrgPluginFreesurferDataModelBeans.get(2)).isNotNull()
                                                            .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "fs:automaticSegmentationData")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                            .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "Auto Seg")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "Auto Segs")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(nrgPluginFreesurferDataModelBeans.get(3)).isNotNull()
                                                            .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "fs:fsData")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                            .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "Freesurfer")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "Freesurfers")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
        assertThat(nrgPluginFreesurferDataModelBeans.get(4)).isNotNull()
                                                            .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "fs:longFSData")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                            .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "LongitudinalFS")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "LongitudinalFSs")
                                                            .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);

        final XnatPluginBean nrgPluginPup = pluginBeans.get(NRG_PLUGIN_PUP);
        assertThat(nrgPluginPup).isNotNull()
                                .hasFieldOrPropertyWithValue(PROPERTY_ID, NRG_PLUGIN_PUP)
                                .hasFieldOrPropertyWithValue(PROPERTY_PLUGIN_CLASS, "org.nrg.xnat.generated.plugin.PupPlugin")
                                .hasFieldOrPropertyWithValue(PROPERTY_NAME, "XNAT 1.7 PUP Plugin")
                                .hasFieldOrPropertyWithValue(PROPERTY_BEAN_NAME, "pupPlugin");
        assertThat(nrgPluginPup.getDataModelBeans()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(nrgPluginPup.getDataModelBeans().get(0)).isNotNull()
                                                           .hasFieldOrPropertyWithValue(PROPERTY_TYPE, "pup:pupTimeCourseData")
                                                           .hasFieldOrPropertyWithValue(PROPERTY_SECURED, true)
                                                           .hasFieldOrPropertyWithValue(PROPERTY_SINGULAR, "PUP Timecourse")
                                                           .hasFieldOrPropertyWithValue(PROPERTY_PLURAL, "PUP Timecourses")
                                                           .hasFieldOrPropertyWithValue(PROPERTY_CODE, null);
    }
}
