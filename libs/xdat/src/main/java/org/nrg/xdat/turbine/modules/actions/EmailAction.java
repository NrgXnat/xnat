/*
 * core: org.nrg.xdat.turbine.modules.actions.EmailAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFT;

import javax.mail.MessagingException;

/**
 * @author Tim
 */

public class EmailAction extends SecureAction {

	static Logger logger = Logger.getLogger(EmailAction.class);

	protected String toAddress = "", ccAddress = "", bccAddress = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache
	 * .turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void doPerform(RunData data, Context context) throws Exception {
		preserveVariables(data, context);
		execute(data, context);
	}

	public void execute(RunData data, Context context) throws Exception {
		setAddresses(data, context);
		sendMessage(data, context);
	}

	public void sendMessage(RunData data, Context context) {

		if(XDAT.getNotificationsPreferences().getSmtpEnabled()){
			if (!StringUtils.isBlank(toAddress) || !StringUtils.isBlank(ccAddress) || !StringUtils.isBlank(bccAddress)) {
				if (XDAT.getNotificationsPreferences().getCopyAdminOnPageEmails()) {
					if (StringUtils.isBlank(bccAddress)) {
						bccAddress = XDAT.getSiteConfigPreferences().getAdminEmail();
					} else {
						bccAddress += ", " + XDAT.getSiteConfigPreferences().getAdminEmail();
					}
				}
				// Split each string on commas and whitespace.
				String[] tos = StringUtils.split(toAddress, ", ");
				String[] ccs = StringUtils.split(ccAddress, ", ");
				String[] bccs = StringUtils.split(bccAddress, ", ");
	
				try {
					XDAT.getMailService().sendHtmlMessage(TurbineUtils.getUser(data).getEmail(), tos, ccs, bccs, getSubject(data, context), getMessage(data, context));
					data.setMessage("Message sent.");
				} catch (MessagingException exception) {
					logger.error("Error sending email", exception);
					data.setMessage("Failure sending message, please contact the system administrator.");
				}
			}
		}else{
			data.setMessage("Failure sending message, SMTP disabled.");
		}
	}

	public void setAddresses(RunData data, Context context) throws Exception {

		for (int i = 1; i <= ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedInteger("RowCount",data,0)); i++) {

			String to = "to" + i;
			String cc = "cc" + i;
			String bcc = "bcc" + i;
			String emailId = "EMAILID_" + i;

			if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(to,data)) != null)
				toAddress += ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(emailId,data)) + ", ";

			if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(cc,data)) != null)
				ccAddress += ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(emailId,data)) + ", ";

			if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(bcc,data)) != null)
				bccAddress += ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter(emailId,data)) + ", ";
		}

		if (XFT.VERBOSE)
			System.out.println("\n " + data.getParameters().toString() + "\n");

		toAddress = toAddress.trim();
		ccAddress = ccAddress.trim() + TurbineUtils.getUser(data).getEmail() + ", ";
		bccAddress = bccAddress.trim();
	}

	public String getSubject(RunData data, Context context) {
        String returnVal = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("subject",data));
		return returnVal != null ? returnVal : "";
	}

	public String getMessage(RunData data, Context context) {
		return ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("message",data));
	}
}
