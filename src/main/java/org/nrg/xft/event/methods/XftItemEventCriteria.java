package org.nrg.xft.event.methods;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

/**
 * Criteria object that can be used to filter {@link XftItemEventI} objects in implementations of the
 * {@link XftItemEventHandlerMethod} interface based on the event's {@link #getXsiTypes() XSI type}
 * and {@link #getActions() action}, as well as {@link #getPredicates() custom predicate definitions}.
 * <p>
 * If nothing is specified for one of the XSI type, action, or predicate attributes, the criteria will
 * match any value for that particular attribute, with one exception: criteria do not match the {@link
 * XftItemEventI#READ} action unless you configure that as a matching action <i>explicitly</i>!
 */
@Getter(PRIVATE)
@Setter(PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public class XftItemEventCriteria {
    public static final class Builder {
        private Builder() {
        }

        private static Builder builder() {
            return new Builder();
        }

        public Builder xsiType(final String xsiType) {
            _xsiTypes.add(xsiType);
            return this;
        }

        public Builder action(final String action) {
            _actions.add(action);
            return this;
        }

        public Builder actions(final String... actions) {
            _actions.addAll(Arrays.asList(actions));
            return this;
        }

        public Builder predicate(final Predicate<XftItemEventI> predicate) {
            _predicates.add(predicate);
            return this;
        }

        public XftItemEventCriteria build() {
            return new XftItemEventCriteria(_xsiTypes, _actions, _predicates);
        }

        private final List<String>                   _xsiTypes   = new ArrayList<>();
        private final List<String>                   _actions    = new ArrayList<>();
        private final List<Predicate<XftItemEventI>> _predicates = new ArrayList<>();
    }

    public static Builder builder() {
        return Builder.builder();
    }

    public static final Predicate<XftItemEventI> IS_PROJECT_GROUP = event -> {
        if (event == null || !StringUtils.equals(XdatUsergroup.SCHEMA_ELEMENT_NAME, event.getXsiType())) {
            return false;
        }
        if (event.isMultiItemEvent()) {
            return !CollectionUtils.isEmpty(event.getIds()) && event.getIds().stream().allMatch(Groups::isProjectGroup);
        }
        return StringUtils.isNotBlank(event.getId()) && Groups.isProjectGroup(event.getId());
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
     * Convenience method for creating a criteria instance that filters on the {@link XFTItem#getXSIType() item's XSI type} and
     * one or more {@link XftItemEventI#getAction() event actions}.
     *
     * @param xsiType The XSI type to match.
     * @param actions The actions to match.
     *
     * @return A new criteria object.
     */
    @SuppressWarnings("unused")
    public static XftItemEventCriteria getXsiTypeAndActionCriteria(final String xsiType, final String... actions) {
        final Builder builder = builder().xsiType(xsiType);
        for (final String action : actions) {
            builder.action(action);
        }
        return builder.build();
    }

    /**
     * Indicates whether the submitted event matches the {@link XFTItem#getXSIType() XSI type} and {@link XftItemEvent#getAction() action}
     * for this criteria object.
     * <p>
     * Note that criteria do not match the {@link XftItemEventI#READ} event unless <i>explicitly</i> configured as a matching action!
     *
     * @param event The event to test.
     *
     * @return Returns true if the event matches the criteria, false otherwise.
     */
    public boolean matches(final XftItemEventI event) {
        final boolean matchesXsiTypes   = matchesXsiType(event.getXsiType());
        final boolean matchesAction     = matchesAction(event.getAction());
        final boolean matchesPredicates = matchesPredicates(event);
        final boolean matches           = matchesXsiTypes && matchesAction && matchesPredicates;
        if (log.isTraceEnabled()) {
            log.trace("Event {}\nCriteria {}\nMatches: {} [XSI types: {}, actions: {}, predicates: {}]", event, this, matches, matchesXsiTypes, matchesAction, matchesPredicates);
        }
        return matches;
    }

    @Override
    public String toString() {
        return "{" + Stream.of(representCriteria("XSI types", _xsiTypes), representCriteria("actions", _actions), representCriteria("predicates", _predicates)).filter(Objects::nonNull).collect(Collectors.joining(", ")) + "}";
    }

    /**
     * Creates a new criteria object, with one or more regular expression to match the desired {@link XFTItem#getXSIType() item's XSI type}
     * and {@link XftItemEvent#getAction() event action}.
     *
     * @param xsiTypes   A list of regular expressions defining matching XSI types.
     * @param actions    A list of regular expressions defining matching actions.
     * @param predicates A list of predicates defining matching event properties.
     */
    private XftItemEventCriteria(final List<String> xsiTypes, final List<String> actions, final List<Predicate<XftItemEventI>> predicates) {
        final boolean hasXsiTypes   = !CollectionUtils.isEmpty(xsiTypes);
        final boolean hasActions    = !CollectionUtils.isEmpty(actions);
        final boolean hasPredicates = !CollectionUtils.isEmpty(predicates);

        if (log.isDebugEnabled()) {
            log.debug("Creating XFT item event criteria with {}, {}, and {} predicates", representCriteria("XSI types", xsiTypes), representCriteria("actions", actions), hasPredicates ? predicates.size() : "no");
        }

        // If we have an empty list, that's a universal match.
        _xsiTypes = hasXsiTypes ? initializeCaseInsensitiveSet(xsiTypes) : Collections.emptySet();
        _actions = hasActions ? initializeCaseInsensitiveSet(actions) : Collections.emptySet();
        _predicates = hasPredicates ? predicates : Collections.emptyList();
    }

    /**
     * Create a TreeSet with case-insensitive ordering and insert the submitted list into the set. This allows for case-insensitive look-ups
     * on the items, so that looking for, e.g., "c" will properly find the item "C".
     *
     * @param items A list of items to populate the set.
     *
     * @return A set containing all submitted items.
     */
    private Set<String> initializeCaseInsensitiveSet(final List<String> items) {
        final TreeSet<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        set.addAll(items);
        return set;
    }

    private boolean matchesXsiType(final String xsiType) {
        if (_xsiTypes.contains(xsiType)) {
            return true;
        }
        for (final String mappedXsiType : _xsiTypes) {
            try {
                final GenericWrapperElement element = GenericWrapperElement.GetElement(xsiType);
                if (element.instanceOf(mappedXsiType)) {
                    return true;
                }
            } catch (XFTInitException e) {
                log.error("An XFT error occurred trying to retrieve the element for XSI type {}.", xsiType, e);
            } catch (ElementNotFoundException e) {
                log.error("Couldn't find the element {} while trying to retrieve requested type {}", e.ELEMENT, xsiType, e);
            }
        }
        return false;
    }

    private boolean matchesAction(final String action) {
        return _actions.isEmpty() && !XftItemEventI.READ.equals(action) || _actions.contains(action);
    }

    private boolean matchesPredicates(final XftItemEventI event) {
        return _predicates.isEmpty() || _predicates.stream().anyMatch(predicate -> predicate.test(event));
    }

    private static String representCriteria(final String label, final Collection<?> items) {
        return items.isEmpty() ? null : label + ": [" + StringUtils.join(items, ", ") + "]";
    }

    private final Set<String>                    _xsiTypes;
    private final Set<String>                    _actions;
    private final List<Predicate<XftItemEventI>> _predicates;
}
