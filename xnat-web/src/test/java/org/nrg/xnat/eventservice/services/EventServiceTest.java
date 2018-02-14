package org.nrg.xnat.eventservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xnat.eventservice.config.EventServiceTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.empty;



@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = EventServiceTestConfig.class)
public class EventServiceTest {
    private static final Logger log = LoggerFactory.getLogger(EventServiceTest.class);

    @Autowired
    private ObjectMapper objectMapper;



    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void getActionsByEvent() throws Exception {
    }

    @Test
    public void getEvents() throws Exception {
    }

    @Test
    public void triggerEvent() throws Exception {
    }

    @Test
    public void processEvent()  throws Exception {
    }

    @Test
    public void jsonFilterTest() throws Exception {
        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);
        String jsonFilter;
        String jsonObject;
        List<String> filterResult;

        // ** Match Project ** //
        jsonObject = projectJson;

        jsonFilter = jsonPathProjectByTitle;
        filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
        assertThat("Expected non-null result for filter match project", filterResult, not(nullValue()));
        assertThat("Expected result for filter match project", filterResult, is(not(empty())));

        jsonFilter = jsonPathProjectByRegExTitle;
        filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
        assertThat("Expected non-null result for filter match project", filterResult, not(nullValue()));
        assertThat("Expected result for filter match project", filterResult, is(not(empty())));

        // ** Mismatch Project ** //
        jsonFilter = jsonPathProjectByTitleMismatch;
        filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
        assertThat("Expected non-null result for filter mismatch project", filterResult, not(nullValue()));
        assertThat("Expected empty result for filter mismatch project", filterResult, is(empty()));

        jsonFilter = jsonPathProjectByRegExTitleMismatch;
        filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
        assertThat("Expected non-null result for filter mismatch project", filterResult, not(nullValue()));
        assertThat("Expected empty result for filter mismatch project", filterResult, is(empty()));


        // ** Match Subject ** //
        jsonObject = subjectJson;

        jsonFilter = jsonPathSubjectByRegExProject;
        filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
        assertThat("Expected non-null result for filter match subject", filterResult, not(nullValue()));
        assertThat("Expected result for filter match subject", filterResult, is(not(empty())));

        // ** Mismatch Subject ** //
        jsonFilter = jsonPathSubjectByRegExProjectMismatch;
        filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
        assertThat("Expected non-null result for filter mismatch subject", filterResult, not(nullValue()));
        assertThat("Expected empty result for filter mismatch subject", filterResult, is(empty()));

        
        // ** Match Session ** //
        jsonObject = sessionWoFilesJson;

        jsonFilter = jsonPathSessionBySubjectAndModality;
        filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
        assertThat("Expected non-null result for filter match session", filterResult, not(nullValue()));
        assertThat("Expected result for filter match session", filterResult, is(not(empty())));

        jsonFilter = jsonPathSessionByModalityAndNumScans;
        jsonObject = sessionWoFilesJson;
        filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
        assertThat("Expected non-null result for filter match session", filterResult, not(nullValue()));
        assertThat("Expected result for filter match session", filterResult, is(not(empty())));

        // ** Mismatch Session ** //
        jsonFilter = jsonPathSessionBySubjectAndModalityMismatch;
        filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
        assertThat("Expected non-null result for filter mismatch session", filterResult, not(nullValue()));
        assertThat("Expected empty result for filter mismatch session", filterResult, is(empty()));

        jsonFilter = jsonPathSessionByModalityAndNumScansMismatch;
        filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
        assertThat("Expected non-null result for filter mismatch session", filterResult, not(nullValue()));
        assertThat("Expected empty result for filter mismatch session", filterResult, is(empty()));


        // ** Match Scan ** //
        jsonObject = scanWoFilesJson;

        jsonFilter = jsonPathScanByModalityAndQuality;
        filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
        assertThat("Expected non-null result for filter match scan", filterResult, not(nullValue()));
        assertThat("Expected result for filter match scan", filterResult, is(not(empty())));

        jsonFilter = jsonPathScanByRegExModalityNumFramesEtc;
        jsonObject = scanWoFilesJson;
        filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
        assertThat("Expected non-null result for filter match scan", filterResult, not(nullValue()));
        assertThat("Expected result for filter match scan", filterResult, is(not(empty())));

