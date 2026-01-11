package io.github.balaelangovan.orders.infra.repository

import io.github.balaelangovan.orders.infra.entity.AddressEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * JPA repository for Address entities.
 */
@Repository
interface AddressJpaRepository : JpaRepository<AddressEntity, Long>
