package com.xiaofa.pulsar.demo;

import com.xiaofa.pulsar.annotations.Consume;
import com.xiaofa.pulsar.annotations.PulsarListener;
import com.xiaofa.pulsar.annotations.TopicBinding;
import com.xiaofa.pulsar.listener.ConsumerMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;

/**
 * @author Pig/linxiaofa
 * @date 2020/8/6 2:15 下午
 */
@Slf4j
@PulsarListener(bindings = @TopicBinding(@Consume("sl-test-DLQ")))
public class DeadMessageListener extends ConsumerMessageListener {
    /**
     * 消息处理
     *
     * @param consumer 消费者
     * @param message  消息
     */
    @Override
    public void handle(Consumer<String> consumer, Message<String> message) {
        log.info("死信消息消费成功，topic：{}", message.getTopicName());
    }

    /**
     * confirm the message
     *
     * @param consumer pulsar consumer
     * @param msg      new message
     * @throws PulsarClientException ack error
     */
    @Override
    public void acknowledge(Consumer<String> consumer, Message<String> msg) throws PulsarClientException {
        consumer.acknowledge(msg);
    }
}
