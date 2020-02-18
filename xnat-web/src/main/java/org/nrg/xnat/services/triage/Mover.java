package org.nrg.xnat.services.triage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.nrg.action.ActionException;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;

import com.google.common.collect.ListMultimap;

public interface Mover {
	
	abstract  public XnatResourceInfo buildResourceInfo(UserI user,ListMultimap<String,Object> params,EventMetaI ci) throws Exception;
	abstract  public List<String> move(UserI user,Integer eventId,Boolean overwrite,ListMultimap<String,Object> params,org.nrg.xnat.helpers.uri.URIManager.DataURIA src,ResourceURII dest,EventMetaI ci) throws Exception;	
	abstract  public File getSource(UserI user, org.nrg.xnat.helpers.uri.URIManager.DataURIA src);
	abstract  public ResourceModifierA buildResourceModifier(UserI user,final ResourceURII arcURI,final boolean overwrite, final String type, final EventMetaI ci) throws ActionException;
	abstract  public void cleanup(File srcF) throws FileNotFoundException, IOException;
	}