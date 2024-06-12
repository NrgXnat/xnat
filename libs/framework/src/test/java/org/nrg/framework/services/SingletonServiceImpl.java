package org.nrg.framework.services;

import org.springframework.stereotype.Service;

@Service
public class SingletonServiceImpl implements SingletonService {
    private final String _name;

    public SingletonServiceImpl(final String name) {
        _name = name;
    }

    @Override
    public String getName() {
        return _name;
    }
}
