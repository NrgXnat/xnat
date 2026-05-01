package org.nrg.framework.utilities.classes;

public interface DataExtractor<K, V> {
    Class<K> getKeyType();

    Class<V> getValueType();
}
