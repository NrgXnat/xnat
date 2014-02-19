/*
 * org.nrg.xft.schema.XFTDataModel
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.schema;
import java.io.File;

import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.utils.XMLUtils;
public class XFTDataModel {
	public String db = "";
	public String fileLocation = "";
	public String fileName = "";
	public String packageName = "";
	public XFTSchema schema = null;
	/**
	 * ID of the DB which this schema uses.
	 * @return
	 */
	public String getDb() {
		return db;
	}

	/**
	 * Returns the local xml names of each element specified in this schema.
	 * @return ArrayList of Strings
	 */
	public java.util.ArrayList getElementNames()
	{
		return schema.getSortedElementNames();
	}
	/**
	 * Location of the schema file.
	 * @see org.nrg.xft.schema.XFTDataModel#getFullFileSpecification()
	 * @return
	 */
	public String getFileLocation() {
		return fileLocation;
	}

	/**
	 * name of the schema file (will be used to uniquely identify the schema).
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * returns the fileLocation + fileName
	 * @return
	 */
	public String getFullFileSpecification()
	{
		if (! fileLocation.endsWith(File.separator))
			fileLocation += File.separator;
		return fileLocation + fileName;
	}
	
	public String getFolderName()
	{
	    String temp = fileLocation;
	    
	    String fs = null;
	    if (temp.indexOf("/")==-1)
	    {
	        fs = File.separator;
	    }else{
	        fs = "/";
	    }
	    
	    if (temp.endsWith(File.separator))
	    {
	        temp = temp.substring(0,temp.length()-1);
	    }
	    
	    String folder = temp.substring(temp.lastIndexOf(fs) + 1);
	    return folder;
	}

	/**
	 * if the schema has been populated it is returned, else it is populated and returned.
	 * @return
	 */
	public XFTSchema getSchema() {
		if (schema == null)
		{
			try {
				setSchema();
			} catch (XFTInitException e) {
				e.printStackTrace();
			} catch (ElementNotFoundException e) {
				e.printStackTrace();
			}
		}
		return schema;
	}
	
	/**
	 * schema's target namespace prefix
	 * @return
	 */
	public String getSchemaAbbr()
	{
		return getSchema().getTargetNamespacePrefix();
	}
	
	/**
	 * schema's target namespace URI
	 * @return
	 */
	public String getURI()
	{
		return getSchema().getTargetNamespaceURI();
	}

	/**
	 * @param string
	 */
	public void setDb(String string) {
		db = string;
	}

	/**
	 * @param string
	 */
	public void setFileLocation(String string) {
		if (! string.endsWith(java.io.File.separator))
		{
			string = string + java.io.File.separator;
		}
		fileLocation = string;
	}

	/**
	 * @param string
	 */
	public void setFileName(String string) {
		fileName = string;
	}

	/**
	 * @param schema
	 */
	public void setSchema() throws XFTInitException,ElementNotFoundException {
		this.schema = new XFTSchema(XMLUtils.GetDOM(new File(this.fileLocation + this.fileName)),fileLocation,this);
	}

	/**
	 * @param schema
	 */
	public void setSchema(XFTSchema s) {
		this.schema = s;
		schema.setDataModel(this);
	}

	public String toString()
	{
		return getSchema().toString();
	}
    /**
     * @return Returns the packageName.
     */
    public String getPackageName() {
        return packageName;
    }
    /**
     * @param packageName The packageName to set.
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}

