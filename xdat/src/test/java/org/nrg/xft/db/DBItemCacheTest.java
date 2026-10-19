package org.nrg.xft.db;

import org.junit.Test;

import static org.junit.Assert.fail;

public class DBItemCacheTest {
    /**
     * The JVM calls a class's finalize() on its finalizer thread when an instance is collected. A cache's change-info
     * insert run there builds an XFT item outside any save, and a collection that lands while XFT is loading its schemas
     * can break the load.
     */
    @Test
    public void aCacheDeclaresNoFinalizer() {
        try {
            DBItemCache.class.getDeclaredMethod("finalize");
            fail("DBItemCache must not declare finalize(): the JVM runs it on every collected cache");
        } catch (NoSuchMethodException expected) {
            // what we want
        }
    }
}
