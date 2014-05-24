//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Nov 10, 2003
 */
package org.nrg.xft.utils;
import java.io.File;
import java.util.ArrayList;

import org.nrg.xft.XFT;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
/**
 * A collection of methods for easy manipulation of Strings.
 *
 * @author Tim
 *
 */
public class StringUtils {

    public static boolean IsEmpty(String s)
    {
        if (s== null || s.equalsIgnoreCase(""))
        {
            return true;
        }else{
            return false;
        }
    }

    public static boolean HasContent(String s)
    {
        return !IsEmpty(s);
    }

	/**
	 * Replaces all instances of the Old string in the Base String with the New String.
	 * @param _base Original string
	 * @param _old String to be replaced
	 * @param _new String to be added.
	 * @return
	 */
	public static String ReplaceStr(String _base, String _old, String _new)
	{
	    if (_base.indexOf(_old)==-1)
	    {
	        return _base;
	    }else{
			StringBuffer sb = new StringBuffer();
//			if (_base != null)
//			{
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
//			}

			return sb.toString();
	    }
	}

	public static String WhiteSpace(int i)
	{
		StringBuffer sb = new StringBuffer();
		while (i > 0)
		{
			sb.append(" ");
			i--;
		}
		return sb.toString();
	}

	public static String RemoveChar(String _base, char _old)
	{
	    while (_base.indexOf(_old) !=-1)
	    {
	        int index =_base.indexOf(_old);
	        if (index==0)
	        {
	            _base = _base.substring(1);
	        }else if (index== (_base.length()-1)) {
	            _base = _base.substring(0,index);
	        }else{
	            String pre = _base.substring(0,index);
	            _base = pre + _base.substring(index+1);
	        }
	    }

	    return _base;
	}

	public static String ReplaceStr(String _base, char _old, char _new)
	{
	    return _base.replace(_old,_new);
	}

	/**
	 * Returns a substring of all characters after the last File.seperator.
	 * @param path
	 * @return
	 */
	public static String GetFileName(String path)
	{
		while (path.indexOf(File.separator) != -1)
		{
			path = path.substring(path.lastIndexOf(File.separator) + 1);
		}
		return path;
	}

	/**
	 * Returns a substring with all characters before the last File.seperator.
	 * @param path
	 * @return
	 */
	public static String GetDirName(String path)
	{
		path = path.substring(0,path.lastIndexOf(File.separator));
		return path;
	}

	/**
	 * Reformats a Java Method Name into an acceptable header.  It removes 'get', makes
	 * the first letter capitalized, and adds spaces before other capitalized letters.
	 * @param method
	 * @return
	 */
	public static String FormatMethodNameToHeader(String method)
	{
	 	String header = ReplaceStr(method,"get","");

	 	StringBuffer sb = new StringBuffer("");

	 	int begin = 0;
	 	int end = header.toCharArray().length - 1;

	 	for(int i=0; i< header.toCharArray().length;i++)
	 	{
	 		if (i == end)
	 		{
	 			sb.append(header.substring(begin));
	 		}else
	 		{
		 		if (i !=begin)
		 		{
			 		if (Character.isUpperCase(header.charAt(i)))
			 		{
			 			sb.append(header.substring(begin,(i))).append(" ");
			 			begin = i;
			 		}
		 		}
	 		}
	 	}

	 	return sb.toString();
	}

