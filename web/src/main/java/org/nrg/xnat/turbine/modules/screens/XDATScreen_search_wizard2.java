/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_search_wizard2
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;
import java.util.Map;

public class XDATScreen_search_wizard2 extends SecureScreen {

	@Override
	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		UserI user = TurbineUtils.getUser(data);
		Map<String,Object> additional=new Hashtable<String,Object>();
		Map<String,Object> additionalDescriptions=new Hashtable<String,Object>();
		Map<String,Object> additionalSchemaElements=new Hashtable<String,Object>();
		Object elementZero = TurbineUtils.GetPassedParameter("ELEMENT_0", data);
		context.put("ELEMENT_0", TurbineUtils.GetPassedParameter("ELEMENT_0", data));
		for(ElementDisplay ed : UserHelper.getUserHelperService(TurbineUtils.getUser(data)).getSearchableElementDisplaysByPluralDesc()){
			if(TurbineUtils.HasPassedParameter("super_"+ed.getElementName(), data)) {
				Object superElement = TurbineUtils.GetPassedParameter("super_" + ed.getElementName(), data);
				additional.put(ed.getElementName(), superElement);
				additionalDescriptions.put(ed.getElementName(), UserHelper.getUserHelperService(user).getElementSecurity(ed.getElementName()).getPluralDescription());
				additionalSchemaElements.put(ed.getElementName(), UserHelper.getUserHelperService(user).getElementSecurity(ed.getElementName()).getSchemaElement());
			}
		}
		
		context.put("additional_types", additional);
		context.put("additional_descriptions", additionalDescriptions);
		context.put("additional_schema_elements", additionalSchemaElements);

		String pivotDescription = UserHelper.getUserHelperService(user).getElementSecurity(elementZero.toString()).getPluralDescription();
		context.put("pivot_description", pivotDescription);
		SchemaElement pivotSchemaElement = UserHelper.getUserHelperService(user).getElementSecurity(elementZero.toString()).getSchemaElement();
		context.put("pivot_schema_element", pivotSchemaElement);

	}
}
