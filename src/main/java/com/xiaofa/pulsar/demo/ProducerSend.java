package com.xiaofa.pulsar.demo;

import com.xiaofa.pulsar.client.PulsarTemplate;
import com.xiaofa.pulsar.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 生产者发送消息示例
 * @author Pig/linxiaofa
 * @date 2020/8/7 5:45 下午
 */
@Component
@Slf4j
public class ProducerSend {
    @Autowired
    private PulsarTemplate pulsarTemplate;

    public void send() throws PulsarClientException {
        for(int i=1; i<=1000; i++) {
            MessageVo messageVo = new MessageVo();
            messageVo.setName("hello pulsar, index is " + i);
            Map<String, MessageId> result = pulsarTemplate.createBuilder()
                    .persistent(true)
                    .tenancy("GEO_test")
                    .namespace("GEO_test_product")
                    .topics("xiaofa-test")
                    .send(JsonUtil.toJson(messageVo));
            log.info("信息发送成功, result={}", JsonUtil.toJson(result));
        }
    }
}
