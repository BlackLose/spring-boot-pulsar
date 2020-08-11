package com.xiaofa.pulsar.listener;

import com.xiaofa.pulsar.event.MessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.PulsarClientException;

/**
 * Customize the message processing listener.
 * Need to manual ack.
 * Listens for new messages and processes incoming messages
 *
 * @author pig
 **/
@Slf4j
public abstract class ConsumerMessageListener implements MessageListener<String>, MessageEvent {
    @Override
    public void received(Consumer<String> consumer, Message<String> message) {
        before(consumer, message);
        handle(consumer, message);
        try {
            acknowledge(consumer, message);
        } catch (PulsarClientException e) {
            log.error("ack confirm error, consumerName: {}, topic: {}, messageId: {}, message: {}",
                    consumer.getConsumerName(), consumer.getTopic(),
                    message.getMessageId().toString(), message.getValue(), e);
        }
    }

    @Override
    public void before(Consumer<String> consumer, Message<String> message) {
        log.info("[Pulsar] Received a message, topic: {}, message: {}",
                message.getTopicName(), message.getValue());
    }

    /**
     * processing the message
     * @param consumer  pulsar consumer
     * @param message   new message
     */
    @Override
    public abstract void handle(Consumer<String> consumer, Message<String> message);
}
