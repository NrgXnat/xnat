/*
 * org.nrg.xdat.security.TemporaryTokenStore
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.security;

import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;

import javax.mail.MessagingException;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TemporaryTokenStore {
	static Logger logger = Logger.getLogger(TemporaryTokenStore.class);
	
	private static ConcurrentHashMap<String,TokenTimestampTuple> tokenStore = new ConcurrentHashMap<String,TokenTimestampTuple>();
	private static final long expiration = (1000 * 60 * 60 * 24); // 1 day in milliseconds
	
	static class LoginTokenMismatchException extends Exception {
		public LoginTokenMismatchException() {
			super();
		}
		public LoginTokenMismatchException(String message) {
			super(message);
		}
		public LoginTokenMismatchException(Throwable cause) {
			super(cause);
		}
	}
	
	static class TokenTimestampTuple {
		final String token;
		final long timestamp;
		TokenTimestampTuple (String token) {
			this.token=token;
			this.timestamp=Calendar.getInstance().getTimeInMillis();
		}
		public boolean equals(TokenTimestampTuple t){
			if(t.token.equals(this.token) && (t.timestamp)==(this.timestamp)){
				return true;
			}
			else{
				return false;
			}
		}
	}
	
	/**
	 * A callable that takes an argument 
	 * @author deech
	 *
	 * @param <A> return value for callable
	 * @param <B> argument to callable
	 */
	interface CallableWith<A,B> {
		public A call(B b);
	}
	
	public static Boolean expired (String login) {
		TokenTimestampTuple t = tokenStore.get(login);
		long now = Calendar.getInstance().getTimeInMillis();
		if (t!=null) {
			long createTime =t.timestamp;
			if (now - createTime > expiration) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return null;
		}
	}
	
	public synchronized static TokenTimestampTuple addToken(String login) {
		String token = TemporaryTokenStore.generateToken();
		TokenTimestampTuple t = new TokenTimestampTuple(token);
		tokenStore.put(login,t);
		return t;
	}
	
	public static synchronized TokenTimestampTuple lookupLogin (String login) {
		if (expired(login) != null) {
			if (expired(login)) {
				removeToken(login);
				return null;
			}
			else {
				return tokenStore.get(login);		
			}
		}
		else {
			return null;
		}
	}
	
	public static synchronized void withLogin(String login, CallableWith<Void,String> c) {
		TokenTimestampTuple t = lookupLogin(login);
		if (t != null) {
			c.call(t.token);
		}
	}
	
	public static synchronized void withLogin (String login, String token, CallableWith<Void,String> c) throws LoginTokenMismatchException{
		TokenTimestampTuple t = lookupLogin(login);
		if (t != null) {
			if (!t.token.equals(token)) {
				throw new LoginTokenMismatchException();
			}
			else {
				c.call(t.token);
			}
		}
	}
	
	public static synchronized String getLoginByToken(String token) {
	    for (Entry<String,TokenTimestampTuple> entry : tokenStore.entrySet()) {
	        if (token.equals(entry.getValue().token)) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	public static synchronized void addTokenWith(String login, CallableWith<Void,String> c){
		TokenTimestampTuple t = addToken(login);
		if (t!=null) {
			c.call(t.token);	
		}
	}
	
	public static String emailBody (String token) {
		return "Please click this link to reset your password: " + TurbineUtils.GetFullServerPath() + "app/template/XDATScreen_UpdateUser.vm?token=" + token + "\n This link will expire in 24 hours.";
	}
	
 	public static void addTokenAndEmail(final String login, final String subject) throws Exception {
		final UserI u = Users.getUser(login);
		final String from = XDAT.getSiteConfigPreferences().getAdminEmail();
		final String[] tos = {u.getEmail()};
		final String[] ccs = {};
		final String subj = TurbineUtils.GetSystemName() + ": " + subject;
		final String body = "Dear " + u.getFirstname() + " " + u.getLastname() + ",\n" + emailBody(null);
		CallableWith<Void,String> emailAction = new CallableWith<Void,String>() {
			public Void call(String login) {
				if(XDAT.getNotificationsPreferences().getSmtpEnabled()){
					try {
						XDAT.getMailService().sendHtmlMessage(from, tos, ccs, null, subj, body);
					} catch (MessagingException exception) {
						logger.error("Unable to send mail", exception);
					}
				}
				return null;
			}
		};
		addTokenWith(login, emailAction);
	}
	
	public static boolean removeToken(String login) {
		if (tokenStore.containsKey(login)) {
			tokenStore.remove(login);
			return true;
		}
		else {
			return false;
		}
	}
	
	public static String generateToken() {
		return UUID.randomUUID().toString();
	}
}
