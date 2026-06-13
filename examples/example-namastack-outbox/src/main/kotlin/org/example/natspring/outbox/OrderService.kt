package org.example.natspring.outbox

import io.namastack.outbox.Outbox
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val outbox: Outbox,
) {
    @Transactional
    fun placeOrder(request: PlaceOrderRequest): PlaceOrderResponse {
        val order = Order(id = UUID.randomUUID(), customerName = request.customerName)
        orderRepository.save(order)
        outbox.schedule(
            payload = OrderCreatedEvent(orderId = order.id, customerName = order.customerName),
            key = order.id.toString(),
        )
        return PlaceOrderResponse(id = order.id, customerName = order.customerName)
    }
}
