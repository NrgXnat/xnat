/*
 * core: org.nrg.xft.compare.ItemUniqueEquality
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.compare;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;

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
                            	final Object o = newI.getProperty(newI.getGenericSchemaElement().getFullXMLName() + XFT.PATH_SEPARATOR + (String)field.get(0));

                                if (o!= null)
                                {
                                	final Object o2 = oldI.getProperty(oldI.getGenericSchemaElement().getFullXMLName() + XFT.PATH_SEPARATOR + (String)field.get(0));
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
