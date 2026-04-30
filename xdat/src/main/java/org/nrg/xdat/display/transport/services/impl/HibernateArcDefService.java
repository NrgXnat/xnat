/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.services.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.display.ArcDefinition;
import org.nrg.xdat.display.transport.dao.ArcDefinitionDAO;
import org.nrg.xdat.display.transport.entities.DisplayArcDefFiltersDB;
import org.nrg.xdat.display.transport.entities.DisplayArcDefinitionDB;
import org.nrg.xdat.display.transport.services.ArcDefService;
import org.python.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author Tim Olsen
 *
 * Service implementation for ArcDefs stored via Hibernate.
 */

@Service
@Transactional
public class HibernateArcDefService extends AbstractHibernateEntityService<DisplayArcDefinitionDB, ArcDefinitionDAO> implements ArcDefService {
    /**
     * Finds the SQLObject by name
     *
     * @param name The name for the configuration
     *
     * @return
     */
    @Override
    public ArcDefinition findArcDefByName(final String name) {
        final DisplayArcDefinitionDB arcDef= getDao().findByName(name,false);
        return wrapArcDefObject(arcDef);
    }

    private ArcDefinition wrapArcDefObject(final DisplayArcDefinitionDB storedAD) {
        final ArcDefinition arcDefine = new ArcDefinition();
        arcDefine.setName(storedAD.getName());
        arcDefine.setBridgeElement(storedAD.getBridgeElement());
        arcDefine.setBridgeField(storedAD.getBridgeField());

        for(final Map.Entry<String,String> entry: storedAD.getCommonFields().entrySet()){
            arcDefine.addCommonField(entry.getKey(), entry.getValue());
        }

        for(final DisplayArcDefFiltersDB arcDefFilters : storedAD.getFilters()){
            arcDefine.addFilter(arcDefFilters.getFilterKey(),arcDefFilters.getFilterType());
        }
        return arcDefine;
    }

    @Override
    public List<ArcDefinition> findAllArcDefs() {
        final List<ArcDefinition> _return = Lists.newArrayList();

        for(final DisplayArcDefinitionDB arcDef: getDao().findAllEnabled()){
            _return.add(wrapArcDefObject(arcDef));
        }
        return _return;
    }

    /**
     * Finds and deactivate the view with the given name
     *
     * @param name The name of the view to deactivate
     */
    @Override
    public void deleteByName(String name) {
        final DisplayArcDefinitionDB view = this.getDao().findByName(name,true);
        if (view != null) {
            this.getDao().delete(view);
        }
    }

    @Override
    public DisplayArcDefinitionDB createArcDef(final ArcDefinition tempAD){
        if(this.getDao().findByName(tempAD.getName(),true)!=null){
            return getDao().findByName(tempAD.getName(),false);
        }

        final DisplayArcDefinitionDB arcDefine = new DisplayArcDefinitionDB();
        arcDefine.setName(tempAD.getName());
        arcDefine.setBridgeElement(tempAD.getBridgeElement());
        arcDefine.setBridgeField(tempAD.getBridgeField());

        for(final Map.Entry<String,String> entry: tempAD.getCommonFields().entrySet()){
            if(StringUtils.isNotBlank(entry.getKey()) &&  StringUtils.isNotBlank(entry.getValue())){
                arcDefine.addCommonField(entry.getKey(), entry.getValue());
            }
        }

        final List<DisplayArcDefFiltersDB> filters = Lists.newArrayList();
        for(final String[] adFilter : tempAD.getFilters()){
            DisplayArcDefFiltersDB filt = new DisplayArcDefFiltersDB();
            filt.setFilterKey(adFilter[0]);
            filt.setFilterType(adFilter[1]);

            filters.add(filt);
        }

        arcDefine.setFilters(filters);

        this.getDao().create(arcDefine);

        return getDao().findByName(arcDefine.getName(),false);
    }

}
