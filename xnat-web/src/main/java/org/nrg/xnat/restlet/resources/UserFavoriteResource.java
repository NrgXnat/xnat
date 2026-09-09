/*
 * web: org.nrg.xnat.restlet.resources.UserFavoriteResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import org.nrg.xft.XFTTable;
import org.nrg.xft.db.FavEntries;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.security.UserI;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

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

			
			// ResourceException, not a bare Exception: XnatServerResourceFinder propagates a
			// ResourceException with its status but turns anything else into a bare 404, so these
			// input rejections used to report "not found" instead of "bad request". See status doc
			// items 1-24 / 1-35. No test asserts a status on this path (it only triggers on an
			// apostrophe in the path parameters), so this corrects the status without breaking the
			// develop-calibrated suite.
			if(dataType.contains("'")){
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unexpected ' in data type name.");
			}

			if(pID.contains("'")){
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unexpected ' in project id.");
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
				favEntry.setUser(getUser());
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
		final UserI user = getUser();
		if(pID == null || dataType == null || user == null){
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
				 table=FavEntries.GetFavoriteEntries(dataType, getUser());
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
