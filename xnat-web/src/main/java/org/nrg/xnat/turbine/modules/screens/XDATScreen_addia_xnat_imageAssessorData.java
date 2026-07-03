/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_edit_xnat_imageAssessorData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2025, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.security.UserI;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.utils.XftStringUtils;

public class XDATScreen_addia_xnat_imageAssessorData
		extends
		org.nrg.xdat.turbine.modules.screens.SecureScreen {
	static Logger logger = Logger.getLogger(XDATScreen_addia_xnat_imageAssessorData.class);


	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.XDATScreen_edit_xnat_imageAssessorData#getEmptyItem(org.apache.turbine.util.RunData)
	 */
	public ItemI getEmptyItem(RunData data) throws Exception {
		final UserI user = TurbineUtils.getUser(data);
		final String elementName = data.getParameters().getString("xsiType");
		final XnatImageassessordata assessor = new XnatImageassessordata(XFTItem.NewItem(elementName, user));
		final String searchElement = TurbineUtils.GetSearchElement(data);
		if (!StringUtils.isEmpty(searchElement)) {
			final GenericWrapperElement se = GenericWrapperElement.GetElement(searchElement);
			if (se.instanceOf(XnatImagesessiondata.SCHEMA_ELEMENT_NAME)) {
				final String searchValue = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data));
				if (!StringUtils.isEmpty(searchValue)) {
					XnatImagesessiondata imageSession = new XnatImagesessiondata(TurbineUtils.GetItemBySearch(data));

					populateDetails(assessor,imageSession,user,data);
				}
			}
		}

		return assessor.getItem();
	}

	/**
	 * @param assessor populate default image assessor
	 * @param imageSession session for assessment
	 * @param user for transaction
	 * @throws Exception passed out from XFT transactions
	 */
	private void populateDetails(final XnatImageassessordata assessor, final XnatImagesessiondata imageSession, final UserI user,final RunData data) throws Exception{
		assessor.setImageSessionData(imageSession);

		// set defaults for new assessors
		if(StringUtils.isEmpty(assessor.getImagesessionId())){
			assessor.setImagesessionId(imageSession.getId());
		}

		if(StringUtils.isEmpty(assessor.getId())){
			assessor.setId(XnatExperimentdata.CreateNewID());
		}

		if(StringUtils.isEmpty(assessor.getLabel())){
			assessor.setLabel(imageSession.getLabel() + "_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()));
		}

		if(StringUtils.isEmpty(assessor.getProject())){
			assessor.setProject(imageSession.getProject());
		}
	}

	public void finalProcessing(RunData data, Context context) {
		final XnatImageassessordata assessor=(XnatImageassessordata)context.get("om");

		if(assessor.getImageSessionData()!=null){
			try {
				populateDetails(assessor,assessor.getImageSessionData(),TurbineUtils.getUser(data),data);
			} catch (Exception e) {
				logger.error("",e);
			}
		}
	}

	public String getStringIdentifierForPassedItem(final RunData data) {
		if (TurbineUtils.HasPassedParameter("tag", data)) {
			return (String) TurbineUtils.GetPassedParameter("tag", data);
		} else {
			return "edit_item";
		}
	}

	public void doBuildTemplate(PipelineData pipelineData, Context context) {
        RunData data = pipelineData.getRunData();
		try {
			if (TurbineUtils.HasPassedParameter("destination", data)) {
				context.put("destination", TurbineUtils.GetPassedParameter("destination", data));
			}
			context.put("edit_screen", data.getScreen());

			if (TurbineUtils.HasPassedParameter("tag", data)) {
				context.put("tag", TurbineUtils.GetPassedParameter("tag", data));
			}

			if (TurbineUtils.GetPassedParameter("source", data) != null) {
				context.put("source", TurbineUtils.GetPassedParameter("source", data));
			}

			ItemI item = null;

			if (!getStringIdentifierForPassedItem(data).equals("edit_item")) {
				item = (ItemI) data.getSession().getAttribute(getStringIdentifierForPassedItem(data));
				if (item != null) {
					data.getSession().removeAttribute(getStringIdentifierForPassedItem(data));
				} else {
					item = (ItemI) TurbineUtils.GetEditItem(data);
				}

			} else {
				item = (ItemI) TurbineUtils.GetEditItem(data);
			}

			if (item != null) {
				if (getElementName()!=null && (!item.getXSIType().equals(getElementName())) && (!(item.getItem()).getGenericSchemaElement().isExtensionOf(GenericWrapperElement.GetElement(getElementName())))) {
					item = null;
				}
			}

			if (item == null) {
				log.debug("No edit item passed... looking for item passed by variables");
				try {
					ItemI temp = TurbineUtils.GetItemBySearch(data);
					if (temp != null) {
						if (temp.getXSIType().equalsIgnoreCase(getElementName())) {
							item = temp;
						}
					}
				} catch (Exception ignored) {
				}
			}
			context.put("edit_screen", XftStringUtils.getLocalClassName(this.getClass()) + ".vm");
			if (item == null) {
				try {
					item = getEmptyItem(data);

					log.info("No passed item found. Created new item of type "+ item.getXSIType());
					SchemaElementI se = SchemaElement.GetElement(item.getXSIType());

					context.put("item", item);
					context.put("element", se);
					context.put("search_element", se.getFullXMLName());

					context.put("om", BaseElement.GetGeneratedItem(item));
					finalProcessing(data, context);
				} catch (Exception e) {
					log.warn(ERROR_CREATING_ITEM, e);
					data.setMessage(ERROR_CREATING_ITEM);
					data.setScreen("Index");
					TurbineUtils.OutputPassedParameters(data, context, getClass().getName());
				}
			} else {
				try {
					SchemaElementI se = SchemaElement.GetElement(item.getXSIType());
					if (context.get("source") == null) {
						context.put("source", "XDATScreen_report_" + se.getFormattedName() + ".vm");
					}
					context.put("item", item);
					context.put("element", org.nrg.xdat.schema.SchemaElement.GetElement(item.getXSIType()));
					context.put("search_element", TurbineUtils.GetPassedParameter("search_element", data));
					context.put("search_field", TurbineUtils.GetPassedParameter("search_field", data));
					context.put("search_value", TurbineUtils.GetPassedParameter("search_value", data));

					context.put("om", BaseElement.GetGeneratedItem(item));
					finalProcessing(data, context);
				} catch (Exception e) {
					log.warn(NO_DATA_ITEM_FOUND, e);
					data.setMessage(NO_DATA_ITEM_FOUND);
					data.setScreen("Index");
					TurbineUtils.OutputPassedParameters(data, context, this.getClass().getName());
				}

			}
		} catch (Exception e) {
			log.warn(NO_DATA_ITEM_FOUND, e);
			data.setMessage(NO_DATA_ITEM_FOUND);
			data.setScreen("Index");
			TurbineUtils.OutputPassedParameters(data, context, getClass().getName());
		}

		preserveVariables(data, context);
	}

	public String getElementName() {
		//returning null here because returning xnat:imageAssessorData might hard code the creation of that types
		return null;
	}

	private static final String NO_DATA_ITEM_FOUND = "Invalid Search Parameters: No data item found.";
	private static final String ERROR_CREATING_ITEM = "Invalid Search Parameters: Error creating item.";
}
