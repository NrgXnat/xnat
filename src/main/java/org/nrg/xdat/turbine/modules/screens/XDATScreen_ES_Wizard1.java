/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_ES_Wizard1
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;

/**
 * @author Tim
 *
 */
public class XDATScreen_ES_Wizard1 extends AdminEditScreenA {

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
     */
    public String getElementName() {
        return "xdat:element_security";
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
        try {
            SchemaElement se = SchemaElement.GetElement(item.getStringProperty("element_name"));
            if (se.getDefaultPrimarySecurityField()==null)
            {
                context.put("hasDefaultField",new Boolean(false));
            }else{
                context.put("hasDefaultField",new Boolean(true));
            }
        } catch (XFTInitException e) {
            logger.error("",e);
            context.put("hasDefaultField",new Boolean(false));
        } catch (ElementNotFoundException e) {
            logger.error("",e);
            context.put("hasDefaultField",new Boolean(false));
        } catch (FieldNotFoundException e) {
            logger.error("",e);
            context.put("hasDefaultField",new Boolean(false));
        }
    }

}
