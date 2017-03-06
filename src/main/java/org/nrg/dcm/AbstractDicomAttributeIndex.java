/*
 * DicomDB: org.nrg.dcm.AbstractDicomAttributeIndex
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.util.TagUtils;
import org.nrg.attr.ConversionFailureException;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public abstract class AbstractDicomAttributeIndex implements DicomAttributeIndex {
	protected static final String[] EMPTY_STRING_ARRAY = new String[0];

	private static final int compare(final Integer[] a0, final Integer[] a1) {
		for (int i = 0; i < a0.length && i < a1.length; i++) {
			if (a0[i] != a1[i]) {
				if (null == a0[i]) {
					return 1;
				} else if (null == a1[i]) {
					return -1;
				} else {
					return a0[i] - a1[i];
				}
			}
		}
		return a0.length - a1.length;
	}  

	public static final String pathToString(final DicomObject context, final Integer[] path) {
	    final StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < path.length - 1; i+=2) {
	        sb.append(context.nameOf(path[i]));
	        sb.append("[");
	        sb.append(null == path[i+1] ? "*" : path[i+1]);
	        sb.append("]:");
	    }
	    sb.append(context.nameOf(path[path.length-1]));
	    return sb.toString();
	}
	    
	/* (non-Javadoc)
	 * @see org.nrg.dcm.DicomAttributeIndex#compareTo(org.nrg.dcm.DicomAttributeIndex, org.dcm4che2.data.DicomObject)
	 */
	public final int compareTo(final DicomAttributeIndex other, final DicomObject context) {
		return compare(getPath(context), other.getPath(context));
	}

	/*
	 * (non-Javadoc)
	 * @see org.nrg.dcm.DicomAttributeIndex#getAttributeName(org.dcm4che2.data.DicomObject)
	 */
	public String getAttributeName(DicomObject o) {
	    final Integer[] path = getPath(o);
	    return o.nameOf(path[path.length - 1]);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.dcm.DicomAttributeIndex#getString(org.dcm4che2.data.DicomObject)
	 */
	public final String getString(final DicomObject o) throws ConversionFailureException {
	    return getString(o, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nrg.dcm.DicomAttributeIndex#getString(org.dcm4che2.data.DicomObject, java.lang.String)
	 */
	public final String getString(final DicomObject o, final String defaultValue)
	throws ConversionFailureException {
        final DicomElement de = getElement(o);
         if (null == de) {
            return defaultValue;
        } else {
            try {
                return Joiner.on('\\').join(de.getStrings(o.getSpecificCharacterSet(), false));
            } catch (UnsupportedOperationException e) {
                throw new ConversionFailureException(this, de.getBytes(),
                        "conversion failed for " + TagUtils.toString(de.tag()) + " VR " + de.vr(), e);
            } catch (IllegalArgumentException e) {
                throw new ConversionFailureException(this, de.getBytes(),
                        "conversion failed for " + TagUtils.toString(de.tag()) + " VR " + de.vr(), e);
            }
        }
	}

	/* (non-Javadoc)
	 * @see org.nrg.dcm.DicomAttributeIndex#getStrings(org.dcm4che2.data.DicomObject)
	 */
	public final String[] getStrings(final DicomObject o) {
	    final DicomElement e = getElement(o);
	    return null == e ? null : e.getStrings(o.getSpecificCharacterSet(), false);
	}
	
    protected static Set<DicomElement> getElements(final DicomObject o,
            final Integer[] path, final int starti, final Set<DicomElement> des) {
        final int last = path.length - 1;
        if (last == starti) {
            final DicomElement de = o.get(path[starti]);
            if (null != de) {
                des.add(de);
            }
        } else {
            DicomObject subo = o;
            for (int i = starti; i < last && null != subo; i += 2) {
                final DicomElement e = subo.get(path[i]);
                if (null == e) {
                    break;
                }
                final Integer subi = path[i+1];
                if (null == subi) {
                    final int substarti = i + 2;
                    for (int ei = 0; ei < e.countItems(); ei++) {
                        getElements(e.getDicomObject(ei), path, substarti, des);
                    }
                    break;
                } else {
                    subo = e.getDicomObject(subi);
                }
            }
        }
        return des;
    }

    protected static final DicomElement getElement(final DicomObject o, final Integer[] path) {
        final Set<DicomElement> es = getElements(o, path, 0, new LinkedHashSet<DicomElement>());
        final Iterator<DicomElement> esi = es.iterator();
        if (!esi.hasNext()) {
            return null;
        }
        final DicomElement e0 = esi.next();
        if (esi.hasNext()) {
            return new MergedDicomElement(o, e0, Iterators.toArray(esi, DicomElement.class));
        } else {
            return e0;
        }
    }
}
