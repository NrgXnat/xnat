/*
 * core: org.nrg.xdat.security.UserGroup
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import com.google.common.base.Function;
import com.google.common.collect.*;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.GroupFeature;
import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatFieldMapping;
import org.nrg.xdat.om.XdatFieldMappingSet;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.services.GroupFeatureService;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UserGroup implements UserGroupI{
	public UserGroup(XdatUsergroup gp) throws Exception{
		id=gp.getId();
		pk=gp.getXdatUsergroupId();
		displayName=gp.getDisplayname();
		init(gp.getItem());
	}
	
	public UserGroup(){

	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.security.UserGroupI#getId()
	 */
	@Override
	public String getId(){
		return id;
	}

	@Override
	public String getTag(){
		return tag;
	}

	@Override
	public String getDisplayname(){
		return displayName;
	}

	protected synchronized Map<String,ElementAccessManager> getAccessManagers() {
        if (accessManagers.isEmpty()){
            try {
                init(getUserGroupImpl());
            } catch (Exception e) {
                logger.error("",e);
            }
        }
        return accessManagers;
    }
    
    public void clearUserGroup(){
		xdatGroup = null;
	}

    public XdatUsergroup getUserGroupImpl(){
    	if(xdatGroup==null){
    		return XdatUsergroup.getXdatUsergroupsById(id, null, true);
    	}else{
    		return xdatGroup;
    	}
    }
    
    private XdatUsergroup getSavedUserGroupImpl(UserI user){
    	if(xdatGroup==null){
    		xdatGroup=XdatUsergroup.getXdatUsergroupsById(id, user, true);
    		
    		if(xdatGroup==null){
    			xdatGroup= new XdatUsergroup(user);
    			xdatGroup.setId(this.getId());
    			xdatGroup.setTag(this.getTag());
    			xdatGroup.setDisplayname(this.getDisplayname());
    		}
    	}
    	return xdatGroup;
    }

    public synchronized void init(ItemI item) throws Exception
    {
    	tag = XftStringUtils.intern(item.getStringProperty("tag"));

		for (final ItemI sub : item.getChildItems("xdat:userGroup.element_access")) {
			final ElementAccessManager eam = new ElementAccessManager(sub);
			accessManagers.put(eam.getElement(), eam);
		}

        for(GroupFeature feature:(XDAT.getContextService().getBean(GroupFeatureService.class).findFeaturesForGroup(this.getId()))){
        	if(feature.isBlocked()){
            	blocked.add(feature.getFeature());
        	}else if(feature.isOnByDefault()){
            	features.add(feature.getFeature());
        	}
        }
    }


    public String toString(){
    	StringBuilder sb = new StringBuilder();
    	sb.append(getId()).append("\n");
    	sb.append(getTag()).append("\n");
    	
    	for(ElementAccessManager eam:this.getAccessManagers().values()){
    		sb.append(eam.toString()).append("\n");
    	}
    	
    	return sb.toString();
    }

    /**
     * @return Returns a list of stored search objects
     */
    protected List<XdatStoredSearch> getStoredSearches() {
        if (_storedSearches.isEmpty()) {
			synchronized (_storedSearches) {
				try {
                    for (final XdatStoredSearch search : XdatStoredSearch.GetPreLoadedSearchesByAllowedGroup(id)) {
                        _storedSearches.put(search.getId(), search);
                    }
                } catch (Exception e) {
                    logger.error("",e);
                }
			}
		}
        return ImmutableList.copyOf(_storedSearches.values());
    }


    /**
	 * Retrieves the stored search associated with the submitted ID.
     * @param id The ID of the stored search to retrieve.
     * @return Returns the stored search for the given ID
     */
    protected XdatStoredSearch getStoredSearch(String id) {
    	if (_storedSearches.isEmpty()) {
    		getStoredSearches();
		}
        return _storedSearches.get(id);
    }

    protected void replacePreLoadedSearch(final XdatStoredSearch search){
		try {
			final String id  = search.getStringProperty("ID");
			_storedSearches.put(id, search);
		} catch (ElementNotFoundException | FieldNotFoundException e) {
			logger.error("",e);
		}
	}

    /**
     * ArrayList: 0:elementName 1:ArrayList of PermissionItems
     * @return Returns a list of the lists of PermissionItems for each element
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unchecked")
	public List<List<Object>> getPermissionItems(String login) throws Exception {
		if (!_permissionItemsByLogin.containsKey(login)) {
			final List<ElementSecurity> elements = ElementSecurity.GetSecureElements();

			Collections.sort(elements, elements.get(0).getComparator());

			for (ElementSecurity es : elements) {
				final List<PermissionItem> permissionItems = (this.getTag() == null) ? es.getPermissionItems(login) : es.getPermissionItemsForTag(this.getTag());
				boolean                    isAuthenticated = true;
				boolean                    wasSet          = false;
				for (PermissionItem pi : permissionItems) {
					final ElementAccessManager eam = this.getAccessManagers().get(es.getElementName());
					if (eam != null) {
						final PermissionCriteriaI pc = eam.getMatchingPermissions(pi.getFullFieldName(), pi.getValue());
						if (pc != null) {
							pi.set(pc);
						}
					}
					if (!pi.isAuthenticated()) {
						isAuthenticated = false;
					}
					if (pi.wasSet()) {
						wasSet = true;
					}
				}

				final List<Object> elementManager = new ArrayList<>();
				elementManager.add(es.getElementName());
				elementManager.add(permissionItems);
				elementManager.add(es.getSchemaElement().getSQLName());
				elementManager.add((isAuthenticated) ? Boolean.TRUE : Boolean.FALSE);
				elementManager.add((wasSet) ? Boolean.TRUE : Boolean.FALSE);
				elementManager.add(es);

				if (permissionItems.size() > 0) {
					_permissionItemsByLogin.put(login, elementManager);
				}
			}
		}

		return ImmutableList.copyOf(_permissionItemsByLogin.get(login));
	}

	@Override
	public Integer getPK() {
		return pk;
	}

    public Map<String,List<PermissionCriteriaI>> getAllPermissions() {
		if (_permissionCriteriaByDataType.isEmpty()) {
			for(final ElementAccessManager manager : getAccessManagers().values()) {
                final String element = manager.getElement();
                for (final PermissionSetI permissions : manager.getPermissionSets()) {
                    if (permissions.isActive()) {
                        final List<PermissionCriteriaI> criteria = permissions.getAllCriteria();
                        _permissionCriteriaByDataType.putAll(element, criteria);
                        for (final PermissionCriteriaI criterion : criteria) {
                            _permissionCriteriaByDataTypeAndField.put(formatTypeAndField(element, criterion.getField()), criterion);
                        }
                    }
                }
            }
		}

		return Maps.transformValues(Multimaps.asMap(_permissionCriteriaByDataType), new Function<Collection<PermissionCriteriaI>, List<PermissionCriteriaI>>() {
			@Override
			public List<PermissionCriteriaI> apply(final Collection<PermissionCriteriaI> input) {
				return new ArrayList<>(input);
			}
		});
    }


    public List<PermissionCriteriaI> getPermissionsByDataType(final String type) {
    	if (!_permissionCriteriaByDataType.containsKey(type)) {
    		getAllPermissions();
		}

        return ImmutableList.copyOf(_permissionCriteriaByDataType.get(type));
    }

	@SuppressWarnings("unused")
	public List<PermissionCriteriaI> getPermissionsByDataTypeAndField(String dataType, String field){
		final String compositeKey = formatTypeAndField(dataType, field);
		if (!_permissionCriteriaByDataTypeAndField.containsKey(compositeKey)) {
			getPermissionsByDataType(dataType);
		}

		return _permissionCriteriaByDataTypeAndField.containsKey(compositeKey) ? ImmutableList.copyOf(_permissionCriteriaByDataTypeAndField.get(compositeKey)) : Collections.<PermissionCriteriaI>emptyList();
    }

	@Override
	public void setId(String id) {
		this.id=id;
	}

	@Override
	public void setTag(String tag) {
		this.tag=tag;
	}

	@Override
	public void setDisplayname(String displayName) {
		this.displayName=displayName;
	}

	@Override
	public void setPK(Integer pk) {
		this.pk=pk;
	}
	
    public void addPermission(String elementName,PermissionCriteriaI pc, UserI authenticatedUser) throws Exception
    {
    	XdatUsergroup xdatGroup=getSavedUserGroupImpl(authenticatedUser);
    	
    	XdatElementAccess xea = null;
		for(XdatElementAccess temp:xdatGroup.getElementAccess()){
			if(temp.getElementName().equals(elementName)){
				xea=temp;
				break;
			}
		}
		
		if(xea==null){
			xea=new XdatElementAccess(authenticatedUser);
			xea.setElementName(elementName);
			xdatGroup.setElementAccess(xea);
		}
		
		final XdatFieldMappingSet xfms;
		final List<XdatFieldMappingSet> set=xea.getPermissions_allowSet();
		if(set.size()==0){
			xfms = new XdatFieldMappingSet(authenticatedUser);
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
			xfm=new XdatFieldMapping(authenticatedUser);
			xfm.setField(pc.getField());
			xfm.setFieldValue((String)pc.getFieldValue());
			xfms.setAllow(xfm);
		}
		
		xfm.setCreateElement(pc.getCreate());
		xfm.setReadElement(pc.getRead());
		xfm.setEditElement(pc.getEdit());
		xfm.setDeleteElement(pc.getDelete());
		xfm.setActiveElement(pc.getActivate());
		xfm.setComparisonType("equals");
    }
    
    public List<String> getFeatures(){
    	return ImmutableList.copyOf(features);
    }
    
    public List<String> getBlockedFeatures(){
    	return ImmutableList.copyOf(blocked);
    }

	private static String formatTypeAndField(final String type, final String field) {
		return type + "/" + field;
	}

	private static final Logger logger = LoggerFactory.getLogger(UserGroup.class);

	private String        id          = null;
	private Integer       pk          = null;
	private String        tag         = null;
	private String        displayName = null;
	private XdatUsergroup xdatGroup   = null;

	private final Map<String, ElementAccessManager>     accessManagers                        = new HashMap<>();
	private final Multimap<String, PermissionCriteriaI> _permissionCriteriaByDataType         = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, PermissionCriteriaI>create());
	private final Multimap<String, PermissionCriteriaI> _permissionCriteriaByDataTypeAndField = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, PermissionCriteriaI>create());
	private final Multimap<String, List<Object>>        _permissionItemsByLogin               = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, List<Object>>create());
	private final Map<String, XdatStoredSearch>         _storedSearches                       = new HashMap<>();
	private final List<String>                          features                              = new ArrayList<>();
	private final List<String>                          blocked                               = new ArrayList<>();
}
