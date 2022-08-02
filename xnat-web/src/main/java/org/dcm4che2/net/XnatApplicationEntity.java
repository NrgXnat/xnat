package org.dcm4che2.net;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class XnatApplicationEntity extends NetworkApplicationEntity {

    private boolean whitelistEnabled  = false;
    private List<String> whitelist    = new ArrayList<>();

    @Override
    AAssociateAC negotiate(Association a, AAssociateRQ rq) throws AAssociateRJ {
        if(whitelistEnabled && !isWhitelisted(a)){
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT, AAssociateRJ.SOURCE_SERVICE_USER, AAssociateRJ.REASON_CALLING_AET_NOT_RECOGNIZED);
        }
        return super.negotiate(a, rq);
    }

    public void setWhitelistEnabled(final boolean whitelistEnabled){
        this.whitelistEnabled = whitelistEnabled;
    }

    public void setWhitelist(final List<String> whitelist){
        this.whitelist.addAll(whitelist.stream().map(String::trim).filter(StringUtils::isNotEmpty).collect(Collectors.toSet()));
    }

    private boolean isWhitelisted(final Association as){
        final String remoteIp = as.getSocket().getInetAddress().getHostAddress();
        final String remoteAe = as.getRemoteAET();

        for(String whitelistedItems : whitelist){
            final List<String> whitelistedItem = Arrays.asList(whitelistedItems.split("@"));
            if(whitelistedItem.size() == 2){
                final String whitelistedAe = whitelistedItem.get(0);
                final String whitelistedIp = whitelistedItem.get(1);
                try {
                    if (remoteAe.equals(whitelistedAe) && new IpAddressMatcher(whitelistedIp).matches(remoteIp)) {
                        return true;
                    }
                }catch(IllegalArgumentException e){
                    log.error("Invalid ip address in the whitelist: {}. Ignoring ...", whitelistedIp);
                }
            }else if(whitelistedItem.size() == 1){
                try{
                    if(new IpAddressMatcher(whitelistedItem.get(0)).matches(remoteIp)){
                        return true;
                    }
                }catch(IllegalArgumentException e){
                    if(remoteAe.equals(whitelistedItem.get(0))){
                        return true;
                    }
                }
            }
        }
        log.debug("{}@{} did not match the whitelist. Refusing connection.", remoteAe, remoteIp);
        return false;
    }
}
