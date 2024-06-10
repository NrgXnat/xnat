/*
 * web: org.nrg.xnat.helpers.move.FileMover
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.move;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.nrg.action.ActionException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.services.cache.UserDataCache;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.file.StoredFile;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.services.triage.TriageUtils;

import java.io.File;
import java.nio.file.Paths;

@Slf4j
public class FileMover {
	private final boolean overwrite;
	private final UserI user;
	private final ListMultimap<String,Object> params;
	private final UserDataCache cache;
	
	public FileMover(Boolean overwrite, UserI user, ListMultimap<String,Object> params){
		this.overwrite = BooleanUtils.toBooleanDefaultIfNull(overwrite, false);
		this.user=user;
		this.params=params;
		cache = XDAT.getContextService().getBean(UserDataCache.class);
	}
	
	public Boolean call(final URIManager.UserCacheURI src, final ResourceURII dest, final EventMetaI ci) throws Exception {
		final File srcF = src.getProps().containsKey(UriParserUtils._REMAINDER)
						  ? cache.getUserDataCacheFile(user, Paths.get((String) src.getProps().get(URIManager.XNAME), (String) src.getProps().get(UriParserUtils._REMAINDER)))
						  : cache.getUserDataCacheFile(user, Paths.get((String) src.getProps().get(URIManager.XNAME)));

		final String label    = dest.getResourceLabel();
		final String filePath = org.apache.commons.lang3.StringUtils.equals(dest.getResourceFilePath(), "/") ? null : dest.getResourceFilePath();
		final String type     = (String) dest.getProps().get(URIManager.TYPE);
						
		this.buildResourceModifier(dest,overwrite,type,ci).addFile(
				Lists.newArrayList(new StoredFile(srcF,overwrite)),
				label,
				type, 
				filePath,
				this.buildResourceInfo(ci),
				overwrite);
		
		return Boolean.TRUE;
	}

	public XnatResourceInfo buildResourceInfo(final EventMetaI ci) {
		return TriageUtils.buildResourceInfo(user, params, ci);
	}

	protected ResourceModifierA buildResourceModifier(final ResourceURII arcURI, final boolean overwrite, final String type, final EventMetaI ci) throws ActionException{
		return TriageUtils.buildResourceModifier(user, arcURI, overwrite, type, ci);
	}
}
