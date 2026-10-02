package org.nrg.xnat.archive;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * A session's XML is rebuilt in place each time more of the session arrives. A build that fails
 * partway must leave the XML the last finished build wrote, not a truncated one the prearchive
 * cannot parse.
 */
public class XNATSessionBuilderTest {
    private static final byte[] EARLIER = "<xnat:MRSession ID=\"earlier\"/>".getBytes(StandardCharsets.UTF_8);
    private static final byte[] LATER   = "<xnat:MRSession ID=\"later\"/>".getBytes(StandardCharsets.UTF_8);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void aFinishedBuildReplacesTheXmlOnlyOnceItIsComplete() throws Exception {
        final File xml = existingXml();
        assertTrue(XNATSessionBuilder.writeAtomically(xml, output -> {
            write(output, LATER);
            assertArrayEquals("a reader during the build must still see the earlier XML", EARLIER, Files.readAllBytes(xml.toPath()));
            return true;
        }));
        assertArrayEquals(LATER, Files.readAllBytes(xml.toPath()));
        assertOnlyTheXmlIsLeft(xml);
    }

    @Test
    public void theFirstBuildCreatesTheXml() throws Exception {
        final File xml = new File(folder.newFolder("session"), "session.xml");
        assertTrue(XNATSessionBuilder.writeAtomically(xml, output -> write(output, LATER)));
        assertArrayEquals(LATER, Files.readAllBytes(xml.toPath()));
        assertOnlyTheXmlIsLeft(xml);
    }

    @Test
    public void aBuildThatThrowsLeavesTheEarlierXml() throws Exception {
        final File xml = existingXml();
        final IOException thrown = assertThrows(IOException.class, () -> XNATSessionBuilder.writeAtomically(xml, output -> {
            write(output, Arrays.copyOf(LATER, 10));
            throw new IOException("disk full");
        }));
        assertEquals("disk full", thrown.getMessage());
        assertArrayEquals(EARLIER, Files.readAllBytes(xml.toPath()));
        assertOnlyTheXmlIsLeft(xml);
    }

    @Test
    public void aBuildThatGivesUpPartwayLeavesTheEarlierXml() throws Exception {
        final File xml = existingXml();
        assertFalse(XNATSessionBuilder.writeAtomically(xml, output -> {
            write(output, Arrays.copyOf(LATER, 10));
            return false;
        }));
        assertArrayEquals(EARLIER, Files.readAllBytes(xml.toPath()));
        assertOnlyTheXmlIsLeft(xml);
    }

    /** What a builder that does not recognize the session writes: nothing, so the next builder can be tried. */
    @Test
    public void aBuildThatWritesNothingLeavesTheEarlierXml() throws Exception {
        final File xml = existingXml();
        assertFalse("an empty output", XNATSessionBuilder.writeAtomically(xml, output -> write(output, new byte[0])));
        assertFalse("no output at all", XNATSessionBuilder.writeAtomically(xml, output -> true));
        assertArrayEquals(EARLIER, Files.readAllBytes(xml.toPath()));
        assertOnlyTheXmlIsLeft(xml);
    }

    private File existingXml() throws IOException {
        final File xml = new File(folder.newFolder("session"), "session.xml");
        Files.write(xml.toPath(), EARLIER);
        return xml;
    }

    private static boolean write(final File output, final byte[] content) throws IOException {
        Files.write(output.toPath(), content);
        return true;
    }

    private static void assertOnlyTheXmlIsLeft(final File xml) {
        assertEquals("the attempt must not be left beside the XML",
                     Collections.singletonList(xml.getName()), Arrays.asList(xml.getParentFile().list()));
    }
}
