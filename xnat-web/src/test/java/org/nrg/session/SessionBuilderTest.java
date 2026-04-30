/*
 * SessionBuilders: org.nrg.session.SessionBuilderTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.session;

import static org.junit.Assert.*;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.nrg.attr.BasicExtAttrValue;
import org.nrg.attr.ExtAttrValue;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.base.BaseElement;

public class SessionBuilderTest {
    private static class DummyBean extends BaseElement {
        private String element;
        public DummyBean(final String element) { this.element = element; }
        public final String getSchemaElementName() { return element; }
        public final void toXML(final Writer writer, boolean prettyPrint) {}
        public final String getFullSchemaElementName() { return "dummy"; }
    }

    /**
     * Test method for {@link org.nrg.session.SessionBuilder#setValues(org.nrg.xdat.bean.base.BaseElement, java.util.Collection, java.util.Map, java.lang.String[])}.
     */
    @Test
    public final void testSetValuesBaseElementCollectionOfExtAttrValueMapOfStringBeanBuilderStringArray()
    throws Exception {
        final Map<String,BaseElement> subbeans = new HashMap<String,BaseElement>();
        final Map<String,String> values = new HashMap<String,String>();

        final BaseElement bean = new DummyBean("base") {
            public void setReferenceField(final String xmlPath, final BaseElement s) {
                subbeans.put(xmlPath, s);
            }
            public void setDataField(final String xmlPath, final String value) {
                values.put(xmlPath, value);
            }
        };

        final Map<String,BeanBuilder> builders = new HashMap<String,BeanBuilder>();
        builders.put("textValue", new BeanBuilder() {
            public Collection<BaseElement> buildBeans(final ExtAttrValue value) {
                final Collection<BaseElement> beans = new ArrayList<BaseElement>(1);
                beans.add(new DummyBean(value.getName() + ":" + value.getText()));
                return beans;
            }
        });
        builders.put("attrFoo", new BeanBuilder() {
            public Collection<BaseElement> buildBeans(final ExtAttrValue value) {
                final List<BaseElement> beans = new ArrayList<BaseElement>(1);
                beans.add(new DummyBean("foo:" + value.getAttrs().get("foo")));
                return beans;
            }
        });

        final Set<ExtAttrValue> attrs = new LinkedHashSet<ExtAttrValue>();
        attrs.add(new BasicExtAttrValue("textValue", "bar"));
        final ExtAttrValue fooVal = new BasicExtAttrValue("attrFoo", null, Collections.singletonMap("foo", "baz"));
        attrs.add(fooVal);
        attrs.add(new BasicExtAttrValue("ack", "thpthpppt"));
        attrs.add(new BasicExtAttrValue("skipme", "please"));

        final List<Collection<ExtAttrValue>> skipped = new LinkedList<Collection<ExtAttrValue>>();;

        final File root = File.createTempFile("org.nrg.session.SessionBuilder", "test");
        try {
            final SessionBuilder sb = new SessionBuilder(root, "desc", new StringWriter()) {
                public final XnatImagesessiondataBean call() {
                    skipped.add(setValues(bean, attrs, builders, "skipme"));
                    return null;
                }
                public String getSessionInfo() { return "test session"; }
            };
            sb.call();
        } finally {
            root.delete();
        }

        assertEquals(1, skipped.size());
        assertEquals(1, skipped.getFirst().size());
        for (final ExtAttrValue val : skipped.getFirst()) {
            assertEquals("skipme", val.getName());
            assertEquals("please", val.getText());
        }

        assertEquals(1, values.size());
        assertTrue(values.containsKey("ack"));
        assertEquals("thpthpppt", values.get("ack"));

        assertEquals(2, subbeans.size());
        assertTrue(subbeans.containsKey("textValue"));
        assertEquals("textValue:bar", subbeans.get("textValue").getSchemaElementName());
        assertTrue(subbeans.containsKey("attrFoo"));
        assertEquals("foo:baz", subbeans.get("attrFoo").getSchemaElementName());
    }
}
