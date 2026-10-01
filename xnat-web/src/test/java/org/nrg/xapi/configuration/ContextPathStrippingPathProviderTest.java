package org.nrg.xapi.configuration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContextPathStrippingPathProviderTest {
    @Test
    public void stripsContextPathFromOperationPath() {
        assertThat(new ContextPathStrippingPathProvider("/xnat").getOperationPath("/xnat/siteConfig")).isEqualTo("/siteConfig");
    }

    @Test
    public void stripsContextPathOnlyOnce() {
        assertThat(new ContextPathStrippingPathProvider("/xnat").getOperationPath("/xnat/xnat/foo")).isEqualTo("/xnat/foo");
    }

    @Test
    public void keepsPathThatOnlySharesAPrefixWithContextPath() {
        assertThat(new ContextPathStrippingPathProvider("/xnat").getOperationPath("/xnatFoo/bar")).isEqualTo("/xnatFoo/bar");
    }

    @Test
    public void mapsBareContextPathToRoot() {
        assertThat(new ContextPathStrippingPathProvider("/xnat").getOperationPath("/xnat")).isEqualTo("/");
    }

    @Test
    public void leavesPathUnchangedForRootContext() {
        assertThat(new ContextPathStrippingPathProvider("").getOperationPath("/siteConfig")).isEqualTo("/siteConfig");
    }

    @Test
    public void leavesPathUnchangedForNullContext() {
        assertThat(new ContextPathStrippingPathProvider(null).getOperationPath("/siteConfig")).isEqualTo("/siteConfig");
    }
}
