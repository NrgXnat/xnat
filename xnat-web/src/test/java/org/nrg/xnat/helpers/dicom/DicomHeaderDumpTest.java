package org.nrg.xnat.helpers.dicom;

import org.junit.Test;
import org.nrg.xft.XFTTable;

import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class DicomHeaderDumpTest {

    /**
     * XNAT-7945. Elements are stored in ascending tag order, so anything in a group above 0x7FE0 sorts
     * after the pixel data. The default dump used to read with a stop tag of (7FE0,0010), which did not
     * skip the pixels -- the reader already excludes bulk data -- it just truncated every element past
     * them, and a private block like (F215,1050) never appeared.
     *
     * The fixture carries one private block on each side of the pixel data, so a regression that empties
     * the dump entirely is distinguishable from one that only loses the tail.
     */
    @Test
    public void rendersTagsThatSortAfterPixelData() throws Exception {
        final String file  = Paths.get("src", "test", "resources", "dicomHeaderDump_tagAfterPixelData.dcm").toString();
        final XFTTable table = new DicomHeaderDump(file).render();
        final String  dump = table.toString();

        assertTrue("the block before the pixel data should render", dump.contains("(0019,1050)"));
        assertTrue("the block after the pixel data should render too", dump.contains("(F215,1050)"));
    }
}
