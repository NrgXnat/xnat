/*
 * web: org.nrg.xnat.ajax.SubjectFinder
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.ajax;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatProjectparticipantI;
import org.nrg.xdat.model.XnatSubjectdataAddidI;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.ItemI;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.search.ItemSearch.IdentifierResults;
import org.nrg.xft.security.UserI;

public class SubjectFinder {
    static org.apache.log4j.Logger logger = Logger.getLogger(SubjectFinder.class);

    public void execute(HttpServletRequest req, HttpServletResponse response) throws IOException{
        String s = req.getParameter("subject_search");
        //System.out.print("Monitor Progress " + uploadID + "... ");
        StringBuffer sb = new StringBuffer();
        if (s!=null)
        {
            List<List<IdentifierResults>> matches=null;
            try {
                final GenericWrapperElement element = GenericWrapperElement.GetElement("xnat:subjectData");
                final UserI user = XDAT.getUserDetails();
                sb.append("<matchingSubjects ID=\"").append(s).append("\">");
                CriteriaCollection cc = new CriteriaCollection("OR");
                cc.addClause("xnat:subjectData/ID", "=",  s);
                cc.addClause("xnat:subjectData/label", "=", s );
                cc.addClause("xnat:subjectData/sharing/share/label", "=", s );
            
            
                final ItemSearch is = new ItemSearch(user,"xnat:subjectData",cc);
                matches = is.getIdentifiers();
                if (matches.size()==0){
                    cc = new CriteriaCollection("OR");
                    cc.addClause("xnat:subjectData/ID", " LIKE ", "%" + s + "%");
                    cc.addClause("xnat:subjectData/label", " LIKE ", "%" + s + "%");
                    cc.addClause("xnat:subjectData/sharing/share/label", " LIKE ", "%" + s + "%");
                }
                final ItemCollection items = ItemSearch.GetItems(cc,user,false);
                for (final ItemI i :items.getItems())
                {
                    if (i.getXSIType().equals("xnat:subjectData"))
                    {
                        final XnatSubjectdata subject = new XnatSubjectdata(i);
                        
                        if (!Permissions.canRead(user, i)){
                            i.setProperty(subject.getXSIType() + "/ID","*****");
                            i.setProperty(subject.getXSIType() + "/label","*****");
                            
                            for (int j=0;j<subject.getSharing_share().size();j++){
                                i.setProperty(subject.getXSIType() + "/sharing/share[" + j +"]/label", "*****");
                            }
                        }

                        sb.append("<subject ID=\"").append(subject.getId()).append("\" label=\"").append(subject.getLabel()).append("\" project=\"").append(subject.getProject()).append("\">");

                        
                        if (!subject.getSharing_share().isEmpty()){
                            sb.append("<sharing>");
                            for (final XnatProjectparticipantI pp : subject.getSharing_share()){
                                sb.append("<share ");
                                if (pp.getLabel()==null){
                                    sb.append("label=\"").append(subject.getId());
                                }else{
                                    sb.append("label=\"").append(pp.getLabel());
                                }
                                sb.append(" project=\"").append(pp.getProject());
                                sb.append("/>");
                            }
                            sb.append("</sharing>");
                        }
                        
                        if (!subject.getAddid().isEmpty()){
                            for (final XnatSubjectdataAddidI pp : subject.getAddid()){
                                sb.append("<addID ");
                                sb.append("name=\"").append(pp.getName()).append("\">");
                                sb.append(pp.getAddid());
                                sb.append("</addID>");
                            }
                        }
                        
                        sb.append("<demographics>");
                        if (subject.getGenderText()!=null)
                            sb.append("<gender>").append(subject.getGenderText()).append("</gender>");
                        else
                            sb.append("<gender></gender>");
                        
                        if (subject.getHandedText()!=null)
                            sb.append("<handedness>").append(subject.getHandedText()).append("</handedness>");
                        else
                            sb.append("<handedness></handedness>");
                        sb.append("</demographics>");
                        
                        
                        sb.append("</subject>");
                    }
                }
            } catch (Exception e) {
            }
            sb.append("</matchingSubjects>");
            response.setContentType("text/xml");
            response.setHeader("Cache-Control", "no-cache");
            response.getWriter().write(sb.toString());
        }
    }
}
