/*
 * core: org.nrg.xdat.presentation.PresentationA
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.presentation;

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

import java.util.ArrayList;
import java.util.List;
/**
 * @author Tim
 *
 */
public abstract class PresentationA {
	private SchemaElementI rootElement = null;
	private String display = null;
	private final List<String[]> additionalViews = new ArrayList<>();

    @SuppressWarnings("unused")
	public abstract XFTTableI formatTable(XFTTableI table,DisplaySearch search)throws Exception;
	public abstract XFTTableI formatTable(XFTTableI table,DisplaySearch search,boolean allowDiffs)throws Exception;
	public abstract String getVersionExtension();
	/**
	 * @return A list of the additional views for this presentation.
	 */
	public List<String[]> getAdditionalViews() {
		return new ArrayList<>(additionalViews);
	}

	/**
	 * @return The display for this presentation.
	 */
	public String getDisplay() {
		return display;
	}

	/**
	 * @return The root {@link SchemaElementI schema element} for this presentation.
	 */
	public SchemaElementI getRootElement() {
		return rootElement;
	}

	/**
	 * @param views    The views to set for the presentation.
	 */
	public void setAdditionalViews(final List<String[]> views) {
		additionalViews.clear();
		additionalViews.addAll(views);
	}

	/**
	 * Sets the display for the presentation.
	 * @param display    The display to set for the presentation.
	 */
	public void setDisplay(final String display) {
		this.display = display;
	}

	/**
	 * Sets the root element for the presentation.
	 * @param element The {@link SchemaElementI schema element} to set as the root element for the presentation.
	 */
	public void setRootElement(SchemaElementI element) {
		rootElement = element;
	}
	
	public ArrayList<DisplayFieldReferenceI> getVisibleFields(ElementDisplay ed, DisplaySearch search) throws ElementNotFoundException, XFTInitException
	{
	    DisplayVersion dv;
		ArrayList<DisplayFieldReferenceI> visibleFields;
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
				for (final String[] keys : getAdditionalViews()) {
					String elementName = keys[0];
					String version = keys[1];
					GenericWrapperElement foreign = GenericWrapperElement.GetElement(elementName);

					ElementDisplay foreignEd = DisplayManager.GetElementDisplay(foreign.getType().getFullForeignType());
					DisplayVersion foreignDV;
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
	
	public List<DisplayFieldReferenceI> getAllFields(ElementDisplay ed, DisplaySearch search) throws ElementNotFoundException, XFTInitException
	{
	    DisplayVersion dv;
		final List<DisplayFieldReferenceI> allFields;
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
				for (final String[] key : getAdditionalViews()) {
					String elementName = key[0];
					String version = key[1];
					GenericWrapperElement foreign = GenericWrapperElement.GetElement(elementName);
					ElementDisplay foreignEd = DisplayManager.GetElementDisplay(foreign.getType().getFullForeignType());
					DisplayVersion foreignDV;
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

