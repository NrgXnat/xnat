package org.nrg.xnat.micrometer.tags;

import io.micrometer.core.instrument.Counter;

import java.util.Map;

public class TaggedCounterWrapper {

    private final Map<String, TaggedCounter> containerCounters;

    public TaggedCounterWrapper(final Map<String, TaggedCounter> containerCounters) {
        this.containerCounters = containerCounters;
    }

    public void increment(final String name, final String tagValue){
        containerCounters.get(name).increment(tagValue);
    }


}
