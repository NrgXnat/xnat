//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 15, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.schema.design.SchemaElementI;


/**
 * @author Tim
 *
 */
public class DisplaySearchAction extends SearchA {
	static org.apache.log4j.Logger logger = Logger.getLogger(DisplaySearchAction.class);


    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.actions.SearchA#setupSearch(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public DisplaySearch setupSearch(RunData data, Context context) throws Exception {
        if (data.getParameters().get("search_xml")!=null || data.getParameters().get("search_id") !=null || data.getRequest().getAttribute("xss")!=null){
            return TurbineUtils.getDSFromSearchXML(data);
        }else{
            String elementName= data.getParameters().getString("ELEMENT_0");
            if (elementName==null)
            {
                return null;
            }else{
                SchemaElementI se = SchemaElement.GetElement(elementName);

                DisplaySearch ds = getSearchCriteria(se, elementName, data);
                //logger.error(ds.getCriteriaCollection().toString());

                return ds;
            }
        }
    }

    public DisplaySearch getSearchCriteria(SchemaElementI se, String elementName, RunData data) throws Exception
    {
        SchemaElement e = new SchemaElement(se.getGenericXFTElement());

        String level = data.getParameters().getString("displayversion", "listing");
        if (e.getDisplay().getVersion(level)==null)
        {
            level = "listing";
        }
        DisplaySearch ds = TurbineUtils.getUser(data).getSearch(elementName,level);

        ds.setPagingOn(true);

        ds = setSearchCriteria(data,ds);

        return ds;
    }
}

