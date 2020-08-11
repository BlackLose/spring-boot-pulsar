package com.xiaofa.pulsar.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Topic, retry topic and dead letter topic binding
 * @author pig
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface TopicBinding {

    /**
     * consumer config
     * @return the consumer
     */
    Consume value();
}
