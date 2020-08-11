package com.xiaofa.pulsar.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Turn on message listening
 * @author pig
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Component
public @interface PulsarListener {
    /**
     * This identifies the type of topic.
     * Pulsar supports two kind of topics: persistent and non-persistent
     * (persistent is the default, so if you don’t specify a type the topic will be persistent).
     * With persistent topics, all messages are durably persisted on disk
     * (that means on multiple disks unless the broker is standalone),
     * whereas data for non-persistent topics isn’t persisted to storage disks.
     * @return persistent or non-persistent
     */
    String persistent() default "";
    /**
     * The topic's tenant within the instance.
     * Tenants are essential to multi-tenancy in Pulsar and can be spread across clusters.
     * @return  tenant
     */
    String tenancy() default "";
    /**
     * The administrative unit of the topic, which acts as a grouping mechanism for related topics.
     * Most topic configuration is performed at the namespace level.
     * Each tenant can have multiple namespaces.
     * @return  namespace
     */
    String namespace() default "";
    /**
     * @return  the topic binding
     */
    TopicBinding[] bindings();
}
