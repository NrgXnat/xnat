package org.nrg.xnat.utils;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.Striped;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Provides a class that locks a file so that only a single thread or
 * process may access it.  Note that all the classes have to try to get the
 * fileLock, this is simply a synchronization tool and will not work if some
 * threads or processes do not first try to acquire the fileLock.
 *
 * <p>
 * <p>
 * The inter-process locking is done using Java NIO file locks on a dummy file (so we
 * don't have to open a channel prior to having the lock). Because "file
 * locks are held on behalf of the entire Java virtual machine. They are not
 * suitable for controlling access to a file by multiple threads within the same
 * virtual machine", we need another solution for intra-process (aka inter-thread) locking.
 *
 * <p>
 * <p>
 * The intra-process (aka inter-thread) locking is done with a Java ReadWriteLock,
 * shared between instances of this ThreadAndProcessFileLock class. In order to achieve
 * this sharing, a static mapping of files -> ThreadAndProcessFileLock instances has to
 * be maintained and synchronized on.
 *
 * <p>
 * <p>
 * See SunLabsAST's
 * <a href="https://github.com/SunLabsAST/Minion/blob/master/src/com/sun/labs/minion/util/FileLock.java">FileLock.java</a>
 */
@Slf4j
public class ThreadAndProcessFileLock {

    // The following static methods are used to associate a File with a ThreadAndProcessFileLock instance so that
    // we can keep our actual concurrency control objects (within ThreadAndProcessFileLock) specific to files
    private final static Map<File, ThreadAndProcessFileLock> FILE_LOCKS = new HashMap<>();
    private final static Map<File, AtomicInteger> ACCESSOR_COUNT = new HashMap<>();

    // Striped lock for protecting access to the static map of file locks
    private final static int NUM_LOCKS = 128;
    private final static Striped<Lock> STRIPED_MAP_LOCK = Striped.lock(NUM_LOCKS);

    private static SiteConfigPreferences PREFERENCES;

    public static final String LOCKFILE_PREFIX = ".~lock-";

    public static ThreadAndProcessFileLock getThreadAndProcessFileLock(File file,
                                                                       boolean readOnly)
            throws IOException {
        Lock mapLock = STRIPED_MAP_LOCK.get(file);
        mapLock.lock();
        try {
            ThreadAndProcessFileLock lock;
            if (FILE_LOCKS.containsKey(file)) {
                // Another thread is accessing this file, too. Use its ReadWriteLock
                lock = new ThreadAndProcessFileLock(FILE_LOCKS.get(file), file, readOnly);
                ACCESSOR_COUNT.get(file).incrementAndGet();
            } else {
                lock = new ThreadAndProcessFileLock(file, readOnly);
                FILE_LOCKS.put(file, lock);
                ACCESSOR_COUNT.put(file, new AtomicInteger(1));
            }
            return lock;
        } finally {
            mapLock.unlock();
        }
    }

    public static void removeThreadAndProcessFileLock(File file) {
        Lock mapLock = STRIPED_MAP_LOCK.get(file);
        mapLock.lock();
        try {
            if (!FILE_LOCKS.containsKey(file)) {
                return;
            }
            if (ACCESSOR_COUNT.get(file).decrementAndGet() == 0) {
                try {
                    Path dummyFilePath = FILE_LOCKS.get(file).dummyFile.toPath();
                    Files.deleteIfExists(dummyFilePath);
                    deleteEmptyParentDirectories(dummyFilePath.getParent(), FILE_LOCKS.get(file).getCachePath());
                } catch (IOException e) {
                    log.error("Unable to delete dummy file", e);
                }
                FILE_LOCKS.remove(file);
                ACCESSOR_COUNT.remove(file);
            }
        } finally {
            mapLock.unlock();
        }
    }

    /**
     * Recursively deletes empty parent directories up to (but not including) the top-level directory.
     *
     * @param directory the directory to check and potentially delete
     * @param topLevelPath the top-level path to stop at (e.g., the cache path)
     */
    private static void deleteEmptyParentDirectories(Path directory, String topLevelPath) {
        if (directory == null) {
            return;
        }

        Path topLevel = Paths.get(topLevelPath, "file-locks");

        // Stop if we've reached or gone above the top-level directory
        if (!directory.startsWith(topLevel) || directory.equals(topLevel)) {
            return;
        }

        try {
            // Try to delete the directory - this will only succeed if it's empty
            if (Files.isDirectory(directory) && Files.deleteIfExists(directory)) {
                // If successful, recursively try to delete the parent
                deleteEmptyParentDirectories(directory.getParent(), topLevelPath);
            }
        } catch (IOException e) {
            // Directory is not empty or cannot be deleted, stop here
            log.debug("Could not delete directory {}: {}", directory, e.getMessage());
        }
    }

    // The dummy file on which we synchronize for inter-process reading & writing
    private File dummyFile;
    private RandomAccessFile dummyRAF;
    private FileChannel channel;

    // Are we just trying to read?
    private boolean readOnly;

    // For managing inter-process reading & writing
    @Nullable
    private FileLock fileLock;

    // For managing inter-thread reading & writing, shared between instances
    private final ReadWriteLock mutex;

    // The ReadLock or WriteLock for the current instance
    private final Lock threadLock;

    /**
     * Creates a ThreadAndProcessFileLock that will wait timeout units to acquire the read or write
     * (per readOnly parameter) java.nio.channels.FileLock on the channel. This <em><b>does not</b></em>
     * fileLock the file; use the <code>acquireLock</code> method for that.
     *
     * @param file     the file
     * @param readOnly Is this a readonly lock?
     */
    private ThreadAndProcessFileLock(File file, boolean readOnly) throws IOException {
        this(null, file, readOnly);
    }

    /**
     * Creates a ThreadAndProcessFileLock that will share a ReadWriteLock with another ThreadAndProcessFileLock instance
     *
     * @param file     the file
     * @param l        the ThreadAndProcessFileLock whose ReadWriteLock we want to share
     * @param readOnly Is this a readonly lock?
     */
    private ThreadAndProcessFileLock(ThreadAndProcessFileLock l, File file, boolean readOnly) throws IOException {
        // Have to share the mutex for thread locking
        this.mutex = l == null ? new ReentrantReadWriteLock() : l.mutex;
        this.threadLock = readOnly ? mutex.readLock() : mutex.writeLock();
        this.readOnly = readOnly;

        // Open channel on the dummy file to synchronize across processes
        this.dummyFile = Paths.get(getCachePath(), "file-locks", file.getParent(), LOCKFILE_PREFIX + file.getName()).toFile();
        openDummyRAF(true);
        this.channel = dummyRAF.getChannel();

        // Store the inter-process file channel file lock so we can unlock it
        this.fileLock = null;
    }

    private String getCachePath() {
        final SiteConfigPreferences preferences = getSiteConfigPreferences();
        return preferences == null ? System.getProperty("java.io.tmpdir") : preferences.getCachePath();
    }

    private SiteConfigPreferences getSiteConfigPreferences() {
        if (PREFERENCES == null) {
            PREFERENCES = XDAT.getSiteConfigPreferences();
        }
        return PREFERENCES;
    }

    /**
     * Open Random Access File for dummyFile, retry once if this fails in case the issue is another process deleting the
     * dummyFile at the exact time we're trying to open it
     *
     * @param retry catch one FNF and retry
     * @throws FileNotFoundException if the given file object does not denote an existing, writable regular file and a new regular file of
     *                               that name cannot be created, or if some other error occurs while opening or creating the file
     */
    private void openDummyRAF(boolean retry) throws FileNotFoundException {
        try {
            final Path parentPath = dummyFile.toPath().getParent();
            if (parentPath != null && !Files.exists(parentPath)) {
                Files.createDirectories(parentPath);
            }
            dummyRAF = new RandomAccessFile(dummyFile, "rw");
        } catch (FileNotFoundException e) {
            if (retry) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e2) {
                    // ignore
                }
                openDummyRAF(false);
            } else {
                throw e;
            }
        } catch (IOException e) {
            if (retry) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e2) {
                    // ignore
                }
                openDummyRAF(false);
            } else {
                throw new RuntimeException("An error occurred trying to create a lock file", e);
            }
        }
    }

    /**
     * Acquires the fileLock on the file.  This code will try repeatedly to
     * acquire the fileLock.
     *
     * @param timeout the timeout to use when trying to acquire the fileLock
     * @param units   the units for the timeout.  This will be converted to milliseconds, so beware of truncation!
     * @throws IOException when the fileLock cannot be acquired within
     *                     the specified timeout or if there is an I/O error while obtaining
     *                     the fileLock
     */
    public void tryLock(long timeout, TimeUnit units) throws IOException {
        long timeoutTime = System.currentTimeMillis() + units.toMillis(timeout);
        do {
            // Try for both the thread lock and the file lock
            try {
                if (threadLock.tryLock(250L, TimeUnit.MILLISECONDS)) {
                    if (tryFileLock()) {
                        return;
                    }
                    threadLock.unlock();
                }
            } catch (InterruptedException e) {
                // Ignore
            }
        } while (System.currentTimeMillis() <= timeoutTime);

        dummyRAF.close();
        throw new IOException("Unable to acquire thread and process locks");
    }

    /**
     * Release the fileLock on the file, if the calling thread currently holds it.
     *
     * @throws IOException when the fileLock cannot be released.
     */
    public void unlock() throws IOException {
        if (fileLock != null) {
            try {
                fileLock.release(); // Is released when we close the channel, but just in case...
                fileLock = null;
            } catch (ClosedChannelException e) {
                fileLock = null;
            } catch (IOException e) {
                throw new IOException("Unable to release fileLock " + fileLock, e);
            }
        }

        try {
            if (channel.isOpen()) channel.close();
            dummyRAF.close();
        } catch (IOException e) {
            // ignore, just trying to cleanup
        }

        threadLock.unlock();
    }

    /**
     * Try to get the fileLock on the channel.
     *
     * @return <code>true</code> if we got the fileLock, <code>false</code>
     * otherwise.
     */
    public boolean tryFileLock() throws IOException {
        if (channel == null) return false;
        try {
            fileLock = channel.tryLock(0L, Long.MAX_VALUE, readOnly);
        } catch (OverlappingFileLockException ole) {
            // we already have the lock in this JVM
            return true;
        } catch (Error err) {
            // XNAT-8495 The java 8 version of sun.nio.ch.FileKey::create catches IOException and wraps it in an Error
            // TODO We can remove this after updating from java 8
            if (err.getCause() instanceof IOException) {
                throw (IOException) err.getCause();
            }
            throw err;
        }
        return fileLock != null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("readonly", readOnly)
                .add("fileLock", fileLock)
                .add("mutex", mutex)
                .add("threadlock", threadLock)
                .toString();
    }
}
