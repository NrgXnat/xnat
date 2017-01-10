/*
 * web: org.nrg.xnat.restlet.actions.PrearcImporterA
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.status.StatusProducer;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.turbine.utils.PropertiesHelper;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.PrearcImporterHelper;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcTableBuilder;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.prearchive.SessionException;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.restlet.actions.PrearcImporterA.PrearcSession;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

@SuppressWarnings("unchecked")
public abstract class PrearcImporterA extends StatusProducer implements Callable<Iterable<PrearcSession>>{
	@SuppressWarnings("serial")
	public static class UnknownPrearcImporterException extends Exception {
		public UnknownPrearcImporterException(String string,
				IllegalArgumentException illegalArgumentException) {
			super(string,illegalArgumentException);
		}
	}

	static Logger logger = Logger.getLogger(PrearcImporterA.class);

	public static final String ECAT = "ECAT";
	public static final String DICOM = "DICOM";
	
	public static final String PREARC_IMPORTER_ATTR= "prearc-importer";

	static String DEFAULT_HANDLER=DICOM;
	final static Map<String,Class<? extends PrearcImporterA>> PREARC_IMPORTERS=new HashMap<String,Class<? extends PrearcImporterA>>();

	private static final String PROP_OBJECT_IDENTIFIER = "org.nrg.PrearcImporter.impl";
	private static final String SESSION_BUILDER_PROPERTIES = "prearc-importer.properties";
	private static final String CLASS_NAME = "className";
	private static final String[] PROP_OBJECT_FIELDS = new String[]{CLASS_NAME};
	static{
		//EXAMPLE PROPERTIES FILE 
		//org.nrg.PrearcImporter=NIFTI
		//org.nrg.PrearcImporter.impl.NIFTI.className=org.nrg.prearc.importers.CustomNiftiImporter
		try {
			PREARC_IMPORTERS.putAll((new PropertiesHelper<PrearcImporterA>()).buildClassesFromProps(SESSION_BUILDER_PROPERTIES, PROP_OBJECT_IDENTIFIER, PROP_OBJECT_FIELDS, CLASS_NAME));
			
			if(!PREARC_IMPORTERS.containsKey(DICOM))PREARC_IMPORTERS.put(DICOM, PrearcImporterHelper.class);
			if(!PREARC_IMPORTERS.containsKey(ECAT))PREARC_IMPORTERS.put(ECAT, PrearcImporterHelper.class);
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	
	public static <A extends PrearcImporterA> A buildImporter(String format,Object uID, final UserI u, final FileWriterWrapperI fi, Map<String,Object> params,boolean allowSessionMerge, boolean overwriteFiles) throws ClientException, ServerException, SecurityException, NoSuchMethodException, UnknownPrearcImporterException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException{
		if(StringUtils.isEmpty(format)){
			format=DEFAULT_HANDLER;
		}
		
		Class<A> importerImpl=(Class<A>)PREARC_IMPORTERS.get(format);
		if(importerImpl==null){
			throw new UnknownPrearcImporterException("Unknown prearc-importer implementation specified: " + format,new IllegalArgumentException());
		}
		
		final Constructor<A> con=importerImpl.getConstructor(Object.class, UserI.class, FileWriterWrapperI.class, Map.class, boolean.class, boolean.class);
		return con.newInstance(uID, u, fi, params,allowSessionMerge,overwriteFiles);
		
	}
	
	/**
	 * This method was added to allow other developers to manually add importers to the list, without adding a configuration file.  However, this would some how need to be done before the import is executed (maybe as a servlet?).
	 * @return
	 */
	public static Map<String,Class<? extends PrearcImporterA>> getPrearcImporters(){
		return PREARC_IMPORTERS;
	}
	
	public PrearcImporterA(Object control, final UserI u, final FileWriterWrapperI fi, Map<String,Object> params, boolean overwrite, boolean allowDataDeletion) {
		super(control);
	}
	
	public abstract List<PrearcSession> call() throws ActionException;
	
	public static class PrearcSession{
		private final File sessionDir;
		private final String project,timestamp,folderName;
		private final Map<String,Object> additionalValues=new HashMap<String,Object>();
		
		public PrearcSession(final File sessionDir) throws InvalidPermissionException, Exception{
			this.sessionDir=sessionDir;
			final File sessionXML=new File(sessionDir.getAbsolutePath()+".xml");
			
			folderName=sessionDir.getName();
			timestamp=sessionDir.getParentFile().getName();
			
			final XnatImagesessiondataBean isd=PrearcTableBuilder.parseSession(sessionXML);
			project=isd.getProject();
		}

		public PrearcSession(URIManager.PrearchiveURI parsedURI,final Map<String,Object> additionalValues,final UserI user) throws InvalidPermissionException, Exception{
			this((String)parsedURI.getProps().get(URIManager.PROJECT_ID),
					(String)parsedURI.getProps().get(PrearcUtils.PREARC_TIMESTAMP),
					(String)parsedURI.getProps().get(PrearcUtils.PREARC_SESSION_FOLDER), additionalValues,user);
		}

		public PrearcSession(final String project, final String timestamp, final String folderName,final Map<String,Object> props, final UserI user) throws InvalidPermissionException, Exception{
			this.folderName=folderName;
			this.project=project;
			this.timestamp=timestamp;
			if(folderName==null || timestamp==null){
				throw new IllegalArgumentException();
			}
			if (null != props) {
			    this.additionalValues.putAll(props);
			}
			this.sessionDir=PrearcUtils.getPrearcSessionDir(user, project, timestamp, folderName, true);
		}

		public File getSessionDir() {
			return sessionDir;
		}

		public String getProject() {
			return project;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public String getFolderName() {
			return folderName;
		}

		public Map<String, Object> getAdditionalValues() {
			return additionalValues;
		}

		public String getUrl() {
			return PrearcUtils.buildURI(project, timestamp, folderName);
		}
		
		public SessionData getSessionData() throws Exception, SQLException, SessionException{
			return PrearcDatabase.getSessionIfExists(folderName, timestamp, project);		
		}
	}
}
