/*
 * org.nrg.xdat.security.XDATUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 3:42 PM
 */


package org.nrg.xdat.security;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xdat.entities.UserRole;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.om.XdatUserGroupid;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.search.QueryOrganizer;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.ActivationException;
import org.nrg.xdat.security.user.exceptions.EnabledException;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xdat.security.user.exceptions.PasswordAuthenticationException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.UserRoleService;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.MetaDataException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.StringUtils;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Tim
 */
@SuppressWarnings({"unchecked"})
public class XDATUser extends XdatUser implements UserI, Serializable {
    private static final long serialVersionUID = -8144623503683531831L;
    static Logger logger = Logger.getLogger(XDATUser.class);
    public static final String USER_ELEMENT = "xdat:user";
    private Hashtable<String, ElementAccessManager> accessManagers = null;
    private final Map<String, UserGroupI> groups = Maps.newHashMap();
    private final Map<String, String> groupsByTag = Maps.newHashMap();
    private boolean extended = false;
    private List<String> roleNames = null;
    private ArrayList<XdatStoredSearch> stored_searches = null;

    private ArrayList<ElementDisplay> browseable = null;

    long startTime = Calendar.getInstance().getTimeInMillis();

    private UserAuthI _authorization;
    private List<GrantedAuthority> _authorities;
    /**
     * DO NOT USE THIS.  IT is only for use in unit testings.  Use XDATUser(login).
     */
    public XDATUser() {
    }

    public XDATUser(String login) throws UserNotFoundException,UserInitException {
        super((UserI) null);
        try {
			SchemaElementI e = SchemaElement.GetElement(USER_ELEMENT);

			ItemSearch search = new ItemSearch(null, e.getGenericXFTElement());
			search.addCriteria(USER_ELEMENT + XFT.PATH_SEPERATOR + "login", login);
			search.setLevel(ViewManager.ACTIVE);
			ArrayList found = search.exec(true).items();

			if (found.size() == 0) {
			    throw new UserNotFoundException(login);
			} else {
			    setItem((ItemI) found.get(0));
			}

			if (!isExtended()) {
			    init();
			    if (!this.getItem().isPreLoaded()) extend(true);
			    setExtended(true);
			}
		} catch (XFTInitException e) {
			throw new UserInitException(e.getMessage());
		} catch (ElementNotFoundException e) {
			throw new UserInitException(e.getMessage());
		} catch (DBPoolException e) {
			throw new UserInitException(e.getMessage());
		} catch (SQLException e) {
			throw new UserInitException(e.getMessage());
		} catch (Exception e) {
			throw new UserInitException(e.getMessage());
		}
        if (XFT.VERBOSE) System.out.println("User(login) Loaded (" + (Calendar.getInstance().getTimeInMillis() - startTime) + ")ms");
    }

    public XDATUser(ItemI i) throws UserInitException {
        this(i, true);
    }

    public XDATUser(ItemI i, boolean extend) throws UserInitException  {
        super((UserI) null);
        setItem(i);
        if (extend) {
            if (!isExtended()) {
                try {
					init();
					if (!this.getItem().isPreLoaded()) extend(true);
					setExtended(true);
				}catch (Exception e) {
					throw new UserInitException(e.getMessage());
				}
            }
        }
    }

    public XDATUser(String login, String password) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException, FieldNotFoundException, FailedLoginException, PasswordAuthenticationException, Exception {
        this(login);

        if (Authenticator.Authenticate(this, new Authenticator.Credentials(login, password))) {
            if (!isExtended()) {
                init();
                if (!this.getItem().isPreLoaded()) extend(true);
                setExtended(true);
            }
        }
        if (XFT.VERBOSE) System.out.println("User(login,password) Loaded (" + (Calendar.getInstance().getTimeInMillis() - startTime) + ")ms");
    }

    public boolean login(String password) throws PasswordAuthenticationException, Exception {
        boolean loggedIn = false;

        if (!this.isEnabled()) {
            throw new EnabledException(this.getUsername());
        }

        if ((!this.isActive()) && (!this.checkRole("Administrator"))) {
            throw new ActivationException(this.getUsername());
        }

        String pass = (String) this.getStringProperty("primary_password");
        String salt = (String) this.getStringProperty("salt");

        if (StringUtils.IsEmpty(pass)) throw new PasswordAuthenticationException(getUsername());

        // encryption
        if (new ShaPasswordEncoder(256).isPasswordValid(pass, password, salt)) {
            loggedIn = true;
        } else if (new ObfuscatedPasswordEncoder(256).isPasswordValid(pass, password, salt)) {
            loggedIn = true;
        }

        if (!loggedIn) {
            throw new PasswordAuthenticationException(getUsername());
        }
        return loggedIn;
    }

    private ElementAccessManager getAccessManager(String s) throws Exception {
        return this.getAccessManagers().get(s);
    }

