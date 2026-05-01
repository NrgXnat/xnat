package org.nrg.framework.orm.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Slf4j
public class PrefixedPhysicalNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {
    private static final Pattern               PATTERN_ID_REFERENCE = Pattern.compile("^[A-Z][A-z0-9_]+_id$");
    private static final Predicate<Identifier> IS_ID_REFERENCE      = identifier -> PATTERN_ID_REFERENCE.matcher(identifier.getText()).matches();

    private final String _prefix;

    public PrefixedPhysicalNamingStrategy(final String prefix) {
        super();
        _prefix = StringUtils.appendIfMissing(prefix, "_");
        log.debug("Set Hibernate table prefix to {}", prefix);
    }

    @Override
    public Identifier toPhysicalTableName(final Identifier name, final JdbcEnvironment environment) {
        return Identifier.toIdentifier(StringUtils.prependIfMissing(super.toPhysicalTableName(name, environment).getText(), _prefix));
    }

    public Identifier toPhysicalColumnName(final Identifier name, final JdbcEnvironment environment) {
        final Identifier generated = super.toPhysicalTableName(name, environment);
        return IS_ID_REFERENCE.test(name) ? Identifier.toIdentifier(StringUtils.removeEnd(generated.getText(), "_id")) : generated;
    }
}
