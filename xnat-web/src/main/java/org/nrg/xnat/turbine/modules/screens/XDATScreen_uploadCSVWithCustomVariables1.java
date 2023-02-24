/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 * 
 * @author: Mohana Ramaratnam (mohana@radiologics.com)
 * @since: 07-03-2021

 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.modules.screens.XDATScreen_uploadCSV1;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.utils.FieldMapping;
import org.nrg.xnat.customforms.helpers.CustomFormHelper;
import org.nrg.xnat.customforms.pojo.FormIOJsonToXnatCustomField;
import org.nrg.xnat.customforms.service.impl.CustomVariableFormAppliesToServiceImpl;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.entities.CustomVariableForm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

public class XDATScreen_uploadCSVWithCustomVariables1 extends XDATScreen_uploadCSV1 {

	protected void doBuildTemplate(RunData data, Context context) throws Exception {
		super.doBuildTemplate(data, context);
		Object allFieldsAddedBySuperClass = context.get("allElements");		//Add all the custom variables for this datatype
		if (!(allFieldsAddedBySuperClass instanceof Hashtable)) {
			return;
		}
		CustomVariableFormAppliesToServiceImpl customVariableFormAppliesToService = XDAT.getContextService().getBeanSafely(CustomVariableFormAppliesToServiceImpl.class);
		if (customVariableFormAppliesToService == null) {
			return;
		}
		FieldMapping fm = (FieldMapping)context.get("fm");
	    String fm_id = (String)TurbineUtils.GetPassedParameter("fm_id", data);
        if (fm==null && fm_id!=null){
            File f = Users.getUserCacheFile(TurbineUtils.getUser(data),"csv/" + fm_id + ".xml");
            fm  = new FieldMapping(f);
        }
	    String dataType = fm.getElementName();

		List<CustomVariableForm> allEnabledFormsForDataType = customVariableFormAppliesToService.findAllDistinctFormsByDatatype(dataType, CustomFormsConstants.ENABLED_STATUS_STRING);

	    //For this datatype root, get all custom variables across site and projects
	    //organize by configuration path
	    Hashtable<String, ArrayList<Object>> formsById = new Hashtable<String,ArrayList<Object>>();
	    for (CustomVariableForm form : allEnabledFormsForDataType) {
			String key =  String.format("%s (Form ID: %s)", form.title(), form.getFormUuid());

			List<FormIOJsonToXnatCustomField> customFields = CustomFormHelper.GetFormObj(form);
			List<FormIOJsonToXnatCustomField> appendedFormUUIDToKeys = appendFormUUIDToKey(customFields);
			ArrayList<Object> formFields = formsById.get(key);
			if (formFields == null || formFields.isEmpty()) {
				formFields = new ArrayList<Object>();
			}
			formFields.addAll(appendedFormUUIDToKeys);
			formsById.put(key, formFields);
		}
 	    Enumeration<String> keys = formsById.keys();
	    while(keys.hasMoreElements()) {
	    	String key = keys.nextElement();
	    	((Hashtable<String,ArrayList<Object>>)allFieldsAddedBySuperClass).put(key, formsById.get(key));
	    }
	}

	private List<FormIOJsonToXnatCustomField> appendFormUUIDToKey(final List<FormIOJsonToXnatCustomField> fields) {
		return fields == null ? Collections.emptyList() :
				fields.stream()
						.map(f -> new FormIOJsonToXnatCustomField(f.getFormUUID(), f.getLabel(), f.getFormUUID() + CustomFormsConstants.DOT_SEPARATOR + f.getKey(), f.getFieldName(), f.getType()))
						.collect(Collectors.toList());
	}
}
