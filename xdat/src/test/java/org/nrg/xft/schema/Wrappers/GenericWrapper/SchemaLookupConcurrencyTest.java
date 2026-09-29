package org.nrg.xft.schema.Wrappers.GenericWrapper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nrg.framework.services.ContextService;
import org.nrg.xft.XFT;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTPseudonymManager;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.design.SchemaElementI;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The schema lookups that are built on first use (field names and maps, referenced elements, meta fields, extenders)
 * build without holding locks. These tests load the security schema, which has a deep reference graph and one
 * extension (xdat:Search extends xdat:stored_search), and build the lookups cold from many threads at once.
 */
public class SchemaLookupConcurrencyTest {
    private static final int THREADS = 8;
    private static final int ROUNDS  = 20;

    @BeforeClass
    public static void runWithoutSpring() {
        // XFT looks up optional Spring beans through the static ContextService and loads schemas without them. A test
        // earlier in the same JVM can leave its application context registered, and beans missing from that context
        // then throw instead of being skipped.
        ContextService.getInstance().setApplicationContext(null);
    }

    @AfterClass
    public static void unloadSchema() {
        XFTManager.clean();
        XFTMetaManager.clean();
        XFTReferenceManager.clean();
        XFTPseudonymManager.clean();
        GenericWrapperElement.ClearElementCache();
        ViewManager.FIELD_MAPS.clear();
        ViewManager.FIELD_NAMES.clear();
    }

    @Test(timeout = 120_000)
    public void buildsTheSameLookupsConcurrentlyAsInOneThread() throws Exception {
        final List<String> names = loadSchema();
        final Map<String, String> expected = new HashMap<>();
        for (final String name : names) {
            expected.put(name, describe(name));
        }

        loadSchema();
        final List<Map<String, String>> built  = Collections.synchronizedList(new ArrayList<>());
        final Queue<Throwable>          errors = new ConcurrentLinkedQueue<>();
        final List<Thread> workers = startWorkers(names, 0, errors, (index, order) -> {
            final Map<String, String> lookups = new HashMap<>();
            for (final String name : order) {
                lookups.put(name, describe(name));
            }
            built.add(lookups);
        });
        awaitWorkers(workers, errors, 0);

        assertEquals(THREADS, built.size());
        for (final Map<String, String> lookups : built) {
            for (final String name : names) {
                assertEquals("the lookups of " + name, expected.get(name), lookups.get(name));
            }
        }
    }

    @Test
    public void buildsTheExpectedLookups() throws Exception {
        loadSchema();
        final GenericWrapperElement user = GenericWrapperElement.GetElement("xdat:user");
        assertEquals("xdat_user", user.getSQLName());
        assertTrue(ViewManager.GetFieldNames(user, ViewManager.QUARANTINE, false, true).contains("xdat:user/login"));
        assertTrue(ViewManager.GetFieldMap(user, ViewManager.ACTIVE, true, true).containsKey("xdat:user/login"));
        assertEquals(15, user.getReferencedElements().size());
        assertEquals(25, user.getMetaFields().getCollection().size());

        final List<SchemaElementI> extenders = GenericWrapperElement.GetElement("xdat:stored_search").getPossibleExtenders();
        assertEquals(1, extenders.size());
        assertEquals("xdat:Search", extenders.get(0).getFullXMLName());
    }

    /**
     * Half the threads build field maps through ViewManager and half run the elements' own lazy builds, each in its
     * own order, on a freshly loaded schema every round. With the locks these used to take, this deadlocked: a
     * field-map build holding the ViewManager class lock waited for an element whose lazy build held that element
     * and waited for the class lock.
     */
    @Test(timeout = 300_000)
    public void concurrentFirstUseDoesNotDeadlock() throws Exception {
        for (int round = 1; round <= ROUNDS; round++) {
            final List<String>     names  = loadSchema();
            final Queue<Throwable> errors = new ConcurrentLinkedQueue<>();
            final List<Thread> workers = startWorkers(names, round, errors, (index, order) -> {
                for (final String name : order) {
                    final GenericWrapperElement element = GenericWrapperElement.GetElement(name);
                    if (index % 2 == 0) {
                        ViewManager.GetFieldMap(element, ViewManager.ACTIVE, true, true);
                    } else {
                        element.getReferencedElements();
                        element.getMetaFields();
                        element.getPossibleExtenders();
                    }
                }
            });
            awaitWorkers(workers, errors, round);
        }
    }

