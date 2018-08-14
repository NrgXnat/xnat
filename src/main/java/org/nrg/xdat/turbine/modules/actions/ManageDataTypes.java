/*
 * core: org.nrg.xdat.turbine.modules.actions.ManageDataTypes
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XdatElementSecurity;
import org.nrg.xdat.om.XdatSecurity;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.utils.SaveItemHelper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ManageDataTypes extends AdminAction {
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        final PopulateItem populater  = PopulateItem.Populate(data, "xdat:security", true);
        final XFTItem      found      = populater.getItem();
        final List<String> updatedIds = new ArrayList<>();

        for (final XdatElementSecurity elementSecurity : new XdatSecurity(found).getElementSecuritySet_elementSecurity()) {
            if (elementSecurity.getProperty("accessible") == null) {
                elementSecurity.setAccessible("false");
            }
            if (elementSecurity.getProperty("secure") == null) {
                elementSecurity.setSecure("false");
            }
            if (elementSecurity.getProperty("browse") == null) {
                elementSecurity.setBrowse("false");
            }
            if (elementSecurity.getProperty("searchable") == null) {
                elementSecurity.setSearchable("false");
            }

            final String              elementName = elementSecurity.getElementName();
            final XdatElementSecurity current     = XdatElementSecurity.getXdatElementSecuritysByElementName(elementName, getUser(), false);

            log.debug("Found existing element security for {} data type, comparing with submitted version to see if there was a change.", elementName);
            if (current == null ||
                current.getSecure() != elementSecurity.getSecure() ||
                current.getAccessible() != elementSecurity.getAccessible() ||
                current.getBrowse() != elementSecurity.getBrowse() ||
                current.getSearchable() != elementSecurity.getSearchable() ||
                current.getSequence() != elementSecurity.getSequence() ||
                !StringUtils.equals(current.getCode(), elementSecurity.getCode()) ||
                !StringUtils.equals(current.getSingular(), elementSecurity.getSingular()) ||
                !StringUtils.equals(current.getPlural(), elementSecurity.getPlural())) {
                if (current == null) {
                    log.info("New element security for {} data type was created. Saving it now.", elementName);
                } else {
                    log.info("Element security for {} data type was updated. Saving it now.", elementName);
                }
                updatedIds.add(elementName);
                SaveItemHelper.authorizedSave(elementSecurity, getUser(), false, false, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified Data-type (batch)"));
            } else {
                log.debug("Element security for data type {} wasn't modified.", elementName);
            }
        }

        if (!updatedIds.isEmpty()) {
            ElementSecurity.refresh();
            XDAT.triggerEvent(XftItemEvent.builder().action(XftItemEvent.UPDATE).xsiType(XdatElementSecurity.SCHEMA_ELEMENT_NAME).ids(updatedIds).build());
            data.setMessage("Data-Types modified.");
        } else {
            data.setMessage("No changes were detected to the data-type configuration.");
        }
        data.setScreenTemplate("XDATScreen_dataTypes.vm");
    }
}
