//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT ï¿½ Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 15, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xft.utils.StringUtils;

/**
 * @author Tim
 *
 */
public abstract class SearchA extends SecureAction {
    static Logger logger = Logger.getLogger(SearchA.class);
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

    public boolean executeSearch()
    {
        return true;
    }

    public Integer getDefaultPageSize(){
        return new Integer(40);
    }

	public void doPerform(RunData data, Context context)
	{
		try {
		    doPreliminaryProcessing(data,context);

			XDATUser user = TurbineUtils.getUser(data);
			String display = data.getParameters().getString("display","listing");
			String elementName = data.getParameters().getString("element");
			Integer page = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("page",data));
			String sortBy = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("sortBy",data));
			String sortOrder = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("sortOrder",data));
			String queryType = data.getParameters().getString("queryType","stored");

			//TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());


			if (elementName == null || elementName.equalsIgnoreCase(""))
			{
				DisplaySearch search = TurbineUtils.getSearch(data);
				if (hasSuperSearchVariables(data))
				{
					search.setAdditionalViews(getSuperSearchVariables(data));

					if (search.getRootElement().getDisplay().getVersion("root")!=null)
					{
					    search.setDisplay("root");
					}
				}


                if (search==null)
                {
                    throw new SearchTimeoutException("Session Expired: The previously performed search has timed out.");
                }

				if (search == null)
				{
					throw new Exception("Unknown element'" + elementName + "'");
				}else
				{
					XdatStoredSearch xss= search.convertToStoredSearch("");
					StringWriter sw = new StringWriter();
					xss.toXML(sw, false);
					
					context.put("xss", StringEscapeUtils.escapeXml(sw.toString()));
				}
			}else{
				DisplaySearch search = TurbineUtils.getSearch(data);
				if (hasSuperSearchVariables(data))
				{
					search.setAdditionalViews(getSuperSearchVariables(data));
				}
				if (search == null || hasSuperSearchVariables(data) || queryType.equalsIgnoreCase("new"))
				{
					search = user.getSearch(elementName,display);

					if (hasSuperSearchVariables(data))
					{
						search.setAdditionalViews(getSuperSearchVariables(data));
					}
				}

				XdatStoredSearch xss= search.convertToStoredSearch("");
				StringWriter sw = new StringWriter();
				xss.toXML(sw, false);
				
				context.put("xss", StringEscapeUtils.escapeXml(sw.toString()));
			}
			data.setScreenTemplate(getScreenTemplate(data));

			doFinalProcessing(data,context);
		} catch (SearchTimeoutException e) {
            logger.error(e);
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
		}catch (SQLException e) {
            this.error(e, data);
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
			if (data.getParameters().getString("super_" + key.toLowerCase()) != null)
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
			if (data.getParameters().getString("super_" + key.toLowerCase()) != null)
			{
			    String s = data.getParameters().getString("super_" + key.toLowerCase());
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

        XDATUser user = TurbineUtils.getUser(data);
        Iterator eds = user.getSearchableElementDisplays().iterator();
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
                        Object[] os = ((Object[])org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_equals",data));

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

                        temp = StringUtils.ReplaceStr(temp.trim(),"\r\n",",");

                        ds.addInClause(s,temp);
                    }
                }else if (type.equalsIgnoreCase("date"))
                {
                    if (TurbineUtils.HasPassedParameter(s + "_to_fulldate",data) || TurbineUtils.HasPassedParameter(s + "_from_fulldate",data))
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
                        }else if ((TurbineUtils.HasPassedParameter(s + "_to_fulldate",data))){
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
                        Integer tomonth = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_to_month",data));
                        Integer todate = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_to_date",data));
                        Integer toyear = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_to_year",data));

                        Integer frommonth = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_from_month",data));
                        Integer fromdate = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_from_date",data));
                        Integer fromyear = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(s + "_from_year",data));

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


            int counter = 0;
            while (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(ed.getElementName(,data)) + ".COMBO" + counter) != null)
            {
                if(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(ed.getElementName(,data)) + ".COMBO" + counter).length() == 0){counter++;continue;}

                final CriteriaCollection cc = new CriteriaCollection("OR");
                final String value = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(ed.getElementName(,data)) + ".COMBO" + counter);
                final String keys = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(ed.getElementName(,data)) + ".COMBO" + counter + "_FIELDS");

                ds.setWebFormValue(ed.getElementName() + ".COMBO" + counter,value);
                ds.setWebFormValue(ed.getElementName() + ".COMBO" + counter + "_FIELDS",keys);

                String inClause = "";

                Iterator keyIter = StringUtils.CommaDelimitedStringToArrayList(keys).iterator();
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
                        
                        final String elementName1 = StringUtils.GetRootElementName(key);

                        final SchemaElement element = SchemaElement.GetElement(elementName1);
                        final DisplayField df = DisplayField.getDisplayFieldForDFIdOrXPath(key);

                        if (df.getDataType().equalsIgnoreCase("string"))
                        {
                            cc.addCriteria(SearchA.processStringData(value,ds,element.getDisplay(),df));
                        }else{
                            final CriteriaCollection sub=SearchA.processNumericData(value,ds,ed,df);
                            if(sub.size()>0)
                            	ds.addCriteria(sub);
                        }
                    }
                }
                counter++;

                if (!inClause.equalsIgnoreCase(""))
                {
                    ds.addInClause(inClause,value);
                }else{
                    ds.addCriteria(cc);
                }
            }
        }

        int counter = 0;
        while (TurbineUtils.HasPassedParameter("COMBO" +counter,data))
        {
            CriteriaCollection cc = new CriteriaCollection("OR");
            String value = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("COMBO" + counter,data));
            String keys = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("COMBO" + counter + "_FIELDS",data));

            ds.setWebFormValue("COMBO" + counter,value);
            ds.setWebFormValue("COMBO" + counter + "_FIELDS",keys);

            String inClause = "";

            Iterator keyIter = StringUtils.CommaDelimitedStringToArrayList(keys).iterator();
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
                    
                    final String elementName1 = StringUtils.GetRootElementName(key);
                    final SchemaElement element = SchemaElement.GetElement(elementName1);
                    
                    final DisplayField df = DisplayField.getDisplayFieldForDFIdOrXPath(key);

                    if (df.getDataType().equalsIgnoreCase("string"))
                    {
                        cc.addCriteria(SearchA.processStringData(value,ds,element.getDisplay(),df));
                    }else{
                        CriteriaCollection sub=SearchA.processNumericData(value,ds,element.getDisplay(),df);
                        if(sub.size()>0)
                        	ds.addCriteria(sub);
                    }
                }
            }
            counter++;

            if (!inClause.equalsIgnoreCase(""))
            {
                ds.addInClause(inClause,value);
            }else{
                ds.addCriteria(cc);
            }
        }

        return ds;
    }

    private static CriteriaCollection processStringData(String value, DisplaySearch ds, ElementDisplay ed, DisplayField df) throws Exception
    {
      //logger.error("DisplaySearchAction:" + value);

        CriteriaCollection cc = new CriteriaCollection("OR");
        value = StringUtils.ReplaceStr(value.trim(),"\r\n,",",");
        value = StringUtils.ReplaceStr(value.trim(),",\r\n",",");
        value = StringUtils.ReplaceStr(value.trim(),"\r\n",",");
        value = StringUtils.ReplaceStr(value.trim(),"NOT NULL","NOT_NULL");
        value = StringUtils.ReplaceStr(value.trim(),"IS NULL","IS_NULL");
        value = StringUtils.ReplaceStr(value.trim(),"IS NOT NULL","IS_NOT_NULL");
        value = StringUtils.ReplaceStr(value,"*","%");
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
                String temp = value.substring(0,value.indexOf(",")).trim();
                value = value.substring(value.indexOf(",") + 1);

                if (temp.startsWith("'"))
                {
                    temp= StringUtils.ReplaceStr(temp,"'","");
                    DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",temp);
                    cc.add(dc);
                }else if (temp.startsWith("\"")){
                    temp= StringUtils.ReplaceStr(temp,"\"","");
                    DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",temp);
                    cc.add(dc);
                }else{
                    if (temp.indexOf(" ")!=-1)
                    {
                    	
                        CriteriaCollection subCC = new CriteriaCollection("OR");
                        Iterator strings= StringUtils.DelimitedStringToArrayList(temp," ").iterator();
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
                                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",s);
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
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",s);
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
                temp= StringUtils.ReplaceStr(temp,"'","");
                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",temp);
                cc.add(dc);
            }else if (temp.startsWith("\"")){
                temp= StringUtils.ReplaceStr(temp,"\"","");
                DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",temp);
                cc.add(dc);
            }else{
                if (temp.indexOf(" ")!=-1)
                {
                    CriteriaCollection subCC = new CriteriaCollection("OR");
                    Iterator strings= StringUtils.DelimitedStringToArrayList(temp," ").iterator();
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
                            DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",s);
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
                        DisplayCriteria dc = DisplayCriteria.addCriteria(ed.getElementName(),df.getId(),"=",s);
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
        value = StringUtils.ReplaceStr(value.trim(),"\r\n",",");
        value = StringUtils.ReplaceStr(value.trim(),"'","");
        value = StringUtils.ReplaceStr(value.trim(),"\"","");
        value = StringUtils.ReplaceStr(value.trim(),"NOT NULL","NOT_NULL");
        value = StringUtils.ReplaceStr(value.trim(),"IS NULL","IS_NULL");
        value = StringUtils.ReplaceStr(value.trim(),"IS NOT NULL","IS_NOT_NULL");
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
                    Iterator strings= StringUtils.DelimitedStringToArrayList(integer," ").iterator();
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
                                    pre = StringUtils.ReplaceStr(pre,"(","");
                                    s = s.substring(s.indexOf(")-")+2);
                                }else{
                                    pre = s.substring(s.indexOf("-"));
                                    s = s.substring(s.indexOf("-")+1);
                                }

                                post = StringUtils.ReplaceStr(s,"(","");
                                post = StringUtils.ReplaceStr(post,")","");

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
                                pre = StringUtils.ReplaceStr(pre,"(","");
                                s = s.substring(s.indexOf(")-")+2);
                            }else{
                                pre = s.substring(s.indexOf("-"));
                                s = s.substring(s.indexOf("-")+1);
                            }

                            post = StringUtils.ReplaceStr(s,"(","");
                            post = StringUtils.ReplaceStr(post,")","");

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
                Iterator strings= StringUtils.DelimitedStringToArrayList(integer," ").iterator();
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
                                pre = StringUtils.ReplaceStr(pre,"(","");
                                s = s.substring(s.indexOf(")-")+2);
                            }else{
                                pre = s.substring(s.indexOf("-"));
                                s = s.substring(s.indexOf("-")+1);
                            }

                            post = StringUtils.ReplaceStr(s,"(","");
                            post = StringUtils.ReplaceStr(post,")","");

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
                            pre = StringUtils.ReplaceStr(pre,"(","");
                            s = s.substring(s.indexOf(")-")+2);
                        }else{
                            pre = s.substring(s.indexOf("-"));
                            s = s.substring(s.indexOf("-")+1);
                        }

                        post = StringUtils.ReplaceStr(s,"(","");
                        post = StringUtils.ReplaceStr(post,")","");

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
        s = StringUtils.ReplaceStr(s,"  "," ");

        s = StringUtils.ReplaceStr(s," -","-");
        s = StringUtils.ReplaceStr(s,"- ","-");

        //s = StringUtils.ReplaceStr(s," >",">");
        s = StringUtils.ReplaceStr(s,"> ",">");

        //s = StringUtils.ReplaceStr(s," <","<");
        s = StringUtils.ReplaceStr(s,"< ","<");

        //s = StringUtils.ReplaceStr(s," <=","<=");
        s = StringUtils.ReplaceStr(s,"<= ","<=");

        //s = StringUtils.ReplaceStr(s," >=",">=");
        s = StringUtils.ReplaceStr(s,">= ",">=");

        return s;
    }
}

