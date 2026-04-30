/*
 * framework: org.nrg.framework.concurrency.LoggingThreadPoolExecutor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.concurrency;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.utilities.StreamUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An override of the standard thread pool executor class to allow for capturing messaging from exceptions that occur
 * inside the thread pool.
 */
@SuppressWarnings("unused")
@Slf4j
public class LoggingThreadPoolExecutor extends ThreadPoolExecutor {
    private static final RejectedExecutionHandler DEFAULT_HANDLER      = new AbortPolicy();
    private static final AtomicBoolean            USE_LOGGING_EXECUTOR = new AtomicBoolean(false);

    private final List<ExceptionHandler> _handlers = new ArrayList<>();

    public LoggingThreadPoolExecutor() {
        this(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    public LoggingThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), DEFAULT_HANDLER);
    }

    public LoggingThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, DEFAULT_HANDLER);
    }

    public LoggingThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), handler);
    }

    public LoggingThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory, final RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public static void setUseLoggingExecutor(final boolean useLoggingExecutor) {
        USE_LOGGING_EXECUTOR.set(useLoggingExecutor);
    }

    public static ExecutorService newCachedThreadPool() {
        return newCachedThreadPool(null);
    }

    public static ExecutorService newCachedThreadPool(final ExceptionHandler first, final ExceptionHandler... others) {
        return newCachedThreadPool(StreamUtils.asList(first, others));
    }

    public static ExecutorService newCachedThreadPool(final List<? extends ExceptionHandler> handlers) {
        if (USE_LOGGING_EXECUTOR.get()) {
            final LoggingThreadPoolExecutor executor = new LoggingThreadPoolExecutor();
            if (handlers != null) {
                executor.setExceptionHandlers(handlers);
            }
            return executor;
        }
        return Executors.newCachedThreadPool();
    }

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return newFixedThreadPool(nThreads, null);
    }

    public static ExecutorService newFixedThreadPool(int nThreads, final ExceptionHandler first, final ExceptionHandler... others) {
        return newFixedThreadPool(nThreads, StreamUtils.asList(first, others));
    }

    public static ExecutorService newFixedThreadPool(int nThreads, final List<? extends ExceptionHandler> handlers) {
        if (USE_LOGGING_EXECUTOR.get()) {
            final LoggingThreadPoolExecutor executor = new LoggingThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
            if (handlers != null) {
                executor.setExceptionHandlers(handlers);
            }
            return executor;
        }
        return Executors.newFixedThreadPool(nThreads);
    }

    public static ExecutorService newSingleThreadExecutor() {
        return newSingleThreadExecutor(null);
    }

    public static ExecutorService newSingleThreadExecutor(final ExceptionHandler first, final ExceptionHandler... others) {
        return newSingleThreadExecutor(StreamUtils.asList(first, others));
    }

    public static ExecutorService newSingleThreadExecutor(final List<? extends ExceptionHandler> handlers) {
        if (USE_LOGGING_EXECUTOR.get()) {
            final LoggingThreadPoolExecutor executor = new LoggingThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
            if (handlers != null) {
                executor.setExceptionHandlers(handlers);
            }
            return executor;
        }
        return Executors.newSingleThreadExecutor();
    }

    public void setExceptionHandlers(final List<? extends ExceptionHandler> handlers) {
        _handlers.clear();
        _handlers.addAll(handlers);
    }

    /**
     * This is the money right here. We can capture both the runnable object and any throwables that result from
     * execution.
     *
     * @param runnable  The runnable object that has completed execution.
     * @param throwable The throwable error, if any.
     */
    @Override
    protected void afterExecute(final Runnable runnable, final Throwable throwable) {
        super.afterExecute(runnable, throwable);
        final String runnableClassName = runnable.getClass().getName();
        if (throwable == null) {
            try {
                if (runnable instanceof Future<?> future) {
                    final Object result = future.get();
                    if (result == null) {
                        log.debug("I seemed to get a normal result from a runnable object of type {}, but that result was null.", runnableClassName);
                    } else {
                        log.debug("I got a normal result from a runnable object of type {}: {}", runnableClassName, result);
                    }
                } else {
                    log.debug("A runnable of type {} completed normally.", runnableClassName);
                }
            } catch (final CancellationException e) {
                if (!handled(e.getCause())) {
                    log.warn("A runnable task of type {} was cancelled", runnableClassName, e);
                }
            } catch (final ExecutionException e) {
                if (!handled(e.getCause())) {
                    log.warn("A runnable task of type {} had some kind of exception", runnableClassName, e);
                }
            } catch (final InterruptedException e) {
                if (!handled(e.getCause())) {
                    log.warn("A runnable task of type {} was interrupted.", runnableClassName, e);
                }
                Thread.currentThread().interrupt();
            }
        } else {
            if (!handled(throwable) && !handled(throwable.getCause())) {
                log.warn("A runnable task of type {} threw an exception of type: {}", runnableClassName, throwable.getClass().getName(), throwable);
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean handled(final Throwable throwable) {
        if (throwable == null || _handlers == null) {
            return false;
        }
        Optional<ExceptionHandler> optional = _handlers.stream().filter(handler -> handler.handles(throwable)).findFirst();
        if (optional.isEmpty()) {
            return false;
        }
        optional.get().handle(throwable, log);
        return true;
    }
}
