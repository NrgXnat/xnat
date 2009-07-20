/**
 * $Id: ExtAttrDef.java,v 1.8 2008/04/29 19:23:08 karchie Exp $
 * Copyright (c) 2006-2008 Washington University
 */
package org.nrg.attr;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


/**
 * Definitions of external attributes in terms of native attributes.
 * The external attributes are well suited to XML generation, in that each
 * can have a text (String) value and/or multiple named text (String)
 * attributes.  The native attributes are represented by objects of type
 * S, and have values of type V.  The default V conversion in Text simply
 * uses V.toString(), but more complex conversions can be defined.
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 */
public interface ExtAttrDef<S,V> {
  ExtAttrValue convert(Map<S,V> vals) throws ConversionFailureException;
  String convertText(Map<S,V> vals) throws ConversionFailureException;
  String getName();
  List<S> getAttrs();

  boolean requires(S s);
  void require(S...ss);
  void makeOptional(S...ss);

  public static interface Optional { }

  public static interface MultiValue {
    String getValueSeparator();
  }

  /**
   * Provides a partial implementation used by many subclasses of ExtAttrDef
   * @author Kevin A. Archie <karchie@npg.wustl.edu>
   */
  public static abstract class Abstract<S,V> implements ExtAttrDef<S,V> {
    private final List<S> attrs;
    private final String name;
    private final Set<S> optional;

    /**
     * Converts from native attributes to the full representation of
     * external attributes, including text (usually derived by convertText())
     * and attribute values.  This implementation assumes that the text is
     * the only value in the attribute; override to build more complex values
     * (e.g., with child attributes).
     * @param vals Map from native attribute representations to their values.
     * @return Value structure include text and attribute components
     */
    public ExtAttrValue convert(final Map<S,V> vals) throws ConversionFailureException {
      return new ExtAttrValue(getName(), convertText(vals));
    }

    public abstract String convertText(final Map<S,V> vals) throws ConversionFailureException;

    final public String getName() { return name; }

    final public List<S> getAttrs() {
      return new LinkedList<S>(attrs);
    }


    public Abstract(final String name, final S...attrs) {
      this(name, Arrays.asList(attrs));
    }

    public Abstract(final String name, final Collection<S> attrs) {
      this.name = name;
      this.attrs = new LinkedList<S>(attrs);
      this.optional = new HashSet<S>();
    }

    public final boolean requires(final S s) {
      return !optional.contains(s);
    }

    public final void require(final S...ss) {
      optional.removeAll(Arrays.asList(ss));
    }

    public final void makeOptional(final S...ss) {
      optional.addAll(Arrays.asList(ss));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
      if (o == null || !this.getClass().equals(o.getClass())) {
        return false;
      }
      final ExtAttrDef<S,V> oad = (ExtAttrDef<S,V>) o;
      return (name.equals(oad.getName()) && attrs.equals(oad.getAttrs()));
    }

    @Override
    public int hashCode() {
      int result = 17;
      result = 37*result + name.hashCode();
      result = 37*result + attrs.hashCode();
      return result;
    }

    @Override
    public String toString() { 
      return new StringBuilder(super.toString()).append(" ").append(name).toString();
    }
  }

  public static class Labeled<S,V> implements ExtAttrDef<S,V> {
    private final ExtAttrDef<S,V> base;
    private final Map<String,String> labels;
    private final Set<S> optional;

    public Labeled(final ExtAttrDef<S,V> base, final Map<String,String> labels) {
      this.base = base;
      this.labels = new LinkedHashMap<String, String>(labels);
      this.optional = new HashSet<S>();
    }

    public Labeled(final ExtAttrDef<S,V> base, final String[] names, final String[] values) {
      this(base, Utils.put(new LinkedHashMap<String,String>(), names, values));
    }

    public Labeled(final ExtAttrDef<S,V> base, final String name, final String value) {
      this(base, new String[]{name}, new String[]{value});
    }

    public final String convertText(final Map<S,V> vals) throws ConversionFailureException {
      return base.convertText(vals);
    }

    public final ExtAttrValue convert(final Map<S,V> vals) throws ConversionFailureException {
      final ExtAttrValue eav = base.convert(vals);
      for (Map.Entry<String,String> e : labels.entrySet()) {
        eav.addAttr(e.getKey(), e.getValue());
      }
      return eav;
    }

    public final String getName() { return base.getName(); }

    public final List<S> getAttrs() { return base.getAttrs(); }

    public final boolean requires(final S s) {
      return !optional.contains(s);
    }

    public final void require(final S...ss) {
      optional.removeAll(Arrays.asList(ss));
    }

    public final void makeOptional(final S...ss) {
      optional.addAll(Arrays.asList(ss));
    }
    
    public String toString() {
      return base + "+" + labels;
    }
  }

  /**
   * Defines an external attribute with fixed value
   */
  public static class Constant<S,V> extends Abstract<S,V> {
    private final String value;
    @Override
    public String convertText(Map<S,V> vals) { return value; }
    public Constant(final String name, final String value) {
      super(name); this.value = value;
    }
  }

