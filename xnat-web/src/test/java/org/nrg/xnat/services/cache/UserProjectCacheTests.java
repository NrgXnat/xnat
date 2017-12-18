package org.nrg.xnat.services.cache;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = UserProjectCacheTestConfig.class)
public class UserProjectCacheTests {
    @Autowired
    public void setUserProjectCache(final UserProjectCache cache) {
        _cache = cache;
    }

    private UserProjectCache _cache;
}
