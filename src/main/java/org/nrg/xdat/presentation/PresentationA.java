/*
 * org.nrg.xdat.presentation.PresentationA
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.presentation;

import java.util.ArrayList;
import java.util.Iterator;

import org.nrg.xdat.display.DisplayFieldReferenceI;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.DisplayVersion;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.design.SchemaElementI;
/**
 * @author Tim
 *
 */
public abstract class PresentationA {
	private SchemaElementI rootElement = null;
	private String display = null;
	private ArrayList additionalViews = null;
	 
	public abstract XFTTableI formatTable(XFTTableI table,DisplaySearch search)throws Exception;
	public abstract XFTTableI formatTable(XFTTableI table,DisplaySearch search,boolean allowDiffs)throws Exception;
	public abstract String getVersionExtension();
	/**
	 * @return
	 */
	public ArrayList getAdditionalViews() {
		return additionalViews;
	}

	/**
	 * @return
	 */
	public String getDisplay() {
		return display;
	}

	/**
	 * @return
	 */
	public SchemaElementI getRootElement() {
		return rootElement;
	}

	/**
	 * @param hashtable
	 */
	public void setAdditionalViews(ArrayList hashtable) {
		additionalViews = hashtable;
	}

	/**
	 * @param string
	 */
	public void setDisplay(String string) {
		display = string;
	}

	/**
	 * @param element
	 */
	public void setRootElement(SchemaElementI element) {
		rootElement = element;
	}
	
	public ArrayList<DisplayFieldReferenceI> getVisibleFields(ElementDisplay ed, DisplaySearch search) throws ElementNotFoundException, XFTInitException
	{
	    DisplayVersion dv = null;
		ArrayList<DisplayFieldReferenceI> visibleFields=new ArrayList<DisplayFieldReferenceI>();
		if (search.useVersions())
		{
			if (! getVersionExtension().equalsIgnoreCase(""))
			{
				dv = ed.getVersion(getDisplay() + "_" + getVersionExtension(),getDisplay());
			}else{
				dv = ed.getVersion(getDisplay(),"default");
			}
			visibleFields = dv.getVisibleFields();
		
			if (getAdditionalViews() != null && getAdditionalViews().size() > 0)
			{
				Iterator keys = getAdditionalViews().iterator();
				while (keys.hasNext())
				{
				    String[] key = (String[])keys.next();
					String elementName = key[0];
					String version = key[1];
					GenericWrapperElement foreign = GenericWrapperElement.GetElement(elementName);

					ElementDisplay foreignEd = DisplayManager.GetElementDisplay(foreign.getType().getFullForeignType());
					DisplayVersion foreignDV = null;
					if (! getVersionExtension().equalsIgnoreCase(""))
					{
						foreignDV = foreignEd.getVersion(version + "_" + getVersionExtension(),version);
					}else{
						foreignDV = foreignEd.getVersion(version,"default");
					}
				
					visibleFields.addAll(foreignDV.getVisibleFields());
				}
			}
		}else{
		    visibleFields = search.getFields().getSortedVisibleFields();
		}
		
		return visibleFields;
	}
	
	public ArrayList getAllFields(ElementDisplay ed, DisplaySearch search) throws ElementNotFoundException, XFTInitException
	{
	    DisplayVersion dv = null;
		ArrayList allFields=new ArrayList();
		if (search.useVersions())
		{
			if (! getVersionExtension().equalsIgnoreCase(""))
			{
				dv = ed.getVersion(getDisplay() + "_" + getVersionExtension(),getDisplay());
			}else{
				dv = ed.getVersion(getDisplay(),"default");
			}
			allFields = dv.getAllFields();
		
			if (getAdditionalViews() != null && getAdditionalViews().size() > 0)
			{
				Iterator keys = getAdditionalViews().iterator();
				while (keys.hasNext())
				{
				    String[] key = (String[])keys.next();
					String elementName = key[0];
					String version = key[1];
					GenericWrapperElement foreign = GenericWrapperElement.GetElement(elementName);
					ElementDisplay foreignEd = DisplayManager.GetElementDisplay(foreign.getType().getFullForeignType());
					DisplayVersion foreignDV = null;
					if (! getVersionExtension().equalsIgnoreCase(""))
					{
						foreignDV = foreignEd.getVersion(version + "_" + getVersionExtension(),version);
					}else{
						foreignDV = foreignEd.getVersion(version,"default");
					}
				
					allFields.addAll(foreignDV.getVisibleFields());
				}
			}
		}else{
		    allFields = search.getFields().getSortedVisibleFields();
		}
		
		return allFields;
	}
	
}

