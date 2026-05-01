/*
 * DicomEdit: TestTagPath
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: this was cut/paste form TagPath to get it here where it belongs.  Fix this.
 */
public class TestTagPath {
    private static final Logger logger = LoggerFactory.getLogger(TestTagPath.class);

    private static boolean assertTrue(boolean b) {
        return b;
    }

    private static boolean assertFalse(boolean b) {
        return !b;
    }

//    private static String assertTrueOutcome(boolean b) {
//        return "assert true " + (assertTrue(b) ? "passes." : "fails.");
//    }
//
//    private static String assertFalseOutcome(boolean b) {
//        return "assert false " + (assertFalse(b) ? "passes." : "fails.");
//    }

    public static void main(String[] args) {
//        Tag[] foo = {new TagPublic("",""),new TagPublic("","")};
//        List<Tag> tags = Arrays.asList( foo);
//        String scriptPathString = "00100010";
//        String testPathString = "00100010";
//        TagPath path = new TagPath(scriptPathString);
//        TagPath testPath = new TagPath(testPathString);
//
//        logger.info("base: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));
//
//
//        scriptPathString = "0010001x";
//        testPathString = "00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "001000@0";
//        testPathString = "00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertFalseOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "00100#10";
//        testPathString = "00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertFalseOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "00801110[0]/00100010";
//        testPathString = "00801110[0]/00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "00801110[0]/00080240[2]/00100010";
//        testPathString = "00801110[0]/00080240[2]/00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "00801110[%]/00100010";
//        testPathString = "00801110[666]/00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "*/00100010";
//        testPathString = "00801110[666]/00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "*/00100010";
//        testPathString = "00801110[666]/11111111[0]/00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "*/00100010";
//        testPathString = "00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "+/00100010";
//        testPathString = "00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertFalseOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "+/00100010";
//        testPathString = "22223333[1]/00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "+/00100010";
//        testPathString = "22223333[1]/22222222[0]/00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "./00100010";
//        testPathString = "00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertFalseOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "./00100010";
//        testPathString = "22223333[1]/00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "./00100010";
//        testPathString = "22223333[1]/22222222[0]/00100010";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertFalseOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "0019\"SIEMENS MR HEADER\"14";
//        testPathString = "0019\"SIEMENS MR HEADER\"14";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "0019\"SIEMENS MR\\=HEADER\"14";
//        testPathString = "0019\"SIEMENS MR\\=HEADER\"14";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "0019\"SIEMENS.? MR\\=HEADER\"14";
//        testPathString = "0019\"SIEMENS.? MR\\=HEADER\"14";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));
//
//        scriptPathString = "*/08401110[%]/0019\"SIEMENS MR HEADER\"xx";
//        testPathString = "08401110[2]/08401110[100]/0019\"SIEMENS MR HEADER\"14";
//        path = new TagPath(scriptPathString);
//        testPath = new TagPath(testPathString);
//
//        logger.info("\nbase: " + scriptPathString);
//        logger.info("regex: " + path.getRegex());
//        logger.info("test: " + testPathString);
//        logger.info("match = " + assertTrueOutcome(path.isMatch(testPath)));

    }

}
