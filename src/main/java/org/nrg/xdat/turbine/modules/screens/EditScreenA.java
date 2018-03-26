/*
 * core: org.nrg.xdat.turbine.modules.screens.EditScreenA
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
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.XftStringUtils;

/**
 * @author Tim
 */
@Slf4j
public abstract class EditScreenA extends SecureScreen {
    /**
     * ArrayList of Object[3] {xmlPath,option,(Possible Values)ArrayList of ArrayList(2){value,display},defaultVALUE}
     *
     * @return The element name.
     */
    public abstract String getElementName();

    public abstract void finalProcessing(RunData data, Context context);

    public ItemI getEmptyItem(final RunData data) throws Exception {
        return XFTItem.NewItem(getElementName(), XDAT.getUserDetails());
    }

    public String getStringIdentifierForPassedItem(final RunData data) {
        if (TurbineUtils.HasPassedParameter("tag", data)) {
            return (String) TurbineUtils.GetPassedParameter("tag", data);
        } else {
            return "edit_item";
        }
    }

    public void doBuildTemplate(RunData data, Context context) {
        try {
            if (TurbineUtils.HasPassedParameter("destination", data)) {
                context.put("destination", TurbineUtils.GetPassedParameter("destination", data));
            }
            context.put("edit_screen", data.getScreen());

            if (TurbineUtils.HasPassedParameter("tag", data)) {
                context.put("tag", TurbineUtils.GetPassedParameter("tag", data));
            }

            if (TurbineUtils.GetPassedParameter("source", data) != null) {
                context.put("source", TurbineUtils.GetPassedParameter("source", data));
            }

            item = null;

            if (!getStringIdentifierForPassedItem(data).equals("edit_item")) {
                item = (ItemI) data.getSession().getAttribute(getStringIdentifierForPassedItem(data));
                if (item != null) {
                    data.getSession().removeAttribute(getStringIdentifierForPassedItem(data));
                } else {
                    item = (ItemI) TurbineUtils.GetEditItem(data);
                }

            } else {
                item = (ItemI) TurbineUtils.GetEditItem(data);
            }

            if (item != null) {
                if ((!item.getXSIType().equals(getElementName())) && (!(item.getItem()).getGenericSchemaElement().isExtensionOf(GenericWrapperElement.GetElement(getElementName())))) {
                    item = null;
                }
            }

            if (item == null) {
                log.debug("No edit item passed... looking for item passed by variables");
                try {
                    ItemI temp = TurbineUtils.GetItemBySearch(data);
                    if (temp != null) {
                        if (temp.getXSIType().equalsIgnoreCase(getElementName())) {
                            item = temp;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            context.put("edit_screen", XftStringUtils.getLocalClassName(this.getClass()) + ".vm");
            if (item == null) {
                try {
                    String s = getElementName();
                    item = getEmptyItem(data);

                    log.info("No passed item found. Created new item of type {}", item.getXSIType());
                    SchemaElementI se = SchemaElement.GetElement(item.getXSIType());

                    context.put("item", item);
                    context.put("element", se);
                    context.put("search_element", s);

                    context.put("om", BaseElement.GetGeneratedItem(item));
                    finalProcessing(data, context);
                } catch (Exception e) {
                    log.warn(ERROR_CREATING_ITEM, e);
                    data.setMessage(ERROR_CREATING_ITEM);
                    data.setScreen("Index");
                    TurbineUtils.OutputPassedParameters(data, context, getClass().getName());
                }
            } else {
                try {
                    SchemaElementI se = SchemaElement.GetElement(item.getXSIType());
                    if (context.get("source") == null) {
                        context.put("source", "XDATScreen_report_" + se.getFormattedName() + ".vm");
                    }
                    context.put("item", item);
                    context.put("element", org.nrg.xdat.schema.SchemaElement.GetElement(item.getXSIType()));
                    context.put("search_element", TurbineUtils.GetPassedParameter("search_element", data));
                    context.put("search_field", TurbineUtils.GetPassedParameter("search_field", data));
                    context.put("search_value", TurbineUtils.GetPassedParameter("search_value", data));

                    context.put("om", BaseElement.GetGeneratedItem(item));
                    finalProcessing(data, context);
                } catch (Exception e) {
                    log.warn(NO_DATA_ITEM_FOUND, e);
                    data.setMessage(NO_DATA_ITEM_FOUND);
                    data.setScreen("Index");
                    TurbineUtils.OutputPassedParameters(data, context, this.getClass().getName());
                }

            }
        } catch (Exception e) {
            log.warn(NO_DATA_ITEM_FOUND, e);
            data.setMessage(NO_DATA_ITEM_FOUND);
            data.setScreen("Index");
            TurbineUtils.OutputPassedParameters(data, context, getClass().getName());
        }

        preserveVariables(data, context);
    }

    protected ItemI getEditItem() {
        return item;
    }

    protected boolean hasEditItem() {
        return item != null;
    }

    private static final String NO_DATA_ITEM_FOUND = "Invalid Search Parameters: No data item found.";
    private static final String ERROR_CREATING_ITEM = "Invalid Search Parameters: Error creating item.";

    private ItemI item = null;
}
