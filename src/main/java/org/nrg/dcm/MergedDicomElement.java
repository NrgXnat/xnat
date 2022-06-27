/*
 * DicomDB: org.nrg.dcm.MergedDicomElement
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.dcm4che2.data.DateRange;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.TagUtils;
import org.nrg.util.ByteArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * A DicomElement with values merged from multiple primary elements. Merged
 * values are included as (unique) multiple values. Some attributes, such as
 * Image Orientation (Patient) and Pixel Spacing; and some VRs cannot be
 * sensibly merged, so multiple values for such cases are mapped into null
 * content.
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 * 
 */
public final class MergedDicomElement implements DicomElement {
	private static final long serialVersionUID = 1L;

	private static final ThreadLocal<char[]> cbuf = new ThreadLocal<char[]>() {
		@Override
		protected char[] initialValue() {
			return new char[64];
		}
	};

	private static final Set<Integer> unmergeable = ImmutableSet.of(Tag.ImageOrientationPatient,
			Tag.PixelSpacing);	// TODO: find more. they're out there.
	
	private static final Set<VR> unmergeable_VRs = ImmutableSet.of(VR.AE, VR.AS, VR.AT, VR.DA,
			VR.DT, VR.OB, VR.OF, VR.OW, VR.PN, VR.SQ, VR.TM, VR.UN, VR.UT);
	
	private static final byte[] STRING_DELIM = "\\".getBytes();
	private static final byte[] BINARY_DELIM = new byte[0];

	private static final Map<VR,byte[]> separators = new ImmutableMap.Builder<VR,byte[]>()
	.put(VR.CS, STRING_DELIM)
	.put(VR.DS, STRING_DELIM)
	.put(VR.FL, BINARY_DELIM)
	.put(VR.FD, BINARY_DELIM)
	.put(VR.IS, STRING_DELIM)
	.put(VR.LO, STRING_DELIM)
	.put(VR.LT, STRING_DELIM)
	.put(VR.SH, STRING_DELIM)
	.put(VR.SL, BINARY_DELIM)
	.put(VR.SS, BINARY_DELIM)
	.put(VR.ST, STRING_DELIM)
	.put(VR.UI, STRING_DELIM)
	.put(VR.UL, BINARY_DELIM)
	.put(VR.US, BINARY_DELIM)
	.build();
	
	private static final Map<VR,Integer> binarySizes = new ImmutableMap.Builder<VR,Integer>()
	.put(VR.FL, 4)
	.put(VR.FD, 8)
	.put(VR.SL, 4)
	.put(VR.SS, 2)
	.put(VR.UL, 4)
	.put(VR.US, 2)
	.build();

	private final Logger logger = LoggerFactory.getLogger(MergedDicomElement.class);

	private final int tag;
	private final VR vr;
	private final boolean isBigEndian;
	private final Set<byte[]> unmerged = Sets.newLinkedHashSet();
	private byte[] bytes;

	public MergedDicomElement(final DicomObject context,
			final DicomElement e, final DicomElement...es) {
		check(e);
		this.tag = e.tag();
		this.vr = e.vr();
		this.isBigEndian = e.bigEndian();
		this.bytes = e.getBytes();
		add(es);
	}

	private static DicomElement check(final DicomElement e) {
		if (e.hasItems()) {
			throw new IllegalArgumentException("cannot merge compound DicomElement");
		}
		return e;
	}

	public synchronized DicomElement add(final DicomElement e) {
		check(e);
		if (tag != e.tag()) {
			throw new IllegalArgumentException("can't merge DicomElements with different tags");
		}
		if (vr != e.vr()) {
			throw new IllegalArgumentException("can't merge DicomElements with different VRs");
		}
		if (isBigEndian != e.bigEndian()) {
			throw new IllegalArgumentException("can't merge DicomElements with different endianness");
		}
		final byte[] newBytes = e.getBytes();
		synchronized (this) {
			if (!Arrays.equals(bytes, newBytes)) {
				if (unmergeable.contains(tag) || unmergeable_VRs.contains(e.vr())) {
					if (null != this.bytes) {
						logger.debug("Unmergeable attribute {} has multiple values; setting to null",
								TagUtils.toString(tag));
					}
					this.bytes = null;
					assert unmerged.isEmpty();
				} else {
					if (null != this.bytes) {
						unmerged.add(this.bytes);
						this.bytes = null;
					}
					unmerged.add(newBytes);
					logger.trace("{} elements unmerged: {}", unmerged.size(), unmerged);
				}
			}
		}
		return this;
	}

