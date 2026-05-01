/*
 * ecat4xnat: org.nrg.ecat.xnat.XnatAttrDef
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat.xnat;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;

import org.nrg.attr.AbstractExtAttrDef;
import org.nrg.attr.AttributesOnlyAttrDef;
import org.nrg.attr.ConstantAttrDef;
import org.nrg.attr.EvaluableAttrDef;
import org.nrg.attr.ExtAttrDef;
import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.attr.NoUniqueValueException;
import org.nrg.attr.SingleValueTextAttr;
import org.nrg.ecat.Variable;


public abstract interface XnatAttrDef extends ExtAttrDef<Variable> {
    public static abstract class Abstract<V,A> extends AbstractExtAttrDef<Variable,V,A> implements XnatAttrDef {
        public Abstract(final String name, final Variable...attrs) {
            super(name,attrs);
        }
    }

    public static class ArrayElement extends Abstract<Object,String> {
        final Variable v;
        final int i;
        public ArrayElement(final String name, final Variable var, final int i) {
            super(name, var);
            this.v = var;
            this.i = i;
        }

        /*
         * (non-Javadoc)
         * @see org.nrg.attr.EvaluableAttrDef#apply(java.lang.Object)
         */
        public Iterable<ExtAttrValue> apply(final String a) throws ExtAttrException {
            return applyString(a);
        }

        /*
         * (non-Javadoc)
         * @see org.nrg.attr.EvaluableAttrDef#foldl(java.lang.Object, java.util.Map)
         */
        public String foldl(final String a, final Map<? extends Variable, ? extends Object> m)
                throws ExtAttrException {
            final Object va = m.get(v);
            if (null == va) {
                throw new NoUniqueValueException(this.getName());
            }
            final Object o = Array.get(va, i);
            if (null == o) {
                throw new NoUniqueValueException(this.getName());
            }
            final String s = o.toString();
            if (null == a) {
                return s;
            } else if (!a.equals(s)) {
                throw new NoUniqueValueException(this.getName(), new String[]{a, s});
            } else {
                return a;
            }
        }

        /*
         * (non-Javadoc)
         * @see org.nrg.attr.Foldable#start()
         */
        public String start() { return null; }
    }

    public static class AttributesOnly extends AttributesOnlyAttrDef<Variable,Object> implements XnatAttrDef {
        public AttributesOnly(final String name, final Map<String,Variable> attrdefs) {
            super(name, attrdefs, false);
        }

        public AttributesOnly(final String name, final String[] attrs, final Variable[] nattrs) {
            super(name, attrs, nattrs, false);
        }
    }

    public static class Constant extends ConstantAttrDef<Variable> implements XnatAttrDef {
        public Constant(final String name, final String value) {
            super(name, value);
        }
    }

    public static class Date extends DateBasedAttrDef {
        private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        public Date(final String name, final Variable var) {
            super(name, var);
        }

        public Iterable<ExtAttrValue> apply(java.util.Date a) throws ExtAttrException {
            return applyString(format.format(a));
        }
    }

    public static abstract class DateBasedAttrDef extends Abstract<java.util.Date,java.util.Date> {
        private final Variable var;
        
        protected DateBasedAttrDef(final String name, final Variable var) {
            super(name, var);
            this.var = var;
        }
        
        public java.util.Date foldl(java.util.Date a, final Map<? extends Variable,? extends java.util.Date> m)
        throws ExtAttrException {
            final java.util.Date v = m.get(var);
            if (null == a) {
                return v;
            } else if (!a.equals(v)) {
                throw new NoUniqueValueException(getName(), new String[]{a.toString(), v.toString()});
            } else {
                return a;
            }
        }
        
        public java.util.Date start() { return null; }
    }

    public static class Empty extends ConstantAttrDef<Variable> implements XnatAttrDef {
        public Empty(final String name) { super(name, null); }
    }
    
    public static class Text extends SingleValueTextAttr<Variable> implements XnatAttrDef {
        public Text(final String name, final Variable var) { super(name, var); }

        public Text(final String name, final Variable var, final String format) {
            super(name, var, false, format);
        }
    }

    public static class Word implements XnatAttrDef {
        private final EvaluableAttrDef<Variable,?,String> delegate;
        public Word(final String name, final Variable var) {
            delegate = new SingleValueTextAttr<Variable>(name, var);
        }

        public Word(final String name, final Variable var, final String format) {
            delegate = new SingleValueTextAttr<Variable>(name, var, false, format);
        }

        public Iterable<ExtAttrValue> apply(final String a) throws ExtAttrException {
            return delegate.apply(a.replaceAll("\\W", "_"));
        }

        public Set<Variable> getAttrs() { return delegate.getAttrs(); }

        public String getName() { return delegate.getName(); }

        public boolean requires(final Variable attr) { return delegate.requires(attr); }
    }
}
