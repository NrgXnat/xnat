/*
 * framework: org.nrg.framework.scope.TestEntityResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.scope;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.constants.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestEntityResolverConfiguration.class)
public class TestEntityResolver {
    @Test
    public void testSimpleResolve() {
        for (int index1 = 0; index1 < 2; index1++) {
            for (int index2 = 0; index2 < 6; index2++) {
                final EntityId start = new EntityId(Scope.Subject, String.format("p%ds%d", index1 + 1, index2 + 1));
                final Wired finish = _resolver.resolve(start);
                assertTrue(_resolver.checkResults(start, finish.getEntityId()));
            }
            final EntityId start = new EntityId(Scope.Project, String.format("project%d", index1 + 1));
            final Wired finish = _resolver.resolve(start);
            assertTrue(_resolver.checkResults(start, finish.getEntityId()));
        }
    }

    @Autowired
    private SimpleEntityResolver _resolver;
}
