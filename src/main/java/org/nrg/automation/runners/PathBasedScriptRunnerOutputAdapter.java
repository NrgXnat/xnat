package org.nrg.automation.runners;

import org.nrg.automation.entities.Script;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
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

public class PathBasedScriptRunnerOutputAdapter implements ScriptRunnerOutputAdapter {

    public PathBasedScriptRunnerOutputAdapter(final String path) throws NrgServiceException {
        _path = Paths.get(path);
        if (!_path.toFile().exists()) {
            throw new NrgServiceException(NrgServiceError.Unknown, "The path " + path + " doesn't exist.");
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


    final Path _path;
    final Map<String, Path> _folders = new HashMap<String, Path>();
}
