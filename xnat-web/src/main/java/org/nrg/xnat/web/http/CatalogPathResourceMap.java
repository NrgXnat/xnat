/*
 * web: org.nrg.xnat.web.http.PathResourceMapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.web.http;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.model.CatCatalogI;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.services.archive.PathResourceMap;
import org.nrg.xnat.utils.CatalogUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates path-to-resource mappings from the entries in a {@link CatCatalogI resource catalog}.
 */
@Slf4j
public class CatalogPathResourceMap implements PathResourceMap<String, Resource> {
    public CatalogPathResourceMap(final CatCatalogI catalog, final String archiveRoot, final boolean testMode) {
        _archiveRoot = archiveRoot;
        _catalogId = catalog.getId();

        log.debug("{}: Added catalog: {}", _catalogId, StringUtils.defaultIfBlank(catalog.getDescription(), "(no description)"));
        for (final CatCatalogI entrySet : catalog.getSets_entryset()) {
            _sessions.push(entrySet);
            log.debug("{}: Added entry set {}: {}", _catalogId, entrySet.getId(), StringUtils.defaultIfBlank(entrySet.getDescription(), "(no description)"));
        }
        if (!testMode) {
            _testFile = null;
        } else {
            try {
                _testFile = Files.createTempFile("download-test", ".txt").toFile();
                _testFile.deleteOnExit();
                try (final PrintWriter writer = new PrintWriter(_testFile)) {
                    writer.println("Temporary file for catalog test.");
                }
                log.debug("{}: Wrote out temporary file to use for catalog test: {}", _catalogId, _testFile.getAbsolutePath());
            } catch (IOException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to create a download test file.", e);
            }
        }
    }

    @Override
    public long getProcessedCount() {
        return _resourceCount;
    }

    @Override
    public boolean hasNext() {
        // If we have available resources on the stack, then there's no need to refresh.
        if (!_resources.isEmpty()) {
            return true;
        }
        // If we don't have any available resources AND the entries and catalog stack are empty, we're done here.
        if (_entries.isEmpty() && _sessions.isEmpty()) {
            log.info("{}: No entries or sets left in resource map, processed {} total resources", _catalogId, _resourceCount);
            return false;
        }
        try {
            // If the resources are empty, that's OK as long as we have more entries or catalogs to burn through.
            if (!refresh()) {
                log.info("{}: No entries left in resource map, processed {} total resources", _catalogId, _resourceCount);
                return false;
            }
            return true;
        } catch (EmptyStackException e) {
            // This is OK: it just means we're done.
            log.info("{}: No entries left in resource map, processed {} total resources", _catalogId, _resourceCount);
            return false;
        }
    }

