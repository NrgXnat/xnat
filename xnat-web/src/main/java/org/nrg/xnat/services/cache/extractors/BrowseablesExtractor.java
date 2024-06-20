package org.nrg.xnat.services.cache.extractors;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nrg.xdat.display.ElementDisplay.formatElementDisplays;
import static org.nrg.xnat.services.cache.DefaultGroupsAndPermissionsCache.CACHE_BROWSEABLES;

@Component
@Slf4j
public class BrowseablesExtractor extends AbstractGroupsAndPermissionsCacheDataExtractor<String, Map<String, ElementDisplay>> {
    @Autowired
    public BrowseablesExtractor(final @Lazy GroupsAndPermissionsCache cache, final NamedParameterJdbcTemplate template) {
        super(cache, CACHE_BROWSEABLES, template);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ElementDisplay> extract(final String username, final Object... parameters) {
        if (isInvalidExtractRequest(username, parameters)) {
            return Collections.emptyMap();
        }

        log.info("Extracting browseable element displays for user '{}'", username);

        final Map<String, Long> counts = getCache().getReadableCounts(username);
        log.debug("Found {} readable counts for user {}: {}", counts.size(), username, counts);

        try {
            final Map<String, ElementDisplay> browseableElements = new HashMap<>();
            final List<ElementDisplay>        elementDisplays    = getCache().getActionElementDisplays(username, SecurityManager.READ);
            if (log.isTraceEnabled()) {
                log.trace("Found {} readable action element displays for user {}: {}", elementDisplays.size(), username, formatElementDisplays(elementDisplays));
            }

            for (final ElementDisplay elementDisplay : elementDisplays) {
                final String  elementName         = elementDisplay.getElementName();
                final boolean isBrowseableElement = ElementSecurity.IsBrowseableElement(elementName);
                final boolean countsContainsKey   = counts.containsKey(elementName);
                final boolean hasOneOrMoreElement = countsContainsKey && counts.get(elementName) > 0;
                if (isBrowseableElement && countsContainsKey && hasOneOrMoreElement) {
                    log.debug("Adding element display {} for user {}", elementName, username);
                    browseableElements.put(elementName, elementDisplay);
                } else if (log.isDebugEnabled()) {
                    log.debug("Did not add element display {} for user {}: {}, {}, {}", elementName, username, isBrowseableElement ? "browseable" : "not browseable", countsContainsKey ? "counts contains key" : "counts does not contain key", hasOneOrMoreElement ? "counts has one or more elements of this type" : "counts does not have any elements of this type");
                }
            }

            log.info("Extracted {} element displays for user {}", browseableElements.size(), username);
            return browseableElements;
        } catch (ElementNotFoundException e) {
            log.warn("Element '{}' not found. This may be a data type that was installed previously but can't be located now. This warning will only be displayed once. Set logging level to DEBUG to see a message each time this occurs for each element, along with a count of the number of times the element was referenced.", e.ELEMENT);
        } catch (XFTInitException e) {
            log.error("There was an error initializing or accessing XFT", e);
        } catch (Exception e) {
            log.error("An unknown error occurred", e);
        }

        log.info("No browseable element displays found for user {} due to errors", username);
        return Collections.emptyMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getKeys() {
        return Users.getUsernames(getTemplate());
    }
}
