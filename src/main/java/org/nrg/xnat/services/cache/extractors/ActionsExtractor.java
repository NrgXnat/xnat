package org.nrg.xnat.services.cache.extractors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.nrg.xdat.display.ElementDisplay.formatElementDisplays;
import static org.nrg.xdat.services.cache.GroupsAndPermissionsCache.ACTIONS;
import static org.nrg.xnat.services.cache.DefaultGroupsAndPermissionsCache.CACHE_ACTIONS;

@Component
@Slf4j
public class ActionsExtractor extends AbstractGroupsAndPermissionsCacheDataExtractor<String, Map<String, List<ElementDisplay>>> {
    private static final Function<String, List<ElementDisplay>> NEW_ARRAY_LIST_FUNCTION = key -> new ArrayList<>();

    @Autowired
    public ActionsExtractor(final @Lazy GroupsAndPermissionsCache cache, final NamedParameterJdbcTemplate template) {
        super(cache, CACHE_ACTIONS, template, List.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<ElementDisplay>> extract(final String username, final Object... parameters) {
        if (isInvalidExtractRequest(username, parameters)) {
            return Collections.emptyMap();
        }

        log.debug("Extracting actions for user {}", username);

        final Map<String, List<ElementDisplay>> elementDisplays = new HashMap<>();
        try {
            final List<ElementSecurity> securities = ElementSecurity.GetSecureElements();
            if (log.isDebugEnabled()) {
                log.debug("Evaluating {} element security objects: {}", securities.size(), securities.stream().map(security -> {
                    try {
                        return security.getElementName();
                    } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
                        log.error("Got an error trying to get an element security object name", e);
                        return "";
                    }
                }).filter(StringUtils::isNotBlank).collect(Collectors.joining(", ")));
            }
            for (final ElementSecurity elementSecurity : securities) {
                try {
                    final SchemaElement schemaElement = elementSecurity.getSchemaElement();
                    if (schemaElement != null) {
                        final String fullXMLName = schemaElement.getFullXMLName();
                        log.debug("Evaluating schema element {}", fullXMLName);
                        if (schemaElement.hasDisplay()) {
                            log.debug("Schema element {} has a display", fullXMLName);
                            for (final String action : getPartitionKeys()) {
                                log.debug("Check user {} permission for action {} on schema element {}", username, action, fullXMLName);
                                if (Permissions.canAny(username, elementSecurity.getElementName(), action)) {
                                    log.debug("User {} can {} schema element {}", username, action, fullXMLName);
                                    final ElementDisplay elementDisplay = schemaElement.getDisplay();
                                    if (elementDisplay != null) {
                                        log.debug("Adding element display {} to action {} for user {}", elementDisplay.getElementName(), action, username);
                                        elementDisplays.computeIfAbsent(action, NEW_ARRAY_LIST_FUNCTION).add(elementDisplay);
                                    }
                                } else {
                                    log.debug("User {} can not {} schema element {}", username, action, fullXMLName);
                                }
                            }
                        } else {
                            log.debug("Schema element {} does not have a display, rejecting", fullXMLName);
                        }
                    } else {
                        log.warn("Element '{}' not found. This may be a data type that was installed previously but can't be located now.", elementSecurity.getElementName());
                    }
                } catch (ElementNotFoundException e) {
                    log.warn("Element '{}' not found. This may be a data type that was installed previously but can't be located now.", e.ELEMENT);
                } catch (Exception e) {
                    log.error("An exception occurred trying to retrieve a secure element schema", e);
                }
            }
        } catch (Exception e) {
            log.error("An error occurred trying to retrieve the list of secure elements. Proceeding but things probably won't go well from here on out.", e);
        }

        try {
            for (final ElementSecurity elementSecurity : ElementSecurity.GetInSecureElements()) {
                try {
                    final SchemaElement schemaElement = elementSecurity.getSchemaElement();
                    if (schemaElement.hasDisplay()) {
                        final ElementDisplay elementDisplay = schemaElement.getDisplay();
                        log.debug("Adding all actions for insecure schema element {} to user {} permissions", elementDisplay.getElementName(), username);
                        for (final String action : getPartitionKeys()) {
                            elementDisplays.computeIfAbsent(action, NEW_ARRAY_LIST_FUNCTION).add(elementDisplay);
                        }
                    }
                } catch (ElementNotFoundException e) {
                    log.warn("Element '{}' not found. This may be a data type that was installed previously but can't be located now.", e.ELEMENT);
                } catch (Exception e) {
                    log.error("An exception occurred trying to retrieve an insecure element schema", e);
                }
            }
        } catch (Exception e) {
            log.error("An error occurred trying to retrieve the list of insecure elements. Proceeding but things probably won't go well from here on out.", e);
        }

        if (log.isTraceEnabled()) {
            final List<ElementDisplay> readElements   = elementDisplays.get(SecurityManager.READ);
            final List<ElementDisplay> editElements   = elementDisplays.get(SecurityManager.EDIT);
            final List<ElementDisplay> createElements = elementDisplays.get(SecurityManager.CREATE);
            log.trace("Cached {} elements with READ access, {} elements with EDIT access, and {} elements with CREATE access for user {}:\n * READ: {}\n * EDIT: {}\n * CREATE: {}", readElements.size(), editElements.size(), createElements.size(), username, formatElementDisplays(readElements), formatElementDisplays(editElements), formatElementDisplays(createElements));
        } else {
            log.info("Cached {} elements with READ access, {} elements with EDIT access, and {} elements with CREATE access for user {}", elementDisplays.get(SecurityManager.READ).size(), elementDisplays.get(SecurityManager.EDIT).size(), elementDisplays.get(SecurityManager.CREATE).size(), username);
        }

        return elementDisplays;
    }

    @Override
    public List<String> getPartitionKeys() {
        return ACTIONS;
    }
}
