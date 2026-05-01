/*
 * DicomEdit: org.nrg.dcm.edit.TagPattern
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import java.util.Iterator;
import java.util.List;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class TagPattern implements Function<DicomObject,Iterable<Long>> {
    public static final Character ANY_DIGIT = 'X';
    public static final Character EVEN_DIGIT = '@';
    public static final Character ODD_DIGIT = '#';
    public static final ImmutableSet<Character> HEX_DIGITS = ImmutableSet.of('0','1','2','3','4','5','6','7','8','9','a','A','b','B','c','C','d','D','e','E','f','F');

    private static final long MASK_ALL = 0xffffffffL, MAX_EVEN = 0xeeeeeeeeL, MAX_ODD = 0xffffffffL;

    private static boolean matchesMod2(long v, long mask, final int mod2) {
        while (mask != 0) {
            if (0 != (mask & 0xf)) {
                if ((v & 0xf) % 2 != mod2) {
                    return false;
                }
            }
            v >>= 4;
            mask >>= 4;
        }
        return true;
    }

    private final String pattern;
    private final long exactMask, anyMask, evenMask, oddMask, exactPart;

    /**
     * Trivial tag matcher for exact tag value
     * @param exact tag
     */
    public TagPattern(final int exact) {
        exactMask = MASK_ALL;
        anyMask = evenMask = oddMask = 0L;
        exactPart = exact;
        this.pattern = String.format("0x%08x", exact);
    }

    /**
     * Builds a tag matcher from a tag specification of the form gggg,eeee
     * For each of the four digits, a hex digit 0-9a-fA-F requires a value match
     * X indicates any digit is acceptable
     * {@literal @} indicates any even digit is acceptable [02468ace]
     * # indicates any odd digit is acceptable [13579bdf]
     * @param spec tag specification
     * @throws ScriptEvaluationException When an error occurs evaluating the script.
     */
    public TagPattern(final String spec) throws ScriptEvaluationException {
        this.pattern = spec;
        if (9 != spec.length() || ',' != spec.charAt(4)) {
            throw new ScriptEvaluationException("tag must be of form gggg,eeee");
        }
        long exact = 0L, any = 0L, even = 0L, odd = 0L, exactPart = 0L;
        for (int i = 0; i < 9; i++) {
            final Character c = spec.charAt(i);
            if (c.equals(',')) {
                if (i != 4) {
                    throw new ScriptEvaluationException("tag comma at unexpected position " + i);
                }
                continue;
            }
            exact <<= 4;
            any <<= 4;
            even <<= 4;
            odd <<= 4;
            exactPart <<= 4;
            if (HEX_DIGITS.contains(c)) {
                exact |= 0xf;
                exactPart |= Integer.parseInt(String.valueOf(c), 16);
            } else if (ANY_DIGIT.equals(c)) {
                any |= 0xf;
            } else if (ODD_DIGIT.equals(c)) {
                odd |= 0xf;
            } else if (EVEN_DIGIT.equals(c)) {
                even |= 0xf;
            } else {
                throw new ScriptEvaluationException("unrecognized tag digit '" + c + "'");
            }
        }
        assert (exact | any | even | odd) == MASK_ALL;
        assert (exact & any) == 0;
        assert (exact & even) == 0;
        assert (exact & odd) == 0;
        assert (any & even) == 0;
        assert (any & odd) == 0;
        assert (even & odd) == 0;

        exactMask = exact;
        anyMask = any;
        evenMask = even;
        oddMask = odd;
        this.exactPart = exactPart;
    }

    /*
     * (non-Javadoc)
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    public Iterable<Long> apply(final DicomObject o) {
        final List<Long> tags = Lists.newArrayList();
        for (final Iterator<DicomElement> ei = o.datasetIterator(); ei.hasNext(); ) {
            final Integer tag = ei.next().tag();
            if (apply(tag)) {
                tags.add(0xffffffffL & tag);
            }
        }
        if (exactMask == MASK_ALL && !tags.contains(exactPart)) {
            tags.add(exactPart);
        }
        return tags;
    }

    /*
     * (non-Javadoc)
     * @see com.google.common.base.Predicate#apply(java.lang.Object)
     */
    public boolean apply(final Integer tag) {
        if (null == tag) {
            return false;
        }

        final long t = tag & 0xffffffffL;
        return (t & exactMask) == exactPart
                && matchesMod2(t, evenMask, 0)
                && matchesMod2(t, oddMask, 1);
    }

    /**
     * Determine the largest tag value this pattern might represent.
     * @return Returns the maximum value.
     */
    public long getTopTag() {
        return exactPart | (evenMask & MAX_EVEN) | ((oddMask | anyMask) & MAX_ODD);
    }

    /* TODO: hashCode equals */

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "TagPattern " + pattern;
    }
}
