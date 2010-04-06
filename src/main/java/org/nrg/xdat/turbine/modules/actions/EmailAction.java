//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved

/*

 * Created on Jun 23, 2005

 *

 */

package org.nrg.xdat.turbine.modules.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.email.EmailUtils;
import org.nrg.xft.utils.StringUtils;

/**
 *
 * @author Tim
 *
 *
 *
 */

public class EmailAction extends SecureAction {

	static Logger logger = Logger.getLogger(EmailAction.class);

	protected String toAddress = "", ccAddress = "", bccAddress = "";

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache.turbine.util.RunData,
	 *      org.apache.velocity.context.Context)
	 *
	 */

	public void doPerform(RunData data, Context context) throws Exception {

		preserveVariables(data, context);

		execute(data, context);

	}

	public void execute(RunData data, Context context) throws Exception

	{

		setAddresses(data, context);

		if (!toAddress.equals("") || !ccAddress.equals("")
				|| !bccAddress.equals("")) {

			sendMessage(data, context);

		}

	}

	public void sendMessage(RunData data, Context context) {

		if (!toAddress.equals("") || !ccAddress.equals("")
				|| !bccAddress.equals("")) {
			try {
				List<InternetAddress> tos=new ArrayList<InternetAddress>();
				List<InternetAddress> ccs=new ArrayList<InternetAddress>();
				List<InternetAddress> bccs=new ArrayList<InternetAddress>();

				if (!toAddress.equals("")) {

					ArrayList al = StringUtils
							.CommaDelimitedStringToArrayList(toAddress.substring(0,
									toAddress.lastIndexOf(",")));

					Iterator iter = al.iterator();

					while (iter.hasNext())

					{

						try {
							tos.add(new InternetAddress((String) iter.next()));
						} catch (AddressException e) {
							logger.error(e);
						}

					}

				}

				if (!ccAddress.equals("")) {

					ArrayList al = StringUtils
							.CommaDelimitedStringToArrayList(ccAddress.substring(0,
									ccAddress.lastIndexOf(",")));

					Iterator iter = al.iterator();

					while (iter.hasNext())

					{

						try {
							ccs.add(new InternetAddress((String) iter.next()));
						} catch (AddressException e) {
							logger.error(e);
						}

					}

				}

				if (!bccAddress.equals("")) {

					ArrayList al = StringUtils
							.CommaDelimitedStringToArrayList(bccAddress.substring(
									0, bccAddress.lastIndexOf(",")));

					Iterator iter = al.iterator();

					while (iter.hasNext())

					{

						try {
							bccs.add(new InternetAddress((String) iter.next()));
						} catch (AddressException e) {
							logger.error(e);
						}

					}

				}

				if (AdminUtils.GetPageEmail()) {

					try {
						bccs.add(new InternetAddress(AdminUtils.getAdminEmailId()));
					} catch (AddressException e) {
						logger.error(e);
					}

				}
				
				EmailUtils.sendEmail(tos, ccs, bccs, TurbineUtils.getUser(data).getEmail(), getSubject(data, context), getMessage(data, context));

				data.setMessage("Message sent.");
			} catch (EmailException e) {
				logger.error(e);
				
				data.setMessage("Error: " + e.getMessage());
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

		ccAddress = ccAddress.trim() + TurbineUtils.getUser(data).getEmail()
				+ ", ";

		bccAddress = bccAddress.trim();

	}

	public String getSubject(RunData data, Context context)

	{

		return data.getParameters().getString("subject");

	}

	public String getMessage(RunData data, Context context)

	{

		return data.getParameters().getString("message");

	}

}
