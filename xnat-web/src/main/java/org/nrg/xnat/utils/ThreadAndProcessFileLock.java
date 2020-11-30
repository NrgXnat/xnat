package org.nrg.xnat.utils;

/**
 * Provides a class that locks a file so that only a single thread or
 * process may access it.  Note that all the classes have to try to get the
 * fileLock, this is simply a synchronization tool and will not work if some
 * threads or processes do not first try to acquire the fileLock.
 *
 * <p>
 *
 * The inter-process locking is done using Java NIO file locks on a dummy file (so we
 * don't have to open a channel prior to having the lock). Because "file
 * locks are held on behalf of the entire Java virtual machine. They are not
 * suitable for controlling access to a file by multiple threads within the same
 * virtual machine", we need another solution for intra-process (aka inter-thread) locking.
 *
 * <p>
 *
 * The intra-process (aka inter-thread) locking is done with a Java ReadWriteLock,
 * shared between instances of this ThreadAndProcessFileLock class. In order to achieve
 * this sharing, a static mapping of files -> ThreadAndProcessFileLock instances has to
 * be maintained and synchronized on.
 *
 * <p>
 *
 * See also https://github.com/SunLabsAST/Minion/blob/master/src/com/sun/labs/minion/util/FileLock.java
 *
 */

import com.google.common.base.MoreObjects;
import lombok.extern.slf4j.Slf4j;

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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class ThreadAndProcessFileLock {

    // The following static methods are used to associate a File with a ThreadAndProcessFileLock instance so that
    // we can keep our actual concurrency control objects (within ThreadAndProcessFileLock) specific to files
    private final static Map<File, ThreadAndProcessFileLock> lockMap = new HashMap<>();
    private final static Map<File, AtomicInteger> accessorCount = new HashMap<>();

    public synchronized static ThreadAndProcessFileLock getThreadAndProcessFileLock(File file,
                                                                                    boolean readOnly)
            throws IOException {
        ThreadAndProcessFileLock lock;
        if (lockMap.containsKey(file)) {
            // Another thread is accessing this file, too. Use its ReadWriteLock
            lock = new ThreadAndProcessFileLock(lockMap.get(file), file, readOnly);
            accessorCount.get(file).incrementAndGet();
        } else {
            lock = new ThreadAndProcessFileLock(file, readOnly);
            lockMap.put(file, lock);
            accessorCount.put(file, new AtomicInteger(1));
        }
        return lock;
    }

    public synchronized static void removeThreadAndProcessFileLock(File file) {
        if (!lockMap.containsKey(file)) {
            return;
        }
        if (accessorCount.get(file).decrementAndGet() == 0) {
            try {
                Files.deleteIfExists(lockMap.get(file).dummyFile.toPath());
            } catch (IOException e) {
                log.error("Unable to delete dummy file", e);
            }
            lockMap.remove(file);
            accessorCount.remove(file);
        }
    }

    // The dummy file on which we synchronize for inter-process reading & writing
    private File dummyFile;
    private RandomAccessFile dummyRAF;
    private FileChannel channel;

    // Are we just trying to read?
    private boolean readOnly;

    // For managing inter-process reading & writing
    @Nullable private FileLock fileLock;

    // For managing inter-thread reading & writing, shared between instances
    private final ReadWriteLock mutex;

    // The ReadLock or WriteLock for the current instance
    private final Lock threadLock;

    /**
     * Creates a ThreadAndProcessFileLock that will wait timeout units to acquire the read or write
     * (per readOnly parameter) java.nio.channels.FileLock on the channel. This <em><b>does not</b></em>
     * fileLock the file; use the <code>acquireLock</code> method for that.
     *
     * @param file the file
     * @param readOnly Is this a readonly lock?
     */
    private ThreadAndProcessFileLock(File file, boolean readOnly) throws IOException {
        this(null, file, readOnly);
    }

    /**
     * Creates a ThreadAndProcessFileLock that will share a ReadWriteLock with another ThreadAndProcessFileLock instance
     *
     * @param file the file
     * @param l the ThreadAndProcessFileLock whose ReadWriteLock we want to share
     * @param readOnly Is this a readonly lock?
     */
    private ThreadAndProcessFileLock(ThreadAndProcessFileLock l, File file, boolean readOnly) throws IOException {
        // Have to share the mutex for thread locking
        this.mutex = l == null ? new ReentrantReadWriteLock() : l.mutex;
        this.threadLock = readOnly ? mutex.readLock() : mutex.writeLock();
        this.readOnly = readOnly;

        // Open channel on the dummy file to synchronize across processes
        this.dummyFile = new File(file.getParent(), "." + file.getName() + ".lock");
        openDummyRAF(true);
        this.channel = dummyRAF.getChannel();

        // Store the inter-process file channel file lock so we can unlock it
        this.fileLock = null;
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
        }
    }

    /**
     * Acquires the fileLock on the file.  This code will try repeatedly to
     * acquire the fileLock.
     * @param timeout the timeout to use when trying to acquire the fileLock
     * @param units the units for the timeout.  This will be converted to milliseconds, so beware of truncation!
     *
     * @throws IOException when the fileLock cannot be acquired within
     * the specified timeout or if there is an I/O error while obtaining
     * the fileLock
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


