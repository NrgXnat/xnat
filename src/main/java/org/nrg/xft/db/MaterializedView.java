// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.db;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.views.service.CustomMaterializedViewService;
import org.nrg.xft.db.views.service.MaterializedViewServiceI;
import org.nrg.xft.security.UserI;

import java.util.List;
import java.util.Map;

public class MaterializedView {
	public static final String CACHING_HANDLER = "cachingHandler";
	static org.apache.log4j.Logger logger = Logger.getLogger(MaterializedView.class);
	
	public static final String DEFAULT_MATERIALIZED_VIEW_SERVICE="org.nrg.xft.db.views.service.LegacyMaterializedViewServiceImpl";
	public static final String DEFAULT_MATERIALIZED_VIEW_SERVICE_CODE="default";
	
	private static Map<String,Class<?>> services=null;

    private static void getServices(){
        if(services==null){
            //default to LegacyMaterializedViewServiceImpl implementation (unless a different default is configured)
            //we can swap in other ones later by setting a default
            //we can even have a config tab in the admin ui which allows sites to select their configuration of choice.
            services=Maps.newHashMap();

            List<Class<?>> classes;
            try {
                classes = Reflection.getClassesForPackage("org.nrg.xft.db.views.service.custom");
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }

            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(CustomMaterializedViewService.class)) {
                    CustomMaterializedViewService annotation = clazz.getAnnotation(CustomMaterializedViewService.class);
                    String handledCode = annotation.value();
                    if (!MaterializedViewServiceI.class.isAssignableFrom(clazz)) {
                        String message = "You can only apply the CustomMaterializedViewService annotation to classes that implement the MaterializedViewServiceI interface";
                        logger.error(message);
                    } else {
                        services.put(handledCode, clazz);
                    }
                }
            }

            try {
                String className=XDAT.getSiteConfigurationProperty("db.materializedViewService.default", DEFAULT_MATERIALIZED_VIEW_SERVICE);
                services.put(DEFAULT_MATERIALIZED_VIEW_SERVICE_CODE, Class.forName(className));
            } catch (ClassNotFoundException e) {
                logger.error("",e);
            } catch (ConfigServiceException e) {
                logger.error("",e);
            }

        }
    }

    public static MaterializedViewServiceI getViewManagementService(String code){
        getServices();
    	
	    try{
	    	if(services.containsKey(code)){
	    		Class<?> clazz=services.get(code);
	    		return (MaterializedViewServiceI)clazz.newInstance();
	    	}else{
				String className=XDAT.getSiteConfigurationProperty("db.materializedViewService.default", DEFAULT_MATERIALIZED_VIEW_SERVICE);
				return (MaterializedViewServiceI)Class.forName(className).newInstance();
	    	}
		} catch (ClassNotFoundException e) {
			logger.error("",e);
		} catch (InstantiationException e) {
			logger.error("",e);
		} catch (IllegalAccessException e) {
			logger.error("",e);
		} catch (ConfigServiceException e) {
			logger.error("",e);
		}
	    
	    return null;
    }
	
	public static void deleteByUser(UserI user)throws Exception{
        getServices();
		for(Class<?> serviceC : services.values()){
			MaterializedViewServiceI service=(MaterializedViewServiceI)serviceC.newInstance();
			service.deleteViewsByUser(user);
		}
	}
	
	public static MaterializedViewI createView(UserI user,String code)throws Exception{
		MaterializedViewServiceI service=getViewManagementService(code);
		return service.createView(user);
	}

	public static Long createView(String tablename, String query,UserI user, String code) throws Exception{
	    MaterializedViewI mv = createView(user,code);
	    mv.setTable_name(tablename);
	    mv.setSearch_sql(query);
	    save(mv,code);
	
	    return mv.getSize();
	}

    public static void save(MaterializedViewI viewI, String code) throws Exception {
        MaterializedViewServiceI service=getViewManagementService(code);
        service.save(viewI);
    }

    public static void delete(MaterializedViewI viewI) throws Exception{
        MaterializedViewServiceI service=getViewManagementService(viewI.getCode());
        service.delete(viewI);
    }

	public static XFTTable retrieveView(String tablename,UserI user,int offset, int rowsPerPage) throws Exception{
	    MaterializedViewI mv = retrieveView(tablename, user);
	    if(mv==null){
	    	return null;
	    }else{
	    	return mv.getData(null, offset, rowsPerPage);
	    }
	}

	public static MaterializedViewI retrieveView(String tablename,UserI user) throws Exception{
		String code=DEFAULT_MATERIALIZED_VIEW_SERVICE_CODE;
		if(tablename.indexOf("#")>-1){
			code=tablename.substring(0,tablename.indexOf("#"));
			tablename=tablename.substring(tablename.indexOf("#")+1);
		}
		
		MaterializedViewServiceI service=getViewManagementService(code);
		return service.getViewByTablename(tablename,user);
	}
	
	public static MaterializedViewI getViewBySearchID(String search_id, UserI user, String code) throws Exception{
		MaterializedViewServiceI service=getViewManagementService(code);
		return service.getViewBySearchID(search_id,user);
	}
}
