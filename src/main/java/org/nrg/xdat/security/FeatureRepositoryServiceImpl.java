package org.nrg.xdat.security;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.FeatureDefinition;
import org.nrg.xdat.security.helpers.FeatureDefinitionI;
import org.nrg.xdat.security.services.FeatureRepositoryServiceI;
import org.nrg.xdat.services.FeatureDefinitionService;
import org.nrg.xft.XFT;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.EventUtils.CATEGORY;
import org.nrg.xft.utils.SaveItemHelper;

public class FeatureRepositoryServiceImpl implements FeatureRepositoryServiceI {
	static Logger logger = Logger.getLogger(FeatureRepositoryServiceImpl.class);
	private static final String ELEMENT_ACTION_NAME = "element_action_name";
	private static final String ON_BY_DEFAULT = "OnByDefault";
	private static final String FEATURE_DEFINITION_PROPERTIES = "-feature-definition.properties";
	private static final String NAME="name";
	private static final String DESC="description";
	private static final String KEY="key";
	private static final String[] PROP_OBJECT_FIELDS = new String[]{NAME,DESC,KEY,ON_BY_DEFAULT,ELEMENT_ACTION_NAME};
	private static final String PROP_OBJECT_IDENTIFIER = "org.nrg.Feature";
	
	static Boolean initd=false;//used to check if we've initialized yet and used for synchronization
	
	public FeatureRepositoryServiceImpl(){
		if(!initd){
			init();
		}
	}
	
	
	@Override
	public Collection<? extends FeatureDefinitionI> getAllFeatures() {
		if(!initd){
			init();
		}
		
		List<FeatureDefinition> features=XDAT.getContextService().getBean(FeatureDefinitionService.class).getAll();
		
		Collections.sort(features, new Comparator<FeatureDefinition>() {
			@Override
			public int compare(FeatureDefinition o1, FeatureDefinition o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
    	return features;
	}
	
	private void init(){
		synchronized (initd){
			if(initd){
				//if we had a race condition and a second thread was waiting here, while the first executed
				return ;
			}
			
			//load properties files
			//looks in WEB-INF/conf for anything that ends with -feature-definition.properties

			//EXAMPLE PROPERTIES FILE 
			//org.nrg.Feature=Feature1
			//org.nrg.Feature.Feature1.key=Feature1
			//org.nrg.Feature.Feature1.name=Feature One
			//org.nrg.Feature.Feature1.OnByDefault=false
			//org.nrg.Feature.Feature1.description=A sample feature that does something.
			
			//TODO: This is not safe for load balancing.  If two different servers hit this logic at the same time, you would have a problem.
			
			File[] propFiles=new File(XFT.GetConfDir()).listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(FEATURE_DEFINITION_PROPERTIES);
				}});
			
			if(propFiles!=null){
				for(File props: propFiles){
					final Map<String,Map<String,Object>> features=org.nrg.xdat.turbine.utils.PropertiesHelper.RetrievePropertyObjects(props, PROP_OBJECT_IDENTIFIER, PROP_OBJECT_FIELDS);
					
					List<FeatureDefinition> allfeatures=XDAT.getContextService().getBean(FeatureDefinitionService.class).getAllWithDisabled();
					
					for(Map<String,Object> feature:features.values()){
						
						FeatureDefinition def=new FeatureDefinition();
						def.setKey((String)feature.get(KEY));
						def.setName((String)feature.get(NAME));
						def.setDescription((String)feature.get(DESC));
						
						FeatureDefinition match=null;
						
						for(FeatureDefinition potential: allfeatures){
							if(potential.getKey().equals(def.getKey())){
								match=potential;
							}
						}
						
						if(match!=null){
							// already there
							if(!def.equals(match)){
								match.setName(def.getName());
								match.setDescription(def.getDescription());
								update(match);
							}
						}else{
							//is new
							//new ones can be turned on by default, old ones won't
							if(feature.get(ON_BY_DEFAULT)!=null && (BooleanUtils.toBoolean((String)feature.get(ON_BY_DEFAULT)))){
								def.setOnByDefault(Boolean.TRUE);
							}
							
							create(def);
							
							//after creating a new feature definition, if the feature is supposed to be related to an element action, it should be registered
							if(feature.get(ELEMENT_ACTION_NAME)!=null){
								String action_name=(String)feature.get(ELEMENT_ACTION_NAME);
								
								try {
									for(ElementSecurity es: ElementSecurity.GetElementSecurities().values()){
										for(ElementAction ea:es.getElementActions()){
											try {
												if(StringUtils.equals(ea.getName(), action_name)){
													if(!StringUtils.equals(ea.getSecureFeature(), def.getKey())){
														//need to register this action
														ea.getItem().setProperty("secureFeature", def.getKey());
														SaveItemHelper.authorizedSave(ea.getItem(), new XDATUser("admin"), true, false, EventUtils.newEventInstance(CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_SERVICE, "Configure new feature."));
													}
												}
											} catch (Exception e) {
												logger.error("",e);
												//otherwise ignore failure
											}
										}
									}
								} catch (Exception e) {
									logger.error("",e);
									//otherwise ignore failure
								}
							}
						}
					}
				}
			}
			
			initd=true;
		}
	}

	@Override
	public FeatureDefinitionI getByKey(String key) {
		return XDAT.getContextService().getBean(FeatureDefinitionService.class).findFeatureByKey(key);
	}


	public void create(FeatureDefinition feature) {
		XDAT.getContextService().getBean(FeatureDefinitionService.class).create(feature);
	}


	public void delete(FeatureDefinition feature) {
		XDAT.getContextService().getBean(FeatureDefinitionService.class).delete(feature);
	}


	public void update(FeatureDefinition feature) {
		XDAT.getContextService().getBean(FeatureDefinitionService.class).update(feature);
	}
	
	@Override
	public void banFeature(String feature){
		FeatureDefinition def=XDAT.getContextService().getBean(FeatureDefinitionService.class).findFeatureByKey(feature);
		if(def!=null){
			def.setBanned(true);
			update(def);
		}
	}

	@Override
	public void unBanFeature(String feature){
		FeatureDefinition def=XDAT.getContextService().getBean(FeatureDefinitionService.class).findFeatureByKey(feature);
		if(def!=null){
			def.setBanned(false);
			update(def);
		}
	}


	@Override
	public void enableByDefault(String feature) {
		FeatureDefinition def=XDAT.getContextService().getBean(FeatureDefinitionService.class).findFeatureByKey(feature);
		if(def!=null){
			def.setOnByDefault(true);
			update(def);
		}
	}


	@Override
	public void disableByDefault(String feature) {
		FeatureDefinition def=XDAT.getContextService().getBean(FeatureDefinitionService.class).findFeatureByKey(feature);
		if(def!=null){
			def.setOnByDefault(false);
			update(def);
		}
	}

}
