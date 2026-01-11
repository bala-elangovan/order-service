package io.github.balaelangovan.orders.domain.mapper

/**
 * Base mapper interface for bidirectional conversion between domain objects and other representations.
 *
 * @param D Domain type (aggregate, entity, value object)
 * @param T Target type (DTO, Entity, etc.)
 *
 * All mappers implement this interface with bidirectional mapping support.
 * If a mapper doesn't support one direction, it should throw UnsupportedOperationException.
 *
 * Usage examples:
 * - RequestMapper: CreateOrderRequest → Order (toDomain supported, fromDomain throws exception)
 * - PersistenceMapper: Order ↔ OrderEntity (both directions supported)
 * - ResponseMapper: Order → OrderResponse (fromDomain supported, toDomain throws exception)
 */
interface DomainMapper<D, T> {

    /**
     * Converts target type (DTO/Entity) to domain object.
     * Used by: Request Mappers, Persistence Mappers (entity → domain).
     *
     * @param source the source object to convert from
     * @return domain object of type D
     * @throws UnsupportedOperationException if this mapper doesn't support this direction
     */
    fun toDomain(source: T): D

    /**
     * Converts domain object to target type (DTO/Entity).
     * Used by: Response Mappers, Persistence Mappers (domain → entity).
     *
     * @param domain the domain object to convert from
     * @return target object of type T
     * @throws UnsupportedOperationException if this mapper doesn't support this direction
     */
    fun fromDomain(domain: D): T
}
