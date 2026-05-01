/*
 * automation: org.nrg.automation.runners.PathBasedScriptRunnerOutputAdapter
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.runners;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.automation.entities.Script;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
@Slf4j
public class PathBasedScriptRunnerOutputAdapter implements ScriptRunnerOutputAdapter {
    public PathBasedScriptRunnerOutputAdapter() {
        // Nothing to do here.
    }

    public PathBasedScriptRunnerOutputAdapter(final String path) {
        initialize(path);
    }

    @Override
    public PrintWriter getWriter(final Script script) {
        final String scriptId     = script.getScriptId();
        final Path   scriptFolder = _folders.containsKey(scriptId) ? _folders.get(scriptId) : getScriptFolder(scriptId);
        final Path   output       = scriptFolder.resolve(scriptId + "." + new SimpleDateFormat("yyyy-MM-dd-k:mm:ss").format(new Date()) + ".txt");
        try {
            return new PrintWriter(new FileWriter(output.toFile()));
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Something went wrong trying to create the file writer", e);
        }
    }

    public boolean hasPath() {
        return _path != null;
    }

    public void setPath(final String path) {
        if (StringUtils.isBlank(path)) {
            return;
        }
        Path resolved = Paths.get(path);
        if (resolved.compareTo(_path) == 0) {
            return;
        }
        if (!resolved.toFile().exists()) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "The path " + path + " doesn't exist.");
        }
        _path = resolved;
        _folders.clear();
    }

    protected void initialize(String path) {
        if (StringUtils.isNotBlank(path)) {
            log.debug("Initializing the path-based script runner output adapter with the path: {}", path);
            setPath(path);
        } else {
            log.debug("Initializing the path-based script runner output adapter without a path specified. Using temp path.");
            final Path tmpdir  = Paths.get(System.getProperty("java.io.tmpdir"));
            final Path scripts = tmpdir.resolve("scripts");
            //noinspection ResultOfMethodCallIgnored
            scripts.toFile().mkdir();
            setPath(scripts.toString());
        }
    }

    private Path getScriptFolder(final String scriptId) {
        final Path scriptFolder = _path.resolve(scriptId);
        if (!scriptFolder.toFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            scriptFolder.toFile().mkdirs();
        } else if (!scriptFolder.toFile().isDirectory()) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Can't write to the path " + scriptFolder + ": it already exists but isn't a directory.");
        }
        _folders.put(scriptId, scriptFolder);
        return scriptFolder;
    }

    private              Path   _path;
    private final        Map<String, Path> _folders = new HashMap<>();
}
