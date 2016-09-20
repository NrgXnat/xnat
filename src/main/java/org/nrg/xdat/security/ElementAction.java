/*
 * core: org.nrg.xdat.security.ElementAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.security;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.ItemWrapper;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class ElementAction extends ItemWrapper {

    public ElementAction()
	{
	}

	public ElementAction(ItemI item)
	{
		setItem(item);
	}
	
	
	public String getName() 
	{
	    try {
            return (String)getProperty("element_action_name");
        } catch (ElementNotFoundException e) {
            return null;
        } catch (FieldNotFoundException e) {
            return null;
        }

	}
	
	public String getSchemaElementName()
	{
	    return "xdat:element_action";
	}
	
	public String getDisplayName() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		try {
			return (String)getProperty("display_name");
		} catch (FieldEmptyException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean hasImage() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		try {
			if ( (String)getProperty("image")== null)
			{
			    return false;
			}else{
			    return true;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	public String getImage() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		try {
			return (String)getProperty("image");
		} catch (FieldEmptyException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getPopup() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		try {
            // This method will initialize the matcher and all associated metadata.
            initializePopupOptions();
            // So return whatever the results of that are.
            return popup;
		} catch (FieldEmptyException e) {
			e.printStackTrace();
			return "never";
		}
	}

    public String getPopupWidth() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        try {
            initializePopupOptions();
            return popupWidth;
        } catch (FieldEmptyException e) {
            e.printStackTrace();
        }
        return popupWidth;
    }

    public String getPopupHeight() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        try {
            initializePopupOptions();
            return popupHeight;
        } catch (FieldEmptyException e) {
            e.printStackTrace();
        }
        return popupHeight;
    }

    public String getSecureFeature() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		try {
			return (String)getProperty("secureFeature");
		} catch (FieldEmptyException e) {
			return "";
		}
	}

    public String getSecureAccess() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		try {
			return (String)getProperty("secureAccess");
		} catch (FieldEmptyException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public String getParameterString() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		try {
			return (String)getProperty("parameterString");
		} catch (FieldEmptyException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public String getGrouping(){
		try {
			return (String)getProperty("grouping");
		} catch (Exception e) {
			return "";
		}
	}

    private void initializePopupOptions() throws ElementNotFoundException, FieldNotFoundException {
        // If popup is null, we haven't initialized this.
        if (popup == null) {
            try {
                // So try to initialize from the property.
                popup = (String) getProperty("popup");
            } catch (FieldEmptyException ignored) {
                // If that failed, just do nothing.
            }
            // If it's still blank, set it to the default.
            if (StringUtils.isBlank(popup)) {
                popup = "never";
            } else {
                // If it's not blank, it might have dimension data.
                Matcher matcher = pattern.matcher(popup);
                if (matcher.matches()) {
                    popup = matcher.group(1);
                    popupWidth = matcher.group(2);
                    popupHeight = matcher.group(3);
                }
            }
        }
    }

    private static final Pattern pattern = Pattern.compile("(\\w+):(\\d+),(\\d+)", Pattern.CASE_INSENSITIVE);
    private String popup = null;
    private String popupWidth = "700";
    private String popupHeight = "600";
}

