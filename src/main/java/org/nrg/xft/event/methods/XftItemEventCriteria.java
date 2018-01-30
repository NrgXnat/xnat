package org.nrg.xft.event.methods;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.XftItemEvent;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static lombok.AccessLevel.PRIVATE;

/**
 * Criteria object that can be used to filter {@link XftItemEvent} objects in implementations of the
 * {@link XftItemEventHandlerMethod} interface.
 */
@Getter(PRIVATE)
@Setter(PRIVATE)
@Slf4j
public class XftItemEventCriteria {
    public static final class XftItemEventCriteriaBuilder {
        private XftItemEventCriteriaBuilder() {
        }

        public static XftItemEventCriteriaBuilder builder() {
            return new XftItemEventCriteriaBuilder();
        }

        public XftItemEventCriteriaBuilder xsiType(final String xsiType) {
            _xsiTypeRegex.add(Pattern.compile("^" + Pattern.quote(xsiType) + "$"));
            return this;
        }

        public XftItemEventCriteriaBuilder xsiTypeRegex(final String xsiTypeRegex) {
            _xsiTypeRegex.add(Pattern.compile(xsiTypeRegex));
            return this;
        }

        public XftItemEventCriteriaBuilder action(final String action) {
            _actionRegex.add(Pattern.compile("^" + Pattern.quote(action) + "$"));
            return this;
        }

        public XftItemEventCriteriaBuilder actionRegex(final String actionRegex) {
            _actionRegex.add(Pattern.compile(actionRegex));
            return this;
        }

        public XftItemEventCriteria build() {
            return new XftItemEventCriteria(_xsiTypeRegex, _actionRegex);
        }

        private final List<Pattern> _xsiTypeRegex = new ArrayList<>();
        private final List<Pattern> _actionRegex  = new ArrayList<>();
    }

    public static XftItemEventCriteriaBuilder builder() {
        return XftItemEventCriteriaBuilder.builder();
    }

    public static final List<Pattern> UNIVERSAL_LIST = Collections.singletonList(Pattern.compile("^.*$"));

    /**
     * The universal matcher matches all XSI types and actions.
     */
    public static final XftItemEventCriteria UNIVERSAL = builder().build();

    /**
     * Convenience method for creating a criteria instance that just filters on the {@link XFTItem#getXSIType() item's XSI type}.
     *
     * @param xsiType A regular expression defining matching XSI types.
     *
     * @return A new criteria object.
     */
    public static XftItemEventCriteria getXsiTypeCriteria(final String xsiType) {
        return builder().xsiType(xsiType).build();
    }

    /**
     * Indicates whether the submitted event matches the {@link XFTItem#getXSIType() XSI type} and {@link XftItemEvent#getAction() action}
     * for this criteria object.
     *
     * @param event The event to test.
     *
     * @return Returns true if the event matches the criteria, false otherwise.
     */
    public boolean matches(final XftItemEvent event) {
        return matchesXsiType(event.getXsiType()) && matchesAction(event.getAction());
    }

    /**
     * Creates a new criteria object, with one or more regular expression to match the desired {@link XFTItem#getXSIType() item's XSI type}
     * and {@link XftItemEvent#getAction() event action}.
     *
     * @param xsiTypes A list of regular expressions defining matching XSI types.
     * @param actions  A list of regular expressions defining matching actions.
     */
    private XftItemEventCriteria(final List<Pattern> xsiTypes, final List<Pattern> actions) {
        final boolean hasXsiTypes = !CollectionUtils.isEmpty(xsiTypes);
        final boolean hasActions  = !CollectionUtils.isEmpty(actions);

        if (log.isDebugEnabled()) {
            log.debug("Creating XFT item event criteria with XSI type patterns '{}' and action patterns '{}'", hasXsiTypes ? StringUtils.join(xsiTypes, ", ") : "N/A", hasActions ? StringUtils.join(actions, ", ") : "N/A");
        }

        // If we have an empty list, that's a universal match.
        _xsiTypeRegex = hasXsiTypes ? xsiTypes : UNIVERSAL_LIST;
        _actionRegex = hasActions ? actions : UNIVERSAL_LIST;
    }

    private boolean matchesXsiType(final String xsiType) {
        return testRegexList(_xsiTypeRegex, xsiType);
    }

    private boolean matchesAction(final String action) {
        return testRegexList(_actionRegex, action);
    }

    private static boolean testRegexList(final List<Pattern> patterns, final String target) {
        if (patterns.isEmpty()) {
            return true;
        }
        for (final Pattern pattern : patterns) {
            if (pattern.matcher(StringUtils.defaultIfBlank(target, "")).matches()) {
                return true;
            }
        }
        return false;
    }

    private final List<Pattern> _xsiTypeRegex;
    private final List<Pattern> _actionRegex;
}