  /**
   * Defines an empty, placeholder external attribute
   */
  public static class Empty<S,V> extends Constant<S,V> {
    public Empty(final String name) { super(name, null); }
  }

  /**
   * Defines a trivial translation from one native attribute to
   * one external, optionally with an applied String format.  Uses
   * V.toString() as the text value.
   */
  public static class Text<S,V> extends Abstract<S,V> {
    private static final String NO_FORMAT = "%1$s";
    private final String format;

    @Override
    public String convertText(Map<S,V> vals) throws ConversionFailureException {
      assert getAttrs().size() == 1 : "Text must derive from exactly one attribute";
      final S attr = getAttrs().get(0);
      final boolean defined = vals.containsKey(attr);
      if (!defined && requires(attr)) {
        throw new ConversionFailureException(attr, null, "no value defined");
      }
      return String.format(format, vals.get(attr));
    }

    @SuppressWarnings("unchecked")
    public Text(final String name, final S attr, final String format) {
      super(name, attr);
      this.format = format;
    }

    public Text(final String name, final S attr) { this(name, attr, NO_FORMAT); }
  }

  public static abstract class AbstractWrapper<S,V> implements ExtAttrDef<S,V> {
    private final ExtAttrDef<S,V> attr;

    public AbstractWrapper(final ExtAttrDef<S,V> attr) {
      this.attr = attr;
    }

    public ExtAttrValue convert(final Map<S,V> vals) throws ConversionFailureException {
      return attr.convert(vals);
    }

    public String convertText(final Map<S,V> vals) throws ConversionFailureException {
      return attr.convertText(vals);
    }

    public String getName() { return attr.getName(); }

    public List<S> getAttrs() { return attr.getAttrs(); }

    public boolean requires(final S s) { return attr.requires(s); }

    public void require(final S...ss) { attr.require(ss); }

    public void makeOptional(final S...ss) { attr.makeOptional(ss); }
    
    public String toString() { return attr.toString(); }
  }
  
  public static class OptionalWrapper<S,V>
  extends AbstractWrapper<S,V>
  implements ExtAttrDef<S,V>,Optional {
    public OptionalWrapper(final ExtAttrDef<S,V> attr) {
      super(attr);
    }
    
    public String toString() {
      return super.toString() + " [optional]";
    }
  }

  public static class MultiValueWrapper<S,V> 
  extends AbstractWrapper<S,V>
  implements ExtAttrDef<S,V>,MultiValue {
    private final String separator;

    public MultiValueWrapper(final ExtAttrDef<S,V> attr, final String separator) {
      super(attr);
      this.separator = separator;
    }

    public String getValueSeparator() { return separator; }
  }


  /**
   * Defines an external attribute for which the text and each child
   * attribute are simple translations of a single native attribute.
   * Conversion from native to child attributes is via toString().
   */
  public static class TextWithAttributes<S,V> extends Abstract<S,V> {
    private final S na;
    private final Map<String,S> attrdefs;

    @Override
    public int hashCode() {
      return super.hashCode() + 37*attrdefs.hashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object o) {
      return super.equals(o) && attrdefs.equals(((TextWithAttributes)o).attrdefs);
    }

    @Override
    public String convertText(final Map<S,V> vals) throws ConversionFailureException {
      if (null == na)
        return null;
      final V val = vals.get(na);
      if (null == val && requires(na))
        throw new ConversionFailureException(na, null, "no value defined");
      return (null == val) ? null : val.toString();
    }

    public ExtAttrValue convert(final Map<S,V> vals) throws ConversionFailureException {
      final String name = getName();
      final ExtAttrValue val = new ExtAttrValue(name, convertText(vals));

      final Set<String> attrs = new LinkedHashSet<String>(attrdefs.keySet());
      attrs.remove(name);
      for (final String attr : attrs) {
        final S s = attrdefs.get(attr);
        final boolean defined = vals.containsKey(s);
        if (!defined && requires(s)) {
          throw new ConversionFailureException(attr, null, "no value defined");
        }
        final V attrval = vals.get(s);
        final String strval = (attrval == null) ? null : attrval.toString();
        val.addAttr(attr, strval);
      }

      return val;
    }

    private TextWithAttributes(final String name, final S s, Map<String,S> m) {
      super(name, Utils.add(new LinkedHashSet<S>(m.values()), s));
      this.na = s;
      this.attrdefs = m;
    }

    public TextWithAttributes(final String name, final S s, final String[] attrs, final S[] nattrs) {
      this(name, s, Utils.put(new HashMap<String,S>(), attrs, nattrs));
    }
  }


  /**
   * Defines an external attribute for which the value will have no text,
   * only child attributes, each defined by a single native attribute.
   */
  public static class AttributesOnly<S,V> extends TextWithAttributes<S,V> {
    public AttributesOnly(final String name, final Map<String,S> attrdefs) {
      super(name, null, attrdefs);
    }

    public AttributesOnly(final String name, final String[] attrs, final S[] nattrs) {
      super(name, null, attrs, nattrs);
    }
  }
}
