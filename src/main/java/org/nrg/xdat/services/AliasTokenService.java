/*
 * core: org.nrg.xdat.services.AliasTokenService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * AliasTokenService
 * (C) 2016 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 */
package org.nrg.xdat.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xft.security.UserI;

import java.util.List;
import java.util.Set;

public interface AliasTokenService extends BaseHibernateService<AliasToken> {
    /**
     * Finds all active tokens for a particular user.
     * @param xdatUserId    The user ID from the XdatUser table.
     * @return An list of the {@link AliasToken alias tokens} issued to the indicated user.
     */
    List<AliasToken> findTokensForUser(String xdatUserId);
    /**
     * Finds and deactivates all active tokens for a particular user.
     * @param xdatUserId    The user ID from the XdatUser table.
     */
    void deactivateAllTokensForUser(String xdatUserId);
    /**
     * Issues a token to the user with the indicated name.
     *
     * @param xdatUserId    The user ID from the XdatUser table.
     * @return An {@link AliasToken} issued to the indicated user.
     * @throws Exception When something goes wrong.
     */
    AliasToken issueTokenForUser(String xdatUserId) throws Exception;
    /**
     * Issues a token to the indicated user. This calls the {@link #issueTokenForUser(UserI, boolean)} version of
     * this method, passing <b>false</b> by default for the boolean parameter.
     *
     * @param xdatUser    The user requesting a token.
     * @return An {@link AliasToken} issued to the indicated user.
     */
    AliasToken issueTokenForUser(UserI xdatUser);
    /**
     * Issues a token to the indicated user. The <b>isSingleUse</b> parameter indicates whether the issued token should
     * be disposed of when the token is used.
     *
     * @param xdatUser    The user requesting a token.
     * @param isSingleUse Indicates whether the token should be disposed of once the token is used once.
     * @return An {@link AliasToken} issued to the indicated user.
     */
    AliasToken issueTokenForUser(UserI xdatUser, boolean isSingleUse);
    /**
     * Issues a token to the indicated user. The <b>validIPAddresses</b> parameter indicates which originating IPs
     * should be permitted to offer the returned alias tokens. Note that there is nothing in the issued token that
     * indicates the acceptable IP addresses.
     *
     * @param xdatUser    The user requesting a token.
     * @param validIPAddresses    The list of IP addresses from which the alias token will be accepted.
     * @return An {@link AliasToken} issued to the indicated user.
     */
    AliasToken issueTokenForUser(UserI xdatUser, Set<String> validIPAddresses);
    /**
     * Issues a token to the indicated user.  The <b>isSingleUse</b> parameter indicates whether the issued token should
     * be disposed of when the token is used.The <b>validIPAddresses</b> parameter indicates which originating IPs
     * should be permitted to offer the returned alias tokens. Note that there is nothing in the issued token that
     * indicates the acceptable IP addresses.
     *
     * @param xdatUser    The user requesting a token.
     * @param isSingleUse Indicates whether the token should be disposed of once the token is used once.
     * @param validIPAddresses    The list of IP addresses from which the alias token will be accepted.
     * @return An {@link AliasToken} issued to the indicated user.
     */
    AliasToken issueTokenForUser(UserI xdatUser, boolean isSingleUse, Set<String> validIPAddresses);

    /**
     * Locates and returns the token indicated by the alias string. The returned token should not be considered fully
     * validated until the {@link AliasToken#getSecret() token secret} and {@link AliasToken#getValidIPAddresses() IP
     * addresses} have also been checked and validated against the requesting client.
     *
     * @param alias    The alias for the requested token.
     * @return The token matching the indicated alias if one exists; otherwise this returns null.
     */
    AliasToken locateToken(String alias);

    /**
     * Checks whether a token exists with the indicated alias and secret and no IP address restrictions. If so, this
     * method returns the {@link UserI#getLogin() corresponding XDAT user login ID}. Otherwise, this method returns
     * <b>null</b>.
     *
     * @param alias     The alias to check.
     * @param secret    The secret to validate the indicated alias.
     * @return The {@link UserI#getLogin() XDAT user login ID} of the matching token exists, or <b>null</b> if not.
     */
    String validateToken(String alias, String secret);

    /**
     * Checks whether a token exists with the indicated alias and secret and an IP address matching one of the defined
     * IP addresses (if there are no IP address restrictions in the token, any given IP address will match). If so, this
     * method returns the {@link UserI#getLogin() corresponding XDAT user login ID}. Otherwise, this method returns
     * <b>null</b>.
     *
     * @param alias     The alias to check.
     * @param secret    The secret to validate the indicated alias.
     * @param address   The IP address to validate.
     * @return The {@link UserI#getLogin() XDAT user login ID} of the matching token exists, or <b>null</b> if not.
     */
    String validateToken(String alias, String secret, String address);

    /**
     * Invalidates the token with the given alias. No supporting validation is required for this operation.
     *
     * @param alias    The alias of the token to be invalidated.
     */
    void invalidateToken(String alias);

    void invalidateToken(final AliasToken token);

    void invalidateExpiredTokens(String interval);
}
