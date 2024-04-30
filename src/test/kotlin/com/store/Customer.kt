package com.store

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.map
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.contains
import com.natpryce.hamkrest.equalTo
import com.store.cli.customerCliApp
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.LocalDate

abstract class Customer {
    abstract fun canBuy(productId: Int)
    abstract fun canBuy(productId: Int, customerAge: Int)
    abstract fun cannotBuy(productId: Int, dueTo: ErrorCode)
    abstract fun canSeeProductsCatalog(productIds: List<Int>)
    abstract fun logsIn(password: String, birthday: LocalDate)
    abstract fun cannotSeeProductsCatalog(dueTo: ErrorCode)
}

class InMemoryCustomer(private val hub: StoreAppHub) : Customer() {
    override fun canBuy(productId: Int) {
        assertThat(hub.buy(productId), wasSuccessful)
    }

    override fun canBuy(productId: Int, customerAge: Int) {
        assertThat(hub.buy(productId, customerAge), wasSuccessful)
    }

    override fun cannotBuy(productId: Int, dueTo: ErrorCode) {
        assertThat(hub.buy(productId), equalTo(Err(dueTo)))
    }

    override fun canSeeProductsCatalog(productIds: List<Int>) {
        hub.catalog().map { products ->
            assertEquals(productIds, products.map { it.id })
        }
    }

    override fun logsIn(password: String, birthday: LocalDate) {
        assertThat(hub.logInAsACustomer(password, birthday), wasSuccessful)
    }

    override fun cannotSeeProductsCatalog(dueTo: ErrorCode) {
        assertThat(hub.catalog(), equalTo(Err(dueTo)))
    }
}

class CliCustomer(repository: StorageRepository) : Customer() {
    private val app by lazy {
        customerCliApp(repository = repository)
    }

    override fun canBuy(productId: Int) {
        interactWithSystemIn("${productId}\n10\n") { app }
    }

    override fun canBuy(productId: Int, customerAge: Int) {
        interactWithSystemIn("$productId\n$customerAge\n") { app }
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

    override fun logsIn(password: String, birthday: LocalDate) {
        TODO("Not yet implemented")
    }

    override fun cannotSeeProductsCatalog(dueTo: ErrorCode) {
        TODO("Not yet implemented")
    }
}
