package io.github.balaelangovan.orders.infra.entity

import io.github.balaelangovan.orders.infra.annotation.TimestampId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA entity for Address.
 * ANEMIC - no business logic, just data holder.
 */
@Entity
@Table(name = "addresses")
data class AddressEntity(
    @Id
    @TimestampId
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Column(name = "address_key", nullable = false, unique = true, columnDefinition = "UUID")
    val addressKey: UUID = UUID.randomUUID(),

    @Column(name = "full_name", nullable = false, length = 200)
    val fullName: String = "",

    @Column(name = "address_line1", nullable = false, length = 500)
    val addressLine1: String = "",

    @Column(name = "address_line2", length = 500)
    val addressLine2: String? = null,

    @Column(name = "city", nullable = false, length = 100)
    val city: String = "",

    @Column(name = "state_province", nullable = false, length = 100)
    val stateProvince: String = "",

    @Column(name = "postal_code", nullable = false, length = 20)
    val postalCode: String = "",

    @Column(name = "country", nullable = false, length = 100)
    val country: String = "",

    @Column(name = "phone_number", length = 50)
    val phoneNumber: String? = null,

    @Column(name = "email", length = 200)
    val email: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
