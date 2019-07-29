package org.nrg.framework.services.impl;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(prefix = "_")
@Slf4j
public class ValidationHandler extends DefaultHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public void fatalError(final SAXParseException e) {
        log.error("A fatal error occurred during document parsing", e);
        getErrors().add(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(SAXParseException e) {
        log.error("An error occurred during document parsing", e);
        getErrors().add(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warning (final SAXParseException e) {
        log.warn("A warning occurred during document parsing", e);
    }

    /**
     * Indicates whether the parsed XML document was valid.
     */
    @SuppressWarnings("unused")
    public boolean assertValid(){
        return getErrors().isEmpty();
    }

    private final List<SAXParseException> _errors = new ArrayList<>();
}
