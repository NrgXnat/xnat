/*
 * web: org.nrg.xnat.archive.ImportScope
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * The settings one import reads once. XNAT receives DICOM objects in batches (the objects of one C-STORE
 * association, the entries of one uploaded archive, the files of one inbox request) and read the routing
 * rules, the enabled archive processors and a site preference from the database again for every object. The
 * owner of a batch creates a scope and gives it to each object's {@link GradualDicomImporter}, which binds it
 * to the thread while it runs; the readers under the importer ask {@link #scoped} for a value and compute it
 * the first time only. A setting changed while a batch is under way therefore applies from the next
 * association or request, not part-way through one, on this node and on every other. Without a scope on the
 * thread a reader reads as it always did.
 */
public final class ImportScope {
    private static final ThreadLocal<ImportScope> CURRENT = new ThreadLocal<>();

    private final ConcurrentMap<String, Optional<?>> values = new ConcurrentHashMap<>();

    /**
     * The value under the key in the scope bound to this thread, computed by the supplier when the scope has
     * none yet, or the supplier's value when no scope is bound.
     *
     * @param key      Names the setting; readers choose keys that cannot collide.
     * @param supplier Reads the setting.
     *
     * @return The value, possibly null.
     */
    public static <T> T scoped(final String key, final Supplier<T> supplier) {
        final ImportScope scope = CURRENT.get();
        return scope == null ? supplier.get() : scope.get(key, supplier);
    }

    /**
     * Binds this scope to the current thread until the returned binding is closed, when the scope bound before
     * (if any) is bound again.
     */
    public Binding bind() {
        final ImportScope previous = CURRENT.get();
        CURRENT.set(this);
        return () -> {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T> T get(final String key, final Supplier<T> supplier) {
        Optional<?> value = values.get(key);
        if (value == null) {
            // Not computeIfAbsent: a supplier may itself read a scoped setting.
            value = Optional.ofNullable(supplier.get());
            values.putIfAbsent(key, value);
        }
        return (T) value.orElse(null);
    }

    public interface Binding extends AutoCloseable {
        @Override
        void close();
    }
}
