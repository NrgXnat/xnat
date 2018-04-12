/*
 * core: org.nrg.xdat.security.XDATUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;

import static org.nrg.xdat.security.PermissionCriteria.dumpCriteriaList;
import static org.nrg.xdat.security.SecurityManager.*;

/**
 * @author Tim
 */
@SuppressWarnings({"unchecked"})
@Slf4j
public class XDATUser extends XdatUser implements UserI, Serializable {
    /**
     * DO NOT USE THIS.  IT is primarily for use in unit testings.  Use XDATUser(login).
     */
    public XDATUser() {
    }

    public XDATUser(String login) throws UserNotFoundException, UserInitException {
        super((UserI) null);
        try {
            SchemaElementI e = SchemaElement.GetElement(SCHEMA_ELEMENT_NAME);

            ItemSearch search = new ItemSearch(null, e.getGenericXFTElement());
            search.addCriteria(SCHEMA_ELEMENT_NAME + XFT.PATH_SEPARATOR + "login", login);
            search.setLevel(ViewManager.ACTIVE);
            ArrayList found = search.exec(true).items();

            if (found.size() == 0) {
                throw new UserNotFoundException(login);
            } else {
                setItem((ItemI) found.get(0));
            }

            if (!isExtended()) {
                init();
                if (!this.getItem().isPreLoaded()) {
                    extend(true);
                }
                setExtended(true);
            }
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UserInitException(e.getMessage());
        }
        if (XFT.VERBOSE) {
            System.out.println("User(login) Loaded (" + (Calendar.getInstance().getTimeInMillis() - startTime) + ")ms");
        }
    }

    public XDATUser(ItemI i) throws UserInitException {
        this(i, true);
    }

