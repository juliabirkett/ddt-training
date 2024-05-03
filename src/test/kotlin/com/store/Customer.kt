package com.store

import com.github.michaelbull.result.Err
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.contains
import com.natpryce.hamkrest.equalTo
import com.store.cli.app
import java.time.LocalDate

abstract class Customer {
    abstract fun canBuy(productId: Int)
    abstract fun cannotBuy(productId: Int, dueTo: ErrorCode)
    abstract fun canSeeProductsCatalog(productIds: List<Int>)
    abstract fun logsIn(password: String, birthday: LocalDate)
    abstract fun cannotSeeProductsCatalog(dueTo: ErrorCode)
}

class InMemoryCustomer(private val hub: CustomerAppHub) : Customer() {
    override fun canBuy(productId: Int) {
        assertThat(hub.buy(productId), wasSuccessful)
    }

    override fun cannotBuy(productId: Int, dueTo: ErrorCode) {
        assertThat(hub.buy(productId), equalTo(Err(dueTo)))
    }

    override fun canSeeProductsCatalog(productIds: List<Int>) {
        assertThat(
            hub.catalog().value.map { it.id },
            equalTo(productIds)
        )
    }

    override fun logsIn(password: String, birthday: LocalDate) {
        assertThat(hub.logIn(password, birthday), wasSuccessful)
    }

    override fun cannotSeeProductsCatalog(dueTo: ErrorCode) {
        assertThat(hub.catalog(), equalTo(Err(dueTo)))
    }
}

class CliCustomer(
    private val storage: StorageRepository,
    private val userManager: UserManagerRepository,
) : Customer() {
    override fun canBuy(productId: Int) {
        val output = captureSystemOut {
            interactWithSystemIn("buy $productId") { app(storage, userManager) }
        }

        assertThat(output, contains(Regex("Product bought! $productId")))
    }

    override fun cannotBuy(productId: Int, dueTo: ErrorCode) {
        val output = captureSystemOut {
            interactWithSystemIn("buy $productId") { app(storage, userManager) }
        }

        assertThat(output, contains(Regex(dueTo.message)))
    }

    override fun canSeeProductsCatalog(productIds: List<Int>) {
        val output = captureSystemOut {
            interactWithSystemIn("show-catalog") { app(storage, userManager) }
        }

        productIds.forEach { id ->
            assertThat(output, contains(Regex(id.toString())))
        }
    }

    override fun logsIn(password: String, birthday: LocalDate) {
        val output = captureSystemOut {
            interactWithSystemIn("customer-login $password,$birthday") { app(storage, userManager) }
        }

        assertThat(output, contains(Regex("Logged in successfully!")))
    }

    override fun cannotSeeProductsCatalog(dueTo: ErrorCode) {
        val output = captureSystemOut {
            interactWithSystemIn("show-catalog") { app(storage, userManager) }
        }

        assertThat(output, contains(Regex(dueTo.message)))
    }
}
