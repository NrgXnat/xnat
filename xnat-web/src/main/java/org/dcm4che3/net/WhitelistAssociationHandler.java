package org.dcm4che3.net;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.net.pdu.AAssociateAC;
import org.dcm4che3.net.pdu.AAssociateRJ;
import org.dcm4che3.net.pdu.AAssociateRQ;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class WhitelistAssociationHandler extends AssociationHandler {
    private final Map<String, List<String>> aeWhitelists = new HashMap<>();
    private final Set<String> enabledWhitelistAEs = new HashSet<>();

    public void addWhitelist(String aeTitle, boolean enabled, List<String> whitelist) {
        if (enabled) {
            enabledWhitelistAEs.add(aeTitle);
            aeWhitelists.put(aeTitle, whitelist);
        }
    }

    @Override
    public AAssociateAC negotiate(Association as, AAssociateRQ rq) throws IOException {
        String callingAET = rq.getCallingAET();
        String aeTitle    = as.getApplicationEntity().getAETitle();
        String remoteIp   = as.getSocket().getInetAddress().getHostAddress();

        if (enabledWhitelistAEs.contains(aeTitle)) {
            List<String> whitelist = aeWhitelists.getOrDefault(aeTitle, Collections.emptyList());
            boolean allowed = matchesWhitelist(callingAET, remoteIp, whitelist);
            if (!allowed) {
                throw new AAssociateRJ(
                        AAssociateRJ.RESULT_REJECTED_PERMANENT,
                        AAssociateRJ.SOURCE_SERVICE_USER,
                        AAssociateRJ.REASON_CALLING_AET_NOT_RECOGNIZED
                );
            }
        }
        return super.negotiate(as, rq);
    }

    private boolean matchesWhitelist(String ae, String ip, List<String> whitelist) {
        for (String entry : whitelist) {
            String[] parts = entry.split("@");
            if (parts.length == 2) {
                if (parts[0].equals(ae) && ipMatches(parts[1], ip)) {
                    return true;
                }
            } else if (parts.length == 1) {
                if (ipMatches(parts[0], ip) || ae.equals(parts[0])) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean ipMatches(String pattern, String ip) {
        try {
            return new org.springframework.security.web.util.matcher.IpAddressMatcher(pattern).matches(ip);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
