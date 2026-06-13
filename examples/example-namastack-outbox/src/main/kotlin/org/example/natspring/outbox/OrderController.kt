package org.example.natspring.outbox

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun placeOrder(
        @RequestBody request: PlaceOrderRequest,
    ): PlaceOrderResponse {
        val order = orderService.placeOrder(request)
        return PlaceOrderResponse(id = order.id, customerName = order.customerName)
    }
}
