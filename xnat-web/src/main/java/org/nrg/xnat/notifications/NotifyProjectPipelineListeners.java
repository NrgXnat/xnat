package org.nrg.xnat.notifications;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;

import java.io.File;
import java.util.*;

/**
 * Created by flavin on 2/27/15.
 */
public class NotifyProjectPipelineListeners extends NotifyProjectListeners {
    static Logger logger = Logger.getLogger(NotifyProjectPipelineListeners.class);
    private final XnatExperimentdata _expt;
    private final String _template,_subject,_action;
    private final UserI _user;
    private final Map<String,Object> _params;
    private final List<String> _emails;
    private final Map<String,File> _attachments;
    private final String _type;
    private final ProjectListenersI listenersBuilder;
    private final WrkWorkflowdata _workflow;

    public NotifyProjectPipelineListeners(XnatExperimentdata expt,WrkWorkflowdata workflow,String template, String subject, UserI user, Map<String,Object> params, String action, List<String> emails, String type, ProjectListenersI listeners){
        super(expt,template,subject,user,params,action,emails,listeners);
        this._template=template;
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

    public NotifyProjectPipelineListeners(XnatExperimentdata expt,WrkWorkflowdata workflow,String template, String subject, UserI user, Map<String,Object> params, String action, List<String> emails, String type){
        this(expt,workflow,template,subject,user,params,action,emails,type,null);
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
            if(!email.contains(AdminUtils.getAdminEmailId())){
                email.add(AdminUtils.getAdminEmailId());
            }

            if(email.size()>0){
                Context context =new VelocityContext(Maps.newHashMap());


                String from = AdminUtils.getAdminEmailId();
                context.put("user", _user);
                context.put("expt", _expt);
                context.put("username", _user.getUsername());
                context.put("server", TurbineUtils.GetFullServerPath());
                context.put("system", TurbineUtils.GetSystemName());
                context.put("admin_email", AdminUtils.getAdminEmailId());
                context.put("workflow",_workflow);
                context.put("params", _params);
                for(Map.Entry<String,Object> entry : _params.entrySet()) {
                    context.put(entry.getKey(),entry.getValue());
                }
                if(_params.get("justification")==null && _params.get("event_reason")!=null){
                    context.put("justification",_params.get("event_reason"));
                }
                String body = AdminUtils.populateVmTemplate(context, _template);

                if (_type.equalsIgnoreCase("failure")) {
                    XDAT.getMailService().sendHtmlMessage(from, email.toArray(new String[email.size()]),null,null, _subject, body, body, _attachments);
                } else if (_type.equalsIgnoreCase("success")) {
                    XDAT.getMailService().sendHtmlMessage(from, email.toArray(new String[email.size()]), _subject, body);
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
