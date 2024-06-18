/*
 * core: org.nrg.xdat.turbine.modules.actions.StoreActiveSearchAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.display.DisplayFieldRef;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.DisplayVersion;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.om.XdatCriteria;
import org.nrg.xdat.om.XdatCriteriaSet;
import org.nrg.xdat.om.XdatSearchField;
import org.nrg.xdat.om.XdatStoredSearch;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.search.ElementCriteria;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.SQLClause;
import org.nrg.xft.security.UserI;

/**
 * @author Tim
 *
 */
public class StoreActiveSearchAction extends SearchAction {
    public String getScreenTemplate(RunData data)
	{
	    return "XDATScreen_edit_xdat_stored_search.vm";
	}
    
    public boolean executeSearch()
    {
        return false;
    }

    public void doFinalProcessing(RunData data, Context context) throws Exception
    {
        UserI user = TurbineUtils.getUser(data);
        DisplaySearch ds = TurbineUtils.getSearch(data);
        
        XdatStoredSearch xss = new XdatStoredSearch();
        xss.setRootElementName(ds.getRootElement().getFullXMLName());
        String sortBy=ds.getSortBy();
        
        if (sortBy.indexOf(".")==-1)
        {
            xss.setSortBy_elementName(xss.getRootElementName());
            xss.setSortBy_fieldId(sortBy);
        }else{
            String elementName = sortBy.substring(0,sortBy.indexOf("."));
            String fieldId = sortBy.substring(sortBy.indexOf(".")+1);
            xss.setSortBy_elementName(elementName);
            xss.setSortBy_fieldId(fieldId);
        }
        
        xss.setProperty("xdat:stored_search.allowed_user[0].login",user.getUsername());
        

		ElementDisplay ed = DisplayManager.GetElementDisplay(xss.getRootElementName());
		DisplayVersion dv = ed.getVersion(ds.getDisplay(),"default");
		
		ArrayList displayVersions = new ArrayList();
		displayVersions.add(dv);
		
		
		if (ds.getAdditionalViews() != null && ds.getAdditionalViews().size() > 0)
		{
		    Iterator keys = ds.getAdditionalViews().iterator();
			while (keys.hasNext())
			{
			    String[] key = (String[])keys.next();
				String elementName = key[0];
				String version = key[1];
				SchemaElementI foreign = SchemaElement.GetElement(elementName);
	
				ElementDisplay foreignEd = DisplayManager.GetElementDisplay(foreign.getFullXMLName());
				DisplayVersion foreignDV = null;
				foreignDV = foreignEd.getVersion(version,"default");
				displayVersions.add(foreignDV);
			}
		}
		
		int sequence = 0;
		
		for(int i=0;i<displayVersions.size();i++)
		{
		   dv = (DisplayVersion)displayVersions.get(i);
		   Iterator iter = dv.getDisplayFieldRefIterator();
		   while(iter.hasNext())
		   {
		       DisplayFieldRef ref = (DisplayFieldRef)iter.next();
		       XdatSearchField xsf = new XdatSearchField();
		       xsf.setElementName(dv.getParentElementDisplay().getElementName());
		       xsf.setFieldId(ref.getId());
               if (ref.getHeader()==null || ref.getHeader().equals(""))
                   xsf.setHeader("  ");
               else
                   xsf.setHeader(ref.getHeader());
		       xsf.setType(ref.getDisplayField().getDataType());
		       xsf.setSequence(new Integer(i++));
		       xss.setSearchField(xsf);
		   }
		}
		
		XdatCriteriaSet set = new XdatCriteriaSet();
		
		Iterator al = ds.getCriteria().iterator();
		while (al.hasNext())
		{
		    org.nrg.xft.search.CriteriaCollection cc = (org.nrg.xft.search.CriteriaCollection)al.next();
		    set.setMethod(cc.getJoinType());
		    Iterator iter = cc.iterator();
		    while (iter.hasNext())
		    {
		        SQLClause c = (SQLClause)iter.next();
			    if (c instanceof ElementCriteria)
			    {
			        ElementCriteria ec = (ElementCriteria)c;
			        XdatCriteria criteria = new XdatCriteria();
			        criteria.setSchemaField(ec.getXMLPath());
			        criteria.setComparisonType(ec.getComparison_type());
			        criteria.setValue(ec.getValue().toString());
			        set.setCriteria(criteria);
			    }else{
			        DisplayCriteria dc = (DisplayCriteria)c;
			        XdatCriteria criteria = new XdatCriteria();
			        criteria.setSchemaField(dc.getElementName() + "." + dc.getField());
			        criteria.setComparisonType(dc.getComparisonType());
			        criteria.setValue(dc.getValue().toString());
			        set.setCriteria(criteria);
			    }
		    }
		    
		}
		
		if (set.getChildSet().size()> 0)
		{
			xss.setSearchWhere(set);
		}
		
		
		TurbineUtils.SetEditItem(xss.getItem(),data);
    }
}
