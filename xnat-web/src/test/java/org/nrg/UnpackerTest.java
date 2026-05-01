/*
 * PrearcImporter: org.nrg.UnpackerTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import static org.junit.Assert.*;

import org.junit.Test;
import org.nrg.framework.status.StatusListenerI;
import org.nrg.framework.status.StatusMessage;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class UnpackerTest {
    final static File testDataDir = new File("src/test/data");
    final static File textFile1 = new File(testDataDir, "file1.txt");
    final static File textFile2 = new File(testDataDir, "file2.txt");
    final static File subDir1 = new File(testDataDir, "subdir1");

    static {
        assertTrue(testDataDir.isDirectory());
        assertTrue(textFile1.isFile());
        assertTrue(textFile2.isFile());
        assertTrue(subDir1.isDirectory());
    }

    @Test
    public final void testUnpackFile() {
        final Map<String,File> vals = new HashMap<String,File>();

        final Unpacker up = new Unpacker() {
            public void unpack(final File file, final File destination) {
                vals.put("file", file);
                vals.put("dest", destination);
            }
        };

        assertFalse(vals.containsKey("file"));
        assertFalse(vals.containsKey("dest"));
        up.unpack(testDataDir);
        assertTrue(vals.containsKey("file"));
        assertEquals(testDataDir, vals.get("file"));
        assertTrue(vals.containsKey("dest"));
        assertNull(vals.get("dest"));
    }

    @Test
    public final void testAddStatusListener() {
        final Map<Object,String> messages = new HashMap<Object,String>();

        final Unpacker up = new Unpacker() {
            public void unpack(final File file, final File destination) {
                publishStatus(file, "unpacking");
                publishStatus(destination, "into");
            }
        };

        up.addStatusListener(new StatusListenerI() {
            public void notify(final StatusMessage m) {
                assertFalse(messages.containsKey(m.getSource()));
                messages.put(m.getSource(), m.getMessage());
            }
        });

        assertFalse(messages.containsKey(textFile1));
        assertFalse(messages.containsKey(testDataDir));
        up.unpack(textFile1, testDataDir);

        assertTrue(messages.containsKey(textFile1));
        assertEquals("unpacking", messages.get(textFile1));
        assertTrue(messages.containsKey(testDataDir));
        assertEquals("into", messages.get(testDataDir));
    }

    @Test
    public final void testRemoveStatusListener() {
        final Map<Object,Object> messages = new HashMap<Object,Object>();

        final Unpacker up = new Unpacker() {
            public void unpack(final File file, final File destination) {
                publishStatus(file, file.getName());
                publishStatus(destination, destination.getName());
            }
        };

        final StatusListenerI lf = new StatusListenerI() {
            public void notify(final StatusMessage m) {
                assertFalse(messages.containsKey(m.getSource()));
                messages.put(m.getSource(), m.getMessage());
            }
        };

        final StatusListenerI lb = new StatusListenerI() {
            public void notify(final StatusMessage m) {
                assertFalse(messages.containsKey(m.getMessage()));
                messages.put(m.getMessage(), m.getSource());
            }
        };

        up.addStatusListener(lf);
        up.addStatusListener(lb);
        assertFalse(messages.containsKey(textFile1));
        assertFalse(messages.containsKey(testDataDir));
        up.unpack(textFile1, testDataDir);

        assertTrue(messages.containsKey(textFile1));
        assertEquals(textFile1.getName(), messages.get(textFile1));
        assertTrue(messages.containsKey(testDataDir));
        assertEquals(testDataDir.getName(), messages.get(testDataDir));

        assertFalse(messages.containsKey(textFile2.getName()));
        assertFalse(messages.containsKey(subDir1.getName()));
        up.removeStatusListener(lf);
        up.unpack(textFile2, subDir1);

        // removed listener shouldn't be recording messages
        assertFalse(messages.containsKey(textFile2));
        assertFalse(messages.containsKey(subDir1));

        // other listener still should.
        assertTrue(messages.containsKey(textFile2.getName()));
        assertEquals(textFile2, messages.get(textFile2.getName()));
        assertTrue(messages.containsKey(subDir1.getName()));
        assertEquals(subDir1, messages.get(subDir1.getName()));
    }

    @Test
    public final void testPublishStatus() {
        final Map<Object,StatusMessage> messages = new HashMap<Object,StatusMessage>();

        final Unpacker up = new Unpacker() {
            public void unpack(final File file, final File destination) {
                publishStatus(file, "unpacking");
                publishStatus(destination, "into");
            }
        };

        up.addStatusListener(new StatusListenerI() {
            public void notify(final StatusMessage m) {
                assertFalse(messages.containsKey(m.getSource()));
                messages.put(m.getSource(), m);
            }
        });

        assertFalse(messages.containsKey(textFile1));
        up.unpack(textFile1, subDir1);
        assertTrue(messages.containsKey(textFile1));
        assertEquals(StatusMessage.Status.PROCESSING, messages.get(textFile1).getStatus());
    }

    @Test
    public final void testPublishWarning() {
        final Map<Object,StatusMessage> messages = new HashMap<Object,StatusMessage>();

        final Unpacker up = new Unpacker() {
            public void unpack(final File file, final File destination) {
                publishWarning(file, "unpacking");
                publishWarning(destination, "into");
            }
        };

        up.addStatusListener(new StatusListenerI() {
            public void notify(final StatusMessage m) {
                assertFalse(messages.containsKey(m.getSource()));
                messages.put(m.getSource(), m);
            }
        });

        assertFalse(messages.containsKey(textFile1));
        up.unpack(textFile1, subDir1);
        assertTrue(messages.containsKey(textFile1));
        assertEquals(StatusMessage.Status.WARNING, messages.get(textFile1).getStatus());
    }

    @Test
    public final void testPublishFailure() {
        final Map<Object,StatusMessage> messages = new HashMap<Object,StatusMessage>();

        final Unpacker up = new Unpacker() {
            public void unpack(final File file, final File destination) {
                publishFailure(file, "unpacking");
                publishFailure(destination, "into");
            }
        };

        up.addStatusListener(new StatusListenerI() {
            public void notify(final StatusMessage m) {
                assertFalse(messages.containsKey(m.getSource()));
                messages.put(m.getSource(), m);
            }
        });

        assertFalse(messages.containsKey(textFile1));
        up.unpack(textFile1, subDir1);
        assertTrue(messages.containsKey(textFile1));
        assertEquals(StatusMessage.Status.FAILED, messages.get(textFile1).getStatus());
    }

    @Test
    public final void testPublishSuccess() {
        final Map<Object,StatusMessage> messages = new HashMap<Object,StatusMessage>();

        final Unpacker up = new Unpacker() {
            public void unpack(final File file, final File destination) {
                publishSuccess(file, "unpacking");
                publishSuccess(destination, "into");
            }
        };

        up.addStatusListener(new StatusListenerI() {
            public void notify(final StatusMessage m) {
                assertFalse(messages.containsKey(m.getSource()));
                messages.put(m.getSource(), m);
            }
        });

        assertFalse(messages.containsKey(textFile1));
        up.unpack(textFile1, subDir1);
        assertTrue(messages.containsKey(textFile1));
        assertEquals(StatusMessage.Status.COMPLETED, messages.get(textFile1).getStatus());
    }
}