    private interface Work {
        void run(int index, List<String> order) throws Exception;
    }

    private static List<Thread> startWorkers(final List<String> names, final int round, final Queue<Throwable> errors, final Work work) {
        final CyclicBarrier start   = new CyclicBarrier(THREADS);
        final List<Thread>  workers = new ArrayList<>();
        for (int index = 0; index < THREADS; index++) {
            final int          position = index;
            final List<String> order    = new ArrayList<>(names);
            Collections.shuffle(order, new Random(round * THREADS + index));
            final Thread worker = new Thread(() -> {
                try {
                    start.await();
                    work.run(position, order);
                } catch (Throwable e) {
                    errors.add(e);
                }
            }, "schema-lookup-" + round + "-" + index);
            worker.setDaemon(true);   // a thread stuck in a deadlock must not keep the test JVM alive
            worker.start();
            workers.add(worker);
        }
        return workers;
    }

    private static void awaitWorkers(final List<Thread> workers, final Queue<Throwable> errors, final int round) throws InterruptedException {
        final long deadline = System.currentTimeMillis() + 60_000;
        for (final Thread worker : workers) {
            worker.join(Math.max(1, deadline - System.currentTimeMillis()));
        }
        final ThreadMXBean threads    = ManagementFactory.getThreadMXBean();
        final long[]       deadlocked = threads.findDeadlockedThreads();
        assertNull("Round " + round + " deadlocked:\n" + (deadlocked == null ? "" : Arrays.toString(threads.getThreadInfo(deadlocked, true, true))), deadlocked);
        for (final Thread worker : workers) {
            assertFalse(worker.getName() + " did not finish", worker.isAlive());
        }
        assertTrue("Lookups failed: " + errors, errors.isEmpty());
    }

    private static List<String> loadSchema() throws Exception {
        final File schema = new File(SchemaLookupConcurrencyTest.class.getClassLoader().getResource("schemas/security/security.xsd").toURI());
        XFT.init(schema.getParentFile().getParentFile().getParent());
        // Field maps are cached by element name across loads; clear them so every load starts cold.
        ViewManager.FIELD_MAPS.clear();
        ViewManager.FIELD_NAMES.clear();
        final List<String> names = new ArrayList<>();
        for (final Object name : XFTMetaManager.GetElementNames()) {
            names.add((String) name);
        }
        return names;
    }

    private static String describe(final String name) throws Exception {
        final GenericWrapperElement element = GenericWrapperElement.GetElement(name);
        final StringBuilder         out     = new StringBuilder(element.getSQLName());
        out.append("\nfield names ").append(ViewManager.GetFieldNames(element, ViewManager.QUARANTINE, false, true));
        out.append("\nfield map ").append(new TreeMap<>(ViewManager.GetFieldMap(element, ViewManager.ACTIVE, true, true)));
        out.append("\nreferenced");
        for (final Object reference : element.getReferencedElements()) {
            final List<?> pair = (List<?>) reference;
            out.append(' ').append(((SchemaElementI) pair.get(0)).getFullXMLName()).append('@').append(pair.get(1));
        }
        final List<String> metaFields = new ArrayList<>();
        for (final Object field : element.getMetaFields().getCollection()) {
            metaFields.add(((MetaField) field).getXmlPathName());
        }
        Collections.sort(metaFields);
        out.append("\nmeta fields ").append(metaFields);
        out.append("\nextenders");
        for (final SchemaElementI extender : element.getPossibleExtenders()) {
            out.append(' ').append(extender.getFullXMLName());
        }
        return out.toString();
    }
}
