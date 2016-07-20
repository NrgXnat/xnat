package org.nrg.xdat.security.services.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.entities.FeatureDefinition;
import org.nrg.xdat.security.ElementAction;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.FeatureDefinitionI;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.FeatureRepositoryServiceI;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.services.FeatureDefinitionService;
import org.nrg.xdat.turbine.utils.PropertiesHelper;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class FeatureRepositoryServiceImpl implements FeatureRepositoryServiceI, InitializingBean {
    @Autowired
    public void setFeatureDefinitionService(final FeatureDefinitionService service) {
        _service = service;
    }

    @Autowired
    public void setRoleHolder(final RoleHolder roleHolder) {
        _roleHolder = roleHolder;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Set<String> propFiles = Reflection.findResources(FEATURE_DEFINITION_PACKAGE, FEATURE_DEFINITION_PROPERTIES);
        if (propFiles.size() > 0) {
            for (final String props : propFiles) {

                final Map<String, Map<String, Object>> features = PropertiesHelper.RetrievePropertyObjects(props, PROP_OBJECT_IDENTIFIER, PROP_OBJECT_FIELDS);

                final List<FeatureDefinition> allFeatures = _service.getAllWithDisabled();

                for (final Map<String, Object> feature : features.values()) {

                    final FeatureDefinition definition = new FeatureDefinition();
                    definition.setKey((String) feature.get(KEY));
                    definition.setName((String) feature.get(NAME));
                    definition.setDescription((String) feature.get(DESC));

                    FeatureDefinition match = null;

                    for (FeatureDefinition potential : allFeatures) {
                        if (potential.getKey().equals(definition.getKey())) {
                            match = potential;
                            break;
                        }
                    }

                    if (match != null) {
                        // already there
                        if (!definition.equals(match)) {
                            match.setName(definition.getName());
                            match.setDescription(definition.getDescription());
                            update(match);
                        }
                    } else {
                        //is new
                        //new ones can be turned on by default, old ones won't
                        if (feature.get(ON_BY_DEFAULT) != null && (BooleanUtils.toBoolean((String) feature.get(ON_BY_DEFAULT)))) {
                            definition.setOnByDefault(Boolean.TRUE);
                        }

                        create(definition);

                        final String actionName = (String) feature.get(ELEMENT_ACTION_NAME);
                        if (StringUtils.isNotEmpty(actionName)) {
                            _newFeatures.put(definition.getKey(), actionName);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Collection<? extends FeatureDefinitionI> getAllFeatures() {
        final List<FeatureDefinition> features = _service.getAll();

        Collections.sort(features, new Comparator<FeatureDefinition>() {
            @Override
            public int compare(FeatureDefinition o1, FeatureDefinition o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return features;
    }

    @Override
    public FeatureDefinitionI getByKey(final String key) {
        return _service.findFeatureByKey(key);
    }

    public void create(final FeatureDefinition feature) {
        _service.create(feature);
    }

    public void delete(final FeatureDefinition feature) {
        _service.delete(feature);
    }

    public void update(final FeatureDefinition feature) {
        _service.update(feature);
    }

    @Override
    public void banFeature(final String feature) {
        FeatureDefinition def = _service.findFeatureByKey(feature);
        if (def != null) {
            def.setBanned(true);
            update(def);
        }
    }

    @Override
    public void unBanFeature(final String feature) {
        FeatureDefinition def = _service.findFeatureByKey(feature);
        if (def != null) {
            def.setBanned(false);
            update(def);
        }
    }

    @Override
    public void enableByDefault(final String feature) {
        FeatureDefinition def = _service.findFeatureByKey(feature);
        if (def != null) {
            def.setOnByDefault(true);
            update(def);
        }
    }

    @Override
    public void disableByDefault(final String feature) {
        FeatureDefinition def = _service.findFeatureByKey(feature);
        if (def != null) {
            def.setOnByDefault(false);
            update(def);
        }
    }

    @Override
    public void updateNewSecureDefinitions() {
        try {
            logger.debug("Element security data found, processing new feature definitions.");
            for (final String definitionKey : _newFeatures.keySet()) {
                //after creating a new feature definition, if the feature is supposed to be related to an element action, it should be registered
                final String actionName = _newFeatures.get(definitionKey);
                for (final ElementSecurity elementSecurity : ElementSecurity.GetElementSecurities().values()) {
                    for (final ElementAction elementAction : elementSecurity.getElementActions()) {
                        if (StringUtils.equals(elementAction.getName(), actionName)) {
                            if (!StringUtils.equals(elementAction.getSecureFeature(), definitionKey)) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Found new element action {}, setting secure feature to: {}", actionName, definitionKey);
                                }
                                //need to register this action
                                elementAction.getItem().setProperty("secureFeature", definitionKey);
                                SaveItemHelper.authorizedSave(elementAction.getItem(), getAdminUser(), true, false, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_SERVICE, "Configure new feature."));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("", e);
            //otherwise ignore failure
        }
        _newFeatures.clear();
    }

    /**
     * Get the username of the site administrator. If there are multiple
     * site admins, just get the first one. If none are found, return null.
     *
     * @return The name of the admin user.
     */
    private UserI getAdminUser() throws Exception {
        for (String login : Users.getAllLogins()) {
            final UserI user = Users.getUser(login);
            if (_roleHolder.isSiteAdmin(user)) {
                return user;
            }
        }
        return null;
    }

    private static final Logger   logger                        = LoggerFactory.getLogger(FeatureRepositoryServiceImpl.class);
    private static final String   ELEMENT_ACTION_NAME           = "element_action_name";
    private static final String   ON_BY_DEFAULT                 = "OnByDefault";
    private static final String   FEATURE_DEFINITION_PACKAGE    = "config.features";
    private static final Pattern  FEATURE_DEFINITION_PROPERTIES = Pattern.compile(".*-feature-definition\\.properties");
    private static final String   NAME                          = "name";
    private static final String   DESC                          = "description";
    private static final String   KEY                           = "key";
    private static final String[] PROP_OBJECT_FIELDS            = new String[]{NAME, DESC, KEY, ON_BY_DEFAULT, ELEMENT_ACTION_NAME};
    private static final String   PROP_OBJECT_IDENTIFIER        = "org.nrg.Feature";

    private final Map<String, String> _newFeatures = new HashMap<>();

    private FeatureDefinitionService _service;
    private RoleHolder               _roleHolder;
}
