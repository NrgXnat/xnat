package org.nrg.xft.event.methods;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.XftItemEventI;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static lombok.AccessLevel.PRIVATE;

/**
 * Criteria object that can be used to filter {@link XftItemEventI} objects in implementations of the
 * {@link XftItemEventHandlerMethod} interface.
 */
@Getter(PRIVATE)
@Setter(PRIVATE)
@Slf4j
public class XftItemEventCriteria {
    public static final class Builder {
        private Builder() {
        }

        private static Builder builder() {
            return new Builder();
        }

        public Builder xsiType(final String xsiType) {
            _xsiTypeRegex.add(Pattern.compile("^" + Pattern.quote(xsiType) + "$"));
            return this;
        }

        public Builder xsiTypeRegex(final String xsiTypeRegex) {
            _xsiTypeRegex.add(Pattern.compile(xsiTypeRegex));
            return this;
        }

        public Builder action(final String action) {
            _actionRegex.add(Pattern.compile("^" + Pattern.quote(action) + "$"));
            return this;
        }

        public Builder actionRegex(final String actionRegex) {
            _actionRegex.add(Pattern.compile(actionRegex));
            return this;
        }

        public Builder predicate(final Predicate<XftItemEventI> predicate) {
            _predicates.add(predicate);
            return this;
        }

        public XftItemEventCriteria build() {
            return new XftItemEventCriteria(_xsiTypeRegex, _actionRegex, _predicates);
        }

        private final List<Pattern>                 _xsiTypeRegex = new ArrayList<>();
        private final List<Pattern>                 _actionRegex  = new ArrayList<>();
        private final List<Predicate<XftItemEventI>> _predicates   = new ArrayList<>();
    }

    public static Builder builder() {
        return Builder.builder();
    }

    public static final List<Pattern> UNIVERSAL_LIST = Collections.singletonList(Pattern.compile("^.*$"));

    /**
     * The universal matcher matches all XSI types and actions.
     */
    public static final XftItemEventCriteria UNIVERSAL = builder().build();

    public static final Predicate<XftItemEventI> isProjectGroup = new Predicate<XftItemEventI>() {
        @Override
        public boolean apply(@Nullable final XftItemEventI event) {
            return event != null &&
                   StringUtils.isNotBlank(event.getId()) &&
                   StringUtils.equals(XdatUsergroup.SCHEMA_ELEMENT_NAME, event.getXsiType()) &&
                   XdatUsergroup.PROJECT_GROUP.matcher(event.getId()).matches();
        }
    };

    /**
     * Convenience method for creating a criteria instance that just filters on the {@link XFTItem#getXSIType() item's XSI type}.
     *
     * @param xsiType The XSI type to match.
     *
     * @return A new criteria object.
     */
    public static XftItemEventCriteria getXsiTypeCriteria(final String xsiType) {
        return builder().xsiType(xsiType).build();
    }

    /**
     * Convenience method for creating a criteria instance that just filters on the item's action.
     *
     * @param action The action to match.
     *
     * @return A new criteria object.
     */
    public static XftItemEventCriteria getActionCriteria(final String action) {
        return builder().action(action).build();
    }

    /**
     * Indicates whether the submitted event matches the {@link XFTItem#getXSIType() XSI type} and {@link XftItemEvent#getAction() action}
     * for this criteria object.
     *
     * @param event The event to test.
     *
     * @return Returns true if the event matches the criteria, false otherwise.
     */
    public boolean matches(final XftItemEventI event) {
        final List<String> xsiTypes = Lists.transform(event.getTypeAndIds(), new Function<Pair<String, String>, String>() {
            @Override
            public String apply(final Pair<String, String> typeAndId) {
                return typeAndId.getKey();
            }
        });
        return matchesXsiType(xsiTypes) && matchesAction(event.getAction()) && matchesPredicates(event);
    }

    /**
     * Creates a new criteria object, with one or more regular expression to match the desired {@link XFTItem#getXSIType() item's XSI type}
     * and {@link XftItemEvent#getAction() event action}.
     *
     * @param xsiTypes   A list of regular expressions defining matching XSI types.
     * @param actions    A list of regular expressions defining matching actions.
     * @param predicates A list of predicates defining matching event properties.
     */
    private XftItemEventCriteria(final List<Pattern> xsiTypes, final List<Pattern> actions, final List<Predicate<XftItemEventI>> predicates) {
        final boolean hasXsiTypes   = !CollectionUtils.isEmpty(xsiTypes);
        final boolean hasActions    = !CollectionUtils.isEmpty(actions);
        final boolean hasPredicates = !CollectionUtils.isEmpty(predicates);

        if (log.isDebugEnabled()) {
            log.debug("Creating XFT item event criteria with XSI type patterns '{}', action patterns '{}', and {} predicates", hasXsiTypes ? StringUtils.join(xsiTypes, ", ") : "N/A", hasActions ? StringUtils.join(actions, ", ") : "N/A", hasPredicates ? predicates.size() : 0);
        }

        // If we have an empty list, that's a universal match.
        _xsiTypeRegex = hasXsiTypes ? xsiTypes : UNIVERSAL_LIST;
        _actionRegex = hasActions ? actions : UNIVERSAL_LIST;
        _predicates = hasPredicates ? predicates : Collections.<Predicate<XftItemEventI>>emptyList();
    }

    private boolean matchesXsiType(final List<String> xsiTypes) {
        for (final String xsiType : xsiTypes) {
            if (matchesXsiType(xsiType)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesXsiType(final String xsiType) {
        return testRegexList(_xsiTypeRegex, xsiType);
    }

    private boolean matchesAction(final String action) {
        return testRegexList(_actionRegex, action);
    }

    private boolean matchesPredicates(final XftItemEventI event) {
        if (_predicates.isEmpty()) {
            return true;
        }
        for (final Predicate<XftItemEventI> predicate : _predicates) {
            if (predicate.apply(event)) {
                return true;
            }
        }
        return false;
    }

    private static boolean testRegexList(final List<Pattern> patterns, final String target) {
        if (StringUtils.isBlank(target)) {
            return false;
        }
        if (patterns.isEmpty()) {
            return true;
        }
        for (final Pattern pattern : patterns) {
            if (pattern.matcher(target).matches()) {
                return true;
            }
        }
        return false;
    }

    private final List<Pattern>                 _xsiTypeRegex;
    private final List<Pattern>                 _actionRegex;
    private final List<Predicate<XftItemEventI>> _predicates;
}
