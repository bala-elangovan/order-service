package io.github.balaelangovan.orders.domain.valueobject

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ChannelTest :
    DescribeSpec({

        describe("Channel properties") {
            it("should have correct prefixes") {
                Channel.WEB.prefix shouldBe 10
                Channel.MOBILE.prefix shouldBe 20
                Channel.API.prefix shouldBe 30
                Channel.POS.prefix shouldBe 40
                Channel.CALL_CENTER.prefix shouldBe 50
            }

            it("should have descriptions") {
                Channel.WEB.description shouldBe "Web - Desktop Browser"
                Channel.MOBILE.description shouldBe "Mobile - iOS/Android App"
                Channel.API.description shouldBe "API - Third-party Integration"
                Channel.POS.description shouldBe "POS - Point of Sale"
                Channel.CALL_CENTER.description shouldBe "Call Center - Phone Orders"
            }
        }

        describe("Channel fromPrefix") {
            it("should find channel by prefix") {
                Channel.fromPrefix(10) shouldBe Channel.WEB
                Channel.fromPrefix(20) shouldBe Channel.MOBILE
                Channel.fromPrefix(30) shouldBe Channel.API
                Channel.fromPrefix(40) shouldBe Channel.POS
                Channel.fromPrefix(50) shouldBe Channel.CALL_CENTER
            }

            it("should throw for unknown prefix") {
                val exception = shouldThrow<IllegalArgumentException> {
                    Channel.fromPrefix(99)
                }
                exception.message shouldContain "Unknown channel prefix"
            }
        }

        describe("Channel fromNameOrDefault") {
            it("should find channel by name") {
                Channel.fromNameOrDefault("WEB") shouldBe Channel.WEB
                Channel.fromNameOrDefault("MOBILE") shouldBe Channel.MOBILE
                Channel.fromNameOrDefault("API") shouldBe Channel.API
                Channel.fromNameOrDefault("POS") shouldBe Channel.POS
                Channel.fromNameOrDefault("CALL_CENTER") shouldBe Channel.CALL_CENTER
            }

            it("should return default for null name") {
                Channel.fromNameOrDefault(null) shouldBe Channel.API
            }

            it("should return default for unknown name") {
                Channel.fromNameOrDefault("UNKNOWN") shouldBe Channel.API
            }

            it("should use custom default") {
                Channel.fromNameOrDefault("UNKNOWN", Channel.WEB) shouldBe Channel.WEB
            }

            it("should be case sensitive") {
                Channel.fromNameOrDefault("web") shouldBe Channel.API // lowercase doesn't match
            }
        }
    })
