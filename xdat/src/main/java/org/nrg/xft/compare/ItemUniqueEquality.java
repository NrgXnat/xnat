/*
 * core: org.nrg.xft.compare.ItemUniqueEquality
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.utils.SaveLaps;

/**
 * @author timo
 *
 */
public class ItemUniqueEquality extends ItemEqualityA implements ItemEqualityI {
	static org.apache.log4j.Logger logger = Logger.getLogger(ItemUniqueEquality.class);
	
	public ItemUniqueEquality(final boolean allowNewNull,final boolean checkExtensions){
		super(allowNewNull,checkExtensions);
	}
	
	public ItemUniqueEquality(final boolean allowNewNull){
		super(allowNewNull);
	}
	
	public ItemUniqueEquality(){
		super();
	}

	/**
	 * The values {@link #doCheck} compares, as strings: one key per unique field the item has a value for
	 * ("U|type|field|value") and one per unique composite all of whose fields it has values for
	 * ("C|type|group|value|value…"), each value formatted exactly as doCheck formats it. Two items of one type
	 * that doCheck would match share a key, so a collection can index its items by these keys and confirm the
	 * few candidates with doCheck instead of comparing every pair. The one exception is a field that cannot be
	 * read at all: doCheck logs that and matches on the remaining fields, which no key can express, so a
	 * caller that finds no candidate for a probe whose reads fail should fall back to comparing every item.
	 *
	 * @param item The item to derive the keys for.
	 *
	 * @return The keys, possibly empty.
	 */
	@SuppressWarnings("unchecked")
	public static List<String> uniqueKeys(final XFTItem item) throws XFTInitException, ElementNotFoundException, InvalidValueException {
		final GenericWrapperElement element = item.getGenericSchemaElement();
		final String                 type    = item.getXSIType().toLowerCase();
		final List<String>           keys    = new ArrayList<>();
		for (final GenericWrapperField key : (List<GenericWrapperField>) element.getUniqueFields()) {
			try {
				final Object o = item.getProperty(key.getXMLPathString(element.getFullXMLName()));
				if (o != null) {
					keys.add("U|" + type + "|" + key.getXMLPathString(element.getFullXMLName()) + "|" + parse(o, key));
				}
			} catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
				logger.error("", e);
			}
		}
		final Map<String, List<GenericWrapperField>> uHash = element.getUniqueCompositeFields();
		for (final Map.Entry<String, List<GenericWrapperField>> entry : uHash.entrySet()) {
			final StringBuilder sb       = new StringBuilder("C|").append(type).append('|').append(entry.getKey());
			boolean             complete = true;
			for (final GenericWrapperField key : entry.getValue()) {
				if (key.isReference()) {
					for (final List<Object> field : (List<List<Object>>) key.getLocalRefNames()) {
						try {
							final Object o = item.getProperty(element.getFullXMLName() + XFT.PATH_SEPARATOR + (String) field.getFirst());
							if (o == null) {
								complete = false;
								break;
							}
							sb.append('|').append(parse(o, ((GenericWrapperField) field.get(1)).getXMLType().getLocalType()));
						} catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
							logger.error("", e);   // doCheck skips a field it cannot read; so does the key
						}
					}
				} else {
					try {
						final Object o = item.getProperty(key.getXMLPathString(element.getFullXMLName()));
						if (o == null) {
							complete = false;
						} else {
							sb.append('|').append(parse(o, key.getXMLPathString(element.getFullXMLName())));
						}
					} catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
						logger.error("", e);
					}
				}
				if (!complete) {
					break;
				}
			}
			if (complete) {
				keys.add(sb.toString());
			}
		}
		return keys;
	}

	// The two ValueParser forms doCheck uses, timed so the save's lap line can say what the keys cost.
	private static String parse(final Object o, final GenericWrapperField field) throws XFTInitException, InvalidValueException {
		final long started = SaveLaps.start();
		try {
			return DBAction.ValueParser(o, field, true);
		} finally {
			SaveLaps.add(SaveLaps.Lap.VALUE_PARSE, started);
		}
	}

	private static String parse(final Object o, final String type) throws InvalidValueException {
		final long started = SaveLaps.start();
		try {
			return DBAction.ValueParser(o, type, true);
		} finally {
			SaveLaps.add(SaveLaps.Lap.VALUE_PARSE, started);
		}
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.compare.ItemEqualityA#doCheck(org.nrg.xft.XFTItem, org.nrg.xft.XFTItem)
	 */
	public boolean doCheck(final XFTItem newI, final XFTItem oldI) throws XFTInitException, ElementNotFoundException, FieldNotFoundException,Exception{
		@SuppressWarnings("unchecked")
		final List<GenericWrapperField> ufields= newI.getGenericSchemaElement().getUniqueFields();
        for(final GenericWrapperField key:ufields)
        {
            try {
               final Object o = newI.getProperty(key.getXMLPathString(newI.getGenericSchemaElement().getFullXMLName()));
                if (o!= null)
                {
                    final Object o2 = oldI.getProperty(key.getXMLPathString(oldI.getGenericSchemaElement().getFullXMLName()));
                    if (o2!= null)
                    {
                        final Object format1 = DBAction.ValueParser(o,key,true);
                        final Object format2 = DBAction.ValueParser(o2,key,true);
                        
                        if (format1.equals(format2))
                        {
                            return true;
                        }
                    }
                }
            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            } catch (FieldNotFoundException e) {
                logger.error("",e);
            }
        }

        @SuppressWarnings("unchecked")
		final Map<String,List<GenericWrapperField>> uHash = newI.getGenericSchemaElement().getUniqueCompositeFields();
        if (uHash.size() > 0)
        {
        	for(Map.Entry<String, List<GenericWrapperField>> entry:uHash.entrySet()){
        		final List<GenericWrapperField> uniqueComposites = entry.getValue();

                boolean matchAll = true;
                for (final GenericWrapperField key:uniqueComposites)
                {
                    if (key.isReference())
                    {
                        @SuppressWarnings("unchecked")
                        final List<List<Object>> fields=key.getLocalRefNames();
                        for (final List<Object> field:fields)
                        {
                            try {
                            	final Object o = newI.getProperty(newI.getGenericSchemaElement().getFullXMLName() + XFT.PATH_SEPARATOR + (String)field.getFirst());

                                if (o!= null)
                                {
                                	final Object o2 = oldI.getProperty(oldI.getGenericSchemaElement().getFullXMLName() + XFT.PATH_SEPARATOR + (String)field.getFirst());
                                    if (o2!= null)
                                    {
                                    	final Object format1 = DBAction.ValueParser(o,((GenericWrapperField)field.get(1)).getXMLType().getLocalType(),true);
                                    	final Object format2 = DBAction.ValueParser(o2,((GenericWrapperField)field.get(1)).getXMLType().getLocalType(),true);
                                        if (! format1.equals(format2))
                                        {
                                            matchAll = false;
                                            break;
                                        }
                                    }else{
                                        matchAll = false;
                                        break;
                                    }
                                }else{
                                    matchAll = false;
                                    break;
                                }
                            } catch (XFTInitException e) {
                                logger.error("",e);
                            } catch (ElementNotFoundException e) {
                                logger.error("",e);
                            } catch (FieldNotFoundException e) {
                                logger.error("",e);
                            }
                        }
                    }else{
                        try {
                            Object o = newI.getProperty(key.getXMLPathString(newI.getGenericSchemaElement().getFullXMLName()));

                            if (o!= null)
                            {
                                Object o2 = oldI.getProperty(key.getXMLPathString(oldI.getGenericSchemaElement().getFullXMLName()));
                                if (o2!= null)
                                {
                                    Object format1 = DBAction.ValueParser(o,key.getXMLPathString(newI.getGenericSchemaElement().getFullXMLName()),true);
                                    Object format2 = DBAction.ValueParser(o2,key.getXMLPathString(oldI.getGenericSchemaElement().getFullXMLName()),true);
                                    if (! format1.equals(format2))
                                    {
                                        matchAll = false;
                                        break;
                                    }
                                }else{
                                    matchAll = false;
                                    break;
                                }
                            }else{
                                matchAll = false;
                                break;
                            }
                        } catch (XFTInitException e) {
                            logger.error("",e);
                        } catch (ElementNotFoundException e) {
                            logger.error("",e);
                        } catch (FieldNotFoundException e) {
                            logger.error("",e);
                        }
                    }
                }


                if (matchAll)
                {
                    return true;
                }
        	}
        }

        if (checkExtensions){
            //CHECK EXTENDED ITEM
            //ADDED 9/26 when adding support for multiple references to 'no field' elements (abstract)
            if (newI.getGenericSchemaElement().isExtension()){
            	final XFTItem child1 = newI.getExtensionItem();
            	final XFTItem child2 = oldI.getExtensionItem();
                return this.isEqualTo(child1,child2);
            }
        }
        
        return false;
	}
}
