/*
 * web: org.nrg.xnat.helpers.transactions.HTTPSessionStatusManagerQueue
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.transactions;

import org.nrg.framework.status.StatusMessage;
import org.nrg.xnat.status.StatusList;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

public class HTTPSessionStatusManagerQueue implements PersistentStatusQueueManagerI {
    public HTTPSessionStatusManagerQueue(final HttpSession session) {
        _session = session;
    }

    @Override
    public synchronized StatusList storeStatusQueue(final String id, final StatusList statusList) throws IllegalArgumentException {
        _session.setAttribute(TransactionUtils.buildTransactionID(id), statusList);
        return statusList;
    }

    @Override
    public synchronized StatusList retrieveStatusQueue(final String id) throws IllegalArgumentException {
        return (StatusList) _session.getAttribute(TransactionUtils.buildTransactionID(id));
    }

    @Override
    public synchronized List<StatusMessage> retrieveCopyOfStatusQueueMessages(String id) throws IllegalArgumentException {
        StatusList sl = (StatusList) _session.getAttribute(TransactionUtils.buildTransactionID(id));
        if (sl != null) {
            return new ArrayList<>(sl.getMessages());
        } else {
            return null;
        }
    }

    @Override
    public synchronized StatusList deleteStatusQueue(final String id) throws IllegalArgumentException {
        final StatusList statusList = retrieveStatusQueue(TransactionUtils.buildTransactionID(id));
        _session.removeAttribute(id);
        return statusList;
    }

    private final HttpSession _session;
}