	/**
	 * removes 'get' and un-capitalizes every character and inserts a '_'.
	 * @param method
	 * @return
	 */
	public static String FormatMethodNameToSQL(String method)
	{
		String header = ReplaceStr(method,"get","");

		StringBuffer sb = new StringBuffer("");

		int begin = 0;
		int end = header.toCharArray().length - 1;

		for(int i=0; i< header.toCharArray().length;i++)
		{
			if (i == end)
			{
				sb.append(header.substring(begin));
			}else
			{
				if (i !=begin)
				{
					if (Character.isUpperCase(header.charAt(i)))
					{
						sb.append(header.substring(begin,(i))).append("_");
						begin = i;
					}
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Removes '_' characters and capitalizes the next character, and inserts the prefix before it.
	 * @param prefix
	 * @param name
	 * @return
	 */
	public static String FormatStringToMethodName(String prefix,String name)
	{
		StringBuffer sb = new StringBuffer(prefix);

		String first = name.substring(0,1);
		sb.append(first.toUpperCase());
		name = name.substring(1).toLowerCase();
		while(name.indexOf("_") != -1)
		{
			int i=name.indexOf("_");
			if (i+2>name.length())
			{
				break;
			}else if (i != 0)
			{
				sb.append(name.substring(0,i));
				sb.append(name.substring(i+1,i+2).toUpperCase());
				name = name.substring(i+2);
			}else
			{
				name = name.substring(1);
			}
		}
		sb.append(name);
		return sb.toString();
	}

	/**
	 * Removes '_' characters and capitalizes the next character.
	 * @param name
	 * @return
	 */
	public static String FormatStringToClassName(String name)
	{
		StringBuffer sb = new StringBuffer();

		name = StringUtils.ReplaceStr(name,".","_");
		name = StringUtils.ReplaceStr(name,":","_");

		String first = name.substring(0,1);
		sb.append(first.toUpperCase());
		name = name.substring(1).toLowerCase();

		while(name.indexOf("_") != -1)
		{
			int i=name.indexOf("_");
			if (i+2>name.length())
			{
				break;
			}else if (i != 0)
			{
				sb.append(name.substring(0,i));
				sb.append(name.substring(i+1,i+2).toUpperCase());
				name = name.substring(i+2);
			}else
			{
				name = name.substring(1);
			}
		}
		sb.append(name);
		return sb.toString();
	}

	/**
	 * Removes '_' characters and capitalizes the next character.
	 * @param name
	 * @return
	 */
	public static String FormatStringToMethodSignature(String name)
	{
		StringBuffer sb = new StringBuffer();

		name = StringUtils.ReplaceStr(name,":","_");
		name = StringUtils.ReplaceStr(name,"-","_");

		String first = name.substring(0,1);
		sb.append(first.toUpperCase());
		name = name.substring(1).toLowerCase();

		while(name.indexOf("_") != -1)
		{
			if (name.indexOf("_") != 0)
			{
				sb.append(name.substring(0,name.indexOf("_")));
				sb.append(name.substring(name.indexOf("_")+1,name.indexOf("_")+2).toUpperCase());
				name = name.substring(name.indexOf("_")+2);
			}else
			{
				name = name.substring(1);
			}
		}

		sb.append(name);


		name = sb.toString().replace(XFT.PATH_SEPERATOR,'_');
		return name;
	}


	/**
	 * Returns just the class name (without its package).
	 * @param c
	 * @return
	 */
	public static String getLocalClassName(Class c)
	{
		String className = c.getName();
		if(className.indexOf(".") != -1)
		{
			className = className.substring(className.lastIndexOf(".") + 1,className.length());
		}
		return className;
	}

	/**
	 * Capitalizes the first character of the string.
	 * @param name
	 * @return
	 */
	public static String CapitalFirstLetter(String name)
	{
		StringBuffer sb = new StringBuffer();

		String first = name.substring(0,1);
		sb.append(first.toUpperCase());
		sb.append(name.substring(1));

		return sb.toString();
	}


	public static String MinCharsAbbr(String name)
	{
		return FirstNChars(name,2);
	}

	public static String RegCharsAbbr(String name)
	{
		return FirstNChars(name,6);
	}

	public static String MaxCharsAbbr(String name)
	{
		return FirstNChars(name,10);
	}

	public static String SQLMaxCharsAbbr(String name)
	{
		return FirstNChars(name,62);
	}

	public static String SQLMaxCharsAbbr(String tableName,String colName)
	{
		return FirstNChars(tableName,31) + "_" + FirstNChars(colName,31);
	}

	public static String SQLSequenceFormat1(String table,String keyName)
	{
	    String temp = table + "_" + keyName + "_seq";
		if (temp.length() > 63)
		{
			if (keyName.length() > 30)
			{
				if (table.length()> 30)
				{
					table = table.substring(0,30);
				}
				if (keyName.length() > 30)
				{
					keyName = keyName.substring(0,30);
				}

				return table + "_" + keyName + "_seq";
			}else{
				int colLength = keyName.length() + 5;

				int tableLength = 63- colLength;
				if (table.length()> tableLength)
				{
					table = table.substring(0,tableLength);
				}
//					if (keyName.length() > 29)
//					{
//						keyName = keyName.substring(0,29);
//					}

				return table + "_" + keyName + "_seq";
			}
		}else{
			return temp;
		}
	}

	public static String SQLSequenceFormat2(String table,String keyName)
	{
	    String temp = table + "_" + keyName + "_seq";
		if (temp.length() > 63)
		{
			if (keyName.length() > 29)
			{
				if (table.length()> 29)
				{
					table = table.substring(0,29);
				}
				if (keyName.length() > 29)
				{
					keyName = keyName.substring(0,29);
				}

				return table + "_" + keyName + "_seq";
			}else{
				int colLength = keyName.length() + 5;

				int tableLength = 63- colLength;
				if (table.length()> tableLength)
				{
					table = table.substring(0,tableLength);
				}
//					if (keyName.length() > 29)
//					{
//						keyName = keyName.substring(0,29);
//					}

				return table + "_" + keyName + "_seq";
			}
		}else{
			return temp;
		}
	}

	public static String FirstNChars(String name, int num)
	{
		if (name.length() > num)
		{
			return name.substring(0,num);
		}else
		{
			return name;
		}
	}

	/**
	 * Translates the comma-delimited string to an ArrayList of strings.
	 * @param s
	 * @return
	 */
	public static ArrayList<String> CommaDelimitedStringToArrayList(String s)
	{
		return DelimitedStringToArrayList(s,",");
	}

	/**
	 * Translates the comma-delimited string to an ArrayList of strings.
	 * @param s
	 * @return
	 */
	public static ArrayList<String> DelimitedStringToArrayList(String s, String delimiter)
	{
		if(s==null)return new ArrayList<String>();
		if(s.trim().equals(""))return new ArrayList<String>();
		
		ArrayList<String> al = new ArrayList<String>();

		while(s.indexOf(delimiter) != -1)
		{
			al.add(s.substring(0,s.indexOf(delimiter)));
			s = s.substring(s.indexOf(delimiter) + 1);
		}

		al.add(s);

		return al;
	}

    /**
     * Translates the comma-delimited string to an ArrayList of strings.
     * @param s
     * @return
     */
    public static ArrayList<String> CommaDelimitedStringToArrayList(String s,boolean trim)
    {
        return DelimitedStringToArrayList(s,",",trim);
    }

    /**
     * Translates the comma-delimited string to an ArrayList of strings.
     * @param s
     * @return
     */
    public static ArrayList<String> DelimitedStringToArrayList(String s, String delimiter,boolean trim)
    {
        ArrayList<String> al = new ArrayList<String>();

        while(s.indexOf(delimiter) != -1)
        {
            al.add(s.substring(0,s.indexOf(delimiter)).trim());
            s = s.substring(s.indexOf(delimiter) + 1);
        }

        if (s.length() > 0)
        {
            if (trim){
                al.add(s.trim());
            }else{
                al.add(s);
            }
        }

        return al;
    }

	public static String CleanForSQL(String temp)
	{
		temp = StringUtils.ReplaceStr(temp,"-","_");
		temp = StringUtils.ReplaceStr(temp," ","_");
		temp = StringUtils.ReplaceStr(temp,":","_");
		return temp;
	}

    public static String CleanForSQLTableName(String temp)
    {
        temp = StringUtils.ReplaceStr(temp,"-","_");
        temp = StringUtils.ReplaceStr(temp," ","_");
        temp = StringUtils.ReplaceStr(temp,":","_");

        if (temp.length()>64)
        {
            temp = temp.substring(0,63);
        }
        return temp;
    }



	public static String CleanForSQLValue(String temp)
	{
		temp = StringUtils.ReplaceStr(temp,"''","#3939#");
		temp = StringUtils.ReplaceStr(temp,"\'","#\39#");
		temp = StringUtils.ReplaceStr(temp,"'","#39#");
		temp = StringUtils.ReplaceStr(temp,"#39#","''");
		temp = StringUtils.ReplaceStr(temp,"#\39#","''");
		temp = StringUtils.ReplaceStr(temp,"#3939#","''");

		//DOUBLE \
		temp = StringUtils.ReplaceStr(temp,"\\","#39#39#");
		temp = StringUtils.ReplaceStr(temp,"#39#39#","\\\\");
		return temp;
	}

	public static int GetEndingInt(String next)
	{
		int index = next.length()-1;
		while (Character.isDigit(next.charAt(index)))
		{
			index--;
		}

		String s = next.substring(index+1);
		Integer i = new Integer(s);
		return i.intValue();
	}

	public static boolean EndsWithInt(String next)
	{
		int index = next.length()-1;
		if (Character.isDigit(next.charAt(index)))
		{
			return true;
		}else{
		    return false;
		}
	}


	public static String CleanEndingInt(String next)
	{
		int index = next.length()-1;
		while (Character.isDigit(next.charAt(index)))
		{
			index--;
		}
		return next.substring(0,index+1);
	}

	public static String InsertCharsIntoDelimitedString(String s, String insert)
	{
	    s = StringUtils.ReplaceStr(s,".","*$*");
	    return StringUtils.ReplaceStr(s,"*$*","." + insert);
	}

	public static  String ToString(boolean b)
	{
	    if (b)
	    {
	        return "true";
	    }else{
	        return "false";
	    }
	}

	public static String Last62Chars(String s)
	{
	    if (s.length()>62)
	    {
	        int length = s.length();
	        int index = length -62;
	        return s.substring(index);
	    }else{
	        return s;
	    }
	}

	public static String Last30Chars(String s)
	{
	    if (s.length()>30)
	    {
	        int length = s.length();
	        int index = length -30;
	        return s.substring(index);
	    }else{
	        return s;
	    }
	}

	public static String CreateAlias(String tableName,String colName)
	{
	    String temp = tableName +"_" + colName;

	    if (temp.length() > 62)
		{
			String table = tableName;
			String keyName = colName;
			if (keyName.length() > 30)
			{
				if (tableName.length()> 30)
				{
					table = table.substring(0,30);
				}
				if (keyName.length() > 30)
				{
					keyName = Last30Chars(keyName);
				}

				return table + "_" + keyName;
			}else{
				int colLength = keyName.length();

				int tableLength = 62- colLength;
				if (table.length()> tableLength)
				{
					table = table.substring(0,tableLength);
				}

				return table + "_" + keyName;
			}
		}else{
			return temp;
		}
	}

	public static int CountStringOccurrences(String fullString,String searchFor)
	{
	    int counter =0;
	    String temp = fullString.toString();

	    while (temp.indexOf(searchFor)!=-1)
	    {
	        counter++;
	        temp = temp.substring(temp.indexOf(searchFor)+1);
	    }

	    return counter;
	}


	public static String GetRootElementName(String fullString)
	{
	    if (fullString.indexOf(".")!=-1)
	    {
	        return fullString.substring(0,fullString.indexOf("."));
	    }else if (fullString.indexOf("/")!=-1)
	    {
	        return fullString.substring(0,fullString.indexOf("/"));
	    }else if (fullString.indexOf("@")!=-1)
	    {
	        return fullString.substring(0,fullString.indexOf("@"));
	    }

	    return fullString;
	}

	public static String GetFieldText(String fullString)
	{
	    if (fullString.indexOf(".")!=-1)
	    {
	        return fullString.substring(fullString.indexOf(".") + 1);
	    }else if (fullString.indexOf("/")!=-1)
	    {
	        return fullString.substring(fullString.indexOf("/") + 1);
	    }

	    return fullString;
	}


	public static GenericWrapperElement GetRootElement(String fullString) throws org.nrg.xft.exception.ElementNotFoundException
	{
	    try {
            String rootElementName = GetRootElementName(fullString);
            return GenericWrapperElement.GetElement(rootElementName);
        } catch (XFTInitException e) {
            return null;
        }
	}

	public static String StandardizeXMLPath(String fullString)
	{
	    if (XFT.PATH_SEPERATOR=='.')
	    {
	        fullString = StringUtils.ReplaceStr(fullString,'/',XFT.PATH_SEPERATOR);
	    }else{
	        fullString = StringUtils.ReplaceStr(fullString,'.',XFT.PATH_SEPERATOR);
	    }
        fullString = StringUtils.ReplaceStr(fullString,"[@","[*");
        fullString = StringUtils.ReplaceStr(fullString,'@',XFT.PATH_SEPERATOR);
        fullString = StringUtils.ReplaceStr(fullString,"[*","[@");

        while (fullString.startsWith(String.valueOf(XFT.PATH_SEPERATOR)))
        {
            fullString = fullString.substring(1);
        }

        return fullString;
	}

	
	public static boolean IsAlphaNumericUnderscore(String s){
		if(s==null)return false;
		if(s.indexOf(',')>-1) return false;
		if(s.indexOf('.')>-1) return false;
		if(s.indexOf('/')>-1) return false;
		if(s.indexOf('`')>-1) return false;
		if(s.indexOf('`')>-1) return false;
		if(s.indexOf('~')>-1) return false;
		if(s.indexOf('!')>-1) return false;
		if(s.indexOf('@')>-1) return false;
		if(s.indexOf('#')>-1) return false;
		if(s.indexOf('$')>-1) return false;
		if(s.indexOf('%')>-1) return false;
		if(s.indexOf('^')>-1) return false;
		if(s.indexOf('&')>-1) return false;
		if(s.indexOf('*')>-1) return false;
		if(s.indexOf('(')>-1) return false;
		if(s.indexOf(')')>-1) return false;
		if(s.indexOf('+')>-1) return false;
		if(s.indexOf('=')>-1) return false;
		if(s.indexOf('|')>-1) return false;
		if(s.indexOf('\\')>-1) return false;
		if(s.indexOf('{')>-1) return false;
		if(s.indexOf('[')>-1) return false;
		if(s.indexOf('}')>-1) return false;
		if(s.indexOf(']')>-1) return false;
		if(s.indexOf(':')>-1) return false;
		if(s.indexOf(';')>-1) return false;
		if(s.indexOf('"')>-1) return false;
		if(s.indexOf('\'')>-1) return false;
		if(s.indexOf('<')>-1) return false;
		if(s.indexOf('>')>-1) return false;
		if(s.indexOf('?')>-1) return false;
		
		return true;
	}
	
	public static boolean OccursBefore(String root,String f, String l){
		if(root==null || f==null || l==null)
		{
			return false;
		}
		
		if(root.indexOf(f)==-1 || root.indexOf(l)==-1){
			return false;
		}
		
		if(root.indexOf(f)< root.indexOf(l)){
			return true;
		}
		
		return false;
	}
	
	public static String intern(String s){
		return (s!=null)?s.intern():s;
	}
}

