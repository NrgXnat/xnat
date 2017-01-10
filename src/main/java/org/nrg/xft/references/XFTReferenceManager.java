/*
 * core: org.nrg.xft.references.XFTReferenceManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.references;
import org.apache.log4j.Logger;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperFactory;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.XFTSchema;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.SearchCriteria;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
public class XFTReferenceManager {
	static org.apache.log4j.Logger logger = Logger.getLogger(XFTReferenceManager.class);
	private static XFTReferenceManager instance = null;
	
	private Hashtable properNames = new Hashtable();
	private Hashtable elementType = new Hashtable();
	private ArrayList manyToOnes = new ArrayList();//ArrayList(0:subordinateElement,1:superiorElement,2:XFTSuperiorReference)
	private ArrayList manyToManys = new ArrayList();//ArrayList(0:element,1:field,2:XFTManyToManyReference)
	
	private static Hashtable allXFTReferences = new Hashtable();
	
	private XFTReferenceManager()
	{
		
	}
	
	/**
	 * Gets singleton object
	 * @return Returns the singleton XFTReferenceManager object
	 */
	public static XFTReferenceManager GetInstance()
	{
		if (instance == null)
		{
			instance = new XFTReferenceManager();
		}
		return instance;
	}
	
	/**
	 * Adds a proper name/element type to the singleton object's collection.
	 * @param elementName
	 * @param namedType
	 */
	public static void AddProperName(String elementName,String namedType)
	{
		GetInstance().addElementType(elementName,namedType);
		GetInstance().addProperName(elementName,namedType);
		XFTPseudonymManager.AddNewPseudonymManager(namedType,elementName);
	}
	
	/**
	 * @param elementName
	 * @param namedType
	 */
	private void addProperName(String elementName,String namedType)
	{
		if ((elementName != null) && (namedType != null))
		{
			if ((!elementName.equalsIgnoreCase("")) && (!namedType.equalsIgnoreCase("")))
			{
				this.getProperNames().put(namedType,elementName);
			}
		}
	}
	
	/**
	 * @param elementName
	 * @param namedType
	 */
	private void addElementType(String elementName,String namedType)
	{
		if ((elementName != null) && (namedType != null))
		{
			if ((!elementName.equalsIgnoreCase("")) && (!namedType.equalsIgnoreCase("")))
			{
				this.getElementType().put(elementName,namedType);
			}
		}
	}
	
	/**
	 * Get proper name for the given element type (full xml Name)
	 * @param type
	 * @return (null if not found)
	 */
	public static String GetProperName(String type)
	{
		return (String)GetInstance().getProperNames().get(type);
	}

	/**
	 * Get element type (full xml Name) for the given proper name 
	 * @param properName
	 * @return (null if not found)
	 */
	public static String GetElementType(String properName)
	{
		return (String)GetInstance().getElementType().get(properName);
	}
	
	/**
	 * Checks to see if the item is already stored, and adds it to the collection of one-to-manys.
	 * @param ref
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	private void addManyToOne(XFTSuperiorReference ref) throws XFTInitException,ElementNotFoundException
	{
		if(! checkManyToOnes(ref.getSubordinateElementName(),ref.getSubordinateFieldSQLName(),ref.getSuperiorElementName(),ref.getSuperiorFieldSQLName()))
		{
			ArrayList sub = new ArrayList();
			sub.add(ref.getSubordinateElementName());
			sub.add(ref.getSuperiorElementName());
			sub.add(ref);
			sub.trimToSize();
			GetInstance().manyToOnes.add(sub);
			logger.debug("FOUND MANY-ONE RELATIONSHIP: '"+ref.getSubordinateElementName() +"'->'" + ref.getSuperiorElementName() + "'");
		}
	}
	
	/**
	 * Checks to see if the item is already stored, and adds it to the collection of many-to-manys.
	 * @param ref
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	private void addManyToMany(XFTManyToManyReference ref) throws XFTInitException,ElementNotFoundException
	{
		if (! checkManyToManys(ref.getElement1().getFullXMLName(),ref.getField1().getSQLName()))
		{
			ArrayList sub = new ArrayList();
			sub.add(ref.getElement1());
			sub.add(ref.getField1());
			sub.add(ref);
			sub.trimToSize();
			GetInstance().manyToManys.add(sub);
		
			sub = new ArrayList();
			sub.add(ref.getElement2());
			sub.add(ref.getField2());
			sub.add(ref);
			sub.trimToSize();
			GetInstance().manyToManys.add(sub);
			
			logger.debug("FOUND MANY-MANY RELATIONSHIP: '"+ref.getMappingTable()+ "'");
		}
	}

	/**
	 * ArrayList(0:element,1:field,2:XFTManyToManyReference)
	 * @return Returns the manyToManys list
	 */
	private ArrayList getManyToManys() {
		return manyToManys;
	}

	/**
	 * ArrayList of (XFTManyToManyReference)
	 * @return Returns the list of unique mappings
	 */
	public ArrayList getUniqueMappings() {
		ArrayList al = new ArrayList();
		
		Iterator iter = manyToManys.iterator();
		while (iter.hasNext())
		{
			ArrayList map = (ArrayList)iter.next();
			if (! al.contains(map.get(2)))
			{
				al.add(map.get(2));
			}
		}
		
		al.trimToSize();
		return al;
	}

	/**
	 * ArrayList(0:subordinateElement,1:superiorElement,2:XFTSuperiorReference)
	 * @return Returns the manyToOnes list
	 */
	private ArrayList getManyToOnes() {
		return manyToOnes;
	}
	
	/**
	 * @param subordinateElementName
	 * @param subordinateFieldName
	 * @param superiorElementName
	 * @param superiorFieldName
	 * @return Returns whether the fields are linked in the manyToOnes object
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	private boolean checkManyToOnes(String subordinateElementName,String subordinateFieldName,String superiorElementName,String superiorFieldName) throws XFTInitException,ElementNotFoundException
	{
		Iterator iter = this.manyToOnes.iterator();
		while(iter.hasNext())
		{
			ArrayList child = (ArrayList)iter.next();
			if (((String)child.get(0)).equalsIgnoreCase(subordinateElementName) && ((String)child.get(1)).equalsIgnoreCase(superiorElementName))
			{
				XFTSuperiorReference ref = (XFTSuperiorReference)child.get(2);
				if (subordinateFieldName !=null)
				{
					if(! subordinateFieldName.equalsIgnoreCase(ref.getSubordinateFieldSQLName()))
					{
						return false;
					}
				}else
				{
					if (ref.getSubordinateField() != null)
					{
						return false;
					}
				}
				if (superiorFieldName !=null)
				{
					if(! superiorFieldName.equalsIgnoreCase(ref.getSuperiorFieldSQLName()))
					{
						return false;
					}
				}else
				{
					if (ref.getSuperiorField() != null)
					{
						return false;
					}
				}
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @param elementName
	 * @param fieldSQLName
	 * @return Returns whether the elementName and fieldSQLName are linked in the manyToManys object
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	private boolean checkManyToManys(String elementName,String fieldSQLName) throws XFTInitException,ElementNotFoundException
	{
		Iterator iter = this.manyToManys.iterator();
		while(iter.hasNext())
		{
			ArrayList child = (ArrayList)iter.next();
			if (child.get(1) != null)
			{
				if (((GenericWrapperElement)child.get(0)).getFullXMLName().equalsIgnoreCase(elementName) && ((GenericWrapperField)child.get(1)).getSQLName().equalsIgnoreCase(fieldSQLName))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Finds the XFTReferenceI for the given reference field.
	 * @param f
	 * @return Returns the XFTReferenceI for the given reference field
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static XFTReferenceI FindReference(GenericWrapperField f) throws XFTInitException,ElementNotFoundException
	{
		GenericWrapperElement parent = f.getParentElement().getGenericXFTElement();
		String s = f.getXMLPathString(parent.getFullXMLName()) + " " + f.getXMLType();
		if (allXFTReferences.get(s)==null)
		{
		    XFTReferenceI found = null;
			if (f.isMultiple())
			{
				Iterator iter = GetInstance().getManyToManys().iterator();
				while(iter.hasNext())
				{
					ArrayList child = (ArrayList)iter.next();
					if (((GenericWrapperElement)child.get(0)).getFullXMLName().equalsIgnoreCase(parent.getFullXMLName()) && ((GenericWrapperField)child.get(1)).getSQLName().equalsIgnoreCase(f.getSQLName()))
					{
					    found = ((XFTManyToManyReference)child.get(2));
					    break;
					}
				}
				
				if (found ==null)
				{
					Iterator iter2 = GetInstance().getManyToOnes().iterator();
					while(iter2.hasNext())
					{
						ArrayList child = (ArrayList)iter2.next();
						if (((String)child.get(1)).equalsIgnoreCase(f.getParentElement().getFullXMLName()))
						{
							XFTSuperiorReference ref = (XFTSuperiorReference)child.get(2);
							if (ref.getSuperiorField() != null)
							{
							    String superior = ref.getSubordinateElement().getFullXMLName();
								String refType = f.getXMLType().getFullForeignType();
								
								String localName = f.getSQLName();
								String foreignName = ref.getSuperiorField().getSQLName();
								
								if (superior.equalsIgnoreCase(refType))
								{
								    found = ref;
								    if (localName.equals(foreignName))
								    {
										break;
								    }
								}
//								if (ref.getSuperiorField().getName().equalsIgnoreCase(f.getName()))
//								{
//									return ref;
//								}
							}
						}
					}
				}
			    
			}else
			{
				Iterator iter = GetInstance().getManyToOnes().iterator();
				while(iter.hasNext())
				{
					ArrayList child = (ArrayList)iter.next();
					String rootElementName = (String)child.get(0);
					String foreignElementName = f.getParentElement().getFullXMLName();
					if (((String)child.get(0)).equalsIgnoreCase(foreignElementName))
					{
						XFTSuperiorReference ref = (XFTSuperiorReference)child.get(2);
						if (ref.getSubordinateField() != null)
						{
							if (ref.getSubordinateField().getSQLName().equalsIgnoreCase(f.getSQLName()))
							{
								found= ref;
								break;
							}
						}else
						{
							if (ref.getSuperiorField() != null)
							{
								String superior = ref.getSuperiorElement().getFullXMLName();
								String refType = f.getXMLType().getFullForeignType();
								
								String localName = f.getSQLName();
								String foreignName = ref.getSuperiorField().getSQLName();
								
								if (superior.equalsIgnoreCase(refType))
								{
								    found = ref;
								    if (localName.equals(foreignName))
								    {
										break;
								    }
								}
							}
						}
					}
				}
			}
			if (found != null)
			{
			    allXFTReferences.put(s,found);
			}
		}
		
		return (XFTReferenceI)allXFTReferences.get(s);
	}
	
	/**
	 * Find elements that reference this given element without being referenced within the given element
	 * but will result in an implied column in the given element.
	 * @param e
	 * @return Returns an array list of the elements that reference this element without being referenced within the element
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static ArrayList FindHiddenSuperiorsFor(GenericWrapperElement e)throws XFTInitException,ElementNotFoundException
	{
		ArrayList al = new ArrayList();
		
		Iterator iter = GetInstance().getManyToOnes().iterator();
		while(iter.hasNext())
		{
			ArrayList child = (ArrayList)iter.next();
			if (((String)child.get(0)).equalsIgnoreCase(e.getFullXMLName()))
			{
				XFTSuperiorReference ref = (XFTSuperiorReference)child.get(2);
				if (ref.getSubordinateField() == null)
				{
					al.add(ref);
				}
			}
		}
		
		al.trimToSize();
		return al;
	}
	
	/**
	 * Find elements that reference this element.
	 * @param e
	 * @return ArrayList of XFTSuperiorReferences
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static ArrayList FindSuperiorsFor(GenericWrapperElement e)throws XFTInitException,ElementNotFoundException
	{
		ArrayList al = new ArrayList();
		
		Iterator iter = GetInstance().getManyToOnes().iterator();
		while(iter.hasNext())
		{
			ArrayList child = (ArrayList)iter.next();
			if (((String)child.get(0)).equalsIgnoreCase(e.getFullXMLName()))
			{
				XFTSuperiorReference ref = (XFTSuperiorReference)child.get(2);
				al.add(ref);
			}
		}
		
		al.trimToSize();
		return al;
	}
	
	/**
	 * Find elements that this element references.
	 * @param e
	 * @return ArrayList of XFTSuperiorReferences
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static ArrayList FindSubordinatesFor(GenericWrapperElement e)throws XFTInitException,ElementNotFoundException
	{
		ArrayList al = new ArrayList();
		
		Iterator iter = GetInstance().getManyToOnes().iterator();
		while(iter.hasNext())
		{
			ArrayList child = (ArrayList)iter.next();
			if (((String)child.get(1)).equalsIgnoreCase(e.getFullXMLName()))
			{
				XFTSuperiorReference ref = (XFTSuperiorReference)child.get(2);
				al.add(ref);
			}
		}
		
		al.trimToSize();
		return al;
	}

	
	/**
	 * Find elements that this element references as many-to-many.
	 * @param e
	 * @return ArrayList of XFTManyToManyReference
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static ArrayList FindManyToManysFor(GenericWrapperElement e)throws XFTInitException,ElementNotFoundException
	{
		ArrayList al = new ArrayList();
		
		Iterator iter = GetInstance().getManyToManys().iterator();
		while(iter.hasNext())
		{
			ArrayList child = (ArrayList)iter.next();
			if (((GenericWrapperElement)child.get(0)).getFullXMLName().equalsIgnoreCase(e.getFullXMLName()))
			{
				XFTManyToManyReference ref = (XFTManyToManyReference)child.get(2);
				al.add(ref);
			}
		}
		
		al.trimToSize();
		return al;
	}
	
	/**
	 * Initializes the XFTReferenceManager and XFTPseudonymManager by iterating through 
	 * the XFTSchema and identifying references.  
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static void init() throws XFTInitException,ElementNotFoundException
	{
		Iterator schemas = XFTManager.GetSchemas().iterator();
		while (schemas.hasNext())
		{
			XFTSchema s = (XFTSchema)schemas.next();
			GetInstance().assignReferences(s);
		}
		
	}
	
	public static void clean(){
		instance = null;
	}
	

	
	/**
	 * 
	 * @param s
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	private void assignReferences(XFTSchema s) throws XFTInitException,ElementNotFoundException
	{
		Iterator elements = s.getWrappedElementsSorted(GenericWrapperFactory.GetInstance()).iterator();
		while (elements.hasNext())
		{
			GenericWrapperElement input = (GenericWrapperElement)elements.next();

			Iterator iter = input.getAllFields(false,true).iterator();
			while (iter.hasNext())
			{
				GenericWrapperField xf = (GenericWrapperField)iter.next();
				if (xf.isReference())
				{
					if (! xf.isMultiple())
					{
						XFTSuperiorReference ref = new XFTSuperiorReference(xf);
						if (! xf.getName().equalsIgnoreCase(xf.getXMLType().getLocalType()))
							XFTPseudonymManager.AddPseudonym(xf.getName(),xf.getXMLType().getLocalType());
						addManyToOne(ref);
					}else
					{
						if (xf.getRelationType().equalsIgnoreCase("single"))
						{
							XFTSuperiorReference ref = new XFTSuperiorReference(xf);
							addManyToOne(ref);
						}else
						{
							GenericWrapperElement foreign = (GenericWrapperElement)xf.getReferenceElement();
							Iterator foreignRefs = foreign.getAllFields(false,true).iterator();
							boolean foundSubordinateField = false;
							while (foreignRefs.hasNext())
							{
								GenericWrapperField temp = (GenericWrapperField)foreignRefs.next();
								if (temp.isReference()) {
									String inputName = input.getLocalXMLName();
									String referencedType = temp.getXMLType().getLocalType();
									if  (referencedType.equalsIgnoreCase(inputName))
									{
										if (temp.isMultiple())
										{
											//MANY TO MANY
											XFTManyToManyReference ref = new XFTManyToManyReference(xf,temp);
											ref.setMapping_name(temp.getRelationName());
											ref.setUnique(temp.getRelationUnique());
											addManyToMany(ref);
											foundSubordinateField = true;
										}else
										{
											//MANY TO ONE
											XFTSuperiorReference ref = new XFTSuperiorReference(xf);
											addManyToOne(ref);
											foundSubordinateField = true;
										}
										break;
									}
								}
							}
							
							if (! foundSubordinateField)
							{
								if (foreign.getWrapped().isCreatedChild())
								{
									XFTSuperiorReference ref = new XFTSuperiorReference(xf);
									addManyToOne(ref);
								}else{
									if (xf.getRelationType().equalsIgnoreCase("single"))
									{
										XFTSuperiorReference ref = new XFTSuperiorReference(xf);
										addManyToOne(ref);
									}else
									{
										XFTManyToManyReference ref = new XFTManyToManyReference(xf,foreign);
										ref.setMapping_name(xf.getRelationName());
										ref.setUnique(xf.getRelationUnique());
										addManyToMany(ref);
									}
								}
								//CHANGED 12-06-04 so that the default behavior is to make maxOccurs relationships many-to-many
	//							XFTSuperiorReference ref = new XFTSuperiorReference(xf);
	//							addManyToOne(ref);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @return Returns a hastable of the proper names
	 */
	public Hashtable getProperNames() {
		return properNames;
	}

	/**
	 * @return Returns the element type
	 */
	private Hashtable getElementType() {
		return elementType;
	}

	/**
	 * @param hashtable
	 */
    @SuppressWarnings("unused")
	private void setElementType(Hashtable hashtable) {
		elementType = hashtable;
	}
	
	
	/**
	 * Outputs all of the references defined in the XFTReferenceManager.
	 */
	public static void OutputReferences()
	{
		try {
			StringBuffer sb = new StringBuffer("Element Types:");
			
			Enumeration enumer = GetInstance().getElementType().keys();
			while(enumer.hasMoreElements())
			{
				String key =(String)enumer.nextElement();
				sb.append("\n").append(key).append("->").append(GetInstance().getElementType().get(key));
			}
			
			sb.append("\n\nProper Names:");
			enumer = GetInstance().getProperNames().keys();
			while(enumer.hasMoreElements())
			{
				String key =(String)enumer.nextElement();
				sb.append("\n").append(key).append("->").append(GetInstance().getProperNames().get(key));
			}
			
			sb.append("\n\nMany-To-Many:");
			Iterator iter = GetInstance().getManyToManys().iterator();
			while(iter.hasNext())
			{
				ArrayList key =(ArrayList)iter.next();
				sb.append("\n").append(((GenericWrapperElement)key.get(0)).getName()).append("->").append(((GenericWrapperField)key.get(1)).getName()).append(":").append(((XFTManyToManyReference)key.get(2)).getMappingTable());
			}
			
			sb.append("\n\nMany-To-One:");
			iter = GetInstance().getManyToOnes().iterator();
			while(iter.hasNext())
			{
				ArrayList key =(ArrayList)iter.next();
				sb.append("\n").append(key.get(0)).append("(FK)->").append(key.get(1));
			}
			
			org.nrg.xft.utils.FileUtils.OutputToFile(sb.toString(),XFTManager.GetInstance().getSourceDir() + "references.txt");
		} catch (org.nrg.xft.exception.XFTInitException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	/**
	 * @param item
	 * @return Returns the count of the number of references in the item
	 * @throws Exception
	 */
	public static Integer NumberOfReferences(XFTItem item) throws Exception
	{
	    int i =0;
	    
	    GenericWrapperElement root = item.getGenericSchemaElement();
	    
	    ArrayList possibleParents = root.getPossibleParents(false);
	    
	    if (possibleParents.size() > 0)
	    {
	        Iterator iter = possibleParents.iterator();
	        while (iter.hasNext())
	        {
	            Object[] pp = (Object[])iter.next();
	            GenericWrapperElement foreign = (GenericWrapperElement)pp[0];
	            String xmlPath = (String)pp[1];
	            GenericWrapperField gwf = (GenericWrapperField)pp[2];
	            
	            String extensionType = "";

	            if (foreign.isExtension())
	            {
	                extensionType =  foreign.getExtensionType().getFullForeignType();
	            }
	            
	            if (!root.getFullXMLName().equalsIgnoreCase(extensionType))
	            {
	                if (foreign.getAddin().equalsIgnoreCase(""))
	                {
	                    XFTReferenceI ref = gwf.getXFTReference();
                        if (ref.isManyToMany())
                        {
                            CriteriaCollection cc = new CriteriaCollection("AND");
                          
	                          Iterator refCols = ((XFTManyToManyReference)ref).getMappingColumnsForElement(root).iterator();
	                          while (refCols.hasNext())
	                          {
	                              XFTMappingColumn spec = (XFTMappingColumn)refCols.next();
	                              Object o = item.getProperty(spec.getForeignKey().getXMLPathString());
	                              SearchCriteria sc = new SearchCriteria();
	                              sc.setField_name(spec.getLocalSqlName());
	                              sc.setCleanedType(spec.getXmlType().getLocalType());
	                              sc.setValue(o);
	                              cc.add(sc);
	                          }
	                          
	                          Long o =  DBAction.CountInstancesOfFieldValues(((XFTManyToManyReference)ref).getMappingTable(),foreign.getDbName(),cc);
	                          i += o.intValue();
                        }else{
                             XFTSuperiorReference supRef = (XFTSuperiorReference)ref;
                                                          
                             if (supRef.getSubordinateElement().equals(root))
                             {
                                 //ROOT has the fk column (check if it is null)
                                 Iterator refsCols = supRef.getKeyRelations().iterator();
	                               while (refsCols.hasNext())
	                               {
	                                   XFTRelationSpecification spec = (XFTRelationSpecification)refsCols.next();
	                                   Object o = item.getProperty(spec.getLocalCol());
	                                   if (o != null)
	                                   {
	                                       i++;
	                                       break;
	                                   }
	                               }
                             }else{
                                 //FOREIGN has the fk column
                                 CriteriaCollection cc = new CriteriaCollection("AND");
	                               
	                               Iterator refsCols = supRef.getKeyRelations().iterator();
	                               while (refsCols.hasNext())
	                               {
	                                   XFTRelationSpecification spec = (XFTRelationSpecification)refsCols.next();
	                                   Object o = item.getProperty(spec.getForeignCol());
	                                   SearchCriteria sc = new SearchCriteria();
	                                   sc.setField_name(spec.getLocalCol());
	                                   sc.setValue(DBAction.ValueParser(o,spec.getSchemaType().getLocalType(),true));
	                                   cc.add(sc);
	                               }
	                               
	                               Long o =  DBAction.CountInstancesOfFieldValues(foreign.getSQLName(),foreign.getDbName(),cc);
	                               if (o.intValue() > 1)
	                               {
	                                   return new Integer(o.intValue());
	                               }else{
	                                   i += o.intValue();
	                               } 
                             }
                                                              
                        }
	                }
	            }
	        }
	    }        

		if (item.getGenericSchemaElement().isExtension())
		{
		    XFTItem extensionItem = item.getExtensionItem();
		    i += NumberOfReferences(extensionItem).intValue();
		}
        
	    return new Integer(i);
	}
}

