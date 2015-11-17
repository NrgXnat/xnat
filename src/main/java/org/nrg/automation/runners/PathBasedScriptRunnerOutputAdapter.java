package org.nrg.automation.runners;

import org.apache.commons.lang3.StringUtils;
import org.nrg.automation.entities.Script;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PathBasedScriptRunnerOutputAdapter implements ScriptRunnerOutputAdapter {

    public PathBasedScriptRunnerOutputAdapter(final String path) throws NrgServiceException {
        if (StringUtils.isNotBlank(path)) {
            if (_log.isDebugEnabled()) {
                _log.debug("Initializing the path-based script runner output adapter with the path: " + path);
            }
            setPath(path);
        } else {
            if (_log.isDebugEnabled()) {
                _log.debug("Initializing the path-based script runner output adapter without a path specified. Using temp path.");
            }
            final Path tmpdir = Paths.get(System.getProperty("java.io.tmpdir"));
            final Path scripts = tmpdir.resolve("scripts");
            scripts.toFile().mkdir();
            setPath(scripts.toString());
        }
    }

    @Override
    public PrintWriter getWriter(final Script script) {
        final String scriptId = script.getScriptId();
        final Path scriptFolder = _folders.containsKey(scriptId) ? _folders.get(scriptId) : getScriptFolder(scriptId);
        final Path output = scriptFolder.resolve(scriptId + "." + new SimpleDateFormat("yyyy-MM-dd-k:mm:ss").format(new Date()) + ".txt");
        try {
            return new PrintWriter(new FileWriter(output.toFile()));
        } catch (IOException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Something went wrong trying to create the file writer", e);
        }
    }

    public void setPath(final String path) {
        if (StringUtils.isBlank(path) || (_path != null &&  Paths.get(path).compareTo(_path) == 0)) {
            return;
        }
        _path = Paths.get(path);
        if (!_path.toFile().exists()) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "The path " + path + " doesn't exist.");
        }
        _folders.clear();
    }

    private Path getScriptFolder(final String scriptId) {
        final Path scriptFolder = _path.resolve(scriptId);
        if (!scriptFolder.toFile().exists()) {
            scriptFolder.toFile().mkdirs();
        } else if (!scriptFolder.toFile().isDirectory()) {
            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Can't write to the path " + scriptFolder + ": it already exists but isn't a directory.");
        }
        _folders.put(scriptId, scriptFolder);
        return scriptFolder;
    }

    private static final Logger _log = LoggerFactory.getLogger(PathBasedScriptRunnerOutputAdapter.class);
    private Path _path;
    private final Map<String, Path> _folders = new HashMap<String, Path>();
}
