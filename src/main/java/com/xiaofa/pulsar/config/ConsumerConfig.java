package com.xiaofa.pulsar.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.pulsar.client.api.ConsumerInterceptor;
import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;

/**
 * @author pig
 */
@Getter
@Setter
public class ConsumerConfig {
    /**
     * topics that consumers expect to subscribe to.
     */
    private String[] topics;
    /**
     * setup retry topic.
     * The default dead letter topic uses this format
     * <tenancy>/<namespace>/<topicName>-RETRY
     */
    private String retryTopic;
    /**
     * setup dead letter topic
     * The default dead letter topic uses this format
     * <tenancy>/<namespace>/<topicName>-DLQ
     */
    private String deadLetterTopic;
    /**
     * Maximum number of times that a message will be redelivered before being sent to the dead letter queue.
     * default 3 times
     */
    private int maxRedeliverCount = 3;
    /**
     * Specify the subscription name for this consumer.
     */
    private String subscriptionName;
    /**
     * Set the timeout for unacked messages, truncated to the nearest millisecond.
     * The timeout needs to be greater than 1 second.
     * default 30s
     */
    private long ackTimeout = 30000;
    /**
     * Select the subscription type to be used when subscribing to the topic.
     */
    private SubscriptionType subscriptionType = SubscriptionType.Shared;
    /**
     * Select the subscription mode to be used when subscribing to the topic.
     */
    private SubscriptionMode subscriptionMode = SubscriptionMode.Durable;
    /**
     * Sets the size of the consumer receive queue.
     * default 1000
     */
    private Integer receiverQueueSize = 1000;
    /**
     * Sets message consumer mode
     */
    private boolean enableAsync = true;
    /**
     * If enabled, the consumer will auto retry message.
     * default unable.
     */
    private boolean enableRetry = false;
    /**
     * Set the delay to wait before re-delivering messages that have failed to be process.
     */
    private long negativeAckRedeliveryDelay;
    /**
     * Set the consumer name.
     *
     * <p>Consumer name is informative and it can be used to indentify a particular consumer
     * instance from the topic stats.
     */
    private String consumerName;
    /**
     * A plugin interface that allows you to intercept (and possibly mutate)
     * messages received by the consumer.
     */
    private ConsumerInterceptor<String>[] consumerInterceptors;
}