    public synchronized void init() throws Exception {
        final long startTime = Calendar.getInstance().getTimeInMillis();
        accessManagers = new Hashtable<String, ElementAccessManager>();
        final Iterator items = this.getChildItems(USER_ELEMENT + ".element_access").iterator();

        while (items.hasNext()) {
            final ItemI sub = (ItemI) items.next();
            final ElementAccessManager eam = new ElementAccessManager(sub);
//            if (guestManagers.containsKey(eam.getElement())){
//                eam.setGuestManager(guestManagers.get(eam.getElement()));
//            }
            accessManagers.put(eam.getElement(), eam);
        }

        this.groups.clear();
        this.groupsByTag.clear();
        this.stored_searches = null;
        this.clearLocalCache();

        if (XFT.VERBOSE) System.out.println("User Init(" + this.getUsername() + "): " + (Calendar.getInstance().getTimeInMillis() - startTime) + "ms");
    }

    public boolean isEnabled() {
        try {
            return this.getItem().getBooleanProperty("enabled", true);
        } catch (Exception e) {
            return false;
        }
    }
    
    public Boolean isVerified() {
        try {
            return this.getItem().getBooleanProperty("verified", true);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return
     */
    public String getUsername() {
        try {
            return (String) getProperty("login");
        } catch (ElementNotFoundException e) {
            logger.error("", e);
            return null;
        } catch (FieldNotFoundException e) {
            logger.error("", e);
            return null;
        }
    }

    /**
     * @return
     */
    public String getFirstname() {
        try {
            return super.getFirstname();
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    /**
     * @return
     */
    public String getLastname() {
        try {
            return super.getLastname();
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    /**
     * @return
     */
    public String getEmail() {
        try {
            return super.getEmail();
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    public Integer getID() {
        try {
            return super.getIntegerProperty("xdat_user_id");
        } catch (FieldNotFoundException e) {
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        }
        return null;
    }


    public boolean isGuest() {
        final String login = getLogin();
        return StringUtils.IsEmpty(login) || login.equalsIgnoreCase("guest");
    }
    
    public String getPassword(){
    	return getPrimaryPassword();
    }

	@Override
	public Date getLastModified() {
		return this.getItem().getLastModified();
	}

	@Override
	public Object getCustomField(String key) {
		if(org.apache.commons.lang.StringUtils.equalsIgnoreCase(key,"DN")){
			//this is to support an old hack that used a dead field (quarantine path) to store DN data
			return getQuarantinePath();
		}
		return null;
	}

	@Override
	public void setCustomField(String key, Object value) throws Exception{
		if(value!=null){
			if(org.apache.commons.lang.StringUtils.equalsIgnoreCase(key,"DN")){
				//this is to support an old hack that used a dead field (quarantine path) to store DN data
				this.setQuarantinePath(value.toString());
			}else{
				throw new Exception("Not Currently Supported.");
			}
		}
	}

	@Override
	public void setPassword(String encodePassword) {
		this.setPrimaryPassword(encodePassword);
	}
    /**
     * ArrayList of ElementDisplays which this user could edit
     *
     * @return
     * @throws ElementNotFoundException
     * @throws XFTInitException
     */
	protected List<ElementDisplay> getCreateableElementDisplays() throws ElementNotFoundException, XFTInitException, Exception {
        return getActionElementDisplays(SecurityManager.CREATE);
    }

    private final static Comparator NameComparator = new Comparator() {
        public int compare(Object mr1, Object mr2) throws ClassCastException {
            try {
                String value1 = ((ElementDisplay) mr1).getElementName();
                String value2 = ((ElementDisplay) mr2).getElementName();

                return value1.compareToIgnoreCase(value2);
            } catch (Exception ex) {
                throw new ClassCastException("Error Comparing Sequence");
            }
        }
    };

    private final static Comparator DescriptionComparator = new Comparator() {
        public int compare(Object mr1, Object mr2) throws ClassCastException {
            try {
                String value1 = ((ElementDisplay) mr1).getSchemaElement().getSingularDescription();
                String value2 = ((ElementDisplay) mr2).getSchemaElement().getSingularDescription();

                return value1.compareToIgnoreCase(value2);
            } catch (Exception ex) {
                throw new ClassCastException("Error Comparing Sequence");
            }
        }
    };

    private final static Comparator PluralDescriptionComparator = new Comparator() {
        public int compare(Object mr1, Object mr2) throws ClassCastException {
            try {
                String value1 = ((ElementDisplay) mr1).getSchemaElement().getPluralDescription();
                String value2 = ((ElementDisplay) mr2).getSchemaElement().getPluralDescription();

                return value1.compareToIgnoreCase(value2);
            } catch (Exception ex) {
                throw new ClassCastException("Error Comparing Sequence");
            }
        }
    };

    /**
     * ArrayList of ElementDisplays which this user could edit
     *
     * @return
     * @throws ElementNotFoundException
     * @throws XFTInitException
     */
    private List<ElementDisplay> getActionElementDisplays(final String action) throws ElementNotFoundException, XFTInitException, Exception {
        final Map<String, ElementDisplay> hash = new Hashtable<String, ElementDisplay>();
                
        for(ElementSecurity es: ElementSecurity.GetSecureElements()){
            if (es.getSchemaElement().hasDisplay()){
	        	if(Permissions.canAny(this, es.getElementName(), action)){
	        		ElementDisplay ed = es.getSchemaElement().getDisplay();
	                if (ed != null) {
	                    hash.put(ed.getElementName(), ed);
	                }
	        	}
            }
        }
    	
        for (ElementSecurity es : ElementSecurity.GetInSecureElements()) {
            if (es.getSchemaElement().hasDisplay()) {
                hash.put(es.getElementName(), es.getSchemaElement().getDisplay());
            }
        }
        
        final ArrayList<ElementDisplay> al = new ArrayList<ElementDisplay>();
        al.addAll(hash.values());
        al.trimToSize();

        Collections.sort(al, ElementDisplay.SequenceComparator);
        return al;
    }

    /**
     * ArrayList of ElementDisplays which this user could edit
     *
     * @return
     * @throws ElementNotFoundException
     * @throws XFTInitException
     */
    protected List<ElementDisplay> getEditableElementDisplays() throws ElementNotFoundException, XFTInitException, Exception {
        return getActionElementDisplays(SecurityManager.EDIT);
    }

    /**
     * ArrayList of ElementDisplays Which this user can read
     *
     * @return
     * @throws ElementNotFoundException
     * @throws XFTInitException
     */
    protected List<ElementDisplay> getReadableElementDisplays() throws ElementNotFoundException, XFTInitException, Exception {
        return getActionElementDisplays(SecurityManager.READ);
    }

    /**
     * @return
     */

    protected Hashtable<String, ElementAccessManager> getAccessManagers() throws Exception {
        if (this.accessManagers == null) {
            this.init();
        }
        return this.accessManagers;
    }


    protected DisplaySearch getSearch(String elementName, String display) throws Exception {
        DisplaySearch search = new DisplaySearch();
        search.setUser(this);
        search.setDisplay(display);
        search.setRootElement(elementName);
        return search;
    }

    /**
     * ArrayList of ElementDisplays
     *
     * @return
     * @throws Exception
     */
    protected List<ElementDisplay> getUnSecuredElements() throws XFTInitException, ElementNotFoundException, Exception {
        final List<ElementDisplay> al = new ArrayList<ElementDisplay>();

        for (ElementSecurity es : ElementSecurity.GetInSecureElements()) {
            if (es.getSchemaElement().hasDisplay()) {
                al.add(es.getSchemaElement().getDisplay());
            }
        }
        return al;
    }

    /**
     * @return
     */
    public boolean isExtended() {
        return extended;
    }

    /**
     * @param b
     */
    public void setExtended(boolean b) {
        extended = b;
    }
    
    /**
     * Removes the user from the specified role.  The authenticatedUser must be a site admin.  The operation will remove the role from the old XFT structure and the new Hibernate structure.
     * @param authenticatedUser
     * @param dRole
     * @throws Exception
     */
    public void deleteRole(UserI authenticatedUser, String dRole) throws Exception{
		if(!((XDATUser)authenticatedUser).isSiteAdmin()){
			throw new Exception("Invalid permissions for user modification.");
		}
		
    	for (ItemI role: (List<ItemI>)this.getChildItems(org.nrg.xft.XFT.PREFIX + ":user.assigned_roles.assigned_role")) {
    		if(org.apache.commons.lang.StringUtils.equals(role.getStringProperty("role_name"),dRole)){
    			PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, authenticatedUser, "xdat:user",this.getStringProperty("xdat_user_id"),PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Removed " + dRole + " role"));
    			EventMetaI ci=wrk.buildEvent();
    			SaveItemHelper.unauthorizedRemoveChild(this.getItem(), "xdat:user/assigned_roles/assigned_role", role.getItem(), authenticatedUser,ci);
    			PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
    		}
        }
    	
    	List<UserRole> roles =XDAT.getContextService().getBean(UserRoleService.class).findRolesForUser(this.getLogin());
        if(roles!=null){
        	for(final UserRole ur: roles){
        		if(org.apache.commons.lang.StringUtils.equals(ur.getRole(), dRole)){
        			XDAT.getContextService().getBean(UserRoleService.class).delete(ur);
        			PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, authenticatedUser, "xdat:user",this.getStringProperty("xdat_user_id"),PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Removed " + dRole + " role"));
        			PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
        		}
        	}
        }
    }
    
    /**
     * Method used to add a user to a role. The authenticatedUser must be a site admin.  It stores the role in the Hibernate UserRole impl.
     * @param authenticatedUser
     * @param dRole
     * @throws Exception
     */
    public void addRole(UserI authenticatedUser, String dRole) throws Exception{
    	if(!StringUtils.IsEmpty(dRole)){
	    	if(XDAT.getContextService().getBean(UserRoleService.class).findUserRole(this.getLogin(), dRole)==null){
	    		if(!((XDATUser)authenticatedUser).isSiteAdmin()){
	    			throw new Exception("Invalid permissions for user modification.");
	    		}
	    		
	    		XDAT.getContextService().getBean(UserRoleService.class).addRoleToUser(this.getLogin(), dRole);
	    		
	    		PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, authenticatedUser, "xdat:user",this.getStringProperty("xdat_user_id"),PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Added " + dRole + " role"));
	    		PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
	    	}
    	}
    }
    
    private List<String> loadRoleNames() throws Exception {
    	final List<String> r=Lists.newArrayList();
    	
    	//load from the old role store
        for (ItemI sub: (List<ItemI>)this.getChildItems(org.nrg.xft.XFT.PREFIX + ":user.assigned_roles.assigned_role")) {
            r.add(sub.getStringProperty("role_name"));
        }
        
        try {
			//load from the new role store
        	//TODO: Fix it so that this is required in tomcat mode, but optional in command line mode.
			UserRoleService roleService=XDAT.getContextService().getBean(UserRoleService.class);
			if(roleService!=null){
			    List<UserRole> roles =roleService.findRolesForUser(this.getLogin());
			    if(roles!=null){
			    	for(final UserRole ur: roles){
			    		r.add(ur.getRole());
			    	}
			    }
			}else{
				logger.error("skipping user role service review... service is null");
			}
		} catch (Throwable e) {
			logger.error("",e);
		}
        
        return r;
    }
    
    /**
     * @return ArrayList of XFTItems
     */
    public List<String> getRoleNames() {
        if (roleNames == null) {
            try {
				roleNames = loadRoleNames();
			} catch (Exception e) {
				logger.error("",e);
			}
        }
        return roleNames;
    }

    public boolean checkRole(String s) throws Exception {
        boolean check = false;

        Iterator roles = getRoleNames().iterator();
        while (roles.hasNext()) {
            String role = (String) roles.next();
            if (s.equalsIgnoreCase(role)) {
                check = true;
                break;
            }
        }

        return check;
    }
    
    protected ArrayList<ElementDisplay> getBrowseableElementDisplays() {
        if (browseable == null) {
            browseable = new ArrayList<ElementDisplay>();

            Map counts = this.getReadableCounts();
            try {
                Iterator iter = getReadableElementDisplays().iterator();
                while (iter.hasNext()) {
                    ElementDisplay ed = (ElementDisplay) iter.next();
                    if (ElementSecurity.IsBrowseableElement(ed.getElementName())) {
                        if (counts.containsKey(ed.getElementName()) && ((Long) counts.get(ed.getElementName()) > 0))
                            browseable.add(ed);
                    }
                }
            } catch (ElementNotFoundException e) {
                logger.error("", e);
            } catch (XFTInitException e) {
                logger.error("", e);
            } catch (Exception e) {
                logger.error("", e);
            }
            browseable.trimToSize();
        }

        return browseable;
    }

    protected ElementDisplay getBrowseableElementDisplay(String elementName) {
        for (ElementDisplay ed : getBrowseableElementDisplays()) {
            if (ed.getElementName().equals(elementName)) {
                return ed;
            }
        }

        return null;
    }

    protected ArrayList<ElementDisplay> getBrowseableCreateableElementDisplays() {
        ArrayList<ElementDisplay> elementDisplays = new ArrayList<ElementDisplay>();
        try {
            Iterator iter = getCreateableElementDisplays().iterator();
            while (iter.hasNext()) {
                ElementDisplay ed = (ElementDisplay) iter.next();
                if (ElementSecurity.IsBrowseableElement(ed.getElementName())) {
                    elementDisplays.add(ed);
                }
            }
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (XFTInitException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }

        Collections.sort(elementDisplays, new Comparator<ElementDisplay>() {
            @Override
            public int compare(ElementDisplay elementDisplay1, ElementDisplay elementDisplay2) {
                int sequence1 = 1000;
                try {
                    sequence1 = elementDisplay1.getElementSecurity().getSequence();
                } catch (Exception ignored) {
                    // Just ignore it.
                }
                int sequence2 = 1000;
                try {
                    sequence2 = elementDisplay2.getElementSecurity().getSequence();
                } catch (Exception ignored) {
                    // Just ignore it.
                }
                return sequence1 == sequence2 ? 0 : (sequence1 < sequence2 ? -1 : 1);
            }
        });

        return elementDisplays;
    }


    ArrayList<ElementDisplay> searchable = null;

    protected ArrayList<ElementDisplay> getSearchableElementDisplays() {
        return getSearchableElementDisplays(NameComparator);
    }

    protected ArrayList<ElementDisplay> getSearchableElementDisplaysByDesc() {
        return getSearchableElementDisplays(DescriptionComparator);
    }

    protected ArrayList<ElementDisplay> getSearchableElementDisplaysByPluralDesc() {
        return getSearchableElementDisplays(PluralDescriptionComparator);
    }

    protected ArrayList<ElementDisplay> getSearchableElementDisplays(Comparator comp) {
        if (searchable == null) {
            searchable = new ArrayList<ElementDisplay>();
            Map counts = this.getReadableCounts();
            try {
                Iterator iter = getReadableElementDisplays().iterator();
                while (iter.hasNext()) {
                    ElementDisplay ed = (ElementDisplay) iter.next();
                    if (ElementSecurity.IsSearchable(ed.getElementName())) {
                        if (counts.containsKey(ed.getElementName()) && ((Long) counts.get(ed.getElementName()) > 0))
                            searchable.add(ed);
                    }
                }
            } catch (ElementNotFoundException e) {
                logger.error("", e);
            } catch (XFTInitException e) {
                logger.error("", e);
            } catch (Exception e) {
                logger.error("", e);
            }
            searchable.trimToSize();

            Collections.sort(searchable, comp);
        }

        return searchable;
    }

    /**
     * @return
     */
    protected List<XdatStoredSearch> getStoredSearches() {
        if (this.stored_searches == null) {
            try {
                stored_searches = XdatStoredSearch.GetPreLoadedSearchesByAllowedUser(this.getLogin());

            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return stored_searches;
    }

    protected void replacePreLoadedSearch(XdatStoredSearch i) {
        try {
            ItemI old = getStoredSearchItem(i.getStringProperty("ID"));
            if (old != null) {
                stored_searches.remove(old);
            }
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
        }
        stored_searches.add(i);
    }

    private String getServer() {
        if (TurbineUtils.GetFullServerPath().endsWith("/")) {
            return TurbineUtils.GetFullServerPath();
        } else {
            return TurbineUtils.GetFullServerPath() + "/";
        }
    }

    /**
     * @param id
     * @return
     */
    private XdatStoredSearch getStoredSearchItem(String id) {
        List<XdatStoredSearch> temp = getStoredSearches();
        XdatStoredSearch xss = null;
        try {
            for (XdatStoredSearch search : temp) {
                if (id.equalsIgnoreCase(search.getStringProperty("xdat:stored_search.ID"))) {
                    xss = search;
                }
            }
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }
        return xss;
    }

    /**
     * @param id
     * @return
     */
    protected XdatStoredSearch getStoredSearch(String id) {
        List<XdatStoredSearch> temp = getStoredSearches();
        XdatStoredSearch xss = null;
        try {
            for (XdatStoredSearch search : temp) {
                if (id.equalsIgnoreCase(search.getStringProperty("xdat:stored_search.ID"))) {
                    xss = search;
                }
            }

            return xss;
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }

    private boolean canReadStoredSearch(String ss_id) {
        Object o = getStoredSearch(ss_id);
        if (o == null) {
            return false;
        } else {
            return true;
        }
    }


    protected void initGroups() {
        synchronized (groups) {
            groups.clear();
            getGroups();
        }
    }
 
    protected Map<String, UserGroupI> getGroups() {
        synchronized (groups) {
            if (groups.size() == 0) {
                try {
                    XFTTable t = XFTTable.Execute("SELECT * FROM xdat_user_groupid WHERE groups_groupid_xdat_user_xdat_user_id=" + this.getXdatUserId(), this.getDBName(), this.getLogin());
 
                    List<String> groupids = t.convertColumnToArrayList("groupid");
 
                    for (int i = 0; i < groupids.size(); i++) {
                        String groupID = (String) groupids.get(i);
                        UserGroupI group = Groups.getGroup(groupID);
                        if (group != null) {
                            groups.put(groupID, group);
                            if(group.getTag()!=null){
                            	groupsByTag.put(group.getTag(), group.getId());
                            }
                        }
                    }
                } catch (SQLException e) {
                    logger.error("", e);
                } catch (DBPoolException e) {
                    logger.error("", e);
                }
            }
        }
 
        return groups;
    }

    protected void addGroup(String id) {
        UserGroupI g = Groups.getGroup(id);
        if (g != null) {
            getGroups().put(id, g);
        }
    }

    protected Date getPreviousLogin() throws SQLException, Exception {
        String query = "SELECT login_date FROM xdat_user_login WHERE user_xdat_user_id=" + this.getXdatUserId() + " AND login_date < (SELECT MAX(login_date) FROM xdat_user_login WHERE user_xdat_user_id=" + this.getXdatUserId() + ") ORDER BY login_date DESC LIMIT 1";
        return (Date) PoolDBUtils.ReturnStatisticQuery(query, "login_date", this.getDBName(), this.getUsername());
    }

    protected void refreshGroup(String id) {
        Map<String, UserGroupI> groups = getGroups();
        if (groups.containsKey(id)) {
            UserGroupI g = Groups.getGroup(id);
            if (g != null) {
                groups.remove(groups.get(id));
                groups.put(id, g);
            }
        }
    }
    
    public boolean isSiteAdmin(){
    	try {
			return this.checkRole("Administrator");
		} catch (Exception e) {
			return false;
		}
    }

    private UserGroupI getGroup(String id) {
        return getGroups().get(id);
    }

    public UserGroupI getGroupByTag(String tag) {
    	if(groupsByTag.size()==0){
    		initGroups();
    	}
    	return Groups.getGroup(groupsByTag.get(tag));
    }

    protected ArrayList getRecentItems(String elementName, int limit) {
        ArrayList al = new ArrayList();
        try {
            QueryOrganizer qo = new QueryOrganizer(elementName, this, ViewManager.ALL);
            GenericWrapperElement gwe = GenericWrapperElement.GetElement(elementName);

            Iterator iter = gwe.getAllPrimaryKeys().iterator();
            ArrayList pks = new ArrayList();
            while (iter.hasNext()) {
                GenericWrapperField f = (GenericWrapperField) iter.next();
                String pk = f.getXMLPathString(elementName);
                qo.addField(pk);
                pks.add(pk);
            }

            qo.addField(elementName + "/meta/insert_date");
            String query = qo.buildQuery();

            XFTTable t = XFTTable.Execute(query + " ORDER BY " + qo.translateXMLPath(elementName + "/meta/insert_date") + " DESC LIMIT " + limit, gwe.getDbName(), this.getLogin());
            t.resetRowCursor();


            while (t.hasMoreRows()) {
                Hashtable row = t.nextRowHash();
                ItemSearch is = ItemSearch.GetItemSearch(elementName, this);
                Iterator pkIter = pks.iterator();
                while (pkIter.hasNext()) {
                    String pk = (String) pkIter.next();
                    is.addCriteria(pk, row.get(qo.translateXMLPath(pk).toLowerCase()));
                }

                ItemCollection items = is.exec(false);
                if (items.size() > 0) {
                    al.add(BaseElement.GetGeneratedItem(items.first()));
                }
            }
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (XFTInitException e) {
            logger.error("", e);
        } catch (SQLException e) {
            logger.error("", e);
        } catch (DBPoolException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }

        return al;
    }


    /**
     * returns an ArrayList of ArrayLists
     * xmlPaths: comma-delimited list of xmlPaths to return
     *
     * @param xmlPaths
     * @param rootElement
     * @return
     */
    protected List<List> getQueryResults(String xmlPaths, String rootElement) {
        ArrayList results = new ArrayList();
        try {

            QueryOrganizer qo = new QueryOrganizer(rootElement, this, ViewManager.ALL);

            ArrayList fields = StringUtils.CommaDelimitedStringToArrayList(xmlPaths);
            for (int i = 0; i < fields.size(); i++) {
                qo.addField((String) fields.get(i));
            }

            String query = qo.buildQuery();

            XFTTable t = getQueryResults(query);

            ArrayList<Integer> colHeaders = new ArrayList<Integer>();
            for (int i = 0; i < fields.size(); i++) {
                String header = qo.translateXMLPath((String) fields.get(i));
                Integer index = t.getColumnIndex(header.toLowerCase());
                colHeaders.add(index);
            }

            t.resetRowCursor();

            while (t.hasMoreRows()) {
                Object[] row = t.nextRow();

                ArrayList newRow = new ArrayList();

                for (Integer index : colHeaders) {
                    newRow.add(row[index.intValue()]);
                }

                results.add(newRow);
            }

        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (SQLException e) {
            logger.error("", e);
        } catch (DBPoolException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }
        return results;
    }

    protected XFTTable getQueryResults(String query) throws SQLException, DBPoolException {
        return XFTTable.Execute(query, this.getDBName(), this.getLogin());
    }

    protected ArrayList<List> getQueryResultsAsArrayList(String query) throws SQLException, DBPoolException {
        XFTTable t = XFTTable.Execute(query, this.getDBName(), this.getLogin());
        return t.toArrayListOfLists();
    }

    protected boolean canCreateElement(String elementName, String xmlPath) throws Exception {
        return Permissions.canAny(this, elementName, xmlPath, SecurityManager.CREATE);
    }

    private Hashtable<String, ArrayList<ItemI>> userSessionCache = new Hashtable<String, ArrayList<ItemI>>();

    protected ArrayList<ItemI> getCachedItems(String elementName, String security_permission, boolean preLoad) {
        if (!userSessionCache.containsKey(elementName + security_permission + preLoad)) {
            ItemSearch search = new ItemSearch();
            ArrayList<ItemI> items = new ArrayList<ItemI>();
            try {
                search.setElement(elementName);
                if (security_permission != null && security_permission.equals(SecurityManager.READ)) search.setUser(this);
                search.setAllowMultiples(preLoad);
                ArrayList<ItemI> al = search.exec().getItems();

                if (security_permission != null && !security_permission.equals(SecurityManager.READ)) {
                    for (ItemI item : al) {
                        if (Permissions.can(this, item, security_permission)) {
                            item.getItem().setUser(this);
                            items.add(BaseElement.GetGeneratedItem(item));
                        }
                    }
                } else {
                    for (ItemI item : al) {
                        item.getItem().setUser(this);
                        items.add(BaseElement.GetGeneratedItem(item));
                    }
                }

                userSessionCache.put(elementName + security_permission + preLoad, items);
            } catch (ElementNotFoundException e) {
                logger.error("", e);
            } catch (IllegalAccessException e) {
                logger.error("", e);
            } catch (MetaDataException e) {
                logger.error("", e);
            } catch (Throwable e) {
                logger.error("", e);
            }
        }

        return userSessionCache.get(elementName + security_permission + preLoad);
    }

    protected void clearLocalCache() {
        userSessionCache = new Hashtable<String, ArrayList<ItemI>>();
        total_counts = null;
        readable_counts = null;
        criteria=null;
        _editableProjects = null;
    }

    Map readable_counts = null;

    protected Map getReadableCounts() {
        if (readable_counts == null) {
            try {
                readable_counts = new Hashtable<String, Long>();

                try {
                    //projects
                    org.nrg.xft.search.QueryOrganizer qo = new org.nrg.xft.search.QueryOrganizer("xnat:projectData", this, ViewManager.ALL);
                    qo.addField("xnat:projectData/ID");

                    String query = qo.buildQuery();

                    String idField = qo.translateXMLPath("xnat:projectData/ID");

                    Long proj_count = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM (" + qo.buildQuery() + ") SEARCH;", "count", this.getDBName(), this.getUsername());
                    readable_counts.put("xnat:projectData", proj_count);

                    //subjects
                    qo = new org.nrg.xft.search.QueryOrganizer("xnat:subjectData", this, ViewManager.ALL);
                    qo.addField("xnat:subjectData/ID");

                    query = qo.buildQuery();
                    idField = qo.translateXMLPath("xnat:subjectData/ID");

                    Long sub_count = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM (" + qo.buildQuery() + ") SEARCH;", "count", this.getDBName(), this.getUsername());
                    readable_counts.put("xnat:subjectData", sub_count);

                    //workflows
                    qo = new org.nrg.xft.search.QueryOrganizer("wrk:workflowData", this, ViewManager.ALL);
                    qo.addField("wrk:workflowData/ID");

                    //
                    // These two lines were causing an issue where the file viewer was not opening. (See XNAT-2511)
                    // Since we are reusing the query and idField variables below, if we overwrite them here it causes
                    // any experiment dataType that was added before XNAT 1.6 (when we started using the wrk:workflowData
                    // table heavily) to not be returned from this method.
                    //
                    // query = qo.buildQuery();
                    // idField = qo.translateXMLPath("wrk:workflowData/ID");
                    //

                    Long wrk_count = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM (" + qo.buildQuery() + ") SEARCH;", "count", this.getDBName(), this.getUsername());
                    readable_counts.put("wrk:workflowData", wrk_count);

                    //experiments
                    query = StringUtils.ReplaceStr(query, idField, "id");
                    query = StringUtils.ReplaceStr(query, "xnat_subjectData", "xnat_experimentData");
                    query = StringUtils.ReplaceStr(query, "xnat_projectParticipant", "xnat_experimentData_share");
                    query = StringUtils.ReplaceStr(query, "subject_id", "sharing_share_xnat_experimentda_id");

                    XFTTable t = XFTTable.Execute("SELECT element_name, COUNT(*) FROM (" + query + ") SEARCH  LEFT JOIN xnat_experimentData expt ON search.id=expt.id LEFT JOIN xdat_meta_element xme ON expt.extension=xme.xdat_meta_element_id GROUP BY element_name", this.getDBName(), this.getUsername());
                    readable_counts.putAll(t.convertToHashtable("element_name", "count"));
                } catch (org.nrg.xdat.exceptions.IllegalAccessException e) {
                    //not a member of anything
                    System.out.println("USER:" + this.getUsername() + " doesn't have access to any project data.");
                }
            } catch (SQLException e) {
                logger.error("", e);
            } catch (DBPoolException e) {
                logger.error("", e);
            } catch (Exception e) {
                logger.error("", e);
            }
        }

        return readable_counts;
    }

    Map total_counts = null;

    protected Map<String,Long> getTotalCounts() {
        if (total_counts == null) {
            try {
                total_counts = new Hashtable<String, Long>();

                Long proj_count = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM xnat_projectData", "count", this.getDBName(), this.getUsername());
                total_counts.put("xnat:projectData", proj_count);

                Long sub_count = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM xnat_subjectData", "count", this.getDBName(), this.getUsername());
                total_counts.put("xnat:subjectData", sub_count);

                XFTTable t = XFTTable.Execute("SELECT element_name, COUNT(ID) FROM xnat_experimentData expt LEFT JOIN xdat_meta_element xme ON expt.extension=xme.xdat_meta_element_id GROUP BY element_name", this.getDBName(), this.getUsername());

                total_counts.putAll(t.convertToHashtable("element_name", "count"));
            } catch (SQLException e) {
                logger.error("", e);
            } catch (DBPoolException e) {
                logger.error("", e);
            } catch (Exception e) {
                logger.error("", e);
            }
        }

        return total_counts;
    }

    protected ArrayList<ItemI> getCachedItemsByFieldValue(String elementName, String security_permission, boolean preLoad, String field, Object value) {
        ArrayList<ItemI> items = getCachedItems(elementName, security_permission, preLoad);
        ArrayList<ItemI> results = new ArrayList<ItemI>();
        if (items.size() > 0) {
            for (ItemI i : items) {
                try {
                    Object v = i.getProperty(field);
                    if (v != null) {
                        if (v.equals(value)) {
                            results.add(i);
                        }
                    }
                } catch (XFTInitException e) {
                    logger.error("", e);
                } catch (ElementNotFoundException e) {
                    logger.error("", e);
                } catch (FieldNotFoundException e) {
                    logger.error("", e);
                }
            }
        }

        return results;
    }

    protected Hashtable<Object, Object> getCachedItemValuesHash(String elementName, String security_permission, boolean preLoad, String idField, String valueField) {
        Hashtable<Object, Object> hash = new Hashtable<Object, Object>();

        ArrayList<ItemI> items = getCachedItems(elementName, security_permission, preLoad);
        if (items.size() > 0) {
            for (ItemI i : items) {
                try {
                    Object id = i.getProperty(idField);
                    Object value = i.getProperty(valueField);
                    if (value == null) {
                        value = id;
                    }

                    hash.put(id, value);
                } catch (XFTInitException e) {
                    logger.error("", e);
                } catch (ElementNotFoundException e) {
                    logger.error("", e);
                } catch (FieldNotFoundException e) {
                    logger.error("", e);
                }
            }
        }

        return hash;
    }

    protected boolean containsGroup(String groupID) {
        for (XdatUserGroupid g : this.getGroups_groupid()) {
            if (g.getGroupid().equals(groupID)) {
                return true;
            }
        }
        return false;
    }


    protected boolean isOwner(String tag) {
        final UserGroupI ug = this.getGroup(tag + "_owner");
        if (ug != null) {
            return true;
        } else {
            return false;
        }
    }
    
    private Map<String,List<PermissionCriteriaI>> criteria;
    
    protected List<PermissionCriteriaI> getPermissionsByDataType( String type){
    	if(criteria==null){
    		criteria=Maps.newHashMap();
    	}
    	
    	if(criteria.get(type)==null){
	    	criteria.put(type, new ArrayList<PermissionCriteriaI>());
	    	
	    	ElementAccessManager eam3;
			try {
				eam3 = this.getAccessManager(type);
				
				if (eam3 != null) {
			        criteria.get(type).addAll(eam3.getCriteria());
		        }
	
			} catch (Exception e) {
				logger.error("",e);
			}

	        for (UserGroupI group : Groups.getGroupsForUser(this).values()) {
	            criteria.get(type).addAll(((UserGroup)group).getPermissionsByDataType(type));
	        }
	        	        
	        if(!this.isGuest()){
		        try {
					UserI guest=Users.getGuest();
					criteria.get(type).addAll(((XDATUser)guest).getPermissionsByDataType(type));
				} catch (Exception e) {
					logger.error("",e);
				}	
	        }
	        
    	}
        return criteria.get(type);
    }   
    

    /******************************************
    /* Copied from XDATUserDetails
    */
    public UserAuthI getAuthorization() {
        return _authorization;
    }

    public UserAuthI setAuthorization(UserAuthI auth) {
        return _authorization = auth;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        if (_authorities == null) {
            _authorities = new ArrayList<GrantedAuthority>();
            _authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
            if (isSiteAdmin()) {
                _authorities.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
            }
            List<String> groups = Groups.getGroupIdsForUser(this);
            if (groups != null && groups.size() > 0) {
                for (String group : groups) {
                    _authorities.add(new GrantedAuthorityImpl(group));
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Created granted authorities list for user " + getLogin() + ": " + Joiner.on(", ").join(_authorities));
            }
        }
        return _authorities;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    //necessary for spring security session management
	public int hashCode() {
        return new HashCodeBuilder(691, 431).append(getUsername()).toHashCode();
    }

	public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        UserI rhs = (UserI) obj;
        return new EqualsBuilder().append(getUsername(), rhs.getUsername()).isEquals();
    }

	public void resetCriteria() {
		criteria=null;
	}

	public Collection<String> getFeaturesForUserByTag(String tag) {
		return Features.getFeaturesForGroup(getGroupByTag(tag));
	}

	public Collection<String> getFeaturesForUserByTags(Collection<String> tags) {
		List<String> combined=Lists.newArrayList();
		for(String tag: tags){
			for(String feature: getFeaturesForUserByTag(tag)){
				if(!combined.contains(feature)){
					combined.add(feature);
				}
			}
		}
		return combined;
	}

	public boolean checkSiteRole(String role) {
		try {
			return this.checkRole(role);
		} catch (Exception e) {
			logger.error("",e);
			return false;
		}
	}

	/**
	 * Returns true if the user is a part of a group with the matching tag and feature
	 * @param tag
	 * @param feature
	 * @return
	 */
	public boolean checkFeature(String tag, String feature) {
		return Features.checkFeature(getGroupByTag(tag), feature);
	}

	/**
	 * Returns true if the user is part of any groups with the matching tag and feature
	 * @param tags
	 * @param feature
	 * @return
	 */
	public boolean checkFeature(Collection<String> tags, String feature) {
        if (Features.isBanned(feature)) {
            return false;
        }

        if (this.isSiteAdmin()) {
            return true;
        }

		for(String tag: tags){			
			if(tag instanceof String && checkFeature(tag,feature)){
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Returns true if the user is part of any groups with the matching tag and feature
	 * @param item       The item to check.
	 * @param feature    The feature to check.
	 * @return
	 */
	public boolean checkFeature(BaseElement item, String feature) {
		return checkFeature(item.getSecurityTags().getHash().values(),feature);
	}
	
	public boolean checkFeatureForAnyTag(String feature){
		return Features.checkFeatureForAnyTag(this, feature);
	}

    private List<String> _editableProjects;

    protected boolean hasAccessTo(final String projectId) throws Exception {
        return Roles.isSiteAdmin(this) || getAccessibleProjects().contains(projectId);
    }

    /**
     * Code copied here from
     * @return All the projects where this user has edit permissions.
     * @throws Exception
     */
    protected List<String> getAccessibleProjects() throws Exception {
        if (_editableProjects == null) {
            _editableProjects = new ArrayList<String>();
            for (final List<String> row : getQueryResults("xnat:projectData/ID", "xnat:projectData")) {
                final String id = row.get(0);
                if (_editableProjects.contains(id))
                    continue;
                try {
                    if (canModify(id)) {
                        _editableProjects.add(id);
                    }
                } catch (Exception e) {
                    logger.error("Exception caught testing prearchive access", e);
                }
            }
            // if the user is an admin also add unassigned projects
            if (Roles.isSiteAdmin(this)) {
                _editableProjects.add(null);
            }
        }
        return _editableProjects;
    }

    private boolean canModify(final String projectId) throws Exception{
        if (Roles.isSiteAdmin(this)) {
            return true;
        }
        if (projectId == null) {
            return false;
        }
        final List<ElementSecurity> secureElements = ElementSecurity.GetSecureElements();
        for (ElementSecurity secureElement : secureElements) {
            if (secureElement.getSchemaElement().instanceOf("xnat:imageSessionData")) {
                if (Permissions.can(this, secureElement.getElementName() + "/project", projectId, SecurityManager.EDIT)) {
                    return true;
                }
            }
        }
        return false;
    }
}

