package org.nrg.xnat.turbine.modules.actions;


import org.nrg.framework.utilities.Reflection;

import java.util.List;
import java.util.Map;

import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xdat.om.XnatProjectdata;

import org.nrg.xft.security.UserI;

public abstract class AbstractProjectSecureAction extends SecureAction {

    public interface PreSaveAction {
        void execute(UserI user, XnatProjectdata src, Map<String, String> params, PersistentWorkflowI wrk) throws Exception;
    }


    protected void dynamicPreSave(UserI user, XnatProjectdata src, Map<String,String> params, PersistentWorkflowI wrk) throws Exception{
        List<Class<?>> classes = Reflection.getClassesForPackage("org.nrg.xnat.actions.projectEdit.preSave");

        if(classes!=null && classes.size()>0){
            for(Class<?> clazz: classes){
                if(AddProject.PreSaveAction.class.isAssignableFrom(clazz)){
                    AddProject.PreSaveAction action=(AddProject.PreSaveAction)clazz.newInstance();
                    action.execute(user,src,params,wrk);
                }
            }
        }
    }
}
