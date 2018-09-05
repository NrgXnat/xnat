package org.nrg.xdat.search;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestDisplaySearch {
    @Test
    public void testCleanColumnName() {
        final String cleanColumnName = DisplaySearch.cleanColumnName(",:.-\\;'\"?!~`#$%^&*()+=|{}<>/@[]");
        assertEquals("_com__col__________________________", cleanColumnName);
    }
}
