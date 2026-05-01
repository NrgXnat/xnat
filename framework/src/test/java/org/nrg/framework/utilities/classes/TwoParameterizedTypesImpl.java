package org.nrg.framework.utilities.classes;

public class TwoParameterizedTypesImpl implements TwoParameterizedTypes<String, Long> {
    @Override
    public Long get(final String key) {
        return Long.parseLong(key);
    }
}
