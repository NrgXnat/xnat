/*
 * org.nrg.mail.api.MailMessageTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.mail.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.mail.MessagingException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mail.SimpleMailMessage;

@RunWith(JUnit4.class)
public class MailMessageTest {

    @Test
    public void asSimpleMailMessage() throws MessagingException {
	MailMessage mailMessage = createMockMailMessage();
	SimpleMailMessage simpleMailMessage = mailMessage.asSimpleMailMessage();
	assertNotNull(simpleMailMessage.getTo());
	assertEquals(1, simpleMailMessage.getTo().length);
	assertNotNull(simpleMailMessage.getCc());
	assertEquals(1, simpleMailMessage.getCc().length);
	assertNotNull(simpleMailMessage.getBcc());
	assertEquals(1, simpleMailMessage.getBcc().length);
	assertNotNull(simpleMailMessage.getFrom());
	assertNotNull(simpleMailMessage.getSubject());
	assertNotNull(simpleMailMessage.getText());
    }

    private MailMessage createMockMailMessage() {
	MailMessage message = new MailMessage();
	message.setTos(new String[] {
	    "test@yahoo.com"
	});
	message.setCcs(new String[] {
	    "test@gmail.com"
	});
	message.setBccs(new String[] {
	    "boss@gmail.com"
	});
	message.setFrom("test@hotmail.com");
	message.setSubject("Testing");
	message.setText("123");
	return message;
    }
}
