/*
 * web: org.nrg.xnat.turbine.modules.actions.ModifyImageAssessorData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import groovy.util.logging.Slf4j;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.turbine.modules.actions.ModifyItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Slf4j
public class ModifyImageAssessorData extends ModifyItem {

	private Logger logger = LoggerFactory.getLogger(ModifyImageAssessorData.class);

	//Any Action class inheriting from ModifyImageAssessorData
	//that overrides ModifyItem.preSave should now include call to
	//super.preSave() to be able to escape the &amp; in the XML
	@Override
	public void preSave(XFTItem item,RunData data, Context context) throws Exception{
		try {
			final XFTItem currentDbVersion = item.getCurrentDBVersion();
			PersistentWorkflowI wrk= PersistentWorkflowUtils.getOrCreateWorkflowData(null, XDAT.getUserDetails(), item,newEventInstance(data, EventUtils.CATEGORY.DATA, EventUtils.getAddModifyAction(item.getXSIType(), currentDbVersion==null)));
			XnatImageassessordata imageAssessorData = (XnatImageassessordata) BaseElement.GetGeneratedItem(item);
			dynamicPreSave(XDAT.getUserDetails(),imageAssessorData, TurbineUtils.GetDataParameterHash(data), wrk);
		} catch (CriticalException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.error("",e);
			throw e;
		}
	}

	@Override
	public void postProcessing(XFTItem item, RunData data, Context context)
			throws Exception {
		super.postProcessing(item, data, context);
//		
//		XnatImageassessordata assessor= (XnatImageassessordata) BaseElement.GetGeneratedItem(item);
//
//		final PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, TurbineUtils.getUser(data), assessor.getImageSessionData().getItem(),"Added image assessment");
//    	EventMetaI c=wrk.buildEvent();
//        PersistentWorkflowUtils.save(wrk,c);
	}

	public interface PreSaveAction {
		void execute(UserI user, XnatImageassessordata src, Map<String, String> params, PersistentWorkflowI wrk) throws Exception;
	}

	private void dynamicPreSave(UserI user, XnatImageassessordata src, Map<String,String> params,PersistentWorkflowI wrk) throws Exception{
		List<Class<?>> classes = Reflection.getClassesForPackage("org.nrg.xnat.actions.imageAssessorEdit.preSave");

		if(classes!=null && classes.size()>0){
			for(Class<?> clazz: classes){
				if(ModifyImageAssessorData.PreSaveAction.class.isAssignableFrom(clazz)){
					ModifyImageAssessorData.PreSaveAction action=(ModifyImageAssessorData.PreSaveAction)clazz.newInstance();
					action.execute(user,src,params,wrk);
				}
			}
		}
	}




}
