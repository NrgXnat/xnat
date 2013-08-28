/**
 * AliasToken
 * (C) 2012 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 4/17/12 by rherri01
 */
package org.nrg.xdat.entities;


import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.util.SubnetUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "xdatCore")
public class AliasToken extends AbstractHibernateEntity {
    public AliasToken() {
        _alias = UUID.randomUUID().toString();
        _secret = new Date().getTime();
    }

    /**
     * The alias is the primary reference to the token instance.
     * @return The alias for this authentication token.
     */
    @Column(unique = true, nullable = false)
    public String getAlias() {
        return _alias;
    }

    /**
     * Sets the alias for the token. This should not be called after the token has been created.
     * @param alias    The alias to set for the token.
     */
    public void setAlias(final String alias) {
        _alias = alias;
    }

    /**
     * Gets the token secret.
     * @return A value representing the token secret.
     */
    public long getSecret() {
        return _secret;
    }

    /**
     * Sets the token secret.
     * @param secret    A value representing the token secret.
     */
    public void setSecret(final long secret) {
        _secret = secret;
    }

    /**
     * Indicates whether this token is for a single use (e.g. change or reset password) or
     * repeated use.
     * @return Whether this token is for a single use.
     */
    public boolean isSingleUse() {
        return _isSingleUse;
    }

    /**
     * Sets whether this token is for a single use only.
     * @param singleUse    Whether the token is for a single use.
     */
    public void setSingleUse(final boolean singleUse) {
        _isSingleUse = singleUse;
    }

    /**
     * The username of the XDAT user account for whom the token was issued.
     * @return The username of the XDAT user account for whom the token was issued.
     */
    public String getXdatUserId() {
        return _xdatUserId;
    }

    /**
     * Sets the username of the XDAT user account for whom the token was issued.
     * @param xdatUserId    The username of the XDAT user account for whom the token was issued.
     */
    public void setXdatUserId(String xdatUserId) {
        _xdatUserId = xdatUserId;
    }

    /**
     * Returns a list of the IP addresses and address ranges from which requests using this
     * authentication token can originate.
     * @return A list of the valid originating IP addresses and address ranges.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(nullable = true)
    public Set<String> getValidIPAddresses() {
        return _validIPAddresses;
    }

    /**
     * Sets the list of the IP addresses and address ranges from which requests using this
     * authentication token can originate.
     */
    public void setValidIPAddresses(Set<String> validIPAddresses) {
        _validIPAddresses = validIPAddresses;
    }

    /**
     * Tests whether the specified address matches one of the specified IP addresses.
     * @param address    The address to test.
     * @return <b>true</b> if the submitted address matches one of the plain IPs or subnet masks, <b>false</b> otherwise.
     */
    @Transient
    public boolean isValidIPAddress(String address) {
        // If there are no valid IPs, then all IPs are valid.
        if (_validIPAddresses == null || _validIPAddresses.size() == 0) {
            return true;
        }
        // If there are valid IP restrictions but no address was specified, then it's not a valid IP.
        if (StringUtils.isBlank(address)) {
            return false;
        }

        if (_validPlainIPAddresses == null) {
            initializeAddressLists();
        }

        // These both should be initialized after first pass through initializeAddressLists().
        assert _validPlainIPAddresses != null;
        assert _validSubnets != null;

        // If we have both a list of valid IPs and an originating IP, just see if that IP is in the list.
        if (_validPlainIPAddresses.contains(address)) {
            if (_log.isDebugEnabled()) {
                _log.debug("Found valid IP address: " + address);
            }
            return true;
        }

        // If we have both a list of valid subnet masks and an originating IP, just see if that IP matches one of the subnets.
        for (SubnetUtils.SubnetInfo subnet : _validSubnets) {
            if (subnet.isInRange(address)) {
                if (_log.isDebugEnabled()) {
                    _log.debug("Found valid IP address: " + address + " on subnet specifier: " + subnet.getAddress());
                }
                return true;
            }
        }

        // If we went through all the IPs and subnets and couldn't find a valid IP, then we fail.
        if (_log.isInfoEnabled()) {
            _log.info("Found invalid IP address: " + address);
        }

        return false;
    }

    /**
     * Indicates whether the candidate string matches the format used by the token for aliases.
     * Note that this provides no indication as to whether candidate string is an actual alias
     * or is still a valid alias in the system.
     *
     * @param candidate    The string to test for format compatibility.
     * @return <b>true</b> if the string matches the alias format, <b>false</b> otherwise.
     */
    public static boolean isAliasFormat(String candidate) {
        return PATTERN_UUID.matcher(candidate).matches();
    }

    private void initializeAddressLists() {
        if (_validIPAddresses == null) {
            return;
        }
        _validPlainIPAddresses = Lists.newArrayList();
        _validSubnets = Lists.newArrayList();

        for (String address : _validIPAddresses) {
            if (PATTERN_IP_PLAIN.matcher(address).matches()) {
                _validPlainIPAddresses.add(address);
            } else if (PATTERN_IP_MASK.matcher(address).matches()) {
                _validSubnets.add(new SubnetUtils(address).getInfo());
            } else {
                _log.warn("Found specified IP address that doesn't match patterns for IP or IP subnet mask: "+ address);
            }
        }
    }

    private static final Log _log = LogFactory.getLog(AliasToken.class);
    private static final Pattern PATTERN_UUID = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final Pattern PATTERN_IP_PLAIN = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final Pattern PATTERN_IP_MASK = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}/\\d{1,2}\\b");
    private String _alias;
    private long _secret;
    private boolean _isSingleUse;
    private String _xdatUserId;
    private Set<String> _validIPAddresses;
    private List<String> _validPlainIPAddresses;
    private List<SubnetUtils.SubnetInfo> _validSubnets;
}
