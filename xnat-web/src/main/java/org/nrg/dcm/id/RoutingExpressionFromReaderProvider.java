package org.nrg.dcm.id;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * RoutingExpressionFromReaderProvider is an implementation of RoutingExpressionProvider
 * that extracts expressions as lines from a Reader.
 *
 * It is assumed that the Reader does not have sufficient meta-data to indicate the type of expressions contained
 * by the Reader.
 */
@Slf4j
public class RoutingExpressionFromReaderProvider implements RoutingExpressionProvider {
    private Reader _source;

    public RoutingExpressionFromReaderProvider(Reader reader) {
        _source = reader;
    }

    @Override
    public List<String> provide( CompositeDicomObjectIdentifier.ExtractorType type) {
        throw new UnsupportedOperationException("I don't know the type of my expressions.");
    }

    public List<String> provide() {
        try {
            return parseSource();
        }
        catch (IOException e) {
            throw new RuntimeException( "Error reading routing-rule source " + _source, e);
        }
    }

    @Nonnull
    protected List<String> parseSource() throws IOException {
        List<String> expressions = new ArrayList<>();
        try (final BufferedReader reader = new BufferedReader( _source)) {
            String line;
            while (null != (line = reader.readLine())) {
                expressions.add( line);
            }
        }
        return expressions;
    }

    public Reader getSource() { return _source; }
    public void setSource(Reader source) { _source = source; }
}
