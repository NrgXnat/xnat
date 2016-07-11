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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xdat.entities.UserRole;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.search.QueryOrganizer;
import org.nrg.xdat.security.helpers.*;
import org.nrg.xdat.security.user.exceptions.*;
import org.nrg.xdat.services.UserRoleService;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.*;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.XftStringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;

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
    private final Set<String> roleNames = new HashSet<>();
    private boolean rolesNotUpdatedFromService = true;
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

    public XDATUser(String login) throws UserNotFoundException, UserInitException {
        super((UserI) null);
        try {
			SchemaElementI e = SchemaElement.GetElement(USER_ELEMENT);

			ItemSearch search = new ItemSearch(null, e.getGenericXFTElement());
			search.addCriteria(USER_ELEMENT + XFT.PATH_SEPARATOR + "login", login);
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
        } catch (UserNotFoundException e) {
            throw e;
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

    public XDATUser(String login, String password) throws Exception {
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
        if (!isEnabled()) {
            throw new EnabledException(this.getUsername());
        }

        if ((!this.isActive()) && (!this.checkRole("Administrator"))) {
            throw new ActivationException(this.getUsername());
        }

        String pass = getStringProperty("primary_password");
        String salt = getStringProperty("salt");

        if (StringUtils.isBlank(pass)) throw new PasswordAuthenticationException(getUsername());

        // encryption
        if (new ShaPasswordEncoder(256).isPasswordValid(pass, password, salt)) {
            return true;
        }
        throw new PasswordAuthenticationException(getUsername());
    }

    private ElementAccessManager getAccessManager(String s) throws Exception {
        return this.getAccessManagers().get(s);
    }

    public synchronized void init() throws Exception {
        final long startTime = Calendar.getInstance().getTimeInMillis();
        accessManagers = new Hashtable<>();

        for (final Object o : this.getChildItems(USER_ELEMENT + ".element_access")) {
            final ItemI sub = (ItemI) o;
            final ElementAccessManager eam = new ElementAccessManager(sub);
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
     * Gets the user's username. This is the same as the login name.
     * @return The username.
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
     * Gets the user's first name.
     * @return User's first name.
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
     * Gets the user's last name.
     * @return User's last name.
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
     * Gets the user's email.
     * @return The user's email.
     */
    public String getEmail() {
        try {
            return super.getEmail();
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    /**
     * Gets the user's system-unique ID. This is usually something like the primary key in the database or something
     * similar.
     * @return User's system-unique ID.
     */
    public Integer getID() {
        try {
            return super.getIntegerProperty("xdat_user_id");
        } catch (FieldNotFoundException | ElementNotFoundException e) {
            logger.error("", e);
        }
        return null;
    }

    /**
     * Indicates whether the user object represents a guest account.
     * @return <b>true</b> if the user is a guest, <b>false</b> otherwise.
     */
    public boolean isGuest() {
        final String login = getLogin();
        return StringUtils.isBlank(login) || login.equalsIgnoreCase("guest");
    }

    /**
     * Gets the user's password. For existing users, this will be the encrypted or hashed value stored in the database
     * and won't represent the password as actually submitted by the user.
     * @return User's password.
     */
    public String getPassword(){
    	return getPrimaryPassword();
    }

    /**
     * Gets the date that the user's information was last modified.
     * @return The date on which the user's information was last modified.
     */
	@Override
	public Date getLastModified() {
		return this.getItem().getLastModified();
	}

    /**
     * Sets the user's password to the indicated value. You should only set securely encrypted or hashed values for the
     * user's password.
     * @param encodePassword    The encoded password.
     */
	@Override
	public void setPassword(String encodePassword) {
		this.setPrimaryPassword(encodePassword);
	}
    /**
     * List of {@link ElementDisplay element displays} that this user can create.
     *
     * @return A list of all {@link ElementDisplay element displays} that this user can create.
     * @throws Exception
     */
	protected List<ElementDisplay> getCreateableElementDisplays() throws Exception {
        return getActionElementDisplays(SecurityManager.CREATE);
    }

    /**
     * List of {@link ElementDisplay element displays} that this user can edit.
     *
     * @return A list of all {@link ElementDisplay element displays} that this user can edit.
     * @throws Exception
     */
    protected List<ElementDisplay> getEditableElementDisplays() throws ElementNotFoundException, XFTInitException, Exception {
        return getActionElementDisplays(SecurityManager.EDIT);
    }

    /**
     * List of {@link ElementDisplay element displays} that this user can read.
     *
     * @return A list of all {@link ElementDisplay element displays} that this user can read.
     * @throws Exception
     */
    protected List<ElementDisplay> getReadableElementDisplays() throws ElementNotFoundException, XFTInitException, Exception {
        return getActionElementDisplays(SecurityManager.READ);
    }

    /**
     * List of available {@link ElementAccessManager access managers} for this system.
     *
     * @return List of available {@link ElementAccessManager access managers} for this system.
     * @throws Exception
     */
    protected Hashtable<String, ElementAccessManager> getAccessManagers() throws Exception {
        if (this.accessManagers == null) {
            this.init();
        }
        return this.accessManagers;
    }

    /**
     * Gets the indicated {@link DisplaySearch display search}.
     * @param elementName    The element name to be searched on.
     * @param display        The display.
     * @return The indicated {@link DisplaySearch display search} if it exists.
     * @throws Exception
     */
    protected DisplaySearch getSearch(String elementName, String display) throws Exception {
        DisplaySearch search = new DisplaySearch();
        search.setUser(this);
        search.setDisplay(display);
        search.setRootElement(elementName);
        return search;
    }

    /**
     * Gets a list of unsecured {@link ElementDisplay display elements} for this system.
     *
     * @return A list of unsecured {@link ElementDisplay display elements} for this system.
     * @throws Exception
     */
    protected List<ElementDisplay> getUnSecuredElements() throws XFTInitException, ElementNotFoundException, Exception {
        final List<ElementDisplay> al = new ArrayList<>();

        for (ElementSecurity es : ElementSecurity.GetInSecureElements()) {
            if (es.getSchemaElement().hasDisplay()) {
                al.add(es.getSchemaElement().getDisplay());
            }
        }
        return al;
    }

    /**
     * Indicates whether the user has extended attributes.
     * @return <b>true</b> if the user has extended attributes, <b>false</b> otherwise.
     */
    public boolean isExtended() {
        return extended;
    }

    /**
     * Sets whether the user has extended attributes.
     * @param extended    Sets whether the user has extended attributes.
     */
    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    /**
     * Removes the user from the specified role.  The authenticated user must be a site admin.  This operation removes
     * the role from the old XFT structure and the new Hibernate structure.
     * @param authenticatedUser    The user requesting the role deletion.
     * @param dRole                The role to be deleted.
     * @throws Exception
     */
    public void deleteRole(UserI authenticatedUser, String dRole) throws Exception{
		if(!((XDATUser)authenticatedUser).isSiteAdmin()){
			throw new Exception("Invalid permissions for user modification.");
		}

    	for (ItemI role: (List<ItemI>)this.getChildItems(org.nrg.xft.XFT.PREFIX + ":user.assigned_roles.assigned_role")) {
    		if(StringUtils.equals(role.getStringProperty("role_name"),dRole)){
    			PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, authenticatedUser, "xdat:user",this.getStringProperty("xdat_user_id"),PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Removed " + dRole + " role"));
    			EventMetaI ci=wrk.buildEvent();
    			SaveItemHelper.unauthorizedRemoveChild(this.getItem(), "xdat:user/assigned_roles/assigned_role", role.getItem(), authenticatedUser,ci);
    			PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
    		}
        }

    	List<UserRole> roles =XDAT.getContextService().getBean(UserRoleService.class).findRolesForUser(this.getLogin());
        if(roles!=null){
        	for(final UserRole ur: roles){
        		if(StringUtils.equals(ur.getRole(), dRole)){
        			XDAT.getContextService().getBean(UserRoleService.class).delete(ur);
        			PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, authenticatedUser, "xdat:user",this.getStringProperty("xdat_user_id"),PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Removed " + dRole + " role"));
        			PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
        		}
        	}
        }
    }

    /**
     * Add the specified role to the user.  The authenticated user must be a site admin.  This operation add the role
     * to the old XFT structure and the new Hibernate structure.
     * @param authenticatedUser    The user requesting the role addition.
     * @param dRole                The role to be added.
     * @throws Exception
     */
    public void addRole(UserI authenticatedUser, String dRole) throws Exception{
    	if(StringUtils.isNotBlank(dRole)){
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

    private Set<String> loadRoleNames() throws Exception {
    	final Set<String> r= Sets.newHashSet();

    	//load from the old role store
        for (ItemI sub: (List<ItemI>)this.getChildItems(org.nrg.xft.XFT.PREFIX + ":user.assigned_roles.assigned_role")) {
            r.add(sub.getStringProperty("role_name"));
        }

        try {
			//load from the new role store
        	//TODO: Fix it so that this is required in tomcat mode, but optional in command line mode.
                try {
                    final UserRoleService roleService=XDAT.getContextService().getBean(UserRoleService.class);
                    if(roleService!=null){
                        List<UserRole> roles =roleService.findRolesForUser(this.getLogin());
                        if(roles!=null){
                            for(final UserRole ur: roles){
                                r.add(ur.getRole());
                            }
                        }
                        rolesNotUpdatedFromService = false;
                    }else{
                        logger.error("skipping user role service review... service is null");
                    }
                } catch (NoSuchBeanDefinitionException e) {
                    logger.warn("Unable to update roles for user " + getUsername() + " due to not finding the user role service. Will mark as incomplete.");
                }
        } catch (Throwable e) {
			logger.error("",e);
		}

        return r;
    }

    /**
     * Get the names of the available system roles.
     * @return A list of the available system roles.
     */
    public List<String> getRoleNames() {
        if (roleNames.size() == 0 || rolesNotUpdatedFromService) {
            try {
				roleNames.addAll(loadRoleNames());
			} catch (Exception e) {
				logger.error("",e);
			}
        }
        return ImmutableList.copyOf(roleNames);
    }

    /**
     * Checks whether the user has the indicated role.
     * @param role    The role to check.
     * @return <b>true</b> if the user has the indicated role, <b>false</b> otherwise.
     * @throws Exception
     */
    public boolean checkRole(String role) throws Exception {
        return getRoleNames().contains(role);
    }

    public void clearBrowseableElementDisplays() {
        browseable = null;
        readable_counts = null;
    }

    protected ArrayList<ElementDisplay> getBrowseableElementDisplays() {
        if (browseable == null) {
            browseable = new ArrayList<>();

            Map counts = this.getReadableCounts();
            try {
                for (final ElementDisplay ed : getReadableElementDisplays()) {
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
        ArrayList<ElementDisplay> elementDisplays = new ArrayList<>();
        try {
            for (final ElementDisplay ed : getCreateableElementDisplays()) {
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
            searchable = new ArrayList<>();
            Map counts = this.getReadableCounts();
            try {
                for (final ElementDisplay ed : getReadableElementDisplays()) {
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
     * Gets a list of available stored searches.
     * @return A list of the available stored searches.
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
            ItemI old = getStoredSearch(i.getStringProperty("ID"));
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

    /**
     * Gets a particular stored search item by ID.
     * @param id    The ID of the stored search to retrieve.
     * @return The {@link XdatStoredSearch requested stored search} if it exists, null otherwise.
     */
    protected XdatStoredSearch getStoredSearch(String id) {
        List<XdatStoredSearch> temp = getStoredSearches();
        try {
            for (XdatStoredSearch search : temp) {
                if (id.equalsIgnoreCase(search.getStringProperty("xdat:stored_search.ID"))) {
                    return search;
                }
            }
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }


    protected void initGroups() {
        synchronized (groups) {
            groups.clear();
            getGroups();
        }
    }

    public Map<String, UserGroupI> getGroups() {
        synchronized (groups) {
            if (groups.size() == 0) {
                try {
                    XFTTable t = XFTTable.Execute("SELECT * FROM xdat_user_groupid WHERE groups_groupid_xdat_user_xdat_user_id=" + this.getXdatUserId(), this.getDBName(), this.getLogin());

                    List<String> groupids = t.convertColumnToArrayList("groupid");

                    for (final String groupId : groupids) {
                        UserGroupI group = Groups.getGroup(groupId);
                        if (group != null) {
                            groups.put(groupId, group);
                            if (group.getTag() != null) {
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


    /**
     * Returns an ArrayList of ArrayLists
     * xmlPaths: comma-delimited list of xmlPaths to return
     *
     * @param xmlPaths       The XML paths to be returned in the results.
     * @param rootElement    The root element from which to start.
     * @return A list of query results (each set of results is itself a list).
     */
    protected List<List> getQueryResults(String xmlPaths, String rootElement) {
        ArrayList results = new ArrayList();
        try {

            QueryOrganizer qo = new QueryOrganizer(rootElement, this, ViewManager.ALL);

            ArrayList fields = XftStringUtils.CommaDelimitedStringToArrayList(xmlPaths);
            for (Object field : fields) {
                qo.addField((String) field);
            }

            String query = qo.buildQuery();

            XFTTable t = getQueryResults(query);

            ArrayList<Integer> colHeaders = new ArrayList<>();
            for (Object field : fields) {
                String header = qo.translateXMLPath((String) field);
                Integer index = t.getColumnIndex(header.toLowerCase());
                colHeaders.add(index);
            }

            t.resetRowCursor();

            while (t.hasMoreRows()) {
                Object[] row = t.nextRow();

                ArrayList newRow = new ArrayList();

                for (Integer index : colHeaders) {
                    newRow.add(row[index]);
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

    private Hashtable<String, ArrayList<ItemI>> userSessionCache = new Hashtable<>();

    protected ArrayList<ItemI> getCachedItems(String elementName, String security_permission, boolean preLoad) {
        if (!userSessionCache.containsKey(elementName + security_permission + preLoad)) {
            ItemSearch search = new ItemSearch();
            ArrayList<ItemI> items = new ArrayList<>();
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
        userSessionCache = new Hashtable<>();
        total_counts = null;
        readable_counts = null;
        criteria=null;
        _editableProjects = null;
    }

    Map readable_counts = null;

    protected Map getReadableCounts() {
        if (readable_counts == null) {
            try {
                readable_counts = new Hashtable<>();

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
                    query = StringUtils.replace(query, idField, "id");
                    query = StringUtils.replace(query, "xnat_subjectData", "xnat_experimentData");
                    query = StringUtils.replace(query, "xnat_projectParticipant", "xnat_experimentData_share");
                    query = StringUtils.replace(query, "subject_id", "sharing_share_xnat_experimentda_id");

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
                total_counts = new Hashtable<>();

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
        ArrayList<ItemI> results = new ArrayList<>();
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
        Hashtable<Object, Object> hash = new Hashtable<>();

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


    protected boolean isOwner(String tag) {
        final UserGroupI ug = this.getGroup(tag + "_owner");
        return ug != null;
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

			try{
				PoolDBUtils.CheckSpecialSQLChars(type);
			}catch (Exception e){
				return null;
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
            _authorities = new ArrayList<>();
            _authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            if (isSiteAdmin()) {
                _authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
            List<String> groups = Groups.getGroupIdsForUser(this);
            if (groups != null && groups.size() > 0) {
                for (String group : groups) {
                    _authorities.add(new SimpleGrantedAuthority(group));
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

    private boolean checkFeatureBySiteRoles(String feature){
        try {
			for(String role: getRoleNames()){
				if(Features.isOnByDefaultBySiteRole(feature, role)){
					return true;
				}
			}
		} catch (Exception e) {
			logger.error("",e);
		}

        return false;
	}

	/**
	 * Returns true if the user is a part of a group with the matching tag and feature.
	 * @param tag        The tag for the feature to match.
	 * @param feature    The feature name.
	 * @return <b>true</b> if the user is a member of a group with the matching tag and feature, <b>false</b> otherwise.
	 */
	public boolean checkFeature(String tag, String feature) {
        return !Features.isBanned(feature) && (checkFeatureBySiteRoles(feature) || Features.checkFeature(getGroupByTag(tag), feature));

    }

	/**
	 * Returns true if the user is part of any groups with the matching tags and feature.
     * @param tags       The tags for the feature to match.
     * @param feature    The feature name.
     * @return <b>true</b> if the user is a member of a group with the matching tag and feature, <b>false</b> otherwise.
	 */
	public boolean checkFeature(Collection<String> tags, String feature) {
        if (Features.isBanned(feature)) {
            return false;
        }

        if(checkFeatureBySiteRoles(feature)){
        	return true;
        }

		for(final String tag: tags){
			if(tag != null && checkFeature(tag,feature)){
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if the user is part of any groups with the matching tag and feature
	 * @param item       The item to check.
	 * @param feature    The feature to check.
     * @return <b>true</b> if the user is a member of a group with the matching tag and feature, <b>false</b> otherwise.
	 */
	public boolean checkFeature(BaseElement item, String feature) {
		return checkFeature(item.getSecurityTags().getHash().values(),feature);
	}

    /**
     * Checks whether the indicated feature has any associated tags.
     * @param feature    The feature to check.
     * @return <b>true</b> if the feature has any tags, <b>false</b> otherwise.
     */
	public boolean checkFeatureForAnyTag(String feature) {
        return !Features.isBanned(feature) && (checkFeatureBySiteRoles(feature) || Features.checkFeatureForAnyTag(this, feature));

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
            _editableProjects = new ArrayList<>();
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

    /**
     * List of {@link ElementDisplay element displays} that this user can invoke.
     *
     * @return A list of all {@link ElementDisplay element displays} that this user can invoke.
     * @throws Exception
     */
    private List<ElementDisplay> getActionElementDisplays(final String action) throws ElementNotFoundException, XFTInitException, Exception {
        final Map<String, ElementDisplay> hash = new Hashtable<>();

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

        final ArrayList<ElementDisplay> al = new ArrayList<>();
        al.addAll(hash.values());
        al.trimToSize();

        Collections.sort(al, ElementDisplay.SequenceComparator);
        return al;
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
}


