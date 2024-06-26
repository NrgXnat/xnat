/*
 * core: org.nrg.xdat.turbine.modules.actions.SearchA
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.exceptions.InvalidSearchException;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xft.utils.XftStringUtils;

import java.io.StringWriter;
import java.util.*;
import java.util.regex.Pattern;

import static org.nrg.xdat.security.helpers.Permissions.getUserProjectAccess;

/**
 * @author Tim
 *
 */
@Slf4j
public abstract class SearchA extends SecureAction {
    private long startTime = Calendar.getInstance().getTimeInMillis();

    public abstract DisplaySearch setupSearch(RunData data, Context context) throws Exception;

    public void doPreliminaryProcessing(RunData data, Context context) throws Exception{
        preserveVariables(data,context);
        DisplaySearch ds = setupSearch(data,context);
        if (ds !=null)
        {
            TurbineUtils.setSearch(data,ds);
        }
    }

    public void doFinalProcessing(RunData data, Context context) throws Exception{
    }

    @SuppressWarnings("unused")
    public boolean executeSearch()
    {
        return true;
    }

    @SuppressWarnings("unused")
    public Integer getDefaultPageSize(){
        return 40;
    }

	public void doPerform(RunData data, Context context)
	{
		try {
		    doPreliminaryProcessing(data,context);

			UserI user = TurbineUtils.getUser(data);
			String display = data.getParameters().getString("display", "listing");
            String queryMode = data.getParameters().getString("queryMode");
			String elementName = ((String)TurbineUtils.GetPassedParameter("element",data));
			Integer page = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedInteger("page",data));
			String sortBy = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("sortBy",data));
			String sortOrder = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("sortOrder",data));
			String queryType = data.getParameters().getString("queryType","stored");

			//TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());


			if (elementName == null || elementName.equalsIgnoreCase(""))
			{
				DisplaySearch search = TurbineUtils.getSearch(data);

                if (search==null) {
                    throw new SearchTimeoutException("Session Expired: The previously performed search has timed out.");
                }

                List<String> readableProjects = Permissions.getReadableProjects(user);
                List<String> protectedProjects = Permissions.getAllProtectedProjects(XDAT.getJdbcTemplate());
                Collection<String> readableExcludingProtected = CollectionUtils.subtract(readableProjects,protectedProjects);
                if(readableExcludingProtected.size()<=0 && (search.getRootElement()==null || !StringUtils.equals(search.getRootElement().getSQLName(),"xnat_projectData"))){//Projects user can see, excluding those that they might only be seeing because they are protected
                    boolean hasExplicitAccessToAtLeastOneProtectedProject = false;
                    for(String protectedProject: protectedProjects){
                        if(StringUtils.isNotBlank(getUserProjectAccess(user, protectedProject))){
                            hasExplicitAccessToAtLeastOneProtectedProject = true;
                        }
                    }
                    if(!hasExplicitAccessToAtLeastOneProtectedProject) {
                        throw new IllegalAccessException("The user is trying to search for data, but does not have access to any projects.");
                    }
                }

				if (hasSuperSearchVariables(data)) {
					search.setAdditionalViews(getSuperSearchVariables(data));

					if (search.getRootElement().getDisplay().getVersion("root")!=null)
					{
					    search.setDisplay("root");
					}
				}

                XdatStoredSearch xss= search.convertToStoredSearch("", queryMode);
					StringWriter sw = new StringWriter();
					xss.toXML(sw, false);

					context.put("xss", StringEscapeUtils.escapeXml10(sw.toString()));
			}else{
				DisplaySearch search = TurbineUtils.getSearch(data);
				if (search != null && hasSuperSearchVariables(data))
				{
					search.setAdditionalViews(getSuperSearchVariables(data));
				}
				if (search == null || hasSuperSearchVariables(data) || queryType.equalsIgnoreCase("new"))
				{
					search = UserHelper.getSearchHelperService().getSearchForUser(user, elementName, display);

					if (hasSuperSearchVariables(data))
					{
						search.setAdditionalViews(getSuperSearchVariables(data));
					}
				}

				XdatStoredSearch xss= search.convertToStoredSearch("", queryMode);
				StringWriter sw = new StringWriter();
				xss.toXML(sw, false);
				
				context.put("xss", StringEscapeUtils.escapeXml10(sw.toString()));
			}

			data.setScreenTemplate(getScreenTemplate(data));

			doFinalProcessing(data,context);
		} catch (SearchTimeoutException e) {
            log.error("The requested search timed out.", e);
            data.setMessage(e.getMessage());
            data.setScreenTemplate("Index.vm");
        } catch (XFTInitException e) {
            this.error(e, data);
		} catch (ElementNotFoundException e) {
            this.error(e, data);
		} catch (DBPoolException e) {
            this.error(e, data);
		}catch (IllegalAccessException e){
            data.setMessage("The user does not have access to this data.");
            data.setScreenTemplate("Error.vm");
            data.getParameters().setString("exception", e.toString());
		}catch (InvalidSearchException e){
            data.setMessage("You specified an invalid search condition: " + e.getMessage());
            data.setScreenTemplate("Error.vm");
		} catch (Exception e) {
            this.error(e, data);
		}

        data.getParameters().add("results_time", Calendar.getInstance().getTimeInMillis()-startTime);

	}

    @SuppressWarnings("serial")
    public class SearchTimeoutException extends Exception{
        public SearchTimeoutException(){
            super();
        }
        public SearchTimeoutException(String message){
            super(message);
        }
        public SearchTimeoutException(String message,Throwable error){
            super(message,error);
        }
    }

	public String getScreenTemplate(RunData data)
	{
	    return "Search.vm";
	}

	private boolean hasSuperSearchVariables(RunData data)
	{
		boolean found = false;
		Enumeration enumer = DisplayManager.GetInstance().getElements().keys();
		while (enumer.hasMoreElements())
		{
			String key = (String)enumer.nextElement();
			if ((String)TurbineUtils.GetPassedParameter("super_" + key.toLowerCase(),data) != null)
			{
				found = true;
				break;
			}
		}
		return found;
	}

	private ArrayList getSuperSearchVariables(RunData data)
	{
	    ArrayList found = new ArrayList();
		Enumeration enumer = DisplayManager.GetInstance().getElements().keys();
		while (enumer.hasMoreElements())
		{
			String key = (String)enumer.nextElement();
			if (TurbineUtils.GetPassedParameter("super_" + key.toLowerCase(), data) != null)
			{
			    String s = ((String)TurbineUtils.GetPassedParameter("super_" + key.toLowerCase(), data));
			    if (! s.equalsIgnoreCase(""))
			        found.add(new String[]{key,s});
			}
		}
		return found;
	}


    @SuppressWarnings("deprecation")
    public DisplaySearch setSearchCriteria(RunData data,DisplaySearch ds) throws Exception
    {
        ds.resetWebFormValues();

        UserI user = TurbineUtils.getUser(data);
        Iterator eds = UserHelper.getUserHelperService(user).getSearchableElementDisplays().iterator();
        while (eds.hasNext())
        {
            ElementDisplay ed = (ElementDisplay)eds.next();

            Collection al = ed.getSortedFields();
            Iterator iter = al.iterator();
            while (iter.hasNext())
            {
                DisplayField df = (DisplayField)iter.next();
                String s = ed.getElementName() + "." + df.getId();
                String type = df.getDataType();
                if (type.equalsIgnoreCase("string"))
                {
                    //logger.debug("");
                    if (TurbineUtils.HasPassedParameter(s + "_equals",data))
                    {
                        //logger.debug("like " + s);
                        Object[] os = ((Object[])org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedObjects(s + "_equals",data));

                        String osString = "";
                        int c =0;
                        for(Object o : os){
                            String temp = (String)o;
                            if(c++>0)osString +=",";
                            osString += temp;
                        }

                        ds.setWebFormValue(s + "_equals", osString);
                        ds.addCriteria(SearchA.processStringData(osString,ds,ed,df));
                    }

                    if (TurbineUtils.HasPassedParameter(s + "_in",data))
                    {
                        //logger.debug("like " + s);
                        Object o = org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_in",data);
                        ds.setWebFormValue(s + "_in",o);
                        String temp = (String)o;

                        temp = StringUtils.replace(temp.trim(), "\r\n", ",");

                        ds.addInClause(s,temp);
                    }
                }else if (type.equalsIgnoreCase("date"))
                {
                	if(TurbineUtils.HasPassedParameter(s + "_equals",data) && Pattern.matches("^[0-9]{4}$", (String)TurbineUtils.GetPassedParameter(s + "_equals",data))){
                		String to = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_equals",data));
                        ds.setWebFormValue(s + "_equals",to);
                        ds.addCriteria(ed.getElementName(),df.getId(),"=",to);
                	}
                	else if (TurbineUtils.HasPassedParameter(s + "_to_fulldate",data) || TurbineUtils.HasPassedParameter(s + "_from_fulldate",data))
                    {
                        if (TurbineUtils.HasPassedParameter(s + "_to_fulldate",data) && TurbineUtils.HasPassedParameter(s + "_from_fulldate",data))
                        {
                            String to = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_to_fulldate",data));
                            String from = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_from_fulldate",data));

                            ds.setWebFormValue(s + "_to_fulldate",to);
                            ds.setWebFormValue(s + "_from_fulldate",from);
                            Date toD = DateUtils.parseDate(to);
                            Date fromD = DateUtils.parseDate(from);
                            CriteriaCollection cc= ds.getEmptyCollection("AND");

                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">",to);
                            cc.add(dc);

                            dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<",from);
                            cc.add(dc);

                            ds.addCriteria(cc);
                        }
                        else if ((TurbineUtils.HasPassedParameter(s + "_to_fulldate",data))){
                            String to = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_to_fulldate",data));
                            ds.setWebFormValue(s + "_to_fulldate",to);
                            Date toD = DateUtils.parseDate(to);
                            ds.addCriteria(ed.getElementName(),df.getId(),">",to);
                        }else{
                            String from = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_from_fulldate",data));

                            ds.setWebFormValue(s + "_from_fulldate",from);

                            Date fromD = DateUtils.parseDate(from);
                            ds.addCriteria(ed.getElementName(),df.getId(),"<",from);
                        }
                    }else{
                        Integer tomonth = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedInteger(s + "_to_month",data));
                        Integer todate = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedInteger(s + "_to_date",data));
                        Integer toyear = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedInteger(s + "_to_year",data));

                        Integer frommonth = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedInteger(s + "_from_month",data));
                        Integer fromdate = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedInteger(s + "_from_date",data));
                        Integer fromyear = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedInteger(s + "_from_year",data));

                        boolean hasTo=false;
                        boolean hasFrom=false;

                        if (TurbineUtils.HasPassedParameter(s + "_to_month",data) && TurbineUtils.HasPassedParameter(s + "_to_date",data) && TurbineUtils.HasPassedParameter(s + "_to_year",data))
                        {
                            hasTo=true;
                        }

                        if (TurbineUtils.HasPassedParameter(s + "_from_month",data) && TurbineUtils.HasPassedParameter(s + "_from_date",data) && TurbineUtils.HasPassedParameter(s + "_from_year",data))
                        {
                            hasFrom=true;
                        }

                        if (hasTo)
                        {
                            if(hasFrom)
                            {
                                //logger.debug("fromdate " + s);
                                CriteriaCollection cc= ds.getEmptyCollection("AND");

                                GregorianCalendar cal = new GregorianCalendar(0,0,0);
        		    			Date date= cal.getTime();
                                date.setDate(todate.intValue());
                                date.setMonth(tomonth.intValue() - 1);
                                date.setYear(toyear.intValue()-1900);

                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">",date);
                                cc.add(dc);

        		    			date= cal.getTime();
                                date.setDate(fromdate.intValue());
                                date.setMonth(frommonth.intValue() - 1);
                                date.setYear(fromyear.intValue()-1900);

                                dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<",date);
                                cc.add(dc);

                                ds.addCriteria(cc);
                            }else{
                                //logger.debug("todate " + s);
                                GregorianCalendar cal = new GregorianCalendar(0,0,0);
        		    			Date date= cal.getTime();
                                date.setDate(todate.intValue());
                                date.setMonth(tomonth.intValue() - 1);
                                date.setYear(toyear.intValue()-1900);
                                ds.addCriteria(ed.getElementName(),df.getId(),">",date);
                            }
                        }else{
                            if(hasFrom)
                            {
                                //logger.debug("fromdate " + s);
                                GregorianCalendar cal = new GregorianCalendar(0,0,0);
        		    			Date date= cal.getTime();
                                date.setDate(fromdate.intValue());
                                date.setMonth(frommonth.intValue() - 1);
                                date.setYear(fromyear.intValue()-1900);
                                ds.addCriteria(ed.getElementName(),df.getId(),"<",date);
                            }
                        }
                    }
                }else if (type.equalsIgnoreCase("integer"))
                {
                    if (TurbineUtils.HasPassedParameter(s + "_equals",data))
                    {
                        ////logger.debug("equals " + s);
                        Object o = org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_equals",data);
                        ds.setWebFormValue(s + "_equals",o);
                        if (o != null && !o.toString().equals(""))
                        {
                            String fullLine = o.toString();
                            CriteriaCollection cc=SearchA.processNumericData(fullLine,ds,ed,df);
                            if(cc.size()>0)
                            	ds.addCriteria(cc);
                        }
                    }

                }else if (type.equalsIgnoreCase("float"))
                {
                    if (TurbineUtils.HasPassedParameter(s + "_equals",data))
                    {
                        ////logger.debug("equals " + s);
                        Object o = org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_equals",data);
                        if (o != null && !o.toString().equals(""))
                        {
                            ds.setWebFormValue(s + "_equals",o);
                            String fullLine = o.toString();
                            CriteriaCollection cc=SearchA.processNumericData(fullLine,ds,ed,df);
                            if(cc.size()>0)
                            	ds.addCriteria(cc);
                        }
                    }

                }else if (type.equalsIgnoreCase("double"))
                {
                    if (TurbineUtils.HasPassedParameter(s + "_equals",data))
                    {
                        ////logger.debug("equals " + s);
                        Object o = org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_equals",data);
                        if (o != null && !o.toString().equals(""))
                        {
                            ds.setWebFormValue(s + "_equals",o);
                            String fullLine = o.toString();
                            CriteriaCollection cc=SearchA.processNumericData(fullLine,ds,ed,df);
                            if(cc.size()>0)
                            	ds.addCriteria(cc);
                        }
                    }
                }else if (type.equalsIgnoreCase("decimal"))
                {
                    if (TurbineUtils.HasPassedParameter(s + "_equals",data))
                    {
                        ////logger.debug("equals");
                        Object o = org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_equals",data);
                        if (o != null && !o.toString().equals(""))
                        {
                            ds.setWebFormValue(s + "_equals",o);
                            String fullLine = o.toString();
                            CriteriaCollection cc=SearchA.processNumericData(fullLine,ds,ed,df);
                            if(cc.size()>0)
                            	ds.addCriteria(cc);
                        }
                    }
                }else{
                    if (TurbineUtils.HasPassedParameter(s + "_equals",data))
                    {
                        ////logger.debug("default " + s);
                        Object o = org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_equals",data);
                        if (o != null && !o.toString().equals(""))
                        {
                            ds.setWebFormValue(s + "_equals",o);
                            ds.addCriteria(ed.getElementName(),df.getId(),"=",o);
                        }
                    }
                }
            }

            SearchA.addComboFields(ed.getElementName() + ".COMBO", ds, data);
        }

        SearchA.addComboFields("COMBO", ds, data);
        
        return ds;
    }
    
    public static void addComboFields(String comboID, DisplaySearch ds, RunData data) throws DisplayFieldNotFoundException, Exception{
    	for(int counter=0;counter<10;counter++){
            Collection<String> values = org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedStrings(comboID + counter, data);
                if(values == null || values.size() == 0){
                   continue;
                }
                final CriteriaCollection outer = new CriteriaCollection("OR");
                for(String value : values){
                    if(value == null || value.length() == 0){ continue; }
                    final CriteriaCollection cc = new CriteriaCollection("OR");
                    final String keys = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(comboID + counter + "_FIELDS",data));

                    ds.setWebFormValue(comboID + counter,value);
                    ds.setWebFormValue(comboID + counter + "_FIELDS",keys);
                    String inClause = "";

                    Iterator<String> keyIter = XftStringUtils.CommaDelimitedStringToArrayList(keys).iterator();
                    while (keyIter.hasNext())
                    {
                        String key = (String)keyIter.next();
                        if (key.endsWith("_in"))
                        {
                            key = key.substring(0,key.length()-3);

                            if (inClause.equalsIgnoreCase(""))
                                inClause += key;
                            else
                                inClause += "," + key;
                        }else if(key.endsWith("_equals"))
                        {
                            key = key.substring(0,key.length()-7);
                            
                            final String elementName1 = XftStringUtils.GetRootElementName(key);

                            final SchemaElement element = SchemaElement.GetElement(elementName1);
                            final DisplayField df = DisplayField.getDisplayFieldForDFIdOrXPath(key);

                            if (df.getDataType().equalsIgnoreCase("string"))
                            {
                                cc.addCriteria(SearchA.processStringData(value,ds,element.getDisplay(),df));
                            }else{
                                final CriteriaCollection sub=SearchA.processNumericData(value,ds,element.getDisplay(),df);
                                if(sub.size()>0)
                                {
                                   outer.addCriteria(sub);
                                }
                            }
                        }
                    }

                    if (!inClause.equalsIgnoreCase(""))
                    {
                        ds.addInClause(inClause,value);
                    }else{
                        outer.addCriteria(cc);
                    }
                }
                if(outer.numClauses() > 0){
                   ds.addCriteria(outer);
                }
        }
    }

    private static CriteriaCollection processStringData(String value, DisplaySearch ds, ElementDisplay ed, DisplayField df) throws Exception
    {
      //logger.error("DisplaySearchAction:" + value);

        CriteriaCollection cc = new CriteriaCollection("OR");
        value=StringEscapeUtils.unescapeXml(value);
        value = StringUtils.replace(value.trim(), "\r\n,", ",");
        value = StringUtils.replace(value.trim(), ",\r\n", ",");
        value = StringUtils.replace(value.trim(), "\r\n", ",");
        value = StringUtils.replace(value.trim(), "NOT NULL", "NOT_NULL");
        value = StringUtils.replace(value.trim(), "IS NULL", "IS_NULL");
        value = StringUtils.replace(value.trim(), "IS NOT NULL", "IS_NOT_NULL");
        value = StringUtils.replace(value, "*", "%");
        while (value.indexOf(",")!=-1 && !df.getId().equalsIgnoreCase("PROJECT_INVS")) // PROJECT_INVS has a comma in its middle
        {
            if (value.indexOf(",")==0)
            {
                if (value.length()>1)
                {
                    value = value.substring(1);
                }else{
                    value = "";
                }
            }else{
                String temp = value.substring(0,value.indexOf(",")).trim();
                value = value.substring(value.indexOf(",") + 1);

                if (temp.startsWith("'"))
                {
                    temp= StringUtils.replace(temp, "'", "");
                    DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",temp);
                    cc.add(dc);
                }else if (temp.startsWith("\"")){
                    temp= StringUtils.replace(temp, "\"", "");
                    DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",temp);
                    cc.add(dc);
                }else{
                    if (temp.indexOf(" ")!=-1)
                    {
                    	
                        CriteriaCollection subCC = new CriteriaCollection("OR");
                        Iterator strings= XftStringUtils.DelimitedStringToArrayList(temp, " ").iterator();
                        while (strings.hasNext())
                        {
                            String s= (String)strings.next();
                            if (s.startsWith(">="))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",s.substring(2));
                                subCC.add(dc);
                            }else if (s.startsWith("<="))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",s.substring(2));
                                subCC.add(dc);
                            }else if (s.startsWith("<"))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<",s.substring(1));
                                subCC.add(dc);
                            }else if (s.startsWith(">"))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">",s.substring(1));
                                subCC.add(dc);
                            }else if (s.equalsIgnoreCase("IS_NULL"))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                                subCC.add(dc);
                            }else if (s.equalsIgnoreCase("NULL"))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                                subCC.add(dc);
                            }else if (s.equalsIgnoreCase("IS_NOT_NULL"))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                                subCC.add(dc);
                            }else if (s.equalsIgnoreCase("NOT_NULL"))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                                subCC.add(dc);
                            }else if(s.startsWith("=")){
                                if (s.startsWith("/")){
                                    s = s.substring(1);
                                }
                                //equals
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",s.substring(1));
                                subCC.add(dc);
                            }else{
                                if (s.startsWith("/")){
                                    s = s.substring(1);
                                }
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," LIKE ","%" + s + "%");
                                subCC.add(dc);
                            }
                        }

                        cc.add(subCC);
                    }else{
                        String s= temp;
                        if (s.startsWith(">="))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",s.substring(2));
                            cc.add(dc);
                        }else if (s.startsWith("<="))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",s.substring(2));
                            cc.add(dc);
                        }else if (s.startsWith("<"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<",s.substring(1));
                            cc.add(dc);
                        }else if (s.startsWith(">"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">",s.substring(1));
                            cc.add(dc);
                        }else if (s.equalsIgnoreCase("IS_NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                            cc.add(dc);
                        }else if (s.equalsIgnoreCase("NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                            cc.add(dc);
                        }else if (s.equalsIgnoreCase("IS_NOT_NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                            cc.add(dc);
                        }else if (s.equalsIgnoreCase("NOT_NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                            cc.add(dc);
                        }else if(s.startsWith("=")){
                            if (s.startsWith("/")){
                                s = s.substring(1);
                            }
                            //equals
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",s.substring(1));
                            cc.add(dc);
                        }else{
                            if (temp.startsWith("/")){
                                temp = temp.substring(1);
                            }
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," LIKE ","%" + temp + "%");
                            cc.add(dc);
                        }
                    }
                }
            }
        }

        if (!value.equalsIgnoreCase(""))
        {
            String temp = value.trim();

            if (temp.startsWith("'"))
            {
                temp= StringUtils.replace(temp, "'", "");
                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",temp);
                cc.add(dc);
            }else if (temp.startsWith("\"")){
                temp= StringUtils.replace(temp, "\"", "");
                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",temp);
                cc.add(dc);
            }else{
                if (temp.indexOf(" ")!=-1)
                {
                    CriteriaCollection subCC = new CriteriaCollection("OR");
                    Iterator strings = null;
                    if (df.getId().equalsIgnoreCase("PROJECT_INVS")) {  //project_invs always has a space in it and we don't want it torn apart
                        strings = Arrays.asList(new String[]{temp}).iterator(); // this is stupid, but it needs an iterator
                    } else {
                        strings= XftStringUtils.DelimitedStringToArrayList(temp, " ").iterator();
                    }
                    while (strings.hasNext())
                    {
                        String s= (String)strings.next();

                        if (s.startsWith(">="))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",s.substring(2));
                            subCC.add(dc);
                        }else if (s.startsWith("<="))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",s.substring(2));
                            subCC.add(dc);
                        }else if (s.startsWith("<"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<",s.substring(1));
                            subCC.add(dc);
                        }else if (s.startsWith(">"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">",s.substring(1));
                            subCC.add(dc);
                        }else if (s.equalsIgnoreCase("IS_NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                            subCC.add(dc);
                        }else if (s.equalsIgnoreCase("NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                            subCC.add(dc);
                        }else if (s.equalsIgnoreCase("IS_NOT_NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                            subCC.add(dc);
                        }else if (s.equalsIgnoreCase("NOT_NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                            subCC.add(dc);
                        }else if(s.startsWith("=")){
                            if (s.startsWith("/")){
                                s = s.substring(1);
                            }
                            //equals
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",s.substring(1));
                            subCC.add(dc);
                        }else{
                            if (s.startsWith("/")){
                                s = s.substring(1);
                            }
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," LIKE ","%" + s + "%");
                            subCC.add(dc);
                        }
                    }

                    cc.add(subCC);
                }else{
                    String s= temp;
                    if (s.startsWith(">="))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",s.substring(2));
                        cc.add(dc);
                    }else if (s.startsWith("<="))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",s.substring(2));
                        cc.add(dc);
                    }else if (s.startsWith("<"))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<",s.substring(1));
                        cc.add(dc);
                    }else if (s.startsWith(">"))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">",s.substring(1));
                        cc.add(dc);
                    }else if (s.equalsIgnoreCase("IS_NULL"))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                        cc.add(dc);
                    }else if (s.equalsIgnoreCase("NULL"))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                        cc.add(dc);
                    }else if (s.equalsIgnoreCase("IS_NOT_NULL"))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                        cc.add(dc);
                    }else if (s.equalsIgnoreCase("NOT_NULL"))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                        cc.add(dc);
                    }else if(s.startsWith("=")){
                        if (s.startsWith("/")){
                            s = s.substring(1);
                        }
                        //equals
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",s.substring(1));
                        cc.add(dc);
                    }else{
                        if (temp.startsWith("/")){
                            temp = temp.substring(1);
                        }
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," LIKE ","%" + temp + "%");
                        cc.add(dc);
                    }
                }
            }
        }
        return cc;
    }

    private static CriteriaCollection processNumericData(String value, DisplaySearch ds, ElementDisplay ed, DisplayField df) throws Exception
    {
        CriteriaCollection cc = new CriteriaCollection("OR");
        value=StringEscapeUtils.unescapeXml(value);
        value = StringUtils.replace(value.trim(), "\r\n", ",");
        value = StringUtils.replace(value.trim(), "'", "");
        value = StringUtils.replace(value.trim(), "\"", "");
        value = StringUtils.replace(value.trim(), "NOT NULL", "NOT_NULL");
        value = StringUtils.replace(value.trim(), "IS NULL", "IS_NULL");
        value = StringUtils.replace(value.trim(), "IS NOT NULL", "IS_NOT_NULL");
        while (value.indexOf(",")!=-1)
        {
            if (value.indexOf(",")==0)
            {
                if (value.length()>1)
                {
                    value = value.substring(1);
                }else{
                    value = "";
                }
            }else{
                String integer = value.substring(0,value.indexOf(",")).trim();

                integer = CleanWhiteSpaces(integer);

                value = value.substring(value.indexOf(",") + 1);
                if (integer.indexOf(" ")!= -1)
                {
                    CriteriaCollection subCC = new CriteriaCollection("OR");
                    Iterator strings= XftStringUtils.DelimitedStringToArrayList(integer, " ").iterator();
                    while (strings.hasNext())
                    {
                        String s= (String)strings.next();

                        if (s.indexOf("-")==-1)
                        {
                            if (s.startsWith(">="))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",s.substring(2));
                                subCC.add(dc);
                            }else if (s.startsWith("<="))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",s.substring(2));
                                subCC.add(dc);
                            }else if (s.startsWith("<"))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<",s.substring(1));
                                subCC.add(dc);
                            }else if (s.startsWith(">"))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">",s.substring(1));
                                subCC.add(dc);
                            }else if (s.equalsIgnoreCase("IS_NULL"))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                                subCC.add(dc);
                            }else if (s.equalsIgnoreCase("NULL"))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                                subCC.add(dc);
                            }else if (s.equalsIgnoreCase("IS_NOT_NULL"))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                                subCC.add(dc);
                            }else if (s.equalsIgnoreCase("NOT_NULL"))
                            {
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                                subCC.add(dc);
                            }else{
                                //equals
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",s);
                                subCC.add(dc);
                            }
                        }else{
                            //range
                            if (s.indexOf("(-")==-1)
                            {
                                CriteriaCollection newcc= ds.getEmptyCollection("AND");
                                String pre = s.substring(0,s.indexOf("-"));
                                String post = s.substring(s.indexOf("-")+1);

                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",pre);
                                newcc.add(dc);

                                dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",post);
                                newcc.add(dc);

                                subCC.addCriteria(newcc);
                            }else{
                                String pre=null;
                                String post=null;
                                if (s.startsWith("("))
                                {
                                    pre = s.substring(0,s.indexOf(")"));
                                    pre = StringUtils.replace(pre, "(", "");
                                    s = s.substring(s.indexOf(")-")+2);
                                }else{
                                    pre = s.substring(s.indexOf("-"));
                                    s = s.substring(s.indexOf("-")+1);
                                }

                                post = StringUtils.replace(s, "(", "");
                                post = StringUtils.replace(post, ")", "");

                                CriteriaCollection newcc= ds.getEmptyCollection("AND");

                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",pre);
                                newcc.add(dc);

                                dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",post);
                                newcc.add(dc);

                                subCC.addCriteria(newcc);
                            }
                        }
                    }

                    cc.add(subCC);
                }else{
                    String s= integer;
                    if (s.indexOf("-")==-1)
                    {
                        if (s.startsWith(">="))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",s.substring(2));
                            cc.add(dc);
                        }else if (s.startsWith("<="))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",s.substring(2));
                            cc.add(dc);
                        }else if (s.startsWith("<"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<",s.substring(1));
                            cc.add(dc);
                        }else if (s.startsWith(">"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">",s.substring(1));
                            cc.add(dc);
                        }else if (s.equalsIgnoreCase("IS_NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                            cc.add(dc);
                        }else if (s.equalsIgnoreCase("NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                            cc.add(dc);
                        }else if (s.equalsIgnoreCase("IS_NOT_NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                            cc.add(dc);
                        }else if (s.equalsIgnoreCase("NOT_NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                            cc.add(dc);
                        }else{
                            //equals
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",s);
                            cc.add(dc);
                        }
                    }else{
                        //range
                        if (s.indexOf("(-")==-1)
                        {
                            CriteriaCollection newcc= ds.getEmptyCollection("AND");
                            String pre = s.substring(0,s.indexOf("-"));
                            String post = s.substring(s.indexOf("-")+1);

                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",pre);
                            newcc.add(dc);

                            dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",post);
                            newcc.add(dc);

                            cc.addCriteria(newcc);
                        }else{
                            String pre=null;
                            String post=null;
                            if (s.startsWith("("))
                            {
                                pre = s.substring(0,s.indexOf(")"));
                                pre = StringUtils.replace(pre, "(", "");
                                s = s.substring(s.indexOf(")-")+2);
                            }else{
                                pre = s.substring(s.indexOf("-"));
                                s = s.substring(s.indexOf("-")+1);
                            }

                            post = StringUtils.replace(s, "(", "");
                            post = StringUtils.replace(post, ")", "");

                            CriteriaCollection newcc= ds.getEmptyCollection("AND");

                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",pre);
                            newcc.add(dc);

                            dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",post);
                            newcc.add(dc);

                            cc.addCriteria(newcc);
                        }
                    }
                }

            }
        }

        if (! value.equalsIgnoreCase(""))
        {
            String integer = value.trim();

            integer = CleanWhiteSpaces(integer);
            

            if (integer.indexOf(" ")!= -1)
            {
                CriteriaCollection subCC = new CriteriaCollection("OR");
                Iterator strings= XftStringUtils.DelimitedStringToArrayList(integer, " ").iterator();
                while (strings.hasNext())
                {
                    String s= (String)strings.next();

                    if (s.indexOf("-")==-1)
                    {
                        if (s.startsWith(">="))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",s.substring(2));
                            subCC.add(dc);
                        }else if (s.startsWith("<="))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",s.substring(2));
                            subCC.add(dc);
                        }else if (s.startsWith("<"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<",s.substring(1));
                            subCC.add(dc);
                        }else if (s.startsWith(">"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">",s.substring(1));
                            subCC.add(dc);
                        }else if (s.equalsIgnoreCase("IS_NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                            subCC.add(dc);
                        }else if (s.equalsIgnoreCase("NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                            subCC.add(dc);
                        }else if (s.equalsIgnoreCase("IS_NOT_NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                            subCC.add(dc);
                        }else if (s.equalsIgnoreCase("NOT_NULL"))
                        {
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                            subCC.add(dc);
                        }else{
                            //equals
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",s);
                            subCC.add(dc);
                        }
                    }else{
                        //range
                        if (s.indexOf("(-")==-1)
                        {
                            CriteriaCollection newcc= ds.getEmptyCollection("AND");
                            String pre = s.substring(0,s.indexOf("-"));
                            String post = s.substring(s.indexOf("-")+1);

                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",pre);
                            newcc.add(dc);

                            dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",post);
                            newcc.add(dc);

                            subCC.addCriteria(newcc);
                        }else{
                            String pre=null;
                            String post=null;
                            if (s.startsWith("("))
                            {
                                pre = s.substring(0,s.indexOf(")"));
                                pre = StringUtils.replace(pre, "(", "");
                                s = s.substring(s.indexOf(")-")+2);
                            }else{
                                pre = s.substring(s.indexOf("-"));
                                s = s.substring(s.indexOf("-")+1);
                            }

                            post = StringUtils.replace(s, "(", "");
                            post = StringUtils.replace(post, ")", "");

                            CriteriaCollection newcc= ds.getEmptyCollection("AND");

                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",pre);
                            newcc.add(dc);

                            dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",post);
                            newcc.add(dc);

                            subCC.addCriteria(newcc);
                        }
                    }
                }

                cc.add(subCC);
            }else{
                String s= integer;
                
                s=StringEscapeUtils.unescapeXml(s);
                
                if (s.indexOf("-")==-1)
                {
                    if (s.startsWith(">="))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",s.substring(2));
                        cc.add(dc);
                    }else if (s.startsWith("<="))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",s.substring(2));
                        cc.add(dc);
                    }else if (s.startsWith("<"))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<",s.substring(1));
                        cc.add(dc);
                    }else if (s.startsWith(">"))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">",s.substring(1));
                        cc.add(dc);
                    }else if (s.equalsIgnoreCase("IS_NULL"))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                        cc.add(dc);
                    }else if (s.equalsIgnoreCase("NULL"))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS ","NULL");
                        cc.add(dc);
                    }else if (s.equalsIgnoreCase("IS_NOT_NULL"))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NOT NULL");
                        cc.add(dc);
                    }else if (s.equalsIgnoreCase("NOT_NULL"))
                    {
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId()," IS NOT ","NULL");
                        cc.add(dc);
                    }else{
                        //equals
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",s);
                        cc.add(dc);
                    }
                }else{
                    //range
                    if (s.indexOf("(-")==-1)
                    {
                        CriteriaCollection newcc= ds.getEmptyCollection("AND");
                        String pre = s.substring(0,s.indexOf("-"));
                        String post = s.substring(s.indexOf("-")+1);

                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",pre);
                        newcc.add(dc);

                        dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",post);
                        newcc.add(dc);

                        cc.addCriteria(newcc);
                    }else{
                        String pre=null;
                        String post=null;
                        if (s.startsWith("("))
                        {
                            pre = s.substring(0,s.indexOf(")"));
                            pre = StringUtils.replace(pre, "(", "");
                            s = s.substring(s.indexOf(")-")+2);
                        }else{
                            pre = s.substring(s.indexOf("-"));
                            s = s.substring(s.indexOf("-")+1);
                        }

                        post = StringUtils.replace(s, "(", "");
                        post = StringUtils.replace(post, ")", "");

                        CriteriaCollection newcc= ds.getEmptyCollection("AND");

                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),">=",pre);
                        newcc.add(dc);

                        dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"<=",post);
                        newcc.add(dc);

                        cc.addCriteria(newcc);
                    }
                }
            }
        }

		return cc;
    }



    public static String CleanWhiteSpaces(String s)
    {
        s = StringUtils.replace(s, "  ", " ");

        s = StringUtils.replace(s, " -", "-");
        s = StringUtils.replace(s, "- ", "-");

        //s = StringUtils.replace(s," >",">");
        s = StringUtils.replace(s, "> ", ">");

        //s = StringUtils.replace(s," <","<");
        s = StringUtils.replace(s, "< ", "<");

        //s = StringUtils.replace(s," <=","<=");
        s = StringUtils.replace(s, "<= ", "<=");

        //s = StringUtils.replace(s," >=",">=");
        s = StringUtils.replace(s, ">= ", ">=");

        return s;
    }
}