    @Override
    public Mapping<String, Resource> next() {
        if (hasNext()) {
            final Mapping<String, Resource> mapping = _resources.pop();
            log.debug("{}: Just popped resource with path {}, resource location: {}", _catalogId, mapping.getPath(), ((CatalogPathResourceMapping) mapping).getFile().getAbsolutePath());
            return mapping;
        }

        // We only end up here if there are no more elements in any of the stacks.
        throw new EmptyStackException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    private boolean refresh() {
        if (!_resources.isEmpty()) {
            return true;
        }
        // If there are resources in the queue, we don't refresh. If not, we need to keep doing this as long as we have
        // entries and/or sets.
        while (_resources.isEmpty() && (!_entries.isEmpty() || !_sessions.isEmpty())) {
            // If the entries stack is empty, keep popping catalogs until we have some entries.
            while (_entries.isEmpty() && !_sessions.isEmpty()) {
                final CatCatalogI catalog = _sessions.pop();
                final String catalogId = catalog.getId();
                log.debug("{}: Just popped a fresh catalog {}: {}", _catalogId, catalogId, StringUtils.defaultIfBlank(catalog.getDescription(), "(no description)"));
                for (final CatCatalogI entrySet : catalog.getSets_entryset()) {
                    _sessions.push(entrySet);
                    log.debug("{}: Added catalog {} entry to stack, {}: {}", _catalogId, catalogId, entrySet.getId(), StringUtils.defaultIfBlank(entrySet.getDescription(), "(no description)"));
                }
                final List<CatEntryI> entries = catalog.getEntries_entry();
                _entries.addAll(entries);
                log.debug("{}: Added {} entries from catalog {}", _catalogId, entries.size(), catalogId);
            }

            // We will only get here if we have no resources, no entries, and have exhausted the catalog stack, which means
            // we return false, because there are no more elements to refresh.
            if (_entries.isEmpty()) {
                return false;
            }

            // We'll get here if we have no resources but do have entries, so process the first entry. This should get
            // some items in the resources stack. If not, we'll stay in the while loop due to the isEmpty() check.
            processEntry(_entries.pop());
        }

        // We could only have gotten here if there are items in the resource stack OR if there are no items in the
        // resource, entry, and catalog stacks. Checking for no resources is a proxy for checking that there are no
        // resources left at all.
        return !_resources.isEmpty();
    }

    private void processEntry(final CatEntryI currentEntry) {
        try {
            log.info("{}: Primary entry: {}, {}", _catalogId, currentEntry.getName(), currentEntry.getUri());

            // Create a data URI from the entry URI.
            final URIManager.DataURIA raw = UriParserUtils.parseURI(currentEntry.getUri());
            log.info("{}: Got a DataURIA of type {}", _catalogId, raw.getClass());

            if (raw instanceof ResourceURII) {
                final String resourceName = currentEntry.getName();
                final String resourceUri = currentEntry.getUri();
                final ResourceURII uri = (ResourceURII) raw;
                final File catalogFile = CatalogUtils.getCatalogFile(_archiveRoot, (XnatResourcecatalogI) uri.getXnatResource());
                final CatCatalogI catalog = CatalogUtils.getCatalog(catalogFile);
                if (catalog == null) {
                    log.warn("The catalog entry {} references the file {}, but that doesn't appear to be a valid catalog. The associated resource file path was {}.", currentEntry.getName(), catalogFile.getAbsolutePath(), uri.getResourceFilePath());
                    return;
                }
                final List<Mapping<String, Resource>> entries = Lists.transform(CatalogUtils.getFiles(catalog, catalogFile.getParent()), new Function<File, Mapping<String, Resource>>() {
                    @Nullable
                    @Override
                    public Mapping<String, Resource> apply(@Nullable final File file) {
                        if (file == null) {
                            return null;
                        }

                        log.debug("{}: Resource entry {} with name {}: {}", _catalogId, ++_resourceCount, getResourceName(resourceName, file), resourceUri);
                        return new CatalogPathResourceMapping(resourceName, file);
                    }
                });
                _resources.addAll(entries);
            } else {
                log.warn("{}: Got a DataURIA of type {}, I'm not really sure what to do with it.", _catalogId);
            }
        } catch (MalformedURLException e) {
            // TODO: We created these, they should be good for now. Add checks later.
        }
    }

    private static String getResourceName(final String resourceName, final File file) {
        final Iterable<String> components = Iterables.filter(Arrays.asList(resourceName.split("/")), IGNORED_PATH_COMPONENT);
        final Matcher matcher = Pattern.compile(Joiner.on(".*").join(components) + "/(?<remnant>.*)$").matcher(file.getAbsolutePath());
        if (matcher.find()) {
            return matcher.group("remnant");
        }
        return file.getName();
    }

    private class CatalogPathResourceMapping implements Mapping<String, Resource> {
        CatalogPathResourceMapping(final String resource, final File file) {
            _path = Paths.get(resource, getResourceName(resource, file)).toString();
            _file = file;
            log.info("Created resource mapping to path {} for file {}", _path, _file);
        }

        @Override
        public String getPath() {
            return _path;
        }

        @Override
        public Resource getResource() {
            return new FileSystemResource(_testFile == null ? _file : _testFile);
        }

        public File getFile() {
            return _file;
        }

        private final String _path;
        private final File   _file;
    }

    private static final Predicate<String> IGNORED_PATH_COMPONENT = new Predicate<String>() {
        @Override
        public boolean apply(@Nullable final String input) {
            return !_ignored.contains(input);
        }

        private final List<String> _ignored = Arrays.asList("assessors", "resources", "scans");
    };

    private final Stack<CatCatalogI>               _sessions  = new Stack<>();
    private final Stack<CatEntryI>                 _entries   = new Stack<>();
    private final Stack<Mapping<String, Resource>> _resources = new Stack<>();

    private final String                 _catalogId;
    private final String                 _archiveRoot;
    private final File                   _testFile;

    private long _resourceCount = 0;
}