    public XDATUser(ItemI i, boolean extend) throws UserInitException {
        super((UserI) null);
        setItem(i);
        if (extend) {
            if (!isExtended()) {
                try {
                    init();
                    if (!this.getItem().isPreLoaded()) {
                        extend(true);
                    }
                    setExtended(true);
                } catch (Exception e) {
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
                if (!this.getItem().isPreLoaded()) {
                    extend(true);
                }
                setExtended(true);
            }
        }
        if (XFT.VERBOSE) {
            System.out.println("User(login,password) Loaded (" + (Calendar.getInstance().getTimeInMillis() - startTime) + ")ms");
        }
    }

    public boolean login(final String submitted) throws PasswordAuthenticationException, Exception {
        if (!isEnabled()) {
            throw new EnabledException(getUsername());
        }

        if ((!isActive()) && (!checkRole("Administrator"))) {
            throw new ActivationException(getUsername());
        }

        final String password = getStringProperty("primary_password");
        if (StringUtils.isBlank(password)) {
            throw new PasswordAuthenticationException(getUsername());
        }

        final String salt = getStringProperty("salt");

        // encryption
        if (Users.isPasswordValid(password, submitted, salt)) {
            return true;
        }
        throw new PasswordAuthenticationException(getUsername());
    }

    private ElementAccessManager getAccessManager(String s) throws Exception {
        return getAccessManagers().get(s);
    }

    public synchronized void init() throws Exception {
        init(null);
    }

    public synchronized void init(final NamedParameterJdbcTemplate template) throws Exception {
        final long startTime = Calendar.getInstance().getTimeInMillis();
        _accessManagers.clear();

        if (template == null) {
            for (final Object o : this.getChildItems(SCHEMA_ELEMENT_NAME + ".element_access")) {
                final ItemI                sub = (ItemI) o;
                final ElementAccessManager eam = new ElementAccessManager(sub);
                _accessManagers.put(eam.getElement(), eam);
            }
        } else {
            _accessManagers.putAll(ElementAccessManager.initialize(template, QUERY_USER_PERMISSIONS, new MapSqlParameterSource("userId", getXdatUserId())));
        }

        _storedSearches.clear();
        clearLocalCache();

        if (XFT.VERBOSE) {
            System.out.println("User Init(" + this.getUsername() + "): " + (Calendar.getInstance().getTimeInMillis() - startTime) + "ms");
        }
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
     *
     * @return The username.
     */
    public String getUsername() {
        try {
            return (String) getProperty("login");
        } catch (ElementNotFoundException e) {
            log.error("Element '{}' not found", e.ELEMENT, e);
            return null;
        } catch (FieldNotFoundException e) {
            log.error("Field '{}' not found", e.FIELD, e);
            return null;
        }
    }

    /**
     * Gets the user's first name.
     *
     * @return User's first name.
     */
    public String getFirstname() {
        try {
            return super.getFirstname();
        } catch (Exception e) {
            log.error("An unknown error occurred", e);
            return null;
        }
    }

    /**
     * Gets the user's last name.
     *
     * @return User's last name.
     */
    public String getLastname() {
        try {
            return super.getLastname();
        } catch (Exception e) {
            log.error("An unknown error occurred", e);
            return null;
        }
    }

    /**
     * Gets the user's email.
     *
     * @return The user's email.
     */
    public String getEmail() {
        try {
            return super.getEmail();
        } catch (Exception e) {
            log.error("An unknown error occurred", e);
            return null;
        }
    }

    /**
     * Gets the user's system-unique ID. This is usually something like the primary key in the database or something
     * similar.
     *
     * @return User's system-unique ID.
     */
    public Integer getID() {
        try {
            return super.getIntegerProperty("xdat_user_id");
        } catch (ElementNotFoundException e) {
            log.error("Element '{}' not found", e.ELEMENT, e);
        } catch (FieldNotFoundException e) {
            log.error("Field '{}' not found", e.FIELD, e);
        }
        return null;
    }

    /**
     * Indicates whether the user object represents a guest account.
     *
     * @return <b>true</b> if the user is a guest, <b>false</b> otherwise.
     */
    public boolean isGuest() {
        return getAuthorities().contains(AUTHORITY_ANONYMOUS);
    }

    /**
     * Gets the user's password. For existing users, this will be the encrypted or hashed value stored in the database
     * and won't represent the password as actually submitted by the user.
     *
     * @return User's password.
     */
    public String getPassword() {
        return getPrimaryPassword();
    }

    /**
     * Gets the date that the user's information was last modified.
     *
     * @return The date on which the user's information was last modified.
     */
    @Override
    public Date getLastModified() {
        return this.getItem().getLastModified();
    }

    /**
     * Sets the user's password to the indicated value. You should only set securely encrypted or hashed values for the
     * user's password.
     *
     * @param encodePassword The encoded password.
     */
    @Override
    public void setPassword(String encodePassword) {
        this.setPrimaryPassword(encodePassword);
    }

    /**
     * List of {@link ElementDisplay element displays} that this user can create.
     *
     * @return A list of all {@link ElementDisplay element displays} that this user can create.
     *
     * @throws Exception When an error occurs.
     */
    protected List<ElementDisplay> getCreateableElementDisplays() throws Exception {
        return getCache().getActionElementDisplays(this, CREATE);
    }

    /**
     * List of {@link ElementDisplay element displays} that this user can edit.
     *
     * @return A list of all {@link ElementDisplay element displays} that this user can edit.
     *
     * @throws Exception When an error occurs.
     */
    protected List<ElementDisplay> getEditableElementDisplays() throws ElementNotFoundException, XFTInitException, Exception {
        return getCache().getActionElementDisplays(this, EDIT);
    }

    /**
     * List of {@link ElementDisplay element displays} that this user can read.
     *
     * @return A list of all {@link ElementDisplay element displays} that this user can read.
     *
     * @throws Exception When an error occurs.
     */
    protected List<ElementDisplay> getReadableElementDisplays() throws ElementNotFoundException, XFTInitException, Exception {
        return getCache().getActionElementDisplays(this, READ);
    }

    /**
     * List of available {@link ElementAccessManager access managers} for this system.
     *
     * @return List of available {@link ElementAccessManager access managers} for this system.
     *
     * @throws Exception When an error occurs.
     */
    protected Map<String, ElementAccessManager> getAccessManagers() throws Exception {
        if (_accessManagers.isEmpty()) {
            init();
        }
        return _accessManagers;
    }

    /**
     * Gets the indicated {@link DisplaySearch display search}.
     *
     * @param elementName The element name to be searched on.
     * @param display     The display.
     *
     * @return The indicated {@link DisplaySearch display search} if it exists.
     *
     * @throws Exception When an error occurs.
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
     *
     * @throws Exception When an error occurs.
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
     *
     * @return <b>true</b> if the user has extended attributes, <b>false</b> otherwise.
     */
    public boolean isExtended() {
        return extended;
    }

    /**
     * Sets whether the user has extended attributes.
     *
     * @param extended Sets whether the user has extended attributes.
     */
    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    /**
     * Removes the user from the specified role.  The authenticated user must be a site admin.  This operation removes
     * the role from the old XFT structure and the new Hibernate structure.
     *
     * @param authenticatedUser The user requesting the role deletion.
     * @param dRole             The role to be deleted.
     *
     * @throws Exception When an error occurs.
     */
    public void deleteRole(UserI authenticatedUser, String dRole) throws Exception {
        if (!((XDATUser) authenticatedUser).isSiteAdmin()) {
            throw new Exception("Invalid permissions for user modification.");
        }

        for (ItemI role : (List<ItemI>) this.getChildItems(org.nrg.xft.XFT.PREFIX + ":user.assigned_roles.assigned_role")) {
            if (StringUtils.equals(role.getStringProperty("role_name"), dRole)) {
                PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, authenticatedUser, "xdat:user", this.getStringProperty("xdat_user_id"), PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Removed " + dRole + " role"));
                EventMetaI          ci  = wrk.buildEvent();
                SaveItemHelper.unauthorizedRemoveChild(this.getItem(), "xdat:user/assigned_roles/assigned_role", role.getItem(), authenticatedUser, ci);
                PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
            }
        }

        List<UserRole> roles = XDAT.getContextService().getBean(UserRoleService.class).findRolesForUser(this.getLogin());
        if (roles != null) {
            for (final UserRole ur : roles) {
                if (StringUtils.equals(ur.getRole(), dRole)) {
                    XDAT.getContextService().getBean(UserRoleService.class).delete(ur);
                    PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, authenticatedUser, "xdat:user", this.getStringProperty("xdat_user_id"), PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Removed " + dRole + " role"));
                    PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
                }
            }
        }
    }

    /**
     * Add the specified role to the user.  The authenticated user must be a site admin.  This operation add the role
     * to the old XFT structure and the new Hibernate structure.
     *
     * @param authenticatedUser The user requesting the role addition.
     * @param dRole             The role to be added.
     *
     * @throws Exception When an error occurs.
     */
    public void addRole(UserI authenticatedUser, String dRole) throws Exception {
        if (StringUtils.isNotBlank(dRole)) {
            if (XDAT.getContextService().getBean(UserRoleService.class).findUserRole(this.getLogin(), dRole) == null) {
                if (!((XDATUser) authenticatedUser).isSiteAdmin()) {
                    throw new Exception("Invalid permissions for user modification.");
                }

                XDAT.getContextService().getBean(UserRoleService.class).addRoleToUser(this.getLogin(), dRole);

                PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, authenticatedUser, "xdat:user", this.getStringProperty("xdat_user_id"), PersistentWorkflowUtils.ADMIN_EXTERNAL_ID, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Added " + dRole + " role"));
                PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
            }
        }
    }

    private Set<String> loadRoleNames() {
        final Set<String> r = Sets.newHashSet();

        try {
            //load from the old role store
            for (ItemI sub : (List<ItemI>) this.getChildItems(org.nrg.xft.XFT.PREFIX + ":user.assigned_roles.assigned_role")) {
                r.add(sub.getStringProperty("role_name"));
            }
        } catch (Throwable e) {
            log.info("Error loading roles from old role store. Will use role service instead.");
        }

        try {
            //load from the new role store
            //TODO: Fix it so that this is required in tomcat mode, but optional in command line mode.
            try {
                final UserRoleService roleService = XDAT.getContextService().getBean(UserRoleService.class);
                if (roleService != null) {
                    List<UserRole> roles = roleService.findRolesForUser(this.getLogin());
                    if (roles != null) {
                        for (final UserRole ur : roles) {
                            r.add(ur.getRole());
                        }
                    }
                    rolesNotUpdatedFromService = false;
                } else {
                    log.error("skipping user role service review... service is null");
                }
            } catch (NoSuchBeanDefinitionException e) {
                log.warn("Unable to update roles for user " + getUsername() + " due to not finding the user role service. Will mark as incomplete.");
            }
        } catch (Throwable e) {
            log.error("An unknown error occurred", e);
        }

        return r;
    }

    /**
     * Get the names of the available system roles.
     *
     * @return A list of the available system roles.
     */
    public List<String> getRoleNames() {
        if (_roleNames.size() == 0 || rolesNotUpdatedFromService) {
            try {
                _roleNames.addAll(loadRoleNames());
            } catch (Exception e) {
                log.error("An unknown error occurred", e);
            }
        }
        return ImmutableList.copyOf(_roleNames);
    }

    /**
     * Checks whether the user has the indicated role.
     *
     * @param role The role to check.
     *
     * @return <b>true</b> if the user has the indicated role, <b>false</b> otherwise.
     *
     * @throws Exception When an error occurs.
     */
    public boolean checkRole(String role) throws Exception {
        return getRoleNames().contains(role);
    }

    protected List<ElementDisplay> getBrowseableElementDisplays() {
        return new ArrayList<>(getBrowseableElementDisplayMap().values());
    }

    protected Map<String, ElementDisplay> getBrowseableElementDisplayMap() {
        return getCache().getBrowseableElementDisplays(this);
    }

    protected ElementDisplay getBrowseableElementDisplay(final String elementName) {
        return getBrowseableElementDisplayMap().get(elementName);
    }

    protected List<ElementDisplay> getBrowseableCreateableElementDisplays() {
        try {
            final List<ElementDisplay> elementDisplays = Lists.newArrayList(Iterables.filter(getCreateableElementDisplays(), new Predicate<ElementDisplay>() {
                @Override
                public boolean apply(@Nullable final ElementDisplay elementDisplay) {
                    try {
                        return elementDisplay != null && ElementSecurity.IsBrowseableElement(elementDisplay.getElementName());
                    } catch (Exception e) {
                        return false;
                    }
                }
            }));
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
                    return Integer.compare(sequence1, sequence2);
                }
            });
            return elementDisplays;
        } catch (ElementNotFoundException e) {
            log.error("Element '{}' not found", e.ELEMENT, e);
        } catch (XFTInitException e) {
            log.error("There was an error initializing or accessing XFT", e);
        } catch (Exception e) {
            log.error("An unknown error occurred", e);
        }
        return Collections.emptyList();
    }

    protected List<ElementDisplay> getSearchableElementDisplays() {
        return getSearchableElementDisplays(NameComparator);
    }

    protected List<ElementDisplay> getSearchableElementDisplaysByDesc() {
        return getSearchableElementDisplays(DescriptionComparator);
    }

    protected List<ElementDisplay> getSearchableElementDisplaysByPluralDesc() {
        return getSearchableElementDisplays(PluralDescriptionComparator);
    }

    protected List<ElementDisplay> getSearchableElementDisplays(Comparator comp) {
        if (_searchableElementDisplays.isEmpty()) {
            final Map<Object, Object> counts = getReadableCounts();
            try {
                for (final ElementDisplay ed : getReadableElementDisplays()) {
                    if (ElementSecurity.IsSearchable(ed.getElementName())) {
                        if (counts.containsKey(ed.getElementName()) && ((Long) counts.get(ed.getElementName()) > 0)) {
                            _searchableElementDisplays.add(ed);
                        }
                    }
                }
            } catch (ElementNotFoundException e) {
                log.error("Element '{}' not found", e.ELEMENT, e);
            } catch (XFTInitException e) {
                log.error("There was an error initializing or accessing XFT", e);
            } catch (Exception e) {
                log.error("An unknown error occurred", e);
            }
            Collections.sort(_searchableElementDisplays, comp);
        }

        return _searchableElementDisplays;
    }

    /**
     * Gets a list of available stored searches.
     *
     * @return A list of the available stored searches.
     */
    protected List<XdatStoredSearch> getStoredSearches() {
        if (_storedSearches.isEmpty()) {
            try {
                _storedSearches.addAll(XdatStoredSearch.GetPreLoadedSearchesByAllowedUser(getLogin()));
            } catch (Exception e) {
                log.error("An unknown error occurred", e);
            }
        }
        return _storedSearches;
    }

    protected void replacePreLoadedSearch(XdatStoredSearch i) {
        try {
            ItemI old = getStoredSearch(i.getStringProperty("ID"));
            if (old != null) {
                _storedSearches.remove(old);
            }
        } catch (ElementNotFoundException e) {
            log.error("Element '{}' not found", e.ELEMENT, e);
        } catch (FieldNotFoundException e) {
            log.error("Field '{}' not found", e.FIELD, e);
        }
        _storedSearches.add(i);
    }

    /**
     * Gets a particular stored search item by ID.
     *
     * @param id The ID of the stored search to retrieve.
     *
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
            log.error("Element '{}' not found", e.ELEMENT, e);
        } catch (FieldNotFoundException e) {
            log.error("Field '{}' not found", e.FIELD, e);
        } catch (Exception e) {
            log.error("An unknown error occurred", e);
        }
        return null;
    }

    /**
     * This delegates to the {@link GroupsAndPermissionsCache} implementation.
     *
     * @return The groups to which the user belongs.
     */
    public Map<String, UserGroupI> getGroups() {
        try {
            return getCache().getGroupsForUser(getUsername());
        } catch (UserNotFoundException e) {
            return Collections.emptyMap();
        }
    }

    protected Date getPreviousLogin() throws SQLException, Exception {
        String query = "SELECT login_date FROM xdat_user_login WHERE user_xdat_user_id=" + this.getXdatUserId() + " AND login_date < (SELECT MAX(login_date) FROM xdat_user_login WHERE user_xdat_user_id=" + this.getXdatUserId() + ") ORDER BY login_date DESC LIMIT 1";
        return (Date) PoolDBUtils.ReturnStatisticQuery(query, "login_date", this.getDBName(), this.getUsername());
    }

    protected void refreshGroup(final String id) {
        // getGroups() returns immutable list, so just call this to make sure groups field is initialized, then
        // reference that. This is OK to do internally.
        getGroups();
    }

    public boolean isSiteAdmin() {
        if (_isSiteAdmin == null) {
            try {
                _isSiteAdmin = checkRole("Administrator");
            } catch (Exception e) {
                return false;
            }
        }
        return _isSiteAdmin;
    }

    private UserGroupI getGroup(String id) {
        return getGroups().get(id);
    }

    public UserGroupI getGroupByTag(String tag) {
        return Groups.getGroupForUserAndTag(this, tag);
    }


    /**
     * Returns an ArrayList of ArrayLists
     * xmlPaths: comma-delimited list of xmlPaths to return
     *
     * @param xmlPaths    The XML paths to be returned in the results.
     * @param rootElement The root element from which to start.
     *
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
                String  header = qo.translateXMLPath((String) field);
                Integer index  = t.getColumnIndex(header.toLowerCase());
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

        } catch (Exception e) {
            log.error("An unknown error occurred", e);
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

    protected ArrayList<ItemI> getCachedItems(String elementName, String security_permission, boolean preLoad) {
        if (!_userSessionCache.containsKey(elementName + security_permission + preLoad)) {
            ItemSearch       search = new ItemSearch();
            ArrayList<ItemI> items  = new ArrayList<>();
            try {
                search.setElement(elementName);
                if (security_permission != null && security_permission.equals(READ)) {
                    search.setUser(this);
                }
                search.setAllowMultiples(preLoad);
                ArrayList<ItemI> al = search.exec().getItems();

                if (security_permission != null && !security_permission.equals(READ)) {
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

                _userSessionCache.put(elementName + security_permission + preLoad, items);
            } catch (ElementNotFoundException e) {
                log.error("Element '{}' not found", e.ELEMENT, e);
            } catch (IllegalAccessException e) {
                log.error("An error occurred trying to access an object", e);
            } catch (MetaDataException e) {
                log.error("An error occurred trying to read XFT metadata", e);
            } catch (Throwable e) {
                log.error("An unknown error occurred", e);
            }
        }

        return _userSessionCache.get(elementName + security_permission + preLoad);
    }

    protected void clearLocalCache() {
        _userSessionCache.clear();
        _totalCounts.clear();
        _permissionCriteria.clear();
        _editableProjects.clear();
        _isSiteAdmin = null;
    }

    protected Map<Object, Object> getReadableCounts() {
        return getCache().getReadableCounts(this);
    }

    protected Map<String, Long> getTotalCounts() {
        if (_totalCounts.isEmpty()) {
            try {
                final Long projectCount = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM xnat_projectData", "count", this.getDBName(), this.getUsername());
                _totalCounts.put("xnat:projectData", projectCount);

                final Long subjectCount = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM xnat_subjectData", "count", this.getDBName(), this.getUsername());
                _totalCounts.put("xnat:subjectData", subjectCount);

                final XFTTable table = XFTTable.Execute("SELECT element_name, COUNT(ID) FROM xnat_experimentData expt LEFT JOIN xdat_meta_element xme ON expt.extension=xme.xdat_meta_element_id GROUP BY element_name", this.getDBName(), this.getUsername());
                _totalCounts.putAll(table.convertToMap("element_name", "count", String.class, Long.class));
            } catch (SQLException e) {
                log.error("An error occurred running an SQL query: [{}] {}", e.getErrorCode(), e.getSQLState(), e);
            } catch (DBPoolException e) {
                log.error("An error occurred accessing the database", e);
            } catch (Exception e) {
                log.error("An unknown error occurred", e);
            }
        }

        return _totalCounts;
    }

    protected ArrayList<ItemI> getCachedItemsByFieldValue(String elementName, String security_permission, boolean preLoad, String field, Object value) {
        ArrayList<ItemI> items   = getCachedItems(elementName, security_permission, preLoad);
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
                    log.error("There was an error initializing or accessing XFT", e);
                } catch (ElementNotFoundException e) {
                    log.error("Element '{}' not found", e.ELEMENT, e);
                } catch (FieldNotFoundException e) {
                    log.error("Field '{}' not found", e.FIELD, e);
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
                    Object id    = i.getProperty(idField);
                    Object value = i.getProperty(valueField);
                    if (value == null) {
                        value = id;
                    }

                    hash.put(id, value);
                } catch (XFTInitException e) {
                    log.error("There was an error initializing or accessing XFT", e);
                } catch (ElementNotFoundException e) {
                    log.error("Element '{}' not found", e.ELEMENT, e);
                } catch (FieldNotFoundException e) {
                    log.error("Field '{}' not found", e.FIELD, e);
                }
            }
        }

        return hash;
    }


    protected boolean isOwner(String tag) {
        final UserGroupI ug = this.getGroup(tag + "_owner");
        return ug != null;
    }

    public List<PermissionCriteriaI> getPermissionsByDataType(final String type) {
        if (!_permissionCriteria.containsKey(type)) {
            try {
                final ElementAccessManager elementAccessManager = getAccessManager(type);

                if (elementAccessManager != null) {
                    final List<PermissionCriteriaI> criteria = elementAccessManager.getCriteria();
                    _permissionCriteria.get(type).addAll(criteria);
                    log.info("Found {} criteria from the element access manager for data type {}", criteria.size(), type);
                } else {
                    log.info("Couldn't find an element access manager for data type {}", type);
                }

                try {
                    PoolDBUtils.CheckSpecialSQLChars(type);
                } catch (Exception e) {
                    return null;
                }
            } catch (Exception e) {
                log.error("An unknown error occurred", e);
            }

            final Map<String, UserGroupI> userGroups = Groups.getGroupsForUser(this);
            final Set<String>             groupIds   = userGroups.keySet();
            if (log.isInfoEnabled()) {
                log.info("Found {} user groups for the user {}: {}", groupIds.size(), getUsername(), Joiner.on(", ").join(groupIds));
            }

            for (final String groupId : groupIds) {
                final UserGroupI group = userGroups.get(groupId);
                if (group != null) {
                    final List<PermissionCriteriaI> permissions = group.getPermissionsByDataType(type);
                    if (permissions != null) {
                        if (log.isInfoEnabled()) {
                            log.info("Searched for permission criteria for user {} on type {} in group {}: {}", getUsername(), type, groupId, dumpCriteriaList(permissions));
                        }
                        _permissionCriteria.get(type).addAll(permissions);
                    } else {
                        log.warn("Tried to retrieve permissions for data type {} for user {} in group {}, but this returned null.", type, getUsername(), groupId);
                    }
                } else {
                    log.warn("Tried to retrieve group {} for user {}, but this returned null.", groupId, getUsername());
                }
            }

            if (!isGuest()) {
                try {
                    final UserI                     guest       = Users.getGuest();
                    final List<PermissionCriteriaI> permissions = ((XDATUser) guest).getPermissionsByDataType(type);
                    if (permissions != null) {
                        if (log.isInfoEnabled()) {
                            log.info("Searched for permission criteria from guest for user {} on type {}: {}", getUsername(), type, dumpCriteriaList(permissions));
                        }
                        _permissionCriteria.get(type).addAll(permissions);
                    } else {
                        log.warn("Tried to retrieve permissions for data type {} for the guest user, but this returned null.", type);
                    }
                } catch (Exception e) {
                    log.error("An error occurred trying to retrieve the guest user", e);
                }
            }

            if (log.isInfoEnabled()) {
                log.info("Retrieved permission criteria for user {} on the data type {}: {}", getUsername(), type, dumpCriteriaList(_permissionCriteria.get(type)));
            }
        } else {
            if (log.isDebugEnabled()) {
                final Collection<PermissionCriteriaI> permissionCriteria = _permissionCriteria.get(type);
                log.debug("Found {} cached criteria for the type {} in user {}", permissionCriteria.size(), type, getUsername(), dumpCriteriaList(permissionCriteria));
            }
        }

        return ImmutableList.copyOf(_permissionCriteria.get(type));
    }


    /******************************************
     /* Copied from XDATUserDetails
     */
    public UserAuthI getAuthorization() {
        return _authorization;
    }

    public UserAuthI setAuthorization(final UserAuthI auth) {
        return _authorization = auth;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        if (_authorities.size() == 0) {
            final String username = getUsername();
            if (StringUtils.isBlank(username) || StringUtils.equalsIgnoreCase("guest", username)) {
                _authorities.add(AUTHORITY_ANONYMOUS);
            } else {
                if (isSiteAdmin()) {
                    _authorities.add(AUTHORITY_ADMIN);
                }
                _authorities.add(AUTHORITY_USER);
            }
            final List<String> groups = Groups.getGroupIdsForUser(this);
            if (groups != null && groups.size() > 0) {
                for (String group : groups) {
                    if (group != null) {
                        _authorities.add(new SimpleGrantedAuthority(group));
                    }
                }
            }
            log.debug("Created granted authorities list for user {}: {}", getUsername(), Joiner.on(", ").join(_authorities));
        }
        return ImmutableSet.copyOf(_authorities);
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return _authorization != null && _authorization.getLockoutTime() == null;
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
        _permissionCriteria.clear();
    }

    public Collection<String> getFeaturesForUserByTag(String tag) {
        return Features.getFeaturesForGroup(getGroupByTag(tag));
    }

    private boolean checkFeatureBySiteRoles(String feature) {
        try {
            for (String role : getRoleNames()) {
                if (Features.isOnByDefaultBySiteRole(feature, role)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("An unknown error occurred", e);
        }

        return false;
    }

    /**
     * Returns true if the user is a part of a group with the matching tag and feature.
     *
     * @param tag     The tag for the feature to match.
     * @param feature The feature name.
     *
     * @return <b>true</b> if the user is a member of a group with the matching tag and feature, <b>false</b> otherwise.
     */
    public boolean checkFeature(String tag, String feature) {
        return !Features.isBanned(feature) && (checkFeatureBySiteRoles(feature) || Features.checkFeature(getGroupByTag(tag), feature));

    }

    /**
     * Returns true if the user is part of any groups with the matching tags and feature.
     *
     * @param tags    The tags for the feature to match.
     * @param feature The feature name.
     *
     * @return <b>true</b> if the user is a member of a group with the matching tag and feature, <b>false</b> otherwise.
     */
    public boolean checkFeature(Collection<String> tags, String feature) {
        if (Features.isBanned(feature)) {
            return false;
        }

        if (checkFeatureBySiteRoles(feature)) {
            return true;
        }

        for (final String tag : tags) {
            if (tag != null && checkFeature(tag, feature)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the user is part of any groups with the matching tag and feature
     *
     * @param item    The item to check.
     * @param feature The feature to check.
     *
     * @return <b>true</b> if the user is a member of a group with the matching tag and feature, <b>false</b> otherwise.
     */
    public boolean checkFeature(BaseElement item, String feature) {
        return checkFeature(item.getSecurityTags().getHash().values(), feature);
    }

    /**
     * Checks whether the indicated feature has any associated tags.
     *
     * @param feature The feature to check.
     *
     * @return <b>true</b> if the feature has any tags, <b>false</b> otherwise.
     */
    public boolean checkFeatureForAnyTag(String feature) {
        return !Features.isBanned(feature) && (checkFeatureBySiteRoles(feature) || Features.checkFeatureForAnyTag(this, feature));

    }

    protected boolean hasAccessTo(final String projectId) throws Exception {
        return Roles.isSiteAdmin(this) || getAccessibleProjects().contains(projectId);
    }

    /**
     * Code copied here from
     *
     * @return All the projects where this user has edit permissions.
     *
     * @throws Exception When an error occurs.
     */
    protected List<String> getAccessibleProjects() throws Exception {
        if (_editableProjects.isEmpty()) {
            for (final List<String> row : getQueryResults("xnat:projectData/ID", "xnat:projectData")) {
                final String id = row.get(0);
                if (_editableProjects.contains(id)) {
                    continue;
                }
                try {
                    if (canModify(id)) {
                        _editableProjects.add(id);
                    }
                } catch (Exception e) {
                    log.error("Exception caught testing prearchive access", e);
                }
            }
            // if the user is an admin also add unassigned projects
            if (Roles.isSiteAdmin(this)) {
                _editableProjects.add(null);
            }
        }
        return _editableProjects;
    }

    private boolean canModify(final String projectId) throws Exception {
        if (Roles.isSiteAdmin(this)) {
            return true;
        }
        if (projectId == null) {
            return false;
        }
        final List<ElementSecurity> secureElements = ElementSecurity.GetSecureElements();
        for (ElementSecurity secureElement : secureElements) {
            try {
                if (secureElement.getSchemaElement().instanceOf("xnat:imageSessionData")) {
                    if (Permissions.can(this, secureElement.getElementName() + "/project", projectId, EDIT)) {
                        return true;
                    }
                }
            } catch (ElementNotFoundException e) {
                log.warn("Couldn't find schema element '{}' for secure element '{}', was it removed from the system?", e.ELEMENT, secureElement.getElementName());
            }
        }
        return false;
    }

    private GroupsAndPermissionsCache getCache() {
        if (_cache == null) {
            _cache = XDAT.getContextService().getBean(GroupsAndPermissionsCache.class);
        }
        return _cache;
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

    private static final long   serialVersionUID = -8144623503683531831L;

    private static final String QUERY_USER_PERMISSIONS = "SELECT " +
                                                          "  xea.element_name    AS element_name, " +
                                                          "  xeamd.status        AS active_status, " +
                                                          "  xfms.method         AS method, " +
                                                          "  xfm.field           AS field, " +
                                                          "  xfm.field_value     AS field_value, " +
                                                          "  xfm.comparison_type AS comparison_type, " +
                                                          "  xfm.read_element    AS can_read, " +
                                                          "  xfm.edit_element    AS can_edit, " +
                                                          "  xfm.create_element  AS can_create, " +
                                                          "  xfm.delete_element  AS can_delete, " +
                                                          "  xfm.active_element  AS can_active " +
                                                          "FROM xdat_user u " +
                                                          "  LEFT JOIN xdat_element_access xea ON u.xdat_user_id = xea.xdat_user_xdat_user_id " +
                                                          "  LEFT JOIN xdat_element_access_meta_data xeamd ON xea.element_access_info = xeamd.meta_data_id " +
                                                          "  LEFT JOIN xdat_field_mapping_set xfms ON xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                          "  LEFT JOIN xdat_field_mapping xfm ON xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                          "WHERE " +
                                                          "  u.xdat_user_id = :userId";

    private static final SimpleGrantedAuthority AUTHORITY_ANONYMOUS = new SimpleGrantedAuthority("ROLE_ANONYMOUS");
    private static final SimpleGrantedAuthority AUTHORITY_ADMIN     = new SimpleGrantedAuthority("ROLE_ADMIN");
    private static final SimpleGrantedAuthority AUTHORITY_USER      = new SimpleGrantedAuthority("ROLE_USER");

    private final Map<String, ElementAccessManager> _accessManagers            = new HashMap<>();
    private final Set<String>                       _roleNames                 = new HashSet<>();
    private final List<XdatStoredSearch>            _storedSearches            = new ArrayList<>();
    private final List<ElementDisplay>              _searchableElementDisplays = new ArrayList<>();
    private final Set<GrantedAuthority>             _authorities               = new HashSet<>();
    private final Map<String, Long>                 _totalCounts               = new HashMap<>();
    private final List<String>                      _editableProjects          = new ArrayList<>();
    private final Map<String, ArrayList<ItemI>>     _userSessionCache          = new HashMap<>();
    private final Multimap<String, PermissionCriteriaI> _permissionCriteria = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, PermissionCriteriaI>create());

    private boolean   extended                   = false;
    private boolean   rolesNotUpdatedFromService = true;
    private long      startTime                  = Calendar.getInstance().getTimeInMillis();
    private UserAuthI _authorization = null;

    private GroupsAndPermissionsCache _cache       = null;
    private Boolean                   _isSiteAdmin = null;
}
