/*
 * web: org.nrg.xnat.restlet.services.FeatureDefinitionRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.services;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.helpers.FeatureDefinitionI;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.services.RoleRepositoryServiceI.RoleDefinitionI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

public class FeatureDefinitionRestlet extends SecureResource {
    public FeatureDefinitionRestlet(Context context, Request request, Response response) {
        super(context, request, response);
        
        if (request.getMethod() == Method.POST && !request.isEntityAvailable()) {
            getResponse().setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED, "You must provide a configuration for whitelisted IP addresses.");
        } else {
	        this.getVariants().add(new Variant(MediaType.ALL));
	    }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (_log.isDebugEnabled()) {
            _log.debug("Entering the featureDefinitionRestlet represent() method");
        }

        if(getQueryVariable("tag")==null && getQueryVariable("type")==null && getQueryVariable("group")==null){
	        XFTTable t = new XFTTable();
	        t.initTable(new String[]{"key","name","description","enabled","banned"});
	        
	        for(FeatureDefinitionI feature: Features.getAllFeatures()){
	        	t.insertRow(new Object[]{feature.getKey(),feature.getName(),feature.isOnByDefault(),feature.isBanned()});
	        }
	        
	        return representTable(t, overrideVariant(variant), new Hashtable<String, Object>());
        }else if(getQueryVariable("tag")!=null){
        	String[] tags=StringUtils.split(getQueryVariable("tag"), ",");
        	
        	Collection<String> siteWideEnabled=Features.getEnabledFeatures();
        	Collection<String> siteWideBanned=Features.getBannedFeatures();


			final UserI     user     = getUser();
			final JSONArray projects = new JSONArray();
			for(String tag:tags){
        		XnatProjectdata proj=XnatProjectdata.getProjectByIDorAlias(tag, user, false);
        		        		
        		try {
					JSONObject project= new JSONObject();
					project.put("id",proj.getId());
					project.put("banned",siteWideBanned);
					project.put("onByDefault",siteWideEnabled);
					
					List<JSONObject> groups=Lists.newArrayList();
					
					for(List gID:proj.getGroupIDs()){
						UserGroupI ug=Groups.getGroup((String)gID.get(0));
						
						JSONObject group = new JSONObject();
						group.put("id",ug.getId());
						group.put("display",gID.get(1));
						
						group.put("features",Features.getFeaturesForGroup(ug));
						group.put("blocked", Features.getBlockedFeaturesForGroup(ug));
						
						group.put("inherited_features",Features.getEnabledFeaturesForGroupType((String)gID.get(1)));
						group.put("inherited_banned", Features.getBannedFeaturesForGroupType((String)gID.get(1)));
						
						groups.add(group);
					}
					
					project.put("groups", groups);
					projects.put(project);
				} catch (JSONException e) {
					logger.error("",e);
				}
        	}
        	
        	return new StringRepresentation(projects.toString());
        }else if(getQueryVariable("type")!=null){        	
        	Collection<String> siteWideEnabled=Features.getEnabledFeatures();
        	Collection<String> siteWideBanned=Features.getBannedFeatures();
        	
        	
        	JSONArray projects = new JSONArray();
    		        		
    		try {
				JSONObject project= new JSONObject();
				project.put("id",Features.SITE_WIDE);
				project.put("banned",siteWideBanned);
				project.put("onByDefault",siteWideEnabled);
				
				List<JSONObject> groups=Lists.newArrayList();
				
				XFTTable t= XFTTable.Execute("SELECT DISTINCT displayname FROM xdat_usergroup WHERE tag IS NOT NULL;", null, null);
		        List<Object> groupTypes=t.convertColumnToArrayList("displayname");
				
				for(Object gType:groupTypes){
					JSONObject group = new JSONObject();
					group.put("id",gType);
					group.put("display",gType);
					
					group.put("features",Features.getEnabledFeaturesForGroupType((String)gType));
					group.put("blocked", Features.getBannedFeaturesForGroupType((String)gType));
					
					groups.add(group);
				}
				
				for(RoleDefinitionI role : Roles.getRoles()){
					JSONObject group = new JSONObject();
					String key="role:"+role.getKey();
					group.put("id",key);
					group.put("display","Role: " + role.getName());
					group.put("isRole", true);
					
					group.put("features",Features.getEnabledFeaturesForGroupType(key));
					group.put("blocked", Features.getBannedFeaturesForGroupType(key));
					
					groups.add(group);
				}
				
				project.put("groups", groups);
				projects.put(project);
			} catch (Exception e) {
				logger.error("",e);
				getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
				return null;
			}
        	return new StringRepresentation(projects.toString());
        }else{
        	this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        	return null;
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void handlePost() {
    	try {
			InputStream is = this.getRequest().getEntity().getStream();
			
			String body= IOUtils.toString(is);
			
			JSONObject json=new JSONObject(body);
			
			Boolean enabled=null;
			try {
				enabled = json.getBoolean("enabled");
			} catch (Exception e) {
				//missing fields are thrown as exceptions
			}
			Boolean banned=null;
			try {
				banned = json.getBoolean("banned");
			} catch (Exception e) {
				//missing fields are thrown as exceptions
			}
			
			String key = json.getString("key");

			final UserI user = getUser();

			if(getQueryVariable("type")==null && getQueryVariable("group")==null){
				if (!Roles.isSiteAdmin(user)) {
		            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		            return ;
		        }
				//site wide configuration
				FeatureDefinitionI def=Features.getFeatureRepositoryService().getByKey(key);
				if(def==null){
					this.getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
					return;
				}
				
				//manage banning
				if(banned!=null){
					if(BooleanUtils.toBoolean(def.isBanned()) && !BooleanUtils.toBoolean(banned)){
						Features.unBanFeature(key);
					}else if(BooleanUtils.toBoolean(banned) && !BooleanUtils.toBoolean(def.isBanned())){
						Features.banFeature(key);
					}
				}
				
				//manage OnByDefault
				if(enabled!=null){
					if(BooleanUtils.toBoolean(def.isOnByDefault()) && !BooleanUtils.toBoolean(enabled)){
						Features.disableByDefault(key);
					}else if(BooleanUtils.toBoolean(enabled) && !BooleanUtils.toBoolean(def.isOnByDefault())){
						Features.enableByDefault(key);
					}
				}
			}else if(getQueryVariable("type")!=null){
				if (!Roles.isSiteAdmin(user)) {
		            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		            return ;
		        }
				//display name specific customization
				String type=getQueryVariable("type");
				
				//manage banning
				if(banned!=null){
					if(!BooleanUtils.toBoolean(banned) && Features.isBlockedByGroupType(key, type)){
						Features.unblockByGroupType(key, type, user);
					}else if(BooleanUtils.toBoolean(banned) && !Features.isBlockedByGroupType(key, type)){
						Features.blockByGroupType(key, type, user);
					}
				}
				
				//manage OnByDefault
				if(enabled!=null){
					if(!BooleanUtils.toBoolean(enabled) && Features.isOnByDefaultByGroupType(key, type)){
						Features.disableIsOnByDefaultByGroupType(key, type, user);
					}else if(BooleanUtils.toBoolean(enabled) && !Features.isOnByDefaultByGroupType(key, type)){
						Features.enableIsOnByDefaultByGroupType(key, type, user);
					}
				}
			}else if(getQueryVariable("group")!=null){
				//tag specific customization
				String groupid=getQueryVariable("group");
				
				UserGroupI group=Groups.getGroup(groupid);
				
				//manage banning
				if(banned!=null){
					if(!BooleanUtils.toBoolean(banned) && Features.isBannedByGroup(group, key)){
						Features.unblockFeatureForGroup(group, key, user);
					}else if(BooleanUtils.toBoolean(banned) && !Features.isBannedByGroup(group, key)){
						Features.blockFeatureForGroup(group, key, user);
					}
				}
				
				//manage OnByDefault
				if(enabled!=null){
					if(!BooleanUtils.toBoolean(enabled) && Features.isOnByDefaultByGroup(group,key)){
						Features.disableFeatureForGroup(group, key, user);
					}else if(BooleanUtils.toBoolean(enabled) && !Features.isOnByDefaultByGroup(group,key)){
						Features.enableFeatureForGroup(group, key, user);
					}
				}
			}
		} catch (IOException e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Need posted JSON content");
		} catch (JSONException e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid JSON content");
		} catch (Exception e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
		}
    }


    private static final Logger _log = LoggerFactory.getLogger(FeatureDefinitionRestlet.class);
}
