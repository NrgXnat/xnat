package org.nrg.xnat.utils.predicates;

import com.google.common.base.Predicate;

import java.util.concurrent.Future;

public class Predicates {
    public static final Predicate<Future<?>> isDone = new Predicate<Future<?>>() {
        @Override
        public boolean apply(final Future<?> future) {
            return future.isDone();
        }
    };

    public static final Predicate<Future<?>> isCancelled = new Predicate<Future<?>>() {
        @Override
        public boolean apply(final Future<?> future) {
            return future.isCancelled();
        }
    };

    public static final Predicate<Future<?>> isCompleted = new Predicate<Future<?>>() {
        @Override
        public boolean apply(final Future<?> future) {
            return future.isDone() || future.isCancelled();
        }
    };
}
