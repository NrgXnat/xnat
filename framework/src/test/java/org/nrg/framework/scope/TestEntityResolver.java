/*
 * framework: org.nrg.framework.scope.TestEntityResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.scope;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.framework.constants.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestEntityResolverConfiguration.class)
public class TestEntityResolver {
    @Test
    public void testSimpleResolve() {
        for (int index1 = 0; index1 < 2; index1++) {
            for (int index2 = 0; index2 < 6; index2++) {
                final EntityId start  = new EntityId(Scope.Subject, String.format("p%ds%d", index1 + 1, index2 + 1));
                final Wired    finish = _resolver.resolve(start);
                assertThat(_resolver.checkResults(start, finish.getEntityId())).isTrue();
            }
            final EntityId start  = new EntityId(Scope.Project, String.format("project%d", index1 + 1));
            final Wired    finish = _resolver.resolve(start);
            assertThat(_resolver.checkResults(start, finish.getEntityId())).isTrue();
        }
    }

    @Autowired
    private SimpleEntityResolver _resolver;
}
