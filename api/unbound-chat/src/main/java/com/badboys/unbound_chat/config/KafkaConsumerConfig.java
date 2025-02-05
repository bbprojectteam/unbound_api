package com.badboys.unbound_chat.config;

import com.badboys.unbound_match.model.RequestMatchDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, RequestMatchDto> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "15.165.178.17:10000,15.165.178.17:10001,15.165.178.17:10002");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonDeserializer.class);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "match-consumer-group");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // 🔥 JSON 직렬화 설정을 configProps에서만 지정 (Setter 사용 X)
        configProps.put("spring.json.trusted.packages", "*");
        configProps.put("spring.json.use.type.headers", "false");
        configProps.put("spring.json.value.default.type", "com.badboys.unbound_match.model.RequestMatchDto");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RequestMatchDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, RequestMatchDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}