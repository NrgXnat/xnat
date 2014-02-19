/*
 * org.nrg.xdat.security.ObfuscatedPasswordEncoder
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/11/13 3:34 PM
 */
package org.nrg.xdat.security;/*
 * org.nrg.xnat.helpers.prearchive.PrearcDatabase
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Created 11/19/13 3:29 PM
 */

import org.springframework.security.authentication.encoding.ShaPasswordEncoder;


import org.nrg.xdat.security.XDATUser;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

public class ObfuscatedPasswordEncoder extends ShaPasswordEncoder {

    public ObfuscatedPasswordEncoder(int strength) {
        super(strength);
    }

    public String encodePassword(String rawPass, Object salt) {
        String obfuscatedPass = obfuscatePassword(rawPass);
        return super.encodePassword(obfuscatedPass,salt);
    }

    private String obfuscatePassword(String stringToEncrypt) {
        int prime = 373;
        int g = 2;
        String ans = "";
        char[] as = stringToEncrypt.toCharArray();
        for (int i = 0; i < stringToEncrypt.length(); i++) {
            int encrypt_digit = g ^ (char) (as[i]) % prime;
            ans = ans + (char) (encrypt_digit);
        }
        return (ans);
    }
}
