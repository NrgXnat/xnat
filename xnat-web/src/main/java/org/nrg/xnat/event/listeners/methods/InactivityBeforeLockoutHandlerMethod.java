package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.DisableInactiveUsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Component
public class InactivityBeforeLockoutHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateInactivityBeforeLockout();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if(PREFERENCES.contains(preference)){
            updateInactivityBeforeLockout();
        }
    }

	private void updateInactivityBeforeLockout(){
		try {
            _scheduler.getScheduledThreadPoolExecutor().setRemoveOnCancelPolicy(true);
			Iterator<Runnable> iter = _scheduler.getScheduledThreadPoolExecutor().getQueue().iterator();

			for(ScheduledFuture temp: scheduledInactivityBeforeLockout){
				temp.cancel(false);
			}

			scheduledInactivityBeforeLockout.add(_scheduler.schedule(new DisableInactiveUsers((new Long(SiteConfigPreferences.convertPGIntervalToSeconds(XDAT.getSiteConfigPreferences().getInactivityBeforeLockout()))).intValue(),(new Long(SiteConfigPreferences.convertPGIntervalToSeconds(XDAT.getSiteConfigPreferences().getMaxFailedLoginsLockoutDuration()))).intValue()),new CronTrigger(XDAT.getSiteConfigPreferences().getInactivityBeforeLockoutSchedule())));

		} catch (Exception e1) {
			_log.error("", e1);
		}
	}

    private static final Logger       _log        = LoggerFactory.getLogger(InactivityBeforeLockoutHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("inactivityBeforeLockout", "inactivityBeforeLockoutSchedule"));

    @Autowired
    @Lazy
    private JdbcTemplate _template;

    private              ArrayList<ScheduledFuture> scheduledInactivityBeforeLockout = new ArrayList<>();

    @Autowired
    @Qualifier("taskScheduler")
    private ThreadPoolTaskScheduler _scheduler;
}
