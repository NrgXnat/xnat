/*
 * web: org.nrg.xnat.turbine.modules.screens.QDECScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.security.UserI;

public class QDECScreen extends SecureScreen {

    @Override

    protected void doBuildTemplate(RunData data, Context context) throws Exception {

        UserI user = TurbineUtils.getUser(data);

        if (user == null)

        {

            throw new Exception("Invalid User.");

        }

        DisplaySearch search = TurbineUtils.getSearch(data);

        search.setPagingOn(false);

        search.execute(new org.nrg.xdat.presentation.HTMLNoTagsAllFieldsPresenter(),TurbineUtils.getUser(data).getLogin());
        
        XFTTableI table = search.getPresentedTable();

        
        Hashtable<String, ArrayList> bins = separateColTypes(table);
        ArrayList discreteCols = bins.get("discreteCols");
        ArrayList continuousCols = bins.get("continuousCols");
        context.put("discereteCols",discreteCols);
        context.put("continuousCols", continuousCols);
        context.put("table",table);
        data.getParameters().add("popup","true");
    }

    private Hashtable<String, ArrayList> separateColTypes(XFTTableI table) {
    	Hashtable<String, ArrayList> bins = new Hashtable<String, ArrayList>();
    	ArrayList discrete = new ArrayList();
    	ArrayList continuous = new ArrayList();
    	if (table != null) {
	    	String[] cols = table.getColumns();
	    	for (int i = 0; i < cols.length; i++) {
	    		if (isDiscrete(cols[i], table)) discrete.add(cols[i]);
	    		else continuous.add(cols[i]);
	    	}
    	}
    	bins.put("discreteCols", discrete); 
    	bins.put("continuousCols", continuous);
    	return bins;
    }
    
    private boolean isDiscrete(String columnHeader, XFTTableI table) {
    	boolean rtn = false;
		Hashtable distinctValues = new Hashtable();
		if (table != null) {
			table.resetRowCursor();
			while (table.hasMoreRows()) {
				table.nextRow();
				Object rowColumnValue = table.getCellValue(columnHeader);
				if (rowColumnValue !=  null) {
					String colValue = rowColumnValue.toString();
					if (!distinctValues.containsKey(colValue)) {
						distinctValues.put(colValue, "");
					}
					if (distinctValues.size() > 3) break;
				}
			}
		}
		if (distinctValues.size() >= 2 && distinctValues.size() < 4) rtn = true;
    	return rtn;
    }
    
    
}
