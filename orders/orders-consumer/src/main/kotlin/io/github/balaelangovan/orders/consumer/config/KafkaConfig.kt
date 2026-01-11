package io.github.balaelangovan.orders.consumer.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter
import org.springframework.util.backoff.FixedBackOff
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

/**
 * Kafka configuration for the consumer application.
 * Configures message converters and listener container factories for all event types.
 * Uses INFERRED type precedence to determine the target type from the listener method parameter.
 */
@Configuration
class KafkaConfig(
    @param:Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @param:Value("\${spring.kafka.consumer.group-id}") private val groupId: String,
    @param:Value("\${spring.kafka.consumer.auto-offset-reset:earliest}") private val autoOffsetReset: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val props = mutableMapOf<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = autoOffsetReset
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        return DefaultKafkaConsumerFactory(props)
    }

    /**
     * JsonMapper configured for Kafka message deserialization.
     * - Kotlin module for data class support
     * - Java time support built into Jackson 3.x
     * - Field names are explicitly defined via @JsonProperty annotations
     */
    @Bean
    fun kafkaObjectMapper(): JsonMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()

    /**
     * Creates a message converter that infers the target type from the listener method parameter.
     * This allows messages without type headers to be deserialized correctly.
     */
    @Bean
    fun messageConverter(kafkaObjectMapper: JsonMapper): StringJacksonJsonMessageConverter =
        StringJacksonJsonMessageConverter(kafkaObjectMapper)

    /**
     * Error handler for Kafka listeners.
     * - Retries failed messages up to 3 times with 1 second delay
     * - After exhausting retries, logs the error and skips the message (commits offset)
     * - Prevents poison messages from blocking the consumer
     */
    @Bean
    fun kafkaErrorHandler(): CommonErrorHandler {
        val errorHandler = DefaultErrorHandler(
            { record: ConsumerRecord<*, *>, exception: Exception ->
                logger.error(
                    "Failed to process message after retries - topic={}, partition={}, offset={}, key={}, error={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    exception.message,
                    exception,
                )
            },
            FixedBackOff(1000L, 3L),
        )
        return errorHandler
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>,
        messageConverter: StringJacksonJsonMessageConverter,
        kafkaErrorHandler: CommonErrorHandler,
    ): ConcurrentKafkaListenerContainerFactory<String, String> =
        ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            setConsumerFactory(consumerFactory)
            setRecordMessageConverter(messageConverter)
            setCommonErrorHandler(kafkaErrorHandler)
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        }

    @Bean
    fun releaseKafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>,
        messageConverter: StringJacksonJsonMessageConverter,
        kafkaErrorHandler: CommonErrorHandler,
    ): ConcurrentKafkaListenerContainerFactory<String, String> =
        ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            setConsumerFactory(consumerFactory)
            setRecordMessageConverter(messageConverter)
            setCommonErrorHandler(kafkaErrorHandler)
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        }

    @Bean
    fun shipmentKafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>,
        messageConverter: StringJacksonJsonMessageConverter,
        kafkaErrorHandler: CommonErrorHandler,
    ): ConcurrentKafkaListenerContainerFactory<String, String> =
        ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            setConsumerFactory(consumerFactory)
            setRecordMessageConverter(messageConverter)
            setCommonErrorHandler(kafkaErrorHandler)
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        }
}
