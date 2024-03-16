package com.push.messenger.api.configuration;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class KafkaConsumerConfig {
	
	private static final String KAFKA_BROKER = "DESKTOP-ATHCR5Q:9092";

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Add more properties as needed.

        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> factory(
    		ConsumerFactory<String, String> consumerFactory
    		) {
    	ConcurrentKafkaListenerContainerFactory<String, String> factory = 
    			new ConcurrentKafkaListenerContainerFactory<>();
    	factory.setConsumerFactory(consumerFactory());
    	return factory;
    }

}
