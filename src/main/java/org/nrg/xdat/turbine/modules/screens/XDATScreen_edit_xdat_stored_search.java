//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * GENERATED FILE
 * Created on Fri Sep 16 10:14:37 CDT 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XDATUser.UserNotFoundException;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;


/**
 * @author XDAT
 *
 */
public class XDATScreen_edit_xdat_stored_search extends AdminEditScreenA {
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_xdat_stored_search.class);
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
	 */
	public String getElementName() {
	    return "xdat:stored_search";
	}

	public ItemI getEmptyItem(RunData data) throws Exception
	{
	    String s = getElementName();
		ItemI temp =  XFTItem.NewItem(s,TurbineUtils.getUser(data));
		return temp;
	}
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void finalProcessing(RunData data, Context context) {
		Iterator<String> itr = (XDATUser.getAllLogins()).iterator();
		ArrayList<String> users = new ArrayList<String>();
		Hashtable<String,String> users_h = new Hashtable<String,String>();
		while (itr.hasNext()) {
			String login = itr.next();
			try {
				XDATUser u = new XDATUser(login);
				String user = u.getLastname() + "," + u.getFirstname();
				users_h.put(login, user);
				users.add(user);
			} catch (UserNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XFTInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ElementNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DBPoolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FieldNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			context.put("usernames", users_h);
			context.put("elements",ElementSecurity.GetNonXDATElementNames());
		} catch (Exception e) {
			logger.error("",e);
		}

		if (data.getParameters().containsKey("destination")){
			context.put("destination", data.getParameters().getString("destination"));
		}
	}
}
