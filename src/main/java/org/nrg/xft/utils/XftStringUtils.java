/*
 * core: org.nrg.xft.utils.XftStringUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.utils;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
public class XftStringUtils {

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
	 	String header = StringUtils.replace(method, "get", "");

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
		String header = StringUtils.replace(method,"get","");

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

		name = StringUtils.replace(name, ".", "_");
		name = StringUtils.replace(name, ":", "_");

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

		name = StringUtils.replace(name, ":", "_");
		name = StringUtils.replace(name, "-", "_");

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


		name = sb.toString().replace(XFT.PATH_SEPARATOR, '_');
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
		temp = StringUtils.replace(temp, "-", "_");
		temp = StringUtils.replace(temp, " ", "_");
		temp = StringUtils.replace(temp, ":", "_");
		return temp;
	}

    public static String CleanForSQLTableName(String temp)
    {
        temp = StringUtils.replace(temp, "-", "_");
        temp = StringUtils.replace(temp, " ", "_");
        temp = StringUtils.replace(temp, ":", "_");

        if (temp.length()>64)
        {
            temp = temp.substring(0,63);
        }
        return temp;
    }

	public static String CleanForSQLValue(String temp)
	{
		temp = StringUtils.replace(temp, "''", "#3939#");
		temp = StringUtils.replace(temp, "\'", "#\39#");
		temp = StringUtils.replace(temp, "'", "#39#");
		temp = StringUtils.replace(temp, "#39#", "''");
		temp = StringUtils.replace(temp, "#\39#", "''");
		temp = StringUtils.replace(temp, "#3939#", "''");

		//DOUBLE \
		temp = StringUtils.replace(temp, "\\", "#39#39#");
		temp = StringUtils.replace(temp, "#39#39#", "\\\\");
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
	    s = StringUtils.replace(s, ".", "*$*");
	    return StringUtils.replace(s, "*$*", "." + insert);
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
		fullString = StringUtils.replaceChars(fullString, '.', XFT.PATH_SEPARATOR);
        fullString = StringUtils.replace(fullString, "[@", "[*");
        fullString = StringUtils.replaceChars(fullString, '@', XFT.PATH_SEPARATOR);
        fullString = StringUtils.replace(fullString, "[*", "[@");

        while (fullString.startsWith(String.valueOf(XFT.PATH_SEPARATOR)))
        {
            fullString = fullString.substring(1);
        }

        return fullString;
	}

	public static boolean isValidId(String s) {
		return !StringUtils.isBlank(s) && REGEX_VALID_ID.matcher(s).matches();
	}
	
	public static boolean OccursBefore(String root,String f, String l) {
		return !(root == null || f == null || l == null) && !(!root.contains(f) || !root.contains(l)) && root.indexOf(f) < root.indexOf(l);
	}
	
	public static String intern(String s){
		return (s!=null)?s.intern():s;
	}

	private static final Pattern REGEX_VALID_ID = Pattern.compile("^[A-z0-9_-]+$");
}

