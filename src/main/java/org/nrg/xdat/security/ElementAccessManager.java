/*
 * core: org.nrg.xdat.security.ElementAccessManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.security;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.ItemI;
import org.nrg.xft.ItemWrapper.FieldEmptyException;
import org.nrg.xft.XFT;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.utils.XftStringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.annotation.Nullable;
import java.util.*;

import static org.nrg.xdat.security.PermissionCriteria.ACTIVATE_ELEMENT;
import static org.nrg.xdat.security.PermissionCriteria.READ_ELEMENT;

/**
 * @author Tim
 */
@Slf4j
public class ElementAccessManager {
    public static Map<String, ElementAccessManager> initialize(final NamedParameterJdbcTemplate template, final String query, final SqlParameterSource parameters) {
        final List<Map<String, Object>> results = Lists.newArrayList(Iterables.filter(template.queryForList(query, parameters), new Predicate<Map<String, Object>>() {
            @Override
            public boolean apply(@Nullable final Map<String, Object> definition) {
                // Use having read and active elements as a proxy for not actually being populated properly.
                return definition != null && definition.get(READ_ELEMENT) != null && definition.get(ACTIVATE_ELEMENT) != null;
            }
        }));
        if (results.isEmpty()) {
            return Collections.emptyMap();
        }

        final ArrayListMultimap<String, Map<String, Object>> definitions = ArrayListMultimap.create();
        for (final Map<String, Object> result : results) {
            final String elementName = (String) result.remove("element_name");
            definitions.put(elementName, result);
        }
        final Map<String, ElementAccessManager> accessManagers = new HashMap<>();
        for (final String elementName : definitions.keySet()) {
            final ElementAccessManager elementAccessManager = new ElementAccessManager(elementName, definitions.get(elementName));
            accessManagers.put(elementAccessManager.getElement(), elementAccessManager);
        }
        return accessManagers;
    }

    public ElementAccessManager(final String elementName, final List<Map<String, Object>> propertySets) {
        setElementName(elementName);
        log.info("Creating element access manager for element {}", elementName);

        final PermissionSet permissionSet = new PermissionSet(getElement(), propertySets);
        if (log.isTraceEnabled()) {
            logPermissionSet(permissionSet, index++);
        }
        sets.add(permissionSet);
    }

    public ElementAccessManager(final ItemI item) throws Exception {
        final String elementName = item.getStringProperty("element_name");
        setElementName(elementName);
        log.info("Creating element access manager for element {}", elementName);

        index = 1;
        // Each sub item is ElementAccess
        for (final ItemI sub : item.getChildItems(XFT.PREFIX + ":element_access.permissions.allow_set")) {
            // Each permission set has:
            // Method: OR, AND?
            // One or more permission criteria, which is:
            // 	field           xnat:projectData/ID
            // 	comparison      equals
            // 	value           * (or project ID)
            // 	canRead
            // 	canEdit
            // 	canCreate
            // 	canDelete
            // 	canActivate
            // 	elementName     xnat:projectData
            // 	authorized      true (?)
            // One or more permission sets
            final PermissionSet permissionSet = new PermissionSet(getElement(), sub);
            if (log.isTraceEnabled()) {
                logPermissionSet(permissionSet, index++);
            }
            sets.add(permissionSet);
        }
    }

    @Override
    public String toString() {
        return getElement() + "\n" + StringUtils.join(getPermissionSets(), "\n");
    }

    /**
     * @return Gets the element name.
     */
    public String getElement() {
        return elementName;

    }

    public void setElementName(String e) {
        elementName = XftStringUtils.intern(e);
    }

    /**
     * @return ArrayList of PermissionSets
     */
    public List<PermissionSetI> getPermissionSets() {
        return sets;
    }

    public List<PermissionCriteriaI> getRootPermissions() throws Exception {
        final List<PermissionSetI> sets = getPermissionSets();
        if (!sets.isEmpty()) {
            final PermissionSetI set = sets.get(0);
            if (set != null) {
                return set.getPermCriteria();
            }
        }
        return Collections.emptyList();
    }

