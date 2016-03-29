/*
 * GENERATED FILE
 * Created on Fri Apr 06 12:20:56 CDT 2007
 *
 */
package org.nrg.xdat.bean.base;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang.StringEscapeUtils;



@SuppressWarnings({"rawtypes"})
public abstract class BaseElement{
    //public enum FIELD_TYPE {data,single_reference,multi_reference,inline_repeater,LONG_DATA,NO_CHILD}
    public final static String field_data="DATA";
    public final static String field_single_reference="SINGLE";
    public final static String field_multi_reference="MULTI";
    public final static String field_inline_repeater="INLINE";
    public final static String field_LONG_DATA="LONG_DATA";
    public final static String field_NO_CHILD="NO_CHILD";

    public Date formatDate(String s) {
        try {
            return parseDate(s);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
    public Date formatDateTime(String s) {
        try {
            return parseDateTime(s);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
    public Date formatTime(String s) {
        try {
            return parseTime(s);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
    public Double formatDouble(String s) {
       try {
           if (s.equalsIgnoreCase("inf")){
               s="Infinity";
           }else if (s.equalsIgnoreCase("-inf")){
               s="-Infinity";
           }

           return Double.valueOf(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }
    public Integer formatInteger(String s) {
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Boolean formatBoolean(String s){
        if (s.equals("0") || s.equalsIgnoreCase("false")|| s.equalsIgnoreCase("f"))
        {
            return Boolean.FALSE;
        }else if (s.equals("1") || s.equalsIgnoreCase("true")|| s.equalsIgnoreCase("t"))
       {
            return Boolean.TRUE;
        }
        throw new IllegalArgumentException("Unable to translate '" + s + "' to a boolean value.");
    }
    
    public class FormatException extends Exception{
        public FormatException(String s)
        {
           super(s);
        }
        public FormatException(Exception s)
        {
            super(s);
        }
        public FormatException(String s,Exception e)
        {
            super(s,e);
        }
    }
    
    public class UnknownFieldException extends Exception{
        public UnknownFieldException(String s)
        {
            super(s);
        }
        public UnknownFieldException(Exception s)
        {
            super(s);
        }
        public UnknownFieldException(String s,Exception e)
        {
            super(s,e);
       }
    }
    
    public static Date parseDate(String s) throws ParseException
    {
        if (s.indexOf("'")!= -1)
        {
            s = ReplaceStr(s,"'","");
        }
        if (s==null)
        {
            return null;
        }else{
            try {
                return DateFormat.getInstance().parse(s);
            } catch (ParseException e) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                try {
                    return sdf.parse(s);
                } catch (ParseException e1) {
                    sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
                    try {
                        return sdf.parse(s);
                    } catch (ParseException e2) {
                        sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                        try {
                            return sdf.parse(s);
                        } catch (ParseException e3) {
                            sdf = new SimpleDateFormat("MM.dd.yyyy", Locale.US);
                            return sdf.parse(s);
                        }
                    }
                }
           }
        }
    }
    public static String ReplaceStr(String _base, String _old, String _new)
    {
        if (_base.indexOf(_old)==-1)
        {
            return _base;
        }else{
            StringBuffer sb = new StringBuffer();
                while(_base.indexOf(_old) != -1)
                {

                    String pre = _base.substring(0,_base.indexOf(_old));
                    String post;
                    try {
                        post = _base.substring(_base.indexOf(_old) + _old.length());
                    } catch (RuntimeException e) {
                        post = "";
                    }

                    sb.append(pre).append(_new);
                    _base = post;
                }
                sb.append(_base);

            return sb.toString();
        }
   }
     
    private static List<String> formatsDT=Arrays.asList(
    		"yyyy-MM-dd'T'HH:mm:ss.SSS".intern()
    		,"yyyy-MM-dd'T'HH:mm:ss".intern()
    		,"EEE MMM dd HH:mm:ss z yyyy".intern()
    		,"yyyy-MM-dd HH:mm:ss.S".intern()
    		,"yyyy-MM-dd HH:mm:ss".intern()
    		,"EEE MMM dd HH:mm:ss.S".intern()
    		,"yyyy-MM-dd HH:mm:ss z".intern()
    		,"EEE MMM dd HH:mm:ss".intern()
    		,"EEE MMM dd HH:mm:ss z".intern()
    		,"MM/dd/yyyy".intern()
    		,"yyyy-MM-dd".intern());
	
    public static Date parseDateTime(String s) throws ParseException
    {
        if (s.indexOf("'")!= -1)
        {
            s = ReplaceStr(s,"'","");
        }
        if (s==null)
        {
            return null;
        }else{
            try {
                return DateFormat.getInstance().parse(s);
            } catch (ParseException e) {
            	for(final String format:formatsDT){
            		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                    try {
                        return sdf.parse(s);
                    } catch (ParseException e1) {}
                }
            	
            	throw e;
            }
        }
    }

    public static Date parseTime(String s) throws ParseException
    {
        if (s.indexOf("'")!= -1)
        {
            s = ReplaceStr(s,"'","");
        }
        if (s==null)
        {
           return null;
        }else{
            try {
                return DateFormat.getInstance().parse(s);
            } catch (ParseException e) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
                try {
                    return sdf.parse(s);
                } catch (ParseException e1) {
                    sdf = new SimpleDateFormat("HH:mm:ss z", Locale.US);
                    return sdf.parse(s);
                }
            }
        }
    }
    public void setDataField(String xmlPath,String s) throws UnknownFieldException
    {
    	if(!"schemaLocation".equals(xmlPath))
        throw new UnknownFieldException(xmlPath);
    }
    public void setReferenceField(String xmlPath,BaseElement s) throws UnknownFieldException
    {
        throw new UnknownFieldException(xmlPath);
    }
    public String getFieldType(String xmlPath) throws UnknownFieldException
    {
        throw new UnknownFieldException(xmlPath);
    }

    public String getReferenceFieldName(String xmlPath) throws BaseElement.UnknownFieldException{
        throw new UnknownFieldException(xmlPath);
    }

    public Object getReferenceField(String xmlPath) throws BaseElement.UnknownFieldException{
        throw new UnknownFieldException(xmlPath);
    }

    public Object getDataFieldValue(String xmlPath) throws BaseElement.UnknownFieldException{
        throw new UnknownFieldException(xmlPath);
    }

    public ArrayList getAllFields(){
        return new ArrayList();
    }


    public String ValueParser(Object o,String type)
    {
        if (o.getClass().getName().equalsIgnoreCase("[B"))
        {
            byte[] b = (byte[]) o;
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            try {
                baos.write(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return baos.toString();
        }
            if (type.equalsIgnoreCase("string"))
            {
    		StringBuilder input = new StringBuilder((String) o);

    		for (int i = 0;i < input.length(); i++) {
    			if (input.charAt(i) == Byte.decode("0x00")){
    				input.setCharAt(i, ' ');
    			}
    		}

    		return StringEscapeUtils.escapeXml(input.toString());
            }else if (type.equalsIgnoreCase("boolean"))
            {
                if (o.toString().equalsIgnoreCase("true")|| o.toString().equalsIgnoreCase("1"))
                {
                    return "1";
                }else
                {
                    return "0";
                }
            }else if (type.equalsIgnoreCase("float"))
            {
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
                    return o.toString();
            }else if (type.equalsIgnoreCase("double"))
            {
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
                    return o.toString();
            }else if (type.equalsIgnoreCase("decimal"))
            {
                if (o.toString().equalsIgnoreCase("Infinity")){
                    return "INF";
                }else if (o.toString().equalsIgnoreCase("-Infinity")){
                    return "-INF";
                }else
                    return o.toString();
            }else if (type.equalsIgnoreCase("integer"))
            {
                return o.toString();
            }else if (type.equalsIgnoreCase("nonPositiveInteger"))
            {
                return o.toString();
            }else if (type.equalsIgnoreCase("negativeInteger"))
            {
                return o.toString();
            }else if (type.equalsIgnoreCase("long"))
            {
                return o.toString();
            }else if (type.equalsIgnoreCase("int"))
            {
                return o.toString();
            }else if (type.equalsIgnoreCase("short"))
            {
                return o.toString();
            }else if (type.equalsIgnoreCase("byte"))
            {
                return o.toString();
            }else if (type.equalsIgnoreCase("nonNegativeInteger"))
            {
                return o.toString();
            }else if (type.equalsIgnoreCase("unsignedLong"))
            {
                return o.toString();
            }else if (type.equalsIgnoreCase("unsignedInt"))
            {
                return o.toString();
            }else if (type.equalsIgnoreCase("unsignedShort"))
            {
                return o.toString();
            }else if (type.equalsIgnoreCase("unsignedByte"))
            {
                return o.toString();
            }else if (type.equalsIgnoreCase("positiveInteger"))
            {
                return o.toString();
            }else if (type.equalsIgnoreCase("time"))
            {
                if (o.getClass().getName().equalsIgnoreCase("java.util.Date"))
                {
                    java.util.Date d = (java.util.Date)o;
                    StringBuffer sb = new StringBuffer();
                    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("HH:mm:ss");
                    sb.append(formatter.format(d));
                    return sb.toString();
                }else if (o.getClass().getName().equalsIgnoreCase("java.sql.Date"))
                {
                    java.sql.Date d = (java.sql.Date)o;
                    StringBuffer sb = new StringBuffer();
                    sb.append(d.getHours());
                    sb.append(":");
                    sb.append(d.getMinutes());
                    sb.append(":");
                    sb.append(d.getSeconds());
                    return sb.toString();
                }else if (o.getClass().getName().equalsIgnoreCase("java.sql.Timestamp"))
                {
                    java.util.Date d = (java.util.Date)o;
                    StringBuffer sb = new StringBuffer();
                    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("HH:mm:ss");
                    sb.append(formatter.format(d));
                    return sb.toString();
                }
                return o.toString();
            }else if (type.equalsIgnoreCase("date"))
            {
                if (o instanceof String)
                {
                    try {
                        java.util.Date d = formatDate((String)o);
                        o=d;
                    } catch (IllegalArgumentException e) {
                    }
                }

                if (o instanceof java.util.Date)
                {
                    java.util.Date d = (java.util.Date)o;
                    StringBuffer sb = new StringBuffer();
                    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyy-MM-dd");
                    sb.append(formatter.format(d));
                    return sb.toString();
                }else if (o instanceof java.sql.Date)
                {
                    java.sql.Date d = (java.sql.Date)o;
                    StringBuffer sb = new StringBuffer();
                    sb.append(d.getYear());
                    sb.append("-");
                    sb.append(d.getMonth());
                    sb.append("-");
                    sb.append(d.getDate());
                    return sb.toString();
                }else if (o instanceof java.sql.Timestamp)
                {
                    java.util.Date d = (java.util.Date)o;
                    StringBuffer sb = new StringBuffer();
                    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyy-MM-dd");
                    sb.append(formatter.format(d));
                    return sb.toString();
                }
                return o.toString();
            }else if (type.equalsIgnoreCase("dateTime"))
            {
                if (o instanceof String)
                {
                    try {
                        java.util.Date d = formatDateTime((String)o);
                        o=d;
                    } catch (IllegalArgumentException e) {
                    }
                }

                if (o.getClass().getName().equalsIgnoreCase("java.util.Date"))
                {
                    java.util.Date d = (java.util.Date)o;
                    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    String s=formatter.format(d);
                    return s;
                }else if (o.getClass().getName().equalsIgnoreCase("java.sql.Date"))
                {
                    java.sql.Date d = (java.sql.Date)o;
                    StringBuffer sb = new StringBuffer();
                    sb.append(d.getYear());
                    sb.append("-");
                    sb.append(d.getMonth());
                    sb.append("-");
                    sb.append(d.getDate());
                    sb.append("T");
                    sb.append(d.getHours());
                    sb.append(":");
                    sb.append(d.getMinutes());
                    sb.append(":");
                    sb.append(d.getSeconds());
                    return sb.toString();
                }else if (o.getClass().getName().equalsIgnoreCase("java.sql.Timestamp"))
                {
                    java.util.Date d = (java.util.Date)o;
                    StringBuffer sb = new StringBuffer();
                    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
                    sb.append(formatter.format(d));
                    return sb.toString();
                }
                return o.toString();
            }else
            {
                return o.toString();
            }
    }

	
	public BaseElement copy(){
		try {
			BaseElement base2=this.getClass().newInstance();
			
			for(int i=0;i<this.getAllFields().size();i++){
				try {
					String path=(String)this.getAllFields().get(i);
					String ft=this.getFieldType(path);
					if(ft.equals(BaseElement.field_data) || ft.equals(BaseElement.field_LONG_DATA)){
						Object v1=this.getDataFieldValue(path);
						if(v1!=null && !(v1 instanceof Date)){
							base2.setDataField(path,v1.toString());
						}else if(v1!=null){
							base2.setDataField(path, (new java.text.SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS")).format((Date)v1));
						}
					}else{
						//reference
						Object o1=this.getReferenceField(path);
						
						if(o1 instanceof ArrayList){
							ArrayList<BaseElement> children1=(ArrayList<BaseElement>)o1;
								for(int j=0;j<children1.size();j++){
									base2.setReferenceField(path,children1.get(j).copy());
								}
						}else if(o1!=null){
							BaseElement child1=(BaseElement)o1;
							base2.setReferenceField(path,child1.copy());
						}
					}
				} catch (UnknownFieldException e) {
					e.printStackTrace();
				}
				
			}
			
			return base2;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return null;
    }

    protected void addXMLAtts(java.io.Writer writer) throws java.io.IOException{
    }


    protected boolean addXMLBody(java.io.Writer writer,int header) throws java.io.IOException{
        return false;
    }

    public abstract String getSchemaElementName();

    public abstract void toXML(java.io.Writer os, boolean prettyPrint) throws java.io.IOException;

    public String createHeader(int i){
        String header = "";
        int counter =0;
        while (counter++ <i)
        {
            header+=getHeaderString();
        }

        return header;
    }

    public String getHeaderString(){
        return "\t";
    }
    protected boolean hasXMLBodyContent(){
        return false;
    }
    protected TreeMap getXMLAtts() {
        return new TreeMap();
    }

    public abstract String getFullSchemaElementName();

    public String getXSIType(){
    	return this.getFullSchemaElementName();
}

    public void toXML(java.io.Writer os) throws java.lang.Exception{
    	this.toXML(os,false);
    }
}
