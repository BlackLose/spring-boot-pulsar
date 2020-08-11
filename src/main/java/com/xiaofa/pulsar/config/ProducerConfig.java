package com.xiaofa.pulsar.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author pig
 */
@Getter
@Setter
public class ProducerConfig {
    /**
     * Set the send timeout <i>(default: 30 seconds)</i>.
     *
     * <p>If a message is not acknowledged by the server before the sendTimeout expires, an error will be reported.
     *
     * <p>Setting the timeout to zero, for example {@code setTimeout(0, TimeUnit.SECONDS)} will set the timeout
     * to infinity, which can be useful when using Pulsar's message deduplication feature, since the client
     * library will retry forever to publish a message. No errors will be propagated back to the application.
            */
    private int sendTimeout = 30000;
    /**
     * Control whether automatic batching of messages is enabled for the producer. <i>default: enabled</i>
     */
    private boolean enableBatching = true;
    /**
     * Set whether the {@link Producer#send} and {@link Producer#sendAsync} operations should block when the outgoing message queue is full.
     */
    private boolean blockIfQueueFull = false;
}