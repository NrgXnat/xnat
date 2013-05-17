// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Fri Jun 29 12:54:15 CDT 2007
 *
 */
package org.nrg.xdat.om;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.nrg.xdat.om.base.BaseXdatUsergroup;
import org.nrg.xdat.security.ElementAccessManager;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.PermissionCriteria;
import org.nrg.xdat.security.PermissionItem;
import org.nrg.xdat.security.UserGroup;
import org.nrg.xdat.security.UserGroupManager;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.ItemI;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatUsergroup extends BaseXdatUsergroup {

	public XdatUsergroup(ItemI item)
	{
		super(item);
	}

	public XdatUsergroup(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatUsergroup(UserI user)
	 **/
	public XdatUsergroup()
	{}

	public XdatUsergroup(Hashtable properties, UserI user)
	{
		super(properties,user);
	}



    public void addRootPermission(String elementName,PermissionCriteria pc) throws Exception
    {
    	XdatElementAccess xea = null;
		for(XdatElementAccess temp:this.getElementAccess()){
			if(temp.getElementName().equals(elementName)){
				xea=temp;
				break;
			}
		}
		
		if(xea==null){
			xea=new XdatElementAccess((UserI)this.getUser());
			xea.setElementName(elementName);
			this.getItem().setChild("xdat:userGroup.element_access",xea.getItem(),true);
		}
		
		XdatFieldMappingSet xfms=null;
		final List<XdatFieldMappingSet> set=xea.getPermissions_allowSet();
		if(set.size()==0){
			xfms = new XdatFieldMappingSet(this.getUser());
			xfms.setMethod("OR");
			xea.setPermissions_allowSet(xfms);
		}else{
			xfms=set.get(0);
		}
		
		
		XdatFieldMapping xfm=null;
		
		for(XdatFieldMapping t:xfms.getAllow()){
			if(t.getField().equals(pc.getField()) && t.getFieldValue().equals(pc.getFieldValue())){
				xfm=t;
				break;
			}
		}
		
		if(xfm==null){
			xfm=new XdatFieldMapping(this.getUser());
			xfm.setField(pc.getField());
			xfm.setFieldValue((String)pc.getFieldValue());
			xfms.setAllow(xfm);
		}
		
		xfm.setCreateElement(pc.getCreate());
		xfm.setReadElement(pc.getRead());
		xfm.setEditElement(pc.getEdit());
		xfm.setDeleteElement(pc.getDelete());
		xfm.setActiveElement(pc.getActivate());
		xfm.setComparisonType(pc.getComparisonType());
    }

    public void removePermissions(String elementName,UserI user,EventMetaI c){
        try {
            final ElementSecurity es = ElementSecurity.GetElementSecurity(elementName);

            XdatElementAccess ea = null;
            for (XdatElementAccess temp:getElementAccess())
            {
                if(temp.getElementName().equals(elementName))
                {
                    ea= temp;
                    break;
                }
            }

            if (ea!=null)
            {
            	SaveItemHelper.unauthorizedDelete(ea.getItem(), user,c);
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        } catch (InvalidValueException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }
    }
    
    public void init(String elementName, String value,Boolean create,Boolean read,Boolean delete,Boolean edit,Boolean activate) throws Exception{
    	XdatElementAccess ea=new XdatElementAccess((UserI)this.getUser());
        ea.setElementName(elementName);
        this.setElementAccess(ea);
        
        //container for field mapping settings
        XdatFieldMappingSet fms = new XdatFieldMappingSet((UserI)this.getUser());
        fms.setMethod("OR");
        ea.setPermissions_allowSet(fms);
                            
        //access permissions for owned data
        XdatFieldMapping fm= new XdatFieldMapping((UserI)this.getUser());
        fm.init( elementName + "/project", value, create,read,delete,edit,activate);
        fms.setAllow(fm);

        //access permissions for shared data
        fm= new XdatFieldMapping((UserI)this.getUser());
        fm.init( elementName + "/sharing/share/project", value, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE);
        fms.setAllow(fm);
    }


    public boolean setPermissions(String elementName, String psf,String value,Boolean create,Boolean read,Boolean delete,Boolean edit,Boolean activate,boolean activateChanges, XDATUser user, boolean includesModification,EventMetaI c) throws Exception
    {
        try {
            final ElementSecurity es = ElementSecurity.GetElementSecurity(elementName);

                XdatElementAccess ea = null;
                for (XdatElementAccess temp:getElementAccess())
                {
                    if(temp.getElementName().equals(elementName))
                    {
                        ea= temp;
                        break;
                    }
                }

                if (ea==null)
                {
                    ea = new XdatElementAccess((UserI)user);
                    ea.setElementName(elementName);
                    ea.setProperty("xdat_usergroup_xdat_usergroup_id", this.getXdatUsergroupId());
                }

                XdatFieldMappingSet fms = null;
                ArrayList al =  ea.getPermissions_allowSet();
                if (al.size()>0){
                    fms = (XdatFieldMappingSet)ea.getPermissions_allowSet().get(0);
                }else{
                    fms = new XdatFieldMappingSet((UserI)user);
                    fms.setMethod("OR");
                    ea.setPermissions_allowSet(fms);
                }

                XdatFieldMapping fm = null;

                Iterator iter = fms.getAllow().iterator();
                while (iter.hasNext())
                {
                    Object o = iter.next();
                    if (o instanceof XdatFieldMapping)
                    {
                        if (((XdatFieldMapping)o).getFieldValue().equals(value) && ((XdatFieldMapping)o).getField().equals(psf)){
                            fm = (XdatFieldMapping)o;
                        }
                    }
                }

                if (fm ==null){
                	if(create || read || edit || delete || activate)
                		fm = new XdatFieldMapping((UserI)user);
                	else
                		return false;
                }else if(!includesModification){
                	if(!(create || read || edit || delete || activate)){
                		if(fms.getAllow().size()==1){
                			SaveItemHelper.authorizedDelete(fms.getItem(), user,c);
                			return true;
                		}else{
                			SaveItemHelper.authorizedDelete(fm.getItem(), user,c);
                			return true;
                		}
                	}
                    return false;
                }

                fm.init(psf, value, create, read, delete, edit, activate);
                
                fms.setAllow(fm);

                if (fms.getXdatFieldMappingSetId()!=null)
                {
                    fm.setProperty("xdat_field_mapping_set_xdat_field_mapping_set_id", fms.getXdatFieldMappingSetId());

                    if (activateChanges){
                    	SaveItemHelper.authorizedSave(fm,user, true, false, true, false,c);
                        fm.activate(user);
                    }else{
                    	SaveItemHelper.authorizedSave(fm,user, true, false, false, false,c);
                    }
                }else if(ea.getXdatElementAccessId()!=null){
                    fms.setProperty("permissions_allow_set_xdat_elem_xdat_element_access_id", ea.getXdatElementAccessId());
                    if (activateChanges){
                    	SaveItemHelper.authorizedSave(fms,user, true, false, true, false,c);
                        fms.activate(user);
                    }else{
                    	SaveItemHelper.authorizedSave(fms,user, true, false, false, false,c);
                    }
                }else{
                    if (activateChanges){
                    	SaveItemHelper.authorizedSave(ea,user, true, false, true, false,c);
                        ea.activate(user);
                    }else{
                    	SaveItemHelper.authorizedSave(ea,user, true, false, false, false,c);
                    }
                    this.setElementAccess(ea);
                }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        } catch (InvalidValueException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }

        return true;
    }

    /**
     * ArrayList: 0:elementName 1:ArrayList of PermissionItems
     * @return
     * @throws Exception
     */
    public List<List<Object>> getPermissionItems(String login) throws Exception
    {
        final ArrayList<List<Object>> allElements = new ArrayList<List<Object>>();
        final List<ElementSecurity> elements = ElementSecurity.GetSecureElements();
        
        Collections.sort(elements,((ElementSecurity)elements.get(0)).getComparator());
        
        UserGroup ug =UserGroupManager.GetGroup(this.getId());
        
        for (ElementSecurity es:elements)
        {
            final List<PermissionItem> permissionItems = es.getPermissionItems(login);
            boolean isAuthenticated = true;
            boolean wasSet = false;
            for (PermissionItem pi:permissionItems)
            {
                final ElementAccessManager eam = ug.getAccessManagers().get(es.getElementName());
                if (eam != null)
                {
                    final PermissionCriteria pc = eam.getRootPermission(pi.getFullFieldName(),pi.getValue());
                    if (pc != null)
                    {
                        pi.set(pc);
                    }
                }
                if (!pi.isAuthenticated())
                {
                    isAuthenticated = false;
                }
                if (pi.wasSet())
                {
                    wasSet = true;
                }
            }
            
            final List<Object> elementManager = new ArrayList<Object>();
            elementManager.add(es.getElementName());
            elementManager.add(permissionItems);
            elementManager.add(es.getSchemaElement().getSQLName());
            elementManager.add((isAuthenticated)?Boolean.TRUE:Boolean.FALSE);
            elementManager.add((wasSet)?Boolean.TRUE:Boolean.FALSE);
            
            if (permissionItems.size() > 0)
                allElements.add(elementManager);

        }
        return allElements;
    }
}
