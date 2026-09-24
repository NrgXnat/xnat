package org.nrg.xnat.utils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.nrg.xdat.preferences.SiteConfigPreferences;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Exercises the static bookkeeping in {@link ThreadAndProcessFileLock} from many threads at once. Threads working on
 * <em>different</em> files must not interfere with each other: previously the lock map was a plain HashMap guarded
 * by a striped lock, so threads on different files could modify it concurrently and lose entries, which surfaced as
 * a NullPointerException in {@link ThreadAndProcessFileLock#removeThreadAndProcessFileLock(File)} during imports.
 */
public class TestThreadAndProcessFileLockConcurrency {
    private static final int THREADS          = 32;
    private static final int FILES_PER_THREAD = 25;
    private static final int ITERATIONS       = 150;
    private static final int SHARED_FILES     = 4;

    @ClassRule
    public static final TemporaryFolder TEMP = new TemporaryFolder();

    private static Object savedPreferences;

    @BeforeClass
    public static void setup() throws Exception {
        // Point the lock files at a writable temp directory instead of whatever cache path a prior test configured
        final Field prefsField = getPreferencesField();
        savedPreferences = prefsField.get(null);
        final SiteConfigPreferences mockPrefs = Mockito.mock(SiteConfigPreferences.class);
        Mockito.when(mockPrefs.getCachePath()).thenReturn(TEMP.newFolder("cache").getAbsolutePath());
        prefsField.set(null, mockPrefs);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        getPreferencesField().set(null, savedPreferences);
    }

    /**
     * Each thread repeatedly takes and releases locks on its own set of files, holding several at once so the map
     * stays populated. No file is shared between threads, so no thread should ever see another thread's state.
     */
    @Test(timeout = 180_000)
    public void testConcurrentAccessToDifferentFiles() throws Throwable {
        final File root = TEMP.newFolder();
        final List<File> allFiles = new ArrayList<>();
        final List<Callable<Void>> tasks = new ArrayList<>();
        for (int thread = 0; thread < THREADS; thread++) {
            final List<File> files = createFiles(root, "thread-" + thread, FILES_PER_THREAD);
            allFiles.addAll(files);
            tasks.add(() -> {
                for (int iteration = 0; iteration < ITERATIONS; iteration++) {
                    lockAndReleaseAll(files, true);
                }
                return null;
            });
        }

        runConcurrently(tasks);

        assertNoEntriesRemainFor(allFiles);
    }

    /**
     * Writers contend for a few shared files while other threads churn through their own files. A write lock must
     * stay exclusive: if churn on other files drops the shared file's entry from the map, the next writer creates a
     * new ReadWriteLock and two threads end up "holding" the same write lock.
     */
    @Test(timeout = 180_000)
    public void testWriteLocksStayExclusiveWhileOtherFilesChurn() throws Throwable {
        final File                     root          = TEMP.newFolder();
        final List<File>               sharedFiles   = createFiles(root, "shared", SHARED_FILES);
        final List<File>               allFiles      = new ArrayList<>(sharedFiles);
        final Map<File, AtomicInteger> activeWriters = new ConcurrentHashMap<>();
        final AtomicInteger            violations    = new AtomicInteger();
        sharedFiles.forEach(file -> activeWriters.put(file, new AtomicInteger()));

        final List<Callable<Void>> tasks = new ArrayList<>();
        for (int thread = 0; thread < THREADS; thread++) {
            if (thread % 2 == 0) {
                final int offset = thread;
                tasks.add(() -> {
                    for (int iteration = 0; iteration < ITERATIONS * 5; iteration++) {
                        final File file = sharedFiles.get((offset + iteration) % SHARED_FILES);
                        final ThreadAndProcessFileLock lock = ThreadAndProcessFileLock.getThreadAndProcessFileLock(file, false);
                        try {
                            lock.tryLock(1L, TimeUnit.MINUTES);
                            try {
                                if (activeWriters.get(file).incrementAndGet() > 1) {
                                    violations.incrementAndGet();
                                }
                                Thread.yield();
                                activeWriters.get(file).decrementAndGet();
                            } finally {
                                lock.unlock();
                            }
                        } finally {
                            ThreadAndProcessFileLock.removeThreadAndProcessFileLock(file);
                        }
                    }
                    return null;
                });
            } else {
                final List<File> files = createFiles(root, "thread-" + thread, FILES_PER_THREAD);
                allFiles.addAll(files);
                tasks.add(() -> {
                    for (int iteration = 0; iteration < ITERATIONS; iteration++) {
                        lockAndReleaseAll(files, true);
                    }
                    return null;
                });
            }
        }

        runConcurrently(tasks);

        assertEquals("More than one thread held the write lock on the same file at the same time", 0, violations.get());
        assertNoEntriesRemainFor(allFiles);
    }

    /**
     * Takes a lock on each file (keeping the map entry but closing the lock file channel), then releases them all.
     * This mirrors how CatalogUtils uses the lock, while keeping several entries in the map at once.
     */
    private static void lockAndReleaseAll(final List<File> files, final boolean readOnly) throws Exception {
        final List<File> acquired = new ArrayList<>();
        try {
            for (final File file : files) {
                final ThreadAndProcessFileLock lock = ThreadAndProcessFileLock.getThreadAndProcessFileLock(file, readOnly);
                acquired.add(file);
                lock.tryLock(1L, TimeUnit.MINUTES);
                lock.unlock();
            }
        } finally {
            for (final File file : acquired) {
                ThreadAndProcessFileLock.removeThreadAndProcessFileLock(file);
            }
        }
    }

    /**
     * Starts all tasks at the same moment and waits for them. If any task fails, its original exception is rethrown
     * so the test failure shows the real error rather than an ExecutionException wrapper.
     */
    private static void runConcurrently(final List<Callable<Void>> tasks) throws Throwable {
        final ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        final CountDownLatch  start    = new CountDownLatch(1);
        try {
            final List<Future<Void>> futures = new ArrayList<>();
            for (final Callable<Void> task : tasks) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return task.call();
                }));
            }
            start.countDown();
            for (final Future<Void> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static List<File> createFiles(final File root, final String prefix, final int count) {
        final List<File> files = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            files.add(new File(root, prefix + "-file-" + index + ".xml"));
        }
        return files;
    }

    /**
     * Every get was matched by a remove, so none of the given files should still have an entry in the lock map. This
     * looks up each file rather than walking the map, since other test classes may have their own entries.
     */
    private static void assertNoEntriesRemainFor(final List<File> files) throws Exception {
        final Field field = ThreadAndProcessFileLock.class.getDeclaredField("FILE_LOCKS");
        field.setAccessible(true);
        final Map<?, ?> fileLocks = (Map<?, ?>) field.get(null);
        final List<File> leftovers = files.stream().filter(fileLocks::containsKey).collect(Collectors.toList());
        assertTrue("Lock map still has entries after every lock was released: " + leftovers, leftovers.isEmpty());
    }

    private static Field getPreferencesField() throws NoSuchFieldException {
        final Field field = ThreadAndProcessFileLock.class.getDeclaredField("PREFERENCES");
        field.setAccessible(true);
        return field;
    }
}
