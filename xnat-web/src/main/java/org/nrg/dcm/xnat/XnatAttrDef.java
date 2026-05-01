/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.XnatAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.nrg.attr.*;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.FixedDicomAttributeIndex;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * Specifies an external (e.g., XNAT) attribute in terms of underlying
 * DICOM attributes.  Includes several ready-to-use subclasses for simple
 * cases:
 *
 * Text defines an element that has only a single text value,
 *      equal to the value of a single DICOM attribute
 * AttributesOnly defines an element that has only attributes (no text),
 *      each equal to the value of a single DICOM attribute
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public interface XnatAttrDef extends ExtAttrDef<DicomAttributeIndex> {
    public abstract static class Abstract<A> extends AbstractExtAttrDef<DicomAttributeIndex,String,A>
    implements XnatAttrDef {
        public Abstract(final String name, final DicomAttributeIndex...attrs) {
            super(name, attrs);
        }

        public Abstract(final String name, final Integer...attrs) {
            this(name, FixedDicomAttributeIndex.makeIndices(attrs));
        }
    }

    class Constant extends ConstantAttrDef<DicomAttributeIndex> implements XnatAttrDef {
        public Constant(final String name, final String value) {
            super(name, value);
            this.value = value;
        }
        public String getValue() {
            // Since this is a constant, we shouldn't have to go through the DicomStore to get the value
            return value;
        }
        private final String value;
    }

    class Empty extends Constant {
        public Empty(final String name) { super(name, null); }
    }

    Function<XnatAttrDef, Constant> ATTR_TO_CONSTANT_FN = new Function<XnatAttrDef, Constant>() {
        @Nullable
        @Override
        public XnatAttrDef.Constant apply(final XnatAttrDef attribute) {
            return attribute instanceof XnatAttrDef.Constant ? (XnatAttrDef.Constant) attribute : null;
        }
    };

    class NamedAttributePredicate implements Predicate<XnatAttrDef> {
        public NamedAttributePredicate(final String name) {
            this.name = name;
        }

        @Override
        public boolean apply(@Nullable final XnatAttrDef attribute) {
            return attribute != null && StringUtils.equals(name, attribute.getName());
        }

        private final String name;
    };

    class EmptySupplier implements Supplier<XnatAttrDef.Constant> {
        public EmptySupplier(final String name) {
            this.name = name;
        }

        @Override
        public XnatAttrDef.Constant get() {
            return new XnatAttrDef.Empty(name);
        }

        private final String name;
    }

    public static class Labeled<A> extends LabeledAttrDef<DicomAttributeIndex,String,A>
    implements XnatAttrDef {
        public Labeled(final EvaluableAttrDef<DicomAttributeIndex,String,A> base, final Map<String,String> labels) {
            super(base, labels);
        }

        public static <A> Labeled<A> wrap(final EvaluableAttrDef<DicomAttributeIndex,String,A> base,
                final Map<String,String> labels) {
            return new Labeled<A>(base, labels);
        }

        public static <A> Labeled<A> wrap(final EvaluableAttrDef<DicomAttributeIndex,String,A> base,
                final String[] names, final String[] values) {
            return new Labeled<A>(base, Utils.zipmap(names, values));
        }

        public static <A> Labeled<A> wrap(final EvaluableAttrDef<DicomAttributeIndex,String,A> base,
                final String name, final String value) {
            return new Labeled<A>(base, Collections.singletonMap(name, value));
        }
    }

    public static class Text extends SingleValueTextAttr<DicomAttributeIndex>
    implements XnatAttrDef {
        public Text(final String name, final DicomAttributeIndex attr, final String format) {
            super(name, attr, true, format);
        }

        public Text(final String name, final DicomAttributeIndex attr) {
            super(name, attr);
        }

        public Text(final String name, final int tag, final String format) {
            this(name, new FixedDicomAttributeIndex(tag), format);
        }

        public Text(final String name, final int tag) {
            this(name, new FixedDicomAttributeIndex(tag));
        }

        /*
         * (non-Javadoc)
         * @see org.nrg.attr.AbstractExtAttrDef#apply(java.lang.String)
         *
        @Override
        public Iterable<ExtAttrValue> apply(final String a) throws ExtAttrException {
            return super.apply(a);
        }
        */
    }

    public static final class Optional<A>
    extends OptionalAttrDef<DicomAttributeIndex,String,A> implements XnatAttrDef {
        public Optional(final EvaluableAttrDef<DicomAttributeIndex,String,A> attr) {
            super(attr);
        }
    }

    /**
     * Defines a translation from one DICOM attribute to one external numeric
     * value; throws an exception if the value is not a valid number
     */
    public static class Real extends RealExtAttrDef<DicomAttributeIndex>
    implements XnatAttrDef {
        public Real(final String name, final DicomAttributeIndex attr) {
            super(name, attr);
        }

        public Real(final String name, final int tag) {
            this(name, new FixedDicomAttributeIndex(tag));
        }

        /*
         * (non-Javadoc)
         * @see org.nrg.attr.AbstractExtAttrDef#apply(java.lang.Double)
         *
        @Override
        public Iterable<ExtAttrValue> apply(final Double a) throws ExtAttrException {
            return super.apply(a);
        }
        */
    }

    /**
     * Defines a translation from one DICOM attribute to one external integer value.
     * Constructor throws ConversionFailureException if the value cannot be converted to an integer
     */
    public static class Int extends IntegerExtAttrDef<DicomAttributeIndex>
    implements XnatAttrDef {
        private final Integer defaultValue;

        public Int(String name, DicomAttributeIndex index) {
            this(name, index, null);
        }

        public Int(String name, int tag) {
            this(name, new FixedDicomAttributeIndex(tag), null);
        }

        public Int(String name, int tag, int defaultValue) {
            this(name, new FixedDicomAttributeIndex(tag), defaultValue);
        }

        public Int(String name, DicomAttributeIndex index, Integer defaultValue) {
            super(name, index);
            this.defaultValue = defaultValue;
        }

        /*
         * (non-Javadoc)
         * @see org.nrg.attr.NumericExtAttrDef#apply(java.lang.Number)
         *
         */
        public Iterable<ExtAttrValue> apply(final Integer i) throws ExtAttrException {
            return null == i ? super.apply(defaultValue) : super.apply(i);
        }
    }

    /**
     * Defines a translation from DICOM to XML date format
     * Constructor throws ConversionFailureException if value cannot be converted
     */
    public static class Date extends AbstractExtAttrDef<DicomAttributeIndex,String,String>
    implements XnatAttrDef {
        private final DicomAttributeIndex index;

        public Date(final String name, final DicomAttributeIndex index) {
            super(name, index);
            this.index = index;
        }

        public Date(String name, int tag) {
            this(name, new FixedDicomAttributeIndex(tag));
        }

        public String start() { return null; }

        public String foldl(final String a, final Map<? extends DicomAttributeIndex,? extends String> m)
                throws ExtAttrException {
            final String v = m.get(index);
            if (null == v) {
                return a;
            } else if (null == a || a.equals(v)) {
                return v;
            } else {
                throw new NoUniqueValueException(getName(), Arrays.asList(a, v));
            }
        }

        public Iterable<ExtAttrValue> apply(final String a) throws ExtAttrException {
            if (a == null) {
                throw new NoUniqueValueException(getName());
            }

            final String xsddate;
            switch (a.length()) {
                case 8: // DICOM v. 3.0 and later
                    xsddate = String.format(DATE_FORMAT, a.substring(0, 4), a.substring(4, 6), a.substring(6, 8));
                    break;

                case 10:  // old DICOM
                    if (a.charAt(4) == '.' && a.charAt(7) == '.') {
                        xsddate = String.format(DATE_FORMAT, a.substring(0, 4), a.substring(5, 7), a.substring(8, 10));
                        break;
                    }
                    // else fall through to throw exception

                default:
                    try {
                        java.util.Date date = FORMATTER.parse(a);
                        xsddate = date.toString();
                    } catch (Exception e) {
                        throw new ConversionFailureException(index, a, getName(), "invalid");
                    }
            }
            return applyString(xsddate);
        }

        private static final SimpleDateFormat FORMATTER   = new SimpleDateFormat ("yyyyMMddHHmmss");
        private static final String           DATE_FORMAT = "%1$4s-%2$2s-%3$2s";
    }

    /**
     * Defines a translation from DICOM to boolean
     * Constructor throws ConversionFailureException if value cannot be converted
     */
    public static class BooleanAttr extends AbstractExtAttrDef<DicomAttributeIndex,String,Boolean>
            implements XnatAttrDef {
        private final DicomAttributeIndex index;

        public BooleanAttr(final String name, final DicomAttributeIndex index) {
            super(name, index);
            this.index = index;
        }

        public BooleanAttr(String name, int tag) {
            this(name, new FixedDicomAttributeIndex(tag));
        }

        public Boolean start() { return null; }

        public Boolean foldl(final Boolean a, final Map<? extends DicomAttributeIndex,? extends String> m)
                throws ExtAttrException {
            final String v = m.get(index);
            if (null == v) {
                return a;
            } else {
                boolean b = this.valueOf(v);
                if (null != a && !a.equals(b)) {
                    throw new NoUniqueValueException(this.getName(), Arrays.asList(a, b));
                } else {
                    return b;
                }
            }
        }

        private boolean valueOf(String v) throws ConversionFailureException {
            boolean isTrue = v.equalsIgnoreCase("true");
            boolean isFalse = v.equalsIgnoreCase("false");
            if (!isTrue && !isFalse) {
                throw new ConversionFailureException(this, v, "unable to convert");
            }
            return v.equalsIgnoreCase("true");
        }

        public Iterable<ExtAttrValue> apply(final Boolean a) throws ExtAttrException {
            return applyString(a == null ? null : String.valueOf(a));
        }
    }

    /**
     * Defines a translation from DICOM to XML time format
     * Constructor throws ConversionFailureException if value cannot be converted
     */
    public static class Time extends AbstractExtAttrDef<DicomAttributeIndex,String,String>
    implements XnatAttrDef {
        private final DicomAttributeIndex index;

        public Time(String name, DicomAttributeIndex index) {
            super(name, index);
            this.index = index;
        }

        public Time(String name, int tag) {
            this(name, new FixedDicomAttributeIndex(tag));
        }

        public String start() { return null; }

        public String foldl(final String a, final Map<? extends DicomAttributeIndex,? extends String> m)
                throws ExtAttrException {
            final String v = m.get(index);
            if (null == v) {
                return a;
            } else if (null == a || a.equals(v)) {
                return v;
            } else {
                throw new NoUniqueValueException(getName(), Arrays.asList(a, v));
            }
        }

        public Iterable<ExtAttrValue> apply(final String a) throws ExtAttrException {
            if (a == null) {
                throw new NoUniqueValueException(getName());
            }
            final String format = "%1$2s:%2$2s:%3$2s";
            final String xsdtime;
            if (a.indexOf(':') > 0) {
                // looks like old DICOM format
                if (a.length() >= 8 && a.charAt(2) == ':' && a.charAt(4) == ':') {
                    xsdtime = a.substring(0,9);    // essentially same as XML format
                } else {
                    throw new ConversionFailureException(index, a, getName(), "invalid");
                }
            } else if (a.length() >= 6) {
                xsdtime = String.format(format, a.substring(0,2), a.substring(2,4), a.substring(4,6));
            } else {
                throw new ConversionFailureException(index, a, getName(), "invalid");

            }
            return applyString(xsdtime);
        }
    }

    /**
     * Defines an external attribute for which the value element will
     * have no text, only attributes.
     */
    public static class AttributesOnly
    extends AttributesOnlyAttrDef<DicomAttributeIndex,String> implements XnatAttrDef{
        public AttributesOnly(String name, String[] attrs, DicomAttributeIndex[] tags) {
            super(name,attrs,tags,true);
        }

        public AttributesOnly(String name, String[] attrs, Integer[] tags) {
            this(name, attrs, FixedDicomAttributeIndex.makeIndices(tags));
        }

        public AttributesOnly(String name, String attr, Integer tag) {
            this(name, new String[]{attr}, new Integer[]{tag});
        }
    }

    public static class ValueWithAttributes
    extends TextWithAttrsAttrDef<DicomAttributeIndex,String> implements XnatAttrDef {
        public ValueWithAttributes(String name, DicomAttributeIndex textindex,
                final String[] attrs, final DicomAttributeIndex...indices) {
            super(name, textindex, attrs, indices, true);
        }

        public ValueWithAttributes(String name, DicomAttributeIndex texti, final String attr, final DicomAttributeIndex...indices) {
            super(name, texti, new String[]{attr}, indices, true);
        }

        public ValueWithAttributes(String name, Integer valuev, final String[] attrs, final Integer[] nattrs) {
            this(name, new FixedDicomAttributeIndex(valuev), attrs, FixedDicomAttributeIndex.makeIndices(nattrs));
        }

        public ValueWithAttributes(String name, Integer valuev, String attr, Integer...tags) {
            this(name, valuev, new String[]{attr}, tags);
        }
    }

    public static class AddParam<V, A>
    extends KeyValueAttrDef<DicomAttributeIndex,V,A> implements XnatAttrDef {
        public AddParam(final EvaluableAttrDef<DicomAttributeIndex,V,A> base) {
            super("parameters/addParam", base);
        }

        public static <V, A> AddParam<V, A> wrap(final EvaluableAttrDef<DicomAttributeIndex,V,A> base) {
            return new AddParam<V, A>(base);
        }

        public static AddParam<?, ?> wrap(final String name, final DicomAttributeIndex attr) {
            return wrap(new SingleValueTextAttr<DicomAttributeIndex>(name, attr));
        }

        public static AddParam<?, ?> wrap(final DicomAttributeIndex attr) {
            return wrap(new SingleValueTextAttr<DicomAttributeIndex>(attr.getAttributeName(null), attr));
        }
    }

    public static class AddField<V, A>
            extends KeyValueAttrDef<DicomAttributeIndex,V,A> implements XnatAttrDef {
        public AddField(final EvaluableAttrDef<DicomAttributeIndex,V,A> base) {
            super("fields/field", base);
        }

        public static <V, A> AddField<V, A> wrap(final EvaluableAttrDef<DicomAttributeIndex,V,A> base) {
            return new AddField<V, A>(base);
        }

        public static AddField<?, ?> wrap(final String name, final DicomAttributeIndex attr) {
            return wrap(new SingleValueTextAttr<DicomAttributeIndex>(name, attr));
        }

        public static AddField<?, ?> wrap(final DicomAttributeIndex attr) {
            return wrap(new SingleValueTextAttr<DicomAttributeIndex>(attr.getAttributeName(null), attr));
        }
    }
}
