package org.example.natspring.outbox

import java.util.UUID

data class PlaceOrderRequest(
    val customerName: String,
)

data class PlaceOrderResponse(
    val id: UUID,
    val customerName: String,
)
