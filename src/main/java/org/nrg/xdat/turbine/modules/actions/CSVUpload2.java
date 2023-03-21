/*
 * core: org.nrg.xdat.turbine.modules.actions.CSVUpload2
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
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
import org.nrg.xft.exception.*;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FieldMapping;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.ValidationUtils.XFTValidator;
import org.nrg.xft.utils.XftStringUtils;
import org.postgresql.util.PGobject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@SuppressWarnings("unused")
public class CSVUpload2 extends SecureAction {

    public CSVUpload2() {
        super();
        objectMapper = XDAT.getContextService().getBeanSafely(ObjectMapper.class);
    }

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        preserveVariables(data,context);
    }

    public void doUpload(RunData data, Context context) throws Exception {
        preserveVariables(data,context);
        ParameterParser params = data.getParameters();

        //grab the FileItems available in ParameterParser
        FileItem fi = params.getFileItem("csv_to_store");


        String fm_id=TurbineUtils.escapeParam(((String)TurbineUtils.GetPassedParameter("fm_id",data)));
        File f = Users.getUserCacheFile(XDAT.getUserDetails(),"csv/" + fm_id + ".xml");
        FieldMapping fm = new FieldMapping(f);
        context.put("fm",fm);
        context.put("fm_id", fm_id);

        if (fi != null) {
            File temp = File.createTempFile("xnat", "csv");
            fi.write(temp);

            List<List<String>> rows = FileUtils.csvFileToArrayListUsingApacheCommons(temp);
            if (rows.size() > 0 && rows.get(0).get(0).equals("ID")) {
                rows.remove(0);
            }
            //This is required as CSV is parsed further downstream and strings need to conform to CSV encoding
            rows = XftStringUtils.toCsvEncodedListUsingApacheCommons(rows);

            temp.delete();
            fi.delete();

            data.getSession().setAttribute("rows", rows);
        }
        data.setScreenTemplate("XDATScreen_uploadCSV2_Save.vm");
    }

    public void doStore(RunData data,Context context) throws Exception{
        preserveVariables(data,context);
        ArrayList rows = (ArrayList)data.getSession().getAttribute("rows");

        String fm_id=((String)TurbineUtils.GetPassedParameter("fm_id",data));
        File f = Users.getUserCacheFile(XDAT.getUserDetails(),"csv/" + fm_id + ".xml");
        FieldMapping fm = new FieldMapping(f);
        context.put("fm",fm);
        context.put("fm_id", fm_id);
        final String dataType = fm.getElementName();

        String project = ((String)TurbineUtils.GetPassedParameter("project",data));

        ArrayList displaySummary = new ArrayList();
        List fields = fm.getFields();
        int indexOfCustomField = getIndexOfCustomField(fields, dataType);
        try {

            String rootElementName = fm.getElementName();
            GenericWrapperElement.GetElement(rootElementName);

            UserI user = XDAT.getUserDetails();
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
                        rowSummary.add(escapeHTML(column));

                        GenericWrapperField gwf = null;
                        try {
                            gwf = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
                        } catch (FieldNotFoundException ignored) {
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
                            } catch (Exception ignored) {}
                        }
                        try {
                            if (isEntireCustomField(dataType, xmlPath)) {
                                item.setProperty(xmlPath, validateJson(StringEscapeUtils.unescapeJson(column)));
                            } else {
                                item.setProperty(xmlPath, TurbineUtils.escapeParam(TurbineUtils.unescapeParam(column)));
                            }
                        } catch (FieldNotFoundException | InvalidValueException e) {
                            log.error("", e);
                        }
                    }
                    columnIndex++;
                }

                if (project!=null && !project.equals("")) {
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
                                                    log.error("", e3);
                                                }
                                            } catch (IllegalArgumentException e2) {
                                                log.error("", e2);
                                            } catch (InvocationTargetException e2) {
                                                log.error("", e2);
                                            }
                                        }
                                    } catch (SecurityException e1) {
                                        log.error("", e1);
                                    } catch (NoSuchMethodException e1) {
                                        log.error("", e1);
                                    }

                                    if(newID!=null){
                                        item.setProperty("ID", newID);
                                    }else{
                                        item.setProperty("ID",XFT.CreateIDFromBase(XDAT.getSiteConfigPreferences().getSiteId(), 5, "ID", se.getSQLName(), null, null));
                                    }
                                }
                            }
                        } catch (FieldNotFoundException e) {
                            log.error("", e);
                        } catch (InvalidValueException e) {
                            log.error("", e);
                        } catch (Exception e) {
                            log.error("", e);
                        }
                    }
                }

                PersistentWorkflowI wrk=PersistentWorkflowUtils.buildOpenWorkflow(user, item, CSVUpload2.newEventInstance(data, EventUtils.CATEGORY.DATA, "Upload Spreadsheet"));

                try {
                    SaveItemHelper.unauthorizedSave(item,user, false, false,wrk.buildEvent());
                    PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
                    rowSummary.add("<font color='black'><b>Successful</b></font>");
                } catch (Throwable e1) {
                    log.error("", e1);
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
                log.error("", e);
            } catch (SQLException e) {
                log.error("", e);
            } catch (Exception e) {
                log.error("", e);
            }
        } catch (XFTInitException e) {
            log.error("", e);
            data.setScreenTemplate("XDATScreen_uploadCSV3.vm");
        } catch (ElementNotFoundException e) {
            log.error("", e);
            data.setScreenTemplate("XDATScreen_uploadCSV3.vm");
        }
    }


    public void doProcess(RunData data, Context context)  {
        preserveVariables(data,context);
        String project = ((String)TurbineUtils.GetPassedParameter("project",data));

        String fm_id=((String)TurbineUtils.GetPassedParameter("fm_id",data));
        File f = Users.getUserCacheFile(XDAT.getUserDetails(),"csv/" + fm_id + ".xml");
        FieldMapping fm = new FieldMapping(f);
        String dataType = fm.getElementName();
        context.put("fm",fm);
        context.put("fm_id", fm_id);
        List fields = fm.getFields();
        int indexOfCustomField = getIndexOfCustomField(fields, dataType);
        try {
            List rows = buildRows(data, fields, dataType, indexOfCustomField);

            data.getSession().setAttribute("rows", rows);

            ArrayList displaySummary = new ArrayList();
            Map<String,List<String>> problematicItems = new HashedMap();

            String rootElementName = fm.getElementName();

            UserI user = XDAT.getUserDetails();
            Iterator iter = rows.iterator();
            while(iter.hasNext()) {
                ArrayList row = (ArrayList)iter.next();
                XFTItem item = XFTItem.NewItem(rootElementName, user);
                Iterator iter2 = row.iterator();
                int columnIndex = 0;
                while (iter2.hasNext()) {
                    List<String> problematicXmlPaths = new ArrayList<>();
                    String column = (String)iter2.next();
                    String xmlPath = (String)fields.get(columnIndex);
                    if (!column.equals("")){
                        try {
                            if (columnIndex == indexOfCustomField) {
                                item.setProperty(xmlPath, validateJson(StringEscapeUtils.unescapeJson(column)));
                            } else {
                                item.setProperty(xmlPath, TurbineUtils.unescapeParam(column));
                            }
                        } catch (FieldNotFoundException e) {
                            log.error("", e);
                            problematicXmlPaths.add("Field Not Found for " + xmlPath);
                        } catch (InvalidValueException e) {
                            log.error("", e);
                            problematicXmlPaths.add("Invalid Value passed for " + xmlPath);
                        }
                    }
                    columnIndex++;
                    if (!problematicXmlPaths.isEmpty()) {
                        problematicItems.put(item.getIDValue(), problematicXmlPaths);
                    }
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
                            log.error("", e);
                        } catch (InvalidValueException e) {
                            log.error("", e);
                        } catch (Exception e) {
                            log.error("", e);
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
                            } catch (FieldNotFoundException ignored) {}

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
                                        sb.append("<FONT COLOR='black'><b>").append(escapeHTML(nValue)).append("</b></FONT>");
                                        rowSummary.add(sb.toString());
                                    }else{
                                        sb.append("<A TITLE=\"Value does not match an existing " +gwf.getBaseElement() + "/" + gwf.getBaseCol() +".\"><FONT COLOR='red'><b>"+ escapeHTML(nValue) + "</b></FONT></A>");
                                        rowSummary.add(sb.toString());
                                    }
                                } catch (Exception e) {
                                    log.error("", e);
                                    sb.append("<A TITLE='Unknown exception.'><FONT COLOR='red'><b>"+ escapeHTML(nValue) + "<</b></FONT></A>");
                                    rowSummary.add(sb.toString());
                                }

                            }else{
                                if (gwf!=null){
                                    ValidationResults vr = XFTValidator.ValidateValue(nValue, gwf.getRules(), "xs", gwf, xmlPath, gwf.getParentElement().getGenericXFTElement());
                                    if (!vr.isValid()){
                                        sb.append("<A TITLE='" + vr.getResults().get(0)[1] +"'><FONT COLOR='red'><b>"+ escapeHTML(nValue) + "<</b></FONT></A>");
                                        rowSummary.add(sb.toString());
                                        continue;
                                    }
                                }

                                sb.append("<FONT COLOR='black'><b>").append(escapeHTML(nValue)).append("</b></FONT>");
                                rowSummary.add(sb.toString());
                            }
                        } catch (FieldNotFoundException e) {
                            log.error("", e);
                            sb.append("<A TITLE='Unknown field: " + xmlPath +"'><FONT COLOR='red'><b>ERROR</b></FONT></A>");
                            rowSummary.add(sb.toString());
                        }
                    }
                    if (problematicItems.containsKey(item.getIDValue())) {
                        rowSummary.add("<FONT COLOR='red'><b> ERROR: " + StringUtils.join(problematicItems.get(item.getIDValue()),",") + "</b></font>");
                    }else {
                        rowSummary.add("NEW");
                    }
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
                        } catch (FieldNotFoundException e) {}

                        try {
                            oValue = dbVersion.getProperty(xmlPath);
                            nValue = item.getProperty(xmlPath);
                        } catch (FieldNotFoundException e) {
                            log.error("", e);
                            sb.append("<A  TITLE='Unknown field: " + xmlPath +"'><FONT COLOR='red'><b>"+ escapeHTML(nValue) + "<</b></FONT></A>");
                            rowSummary.add(sb.toString());
                            continue;
                        }


                        if (gwf!=null){
                            ValidationResults vr = XFTValidator.ValidateValue(nValue, gwf.getRules(), "xs", gwf, xmlPath, gwf.getParentElement().getGenericXFTElement());
                            if (!vr.isValid()){
                                sb.append("<A TITLE='").append(vr.getResults().get(0)[1]).append("'><FONT COLOR='red'><b>").append(escapeHTML(nValue)).append("</b></FONT></A>");
                                rowSummary.add(sb.toString());
                                continue;
                            }
                        }

                        if (oValue==null || oValue.equals(""))
                        {
                            if (nValue!=null){
                                sb.append("<FONT COLOR='black'><b>").append(escapeHTML(nValue)).append("</b></FONT><FONT COLOR='#999999'>(NULL)</font>");
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
                                sb.append("<FONT COLOR='black'><b>NULL</b></FONT><FONT COLOR='#999999'>(").append(escapeHTML(oValue)).append(")</font>");
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
                                    sb.append("<FONT COLOR='black'><b>").append(escapeHTML(nValue)).append("</b></font> <FONT COLOR='#999999'>(").append(escapeHTML(oValue)).append(")</font>");
                                    modified=true;
                                }else{
                                    sb.append("<FONT COLOR='#999999'>").append(escapeHTML(nValue)).append("</FONT>");
                                }
                            }else{
                                sb.append("<FONT COLOR='#999999'><b>").append(escapeHTML(nValue)).append("</b></font> <FONT COLOR='#999999'>(").append(escapeHTML(oValue)).append(")</font>");
                            }
                            rowSummary.add(sb.toString());
                        } catch (InvalidValueException e) {
                            log.error("", e);
                        }
                    }
                    if (problematicItems.containsKey(item.getIDValue())) {
                        rowSummary.add("<FONT COLOR='red'><b>ERROR: " + StringUtils.join(problematicItems.get(item.getIDValue()),",") +  "</b></font>");
                    }else {
                        if (modified)
                            rowSummary.add("MODIFIED");
                        else
                            rowSummary.add("NO CHANGE");
                    }
                }

                displaySummary.add(rowSummary);
            }

            context.put("summary", displaySummary);

            data.setScreenTemplate("XDATScreen_uploadCSV3.vm");
        } catch (XFTInitException | ElementNotFoundException | IOException e) {
            log.error("", e);
            data.setScreenTemplate("XDATScreen_uploadCSV2.vm");
        }
    }

    private Object escapeHTML(Object o) {
        if (null == o) {
            return null;
        }
        if (o instanceof String) {
            return escapeHTML((String)o);
        }else if (o instanceof PGobject) {
            return escapeHTML(((PGobject)o).getValue());
        }
        return o;
    }

    private String escapeHTML(final String str) {
        return StringEscapeUtils.escapeHtml4(str);
    }


    private List buildRows(RunData data, List fields, String dataType, int indexOfCustomField) throws IOException {
        ArrayList rows = new ArrayList();
        if (indexOfCustomField == -1) { //All XML columns, escape all
            int i=0;
            while (data.getParameters().containsKey("row" + i)) {
                String row = ((String)TurbineUtils.GetPassedParameter("row" + i, data));
                List rowAL = XftStringUtils.commaDelimitedStringToArrayListUsingApacheCommons(TurbineUtils.unescapeParam(row));
                rows.add(rowAL.stream().map(r->TurbineUtils.escapeParam((String)r)).collect(Collectors.toList()));
                i++;
            }
        }else { //Only escape the non custom-field columns
            int i=0;
            while (data.getParameters().containsKey("row" + i)) {
                String row = data.getParameters().getString("row" + i);
                List rowAL = XftStringUtils.commaDelimitedStringToArrayListUsingApacheCommons(TurbineUtils.unescapeParam(row));

                for (int ind = 0;  ind < rowAL.size(); ind++) {
                    if (ind != indexOfCustomField) {
                        rowAL.set(ind, TurbineUtils.escapeParam(rowAL.get(ind)));
                    }else {
                        rowAL.set(ind, StringEscapeUtils.escapeJson((String)rowAL.get(ind)));
                    }
                }
                rows.add(rowAL);
                i++;
            }
        }
        return rows;
    }

    private String validateJson(final String value) throws InvalidValueException {
        try {
            final JsonNode validatedJson = objectMapper.readTree(value);
            return objectMapper.writeValueAsString(validatedJson);
        } catch (Exception e) {
            throw new InvalidValueException("Invalid json string");
        }
    }

    private boolean isEntireCustomField(final String dataType, final String xmlPath) {
        return  xmlPath.equals(getCustomFieldsPath(dataType));
    }

    private String getCustomFieldsPath(final String dataType) {
        return String.format("%s%s%s",dataType, XFT.PATH_SEPARATOR, CUSTOM_FIElDS_NAME);
    }

    private int getIndexOfCustomField(final List fields, final String dataType) {
        return IntStream.range(0, fields.size())
                .filter(i -> isEntireCustomField(dataType,(String)fields.get(i)))
                .findFirst().orElse(-1);
    }

    private final String CUSTOM_FIElDS_NAME = "custom_fields";
    private final String DOT_SEPARATOR = ".";
    private final ObjectMapper objectMapper;

}
