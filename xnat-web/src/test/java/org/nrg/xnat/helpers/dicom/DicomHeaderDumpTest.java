package org.nrg.xnat.helpers.dicom;

import org.junit.Test;
import org.nrg.xft.XFTTable;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The fixture carries a private block on each side of the pixel data and a sequence, so a regression
 * that empties the dump is distinguishable from one that only loses the tail, and the recursion into
 * sequence items is exercised.
 */
public class DicomHeaderDumpTest {

    private static final String FIXTURE =
            Paths.get("src", "test", "resources", "dicomHeaderDump_tagAfterPixelData.dcm").toString();

    /**
     * XNAT-7945. Elements are stored in ascending tag order, so anything in a group above 0x7FE0 sorts
     * after the pixel data. The default dump used to read with a stop tag of (7FE0,0010), which did not
     * skip the pixels -- the reader already excludes bulk data -- it just truncated every element past
     * them.
     */
    @Test
    public void rendersTagsThatSortAfterPixelData() throws Exception {
        final String dump = new DicomHeaderDump(FIXTURE).render().toString();

        assertTrue("the block before the pixel data should render", dump.contains("(0019,1050)"));
        assertTrue("the block after the pixel data should render too", dump.contains("(F215,1050)"));
    }

    /**
     * TagUtils.forName resolves keywords and plain hex, and reports failure by returning 0xFFFFFFFF
     * rather than throwing, so the hex fallback in getFields never ran for a tag it could not resolve.
     */
    @Test
    public void parsesHighGroupTagRequestedByField() {
        final Map<Integer, Set<String>> fields = DicomDump.getFields(new String[]{"00080060", "F2151050"});

        assertTrue("the ordinary tag should still resolve", fields.containsKey(0x00080060));
        assertTrue("the high-group tag should resolve to itself", fields.containsKey(0xF2151050));
        assertFalse("no key should be the unresolved sentinel", fields.containsKey(0xFFFFFFFF));
    }

    /**
     * The whole path a ?field= request takes: parse the requested tags, size the read from them, render.
     * Neither half alone catches the reported failure. An unresolved tag became 0xFFFFFFFF, the stop tag
     * derived from it wrapped to 0, and the read halted at the first element -- so asking for a
     * high-group tag alongside an ordinary one returned nothing at all rather than just missing one row.
     */
    @Test
    public void rendersHighGroupTagRequestedAlongsideAnOrdinaryOne() throws Exception {
        final Map<Integer, Set<String>> fields = DicomDump.getFields(new String[]{"00080060", "F2151050"});
        final String dump = new DicomHeaderDump(FIXTURE, fields).render().toString();

        assertTrue("the ordinary tag should survive the request", dump.contains("(0008,0060)"));
        assertTrue("the high-group tag should be returned", dump.contains("(F215,1050)"));
    }

    /**
     * DicomSummaryHeaderDump has its own copy of getHeader, so it needs its own check that elements
     * after the pixel data render. It also recurses into sequence items carrying the top-level header,
     * which used to NPE when it looked the nested element back up there and got null.
     */
    @Test
    public void summaryRendersTagsAfterPixelDataAndSurvivesSequences() throws Exception {
        final Iterable<File> files = Collections.singletonList(new File(FIXTURE));
        final XFTTable table = new DicomSummaryHeaderDump(files, Collections.<Integer, Set<String>>emptyMap()).render();
        final String dump = table.toString();

        assertTrue("the block after the pixel data should render", dump.contains("(F215,1050)"));
        assertTrue("the sequence itself should render", dump.contains("(0008,1140)"));
        // The NPE fired while recursing into the item, after the sequence's own row was inserted, so
        // only the nested element's absence distinguishes the broken case.
        assertTrue("elements inside the sequence item should render", dump.contains("(0008,1150)"));
    }
}
