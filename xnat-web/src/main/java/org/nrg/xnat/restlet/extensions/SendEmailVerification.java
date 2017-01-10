/*
 * web: org.nrg.xnat.restlet.extensions.SendEmailVerification
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.extensions;

import org.nrg.mail.services.EmailRequestLogService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import java.util.Date;
import java.util.List;

@XnatRestlet(value = {"/services/sendEmailVerification"}, secure = false)
public class SendEmailVerification extends Resource {

    private final EmailRequestLogService requests = XDAT.getContextService().getBean(EmailRequestLogService.class);

    public SendEmailVerification(Context context, Request request, Response response) throws Exception{
        super(context, request, response);
        this.getVariants().add(new Variant(MediaType.ALL));
    }
    
    @Override public boolean allowDelete() { return false; }
    @Override public boolean allowPut()    { return false; }
    @Override public boolean allowGet()    { return false; }
    @Override public boolean allowPost()   { return true;  }
    
    @Override public void handlePost(){
       String email = SecureResource.getQueryVariable("email", getRequest());
       if((email != null) && (!email.equals(""))){ 
          try{
             if (requests.isEmailBlocked(email)){ 
                // If the user has exceeded the maximum number of requests.
                throw new ExceededRequestsException("Exceeded maximum number of email requests."); 
             }
             
             // Send email and log request.
             AdminUtils.sendNewUserVerificationEmail(getXDATUser(email));
             requests.logEmailRequest(email, new Date ());
          }
          catch(ExceededRequestsException e){
             this.getResponse().setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, e.getMessage());
          }
          catch(EmailNotFoundException e){
             this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
          }
          catch(Exception e) { 
             this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Unable to send Verification Email.");
          }
       }
    }

    private UserI getXDATUser(String email) throws Exception{
       List<? extends UserI> users=Users.getUsersByEmail(email);
       // If no user could be found throw EmailNotFoundException
       if(users.size()==0){ throw new EmailNotFoundException("No Such Email Exists."); }
       
       // Otherwise return the first user in the list.
       return users.get(0);
    }
    
    public class ExceededRequestsException extends Exception{
       public ExceededRequestsException(String msg){ super(msg); }
    }
    
    public class EmailNotFoundException extends Exception {
    	public EmailNotFoundException(String msg){ super(msg); }
    }
}
