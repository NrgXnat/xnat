/*
 * core: org.nrg.xft.utils.ResourceFile
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.utils;

import java.io.File;

public class ResourceFile {
    private String xpath=null;
    private String xdatPath=null;
    private File f = null;
    private Long size = null;
    private String absolutePath=null;
    
    public ResourceFile(File _f){
        f=_f;
    }

    /**
     * @return the f
     */
    public File getF() {
        return f;
    }

    /**
     * @param f the f to set
     */
    public void setF(File f) {
        this.f = f;
    }

    /**
     * @return the xdatPath
     */
    public String getXdatPath() {
        return xdatPath;
    }

    /**
     * @param xdatPath the xdatPath to set
     */
    public void setXdatPath(String xdatPath) {
        this.xdatPath = xdatPath;
    }

    /**
     * @return the xpath
     */
    public String getXpath() {
        return xpath;
    }

    /**
     * @param xpath the xpath to set
     */
    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    /**
     * @return the size
     */
    public Long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * @return the absolutePath
     */
    public String getAbsolutePath() {
        return absolutePath;
    }

    /**
     * @param absolutePath the absolutePath to set
     */
    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
    
    
}
