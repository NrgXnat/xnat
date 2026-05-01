package org.nrg.framework.jcache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;

import java.lang.reflect.Method;

@Slf4j
public class DefaultHibernateEntityCacheKeyGenerator implements KeyGenerator {
    private final KeyGenerator _generator;

    public DefaultHibernateEntityCacheKeyGenerator() {
        _generator = new SimpleKeyGenerator();
    }

    @Override
    public Object generate(Object target, Method method, Object... params) {
        return _generator.generate(target, method, params);
    }
}
