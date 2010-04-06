// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.email;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.log4j.Logger;
import org.nrg.xdat.turbine.utils.AdminUtils;

public class EmailerImpl implements EmailerI {
	static Logger logger = Logger.getLogger(EmailerImpl.class);
	
	private String from,subject,msg,txtMsg,htmlMsg=null;
	private List<InternetAddress> tos=new ArrayList<InternetAddress>();
	private List<InternetAddress> ccs=new ArrayList<InternetAddress>();
	private List<InternetAddress> bccs=new ArrayList<InternetAddress>();
	private List<String> attachmentPaths=new ArrayList<String>();


	public void setFrom(String from) {
		this.from = from;
	}

	public void setHtmlMsg(String htmlMsg) {
		this.htmlMsg = htmlMsg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setTextMsg(String txtMsg) {
		this.txtMsg = txtMsg;
	}


	public void addBcc(InternetAddress to) {
		bccs.add(to);		
	}

	public void addBcc(String to) throws AddressException{
		bccs.add(new InternetAddress(to));
	}

	public void setBcc(List<InternetAddress> bcc){
		bccs=bcc;
	}

	public void addCc(InternetAddress to) {
		ccs.add(to);
	}

	public void addCc(String to) throws AddressException{
		ccs.add(new InternetAddress(to));
	}

	public void setCc(List<InternetAddress> cc){
		ccs=cc;
	}

	public void addTo(InternetAddress to) {
		tos.add(to);
	}

	public void addTo(String to) throws AddressException{
		tos.add(new InternetAddress(to));
	}

	public void addAttachment(String path) {
		attachmentPaths.add(path);
	}
	
	public void setTo(List<InternetAddress> to){
		tos=to;
	}

	public void send() throws EmailException{
		try {
			String host=AdminUtils.getMailServer();

			HtmlEmail sm = new HtmlEmail();
            
            if(host.indexOf("@")>-1){
            	final String user=host.substring(0, host.indexOf("@"));
            	host=host.substring(host.indexOf("@")+1);
            	if(user.indexOf(":")>-1){
                	sm.setHostName(host);
                	sm.setAuthentication(user.substring(0,user.indexOf(":")), user.substring(user.indexOf(":")+1));
            		sm.getMailSession().getProperties().put("mail.smtp.starttls.enable", "true");
            	}else{
            		sm.setHostName(host);
            	}
            }else{
        		sm.setHostName(host);
            }
            
            sm.setFrom(from);
            
            sm.setTo(tos);
            
            if(ccs!=null && ccs.size()>0){
            	sm.setCc(ccs);
            }
            
            if(bccs!=null && bccs.size()>0){
            	sm.setBcc(bccs);
            }
            
            for (String path : attachmentPaths){
            	attach(sm, path);
            }
            
            sm.setSubject(subject);
            
            if(this.msg!=null)
            	sm.setMsg(this.msg);
            
            if(this.txtMsg!=null)
            	sm.setTextMsg(this.txtMsg);
            
            if(this.htmlMsg!=null)
            	sm.setHtmlMsg(this.htmlMsg);
   
            sm.send();
        } catch (EmailException e) {
            logger.error("Unable to send mail",e);
            throw e;
        }
	}

	private void attach(MultiPartEmail email, String path) throws EmailException {
		EmailAttachment attachment = new EmailAttachment();
		attachment.setPath(path);
		attachment.setDisposition(EmailAttachment.ATTACHMENT);
		email.attach(attachment);
	}
}
