// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.nrg.xdat.security.XDATUser.ActivationException;
import org.nrg.xdat.security.XDATUser.EnabledException;
import org.nrg.xdat.security.XDATUser.PasswordAuthenticationException;
import org.nrg.xdat.security.XDATUser.UserNotFoundException;
import org.nrg.xft.XFT;

public class Authenticator {
	   public XDATUser authenticate(Credentials cred) throws UserNotFoundException,PasswordAuthenticationException,EnabledException,ActivationException,Exception{
		   return new XDATUser(cred.username,cred.password);
	   }
	   
	   public boolean authenticate(XDATUser u, Credentials cred) throws PasswordAuthenticationException,EnabledException,ActivationException,Exception{
		   return u.login(cred.password);
	   }
	   private final static String AUTH_CLASS_NAME="AUTHENTICATION_CLASS";
	   private static String AUTH_CLASS=null;
	   public synchronized static String RetrieveAuthenticatorClassName(){
		   if(AUTH_CLASS==null){
			   File AUTH_PROPS=new File(XFT.GetConfDir(),"authentication.properties");
				if(!AUTH_PROPS.exists()){
					System.out.println("No authentication.properties file found in conf directory. Skipping enhanced authentication method.");
					AUTH_CLASS="org.nrg.xdat.security.Authenticator";
				}else{
					try {
						InputStream inputs = new FileInputStream(AUTH_PROPS);
						 Properties properties = new Properties();
						 properties.load(inputs);
						 
						 if(properties.containsKey(AUTH_CLASS_NAME)){
							 AUTH_CLASS=properties.getProperty(AUTH_CLASS_NAME);
						 }else{
							AUTH_CLASS="org.nrg.xdat.security.Authenticator";
						}
					}  catch (IOException e) {
						AUTH_CLASS="org.nrg.xdat.security.Authenticator";
					}
				}
		   }
		   
		   return AUTH_CLASS;
	   }
	   
	   public static Authenticator CreateAuthenticator() throws ClassNotFoundException,IllegalAccessException,InstantiationException{
		   Class authClass =Class.forName(RetrieveAuthenticatorClassName());
		   return (Authenticator)authClass.newInstance();
	   }
	   
	   public static XDATUser Authenticate(Credentials cred) throws UserNotFoundException,PasswordAuthenticationException,EnabledException,ActivationException,Exception{
		   return CreateAuthenticator().authenticate(cred);
	   }
	   
	   public static boolean Authenticate(XDATUser u, Credentials cred) throws PasswordAuthenticationException,EnabledException,ActivationException,Exception{
		   return CreateAuthenticator().authenticate(u, cred);
	   }
	   
	   public static class Credentials {
		   String username=null;
		   String password=null;
		   
		   public HashMap OTHER=new HashMap();
		   
		   public Credentials(String u, String p){
			   username=u;
			   password=p;
		   }

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
		   
		   
	   }
}
