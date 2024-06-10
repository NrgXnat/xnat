/*
 * org.nrg.xnat.helpers.ecat.ecatSummaryHeaderDump
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 5/11/2016
 * @author james@radiologics.com
 */
package org.nrg.xnat.helpers.ecat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.nrg.ecat.Header;
import org.nrg.ecat.MainHeader;
import org.nrg.ecat.MatrixDataFile;
import org.nrg.ecat.Variable;
import org.nrg.xft.XFTTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;

/**
 * The Class ecatSummaryHeaderDump.
 */
public final class EcatSummaryHeaderDump {
    
    /** The Constant columns. */
    // columns of the XFTTable
    private static final String[] columns = {
        "tag1",  // tag name, never empty.
        "tag2",  // for normal, non-sequence ecat tags this is the empty string.
        "vr",   // ecat Value Representation  
        "value", // Contents of the tag
        "desc"   // Description of the tag
    };

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(EcatSummaryHeaderDump.class);
    
    /** The files. */
    private final Iterable<File> files; // path to the ecat file
    
    /** The fields. */
    private final Map<Integer,Set<String>> fields;

    
    /** The map. */
    //private final ListMultimap<String,ecatSummary> map=ArrayListMultimap.create();
    ListMultimap<String, EcatSummary> map = Multimaps.newListMultimap(
    		  new TreeMap<String, Collection<EcatSummary>>(),
    		  new Supplier<List<EcatSummary>>() {
    		    public List<EcatSummary> get() {
    		      return Lists.newArrayList();
    		    }
    		  });
    
    /**
     * Instantiates a new ecat multi header dump.
     *
     * @param files the files
     */
    EcatSummaryHeaderDump(Iterable<File> files) {
        this(files, Collections.<Integer,Set<String>>emptyMap());
    }

    /**
     * Instantiates a new ecat multi header dump.
     *
     * @param files the files
     * @param fields2 the fields2
     */
    public EcatSummaryHeaderDump(Iterable<File> files, Map<Integer, Set<String>> fields2) {
    	this.files = files;
        this.fields = ImmutableMap.copyOf(fields2);
	}

	/**
	 * Read the header of the ecat file ignoring the pixel data.
	 *
	 * @param f the f
	 * @return the header
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws FileNotFoundException the file not found exception
	 */
    List<SortedMap<Variable,?>> getHeader(File f) throws IOException, FileNotFoundException {
   	 final MatrixDataFile ef = new MatrixDataFile(f, Variable.getVars());
   	 return ef.getScanValues();
   }

    /**
     * Convert a tag into a row of the XFTTable.
     *
     * @param o Necessary so we can get to the description of the tag
     * @param e The current ecat element
     * @param parentTag If non null, this is a nested ecat tag.
     * @param maxLen The maximum number of characters to read from the description and value
     * @return the string[]
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

    
    
    public void write(XFTTable t,SortedMap<Variable,?> hmap){
    	
    	for (final Map.Entry<Variable,?> e : hmap.entrySet()) {
			final Variable v = e.getKey();
			final Object val = e.getValue();
			final Class<?> c = val.getClass();
			if (c.isArray()) {
				final StringBuilder sb = new StringBuilder();
				final int length = Array.getLength(val);
				for (int i = 0; i < length; i++) {
	                t.insertRow(makeRow(v.toString(), Array.get(val, i), null,100));		

				}
				
			} else {
                t.insertRow(makeRow(v.toString(), val, null,100));		

			}
		
            
    	}
    }

    
    
    
    
    
    /**
     * Reformat the existing table and list all unique values of a ecat element in that element (similar to XNAT dicom browser view)
     *
     * @param oldt the existing ecat dump formatted table.
     * @return the XFT table
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FileNotFoundException the file not found exception
     */
    public XFTTable reformat(XFTTable oldt) throws IOException,FileNotFoundException {
    	 
    	
    	XFTTable t = new XFTTable();
         t.initTable(columns);
        
         for (Object[] row : oldt.rows()) {
				add2Map( (String) row[0],(String)  row[1],(String)  row[2],(String)  row[3], (String)  row[4]);
         }
         
         for (String key :map.keySet()){
        	 Collection<EcatSummary> dsummary=map.get(key);
        	 String val="";
        	 int i=0;
        	 EcatSummary consolidated=new EcatSummary();
        	 for (EcatSummary ecatSummary : dsummary) {
				//if(i!=0){
					if (StringUtils.contains(val, ecatSummary.getValue())!=true && StringUtils.isNotBlank(ecatSummary.getValue())){
						if("".equals(val)){
							val=ecatSummary.getValue();
						}else{
							val=val+", "+ecatSummary.getValue();;
						}
					}
				//}
				if (i== dsummary.size()-1){
					consolidated=new EcatSummary(key,ecatSummary.getTag2(),consolidated.getVr(),val,ecatSummary.getDesc());
				}
				i++;
			 }
        	 if(dsummary.size()>0){
				 t.insertRow(new Object[]{consolidated.getTag1(),consolidated.getTag2(),consolidated.getVr(),consolidated.getValue(),consolidated.getDesc()});
        	 }
         }
   
        return t;
    }
    
    
    
    /**
     * Add2 map.
     *
     * @param tag1 the tag1
     * @param tag2 the tag2
     * @param vr the vr
     * @param value the value
     * @param desc the desc
     */
    void add2Map(String  tag1,String tag2,String vr,String value, String desc){
    	EcatSummary summary=new EcatSummary(tag1, tag2, vr, value, desc);
    	
    	map.put(tag1,summary);
    }
    
    /**
     * Render the ecat header to an XFTTable supporting multiple level of tag nesting. Will reformat table to include a 
     * consolidated view of all ecat fields includes in this dump.
     *
     * @return the XFT table
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FileNotFoundException the file not found exception
     */
    public XFTTable render() throws IOException,FileNotFoundException {
        XFTTable t = new XFTTable();
        t.initTable(columns);
      
        for (File file : this.files) {
        	List<SortedMap<Variable,?>> values=this.getHeader(file);
	
        	 for (Iterator<SortedMap<Variable,?>> it = values.iterator(); it.hasNext();) {
             	SortedMap<Variable,?> header = it.next();
	            write( t, header);
	        }
        }
        XFTTable newt=reformat(t);
        return newt;
    }
    
    
    
    
    
    
    
}