package org.nrg.framework.services;

import org.springframework.stereotype.Service;

@Service
public class ManyImplServiceImpl implements ManyImplService {
    private final String _name;

    public ManyImplServiceImpl(final String name) {
        _name = name;
    }

    @Override
    public String getName() {
        return _name;
    }
}
