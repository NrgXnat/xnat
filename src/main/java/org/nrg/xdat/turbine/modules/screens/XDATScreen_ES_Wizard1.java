/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_ES_Wizard1
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;

/**
 * @author Tim
 */
@Slf4j
@SuppressWarnings("unused")
public class XDATScreen_ES_Wizard1 extends AdminEditScreenA {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getElementName() {
        return ElementSecurity.SCHEMA_ELEMENT_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finalProcessing(RunData data, Context context) {
        context.put("hasDefaultField", hasDefaultPrimarySecurityField());
    }

    private boolean hasDefaultPrimarySecurityField() {
        try {
            final SchemaElement element = SchemaElement.GetElement(getEditItem().getStringProperty("element_name"));
            return element.getDefaultPrimarySecurityField() != null;
        } catch (XFTInitException e) {
            log.error("An error occurred trying to access XFT when trying to get the element_name property from this ItemI object:\n{}", getEditItem(), e);
        } catch (ElementNotFoundException e) {
            log.error("Couldn't find the element of type {}: {}", e.ELEMENT, e);
        } catch (FieldNotFoundException e) {
            log.error("Couldn't find the field named {} for type {}: {}", e.FIELD, getEditItem().getXSIType(), e.MESSAGE);
        }
        return false;
    }
}
