package org.nrg.xnat.security.provider;

import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

public class TestProviderConfigurations {
    @Test
    public void testProviderAttributes() {
        final Properties source = new Properties();
        source.setProperty("name", "LDAP");
        source.setProperty("id", "ldap1");
        source.setProperty("type", "ldap");
        source.setProperty("address", "ldap://ldap.xnat.org");
        source.setProperty("userdn", "cn=admin,dc=xnat,dc=org");
        source.setProperty("password", "admin");
        source.setProperty("search.base", "ou=users,dc=xnat,dc=org");
        source.setProperty("search.filter", "(uid={0})");

        final ProviderAttributes attributes = new ProviderAttributes(source);
        assertThat(attributes.getName()).isEqualTo("LDAP");
        assertThat(attributes.getProviderId()).isEqualTo("ldap1");
        assertThat(attributes.getAuthMethod()).isEqualTo("ldap");
        assertThat(attributes.getOrder()).isEqualTo(-1);
        assertThat(attributes.isVisible()).isTrue();

        final Properties properties = attributes.getProperties();
        assertThat(properties).isNotNull();
        assertThat(properties).isNotEmpty();
        assertThat(properties.size()).isEqualTo(5);
        assertThat(properties.containsKey("address")).isTrue();
        assertThat(properties.containsKey("userdn")).isTrue();
        assertThat(properties.containsKey("password")).isTrue();
        assertThat(properties.containsKey("search.base")).isTrue();
        assertThat(properties.containsKey("search.filter")).isTrue();
        assertThat(properties.getProperty("address")).isEqualTo("ldap://ldap.xnat.org");
        assertThat(properties.getProperty("userdn")).isEqualTo("cn=admin,dc=xnat,dc=org");
        assertThat(properties.getProperty("password")).isEqualTo("admin");
        assertThat(properties.getProperty("search.base")).isEqualTo("ou=users,dc=xnat,dc=org");
        assertThat(properties.getProperty("search.filter")).isEqualTo("(uid={0})");
    }
}
