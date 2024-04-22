package com.store

import org.junit.jupiter.api.Test

class StoreDdts {
    private val scenario = newTestScenario(TestScenarioConfig.InMemory)

    @Test
    fun `a customer can buy a product that exists in the stock`() {
        val theCustomer = scenario.newCustomer()
        val theStoreManager = scenario.newManager()

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

        theStoreManager.canRegisterProductArrival(
            listOf(
                Product(id = 0, description = "headphones", quantity = 13),
                Product(id = 1, description = "keyboard", quantity = 1),
                Product(id = 2, description = "cigarettes", quantity = 20),
            )
        )

        theQuickCustomer.canBuy(productId = 1)
        theLateCustomer.cannotBuy(productId = 1)
    }
}