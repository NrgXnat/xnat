/*
 * DicomEdit: TestFunctions
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Run tests of scriptProcessorRemoveEndLineComments.
 *
 */
public class TestRemoveEndLineComments {

    @Test
    public void test() throws MizerException {
        ScriptProcessorRemoveEndLineComments processor = new ScriptProcessorRemoveEndLineComments();

        String line0 =      "(0040,0555),   // AcquisitionContextSequence\n";
        String line0_post = "(0040,0555),   \n";
        String line1 =      "    (0040,0555),   // AcquisitionContextSequence\n";
        String line1_post = "    (0040,0555),   \n";
        String line2 = "// comment.\n";
        String line2_post = "\n";
        String line3 = "// comment. // comment\n";
        String line3_post = "\n";
        String line4 = "\n";
        String line4_post = "\n";
        String line5 = "foo[(0019,{fubar}XX)/*]\n";
        String line5_post = line5;
        String line6 = "foo//\"snafu\"\n";
        String line6_post = "foo\n";
        String line7 = "https://foo.com/bar/nuts\n";
        String line7_post = line7;
        String line8 = "https://foo.com/bar/nuts//comment\n";
        String line8_post = "https://foo.com/bar/nuts\n";
        String line9 = "//https://foo.com/bar/nuts//comment\n";
        String line9_post = "\n";
        String line10 = "(0010,1000) := getURL[\"https://run.mocky.io/v3/bf4da7a0-09c5-4d68-9bec-6ed5d8229930\"]\n";
        String line10_post = line10;

        String line11 = "(0008,0100) := \"//not a comment\"\n";
        String line11_post = line11;
        String line12 = "(0008,0102) := \"https://stillnotacomment\"\n";
        String line12_post = line12;
        String line13 = "(0008,0103) := \"/NOT/A/COMMENT//\"\n";
        String line13_post = line13;
        String line14 = "(0008,0104) := \"http//nope\"\n";
        String line14_post = line14;

        List<String> lines = Arrays.asList( line0, line1, line2, line3, line4, line5, line6, line7, line8, line9, line10, line11, line12, line13, line14);

        assertEquals(line0, lines.get(0));
        assertEquals(line1, lines.get(1));
        assertEquals(line2, lines.get(2));
        assertEquals(line3, lines.get(3));
        assertEquals(line4, lines.get(4));
        assertEquals(line5, lines.get(5));
        assertEquals(line6, lines.get(6));
        assertEquals(line7, lines.get(7));
        assertEquals(line8, lines.get(8));
        assertEquals(line9, lines.get(9));
        assertEquals(line10, lines.get(10));
        assertEquals(line11, lines.get(11));
        assertEquals(line12, lines.get(12));
        assertEquals(line13, lines.get(13));
        assertEquals(line14, lines.get(14));

        List<String> lines_post = processor.process(lines);

        assertEquals(line0_post, lines_post.get(0));
        assertEquals(line1_post, lines_post.get(1));
        assertEquals(line2_post, lines_post.get(2));
        assertEquals(line3_post, lines_post.get(3));
        assertEquals(line4_post, lines_post.get(4));
        assertEquals(line5_post, lines_post.get(5));
        assertEquals(line6_post, lines_post.get(6));
        assertEquals(line7_post, lines_post.get(7));
        assertEquals(line8_post, lines_post.get(8));
        assertEquals(line9_post, lines_post.get(9));
        assertEquals(line10_post, lines_post.get(10));
        assertEquals(line11_post, lines_post.get(11));
        assertEquals(line12_post, lines_post.get(12));
        assertEquals(line13_post, lines_post.get(13));
        assertEquals(line14_post, lines_post.get(14));

    }

    @Test
    public void test2() throws MizerException {
        String line0 = "foo\n";
        String line0_post = line0;
        String line1 = "foo // comment.\n";
        String line1_post = "foo \n";
        String line2 = "// comment.\n";
        String line2_post = "\n";
        String line3 = "// comment. // comment\n";
        String line3_post = "\n";
        String line4 = "\n";
        String line4_post = "\n";
        String line5 = "foo[(0019,{fubar}XX)/*]\n";
        String line5_post = line5;
        String line6 = "foo//\"snafu\"\n";
        String line6_post = "foo\n";
        String line7 = "https://foo.com/bar/nuts\n";
        String line7_post = line7;
        String line8 = "https://foo.com/bar/nuts//comment\n";
        String line8_post = "https://foo.com/bar/nuts\n";
        String line9 = "//https://foo.com/bar/nuts//comment\n";
        String line9_post = "\n";
        String line10 = "(0010,1000) := getURL[\"https://run.mocky.io/v3/bf4da7a0-09c5-4d68-9bec-6ed5d8229930\"]\n";
        String line10_post = line10;

        String line11 = "(0008,0100) := \"//not a comment\"\n";
        String line11_post = line11;
        String line12 = "(0008,0102) := \"https://stillnotacomment\"\n";
        String line12_post = line12;
        String line13 = "(0008,0103) := \"/NOT/A/COMMENT//\"\n";
        String line13_post = line13;
        String line14 = "(0008,0104) := \"http//nope\"\n";
        String line14_post = line14;
        String line15 = "x \"//c in string\" // c s\"c\"\n";
        String line15_post = "x \"//c in string\" \n";
        String line16 = "url://foo/bar \"//c in string\" \"https://url/in/string\" // file://url/snafu // c in c\n";
        String line16_post = "url://foo/bar \"//c in string\" \"https://url/in/string\" \n";

        List<String> linesIn = Arrays.asList(line0, line1, line2, line3, line4, line5, line6, line7, line8, line9, line10, line11, line12, line13, line14, line15, line16);

        assertEquals(line0, linesIn.get(0));
        assertEquals(line1, linesIn.get(1));
        assertEquals(line2, linesIn.get(2));
        assertEquals(line3, linesIn.get(3));
        assertEquals(line4, linesIn.get(4));
        assertEquals(line5, linesIn.get(5));
        assertEquals(line6, linesIn.get(6));
        assertEquals(line7, linesIn.get(7));
        assertEquals(line8, linesIn.get(8));
        assertEquals(line9, linesIn.get(9));
        assertEquals(line10, linesIn.get(10));
        assertEquals(line11, linesIn.get(11));
        assertEquals(line12, linesIn.get(12));
        assertEquals(line13, linesIn.get(13));
        assertEquals(line14, linesIn.get(14));
        assertEquals(line15, linesIn.get(15));
        assertEquals(line16, linesIn.get(16));

        ScriptProcessorRemoveEndLineComments comments = new ScriptProcessorRemoveEndLineComments();
        List<String> linesOut = comments.process(linesIn);

        assertEquals(line0_post, linesOut.get(0));
        assertEquals(line1_post, linesOut.get(1));
        assertEquals(line2_post, linesOut.get(2));
        assertEquals(line3_post, linesOut.get(3));
        assertEquals(line4_post, linesOut.get(4));
        assertEquals(line5_post, linesOut.get(5));
        assertEquals(line6_post, linesOut.get(6));
        assertEquals(line7_post, linesOut.get(7));
        assertEquals(line8_post, linesOut.get(8));
        assertEquals(line9_post, linesOut.get(9));
        assertEquals(line10_post, linesOut.get(10));
        assertEquals(line11_post, linesOut.get(11));
        assertEquals(line12_post, linesOut.get(12));
        assertEquals(line13_post, linesOut.get(13));
        assertEquals(line14_post, linesOut.get(14));
        assertEquals(line15_post, linesOut.get(15));
        assertEquals(line16_post, linesOut.get(16));

    }

}
