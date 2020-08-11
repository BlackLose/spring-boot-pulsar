package com.xiaofa.pulsar.beans;

import lombok.Data;

@Data
public class TopicNameComponent {
    private String tenancy;
    private String namespace;
    private boolean persistent;
}