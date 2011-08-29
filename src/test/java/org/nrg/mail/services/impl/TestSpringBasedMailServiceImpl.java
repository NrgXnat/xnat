/**
 * TestSpringBasedMailServiceImpl
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.mail.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.mail.services.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TestSpringBasedMailServiceImpl {
    @Test
    public void testBasicMessageSend() throws MessagingException {
        _log.debug("Sending a test message");
        _service.sendMessage("test@yahoo.com", "test@gmail.com", "Mock Subject", "Mock message.");
    }

    @Test
    @Ignore("This requires extensive mocking of the MimeMessage class. Restore the test when that's completed.")
    public void testHtmlMessageSend() {
        // First prime the MIME message with a mock object.
        MimeMessage message = Mockito.mock(MimeMessage.class);
        _sender.setMockMimeMessage(message);

        Map<String, FileSystemResource> attachments = new HashMap<String, FileSystemResource>();
        try {
            _service.sendHtmlMessage("test@yahoo.com",                      // From address
                                     new String[] { "test@gmail.com" },     // To address(es)
                                     new String[] { "test@hotmail.com" },   // Cc address(es)
                                     new String[] { "test@hushmail.com" },  // Bcc address(es)
                                     "Email subject",
                                     "<html><body>This is <b>some</b> HTML text for the message.</body></html>",
                                     "This is some plain text for the message.",
                                     attachments);
        } catch (MessagingException exception) {
            fail("Failed with an exception: " + exception);
        }
    }

    private static final Log _log = LogFactory.getLog(TestSpringBasedMailServiceImpl.class);

    @Autowired
    private MockJavaMailSender _sender;
    @Autowired
    private MailService _service;
}