    public PermissionCriteriaI getMatchingPermissions(String fieldName, Object value) throws Exception {
        final List<PermissionSetI> sets = getPermissionSets();
        if (sets.size() > 0) {
            PermissionSetI ps = sets.get(0);
            if (ps != null) {
                return ps.getMatchingPermissions(fieldName, value);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static final String SCHEMA_ELEMENT_NAME = "xdat:element_access";

    public String getSchemaElementName() {
        return SCHEMA_ELEMENT_NAME;
    }

    public SchemaElement getSchemaElement() throws ElementNotFoundException {
        if (se == null) {
            try {
                se = SchemaElement.GetElement(this.getElement());
            } catch (XFTInitException e) {
                log.error("", e);
            }
        }
        return se;
    }

    public ElementDisplay getElementDisplay() throws ElementNotFoundException {
        if (ed == null) {
            ed = getSchemaElement().getDisplay();
        }
        return ed;
    }

    public ElementSecurity getElementSecurity() throws ElementNotFoundException {
        if (es == null) {
            es = getSchemaElement().getElementSecurity();
        }
        return es;
    }


    public boolean canCreateAny() {
        if (hasNoPrimarySecurityField()) {
            return true;
        }

        try {
            for (PermissionSetI ps : getPermissionSets()) {
                if (ps.canCreateAny()) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return false;
    }

    public boolean canEditAny() {
        if (hasNoPrimarySecurityField()) {
            return true;
        }

        try {
            for (final PermissionSetI ps : getPermissionSets()) {
                if (ps.canEditAny()) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return false;
    }

    public boolean canReadAny() {
        if (hasNoPrimarySecurityField()) {
            return true;
        }

        try {
            for (PermissionSetI ps : getPermissionSets()) {
                if (ps.canReadAny()) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return false;
    }

    private boolean hasNoPrimarySecurityField() {
        try {
            final ElementSecurity es = getElementSecurity();
            if (es == null || es.getPrimarySecurityFields().isEmpty()) {
                return true;
            }
        } catch (ElementNotFoundException e1) {
            log.error("", e1);
        }
        return false;
    }

    public CriteriaCollection getXFTCriteria(final String action) {
        synchronized (xftCriteria) {
            if (!xftCriteria.containsKey(action)) {
                try {
                    final CriteriaCollection   coll = new CriteriaCollection("OR");
                    final List<PermissionSetI> al   = getPermissionSets();
                    if (al.isEmpty()) {
                        return null;
                    } else {
                        for (final PermissionSetI ps : al) {
                            final CriteriaCollection sub = Permissions.getXFTCriteria(ps, action);
                            coll.addClause(sub);
                        }

                        xftCriteria.put(action, coll);
                    }
                } catch (XFTInitException e) {
                    log.error("", e);
                } catch (FieldNotFoundException e) {
                    log.error("", e);
                } catch (ElementNotFoundException e) {
                    log.error("", e);
                } catch (Exception e) {
                    log.error("", e);
                }
            }

            return xftCriteria.get(action);
        }
    }

    public CriteriaCollection getXDATCriteria(final String action) {
        synchronized (xdatCriteria) {
            if (!xdatCriteria.containsKey(action)) {
                try {
                    final CriteriaCollection   coll = new CriteriaCollection("OR");
                    final List<PermissionSetI> al   = getPermissionSets();
                    if (al.isEmpty()) {
                        return null;
                    } else {
                        for (final PermissionSetI ps : al) {
                            final CriteriaCollection sub = Permissions.getXDATCriteria(ps, this.getSchemaElement(), action);
                            coll.addClause(sub);
                        }

                        xdatCriteria.put(action, coll);
                    }
                } catch (FieldEmptyException e) {
                    log.error("", e);
                } catch (XFTInitException e) {
                    log.error("", e);
                } catch (FieldNotFoundException e) {
                    log.error("", e);
                } catch (ElementNotFoundException e) {
                    log.error("", e);
                } catch (Exception e) {
                    log.error("", e);
                }
            }

            return xdatCriteria.get(action);
        }
    }

    public List<PermissionCriteriaI> getCriteria() {
        List<PermissionCriteriaI> criteria = Lists.newArrayList();
        for (PermissionSetI ps : this.getPermissionSets()) {
            if (ps.isActive()) {
                criteria.addAll(ps.getAllCriteria());
            }
        }
        return criteria;
    }

    private static void logPermissionSet(final PermissionSet permissionSet, final int index) {
        final StringBuilder builder = new StringBuilder("Permission set ").append(index).append(":\n");
        builder.append(" * Method: ").append(permissionSet.getMethod()).append("\n");
        builder.append(" * Permission criteria: ").append("\n");
        for (final PermissionCriteriaI criteria : permissionSet.getPermCriteria()) {
            builder.append("   * Element:     ").append(criteria.getElementName()).append("\n");
            builder.append("   * Field:       ").append(criteria.getField()).append("\n");
            builder.append("   * Field value: ").append(criteria.getFieldValue()).append("\n");
            builder.append("   * RECDA:       ").append(criteria.getRead()).append(" ").append(criteria.getEdit()).append(" ").append(criteria.getCreate()).append(" ").append(criteria.getDelete()).append(" ").append(criteria.getActivate()).append("\n");
        }
        final List<PermissionSetI> permissionSets = permissionSet.getPermSets();
        if (permissionSets == null || permissionSets.isEmpty()) {
            builder.append(" * No embedded permission sets found.").append("\n");
        } else {
            builder.append(" * Embedded permission sets:").append("\n");
            int embeddedIndex = 1;
            for (final PermissionSetI embedded : permissionSets) {
                builder.append("  * Embedded permission set ").append(embeddedIndex++).append("\n");
                builder.append("    * Method: ").append(embedded.getMethod()).append("\n");
                builder.append("    * Permission criteria: ");
                for (final PermissionCriteriaI criteria : embedded.getPermCriteria()) {
                    builder.append("    *** Element:     ").append(criteria.getElementName()).append("\n");
                    builder.append("      * Field:       ").append(criteria.getField()).append("\n");
                    builder.append("      * Field value: ").append(criteria.getFieldValue()).append("\n");
                    builder.append("      * RECDA:       ").append(criteria.getRead()).append(" ").append(criteria.getEdit()).append(" ").append(criteria.getCreate()).append(" ").append(criteria.getDelete()).append(" ").append(criteria.getActivate()).append("\n");
                }
            }
        }
        log.trace(builder.toString());
    }

    private final List<PermissionSetI>            sets         = new ArrayList<>();
    private final Map<String, CriteriaCollection> xftCriteria  = new HashMap<>();
    private final Map<String, CriteriaCollection> xdatCriteria = new HashMap<>();

    private String          elementName = null;
    private SchemaElement   se          = null;
    private ElementDisplay  ed          = null;
    private ElementSecurity es          = null;

    private int index;
}

