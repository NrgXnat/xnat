package org.nrg.xft.utils.compression;

import java.util.zip.GZIPInputStream;

public class GZipDecompressor extends FileModem {
    public GZipDecompressor() { super(GZIPInputStream::new, null);}
}
