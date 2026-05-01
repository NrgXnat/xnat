package org.nrg.xft.utils.compression;

import java.util.zip.GZIPOutputStream;

public class GZipCompressor extends FileModem {

    public GZipCompressor() { super(null, GZIPOutputStream::new);}
}
