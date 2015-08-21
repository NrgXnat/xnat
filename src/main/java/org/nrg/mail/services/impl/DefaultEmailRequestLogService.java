/*
 * org.nrg.mail.services.impl.DefaultEmailRequestLogService
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/3/14 12:07 PM
 */
package org.nrg.mail.services.impl;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nrg.mail.services.EmailRequestLogService;
import org.springframework.stereotype.Service;

@Service
public class DefaultEmailRequestLogService implements EmailRequestLogService {

    private final HashMap<String, LinkedList<Date>> log = new HashMap<>();
    private final long BAN_DURATION = MILLISECONDS.convert(60, MINUTES); // 1 hour in milliseconds

    /**
     * Function stores the email request.
     *
     * @param email The email address.
     * @param time  The time the email was requested.
     */
    @Override
    public void logEmailRequest(final String email, final Date time) throws Exception {
        if (StringUtils.isBlank(email) || time == null) {
            throw new Exception("Unable to store email request. Email or Date is null.");
        }

        // If the key exists get it's list, else create a new list.
        LinkedList<Date> requests = (log.containsKey(email)) ? log.get(email) : new LinkedList<Date>();

        requests.addLast(time);
        log.put(email, requests);
    }

    /**
     * Function determines weather an email has been blocked from requesting
     * more emails.
     *
     * @param email    The email address.
     */
    @Override
    public boolean isEmailBlocked(String email) {
        //If the log doesn't contain the email then it's not blocked.
        if (!log.containsKey(email)) {
            return false;
        }

        // The email is blocked if the maximum number of requests have been made.
        return (log.get(email).size() >= 5);
    }

    /**
     * Function removes an email from the block list.
     *
     * @param e - the email we are interested in.
     */
    public void unblockEmail(String e) {
        if (log.containsKey(e)) {
            log.remove(e);
        }
    }

    /**
     * Function clears the log of email requests that have expired.
     * The request is expired if a certain amount of time (BAN_DURATION) has passed.
     */
    @Override
    public void clearLogs() {
        Date now = new Date();
        LinkedList<Date> times;
        Map.Entry<String, LinkedList<Date>> entry;
        Iterator<Map.Entry<String, LinkedList<Date>>> it = log.entrySet().iterator();
        while (it.hasNext()) {
            entry = it.next();
            times = entry.getValue();
            for (int i = times.size() - 1; i >= 0; i--) {
                // Determine if the request has expired.
                if (now.getTime() - times.get(i).getTime() >= BAN_DURATION) {
                    // Since the times are in order, we can assume the rest have also expired.
                    times.subList(0, i + 1).clear();
                    if (times.size() == 0) {
                        it.remove();   // Remove the email if there are no more times.
                    } else {
                        entry.setValue(times);
                    }
                    break;
                }
            }
        }
    }
}