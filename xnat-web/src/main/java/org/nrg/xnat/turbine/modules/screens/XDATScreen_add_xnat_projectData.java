/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_add_xnat_projectData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.turbine.modules.screens.EditScreenA;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.turbine.utils.XNATUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

@Slf4j
@SuppressWarnings("unused")
public class XDATScreen_add_xnat_projectData extends EditScreenA {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getElementName() {
	    return XnatProjectdata.SCHEMA_ELEMENT_NAME;
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public void finalProcessing(final RunData data, final Context context) {
        Hashtable hash = XNATUtils.getInvestigatorsForCreate(getElementName(),data);
        context.put("investigators",hash);
        context.put("arc",ArcSpecManager.GetInstance());
        if (TurbineUtils.HasPassedParameter("destination", data)){
            context.put("destination", TurbineUtils.GetPassedParameter("destination", data));
        }
        try {
            ArrayList<ElementSecurity> root = new ArrayList<>();
            ArrayList<ElementSecurity> subjectAssessors = new ArrayList<>();
            ArrayList<ElementSecurity> mrAssessors = new ArrayList<>();
            ArrayList<ElementSecurity> petAssessors = new ArrayList<>();

            final String id = getEditItem().getStringProperty("ID");
            Collection<ElementSecurity> all =ElementSecurity.GetElementSecurities().values();
            for (ElementSecurity es: all){
                try {
                    if (es.getAccessible() || (StringUtils.isNotBlank(id) && es.matchesUsageEntry(id))) {
                        GenericWrapperElement g= es.getSchemaElement().getGenericXFTElement();
                        
                        if(g.instanceOf("xnat:mrAssessorData")){
                            mrAssessors.add(es);
                        }else if(g.instanceOf("xnat:petAssessorData")){
                            petAssessors.add(es);
                        }else if(g.instanceOf("xnat:subjectAssessorData")){
                            subjectAssessors.add(es);
                        }else if (g.instanceOf("xnat:subjectData") || g.instanceOf("xnat:experimentData")){
                            root.add(es);
                        }
                    }
                } catch (Throwable e) {
                    log.error("",e);
                }
            }
            
            context.put("root", root);
            context.put("subjectAssessors", subjectAssessors);
            context.put("mrAssessors", mrAssessors);
            context.put("petAssessors", petAssessors);
	        context.put("page_title", "New " + DisplayManager.GetInstance().getSingularDisplayNameForProject());

            if (StringUtils.isNotBlank(id)) {
                final ArcProject arcProject = ArcSpecManager.GetInstance().getProjectArc(id);
                if (arcProject != null) {
                    context.put("arcP", arcProject);
                }
            }
		} catch (Exception e) {
			log.error("",e);
		}
	}
}
