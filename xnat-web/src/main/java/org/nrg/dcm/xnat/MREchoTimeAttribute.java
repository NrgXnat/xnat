/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.MREchoTimeAttribute
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import static org.nrg.dcm.NamedAttributes.EchoNumbers;
import static org.nrg.dcm.NamedAttributes.EchoTime;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nrg.attr.BasicExtAttrValue;
import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.attr.NoUniqueValueException;
import org.nrg.dcm.DicomAttributeIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Multiplex attribute for Echo Time (TE).  If multiple values are present, this is
 * a multiecho scan, and the values go into parameters/addParam fields named
 * MultiEcho_TE{EchoNumber} instead of parameters/te.
 *
 */
public final class MREchoTimeAttribute extends XnatAttrDef.Abstract<Map<Integer,Double>> {
    private static final String meFormat = "MultiEcho_TE%d";
    private final Logger logger = LoggerFactory.getLogger(MREchoTimeAttribute.class);

    /**
     * Creates a new instance of the class.
     */
    public MREchoTimeAttribute() {
        super("parameters/te", EchoTime, EchoNumbers);
    }

    public Map<Integer,Double> start() { return Maps.newTreeMap(); }

    public Map<Integer,Double> foldl(final Map<Integer,Double> a, final Map<? extends DicomAttributeIndex,? extends String> m)
            throws ExtAttrException {
        final String sTE = m.get(EchoTime);
        if (null == sTE) {
            return a;
        }
        final Double te;
        try {
            te = Double.parseDouble(sTE);
        } catch (NumberFormatException e) {
            throw new ConversionFailureException(EchoTime, sTE, getName(), "not valid real number");
        }
        final String sEchoNum = m.get(EchoNumbers);
        Integer echoNum;
        if (Strings.isNullOrEmpty(sEchoNum)) {
            echoNum = 0;
        } else {
            try {
                echoNum = Integer.parseInt(sEchoNum);
            } catch (NumberFormatException e) {
                logger.error("unable to parse DICOM attribute Echo Numbers", e);
                echoNum = 0;
            }
        }
        if (!a.containsKey(echoNum)) {
            a.put(echoNum, te);
            return a;
        } else if (te.equals(a.get(echoNum))) {
            return a;
        } else {
            throw new NoUniqueValueException(getName() + "[" + echoNum + "]", Arrays.asList(a.get(echoNum), te));
        }
    }

    public Iterable<ExtAttrValue> apply(final Map<Integer,Double> a) throws ExtAttrException {
        final List<ExtAttrValue> vals = Lists.newArrayList();
        if (a.isEmpty()) {
            throw new NoUniqueValueException(getName());
        } else if (1 == a.size()) {
            for (final Map.Entry<Integer,Double> me : a.entrySet()) {
                vals.add(new BasicExtAttrValue(getName(), String.valueOf(me.getValue())));
            }
        } else {
            for (final Map.Entry<Integer,Double> me : a.entrySet()) {
                vals.add(new BasicExtAttrValue("parameters/addParam", 
                        String.valueOf(me.getValue()),
                        Collections.singletonMap("name",
                                meFormat.formatted(me.getKey()))));
            }
        }
        return vals;
    }
}
