/*
 * org.nrg.xdat.turbine.modules.screens.XMLSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * @author timo
 */
public class XMLSearch extends XDATRawScreen {
    private static final Logger logger = LoggerFactory.getLogger(XMLSearch.class);

    /**
     * {@inheritDoc}
     */
    public String getContentType(RunData data) {
        return "text/xml";
    }

    /**
     * {@inheritDoc}
     */
    protected final void doOutput(RunData data) throws Exception {
        final String username = ((String) TurbineUtils.GetPassedParameter("username", data));
        final String password = ((String) TurbineUtils.GetPassedParameter("password", data));
        UserI user = XDAT.getUserDetails();
        if (user == null) {
            if (username != null && password != null) {
                user = Authenticator.Authenticate(new Authenticator.Credentials(username, password));
                data.getSession().invalidate();
            }
        }
        if (user != null) {
            try {
                String dataType = ((String) TurbineUtils.GetPassedParameter("data_type", data));
                String id       = ((String) TurbineUtils.GetPassedParameter("id", data));

                GenericWrapperElement element      = GenericWrapperElement.GetElement(dataType);
                String                functionName = element.getTextFunctionName();

                String     query    = "SELECT " + functionName + "(";
                Object[][] keyArray = element.getSQLKeys();

                for (int i = 0; i < keyArray.length; i++) {
                    if (i > 0) {
                        query += ",";
                    }
                    query += DBAction.ValueParser(id, ((GenericWrapperField) keyArray[i][3]), true);
                }
                query += ",0,FALSE,TRUE,FALSE)";

                String  s    = (String) PoolDBUtils.ReturnStatisticQuery(query, functionName, element.getDbName(), username);
                XFTItem item = XFTItem.PopulateItemFromFlatString(s, user);

                HttpServletResponse response = data.getResponse();
                response.setContentType("text/xml");

                if (item == null) {
                    data.setMessage("No Item found for XML display.");
                    data.setScreenTemplate("Index.vm");
                } else {
                    writeToXml(item, response);
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }
}
