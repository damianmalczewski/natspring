package org.example.natspring.outbox

import io.nats.client.api.StreamConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class OutboxExample {
    @Bean
    fun ordersStream(): StreamConfiguration =
        StreamConfiguration
            .builder()
            .name("orders")
            .subjects("orders.events")
            .build()
}

fun main(args: Array<String>) {
    runApplication<OutboxExample>(*args)
}
