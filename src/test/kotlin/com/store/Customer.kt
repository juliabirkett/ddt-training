package com.store

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.contains
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import com.store.cli.customerCliApp
import org.junit.jupiter.api.Assertions.assertEquals

abstract class Customer {
    abstract fun canBuy(productId: Int)
    abstract fun cannotBuy(productId: Int, dueTo: Throwable)
    abstract fun canSeeProductsCatalog(productIds: List<Int>)
}

class InMemoryCustomer(private val hub: StoreAppHub) : Customer() {
    override fun canBuy(productId: Int) {
        hub.buy(productId)
    }

    override fun cannotBuy(productId: Int, dueTo: Throwable) {
        assertThat({ hub.buy(productId) }, throws(equalTo(dueTo)))
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

    override fun cannotBuy(productId: Int, dueTo: Throwable) {
        assertThat({ canBuy(productId) }, throws(equalTo(dueTo)))
    }

    override fun canSeeProductsCatalog(productIds: List<Int>) {
        val output = captureSystemOut { app }

        productIds.forEach { id ->
            assertThat(output, contains(Regex(id.toString())))
        }
    }
}