	public DicomElement add(final DicomElement...es) {
		for (final DicomElement e : es) {
			add(e);
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#addDicomObject(org.dcm4che2.data.DicomObject)
	 */
	public DicomObject addDicomObject(final DicomObject item) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#addDicomObject(int, org.dcm4che2.data.DicomObject)
	 */
	public DicomObject addDicomObject(int i, DicomObject item) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#addFragment(byte[])
	 */
	public byte[] addFragment(byte[] bytes) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#addFragment(int, byte[])
	 */
	public byte[] addFragment(int i, byte[] bytes) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#bigEndian()
	 */
	public boolean bigEndian() {
		return isBigEndian;
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#bigEndian(boolean)
	 */
	public DicomElement bigEndian(final boolean be) {
		if (be != isBigEndian) {
			throw new UnsupportedOperationException();
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#countItems()
	 */
	public int countItems() {
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#filterItems(org.dcm4che2.data.DicomObject)
	 */
	public DicomElement filterItems(DicomObject filter) {
		throw new UnsupportedOperationException();
	}

	private static final ByteArray[] EMPTY_BA_ARRAY = {};

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getBytes()
	 */
	public byte[] getBytes() {
		synchronized (this) {
			if (!unmerged.isEmpty()) {
				logger.trace("merging {} for {} {}",
						new Object[]{unmerged, vr, TagUtils.toString(tag)});
				assert null == bytes;
				final Set<ByteArray> frags = Sets.newLinkedHashSet();
				final byte[] separator = separators.get(vr);
				final boolean isBinaryMerge = BINARY_DELIM == separator;
				final int binaryLength = isBinaryMerge ? binarySizes.get(vr) : 0;
				for (final byte[] bs : unmerged) {
					Iterables.addAll(frags, isBinaryMerge ? new ByteArray(bs).splitBy(binaryLength)
							: new ByteArray(bs).split(separator));
				}
				bytes = ByteArray.merge(separator, frags.toArray(EMPTY_BA_ARRAY)).getBytes();
				unmerged.clear();
				
				logger.trace("merged to {}", isBinaryMerge ? Arrays.toString(bytes) : new String(bytes));
			}
			return bytes;
		}
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getDate(boolean)
	 */
	public Date getDate(boolean cache) {
		return vr.toDate(getBytes());
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getDateRange(boolean)
	 */
	public DateRange getDateRange(boolean cache) {
		return vr.toDateRange(getBytes());
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getDates(boolean)
	 */
	public Date[] getDates(boolean cache) {
		return vr.toDates(getBytes());
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getDicomObject()
	 */
	public DicomObject getDicomObject() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getDicomObject(int)
	 */
	public DicomObject getDicomObject(int i) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getDouble(boolean)
	 */
	public double getDouble(boolean cache) {
		return vr.toDouble(getBytes(), isBigEndian);
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getDoubles(boolean)
	 */
	public double[] getDoubles(boolean cache) {
		return vr.toDoubles(getBytes(), isBigEndian);
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getFloat(boolean)
	 */
	public float getFloat(boolean cache) {
		return vr.toFloat(getBytes(), isBigEndian);
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getFloats(boolean)
	 */
	public float[] getFloats(boolean cache) {
		return vr.toFloats(getBytes(), isBigEndian);
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getFragment(int)
	 */
	public byte[] getFragment(int i) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getInt(boolean)
	 */
	public int getInt(boolean cache) {
		return vr.toInt(getBytes(), isBigEndian);
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getInts(boolean)
	 */
	public int[] getInts(boolean cache) {
		return vr.toInts(getBytes(), isBigEndian);
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getPattern(org.dcm4che2.data.SpecificCharacterSet, boolean, boolean)
	 */
	public Pattern getPattern(SpecificCharacterSet cs, boolean ignoreCase, boolean cache) {
		return vr.toPattern(getBytes(), isBigEndian, cs, ignoreCase);
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getShorts(boolean)
	 */
	public short[] getShorts(boolean cache) {
		return vr.toShorts(getBytes(), isBigEndian);
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getString(org.dcm4che2.data.SpecificCharacterSet, boolean)
	 */
	public String getString(SpecificCharacterSet cs, boolean cache) {
		return vr.toString(getBytes(), isBigEndian, cs);
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getStrings(org.dcm4che2.data.SpecificCharacterSet, boolean)
	 */
	public String[] getStrings(SpecificCharacterSet cs, boolean cache) {
		return vr.toStrings(getBytes(), isBigEndian, cs);
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#getValueAsString(org.dcm4che2.data.SpecificCharacterSet, int)
	 */
	public String getValueAsString(SpecificCharacterSet cs, int truncate) {
		final byte[] bytes = getBytes();
		if (null == bytes || 0 == bytes.length) {
			return null;
		}
		final StringBuffer sb = new StringBuffer(64);
		vr.promptValue(bytes, isBigEndian, cs, cbuf.get(), truncate, sb);
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#hasDicomObjects()
	 */
	public boolean hasDicomObjects() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#hasFragments()
	 */
	public boolean hasFragments() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#hasItems()
	 */
	public boolean hasItems() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#isEmpty()
	 */
	public boolean isEmpty() {
		return (null == bytes || 0 == bytes.length) && unmerged.isEmpty(); 
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#length()
	 */
	public int length() {
		return getBytes().length;
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#removeDicomObject(int)
	 */
	public DicomObject removeDicomObject(int i) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#removeDicomObject(org.dcm4che2.data.DicomObject)
	 */
	public boolean removeDicomObject(DicomObject item) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#removeFragment(int)
	 */
	public byte[] removeFragment(int i) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#removeFragment(byte[])
	 */
	public boolean removeFragment(byte[] fragment) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#setDicomObject(int, org.dcm4che2.data.DicomObject)
	 */
	public DicomObject setDicomObject(int i, DicomObject item) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#setFragment(int, byte[])
	 */
	public byte[] setFragment(int i, byte[] fragment) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#share()
	 */
	public DicomElement share() {
		// NOOP
		return this;
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#tag()
	 */
	public int tag() {
		return tag;
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#toStringBuffer(java.lang.StringBuffer, int)
	 */
	public StringBuffer toStringBuffer(StringBuffer sb, int maxValLen) {
		if (null == sb) {
			sb = new StringBuffer();
		}
		TagUtils.toStringBuffer(tag, sb);
		sb.append(" ").append(vr);
		sb.append(" #").append(length());
		sb.append(" [");
		vr.promptValue(getBytes(), isBigEndian, null, cbuf.get(), maxValLen, sb);
		sb.append("]");
		return sb;
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#vm(org.dcm4che2.data.SpecificCharacterSet)
	 */
	public int vm(SpecificCharacterSet cs) {
		return vr.vm(getBytes(), cs);
	}

	/* (non-Javadoc)
	 * @see org.dcm4che2.data.DicomElement#vr()
	 */
	public VR vr() {
		return vr;
	}
}
