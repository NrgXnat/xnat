/*
 * web: org.nrg.xnat.turbine.modules.screens.PrearchiveFileList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xft.XFTTable;


public class PrearchiveFileList extends CustomTableScreen {

	@Override
	public void finalProcessing(XFTTable t, RunData data, Context context) {
		super.finalProcessing(t, data, context);
		
		final Collator c = Collator.getInstance();
		
		Collections.sort(t.rows(), new Comparator<Object[]>(){
			@Override
			public int compare(Object[] o1, Object[] o2) {
				String name1=(String)o1[0];
				String name2=(String)o2[0];
				
				if(name1.endsWith(".dcm") && name2.endsWith(".dcm")){
					String[] chunks=name1.split("-");
					String[] chunks2=name2.split("-");
					
					if(chunks.length==4 && chunks2.length==4){
						try {
							Integer n1=Integer.parseInt(chunks[2]);
							Integer n2=Integer.parseInt(chunks2[2]);
							
							return n1.compareTo(n2);
						} catch (NumberFormatException e) {
							
						}
					}
				}
				
				return c.compare(name1, name2);
			}});
	}

}
