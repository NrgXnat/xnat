package org.nrg.xnat.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xft.event.Event;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.notifications.NotifyProjectListeners;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class WorkflowNotificationHandlerAbst extends WorkflowSaveHandlerAbst{
	final static Logger logger = Logger.getLogger(WorkflowNotificationHandlerAbst.class);
	
	public WorkflowNotificationHandlerAbst() {
		super();
	}

	
	static List<Integer> SENT_ALREADY=Lists.newArrayList();

	/**
	 * 
	 * @param e
	 * @param wrk
	 * @param expt
	 * @param user
	 * @param params
	 * @param template
	 * @param subject
	 * @throws Exception
	 */
	public void notify(Event e, WrkWorkflowdata wrk, XnatExperimentdata expt, UserI user,
			Map<String,Object> params, String template, String subject, String list_name, List<String> otherEmails)
			throws Exception {
				//temporary notification manager until we have the notification stuff flushed out.
				(new NotifyProjectListeners(expt,template,subject,wrk.getUser(),new HashMap<String,Object>(),list_name,otherEmails)).call();
			}

	/**
	 * Standardized implementation for sending a notification to a user that a particular workflow has completed.  
	 * @param e
	 * @param wrk
	 * @param pipelineName Only workflow entries with this pipeline (wrk.getOnlyPipelineName()) will be handled
	 * @param template This is the velocity template path used to generate the email text (/screens/email/your_file.vm)
	 * @param subject This will be appended to the email subject line.  The method will prepend it with "{SYSTEM} update: {LABEL}" + {subject}
	 * @param notificationFileName This is the key used to find a project specific notification list (the name of the resource file without .lst)
	 */
	public void standardNotificationImpl(final Event e, final WrkWorkflowdata wrk,
			final String pipelineName, final String template, final String subject, final String notificationFileName) {
				standardNotificationImpl(e, wrk, new Predicate<Event>(){
							@Override
							public boolean apply(Event input) {
								return (StringUtils.equals(wrk.getOnlyPipelineName(),pipelineName) && (completed(e)));
							}
						}, template, subject, notificationFileName);
			}

	public void standardNotificationImpl(final Event e, final WrkWorkflowdata wrk,
			Predicate<Event> doIf, final String template, final String subject, final String notificationFileName) {
					try {
						//this handler only cares about Transfer entries that have completed
						if(doIf.apply(e)){
							if(!SENT_ALREADY.contains(wrk.getWrkWorkflowdataId())){
								SchemaElement objXsiType;
								try {
									objXsiType = SchemaElement.GetElement(wrk.getDataType());
								} catch (Throwable e1) {
									logger.error("",e1);//this shouldn't happen 
									return;
								}
								
								//for now we are only worried about experiments
								if(objXsiType.getGenericXFTElement().instanceOf("xnat:experimentData") && wrk.getId()!=null){
									final XnatExperimentdata expt=XnatExperimentdata.getXnatExperimentdatasById(wrk.getId(), wrk.getUser(), false);
									
									Map<String,Object> params=Maps.newHashMap();
									params.put("justification", wrk.getJustification());
									
									if(expt!=null){								
										notify(e,wrk, expt,wrk.getUser(),new HashMap<String,Object>(),template,subject,notificationFileName,new ArrayList<String>());
									}
								}
								SENT_ALREADY.add(wrk.getWrkWorkflowdataId());
							}
						}
					} catch (Throwable e1) {
						logger.error("",e1);
						return;
					}
			}

}