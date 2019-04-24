package org.nrg.xnat.utils;

/**
 * Provides a class that locks a file so that only a single thread or
 * process may access it.  Note that all the classes have to try to get the
 * fileLock, this is simply a synchronization tool and will not work if some
 * threads or processes do not first try to acquire the fileLock.
 *
 * <p>
 *
 * The inter-process locking is done using Java NIO file locks. However, "file
 * locks are held on behalf of the entire Java virtual machine. They are not
 * suitable for controlling access to a file by multiple threads within the same
 * virtual machine.
 *
 * <p>
 *
 * The intra-process (aka inter-thread) locking is done with a Java ReadWriteLock,
 * shared between instances of this ThreadAndProcessFileLock class. In order to achieve
 * this sharing, a static mapping of files -> ThreadAndProcessFileLock instances has to
 * be maintained and synchronized on.
 *
 */

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
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
                                                                       FileChannel channel,
                                                                       boolean readOnly) {
        ThreadAndProcessFileLock lock;
        if (lockMap.containsKey(file)) {
            // Another thread is accessing this file, too. Use its ReadWriteLock
            lock = new ThreadAndProcessFileLock(lockMap.get(file), channel, readOnly);
            accessorCount.get(file).incrementAndGet();
        } else {
            lock = new ThreadAndProcessFileLock(channel, readOnly);
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
            lockMap.remove(file);
            accessorCount.remove(file);
        }
    }

    // The channel for the java.nio.FileLock
    private FileChannel channel;

    // Are we just trying to read?
    private boolean readOnly;

    // For managing inter-process reading & writing
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
     * @param channel The channel that we want to lock
     * @param readOnly Is this a readonly lock?
     */
    public ThreadAndProcessFileLock(FileChannel channel, boolean readOnly) {
        this(null, channel, readOnly);
    }

    /**
     * Creates a ThreadAndProcessFileLock that will share a ReadWriteLock with another ThreadAndProcessFileLock instance
     *
     * @param l the ThreadAndProcessFileLock whose ReadWriteLock we want to share
     * @param channel The channel that we want to lock
     * @param readOnly Is this a readonly lock?
     */
    public ThreadAndProcessFileLock(ThreadAndProcessFileLock l, FileChannel channel, boolean readOnly) {
        this.mutex = l == null ? new ReentrantReadWriteLock() : l.mutex;
        this.channel = channel;
        this.readOnly = readOnly;
        this.threadLock = readOnly ? mutex.readLock() : mutex.writeLock();
        this.fileLock = null;
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

        throw new IOException("Unable to acquire thread and process locks");
    }

    /**
     * Release the fileLock on the file, if the calling thread currently holds it.
     *
     * @throws IOException when the fileLock cannot be released.
     */
    public void unlock() throws IOException {
        threadLock.unlock();

        if (fileLock != null) {
            try {
                fileLock.release();
                fileLock = null;
            } catch (ClosedChannelException e) {
                fileLock = null;
            } catch (IOException e) {
                throw new IOException("Unable to release fileLock " + fileLock, e);
            }
        }
    }

    /**
     * Try to get the fileLock on the channel.
     * @return <code>true</code> if we got the fileLock, <code>false</code>
     * otherwise.
     */
    public boolean tryFileLock() throws IOException {
        try {
            fileLock = channel.tryLock(0L, Long.MAX_VALUE, readOnly);
        } catch (OverlappingFileLockException ole) {
            fileLock = null;
            return false;
        }
        return fileLock != null;
    }

}


