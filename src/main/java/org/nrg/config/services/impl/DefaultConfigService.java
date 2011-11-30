package org.nrg.config.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang.StringUtils;

import org.nrg.config.daos.ConfigurationDAO;
import org.nrg.config.daos.ConfigurationDataDAO;
import org.nrg.config.entities.Configuration;
import org.nrg.config.entities.ConfigurationData;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
public class DefaultConfigService implements ConfigService {

	public static BeanComparator ConfigComparatorByCreateDate = new BeanComparator("created");
	public static BeanComparator ConfigComparatorByVersion = new BeanComparator("version");

	@Autowired
	private ConfigurationDAO configurationDAO;
	
	@Autowired
	private ConfigurationDataDAO configurationDataDAO;
	
	@Transactional
	public List<Configuration> getAll(){
		return configurationDAO.getAll();
	}	
	
	@Override
	@Transactional
	public Configuration getById(Long id){
		return configurationDAO.findById(id);
	}
	
	@Transactional
	public List<Long> getProjects(){
		return getProjects(null);
	}
	
	@Transactional
	public List<Long> getProjects(String toolName){
		return configurationDAO.getProjects(toolName);
	}
	
	@Transactional
	private List<String> getToolsImpl(Long projectID){
		return configurationDAO.getTools(projectID);
	}
	
	@Transactional
	private List<Configuration> getConfigsByToolImpl(String toolName, Long projectID){
		return configurationDAO.getConfigurationsByTool(toolName, projectID);
	}
	
	//return the most recent configuration. Null if it does not exist.
	@SuppressWarnings("unchecked")
	@Transactional
	private Configuration getConfigImpl(String toolName, String path, Long projectID){
		List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
		if(list == null || list.size() ==0 ){
			return null;
		} else {
			Collections.sort(list,ConfigComparatorByCreateDate);
			return ((Configuration) list.get(list.size()-1));
		}
	}
	
	//return the most recent configuration contents. Null if it does not exist.
	@SuppressWarnings("unchecked")
	@Transactional
	private String getConfigContentsImpl(String toolName, String path, Long projectID){
		List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
		if(list == null || list.size() ==0 ){
			return null;
		} else {
			Collections.sort(list,ConfigComparatorByCreateDate);
			return ((Configuration) list.get(list.size()-1)).getContents();
		}
	}
	
