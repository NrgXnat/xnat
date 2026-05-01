package org.nrg.xnat.hibernate.listeners.methods;

import lombok.AccessLevel;
import lombok.Getter;
import org.nrg.xnat.hibernate.TestEventTracker;
import org.springframework.beans.factory.annotation.Autowired;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractTrackingHibernateEntityEventHandlerMethod extends AbstractHibernateEntityEventHandlerMethod {
    private TestEventTracker testEventTracker;

    protected AbstractTrackingHibernateEntityEventHandlerMethod(final Action action, final Class<?> entityClass) {
        super(action, entityClass);
    }

    @Autowired
    public void setTestEventTracker(final TestEventTracker testEventTracker) {
        this.testEventTracker = testEventTracker;
    }
}
