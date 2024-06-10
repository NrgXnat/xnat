package org.nrg.xnat.customforms.utils;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

public class CustomFormHibernateUtils {

    public static <T> T initializeAndUnproxy(T entity) {
        if (entity == null) {
            throw new NullPointerException("Entity passed for initialization is null");
        }
        Hibernate.initialize(entity);
        if (entity instanceof HibernateProxy) {
            entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
        }
        return entity;
    }

}
