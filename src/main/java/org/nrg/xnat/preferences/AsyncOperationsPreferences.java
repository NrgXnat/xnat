package org.nrg.xnat.preferences;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XnatMixIn;
import org.nrg.framework.beans.ProxiedBeanMixIn;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;

@NrgPreferenceBean(toolId = AsyncOperationsPreferences.ASYNC_OPS_TOOL_ID,
                   toolName = "XNAT Async Operations Preferences",
                   description = "Manages preferences and settings for XNAT asynchronous services and workers.")
@XnatMixIn(ProxiedBeanMixIn.class)
@Slf4j
public class AsyncOperationsPreferences extends AbstractPreferenceBean {
    public static final String ASYNC_OPS_TOOL_ID = "asyncOps";

    public AsyncOperationsPreferences(final NrgPreferenceService preferenceService) {
        super(preferenceService);
    }

    @NrgPreference(defaultValue = "-1")
    public long getDefaultTimeout() {
        return getLongValue("defaultTimeout");
    }

    public void setDefaultTimeout(final long defaultTimeout) {
        try {
            setLongValue(defaultTimeout, "defaultTimeout");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name defaultTimeout: something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "10")
    public int getCorePoolSize() {
        return getIntegerValue("corePoolSize");
    }

    @SuppressWarnings("unused")
    public void setCorePoolSize(final int corePoolSize) {
        try {
            setIntegerValue(corePoolSize, "corePoolSize");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name corePoolSize: something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public boolean getAllowCoreThreadTimeOut() {
        return getBooleanValue("allowCoreThreadTimeOut");
    }

    @SuppressWarnings("unused")
    public void setAllowCoreThreadTimeOut(final boolean allowCoreThreadTimeOut) {
        try {
            setBooleanValue(allowCoreThreadTimeOut, "allowCoreThreadTimeOut");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name allowCoreThreadTimeOut: something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "2048")
    public int getMaxPoolSize() {
        return getIntegerValue("maxPoolSize");
    }

    @SuppressWarnings("unused")
    public void setMaxPoolSize(final int maxPoolSize) {
        try {
            setIntegerValue(maxPoolSize, "maxPoolSize");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name maxPoolSize: something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "60")
    public int getKeepAliveSeconds() {
        return getIntegerValue("keepAliveSeconds");
    }

    @SuppressWarnings("unused")
    public void setKeepAliveSeconds(final int keepAliveSeconds) {
        try {
            setIntegerValue(keepAliveSeconds, "keepAliveSeconds");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name keepAliveSeconds: something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "20")
    public int getReactorWorkQueueDispatcherPoolSize() {
        return getIntegerValue("reactorWorkQueueDispatcherPoolSize");
    }

    @SuppressWarnings("unused")
    public void setReactorWorkQueueDispatcherPoolSize(final int reactorWorkQueueDispatcherPoolSize) {
        try {
            setIntegerValue(reactorWorkQueueDispatcherPoolSize, "reactorWorkQueueDispatcherPoolSize");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name reactorWorkQueueDispatcherPoolSize: something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "13")
    public int getReactorWorkQueueDispatcherRingBufferSizePower() {
        return getIntegerValue("reactorWorkQueueDispatcherRingBufferSizePower");
    }

    @SuppressWarnings("unused")
    public void setReactorWorkQueueDispatcherRingBufferSizePower(final int reactorWorkQueueDispatcherRingBufferSizePower) {
        try {
            setIntegerValue(reactorWorkQueueDispatcherRingBufferSizePower, "reactorWorkQueueDispatcherRingBufferSizePower");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name reactorWorkQueueDispatcherRingBufferSizePower: something is very wrong here.", e);
        }
    }
}
