package org.nrg.framework.utilities.classes;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

@Getter
@Accessors(prefix = "_")
@Slf4j
public abstract class AbstractDataExtractor<C extends XnatCache, K, V> implements DataExtractor<K, V> {
    private static final Function<Type, Class<?>> TYPE_TO_CLASS = type -> (Class<?>) (type instanceof ParameterizedType pt ? pt.getRawType() : type);

    private final Class<K> _keyType;
    private final Class<V> _valueType;

    protected AbstractDataExtractor() {
        final Pair<Class<K>, Class<V>> pair = getKeyAndValueTypes();
        _keyType   = pair.getKey();
        _valueType = pair.getValue();
    }

    @SuppressWarnings("unchecked")
    protected Pair<Class<K>, Class<V>> getKeyAndValueTypes() {
        log.info("Trying to get the key and value types here");
        final Type[] types = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
        return Pair.of((Class<K>) TYPE_TO_CLASS.apply(types[0]), (Class<V>) TYPE_TO_CLASS.apply(types[1]));
    }
}
