/*
 * web: org.nrg.xnat.web.http.PathResourceMapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.web.http;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.CatCatalogI;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.services.archive.PathResourceMap;
import org.nrg.xnat.utils.CatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.EmptyStackException;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * Creates path-to-resource mappings from the entries in a {@link CatCatalogI resource catalog}.
 */
public class CatalogPathResourceMap implements PathResourceMap<String, Resource> {
    public CatalogPathResourceMap(final CatCatalogI catalog, final String archiveRoot) {
        _archiveRoot = archiveRoot;
        for (final CatCatalogI entrySet : catalog.getSets_entryset()) {
            _stack.push(entrySet);
        }
    }

    @Override
    public boolean hasNext() {
        return update();
    }

    @Override
    public Mapping<String, Resource> next() {
        if (hasNext()) {
            return _resources.pop();
        }
        throw new NoSuchElementException("There are no remaining path-resource mappings.");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    /**
     *
     */
    private boolean update() {
        // If we have available resources on the stack, then there's no need to refresh.
        if (!_resources.isEmpty()) {
            return true;
        }
        // If we don't have any available resources AND the entries and catalog stack are empty, we're done here.
        if (_entries.isEmpty() && _stack.isEmpty()) {
            return false;
        }
        try {
            // If the resources are empty, that's OK as long as we have more entries or catalogs to burn through.
            refreshEntriesFromStack();
            refreshResourcesFromEntries();
            return true;
        } catch (EmptyStackException e) {
            // This is OK: it just means we're done.
            return false;
        }
    }

    private void refreshEntriesFromStack() {
        while (_entries.isEmpty() && !_stack.isEmpty()) {
            final CatCatalogI currentCatalog = _stack.pop();
            for (final CatCatalogI entrySet : currentCatalog.getSets_entryset()) {
                _stack.push(entrySet);
            }
            _entries.addAll(currentCatalog.getEntries_entry());
        }
        if (_entries.isEmpty() && _stack.isEmpty()) {
            throw new EmptyStackException();
        }
    }

    private void refreshResourcesFromEntries() {
        _currentEntry = _entries.pop();
        try {
            final URIManager.DataURIA raw = UriParserUtils.parseURI(_currentEntry.getUri());
            _log.info("Got a DataURIA of type {}", raw.getClass());

            if (raw instanceof ResourceURII) {
                final ResourceURII uri = (ResourceURII) raw;
                final File catalogFile = CatalogUtils.getCatalogFile(_archiveRoot, (XnatResourcecatalogI) uri.getXnatResource());
                final CatCatalogBean catalog = CatalogUtils.getCatalog(catalogFile);
                if (catalog != null) {
                    for (final CatEntryI entry : catalog.getEntries_entry()) {
                        _resources.push(new Mapping<String, Resource>() {
                            @Override
                            public String getPath() {
                                return Paths.get(_currentEntry.getName(), StringUtils.defaultIfBlank(entry.getName(), entry.getUri())).toString();
                            }

                            @Override
                            public Resource getResource() {
                                return new FileSystemResource(Paths.get(catalogFile.getParentFile().getAbsolutePath(), entry.getUri()).toFile());
                            }
                        });
                    }
                }
            } else {
                _log.warn("Got a DataURIA of type {}, I'm not really sure what to do with it.");
            }
        } catch (MalformedURLException e) {
            // TODO: We created these, they should be good for now. Add checks later.
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(CatalogPathResourceMap.class);

    private final Stack<CatCatalogI>               _stack     = new Stack<>();
    private final Stack<CatEntryI>                 _entries   = new Stack<>();
    private final Stack<Mapping<String, Resource>> _resources = new Stack<>();

    private final String _archiveRoot;

    private CatEntryI _currentEntry;
}
