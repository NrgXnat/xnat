package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.notify.renderers.ChannelRenderer;
import org.nrg.notify.renderers.NrgMailChannelRenderer;
import org.nrg.xdat.XDAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class MailHandlerMethod extends AbstractSiteConfigNotificationsPreferenceHandlerMethod {
    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateMail();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if(PREFERENCES.contains(preference)){
            updateMail();
        }
    }

    private void updateMail(){
		try {
            ((NrgMailChannelRenderer)_mailRenderer).setFromAddress(XDAT.getSiteConfigPreferences().getAdminEmail());
            ((NrgMailChannelRenderer)_mailRenderer).setSubjectPrefix(XDAT.getNotificationsPreferences().getEmailPrefix());

		} catch (Exception e1) {
			_log.error("", e1);
		}
	}

    private static final Logger       _log        = LoggerFactory.getLogger(MailHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("emailPrefix", "adminEmail"));

    @Inject
    private ChannelRenderer _mailRenderer;

}
