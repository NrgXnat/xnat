/*
 * web: org.nrg.xnat.restlet.representations.ZipRepresentation
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.representations;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.nrg.xdat.XDAT;
import org.nrg.xft.utils.zip.TarUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipOutputStream;

import static lombok.AccessLevel.PROTECTED;

@Getter(PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public class ZipRepresentation extends OutputRepresentation {
    public ZipRepresentation(final MediaType mediaType, final String token, final Integer compression) {
        this(mediaType, Collections.singletonList(token), compression);
    }

    public ZipRepresentation(final MediaType mediaType, final List<String> tokens, final Integer compression) {
        super(mediaType);
        _executor = ObjectUtils.defaultIfNull(XDAT.getContextService().getBeanSafely(ExecutorService.class), Executors.newSingleThreadExecutor());
        _tokens.addAll(tokens);
        _compression = deriveCompression(compression);
    }

    @Override
    public String getDownloadName() {
        final MediaType mediaType = getMediaType();
        if (mediaType.equals(MediaType.APPLICATION_GNU_TAR)) {
            return getTokenName() + ".tar.gz";
        }
        if (mediaType.equals(MediaType.APPLICATION_TAR)) {
            return getTokenName() + ".tar";
        }
        if (mediaType.equals(SecureResource.APPLICATION_XAR)) {
            return getTokenName() + ".xar";
        }
        return getTokenName() + ".zip";
    }

    @Override
    public void write(final OutputStream output) throws IOException {
        final MediaType mediaType = getMediaType();
        try (final ZipI zip = initializeZip(output, mediaType)) {
            for (final ZipEntry zipEntry : _entries) {
                if (zipEntry instanceof ZipFileEntry) {
                    final File file = ((ZipFileEntry) zipEntry).getFile();
                    if (!file.isDirectory()) {
                        zip.write(zipEntry.getPath(), file);
                    }
                } else {
                    zip.write(zipEntry.getPath(), ((ZipStreamEntry) zipEntry).getInputStream());
                }
            }
        } finally {
            if (!_afterWrite.isEmpty()) {
                final Executor executor = getExecutor();
                for (final Runnable runnable : _afterWrite) {
                    executor.execute(runnable);
                }
            }
        }
    }

    public void addEntry(final String path, final File file) {
        _entries.add(new ZipFileEntry(path, file));
    }

    public void addEntry(final String path, final InputStream input) {
        _entries.add(new ZipStreamEntry(path, input));
    }

    public void addFolder(final String path, final File folder) {
        if (folder.isDirectory()) {
            final File[] files = folder.listFiles();
            if (files != null) {
                for (final File file : files) {
                    if (file.isDirectory()) {
                        addFolder(Paths.get(path, file.getName()).toString(), file);
                    } else {
                        addEntry(Paths.get(path, file.getName()).toString(), file);
                    }
                }
            }
        } else {
            addEntry(Paths.get(path, folder.getName()).toString(), folder);
        }
    }

    public void addEntry(final File file) {
        final String path      = file.getAbsolutePath().replace('\\', '/');
        int    i      = -1;
        String _token = null;

        for (final String token : _tokens) {
            _token = token;
            i = path.indexOf('/' + _token + '/');
            if (i == -1) {
                i = path.indexOf('/' + _token);

                if (i == -1) {
                    i = path.indexOf(_token + '/');
                    if (i > -1) {
                        i = (path.substring(0, i)).lastIndexOf('/') + 1;
                        break;
                    }
                } else {
                    i++;
                    break;
                }
            } else {
                i++;
                break;
            }
        }

        if (i == -1) {
            String derivedPath;
            if (path.contains(":")) {
                derivedPath = path.substring(path.indexOf(":"));
                derivedPath = _token + derivedPath.substring(derivedPath.indexOf("/"));
            } else {
                derivedPath = _token + path;
            }

            _entries.add(new ZipFileEntry(derivedPath, file));
        } else {
            _entries.add(new ZipFileEntry(path.substring(i), file));
        }
    }

    public void addAll(final List<File> files) {
        for (final File file : files) {
            addEntry(file);
        }
    }

    public void addAllAtRelativeDirectory(final String dirSpec, final List<File> files) {
        final String dirCleaned = dirSpec.replace('\\', '/');
        for (final File file : files) {
            final String path = file.getAbsolutePath().replace('\\', '/');
            final int    index   = path.indexOf(dirCleaned);
            if (index >= 0) {
                addEntry(path.substring(index + dirCleaned.length() + 1), file);
            } else {
                addEntry(file);
            }
        }
    }

    public int getEntryCount() {
        return _entries.size();
    }

    /**
     * After this ZipRepresentation completes a write, remove the named file.
     *
     * @param file The file object for the directory to be deleted.
     *
     * @return A reference to the current instance.
     */
    @SuppressWarnings("UnusedReturnValue")
    public ZipRepresentation deleteDirectoryAfterWrite(final File file) {
        return afterWrite(new Runnable() {
            public void run() {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    log.error("unable to remove working directory " + file, e);
                }
            }
        });
    }

    /**
     * Adds a task that should be performed asynchronously after this ZipRepresentation
     * completes a write.
     *
     * @param runnable The task to be run after the write operation is completed.
     *
     * @return A reference to the current instance.
     */
    private ZipRepresentation afterWrite(final Runnable runnable) {
        _afterWrite.add(runnable);
        return this;
    }

    private int deriveCompression(final Integer compression) {
        return ObjectUtils.defaultIfNull(compression, ZipUtils.DEFAULT_COMPRESSION);
    }

    private String getTokenName() {
        return _tokens.size() > 1 ? "various" : _tokens.get(0);
    }

    @NotNull
    private ZipI initializeZip(final OutputStream output, final MediaType mediaType) throws IOException {
        final ZipI zip;
        if (mediaType.equals(MediaType.APPLICATION_GNU_TAR)) {
            zip = new TarUtils();
            zip.setOutputStream(output, ZipOutputStream.DEFLATED);
            setDownloadName(getTokenName() + ".tar.gz");
            setDownloadable(true);
        } else if (mediaType.equals(MediaType.APPLICATION_TAR)) {
            zip = new TarUtils();
            zip.setOutputStream(output, ZipOutputStream.STORED);
            setDownloadName(getTokenName() + ".tar");
            setDownloadable(true);
        } else {
            zip = new ZipUtils();
            zip.setOutputStream(output, _compression);
            setDownloadName(getTokenName() + ".zip");
            setDownloadable(true);
        }
        return zip;
    }

    public abstract class ZipEntry {
        ZipEntry(final String path) {
            _path = path;
        }

        public String getPath() {
            return _path;
        }

        private final String _path;
    }

    public class ZipFileEntry extends ZipEntry {
        ZipFileEntry(final String path, final File file) {
            super(path);
            _file = file;
        }

        public File getFile() {
            return _file;
        }

        private final File _file;
    }

    public class ZipStreamEntry extends ZipEntry {
        ZipStreamEntry(final String path, final InputStream input) {
            super(path);
            _input = input;
        }

        public InputStream getInputStream() {
            return _input;
        }

        private final InputStream _input;
    }

    private final List<ZipEntry>  _entries    = new ArrayList<>();
    private final List<String>    _tokens     = new ArrayList<>();
    private final List<Runnable>  _afterWrite = new ArrayList<>();

    private final int             _compression;
    private final ExecutorService _executor;
}
