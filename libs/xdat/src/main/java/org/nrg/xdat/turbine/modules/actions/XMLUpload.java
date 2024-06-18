/*
 * core: org.nrg.xdat.turbine.modules.actions.XMLUpload
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.actions;

import static org.nrg.xdat.turbine.modules.screens.XMLUpload.MESSAGE_NO_GUEST_PERMISSIONS;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.velocity.context.Context;
import org.nrg.framework.services.impl.ValidationHandler;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.*;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.ValidationUtils.XFTValidator;
import org.nrg.xft.utils.XMLValidator;
import org.xml.sax.SAXParseException;

import java.io.IOException;

/**
 * @author Tim
 */
@Slf4j
@SuppressWarnings("unused")
public class XMLUpload extends SecureAction {
    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doPerform(final RunData data, final Context context) throws Exception {
        final UserI user = getUser();
        if (user.isGuest()) {
            handleInvalidPermissions(data);
            return;
        }

        // get the ParameterParser from RunData
        final ParameterParser params = data.getParameters();

        //grab the FileItems available in ParameterParser
        final FileItem          fileItem = params.getFileItem("xml_to_store");
        final ValidationHandler handler  = new XMLValidator().validateInputStream(fileItem.getInputStream());
        if (!handler.assertValid()) {
            throw handler.getErrors().get(0);
        }

        final String    allowDeletion = (String) TurbineUtils.GetPassedParameter("allowdeletion", data);
        final SAXReader reader        = new SAXReader(XDAT.getUserDetails());
        XFTItem         item          = null;
        if (allowDeletion != null) {
            try {
                item = reader.parse(fileItem.getInputStream());
                log.info("Loaded XML Item: {}", item.getProperName());

                final ValidationResults vr = XFTValidator.Validate(item);
                if (vr.isValid()) {
                    log.info("Validation: PASSED");

                    SaveItemHelper.unauthorizedSave(item, XDAT.getUserDetails(), false, item.getGenericSchemaElement().isQuarantine(), false, allowDeletion.equalsIgnoreCase("true"), newEventInstance(data, EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.STORE_XML));

                    log.info("Item Successfully Stored.");

                    final RunData           searchData = TurbineUtils.SetSearchProperties(data, item);
                    final DisplayItemAction dia        = new DisplayItemAction();
                    dia.doPerform(searchData, context);
                    postProcessing(item);
                } else {
                    throw new ValidationException(vr);
                }
            } catch (IOException e) {
                log.error("", e);
                data.setScreenTemplate("Error.vm");
                data.setMessage("Error loading document.");
            } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
                log.error("", e);
            } catch (ValidationException e) {
                log.error("", e);
                data.setScreenTemplate("Error.vm");
                data.setMessage("XML Validation Exception.<BR>" + e.getValidation().toHTML());
            } catch (Exception e) {
                if (e instanceof SAXParseException) {
                    log.error("", e);
                    data.setScreenTemplate("Error.vm");
                    data.setMessage("SAX Parser Exception.<BR><BR>" + e.getMessage());
                } else if (item != null && e instanceof InvalidPermissionException) {
                    log.error("", e);
                    data.setScreenTemplate("Error.vm");
                    final StringBuilder   message = new StringBuilder("Permissions Exception.<BR><BR>").append(e.getMessage());
                    final SchemaElement   se      = SchemaElement.GetElement(item.getXSIType());
                    final ElementSecurity es      = se.getElementSecurity();
                    if (es != null && es.getSecurityFields() != null) {
                        message.append("<BR><BR>Please review the security field (").append(se.getElementSecurity().getSecurityFields()).append(") for this data type.");
                        message.append(" Verify that the data reflects a currently stored value and the user has relevant permissions for this data.");
                    }
                    data.setMessage(message.toString());
                } else {
                    log.error("", e);
                    data.setScreenTemplate("Error.vm");
                    data.setMessage(e.getMessage());
                }
            }
        }
    }

    public void postProcessing(final XFTItem item) throws Exception {
        final SchemaElementI schemaElement = SchemaElement.GetElement(item.getXSIType());
        if (StringUtils.equalsIgnoreCase("xdat", schemaElement.getGenericXFTElement().getType().getLocalPrefix()) ||
            StringUtils.equalsAny(schemaElement.getFullXMLName(), "xnat:investigatorData", "xnat:projectData")) {
            ElementSecurity.refresh();
        }
    }

    private void handleInvalidPermissions(final RunData data) {
        data.setScreenTemplate("Error.vm");
        data.setMessage("Permissions Exception.<BR><BR>" + MESSAGE_NO_GUEST_PERMISSIONS);
    }
}
