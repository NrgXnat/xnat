/*
 * DicomDB: org.nrg.util.ByteArray
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class ByteArray {
	private final byte[] bytes;

	public ByteArray(final byte[] bytes) {
		this.bytes = bytes;
	}

	public ByteArray(final byte[] bytes, final int start, final int length) {
		this.bytes = new byte[length];
		System.arraycopy(bytes, start, this.bytes, 0, length);
	}

	public byte[] getBytes() {
		return bytes;
	}

	public Iterable<ByteArray> splitBy(final int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("invalid splitBy parameter: " + n);
		}
		return new Iterable<ByteArray>() {
			public Iterator<ByteArray> iterator() {
				return new Iterator<ByteArray>() {
					private int i = 0;

					public boolean hasNext() {
						return i < bytes.length;
					}

					public ByteArray next() {
						final int start = i;
						i += n;
						final int tocopy =  i > bytes.length ? bytes.length - start : n;
						return new ByteArray(bytes, start, tocopy);
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	public Iterable<ByteArray> split(final ByteArray delim) {
		return split(delim.bytes);
	}

	public Iterable<ByteArray> split(final byte[] delim) {
		return new Iterable<ByteArray>() {        
			public Iterator<ByteArray> iterator() {
				return new Iterator<ByteArray>() {
					private int i = 0;

					public boolean hasNext() {
						return i < bytes.length;
					}

					public ByteArray next() {
						final int start = i;
						int di = 0;
						while (i < bytes.length) {
							if (bytes[i] == delim[di]) {
								i++;
								di++;
								if (di == delim.length) {
									return new ByteArray(bytes, start, (i - di - start));
								}
							} else if (di > 0) {
								i -= di;
								i++;
								di = 0;
							} else {
								i++;
							}
						}
						if (start > 0) {
							return new ByteArray(bytes, start, i - start);
						} else {
							return ByteArray.this;
						}
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	public static byte[] mergeBytes(final byte[] separator, final ByteArray...bs) {
		IOException ioexception = null;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if (bs.length > 0) {
				baos.write(bs[0].bytes);
			}
			for (int i = 1; i < bs.length; i++) {
				baos.write(separator);
				baos.write(bs[i].bytes);
			}
		} catch (IOException e) {
			throw new RuntimeException(ioexception = e);
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
				throw new RuntimeException (null == ioexception ? e : ioexception);
			}
		}
		return baos.toByteArray();
	}

	public static ByteArray merge(final byte[] separator, final ByteArray...bs) {
		return new ByteArray(mergeBytes(separator, bs));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	 @Override
	 public boolean equals(final Object o) {
		return null != o && o instanceof ByteArray && Arrays.equals(bytes, ((ByteArray)o).bytes);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	 @Override
	 public int hashCode() {
		return Arrays.hashCode(bytes);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	 @Override
	 public String toString() {
		return Arrays.toString(bytes);
	}
}
