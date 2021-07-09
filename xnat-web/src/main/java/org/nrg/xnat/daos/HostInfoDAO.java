/*
 * web: org.nrg.xnat.daos.HostInfoDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.daos;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.entities.HostInfo;
import org.springframework.stereotype.Repository;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Repository
@Slf4j
public class HostInfoDAO extends AbstractHibernateDAO<HostInfo> {
    private final String _hostName;

    public HostInfoDAO() {
        _hostName = getDiscoveredHostName();
    }

    /**
     * Gets the host number for the specified host name.
     *
     * If there's no existing {@link HostInfo} entry for the specified host name, this method creates a new entry for the
     * host name if the <b>setValue</b> parameter is set to true and returns the host number for that new entry.
     * Otherwise the returned host number is just an empty string.
     *
     * @param hostName The host name for which you want to retrieve the host number.
     * @param setValue Indicates whether the host name should be saved and the ID of the new entry returned.
     *
     * @return The host number for the specified host name.
     */
    public String getHostNumber(final String hostName, final boolean setValue) {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("hostName", hostName));
        final List<HostInfo> infos = GenericUtils.convertToTypedList(criteria.list(), HostInfo.class);
        if (infos.isEmpty()) {
            if (!setValue) {
                return "";
            }
            create(new HostInfo(hostName));
            return getHostNumber(hostName, false);
        }
        return String.format("%02d", infos.get(0).getId());
    }

    /**
     * Gets the host number for the specified host name. This method calls {@link #getHostNumber(String, boolean)} with
     * the {@code setValue} parameter set to {@code true}.
     *
     * @param hostName The host name for which you want to retrieve the host number.
     *
     * @return The host number for the specified host name.
     */
    public String getHostNumber(final String hostName) {
        return getHostNumber(hostName, true);
    }

    /**
     * Gets the host number for the current host based on the value returned by the call:
     *
     * <pre>{@code InetAddress.getLocalHost().getHostName()}</pre>
     *
     * If there's no existing {@link HostInfo} entry for the specified host name, this method creates a new entry for the
     * host name if the <b>setValue</b> parameter is set to true and returns the host number for that new entry.
     * Otherwise the returned host number is just an empty string.
     *
     * @return The host number for the current host.
     */
    public String getHostNumber() {
        return getHostNumber(_hostName, true);
    }

    private static String getDiscoveredHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("An error occurred trying to get the host name for this server", e);
            return "0";
        }
    }
}
