package io.github.balaelangovan.orders.infra.adapter.outbound

import io.github.balaelangovan.orders.domain.aggregate.Order
import io.github.balaelangovan.orders.domain.exception.DataPersistenceException
import io.github.balaelangovan.orders.domain.exception.ErrorCode
import io.github.balaelangovan.orders.domain.port.outbound.OrderRepositoryPort
import io.github.balaelangovan.orders.domain.valueobject.CustomerId
import io.github.balaelangovan.orders.domain.valueobject.LineItemId
import io.github.balaelangovan.orders.domain.valueobject.OrderId
import io.github.balaelangovan.orders.infra.entity.AddressEntity
import io.github.balaelangovan.orders.infra.mapper.PersistenceMapper
import io.github.balaelangovan.orders.infra.repository.AddressJpaRepository
import io.github.balaelangovan.orders.infra.repository.OrderJpaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Persistence adapter implementing the OrderRepositoryPort for JPA-based order storage.
 * Acts as the Anti-Corruption Layer between the rich domain model and anemic JPA entities.
 * Handles address deduplication at line level and manages bidirectional entity relationships.
 * Shipping addresses are stored per-line to support mixed fulfillment, while the billing
 * address remains at order level.
 */
@Component
@Transactional
class OrderRepositoryAdapter(
    private val orderJpaRepository: OrderJpaRepository,
    private val addressJpaRepository: AddressJpaRepository,
    private val persistenceMapper: PersistenceMapper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : OrderRepositoryPort {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Persists an order with all related entities (addresses, lines, statuses).
     *
     * @param order the domain Order to persist
     * @return the saved Order with generated IDs
     * @throws DataPersistenceException if persistence fails
     */
    override suspend fun save(order: Order): Order = withContext(ioDispatcher) {
        try {
            logger.debug("Saving order: {}", order.id)
            persistOrder(order)
        } catch (e: DataPersistenceException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to save order: {}", order.id, e)
            throw DataPersistenceException("Failed to save order: ${order.id.value}", ErrorCode.PERSISTENCE_FAILED, e)
        }
    }

    /**
     * Core persistence logic for saving an order with all related entities.
     * Handles billing address, shipping addresses, and order entity creation.
     *
     * @param order the domain Order to persist
     * @return the saved Order with generated IDs
     */
    private fun persistOrder(order: Order): Order {
        val billingAddressEntity = persistenceMapper.addressToEntity(order.billingAddress)
        val savedBillingAddress = addressJpaRepository.save(billingAddressEntity)
        val lineShippingAddresses = saveDeduplicatedShippingAddresses(order)

        val orderEntity = persistenceMapper.orderToEntity(
            order = order,
            billingAddressEntity = savedBillingAddress,
            lineShippingAddresses = lineShippingAddresses,
        )
        val savedOrderEntity = orderJpaRepository.save(orderEntity)

        return persistenceMapper.entityToOrder(
            entity = savedOrderEntity,
            billingAddress = order.billingAddress,
        )
    }

    /**
     * Saves shipping addresses with deduplication.
     * If multiple lines have the same address (by content), only one row is stored.
     */
    private fun saveDeduplicatedShippingAddresses(order: Order): Map<LineItemId, AddressEntity> {
        val lineShippingAddresses = mutableMapOf<LineItemId, AddressEntity>()
        val savedAddressesByContent =
            mutableMapOf<io.github.balaelangovan.orders.domain.valueobject.Address, AddressEntity>()

        order.lines.forEach { line ->
            line.shippingAddress?.let { address ->
                val savedAddress = savedAddressesByContent.getOrPut(address) {
                    val addressEntity = persistenceMapper.addressToEntity(address)
                    addressJpaRepository.save(addressEntity)
                }
                lineShippingAddresses[line.id] = savedAddress
            }
        }

        if (savedAddressesByContent.size < order.lines.count { it.shippingAddress != null }) {
            logger.debug(
                "Address deduplication: {} unique addresses for {} lines",
                savedAddressesByContent.size,
                order.lines.size,
            )
        }

        return lineShippingAddresses
    }

    /**
     * Finds an order by its identifier.
     *
     * @param id the order identifier
     * @return the Order or null if not found
     */
    override suspend fun findById(id: OrderId): Order? = withContext(ioDispatcher) {
        logger.debug("Loading order by ID: {}", id)

        val orderEntity = orderJpaRepository.findByOrderId(id.value).orElse(null) ?: return@withContext null
        val billingAddress = orderEntity.billingAddress?.let {
            persistenceMapper.entityToAddress(it)
        } ?: throw IllegalStateException("Billing address not found for order: ${orderEntity.orderId}")

        persistenceMapper.entityToOrder(orderEntity, billingAddress)
    }

    /**
     * Finds all orders for a customer.
     *
     * @param customerId the customer identifier
     * @return list of orders for the customer
     */
    override suspend fun findByCustomerId(customerId: CustomerId): List<Order> = withContext(ioDispatcher) {
        logger.debug("Loading orders for customer: {}", customerId)

        val entities = orderJpaRepository.findByCustomerId(customerId.value)
        entities.map { entity ->
            val billingAddress = entity.billingAddress?.let {
                persistenceMapper.entityToAddress(it)
            } ?: throw IllegalStateException("Billing address not found for order: ${entity.orderId}")

            persistenceMapper.entityToOrder(entity, billingAddress)
        }
    }

    /**
     * Finds all orders with pagination, sorted by creation date descending.
     *
     * @param page zero-based page number
     * @param size number of orders per page
     * @return list of orders for the page
     */
    override suspend fun findAll(page: Int, size: Int): List<Order> = withContext(ioDispatcher) {
        logger.debug("Loading all orders - page: {}, size: {}", page, size)

        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val entities = orderJpaRepository.findAllBy(pageable)

        entities.map { entity ->
            val billingAddress = entity.billingAddress?.let {
                persistenceMapper.entityToAddress(it)
            } ?: throw IllegalStateException("Billing address not found for order: ${entity.orderId}")

            persistenceMapper.entityToOrder(entity, billingAddress)
        }
    }

    /**
     * Deletes an order by its identifier.
     *
     * @param id the order identifier
     */
    override suspend fun deleteById(id: OrderId): Unit = withContext(ioDispatcher) {
        logger.debug("Deleting order: {}", id)
        orderJpaRepository.deleteByOrderId(id.value)
    }

    /**
     * Checks if an order exists.
     *
     * @param id the order identifier
     * @return true if order exists
     */
    override suspend fun existsById(id: OrderId): Boolean = withContext(ioDispatcher) {
        orderJpaRepository.existsByOrderId(id.value)
    }

    /**
     * Checks if an order exists by external order ID.
     *
     * @param externalOrderId the external order ID from upstream system
     * @return true if an order with this external ID already exists
     */
    override suspend fun existsByExternalOrderId(externalOrderId: UUID): Boolean = withContext(ioDispatcher) {
        orderJpaRepository.existsByExternalOrderId(externalOrderId)
    }

    /**
     * Counts the total number of orders in the repository.
     *
     * @return total count of orders
     */
    override suspend fun count(): Long = withContext(ioDispatcher) {
        orderJpaRepository.count()
    }
}
