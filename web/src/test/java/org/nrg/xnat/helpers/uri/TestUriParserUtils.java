/*
 * web: org.nrg.xnat.helpers.uri.TestUriParserUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestUriParserUtils {
    @Test
    public void testUriGeneration() {
        assertEquals("/archive/experiments/EXPT_ID", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:experimentData")));
        assertEquals("/archive/experiments/EXPT_ID/resources/XNAME", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:experimentData", "xname")));
        assertEquals("/archive/experiments/EXPT_ID/assessors/DERIVED_ID", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:experimentData", "xnat:derivedData")));
        assertEquals("/archive/experiments/EXPT_ID/assessors/DERIVED_ID/TYPE/resources/XNAME", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:experimentData", "xnat:derivedData", "type", "xname")));
        assertEquals("/archive/experiments/EXPT_ID/scans/SCAN_ID", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:experimentData", "xnat:imageScanData")));
        assertEquals("/archive/experiments/EXPT_ID/scans/SCAN_ID/resources/XNAME", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:experimentData", "xnat:imageScanData", "xname")));
        assertEquals("/archive/experiments/EXPT_ID/reconstructions/RECON_ID", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:experimentData", "xnat:reconstructedImageData")));
        assertEquals("/archive/experiments/EXPT_ID/reconstructions/RECON_ID/TYPE/resources/XNAME", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:experimentData", "xnat:reconstructedImageData", "type", "xname")));
        assertEquals("/archive/projects/PROJECT_ID", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:projectData")));
        assertEquals("/archive/projects/PROJECT_ID/resources/XNAME", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:projectData", "xname")));
        assertEquals("/archive/projects/PROJECT_ID/experiments/EXPT_ID", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:projectData", "xnat:experimentData")));
        assertEquals("/archive/projects/PROJECT_ID/experiments/EXPT_ID/resources/XNAME", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:projectData", "xnat:experimentData", "xname")));
        assertEquals("/archive/projects/PROJECT_ID/subjects/SUBJECT_ID", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:projectData", "xnat:subjectData")));
        assertEquals("/archive/projects/PROJECT_ID/subjects/SUBJECT_ID/resources/XNAME", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:projectData", "xnat:subjectData", "xname")));
        assertEquals("/archive/projects/PROJECT_ID/subjects/SUBJECT_ID/experiments/EXPT_ID", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:projectData", "xnat:subjectData", "xnat:experimentData")));
        assertEquals("/archive/projects/PROJECT_ID/subjects/SUBJECT_ID/experiments/EXPT_ID/resources/XNAME", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:projectData", "xnat:subjectData", "xnat:experimentData", "xname")));
        assertEquals("/archive/projects/PROJECT_ID/subjects/SUBJECT_ID/experiments/EXPT_ID/assessors/DERIVED_ID", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:projectData", "xnat:subjectData", "xnat:experimentData", "xnat:derivedData")));
        assertEquals("/archive/projects/PROJECT_ID/subjects/SUBJECT_ID/experiments/EXPT_ID/assessors/DERIVED_ID/TYPE/resources/XNAME", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:projectData", "xnat:subjectData", "xnat:experimentData", "xnat:derivedData", "type", "xname")));
        assertEquals("/archive/projects/PROJECT_ID/subjects/SUBJECT_ID/experiments/EXPT_ID/scans/SCAN_ID", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:projectData", "xnat:subjectData", "xnat:experimentData", "xnat:imageScanData")));
        assertEquals("/archive/projects/PROJECT_ID/subjects/SUBJECT_ID/experiments/EXPT_ID/scans/SCAN_ID/resources/XNAME", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:projectData", "xnat:subjectData", "xnat:experimentData", "xnat:imageScanData", "xname")));
        assertEquals("/archive/projects/PROJECT_ID/subjects/SUBJECT_ID/experiments/EXPT_ID/reconstructions/RECON_ID", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:projectData", "xnat:subjectData", "xnat:experimentData", "xnat:reconstructedImageData")));
        assertEquals("/archive/projects/PROJECT_ID/subjects/SUBJECT_ID/experiments/EXPT_ID/reconstructions/RECON_ID/TYPE/resources/XNAME", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:projectData", "xnat:subjectData", "xnat:experimentData", "xnat:reconstructedImageData", "type", "xname")));
        assertEquals("/archive/subjects/SUBJECT_ID", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:subjectData")));
        assertEquals("/archive/subjects/SUBJECT_ID/resources/XNAME", UriParserUtils.getArchiveUriFromParameterTypes(getParameters("xnat:subjectData", "xname")));
    }
    
    private Map<String, String> getParameters(final String... types) {
        final Map<String, String> parameters = Maps.newHashMap();
        for (final String type : types) {
            if (StringUtils.equals("xnat:projectData", type)) {
                parameters.put("xnat:projectData", "PROJECT_ID");
            } else if (StringUtils.equals("xnat:subjectData", type)) {
                parameters.put("xnat:subjectData", "SUBJECT_ID");
            } else if (StringUtils.equals("xnat:experimentData", type)) {
                parameters.put("xnat:experimentData", "EXPT_ID");
            } else if (StringUtils.equals("xnat:derivedData", type)) {
                parameters.put("xnat:derivedData", "DERIVED_ID");
            } else if (StringUtils.equals("xnat:imageScanData", type)) {
                parameters.put("xnat:imageScanData", "SCAN_ID");
            } else if (StringUtils.equals("xnat:reconstructedImageData", type)) {
                parameters.put("xnat:reconstructedImageData", "RECON_ID");
            } else if (StringUtils.equals("type", type)) {
                parameters.put("type", "TYPE");
            } else if (StringUtils.equals("xname", type)) {
                parameters.put("xname", "XNAME");
            } 
        }
        return parameters;
    }
}
