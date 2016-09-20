/*
 * core: org.nrg.xdat.display.ViewLink
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.display;

/**
 * @author Tim
 */
public class ViewLink {
    private Mapping mapping = null;
    private String  alias   = "";

    /**
     * Gets the view link mapping.
     *
     * @return The view link mapping.
     */
    public Mapping getMapping() {
        return mapping;
    }

    /**
     * Sets the view link mapping.
     *
     * @param mapping The mapping to set.
     */
    public void setMapping(final Mapping mapping) {
        this.mapping = mapping;
    }

    /**
     * Gets the view link alias.
     *
     * @return The view link alias.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the view link alias.
     *
     * @param alias The view link alias to set.
     */
    public void setAlias(final String alias) {
        this.alias = alias;
    }

}

