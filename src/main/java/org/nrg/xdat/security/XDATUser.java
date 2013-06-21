//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 13, 2005
 *
 */
package org.nrg.xdat.security;

import java.io.File;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.Turbine;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.entities.UserRole;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatFieldMapping;
import org.nrg.xdat.om.XdatFieldMappingSet;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.om.XdatUserGroupid;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.search.QueryOrganizer;
import org.nrg.xdat.services.UserRoleService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.FavEntries;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.MetaDataException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.layeredSequence.LayeredSequenceCollection;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.search.SQLClause;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.StringUtils;

import com.google.common.collect.Lists;

/**
 * @author Tim
 */
@SuppressWarnings({"unchecked"})
public class XDATUser extends XdatUser implements UserI, Serializable {
    private static final long serialVersionUID = -8144623503683531831L;
    static Logger logger = Logger.getLogger(XDATUser.class);
    public static final String USER_ELEMENT = "xdat:user";
    private boolean loggedIn = false;
    private Hashtable<String, ElementAccessManager> accessManagers = null;
    private Hashtable actions = null;
    private Hashtable<String, UserGroup> groups = null;
    private boolean extended = false;
    private List<String> roleNames = null;
    private ArrayList<XdatStoredSearch> stored_searches = null;

    private ArrayList<ElementDisplay> browseable = null;
    private ArrayList layered_stored_searches = null;

    long startTime = Calendar.getInstance().getTimeInMillis();

    private String bundleHTMLMenu = null;

    /**
     * DO NOT USE THIS.  IT is only for use in unit testings.  Use XDATUser(login).
     */
    public XDATUser() {
    }

    public XDATUser(String login) throws Exception {
        super((UserI) null);
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
        if (XFT.VERBOSE) System.out.println("User(login) Loaded (" + (Calendar.getInstance().getTimeInMillis() - startTime) + ")ms");
    }

    public XDATUser(ItemI i) throws Exception {
        this(i, true);
    }

    public XDATUser(ItemI i, boolean extend) throws Exception {
        super((UserI) null);
        setItem(i);
        if (extend) {
            if (!isExtended()) {
                init();
                if (!this.getItem().isPreLoaded()) extend(true);
                setExtended(true);
            }
        }
    }

    public XDATUser(String login, String password) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException, FieldNotFoundException, FailedLoginException, PasswordAuthenticationException, Exception {
        this(login);

        if (Authenticator.Authenticate(this, new Authenticator.Credentials(login, password))) {
            this.setLoggedIn(true);
            if (!isExtended()) {
                init();
                if (!this.getItem().isPreLoaded()) extend(true);
                setExtended(true);
            }
        }
        if (XFT.VERBOSE) System.out.println("User(login,password) Loaded (" + (Calendar.getInstance().getTimeInMillis() - startTime) + ")ms");
    }

    public boolean login(String password) throws PasswordAuthenticationException, Exception {
        loggedIn = false;

        if (!this.isEnabled()) {
            throw new EnabledException(this.getUsername());
        }

        if ((!this.isActive()) && (!this.checkRole("Administrator"))) {
            throw new ActivationException(this.getUsername());
        }

        String pass = (String) this.getStringProperty("primary_password");

        if (StringUtils.IsEmpty(pass)) throw new PasswordAuthenticationException(getUsername());

        // encryption
        if (EncryptString(password, "SHA-256").equals(pass)) {
            loggedIn = true;
        } else if (EncryptString(EncryptString(password, "obfuscate"), "SHA-256").equals(pass)) {
            loggedIn = true;
        } else if (EncryptString(password, "obfuscate").equals(pass)) {
            loggedIn = true;
        } else if (password.equals(pass)) {
            loggedIn = true;
        }

        if (!loggedIn) {
            throw new PasswordAuthenticationException(getUsername());
        }
        return loggedIn;
    }

    public ElementAccessManager getAccessManager(String s) throws Exception {
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

        this.groups = null;
        this.stored_searches = null;
        this.clearLocalCache();

        if (XFT.VERBOSE) System.out.println("User Init(" + this.getUsername() + "): " + (Calendar.getInstance().getTimeInMillis() - startTime) + "ms");
    }

    public static String EncryptString(String stringToEncrypt) {
        return EncryptString(stringToEncrypt, "SHA-256");
    }

    public static String EncryptString(String stringToEncrypt, String algorithm) {
        if (algorithm.equals("SHA-256")) {
            return DigestUtils.sha256Hex(stringToEncrypt);
        } else if (algorithm.equals("obfuscate")) {
            int prime = 373;
            int g = 2;
            String ans = "";
            char[] as = stringToEncrypt.toCharArray();
            for (int i = 0; i < stringToEncrypt.length(); i++) {
                int encrypt_digit = g ^ (char) (as[i]) % prime;
                ans = ans + (char) (encrypt_digit);
            }
            return (ans);
        } else {
            return null;
        }
    }

    public static class UserNotFoundException extends FailedLoginException {
        public UserNotFoundException(String login) {
            super("Invalid Login and/or Password", login);
        }
    }

    ;

    public static class PasswordAuthenticationException extends FailedLoginException {
        public PasswordAuthenticationException(String login) {
            super("Invalid Login and/or Password", login);
        }
    }

    ;

    public static class EnabledException extends FailedLoginException {
        public EnabledException(String login) {
            super("User (" + login + ") Account is disabled.", login);
        }
    }

    ;
    
    public static class VerifiedException extends FailedLoginException {
        public VerifiedException(String login) {
            super("User (" + login + ") Account is unverified.", login);
        }
    }

    ;

    public static class ActivationException extends FailedLoginException {
        public ActivationException(String login) {
            super("User (" + login + ") Account is in quarantine.", login);
        }
    }

    ;

    public static class FailedLoginException extends Exception {
        public String FAILED_LOGIN = null;

        public FailedLoginException(String message, String login) {
            super(message);
            FAILED_LOGIN = login;
        }
    }

    ;

    public static class PasswordComplexityException extends Exception {

        public PasswordComplexityException(String message) {
            super(message);
        }
    }

    ;
    /**
     * @return
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean isEnabled() {
        try {
            return this.getItem().getBooleanProperty("enabled", true);
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isVerified() {
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

    /**
     * @param b
     */
    public void setLoggedIn(boolean b) {
        loggedIn = b;
    }

    public CriteriaCollection getCriteriaForDisplayRead(SchemaElementI rootElement) throws IllegalAccessException, Exception {
        return getCriteriaForDisplayRead(new SchemaElement(rootElement.getGenericXFTElement()));
    }

    public CriteriaCollection getCriteriaForDisplayRead(SchemaElement rootElement) throws IllegalAccessException, Exception {
        if (!ElementSecurity.IsSecureElement(rootElement.getFullXMLName(), SecurityManager.READ)) {
            return null;
        } else {
            ElementAccessManager eam = this.getAccessManager(rootElement.getFullXMLName());
            ArrayList eams = new ArrayList();

            if (eam != null) {
                eams.add(eam);
            }

            for (UserGroup group : this.getGroups().values()) {
                ElementAccessManager eam2 = group.getAccessManagers().get(rootElement.getFullXMLName());
                if (eam2 != null) {
                    eams.add(eam2);
                }
            }

            CriteriaCollection cc = new CriteriaCollection("OR");
            Iterator iter2 = eams.iterator();
            while (iter2.hasNext()) {
                ElementAccessManager eam3 = (ElementAccessManager) iter2.next();
                CriteriaCollection sub = eam3.getXDATCriteria(SecurityManager.READ);
                if (sub != null) {
                    if (sub.numClauses() > 0) {
                        cc.add(sub);
                    }
                }
            }

            ElementAccessManager guest = getGuestManagers().get(rootElement.getFullXMLName());
            if (guest != null) {
                CriteriaCollection sub = guest.getXDATCriteria(SecurityManager.READ);
                if (sub != null) {
                    if (sub.numClauses() > 0) {
                        cc.add(sub);
                    }
                }
            }

            if (cc.numClauses() == 0) {
                if (ElementSecurity.IsSecureElement(rootElement.getFullXMLName(), SecurityManager.READ)) {
                    throw new IllegalAccessException("Invalid read privileges for " + rootElement.getFullXMLName());

                } else {
                    return null;
                }
            }

            return cc;
        }
    }

