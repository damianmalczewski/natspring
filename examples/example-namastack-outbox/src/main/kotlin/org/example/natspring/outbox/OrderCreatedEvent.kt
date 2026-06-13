package org.example.natspring.outbox

import java.util.UUID

data class OrderCreatedEvent(
    val orderId: UUID,
    val customerName: String,
)
