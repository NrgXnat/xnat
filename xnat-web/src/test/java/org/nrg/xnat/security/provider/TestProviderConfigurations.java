package org.nrg.xnat.security.provider;

import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

public class TestProviderConfigurations {
    @Test
    public void testProviderAttributes() {
        final Properties source = new Properties();
        source.setProperty("name", "LDAP");
        source.setProperty("provider.id", "ldap1");
        source.setProperty("auth.method", "ldap");
        source.setProperty("auto.enabled", "true");
        source.setProperty("auto.verified", "false");
        source.setProperty("address", "ldap://ldap.xnat.org");
        source.setProperty("userdn", "cn=admin,dc=xnat,dc=org");
        source.setProperty("password", "admin");
        source.setProperty("search.base", "ou=users,dc=xnat,dc=org");
        source.setProperty("search.filter", "(uid={0})");
        source.setProperty("is.true", "true");
        source.setProperty("is.false", "false");
        source.setProperty("ldap.ldap1.prop1", "value1");
        source.setProperty("ldap.ldap1.prop2", "true");
        source.setProperty("ldap.ldap1.prop3", "false");

        final ProviderAttributes attributes = new ProviderAttributes(source);
        assertThat(attributes.getName()).isEqualTo("LDAP");
        assertThat(attributes.getProviderId()).isEqualTo("ldap1");
        assertThat(attributes.getAuthMethod()).isEqualTo("ldap");
        assertThat(attributes.isVisible()).isTrue();
        assertThat(attributes.getProperty("userdn")).isEqualTo("cn=admin,dc=xnat,dc=org");
        assertThat(attributes.getProperty("userdn", "flarn")).isEqualTo("cn=admin,dc=xnat,dc=org");
        assertThat(attributes.getProperty("userdn", () -> "flarn")).isEqualTo("cn=admin,dc=xnat,dc=org");
        assertThat(attributes.getProperty("nada")).isNull();
        assertThat(attributes.getProperty("nada", "flarn")).isEqualTo("flarn");
        assertThat(attributes.getProperty("nada", () -> "flarn")).isEqualTo("flarn");
        assertThat(attributes.getBoolean("is.true")).isTrue();
        assertThat(attributes.getBoolean("is.true", false)).isTrue();
        assertThat(attributes.getBoolean("is.true", () -> false)).isTrue();
        assertThat(attributes.getBoolean("is.false")).isFalse();
        assertThat(attributes.getBoolean("is.false", true)).isFalse();
        assertThat(attributes.getBoolean("is.false", () -> true)).isFalse();
        assertThat(attributes.getBoolean("is.null")).isNull();
        assertThat(attributes.getBoolean("is.null", false)).isFalse();
        assertThat(attributes.getBoolean("is.null", true)).isTrue();
        assertThat(attributes.getBoolean("is.null", () -> false)).isFalse();
        assertThat(attributes.getBoolean("is.null", () -> true)).isTrue();
        assertThat(attributes.getBoolean("is.null", attributes.isVisible())).isTrue();
        assertThat(attributes.getBoolean("is.null", attributes::isVisible)).isTrue();
        assertThat(attributes.getQualifiedProperty("prop1")).isEqualTo("value1");
        assertThat(attributes.getQualifiedProperty("prop1", "filth")).isEqualTo("value1");
        assertThat(attributes.getQualifiedProperty("prop1", () -> "filth")).isEqualTo("value1");
        assertThat(attributes.getQualifiedProperty("prop10")).isNull();
        assertThat(attributes.getQualifiedProperty("prop10", "N/A")).isEqualTo("N/A");
        assertThat(attributes.getQualifiedProperty("prop10", () -> "N/A")).isEqualTo("N/A");
        assertThat(attributes.getQualifiedBoolean("prop1")).isNull();
        assertThat(attributes.getQualifiedBoolean("prop1", true)).isTrue();
        assertThat(attributes.getQualifiedBoolean("prop1", false)).isFalse();
        assertThat(attributes.getQualifiedBoolean("prop1", () -> true)).isTrue();
        assertThat(attributes.getQualifiedBoolean("prop1", () -> false)).isFalse();
        assertThat(attributes.getQualifiedBoolean("prop2")).isTrue();
        assertThat(attributes.getQualifiedBoolean("prop2", false)).isTrue();
        assertThat(attributes.getQualifiedBoolean("prop2", () -> false)).isTrue();
        assertThat(attributes.getQualifiedBoolean("prop3")).isFalse();
        assertThat(attributes.getQualifiedBoolean("prop3", true)).isFalse();
        assertThat(attributes.getQualifiedBoolean("prop3", () -> true)).isFalse();
        assertThat(attributes.getQualifiedBoolean("prop10")).isNull();
        assertThat(attributes.getQualifiedBoolean("prop10", true)).isTrue();
        assertThat(attributes.getQualifiedBoolean("prop10", false)).isFalse();
        assertThat(attributes.getQualifiedBoolean("prop10", () -> true)).isTrue();
        assertThat(attributes.getQualifiedBoolean("prop10", () -> false)).isFalse();

        final Properties properties = attributes.getProperties();
        assertThat(properties).isNotNull();
        assertThat(properties).isNotEmpty();
        assertThat(properties.size()).isEqualTo(10);
        assertThat(properties.getProperty("address")).isEqualTo("ldap://ldap.xnat.org");
        assertThat(properties.getProperty("userdn")).isEqualTo("cn=admin,dc=xnat,dc=org");
        assertThat(properties.getProperty("password")).isEqualTo("admin");
        assertThat(properties.getProperty("search.base")).isEqualTo("ou=users,dc=xnat,dc=org");
        assertThat(properties.getProperty("search.filter")).isEqualTo("(uid={0})");
        assertThat(properties.getProperty("is.true")).isEqualTo("true");
        assertThat(properties.getProperty("is.false")).isEqualTo("false");
        assertThat(properties.getProperty("ldap.ldap1.prop1")).isEqualTo("value1");
        assertThat(properties.getProperty("ldap.ldap1.prop2")).isEqualTo("true");
        assertThat(properties.getProperty("ldap.ldap1.prop3")).isEqualTo("false");
    }
}
