/*
 * core: org.nrg.xdat.services.impl.hibernate.HibernateGroupFeatureService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services.impl.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.GroupFeatureDAO;
import org.nrg.xdat.entities.GroupFeature;
import org.nrg.xdat.services.GroupFeatureService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HibernateGroupFeatureService extends AbstractHibernateEntityService<GroupFeature, GroupFeatureDAO> implements GroupFeatureService{

	@Override
	@Transactional
	public List<GroupFeature> findFeaturesForGroup(String groupId) {
		return getDao().findByGroup(groupId);
	}

	@Override
	@Transactional
	public List<GroupFeature> findFeaturesForGroups(List<String> groupIds) {
		return getDao().findByGroups(groupIds);
	}

	@Override
	@Transactional
	public List<GroupFeature> findGroupsForFeature(String feature) {
		return getDao().findByFeature(feature);
	}

	@Override
	@Transactional
	public List<GroupFeature> findGroupsForFeatures(List<String> feature) {
		return getDao().findByFeatures(feature);
	}

	@Override
	@Transactional
	public void delete(String groupId, String feature) {
		GroupFeature ur = getDao().findByGroupFeature(groupId, feature);
	      if (ur != null) {
		          if (_log.isDebugEnabled()) {
		              _log.debug("Deleting group feature: " + ur.getGroupId() + " " + ur.getFeature());
		          }
		          getDao().delete(ur);
	      }
	}

	@Override
	@Transactional
	public void deleteByGroup(String groupId) {
	      List<GroupFeature> ur = getDao().findByGroup(groupId);
	      if (ur != null) {
	    	  for(GroupFeature gr:ur){
		          if (_log.isDebugEnabled()) {
		              _log.debug("Deleting group feature: " + gr.getGroupId() + " " + gr.getFeature());
		          }
		          getDao().delete(gr);
	    	  }
	      }
	}

	@Override
	@Transactional
	public void deleteByTag(String tag) {
	      List<GroupFeature> ur = getDao().findByTag(tag);
	      if (ur != null) {
	    	  for(GroupFeature gr:ur){
		          if (_log.isDebugEnabled()) {
		              _log.debug("Deleting group feature: " + gr.getGroupId() + " " + gr.getFeature());
		          }
		          getDao().delete(gr);
	    	  }
	      }
	}

	@Override
	@Transactional
	public GroupFeature addFeatureToGroup(String groupId, String tag, String feature) {
		GroupFeature token = newEntity();
		token.setGroupId(groupId);
		token.setTag(tag);
	    token.setFeature(feature);
	    token.setOnByDefault(true);
	    getDao().create(token);
	    if (_log.isDebugEnabled()) {
	        _log.debug("Created new feature " + token.getFeature() + " for group: " + token.getGroupId());
	    }
	    return token;
	}

	@Override
	@Transactional
	public GroupFeature findGroupFeature(String groupId, String feature) {
		return getDao().findByGroupFeature(groupId, feature);
	}
	
    private static final Log _log = LogFactory.getLog(HibernateGroupFeatureService.class);

	@Override
	@Transactional
	public void blockFeatureForGroup(String id, String tag, String feature) {
		GroupFeature ur = getDao().findByGroupFeature(id, feature);
		if(ur!=null){
			ur.setBlocked(Boolean.TRUE);
			this.getDao().update(ur);
		}else{
			GroupFeature token = newEntity();
			token.setGroupId(id);
			token.setTag(tag);
		    token.setFeature(feature);
		    getDao().create(token);
		}
	}

	@Override
	@Transactional
	public List<GroupFeature> findFeaturesForTag(String tag) {
		return this.getDao().findByTag(tag);
	}

	@Override
	@Transactional
	public List<GroupFeature> getEnabledByTag(String tag) {
		return getDao().findEnabledByTag(tag);
	}

	@Override
	@Transactional
	public List<GroupFeature> getBannedByTag(String tag) {
		return getDao().findBannedByTag(tag);
	}

}
