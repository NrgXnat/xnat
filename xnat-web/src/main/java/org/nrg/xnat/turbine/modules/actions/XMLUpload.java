/*
 * web: org.nrg.xnat.turbine.modules.actions.XMLUpload
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import static org.nrg.xdat.turbine.modules.screens.XMLUpload.MESSAGE_NO_GUEST_PERMISSIONS;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.turbine.services.pull.tools.TemplateLink;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.turbine.modules.actions.DisplayItemAction;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.*;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.archive.CatalogService;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tim
 */
@SuppressWarnings("unused")
@Slf4j
public class XMLUpload extends SecureAction {
    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache
     * .turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doPerform(final RunData data, final Context context) throws Exception {
        final UserI user = getUser();
        if (user.isGuest()) {
            handleInvalidPermissions(data, null, MESSAGE_NO_GUEST_PERMISSIONS);
            return;
        }

        // get the ParameterParser from RunData
        final ParameterParser parameters    = data.getParameters();
        final FileItem        fileItem      = parameters.getFileItem("xml_to_store");
        final String          allowDeletion = (String) TurbineUtils.GetPassedParameter("allowdeletion", data);

        if (fileItem != null && allowDeletion != null) {
            if (fileItem.getSize() == 0) {
                data.setScreenTemplate("Error.vm");
                data.setMessage("The file you uploaded appears to be empty. Please verify that your file contains valid XML.");
                final Map<String, String> links = new HashMap<>();
                links.put(getLink(context), "Return to the XML Upload page");
                context.put("links", links);
                return;
            }

            XFTItem item = null;
            try {
                item = getCatalogService().insertXmlObject(user, fileItem.getInputStream(), BooleanUtils.toBoolean(allowDeletion), TurbineUtils.GetTurbineParameters(data, context));

                final DisplayItemAction displayItemAction = new DisplayItemAction();
                displayItemAction.doPerform(TurbineUtils.SetSearchProperties(data, item), context);
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
            } catch (SAXParseException e) {
                if (e.getLineNumber() == -1 && e.getColumnNumber() == -1) {
                    // This probably means they uploaded an empty file.
                    data.setMessage("XNAT was unable to parse the uploaded document. The document appears to be empty.");
                } else if (e.getLineNumber() == 1 && e.getColumnNumber() == 1) {
                    // This probably means they uploaded a non-XML file.
                    data.setMessage("XNAT was unable to parse the uploaded document. Check that your uploaded file is valid XML.");
                } else {
                    // This probably means they uploaded an XML file with errors.
                    data.setMessage("<p>XNAT failed while parsing the uploaded document. Error found at line " + e.getLineNumber() + ", column " + e.getColumnNumber() + ". The specific error message was:</p><p>" + e.getMessage() + "</p>");
                }
                data.setScreenTemplate("Error.vm");
                final Map<String, String> links = new HashMap<>();
                links.put(getLink(context), "Return to the XML Upload page");
                context.put("links", links);
                log.error("", e);
            } catch (InvalidPermissionException e) {
                log.error("The user {} tried to perform an illegal operation when uploading XML", user.getUsername(), e);
                handleInvalidPermissions(data, item, e.getMessage());
            } catch (Exception e) {
                log.error("", e);
                data.setScreenTemplate("Error.vm");
                data.setMessage(e.getMessage());
            }
        }
    }

    private void handleInvalidPermissions(final RunData data, final XFTItem item, final String note) throws XFTInitException, ElementNotFoundException {
        data.setScreenTemplate("Error.vm");
        final StringBuilder message = new StringBuilder("Permissions Exception.<BR><BR>").append(note);
        if (item != null) {
            final SchemaElement   se = SchemaElement.GetElement(item.getXSIType());
            final ElementSecurity es = se.getElementSecurity();
            if (es != null && es.getSecurityFields() != null) {
                message.append("<BR><BR>Please review the security field (").append(se.getElementSecurity().getSecurityFields()).append(") for this data type.");
                message.append(" Verify that the data reflects a currently stored value and the user has relevant permissions for this data.");
            }
        }
        data.setMessage(message.toString());
    }

    private CatalogService getCatalogService() {
        return XDAT.getContextService().getBean(CatalogService.class);
    }

    private String getLink(final Context context) {
        return ((TemplateLink) context.get("link")).setPage(TEMPLATE).getLink();
    }

    private static final String TEMPLATE = "XMLUpload.vm";
}
