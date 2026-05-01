/*
 * DicomDB: org.nrg.dcm.TestAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;


import java.util.Arrays;
import java.util.Collections;
import java.util.Map;


import org.nrg.attr.AbstractExtAttrDef;
import org.nrg.attr.AttributesOnlyAttrDef;
import org.nrg.attr.BasicExtAttrValue;
import org.nrg.attr.ConstantAttrDef;
import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.ExtAttrDef;
import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.attr.NoUniqueValueException;
import org.nrg.attr.SingleValueTextAttr;
import org.nrg.attr.TextWithAttrsAttrDef;

/**
 * Specifies an external attribute in terms of underlying DICOM attributes.
 * Includes two ready-to-use subclasses for simple cases:
 * 
 * Text defines an element that has only a single text value,
 *      equal to the value of a single DICOM attribute
 * AttributesOnly defines an element that has only attributes (no text),
 *      each equal to the value of a single DICOM attribute
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public abstract class TestAttrDef implements ExtAttrDef<DicomAttributeIndex> {
    public abstract static class Abstract<A> extends AbstractExtAttrDef<DicomAttributeIndex,String,A> {
        public Abstract(final String name, final DicomAttributeIndex...attrs) {
            super(name, attrs);
        }
    }

    public static class Constant extends ConstantAttrDef<DicomAttributeIndex> {
        public Constant(final String name, final String value) {
            super(name,value);
        }
    }

    public static class Empty extends ConstantAttrDef<DicomAttributeIndex> {
        public Empty(final String name) { super(name, null); }
    }

    public static class Text extends SingleValueTextAttr<DicomAttributeIndex> {
        public Text(final String name, final DicomAttributeIndex attr, final String format) {
            super(name, attr, true, format);
        }

        public Text(final String name, final DicomAttributeIndex attr) { super(name, attr); }

        public Text(final String name, final int tag) {
            this(name, new FixedDicomAttributeIndex(tag));
        }
    }

    /**
     * Defines a translation from one DICOM attribute to one external numeric
     * value; throws an exception if the value is not a valid number
     */
    public static class Real extends AbstractExtAttrDef<DicomAttributeIndex,String,Double> {
        private final DicomAttributeIndex attr;

        public Real(final String name, final DicomAttributeIndex attr) {
            super(name, attr);
            this.attr = attr;
        }

        public Double start() { return null; }

        public Double foldl(final Double a, final Map<? extends DicomAttributeIndex,? extends String> m) 
                throws ExtAttrException {
            final String sv = m.get(attr);
            if (null == sv) {
                throw new NoUniqueValueException(getName());
            }
            final Double v;
            try {
                v = Double.parseDouble(sv);
            } catch (NumberFormatException e) {
                throw new ConversionFailureException(getName(), attr, "not valid real number");
            }
            if (null == a || a.equals(v)) {
                return v;
            } else {
                throw new NoUniqueValueException(getName(), Arrays.asList(a, v));
            }
        }

        public Iterable<ExtAttrValue> apply(final Double a) throws NoUniqueValueException {
            if (null == a) {
                throw new NoUniqueValueException(getName());
            } else {
                return Collections.<ExtAttrValue>singleton(new BasicExtAttrValue(getName(), a.toString()));
            }
        }
    }


    /**
     * Defines a translation from one DICOM attribute to one external integer value.
     * Constructor throws ConversionFailureException if the value cannot be converted to an integer
     */
    public static class Int extends AbstractExtAttrDef<DicomAttributeIndex,String,Integer> {
        private final DicomAttributeIndex attr;

        public Int(final String name, final DicomAttributeIndex attr) {
            super(name, attr);
            this.attr = attr;
        }

        public Integer start() { return null; }

        public Integer foldl(final Integer a, final Map<? extends DicomAttributeIndex,? extends String> m) 
                throws ExtAttrException {
            final String sv = m.get(attr);
            if (null == sv) {
                throw new NoUniqueValueException(getName());
            }
            final Integer v;
            try {
                v = Integer.parseInt(sv);
            } catch (NumberFormatException e) {
                throw new ConversionFailureException(getName(), attr, "not valid real number");
            }
            if (null == a || a.equals(v)) {
                return v;
            } else {
                throw new NoUniqueValueException(getName(), Arrays.asList(a, v));
            }
        }

        public Iterable<ExtAttrValue> apply(final Integer a) throws NoUniqueValueException {
            if (null == a) {
                throw new NoUniqueValueException(getName());
            } else {
                return Collections.<ExtAttrValue>singleton(new BasicExtAttrValue(getName(), a.toString()));
            }
        }
    }


    /**
     * Defines a translation from DICOM to XML date format
     * Constructor throws ConversionFailureException if value cannot be converted
     */
    public static class Date extends AbstractExtAttrDef<DicomAttributeIndex,String,String> {
        private final DicomAttributeIndex attr;

        public Date(final String name, final DicomAttributeIndex attr) {
            super(name, attr);
            this.attr = attr;
        }

        public Iterable<ExtAttrValue> apply(final String a) throws ExtAttrException {
            return applyString(a);
        }
        
        public String start() { return null; }

        public String foldl(final String a, final Map<? extends DicomAttributeIndex,? extends String> m) 
                throws ExtAttrException {
            final String format = "%1$4s-%2$2s-%3$2s";
            final String dcm = m.get(attr);
            if (dcm == null) {
                throw new NoUniqueValueException(getName());
            }
            final String v;
            switch (dcm.length()) {
            case 8: // DICOM v. 3.0 and later
                v = String.format(format, dcm.substring(0,4), dcm.substring(4,6), dcm.substring(6,8));
                break;

            case 10:  // old DICOM
                if (dcm.charAt(4) == '.' && dcm.charAt(7) == '.') {
                    v = String.format(format, dcm.substring(0,4), dcm.substring(5,7), dcm.substring(8,10));
                    break;
                }
                // else fall through to throw exception

            default:
                throw new ConversionFailureException(attr, dcm, getName(), "invalid");
            }

            if (null == a || a.equals(v)) {
                return v;
            } else {
                throw new NoUniqueValueException(getName(), Arrays.asList(a, v));
            }
        }
    }

    /**
     * Defines a translation from DICOM to XML time format
     * Constructor throws ConversionFailureException if value cannot be converted
     */
    public static class Time extends AbstractExtAttrDef<DicomAttributeIndex,String,String> {
        private final DicomAttributeIndex attr;

        public Time(final String name, final DicomAttributeIndex attr) {
            super(name, attr);
            this.attr = attr;
        }

        public Iterable<ExtAttrValue> apply(final String a) throws ExtAttrException {
            return applyString(a);
        }
        
        public String start() { return null; }

        public String foldl(final String a, final Map<? extends DicomAttributeIndex,? extends String> m) 
                throws ExtAttrException {
            final String format = "%1$2s:%2$2s:%3$2s";
            final String dcm = m.get(attr);
            final String v;
            if (dcm == null) {
                throw new NoUniqueValueException(getName());
            } else if (dcm.indexOf(':') > 0) {
                // looks like old DICOM format
                if (dcm.length() >= 8 && dcm.charAt(2) == ':' && dcm.charAt(4) == ':') {
                    v = dcm.substring(0,9);    // essentially same as XML format
                } else {
                    throw new ConversionFailureException(attr, dcm, getName(), "invalid"); 
                }
            } else if (dcm.length() >= 6) {
                v = String.format(format, dcm.substring(0,2), dcm.substring(2,4), dcm.substring(4,6));
            } else {
                throw new ConversionFailureException(attr, dcm, getName(), "invalid");

            }

            if (null == a || a.equals(v)) {
                return v;
            } else {
                throw new NoUniqueValueException(getName(), Arrays.asList(a, v));
            }
        }
    }

    /**
     * Defines an external attribute for which the value element will
     * have no text, only attributes.
     */
    public static class AttributesOnly extends AttributesOnlyAttrDef<DicomAttributeIndex,String> {
        public AttributesOnly(String name, String[] attrs, DicomAttributeIndex[] nativeAttrs) {
            super(name,attrs,nativeAttrs,true);
        }

        public AttributesOnly(String name, String attr, DicomAttributeIndex nativeAttr) {
            this(name, new String[]{attr}, new DicomAttributeIndex[]{nativeAttr});
        }
    }

    public static class ValueWithAttributes extends TextWithAttrsAttrDef<DicomAttributeIndex,String> {
        public ValueWithAttributes(String name, DicomAttributeIndex valuev, final String[] attrs, final DicomAttributeIndex[] nattrs) {
            super(name, valuev, attrs, nattrs, true);
        }

        public ValueWithAttributes(String name, DicomAttributeIndex valuev, String attr, DicomAttributeIndex tag) {
            this(name, valuev, new String[]{attr}, new DicomAttributeIndex[]{tag});
        }
    }
}
