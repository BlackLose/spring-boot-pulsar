package com.xiaofa.pulsar.demo;

import com.xiaofa.pulsar.annotations.Consume;
import com.xiaofa.pulsar.annotations.PulsarListener;
import com.xiaofa.pulsar.annotations.TopicBinding;
import com.xiaofa.pulsar.constants.PulsarConstants;
import com.xiaofa.pulsar.listener.ConsumerMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.concurrent.TimeUnit;

/**
 * 消息监听器示例，需要引入注解，继承{#ConsumerMessageListener}，或实现{#MessageListener}类
 * commons-pulsar会自动扫描{'@PulsarListener'}注解，在消费成功后，触发消息处理事件
 * @author Pig/linxiaofa
 * @date 2020/8/6 2:15 下午
 */
@Slf4j
@PulsarListener(
    bindings = {
        @TopicBinding(
            @Consume(
                value = "sl-test",
                enableRetry = "true",   //开启消息重试
                retryTopic = "sl-test-RETRY",
                deadLetterTopic = "sl-test_DLQ",
                maxRedeliverCount = 5,
                consumerInterceptors = {DemoConsumerInterceptor.class}
            )
        )
    }
)
public class DemoMessageListener extends ConsumerMessageListener {
    /**
     * 消息处理
     *
     * @param consumer 消费者
     * @param message  消息
     */
    @Override
    public void handle(Consumer<String> consumer, Message<String> message) {
        if(message.getTopicName().contains(PulsarConstants.RETRY)) {
            log.info("重试消息，topic：{}", message.getTopicName());
        } else {
            log.info("正常消息，topic：{}", message.getTopicName());
        }
    }

    /**
     * confirm the message
     *
     * @param consumer pulsar consumer
     * @param msg      new message
     */
    @Override
    public void acknowledge(Consumer<String> consumer, Message<String> msg) throws PulsarClientException {
        consumer.reconsumeLater(msg, 5000, TimeUnit.MILLISECONDS);
    }

}
