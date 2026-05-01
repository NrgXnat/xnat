/*
 * DicomEdit: org.nrg.dcm.edit.fn.GetURLTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.edit.fn;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.service.http.IHttpClient;
import org.nrg.dicom.mizer.values.Value;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GetURLTest {
    @Before
    public void setUp() throws Exception {
        _context.clear();
    }

    @Test
    public void testApply() throws Exception {
        final FakeGetURL function = new FakeGetURL("foobar");
        final Value      value    = function.apply(Collections.singletonList(new ConstantValue("http://nrg.wustl.edu/something")));
        assertEquals("foobar", value.on(_context));
    }

    @Test
    public void testApplyExtraWhitespace() throws Exception {
        final FakeGetURL function = new FakeGetURL("foobar \n\t");
        final Value      value    = function.apply(Collections.singletonList(new ConstantValue("http://nrg.wustl.edu/something")));
        assertEquals("foobar", value.on(_context));
    }

    private static class FakeGetURL extends GetURL {
        FakeGetURL(final String response) {
            _response = response;
        }

        protected IHttpClient getHttpClient() {
            return new IHttpClient() {
                public String request(URL url) throws IOException {
                    return _response;
                }
            };
        }

        private final String _response;
    }

    private final Map<Integer, String> _context = Maps.newHashMap();
}
