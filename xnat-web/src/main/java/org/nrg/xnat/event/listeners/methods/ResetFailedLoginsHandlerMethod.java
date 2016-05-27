package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.security.ResetFailedLogins;
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
public class ResetFailedLoginsHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateResetFailedLogins();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if(PREFERENCES.contains(preference)){
            updateResetFailedLogins();
        }
    }

    private void updateResetFailedLogins(){
		try {
            _scheduler.getScheduledThreadPoolExecutor().setRemoveOnCancelPolicy(true);
            _scheduler.getScheduledThreadPoolExecutor().getQueue().iterator();

			for(ScheduledFuture temp: scheduledResetFailedLogins){
				temp.cancel(false);
			}

			scheduledResetFailedLogins.add(_scheduler.schedule(new ResetFailedLogins(_template,XDAT.getSiteConfigPreferences().getMaxFailedLoginsLockoutDuration()),new CronTrigger(XDAT.getSiteConfigPreferences().getResetFailedLoginsSchedule())));

		} catch (Exception e1) {
			_log.error("", e1);
		}
	}

    private static final Logger       _log        = LoggerFactory.getLogger(ResetFailedLoginsHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("maxFailedLoginsLockoutDuration", "resetFailedLoginsSchedule"));

    @Autowired
    @Lazy
    private JdbcTemplate _template;

    private              ArrayList<ScheduledFuture> scheduledResetFailedLogins = new ArrayList<>();

    @Autowired
    @Qualifier("taskScheduler")
    private ThreadPoolTaskScheduler _scheduler;
}
