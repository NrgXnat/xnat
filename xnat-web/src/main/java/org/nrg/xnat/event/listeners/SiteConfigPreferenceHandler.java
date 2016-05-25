package org.nrg.xnat.event.listeners;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.event.entities.EventSpecificFields;
import org.nrg.prefs.events.AbstractPreferenceHandler;
import org.nrg.prefs.events.PreferenceHandlerMethod;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferenceEvent;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.DisableInactiveUsers;
import org.nrg.xnat.security.ResetFailedLogins;
import org.nrg.xnat.security.alias.ClearExpiredAliasTokens;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Service
public class SiteConfigPreferenceHandler extends AbstractPreferenceHandler<SiteConfigPreferenceEvent> {

	@Inject
	public SiteConfigPreferenceHandler(final EventBus eventBus){
		super(SiteConfigPreferences.SITE_CONFIG_TOOL_ID, eventBus);
	}

//		if (StringUtils.equals(field.getFieldName(), "aliasTokenTimeout")) {
//			updateAliasTokenTimeout(e);
//		}
//		else if (StringUtils.equals(field.getFieldName(), "aliasTokenTimeoutSchedule")) {
//			updateAliasTokenTimeout(e);
//		}
//		else if (StringUtils.equals(field.getFieldName(), "inactivityBeforeLockout")) {
//			updateInactivityBeforeLockout(e);
//		}
//		else if (StringUtils.equals(field.getFieldName(), "inactivityBeforeLockoutSchedule")) {
//			updateInactivityBeforeLockout(e);
//		}
//		else if (StringUtils.equals(field.getFieldName(), "maxFailedLoginsLockoutDuration")) {
//			updateResetFailedLogins(e);
//		}
//		else if (StringUtils.equals(field.getFieldName(), "resetFailedLoginsSchedule")) {
//			updateResetFailedLogins(e);
//		}
//
//	}
//
//	private void updateAliasTokenTimeout(SiteConfigPreferenceEvent e){
//		try {
//			XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").getScheduledThreadPoolExecutor().setRemoveOnCancelPolicy(true);
//			Iterator<Runnable> iter = XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").getScheduledThreadPoolExecutor().getQueue().iterator();
//
//			for(ScheduledFuture temp: scheduledAliasTokenTimeouts){
//				temp.cancel(false);
//			}
//
//			scheduledAliasTokenTimeouts.add(XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").schedule(new ClearExpiredAliasTokens(_template),new CronTrigger(XDAT.getSiteConfigPreferences().getAliasTokenTimeoutSchedule())));
//
//		} catch (Exception e1) {
//			_log.error("", e1);
//		}
//	}
//
//	private void updateInactivityBeforeLockout(SiteConfigPreferenceEvent e){
//		try {
//			XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").getScheduledThreadPoolExecutor().setRemoveOnCancelPolicy(true);
//			Iterator<Runnable> iter = XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").getScheduledThreadPoolExecutor().getQueue().iterator();
//
//			for(ScheduledFuture temp: scheduledInactivityBeforeLockout){
//				temp.cancel(false);
//			}
//
//			scheduledInactivityBeforeLockout.add(XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").schedule(new DisableInactiveUsers((new Long(SiteConfigPreferences.convertPGIntervalToSeconds(XDAT.getSiteConfigPreferences().getInactivityBeforeLockout()))).intValue(),(new Long(SiteConfigPreferences.convertPGIntervalToSeconds(XDAT.getSiteConfigPreferences().getMaxFailedLoginsLockoutDuration()))).intValue()),new CronTrigger(XDAT.getSiteConfigPreferences().getInactivityBeforeLockoutSchedule())));
//
//		} catch (Exception e1) {
//			_log.error("", e1);
//		}
//	}
//
//	private void updateResetFailedLogins(SiteConfigPreferenceEvent e){
//		try {
//			XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").getScheduledThreadPoolExecutor().setRemoveOnCancelPolicy(true);
//			Iterator<Runnable> iter = XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").getScheduledThreadPoolExecutor().getQueue().iterator();
//
//			for(ScheduledFuture temp: scheduledResetFailedLogins){
//				temp.cancel(false);
//			}
//
//			scheduledResetFailedLogins.add(XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").schedule(new ResetFailedLogins(_template,XDAT.getSiteConfigPreferences().getMaxFailedLoginsLockoutDuration()),new CronTrigger(XDAT.getSiteConfigPreferences().getResetFailedLoginsSchedule())));
//
//		} catch (Exception e1) {
//			_log.error("", e1);
//		}
//	}

	@Autowired
	@Lazy
	private JdbcTemplate _template;

	private static final Log _log = LogFactory.getLog(SiteConfigPreferenceHandler.class);
	private ArrayList<ScheduledFuture> scheduledInactivityBeforeLockout = new ArrayList<ScheduledFuture>();

	private ArrayList<ScheduledFuture> scheduledResetFailedLogins = new ArrayList<ScheduledFuture>();
}
