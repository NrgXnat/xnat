/*
 * web: org.nrg.xnat.archive.ImportScopeTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * A scope reads each setting once for as long as it is bound; without one every read goes to the source.
 */
public class ImportScopeTest {
    @Test
    public void readsOnceWithinAScopeAndEveryTimeOutsideOne() {
        final AtomicInteger reads = new AtomicInteger();
        assertEquals("a", ImportScope.scoped("setting", () -> "a" + reads.incrementAndGet()).substring(0, 1));
        ImportScope.scoped("setting", () -> "b" + reads.incrementAndGet());
        assertEquals(2, reads.get());

        try (ImportScope.Binding ignored = new ImportScope().bind()) {
            assertEquals("c3", ImportScope.scoped("setting", () -> "c" + reads.incrementAndGet()));
            assertEquals("c3", ImportScope.scoped("setting", () -> "d" + reads.incrementAndGet()));
            assertEquals("e4", ImportScope.scoped("other", () -> "e" + reads.incrementAndGet()));
        }
        assertEquals(4, reads.get());

        ImportScope.scoped("setting", () -> "f" + reads.incrementAndGet());
        assertEquals(5, reads.get());
    }

    @Test
    public void eachScopeReadsForItself() {
        final AtomicInteger reads = new AtomicInteger();
        for (int i = 0; i < 2; i++) {
            try (ImportScope.Binding ignored = new ImportScope().bind()) {
                ImportScope.scoped("setting", reads::incrementAndGet);
                ImportScope.scoped("setting", reads::incrementAndGet);
            }
        }
        assertEquals(2, reads.get());
    }

    @Test
    public void closingABindingRestoresTheScopeBoundBefore() {
        final ImportScope outer = new ImportScope();
        try (ImportScope.Binding ignored = outer.bind()) {
            assertEquals("outer", ImportScope.scoped("setting", () -> "outer"));
            try (ImportScope.Binding inner = new ImportScope().bind()) {
                assertEquals("inner", ImportScope.scoped("setting", () -> "inner"));
            }
            assertEquals("outer", ImportScope.scoped("setting", () -> "unread"));
        }
        assertEquals("unbound", ImportScope.scoped("setting", () -> "unbound"));
    }

    @Test
    public void keepsANullReading() {
        final AtomicInteger reads = new AtomicInteger();
        try (ImportScope.Binding ignored = new ImportScope().bind()) {
            assertNull(ImportScope.scoped("setting", () -> {
                reads.incrementAndGet();
                return null;
            }));
            assertNull(ImportScope.scoped("setting", () -> {
                reads.incrementAndGet();
                return null;
            }));
        }
        assertEquals(1, reads.get());
    }
}
