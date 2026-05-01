package org.nrg.framework.utilities;

import java.io.InputStream;
import java.io.Reader;
import java.util.*;

public class ImmutableProperties extends Properties {
    public ImmutableProperties(final Properties properties) {
        super(properties);
    }

    public static Properties emptyProperties() {
        return EMPTY_PROPERTIES;
    }

    /**
     * Throws an exception and leaves the property set unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public synchronized Object setProperty(final String key, final String value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an exception and leaves the property set unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public synchronized void load(final Reader reader) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an exception and leaves the property set unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public synchronized void load(final InputStream inputStream) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an exception and leaves the property set unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public synchronized void loadFromXML(final InputStream inputStream) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an exception and leaves the property set unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public synchronized Object put(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an exception and leaves the property set unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public synchronized void putAll(final Map<?, ?> properties) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an exception and leaves the property set unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public synchronized Object remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an exception and leaves the property set unmodified.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public synchronized void clear() {
        throw new UnsupportedOperationException();
    }

    // TODO: Uncomment and add for upgrade to Java 8.
    /*
    @Override
    public synchronized boolean remove(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Object merge(final Object key, final Object value, final BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Object putIfAbsent(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean replace(final Object key, final Object oldValue, final Object newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Object replace(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void replaceAll(final BiFunction<? super Object, ? super Object, ?> function) {
        throw new UnsupportedOperationException();
    }
     */

    private ImmutableProperties() {
        super();
    }

    public static ImmutablePropertiesBuilder builder() {
        return new ImmutablePropertiesBuilder();
    }

    public static final class ImmutablePropertiesBuilder {
        private ImmutablePropertiesBuilder() {
            _entries = new HashMap<>();
        }

        public ImmutablePropertiesBuilder property(final String property, final String value) {
            _entries.put(property, value);
            return this;
        }

        public ImmutableProperties build() {
            final Properties properties = new Properties();
            properties.putAll(_entries);
            return new ImmutableProperties(properties);
        }

        private transient volatile Map<String, String> _entries;
    }

    private static final ImmutableProperties EMPTY_PROPERTIES = new ImmutableProperties();
}
