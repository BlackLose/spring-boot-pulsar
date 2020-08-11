package com.xiaofa.pulsar.client;

import com.xiaofa.pulsar.annotations.Consume;
import com.xiaofa.pulsar.annotations.PulsarListener;
import com.xiaofa.pulsar.annotations.TopicBinding;
import com.xiaofa.pulsar.beans.TopicNameComponent;
import com.xiaofa.pulsar.config.ConsumerConfig;
import com.xiaofa.pulsar.config.PulsarConfiguration;
import com.xiaofa.pulsar.constants.PulsarConstants;
import com.xiaofa.pulsar.listener.ConsumerMessageListener;
import com.xiaofa.pulsar.utils.PulsarUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Pulsar consumer config
 * The consumer is automatically created and the message is received.
 * Inherit {@link ConsumerMessageListener} and introduce the {@link @pulsarListener} annotation
 * @author pig
 **/
@Slf4j
@Component
public class PulsarConsumeClient implements BeanPostProcessor {
    private final PulsarClient pulsarClient;
    private final PulsarConfiguration pulsarProperties;

    private static final long LEAST_ACK_TIMEOUT = 1000;

    private static final ThreadPoolExecutor CONSUMER_POOL =
            new ThreadPoolExecutor(4, 16, 30, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

    public PulsarConsumeClient(
            PulsarClient pulsarClient,
            PulsarConfiguration pulsarProperties) {
        this.pulsarClient = pulsarClient;
        this.pulsarProperties = pulsarProperties;
    }

    @Override
    public Object postProcessBeforeInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, @NotNull String beanName) throws BeansException {
        PulsarListener pulsarListener = AnnotationUtils.findAnnotation(bean.getClass(), PulsarListener.class);
        if (pulsarListener != null) {
            checkAndExec((ConsumerMessageListener) bean, pulsarListener);
        }
        return bean;
    }

    private void checkAndExec(ConsumerMessageListener messageListener, PulsarListener pulsarListener) {
        if(StringUtils.isNotBlank(pulsarProperties.getServiceUrl())) {
            throw new RuntimeException("[Pulsar] service url must be not empty.");
        }
        if(ArrayUtils.isEmpty(pulsarListener.bindings())) {
            throw new RuntimeException("[Pulsar] topic binding must be not empty.");
        }
        String tenancy = StringUtils.isNotEmpty(pulsarListener.tenancy())?
                pulsarListener.tenancy():pulsarProperties.getTenancy();
        if(StringUtils.isNotBlank(tenancy)) {
            throw new RuntimeException("[Pulsar] tenancy must be not empty.");
        }
        String namespace = StringUtils.isNotEmpty(pulsarListener.namespace())?
                pulsarListener.namespace():pulsarProperties.getNamespace();
        if(StringUtils.isNotBlank(namespace)) {
            throw new RuntimeException("[Pulsar] namespace must be not empty.");
        }
        boolean persistent = StringUtils.isNotEmpty(pulsarListener.persistent())?
                Boolean.parseBoolean(pulsarListener.persistent()):pulsarProperties.isPersistent();
        TopicNameComponent topicNameComponent = new TopicNameComponent();
        topicNameComponent.setTenancy(tenancy);
        topicNameComponent.setNamespace(namespace);
        topicNameComponent.setPersistent(persistent);
        for(TopicBinding topicBinding : pulsarListener.bindings()) {
            ConsumerConfig consumerConfig = getConsumerConfig(topicBinding);
            Consumer<String> consumer = createConsumer(topicNameComponent, consumerConfig, messageListener);
            doReceive(messageListener, consumerConfig.isEnableAsync(), consumer);
        }
    }

    private Consumer<String> createConsumer(TopicNameComponent topicNameComponent, ConsumerConfig consumerConfig,
                         ConsumerMessageListener messageListener) {
        ConsumerBuilder<String> consumerBuilder = getConsumerBuilder(topicNameComponent, consumerConfig);
        try {
            if(consumerConfig.isEnableAsync()) {
                consumerBuilder.messageListener(messageListener);
            }
            return consumerBuilder.subscribe();
        } catch (PulsarClientException e) {
            throw new RuntimeException("[Pulsar] Consumer subscription failed", e);
        }
    }

    private void doReceive(ConsumerMessageListener messageListener, boolean isAsync, Consumer<String> consumer) {
        if(isAsync) {
            consumer.receiveAsync();
        } else {
            CONSUMER_POOL.execute(() -> {
                receiveSync(consumer, messageListener);
            });
        }
    }

    private void receiveSync(Consumer<String> consumer, ConsumerMessageListener messageListener) {
        try {
            Message<String> message = consumer.receive();
            messageListener.received(consumer, message);
        } catch (PulsarClientException e) {
            log.error(String.format("[Pulsar] sync consume message failed, topic: %s", consumer.getTopic()), e);
        }
        receiveSync(consumer, messageListener);
    }

