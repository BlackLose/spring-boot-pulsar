package com.xiaofa.pulsar.client;

import com.xiaofa.pulsar.beans.TopicNameComponent;
import com.xiaofa.pulsar.config.PulsarConfiguration;
import com.xiaofa.pulsar.utils.PulsarUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Pulsar producer template, used to create producers and send messages
 * @author pig
 **/
@Component
public class PulsarTemplate {
    private final PulsarClient pulsarClient;
    private final PulsarConfiguration pulsarProperties;
    private final static Map<String, Producer<String>> PRODUCER_MAP = new HashMap<>();

    public PulsarTemplate(PulsarClient pulsarClient, PulsarConfiguration pulsarProperties) {
        this.pulsarClient = pulsarClient;
        this.pulsarProperties = pulsarProperties;
    }

    public class TemplateBuilder{
        /**
         * This identifies the type of topic.
         * Pulsar supports two kind of topics: persistent and non-persistent
         * (persistent is the default, so if you don’t specify a type the topic will be persistent).
         * With persistent topics, all messages are durably persisted on disk
         * (that means on multiple disks unless the broker is standalone),
         * whereas data for non-persistent topics isn’t persisted to storage disks.
         */
        private boolean persistent = true;
        /**
         * The topic's tenant within the instance.
         * Tenants are essential to multi-tenancy in Pulsar and can be spread across clusters.
         */
        private String tenancy;
        /**
         * The topic's tenant within the instance.
         * Tenants are essential to multi-tenancy in Pulsar and can be spread across clusters.
         */
        private String namespace;
        /**
         * the amount of delay before the message will be delivered
         */
        private long delay;
        /**
         * the time unit for the delay
         */
        private TimeUnit timeUnit;
        /**
         * list of message topics
         */
        private String[] topics;
        public TemplateBuilder persistent(boolean persistent) {
            this.persistent = persistent;
            return this;
        }
        public TemplateBuilder tenancy(String tenancy) {
            this.tenancy = tenancy;
            return this;
        }
        public TemplateBuilder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }
        public TemplateBuilder delayAfter(long delay, TimeUnit timeUnit) {
            this.delay = delay;
            this.timeUnit = timeUnit;
            return this;
        }
        public TemplateBuilder topics(String... topics) {
            this.topics = topics;
            return this;
        }

        /**
         * Send message synchronously
         * @param message   the message that the producer wants to send
         * @return  send result
         * @throws PulsarClientException    pulsar send exception
         */
        public Map<String, MessageId> send(String message) throws PulsarClientException {
            Map<String, MessageId> sendResult = new HashMap<>();
            if(ArrayUtils.isEmpty(topics)) {
                throw new PulsarClientException("[Pulsar] topics must be not empty");
            } else {
                TopicNameComponent topicNameComponent = new TopicNameComponent();
                topicNameComponent.setTenancy(StringUtils.isNotEmpty(tenancy)?tenancy:pulsarProperties.getTenancy());
                topicNameComponent.setNamespace(StringUtils.isNotEmpty(namespace)?namespace:pulsarProperties.getNamespace());
                topicNameComponent.setPersistent(persistent);
                for (String topic : topics) {
                    topic = PulsarUtils.getActualTopic(topicNameComponent, topic);
                    TypedMessageBuilder<String> typeMessageBuilder = getTypeMessageBuilder(message, delay, timeUnit, topic);
                    sendResult.put(topic, typeMessageBuilder.send());
                }
            }
            return sendResult;
        }

        /**
         * Send message asynchronously
         * @param message   the message that the producer wants to send
         * @return  send result
         * @throws PulsarClientException    pulsar send exception
         */
        public Map<String, CompletableFuture<MessageId>> sendAsync(String message) throws PulsarClientException {
            Map<String, CompletableFuture<MessageId>> sendResult = new HashMap<>();
            if(ArrayUtils.isEmpty(topics)) {
                throw new PulsarClientException("[Pulsar] topics must be not empty");
            } else {
                TopicNameComponent topicNameComponent = new TopicNameComponent();
                topicNameComponent.setTenancy(StringUtils.isNotBlank(tenancy)?tenancy:pulsarProperties.getTenancy());
                topicNameComponent.setNamespace(StringUtils.isNotBlank(namespace)?namespace:pulsarProperties.getNamespace());
                topicNameComponent.setPersistent(persistent);
                for (String topic : topics) {
                    topic = PulsarUtils.getActualTopic(topicNameComponent, topic);
                    TypedMessageBuilder<String> typeMessageBuilder = getTypeMessageBuilder(message, delay, timeUnit, topic);
                    sendResult.put(topic, typeMessageBuilder.sendAsync());
                }
            }
            return sendResult;
        }
    }

    public TemplateBuilder createBuilder() {
        return new TemplateBuilder();
    }

    private TypedMessageBuilder<String> getTypeMessageBuilder(
            String message, long delay, TimeUnit unit, String topic) throws PulsarClientException {
        Producer<String> producer;
        if(PRODUCER_MAP.containsKey(topic)) {
            producer = PRODUCER_MAP.get(topic);
            if(!producer.isConnected()) {
                producer = createProducer(topic);
            }
        } else {
            producer = createProducer(topic);
        }
        TypedMessageBuilder<String> typedMessageBuilder = producer.newMessage().value(message);
        if(delay > 0) {
            if(unit == null) {
                throw new PulsarClientException("[Pulsar] delay timeunit must be not empty");
            }
            typedMessageBuilder.deliverAfter(delay, unit);
        }
        return typedMessageBuilder;
    }

    private Producer<String> createProducer(String topic) throws PulsarClientException {
        Producer<String> producer = pulsarClient.newProducer(Schema.STRING)
                .topic(topic)
                .blockIfQueueFull(pulsarProperties.getProducer().isBlockIfQueueFull())
                .enableBatching(pulsarProperties.getProducer().isEnableBatching())
                .sendTimeout(pulsarProperties.getProducer().getSendTimeout(), TimeUnit.MILLISECONDS)
                .create();
        PRODUCER_MAP.put(topic, producer);
        return producer;
    }
}