        // ** Mismatch Scan ** //
        jsonFilter = jsonPathScanByModalityAndQualityMismatch;
        filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
        assertThat("Expected non-null result for filter mismatch scan", filterResult, not(nullValue()));
        assertThat("Expected empty result for filter mismatch scan", filterResult, is(empty()));

        jsonFilter = jsonPathScanByRegExModalityNumFramesEtcMismatch;
        filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
        assertThat("Expected non-null result for filter mismatch scan", filterResult, not(nullValue()));
        assertThat("Expected empty result for filter mismatch scan", filterResult, is(empty()));




    }




    String jsonPathProjectByTitle = "$[?(@.label == \"TheProjectTitle\")]";
    String jsonPathProjectByRegExTitle = "$[?(@.label =~ /the.*[title]+[optional]?/i)]";
    String jsonPathProjectByTitleMismatch = "$[?(@.label == \"ADifferentProjectTitle\")]";
    String jsonPathProjectByRegExTitleMismatch = "$[?(@.label =~ /the.*[title]+not-optional/i)]";

    String jsonPathSubjectByRegExProject = "$[?(@.project-id =~ /[optionalString]?.*theproject.*[anotherOptionalString]?/i )]";
    String jsonPathSubjectByRegExProjectMismatch = "$[?(@.project-id =~ /[requiredString]+.*theproject.*[anotherOptionalString]?/i )]";

    String jsonPathSessionBySubjectAndModality = "$[?(@.subject-id == \"XNAT_S00001\" && @.modality == \"MR\")]";
    String jsonPathSessionByModalityAndNumScans = "$[?(@.modality == \"MR\" && @.scans.length() > 0 )]";
    String jsonPathSessionBySubjectAndModalityMismatch = "$[?(@.subject-id == \"XNAT_S00001\" && @.modality == \"CT\")]";
    String jsonPathSessionByModalityAndNumScansMismatch = "$[?(@.modality == \"MR\" && @.scans.length() > 10 )]";

    String jsonPathScanByModalityAndQuality = "$[?(@.modality == \"MR\" && @.quality == \"usable\" )]";
    String jsonPathScanByRegExModalityNumFramesEtc = "$[?(@.xsiType =~ /.*MRScanData/i && @.frames  > 100 && @.scanner-manufacturer =~ /siemens/i && @.scanner-model =~ /TRIOTIM/i )]";
    String jsonPathScanByModalityAndQualityMismatch = "$[?(@.modality == \"MR\" && @.quality == \"poor\" )]";
    String jsonPathScanByRegExModalityNumFramesEtcMismatch = "$[?(@.xsiType =~ /.*MRScanData/i && @.frames  > 100 && @.scanner-manufacturer =~ /siemens/i && @.scanner-model =~ /SIGNA/i )]";


    // ** JSONPath Filter Tests ** //
    final String projectJson = "{\n" +
            "    \"type\": \"Project\",\n" +
            "    \"id\": \"TheProjectID\",\n" +
            "    \"label\": \"TheProjectTitle\",\n" +
            "    \"xsiType\": \"xnat:projectData\",\n" +
            "    \"uri\": \"/archive/projects/TheProjectID\",\n" +
            "    \"resources\": [],\n" +
            "    \"subjects\": [],\n" +
            "    \"directory\": \"/data/xnat/archive/TheProjectID/arc001\"\n" +
            "}";

    final String subjectJson = "{\n" +
            "    \"type\": \"Subject\",\n" +
            "    \"id\": \"XNAT_S00003\",\n" +
            "    \"label\": \"SubjectsID\",\n" +
            "    \"xsiType\": \"xnat:subjectData\",\n" +
            "    \"uri\": \"/archive/subjects/XNAT_S00003\",\n" +
            "    \"sessions\": [],\n" +
            "    \"resources\": [],\n" +
            "    \"project-id\": \"TheProjectID\"\n" +
            "}";

    final String scanWoFilesJson = "{\n" +
            "    \"type\": \"Scan\",\n" +
            "    \"id\": \"4\",\n" +
            "    \"label\": \"4 - t1_mpr_1mm_p2_pos50\",\n" +
            "    \"xsiType\": \"xnat:mrScanData\",\n" +
            "    \"uri\": \"/archive/experiments/XNAT_E00003/scans/4\",\n" +
            "    \"resources\": [{\n" +
            "        \"type\": \"Resource\",\n" +
            "        \"id\": \"DICOM\",\n" +
            "        \"label\": \"DICOM\",\n" +
            "        \"xsiType\": \"xnat:resourceCatalog\",\n" +
            "        \"uri\": \"/archive/experiments/XNAT_E00003/scans/4/resources/DICOM\",\n" +
            "        \"directory\": \"/data/xnat/archive/ABC123/arc001/TeamGo_MR3/SCANS/4/DICOM\",\n" +
            "        \"integer-id\": 4\n" +
            "    }],\n" +
            "    \"directory\": \"/data/xnat/archive/ABC123/arc001/TeamGo_MR3/SCANS/4\",\n" +
            "    \"frames\": 176,\n" +
            "    \"modality\": \"MR\",\n" +
            "    \"quality\": \"usable\",\n" +
            "    \"scanner\": \"MEDPC\",\n" +
            "    \"uid\": \"1.3.12.2.1107.5.2.32.35177.3.2006121409284535196417894.0.0.0\",\n" +
            "    \"integer-id\": 4,\n" +
            "    \"scan-type\": \"t1_mpr_1mm_p2_pos50\",\n" +
            "    \"scanner-manufacturer\": \"SIEMENS\",\n" +
            "    \"scanner-model\": \"TrioTim\",\n" +
            "    \"series-description\": \"t1_mpr_1mm_p2_pos50\",\n" +
            "    \"start-time\": \"09:37:11\"\n" +
            "}";

    final String sessionWoFilesJson = "{\n" +
            "    \"type\": \"Session\",\n" +
            "    \"id\": \"XNAT_E00003\",\n" +
            "    \"label\": \"TeamGo_MR3\",\n" +
            "    \"xsiType\": \"xnat:mrSessionData\",\n" +
            "    \"uri\": \"/archive/experiments/XNAT_E00003\",\n" +
            "    \"scans\": [{\n" +
            "        \"type\": \"Scan\",\n" +
            "        \"id\": \"4\",\n" +
            "        \"label\": \"4 - t1_mpr_1mm_p2_pos50\",\n" +
            "        \"xsiType\": \"xnat:mrScanData\",\n" +
            "        \"uri\": \"/archive/experiments/XNAT_E00003/scans/4\",\n" +
            "        \"resources\": [{\n" +
            "            \"type\": \"Resource\",\n" +
            "            \"id\": \"DICOM\",\n" +
            "            \"label\": \"DICOM\",\n" +
            "            \"xsiType\": \"xnat:resourceCatalog\",\n" +
            "            \"uri\": \"/archive/experiments/XNAT_E00003/scans/4/resources/DICOM\",\n" +
            "            \"directory\": \"/data/xnat/archive/ABC123/arc001/TeamGo_MR3/SCANS/4/DICOM\",\n" +
            "            \"integer-id\": 4\n" +
            "        }],\n" +
            "        \"directory\": \"/data/xnat/archive/ABC123/arc001/TeamGo_MR3/SCANS/4\",\n" +
            "        \"frames\": 176,\n" +
            "        \"modality\": \"MR\",\n" +
            "        \"quality\": \"usable\",\n" +
            "        \"scanner\": \"MEDPC\",\n" +
            "        \"uid\": \"1.3.12.2.1107.5.2.32.35177.3.2006121409284535196417894.0.0.0\",\n" +
            "        \"integer-id\": 4,\n" +
            "        \"scan-type\": \"t1_mpr_1mm_p2_pos50\",\n" +
            "        \"scanner-manufacturer\": \"SIEMENS\",\n" +
            "        \"scanner-model\": \"TrioTim\",\n" +
            "        \"series-description\": \"t1_mpr_1mm_p2_pos50\",\n" +
            "        \"start-time\": \"09:37:11\"\n" +
            "    }],\n" +
            "    \"assessors\": [],\n" +
            "    \"resources\": [],\n" +
            "    \"directory\": \"/data/xnat/archive/ABC123/arc001/TeamGo_MR3/\",\n" +
            "    \"project-id\": \"ABC123\",\n" +
            "    \"subject-id\": \"XNAT_S00001\",\n" +
            "    \"modality\": \"MR\"\n" +
            "}";

}