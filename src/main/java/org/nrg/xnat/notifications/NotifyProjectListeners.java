/*
 * web: org.nrg.xnat.notifications.NotifyProjectListeners
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.notifications;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.nrg.action.ServerException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.utils.CatalogUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Slf4j
public class NotifyProjectListeners implements Callable<Boolean> {
	private static final String NOTIFICATIONS = "notifications";
	private final XnatExperimentdata _expt;
	private final String _template,_subject,_action;
	private final UserI _user;
	private final Map _params;
	private final List<String> _emails;
	private final ProjectListenersI listenersBuilder;

	public NotifyProjectListeners(XnatExperimentdata expt,String template, String subject, UserI user, Map params, String action, List<String> emails, ProjectListenersI listeners){
		this._template=template;
		this._user=user;
		this._expt=expt;
		this._subject=subject;
		if(params==null){
			this._params=Maps.newHashMap();
		}else{
			this._params=params;
		}
		this._action=action;
		this._emails=emails;
		this.listenersBuilder=(listeners!=null)?listeners:new ResourceBasedProjectListeners();
	}

	public NotifyProjectListeners(XnatExperimentdata expt,String template, String subject, UserI user, Map params, String action, List<String> emails){
		this(expt,template,subject,user,params,action,emails,null);
	}

	public static interface ProjectListenersI{
		public List<String> call(String action, XnatProjectdata project, XnatExperimentdata expt);
	}

	@Override
	public Boolean call() throws Exception {
		try {
			List<String> email=listenersBuilder.call(_action, _expt.getProjectData(), _expt);
			for(String e: _emails){
				if(!email.contains(e)){
					email.add(e);
				}
			}

			if(email.size()>0){
				Context context =new VelocityContext(_params);


				String from = XDAT.getSiteConfigPreferences().getAdminEmail();
				context.put("user", _user);
				context.put("expt", _expt);
				context.put("username", _user.getUsername());
				context.put("server", TurbineUtils.GetFullServerPath());
				context.put("siteLogoPath", XDAT.getSiteLogoPath());
				context.put("system", TurbineUtils.GetSystemName());
				context.put("admin_email", XDAT.getSiteConfigPreferences().getAdminEmail());
				context.put("contactEmail", XDAT.getNotificationsPreferences().getHelpContactInfo());
				context.put("params", _params);
				if(_params.get("justification")!=null){
					context.put("justification",_params.get("justification"));
				}else if(_params.get("event_reason")!=null){
					context.put("justification",_params.get("event_reason"));
				}
				String body = AdminUtils.populateVmTemplate(context, _template);

				XDAT.getMailService().sendHtmlMessage(from, email.toArray(new String[email.size()]), TurbineUtils.GetSystemName()+" update: " + _expt.getLabel() +" "+_subject, body);
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	public static class ResourceBasedProjectListeners implements ProjectListenersI{
		public List<String> call(final String action, final XnatProjectdata project, final XnatExperimentdata expt){
			final String fileName=action;

			List<String> names=Lists.newArrayList();
			try {
				names.add(expt.getItem().getGenericSchemaElement().getSQLName()+"_"+ fileName);
				names.add(expt.getItem().getGenericSchemaElement().getXSIType()+"_"+ fileName);
				names.add(fileName);
			} catch (ElementNotFoundException e) {	}


			XnatResourcecatalog res=null;
			for(XnatAbstractresourceI r: project.getResources_resource()){
				if(r instanceof XnatResourcecatalog && NOTIFICATIONS.equals(r.getLabel())){
					res=(XnatResourcecatalog)r;
				}
			}

			File matchedFile=null;
			if(res!=null){
				try {
					final CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate(project.getRootArchivePath(), res, project.getId()
					);
					final CatCatalogBean cat = catalogData.catBean;
					final File catalog_xml = catalogData.catFile;

					CatEntryI entry=null;
					for(String name:names){
						entry=CatalogUtils.getEntryByURI(cat, name);
						if(entry!=null)break;
					}

					if(entry!=null){
						matchedFile=CatalogUtils.getFile(entry, catalog_xml.getParent(), project.getId());
					}
				} catch (ServerException e) {
					log.error("Unable to read or create catalog for resource {}",
							res.getXnatAbstractresourceId(), e);
				}
			}

			if(matchedFile!=null && matchedFile.exists()){
				final String s=FileUtils.GetContents(matchedFile);
				return Arrays.asList(s.split(","));
			}

			return Lists.newArrayList();
		}
	}

}
