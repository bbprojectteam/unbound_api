package com.badboys.unbound_chat.config;

import com.badboys.unbound_chat.api.model.MatchSuccess;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MatchSuccessConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    String serverConfig;

    @Value("${spring.kafka.consumer.key-deserializer}")
    String keyDeserializer;

    @Value("${spring.kafka.consumer.value-deserializer}")
    String valueDeserializer;

    @Value("${spring.kafka.consumer.match.group-id}")
    String matchGroupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    String offsetConfig;

    @Bean
    public ConsumerFactory<String, MatchSuccess> consumerFactory() throws ClassNotFoundException {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverConfig);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Class.forName(keyDeserializer));
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Class.forName(valueDeserializer));
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, matchGroupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetConfig);

        // 🔥 JSON 직렬화 설정을 configProps에서만 지정 (Setter 사용 X)
        configProps.put("spring.json.trusted.packages", "*");
        configProps.put("spring.json.use.type.headers", "false");
        configProps.put("spring.json.value.default.type", "com.badboys.unbound_chat.api.model.MatchSuccess");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MatchSuccess> matchSuccessKafkaListenerContainerFactory() throws ClassNotFoundException {
        ConcurrentKafkaListenerContainerFactory<String, MatchSuccess> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}