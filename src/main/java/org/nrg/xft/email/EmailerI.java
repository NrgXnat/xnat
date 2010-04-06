// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.email;

import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.EmailException;

public interface EmailerI {
	public void addTo(String to) throws AddressException;
	public void addTo(InternetAddress to);
	public void setTo(List<InternetAddress> to);
	
	public void addCc(String to) throws AddressException;
	public void addCc(InternetAddress to);
	public void setCc(List<InternetAddress> to);
	
	public void addBcc(String to) throws AddressException;
	public void addBcc(InternetAddress to);
	public void setBcc(List<InternetAddress> to);
	
	public void setFrom(String f);
	
	public void setSubject(String s);
	
	public void setTextMsg(String s);
	public void setHtmlMsg(String s);
	public void setMsg(String s);

	public void addAttachment(String path);
	
	public void send() throws EmailException;
	
}
