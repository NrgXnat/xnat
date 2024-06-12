package org.nrg.framework.services;

import org.springframework.stereotype.Service;

@Service
public class StandInServiceImpl implements StandInService {
    private final String _name;

    public StandInServiceImpl(final String name) {
        _name = name;
    }

    @Override
    public String getName() {
        return _name;
    }
}
