package io.github.balaelangovan.orders.domain.valueobject

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class AddressTest :
    DescribeSpec({

        describe("Address creation") {
            it("should create valid address with all fields") {
                val address = Address(
                    fullName = "John Doe",
                    addressLine1 = "123 Main Street",
                    addressLine2 = "Apt 4B",
                    city = "New York",
                    stateProvince = "NY",
                    postalCode = "10001",
                    country = "USA",
                    phoneNumber = "+1-555-123-4567",
                    email = "john.doe@example.com",
                )

                address.fullName shouldBe "John Doe"
                address.addressLine1 shouldBe "123 Main Street"
                address.addressLine2 shouldBe "Apt 4B"
                address.city shouldBe "New York"
                address.stateProvince shouldBe "NY"
                address.postalCode shouldBe "10001"
                address.country shouldBe "USA"
                address.phoneNumber shouldBe "+1-555-123-4567"
                address.email shouldBe "john.doe@example.com"
            }

            it("should create address with optional fields as null") {
                val address = Address(
                    fullName = "Jane Smith",
                    addressLine1 = "456 Oak Avenue",
                    addressLine2 = null,
                    city = "Los Angeles",
                    stateProvince = "CA",
                    postalCode = "90001",
                    country = "USA",
                    phoneNumber = null,
                    email = null,
                )

                address.addressLine2 shouldBe null
                address.phoneNumber shouldBe null
                address.email shouldBe null
            }

            it("should create address using of() factory method") {
                val address = Address.of(
                    fullName = "Bob Wilson",
                    addressLine1 = "789 Pine Lane",
                    addressLine2 = null,
                    city = "Chicago",
                    stateProvince = "IL",
                    postalCode = "60601",
                    country = "USA",
                )

                address.fullName shouldBe "Bob Wilson"
                address.city shouldBe "Chicago"
            }
        }

        describe("Address validation - required fields") {
            it("should fail for blank full name") {
                val exception = shouldThrow<IllegalArgumentException> {
                    Address(
                        fullName = "",
                        addressLine1 = "123 Main Street",
                        addressLine2 = null,
                        city = "New York",
                        stateProvince = "NY",
                        postalCode = "10001",
                        country = "USA",
                        phoneNumber = null,
                        email = null,
                    )
                }
                exception.message shouldContain "Full name cannot be blank"
            }

            it("should fail for blank address line 1") {
                val exception = shouldThrow<IllegalArgumentException> {
                    Address(
                        fullName = "John Doe",
                        addressLine1 = "   ",
                        addressLine2 = null,
                        city = "New York",
                        stateProvince = "NY",
                        postalCode = "10001",
                        country = "USA",
                        phoneNumber = null,
                        email = null,
                    )
                }
                exception.message shouldContain "Address line 1 cannot be blank"
            }

            it("should fail for blank city") {
                val exception = shouldThrow<IllegalArgumentException> {
                    Address(
                        fullName = "John Doe",
                        addressLine1 = "123 Main Street",
                        addressLine2 = null,
                        city = "",
                        stateProvince = "NY",
                        postalCode = "10001",
                        country = "USA",
                        phoneNumber = null,
                        email = null,
                    )
                }
                exception.message shouldContain "City cannot be blank"
            }

            it("should fail for blank state/province") {
                val exception = shouldThrow<IllegalArgumentException> {
                    Address(
                        fullName = "John Doe",
                        addressLine1 = "123 Main Street",
                        addressLine2 = null,
                        city = "New York",
                        stateProvince = "",
                        postalCode = "10001",
                        country = "USA",
                        phoneNumber = null,
                        email = null,
                    )
                }
                exception.message shouldContain "State/Province cannot be blank"
            }

            it("should fail for blank postal code") {
                val exception = shouldThrow<IllegalArgumentException> {
                    Address(
                        fullName = "John Doe",
                        addressLine1 = "123 Main Street",
                        addressLine2 = null,
                        city = "New York",
                        stateProvince = "NY",
                        postalCode = "   ",
                        country = "USA",
                        phoneNumber = null,
                        email = null,
                    )
                }
                exception.message shouldContain "Postal code cannot be blank"
            }

            it("should fail for blank country") {
                val exception = shouldThrow<IllegalArgumentException> {
                    Address(
                        fullName = "John Doe",
                        addressLine1 = "123 Main Street",
                        addressLine2 = null,
                        city = "New York",
                        stateProvince = "NY",
                        postalCode = "10001",
                        country = "",
                        phoneNumber = null,
                        email = null,
                    )
                }
                exception.message shouldContain "Country cannot be blank"
            }
        }

        describe("Address validation - email format") {
            it("should fail for invalid email without @") {
                val exception = shouldThrow<IllegalArgumentException> {
                    Address(
                        fullName = "John Doe",
                        addressLine1 = "123 Main Street",
                        addressLine2 = null,
                        city = "New York",
                        stateProvince = "NY",
                        postalCode = "10001",
                        country = "USA",
                        phoneNumber = null,
                        email = "invalid-email",
                    )
                }
                exception.message shouldContain "Invalid email format"
            }

            it("should accept valid email") {
                val address = Address(
                    fullName = "John Doe",
                    addressLine1 = "123 Main Street",
                    addressLine2 = null,
                    city = "New York",
                    stateProvince = "NY",
                    postalCode = "10001",
                    country = "USA",
                    phoneNumber = null,
                    email = "test@example.com",
                )

                address.email shouldBe "test@example.com"
            }

            it("should accept null email") {
                val address = Address(
                    fullName = "John Doe",
                    addressLine1 = "123 Main Street",
                    addressLine2 = null,
                    city = "New York",
                    stateProvince = "NY",
                    postalCode = "10001",
                    country = "USA",
                    phoneNumber = null,
                    email = null,
                )

                address.email shouldBe null
            }
        }

        describe("Address equality") {
            it("should be equal for same values") {
                val address1 = Address.of(
                    fullName = "John Doe",
                    addressLine1 = "123 Main Street",
                    addressLine2 = null,
                    city = "New York",
                    stateProvince = "NY",
                    postalCode = "10001",
                    country = "USA",
                )
                val address2 = Address.of(
                    fullName = "John Doe",
                    addressLine1 = "123 Main Street",
                    addressLine2 = null,
                    city = "New York",
                    stateProvince = "NY",
                    postalCode = "10001",
                    country = "USA",
                )

                (address1 == address2) shouldBe true
            }

            it("should not be equal for different values") {
                val address1 = Address.of(
                    fullName = "John Doe",
                    addressLine1 = "123 Main Street",
                    addressLine2 = null,
                    city = "New York",
                    stateProvince = "NY",
                    postalCode = "10001",
                    country = "USA",
                )
                val address2 = Address.of(
                    fullName = "Jane Doe",
                    addressLine1 = "123 Main Street",
                    addressLine2 = null,
                    city = "New York",
                    stateProvince = "NY",
                    postalCode = "10001",
                    country = "USA",
                )

                (address1 == address2) shouldBe false
            }
        }
    })