    public CriteriaCollection getCriteriaForBackendRead(GenericWrapperElement rootElement) throws Exception {
        return getCriteriaForBackendRead(new SchemaElement(rootElement));
    }

    public CriteriaCollection getCriteriaForBackendRead(SchemaElementI rootElement) throws Exception {
        if (!ElementSecurity.IsSecureElement(rootElement.getFullXMLName(), SecurityManager.READ)) {
            return null;
        } else {
            ElementAccessManager eam = getAccessManager(rootElement.getFullXMLName());

            ArrayList eams = new ArrayList();

            if (eam != null) {
                eams.add(eam);
            }

            for (UserGroup group : this.getGroups().values()) {
                ElementAccessManager eam2 = group.getAccessManagers().get(rootElement.getFullXMLName());
                if (eam2 != null) {
                    eams.add(eam2);
                }
            }

            CriteriaCollection cc = new CriteriaCollection("OR");
            Iterator iter2 = eams.iterator();
            while (iter2.hasNext()) {
                ElementAccessManager eam3 = (ElementAccessManager) iter2.next();
                CriteriaCollection sub = eam3.getXFTCriteria(SecurityManager.READ);
                if (sub != null) {
                    if (sub.numClauses() > 0) {
                        cc.add(sub);
                    }
                }
            }

            ElementAccessManager guest = getGuestManagers().get(rootElement.getFullXMLName());
            if (guest != null) {
                CriteriaCollection sub = guest.getXFTCriteria(SecurityManager.READ);
                if (sub != null) {
                    if (sub.numClauses() > 0) {
                        cc.add(sub);
                    }
                }
            }

            if (cc.numClauses() == 0) {
                if (ElementSecurity.IsSecureElement(rootElement.getFullXMLName(), SecurityManager.READ)) {
                    throw new IllegalAccessException("Invalid read privileges for " + rootElement.getFullXMLName());

                } else {
                    return null;
                }
            }

            return cc;
        }
    }

    Map<String, ElementAccessManager> cached_managers = null;

    public Map<String, ElementAccessManager> getGuestManagers() {
        if (cached_managers == null) {
            cached_managers = ElementAccessManager.GetGuestManagers();
        }

        return cached_managers;
    }

