/*
 * org.nrg.xdat.display.SchemaLink
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display;

/**
 * @author Tim
 */
public class SchemaLink extends ViewLink {
    private String rootElement = "";
    private String element     = "";
    private String type        = "";
    private String alias       = "";

    public SchemaLink(final String root) {
        rootElement = root;
    }

    /**
     * Gets the element.
     *
     * @return The element.
     */
    public String getElement() {
        return element;
    }

    /**
     * Sets the element.
     *
     * @param element The element to set.
     */
    public void setElement(final String element) {
        this.element = element;
    }

    /**
     * Gets the type.
     *
     * @return The type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type The type to set.
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Gets the alias.
     *
     * @return The alias.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the alias.
     *
     * @param alias The alias to set.
     */
    public void setAlias(final String alias) {
        this.alias = alias;
    }

    /**
     * Gets the root element name.
     *
     * @return The root element name.
     */
    public String getRootElement() {
        return rootElement;
    }

    /**
     * Sets the root element name.
     *
     * @param element The name of the root element.
     */
    public void setRootElement(final String element) {
        rootElement = element;
    }
}

