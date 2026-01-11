package io.github.balaelangovan.orders.infra.config

import io.github.balaelangovan.orders.domain.mapper.OrderEventMapper
import io.github.balaelangovan.orders.domain.port.outbound.NotificationPort
import io.github.balaelangovan.orders.domain.port.outbound.OrderRepositoryPort
import io.github.balaelangovan.orders.domain.port.outbound.ReleaseSnapshotRepositoryPort
import io.github.balaelangovan.orders.domain.port.outbound.ShipmentSnapshotRepositoryPort
import io.github.balaelangovan.orders.domain.service.OrderDomainService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Infrastructure configuration for the Order Management Service.
 * Provides beans that require manual configuration, keeping the domain layer free of framework annotations.
 *
 * The OrderDomainService implements all inbound ports:
 * - OrderManagementPort (REST API operations)
 * - OrderEventHandlerPort (Order creation events)
 * - ReleaseEventHandlerPort (Release snapshot events)
 * - ShipmentEventHandlerPort (Shipment snapshot events)
 *
 * Spring will automatically inject it when any of these interfaces are requested.
 */
@Configuration
class ApplicationConfig {

    /**
     * Configure the domain service as a bean without polluting the domain layer with annotations.
     * The domain service implements all inbound ports and coordinates with outbound ports.
     */
    @Bean
    fun orderDomainService(
        orderRepository: OrderRepositoryPort,
        notificationPort: NotificationPort,
        releaseSnapshotRepository: ReleaseSnapshotRepositoryPort,
        shipmentSnapshotRepository: ShipmentSnapshotRepositoryPort,
        orderEventMapper: OrderEventMapper,
    ): OrderDomainService = OrderDomainService(
        orderRepository = orderRepository,
        notificationPort = notificationPort,
        releaseSnapshotRepository = releaseSnapshotRepository,
        shipmentSnapshotRepository = shipmentSnapshotRepository,
        orderEventMapper = orderEventMapper,
    )
}
