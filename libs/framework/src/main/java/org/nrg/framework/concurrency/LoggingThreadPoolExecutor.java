/*
 * framework: org.nrg.framework.concurrency.LoggingThreadPoolExecutor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.concurrency;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * An override of the standard thread pool executor class to allow for capturing messaging from exceptions that occur
 * inside of the thread pool.
 */
public class LoggingThreadPoolExecutor extends ThreadPoolExecutor {
    public LoggingThreadPoolExecutor() {
        this(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    }

    public LoggingThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), DEFAULT_HANDLER);
    }

    @SuppressWarnings("unused")
    public LoggingThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, DEFAULT_HANDLER);
    }

    @SuppressWarnings("unused")
    public LoggingThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), handler);
    }

    public LoggingThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory, final RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public static void setUseLoggingExecutor(final boolean useLoggingExecutor) {
        _useLoggingExecutor = useLoggingExecutor;
    }

    public static ExecutorService newCachedThreadPool() {
        return newCachedThreadPool(null);
    }

    public static ExecutorService newCachedThreadPool(final ExceptionHandler first, final ExceptionHandler... others) {
        return newCachedThreadPool(Lists.asList(first, others));
    }

    public static ExecutorService newCachedThreadPool(final List<? extends ExceptionHandler> handlers) {
        if (_useLoggingExecutor) {
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
        return newFixedThreadPool(nThreads, Lists.asList(first, others));
    }

    public static ExecutorService newFixedThreadPool(int nThreads, final List<? extends ExceptionHandler> handlers) {
        if (_useLoggingExecutor) {
            final LoggingThreadPoolExecutor executor = new LoggingThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
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
        return newSingleThreadExecutor(Lists.asList(first, others));
    }

    public static ExecutorService newSingleThreadExecutor(final List<? extends ExceptionHandler> handlers) {
        if (_useLoggingExecutor) {
            final LoggingThreadPoolExecutor executor = new LoggingThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
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
                if (runnable instanceof Future<?>) {
                    final Object result = ((Future<?>) runnable).get();
                    if (result == null) {
                        _log.debug("I seemed to get a normal result from a runnable object of type {}, but that result was null.", runnableClassName);
                    } else {
                        _log.debug("I got a normal result from a runnable object of type {}: {}", runnableClassName, result.toString());
                    }
                } else {
                    _log.debug("A runnable of type {} completed normally.", runnableClassName);
                }
            } catch (final CancellationException e) {
                if (!handled(e.getCause())) {
                    _log.warn("A runnable task of type " + runnableClassName + " was cancelled", e);
                }
            } catch (final ExecutionException e) {
                if (!handled(e.getCause())) {
                    _log.warn("A runnable task of type " + runnableClassName + " had some kind of exception", e);
                }
            } catch (final InterruptedException e) {
                if (!handled(e.getCause())) {
                    _log.warn("A runnable task of type " + runnableClassName + " was interrupted.", e);
                }
                Thread.currentThread().interrupt();
            }
        } else {
            if (!handled(throwable) && !handled(throwable.getCause())) {
                _log.warn("A runnable task of type " + runnableClassName + " threw an exception of type: " + throwable.getClass().getName(), throwable);
            }
        }
    }

    private boolean handled(final Throwable throwable) {
        if (throwable == null || _handlers == null) {
            return false;
        }
        for (final ExceptionHandler handler : _handlers) {
            if (handler.handles(throwable)) {
                handler.handle(throwable, _log);
                return true;
            }
        }
        return false;
    }

    private static final Logger                   _log            = LoggerFactory.getLogger(LoggingThreadPoolExecutor.class);
    private static final RejectedExecutionHandler DEFAULT_HANDLER = new AbortPolicy();
    private static boolean _useLoggingExecutor = false;

    private final List<ExceptionHandler> _handlers = Lists.newArrayList();
}
