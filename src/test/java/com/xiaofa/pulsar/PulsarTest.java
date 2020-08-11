package com.xiaofa.pulsar;

import com.xiaofa.pulsar.demo.MessageVo;
import com.xiaofa.pulsar.client.PulsarTemplate;
import com.xiaofa.pulsar.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.MessageId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

/**
 * @author Pig/linxiaofa
 * @date 2020/8/5 4:30 下午
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class PulsarTest {

    @Autowired
    private PulsarTemplate pulsarTemplate;

    @Test
    public void testProduct() throws Exception {
        for(int i=1; i<=1; i++) {
            MessageVo messageVo = new MessageVo();
            messageVo.setName("hello pulsar test 1234, index is " + i);
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
