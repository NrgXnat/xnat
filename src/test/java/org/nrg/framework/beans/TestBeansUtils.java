package org.nrg.framework.beans;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.nrg.framework.utilities.BasicXnatResourceLocator;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBeansUtils {
    @Before
    public void setup() throws IOException {
        for (final String id : _properties.keySet()) {
            final Properties properties = _properties.get(id);
            properties.clear();
            properties.load(BasicXnatResourceLocator.getResource("classpath:/org/nrg/framework/beans/namespaced-beans-" + id + ".properties").getInputStream());
        }
    }

    @Test
    public void testNamespacedPropertiesOnSinglePropertiesObject() {
        final Set<String> defaultNamespaces = Beans.discoverNamespaces(_alphaBeta);
        assertThat(defaultNamespaces).isNotNull().isNotEmpty().hasSize(3).containsExactlyInAnyOrder("alpha", "beta", "shared");
        final Set<String> twoTokenNamespaces = Beans.discoverNamespaces(_alphaBeta, 2);
        assertThat(twoTokenNamespaces).isNotNull().isNotEmpty().hasSize(8).containsExactlyInAnyOrder("alpha.one", "alpha.three", "alpha.two", "beta.one", "beta.three", "beta.two", "shared.one", "shared.two");

        final Properties alphaTruncated = Beans.getNamespacedProperties(_alphaBeta, "alpha", true);
        assertThat(alphaTruncated).isNotNull().isNotEmpty().hasSize(9).containsKeys(SUBKEYS);
        final Properties alphaNontruncated = Beans.getNamespacedProperties(_alphaBeta, "alpha", false);
        assertThat(alphaNontruncated).isNotNull().isNotEmpty().hasSize(9).containsKeys(prefixed("alpha", SUBKEYS));
        final Properties betaTruncated = Beans.getNamespacedProperties(_alphaBeta, "beta", true);
        assertThat(betaTruncated).isNotNull().isNotEmpty().hasSize(9).containsKeys(SUBKEYS);
        final Properties betaNontruncated = Beans.getNamespacedProperties(_alphaBeta, "beta", false);
        assertThat(betaNontruncated).isNotNull().isNotEmpty().hasSize(9).containsKeys(prefixed("beta", SUBKEYS));
    }

    @Test
    public void testNamespacedPropertiesOnListOfPropertiesObjects() {
        final Properties alphaTruncated = Beans.getNamespacedProperties(_alphaBetaGammaDelta, "alpha", true);
        assertThat(alphaTruncated).isNotNull().isNotEmpty().hasSize(9).containsKeys(SUBKEYS);
        final Properties alphaNontruncated = Beans.getNamespacedProperties(_alphaBetaGammaDelta, "alpha", false);
        assertThat(alphaNontruncated).isNotNull().isNotEmpty().hasSize(9).containsKeys(prefixed("alpha", SUBKEYS));
        final Properties gammaTruncated = Beans.getNamespacedProperties(_alphaBetaGammaDelta, "gamma", true);
        assertThat(gammaTruncated).isNotNull().isNotEmpty().hasSize(9).containsKeys(SUBKEYS);
        final Properties gammaNontruncated = Beans.getNamespacedProperties(_alphaBetaGammaDelta, "gamma", false);
        assertThat(gammaNontruncated).isNotNull().isNotEmpty().hasSize(9).containsKeys(prefixed("gamma", SUBKEYS));
        final Properties sharedTruncated = Beans.getNamespacedProperties(_alphaBetaGammaDelta, "shared", true);
        assertThat(sharedTruncated).isNotNull().isNotEmpty().hasSize(12).containsKeys(SUBKEYS);
        final Properties sharedNontruncated = Beans.getNamespacedProperties(_alphaBetaGammaDelta, "shared", false);
        assertThat(sharedNontruncated).isNotNull().isNotEmpty().hasSize(12).containsKeys(prefixed("shared", SUBKEYS));
    }

    @Test
    public void testNamespacedPropertiesMapOnSinglePropertiesObject() {
        final Map<String, Properties> alpha1 = Beans.getNamespacedPropertiesMap(_fourLevels, "alpha");
        assertThat(alpha1).isNotNull().isNotEmpty().hasSize(3).containsKeys("one", "two", "three");
        assertThat(alpha1.get("one")).isNotEmpty().hasSize(9).containsKeys("first.a", "first.b", "first.c", "second.a", "second.b", "second.c", "third.a", "third.b", "third.c");
        final Map<String, Properties> alpha2 = Beans.getNamespacedPropertiesMap(_fourLevels, "alpha", 2);
        assertThat(alpha2).isNotNull().isNotEmpty().hasSize(9).containsKeys("one.first", "one.second", "one.third", "two.first", "two.second", "two.third", "three.first", "three.second", "three.third");
        assertThat(alpha2.get("one.first")).isNotEmpty().hasSize(3).containsKeys("a", "b", "c");
        final Map<String, Properties> alpha2NonT = Beans.getNamespacedPropertiesMap(_fourLevels, "alpha", 2, false, true);
        assertThat(alpha2NonT).isNotNull().isNotEmpty().hasSize(9).containsKeys("alpha.one.first", "alpha.one.second", "alpha.one.third", "alpha.two.first", "alpha.two.second", "alpha.two.third", "alpha.three.first", "alpha.three.second", "alpha.three.third");
        assertThat(alpha2NonT.get("alpha.one.first")).isNotEmpty().hasSize(3).containsKeys("a", "b", "c");
        final Map<String, Properties> alpha2TNon = Beans.getNamespacedPropertiesMap(_fourLevels, "alpha", 2, true, false);
        assertThat(alpha2TNon).isNotNull().isNotEmpty().hasSize(9).containsKeys("one.first", "one.second", "one.third", "two.first", "two.second", "two.third", "three.first", "three.second", "three.third");
        assertThat(alpha2TNon.get("one.first")).isNotEmpty().hasSize(3).containsKeys("one.first.a", "one.first.b", "one.first.c");
        final Map<String, Properties> alpha2NonNon = Beans.getNamespacedPropertiesMap(_fourLevels, "alpha", 2, false, false);
        assertThat(alpha2NonNon).isNotNull().isNotEmpty().hasSize(9).containsKeys("alpha.one.first", "alpha.one.second", "alpha.one.third", "alpha.two.first", "alpha.two.second", "alpha.two.third", "alpha.three.first", "alpha.three.second", "alpha.three.third");
        assertThat(alpha2NonNon.get("alpha.one.first")).isNotEmpty().hasSize(3).containsKeys("alpha.one.first.a", "alpha.one.first.b", "alpha.one.first.c");
    }

    @Test
    public void testFullLog4jProperties() throws IOException {
        _fullLog4jProperties.clear();
        _fullLog4jProperties.load(BasicXnatResourceLocator.getResource("classpath:/org/nrg/framework/beans/full-log4j.properties").getInputStream());

        final Map<String, Properties> log4j = Beans.getNamespacedPropertiesMap(_fullLog4jProperties, "log4j");
        final Properties loggers = log4j.get("category");
        loggers.putAll(log4j.get("logger"));
        final Properties additivity = log4j.get("additivity");
        final Map<String, Properties> appenders = Beans.getNamespacedPropertiesMap(log4j.get("appender"));
        assertThat(log4j).isNotNull().isNotEmpty();
    }

    private String[] prefixed(final String prefix, final String[] subkeys) {
        return Lists.transform(Arrays.asList(subkeys), new Function<String, String>() {
            @Override
            public String apply(final String subkey) {
                return prefix + "." + subkey;
            }
        }).toArray(new String[0]);
    }

    private static final String[] SUBKEYS = new String[]{"one.first", "one.second", "one.third", "two.first", "two.second", "two.third", "three.first", "three.second", "three.third"};

    private final Properties              _fullLog4jProperties = new Properties();
    private final Properties              _alphaBeta           = new Properties();
    private final Properties              _gammaDelta          = new Properties();
    private final Properties              _fourLevels          = new Properties();
    private final List<Properties>        _alphaBetaGammaDelta = Arrays.asList(_alphaBeta, _gammaDelta);
    private final Map<String, Properties> _properties          = ImmutableMap.of("ab", _alphaBeta, "gd", _gammaDelta, "4-levels", _fourLevels);
}
