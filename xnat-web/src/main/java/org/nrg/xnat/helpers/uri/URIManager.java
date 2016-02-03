/*
 * org.nrg.xnat.helpers.uri.URIManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.uri;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.uri.archive.impl.*;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.restlet.util.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class URIManager {
	private final static Logger logger = LoggerFactory.getLogger(URIManager.class);
	public static enum TEMPLATE_TYPE {ARC,PREARC,CACHE};
	
	public static final String XNAME = "XNAME";
	public static final String RECON_ID = "RECON_ID";
	public static final String SCAN_ID = "SCAN_ID";
	public static final String ASSESSED_ID = "ASSESSED_ID";
	public static final String SUBJECT_ID = "SUBJECT_ID";
	public static final String EXPT_ID = "EXPT_ID";
	public static final String EXPT_LABEL = "EXPT_LABEL";
	public static final String VISIT_LABEL = "VISIT";
	public static final String TIMEZONE = "TIMEZONE";
	public static final String PROTOCOL_LABEL = "PROTOCOL";
	public static final String PROJECT_ID = "PROJECT_ID";
	public static final String TYPE = "TYPE";
	public static final String SOURCE = "SOURCE";
    public static final String PREVENT_ANON = "prevent_anon";
    public static final String PREVENT_AUTO_COMMIT = "prevent_auto_commit";

	
	private static URIManager manager=null;
	
	public synchronized static URIManager getInstance(){
		if(manager==null){
			manager=new URIManager();
		}
		
		return manager;
	}
	
	private URIManager(){
		add(TEMPLATE_TYPE.CACHE,"/user/cache/resources/{" + XNAME + "}/files",Template.MODE_STARTS_WITH,URIManager.UserCacheURI.class);
		
		
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/experiments/{" + URIManager.EXPT_ID + "}".intern(),Template.MODE_EQUALS,ProjSubjExptURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}".intern(),Template.MODE_EQUALS,ProjSubjURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}/experiments/{" + URIManager.EXPT_ID + "}".intern(),Template.MODE_EQUALS,ProjSubjExptURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}/experiments/{" + URIManager.ASSESSED_ID + "}/assessors/{" + URIManager.EXPT_ID + "}".intern(),Template.MODE_EQUALS,ProjSubjAssExptURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}/experiments/{" + URIManager.ASSESSED_ID + "}/scans/{" + URIManager.SCAN_ID + "}".intern(),Template.MODE_EQUALS,ProjSubjAssScanURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}/experiments/{" + URIManager.ASSESSED_ID + "}/reconstructions/{" + URIManager.RECON_ID + "}".intern(),Template.MODE_EQUALS,ProjSubjAssReconURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}".intern(),Template.MODE_EQUALS,ProjURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/experiments/{" + URIManager.EXPT_ID + "}".intern(),Template.MODE_EQUALS,ExptURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/experiments/{" + URIManager.ASSESSED_ID + "}/scans/{" + URIManager.SCAN_ID + "}".intern(),Template.MODE_EQUALS,ExptScanURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/experiments/{" + URIManager.ASSESSED_ID + "}/reconstructions/{" + URIManager.RECON_ID + "}".intern(),Template.MODE_EQUALS,ExptReconURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/experiments/{" + URIManager.ASSESSED_ID + "}/assessors/{" + URIManager.EXPT_ID + "}".intern(),Template.MODE_EQUALS,ExptAssessorURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/subjects/{SUBJECT_ID}",Template.MODE_EQUALS,SubjURI.class);
		
		//resources with file path
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/experiments/{" + URIManager.EXPT_ID + "}/resources/{" + XNAME + "}/files".intern(),Template.MODE_STARTS_WITH,ResourcesProjSubjExptURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}/resources/{" + XNAME + "}/files".intern(),Template.MODE_STARTS_WITH,ResourcesProjSubjURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}/experiments/{" + URIManager.EXPT_ID + "}/resources/{" + XNAME + "}/files".intern(),Template.MODE_STARTS_WITH,ResourcesProjSubjExptURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}/experiments/{" + URIManager.ASSESSED_ID + "}/assessors/{" + URIManager.EXPT_ID + "}/{" + URIManager.TYPE + "}/resources/{" + XNAME + "}/files".intern(),Template.MODE_STARTS_WITH,ResourcesProjSubjAssExptURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}/experiments/{" + URIManager.ASSESSED_ID + "}/scans/{" + URIManager.SCAN_ID + "}/resources/{" + XNAME + "}/files".intern(),Template.MODE_STARTS_WITH,ResourcesProjSubjAssScanURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}/experiments/{" + URIManager.ASSESSED_ID + "}/reconstructions/{" + URIManager.RECON_ID + "}/{" + URIManager.TYPE + "}/resources/{" + XNAME + "}/files".intern(),Template.MODE_STARTS_WITH,ResourcesProjSubjAssReconURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/resources/{" + XNAME + "}/files".intern(),Template.MODE_STARTS_WITH,ResourcesProjURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/experiments/{" + URIManager.EXPT_ID + "}/resources/{" + XNAME + "}/files".intern(),Template.MODE_STARTS_WITH,ResourcesExptURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/experiments/{" + URIManager.ASSESSED_ID + "}/scans/{" + URIManager.SCAN_ID + "}/resources/{" + XNAME + "}/files".intern(),Template.MODE_STARTS_WITH,ResourcesExptScanURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/experiments/{" + URIManager.ASSESSED_ID + "}/reconstructions/{" + URIManager.RECON_ID + "}/{" + URIManager.TYPE + "}/resources/{" + XNAME + "}/files".intern(),Template.MODE_STARTS_WITH,ResourcesExptReconURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/experiments/{" + URIManager.ASSESSED_ID + "}/assessors/{" + URIManager.EXPT_ID + "}/{" + URIManager.TYPE + "}/resources/{" + XNAME + "}/files".intern(),Template.MODE_STARTS_WITH,ResourcesExptAssessorURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/subjects/{SUBJECT_ID}/resources/{" + XNAME + "}/files",Template.MODE_STARTS_WITH,ResourcesSubjURI.class);

		//resources alone
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/experiments/{" + URIManager.EXPT_ID + "}/resources/{" + XNAME + "}".intern(),Template.MODE_STARTS_WITH,ResourcesProjSubjExptURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}/resources/{" + XNAME + "}".intern(),Template.MODE_STARTS_WITH,ResourcesProjSubjURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}/experiments/{" + URIManager.EXPT_ID + "}/resources/{" + XNAME + "}".intern(),Template.MODE_STARTS_WITH,ResourcesProjSubjExptURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}/experiments/{" + URIManager.ASSESSED_ID + "}/assessors/{" + URIManager.EXPT_ID + "}/{" + URIManager.TYPE + "}/resources/{" + XNAME + "}".intern(),Template.MODE_STARTS_WITH,ResourcesProjSubjAssExptURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}/experiments/{" + URIManager.ASSESSED_ID + "}/scans/{" + URIManager.SCAN_ID + "}/resources/{" + XNAME + "}".intern(),Template.MODE_STARTS_WITH,ResourcesProjSubjAssScanURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/subjects/{" + URIManager.SUBJECT_ID + "}/experiments/{" + URIManager.ASSESSED_ID + "}/reconstructions/{" + URIManager.RECON_ID + "}/{" + URIManager.TYPE + "}/resources/{" + XNAME + "}".intern(),Template.MODE_STARTS_WITH,ResourcesProjSubjAssReconURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/projects/{" + URIManager.PROJECT_ID + "}/resources/{" + XNAME + "}".intern(),Template.MODE_STARTS_WITH,ResourcesProjURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/experiments/{" + URIManager.EXPT_ID + "}/resources/{" + XNAME + "}".intern(),Template.MODE_STARTS_WITH,ResourcesExptURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/experiments/{" + URIManager.ASSESSED_ID + "}/scans/{" + URIManager.SCAN_ID + "}/resources/{" + XNAME + "}".intern(),Template.MODE_STARTS_WITH,ResourcesExptScanURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/experiments/{" + URIManager.ASSESSED_ID + "}/reconstructions/{" + URIManager.RECON_ID + "}/{" + URIManager.TYPE + "}/resources/{" + XNAME + "}".intern(),Template.MODE_STARTS_WITH,ResourcesExptReconURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/experiments/{" + URIManager.ASSESSED_ID + "}/assessors/{" + URIManager.EXPT_ID + "}/{" + URIManager.TYPE + "}/resources/{" + XNAME + "}".intern(),Template.MODE_STARTS_WITH,ResourcesExptAssessorURI.class);
		add(TEMPLATE_TYPE.ARC,"/archive/subjects/{SUBJECT_ID}/resources/{" + XNAME + "}",Template.MODE_STARTS_WITH,ResourcesSubjURI.class);
		
		add(TEMPLATE_TYPE.ARC,"/archive".intern(),Template.MODE_EQUALS,URIManager.ArchiveURI.class);
		
		
		add(TEMPLATE_TYPE.PREARC,"/prearchive/projects/{" + URIManager.PROJECT_ID + "}/{" +PrearcUtils.PREARC_TIMESTAMP + "}/{" + PrearcUtils.PREARC_SESSION_FOLDER + "}".intern(),Template.MODE_EQUALS,URIManager.PrearchiveURI.class);
		add(TEMPLATE_TYPE.PREARC,"/prearchive/projects/{" + URIManager.PROJECT_ID + "}/{" + PrearcUtils.PREARC_TIMESTAMP + "}".intern(),Template.MODE_EQUALS,URIManager.PrearchiveURI.class);
		add(TEMPLATE_TYPE.PREARC,"/prearchive/projects/{" + URIManager.PROJECT_ID + "}".intern(),Template.MODE_EQUALS,URIManager.PrearchiveURI.class);
		add(TEMPLATE_TYPE.PREARC,"/prearchive".intern(),Template.MODE_EQUALS,URIManager.PrearchiveURI.class);
	}
	
	public static Collection<TemplateInfo> getTemplates(TEMPLATE_TYPE type){
		return getInstance().TEMPLATES.get(type);
	}
	
	public static class TemplateInfo<A extends DataURIA>{
		public final String key;
		public final int MODE;
		public final Class<A> clazz;
		
		public TemplateInfo(final String key, final Integer i, final Class<A> clazz){
			this.key=key;
			this.MODE=i;
			this.clazz=clazz;
		}
		
		private final static Class[] CONS=new Class[]{Map.class,String.class};
		
		public A wrap(Map<String,Object> props, final String uri){
			try {
				return clazz.getConstructor(CONS).newInstance(new Object[]{props,uri});
			} catch (Exception e) {
				logger.error("",e);
				return null;
			}
			
		}
	}
	
	public static abstract class DataURIA{
		final protected Map<String,Object> props;
		final protected String uri;
		
		public DataURIA(final Map<String,Object> props, final String uri){
			this.props=props;
			this.uri=uri.intern();
		}
		
		public Map<String,Object> getProps(){
			return props;
		}
		
		public String getUri(){
			return uri;
		}
	}

	public static class UserCacheURI extends DataURIA{
	
		public UserCacheURI(Map<String, Object> props, String uri) {
			super(props,uri);
		}
	}

	public static class ArchiveURI extends DataURIA{
	
		public ArchiveURI(Map<String, Object> props, String uri) {
			super(props,uri);
		}
	}

	public static class PrearchiveURI extends DataURIA{
	
		public PrearchiveURI(Map<String, Object> props, String uri) {
			super(props,uri);
		}
	}
	
	public static interface DateURII{
		public Map<String,Object> getProps();
		public String getUri();
	}
	
	public static interface ArchiveItemURI extends DateURII{
		public abstract ArchivableItem getSecurityItem();
		public abstract List<XnatAbstractresourceI> getResources(boolean includeAll);
	}
	
	final Multimap<TEMPLATE_TYPE, TemplateInfo> TEMPLATES=ArrayListMultimap.create();
	
	private void add(final TEMPLATE_TYPE type,final String template,final int MODE,final Class<? extends URIManager.DataURIA> clazz){
		TEMPLATES.put(type,new TemplateInfo(template.intern(),MODE,clazz));		
	}
	
}
