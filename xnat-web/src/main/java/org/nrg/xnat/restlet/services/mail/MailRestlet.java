/*
 * web: org.nrg.xnat.restlet.services.mail.MailRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.services.mail;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.action.ClientException;
import org.nrg.mail.api.MailMessage;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailRestlet extends SecureResource {
    private static final String PARAM_BCC     = "bcc";
    private static final String PARAM_CC      = "cc";
    private static final String PARAM_TO      = "to";
    private static final String PARAM_HTML    = "html";
    private static final String PARAM_SUBJECT = "subject";
    private static final String PARAM_TEXT    = "text";

    public MailRestlet(Context context, Request request, Response response) {
        super(context, request, response);
        this.getVariants().add(new Variant(MediaType.ALL));
        _log.debug("Instantiated MailRestlet instance");
    }

    @Override
    public boolean allowGet() {
        return false;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void handlePost() {
        // Process incoming parameters
        try {
            Representation entity = this.getRequest().getEntity();

            if (!extractParameters(entity)) {
                _log.warn("I was unable to properly parse an incoming request, check the logs for more information, returning: " + Status.CLIENT_ERROR_BAD_REQUEST);
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }

            MailMessage message = new MailMessage();

            // When receiving email send requests through the REST service, the from address is always the admin, with the mail sent on behalf of the validating user.
            message.setFrom(XDAT.getSiteConfigPreferences().getAdminEmail());
            message.setOnBehalfOf(getUser().getEmail());

            // Handle all the addresses.
            String[] tos = getAddresses(PARAM_TO);
            if (tos.length == 0) {
                throw new ClientException("No valid user IDs were provided for the to address.");
            }
            message.setTos(tos);
            String[] ccs = getAddresses(PARAM_CC);
            if (!(ccs.length == 0)) {
                message.setCcs(ccs);
            }
            String[] bccs = getAddresses(PARAM_BCC);
            if (!(bccs.length == 0)) {
                message.setBccs(bccs);
            }

            // Set the message guts.
            message.setSubject(getSubject());
            String text = getText();
            if (!StringUtils.isBlank(text)) {
                message.setText(text);
            }
            String html = getHtml();
            if (!StringUtils.isBlank(html)) {
                message.setHtml(html);
            }

            // Now the optional stuff...
            if (_files != null && _files.size() > 0) {
                message.setAttachments(_files);
            }
            final Map<String, List<String>> headers = getHeaders();
            if (headers.size() > 0) {
                message.setHeaders(headers);
            }

            // Finally send the mail.
            XDAT.getMailService().sendMessage(message);
        } catch (ClientException exception) {
            respondToException(exception, (exception.status != null) ? exception.status : Status.CLIENT_ERROR_BAD_REQUEST);
        } catch (Exception exception) {
            respondToException(exception, Status.SERVER_ERROR_INTERNAL);
        }

        if (_issues.size() > 0) {
            returnString("OK with warnings, see log.", Status.SUCCESS_OK);
            StringBuilder issues = new StringBuilder("Found a number of issues sending email:\n\n");
            for (String issue : _issues) {
                issues.append(String.format(FORMAT_ISSUE, issue));
            }
            _log.warn(issues);
        } else {
            returnString("OK", Status.SUCCESS_OK);
        }
    }

    private boolean extractParameters(Representation entity) throws Exception {
        MediaType mediaType = entity.getMediaType();

        if (MediaType.APPLICATION_WWW_FORM.equals(mediaType, true)) {
            if (RequestUtil.isMultiPartFormData(entity)) {
                loadBodyVariables();
                loadQueryVariables();
            } else {
                loadQueryVariables();
            }
        } else if (MediaType.MULTIPART_FORM_DATA.equals(mediaType, true)) {
            List<FileWriterWrapperI> files = getFileWritersAndLoadParams(entity, true);
            for (FileWriterWrapperI file : files) {
                String name = file.getName();
                if (_log.isDebugEnabled()) {
                    _log.debug("Found file: " + name);
                }
                // TODO: I don't like having to write to a temporary file here, but this is a restriction coming from JavaMail where attachments may need to be opened multiple times.
                File temp = File.createTempFile("xnat", "mailcache");
                file.write(temp);
                _files.put(name, temp);
            }
        }

        return validateParameters();
    }

    @Override
    public void handleParam(final String key, final Object value) throws ClientException {
        if (!_parameters.containsKey(key)) {
            if (_log.isDebugEnabled()) {
                _log.debug(String.format("Creating new value list for parameter: %s", key));
            }
            _parameters.put(key, new ArrayList<String>());
        }

        if (_log.isDebugEnabled()) {
            _log.debug(String.format("Adding value for parameter %s: [%s]", key, value));
        }

        // For now assume that value is a string until we can support other types.
        _parameters.get(key).add((String) value);
    }

    private String[] getAddresses(String type) {
        List<String> addresses = new ArrayList<>();
        if (_parameters.containsKey(type)) {
            List<String> ids = _parameters.get(type);
            for (String id : ids) {
                try {
                    // Try to parse this as an integer. If it's a user ID, it'll parse and we'll use the get by ID function.
                    UserI requested = Users.getUser(Integer.parseInt(id));
                    if (requested == null) {
                        addIssue(String.format("The user ID %s was not found in the system and was not included on the email.", id));
                    } else {
                        addresses.add(requested.getEmail());
                    }
                } catch (NumberFormatException | UserNotFoundException exception) {
                    // If not an integer, we'll try it as an email address. It has to match an existing email address in the system!
                    List<? extends UserI> users = Users.getUsersByEmail(id);
                    if (users == null || users.size() == 0) {
                        addIssue(String.format("The user email %s was not found in the system and was not included on the email.", id));
                    } else {
                        addresses.add(id);
                    }
                } catch (UserInitException e) {
                    logger.error("An error occurred in user initialization", e);
                }
            }
        }
        return addresses.toArray(new String[addresses.size()]);
    }

    private Map<String, List<String>> getHeaders() {
        Map<String, List<String>> headers = new HashMap<>();
        for (String key : _parameters.keySet()) {
            if (key.startsWith("header:")) {
                List<String> value = _parameters.get(key);
                headers.put(key.substring("header:".length()), value);
            }
        }
        return headers;
    }

    private void addIssue(String issue) {
        _issues.add(issue);
    }

    private String getSubject() {
        return getParameter(PARAM_SUBJECT);
    }

    private String getText() {
        return getParameter(PARAM_TEXT);
    }

    private String getHtml() {
        return StringEscapeUtils.unescapeHtml4(getParameter(PARAM_HTML));
    }

    private String getParameter(String parameter) {
        return _parameters.containsKey(parameter) ? _parameters.get(parameter).get(0) : null;
    }

    private boolean validateParameters() {
        List<String> validationErrors = new ArrayList<>();

        if (!_parameters.containsKey(PARAM_TO)) {
            validationErrors.add("You must specify at least one recipient for your message.");
        }
        if (!_parameters.containsKey(PARAM_SUBJECT)) {
            validationErrors.add("Emails sent through this service must have a subject.");
        } else if (_parameters.get(PARAM_SUBJECT).size() > 1) {
            validationErrors.add("You can only provide a single value for the email subject.");
        }
        if (!_parameters.containsKey(PARAM_TEXT) && !_parameters.containsKey(PARAM_HTML)) {
            validationErrors.add("Emails sent through this service must have a message body in either the HTML or text parameters.");
        } else {
            if (_parameters.containsKey(PARAM_TEXT) && _parameters.get(PARAM_TEXT).size() > 1) {
                validationErrors.add("You can only provide a single value for the email text body.");
            }
            if (_parameters.containsKey(PARAM_HTML) && _parameters.get(PARAM_HTML).size() > 1) {
                validationErrors.add("You can only provide a single value for the email HTML body.");
            }
        }

        if (validationErrors.size() > 0) {
            String message = formatErrorMessage(validationErrors);
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, message);
            return false;
        }

        return true;
    }

    private String formatErrorMessage(List<String> errors) {
        StringBuilder message = new StringBuilder("The following problems were found with your submission:\n\n");
        for (String error : errors) {
            message.append(String.format(FORMAT_ISSUE, error));
        }
        return message.toString();
    }

    private static final String FORMAT_ISSUE = " * %s\n";
    private static final Log _log = LogFactory.getLog(MailRestlet.class);

    private Map<String, List<String>> _parameters = new HashMap<>();
    private List<String> _issues = new ArrayList<>();
    private Map<String, File> _files = new HashMap<>();
}
