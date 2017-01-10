/*
 * web: org.nrg.xnat.event.listeners.PipelineEmailHandlerAbst
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.nrg.framework.services.SerializerService;
import org.nrg.pipeline.xmlbeans.AllResolvedStepsDocument;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.WrkXnatexecutionenvironmentParameterI;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.WrkXnatexecutionenvironment;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatSubjectassessordata;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xnat.notifications.NotifyProjectPipelineListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public abstract class PipelineEmailHandlerAbst extends WorkflowStatusEventHandlerAbst {
    
    /** The default template success. */
    public final String DEFAULT_TEMPLATE_SUCCESS = "/screens/PipelineEmail_success.vm";

    /** The default subject success. */
    public final String DEFAULT_SUBJECT_SUCCESS = "processed without errors";

    /** The default template failure. */
    public final String DEFAULT_TEMPLATE_FAILURE = "/screens/PipelineEmail_failure.vm";

    /** The default subject failure. */
    public final String DEFAULT_SUBJECT_FAILURE = "";

    protected PipelineEmailHandlerAbst() {
        _serializer = XDAT.getContextService().getBean(SerializerService.class);
    }

    /**
     * Send.
     *
     * @param e the e
     * @param wrk the wrk
     * @param expt the expt
     * @param params the params
     * @param emailTemplate the email template
     * @param emailSubject the email subject
     * @param list_name the list_name
     * @param otherEmails the other emails
     * @throws Exception the exception
     */
    public void send(WorkflowStatusEvent e, WrkWorkflowdata wrk, XnatExperimentdata expt,Map<String,Object> params,String emailTemplate, String emailSubject, String list_name, List<String> otherEmails) throws Exception{
        //temporary notification manager until we have the notification stuff flushed out.
        if (completed(e)) {
            (new NotifyProjectPipelineListeners(expt, wrk, emailTemplate, emailSubject, wrk.getUser(), params, list_name, otherEmails, "success")).send();
        } else {
            (new NotifyProjectPipelineListeners(expt,wrk,emailTemplate,emailSubject,wrk.getUser(),params,list_name,otherEmails, "failure")).send();
        }
    }

    /**
     * Standard pipeline email impl.
     *
     * @param e the e
     * @param wrk the wrk
     * @param pipelineName the pipeline name
     * @param template the template
     * @param subject the subject
     * @param notificationFileName the notification file name
     * @param params the params
     */
    public void standardPipelineEmailImpl(final WorkflowStatusEvent e, WrkWorkflowdata wrk, final String pipelineName, final String template, final String subject, final String notificationFileName, Map<String,Object> params){

        try {
            String _pipelineName = wrk.getPipelineName();
            if (_pipelineName==null) {
                _pipelineName = (String)PoolDBUtils.ReturnStatisticQuery("select pipeline_name from wrk_workflowdata where wrk_workflowdata_id="+wrk.getWrkWorkflowdataId(),"pipeline_name",null,null);
            }
            if(StringUtils.endsWith(_pipelineName, pipelineName) && (completed(e) || failed(e))) {
                if ( wrk.getPipelineName() == null) {
                    wrk = WrkWorkflowdata.getWrkWorkflowdatasByWrkWorkflowdataId(wrk.getWrkWorkflowdataId(),wrk.getUser(),false);
                }

                if (completed(e) && Float.parseFloat(wrk.getPercentagecomplete()) < 100.0f) {
                    _log.error("Workflow " + wrk.getWrkWorkflowdataId() + " is \"Complete\" but percentage is less than 100%. Not sending email.");
                    return;
                }
                SchemaElement objXsiType;
                try {
                    objXsiType = SchemaElement.GetElement(wrk.getDataType());
                } catch (Throwable e1) {
                    _log.error("", e1);//this shouldn't happen
                    return;
                }

                //for now we are only worried about experiments
                if (!objXsiType.getGenericXFTElement().instanceOf("xnat:experimentData") || wrk.getId() == null) {
                    throw new Exception("I don't know how to send a pipeline email that isn't about an experiment.");
                }
                final XnatExperimentdata expt = XnatExperimentdata.getXnatExperimentdatasById(wrk.getId(), wrk.getUser(), false);

                params.put("justification", wrk.getJustification());

                // Try to get the subject label if we can.
                String subjectLabel = "";
                try {
                    XnatSubjectassessordata subjAss = (XnatSubjectassessordata) expt;
                    if (null != subjAss) {
                        subjectLabel = subjAss.getSubjectData().getLabel();
                    }
                } catch (ClassCastException e1) {
                    // Do nothing. It's no big deal if the experiment isn't a subject assessor.
                }
                if (!subjectLabel.equals("")) {
                    params.put("subject",subjectLabel);
                }

                String _subject;
                if (expt == null) {
                    throw new Exception("Experiment " + wrk.getId() + " associated with workflow " + wrk.getWrkWorkflowdataId() + " is null");
                }

                if (wrk.getComments()!=null) {
                    String comments = StringEscapeUtils.unescapeXml(wrk.getComments());

                    if (StringUtils.isNotBlank(comments)) {
                        try {
                            params.putAll(getSerializer().deserializeJson(comments, SerializerService.TYPE_REF_MAP_STRING_STRING));
                        } catch (Exception e1) {
                            // Do nothing. This isn't necessarily a problem.
                            params.put("comments",comments);
                        }
                    }
                }

                if(failed(e)) {
                    // Get a list of parameters. Using that information, find the pipeline execution logs.
                    WrkXnatexecutionenvironment wrkEE=null;
                    HashMap<String,String> pipelineCmdLineParamsMap = Maps.newHashMap();
                    HashMap<String,String> pipelineParamsMap = Maps.newHashMap();
                    Map<String,File> attachments = Maps.newHashMap();
                    List<String> stdout = Lists.newArrayList();
                    List<String> stderr = Lists.newArrayList();
                    try {
                        wrkEE = (WrkXnatexecutionenvironment) wrk.getExecutionenvironment();
                    } catch (ClassCastException e1){
                        _log.error("Workflow Execution Environment is not an XNAT Execution Environment", e1);
                    }
                    final List<WrkXnatexecutionenvironmentParameterI> pipelineCmdLineParameters = null == wrkEE ? new ArrayList<WrkXnatexecutionenvironmentParameterI>() : wrkEE.getParameters_parameter();

                    // Gather input params from command line
                    for (WrkXnatexecutionenvironmentParameterI pipelineParameter : pipelineCmdLineParameters) {
                        pipelineCmdLineParamsMap.put(pipelineParameter.getName(), pipelineParameter.getParameter());
                    }

                    // Try to find pipeline logs in their typical location
                    if (pipelineCmdLineParamsMap.containsKey("builddir") && StringUtils.isNotBlank(pipelineCmdLineParamsMap.get("builddir")) &&
                            pipelineCmdLineParamsMap.containsKey("label") && StringUtils.isNotBlank(pipelineCmdLineParamsMap.get("label"))) {
                        String logPath = pipelineCmdLineParamsMap.get("builddir").replaceAll("\\s","") + "/" + pipelineCmdLineParamsMap.get("label").replaceAll("\\s","") + "/LOGS/";
                        File logDirFileObj = new File(logPath);
                        if (logDirFileObj.exists()) {
                            File[] logFileObjs = logDirFileObj.listFiles();
                            if (logFileObjs != null) {
                                for (File logFileObj : logFileObjs ) {
                                    if (logFileObj.getName().endsWith(".log") || logFileObj.getName().endsWith(".err")) {
                                        attachments.put(logFileObj.getName(), logFileObj);
                                        try {
                                            List<String> logFileContents = Files.readAllLines(logFileObj.toPath(), Charset.defaultCharset());
                                            if (logFileObj.getName().endsWith(".log")) {
                                                stdout = logFileContents;
                                            } else {
                                                stderr = logFileContents;
                                            }
                                        } catch (IOException e1) {
                                            _log.error("Could not read pipeline log file " + logFileObj.toPath(), e1);
                                        }
                                    } else if (logFileObj.getName().endsWith(".xml")) {
                                        AllResolvedStepsDocument pipeParamsDoc = null;
                                        try {
                                            pipeParamsDoc = AllResolvedStepsDocument.Factory.parse(logFileObj);
                                        } catch (XmlException | IOException e1) {
                                            _log.error("Encountered a problem parsing pipeline parameter XML for failure email.", e1);
                                        }

                                        if (null!=pipeParamsDoc) {
                                            for (ParameterData pipeParam : pipeParamsDoc.getAllResolvedSteps().getParameters().getParameterArray()){
                                                String val = "";
                                                ParameterData.Values pipeParamVals = pipeParam.getValues();
                                                if (pipeParamVals.isSetUnique() && StringUtils.isNotBlank(pipeParamVals.getUnique())) {
                                                    val = pipeParamVals.getUnique();
                                                } else if (pipeParamVals.getListArray().length > 0) {
                                                    val = "[" + StringUtils.join(pipeParamVals.getListArray(), ',') + "]";
                                                }
                                                pipelineParamsMap.put(pipeParam.getName(), val);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Scrub workdir/builddir from paths in params. Not for security so much as to remove visual clutter.
                    String bd = pipelineParamsMap.containsKey("builddir") ? pipelineParamsMap.get("builddir") : "";
                    String wd = pipelineParamsMap.containsKey("workdir") ? pipelineParamsMap.get("workdir") : "";
                    if (StringUtils.isNotBlank(wd)) {
                        for (Map.Entry<String,String> param : Maps.newHashMap(pipelineParamsMap).entrySet()) {
                            if (!param.getKey().equals("workdir") && param.getValue().startsWith(wd)) {
                                pipelineParamsMap.put(param.getKey(), param.getValue().replaceFirst(wd, "workdir"));
                            } else if (param.getKey().equals("workdir") && StringUtils.isNotBlank(bd) && param.getValue().startsWith(bd)) {
                                pipelineParamsMap.put(param.getKey(), param.getValue().replaceFirst(bd, "builddir"));
                            }
                        }
                    } else if (StringUtils.isNotBlank(bd)) {
                        for (Map.Entry<String,String> param : Maps.newHashMap(pipelineParamsMap).entrySet()) {
                            if (!param.getKey().equals("builddir") && param.getValue().startsWith(bd)) {
                                pipelineParamsMap.put(param.getKey(), param.getValue().replaceFirst(bd, "builddir"));
                            }
                        }
                    }


                    params.put("pipelineParamsMap",pipelineParamsMap);
                    params.put("attachments", attachments);
                    if (stdout.size() > 0) {
                        params.put("stdout", tailLog(stdout));
                    }
                    if (stderr.size() > 0) {
                        params.put("stderr",tailLog(stderr));
                    }

                    _subject = TurbineUtils.GetSystemName()+" update: Processing failed for " + expt.getLabel() +" "+subject;



                } else {
                    _subject = TurbineUtils.GetSystemName()+" update: " + expt.getLabel() +" "+subject;
                }
                send(e, wrk, expt, params, template, _subject, notificationFileName, new ArrayList<String>());


            }
        } catch (Throwable e1) {
            _log.error("", e1);
        }
    }

    /**
     * Tail log.
     *
     * @param log the log
     * @return the list
     */
    List<String> tailLog(List<String> log) {
        List<String> retList = Lists.newArrayList();
        if (log.size()==0) {
            return log;
        }
        int numlines = Math.min(40, log.size());
        for (int line = log.size()-numlines; line < log.size(); line++) {
            retList.add(log.get(line));
        }
        return retList;
    }

    protected SerializerService getSerializer() {
        return _serializer;
    }

    private final static Logger _log = LoggerFactory.getLogger(PipelineEmailHandlerAbst.class);

    private final SerializerService _serializer;
}

