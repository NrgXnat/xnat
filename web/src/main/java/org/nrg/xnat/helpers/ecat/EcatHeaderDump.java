/*
 * org.nrg.xnat.helpers.ECAT.ECATHeaderDump
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.ecat;

import com.google.common.collect.ImmutableMap;

import org.nrg.ecat.Header;
import org.nrg.ecat.MainHeader;
import org.nrg.ecat.MatrixDataFile;
import org.nrg.ecat.Variable;
import org.nrg.xft.XFTTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public final class EcatHeaderDump {
    // columns of the XFTTable
    private static final String[] columns = {
        "tag1",  // tag name, never empty.
        "tag2",  // for normal, non-sequence ECAT tags this is the empty string.
        "vr",   // ECAT Value Representation  
        "value", // Contents of the tag
        "desc"   // Description of the tag
    };

    private final Logger logger = LoggerFactory.getLogger(EcatHeaderDump.class);
    private final String file; // path to the ECAT file
    private final Map<Integer,Set<String>> fields;

    /**
     * @param file Path to the ECAT file
     */
    EcatHeaderDump(final String file, final Map<Integer,Set<String>> fields) {
        this.file = file;
        this.fields = ImmutableMap.copyOf(fields);
    }
    
    EcatHeaderDump(final String file) {
        this(file, Collections.<Integer,Set<String>>emptyMap());
    }

  
	/**
     * Read the header of the ECAT file ignoring the pixel data.
     * @param f 
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    List<SortedMap<Variable,?>> getHeader(File f) throws IOException, FileNotFoundException {
    	 final MatrixDataFile ef = new MatrixDataFile(f, Variable.getVars());

        
    	 return ef.getScanValues();
         //return ef.getSubheaders();
    }

    /**
     * Convert a tag into a row of the XFTTable.
     * @param o Necessary so we can get to the description of the tag
     * @param e The current ECAT element
     * @param parentTag If non null, this is a nested ECAT tag. 
     * @param maxLen The maximum number of characters to read from the description and value 
     * @return
     */
    String[] makeRow(String o, Object e, String parentTag , int maxLen) {
        String tag = o;
        String value = "";

     
        List<String> l = new ArrayList<String>();
        
        String[] _s = {tag,"",value,e.toString(),""};
        l.addAll(Arrays.asList(_s));
        
        String[] row = l.toArray(new String[l.size()]);
        return row;
    }

    /**
     * Render the ECAT header to an XFTTable supporting multiple level of tag nesting. 
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    public XFTTable render() throws IOException,FileNotFoundException {
        XFTTable t = new XFTTable();
        t.initTable(columns);
        if (this.file == null) {
            return t;
        }

       // List<Header> headers = this.getHeader(new File(this.file));
        List<SortedMap<Variable,?>> values=this.getHeader(new File(this.file));
        //ECATObjectToStringParam formatParams = ECATObjectToStringParam.getDefaultParam();

        for (Iterator<SortedMap<Variable,?>> it = values.iterator(); it.hasNext();) {
        	SortedMap<Variable,?> header = it.next();
            write( t, header);
        }
        return t;
    }
    
    public void write(XFTTable t,SortedMap<Variable,?> hmap){
    	
    	for (final Map.Entry<Variable,?> e : hmap.entrySet()) {
			final Variable v = e.getKey();
			final Object val = e.getValue();
			final Class<?> c = val.getClass();
			if (c.isArray()) {
				final StringBuilder sb = new StringBuilder();
				final int length = Array.getLength(val);
				for (int i = 0; i < length; i++) {
					if(!v.toString().startsWith("FILL")){
						t.insertRow(makeRow(v.toString(), Array.get(val, i), null,100));		
					}
				}
				
			} else {
				if(!v.toString().startsWith("FILL")){
					t.insertRow(makeRow(v.toString(), val, null,100));		
				}
			}
		
            
    	}
    }
    
    
    
}