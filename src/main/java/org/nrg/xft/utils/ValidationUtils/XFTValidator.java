//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on May 21, 2004
 */
package org.nrg.xft.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.utils.DateUtils;
/**
 * Uses the specifications in the XFTSchemas to validate the content of an
 * XFTItem and its sub-items.
 *
 * <BR><BR>The Validate() method is used to verify that the data contained in a
 * XFTItem is valid.  The method returns a ValidationResults object which contains
 * the results of the validation.
 *
 * @author Tim
 */
public class XFTValidator {
	private static final String EMPTY = "";
	private static final String TRUE = "true";
	private static final String REQUIRED = "required";
	private static final String MIN_LENGTH = "minLength";
	private static final String MAX_LENGTH = "maxLength";
	static org.apache.log4j.Logger logger = Logger.getLogger(XFTValidator.class);

	/**
	 * Validates the content of this item and its sub-items using the specifications in
	 * the XFTSchema.  It validates for data types, max values, min values, regular expressions,
	 * comparisons, min length, max length and required.  The results of the validation are
	 * returned in a ValidationResults object.
	 * @param item
	 * @return
	 * @throws org.nrg.xft.exception.XFTInitException
	 * @throws ElementNotFoundException
	 */

	public static ValidationResults Validate(ItemI item) throws org.nrg.xft.exception.XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
	    return Validate(item,null);
	}

	private static ValidationResults Validate(ItemI item, String xmlPath) throws org.nrg.xft.exception.XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
	    if (xmlPath == null || xmlPath.equalsIgnoreCase(EMPTY))
	    {
	        xmlPath = item.getXSIType();
	    }
		ValidationResults vr = new ValidationResults();
		GenericWrapperElement element = GenericWrapperElement.GetElement(item.getXSIType());
		String prefix = element.getWrapped().getSchema().getXMLNS();
		Iterator fields = element.getRules().iterator();
		ArrayList checkd = new ArrayList();
		while(fields.hasNext())
		{
			Object [] field = (Object [])fields.next();
			GenericWrapperField vField = (GenericWrapperField)field[0];
			logger.debug(item.getXSIType() + " -> " + vField.getName());
			if (vField.isReference())
			{
				if (vField.isMultiple())
				{
					int counter = 0;
					Iterator children = item.getChildItems(vField).iterator();
					  while (children.hasNext())
					  {
						  Object o = children.next();

						if (o.getClass().getName().equalsIgnoreCase("org.nrg.xft.XFTItem"))
						{
							ValidationResults temp = Validate((XFTItem)o, vField.getXMLPathString(xmlPath) +"[" + counter + "]");
							checkd.add(vField.getId() + counter);
							Iterator iter = temp.getResultsIterator();
							while (iter.hasNext())
							{
								Object [] sub = (Object [])iter.next();
								vr.addResult((GenericWrapperField)sub[0],(String)sub[1],(String)sub[2],(String)sub[3]);
							}
						}
						counter++;
					}
				}else
				{
					if (item.getProperty(vField.getId()) != null)
					{
						if (item.getProperty(vField.getId()).getClass().getName().equalsIgnoreCase("org.nrg.xft.XFTItem"))
						{
							ValidationResults temp = Validate((XFTItem)item.getProperty(vField.getId()), vField.getXMLPathString(xmlPath));
							checkd.add(vField.getId());
							Iterator iter = temp.getResultsIterator();
							while (iter.hasNext())
							{
								Object [] sub = (Object [])iter.next();
								vr.addResult((GenericWrapperField)sub[0],(String)sub[1],(String)sub[2],(String)sub[3]);
							}
						}
					}

					//CHECK local SQL fields
					Iterator iter = vField.getLocalRefNames().iterator();
					while (iter.hasNext())
					{
						ArrayList refMapping = (ArrayList)iter.next();
						if (item.getProperty(refMapping.get(0).toString().toLowerCase()) != null)
						{
							Object value = item.getProperty(refMapping.get(0).toString().toLowerCase());
							if (! (value instanceof XFTItem))
							{
								String [] rule = {REQUIRED,"false",((GenericWrapperField)refMapping.get(1)).getXMLType().getFullLocalType()};
								ArrayList al = new ArrayList();
								al.add(rule);
								checkd.add(refMapping.get(0).toString().toLowerCase());
								vr = ValidateValue(value,al,prefix,vField,vr, vField.getXMLPathString(xmlPath),element);
							}
						}
					}
				}
			}else{
				Object value = item.getProperty(vField.getId());
				checkd.add(vField.getId());

				vr = ValidateValue(value,(ArrayList)field[1],prefix,vField,vr, vField.getXMLPathString(xmlPath),element);
			}
		}


		java.util.Enumeration keys = item.getProps().keys();
		while (keys.hasMoreElements())
		{
			String key = (String)keys.nextElement();
			if (! checkd.contains(key))
			{
				vr.addResult(null,"Unknown field:"+ item.getXSIType() + " -> " + key,EMPTY,element);
			}
		}

		return vr;
	}

	public static ValidationResults ValidateValue(Object value, ArrayList ruleArrayList, String prefix, GenericWrapperField vField, ValidationResults vr, String xmlPath,GenericWrapperElement element)
	{
		Iterator rules = ruleArrayList.iterator();
		boolean hasComparison = false;
		boolean meetsComparison = false;
		while (rules.hasNext())
		{
			String [] rule = (String[])rules.next();
			String type = rule[2];
			if (type.equalsIgnoreCase(prefix+":string"))
			{
				try{
					String temp = (String)value;
					if (temp == null)
					{
						temp = EMPTY;
					}
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (rule[0].equalsIgnoreCase(MAX_LENGTH))
					{
						int max = Integer.valueOf(rule[1]).intValue();
						if (temp.length() > max)
						{
							vr.addResult(vField,"Must Be Less Then " + max + " Characters : Current Length (" + temp.length() + ")", xmlPath,element);
						}
					}
					if (rule[0].equalsIgnoreCase(MIN_LENGTH))
					{
						int min = Integer.valueOf(rule[1]).intValue();
						if (temp.length() < min)
						{
							vr.addResult(vField,"Must Be More Then " + min + " Characters", xmlPath,element);
						}
					}
					if (rule[0].equalsIgnoreCase("mask"))
					{
						if (! temp.trim().equalsIgnoreCase(EMPTY))
						{
							String mask = rule[1];
							try{
							    Pattern pattern = Pattern.compile(mask);
							    Matcher matcher = pattern.matcher(temp);
								if (! matcher.find())
								{
									vr.addResult(vField,"Must Match the Regular Expression '" + mask + "'", xmlPath,element);
								}
							}catch(Exception e)
							{
//								RE pattern = new RE(mask);
//								if (! pattern.match(temp))
//								{
//									vr.addResult(vField,"Must Match the Regular Expression '" + mask + "'", xmlPath,element);
//								}
							}

						}
					}
					if (rule[0].equalsIgnoreCase("comparison") && ! temp.equalsIgnoreCase(EMPTY))
					{
						hasComparison = true;
						if (! meetsComparison)
						{
							if (temp.trim().equalsIgnoreCase(rule[1].trim()))
							{
								meetsComparison = true;
							}
						}
					}
				}catch(Exception ex)
				{
					vr.addResult(vField,"Must Be A Valid String", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":boolean"))
			{
				try{
					String temp = EMPTY;
					if (value == null)
					{
						temp = EMPTY;
					}else
					{
						temp = value.toString();
					}
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if ((temp.equalsIgnoreCase("0")) || (temp.equalsIgnoreCase("1")) || (temp.equalsIgnoreCase(TRUE)) || (temp.equalsIgnoreCase("false")) || (temp.equalsIgnoreCase(EMPTY)) || (temp.equalsIgnoreCase("NULL")))
					{
					}else
					{
						vr.addResult(vField,"Must Be A Valid Boolean", xmlPath,element);
					}
				}catch(Exception ex)
				{
					vr.addResult(vField,"Must Be A Valid Boolean", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":float"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Float num = Float.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Float.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Float.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                        vr.addResult(vField,"Must Be A Valid Float", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":double"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
                        Double num = Double.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Double.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Double.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                       vr.addResult(vField,"Must Be A Valid Double", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":decimal"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                        vr.addResult(vField,"Must Be A Valid Decimal", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":integer"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Integer num = Integer.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                        vr.addResult(vField,"Must Be A Valid Integer", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":gYear"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Integer num = Integer.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                        vr.addResult(vField,"Must Be A Valid Integer", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":nonPositiveInteger"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Integer num = Integer.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                        vr.addResult(vField,"Must Be A Valid Integer", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":negativeInteger"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Integer num = Integer.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                        vr.addResult(vField,"Must Be A Valid Integer", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":long"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Integer num = Integer.valueOf(temp);

						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                        vr.addResult(vField,"Must Be A Valid Integer", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":int"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Integer num = Integer.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                        vr.addResult(vField,"Must Be A Valid Integer", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":short"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Integer num = Integer.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                        vr.addResult(vField,"Must Be A Valid Integer", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":byte"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Byte num = Byte.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Byte.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Byte.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
					vr.addResult(vField,"Must Be A Valid Byte", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":nonNegativeInteger"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Integer num = Integer.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                        vr.addResult(vField,"Must Be A Valid Integer", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":unsignedLong"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Float num = Float.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Float.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Float.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                        vr.addResult(vField,"Must Be A Valid Float", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":unsignedInt"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Integer num = Integer.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                        vr.addResult(vField,"Must Be A Valid Integer", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":unsignedShort"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Integer num = Integer.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                        vr.addResult(vField,"Must Be A Valid Integer", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":unsignedByte"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Byte num = Byte.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Byte.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Byte.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
					vr.addResult(vField,"Must Be A Valid Byte", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":positiveInteger"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					if (!temp.equals(EMPTY))
					{
						Integer num = Integer.valueOf(temp);
						if (rule[0].equalsIgnoreCase(MAX_LENGTH))
						{
							int max = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() > max)
							{
								vr.addResult(vField,"Must Be Less Then " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Then " + min, xmlPath,element);
							}
						}
					}
				}catch(Exception ex)
				{
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
                        vr.addResult(vField,"Must Be A Valid Integer", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":time"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					
					if(!temp.equals(EMPTY))DateUtils.parseTime(temp);
				}catch(Exception ex)
				{
					vr.addResult(vField,"Must Be A Valid Date", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":date"))
			{
                String temp = EMPTY;
                if (value == null)
                {
                    temp = EMPTY;
                }else
                {
                    temp = value.toString();
                }
				try{
					
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					

					if(!temp.equals(EMPTY))DateUtils.parseDate(temp);
				}catch(Exception ex)
				{
					vr.addResult(vField,"Must Be A Valid Date", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":dateTime"))
			{
				try{
					String temp = EMPTY;
					if (value == null)
					{
						temp = EMPTY;
					}else
					{
						temp = value.toString();
					}
					if (rule[0].equalsIgnoreCase(REQUIRED))
					{
						if (rule[1].equalsIgnoreCase(TRUE))
						{
							if (temp.equals(EMPTY))
							{
								vr.addResult(vField,"Required Field", xmlPath,element);
								break;
							}
						}
					}
					
					if(!temp.equals(EMPTY))DateUtils.parseDateTime(temp);
				}catch(Exception ex)
				{
					vr.addResult(vField,"Must Be A Valid Date", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":IDREF"))
			{
				try{
					String temp = EMPTY;
					if (value == null)
					{
						temp = EMPTY;
					}else
					{
						temp = value.toString();

//						Iterator items = XMLReader.LOADED_ITEMS.iterator();
//						boolean foundMatch = false;
//						while (items.hasNext())
//						{
//							XFTItem item = (XFTItem)items.next();
//							if (item.getIDValue() != null)
//							{
//								if (item.getIDValue().equalsIgnoreCase(temp))
//								{
//									foundMatch = true;
//									break;
//								}
//							}
//						}
//
//						if (! foundMatch)
//						{
//							vr.addResult(vField,"IDREF must match an ID field in the document", xmlPath,element);
//						}
					}

				}catch(Exception ex)
				{
					logger.error(EMPTY,ex);
				}
			}
		}
		if (hasComparison && (!meetsComparison))
		{
			String message = "'" + value + "' Must Match Pre-Defined Values...(";
			int count = 0;
			Iterator comps = vField.getWrapped().getRule().getPossibleValues().iterator();
			while (comps.hasNext())
			{
				if ((count++)==0)
				{
					message +=comps.next().toString();
				}else
				{
					message +="," + comps.next().toString();
				}
			}
			vr.addResult(vField,message + ")", xmlPath,element);
		}
		return vr;
	}

	private String getFullMessage(GenericWrapperElement e, GenericWrapperField f)
	{
	    return "The content of element '" + e.getFullXMLName() + "' is not complete. '{\"" + e.getSchemaTargetNamespaceURI() + "\":" + f.getXMLName() +  "}'";
	}

	/**
	 * Gets the possible values for the specified field (sql_name) in the specified element.
	 * @param element
	 * @param field (sql_name)
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public static ArrayList GetPossibleValues(String element, String field) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		ArrayList al = new ArrayList();
		GenericWrapperElement p = GenericWrapperElement.GetElement(element);
		boolean foundField = false;
		if (p.getWrapped() != null)
		{
			Iterator fields = p.getRules().iterator();
			while(fields.hasNext())
			{
				Object [] f = (Object [])fields.next();
				GenericWrapperField vField = (GenericWrapperField)f[0];
				if (field.toLowerCase().equalsIgnoreCase(vField.getSQLName().toLowerCase()))
				{
					foundField = true;
					Iterator rules = ((ArrayList)f[1]).iterator();
					while (rules.hasNext())
					{
						String [] rule = (String[])rules.next();
						if (rule[0].equalsIgnoreCase("comparison"))
						{
							al.add(rule[1].trim());
						}
					}
					break;
				}
			}
		}

		if (al.size() == 0 && (!foundField))
		{
		    if (p.isExtension())
		    {
		        String e = p.getExtensionType().getFullForeignType();
		        al.addAll(XFTValidator.GetPossibleValues(e,field));
		    }else{
		        if (!foundField)
				{
					throw new FieldNotFoundException(element + "->" + field);
				}
		    }
		}

		al.trimToSize();
		return al;
	}




}

