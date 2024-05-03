package com.store

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class StoreDdts {
    @AfterEach
    fun tearDownDb() {
        InMemoryStorageRepository.cleanup()
        InMemoryUserManagerRepository.cleanup()
    }

    @Test
    fun `a manager that is not logged in cannot register products`() {
        val theStoreManager = scenario.newManager()

        theStoreManager.cannotRegisterProducts(NotAuthenticated)
    }

    @Test
    fun `a customer that is not logged in cannot see the catalog`() {
        val theCustomer = scenario.newCustomer()

        theCustomer.cannotSeeProductsCatalog(NotAuthenticated)
    }

    @Test
    fun `a customer can buy a product that exists in the stock`() {
        val theCustomer = scenario.newCustomer()
        val theStoreManager = scenario.newManager()
        theStoreManager.logsIn("admin123")
        theCustomer.logsIn("customer123", LocalDate.parse("1990-02-20"))

        theStoreManager.canRegisterProductArrival(
            listOf(
                Product(id = 0, description = "headphones", quantity = 13),
                Product(id = 1, description = "keyboard", quantity = 13),
                Product(id = 2, description = "pillow", quantity = 13),
            )
        )

        theCustomer.canSeeProductsCatalog(listOf(0, 1, 2))
        theCustomer.canBuy(productId = 0)
    }

    @Test
    fun `an under-aged customer can not even see adult products in the catalog`() {
        val underagedCustomer = scenario.newCustomer()
        val adultCustomer = scenario.newCustomer()
        val manager = scenario.newManager()
        underagedCustomer.logsIn("customer123", LocalDate.parse("2019-02-20"))
        manager.logsIn("admin123")
        manager.canRegisterProductArrival(
            listOf(
                Product(id = 1, description = "sneakers", quantity = 46),
                Product(id = 2, description = "cigarettes", quantity = 20),
                Product(id = 3, description = "headphones", quantity = 10),
            )
        )

        underagedCustomer.canSeeProductsCatalog(listOf(1, 3))
        adultCustomer.logsIn("customer123", LocalDate.parse("1987-09-01"))
        adultCustomer.canSeeProductsCatalog(listOf(1, 2 ,3))
    }

    @Test
    fun `a customer can not buy a product that doesn't have enough stock`() {
        val theQuickCustomer = scenario.newCustomer()
        val theLateCustomer = scenario.newCustomer()
        val theStoreManager = scenario.newManager()
        theQuickCustomer.logsIn("customer123", LocalDate.parse("1990-02-20"))
        theLateCustomer.logsIn("customer123", LocalDate.parse("1999-12-23"))
        theStoreManager.logsIn("admin123")

        theStoreManager.canRegisterProductArrival(
            listOf(
                Product(id = 0, description = "headphones", quantity = 13),
                Product(id = 1, description = "keyboard", quantity = 1),
                Product(id = 2, description = "pillow", quantity = 20),
            )
        )

        theQuickCustomer.canBuy(productId = 1)
        theLateCustomer.cannotBuy(productId = 1, dueTo = ProductIsOutOfStock)
    }

    @Test
    fun `an under-aged customer can not buy a product considered for adults`() {
        val underagedCustomer = scenario.newCustomer()
        val adultCustomer = scenario.newCustomer()
        val manager = scenario.newManager()
        underagedCustomer.logsIn("customer123", LocalDate.parse("2019-02-20"))
        manager.logsIn("admin123")
        manager.canRegisterProductArrival(
            listOf(
                Product(id = 2, description = "cigarettes", quantity = 20),
            )
        )

        underagedCustomer.cannotBuy(productId = 2, dueTo = ProductForAdultsOnly)
        adultCustomer.logsIn("customer123", LocalDate.parse("1987-09-01"))
        adultCustomer.canBuy(productId = 2)
    }

    @Test
    fun `a customer can not buy a product that wasn't registered`() {
        val customer = scenario.newCustomer()
        customer.logsIn("customer123", LocalDate.parse("1990-02-20"))

        customer.cannotBuy(1, dueTo = ProductNotFound)
    }
}