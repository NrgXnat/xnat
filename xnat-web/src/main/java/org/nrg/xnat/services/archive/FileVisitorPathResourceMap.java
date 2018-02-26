package org.nrg.xnat.services.archive;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import static java.nio.file.FileVisitResult.CONTINUE;

@Slf4j
public class FileVisitorPathResourceMap implements PathResourceMap<String, Resource> {
    public FileVisitorPathResourceMap(final Path path) {
        this(path, "*");
    }

    public FileVisitorPathResourceMap(final Path path, final String pattern) {
        _finder = new FileFinder(path, pattern);
    }

    @Override
    public long getProcessedCount() {
        return _processed;
    }

    @Override
    public boolean hasNext() {
        return _iterator.hasNext();
    }

    @Override
    public Mapping<String, Resource> next() {
        final Pair<Path, Path> path = _iterator.next();
        _processed++;
        return new Mapping<String, Resource>() {
            @Override
            public String getPath() {
                return path.getLeft().toString();
            }

            @Override
            public Resource getResource() {
                return new FileSystemResource(path.getRight().toFile());
            }
        };
    }

    @Override
    public void remove() {
        _iterator.remove();
    }

    public void setIncludeEmptyFiles(final boolean includeEmptyFiles) {
        _finder.setIncludeEmptyFiles(includeEmptyFiles);
    }

    public void process() throws IOException {
        _iterator = _finder.call().iterator();
    }

    public int getFileCount() {
        return _iterator == null ? 0 : _finder._paths.size();
    }

    private static class FileFinder extends SimpleFileVisitor<Path> implements Callable<List<Pair<Path, Path>>> {
        FileFinder(final Path root, final String pattern) {
            _matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            _root = root;
            _paths = new ArrayList<>();
        }

        @Override
        public List<Pair<Path, Path>> call() throws IOException {
            Files.walkFileTree(_root, this);
            log.info("Found {} files total", _paths.size());
            return _paths;
        }

        @Override
        public FileVisitResult visitFile(final Path path, final BasicFileAttributes attributes) {
            if (attributes.isRegularFile() && (attributes.size() > 0 || _includeEmptyFiles)) {
                final Path name = path.getFileName();
                if (name != null && _matcher.matches(name)) {
                    log.trace("Added folder {} to list of file paths", path);
                    _paths.add(new ImmutablePair<>(_root.relativize(path), path));
                }
            } else {
                log.trace("Not adding folder {} to list of file paths", path);
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path path, final IOException exception) {
            log.warn("An I/O exception occurred accessing the file {}", path, exception);
            return CONTINUE;
        }

        @SuppressWarnings("unused")
        public List<Path> getRelativePaths() {
            return Lists.transform(_paths, new Function<Pair<Path, Path>, Path>() {
                @Override
                public Path apply(final Pair<Path, Path> path) {
                    return path.getLeft();
                }
            });
        }

        @SuppressWarnings("unused")
        public List<Path> getDerelativizedPaths() {
            return Lists.transform(_paths, new Function<Pair<Path, Path>, Path>() {
                @Override
                public Path apply(final Pair<Path, Path> path) {
                    return path.getRight();
                }
            });
        }

        public void setIncludeEmptyFiles(final boolean includeEmptyFiles) {
            _includeEmptyFiles = includeEmptyFiles;
        }

        private final PathMatcher            _matcher;
        private final Path                   _root;
        private final List<Pair<Path, Path>> _paths;
        private       boolean                _includeEmptyFiles;
    }

    private final FileFinder _finder;

    private Iterator<Pair<Path, Path>> _iterator;
    private long                       _processed = 0;
}
