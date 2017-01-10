/*
 * core: org.nrg.xdat.turbine.modules.actions.CSVUpload2
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.actions; 

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FieldMapping;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.ValidationUtils.XFTValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVUpload2 extends SecureAction {

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        preserveVariables(data,context);
    }

    public void doUpload(RunData data, Context context) throws Exception {
        preserveVariables(data,context);
        ParameterParser params = data.getParameters();

        //grab the FileItems available in ParameterParser
        FileItem fi = params.getFileItem("csv_to_store");


        String fm_id=TurbineUtils.escapeParam(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("fm_id",data)));
        File f = Users.getUserCacheFile(TurbineUtils.getUser(data),"csv/" + fm_id + ".xml");
        FieldMapping fm = new FieldMapping(f);
        context.put("fm",fm);
        context.put("fm_id", fm_id);

        if (fi != null)
        {
            File temp = File.createTempFile("xnat", "csv");
            fi.write(temp);

            List<List<String>> rows = FileUtils.CSVFileToArrayList(temp);

            temp.delete();
            fi.delete();


            if (rows.size()>0 && rows.get(0).get(0).equals("ID")){
                rows.remove(0);
            }

            data.getSession().setAttribute("rows", rows);
            data.setScreenTemplate("XDATScreen_uploadCSV2.vm");
        }else{
            data.setScreenTemplate("XDATScreen_uploadCSV2.vm");
        }
    }

    public void doStore(RunData data,Context context) throws Exception{
        preserveVariables(data,context);
        ArrayList rows = new ArrayList();
        rows = (ArrayList)data.getSession().getAttribute("rows");

        String fm_id=((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("fm_id",data));
        File f = Users.getUserCacheFile(TurbineUtils.getUser(data),"csv/" + fm_id + ".xml");
        FieldMapping fm = new FieldMapping(f);
        context.put("fm",fm);
        context.put("fm_id", fm_id);

        String project = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data));
        
        
        ArrayList displaySummary = new ArrayList();
        List fields = fm.getFields();
        try {
            String rootElementName = fm.getElementName();
            GenericWrapperElement.GetElement(rootElementName);

            UserI user = TurbineUtils.getUser(data);
            Iterator iter = rows.iterator();
            while(iter.hasNext())
            {
                ArrayList row = (ArrayList)iter.next();
                ArrayList rowSummary = new ArrayList();
                XFTItem item = XFTItem.NewItem(rootElementName, user);
                Iterator iter2 = row.iterator();
                int columnIndex = 0;
                while (iter2.hasNext())
                {
                    String column = (String)iter2.next();
                    String xmlPath = (String)fields.get(columnIndex);
                    
                    if (!column.equals("")){
                        rowSummary.add(column);

                        GenericWrapperField gwf = null;
                        try {
                            gwf = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
                        } catch (FieldNotFoundException e) {
                        }


                        if (gwf!=null && gwf.getBaseElement()!=null && !gwf.getBaseElement().equals("")){
                            try {
                                ItemSearch search = ItemSearch.GetItemSearch(gwf.getBaseElement(), user);
                                SchemaElement se =SchemaElement.GetElement(gwf.getBaseElement());
                                if ((project!=null && !project.equals("")) && se.hasField(se.getFullXMLName() + "/sharing/share/project")){
                                    CriteriaCollection cc = new CriteriaCollection("OR");
                                    cc.addClause(se.getFullXMLName() + "/" + gwf.getBaseCol(), column);

                                    CriteriaCollection sub = new CriteriaCollection("AND");
                                    sub.addClause(se.getFullXMLName() + "/sharing/share/project", project);
                                    sub.addClause(se.getFullXMLName() + "/sharing/share/label", column);
                                    cc.add(sub);
                                    
                                    sub = new CriteriaCollection("AND");
                                    sub.addClause(se.getFullXMLName() + "/project", project);
                                    sub.addClause(se.getFullXMLName() + "/label", column);
                                    cc.add(sub);                                    

                                    search.add(cc);
                                }else{
                                    search.addCriteria(se.getFullXMLName() + "/" + gwf.getBaseCol(), column);
                                }

                                ItemCollection items =search.exec(false);

                                if (items.size()==1){
                                    item.setProperty(xmlPath,items.getFirst().getProperty("ID"));
                                    columnIndex++;
                                    continue;
                                }
                            } catch (Exception e) {
                            }

                        }

                        try {
                            item.setProperty(xmlPath, column);
                        } catch (FieldNotFoundException e) {
                            logger.error("",e);
                        } catch (InvalidValueException e) {
                            logger.error("",e);
                        }
                    }
                    columnIndex++;
                }

                if (project!=null && !project.equals("")){
                    SchemaElement se = SchemaElement.GetElement(rootElementName);

                    if (se.hasField(rootElementName +"/sharing/share/project") && se.hasField(rootElementName +"/sharing/share/label")){
                        try {
                            String id = item.getStringProperty("ID");
                            if(item.getStringProperty("project")==null){
                                item.setProperty(rootElementName + "/project", project);
                                if(item.getStringProperty("label")==null){
                                    item.setProperty(rootElementName + "/label", id);   
                                }                         	
                            }else{
                            	if(item.getStringProperty("project").equals(project)){
                                    if(item.getStringProperty("label")==null){
                                        item.setProperty(rootElementName + "/label", id);   
                                    }                         	
                            	}else{
                            		item.setProperty(rootElementName + "/sharing/share/project", project); 
                            		item.setProperty(rootElementName + "/sharing/share/label", id); 
                            	}
                            }

                            ItemSearch search = ItemSearch.GetItemSearch(rootElementName, user);
                            CriteriaCollection cc = new CriteriaCollection("OR");
                            cc.addClause(se.getFullXMLName() + "/ID", id);


                            CriteriaCollection sub = new CriteriaCollection("AND");
                            sub.addClause(se.getFullXMLName() + "/sharing/share/project", project);
                            sub.addClause(se.getFullXMLName() + "/sharing/share/label", id);
                            cc.add(sub);
                            
                            sub = new CriteriaCollection("AND");
                            sub.addClause(se.getFullXMLName() + "/project", project);
                            sub.addClause(se.getFullXMLName() + "/label", id);
                            cc.add(sub);

                            search.add(cc);
                            ItemCollection items =search.exec(false);

                            if (items.size()>0){
                                item.setProperty("ID",items.getFirst().getProperty("ID"));
                            }else{
                            	if(item.getStringProperty("label")!=null && item.getStringProperty("label").equals(item.getStringProperty("ID"))){
                            		ItemI om = BaseElement.GetGeneratedItem(item);
                            		Class c = om.getClass();
                            		Object[] intArgs = new Object[] {};
                        			Class[] intArgsClass = new Class[] {};
                        			
                        			String newID=null;
                            		try {
                                        Method m = c.getMethod("CreateNewID",intArgsClass);
                                        if(m!=null){
                                            try {
                                                try {
                                                	newID =(String)m.invoke(null,intArgs);
                                                } catch (RuntimeException e3) {
                                                    logger.error("",e3);
                                                }
                                            } catch (IllegalArgumentException e2) {
                                                logger.error("",e2);
                                            } catch (InvocationTargetException e2) {
                                                logger.error("",e2);
                                            }
                                        }
                                    } catch (SecurityException e1) {
                                        logger.error("",e1);
                                    } catch (NoSuchMethodException e1) {
                                        logger.error("",e1);
                                    }
                                    
                                    if(newID!=null){
                                    	item.setProperty("ID", newID);
                                    }else{
                                    	item.setProperty("ID",XFT.CreateIDFromBase(XDAT.getSiteConfigPreferences().getSiteId(), 5, "ID", se.getSQLName(), null, null));
                                    }
                            	}
                            }
                        } catch (FieldNotFoundException e) {
                            logger.error("",e);
                        } catch (InvalidValueException e) {
                            logger.error("",e);
                        } catch (Exception e) {
                            logger.error("",e);
                        }
                    }
                }

                PersistentWorkflowI wrk=PersistentWorkflowUtils.buildOpenWorkflow(user, item, CSVUpload2.newEventInstance(data, EventUtils.CATEGORY.DATA, "Upload Spreadsheet"));
                
                try {
                	SaveItemHelper.unauthorizedSave(item,user, false, false,wrk.buildEvent());
                	PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
                    rowSummary.add("<font color='black'><b>Successful</b></font>");
                } catch (Throwable e1) {
                    logger.error("",e1);
                	PersistentWorkflowUtils.fail(wrk, wrk.buildEvent());
                    rowSummary.add("<font color='red'><b>Error</b>&nbsp;"+ e1.getMessage() + "</font>");
                }

                displaySummary.add(rowSummary);
            }

            context.put("summary", displaySummary);

            data.getSession().removeAttribute("rows");

            data.setScreenTemplate("XDATScreen_uploadCSV4.vm");

			Users.clearCache(user);
			try {
				MaterializedView.deleteByUser(user);
			} catch (DBPoolException e) {
	            logger.error("",e);
			} catch (SQLException e) {
	            logger.error("",e);
			} catch (Exception e) {
	            logger.error("",e);
			}
        } catch (XFTInitException e) {
            logger.error("",e);
            data.setScreenTemplate("XDATScreen_uploadCSV3.vm");
        } catch (ElementNotFoundException e) {
            logger.error("",e);
            data.setScreenTemplate("XDATScreen_uploadCSV3.vm");
        }
    }


    public void doProcess(RunData data, Context context)  {
        preserveVariables(data,context);
        ArrayList rows = new ArrayList();
        int i=0;
        while (data.getParameters().containsKey("row" + i))
        {
            String row = TurbineUtils.escapeParam(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("row" + i,data)));
            ArrayList rowAL = XftStringUtils.CommaDelimitedStringToArrayList(row);
            rows.add(rowAL);
            i++;
        }
        data.getSession().setAttribute("rows", rows);

        String project = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data));

        String fm_id=((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("fm_id",data));
        File f = Users.getUserCacheFile(TurbineUtils.getUser(data),"csv/" + fm_id + ".xml");
        FieldMapping fm = new FieldMapping(f);
        context.put("fm",fm);
        context.put("fm_id", fm_id);

        ArrayList displaySummary = new ArrayList();
        List fields = fm.getFields();
        try {
            String rootElementName = fm.getElementName();

            UserI user = TurbineUtils.getUser(data);
            Iterator iter = rows.iterator();
            while(iter.hasNext())
            {
                ArrayList row = (ArrayList)iter.next();
                XFTItem item = XFTItem.NewItem(rootElementName, user);
                Iterator iter2 = row.iterator();
                int columnIndex = 0;
                while (iter2.hasNext())
                {
                    String column = (String)iter2.next();
                    String xmlPath = (String)fields.get(columnIndex);
                    if (!column.equals("")){
                        try {
                            item.setProperty(xmlPath, column);
                        } catch (FieldNotFoundException e) {
                            logger.error("",e);
                        } catch (InvalidValueException e) {
                            logger.error("",e);
                        }
                    }
                    columnIndex++;
                }
                XFTItem dbVersion =null;
                boolean matchedPK=false;
                if (project!=null && !project.equals("")){
                    SchemaElement se = SchemaElement.GetElement(rootElementName);

                    if (se.hasField(rootElementName +"/sharing/share/project") && se.hasField(rootElementName +"/sharing/share/label")){
                        try {
                            String id = item.getStringProperty("ID");

                            ItemSearch search = ItemSearch.GetItemSearch(rootElementName, user);
                            CriteriaCollection cc = new CriteriaCollection("OR");
                            cc.addClause(se.getFullXMLName() + "/ID", id);

                            CriteriaCollection sub = new CriteriaCollection("AND");
                            sub.addClause(se.getFullXMLName() + "/sharing/share/project", project);
                            sub.addClause(se.getFullXMLName() + "/sharing/share/label", id);
                            cc.add(sub);
                            
                            sub = new CriteriaCollection("AND");
                            sub.addClause(se.getFullXMLName() + "/project", project);
                            sub.addClause(se.getFullXMLName() + "/label", id);
                            cc.add(sub);
                            
                            search.add(cc);
                            ItemCollection items =search.exec(false);

                            if (items.size()>0){
                                dbVersion= (XFTItem)items.getFirst();
                                matchedPK=true;
                            }
                        } catch (FieldNotFoundException e) {
                            logger.error("",e);
                        } catch (InvalidValueException e) {
                            logger.error("",e);
                        } catch (Exception e) {
                            logger.error("",e);
                        }
                    }else{
                        dbVersion = item.getCurrentDBVersion(false);
                    }
                }else{
                    dbVersion = item.getCurrentDBVersion(false);
                }

                ArrayList rowSummary= new ArrayList();

                if (dbVersion==null)
                {
                    Iterator fieldIter = fields.iterator();
                    while(fieldIter.hasNext()){

                        String xmlPath = (String)fieldIter.next();
                        GenericWrapperField gwf =null;
                        StringBuffer sb = new StringBuffer();
                        try {
                            Object nValue = item.getProperty(xmlPath);
                            try {
                                gwf = GenericWrapperElement.GetFieldForXMLPath(xmlPath);

                            } catch (FieldNotFoundException e) {
                            }

                            if (gwf!=null && gwf.getBaseElement()!=null && !gwf.getBaseElement().equals("")){
                                try {
                                    ItemSearch search = ItemSearch.GetItemSearch(gwf.getBaseElement(), user);
                                    SchemaElement se =SchemaElement.GetElement(gwf.getBaseElement());
                                    if ((project!=null && !project.equals("")) && se.hasField(se.getFullXMLName() + "/sharing/share/project")){
                                        CriteriaCollection cc = new CriteriaCollection("OR");
                                        cc.addClause(se.getFullXMLName() + "/" + gwf.getBaseCol(), nValue);
                                        cc.addClause(se.getFullXMLName() + "/label", nValue);

                                        CriteriaCollection sub = new CriteriaCollection("AND");
                                        sub.addClause(se.getFullXMLName() + "/sharing/share/project", project);
                                        sub.addClause(se.getFullXMLName() + "/sharing/share/label", nValue);

                                        cc.add(sub);

                                        search.add(cc);
                                    }else{
                                        search.addCriteria(se.getFullXMLName() + "/" + gwf.getBaseCol(), nValue);
                                    }

                                    ItemCollection items =search.exec(false);

                                    if (items.size()>0){
                                        sb.append("<FONT COLOR='black'><b>").append(nValue).append("</b></FONT>");
                                        rowSummary.add(sb.toString());
                                    }else{
                                        sb.append("<A TITLE=\"Value does not match an existing " +gwf.getBaseElement() + "/" + gwf.getBaseCol() +".\"><FONT COLOR='red'><b>"+ nValue + "</b></FONT></A>");
                                        rowSummary.add(sb.toString());
                                    }
                                } catch (Exception e) {
                                    logger.error("",e);
                                    sb.append("<A TITLE='Unknown exception.'><FONT COLOR='red'><b>"+ nValue + "<</b></FONT></A>");
                                    rowSummary.add(sb.toString());
                                }

                            }else{
                                if (gwf!=null){
                                    ValidationResults vr = XFTValidator.ValidateValue(nValue,gwf.getRules(),"xs",gwf,new ValidationResults(), xmlPath,gwf.getParentElement().getGenericXFTElement());
                                    if (!vr.isValid()){
                                        sb.append("<A TITLE='" + vr.getResults().get(0)[1] +"'><FONT COLOR='red'><b>"+ nValue + "<</b></FONT></A>");
                                        rowSummary.add(sb.toString());
                                        continue;
                                    }
                                }

                                sb.append("<FONT COLOR='black'><b>").append(nValue).append("</b></FONT>");
                                rowSummary.add(sb.toString());
                            }
                        } catch (FieldNotFoundException e) {
                            logger.error("",e);
                            sb.append("<A TITLE='Unknown field: " + xmlPath +"'><FONT COLOR='red'><b>ERROR</b></FONT></A>");
                            rowSummary.add(sb.toString());
                        }
                    }
                    rowSummary.add("NEW");
                }else{
                    boolean modified = false;
                    Iterator fieldIter = fields.iterator();
                    while(fieldIter.hasNext()){

                        String xmlPath = (String)fieldIter.next();
                        GenericWrapperField gwf =null;
                        StringBuffer sb = new StringBuffer();
                        Object oValue =null;
                        Object nValue=null;
                        try {
                            gwf = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
                        } catch (FieldNotFoundException e) {
                        }

                        try {
                            oValue = dbVersion.getProperty(xmlPath);
                            nValue = item.getProperty(xmlPath);
                        } catch (FieldNotFoundException e) {
                            logger.error("",e);
                            sb.append("<A  TITLE='Unknown field: " + xmlPath +"'><FONT COLOR='red'><b>"+ nValue + "<</b></FONT></A>");
                            rowSummary.add(sb.toString());
                            continue;
                        }


                        if (gwf!=null){
                            ValidationResults vr = XFTValidator.ValidateValue(nValue,gwf.getRules(),"xs",gwf,new ValidationResults(), xmlPath,gwf.getParentElement().getGenericXFTElement());
                            if (!vr.isValid()){
                                sb.append("<A TITLE='" + vr.getResults().get(0)[1] +"'><FONT COLOR='red'><b>"+ nValue + "</b></FONT></A>");
                                rowSummary.add(sb.toString());
                                continue;
                            }
                        }

                        if (oValue==null || oValue.equals(""))
                        {
                            if (nValue!=null){
                                sb.append("<FONT COLOR='black'><b>").append(nValue).append("</b></FONT><FONT COLOR='#999999'>(NULL)</font>");
                                modified=true;
                                rowSummary.add(sb.toString());
                                continue;
                            }else{
                                 rowSummary.add("");
                                 continue;
                            }
                        }else if (nValue == null || nValue.equals("")){
                            if (oValue !=null && !oValue.equals(""))
                            {
                                sb.append("<FONT COLOR='black'><b>NULL</b></FONT><FONT COLOR='#999999'>(" + oValue + ")</font>");
                                modified=true;
                                rowSummary.add(sb.toString());
                                continue;
                            }
                        }
                        try {
							String newValue = DBAction.ValueParser(nValue,gwf,false);
							String oldValue = DBAction.ValueParser(oValue,gwf,false);
							String type = null;
							if (gwf !=null)
							{
							    type = gwf.getXMLType().getLocalType();
							}


							if (!matchedPK || !xmlPath.equals(rootElementName +"/ID")){
							    if (DBAction.IsNewValue(type, oldValue, newValue)){
							        sb.append("<FONT COLOR='black'><b>").append(nValue + "</b></font> <FONT COLOR='#999999'>(" + oValue + ")</font>");
							        modified=true;
							    }else{
							        sb.append("<FONT COLOR='#999999'>").append(nValue).append("</FONT>");
							    }
							}else{
							    sb.append("<FONT COLOR='#999999'><b>").append(nValue + "</b></font> <FONT COLOR='#999999'>(" + oValue + ")</font>");
							}
							rowSummary.add(sb.toString());
						} catch (InvalidValueException e) {
							logger.error("",e);
						}
                    }
                    if (modified)
                        rowSummary.add("MODIFIED");
                    else
                        rowSummary.add("NO CHANGE");
                }

                displaySummary.add(rowSummary);
            }

            context.put("summary", displaySummary);

            data.setScreenTemplate("XDATScreen_uploadCSV3.vm");
        } catch (XFTInitException e) {
            logger.error("",e);
            data.setScreenTemplate("XDATScreen_uploadCSV2.vm");
        } catch (ElementNotFoundException e) {
            logger.error("",e);
            data.setScreenTemplate("XDATScreen_uploadCSV2.vm");
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(CSVUpload2.class);
}
