
//Author: James Dickson <james@radiologics.com>
package org.nrg.xnat.services.triage;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;

import com.google.common.collect.ListMultimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.direct.DirectResourceModifierBuilder;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierBuilderI;
import org.nrg.xnat.helpers.uri.archive.*;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.restlet.data.Status;
import org.springframework.util.CollectionUtils;

@Slf4j
public class TriageUtils {
	public static String getTriageUploadsPath() {
		return StringUtils.appendIfMissing(getConfiguredTriageUploadsPath(), File.separator);
	}

	public static String getTriageProjectPath(final String project) {
		return Paths.get(getTriageUploadsPath(), "projects", project).toString();
	}

	public static File getTriageFile(final String directory, final String file) {
		return Paths.get(getTriageUploadsPath(), directory, file).toFile();
	}

	public static File getTriageFile(final String directory) {
		return new File(getTriageUploadsPath(), directory);
	}

	public static XnatResourceInfo buildResourceInfo(final UserI user, final ListMultimap<String, Object> params, final EventMetaI ci) {
		final XnatResourceInfo.XnatResourceInfoBuilder builder = XnatResourceInfo.builder();
		if (!CollectionUtils.isEmpty(params.get("description"))) {
			builder.description((String) params.get("description").get(0));
		}
		if (!CollectionUtils.isEmpty(params.get("format"))) {
			builder.format((String) params.get("format").get(0));
		}
		if (!CollectionUtils.isEmpty(params.get("content"))) {
			builder.content((String) params.get("content").get(0));
		}
		if (!CollectionUtils.isEmpty(params.get("tags"))) {
			builder.tags(GenericUtils.convertToTypedList(params.get("tags"), String.class));
		}

		final Date now = EventUtils.getEventDate(ci, false);
		return builder.username(user.getUsername()).created(now).lastModified(now).eventId(EventUtils.getEventId(ci)).build();
	}

	public static ResourceModifierA buildResourceModifier(final UserI user, final ResourceURII arcURI, final boolean overwrite, final String type, final EventMetaI ci) throws ActionException {
		//this should allow dependency injection - TO
		try {
			if(!Permissions.canEdit(user, arcURI.getSecurityItem())){
				throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, new Exception("Unauthorized attempt to add a file to " + arcURI.getUri()));
			}
		} catch (Exception e) {
			log.error("", e);
			throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, e);
		}

		final XnatImagesessiondata     assessed = arcURI instanceof AssessedURII ? ((AssessedURII) arcURI).getSession() : null;
		final ResourceModifierBuilderI builder  =new DirectResourceModifierBuilder();
		if(arcURI instanceof ReconURII){
			//reconstruction
			builder.setRecon(assessed,((ReconURII)arcURI).getRecon(), type);
		}else if(arcURI instanceof ScanURII){
			//scan
			builder.setScan(assessed, ((ScanURII)arcURI).getScan());
		}else if(arcURI instanceof AssessorURII){//			experiment
			builder.setAssess(assessed, ((AssessorURII)arcURI).getAssessor(), type);
		}else if(arcURI instanceof ExperimentURII){
			XnatExperimentdata expt =((ExperimentURII)arcURI).getExperiment();
			builder.setExpt(expt.getPrimaryProject(false),expt);
		}else if(arcURI instanceof SubjectURII){
			XnatSubjectdata sub =((SubjectURII)arcURI).getSubject();
			builder.setSubject(sub.getPrimaryProject(false), sub);
		}else if(arcURI instanceof ProjectURII){
			builder.setProject(((ProjectURII)arcURI).getProject());
		}else{
			throw new ClientException("Unsupported resource:"+arcURI.getUri());
		}

		try {
			return builder.buildResourceModifier(overwrite,user,ci);
		} catch (Exception e) {
			throw new ServerException(e);
		}
	}

	private static String getConfiguredTriageUploadsPath() {
		return XDAT.getSiteConfigPreferences().getTriagePath();
	}
}