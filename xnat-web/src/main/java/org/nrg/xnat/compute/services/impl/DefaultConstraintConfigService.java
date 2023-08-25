package org.nrg.xnat.compute.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.compute.entities.ConstraintConfigEntity;
import org.nrg.xnat.compute.models.AccessScope;
import org.nrg.xnat.compute.models.Constraint;
import org.nrg.xnat.compute.models.ConstraintConfig;
import org.nrg.xnat.compute.models.ConstraintScope;
import org.nrg.xnat.compute.services.ConstraintConfigEntityService;
import org.nrg.xnat.compute.services.ConstraintConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultConstraintConfigService implements ConstraintConfigService {

    private final ConstraintConfigEntityService constraintConfigEntityService;

    @Autowired
    public DefaultConstraintConfigService(ConstraintConfigEntityService constraintConfigEntityService) {
        this.constraintConfigEntityService = constraintConfigEntityService;
    }

    /**
     * Returns the constraint config with the given id.
     *
     * @param id The id of the constraint config to retrieve
     * @return The constraint config with the given id
     */
    @Override
    public Optional<ConstraintConfig> retrieve(Long id) {
        ConstraintConfigEntity entity = constraintConfigEntityService.retrieve(id);
        return Optional.ofNullable(entity)
                .map(ConstraintConfigEntity::toPojo);
    }

    /**
     * Get all constraint configs.
     *
     * @return List of all constraint configs
     */
    @Override
    public List<ConstraintConfig> getAll() {
        return constraintConfigEntityService
                .getAll()
                .stream()
                .map(ConstraintConfigEntity::toPojo)
                .collect(Collectors.toList());
    }

    /**
     * Returns all the constraint configs that are available to the provided execution scope.
     *
     * @param executionScope The scope to get constraint configs for
     *                       (e.g. {Scope.Project: "project1", Scope.User: "user1"})
     * @return All constraint configs that are available for the given scope
     */
    @Override
    public List<ConstraintConfig> getAvailable(Map<Scope, String> executionScope) {
        return constraintConfigEntityService
                .getAll()
                .stream()
                .map(ConstraintConfigEntity::toPojo)
                .filter(config -> {
                    Map<Scope, AccessScope> requiredScopes = config.getScopes()
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    return AccessScope.isEnabledFor(requiredScopes, executionScope);
                })
                .collect(Collectors.toList());
    }

    /**
     * Creates a new constraint config.
     *
     * @param config The constraint config to create
     * @return The created constraint config
     */
    @Override
    public ConstraintConfig create(ConstraintConfig config) {
        // Validate
        validate(config);

        // Create
        ConstraintConfigEntity entity = constraintConfigEntityService.create(ConstraintConfigEntity.fromPojo(config));
        return constraintConfigEntityService.retrieve(entity.getId()).toPojo();
    }

    /**
     * Updates the given constraint config.
     *
     * @param config The constraint config to update
     * @return The updated constraint config
     * @throws NotFoundException If the constraint config does not exist
     */
    @Override
    public ConstraintConfig update(ConstraintConfig config) throws NotFoundException {
        // Validate
        if (config.getId() == null) {
            throw new IllegalArgumentException("Constraint config id cannot be null when updating");
        }

        validate(config);

        // Update
        ConstraintConfigEntity template = constraintConfigEntityService.retrieve(config.getId());
        template.update(config);
        constraintConfigEntityService.update(template);
        return constraintConfigEntityService.retrieve(template.getId()).toPojo();
    }

    /**
     * Deletes the constraint config with the given id.
     *
     * @param id The id of the constraint config to delete
     */
    @Override
    public void delete(Long id) {
        constraintConfigEntityService.delete(id);
    }

    /**
     * Validates that the given constraint config is valid. Throws an IllegalArgumentException if it is not.
     *
     * @param config The constraint config to validate
     */
    protected void validate(ConstraintConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Constraint config cannot be null");
        }

        List<String> errors = new ArrayList<>();

        errors.addAll(validate(config.getConstraint()));
        errors.addAll(validate(config.getScopes()));

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Constraint config is invalid: " + String.join(", ", errors));
        }
    }

    private List<String> validate(Constraint constraint) {
        List<String> errors = new ArrayList<>();

        if (constraint == null) {
            errors.add("Constraint cannot be null");
            return errors;
        }

        if (StringUtils.isBlank(constraint.getKey())) {
            errors.add("Constraint key cannot be null or blank");
        }

        if (constraint.getValues() == null || constraint.getValues().isEmpty()) {
            errors.add("Constraint values cannot be null or empty");
        }

        if (constraint.getOperator() == null) {
            errors.add("Constraint operator cannot be null");
        }

        return errors;
    }

    private List<String> validate(Map<Scope, ConstraintScope> scopes) {
        List<String> errors = new ArrayList<>();

        if (scopes == null || scopes.isEmpty()) {
            errors.add("Scopes cannot be null or empty");
            return errors;
        }

        if (!scopes.containsKey(Scope.Site)) {
            errors.add("Scopes must contain a site scope");
        }

        if (!scopes.containsKey(Scope.Project)) {
            errors.add("Scopes must contain a project scope");
        }

        return errors;
    }
}
