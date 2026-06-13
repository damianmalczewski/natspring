package org.example.natspring.outbox

import io.github.malczuuu.natspring.annotation.JetStreamListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.CopyOnWriteArrayList

@Component
class OrderEventListener {
    val receivedEvents: MutableList<OrderCreatedEvent> = CopyOnWriteArrayList()

    @JetStreamListener(subject = "orders.events", stream = "orders", durable = "order-events-consumer")
    fun onOrderCreated(event: OrderCreatedEvent) {
        log.info("Received order event: orderId={}", event.orderId)
        receivedEvents.add(event)
    }

    fun clear() = receivedEvents.clear()

    companion object {
        private val log = LoggerFactory.getLogger(OrderEventListener::class.java)
    }
}
