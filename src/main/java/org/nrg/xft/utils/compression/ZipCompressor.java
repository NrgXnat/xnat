package org.nrg.xft.utils.compression;

import java.util.zip.ZipOutputStream;

public class ZipCompressor extends FileModem {

    public ZipCompressor() { super( null, ZipOutputStream::new);}
}
