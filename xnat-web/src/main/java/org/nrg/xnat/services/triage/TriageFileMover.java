
//Author: James Dickson <james@radiologics.com>
package org.nrg.xnat.services.triage;

import com.google.common.collect.ListMultimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.xdat.XDAT;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.helpers.file.StoredFile;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.DataURIA;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author james
 *
 */

@Service
@Slf4j
public class TriageFileMover implements Mover {
	final String MANIFEST = ".manifest";
	
	public TriageFileMover(){ }
	
	@Override
	public File getSource(UserI user, DataURIA src) {
		return src.getProps().containsKey(UriParserUtils._REMAINDER)
			   ? TriageUtils.getTriageFile(File.separator + "projects" + File.separator + src.getProps().get(URIManager.PROJECT_ID) + File.separator + "resources" + File.separator + src.getProps().get(URIManager.XNAME), File.separator + src.getProps().get(UriParserUtils._REMAINDER))
			   : TriageUtils.getTriageFile(File.separator + "projects" + File.separator + src.getProps().get(URIManager.PROJECT_ID) + File.separator + "resources" + File.separator + src.getProps().get(URIManager.XNAME));
	}

	@Override
	public List<String> move(UserI user,Integer eventId,Boolean overwrite,ListMultimap<String,Object> params,DataURIA src,ResourceURII dest,EventMetaI ci) throws Exception{
		final File   srcF     = this.getSource(user, src);
		final String label    = dest.getResourceLabel();
		final String filePath = StringUtils.equals(dest.getResourceFilePath(), "/") ? null : dest.getResourceFilePath();
		final String type     = (String) dest.getProps().get(URIManager.TYPE);

		final List<String> duplicates;
		if (srcF.isDirectory()) {
			duplicates = buildResourceModifier(user, dest, overwrite, type, ci).addFile(
					org.apache.commons.io.FileUtils.listFiles(srcF, null, true).stream().filter(file -> !StringUtils.equals(file.getName(), MANIFEST)).map(file -> new StoredFile(file, overwrite)).collect(Collectors.toList()),
					label,
					type,
					filePath,
					this.buildResourceInfo(user, params, ci),
					overwrite);
			if (duplicates.isEmpty()) {
				cleanup(srcF);
			}
		} else {
			duplicates = this.buildResourceModifier(user,dest,overwrite,type,ci).addFile(
					Collections.singletonList(new StoredFile(srcF, overwrite)),
					label,
					type,
					filePath,
					this.buildResourceInfo(user,params,ci),
					overwrite);
		}
		XDAT.triggerXftItemEvent(dest.getProject(), XftItemEventI.UPDATE);
		return duplicates;
	}

	@Override
	public void cleanup(File srcF) throws IOException {
		//cleanup the triage resource.
		FileUtils.MoveToCache(srcF.getName().endsWith("files") ? srcF.getParentFile() : srcF);
	}

    /* (non-Javadoc)
	 * @see org.nrg.xnat.helpers.move.Mover#buildResourceInfo(org.nrg.xft.event.EventMetaI)
	 */
    @Override
	public XnatResourceInfo buildResourceInfo(final UserI user, final ListMultimap<String, Object> params, final EventMetaI ci) {
    	return TriageUtils.buildResourceInfo(user, params, ci);
	}

    @Override
	public ResourceModifierA buildResourceModifier(final UserI user, final ResourceURII arcURI, final boolean overwrite, final String type, final EventMetaI ci) throws ActionException {
		return TriageUtils.buildResourceModifier(user, arcURI, overwrite, type, ci);
	}
}
	