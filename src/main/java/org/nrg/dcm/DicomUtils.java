/*
 * DicomUtils: org.nrg.dcm.DicomUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.io.DicomInputHandler;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class DicomUtils {
    private final static String GZIP_SUFFIX = ".gz";

    private DicomUtils() {
    }   // prevent instantiation

    public static String getStringRequired(final DicomObject o, final int tag)
            throws RequiredAttributeUnsetException {
        final String v = o.getString(tag);
        if (null == v || "".equals(v)) {
            throw new RequiredAttributeUnsetException(o, tag);
        } else {
            return v;
        }
    }

    public static String getTransferSyntaxUID(final DicomObject o) {
        // Default TS UID is Implicit VR LE (PS 3.5, Section 10)
        return o.getString(Tag.TransferSyntaxUID, UID.ImplicitVRLittleEndian);
    }

    private static StopTagInputHandler getStopTagInputHandler(final int maxTag) {
        if (maxTag > 0) {
            return new StopTagInputHandler(maxTag + 1);
        } else {
            return null;
        }
    }

    /**
     * Reads a new DicomObject from the given InputStream
     *
     * @param in      InputStream from which the object will be read
     * @param handler determines whether next DICOM element should be read
     * @return new DicomObject
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final InputStream in, final DicomInputHandler handler)
            throws IOException {
        IOException               ioexception = null;
        final BufferedInputStream bin         = new BufferedInputStream(in);
        try {
            final DicomInputStream din = new DicomInputStream(bin);
            try {
                if (null != handler) {
                    din.setHandler(handler);
                }
                final DicomObject o = din.readDicomObject();
                if (o.contains(Tag.SOPClassUID)) {
                    return o;
                } else {
                    throw new IOException("no SOP class UID in prospective DICOM object");
                }
            } catch (IOException e) {
                throw ioexception = e;
            } catch (Throwable t) {
                throw new IOException("unable to read as DICOM", t);
            } finally {
                try {
                    din.close();
                } catch (IOException e) {
                    throw ioexception = (null == ioexception) ? e : ioexception;
                }
            }
        } finally {
            try {
                bin.close();
            } catch (IOException e) {
                throw ioexception = (null == ioexception) ? e : ioexception;
            }
        }
    }

    /**
     * Reads a new DicomObject from the given InputStream
     *
     * @param in     InputStream from which the object will be read
     * @param maxTag last DICOM attribute to be included in the object
     * @return new DicomObject
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final InputStream in, final int maxTag)
            throws IOException {
        return read(in, getStopTagInputHandler(maxTag));
    }

    /**
     * Reads a complete new DicomObject from the given InputStream
     *
     * @param in InputStream from which the object will be read
     * @return new DicomObject
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final InputStream in)
            throws IOException {
        return read(in, null);
    }


    /**
     * Reads the named file into a new DicomObject
     *
     * @param file    File to be read
     * @param handler determines whether next DICOM element should be read
     * @return new DicomObject
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final File file, final DicomInputHandler handler)
            throws IOException {
        InputStream fin         = new FileInputStream(file);
        IOException ioexception = null;
        try {
            if (file.getName().endsWith(GZIP_SUFFIX)) {
                fin = new GZIPInputStream(fin);
            }
            return read(fin, handler);
        } catch (IOException e) {
            throw ioexception = e;
        } finally {
            try {
                fin.close();
            } catch (IOException e) {
                throw null == ioexception ? e : ioexception;
            }
        }
    }

    /**
     * Reads the named file into a new DicomObject
     *
     * @param file   File to be read
     * @param maxTag last tag to be included in the object
     * @return new DicomObject
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final File file, final int maxTag)
            throws IOException {
        return read(file, getStopTagInputHandler(maxTag));
    }

    /**
     * Reads the complete named file into a new DicomObject
     *
     * @param file File to be read
     * @return new DicomObject
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final File file) throws IOException {
        return read(file, null);
    }

    /**
     * Reads a DicomObject from the named resource.
     *
     * @param uri     URI of the resource to be read
     * @param handler The handler for DICOM.
     * @return The new DicomObject
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final URI uri, final DicomInputHandler handler) throws IOException {
        if ("file".equals(uri.getScheme())) {
            return org.nrg.dcm.DicomUtils.read(new File(uri), handler);
        } else if (uri.isAbsolute()) {
            final URL         url         = uri.toURL();
            final InputStream in          = url.openStream();
            IOException       ioexception = null;
            try {
                return org.nrg.dcm.DicomUtils.read(in, handler);
            } catch (IOException e) {
                throw ioexception = e;
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    throw (null == ioexception) ? e : ioexception;
                }
            }
        } else {
            throw new IllegalArgumentException("URIs must be absolute");
        }
    }

    /**
     * Reads a DicomObject from the named resource.
     *
     * @param uri    URI of the resource to be read
     * @param maxTag last tag to be included in the object
     * @return new DicomObject
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final URI uri, final int maxTag) throws IOException {
        return read(uri, getStopTagInputHandler(maxTag));
    }

    /**
     * Reads a complete DicomObject from the named resource.
     *
     * @param uri URI of the resource to be read
     * @return new DicomObject
     * @throws IOException When an error occurs reading or writing data.
     */
    public static DicomObject read(final URI uri) throws IOException {
        return read(uri, null);
    }

    public static URI getQualifiedUri(final String address) throws URISyntaxException {
        return address.startsWith("/") ? new URI("file://" + address) : new URI(address);
    }

    public static StringBuilder stripTrailingChars(final StringBuilder sb, final char toStrip) {
        for (int i = sb.length() - 1; i >= 0 && toStrip == sb.charAt(i); i--) {
            sb.deleteCharAt(i);
        }
        return sb;
    }

    public static String stripTrailingChars(final String s, final char toStrip) {
        return stripTrailingChars(new StringBuilder(s), toStrip).toString();
    }
}
