//Copyright 2015 Radiologics, Inc
//Author: James Dickson <james@radiologics.com>
package org.nrg.xnat.services.triage;

import java.util.List;

import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.uri.URIManager.DataURIA;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;

import com.google.common.collect.ListMultimap;

/**
 * @author james
 *
 */
public interface TriageService {
	abstract  public List<String> move(UserI user,Integer eventId,Boolean overwrite,ListMultimap<String,Object> params,DataURIA src,ResourceURII dest) throws Exception;
	abstract  public boolean isLocked(UserI user, Integer eventId, Boolean overwrite,ListMultimap<String, Object> otherParams, DataURIA key, ResourceURII value)throws Exception;
}