package org.nrg.xnat.micrometer.tags;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.Map;

//Ref: https://dzone.com/articles/spring-boot-metrics-with-dynamic-tag-values
public class TaggedCounter {
    private String name;
    private String tagName;
    private MeterRegistry registry;
    private Map<String, Counter> counters = new HashMap<>();

    public TaggedCounter(String name, String tagName, MeterRegistry registry) {
        this.name = name;
        this.tagName = tagName;
        this.registry = registry;
    }

    public void increment(String tagValue){
        counters.computeIfAbsent(tagValue, key -> Counter.builder(name).tags(tagName, key).register(registry))
                .increment();
    }
}
