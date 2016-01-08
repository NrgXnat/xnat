package org.nrg.xdat.security.services.impl;

import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.security.services.RoleRepositoryServiceI;
import org.nrg.xdat.turbine.utils.PropertiesHelper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class RoleRepositoryServiceImpl implements RoleRepositoryServiceI {

    private static final String ROLE_DEFINITION_PACKAGE = "config.roles";
	private static final Pattern ROLE_DEFINITION_PROPERTIES = Pattern.compile(".*-role-definition\\.properties");
	private static final String NAME="name";
	private static final String DESC="description";
	private static final String KEY="key";
	private static final String WARNING="warning";
	private static final String[] PROP_OBJECT_FIELDS = new String[]{NAME,DESC,KEY,WARNING};
	private static final String PROP_OBJECT_IDENTIFIER = "org.nrg.Role";
	
	public static Collection<RoleDefinitionI> allRoles=null;
	
	public RoleRepositoryServiceImpl(){
		init();
	}

	private void init(){
		if(allRoles==null){
			//load properties files
			//looks in WEB-INF/conf for anything that ends with -role-definition.properties

			//EXAMPLE PROPERTIES FILE 
			//org.nrg.Role=Role1
			//org.nrg.Role.Role1.key=Role1
			//org.nrg.Role.Role1.name=Role One
			//org.nrg.Role.Role1.description=A sample Role that does something.
			
			allRoles=new TreeSet<>(new Comparator<RoleDefinitionI>(){
                @Override
                public int compare(final RoleDefinitionI o1, final RoleDefinitionI o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });

            final Set<String> propFiles = Reflection.findResources(ROLE_DEFINITION_PACKAGE, ROLE_DEFINITION_PROPERTIES);

            if(propFiles!=null){
                for(final String props: propFiles) {

                    final Map<String,Map<String,Object>> roles = PropertiesHelper.RetrievePropertyObjects(props, PROP_OBJECT_IDENTIFIER, PROP_OBJECT_FIELDS);

					for(Map<String,Object> role:roles.values()){
						
						POJORole def=new POJORole();
						def.setKey((String)role.get(KEY));
						def.setName(role.get(NAME).toString());
						def.setDescription(role.get(DESC).toString());
						def.setWarning(role.get(WARNING).toString());
						
						allRoles.add(def);
					}
				}
			}
		}
	}

	
	@Override
	public Collection<RoleDefinitionI> getRoles() {
		return allRoles;
	}
	
	public static class POJORole implements RoleDefinitionI{
		private String name;
		private String description;
		private String key;
		private String warning;
		
		public String getWarning() {
			return warning;
		}
		public void setWarning(String warning) {
			this.warning = warning;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
	}

}
