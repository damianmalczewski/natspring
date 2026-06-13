package org.example.natspring.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "orders")
class Order(
    @Id
    @Column(name = "order_id", nullable = false)
    val id: UUID,
    @Column(name = "order_customer_name", nullable = false)
    val customerName: String,
)
