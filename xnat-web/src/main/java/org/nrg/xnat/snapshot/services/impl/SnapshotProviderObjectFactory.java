package org.nrg.xnat.snapshot.services.impl;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.snapshot.generator.SnapshotResourceGenerator;
import org.springframework.stereotype.Component;

@Component
public class SnapshotProviderObjectFactory extends BaseKeyedPooledObjectFactory<String, SnapshotProvider> {
    private final CatalogService catalogService;
    private final SnapshotResourceGenerator snapshotResourceGenerator;
    private final XnatUserProvider userProvider;

    public SnapshotProviderObjectFactory(CatalogService catalogService, SnapshotResourceGenerator snapshotResourceGenerator, XnatUserProvider provider) {
        this.catalogService = catalogService;
        this.snapshotResourceGenerator = snapshotResourceGenerator;
        this.userProvider = provider;
    }

    @Override
    public SnapshotProvider create(String key) throws Exception {
        return new SnapshotProvider( catalogService, snapshotResourceGenerator, userProvider);
    }

    @Override
    public PooledObject<SnapshotProvider> wrap(SnapshotProvider value) {
        return new DefaultPooledObject<>(value);
    }

    @Override
    public PooledObject<SnapshotProvider> makeObject(String key) throws Exception {
        return wrap( create( key));
    }
}
