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

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import org.apache.commons.lang.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.xft.XFTTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    /** TagUtils.forName returns this when it cannot resolve a name, and max + 1 wraps to 0 on it. */
    private static final int TAG_NOT_RESOLVED = 0xFFFFFFFF;
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
	 * @param file the file
	 * @return the header
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws FileNotFoundException the file not found exception
	 */
    DicomObjectI getHeader(File file) throws IOException, FileNotFoundException, MizerException {
        if (fields.isEmpty()) {
            // Read the whole object rather than stopping at PixelData. The reader already excludes bulk
            // data, so the stop tag was never what skipped the pixels -- it just truncated every element
            // after them, which is why tags like (9753,1050) went missing from the dump.
            return DicomObjectFactory.newInstance(file, false);
        }
        // DICOM tags are unsigned, so a tag in a group >= 0x8000 is negative as an int and would win a
        // signed max as the smallest value. dcm4che compares stop tags unsigned, so only this needs fixing.
        final int maxTag = fields.keySet().stream().max(Integer::compareUnsigned).orElse(0);
        // maxTag + 1 wraps to 0 at 0xFFFFFFFF, and a stop tag of 0 halts the read at the first element.
        // dcm4che already reads to the end when given -1, which is the right answer for that case.
        final int stopTag = TAG_NOT_RESOLVED == maxTag ? -1 : maxTag + 1;
        return DicomObjectFactory.newInstance(file, stopTag);
    }

    /**
     * Convert a tag into a row of the XFTTable.
     *
     * @param dicomElement The current DICOM element
     * @param parentTag If non null, this is a nested DICOM tag.
     * @return the string[]
     */
    String[] makeRow(DicomElementI dicomElement, String parentTag) {
        String tag = TagUtils.toString(dicomElement.tag());
        String value = "";

        // If this element has nested tags it doesn't have a value and trying to 
        // extract one using dcm4che will result in an UnsupportedOperationException 
        // so check first.
        try {
            if (!dicomElement.hasItems()) {
                value = dicomElement.getValueAsString();
            }
            else {
                value = "";
            } 
        }catch(UnsupportedOperationException usex) {
            value = "UnsupportedBinarySequence";
        }

        String vr = dicomElement.getVRAsString();
        String desc = TagUtils.toString(dicomElement.tag());
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
    public XFTTable render() throws IOException, FileNotFoundException, MizerException {
        XFTTable t = new XFTTable();
        t.initTable(columns);
      
	
        
        for (File file : this.files) {
			DicomObjectI header = this.getHeader(file);
	        // dcm4che3 - DicomObjectToStringParam removed, using maxLen directly
	        int maxLen = 255;
	
	        for (Iterator<DicomElementI> it = header.iterator(); it.hasNext();) {
	            DicomElementI e = it.next();
	            try {
		            write( t, header,e);
	            }catch(Exception ex){
	                logger.error("Error reading dicom tag,"+ e.tag(),ex);
	            }
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
     * @param element the dicomElement
     */
    public void write(XFTTable t,DicomObjectI header,DicomElementI element){
        // Do not look the element back up in header: write() recurses into sequence items with the
        // top-level header, so getElement returns null for anything nested and isShadowHeader then
        // dereferences it. At the top level the lookup only ever returned element anyway.
    	if (fields.isEmpty() || fields.containsKey(element.tag())) {
            if (element.hasItems()) {
                for (int i = 0; i < element.countItems(); i++) {
                    DicomObjectI o = element.getDicomObject(i);
                    t.insertRow(makeRow(element, TagUtils.toString(element.tag())));
                    for (Iterator<DicomElementI> it1 = o.iterator(); it1.hasNext();) {
                        DicomElementI e1 = it1.next();
                        write( t, header, e1);
                    }
                }
            } else if (SiemensShadowHeader.isShadowHeader(header, element)) {
                SiemensShadowHeader.addRows(t, header, element, fields.get(element.tag()));
            } else {
                t.insertRow(makeRow(element, null));
            }
    	}
    }
    
    
    
    
    
}