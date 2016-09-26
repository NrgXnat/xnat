/*
 * core: org.nrg.xdat.configuration.mocks.UserRepository
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.configuration.mocks;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

@Repository
public class MockUserRepository extends AbstractHibernateDAO<MockUser> {
}
