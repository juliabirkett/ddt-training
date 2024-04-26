package com.store

import com.store.cli.InMemoryStorageRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class StoreDdts {
    @AfterEach
    fun tearDownDb() {
        InMemoryStorageRepository.cleanup()
    }

    @Test
    fun `a manager that is not logged in cannot register products`() {
        val theStoreManager = scenario.newManager()

        theStoreManager.cannotRegisterProducts(NotAuthenticatedAsManager())
    }

    @Test
    fun `a customer can buy a product that exists in the stock`() {
        val theCustomer = scenario.newCustomer()
        val theStoreManager = scenario.newManager()
        theStoreManager.needToLogIn("admin123")

        theStoreManager.canRegisterProductArrival(
            listOf(
                Product(id = 0, description = "headphones", quantity = 13),
                Product(id = 1, description = "keyboard", quantity = 13),
                Product(id = 2, description = "cigarettes", quantity = 13),
            )
        )

        theCustomer.canSeeProductsCatalog(listOf(0, 1, 2))
        theCustomer.canBuy(productId = 0)
    }

    @Test
    fun `a customer can not buy a product that doesn't have enough stock`() {
        val theQuickCustomer = scenario.newCustomer()
        val theLateCustomer = scenario.newCustomer()
        val theStoreManager = scenario.newManager()
        theStoreManager.needToLogIn("admin123")

        theStoreManager.canRegisterProductArrival(
            listOf(
                Product(id = 0, description = "headphones", quantity = 13),
                Product(id = 1, description = "keyboard", quantity = 1),
                Product(id = 2, description = "cigarettes", quantity = 20),
            )
        )

        theQuickCustomer.canBuy(productId = 1)
        theLateCustomer.cannotBuy(productId = 1, dueTo = ProductIsOutOfStock("ERROR! Product with id 1 is out of stock at the moment"))
    }

    @Test
    fun `a customer can not buy a product that wasn't registered`() {
        val customer = scenario.newCustomer()

        customer.cannotBuy(1, dueTo = ProductNotFound("ERROR! Product with id 1 wasn't found in the catalog"))
    }
}