/*
 * web: org.nrg.xnat.notifications.NotifyProjectPipelineListeners
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.notifications;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xft.security.UserI;

import java.io.File;
import java.util.*;

/**
 * Created by flavin on 2/27/15.
 */
public class NotifyProjectPipelineListeners extends ConfigurableNotifyProjectListeners {
    static Logger logger = Logger.getLogger(NotifyProjectPipelineListeners.class);
    private final XnatExperimentdata _expt;
    private final String _subject, _action;
    private final UserI _user;
    private final Map<String,Object> _params;
    private final List<String> _emails;
    private final Map<String,File> _attachments;
    private final String _type;
    private final ProjectListenersI listenersBuilder;
    private final WrkWorkflowdata _workflow;

    public NotifyProjectPipelineListeners(XnatExperimentdata expt,WrkWorkflowdata workflow, String subject, String body, UserI user, Map<String,Object> params, String action, List<String> emails, String type, ProjectListenersI listeners){
        super(expt,subject,body,user,params,action,emails,listeners);
        this._user=user;
        this._expt=expt;
        this._subject=subject;
        if(params==null){
            this._params= Maps.newHashMap();
        }else{
            this._params=params;
        }
        this._action=action;
        this._emails=emails;
        this._type = type;
        this._workflow = workflow;
        this.listenersBuilder=(listeners!=null)?listeners:new ResourceBasedProjectListeners();

        if (_params.get("attachments") != null && _params.get("attachments") instanceof Map) {
            _attachments = Maps.newHashMap();
            try {
                _attachments.putAll((Map<String, File>) _params.get("attachments"));
            } catch (ClassCastException e) {
                logger.error("",e);
            }
        } else {
            _attachments = null;
        }

    }

    public NotifyProjectPipelineListeners(XnatExperimentdata expt,WrkWorkflowdata workflow, String subject, String body, UserI user, Map<String,Object> params, String action, List<String> emails, String type){
        this(expt,workflow,subject,body,user,params,action,emails,type,null);
    }

    public Boolean send() throws Exception{
        try {
            ArrayList<String> email = Lists.newArrayList(listenersBuilder.call(_action, _expt.getProjectData(), _expt));
            if(!email.contains(_user.getEmail())){
                email.add(_user.getEmail());
            }
            for(String e: _emails){
                if(!email.contains(e)){
                    email.add(e);
                }
            }
            if(!email.contains(XDAT.getSiteConfigPreferences().getAdminEmail())){
                email.add(XDAT.getSiteConfigPreferences().getAdminEmail());
            }

            if(email.size()>0){
                formEmailMessage();

                String from = XDAT.getSiteConfigPreferences().getAdminEmail();

                if (_type.equalsIgnoreCase("failure")) {
                    XDAT.getMailService().sendHtmlMessage(from, email.toArray(new String[email.size()]),null,null, _subject, _body, _body, _attachments);
                } else if (_type.equalsIgnoreCase("success")) {
                    XDAT.getMailService().sendHtmlMessage(from, email.toArray(new String[email.size()]), _subject, _body);
                }
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
    }
}
