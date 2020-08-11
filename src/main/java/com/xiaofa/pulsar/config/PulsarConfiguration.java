package com.xiaofa.pulsar.config;

import lombok.Data;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Pulsar properties configuration
 * @author pig
 **/
@Configuration
@ConfigurationProperties(prefix = "pulsar")
@ConditionalOnExpression("'${pulsar.serviceUrl}'!=null")
@Data
public class PulsarConfiguration {
    private String serviceUrl;
    private String tenancy;
    private String namespace;
    private boolean persistent = true;
    private ProducerConfig producer;
    private ConsumerConfig consumer;

    /**
     * create Pulsar client Bean
     */
    @Bean(value = "pulsarClient")
    public PulsarClient pulsarClient(PulsarConfiguration pulsarProperties) throws PulsarClientException {
        return PulsarClient.builder().serviceUrl(pulsarProperties.getServiceUrl()).build();
    }

}
