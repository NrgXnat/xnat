package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.helpers.prearchive.SessionXMLRebuilder;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Component
public class SessionXmlRebuilderHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateSessionXmlRebuilder();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if(PREFERENCES.contains(preference)){
            updateSessionXmlRebuilder();
        }
    }

	private void updateSessionXmlRebuilder(){
		try {
            _scheduler.getScheduledThreadPoolExecutor().setRemoveOnCancelPolicy(true);

			for(ScheduledFuture temp: scheduledXmlRebuilder){
				temp.cancel(false);
			}
            scheduledXmlRebuilder.clear();
			scheduledXmlRebuilder.add(_scheduler.schedule(new SessionXMLRebuilder(_provider, XDAT.getSiteConfigPreferences().getSessionXmlRebuilderInterval(), _jmsTemplate),new PeriodicTrigger(XDAT.getSiteConfigPreferences().getSessionXmlRebuilderRepeat())));

		} catch (Exception e1) {
			_log.error("", e1);
		}
	}

    private static final Logger       _log        = LoggerFactory.getLogger(SessionXmlRebuilderHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("sessionXmlRebuilderRepeat", "sessionXmlRebuilderInterval"));

    private              ArrayList<ScheduledFuture> scheduledXmlRebuilder = new ArrayList<>();

    @Autowired
    @Qualifier("taskScheduler")
    private ThreadPoolTaskScheduler _scheduler;

    @Inject
    private JmsTemplate _jmsTemplate;

    @Inject
    private XnatUserProvider _provider;
}