    private ConsumerBuilder<String> getConsumerBuilder(TopicNameComponent topicNameComponent,
            ConsumerConfig consumerConfig) {
        List<String> topics = new ArrayList<>();
        for(String topic : consumerConfig.getTopics()) {
            topics.add(PulsarUtils.getActualTopic(topicNameComponent, topic));
        }
        ConsumerBuilder<String> consumerBuilder = pulsarClient.newConsumer(Schema.STRING)
                .topics(topics)
                .subscriptionType(consumerConfig.getSubscriptionType())
                .subscriptionMode(consumerConfig.getSubscriptionMode())
                .receiverQueueSize(consumerConfig.getReceiverQueueSize())
                .enableRetry(consumerConfig.isEnableRetry());
        if(StringUtils.isNotEmpty(consumerConfig.getSubscriptionName())) {
            consumerBuilder.subscriptionName(consumerConfig.getSubscriptionName());
        }
        if(StringUtils.isNotEmpty(consumerConfig.getConsumerName())) {
            consumerBuilder.consumerName(consumerConfig.getConsumerName());
        }
        long ackTimeout = consumerConfig.getAckTimeout();
        if(ackTimeout < LEAST_ACK_TIMEOUT) {
            throw new RuntimeException("[Pulsar] ack timeout needs to be greater than 1 second.");
        }
        consumerBuilder.ackTimeout(consumerConfig.getAckTimeout(), TimeUnit.MILLISECONDS);
        if(consumerConfig.getConsumerInterceptors() != null) {
            consumerBuilder.intercept(consumerConfig.getConsumerInterceptors());
        }
        if(consumerConfig.getSubscriptionType() == SubscriptionType.Shared) {
            if(consumerConfig.isEnableRetry()) {
                DeadLetterPolicy deadLetterPolicy = createDeadLetterPolicy(topicNameComponent, consumerConfig, topics);
                consumerBuilder.deadLetterPolicy(deadLetterPolicy)
                        .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest);
            }
        }
        return consumerBuilder;
    }

    @NotNull
    private DeadLetterPolicy createDeadLetterPolicy(TopicNameComponent topicNameComponent, ConsumerConfig consumerConfig, List<String> topics) {
        DeadLetterPolicy.DeadLetterPolicyBuilder deadLetterPolicyBuilder = DeadLetterPolicy.builder();
        if(topics.size() == 1) {
            if (StringUtils.isNotEmpty(consumerConfig.getRetryTopic())) {
                deadLetterPolicyBuilder.retryLetterTopic(PulsarUtils.getActualTopic(
                        topicNameComponent, consumerConfig.getRetryTopic()));
            } else {
                deadLetterPolicyBuilder.retryLetterTopic(String.format("%s-%s", topics.get(0), PulsarConstants.RETRY));
            }
            if (StringUtils.isNotEmpty(consumerConfig.getDeadLetterTopic())) {
                deadLetterPolicyBuilder.deadLetterTopic(PulsarUtils.getActualTopic(
                        topicNameComponent, consumerConfig.getDeadLetterTopic()));
            } else {
                deadLetterPolicyBuilder.deadLetterTopic(String.format("%s-%s", topics.get(0), PulsarConstants.DLQ));
            }
        }
        if (consumerConfig.getMaxRedeliverCount() > 0) {
            deadLetterPolicyBuilder.maxRedeliverCount(consumerConfig.getMaxRedeliverCount());
        }
        return deadLetterPolicyBuilder.build();
    }

    @NotNull
    private ConsumerConfig getConsumerConfig(TopicBinding topicBinding) {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        BeanUtils.copyProperties(pulsarProperties.getConsumer(), consumerConfig);
        Consume consume = topicBinding.value();
        if(ArrayUtils.isNotEmpty(consume.value())) {
            consumerConfig.setTopics(consume.value());
        }
        if(StringUtils.isNotEmpty(consume.retryTopic())) {
            consumerConfig.setRetryTopic(consume.retryTopic());
        }
        if(StringUtils.isNotEmpty(consume.deadLetterTopic())) {
            consumerConfig.setDeadLetterTopic(consume.deadLetterTopic());
        }
        if(consume.maxRedeliverCount() > 0) {
            consumerConfig.setMaxRedeliverCount(consume.maxRedeliverCount());
        }
        if(consume.receiverQueueSize() > 0) {
            consumerConfig.setReceiverQueueSize(consume.receiverQueueSize());
        }
        if(consume.ackTimeout() > 0) {
            consumerConfig.setAckTimeout(consume.ackTimeout());
        }
        if(StringUtils.isNotEmpty(consume.enableAsync())) {
            consumerConfig.setEnableAsync(Boolean.parseBoolean(consume.enableAsync()));
        }
        if(StringUtils.isNotEmpty(consume.enableRetry())) {
            consumerConfig.setEnableRetry(Boolean.parseBoolean(consume.enableRetry()));
        }
        if(StringUtils.isNotEmpty(consume.subscriptionMode())) {
            consumerConfig.setSubscriptionMode(SubscriptionMode.valueOf(consume.subscriptionMode()));
        }
        if(StringUtils.isNotEmpty(consume.subscriptionName())) {
            consumerConfig.setSubscriptionName(consume.subscriptionName());
        }
        if(StringUtils.isNotEmpty(consume.subscriptionType())) {
            consumerConfig.setSubscriptionType(SubscriptionType.valueOf(consume.subscriptionType()));
        }
        if(consume.negativeAckRedeliveryDelay() > 0) {
            consumerConfig.setNegativeAckRedeliveryDelay(consume.negativeAckRedeliveryDelay());
        }
        if(StringUtils.isNotEmpty(consume.consumerName())) {
            consumerConfig.setConsumerName(consume.consumerName());
        }
        if(ArrayUtils.isNotEmpty(consume.consumerInterceptors())) {
            Class<?>[] classes = consume.consumerInterceptors();
            ConsumerInterceptor<String>[] consumerInterceptors = new ConsumerInterceptor[classes.length];
            IntStream.range(0, classes.length).forEach(i -> {
                try {
                    consumerInterceptors[i] = (ConsumerInterceptor<String>) classes[i].newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException("consumer interceptor object class error", e);
                }
            });
            consumerConfig.setConsumerInterceptors(consumerInterceptors);
        }
        return consumerConfig;
    }

}
