package org.nrg.dicom.dicomedit.pixels;

import org.dcm4che3.imageio.codec.ImageWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageWriter;

/**
 * Whether dcm4che can encode a given transfer syntax in this deployment.
 * <p>
 * Registration alone does not answer that: the JPEG-family encoders are backed by an OpenCV native
 * library, so a syntax can be registered and still have no usable writer. The only reliable test is
 * to construct one.
 */
public final class ImageWriters {

    private static final Logger logger = LoggerFactory.getLogger(ImageWriters.class);

    private ImageWriters() {
    }

    /** Whether a writer for <b>tsuid</b> can actually be constructed here. */
    public static boolean isAvailable(final String tsuid) {
        try {
            final ImageWriterFactory.ImageWriterParam param = ImageWriterFactory.getImageWriterParam(tsuid);
            if (param == null) {
                return false;
            }
            final ImageWriter writer = ImageWriterFactory.getImageWriter(param);
            writer.dispose();
            return true;
        } catch (Exception | LinkageError e) {
            // LinkageError too: an encoder backed by a native library that is not installed fails
            // here as an Error, and the answer to "is a writer available" is still no. Catching only
            // Exception lets it escape and abandon whatever fallback the caller was working through.
            //
            // Logged rather than discarded. A caller's own warning says what happened -- the object
            // is stored in another transfer syntax -- while this says why, and the two ask different
            // things of an operator: a NoClassDefFoundError means a codec jar is missing from the
            // deployment, not that the transfer syntax has no encoder. A syntax with no writer at
            // all, RLE being the standing example, returns above on a null param and never reaches
            // this.
            logger.debug("No image writer could be constructed for transfer syntax {}", tsuid, e);
            return false;
        }
    }
}
