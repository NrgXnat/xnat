/*
 * core: org.nrg.xft.references.XFTReferenceManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.references;

import lombok.extern.slf4j.Slf4j;
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
import org.nrg.xft.utils.FileUtils;

import java.util.*;

@Slf4j
public class XFTReferenceManager {
	private static       XFTReferenceManager        instance         = null;
	private static final Map<String, XFTReferenceI> allXFTReferences = new HashMap<>();

	private final Map<String, String> properNames = new HashMap<>();
	private final Map<String, String> elementType = new HashMap<>();
	private final List<List<?>>       manyToOnes  = new ArrayList<>(); //ArrayList(0:subordinateElement,1:superiorElement,2:XFTSuperiorReference)
	private final List<List<?>>       manyToManys = new ArrayList<>();//ArrayList(0:element,1:field,2:XFTManyToManyReference)

	private XFTReferenceManager() {
		log.info("Creating the XFTReferenceManager instance");
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
				getProperNames().put(namedType,elementName);
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
		if(!checkManyToOnes(ref.getSubordinateElementName(),ref.getSubordinateFieldSQLName(),ref.getSuperiorElementName(),ref.getSuperiorFieldSQLName()))
		{
			manyToOnes.add(Arrays.asList(ref.getSubordinateElementName(), ref.getSuperiorElementName(), ref));
			log.debug("FOUND MANY-ONE RELATIONSHIP: '" + ref.getSubordinateElementName() + "'->'" + ref.getSuperiorElementName() + "'");
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
			manyToManys.add(Arrays.asList(ref.getElement1(), ref.getField1(), ref));
			manyToManys.add(Arrays.asList(ref.getElement2(), ref.getField2(), ref));
			
			log.debug("FOUND MANY-MANY RELATIONSHIP: '" + ref.getMappingTable() + "'");
		}
	}

	/**
	 * ArrayList(0:element,1:field,2:XFTManyToManyReference)
	 * @return Returns the manyToManys list
	 */
	private List<List<?>> getManyToManys() {
		return new ArrayList<>(manyToManys);
	}

	/**
	 * ArrayList of (XFTManyToManyReference)
	 * @return Returns the list of unique mappings
	 */
	public ArrayList getUniqueMappings() {
		ArrayList al = new ArrayList();
		for (final List<?> manyToMany : getManyToManys()) {
			if (!al.contains(manyToMany.get(2))) {
				al.add(manyToMany.get(2));
			}
		}
		return al;
	}

	/**
	 * ArrayList(0:subordinateElement,1:superiorElement,2:XFTSuperiorReference)
	 * @return Returns the manyToOnes list
	 */
	private List<List<?>> getManyToOnes() {
		//noinspection unchecked
		return new ArrayList(manyToOnes);
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
		for (final List<?> manyToOne : getManyToOnes()) {
			if (((String) manyToOne.get(0)).equalsIgnoreCase(subordinateElementName) && ((String) manyToOne.get(1)).equalsIgnoreCase(superiorElementName)) {
				XFTSuperiorReference ref = (XFTSuperiorReference) manyToOne.get(2);
				if (subordinateFieldName != null) {
					if (!subordinateFieldName.equalsIgnoreCase(ref.getSubordinateFieldSQLName())) {
						return false;
					}
				} else {
					if (ref.getSubordinateField() != null) {
						return false;
					}
				}
				if (superiorFieldName != null) {
					return superiorFieldName.equalsIgnoreCase(ref.getSuperiorFieldSQLName());
				} else {
					return ref.getSuperiorField() == null;
				}
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
		for (final List<?> manyToMany : getManyToManys()) {
			if (manyToMany.get(1) != null) {
				if (((GenericWrapperElement) manyToMany.get(0)).getFullXMLName().equalsIgnoreCase(elementName) && ((GenericWrapperField) manyToMany.get(1)).getSQLName().equalsIgnoreCase(fieldSQLName)) {
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
				for (final List<?> child : GetInstance().getManyToManys()) {
					if (((GenericWrapperElement)child.get(0)).getFullXMLName().equalsIgnoreCase(parent.getFullXMLName()) && ((GenericWrapperField)child.get(1)).getSQLName().equalsIgnoreCase(f.getSQLName()))
					{
					    found = ((XFTManyToManyReference)child.get(2));
					    break;
					}
				}
				
				if (found ==null)
				{
					for (final List<?> child : GetInstance().getManyToOnes()) {
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
				for (final List<?> child : GetInstance().getManyToOnes()) {
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

		for (final Object o : GetInstance().getManyToOnes()) {
			ArrayList child = (ArrayList) o;
			if (((String) child.get(0)).equalsIgnoreCase(e.getFullXMLName())) {
				XFTSuperiorReference ref = (XFTSuperiorReference) child.get(2);
				if (ref.getSubordinateField() == null) {
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
		final ArrayList<XFTSuperiorReference> al = new ArrayList<>();
		for (final List<?> child : GetInstance().getManyToOnes()) {
			if (((String)child.get(0)).equalsIgnoreCase(e.getFullXMLName())) {
				XFTSuperiorReference ref = (XFTSuperiorReference)child.get(2);
				al.add(ref);
			}
		}
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
		final ArrayList<XFTSuperiorReference> al = new ArrayList<>();
		for (final List<?> child : GetInstance().getManyToOnes()) {
			if (((String)child.get(1)).equalsIgnoreCase(e.getFullXMLName()))
			{
				XFTSuperiorReference ref = (XFTSuperiorReference)child.get(2);
				al.add(ref);
			}
		}
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
		final ArrayList<XFTManyToManyReference> al = new ArrayList<>();
		for (final List<?> child : GetInstance().getManyToManys()) {
			if (((GenericWrapperElement)child.get(0)).getFullXMLName().equalsIgnoreCase(e.getFullXMLName()))
			{
				XFTManyToManyReference ref = (XFTManyToManyReference)child.get(2);
				al.add(ref);
			}
		}
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
		for (final XFTSchema s : XFTManager.GetSchemas()) {
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
		for (final Object o : s.getWrappedElementsSorted(GenericWrapperFactory.GetInstance())) {
			GenericWrapperElement input = (GenericWrapperElement) o;

			for (final Object o1 : input.getAllFields(false, true)) {
				GenericWrapperField xf = (GenericWrapperField) o1;
				if (xf.isReference()) {
					if (!xf.isMultiple()) {
						XFTSuperiorReference ref = new XFTSuperiorReference(xf);
						if (!xf.getName().equalsIgnoreCase(xf.getXMLType().getLocalType())) {
							XFTPseudonymManager.AddPseudonym(xf.getName(), xf.getXMLType().getLocalType());
						}
						addManyToOne(ref);
					} else {
						if (xf.getRelationType().equalsIgnoreCase("single")) {
							XFTSuperiorReference ref = new XFTSuperiorReference(xf);
							addManyToOne(ref);
						} else {
							GenericWrapperElement foreign               = (GenericWrapperElement) xf.getReferenceElement();
							boolean               foundSubordinateField = false;
							for (final Object object : foreign.getAllFields(false, true)) {
								GenericWrapperField temp = (GenericWrapperField) object;
								if (temp.isReference()) {
									String inputName      = input.getLocalXMLName();
									String referencedType = temp.getXMLType().getLocalType();
									if (referencedType.equalsIgnoreCase(inputName)) {
										if (temp.isMultiple()) {
											//MANY TO MANY
											XFTManyToManyReference ref = new XFTManyToManyReference(xf, temp);
											ref.setMapping_name(temp.getRelationName());
											ref.setUnique(temp.getRelationUnique());
											addManyToMany(ref);
											foundSubordinateField = true;
										} else {
											//MANY TO ONE
											XFTSuperiorReference ref = new XFTSuperiorReference(xf);
											addManyToOne(ref);
											foundSubordinateField = true;
										}
										break;
									}
								}
							}

							if (!foundSubordinateField) {
								if (foreign.getWrapped().isCreatedChild()) {
									XFTSuperiorReference ref = new XFTSuperiorReference(xf);
									addManyToOne(ref);
								} else {
									if (xf.getRelationType().equalsIgnoreCase("single")) {
										XFTSuperiorReference ref = new XFTSuperiorReference(xf);
										addManyToOne(ref);
									} else {
										XFTManyToManyReference ref = new XFTManyToManyReference(xf, foreign);
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
	public Map<String, String> getProperNames() {
		return properNames;
	}

	/**
	 * @return Returns the element type
	 */
	private Map<String, String> getElementType() {
		return elementType;
	}

	/**
	 * @param hashtable
	 */
    @SuppressWarnings("unused")
	private void setElementType(final Hashtable hashtable) {
    	for (final Object key : hashtable.keySet()) {
			final Object value = hashtable.get(key);
			elementType.put(key.toString(), value != null ? value.toString() : null);
		}
	}

	/**
	 * Outputs all of the references defined in the XFTReferenceManager.
	 */
	public static void OutputReferences() {
		try {
			final StringBuilder sb = new StringBuilder("Element Types:");
			for (final String key : GetInstance().getElementType().keySet()) {
				sb.append("\n").append(key).append("->").append(GetInstance().getElementType().get(key));
			}
			
			sb.append("\n\nProper Names:");
			for (final String key : GetInstance().getProperNames().keySet()) {
				sb.append("\n").append(key).append("->").append(GetInstance().getProperNames().get(key));
			}
			
			sb.append("\n\nMany-To-Many:");
			for (final List<?> key : GetInstance ().getManyToManys()) {
				sb.append("\n").append(((GenericWrapperElement)key.get(0)).getName()).append("->").append(((GenericWrapperField)key.get(1)).getName()).append(":").append(((XFTManyToManyReference)key.get(2)).getMappingTable());
			}
			
			sb.append("\n\nMany-To-One:");
			for (final List<?> key : GetInstance().getManyToOnes()) {
				sb.append("\n").append(key.get(0)).append("(FK)->").append(key.get(1));
			}
			
			FileUtils.OutputToFile(sb.toString(), XFTManager.GetInstance().getSourceDir() + "references.txt");
		} catch (XFTInitException e) {
			log.error("An error occurred accessing XFT", e);
		} catch (Exception e) {
			log.error("An unexpected error occurred", e);
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
			for (final Object possibleParent : possibleParents) {
				Object[]              pp      = (Object[]) possibleParent;
				GenericWrapperElement foreign = (GenericWrapperElement) pp[0];
				String                xmlPath = (String) pp[1];
				GenericWrapperField   gwf     = (GenericWrapperField) pp[2];

				String extensionType = "";

				if (foreign.isExtension()) {
					extensionType = foreign.getExtensionType().getFullForeignType();
				}

				if (!root.getFullXMLName().equalsIgnoreCase(extensionType)) {
					if (foreign.getAddin().equalsIgnoreCase("")) {
						XFTReferenceI ref = gwf.getXFTReference();
						if (ref.isManyToMany()) {
							CriteriaCollection cc = new CriteriaCollection("AND");

							for (final Object object : ((XFTManyToManyReference) ref).getMappingColumnsForElement(root)) {
								XFTMappingColumn spec = (XFTMappingColumn) object;
								Object           o    = item.getProperty(spec.getForeignKey().getXMLPathString());
								SearchCriteria   sc   = new SearchCriteria();
								sc.setField_name(spec.getLocalSqlName());
								sc.setCleanedType(spec.getXmlType().getLocalType());
								sc.setValue(o);
								cc.add(sc);
							}

							Long o = DBAction.CountInstancesOfFieldValues(((XFTManyToManyReference) ref).getMappingTable(), foreign.getDbName(), cc);
							i += o.intValue();
						} else {
							XFTSuperiorReference supRef = (XFTSuperiorReference) ref;

							if (supRef.getSubordinateElement().equals(root)) {
								//ROOT has the fk column (check if it is null)
								for (final XFTRelationSpecification spec: supRef.getKeyRelations()) {
									Object                   o    = item.getProperty(spec.getLocalCol());
									if (o != null) {
										i++;
										break;
									}
								}
							} else {
								//FOREIGN has the fk column
								CriteriaCollection cc = new CriteriaCollection("AND");

								for (final XFTRelationSpecification spec : supRef.getKeyRelations()) {
									Object                   o    = item.getProperty(spec.getForeignCol());
									SearchCriteria           sc   = new SearchCriteria();
									sc.setField_name(spec.getLocalCol());
									sc.setValue(DBAction.ValueParser(o, spec.getSchemaType().getLocalType(), true));
									cc.add(sc);
								}

								Long o = DBAction.CountInstancesOfFieldValues(foreign.getSQLName(), foreign.getDbName(), cc);
								if (o.intValue() > 1) {
									return o.intValue();
								} else {
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
		    i += NumberOfReferences(extensionItem);
		}
        
	    return i;
	}
}

