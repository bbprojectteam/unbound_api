package com.badboys.unbound_chat.config;

import com.badboys.unbound_chat.api.model.ChatMessage;
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
public class ChatConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    String serverConfig;

    @Value("${spring.kafka.consumer.key-deserializer}")
    String keyDeserializer;

    @Value("${spring.kafka.consumer.value-deserializer}")
    String valueDeserializer;

    @Value("${spring.kafka.consumer.chat.group-id}")
    String chatGroupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    String offsetConfig;

    @Bean
    public ConsumerFactory<String, ChatMessage> chatConsumerFactory() throws ClassNotFoundException {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverConfig);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Class.forName(keyDeserializer));
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Class.forName(valueDeserializer));
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, chatGroupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetConfig);

        configProps.put("spring.json.trusted.packages", "*");
        configProps.put("spring.json.use.type.headers", "false");
        configProps.put("spring.json.value.default.type", "com.badboys.unbound_chat.api.model.ChatMessage");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessage> chatKafkaListenerContainerFactory() throws ClassNotFoundException {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(chatConsumerFactory());
        return factory;
    }
}
