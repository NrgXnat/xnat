package org.nrg.dicom.mizer.service.impl;


import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import org.nrg.dicom.mizer.exceptions.MizerException;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Mizer anon context for mizers that require scripts.
 *
 * Convenience MizerContext class so users do not have to know the name of the script context element or cast it on
 * extraction.
 *
 */
public class MizerContextWithScript extends MizerContextBase {
    public MizerContextWithScript() {
        super();
    }

    public MizerContextWithScript(final Map<String, Object> context) {
        super(context);
    }

    public MizerContextWithScript(final long scriptId, final Map<String, Object> context) {
        super(scriptId, context);
    }

    public MizerContextWithScript(final String script) throws MizerException {
        setScript(script);
    }

    public MizerContextWithScript(final String script, final Map<String, Object> context) throws MizerException {
        super(context);
        setScript(script);
    }

    public MizerContextWithScript(final List<String> statements) throws MizerException {
        setScript(statements);
    }

    public MizerContextWithScript(final InputStream inputStream) throws MizerException {
        setScript(inputStream);
    }

    public MizerContextWithScript(final long scriptId, final String script) throws MizerException {
        this(scriptId, script, null);
    }

    public MizerContextWithScript(final long scriptId, final String script, final Map<String, Object> context) throws MizerException {
        super(scriptId, context);
        setScript(script);
    }

    public void setScript(final List<String> statements) {
        setElement( "statements", statements);
    }

    public void setScript(final String script) throws MizerException {
        setElement( "statements", readScript( new StringReader( script)));
    }

    public void setScript(final InputStream input) throws MizerException {
        setElement( "statements", readScript( new InputStreamReader( input)));
    }

    public boolean hasScript() {
        return getElements().containsKey("statements");
    }

    public List<String> getScript() {
        //noinspection unchecked
        return (List<String>) getElement("statements");
    }

    public InputStream getScriptInputStream() {
        try {
            return new ByteArrayInputStream(getScriptAsString().getBytes("UTF-8"));
        } catch(UnsupportedEncodingException ignore) {
            return null;
        }
    }

    public String getScriptAsString() {
        return Joiner.on("\n").join(getScript());
    }

    /**
     * Convert the list of script statements, provided by the {@link Reader}, into a List of Strings.
     *
     * @param reader {@link Reader} to script
     *
     * @return List of Strings containing script statements.
     *
     * @throws MizerException If an error occurs trying to read the script.
     */
    protected List<String> readScript(final Reader reader) throws MizerException {
        try {
            return CharStreams.readLines(reader);
        } catch( IOException e) {
            throw new MizerException(e);
        }
    }
}
