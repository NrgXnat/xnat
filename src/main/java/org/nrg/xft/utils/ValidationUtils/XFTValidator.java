/*
 * core: org.nrg.xft.utils.ValidationUtils.XFTValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils.ValidationUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nrg.xft.utils.predicates.ProjectAccessPredicate.UNASSIGNED;

@Slf4j
public class XFTValidator {
	private static final String EMPTY = "";
	private static final String TRUE = "true";
	private static final String REQUIRED = "required";
	private static final String MIN_LENGTH = "minLength";
	private static final String MAX_LENGTH = "maxLength";

	/**
	 * Validates the content of this item and its sub-items using the specifications in
	 * the XFTSchema.  It validates for data types, max values, min values, regular expressions,
	 * comparisons, min length, max length and required.  The results of the validation are
	 * returned in a ValidationResults object.
	 * @param item
	 * @return Returns ValidationResults object
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */

	public static ValidationResults Validate(ItemI item) throws XFTInitException,ElementNotFoundException,FieldNotFoundException {
	    return Validate(item,null);
	}

	private static ValidationResults Validate(final ItemI item, final String requestedXmlPath) throws XFTInitException,ElementNotFoundException,FieldNotFoundException {
		final String xsiType = item.getXSIType();
		final String xmlPath = StringUtils.defaultIfBlank(requestedXmlPath, xsiType);

		final ValidationResults     results = new ValidationResults();
		final GenericWrapperElement element = GenericWrapperElement.GetElement(xsiType);

		if (((XFTItem) item).instanceOf("xnat:projectData") && !StringUtils.equals(item.getXSIType(), "xnat:projectData_alias")) {
			checkReservedPropertyValue(results, element, item, "xnat:projectData/ID", UNASSIGNED);
			checkReservedPropertyValue(results, element, item, "xnat:projectData/secondary_ID", UNASSIGNED);
			checkReservedPropertyValue(results, element, item, "xnat:projectData/name", UNASSIGNED);
		}

        final String       prefix  = element.getWrapped().getSchema().getXMLNS();
        final List<String> checked = new ArrayList<>();
		
        for (final Object ruleObject : element.getRules()) {
            final Object[]            rule    = (Object[]) ruleObject;
            final GenericWrapperField field   = (GenericWrapperField) rule[0];
            final String              fieldId = field.getId();

            log.debug("{} -> {} {}", xsiType, field.getName(), fieldId);

            if (field.isReference() && !(((XFTItem) item).instanceOf("xnat:projectData") && (StringUtils.startsWith(fieldId, "investigators_investigator_investigatordata"))))
			{
				if (field.isMultiple())
				{
					int counter = 0;
					for (final Object childItem : item.getChildItems(field)) {
						if (childItem.getClass().getName().equalsIgnoreCase("org.nrg.xft.XFTItem")) {
							ValidationResults temp = Validate((XFTItem)childItem, field.getXMLPathString(xmlPath) +"[" + counter + "]");
							checked.add(fieldId + counter);
							for (final Object subObject : temp.getResults()) {
								final Object[] sub = (Object[]) subObject;
                                results.addResult((GenericWrapperField) sub[0], (String) sub[1], (String) sub[2], (String) sub[3]);
							}
						}
						counter++;
					}
				}else {
					if (item.getProperty(fieldId) != null)
					{
						if (item.getProperty(fieldId).getClass().getName().equalsIgnoreCase("org.nrg.xft.XFTItem"))
						{
							ValidationResults temp = Validate((XFTItem)item.getProperty(fieldId), field.getXMLPathString(xmlPath));
							checked.add(fieldId);
							Iterator iter = temp.getResultsIterator();
							while (iter.hasNext())
							{
								Object[] sub = (Object[])iter.next();
								results.addResult((GenericWrapperField)sub[0],(String)sub[1],(String)sub[2],(String)sub[3]);
							}
						}
					}

					//CHECK local SQL fields
					Iterator iter = field.getLocalRefNames().iterator();
					while (iter.hasNext())
					{
						ArrayList refMapping = (ArrayList)iter.next();
						if (item.getProperty(refMapping.get(0).toString().toLowerCase()) != null)
						{
							Object value = item.getProperty(refMapping.get(0).toString().toLowerCase());
							if (! (value instanceof XFTItem))
							{
								checked.add(refMapping.get(0).toString().toLowerCase());
                                results.addResults(ValidateValue(value, Collections.singletonList(new String[]{REQUIRED, "false", ((GenericWrapperField) refMapping.get(1)).getXMLType().getFullLocalType()}), prefix, field, field.getXMLPathString(xmlPath), element));
							}
						}
					}
				}
			}else{
				final Object value = item.getProperty(fieldId);
				checked.add(fieldId);
				results.addResults(ValidateValue(value, (ArrayList) rule[1], prefix, field, field.getXMLPathString(xmlPath), element));
			}
		}


		for (final Object object : item.getProps().keySet()) {
			final String key = (String) object;
			if (!checked.contains(key) && !StringUtils.startsWith(key,"investigators_investigator_investigatordata")) {
				results.addResult(null, "Unknown field:" + xsiType + " -> " + key, EMPTY, element);
			}
		}

		return results;
	}

	private static void checkReservedPropertyValue(final ValidationResults results, final GenericWrapperElement element, final ItemI item, final String property, final String reservedValue) throws ElementNotFoundException, FieldNotFoundException, XFTInitException {
		final String actualValue = item.getStringProperty(property);
		if (StringUtils.equalsIgnoreCase(reservedValue, actualValue)) {
			results.addResult(element.getWrappedField(property),
							  "Can't use the reserved value '" + reservedValue + "' for the '" + property + "' property",
							  property,
							  "The value '" + reservedValue + "' is reserved for system usage and can't be assigned to an item of type '" + item.getXSIType() + "' in XNAT.");
		}
	}

	public static ValidationResults ValidateValue(Object value, List ruleArrayList, String prefix, GenericWrapperField vField, ValidationResults results, String xmlPath, GenericWrapperElement element) {
		final ValidationResults newResults = ValidateValue(value, ruleArrayList, prefix, vField, xmlPath, element);
		results.addResults(newResults);
		return results;
	}

	public static ValidationResults ValidateValue(Object value, List ruleArrayList, String prefix, GenericWrapperField vField, String xmlPath, GenericWrapperElement element) {
		final ValidationResults vr = new ValidationResults();

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
					
					String temp;
					if(value!=null && !(value instanceof String)){
						temp=value.toString();
					}else{
						temp = (String)value;
					}
					
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
							vr.addResult(vField,"Must Be Less Than " + max + " Characters : Current Length (" + temp.length() + ")", xmlPath,element);
						}
					}
					if (rule[0].equalsIgnoreCase(MIN_LENGTH))
					{
						int min = Integer.valueOf(rule[1]).intValue();
						if (temp.length() < min)
						{
							vr.addResult(vField,"Must Be More Than " + min + " Characters", xmlPath,element);
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Float.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Double.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
                final String temp = null == value ? null : value.toString();
                try {
                    if (rule[0].equalsIgnoreCase(REQUIRED)
                            && rule[1].equalsIgnoreCase(TRUE)
                            && StringUtils.isEmpty(temp)) {
                        vr.addResult(vField,"Required Field", xmlPath,element);
                        break;
                    }

                    if (StringUtils.isNotEmpty(temp)) {
                        final long num = Long.parseLong(temp);
                        // valid constraining facets:
                        // totalDigits
                        // fractionDigits
                        // pattern
                        // whiteSpace
                        // enumeration
                        // maxInclusive
                        // maxExclusive
                        // minInclusive
                        // minExclusive
                        if (rule[0].equalsIgnoreCase(MAX_LENGTH)) { // TODO: invalid constraining facet for xs:long
                            final long max = Long.parseLong(rule[1]);
                            if (num > max) {
                                vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
                            }
                        } else if (rule[0].equalsIgnoreCase(MIN_LENGTH)) {// TODO: invalid contraining facet for xs:long
                            long min = Long.parseLong(rule[1]);
                            if (num < min) {
                                vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
                            }
                        }
                    }
                } catch(Exception ex) {
                    if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL")) {
                        vr.addResult(vField,"Must Be A Valid Long Integer", xmlPath,element);
                    }
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Byte.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Float.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Byte.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
								vr.addResult(vField,"Must Be Less Than " + max, xmlPath,element);
							}
						}
						if (rule[0].equalsIgnoreCase(MIN_LENGTH))
						{
							int min = Integer.valueOf(rule[1]).intValue();
							if (num.intValue() < min)
							{
								vr.addResult(vField,"Must Be More Than " + min, xmlPath,element);
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
					if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
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
					if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
						vr.addResult(vField,"Must Be A Valid Date", xmlPath,element);
				}
			}else if (type.equalsIgnoreCase(prefix+":dateTime"))
			{
				String temp = EMPTY;
				try{
					
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
					if (!temp.equalsIgnoreCase("INF") && !temp.equalsIgnoreCase("NaN")&& !temp.equalsIgnoreCase("NULL"))
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
					log.error("An error occurred while validating a value",ex);
				}
			}
		}
		if (hasComparison && (!meetsComparison))
		{
			final StringBuilder message = new StringBuilder();
			message.append("'").append(value).append("' Must Match Pre-Defined Values...(");
			int           count   = 0;
			Iterator      comps   = vField.getWrapped().getRule().getPossibleValues().iterator();
			while (comps.hasNext())
			{
				if ((count++)==0)
				{
					message.append(comps.next().toString());
				}else
				{
					message.append(",").append(comps.next().toString());
				}
			}
			vr.addResult(vField,message + ")", xmlPath,element);
		}
		return vr;
	}

	/**
	 * Gets the possible values for the specified field (sql_name) in the specified element.
	 * @param element
	 * @param field (sql_name)
	 * @return Returns an ArrayList of possible values for the specified field in the specified element
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
			for (final Object object : p.getRules()) {
				final Object[] f = (Object[]) object;
				GenericWrapperField vField = (GenericWrapperField)f[0];
				if (field.toLowerCase().equalsIgnoreCase(vField.getSQLName().toLowerCase()))
				{
					foundField = true;
					for (final Object ruleObject : ((ArrayList)f[1]))
					{
						final String [] rule = (String[]) ruleObject;
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

		return al;
	}
}

