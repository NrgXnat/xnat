package org.nrg.config.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

@Service
public class DefaultConfigService implements ConfigService {

	public static BeanComparator ConfigComparatorByCreateDate = new BeanComparator("created");

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
	public List<String> getTools(){
		return configurationDAO.getTools();
	}
	
	@Transactional
	public List<String> getTools(String projectID){
		return configurationDAO.getTools(projectID);
	}
	
	@Transactional
	public List<Configuration> getConfigsByTool(String toolName){
		return getConfigsByTool(toolName, null);
	}
	
	@Transactional
	public List<Configuration> getConfigsByTool(String toolName, String projectID){
		return configurationDAO.getConfigurationsByTool(toolName, projectID);
	}
	
	@Transactional
	public Configuration getConfig(String toolName, String path){
		return getConfig(toolName, path, null);	
	}
	
	//return the most recent configuration. Null if it does not exist.
	@SuppressWarnings("unchecked")
	@Transactional
	public Configuration getConfig(String toolName, String path, String projectID){

		List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
		if(list == null || list.size() ==0 ){
			return null;
		} else {
			Collections.sort(list,ConfigComparatorByCreateDate);
			return ((Configuration) list.get(list.size()-1));
		}
	}
	
	@Transactional
	public String getConfigContents(String toolName, String path){
		return getConfigContents(toolName, path, null);	
	}
	
	//return the most recent configuration contents. Null if it does not exist.
	@SuppressWarnings("unchecked")
	@Transactional
	public String getConfigContents(String toolName, String path, String projectID){

		List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
		if(list == null || list.size() ==0 ){
			return null;
		} else {
			Collections.sort(list,ConfigComparatorByCreateDate);
			return ((Configuration) list.get(list.size()-1)).getContents();
		}
	}
	
	@Transactional
	public Configuration getConfigById(String toolName, String path, String id){
		return getConfigById(toolName, path, id, null);
	}
	
	@Transactional
	public Configuration getConfigById(String toolName, String path, String id, String projectID){
		//I think it is more efficient to just pull by the ID and make sure it matches the other passed in variables.
		Configuration c = configurationDAO.findById(Long.parseLong(id));
		
		//findById is silly in that it will return a non-null object even if it doesn't find a match... If it didn't find a match it
		//will throw an exception the first time you try to access a property. So, we'll use that to test for a valid return, here.
		//this also takes care of a null return.
		try {
			c.getTool();
		} catch (Exception e){
			return null;
		}

		if(StringUtils.equals(c.getTool(), toolName) && StringUtils.equals(c.getPath(), path) && StringUtils.equals(c.getProject(), projectID)){
			return c;
		} else {
			return null;
		}
	}
	
	@Transactional
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents) throws ConfigServiceException{
		return replaceConfig(xnatUser, reason, toolName, path,contents, null);
	}
	
	@Transactional
	public Configuration replaceConfig(String xnatUser, String reason, String toolName, String path, String contents, String projectID) throws ConfigServiceException{
		
		if(contents != null && contents.length() > ConfigService.MAX_FILE_LENGTH){
			throw new ConfigServiceException("file size must be less than " + ConfigService.MAX_FILE_LENGTH + " characters.");
		}
		
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

		Configuration c = new Configuration();
		c.setTool(toolName);
		c.setConfigData(configData);
		c.setPath(path);
		c.setProject(projectID);
		c.setXnatUser(xnatUser);
		c.setReason(reason);
		c.setStatus(Configuration.ENABLED_STRING);

		configurationDAO.create(c);
		return c;
	}
	
	@Transactional
	public String getStatus(String toolName, String path){
		return getStatus(toolName, path, null);
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public String getStatus(String toolName, String path, String projectID){

		List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
		if(list == null || list.size() == 0) return null;
		Collections.sort(list,ConfigComparatorByCreateDate);
		return ((Configuration) list.get(list.size()-1)).getStatus();
	}
	
	@Transactional
	public void enable(String xnatUser, String reason, String toolName, String path) {
		setStatus(xnatUser,reason, toolName, path, Configuration.ENABLED_STRING, null);
	}

	//fail silently if the configuration does not exist...
	@Transactional
	public void enable(String xnatUser, String reason, String toolName, String path, String projectID) {
		setStatus(xnatUser,reason, toolName, path, Configuration.ENABLED_STRING, projectID);
	}

	//fail silently if the configuration does not exist...
	@Transactional
	public void disable(String xnatUser, String reason, String toolName, String path) {
		setStatus(xnatUser,reason, toolName, path, Configuration.DISABLED_STRING, null);
	}

	//fail silently if the configuration does not exist...
	@Transactional
	public void disable(String xnatUser, String reason, String toolName, String path, String projectID) {
		setStatus(xnatUser,reason, toolName, path, Configuration.DISABLED_STRING, projectID);
	}
	
	//fail silently if the configuration does not exist, throws an unsupported operation exception if status is null.
	@SuppressWarnings("unchecked")
	@Transactional
	private void setStatus(String xnatUser, String reason, String toolName, String path, String status, String projectID) {

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
	
	@Transactional
	public List<Configuration> getHistory(String toolName, String path){
		return getHistory(toolName, path, null);
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public List<Configuration> getHistory(String toolName, String path, String projectID){
		List<Configuration> list = configurationDAO.findByToolPathProject(toolName, path, projectID);
		if(list == null || list.size() == 0) return null;
		Collections.sort(list,ConfigComparatorByCreateDate);
		return list;
	}
	
	@Transactional
	public List<String> getProjects(){
		return getProjects(null);
	}
	
	@Transactional
	public List<String> getProjects(String toolName){
		return configurationDAO.getProjects(toolName);
	}
	
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
