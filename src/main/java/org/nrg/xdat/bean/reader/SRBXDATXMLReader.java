/*
 * org.nrg.xdat.bean.reader.SRBXDATXMLReader
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:12 AM
 */
package org.nrg.xdat.bean.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.GZIPInputStream;

import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xnat.srb.XNATSrbConnection;
import org.xml.sax.SAXException;

import edu.sdsc.grid.io.srb.SRBFile;
import edu.sdsc.grid.io.local.LocalFile;
import edu.sdsc.grid.io.srb.SRBFileInputStream;
import edu.sdsc.grid.io.srb.SRBFileSystem;

public class SRBXDATXMLReader extends XDATXMLReader{

    public SRBXDATXMLReader() {
        super();
    }
    
    public BaseElement parse(String fullPath) throws IOException, SAXException, URISyntaxException {
        if (fullPath.startsWith("srb:")) {
            String file = fullPath.substring(6);
            SRBFileSystem srbFileSystem = XNATSrbConnection.getSRBFileSystem();
            SRBFile srbFile = new SRBFile( srbFileSystem,file);
            if (!srbFile.exists()) {
                srbFile = new SRBFile( srbFileSystem, file +".gz");
            }
            if (srbFile.exists()) {
                InputStream fis = new SRBFileInputStream(srbFile);
                if (srbFile.getName().endsWith(".gz")) {
                    fis = new GZIPInputStream(fis);
                }
                return parse(fis);
            }else {
                throw new IOException(file +  " File " + fullPath + " or " + fullPath +".gz" +  " not found");
            }
        }else {
            File f = new File(fullPath);//OBTAINED path from the mr session document
            if (!f.exists()) {
                f = new File(fullPath + ".gz");
            }
            if (f.exists()) {
                InputStream fis = new FileInputStream(f);
                if (f.getName().endsWith(".gz")) {
                    fis = new GZIPInputStream(fis);
                }
                return parse(fis);
            }else {
                throw new IOException("File " + fullPath + " or " + fullPath +".gz" +  " not found");
            }
        }
    }
    
    public static void main(String args[]) {
        SRBXDATXMLReader reader = new SRBXDATXMLReader();
        try {
            System.out.println(reader.parse("srb://test_SCAN1.xml"));
        }catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("All done");
    }
    
}
