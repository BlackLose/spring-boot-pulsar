package com.xiaofa.pulsar.annotations;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Topic, retry topic and dead letter topic binding
 * @author pig
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Consume {
    /**
     * topics that consumers expect to subscribe to.
     * @return  subscribed topics
     */
    String[] value() default {};

    /**
     * setup retry topic
     * @return  the retry topic
     */
    String retryTopic() default "";

    /**
     * setup dead letter topic
     * @return  the dead topic
     */
    String deadLetterTopic() default "";

    /**
     * Maximum number of times that a message will be redelivered before being sent to the dead letter queue.
     * default 3 times
     * @return  the max retry count
     */
    int maxRedeliverCount() default 0;

    /**
     * If enabled, the consumer will auto retry message.
     * default unabled.
     * @return true or false
     */
    String enableRetry() default "";

    /**
     * Sets message consumer mode
     * @return  true or false
     */
    String enableAsync() default "";

    /**
     * Specify the subscription name for this consumer.
     * @return  the subscription name
     */
    String subscriptionName() default "";

    /**
     * Set the timeout for unacked messages, truncated to the nearest millisecond.
     * The timeout needs to be greater than 1 second.
     * @return the ack timeout
     */
    long ackTimeout() default 0;

    /**
     * Select the subscription type to be used when subscribing to the topic.
     * @return the subscription type
     */
    String subscriptionType() default "";

    /**
     * Select the subscription mode to be used when subscribing to the topic.
     * @return the subscription mode
     */
    String subscriptionMode() default "";

    /**
     * Sets the size of the consumer receive queue.
     *
     * <p>The consumer receive queue controls how many messages can be accumulated by the {@link Consumer} before the
     * application calls {@link Consumer#receive()}. Using a higher value could potentially increase the consumer
     * throughput at the expense of bigger memory utilization.
     *
     * <p><b>Setting the consumer queue size as zero</b>
     * <ul>
     * <li>Decreases the throughput of the consumer, by disabling pre-fetching of messages. This approach improves the
     * message distribution on shared subscription, by pushing messages only to the consumers that are ready to process
     * them. Neither {@link Consumer#receive(int, TimeUnit)} nor Partitioned Topics can be used if the consumer queue
     * size is zero. {@link Consumer#receive()} function call should not be interrupted when the consumer queue size is
     * zero.</li>
     * <li>Doesn't support Batch-Message: if consumer receives any batch-message then it closes consumer connection with
     * broker and {@link Consumer#receive()} call will remain blocked while {@link Consumer#receiveAsync()} receives
     * exception in callback. <b> consumer will not be able receive any further message unless batch-message in pipeline
     * is removed</b></li>
     * </ul>
     * Default value is {@code 1000} messages and should be good for most use cases.
     *
     * @return the consumer builder instance
     */
    int receiverQueueSize() default 0;
    /**
     * Set the delay to wait before re-delivering messages that have failed to be process.
     *
     * <p>When application uses {@link Consumer#negativeAcknowledge(Message)}, the failed message
     * will be redelivered after a fixed timeout. The default is 1 min.

     * @return the consumer builder instance
     * @see Consumer#negativeAcknowledge(Message)
     */
    long negativeAckRedeliveryDelay() default 0;
    /**
     * Set the consumer name.
     *
     * <p>Consumer name is informative and it can be used to indentify a particular consumer
     * instance from the topic stats.
     *
     * @return the consumer builder instance
     */
    String consumerName() default "";

    /**
     * A plugin interface that allows you to intercept (and possibly mutate)
     * messages received by the consumer.
     * @return the interceptors
     */
    Class<?>[] consumerInterceptors() default {};
}
