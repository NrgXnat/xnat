/*
 * org.nrg.xnat.helpers.dicom.DicomSummaryHeaderDump
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 5/11/2016
 * @author james@radiologics.com
 */
package org.nrg.xnat.helpers.dicom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.DicomObjectToStringParam;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.util.TagUtils;
import org.nrg.xft.XFTTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;

/**
 * The Class DicomSummaryHeaderDump.
 */
public final class DicomSummaryHeaderDump {
    
    /** The Constant columns. */
    // columns of the XFTTable
    private static final String[] columns = {
        "tag1",  // tag name, never empty.
        "tag2",  // for normal, non-sequence DICOM tags this is the empty string.
        "vr",   // DICOM Value Representation  
        "value", // Contents of the tag
        "desc"   // Description of the tag
    };

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(DicomSummaryHeaderDump.class);
    
    /** The files. */
    private final Iterable<File> files; // path to the DICOM file
    
    /** The fields. */
    private final Map<Integer,Set<String>> fields;

    
    /** The map. */
    //private final ListMultimap<String,DicomSummary> map=ArrayListMultimap.create();
    ListMultimap<String, DicomSummary> map = Multimaps.newListMultimap(
    		  new TreeMap<String, Collection<DicomSummary>>(),
    		  new Supplier<List<DicomSummary>>() {
    		    public List<DicomSummary> get() {
    		      return Lists.newArrayList();
    		    }
    		  });
    
    /**
     * Instantiates a new dicom multi header dump.
     *
     * @param files the files
     */
    DicomSummaryHeaderDump(Iterable<File> files) {
        this(files, Collections.<Integer,Set<String>>emptyMap());
    }

    /**
     * Instantiates a new dicom multi header dump.
     *
     * @param files the files
     * @param fields2 the fields2
     */
    public DicomSummaryHeaderDump(Iterable<File> files, Map<Integer, Set<String>> fields2) {
    	this.files = files;
        this.fields = ImmutableMap.copyOf(fields2);
	}

	/**
	 * Read the header of the DICOM file ignoring the pixel data.
	 *
	 * @param f the f
	 * @return the header
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws FileNotFoundException the file not found exception
	 */
    DicomObject getHeader(File f) throws IOException, FileNotFoundException {
        final int stopTag;
        if (fields.isEmpty()) {
            stopTag = Tag.PixelData;
        } else {
            stopTag = 1 + Collections.max(fields.keySet());
        }
        final StopTagInputHandler stopHandler = new StopTagInputHandler(stopTag);

        IOException ioexception = null;
        final DicomInputStream dis = new DicomInputStream(f);
        try {
            dis.setHandler(stopHandler);
            return dis.readDicomObject();
        } catch (IOException e) {
            throw ioexception = e;
        } finally {
            try {
                dis.close();
            } catch (IOException e) {
                if (null != ioexception) {
                    logger.error("unable to close DicomInputStream", e);
                    throw ioexception;
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Convert a tag into a row of the XFTTable.
     *
     * @param o Necessary so we can get to the description of the tag
     * @param e The current DICOM element
     * @param parentTag If non null, this is a nested DICOM tag.
     * @param maxLen The maximum number of characters to read from the description and value
     * @return the string[]
     */
    String[] makeRow(DicomObject o, DicomElement e, String parentTag , int maxLen) {
        String tag = TagUtils.toString(e.tag());
        String value = "";

        // If this element has nested tags it doesn't have a value and trying to 
        // extract one using dcm4che will result in an UnsupportedOperationException 
        // so check first.
        if (!e.hasDicomObjects()) {
            value = e.getValueAsString(null, maxLen);	
        }
        else {
            value = "";
        } 

        String vr = e.vr().toString();
        String desc = o.nameOf(e.tag());
        List<String> l = new ArrayList<String>();
        if (parentTag == null) {
            String[] _s = {tag,"",vr,value,desc};
            l.addAll(Arrays.asList(_s));
        }
        else {
            String[] _s = {parentTag, tag, vr, value, desc};
            l.addAll(Arrays.asList(_s));
        }
        String[] row = l.toArray(new String[l.size()]);
        return row;
    }

    
    
    
    
    
    /**
     * Reformat the existing table and list all unique values of a DICOM element in that element (similar to XNAT DICOM browser view)
     *
     * @param oldt the existing DICOM dump formatted table.
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
        	 Collection<DicomSummary> dsummary=map.get(key);
        	 String val="";
        	 int i=0;
        	 DicomSummary consolidated=new DicomSummary();
        	 for (DicomSummary dicomSummary : dsummary) {
				if (StringUtils.contains(val, dicomSummary.getValue())!=true && StringUtils.isNotBlank(dicomSummary.getValue())){
					if("".equals(val)){
						val=dicomSummary.getValue();
					}else{
						val=val+", "+dicomSummary.getValue();;
					}
				}
				if (i== dsummary.size()-1){
					consolidated=new DicomSummary(key,dicomSummary.getTag2(),consolidated.getVr(),val,dicomSummary.getDesc());
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
    	DicomSummary summary=new DicomSummary(tag1, tag2, vr, value, desc);
    	
    	map.put(tag1,summary);
    }
    
    /**
     * Render the DICOM header to an XFTTable supporting multiple level of tag nesting. Will reformat table to include a 
     * consolidated view of all dicom fields includes in this dump.
     *
     * @return the XFT table
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FileNotFoundException the file not found exception
     */
    public XFTTable render() throws IOException,FileNotFoundException {
        XFTTable t = new XFTTable();
        t.initTable(columns);
      
	
        
        for (File file : this.files) {
			DicomObject header = this.getHeader(file);
	        DicomObjectToStringParam formatParams = DicomObjectToStringParam.getDefaultParam();
	
	        for (Iterator<DicomElement> it = header.iterator(); it.hasNext();) {
	            DicomElement e = it.next();
	            write( t, header, formatParams, e);
	        }
        }
        XFTTable newt=reformat(t);
        return newt;
    }
    
    /**
     * Write.
     *
     * @param t the t
     * @param header the header
     * @param formatParams the format params
     * @param e the e
     */
    public void write(XFTTable t,DicomObject header,DicomObjectToStringParam formatParams,DicomElement e){
    	if (fields.isEmpty() || fields.containsKey(e.tag())) {
            if (e.hasDicomObjects()) {
                for (int i = 0; i < e.countItems(); i++) {
                    DicomObject o = e.getDicomObject(i);
                    t.insertRow(makeRow(header, e, TagUtils.toString(e.tag()), formatParams.valueLength));
                    for (Iterator<DicomElement> it1 = o.iterator(); it1.hasNext();) {
                        DicomElement e1 = it1.next();
                        write( t, header, formatParams, e1);
                    }
                }
            } else if (SiemensShadowHeader.isShadowHeader(header, e)) {
                SiemensShadowHeader.addRows(t, header, e, fields.get(e.tag()));
            } else {
                t.insertRow(makeRow(header, e, null, formatParams.valueLength));		
            }
    	}
    }
    
    
    
    
    
}