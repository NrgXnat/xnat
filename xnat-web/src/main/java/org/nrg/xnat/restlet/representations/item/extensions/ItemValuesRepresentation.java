/*
 * org.nrg.xnat.restlet.representations.item.extensions.ItemValuesRepresentation
 * XNAT http://www.xnat.org
 * Copyright (c) 2015, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2015-07-15 11:15 AM
 */
package org.nrg.xnat.restlet.representations.item.extensions;

import org.apache.log4j.Logger;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Hashtable;

/**
 * @author Tim Olsen <tim@deck5consulting.com>
 * @author Flavin <flavinj@mir.wustl.edu>
 *
 * This is an ItemHandler class. I can make a REST request that returns an object, and specify the query param
 * handler=values, and SecureResource will pass that object to this class for representation.
 *
 * The request needs to specify a set of one or more of the object's fields as a csv, either in the request body
 * or as a query param "columns=field1,field2,...". We can then look up those fields and return their values.
 *
 * If only one field is requested, and the query also contains the parameter "asValues=true", we simply return
 * the requested value as a string. Otherwise, we return a table.
 */
@SuppressWarnings("unused")
public class ItemValuesRepresentation implements SecureResource.ItemHandlerI {
    public static final String HANDLER = "values";
    public static final String COLUMNS = "columns";
    public static Logger logger = Logger.getLogger(ItemValuesRepresentation.class);


    @Override
    public String getHandlerString() {
        return HANDLER;
    }

    @Override
    public Representation handle(XFTItem item, MediaType mt, SecureResource resource) {
        String[] columns;
        if (!resource.hasQueryVariable(COLUMNS)) {
            //then the expected fields are in the body, parse them
            if (resource.getRequest().isEntityAvailable() && resource.getRequest().getEntity() != null && resource.getRequest().getEntity().getSize() > 0) {
                try {
                    columns = resource.getRequest().getEntity().getText().split(",");
                } catch (UnsupportedEncodingException e) {
                    logger.error("",e);
                    resource.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Error decoding URL. Ensure all characters in URL are in UTF-8.");
                    return null;
                } catch (IOException e) {
                    resource.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
                    return null;
                }
            } else {
                resource.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Please include a list of columns to return.");
                return null;
            }
        } else {
            try {
                columns = URLDecoder.decode(resource.getQueryVariable(COLUMNS), "UTF-8").split(",");
            } catch (UnsupportedEncodingException e) {
                logger.error("",e);
                resource.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Error decoding URL. Ensure all characters in URL are in UTF-8.");
                return null;
            }
        }


        Object[] row = new Object[columns.length];
        int count = 0;
        for (String field : columns) {
            try {
                row[count++] = item.getProperty(field);
            } catch (ElementNotFoundException | FieldNotFoundException e) {
                resource.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
                return null;
            } catch (XFTInitException e) {
                resource.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
                return null;
            }
        }

        if (resource.isQueryVariableTrue("asValue")) {
            if (columns.length == 1) {
            	if(row[0]!=null){
                    return new StringRepresentation(row[0].toString());
            	}else{
            		return new StringRepresentation("");
            	}
            } else {
                resource.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "asValue can only be used on single-column queries");
                return null;
            }
        } else {
            XFTTable table = new XFTTable();
            table.initTable(columns);
            table.insertRow(row);
            return resource.representTable(table, mt, new Hashtable<String, Object>());
        }

    }
}
