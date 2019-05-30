package org.nrg.xft.utils.predicates;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
@Accessors(prefix = "_")
@Slf4j
public class ExperimentAccessPredicate extends DataAccessPredicate {
    public ExperimentAccessPredicate(final PermissionsServiceI service, final NamedParameterJdbcTemplate template, final UserI user, final AccessLevel accessLevel, final String project, final String subject) throws NotFoundException {
        super(service, template, user, accessLevel, project, subject);
    }

    @Override
    protected boolean applyImpl(final String experiment) {
        final UserI                user     = getUser();
        final Pair<String, String> dataType = getDataType(experiment);
        if (dataType == null) {
            getMissing().add(experiment);
            return false;
        }
        final String property = dataType.getLeft();
        final String project  = dataType.getRight();
        try {
            switch (getAccessLevel()) {
                case Read:
                    return getService().canRead(user, property, project);

                case Edit:
                    return getService().canEdit(user, property, project);

                case Delete:
                    return getService().canDelete(user, property, project);
            }
        } catch (Exception e) {
            getFailed().put("An error occurred while testing " + getAccessLevel().code() + " access to the experiment " + experiment + " in project " + project + " for the user " + user.getUsername() + ". Failing permissions check to be safe, but this may not be correct.", e);
        }
        return false;
    }

    private Pair<String, String> getDataType(final String experiment) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource(getParameters()).addValue("experiment", experiment);
        return getTemplate().query(getQuery(), parameters, new ResultSetExtractor<Pair<String, String>>() {
            @Override
            public Pair<String, String> extractData(final ResultSet resultSet) throws SQLException {
                return resultSet.next() ? ImmutablePair.of(resultSet.getString("secured_property"), resultSet.getString("project")) : null;
            }
        });
    }
}
