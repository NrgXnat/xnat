package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.exceptions.MizerException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This processor recognizes the following directives:
 * 1. include <url>. <url> is the locator of another script. Import directives are followed recursively. An error
 * is thrown if the includes have circular references. The script statements are followed sequentially. Import statements
 * are replaced with the content found at the specified URL.
 *
 */
public class IncludeChecker {
    private final List<URL> knownURLs;

    private static final String XNAT_SCRIPT_PROVIDER_URL_FORMAT = "https://%s/xapi/anonymize/scripts/%s/version/%s";

    public IncludeChecker() {
        this.knownURLs = new ArrayList<>();
    }

    /**
     * This version recursively loads statements from imported scripts. Throws exception if imports are self
     * referencing.
     *
     * @param lines
     * @return
     * @throws MizerException when import urls are circular referencing.
     */
    public List<String> process(List<String> lines) throws MizerException {
        List<String> statements = new ArrayList<>();
        Directive directive = new Directive();
        for( String line: lines) {
            directive.setLine( line);
            if( directive.isImport()) {
                try {
                    URL url = directive.getImportURL().orElseThrow(() -> new MizerException(""));
                    if( knownURLs.contains( url)) {
                        throw new MizerException( "Skipping previously loaded URL: " + url);
                    }
                    else {
                        knownURLs.add( url);
                        statements.addAll( process( readLines(url)));
                    }
                }
                catch( IOException e) {
                    throw new MizerException(e);
                }
            }
            else {
                statements.add( line);
            }
        }
        return statements;
    }

    /**
     * Read a list of strings as lines from the URL.
     *
     * Http scheme:
     *
     * File scheme:
     * The file must be an absolute path to the script file on the server.
     *
     * @param url
     * @return
     * @throws IOException
     */
    public List<String> readLines( URL url) throws IOException {
        List<String> statements = new ArrayList<>();
        switch (url.getProtocol()) {
            case "file":
                statements.addAll( Files.readAllLines( Paths.get( url.getFile())));
                break;
            default:
                throw new UnsupportedOperationException( "Unsupported scheme for anon script: " + url);
        }
        return statements;
    }

    /**
     * This is the parser for directives.
     *
     * It understands the following directives:
     * 1. include
     *     a. include <url>
     *     b. include <host> <script-label> <version>. The generated URL is that of an XNAT script provider.
     *
     */
    private class Directive {
        private Pattern pattern;
        private String line;

        public Directive() {
            line = null;
            pattern = Pattern.compile("include (.*)");
        }

        public void setLine( String line) {
            this.line = line.trim();
        }
        public boolean isImport() {
            return line.matches( "include (.*)");
        }
        public Optional<URL> getImportURL() throws MalformedURLException {
            URL url = null;
            String[] tokens = line.trim().split("\\s+");
            if( tokens.length == 4) {
                url = new URL( String.format(XNAT_SCRIPT_PROVIDER_URL_FORMAT, tokens[1], tokens[2], tokens[3]));
            }
            else if( tokens.length == 2) {
                url = new URL( tokens[1]);
            }
            return Optional.ofNullable( url);
        }
    }

}
