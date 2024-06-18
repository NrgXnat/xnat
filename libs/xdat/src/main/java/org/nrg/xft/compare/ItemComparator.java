/*
 * core: org.nrg.xft.compare.ItemComparator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.compare;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
@Slf4j
public class ItemComparator implements Comparator<XFTItem>, Serializable {
	public static final ItemComparator DEFAULT_ITEM_COMPARATOR = new ItemComparator();

	private static final long serialVersionUID = 1019155701721799668L;

	@SuppressWarnings("unchecked")
	public int compare(XFTItem oldTI, XFTItem newTI) {
		try{
			XFTItem oldI=(XFTItem)oldTI.clone(true);
			XFTItem newI=(XFTItem)newTI.clone(true);

			for (final GenericWrapperField key : GenericUtils.convertToTypedList(newI.getGenericSchemaElement().getUniqueFields(), GenericWrapperField.class)) {
	            try {
	               final Comparable o = (Comparable)newI.getProperty(key.getXMLPathString(newI.getGenericSchemaElement().getFullXMLName()));
	                if (o!= null)
	                {
	                    final Comparable o2 = (Comparable)oldI.getProperty(key.getXMLPathString(oldI.getGenericSchemaElement().getFullXMLName()));
	                    if (o2!= null)
	                    {
	                        return o.compareTo(o2);
	                    }
	                }
	            } catch (XFTInitException e) {
	                log.error(XDAT.XFT_INIT_EXCEPTION_MESSAGE, e);
	            } catch (ElementNotFoundException e) {
					log.error(XDAT.ELEMENT_NOT_FOUND_MESSAGE, e.ELEMENT, e);
	            } catch (FieldNotFoundException e) {
					log.error(XDAT.FIELD_NOT_FOUND_MESSAGE, e.FIELD, e);
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
	                            	final Comparable o = (Comparable) newI.getProperty(newI.getGenericSchemaElement().getFullXMLName() + XFT.PATH_SEPARATOR + field.get(0));

	                                if (o!= null)
	                                {
	                                	final Comparable o2 = (Comparable) oldI.getProperty(oldI.getGenericSchemaElement().getFullXMLName() + XFT.PATH_SEPARATOR + field.get(0));
	                                    if (o2!= null)
	                                    {
	                                        int compare=o.compareTo(o2);
	                                        if(compare!=0){
	                                        	return compare;
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
									log.error(XDAT.XFT_INIT_EXCEPTION_MESSAGE, e);
								} catch (ElementNotFoundException e) {
									log.error(XDAT.ELEMENT_NOT_FOUND_MESSAGE, e.ELEMENT, e);
								} catch (FieldNotFoundException e) {
									log.error(XDAT.FIELD_NOT_FOUND_MESSAGE, e.FIELD, e);
	                            }
	                        }
	                    }else{
	                        try {
	                        	Comparable o = (Comparable) newI.getProperty(key.getXMLPathString(newI.getGenericSchemaElement().getFullXMLName()));

	                            if (o!= null)
	                            {
	                            	Comparable o2 =(Comparable) oldI.getProperty(key.getXMLPathString(oldI.getGenericSchemaElement().getFullXMLName()));
	                                if (o2!= null)
	                                {
	                                	int compare=o.compareTo(o2);
	                                    if(compare!=0){
	                                    	return compare;
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
								log.error(XDAT.XFT_INIT_EXCEPTION_MESSAGE, e);
							} catch (ElementNotFoundException e) {
								log.error(XDAT.ELEMENT_NOT_FOUND_MESSAGE, e.ELEMENT, e);
							} catch (FieldNotFoundException e) {
								log.error(XDAT.FIELD_NOT_FOUND_MESSAGE, e.FIELD, e);
	                        }
	                    }
	                }


	                if (matchAll)
	                {
	                    return 0;
	                }
	        	}
	        }

	        //CHECK EXTENDED ITEM
	        //ADDED 9/26 when adding support for multiple references to 'no field' elements (abstract)
	        if (newI.getGenericSchemaElement().isExtension() && oldI.getGenericSchemaElement().isExtension()){
	            return compare(newI.getExtensionItem(), oldI.getExtensionItem());
	        }
		} catch (Exception e) {
            log.error("An unexpected error occurred", e);
        }
        return 0;
	}

}
