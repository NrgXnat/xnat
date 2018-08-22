package org.nrg.xnat.event.listeners.methods;

import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.methods.AbstractXftItemEventHandlerMethod;
import org.nrg.xft.event.methods.XftItemEventCriteria;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Clears the XFT cache when an item is updated.
 */
@Component
@Slf4j
public class XnatProjectHandlerMethod extends AbstractXftItemEventHandlerMethod {
    public XnatProjectHandlerMethod(final NamedParameterJdbcTemplate template) {
        // super(XftItemEventCriteria.getXsiTypeCriteria(XnatProjectdata.SCHEMA_ELEMENT_NAME));
        // For now this is a no-op criteria, because I don't think this handler actually does anything worthwhile.
        super(XftItemEventCriteria.builder().predicate(new Predicate<XftItemEventI>() {
            @Override
            public boolean apply(@Nullable final XftItemEventI input) {
                return false;
            }
        }).build());
        _template = template;
    }

    @Override
    protected boolean handleEventImpl(final XftItemEventI event) {
        try {
            final UserI  guest     = Users.getGuest(true);
            final String projectId = event.getId();
            final String access    = Permissions.getProjectAccess(_template, projectId);

            log.info("Setting guest access to project {} to {}", projectId, access);

            setGuestProjectAccess(guest, projectId, access);
        } catch (final UserInitException e) {
            // If this occurs, don't make a big fuss: it probably means that the system's starting up. If that's NOT
            // what it means, plenty more parts of the system will be complaining so we don't need to add to it.
            log.debug("Got a UserInitException while refreshing guest user. This probably just means that the system is still initializing: {}", e.getMessage());
        } catch (final Exception e) {
            log.error("An error occurred trying to refresh guest user.", e);
        }
        return true;
    }

    private void setGuestProjectAccess(final UserI guest, final String projectId, final String access) throws Exception {
        if (StringUtils.equals(access, "private")) {
            return;
        }
        final boolean    activate = StringUtils.equals(access, "public");
        final EventMetaI event    = EventUtils.ADMIN_EVENT(guest);
        for (final ElementSecurity securedElement : ElementSecurity.GetSecureElements()) {
            if (securedElement != null && securedElement.hasField(securedElement.getElementName() + "/project") && securedElement.hasField(securedElement.getElementName() + "/sharing/share/project")) {
                Permissions.setPermissions(guest, Users.getAdminUser(), securedElement.getElementName(), securedElement.getElementName() + "/project", projectId, false, true, false, false, activate, true, event);
                Permissions.setPermissions(guest, Users.getAdminUser(), securedElement.getElementName(), securedElement.getElementName() + "/sharing/share/project", projectId, false, true, false, false, activate, true, event);
            }
        }
    }

    private final NamedParameterJdbcTemplate _template;
}
