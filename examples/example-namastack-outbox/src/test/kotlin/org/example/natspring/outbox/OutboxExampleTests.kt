package org.example.natspring.outbox

import io.github.amadeusitgroup.testcontainers.nats.NatsContainer
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.postgresql.PostgreSQLContainer
import java.util.concurrent.TimeUnit

@SpringBootTest
class OutboxExampleTests {
    companion object {
        @Container
        @ServiceConnection
        @JvmField
        val nats: NatsContainer = NatsContainer("nats:2.14").withJetStream()

        @Container
        @ServiceConnection
        @JvmField
        val postgres: PostgreSQLContainer = PostgreSQLContainer("postgres:18")
    }

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var orderEventListener: OrderEventListener

    @BeforeEach
    fun beforeEach() {
        orderEventListener.clear()
    }

    @Test
    fun `placed order triggers outbox event published to NATS JetStream`() {
        val order = orderService.placeOrder(PlaceOrderRequest("John Doe"))

        await()
            .atMost(10, TimeUnit.SECONDS)
            .until { orderEventListener.receivedEvents.isNotEmpty() }

        val events = orderEventListener.receivedEvents
        assertThat(events).hasSize(1)
        assertThat(events.first().orderId).isEqualTo(order.id)
        assertThat(events.first().customerName).isEqualTo("John Doe")
    }
}
