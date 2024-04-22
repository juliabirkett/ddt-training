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
                Product(id = 0, description = "headphones"),
                Product(id = 1, description = "keyboard"),
                Product(id = 2, description = "cigarettes"),
            )
        )

        theCustomer.canSeeProductsCatalog(listOf(0, 1, 2))
        theCustomer.canBuy(productId = 0)
    }
}