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
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ServerException;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.utils.CatalogUtils;

import javax.mail.MessagingException;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Slf4j
public class NotifyProjectListeners implements Callable<Boolean> {
	private static final String NOTIFICATIONS = "notifications";
	private final XnatExperimentdata _expt;
	private final String _subject,_action;
	protected String _body;
	private final UserI _user;
	private final Map _params;
	private final List<String> _emails;
	private final ProjectListenersI listenersBuilder;
	
	public NotifyProjectListeners(XnatExperimentdata expt, String subject, String body, UserI user, Map params, String action, List<String> emails, ProjectListenersI listeners){
		this._user=user;
		this._expt=expt;
		this._subject=subject;
		this._body = body;
		if(params==null){
			this._params=Maps.newHashMap();
		}else{
			this._params=params;
		}
		this._action=action;
		this._emails=emails;
		this.listenersBuilder=(listeners!=null)?listeners:new ResourceBasedProjectListeners();
	}
	
	public NotifyProjectListeners(XnatExperimentdata expt, String subject, String body, UserI user, Map params, String action, List<String> emails){
		this(expt,subject,body,user,params,action,emails,null);
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
				String from = XDAT.getSiteConfigPreferences().getAdminEmail();
				formEmailMessage();

				XDAT.getMailService().sendHtmlMessage(from, email.toArray(new String[email.size()]), TurbineUtils.GetSystemName()+" update: " + _expt.getLabel() +" "+_subject, _body);
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	public void formEmailMessage() {
		_body = XDAT.getNotificationsPreferences().replaceCommonAnchorTags(_body, _user);


		_body = _body.replaceAll("PROJECT_NAME", _expt.getProject());
		if (_params.containsKey("subject")) {
			_body = _body.replaceAll("SUBJECT_NAME", _params.get("subject").toString());
		}

		String success_url = TurbineUtils.GetFullServerPath() + "/data/experiments/" + _expt.getId() + "?format=html";

		String successUrlFinal = "<a href=\""+ success_url + "\">here</a>";

		_body = _body.replaceAll("SUCCESS_URL", successUrlFinal);
		if (_params.containsKey("pipelineName")) {
			_body = _body.replaceAll("PIPELINE_NAME", _params.get("pipelineName").toString());
		}

		_body = _body.replaceAll("EXPERIMENT_NAME", _expt.getLabel());
		String userEmail = "<a href=\"mailto:" + _user.getEmail()+ "\">" + _user.getUsername() + "</a>";

		String contactEmail = "<a href=\"mailto:" + XDAT.getNotificationsPreferences().getHelpContactInfo() + "\">" + XDAT.getNotificationsPreferences().getHelpContactInfo() + "</a>";
		_body = _body.replaceAll("CONTACT_EMAIL", contactEmail);

		if (_params.containsKey("stdout") && _params.containsKey("stderr")) {
			_body = _body.replaceAll("ATTACHMENTS_STATEMENT", "The stdout and stderr log files are attached.");
		} else if (_params.containsKey("stdout")) {
			_body.replaceAll("ATTACHMENTS_STATEMENT", "The stdout log file is attached.");
		} else if (_params.containsKey("stderr")) {
			_body.replaceAll("ATTACHMENTS_STATEMENT", "The stderr log file is attached.");
		} else {
			_body.replaceAll("ATTACHMENTS_STATEMENT", "");
		}

		if (_params.containsKey("pipelineParamsMap")) {
			String pipelineParamsString = "<p>\n <h3>PIPELINE PARAMETERS</h3>\n <table size=\"2\">\n";
			Map<String, String> paramsMap = GenericUtils.convertToTypedMap((Map<?, ?>) _params.get("pipelineParamsMap"), String.class, String.class);
			for (String key : paramsMap.keySet()) {
				if (StringUtils.isNotBlank(paramsMap.get(key))) {
					pipelineParamsString = pipelineParamsString + "<tr>\n <td>" + key + "</td><td>" + paramsMap.get(key) + "</td>\n </tr>";
				}
			}
			pipelineParamsString = pipelineParamsString + "</table>\n </p>";
			_body = _body.replaceAll("PIPELINE_PARAMETERS", pipelineParamsString);
		} else {
			_body = _body.replaceAll("PIPELINE_PARAMETERS", "");
		}

		if (_params.containsKey("stdout")) {
			String stdoutString = "TAIL stdout<br>\n";
			List<String> stdout= (List<String>) _params.get("stdout");
			for (String outLine: stdout) {
				stdoutString = stdoutString + outLine + "<br>\n";
			}
			_body = _body.replaceAll("STDOUT",stdoutString);
		}

		if (_params.containsKey("stderr")) {
			String stderrString = "TAIL stderr<br>\n";
			List<String> stderr= (List<String>) _params.get("stderr");
			for (String errLine: stderr) {
				stderrString = stderrString + errLine + "<br>\n";
			}
			_body = _body.replaceAll("STDERR",stderrString);
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
