package org.nrg.xnat.services.triage;

import com.google.common.collect.ListMultimap;
import org.nrg.action.ActionException;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.DataURIA;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface Mover {
    XnatResourceInfo buildResourceInfo(UserI user, ListMultimap<String, Object> params, EventMetaI ci) throws Exception;

    List<String> move(UserI user, Integer eventId, Boolean overwrite, ListMultimap<String, Object> params, DataURIA src, ResourceURII dest, EventMetaI ci) throws Exception;

    File getSource(UserI user, URIManager.DataURIA src);

    ResourceModifierA buildResourceModifier(UserI user, final ResourceURII arcURI, final boolean overwrite, final String type, final EventMetaI ci) throws ActionException;

    void cleanup(File srcF) throws IOException;
}