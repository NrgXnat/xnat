package org.nrg.xft.schema.Wrappers.GenericWrapper;

import org.junit.After;
import org.junit.Test;
import org.nrg.xft.schema.XFTDataField;
import org.nrg.xft.schema.design.XFTElementWrapper;
import org.nrg.xft.schema.design.XFTFactory;
import org.nrg.xft.schema.design.XFTFactoryI;
import org.nrg.xft.schema.design.XFTFieldWrapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A field wrapper builds its lookups of child fields on first use. These tests hold one thread's build part way
 * through and look the field up from another thread meanwhile, as concurrent archives do when they read the first
 * session XML after a start.
 */
public class GenericWrapperFieldLookupTest {
    private static final Set<String> CHILDREN = new TreeSet<>(List.of("x", "y", "z"));

    private final ExecutorService builder = Executors.newSingleThreadExecutor();

    @After
    public void stopBuilder() {
        builder.shutdownNow();
    }

    @Test(timeout = 30_000)
    public void aFieldNameLookupIsCompleteWhileAnotherThreadBuildsIt() throws Exception {
        final HeldField field = new HeldField();
        final Future<Set<String>> held = builder.submit(() -> new TreeSet<String>(field.getAllPossibleXMLFieldNames().keySet()));
        field.awaitBuilding();

        assertEquals(CHILDREN, new TreeSet<String>(field.getAllPossibleXMLFieldNames().keySet()));
        field.release();
        assertEquals(CHILDREN, held.get(10, TimeUnit.SECONDS));
    }

    @Test(timeout = 30_000)
    public void theDirectFieldsAreCompleteWhileAnotherThreadBuildsThem() throws Exception {
        final HeldField field = new HeldField();
        final Future<Set<String>> held = builder.submit(() -> names(field.getDirectFields()));
        field.awaitBuilding();

        assertEquals(CHILDREN, names(field.getDirectFields()));
        field.release();
        assertEquals(CHILDREN, held.get(10, TimeUnit.SECONDS));
    }

    @Test(timeout = 30_000)
    public void allFieldsAreCompleteWhileAnotherThreadBuildsThem() throws Exception {
        final HeldField field = new HeldField();
        final Future<Set<String>> held = builder.submit(() -> names(field.getAllFields()));
        field.awaitBuilding();

        assertEquals(CHILDREN, names(field.getAllFields()));
        field.release();
        assertEquals(CHILDREN, held.get(10, TimeUnit.SECONDS));
    }

    private static Set<String> names(final Collection<?> fields) {
        final Set<String> names = new TreeSet<>();
        for (final Object field : fields) {
            names.add(((GenericWrapperField) field).getName());
        }
        return names;
    }

    /**
     * A field with x, y and z under it. The first child wrapper any build asks for waits until the test releases it.
     */
    private static final class HeldField extends GenericWrapperField {
        private final CountDownLatch building = new CountDownLatch(1);
        private final CountDownLatch released = new CountDownLatch(1);
        private final AtomicBoolean  holding  = new AtomicBoolean();
        private final XFTFactory     factory  = new XFTFactory() {
            @Override
            public XFTElementWrapper getElementWrapper() {
                throw new UnsupportedOperationException();
            }

            @Override
            public XFTFieldWrapper getFieldWrapper() {
                if (holding.compareAndSet(false, true)) {
                    building.countDown();
                    try {
                        released.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }
                }
                return new GenericWrapperField();
            }
        };

        HeldField() {
            final XFTDataField parent = XFTDataField.GetEmptyField();
            parent.setName("voxelRes");
            int sequence = 0;
            for (final String name : CHILDREN) {
                final XFTDataField child = XFTDataField.GetEmptyField();
                child.setName(name);
                child.setSequence(sequence++);
                parent.addChildField(child);
            }
            setWrapped(parent);
        }

        @Override
        public XFTFactoryI getFactory() {
            return factory;
        }

        void awaitBuilding() throws InterruptedException {
            assertTrue("the build never started", building.await(10, TimeUnit.SECONDS));
        }

        void release() {
            released.countDown();
        }
    }
}
