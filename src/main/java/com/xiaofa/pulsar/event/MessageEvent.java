package com.xiaofa.pulsar.event;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;

/**
 * Pulsar message event
 * @author Pig/linxiaofa
 * @date 2020/8/5 9:32 下午
 */
public interface MessageEvent {
    /**
     * the operation before processing the message
     *
     * @param consumer pulsar consumer
     * @param message  new message
     */
    void before(Consumer<String> consumer, Message<String> message);

    /**
     * processing the message
     * @param consumer  pulsar consumer
     * @param message   new message
     */
    void handle(Consumer<String> consumer, Message<String> message);

    /**
     * confirm the message
     *
     * @param consumer pulsar consumer
     * @param msg      new message
     * @throws PulsarClientException ack error
     */
    void acknowledge(Consumer<String> consumer, Message<String> msg) throws PulsarClientException;

}
