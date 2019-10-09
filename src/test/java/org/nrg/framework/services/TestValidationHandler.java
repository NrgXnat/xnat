package org.nrg.framework.services;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nullable;
import java.util.*;

@Getter
@Accessors(prefix = "_")
@Slf4j
public class TestValidationHandler extends DefaultHandler {
    public TestValidationHandler() {
        // No expected entities or injected text
    }

    public TestValidationHandler(final String expectedEntity, final String injectedText) {
        this(Collections.singletonList(expectedEntity), Collections.singletonList(injectedText));
    }

    public TestValidationHandler(final List<String> expectedEntities, final List<String> injectedText) {
        for (final String expectedEntity : expectedEntities) {
            getExpectedEntities().put(expectedEntity, false);
        }
        getInjectedText().addAll(injectedText);
    }

    public InputSource resolveEntity(final String publicId, final String systemId) {
        log.warn("Got request to resolve entity with public ID \"{}\" and system ID \"{}\"", publicId, systemId);
        return new InputSource(systemId);
    }

    public void notationDecl(final String name, final String publicId, final String systemId) {
        log.warn("Got request to resolve entity with name \"{}\", public ID \"{}\", and system ID \"{}\"", name, publicId, systemId);
    }

    public void unparsedEntityDecl(final String name, final String publicId, final String systemId, final String notationName) {
        log.warn("Got request to resolve entity with name \"{}\", public ID \"{}\", system ID \"{}\", and notation name \"{}\"", name, publicId, systemId, notationName);
    }

    public void setDocumentLocator(final Locator locator) {
        log.warn("Setting document locator with public ID \"{}\", system ID \"{}\", line {}:{}", locator.getPublicId(), locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber());
    }

    public void startDocument() {
        log.warn("Starting the document");
    }

    public void endDocument() {
        log.warn("Ending the document");
    }

    public void startPrefixMapping(final String prefix, final String uri) {
        log.warn("Starting mapping prefix \"{}\" to URI \"{}\"", prefix, uri);
    }

    public void endPrefixMapping(final String prefix) {
        log.warn("Ending mapping prefix \"{}\"", prefix);
    }

    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
        log.warn("Starting element at URL \"{}\", with local name \"{}\", qName \"{}\", and {} attributes: {}", uri, localName, qName, attributes.getLength(), displayAttributes(attributes));
    }

    public void endElement(final String uri, final String localName, final String qName) {
        log.warn("Ending element at URL \"{}\", with local name \"{}\" and qName \"{}\"", uri, localName, qName);
    }

    public void characters(final char[] characters, final int start, final int length) {
        final List<Character> data     = Arrays.asList(ArrayUtils.toObject(ArrayUtils.subarray(characters, start, start + length)));
        final List<String>    filtered = Lists.newArrayList(Iterables.filter(Lists.transform(data, CHARACTER_STRING_FUNCTION), Predicates.not(Predicates.containsPattern("^\\s*$"))));
        if (filtered.isEmpty()) {
            log.warn("Got {} characters, starting at offset {}. The resulting characters are only includes whitespace.", start, length);
        } else {
            log.warn("Got characters, starting at {} and running for {} characters: {}", start, length, data);
        }
        getBuffer().append(new String(ArrayUtils.toPrimitive(data.toArray(new Character[0]))));
    }

    public void ignorableWhitespace(final char[] characters, final int start, final int length) {
        log.warn("Got ignorable whitespace, starting at {} and running for {} characters", start, length);
    }

    public void processingInstruction(final String target, final String data) {
        log.warn("Got processing instructions targeting \"{}\": {}", target, data);
    }

    public void skippedEntity(final String name) {
        if (getExpectedEntities().containsKey(name)) {
            log.warn("Skipping expected entity: {}", name);
            getExpectedEntities().put(name, true);
        } else {
            log.warn("Skipping unexpected entity: {}", name);
        }
    }

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
    public void warning(final SAXParseException e) {
        log.warn("A warning occurred during document parsing", e);
    }

    /**
     * Indicates whether the parsed XML document was valid.
     */
    public boolean isValid() {
        final String text = getText();
        return getErrors().isEmpty() && !getExpectedEntities().containsValue(false) && Iterables.all(getInjectedText(), new Predicate<String>() {
            @Override
            public boolean apply(final String injectedText) {
                return !text.contains(injectedText);
            }
        });
    }

    public List<String> getUnskippedEntities() {
        return new ArrayList<>(Maps.filterValues(getExpectedEntities(), Predicates.equalTo(false)).keySet());
    }

    public String getText() {
        return getBuffer().toString();
    }

    private String displayAttributes(final Attributes attributes) {
        final int length = attributes.getLength();
        if (length == 0) {
            return "<no attributes>";
        }
        final List<String> values = new ArrayList<>();
        for (int index = 0; index < length; index++) {
            values.add(attributes.getType(index) + ": " + attributes.getValue(index));
        }
        return StringUtils.join(values, ", ");
    }

    private static final Function<Character, String> CHARACTER_STRING_FUNCTION = new Function<Character, String>() {
        @Override
        public String apply(@Nullable final Character character) {
            return ObjectUtils.defaultIfNull(character, Character.MIN_VALUE).toString();
        }
    };

    private final List<SAXParseException> _errors           = new ArrayList<>();
    private final Map<String, Boolean>    _expectedEntities = new HashMap<>();
    private final List<String>            _injectedText     = new ArrayList<>();
    private final StringBuilder           _buffer           = new StringBuilder();
}
