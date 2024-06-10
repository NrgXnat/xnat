package org.nrg.xnat.snapshot.services.impl;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.snapshot.generator.SnapshotResourceGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class SnapshotProviderObjectFactory extends BaseKeyedPooledObjectFactory<String, SnapshotProvider> {
    @Autowired
    public SnapshotProviderObjectFactory(final @Lazy SnapshotProviderPool snapshotProviderPool, final CatalogService catalogService, final SnapshotResourceGenerator snapshotResourceGenerator, final XnatUserProvider provider) {
        _snapshotProviderPool = snapshotProviderPool;
        _catalogService = catalogService;
        _snapshotResourceGenerator = snapshotResourceGenerator;
        _userProvider = provider;
    }

    @Override
    public SnapshotProvider create(final String key) throws Exception {
        return new SnapshotProvider(key, _snapshotProviderPool, _catalogService, _snapshotResourceGenerator, _userProvider);
    }

    @Override
    public PooledObject<SnapshotProvider> wrap(SnapshotProvider value) {
        return new DefaultPooledObject<>(value);
    }

    @Override
    public PooledObject<SnapshotProvider> makeObject(String key) throws Exception {
        return wrap(create(key));
    }

    private final SnapshotProviderPool      _snapshotProviderPool;
    private final CatalogService            _catalogService;
    private final SnapshotResourceGenerator _snapshotResourceGenerator;
    private final XnatUserProvider          _userProvider;
}
