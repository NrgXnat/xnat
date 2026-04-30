/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.collections.DisplayFieldRefCollection;
import org.nrg.xdat.display.transport.entities.DisplayFieldDB;
import org.nrg.xdat.display.transport.entities.DisplayVersionDB;
import org.nrg.xdat.display.transport.entities.ElementDisplayDB;
import org.nrg.xdat.display.transport.services.impl.HibernateElementDisplayService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ElementDisplayStorageService extends BaseHibernateService<ElementDisplayDB> {
    /**
     * Find stored Element Display by xsi:type
     * @param elementName
     * @return
     */
    @Transactional
    ElementDisplayDB findByElementName(String elementName);

    /**
     * Convert from in-memory version of DisplayVersion to storable object.
     * @param dv
     * @return
     */
    @Transactional
    DisplayVersionDB convertDisplayVersion(org.nrg.xdat.display.DisplayVersion dv);

    /**
     * @return
     */
    @Transactional
    List<org.nrg.xdat.display.ElementDisplay> findAllActive();

    /**
     * @param elementName
     * @param df
     * @throws HibernateElementDisplayService.NoSuchElementException
     * @throws HibernateElementDisplayService.FieldAlreadyExists
     */
    @Transactional
    void addDisplayField(String elementName, DisplayFieldDB df) throws HibernateElementDisplayService.NoSuchElementException, HibernateElementDisplayService.FieldAlreadyExists;

    /**
     * Create a copy of the specified displayField with the specified values.
     * @param elementName
     * @param displayField
     * @param header
     * @param value
     * @return
     * @throws HibernateElementDisplayService.NoFieldFound
     * @throws HibernateElementDisplayService.NoSuchElementException
     */
    @Transactional
    org.nrg.xdat.display.DisplayField addSqlQueryValue(String elementName, String displayField, String header, String value) throws HibernateElementDisplayService.NoFieldFound, HibernateElementDisplayService.NoSuchElementException;

    /**
     * Configure the specified display field.  If it isn't already created, it will be created based on the in-memory version.
     * @param elementName
     * @param displayField
     * @param header
     * @param searchable
     * @param description
     * @throws HibernateElementDisplayService.NoFieldFound
     * @throws HibernateElementDisplayService.NoSuchElementException
     */
    @Transactional
    void configureDisplayField(String elementName, String displayField, String header, Boolean searchable, String description) throws HibernateElementDisplayService.NoFieldFound, HibernateElementDisplayService.NoSuchElementException;

    /**
     * @param elementName
     * @param dv
     * @throws HibernateElementDisplayService.NoFieldFound
     * @throws HibernateElementDisplayService.NoSuchElementException
     * @throws DisplayFieldRefCollection.DisplayFieldRefNotFoundException
     * @throws HibernateElementDisplayService.NoVersionFound
     */
    @Transactional
    void addOrUpdateDisplayVersion(String elementName, DisplayVersionDB dv) throws HibernateElementDisplayService.NoFieldFound, HibernateElementDisplayService.NoSuchElementException, DisplayFieldRefCollection.DisplayFieldRefNotFoundException, HibernateElementDisplayService.NoVersionFound;


    @Transactional
    void saveElementDisplay(org.nrg.xdat.display.ElementDisplay ed);

    org.nrg.xdat.display.ElementDisplay renderElementDisplay(ElementDisplayDB storedED);
}
