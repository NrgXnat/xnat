package org.nrg.xnat.event.listeners;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.event.entities.EventSpecificFields;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferenceEvent;
import org.nrg.xnat.security.alias.ClearExpiredAliasTokens;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import static reactor.bus.selector.Selectors.type;

@Service
public class SiteConfigPreferenceHandler implements Consumer<Event<SiteConfigPreferenceEvent>> {

	@Inject public SiteConfigPreferenceHandler( EventBus eventBus ){
		eventBus.on(type(SiteConfigPreferenceEvent.class), this);
	}

	@Override
	public void accept(Event<SiteConfigPreferenceEvent> event) {
		final SiteConfigPreferenceEvent scpEvent = event.getData();
		handleEvent(scpEvent);
	}

    public void handleEvent(SiteConfigPreferenceEvent e) {
    	Set<EventSpecificFields> fields = e.getEventSpecificFields();
		if(fields!=null) {
			for (EventSpecificFields field : fields) {
				if (StringUtils.equals(field.getFieldName(), "aliasTokenTimeout")) {
					updateAliasTokenTimeout(e);	
				}
			}
		}
    }
	
	private void updateAliasTokenTimeout(SiteConfigPreferenceEvent e){
		try {
			//SchedulerConfig.removeClearExpiredAliasTokensTask();
			XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").getScheduledThreadPoolExecutor().setRemoveOnCancelPolicy(true);
			Iterator<Runnable> iter = XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").getScheduledThreadPoolExecutor().getQueue().iterator();

			for(ScheduledFuture temp: scheduledAliasTokenTimeouts){
				temp.cancel(false);
			}

			scheduledAliasTokenTimeouts.add(XDAT.getContextService().getBeansOfType(ThreadPoolTaskScheduler.class).get("taskScheduler").schedule(new ClearExpiredAliasTokens(_template),new Trigger() {
				@Override public Date nextExecutionTime(TriggerContext triggerContext) {
					Calendar nextExecutionTime =  new GregorianCalendar();
					Date lastActualExecutionTime = triggerContext.lastActualExecutionTime();
					nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
					long expirationInterval = XDAT.getSiteConfigPreferences().getAliasTokenTimeout();
					if(expirationInterval<120){//Check every minute if interval is 2 hours or less
						nextExecutionTime.add(Calendar.MINUTE, 1);
					}
					else if(expirationInterval<2880){//Check every hour if interval is 2 days or less
						nextExecutionTime.add(Calendar.HOUR, 1);
					}
					else{//Check every day
						nextExecutionTime.add(Calendar.DAY_OF_MONTH, 1);
					}
					return nextExecutionTime.getTime();
				}}));

		} catch (Exception e1) {
			_log.error("", e1);
		}	
	}

	@Autowired
	@Lazy
	private JdbcTemplate _template;

	private static final Log _log = LogFactory.getLog(SiteConfigPreferenceHandler.class);

	private ArrayList<ScheduledFuture> scheduledAliasTokenTimeouts = new ArrayList<ScheduledFuture>();
}
