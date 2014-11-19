/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_edit_xdat_element_security
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.helpers.FeatureDefinitionI;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;

import com.google.common.collect.Lists;

/**
 * @author Tim
 *
 */
public class XDATScreen_edit_xdat_element_security extends AdminEditScreenA {

	static Logger logger = Logger.getLogger(XDATScreen_edit_xdat_element_security.class);
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
	 */
	public String getElementName()
	{
		return "xdat:element_security";
	}

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context)
    {
        try {
            String elementName = item.getStringProperty("xdat:element_security.element_name");
            
            if (elementName != null && !elementName.equals(""))
            {
                SchemaElement gwe = SchemaElement.GetElement(elementName);
                
                context.put("fields",gwe.getAllDefinedFields());
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
        
        List<List> features=Lists.newArrayList();
        for(FeatureDefinitionI def: Features.getAllFeatures()){
        	features.add(Lists.newArrayList(def.getKey(),def.getName()));
        }
        context.put("features", features);
    }

}

