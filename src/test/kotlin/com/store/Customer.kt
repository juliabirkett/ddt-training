package com.store

import com.github.michaelbull.result.Err
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.contains
import com.natpryce.hamkrest.equalTo
import com.store.cli.customerCliApp
import org.junit.jupiter.api.Assertions.assertEquals

abstract class Customer {
    abstract fun canBuy(productId: Int)
    abstract fun cannotBuy(productId: Int, dueTo: ErrorCode)
    abstract fun canSeeProductsCatalog(productIds: List<Int>)
}

class InMemoryCustomer(private val hub: StoreAppHub) : Customer() {
    override fun canBuy(productId: Int) {
        hub.buy(productId)
    }

    override fun cannotBuy(productId: Int, dueTo: ErrorCode) {
        assertThat(hub.buy(productId), equalTo(Err(dueTo)))
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

    override fun cannotBuy(productId: Int, dueTo: ErrorCode) {
        val output = captureSystemOut { canBuy(productId) }

        assertThat(output, contains(Regex(dueTo.message)))
    }

    override fun canSeeProductsCatalog(productIds: List<Int>) {
        val output = captureSystemOut { app }

        productIds.forEach { id ->
            assertThat(output, contains(Regex(id.toString())))
        }
    }
}
