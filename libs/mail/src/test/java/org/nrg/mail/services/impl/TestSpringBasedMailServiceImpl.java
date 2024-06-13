/*
 * mail: org.nrg.mail.services.impl.TestSpringBasedMailServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.mail.services.impl;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.mail.config.TestSpringBasedMailServiceImplConfig;
import org.nrg.mail.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestSpringBasedMailServiceImplConfig.class)
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

        Map<String, File> attachments = new HashMap<>();
        try {
            _service.sendHtmlMessage("test@yahoo.com",                      // From address
                    new String[]{"test@gmail.com"},     // To address(es)
                    new String[]{"test@hotmail.com"},   // Cc address(es)
                    new String[]{"test@hushmail.com"},  // Bcc address(es)
                    "Email subject",
                    "<html><body>This is <b>some</b> HTML text for the message.</body></html>",
                    "This is some plain text for the message.",
                    attachments);
        } catch (MessagingException exception) {
            fail("Failed with an exception: " + exception);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(TestSpringBasedMailServiceImpl.class);

    @Autowired
    private MockJavaMailSender _sender;

    @Autowired
    private MailService        _service;
}
