/*
 * web: org.nrg.xnat.configuration.MqConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2022, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.RedeliveryPolicyMap;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.StoreUsage;
import org.apache.activemq.usage.SystemUsage;
import org.apache.activemq.usage.TempUsage;
import org.apache.activemq.xbean.XBeanBrokerService;
import org.apache.commons.math.util.MathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.ErrorHandler;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Session;

import static org.nrg.xdat.XDAT.DEFAULT_REQUEST_QUEUE;

@Configuration
@EnableJms
@ComponentScan({"org.nrg.framework.messaging", "org.nrg.xnat.services.messaging"})
@Slf4j
public class MqConfig {
    private static final long TEMP_USAGE  = MathUtils.pow(2, 27);
    private static final long MEM_USAGE   = MathUtils.pow(2, 29);
    private static final long STORE_USAGE = MathUtils.pow(2, 30);

    @Value("${spring.activemq.broker-url:vm://localhost}")
    private String _brokerUrl;

    @Value("${spring.activemq.user}")
    private String _username;

    @Value("${spring.activemq.password}")
    private String _password;

    @Value("${amq.usage.temp:0}")
    private long _tempUsage;

    @Value("${amq.usage.mem:0}")
    private long _memoryUsage;

    @Value("${amq.usage.store:0}")
    private long _storeUsage;

    @Bean
    @Primary
    public Destination defaultRequestQueue() {
        return new ActiveMQQueue(DEFAULT_REQUEST_QUEUE);
    }

    @Bean
    public Destination automatedScriptRequest() {
        return new ActiveMQQueue("automatedScriptRequest");
    }

    @Bean
    public Destination dicomInboxImportRequest() {
        return new ActiveMQQueue("dicomInboxImportRequest");
    }

    @Bean
    public Destination directArchiveRequest() {
        return new ActiveMQQueue("directArchiveRequest");
    }

    @Bean
    public Destination initializeGroupRequest() {
        return new ActiveMQQueue("initializeGroupRequest");
    }

    @Bean
    public Destination moveStoredFileRequest() {
        return new ActiveMQQueue("moveStoredFileRequest");
    }

    @Bean
    public Destination prearchiveOperationRequest() {
        return new ActiveMQQueue("prearchiveOperationRequest");
    }

    @Bean
    public Destination processingOperationRequest() {
        return new ActiveMQQueue("processingOperationRequest");
    }

    @Bean
    public RedeliveryPolicyMap redeliveryPolicyMap() {
        final RedeliveryPolicy defaultEntry = new RedeliveryPolicy();
        defaultEntry.setUseExponentialBackOff(true);
        defaultEntry.setMaximumRedeliveries(4);
        defaultEntry.setInitialRedeliveryDelay(300000);
        defaultEntry.setBackOffMultiplier(3);
        defaultEntry.setDestination((ActiveMQDestination) defaultRequestQueue());
        final RedeliveryPolicyMap redeliveryPolicyMap = new RedeliveryPolicyMap();
        redeliveryPolicyMap.setDefaultEntry(defaultEntry);
        return redeliveryPolicyMap;
    }

    @Bean
    public BrokerService brokerService() {
        final TempUsage tempUsage = new TempUsage();
        tempUsage.setLimit(_tempUsage > 0 ? _tempUsage : TEMP_USAGE);
        final MemoryUsage memoryUsage = new MemoryUsage();
        memoryUsage.setLimit(_memoryUsage > 0 ? _memoryUsage : MEM_USAGE);
        final StoreUsage storeUsage = new StoreUsage();
        storeUsage.setLimit(_storeUsage > 0 ? _storeUsage : STORE_USAGE);
        final SystemUsage systemUsage = new SystemUsage();
        systemUsage.setTempUsage(tempUsage);
        systemUsage.setMemoryUsage(memoryUsage);
        systemUsage.setStoreUsage(storeUsage);
        final XBeanBrokerService service = new XBeanBrokerService();
        service.setBrokerName("activeMQBroker");
        service.setUseJmx(false);
        service.setPersistent(false);
        service.setSchedulerSupport(false);
        service.setUseShutdownHook(true);
        service.setSystemUsage(systemUsage);
        return service;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(_brokerUrl);
        factory.setUserName(_username);
        factory.setPassword(_password);
        factory.setTrustAllPackages(true);
        return new CachingConnectionFactory(factory);
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        return new JmsTemplate(connectionFactory());
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(final ErrorHandler errorHandler) {
        final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setErrorHandler(errorHandler);
        factory.setConcurrency("10-40");
        factory.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        return factory;
    }
}
