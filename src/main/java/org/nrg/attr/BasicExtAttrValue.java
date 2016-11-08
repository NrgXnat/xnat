/*
 * ExtAttr: org.nrg.attr.BasicExtAttrValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.lang.IllegalArgumentException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents the value of an external attribute element.
 * A ExtAttrValue may have a text value (corresponding to XML element text),
 * attribute values (element attributes), or both.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class BasicExtAttrValue implements ExtAttrValue {
    private final String name;
    private final String textValue;
    private final Map<String, String> attrValues = Maps.newLinkedHashMap();

    @Override
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof BasicExtAttrValue)) {
            return false;
        }
        final BasicExtAttrValue ov = (BasicExtAttrValue) o;
        if (!StringUtils.equals(textValue, ov.textValue)) {
            if (textValue == null || ov.textValue == null) {
                return false;
            }
            if (!textValue.equals(ov.textValue)) {
                return false;
            }
        }
        assert StringUtils.equals(textValue, ov.textValue);
        return attrValues == ov.attrValues || !(attrValues == null || ov.attrValues == null) && (attrValues.equals(ov.attrValues));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, textValue, attrValues);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("<");
        sb.append(name);
        for (final Map.Entry<String, String> me : attrValues.entrySet()) {
            sb.append(" ");
            sb.append(me.getKey());
            sb.append("=\"");
            sb.append(me.getValue());
            sb.append("\"");
        }
        if (null == textValue) {
            sb.append("/>");
        } else {
            sb.append(">");
            sb.append(textValue);
            sb.append("</");
            sb.append(name);
            sb.append(">");
        }
        return sb.toString();
    }

    public BasicExtAttrValue(final String name, final String value, final Map<String, String> attrValues) {
        if (null == name) {
            throw new IllegalArgumentException("ExtAttrValue name must be non-null");
        }
        this.name = name;
        textValue = value;
        if (null != attrValues) {
            this.attrValues.putAll(attrValues);
        }
    }

    public BasicExtAttrValue(final String name, final String value) {
        this(name, value, null);
    }

    public BasicExtAttrValue(final Iterable<ExtAttrValue> toMerge) {
        this(extractMergedName(toMerge.iterator()),
             mergeText(",", toMerge.iterator()),
             mergeAttrs(",", toMerge.iterator()));
    }

    @SuppressWarnings("unchecked")
    public BasicExtAttrValue(final ExtAttrValue base, final Map<String, String> attrValues) {
        this(base.getName(), base.getText(), Utils.merge(base.getAttrs(), attrValues));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrValue#getName()
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrValue#getText()
     */
    public String getText() {
        return textValue;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.attr.ExtAttrValue#getAttrs()
     */
    public Map<String, String> getAttrs() {
        return Collections.unmodifiableMap(attrValues);
    }

    /**
     * Adds an attribute to the value element.
     *
     * @param name  The name to set for the attribute element.
     * @param value The value to set for the attribute element.
     */
    protected void addAttr(final String name, final String value) {
        if (attrValues.containsKey(name)) {
            throw new IllegalArgumentException("Redefined value attribute " + name);
        }
        attrValues.put(name, value);
    }

    private static String extractMergedName(final Iterator<ExtAttrValue> vi) {
        final Set<String> names = Sets.newLinkedHashSet();
        while (vi.hasNext()) {
            names.add(vi.next().getName());
        }
        if (1 == names.size()) {
            return names.iterator().next();
        } else {
            throw new IllegalArgumentException("values have no or multiple names: " + names);
        }
    }

    private static String mergeText(final String separator, final Iterator<ExtAttrValue> vi) {
        final Set<String> textvs = Sets.newLinkedHashSet();
        while (vi.hasNext()) {
            final String text = vi.next().getText();
            if (!Strings.isNullOrEmpty(text)) {
                textvs.add(text);
            }
        }
        return Joiner.on(separator).join(textvs);
    }

    private static Map<String, String> mergeAttrs(final String separator, final Iterator<ExtAttrValue> vi) {
        final SetMultimap<String, String> vals = LinkedHashMultimap.create();
        while (vi.hasNext()) {
            final ExtAttrValue v = vi.next();
            for (final Map.Entry<String, String> me : v.getAttrs().entrySet()) {
                vals.put(me.getKey(), me.getValue());
            }
        }

        final Joiner joiner = Joiner.on(separator).skipNulls();
        final Map<String, String> merged = Maps.newLinkedHashMap();
        for (final Map.Entry<String, Collection<String>> vme : vals.asMap().entrySet()) {
            merged.put(vme.getKey(), joiner.join(vme.getValue()));
        }
        return merged;
    }
}
