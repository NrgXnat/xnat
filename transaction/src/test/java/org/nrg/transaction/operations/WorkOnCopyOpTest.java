package org.nrg.transaction.operations;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.transaction.TransactionException;
import org.nrg.transaction.TransactionRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

public class WorkOnCopyOpTest {

    private static final byte[] ORIGINAL = "original bytes".getBytes(StandardCharsets.UTF_8);
    private static final byte[] REPLACED = "replacement, which is longer than the original".getBytes(StandardCharsets.UTF_8);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void replacesTheSourceWithWhatTheOperationWrote() throws Throwable {
        final File source  = write(folder.newFolder("data"), "1.dcm", ORIGINAL);
        final File staging = folder.newFolder("staging");

        final String result = new TransactionRunner<String>().runTransaction(
                new WorkOnCopyOp<>(source, staging, writing(REPLACED, "done")));

        assertEquals("done", result);
        assertArrayEquals("the source should hold the operation's output", REPLACED, Files.readAllBytes(source.toPath()));
        assertEquals("nothing should be left in the staging directory", 0, staging.list().length);
    }

    @Test
    public void leavesTheSourceUntouchedAndCleansUpWhenTheOperationFails() throws Exception {
        final File source  = write(folder.newFolder("data"), "1.dcm", ORIGINAL);
        final File staging = folder.newFolder("staging");

        final CallOnFile<String> failing = new CallOnFile<String>() {
            @Override
            public String call() throws Exception {
                Files.write(getFile().toPath(), "partial".getBytes(StandardCharsets.UTF_8));
                throw new IOException("boom");
            }
        };

        try {
            new TransactionRunner<String>().runTransaction(new WorkOnCopyOp<>(source, staging, failing));
            fail("the failure should have propagated");
        } catch (TransactionException expected) {
            // fine
        }

        assertArrayEquals("the source must not change when the operation fails", ORIGINAL, Files.readAllBytes(source.toPath()));
        assertEquals("the partial staged file should have been rolled back", 0, staging.list().length);
    }

    /**
     * An anonymization that rejects or fails on an object reports that as its result and writes no
     * staged version. The source must then stay exactly as it was: putting an absent (or empty)
     * staged file in its place would destroy the object.
     */
    @Test
    public void leavesTheSourceUntouchedWhenTheOperationProducedNothing() throws Throwable {
        final File source  = write(folder.newFolder("data"), "1.dcm", ORIGINAL);
        final File staging = folder.newFolder("staging");

        final CallOnFile<String> producingNothing = new CallOnFile<String>() {
            @Override
            public String call() {
                return "rejected";
            }
        };

        final String result = new TransactionRunner<String>().runTransaction(new WorkOnCopyOp<>(source, staging, producingNothing));

        assertEquals("the operation's result still comes back", "rejected", result);
        assertArrayEquals("the source must be untouched when nothing was staged", ORIGINAL, Files.readAllBytes(source.toPath()));
        assertEquals(0, staging.list().length);
    }

    @Test
    public void keepsConcurrentlyStagedFilesWithTheSameNameApart() throws Exception {
        final File first   = write(folder.newFolder("scan1"), "1.dcm", ORIGINAL);
        final File second  = write(folder.newFolder("scan2"), "1.dcm", ORIGINAL);
        final File staging = folder.newFolder("staging");
        final byte[] firstOutput  = "first".getBytes(StandardCharsets.UTF_8);
        final byte[] secondOutput = "second".getBytes(StandardCharsets.UTF_8);

        // Both operations hold their staged file open at the same moment, so a collision in the
        // staging name would have one overwrite the other.
        final CyclicBarrier bothStaged = new CyclicBarrier(2);
        final CallOnFile<File> firstCall  = writingThenWaiting(firstOutput, bothStaged);
        final CallOnFile<File> secondCall = writingThenWaiting(secondOutput, bothStaged);

        final ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            final List<Future<File>> staged = executor.invokeAll(List.of(
                    () -> run(new WorkOnCopyOp<>(first, staging, firstCall)),
                    () -> run(new WorkOnCopyOp<>(second, staging, secondCall))));
            final File firstStaged  = staged.get(0).get(30, TimeUnit.SECONDS);
            final File secondStaged = staged.get(1).get(30, TimeUnit.SECONDS);
            assertNotEquals("same-named sources must stage under different names", firstStaged, secondStaged);
        } finally {
            executor.shutdownNow();
        }

        assertArrayEquals(firstOutput, Files.readAllBytes(first.toPath()));
        assertArrayEquals(secondOutput, Files.readAllBytes(second.toPath()));
        assertEquals(0, staging.list().length);
    }

    /**
     * A failed replacement can leave the staged file as the only intact copy of the data -- the
     * non-atomic fallback deletes the source before copying over it -- so rollback must not delete
     * it, and the failure must say where it is.
     */
    @Test
    public void keepsTheStagedFileWhenTheReplacementItselfFails() throws Exception {
        final File dataDir = folder.newFolder("data");
        final File source  = write(dataDir, "1.dcm", ORIGINAL);
        final File staging = folder.newFolder("staging");
        assumeTrue("test needs a directory the process cannot rename into", dataDir.setWritable(false));
        assumeFalse("running with permissions that ignore the write bit", dataDir.canWrite());
        try {
            new TransactionRunner<String>().runTransaction(new WorkOnCopyOp<>(source, staging, writing(REPLACED, "unused")));
            fail("the replacement should have failed");
        } catch (TransactionException expected) {
            assertTrue("the failure should name the preserved staged file: " + expected.getMessage(),
                       String.valueOf(expected.getMessage()).contains("preserved at"));
        } finally {
            assertTrue(dataDir.setWritable(true));
        }

        assertArrayEquals("the source must be untouched by a failed replacement", ORIGINAL, Files.readAllBytes(source.toPath()));
        final File[] staged = staging.listFiles();
        assertNotNull(staged);
        assertEquals("the staged file must survive rollback: it may be the only intact copy", 1, staged.length);
        assertArrayEquals("and it must hold the operation's output", REPLACED, Files.readAllBytes(staged[0].toPath()));
    }

    @Test
    public void rollbackIsANoOpBeforeAnythingWasStaged() throws Exception {
        final File source = write(folder.newFolder("data"), "1.dcm", ORIGINAL);
        new WorkOnCopyOp<>(source, folder.newFolder("staging"), writing(REPLACED, "unused")).rollback();
        assertTrue(source.exists());
        assertFalse(new File(folder.getRoot(), "staging/1.dcm").exists());
    }

    /** TransactionException extends Throwable, which a Callable cannot throw. */
    private static File run(final WorkOnCopyOp<File> op) throws Exception {
        try {
            return new TransactionRunner<File>().runTransaction(op);
        } catch (TransactionException e) {
            throw new IllegalStateException(e);
        }
    }

    private static File write(final File directory, final String name, final byte[] content) throws IOException {
        final File file = new File(directory, name);
        Files.write(file.toPath(), content);
        return file;
    }

    private static <T> CallOnFile<T> writing(final byte[] content, final T result) {
        return new CallOnFile<T>() {
            @Override
            public T call() throws Exception {
                Files.write(getFile().toPath(), content);
                return result;
            }
        };
    }

    private static CallOnFile<File> writingThenWaiting(final byte[] content, final CyclicBarrier barrier) {
        return new CallOnFile<File>() {
            @Override
            public File call() throws Exception {
                Files.write(getFile().toPath(), content);
                barrier.await(30, TimeUnit.SECONDS);
                return getFile();
            }
        };
    }
}
