package org.nrg.prefs.transformers;

public interface PreferenceTransformer<T> {
    T transform(final String serialized);
}
