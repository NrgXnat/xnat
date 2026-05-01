/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.AbstractConditionalAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nrg.attr.AbstractExtAttrDef;
import org.nrg.dcm.DicomAttributeIndex;

import com.google.common.collect.Sets;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public abstract class AbstractConditionalAttrDef<A> extends AbstractExtAttrDef<DicomAttributeIndex,String,A>
implements Iterable<AbstractConditionalAttrDef.Rule>,XnatAttrDef {
    public static interface Rule {
        List<DicomAttributeIndex> getTags();
        String getValue(Map<? extends DicomAttributeIndex,? extends String> vals);
    }

    private static abstract class SimpleValueRule implements Rule {
        private final DicomAttributeIndex tag;

        protected SimpleValueRule(final DicomAttributeIndex tag) { this.tag = tag; }

        public final List<DicomAttributeIndex> getTags() { return Collections.singletonList(tag); }

        protected abstract String map(String val);

        public final String getValue(final Map<? extends DicomAttributeIndex,? extends String> vals) {
            return map(vals.get(tag));
        }
    }

    public static final class EqualsRule extends SimpleValueRule {
        public EqualsRule(final DicomAttributeIndex tag) { super(tag); }

        protected String map(final String val) { return val; }

        @Override
        public String toString() {
            return super.toString() + getTags().get(0);
        }
    }

    public static final class MapRule extends SimpleValueRule {
        private final Map<String,String> map;

        public MapRule(final DicomAttributeIndex tag, final Map<String,String> map) {
            super(tag);
            this.map = new HashMap<String,String>(map);
        }

        protected String map(final String val) { 
            final String m = map.get(val);
            return null == m ? val : m;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder(super.toString());
            sb.append(getTags().get(0)).append(" from ").append(map);
            return sb.toString();
        }
    }


    public static final class ContainsAssignmentRule extends MatchesPatternRule implements Rule {
        private final static String START = "(?:\\A|(?:.*[\\s,;]))", OPTWS = "\\s*", END = "(?:(?:[\\s,;].*\\Z)|\\Z)",
                START_GROUP = "(", END_GROUP = ")";
        private final static String DEFAULT_VALUE_PATTERN = "\\w*";
        private final static String DEFAULT_OP = "\\:";

        public ContainsAssignmentRule(final DicomAttributeIndex tag,
                final String id, final String op, final String valuePattern,
                int patternFlags) {
            super(tag, new StringBuilder(START)
            .append(id)
            .append(OPTWS).append(op).append(OPTWS)
            .append(START_GROUP).append(valuePattern).append(END_GROUP)
            .append(END)
            .toString(),
            patternFlags, 1);
        }

        public ContainsAssignmentRule(final DicomAttributeIndex tag, final String id, final String op, final int patternFlags) {
            this(tag, id, op, DEFAULT_VALUE_PATTERN, patternFlags);
        }

        public ContainsAssignmentRule(final DicomAttributeIndex tag, final String id, final String op) {
            this(tag, id, op, 0);
        }

        public ContainsAssignmentRule(final DicomAttributeIndex tag, final String id, final int patternFlags) {
            this(tag, id, DEFAULT_OP, DEFAULT_VALUE_PATTERN, patternFlags);
        }

        public ContainsAssignmentRule(final DicomAttributeIndex tag, final String id) {
            this(tag, id, 0);
        }
    }


    public static class MatchesPatternRule implements Rule {
        private final DicomAttributeIndex tag;
        private final int group;
        private final Pattern pattern;

        public MatchesPatternRule(final DicomAttributeIndex tag, final Pattern pattern, final int group) {
            this.tag = tag;
            this.pattern = pattern;
            this.group = group;
        }

        public MatchesPatternRule(final DicomAttributeIndex tag, final String regex, final int group) {
            this(tag, Pattern.compile(regex), group);
        }

        public MatchesPatternRule(final DicomAttributeIndex tag, final String regex, final int patternFlags, final int group) {
            this(tag, Pattern.compile(regex, patternFlags), group);
        }

        public final List<DicomAttributeIndex> getTags() { return Collections.singletonList(tag); }

        public String getValue(final Map<? extends DicomAttributeIndex,? extends String> vals) {
            final String val = vals.get(tag);
            if (null == val) {
                return null;
            } else {
                final Matcher m = pattern.matcher(val);
                return m.matches() ? m.group(group) : null;
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder(super.toString());
            sb.append(tag).append(" ~ ").append(pattern).append("[").append(group).append("]");
            return sb.toString();
        }
    }

    public static class MatchesPatternWithReplacementRule extends MatchesPatternRule {
        private final String target;
        private final String repl;

        public MatchesPatternWithReplacementRule(DicomAttributeIndex tag, Pattern pattern, int group, String target, String repl) {
            super(tag, pattern, group);
            this.target = target;
            this.repl = repl;
        }

        @Override
        public final String getValue(final Map<? extends DicomAttributeIndex,? extends String> vals) {
            String origVal = super.getValue(vals);
            if (origVal == null) {
                return null;
            }
            if (target == null || repl == null) {
                return origVal;
            }
            return origVal.replaceAll(target, repl);
        }

        @Override
        public String toString() {
            return super.toString() + " replacing " + target + " with " + repl;
        }
    }


    private final static DicomAttributeIndex[] DUMMY_INDEX_ARRAY = {};

    private final Collection<Rule> rules;

    final private static DicomAttributeIndex[] getTags(final Rule...rules) {
        final Collection<DicomAttributeIndex> tags = Sets.newTreeSet(new DicomAttributeIndex.Comparator());
        for (final Rule rule : rules) {
            tags.addAll(rule.getTags());
        }
        return tags.toArray(DUMMY_INDEX_ARRAY);
    }

    AbstractConditionalAttrDef(final String name, final Rule...rules) {
        super(name, getTags(rules));
        this.rules = new ArrayList<Rule>(Arrays.asList(rules)); 
    }

    public final Iterator<Rule> iterator() {
        return rules.iterator();
    }
}
