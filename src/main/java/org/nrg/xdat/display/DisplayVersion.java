/*
 * core: org.nrg.xdat.display.DisplayVersion
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.display;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.nrg.xdat.collections.DisplayFieldRefCollection;
import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tim
 */
public class DisplayVersion extends DisplayFieldRefCollection {
    private static final Logger logger = LoggerFactory.getLogger(DisplayVersion.class);
    private String versionName = "";
    private String defaultOrderBy = "";
    private String defaultSortOrder = "";
    private String briefDescription = "";
    private String darkColor = "";
    private String lightColor = "";
    private boolean allowDiffs = true;
    private ElementDisplay parentElementDisplay = null;

    private HTMLCell headerCell = new HTMLCell();


    /**
     * Gets the version name.
     *
     * @return The version name.
     */
    public String getVersionName() {
        return versionName;
    }


    /**
     * Sets the version name.
     *
     * @param versionName The version name to set.
     */
    public void setVersionName(final String versionName) {
        this.versionName = versionName;
    }

    /**
     * Gets the default order-by column.
     *
     * @return The default order-by column.
     */
    public String getDefaultOrderBy() {
        return defaultOrderBy;
    }

    /**
     * Sets the default order-by column.
     *
     * @param defaultOrderBy The default order-by column to set.
     */
    public void setDefaultOrderBy(final String defaultOrderBy) {
        this.defaultOrderBy = defaultOrderBy;
    }

    /**
     * @return Returns the defaultSortOrder.
     */
    public String getDefaultSortOrder() {
        if (defaultSortOrder == null || defaultSortOrder.equals("")) {
            return "ASC";
        } else {
            return defaultSortOrder;
        }
    }

    /**
     * @param defaultSortOrder The defaultSortOrder to set.
     */
    public void setDefaultSortOrder(String defaultSortOrder) {
        this.defaultSortOrder = defaultSortOrder;
    }

    /**
     * Gets the brief description of the display version.
     *
     * @return The brief description.
     */
    public String getBriefDescription() {
        if (briefDescription == null || briefDescription.equals("")) {
            return this.getParentElementDisplay().getBriefDescription();
        } else {
            return briefDescription;
        }
    }

    /**
     * Gets the dark color setting.
     *
     * @return The dark color setting.
     */
    public String getDarkColor() {
        return darkColor;
    }

    /**
     * Gets the light color setting.
     *
     * @return The light color setting.
     */
    public String getLightColor() {
        return lightColor;
    }

    /**
     * Sets the brief description of the display version.
     *
     * @param briefDescription The brief description to set.
     */
    public void setBriefDescription(final String briefDescription) {
        this.briefDescription = briefDescription;
    }

    /**
     * Sets the dark color setting for the display version.
     *
     * @param darkColor The dark color setting to set.
     */
    public void setDarkColor(final String darkColor) {
        this.darkColor = darkColor;
    }

    /**
     * Sets the light color setting for the display version.
     *
     * @param lightColor The light color setting to set.
     */
    public void setLightColor(final String lightColor) {
        this.lightColor = lightColor;
    }

    /**
     * Returns the visible DisplayFields
     *
     * @return ArrayList of DisplayFields
     */
    public ArrayList<DisplayFieldReferenceI> getVisibleFields() {
        ArrayList<DisplayFieldReferenceI> al = new ArrayList<>();
        Iterator iter = this.getDisplayFieldRefIterator();
        while (iter.hasNext()) {
            DisplayFieldRef df = (DisplayFieldRef) iter.next();
            try {
                if (df.getDisplayField().isVisible()) {
                    al.add(df);
                }
            } catch (DisplayFieldNotFoundException e) {
                if (df.getType() != null) {
                    if (df.getType().equalsIgnoreCase("COUNT")) {
                        al.add(df);
                    }
                } else {
                    logger.error("", e);
                }
            }
        }
        al.trimToSize();
        return al;
    }

    /**
     * Returns the all defined DisplayFields
     *
     * @return ArrayList of DisplayFields
     */
    public ArrayList<DisplayFieldReferenceI> getAllFields() {
        ArrayList al = new ArrayList();
        Iterator iter = this.getDisplayFieldRefIterator();
        while (iter.hasNext()) {
            DisplayFieldRef df = (DisplayFieldRef) iter.next();
            al.add(df);
        }
        al.trimToSize();
        return al;
    }

    /**
     * Gets the parent {@link ElementDisplay element display}.
     *
     * @return The parent {@link ElementDisplay element display}.
     */
    public ElementDisplay getParentElementDisplay() {
        return parentElementDisplay;
    }

    /**
     * Sets the parent {@link ElementDisplay element display}.
     *
     * @param display The display to set as the parent {@link ElementDisplay element display}.
     */
    public void setParentElementDisplay(ElementDisplay display) {
        parentElementDisplay = display;
    }

    /**
     * Gets the {@link HTMLCell header cell}.
     *
     * @return The {@link HTMLCell header cell}.
     */
    public HTMLCell getHeaderCell() {
        return headerCell;
    }

    /**
     * Sets the {@link HTMLCell HTML cell}.
     *
     * @param cell The cell to set.
     */
    @SuppressWarnings("unused")
    public void setHeaderCell(HTMLCell cell) {
        headerCell = cell;
    }

    @SuppressWarnings("unused")
    public Collection getDisplayFields() {
        ArrayList al = new ArrayList();

        Iterator iter = this.getDisplayFieldRefIterator();
        while (iter.hasNext()) {
            DisplayFieldRef ref = (DisplayFieldRef) iter.next();
            try {
                al.add(ref.getDisplayField());
            } catch (DisplayFieldNotFoundException e) {
                logger.error("", e);
            }
        }

        return al;
    }

    /**
     * @return Returns the allowDiffs.
     */
    public boolean isAllowDiffs() {
        return allowDiffs;
    }

    /**
     * @param allowDiffs The allowDiffs to set.
     */
    public void setAllowDiffs(boolean allowDiffs) {
        this.allowDiffs = allowDiffs;
    }
}

