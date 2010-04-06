//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 11, 2005
 *
 */
package org.nrg.xft.utils;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
/**
 * @author Tim
 *
 */
public class DateUtils {
	List<SimpleDateFormat> dates=new ArrayList<SimpleDateFormat>();
	List<SimpleDateFormat> dateTimes=new ArrayList<SimpleDateFormat>();
	List<SimpleDateFormat> times=new ArrayList<SimpleDateFormat>();
	
	private boolean inUse=false;
	
	private DateFormat df=null;
	private static List<DateUtils> FREE=new ArrayList<DateUtils>();
		
	public synchronized static DateUtils GetInstance(){
		DateUtils d=null;
		if(FREE.size()>0){
			d=FREE.remove(0);
		}
		
		if(d!=null){
			d.open();
		}else{
			d=new DateUtils();
			d.open();
		}
		
		return d;
	}
		
	private void open(){
		inUse=true;
	}
	
	private void close(){
		FREE.add(this);
		inUse=false;
	}
	
	private DateUtils(){
		dates.add(new SimpleDateFormat("yyyy-MM-dd", Locale.US));
		dates.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US));
		dates.add(new SimpleDateFormat("MM/dd/yyyy", Locale.US));
		dates.add(new SimpleDateFormat("MM.dd.yyyy", Locale.US));
		dates.add(new SimpleDateFormat("yyyyMMdd", Locale.US));
		

		dateTimes.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US));
		dateTimes.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US));
		dateTimes.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.US));
		dateTimes.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss.S", Locale.US));
		dateTimes.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US));
		dateTimes.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss", Locale.US));
		dateTimes.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss z", Locale.US));
		dateTimes.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US));
		dateTimes.add(new SimpleDateFormat("MM/dd/yyyy", Locale.US));
		dateTimes.add(new SimpleDateFormat("yyyy-MM-dd", Locale.US));

		times.add(new SimpleDateFormat("HH:mm:ss", Locale.US));
		times.add(new SimpleDateFormat("HH:mm:ss z", Locale.US));
		times.add(new SimpleDateFormat("HHmmss", Locale.US));
		
		df=DateFormat.getInstance();
	}
	
	
	
	private Date parse(List<SimpleDateFormat> al,String s) throws ParseException{
		if (s.indexOf("'")!= -1)
        {
            s = StringUtils.ReplaceStr(s,"'","");
        }
        if (s==null)
        {
            return null;
        }else{
        	ParseException firstE=null;
            try {
                return DateFormat.getInstance().parse(s);
            }catch(ParseException e){firstE=e;}
            
            int c=0;
            for(SimpleDateFormat sdf :al){
            	try {
					Date temp= sdf.parse(s);
					return temp;
				} catch (ParseException e) {}
				c++;
            }
            
            throw firstE;
        }
	}
	
	private synchronized Date parseD(String s) throws ParseException{
		return this.parse(dates, s);
	}
	
	private synchronized Date parseDT(String s) throws ParseException{
		return this.parse(dateTimes, s);
	}
	
	private synchronized Date parseT(String s) throws ParseException{
		return this.parse(times, s);
	}
	
    public static Date parseDate(String s) throws ParseException
    {
    	DateUtils du=GetInstance();
    	Date d=du.parseD(s);
    	du.close();
        return d;
    }
    
    
    public static Date parseDateTime(String s) throws ParseException
    {
    	DateUtils du=GetInstance();
    	Date d=du.parseDT(s);
    	du.close();
        return d;
    }
    
    public static Date parseTime(String s) throws ParseException
    {
    	DateUtils du=GetInstance();
    	Date d=du.parseT(s);
    	du.close();
        return d;
    }
}

