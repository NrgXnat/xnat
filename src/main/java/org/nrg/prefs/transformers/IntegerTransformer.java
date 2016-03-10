package org.nrg.prefs.transformers;

public class IntegerTransformer implements PreferenceTransformer<Integer> {
    @Override
    public Integer transform(final String serialized) {
        return Integer.parseInt(serialized);
    }
}
