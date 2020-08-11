package com.xiaofa.pulsar.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerInterceptor;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;

import java.util.Set;

/**
 * 消费者拦截器示例，可以在消息消费之前进行修改，也可以对特定事件处理
 * @author Pig/linxiaofa
 * @date 2020/8/8 10:34 下午
 */
@Slf4j
public class DemoConsumerInterceptor implements ConsumerInterceptor<String> {
    @Override
    public void close() {

    }

    @Override
    public Message<String> beforeConsume(Consumer<String> consumer, Message<String> message) {
        log.info("消费之前，topic: {}, message: {}", message.getTopicName(), message.getValue());
        return message;
    }

    @Override
    public void onAcknowledge(Consumer<String> consumer, MessageId messageId, Throwable exception) {

    }

    @Override
    public void onAcknowledgeCumulative(Consumer<String> consumer, MessageId messageId, Throwable exception) {

    }

    @Override
    public void onNegativeAcksSend(Consumer<String> consumer, Set<MessageId> messageIds) {

    }

    @Override
    public void onAckTimeoutSend(Consumer<String> consumer, Set<MessageId> messageIds) {

    }
}
