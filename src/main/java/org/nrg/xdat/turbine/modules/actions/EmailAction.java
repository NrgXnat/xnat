//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved

/*

 * Created on Jun 23, 2005

 *

 */

package org.nrg.xdat.turbine.modules.actions;

import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFT;

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

	private void sendMessage(RunData data, Context context) {

		if (!StringUtils.isBlank(toAddress) || !StringUtils.isBlank(ccAddress) || !StringUtils.isBlank(bccAddress)) {
			if (AdminUtils.GetPageEmail()) {
				if (StringUtils.isBlank(bccAddress)) {
					bccAddress = AdminUtils.getAdminEmailId();
				} else {
					bccAddress += ", " + AdminUtils.getAdminEmailId();
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
	}

	public void setAddresses(RunData data, Context context) throws Exception {

		for (int i = 1; i <= data.getParameters().getInt("RowCount"); i++) {

			String to = "to" + i;
			String cc = "cc" + i;
			String bcc = "bcc" + i;
			String emailId = "EMAILID_" + i;

			if (data.getParameters().get(to) != null)
				toAddress += data.getParameters().getString(emailId) + ", ";

			if (data.getParameters().get(cc) != null)
				ccAddress += data.getParameters().getString(emailId) + ", ";

			if (data.getParameters().get(bcc) != null)
				bccAddress += data.getParameters().getString(emailId) + ", ";
		}

		if (XFT.VERBOSE)
			System.out.println("\n " + data.getParameters().toString() + "\n");

		toAddress = toAddress.trim();
		ccAddress = ccAddress.trim() + TurbineUtils.getUser(data).getEmail() + ", ";
		bccAddress = bccAddress.trim();
	}

	public String getSubject(RunData data, Context context) {
		return data.getParameters().getString("subject");
	}

	public String getMessage(RunData data, Context context) {
		return data.getParameters().getString("message");
	}
}