    /**
     * ArrayList of ElementDisplays which this user could edit
     *
     * @return
     * @throws ElementNotFoundException
     * @throws XFTInitException
     */
    public ArrayList<ElementDisplay> getCreateableElementDisplays() throws ElementNotFoundException, XFTInitException, Exception {
        final Map<String, ElementDisplay> hash = new Hashtable<String, ElementDisplay>();
        for (Map.Entry<String, ElementAccessManager> keySet : getAccessManagers().entrySet()) {
            ElementAccessManager eam = keySet.getValue();
            if (eam.canCreateAny()) {
                ElementDisplay ed = eam.getElementDisplay();
                if (ed != null) {
                    hash.put(ed.getElementName(), ed);
                }
            }
        }

        for (UserGroup group : this.getGroups().values()) {
            for (String key : group.getAccessManagers().keySet()) {
                if (!hash.containsKey(key)) {
                    ElementAccessManager eam = (ElementAccessManager) group.getAccessManagers().get(key);
                    if (eam.canCreateAny()) {
                        final ElementDisplay ed = eam.getElementDisplay();
                        if (ed != null) {
                            if (!hash.containsKey(ed.getElementName()))
                                hash.put(ed.getElementName(), ed);
                        }
                    }
                }
            }
        }

        for (String key : getGuestManagers().keySet()) {
            final ElementAccessManager eam = (ElementAccessManager) getGuestManagers().get(key);
            if (eam.canCreateAny()) {
                final ElementDisplay ed = eam.getElementDisplay();
                if (ed != null && !hash.containsKey(ed.getElementName())) {
                    hash.put(ed.getElementName(), ed);
                }
            }
        }

        for (ElementDisplay ed : this.getUnSecuredElements()) {
            hash.put(ed.getElementName(), ed);
        }

        final ArrayList<ElementDisplay> al = new ArrayList<ElementDisplay>();
        al.addAll(hash.values());
        al.trimToSize();
        //Collections.sort(al,ElementDisplay.SequenceComparator);

        Collections.sort(al, DescriptionComparator);
        return al;
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

    /**
     * ArrayList of ElementDisplays which this user could edit
     *
     * @return
     * @throws ElementNotFoundException
     * @throws XFTInitException
     */
    public List<ElementDisplay> getEditableElementDisplays() throws ElementNotFoundException, XFTInitException, Exception {
        final Map<String, ElementDisplay> hash = new Hashtable<String, ElementDisplay>();
        for (Map.Entry<String, ElementAccessManager> keySet : getAccessManagers().entrySet()) {
            final ElementAccessManager eam = keySet.getValue();
            if (eam.canEditAny()) {
                ElementDisplay ed = eam.getElementDisplay();
                if (ed != null) {
                    hash.put(ed.getElementName(), ed);
                }
            }
        }

        for (UserGroup group : this.getGroups().values()) {
            for (String key : group.getAccessManagers().keySet()) {
                if (!hash.containsKey(key)) {
                    final ElementAccessManager eam = (ElementAccessManager) group.getAccessManagers().get(key);
                    if (eam.canEditAny()) {
                        final ElementDisplay ed = eam.getElementDisplay();
                        if (ed != null) {
                            if (!hash.containsKey(ed.getElementName()))
                                hash.put(ed.getElementName(), ed);
                        }
                    }
                }
            }
        }

        for (String key : getGuestManagers().keySet()) {
            final ElementAccessManager eam = (ElementAccessManager) getGuestManagers().get(key);
            if (eam.canEditAny()) {
                final ElementDisplay ed = eam.getElementDisplay();
                if (ed != null && !hash.containsKey(ed.getElementName())) {
                    hash.put(ed.getElementName(), ed);
                }
            }
        }

        for (ElementDisplay ed : this.getUnSecuredElements()) {
            hash.put(ed.getElementName(), ed);
        }

        final ArrayList<ElementDisplay> al = new ArrayList<ElementDisplay>();
        al.addAll(hash.values());
        al.trimToSize();

        Collections.sort(al, ElementDisplay.SequenceComparator);
        return al;
    }

    /**
     * ArrayList of ElementDisplays Which this user can read
     *
     * @return
     * @throws ElementNotFoundException
     * @throws XFTInitException
     */
    public List<ElementDisplay> getReadableElementDisplays() throws ElementNotFoundException, XFTInitException, Exception {
        final Map<String, ElementDisplay> hash = new Hashtable<String, ElementDisplay>();
        for (Map.Entry<String, ElementAccessManager> keySet : getAccessManagers().entrySet()) {
            final ElementAccessManager eam = keySet.getValue();
            if (eam.canReadAny()) {
                final ElementDisplay ed = eam.getElementDisplay();
                if (ed != null) {
                    hash.put(ed.getElementName(), ed);
                }
            }
        }

        for (UserGroup group : this.getGroups().values()) {
            for (String key : group.getAccessManagers().keySet()) {
                if (!hash.containsKey(key)) {
                    final ElementAccessManager eam = (ElementAccessManager) group.getAccessManagers().get(key);
                    if (eam.canReadAny()) {
                        final ElementDisplay ed = eam.getElementDisplay();
                        if (ed != null) {
                            if (!hash.containsKey(ed.getElementName()))
                                hash.put(ed.getElementName(), ed);
                        }
                    }
                }
            }
        }

        for (String key : getGuestManagers().keySet()) {
            final ElementAccessManager eam = (ElementAccessManager) getGuestManagers().get(key);
            if (eam.canReadAny()) {
                final ElementDisplay ed = eam.getElementDisplay();
                if (ed != null && !hash.containsKey(ed.getElementName())) {
                    hash.put(ed.getElementName(), ed);
                }
            }
        }

        for (ElementDisplay ed : this.getUnSecuredElements()) {
            hash.put(ed.getElementName(), ed);
        }

        final List<ElementDisplay> al = new ArrayList<ElementDisplay>();
        al.addAll(hash.values());

        Collections.sort(al, ElementDisplay.SequenceComparator);
        return al;
    }

    public boolean hasPermissions() {
        try {
            if (getAccessManagers().size() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return
     */

    public Hashtable<String, ElementAccessManager> getAccessManagers() throws Exception {
        if (this.accessManagers == null) {
            this.init();
        }
        return this.accessManagers;
    }


    public String getSQLSelect(SchemaElement root) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM ").append(root.getDisplayTable());
        SQLClause coll = getCriteriaForDisplayRead(root);
        if (coll != null) {
            sb.append(" WHERE ").append(coll.getSQLClause(null));
        } else {
            if (!root.isInSecure()) {
                throw new IllegalAccessException("Illegal join to " + root.getFullXMLName());
            }
        }

        return sb.toString();
    }

    public DisplaySearch getSearch(String elementName, String display) throws Exception {
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
    public List<ElementDisplay> getUnSecuredElements() throws XFTInitException, ElementNotFoundException, Exception {
        final List<ElementDisplay> al = new ArrayList<ElementDisplay>();

        for (ElementSecurity es : ElementSecurity.GetInSecureElements()) {
            if (es.getSchemaElement().hasDisplay()) {
                al.add(es.getSchemaElement().getDisplay());
            }
        }
        return al;
    }

    private boolean securityCheck(String action, SchemaElementI root, SecurityValues values, String headerFormat) throws Exception {
        boolean can = false;

        if (ElementSecurity.IsInSecureElement(root.getFullXMLName())) {
            can = true;
        } else {
            ElementAccessManager eam3 = this.getAccessManager(root.getFullXMLName());

            ArrayList eams = new ArrayList();
            if (eam3 != null) {
                eams.add(eam3);
            }

            for (UserGroup group : this.getGroups().values()) {
                ElementAccessManager eam2 = group.getAccessManagers().get(root.getFullXMLName());
                if (eam2 != null) {
                    eams.add(eam2);
                }
            }

            ElementAccessManager guest = getGuestManagers().get(root.getFullXMLName());
            if (guest != null) {
                eams.add(guest);
            }

            Iterator iter2 = eams.iterator();
            while (iter2.hasNext()) {
                ElementAccessManager eam = (ElementAccessManager) iter2.next();

                if (eam == null) {
                    can = false;
                } else {
                    if (ElementSecurity.HasPrimarySecurityFields(root.getFullXMLName())) {
                        List<PermissionSet> sets = eam.getPermissionSets();
//                        if (sets.size()==0){
//                            if (eam.getGuestManager() !=null)
//                            {
//                                sets = eam.getGuestManager().getPermissionSets();
//                            }
//                        }
                        Iterator iter3 = sets.iterator();
                        while (iter3.hasNext()) {
                            PermissionSet ps = (PermissionSet) iter3.next();
//                            if (eam.getGuestManager() !=null)
//                            {
//                                ps.setGuestEAM(eam.getGuestManager());
//                            }
                            if (ps == null || !ps.isActive()) {
                                can = false;
                            } else {
                                can = ps.canAccess(action, headerFormat, values);
                                if (can) {
                                    return true;
                                }
                            }
                        }

                    } else {
                        return true;
                    }
                }
            }
        }

        return can;
    }

    private boolean securityCheckByXMLPath(String action, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(action, root, values, SecurityManager.XML_PATH);
    }

    private boolean securityCheckBySelectGrandName(String action, SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheck(action, root, values, SecurityManager.SELECT_GRAND);
    }

    public boolean canCreateByXMLPath(SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheckByXMLPath(SecurityManager.CREATE, root, values);
    }

    public boolean canReadByXMLPath(SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheckByXMLPath(SecurityManager.READ, root, values);
    }

    public boolean canEditByXMLPath(SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheckByXMLPath(SecurityManager.EDIT, root, values);
    }

    public boolean canActivateByXMLPath(SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheckByXMLPath(SecurityManager.ACTIVATE, root, values);
    }

    public boolean canDeleteByXMLPath(SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheckByXMLPath(SecurityManager.DELETE, root, values);
    }

    public boolean canCreateBySelectGrandName(SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheckBySelectGrandName(SecurityManager.CREATE, root, values);
    }

    public boolean canReadBySelectGrandName(SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheckBySelectGrandName(SecurityManager.READ, root, values);
    }

    public boolean canEditBySelectGrandName(SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheckBySelectGrandName(SecurityManager.EDIT, root, values);
    }

    public boolean canDeleteBySelectGrandName(SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheckBySelectGrandName(SecurityManager.DELETE, root, values);
    }

    public boolean canActivateBySelectGrandName(SchemaElementI root, SecurityValues values) throws Exception {
        return securityCheckBySelectGrandName(SecurityManager.ACTIVATE, root, values);
    }

    public boolean canReadByXMLPath(String elementName, SecurityValues values) throws Exception {
        return canReadByXMLPath(SchemaElement.GetElement(elementName), values);
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
    public void deleteRole(XDATUser authenticatedUser, String dRole) throws Exception{
		if(!authenticatedUser.isSiteAdmin()){
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
    public void addRole(XDATUser authenticatedUser, String dRole) throws Exception{
    	if(!StringUtils.IsEmpty(dRole)){
	    	if(XDAT.getContextService().getBean(UserRoleService.class).findUserRole(this.getLogin(), dRole)==null){
	    		if(!authenticatedUser.isSiteAdmin()){
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
        
        //load from the new role store
        List<UserRole> roles =XDAT.getContextService().getBean(UserRoleService.class).findRolesForUser(this.getLogin());
        if(roles!=null){
        	for(final UserRole ur: roles){
        		r.add(ur.getRole());
        	}
        }
        
        return r;
    }
    
    /**
     * @return ArrayList of XFTItems
     */
    public List<String> getRoleNames() throws Exception {
        if (roleNames == null) {
            roleNames = loadRoleNames();
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

    /**
     * ArrayList: 0:elementName 1:ArrayList of PermissionItems
     *
     * @return
     * @throws Exception
     */
    public List<Object> getPermissionItems(String elementName) throws Exception {
        final ElementSecurity es = ElementSecurity.GetElementSecurity(elementName);
        final List<PermissionItem> permissionItems = es.getPermissionItems(getUsername());
        boolean isAuthenticated = true;
        boolean wasSet = false;
        for (PermissionItem pi : permissionItems) {
            final ElementAccessManager eam = this.getAccessManager(es.getElementName());
            if (eam != null) {
                final PermissionCriteria pc = eam.getRootPermission(pi.getFullFieldName(), pi.getValue());
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

        final List<Object> elementManager = new ArrayList<Object>();
        elementManager.add(es.getElementName());
        elementManager.add(permissionItems);
        elementManager.add(es.getSchemaElement().getSQLName());
        elementManager.add(new Boolean(isAuthenticated));
        elementManager.add(new Boolean(wasSet));

        return elementManager;
    }

    /**
     * ArrayList: 0:elementName 1:ArrayList of PermissionItems
     *
     * @return
     * @throws Exception
     */
    public List<List<Object>> getPermissionItems() throws Exception {
        final List<List<Object>> allElements = new ArrayList<List<Object>>();


        for (ElementSecurity es : ElementSecurity.GetSecureElements()) {
            boolean isAuthenticated = true;
            boolean wasSet = false;
            List<PermissionItem> permissionItems = es.getPermissionItems(getUsername());
            for (PermissionItem pi : permissionItems) {
                final ElementAccessManager eam = this.getAccessManager(es.getElementName());
                if (eam != null) {
                    final PermissionCriteria pc = eam.getRootPermission(pi.getFullFieldName(), pi.getValue());
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

            List<Object> elementManager = new ArrayList();
            elementManager.add(es.getElementName());
            elementManager.add(permissionItems);
            elementManager.add(es.getSchemaElement().getSQLName());
            elementManager.add((isAuthenticated) ? Boolean.TRUE : Boolean.FALSE);
            elementManager.add((wasSet) ? Boolean.TRUE : Boolean.FALSE);
            if (permissionItems.size() > 0)
                allElements.add(elementManager);

        }
        return allElements;
    }

    public boolean getRootPermission(String elementName, String fieldName, Object value, String action) throws Exception {
        PermissionCriteria pc = getRootPermissionObject(elementName, fieldName, value);
        if (pc != null) {
            return pc.getAction(action);
        } else {
            return false;
        }
    }

    public PermissionCriteria getRootPermissionObject(String elementName, String fieldName, Object value) throws Exception {
        ElementAccessManager eam = getAccessManager(elementName);
        if (eam == null) {
            return null;
        } else {
            return eam.getRootPermission(fieldName, value);
        }
    }

    public void addRootPermission(String elementName, PermissionCriteria pc) throws Exception {
        XdatElementAccess xea = null;
        for (XdatElementAccess temp : this.getElementAccess()) {
            if (temp.getElementName().equals(elementName)) {
                xea = temp;
                break;
            }
        }

        if (xea == null) {
            xea = new XdatElementAccess((UserI) this);
            xea.setElementName(elementName);
            this.getItem().setChild("xdat:user.element_access", xea.getItem(), true);
        }

        XdatFieldMappingSet xfms = null;
        final List<XdatFieldMappingSet> set = xea.getPermissions_allowSet();
        if (set.size() == 0) {
            xfms = new XdatFieldMappingSet((UserI) this);
            xfms.setMethod("OR");
            xea.setPermissions_allowSet(xfms);
        } else {
            xfms = set.get(0);
        }


        XdatFieldMapping xfm = null;

        for (XdatFieldMapping t : xfms.getAllow()) {
            if (t.getField().equals(pc.getField()) && t.getFieldValue().equals(pc.getFieldValue())) {
                xfm = t;
                break;
            }
        }

        if (xfm == null) {
            xfm = new XdatFieldMapping((UserI) this);
            xfm.setField(pc.getField());
            xfm.setFieldValue((String) pc.getFieldValue());
            xfms.setAllow(xfm);
        }

        xfm.setCreateElement(pc.getCreate());
        xfm.setReadElement(pc.getRead());
        xfm.setEditElement(pc.getEdit());
        xfm.setDeleteElement(pc.getDelete());
        xfm.setActiveElement(pc.getActivate());
        xfm.setComparisonType(pc.getComparisonType());
    }


    private ItemI secureChild(ItemI item) throws Exception {
        ArrayList invalidItems = new ArrayList();

        Iterator iter = item.getChildItems().iterator();
        while (iter.hasNext()) {
            ItemI child = (ItemI) iter.next();
            boolean b = canRead(child);

            if (b) {
                if (!canActivate(child)) {
                    if (!child.isActive()) {
                        b = false;
                    }
                }
            }

            if (b) {
                secureChild(child);
            } else {
                invalidItems.add(child);
            }
        }

        if (invalidItems.size() > 0) {
            Iterator invalids = invalidItems.iterator();
            while (invalids.hasNext()) {
                XFTItem invalid = (XFTItem) invalids.next();
                XFTItem parent = (XFTItem) item;
                parent.removeItem(invalid);
                item = parent;
            }
        }
        return item;
    }

    public String canStoreItem(ItemI item, boolean allowDataDeletion) throws InvalidItemException, Exception {
        String invalidItemName = null;
        try {
            if (!canCreate(item)) {
                return item.getXSIType();
            }

            if (allowDataDeletion) {
                //this should check items stored in db, rather then just local hash
                Iterator iter = item.getChildItems().iterator();
                while (iter.hasNext()) {
                    ItemI child = (ItemI) iter.next();
                    invalidItemName = canStoreItem(child, allowDataDeletion);
                    if (invalidItemName != null) {
                        return invalidItemName;
                    }
                }
            } else {
                Iterator iter = item.getChildItems().iterator();
                while (iter.hasNext()) {
                    ItemI child = (ItemI) iter.next();
                    invalidItemName = canStoreItem(child, allowDataDeletion);
                    if (invalidItemName != null) {
                        return invalidItemName;
                    }
                }
            }
        } catch (XFTInitException e) {
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
        }

        return null;
    }

    public ItemI secureItem(ItemI item) throws IllegalAccessException, org.nrg.xft.exception.MetaDataException {
        try {
            //check readability
            boolean isOK = canRead(item);

            //check quarantine
            if (isOK) {
                if (!canActivate(item)) {
                    if (!item.isActive()) {
                        isOK = false;
                        throw new IllegalAccessException("Access Denied: This data is in quarantine.");
                    }
                }
            }

            if (isOK) {
                ArrayList invalidItems = new ArrayList();

                Iterator iter = item.getChildItems().iterator();
                while (iter.hasNext()) {
                    ItemI child = (ItemI) iter.next();
                    boolean b = canRead(child);
                    if (b) {
                        secureChild(child);
                    } else {
                        invalidItems.add(child);
                    }
                }

                if (invalidItems.size() > 0) {
                    Iterator invalids = invalidItems.iterator();
                    while (invalids.hasNext()) {
                        XFTItem invalid = (XFTItem) invalids.next();
                        XFTItem parent = (XFTItem) item;
                        parent.removeItem(invalid);
                        item = parent;
                    }
                }
            } else {
                throw new IllegalAccessException("Access Denied: Current user does not have permission to read this data.");
            }
        } catch (InvalidItemException e) {
            logger.error("", e);
        } catch (MetaDataException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw e;
        } catch (XFTInitException e) {
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }

        return item;
    }

    public boolean can(ItemI item, String action) throws InvalidItemException, Exception {
        boolean isOK = false;
        if (!ElementSecurity.HasDefinedElementSecurity(item.getXSIType())) {
            isOK = true;
        } else if (ElementSecurity.IsInSecureElement(item.getXSIType())) {
            isOK = true;
        } else {
            ElementSecurity es = ElementSecurity.GetElementSecurity(item.getXSIType());
            if (es.isSecure(action)) {
                SecurityValues sv = item.getItem().getSecurityValues();
                if (securityCheckByXMLPath(action, SchemaElement.GetElement(item.getXSIType()), sv)) {
                    isOK = true;
                } else {
                    isOK = false;
                }
            } else {
                isOK = true;
            }
        }
        return isOK;
    }

    public boolean canRead(ItemI item) throws InvalidItemException, Exception {
        boolean isOK = false;
        if (!ElementSecurity.HasDefinedElementSecurity(item.getXSIType())) {
            isOK = true;
        } else if (ElementSecurity.IsInSecureElement(item.getXSIType())) {
            isOK = true;
        } else {
            ElementSecurity es = ElementSecurity.GetElementSecurity(item.getXSIType());
            if (es.isSecure(SecurityManager.READ)) {
                SecurityValues sv = item.getItem().getSecurityValues();
                if (securityCheckByXMLPath(SecurityManager.READ, SchemaElement.GetElement(item.getXSIType()), sv)) {
                    isOK = true;
                } else {
                    isOK = false;
                }
            } else {
                isOK = true;
            }
        }
        return isOK;
    }


    public boolean canEdit(ItemI item) throws InvalidItemException, Exception {
        boolean isOK = false;
        if (!ElementSecurity.HasDefinedElementSecurity(item.getXSIType())) {
            isOK = true;
        } else if (ElementSecurity.IsInSecureElement(item.getXSIType())) {
            isOK = true;
        } else {
            ElementSecurity es = ElementSecurity.GetElementSecurity(item.getXSIType());
            if (es.isSecure(SecurityManager.EDIT)) {
                SecurityValues sv = item.getItem().getSecurityValues();
                if (securityCheckByXMLPath(SecurityManager.EDIT, SchemaElement.GetElement(item.getXSIType()), sv)) {
                    isOK = true;
                } else {
                    isOK = false;
                }
            } else {
                isOK = true;
            }
        }
        return isOK;
    }

    public boolean canCreate(ItemI item) throws InvalidItemException, Exception {
        boolean isOK = false;
        if (!ElementSecurity.HasDefinedElementSecurity(item.getXSIType())) {
            isOK = true;
        } else if (ElementSecurity.IsInSecureElement(item.getXSIType())) {
            isOK = true;
        } else {
            ElementSecurity es = ElementSecurity.GetElementSecurity(item.getXSIType());
            if (es.isSecure(SecurityManager.CREATE)) {
                SecurityValues sv = item.getItem().getSecurityValues();
                if (securityCheckByXMLPath(SecurityManager.CREATE, SchemaElement.GetElement(item.getXSIType()), sv)) {
                    isOK = true;
                } else {
                    isOK = false;
                }
            } else {
                isOK = true;
            }
        }
        return isOK;
    }

    public boolean canActivate(ItemI item) throws InvalidItemException, Exception {
        boolean isOK = false;
        if (!ElementSecurity.HasDefinedElementSecurity(item.getXSIType())) {
            isOK = true;
        } else if (ElementSecurity.IsInSecureElement(item.getXSIType())) {
            isOK = true;
        } else {
            SecurityValues sv = item.getItem().getSecurityValues();
            if (canActivateByXMLPath(SchemaElement.GetElement(item.getXSIType()), sv)) {
                isOK = true;
            } else {
                isOK = false;
            }
        }
        return isOK;
    }

    public boolean canRead(String xmlPath, Object value) throws Exception {
        String rootElement = StringUtils.GetRootElementName(xmlPath);
        boolean isOK = false;
        if (!ElementSecurity.HasDefinedElementSecurity(rootElement)) {
            isOK = true;
        } else if (ElementSecurity.IsInSecureElement(rootElement)) {
            isOK = true;
        } else {
            SecurityValues sv = new SecurityValues();
            sv.getHash().put(xmlPath, value);
            if (canReadByXMLPath(SchemaElement.GetElement(rootElement), sv)) {
                isOK = true;
            } else {
                isOK = false;
            }
        }
        return isOK;
    }

    public boolean canEdit(String xmlPath, Object value) throws Exception {
        String rootElement = StringUtils.GetRootElementName(xmlPath);
        boolean isOK = false;
        if (!ElementSecurity.HasDefinedElementSecurity(rootElement)) {
            isOK = true;
        } else if (ElementSecurity.IsInSecureElement(rootElement)) {
            isOK = true;
        } else {
            SecurityValues sv = new SecurityValues();
            sv.getHash().put(xmlPath, value);
            if (canEditByXMLPath(SchemaElement.GetElement(rootElement), sv)) {
                isOK = true;
            } else {
                isOK = false;
            }
        }
        return isOK;
    }

    public boolean canCreate(String xmlPath, Object value) throws Exception {
        return canAction(xmlPath, value, SecurityManager.CREATE);
    }

    public boolean canAction(String xmlPath, Object value, String action) throws Exception {
        String rootElement = StringUtils.GetRootElementName(xmlPath);
        boolean isOK = false;
        if (!ElementSecurity.HasDefinedElementSecurity(rootElement)) {
            isOK = true;
        } else if (ElementSecurity.IsInSecureElement(rootElement)) {
            isOK = true;
        } else {
            SecurityValues sv = new SecurityValues();
            sv.getHash().put(xmlPath, value);
            if (securityCheckByXMLPath(action, SchemaElement.GetElement(rootElement), sv)) {
                isOK = true;
            } else {
                isOK = false;
            }
        }
        return isOK;
    }

    public boolean canDelete(ItemI item) throws InvalidItemException, Exception {
        boolean isOK = false;
        if (!ElementSecurity.HasDefinedElementSecurity(item.getXSIType())) {
            isOK = true;
        } else if (ElementSecurity.IsInSecureElement(item.getXSIType())) {
            isOK = true;
        } else {
            ElementSecurity es = ElementSecurity.GetElementSecurity(item.getXSIType());
            if (es.isSecure(SecurityManager.DELETE)) {
                SecurityValues sv = item.getItem().getSecurityValues();
                if (securityCheckByXMLPath(SecurityManager.DELETE, SchemaElement.GetElement(item.getXSIType()), sv)) {
                    isOK = true;
                } else {
                    isOK = false;
                }
            } else {
                isOK = true;
            }
        }
        return isOK;
    }

    public ArrayList<ElementDisplay> getBrowseableElementDisplays() {
        if (browseable == null) {
            browseable = new ArrayList<ElementDisplay>();

            Hashtable counts = this.getReadableCounts();
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

    public ElementDisplay getBrowseableElementDisplay(String elementName) {
        for (ElementDisplay ed : getBrowseableElementDisplays()) {
            if (ed.getElementName().equals(elementName)) {
                return ed;
            }
        }

        return null;
    }

    public ArrayList<ElementDisplay> getBrowseableCreateableElementDisplays() {
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

    public ArrayList<ElementDisplay> getSearchableElementDisplays() {
        if (searchable == null) {
            searchable = new ArrayList<ElementDisplay>();
            Hashtable counts = this.getReadableCounts();
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

            Collections.sort(searchable, NameComparator);
        }

        return searchable;
    }

    /**
     * @return
     */
    public ArrayList<XdatStoredSearch> getStoredSearches() {
        if (this.stored_searches == null) {
            try {
                stored_searches = XdatStoredSearch.GetPreLoadedSearchesByAllowedUser(this.getLogin());

//                Enumeration enumer = this.getGroups().keys();
//                while(enumer.hasMoreElements()){
//                    String key = (String)enumer.nextElement();
//                    XdatUsergroup group =(XdatUsergroup) this.getGroups().get(key);
//
//                    ArrayList groupBundles = group.getStoredSearches();
//                    Iterator iter = groupBundles.iterator();
//                    while(iter.hasNext()){
//                        XdatStoredSearch xss =(XdatStoredSearch) iter.next();
//                        if (!stored_searches.contains(xss)){
//                            stored_searches.add(xss);
//                        }
//                    }
//                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return stored_searches;
    }

    public void replacePreLoadedSearch(XdatStoredSearch i) {
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

    public ArrayList getLayeredStoredSearches() {
        if (this.layered_stored_searches == null) {
            try {
                LayeredSequenceCollection coll = new LayeredSequenceCollection();
                ArrayList<XdatStoredSearch> al = getStoredSearches();
                for (int i = 0; i < al.size(); i++) {
                    XdatStoredSearch xss = al.get(i);
                    coll.addSequencedItem(xss);
                }

                if (coll.size() == 0) {
                    layered_stored_searches = new ArrayList();
                } else {
                    layered_stored_searches = coll.getItems();
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return layered_stored_searches;
    }

    public String outputBundleHTMLMenu() {
        if (this.bundleHTMLMenu == null) {
            StringBuffer sb = new StringBuffer();
            int anchorCounter = 200;
            ArrayList al = getLayeredStoredSearches();
            if (al != null && al.size() > 0) {
                sb.append("<TABLE align=\"left\" valign=\"top\">");
                for (int i = 0; i < al.size(); i++) {
                    XdatStoredSearch xss = (XdatStoredSearch) al.get(i);
                    sb.append("\n\t<tr><td valign=\"top\">");
                    if (xss.getLayeredChildren().size() > 0) {
                        String temp = xss.getId();
                        sb.append("<A NAME=\"LINK").append(temp).append("\"");
                        sb.append(" HREF=\"#LINK").append(temp).append("\" ");
                        sb.append("onClick=\" return blocking('").append(temp).append("');\">");
                        sb.append("<img ID=\"IMG").append(temp).append("\" src=\"");
                        sb.append(Turbine.getContextPath()).append("/images/plus.jpg\" border=0>");
                    } else {
                        sb.append("&#8226;");
                    }
                    sb.append(" </td><td align=\"left\">");
                    sb.append(xss.outputHTMLMenu(getServer(), anchorCounter));
                    sb.append("\n\t</TD></TR>");
                }
                sb.append("\n</TABLE>");
            }

            bundleHTMLMenu = sb.toString();
        }

        return bundleHTMLMenu;
    }

    public String getServer() {
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
    public XdatStoredSearch getStoredSearchItem(String id) {
        ArrayList<XdatStoredSearch> temp = getStoredSearches();
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
    public DisplaySearch getStoredSearch(String id) {
        ArrayList<XdatStoredSearch> temp = getStoredSearches();
        XdatStoredSearch xss = null;
        try {
            for (XdatStoredSearch search : temp) {
                if (id.equalsIgnoreCase(search.getStringProperty("xdat:stored_search.ID"))) {
                    xss = search;
                }
            }

            if (xss == null) {
                return null;
            } else {
                return xss.getDisplaySearch(this);
            }
        } catch (XFTInitException e) {
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }

    public boolean canReadStoredSearch(String ss_id) {
        Object o = getStoredSearch(ss_id);
        if (o == null) {
            return false;
        } else {
            return true;
        }
    }
//  REMOVED.  NOT USED ANYWHERE THAT I CAN FIND. TO
//	public void initializePermissions()
//	{
//	    try {
//            ArrayList elements = ElementSecurity.GetSecureElements();
//            Iterator iter = elements.iterator();
//            while (iter.hasNext())
//            {
//            	try {
//                    ElementSecurity es = (ElementSecurity)iter.next();
//
//                    ArrayList permissionItems = es.getPermissionItems(this.getUsername());
//                    Iterator permissions = permissionItems.iterator();
//                    while (permissions.hasNext())
//                    {
//                    	try {
//                            PermissionItem pi = (PermissionItem)permissions.next();
//                            PermissionCriteria pc = new PermissionCriteria();
//
//                            pc.setField(pi.getFullFieldName());
//                            pc.setFieldValue(pi.getValue());
//                            pc.setRead(false);
//                            pc.setCreate(false);
//                            pc.setEdit(false);
//                            pc.setDelete(false);
//                            pc.setActivate(false);
//                            pc.setComparisonType("equals");
//
//                            addRootPermission(es.getElementName(),pc);
//                        } catch (Exception e) {
//                            logger.error("",e);
//                        }
//                    }
//                } catch (Exception e) {
//                    logger.error("",e);
//                }
//            }
//        } catch (Exception e) {
//            logger.error("",e);
//        }
//	}

    public DisplayManager getDisplayManager() {
        return DisplayManager.GetInstance();
    }

    public static java.util.Collection getAllLogins() {
        try {
            return ElementSecurity.GetDistinctIdValuesFor("xdat:user", "xdat:user.login", null).values();
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList();
        }
    }

    public ArrayList getAllItems(String elementName, boolean preLoad) {
        try {
            ArrayList al = ItemSearch.GetAllItems(elementName, this, preLoad).getItems();
            if (al.size() > 0) {
                al = WrapItems(al);
            }
            return al;
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList();
        }
    }

    public ArrayList getAllItems(String elementName, boolean preLoad, String sortBy) {
        try {
            ArrayList al = ItemSearch.GetAllItems(elementName, this, preLoad).getItems(sortBy);
            if (al.size() > 0) {
                al = WrapItems(al);
            }
            return al;
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList();
        }
    }

    public ArrayList getAllItems(String elementName, boolean preLoad, String sortBy, String sortOrder) {
        try {
            ArrayList al = ItemSearch.GetAllItems(elementName, this, preLoad).getItems(sortBy, sortOrder);
            if (al.size() > 0) {
                al = WrapItems(al);
            }
            return al;
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList();
        }
    }

    public void setPermissions(XDATUser effected, XDATUser authenticated, String elementName,String psf,String value,Boolean create,Boolean read,Boolean delete,Boolean edit,Boolean activate,boolean activateChanges,EventMetaI ci)
    {
        try {
            ElementSecurity es = ElementSecurity.GetElementSecurity(elementName);

            XdatElementAccess ea = null;
            Iterator eams = effected.getElementAccess().iterator();
            while (eams.hasNext()) {
                XdatElementAccess temp = (XdatElementAccess) eams.next();
                if (temp.getElementName().equals(elementName)) {
                    ea = temp;
                    break;
                }
            }

            if (ea == null) {
                ea = new XdatElementAccess((UserI) authenticated);
                ea.setElementName(elementName);
                ea.setProperty("xdat_user_xdat_user_id", effected.getXdatUserId());
            }

            XdatFieldMappingSet fms = null;
            ArrayList al = ea.getPermissions_allowSet();
            if (al.size() > 0) {
                fms = (XdatFieldMappingSet) ea.getPermissions_allowSet().get(0);
            } else {
                fms = new XdatFieldMappingSet((UserI) authenticated);
                fms.setMethod("OR");
                ea.setPermissions_allowSet(fms);
            }

            XdatFieldMapping fm = null;

            Iterator iter = fms.getAllow().iterator();
            while (iter.hasNext()) {
                Object o = iter.next();
                if (o instanceof XdatFieldMapping) {
                    if (((XdatFieldMapping) o).getFieldValue().equals(value) && ((XdatFieldMapping) o).getField().equals(psf)) {
                        fm = (XdatFieldMapping) o;
                    }
                }
            }

            if (fm == null) {
                if (create || read || edit || delete || activate)
                    fm = new XdatFieldMapping((UserI) authenticated);
                else
                    return;
            } else if (!(create || read || edit || delete || activate)) {
                if (fms.getAllow().size() == 1) {
                    SaveItemHelper.authorizedDelete(fms.getItem(), authenticated,ci);
                    return;
                } else {
                    SaveItemHelper.authorizedDelete(fm.getItem(), authenticated,ci);
                    return;
                }
            }


            fm.setField(psf);
            fm.setFieldValue(value);

            fm.setCreateElement(create);
            fm.setReadElement(read);
            fm.setEditElement(edit);
            fm.setDeleteElement(delete);
            fm.setActiveElement(activate);
            fm.setComparisonType("equals");
            fms.setAllow(fm);

            if (fms.getXdatFieldMappingSetId() != null) {
                fm.setProperty("xdat_field_mapping_set_xdat_field_mapping_set_id", fms.getXdatFieldMappingSetId());

                if (activateChanges) {
                    SaveItemHelper.authorizedSave(fm, authenticated, true, false, true, false,ci);
                    fm.activate(authenticated);
                } else {
                    SaveItemHelper.authorizedSave(fm, authenticated, true, false, false, false,ci);
                }
            } else if (ea.getXdatElementAccessId() != null) {
                fms.setProperty("permissions_allow_set_xdat_elem_xdat_element_access_id", ea.getXdatElementAccessId());
                if (activateChanges) {
                    SaveItemHelper.authorizedSave(fms, authenticated, true, false, true, false,ci);
                    fms.activate(authenticated);
                } else {
                    SaveItemHelper.authorizedSave(fms, authenticated, true, false, false, false,ci);
                }
            } else {
                if (activateChanges) {
                    SaveItemHelper.authorizedSave(ea, authenticated, true, false, true, false,ci);
                    ea.activate(authenticated);
                } else {
                    SaveItemHelper.authorizedSave(ea, authenticated, true, false, false, false,ci);
                }
                effected.setElementAccess(ea);
            }
        } catch (XFTInitException e) {
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (FieldNotFoundException e) {
            logger.error("", e);
        } catch (InvalidValueException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }

    }

    public void initGroups() {
        groups = null;
    }

    public Hashtable<String, UserGroup> getGroups() {
        if (groups == null) {
            groups = new Hashtable<String, UserGroup>();

            try {
                XFTTable t = XFTTable.Execute("SELECT * FROM xdat_user_groupid WHERE groups_groupid_xdat_user_xdat_user_id=" + this.getXdatUserId(), this.getDBName(), this.getLogin());

                ArrayList groupids = t.convertColumnToArrayList("groupid");

                for (int i = 0; i < groupids.size(); i++) {
                    String groupID = (String) groupids.get(i);
                    UserGroup group = UserGroupManager.GetGroup(groupID);
                    if (group != null) {
                        groups.put(groupID, group);
                    }
                }
            } catch (SQLException e) {
                logger.error("", e);
            } catch (DBPoolException e) {
                logger.error("", e);
            }

        }

        return groups;
    }

    public void replaceGroup(String id, UserGroup g) {
        if (groups.containsKey(id)) {
            groups.remove(groups.get(id));
            groups.put(id, g);
        }
    }

    public void addGroup(String id) {
        UserGroup g = UserGroupManager.GetGroup(id);
        if (g != null) {
            groups.put(id, g);
        }
    }

    public Date getPreviousLogin() throws SQLException, Exception {
        String query = "SELECT login_date FROM xdat_user_login WHERE user_xdat_user_id=" + this.getXdatUserId() + " AND login_date < (SELECT MAX(login_date) FROM xdat_user_login WHERE user_xdat_user_id=" + this.getXdatUserId() + ") ORDER BY login_date DESC LIMIT 1";
        return (Date) PoolDBUtils.ReturnStatisticQuery(query, "login_date", this.getDBName(), this.getUsername());
    }

    public void refreshGroup(String id) {
        if (groups.containsKey(id)) {
            UserGroup g = UserGroupManager.GetGroup(id);
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

    public UserGroup getGroup(String id) {
        return (UserGroup) getGroups().get(id);
    }

    public ArrayList getRecentItems(String elementName, int limit) {
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
    public List<List> getQueryResults(String xmlPaths, String rootElement) {
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

    public XFTTable getQueryResults(String query) throws SQLException, DBPoolException {
        return XFTTable.Execute(query, this.getDBName(), this.getLogin());
    }

    public ArrayList<List> getQueryResultsAsArrayList(String query) throws SQLException, DBPoolException {
        XFTTable t = XFTTable.Execute(query, this.getDBName(), this.getLogin());
        return t.toArrayListOfLists();
    }

    public boolean canCreateElement(String elementName, String xmlPath) {
        return can(elementName, xmlPath, SecurityManager.CREATE);
    }

    //can("xnat:subjectData","xnat:subjectData/project",SecurityManager.READ)
    public boolean can(String elementName, String xmlPath, String action) {
        // consider caching, but this should not hit the database on every call anyways.
        List<Object> values = getAllowedValues(elementName, xmlPath, action);
        if (values != null && values.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public List<Object> getAllowedValues(String elementName, String xmlPath, String action) {
        try {
            SchemaElement se = SchemaElement.GetElement(elementName);
            return getAllowedValues(se, xmlPath, action);
        } catch (XFTInitException e) {
            logger.error("", e);
        } catch (ElementNotFoundException e) {
            logger.error("", e);
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }


    public List<Object> getAllowedValues(SchemaElement root, String xmlPath, String action) throws Exception {
        ArrayList allowedValues = new ArrayList();

        if (ElementSecurity.IsSecureElement(root.getFullXMLName(), action)) {

            ElementAccessManager eam3 = this.getAccessManager(root.getFullXMLName());

            ArrayList eams = new ArrayList();
            if (eam3 != null) {
                eams.add(eam3);
            }

            for (UserGroup group : this.getGroups().values()) {
                ElementAccessManager eam2 = group.getAccessManagers().get(root.getFullXMLName());
                if (eam2 != null) {
                    eams.add(eam2);
                }
            }


            ElementAccessManager guest = getGuestManagers().get(root.getFullXMLName());
            if (guest != null) {
                eams.add(guest);
            }

            Iterator iter2 = eams.iterator();
            while (iter2.hasNext()) {
                ElementAccessManager eam = (ElementAccessManager) iter2.next();

                if (eam == null) {

                } else {
                    List<PermissionSet> sets = eam.getPermissionSets();
//                    if (sets.size()==0){
//                        if (eam.getGuestManager() !=null)
//                        {
//                            sets = eam.getGuestManager().getPermissionSets();
//                        }
//                    }

                    Iterator iter3 = sets.iterator();
                    while (iter3.hasNext()) {
                        PermissionSet ps = (PermissionSet) iter3.next();
//                        if (eam.getGuestManager() !=null)
//                        {
//                            ps.setGuestEAM(eam.getGuestManager());
//                        }
                        if (ps == null || !ps.isActive()) {
                        } else {
                            Iterator pcs = ps.getPermCriteria().iterator();
                            while (pcs.hasNext()) {
                                PermissionCriteria pc = (PermissionCriteria) pcs.next();
                                if (pc.getField().equals(xmlPath)) {
                                    if (pc.getAction(action)) {
                                        if (!allowedValues.contains(pc.getFieldValue())) {
                                            allowedValues.add(pc.getFieldValue());
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        } else {
            allowedValues = GenericWrapperElement.GetUniqueValuesForField(xmlPath);
        }


        Collections.sort(allowedValues);

        return allowedValues;
    }

    private Hashtable<String, ArrayList<ItemI>> userSessionCache = new Hashtable<String, ArrayList<ItemI>>();

    public ArrayList<ItemI> getCachedItems(String elementName, String security_permission, boolean preLoad) {
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
                        if (this.can(item, security_permission)) {
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

    public void clearLocalCache() {
        userSessionCache = new Hashtable<String, ArrayList<ItemI>>();
        total_counts = null;
        readable_counts = null;
    }

    Hashtable readable_counts = null;

    public Hashtable getReadableCounts() {
        if (readable_counts == null) {
            try {
                readable_counts = new Hashtable<String, Long>();

                try {
                    //projects
                    org.nrg.xft.search.QueryOrganizer qo = new org.nrg.xft.search.QueryOrganizer("xnat:projectData", this, ViewManager.ALL);
                    qo.addField("xnat:projectData/ID");

                    String query = qo.buildQuery();

                    String idField = qo.translateXMLPath("xnat:projectData/ID");

                    Long sub_count = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM (" + qo.buildQuery() + ") SEARCH;", "count", this.getDBName(), this.getUsername());
                    readable_counts.put("xnat:projectData", sub_count);

                    //subjects
                    qo = new org.nrg.xft.search.QueryOrganizer("xnat:subjectData", this, ViewManager.ALL);
                    qo.addField("xnat:subjectData/ID");

                    query = qo.buildQuery();

                    idField = qo.translateXMLPath("xnat:subjectData/ID");

                    sub_count = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM (" + qo.buildQuery() + ") SEARCH;", "count", this.getDBName(), this.getUsername());
                    readable_counts.put("xnat:subjectData", sub_count);

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

    Hashtable total_counts = null;

    public Hashtable getTotalCounts() {
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

    public ArrayList<ItemI> getCachedItemsByFieldValue(String elementName, String security_permission, boolean preLoad, String field, Object value) {
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

    public Hashtable<Object, Object> getCachedItemValuesHash(String elementName, String security_permission, boolean preLoad, String idField, String valueField) {
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

    public File getCachedFile(String relativePath) {
        String cache = XFT.GetCachePath();
        if (cache == null || cache.equals("") || cache.equals("\\")) {
            cache = ".";
        }
        return new File(cache, this.getXdatUserId() + File.separator + relativePath);

    }

    public boolean isFavorite(String elementName, String id) {
        FavEntries fe = null;
        try {
            fe = FavEntries.GetFavoriteEntries(elementName, id, this);
        } catch (DBPoolException e) {
            logger.error("", e);
        } catch (SQLException e) {
            logger.error("", e);
        }
        if (fe == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean containsGroup(String groupID) {
        for (XdatUserGroupid g : this.getGroups_groupid()) {
            if (g.getGroupid().equals(groupID)) {
                return true;
            }
        }
        return false;
    }

    public ElementSecurity getElementSecurity(String name) throws Exception {
        return ElementSecurity.GetElementSecurity(name);
    }

    public boolean isMember(String tag) {
        final UserGroup ug = this.getGroup(tag + "_member");
        if (ug != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOwner(String tag) {
        final UserGroup ug = this.getGroup(tag + "_owner");
        if (ug != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public static String getUsername(Object xdat_user_id){
    	if(xdat_user_id==null)return null;
    	else if(xdat_user_id instanceof Integer){
    		return getUsername((Integer)xdat_user_id);
    	}else {
    		return null;
    	}
    }
    
    public static String getUsername(Integer xdat_user_id){
    	if(xdat_user_id==null)return null;
    	
    	String u=getCachedUserIds().get(xdat_user_id);
    	if(u==null){
    		//check if it was added since init
    		try {
				u=(String)PoolDBUtils.ReturnStatisticQuery("select login FROM xdat_user WHERE xdat_user_id="+xdat_user_id, "login", null, null);
				if(u!=null){
					getCachedUserIds().put(xdat_user_id,u);
				}
			} catch (Exception e) {
				logger.error("",e);
			}
    	}
    	
    	return u;
    }
    public static Integer getUserid(String username){
    	if(username==null)return null;
    	
    	//retrieve cached id
    	for(Entry<Integer,String> entry:getCachedUserIds().entrySet()){
    		if(username.equals(entry.getValue())){
    			return entry.getKey();
    		}
    	}
    	
		//check if it was added since init
    	Integer u;
		try {
			u=(Integer)PoolDBUtils.ReturnStatisticQuery("select xdat_user_id FROM xdat_user WHERE login='"+username+"'", "xdat_user_id", null, null);
			if(u!=null){
				getCachedUserIds().put(u,username);
			}
		} catch (Exception e) {
			logger.error("",e);
			u=null;
		}
			
    	return u;
    }
    
    private static Object usercache=new Object();
    private static Map<Integer,String> users=null;
    private static Map<Integer,String> getCachedUserIds(){
    	if(users==null){
    		synchronized (usercache){
				users=new Hashtable<Integer,String>();
	    		//initialize database users, only done once per server restart
	    		try {
					users.putAll(XFTTable.Execute("select xdat_user_id,login FROM xdat_user ORDER BY xdat_user_id;", null, null).toHashtable("xdat_user_id", "login"));
				} catch (Exception e) {
					logger.error("",e);
				}
    		}
    	}
    	return users;
    }

    public static void ModifyUser(XDATUser authenticatedUser, ItemI found,EventDetails ci) throws InvalidPermissionException, Exception {
    	String id;
    	try {
			id=(found.getStringProperty("xdat_user_id")==null)?found.getStringProperty("login"):found.getStringProperty("xdat_user_id");
		} catch (Exception e1) {
			id=found.getStringProperty("login");
	    }
		
    	PersistentWorkflowI wrk=PersistentWorkflowUtils.getOrCreateWorkflowData(null, authenticatedUser, found.getXSIType(),id,PersistentWorkflowUtils.getExternalId(found), ci);
         
    	try {
	    	ModifyUser(authenticatedUser,found,wrk.buildEvent());
	    	 
	    	if(id.equals(found.getStringProperty("login"))) {
                String userId = found.getStringProperty("xdat_user_id");
                if (org.apache.commons.lang.StringUtils.isBlank(userId)) {
                    XdatUser user = XDATUser.getXdatUsersByLogin(id, null, false);
                    userId = user.getXdatUserId().toString();
                    if (org.apache.commons.lang.StringUtils.isBlank(userId)) {
                        throw new Exception("Couldn't find a user for the indicated login: " + found.getStringProperty("login"));
                    }
                }
                wrk.setId(userId);
	    	}
	    	
			PersistentWorkflowUtils.complete(wrk,wrk.buildEvent());
		} catch (Exception e) {
			PersistentWorkflowUtils.fail(wrk,wrk.buildEvent());
			throw e;
		}
    }
    
    public static void ModifyUser(XDATUser authenticatedUser, ItemI found,EventMetaI ci) throws InvalidPermissionException, Exception {
        ItemSearch search = new ItemSearch();
        search.setAllowMultiples(false);
        search.setElement("xdat:user");
        search.addCriteria("xdat:user.login", found.getProperty("login"));
        ItemI temp = search.exec().getFirst();
		if (temp == null) {
			 // NEW USER
		    if (authenticatedUser.checkRole("Administrator")) {
		        String tempPass = found
		                .getStringProperty("primary_password");
		        if (!StringUtils.IsEmpty(tempPass)){
		        	PasswordValidatorChain validator = XDAT.getContextService().getBean(PasswordValidatorChain.class);
		        	if(validator.isValid(tempPass, null)){
		        		//this is set to null instead of authenticatedUser because new users should be able to use any password even those that have recently been used by other users.
		        		found.setProperty("primary_password", XDATUser
		                    .EncryptString(tempPass, "SHA-256"));
		        	} else {
		        		throw new PasswordComplexityException(validator.getMessage());
		        	}
		        }
		        XDATUser newUser = new XDATUser(found);
		        // newUser.initializePermissions();
		        SaveItemHelper.authorizedSave(newUser, authenticatedUser, true, false, true, false,ci);
		        XdatUserAuth newUserAuth = new XdatUserAuth((String)found.getProperty("login"), XdatUserAuthService.LOCALDB);
		        XDAT.getXdatUserAuthService().create(newUserAuth);
		    } else {
		        throw new InvalidPermissionException("Unauthorized user modification attempt");
		    }
		} else {
		    // OLD USER
		    String tempPass = found.getStringProperty("primary_password");
		    String savedPass = temp.getStringProperty("primary_password");
		    if (StringUtils.IsEmpty(tempPass)
		            && StringUtils.IsEmpty(savedPass)) {

		    } else if (StringUtils.IsEmpty(tempPass)) {

		    } else {
		        if (!tempPass.equals(savedPass)){
		        	PasswordValidatorChain validator = XDAT.getContextService().getBean(PasswordValidatorChain.class);
		        	if(validator.isValid(tempPass, authenticatedUser)){
		            found.setProperty("primary_password", XDATUser
		                    .EncryptString(tempPass, "SHA-256"));
		        	} else {
		        		throw new PasswordComplexityException(validator.getMessage());
		        	}
		        }
		    }

		    if (authenticatedUser.checkRole("Administrator")) {
		        SaveItemHelper.authorizedSave(found, authenticatedUser, false, false,ci);
		    } else if (found.getProperty("login").equals(authenticatedUser.getLogin())) {
		        XFTItem toSave = XFTItem.NewItem("xdat:user", authenticatedUser);
		        toSave.setProperty("login", authenticatedUser.getLogin());
		        toSave.setProperty("primary_password", found.getProperty("primary_password"));
		        toSave.setProperty("email", found.getProperty("email"));
		        if(found.getProperty("verified")!=null && !(found.getProperty("verified").equals(""))){
		        	toSave.setProperty("verified", found.getProperty("verified"));
		        }
		        if(found.getProperty("enabled")!=null && !(found.getProperty("enabled").equals(""))){
		        	toSave.setProperty("enabled", found.getProperty("enabled"));
		        }
		        SaveItemHelper.authorizedSave(toSave, authenticatedUser, false, false,ci);

		        authenticatedUser.setProperty("primary_password", found.getProperty("primary_password"));
		        authenticatedUser.setProperty("email", found.getProperty("email"));
		        if(found.getProperty("verified")!=null && !(found.getProperty("verified").equals(""))){
		        	authenticatedUser.setProperty("verified", found.getProperty("verified"));
		        }
		        if(found.getProperty("enabled")!=null && !(found.getProperty("enabled").equals(""))){
		        	authenticatedUser.setProperty("enabled", found.getProperty("enabled"));
		        }
		        
		    } else {
		        throw new InvalidPermissionException("Unauthorized user modification attempt");
		    }
		}
    }

    public Date getLastLogin() throws SQLException, Exception{
    	String query = "SELECT login_date FROM xdat_user_login WHERE user_xdat_user_id=" + this.getXdatUserId() + " ORDER BY login_date DESC LIMIT 1";
        return (Date) PoolDBUtils.ReturnStatisticQuery(query, "login_date", this.getDBName(), this.getUsername());
    }
}