	@Transactional
	private Configuration getConfigByIdImpl(String toolName, String path, String id, Long projectID){
		//I think it is more efficient to just pull by the ID and make sure it matches the other passed in variables.
		Configuration c = configurationDAO.findById(Long.parseLong(id));
		
		//findById is silly in that it will return a non-null object even if it doesn't find a match (I didn't write it). If it didn't find a match it
		//will throw an exception the first time you try to access a property. So, we'll use that to test for a valid return, here.
		//this also takes care of a null return.
		try {
			c.getTool();
		} catch (Exception e){
			return null;
		}
		if(StringUtils.equals(c.getTool(), toolName) && StringUtils.equals(c.getPath(), path) && ObjectUtils.nullSafeEquals((c.getProject()), projectID)){
			return c;
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	private Configuration getConfigByVersionImpl(String toolName, String path, int version, Long projectID){
		List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
		if(list == null || list.size() < version ){
			return null;
		} else {
			Collections.sort(list,ConfigComparatorByVersion);
			Configuration ret = ((Configuration) list.get(version-1));
			//this will only fail if something truly stupid happened. Still should check, though.
			if(ret.getVersion() == version){
				return ret;
			} else {
				//something odd happened, let's search the list for the version
				for(Configuration c: list){
					if(c.getVersion() == version){
						return c;
					}
				}
				return null;
			}
		}
	}
	
	@Transactional
	private Configuration replaceConfigImpl(String xnatUser, String reason, String toolName, String path, String contents, Long projectID) throws ConfigServiceException{
		
		if(contents != null && contents.length() > ConfigService.MAX_FILE_LENGTH){
			throw new ConfigServiceException("file size must be less than " + ConfigService.MAX_FILE_LENGTH + " characters.");
		}
		
		int version = 1;
		ConfigurationData configData = null;
		
		//if a current config exists and the contents are the same as the previous version, share the config data
		Configuration oldConfig = getConfig(toolName, path, projectID);

		if(oldConfig != null && oldConfig.getConfigData()!= null && contents != null && contents.equals(oldConfig.getConfigData().getContents() ) ){
			configData = oldConfig.getConfigData();
		} else {
			configData = new ConfigurationData();
			configData.setContents(contents);
			configurationDataDAO.create(configData);
		}
		if(oldConfig != null){
			version += oldConfig.getVersion();
		}
		Configuration c = new Configuration();
		c.setTool(toolName);
		c.setConfigData(configData);
		c.setPath(path);
		c.setProject(projectID);
		c.setXnatUser(xnatUser);
		c.setReason(reason);
		c.setStatus(Configuration.ENABLED_STRING);
		c.setVersion(version);

		configurationDAO.create(c);
		return c;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	private String getStatusImpl(String toolName, String path, Long projectID){
		List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
		if(list == null || list.size() == 0) return null;
		Collections.sort(list,ConfigComparatorByCreateDate);
		return ((Configuration) list.get(list.size()-1)).getStatus();
	}
	
	
	//fail silently if the configuration does not exist, throws an unsupported operation exception if status is null.
	@SuppressWarnings("unchecked")
	@Transactional
	private void setStatus(String xnatUser, String reason, String toolName, String path, String status, Long projectID) {

		if(status == null){
			throw new UnsupportedOperationException("Unable to set a config's status to null");
		}

		List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
		if(list == null || list.size() == 0) {
			//fail silently if the configuration does not exist...
			return;
		}
		Collections.sort(list,ConfigComparatorByCreateDate);
		Configuration entity = ((Configuration) list.get(list.size()-1));
		
		if(status.equals(entity.getStatus())){
			return;
		} else {
			//changing and existing Configuration is a no-no. We have to create a new Configuration with a new xnatUser and reason
			Configuration newConfig = new Configuration();
			newConfig.setProject(entity.getProject());
			newConfig.setTool(entity.getTool());
			newConfig.setPath( entity.getPath());
			newConfig.setConfigData(entity.getConfigData());
			
			newConfig.setStatus(status);
			newConfig.setXnatUser(xnatUser);
			newConfig.setReason(reason);
			configurationDAO.create(newConfig);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	private List<Configuration> getHistoryImpl(String toolName, String path, Long projectID){

		List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
		if(list == null || list.size() == 0) return null;
		Collections.sort(list,ConfigComparatorByCreateDate);
		return list;
	}
	
	/*
	 * Why are there 4 methods for each action? At first, there were 2. The one w/o a project would simply
	 * pass null to the method that took in project.  I was asked to also provide a Callable<Long> projectID
	 * for each action.  Doing that made two methods with signatures that would match a call to (s,p, null)
	 * because both Long and Callable would match with the null. I didn't want to copy paste code for both the
	 * Callable and the Long methods, so I had to create an Impl for each set... that made 4 methods.
	 * 
	 * Sorry.
	 * 
	 */	
	@Transactional
	public List<String> getTools(){
		return getToolsImpl(null);
	}
	
	@Transactional
	public List<String> getTools(Callable<Long> projectID){
		try {
			return getToolsImpl(projectID.call());
		} catch (Exception e){
			return null;
		}
	}
	@Transactional
	public List<String> getTools(Long projectID){
		return getToolsImpl(projectID);
	}
		
	@Transactional
	public List<Configuration> getConfigsByTool(String toolName){
		return getConfigsByToolImpl(toolName, null);
	}

	@Transactional
	public List<Configuration> getConfigsByTool(String toolName, Long projectID){
		return getConfigsByToolImpl(toolName, projectID);
	}

	@Transactional
	public List<Configuration> getConfigsByTool(String toolName, Callable<Long> projectID){
		try {
			return getConfigsByToolImpl(toolName, projectID.call());
		} catch (Exception e){
			return null;
		}
	}
	
	@Transactional
	public Configuration getConfig(String toolName, String path){
		return getConfigImpl(toolName, path, null);	
	}
	
	@Transactional
	public Configuration getConfig(String toolName, String path, Callable<Long> projectID){
		try {
			return getConfigImpl(toolName, path, projectID.call());
		} catch (Exception e){
			return null;
		}
	}
	
	@Transactional
	public Configuration getConfig(String toolName, String path, Long projectID){
		return getConfigImpl(toolName, path, projectID);
	}
	
	@Transactional
	public String getConfigContents(String toolName, String path){
		return getConfigContentsImpl(toolName, path, null);	
	}
	
	@Transactional
	public String getConfigContents(String toolName, String path, Callable<Long> projectID){
		try{
			return getConfigContentsImpl(toolName, path, projectID.call());
		} catch (Exception e){
			return null;
		}
	}

	@Transactional
	public String getConfigContents(String toolName, String path, Long projectID){
		return getConfigContentsImpl(toolName, path, projectID);
	}
	
	@Transactional
	public Configuration getConfigById(String toolName, String path, String id){
		return getConfigByIdImpl(toolName, path, id, null);
	}
	
	@Transactional
	public Configuration getConfigById(String toolName, String path, String id, Callable<Long> projectID){
		try {
			return getConfigByIdImpl(toolName, path, id, projectID.call());
		} catch (Exception e){
			return null;
		}
	}
	
	@Transactional
	public Configuration getConfigById(String toolName, String path, String id, Long projectID){
		return getConfigByIdImpl(toolName, path, id, projectID);
	}	

	@Transactional
	public Configuration getConfigByVersion(String toolName, String path, int version){
		return getConfigByVersionImpl(toolName, path, version, null);
	}
	
	@Transactional
	public Configuration getConfigByVersion(String toolName, String path, int version, Callable<Long> projectID){
		try {
			return getConfigByVersionImpl(toolName, path, version, projectID.call());
		} catch (Exception e){
			return null;
		}
	}
	
	@Transactional
	public Configuration getConfigByVersion(String toolName, String path, int version, Long projectID){
		return getConfigByVersionImpl(toolName, path, version, projectID);
	}
	
	@Transactional
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents) throws ConfigServiceException{
		return replaceConfigImpl(xnatUser, reason, toolName, path,contents, null);
	}
	
	@Transactional
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents, Callable<Long> projectID) throws ConfigServiceException{
		try {
			return replaceConfigImpl(xnatUser,reason,toolName,path,contents,projectID.call());
		} catch (Exception e){
			return null;
		}
	}

	@Transactional
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents, Long projectID) throws ConfigServiceException{
		return replaceConfigImpl(xnatUser,reason,toolName,path,contents,projectID);
	}
	
	@Transactional
	public String getStatus(String toolName, String path){
		return getStatusImpl(toolName, path, null);
	}
	
	@Transactional
	public String getStatus(String toolName, String path, Callable<Long> projectID){
		try {
			return getStatusImpl(toolName, path, projectID.call());
		} catch (Exception e){
			return null;
		}
	}
	
	@Transactional
	public String getStatus(String toolName, String path, Long projectID){
		return getStatusImpl(toolName, path, projectID);
	}
	
	@Transactional
	public void enable(String xnatUser, String reason, String toolName, String path) {
		setStatus(xnatUser,reason, toolName, path, Configuration.ENABLED_STRING, null);
	}
	
	@Transactional
	public void enable(String xnatUser, String reason, String toolName, String path, Callable<Long> projectID) {
		try {
			setStatus(xnatUser,reason, toolName, path, Configuration.ENABLED_STRING, projectID.call());
		} catch (Exception e){
		}
	}
	
	//fail silently if the configuration does not exist...
	@Transactional
	public void enable(String xnatUser, String reason, String toolName, String path, Long projectID) {
		setStatus(xnatUser,reason, toolName, path, Configuration.ENABLED_STRING, projectID);
	}

	//fail silently if the configuration does not exist...
	@Transactional
	public void disable(String xnatUser, String reason, String toolName, String path) {
		setStatus(xnatUser,reason, toolName, path, Configuration.DISABLED_STRING, null);
	}

	@Transactional
	public void disable(String xnatUser, String reason, String toolName, String path, Callable<Long> projectID) {
		try {
			setStatus(xnatUser,reason, toolName, path, Configuration.DISABLED_STRING, projectID.call());
		} catch (Exception e){
			return;
		}
	}

	//fail silently if the configuration does not exist...
	@Transactional
	public void disable(String xnatUser, String reason, String toolName, String path, Long projectID) {
		setStatus(xnatUser,reason, toolName, path, Configuration.DISABLED_STRING, projectID);
	}
	
	@Transactional
	public List<Configuration> getHistory(String toolName, String path){
		return getHistoryImpl(toolName, path, null);
	}
	
	@Transactional
	public List<Configuration> getHistory(String toolName, String path, Callable<Long> projectID){
		try {
			return getHistoryImpl(toolName, path, projectID.call());
		} catch (Exception e){
			return null;
		}
	}
	
	@Transactional
	public List<Configuration> getHistory(String toolName, String path, Long projectID){
		return getHistoryImpl(toolName, path, projectID);
	}

	
	/* Setters and Getters */
	
	public ConfigurationDAO getConfigurationDAO() {
		return configurationDAO;
	}
	
	public void setConfigurationDataDAO(ConfigurationDataDAO configurationDataDAO) {
		this.configurationDataDAO = configurationDataDAO;
	}
	
	public ConfigurationDataDAO getConfigurationDataDAO() {
		return configurationDataDAO;
	}
	
	public void setConfigurationDAO(ConfigurationDAO configurationDAO) {
		this.configurationDAO = configurationDAO;
	}
}
