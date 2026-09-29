package org.nrg.xft.schema.Wrappers.GenericWrapper;

import org.junit.Test;
import org.nrg.xft.db.ViewManager;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertFalse;

/**
 * These schema lookups call other elements' synchronized methods while they work. Synchronizing them orders locks
 * between ViewManager and an element, or between two elements, and deadlocked a node when concurrent requests made
 * the first use of a data type after a start. Keep them unsynchronized.
 */
public class SchemaLookupLockingTest {
    @Test
    public void viewManagerFieldLookupsTakeNoClassLock() throws Exception {
        assertNotSynchronized(ViewManager.class.getMethod("GetFieldMap", GenericWrapperElement.class, String.class, boolean.class, boolean.class));
        assertNotSynchronized(ViewManager.class.getMethod("GetFieldNames", GenericWrapperElement.class, String.class, boolean.class, boolean.class));
    }

    @Test
    public void elementLookupsTakeNoElementMonitor() throws Exception {
        for (final String name : new String[]{"getSQLName", "getReferencedElements", "getMetaFields", "getPossibleExtenders"}) {
            assertNotSynchronized(GenericWrapperElement.class.getMethod(name));
        }
    }

    private static void assertNotSynchronized(final Method method) {
        assertFalse(method + " must not be synchronized", Modifier.isSynchronized(method.getModifiers()));
    }
}
