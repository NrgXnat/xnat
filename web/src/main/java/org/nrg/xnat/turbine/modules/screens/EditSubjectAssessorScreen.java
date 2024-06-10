/*
 * web: org.nrg.xnat.turbine.modules.screens.EditSubjectAssessorScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;


import lombok.extern.slf4j.Slf4j;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatSubjectassessordata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.turbine.modules.screens.EditScreenA;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xnat.turbine.utils.ScanQualityUtils;
import org.nrg.xnat.utils.PetTracerListUtils;

@Slf4j
public abstract class EditSubjectAssessorScreen extends EditScreenA {
    @Override
    public void finalProcessing(final RunData data, final Context context) {
        try {
            final String project;
            if (getEditItem() != null) {
                final XnatSubjectassessordata assessor;
                final ItemI part = TurbineUtils.GetParticipantItem(data);
                if (part !=null) {
                    assessor= new XnatSubjectassessordata(getEditItem());
                    context.put("part",new XnatSubjectdata(part));
                } else {
                    assessor = new XnatSubjectassessordata(getEditItem());
                    context.put("notes",assessor.getNote());
                    context.put("part",assessor.getSubjectData());
                }

                if(assessor.getProject()==null){
                    if(context.get("project")!=null){
                        assessor.setProject((String)context.get("project"));
                    }
                }
                project = assessor.getProject();

                dynamicContextExpansion(data, context, assessor);
            } else {
                project = (String)context.get("project");
            }
            context.put("qualityLabels", ScanQualityUtils.getQualityLabels(project, XDAT.getUserDetails()));
            context.put("petTracerList", PetTracerListUtils.getPetTracerList(project));
        } catch(Throwable t) {
            log.warn("error in preparing subject assessor edit screen", t);
        }
    }

    public interface ContextAction {
        void execute(RunData data, Context context, ItemI item);
    }

    private void dynamicContextExpansion(RunData data, Context context, ItemI item) throws Exception {
        for (final Class<?> clazz : Reflection.getClassesForPackage("org.nrg.xnat.screens.sessionEdit.context")) {
            if (ContextAction.class.isAssignableFrom(clazz)) {
                ((ContextAction) clazz.newInstance()).execute(data, context, item);
            }
        }
    }
}
