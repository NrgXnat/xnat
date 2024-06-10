package org.nrg.dcm.scp;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che2.net.Association;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Executor to use for threads supporting {@link DicomSCP} instances. This isolates the long-running daemon threads for
 * receiving DICOM from async tasks and other standard thread pool usages. Info on current active threads can be found
 * through the internal <pre>ThreadGroup</pre> instance and a helper class like <pre>ThreadUtils</pre> from Apache
 * Commons Lang3.
 * <p>
 * Note that the constructor for this class takes an instance of <pre>ExecutorService</pre>. This class manages threads
 * for long-running threads <i>only</i>, but is also referenced from the underlying dcm4che implementation to create
 * threads for tasks to manage network connections, specifically dcm4che <pre>Association</pre> objects. These tasks are
 * in-line with the standard use of a shared thread pool, so the <pre>ExecutorService</pre> should be a thread pool or
 * similar type of service.
 */
@Component
@Slf4j
public class DicomScpExecutor implements Executor {
    private static final String THREAD_NAME_PREFIX = "dicom-scp";

    @Getter
    private final ThreadGroup   threadGroup = new ThreadGroup(THREAD_NAME_PREFIX);
    private final AtomicInteger threadId    = new AtomicInteger();

    private final ExecutorService _executorService;

    /**
     * Creates a new instance of this class.
     *
     * @param executorService The executor service to use for handling DICOM association objects.
     */
    public DicomScpExecutor(final ExecutorService executorService) {
        _executorService = executorService;
    }

    /**
     * Handles executing the submitted <pre>Runnable</pre> instance. <i>How</i> that instance is handled depends on the
     * instance class. For instances of the dcm4che <pre>Association</pre> class, this method submits the object to the
     * injected <pre>ExecutorService</pre> instance, which should be a thread pool. Otherwise, this method creates a new
     * thread in the internal thread group with the name <pre>dicom-scp-</pre><i>N</i>, where <i>N</i> is the number of
     * threads that have been created by this executor over its lifetime.
     *
     * @param runnable The <pre>Runnable</pre> task to be executed.
     */
    @Override
    public void execute(final @Nonnull Runnable runnable) {
        if (runnable instanceof Association) {
            _executorService.submit(runnable);
        } else {
            final String threadName = THREAD_NAME_PREFIX + "-" + threadId.getAndIncrement();
            final Thread thread     = new Thread(threadGroup, runnable, threadName);
            thread.setDaemon(true);
            thread.start();
            log.info("Just created new thread {}. Active count from thread group is {}.", threadName, threadGroup.activeCount());
        }
    }
}
