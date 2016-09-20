/*
 * org.nrg.mail.services.impl.MockJavaMailSender
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.mail.services.impl;

import java.io.InputStream;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

public class MockJavaMailSender implements JavaMailSender {

    public void setMockMimeMessage(MimeMessage message) {
        _message = message;
    }

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        // Nothing to do here yet.
    }

    @Override
    public void send(SimpleMailMessage[] simpleMessages) throws MailException {
        // Nothing to do here yet.
    }

    @Override
    public MimeMessage createMimeMessage() {
        return _message;
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void send(MimeMessage[] mimeMessages) throws MailException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void send(MimeMessagePreparator[] mimeMessagePreparators) throws MailException {
        // TODO Auto-generated method stub
        
    }

    private MimeMessage _message;
}
