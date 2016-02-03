/*
 * org.nrg.xnat.restlet.resources.UserFavoriteResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources;

import org.nrg.xft.XFTTable;
import org.nrg.xft.db.FavEntries;
import org.nrg.xft.exception.DBPoolException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.sql.SQLException;
import java.util.Hashtable;

public class UserFavoriteResource extends SecureResource {
	String dataType=null;
	String pID=null;
	
	public UserFavoriteResource(Context context, Request request, Response response) throws Exception {
		super(context, request, response);
		
			this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
			this.getVariants().add(new Variant(MediaType.TEXT_HTML));
			this.getVariants().add(new Variant(MediaType.TEXT_XML));
			
			dataType= (String)getParameter(request,"DATA_TYPE");
			pID= (String)getParameter(request,"PROJECT_ID");

			
			if(dataType.contains("'")){
				throw new Exception("Unexpected ' in data type name.");
			}
			
			if(pID.contains("'")){
				throw new Exception("Unexpected ' in project id.");
			}
	}

	@Override
	public boolean allowDelete() {
		return true;
	}

	@Override
	public boolean allowPut() {
		return true;
	}

	@Override
	public boolean allowGet() {
		return false;
	}
	
	@Override
	public void handlePut() {
		if(pID==null || dataType==null){
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}else{
			try {
				FavEntries favEntry=new FavEntries();
				favEntry.setId(pID);
				favEntry.setDataType(dataType);
				favEntry.setUser(user);
				favEntry.save();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		returnDefaultRepresentation();
	}

	
	@Override
	public void handleDelete() {
		if(pID==null || dataType==null || user==null){
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}else{
			try {
				FavEntries favEntry=FavEntries.GetFavoriteEntries(dataType, pID, user);
				favEntry.delete();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		returnDefaultRepresentation();
	}

	@Override
	public Representation getRepresentation(Variant variant) {	
		XFTTable table = null;
		if(dataType!=null){
			try {	            
				 table=FavEntries.GetFavoriteEntries(dataType, user);
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (DBPoolException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Hashtable<String,Object> params=new Hashtable<String,Object>();
		params.put("title", "User Favorites");

		MediaType mt = overrideVariant(variant);

		return this.representTable(table, mt, params);
	}
}
