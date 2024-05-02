package org.nrg.xnat.initialization.tasks;

import io.micrometer.core.instrument.binder.jpa.*;
import org.springframework.beans.factory.annotation.Autowired;
import io.micrometer.core.instrument.MeterRegistry;
import org.hibernate.SessionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;



/*
The HibernateQueryMetrics has been deprecated in Micrometer and is now part of the
Hibernate Project since version 5.4.26. XNAT uses version 4.3.11-FINAL as of August 2023
When we upgrade Hibernate, the Query Metrics should be changed.

Needs hibernate.generate_statistics=true in the xnat-conf.properties file
 */

@Component
@Slf4j
public class SetupORMMetrics extends AbstractInitializingTask {

    @Autowired
    public SetupORMMetrics(final MeterRegistry meterRegistry, final SessionFactory sessionFactory) {
        this.meterRegistry = meterRegistry;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public String getTaskName() {
        return "Instrument hibernate metrics";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        //TODO Chnage this to the query metrics in Hibernate if we upgrade to Hibernate version 5.4.26
        // HibernateMetrics.monitor(meterRegistry, sessionFactory, SESSION_FACTORY_NAME);
       // HibernateQueryMetrics.monitor(meterRegistry, sessionFactory, SESSION_FACTORY_NAME);
    }

    private final String SESSION_FACTORY_NAME = "HibernateSessionFactory";
    private final MeterRegistry meterRegistry;
    private final SessionFactory sessionFactory;

}
