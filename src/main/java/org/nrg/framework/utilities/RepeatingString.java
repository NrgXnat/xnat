package org.nrg.framework.utilities;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class RepeatingString implements Iterator<String> {
    public RepeatingString(final String pattern, final int count) {
        _pattern = pattern;
        _count = count;
    }

    @Override
    public boolean hasNext() {
        return _index.get() < _count;
    }

    @Override
    public String next() {
        _index.incrementAndGet();
        return _pattern;
    }

    @Override
    public void remove() {
        _index.incrementAndGet();
    }

    private final String        _pattern;
    private final int           _count;
    private final AtomicInteger _index = new AtomicInteger();
}
