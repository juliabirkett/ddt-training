package com.store

import com.store.cli.customerCliApp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows

abstract class Customer {
    abstract fun canBuy(productId: Int)
    abstract fun cannotBuy(productId: Int)
    abstract fun canSeeProductsCatalog(productIds: List<Int>)
}

class InMemoryCustomer(private val hub: StoreAppHub) : Customer() {
    override fun canBuy(productId: Int) {
        hub.buy(productId)
    }

    override fun cannotBuy(productId: Int) {
        assertThrows<ProductIsOutOfStock> { hub.buy(productId) }
    }

    override fun canSeeProductsCatalog(productIds: List<Int>) {
        assertEquals(productIds, hub.catalog().map { it.id })
    }
}

class CliCustomer(repository: StorageRepository) : Customer() {
    private val app by lazy {
        customerCliApp(repository = repository)
    }

    override fun canBuy(productId: Int) {
        interactWithSystemIn(productId.toString()) { app }
    }

    override fun cannotBuy(productId: Int) {
        assertThrows<ProductIsOutOfStock> { canBuy(productId) }
    }

    override fun canSeeProductsCatalog(productIds: List<Int>) {
        val output = captureSystemOut { app }

        productIds.forEach { id ->
            assertTrue(output.contains(id.toString()))
        }
    }
}
