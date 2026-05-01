package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestRemoveEscNewLine {
    private static final Logger logger = LoggerFactory.getLogger((TestRemoveEscNewLine.class));

    @Test
    public void test1() throws MizerException {
        // A line in the script containing an escaped newline gets ingested as two Strings, the first of which ends in backslash.
        List<String> lines = new ArrayList<>();
        lines.add("line1a");
        lines.add("line2a\\");
        lines.add("line2b");
        lines.add("line3a\\");
        lines.add("line3b\\");
        lines.add("line3c");
        lines.add("\\");
        lines.add("line4b");
        lines.add("\\");
        lines.add("line5b");
        lines.add("\\");
        lines.add("\\");
        lines.add("line6");
        // trailing escaped newline will result in empty line.
        lines.add("\\");

        ScriptProcessorRemoveEscNewline processor = new ScriptProcessorRemoveEscNewline();
        List<String> joinedLines = processor.process(lines);

        assertEquals( 7, joinedLines.size());
        assertEquals( "line1a", joinedLines.get(0));
        assertEquals( "line2aline2b", joinedLines.get(1));
        assertEquals( "line3aline3bline3c", joinedLines.get(2));
        assertEquals( "line4b", joinedLines.get(3));
        assertEquals( "line5b", joinedLines.get(4));
        assertEquals( "line6", joinedLines.get(5));
        assertEquals( "", joinedLines.get(6));
    }
}
