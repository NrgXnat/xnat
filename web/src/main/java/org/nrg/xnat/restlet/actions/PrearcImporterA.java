/*
 * web: org.nrg.xnat.restlet.actions.PrearcImporterA
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.status.StatusProducer;
import org.nrg.xdat.turbine.utils.PropertiesHelper;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.event.archive.ArchiveStatusProducer;
import org.nrg.xnat.helpers.PrearcImporterHelper;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

@SuppressWarnings("unchecked")
@Slf4j
public abstract class PrearcImporterA extends ArchiveStatusProducer implements Callable<Iterable<PrearcSession>>{
	@SuppressWarnings("serial")
	public static class UnknownPrearcImporterException extends Exception {
		public UnknownPrearcImporterException(String string,
				IllegalArgumentException illegalArgumentException) {
			super(string,illegalArgumentException);
		}
	}

	public static final String ECAT = "ECAT";
	public static final String DICOM = "DICOM";
	
	public static final String PREARC_IMPORTER_ATTR= "prearc-importer";

	static String DEFAULT_HANDLER=DICOM;
	final static Map<String,Class<? extends PrearcImporterA>> PREARC_IMPORTERS= new HashMap<>();

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
			log.error("",e);
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
		super(control, u);
	}
	
	public abstract List<PrearcSession> call() throws ActionException;
}
