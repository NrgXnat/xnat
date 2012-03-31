package org.nrg.xft.compare;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperElement;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperFactory;

public class ItemComparator implements Comparator<XFTItem> {
	static org.apache.log4j.Logger logger = Logger.getLogger(ItemComparator.class);

	@SuppressWarnings("unchecked")
	public int compare(XFTItem oldTI, XFTItem newTI) {
		try{
			XFTItem oldI=(XFTItem)oldTI.clone(true);
			XFTItem newI=(XFTItem)newTI.clone(true);
			
			final List<GenericWrapperField> ufields= newI.getGenericSchemaElement().getUniqueFields();
	        for(final GenericWrapperField key:ufields)
	        {
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
	                            	final Comparable o = (Comparable) newI.getProperty(newI.getGenericSchemaElement().getFullXMLName() + XFT.PATH_SEPERATOR + (String)field.get(0));
	
	                                if (o!= null)
	                                {
	                                	final Comparable o2 = (Comparable) oldI.getProperty(oldI.getGenericSchemaElement().getFullXMLName() + XFT.PATH_SEPERATOR + (String)field.get(0));
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
	                                logger.error("",e);
	                            } catch (ElementNotFoundException e) {
	                                logger.error("",e);
	                            } catch (FieldNotFoundException e) {
	                                logger.error("",e);
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
	                    return 0;
	                }
	        	}
	        }
	
	        //CHECK EXTENDED ITEM
	        //ADDED 9/26 when adding support for multiple references to 'no field' elements (abstract)
	        if (newI.getGenericSchemaElement().isExtension() && oldI.getGenericSchemaElement().isExtension()){
	        	final XFTItem child1 = newI.getExtensionItem();
	        	final XFTItem child2 = oldI.getExtensionItem();
	            return this.compare(child1,child2);
	        }
		} catch (Exception e) {
            logger.error("",e);
        }
        return 0;
	}

}
